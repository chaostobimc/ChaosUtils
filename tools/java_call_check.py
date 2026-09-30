#!/usr/bin/env python3
"""Checks the argument count of every call into ChaosUtils' own helper classes.

The helpers in ``dev.chaosutils.util`` and ``dev.chaosutils.gui`` are called hundreds of
times with long parameter lists (draw helpers, animation helpers). A wrong argument
count there is a javac error, and with no JDK in the development sandbox this script
finds them by parsing the sources with tree-sitter instead.

Only calls of the form ``Type.method(...)`` are checked, and only for the types listed
below, so the result is free of the noise a full semantic analysis would produce.

Usage:  python3 tools/java_call_check.py
"""

import os
import sys
from collections import defaultdict

from tree_sitter import Language, Parser
import tree_sitter_java

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java")

# Types whose static helpers are called from everywhere. Merged per simple name.
TARGETS = {
    "Ui", "UiIcons", "UiFonts", "UiWidgets", "UiModals", "UiTheme", "Render", "Anim",
    "Clipboard", "ChaosConfig", "Keybinds", "Keybinds", "ModuleManager", "TickClock",
    "HudPos", "SoundLookup", "ItemLookup", "EnchantLookup", "TpsEstimator", "ChatLog",
    "Features", "ScreenshotManager", "Projection", "SoundClasses", "InputUtil",
}

PARSER = Parser(Language(tree_sitter_java.language()))


def files():
    out = []
    for folder, _, names in os.walk(JAVA):
        out += [os.path.join(folder, n) for n in names if n.endswith(".java")]
    return sorted(out)


def text_of(node, source):
    return source[node.start_byte:node.end_byte].decode("utf-8", "replace")


def declared_constructors(tree, source):
    """class name -> set of constructor arities, or None when the class has no explicit one."""
    found = defaultdict(set)
    records = set()

    def visit(node):
        if node.type in ("class_declaration", "record_declaration", "enum_declaration"):
            name_node = node.child_by_field_name("name")
            if name_node is not None:
                name = text_of(name_node, source)
                found.setdefault(name, set())      # declared here - so it is one of ours
                if node.type == "record_declaration":
                    records.add(name)
                    params = node.child_by_field_name("parameters")
                    found[name].add(len(params.named_children) if params is not None else 0)
                body = node.child_by_field_name("body")
                if body is not None:
                    for child in body.named_children:
                        if child.type == "constructor_declaration":
                            params = child.child_by_field_name("parameters")
                            count = len(params.named_children) if params is not None else 0
                            varargs = params is not None and any(
                                "..." in text_of(p, source) for p in params.named_children)
                            found[name].add(-1 if varargs else count)
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return found


def declared_arities(tree, source):
    """method name -> set of parameter counts (-1 marks a varargs method)."""
    found = defaultdict(set)

    def visit(node):
        if node.type == "method_declaration":
            name = node.child_by_field_name("name")
            params = node.child_by_field_name("parameters")
            varargs = False
            count = 0
            if params is not None:
                count = len(params.named_children)
                for child in params.named_children:
                    if "..." in text_of(child, source):
                        varargs = True
            if name is not None:
                found[text_of(name, source)].add(-1 if varargs else count)
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return found


def main():
    parsed = {}
    for path in files():
        source = open(path, "rb").read()
        parsed[path] = (PARSER.parse(source), source)

    arities = defaultdict(set)
    constructors = defaultdict(set)
    has_class = defaultdict(bool)
    for path, (tree, source) in parsed.items():
        simple = os.path.basename(path)[:-5]
        if simple in TARGETS:
            for name, counts in declared_arities(tree, source).items():
                arities[name] |= counts
        for name, counts in declared_constructors(tree, source).items():
            constructors[name] |= counts
            has_class[name] = True

    def ctor_ok(name, count):
        if not has_class[name]:
            return True            # not one of our classes (a library type)
        allowed = constructors[name] or {0}
        return -1 in allowed or count in allowed

    problems = []
    for path, (tree, source) in parsed.items():
        def visit(node):
            if node.type == "method_invocation":
                obj = node.child_by_field_name("object")
                name_node = node.child_by_field_name("name")
                args = node.child_by_field_name("arguments")
                if obj is not None and name_node is not None and args is not None:
                    owner = text_of(obj, source)
                    if "." in owner:
                        owner = owner.rsplit(".", 1)[1]
                    name = text_of(name_node, source)
                    if owner in TARGETS and name in arities:
                        expected = arities[name]
                        actual = len(args.named_children)
                        if -1 not in expected and actual not in expected:
                            line = node.start_point[0] + 1
                            problems.append(f"{os.path.relpath(path, ROOT)}:{line} "
                                            f"{owner}.{name} called with {actual} argument(s), "
                                            f"declared with {sorted(expected)}")
            if node.type == "object_creation_expression":
                type_node = node.child_by_field_name("type")
                args = node.child_by_field_name("arguments")
                if type_node is not None and args is not None:
                    name = text_of(type_node, source)
                    if name.endswith("[]"):
                        pass
                    elif "(" in name or "." in name and name.rsplit(".", 1)[1][:1].islower():
                        pass
                    elif not ctor_ok(name, len(args.named_children)):
                        line = node.start_point[0] + 1
                        problems.append(f"{os.path.relpath(path, ROOT)}:{line} new {name}(...) with "
                                        f"{len(args.named_children)} argument(s), declared with "
                                        f"{sorted(constructors[name])} (and no default constructor)")
            for child in node.children:
                visit(child)

        visit(tree.root_node)

    for problem in sorted(set(problems)):
        print(problem)
    print(f"{len(parsed)} files scanned, {len(set(problems))} call problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
