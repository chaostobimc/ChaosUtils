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
    for path, (tree, source) in parsed.items():
        simple = os.path.basename(path)[:-5]
        if simple in TARGETS:
            for name, counts in declared_arities(tree, source).items():
                arities[name] |= counts

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
            for child in node.children:
                visit(child)

        visit(tree.root_node)

    for problem in sorted(set(problems)):
        print(problem)
    print(f"{len(parsed)} files scanned, {len(set(problems))} call problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
