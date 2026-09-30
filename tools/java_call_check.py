#!/usr/bin/env python3
"""Static pre-flight checks for the ChaosUtils sources.

Parsing the sources with tree-sitter finds the javac errors that would otherwise only show up
in a build the developer has to run:

1. **Argument counts** of every call ``Type.method(...)`` into ChaosUtils' own helper classes.
2. **Argument types**: a value of a known primitive type handed to a parameter that cannot take
   it (``float`` into an ``int`` parameter needs a cast - the classic slip when declaring a
   colour as ``float`` instead of ``int``).
3. **Constructor calls** ``new Type(...)`` with an argument count no constructor of the class
   accepts, including the implicit no-argument constructor.

The type of an argument is inferred from literals, in-file variable declarations, field
declarations, parameters and the return types of our own methods. A name declared with two
different kinds inside one file counts as unknown, so shadowing cannot produce a false alarm,
and anything that cannot be inferred is simply skipped.

Usage:  python3 tools/java_call_check.py
"""

import os
import sys
from collections import defaultdict

from tree_sitter import Language, Parser
import tree_sitter_java

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java")

# Types whose helpers are called from everywhere. Merged per simple name.
TARGETS = {
    "Ui", "UiIcons", "UiFonts", "UiWidgets", "UiModals", "UiTheme", "Render", "Anim",
    "Clipboard", "ChaosConfig", "Keybinds", "ModuleManager", "TickClock", "HudPos",
    "SoundLookup", "ItemLookup", "EnchantLookup", "TpsEstimator", "ChatLog", "Features",
    "ScreenshotManager", "Projection", "SoundClasses", "InputUtil", "Setting",
}

# Widening order of the Java primitives: a narrower target than the source does not compile.
NUMERIC_RANK = {"byte": 1, "short": 2, "char": 2, "int": 3, "long": 4, "float": 5, "double": 6}
PRIMITIVES = set(NUMERIC_RANK) | {"boolean"}
REFERENCE = "ref"
UNKNOWN = "?"

NUMERIC_LITERAL = {
    "decimal_integer_literal": "int",
    "hex_integer_literal": "int",
    "octal_integer_literal": "int",
    "binary_integer_literal": "int",
    "decimal_floating_point_literal": "double",
    "hex_floating_point_literal": "double",
    "character_literal": "char",
    "true": "boolean",
    "false": "boolean",
}

PARSER = Parser(Language(tree_sitter_java.language()))

# method name -> set of primitive return types, filled in by main()
RETURNS = defaultdict(set)


def text_of(node, source):
    return source[node.start_byte:node.end_byte].decode("utf-8", "replace")


def source_files():
    out = []
    for folder, _, names in os.walk(JAVA):
        out += [os.path.join(folder, n) for n in names if n.endswith(".java")]
    return sorted(out)


def kind_of_type(type_node, source):
    """Primitive name, REFERENCE or None."""
    if type_node is None:
        return None
    value = text_of(type_node, source).strip()
    return value if value in PRIMITIVES else REFERENCE


# --------------------------------------------------------------------------- declaration index
def declared_arities(tree, source):
    """method name -> set of parameter counts (-1 marks a varargs method)."""
    found = defaultdict(set)

    def visit(node):
        if node.type == "method_declaration":
            name = node.child_by_field_name("name")
            params = node.child_by_field_name("parameters")
            count = len(params.named_children) if params is not None else 0
            varargs = params is not None and any("..." in text_of(p, source) for p in params.named_children)
            if name is not None:
                found[text_of(name, source)].add(-1 if varargs else count)
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return found


def declared_returns(tree, source):
    """method name -> primitive return types; references are ignored."""
    found = defaultdict(set)

    def visit(node):
        if node.type == "method_declaration":
            name = node.child_by_field_name("name")
            kind = node.child_by_field_name("type")
            if name is not None and kind is not None:
                value = text_of(kind, source)
                if value in PRIMITIVES:
                    found[text_of(name, source)].add(value)
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return found


def declared_signatures(tree, source):
    """method name -> set of parameter kind tuples; varargs positions are marked with -1."""
    found = defaultdict(set)

    def visit(node):
        if node.type == "method_declaration":
            name = node.child_by_field_name("name")
            params = node.child_by_field_name("parameters")
            if name is not None and params is not None:
                kinds = []
                for param in params.named_children:
                    kind = kind_of_type(param.child_by_field_name("type"), source)
                    if kind is not None and "..." in text_of(param, source):
                        kind = -1
                    kinds.append(kind if kind is not None else REFERENCE)
                found[text_of(name, source)].add(tuple(kinds))
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return found


def declared_constructors(tree, source):
    """class/record name -> set of constructor arities; classes without one get {0}."""
    found = defaultdict(set)

    def visit(node):
        if node.type in ("class_declaration", "record_declaration", "enum_declaration"):
            name_node = node.child_by_field_name("name")
            if name_node is None:
                return
            name = text_of(name_node, source)
            found.setdefault(name, set())
            if node.type == "record_declaration":
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


# --------------------------------------------------------------------------- inference
def declared_kinds_in_file(tree, source):
    """name -> kind, only for names declared with exactly one kind in this file."""
    seen = defaultdict(set)

    def record(name_node, type_node):
        if name_node is None:
            return
        kind = kind_of_type(type_node, source)
        if kind is None:
            # 'var x = ...' or an untyped lambda parameter - remember the name as unknown
            seen[text_of(name_node, source)].add(UNKNOWN)
            return
        seen[text_of(name_node, source)].add(kind)

    def visit(node):
        if node.type in ("local_variable_declaration", "field_declaration", "formal_parameter",
                         "spread_parameter", "catch_formal_parameter"):
            type_node = node.child_by_field_name("type")
            for child in node.named_children:
                if child.type == "variable_declarator":
                    record(child.child_by_field_name("name"), type_node)
                elif child.type == "identifier" and node.type in ("formal_parameter", "spread_parameter"):
                    record(child, type_node)
        elif node.type == "resource" or node.type == "enhanced_for_statement":
            type_node = node.child_by_field_name("type")
            record(node.child_by_field_name("name"), type_node)
        for child in node.children:
            visit(child)

    visit(tree.root_node)
    return {name: next(iter(kinds)) for name, kinds in seen.items() if len(kinds) == 1}


def promote(left, right):
    """Result kind of an arithmetic expression, or UNKNOWN."""
    if left == UNKNOWN or right == UNKNOWN:
        return UNKNOWN
    if left == "double" or right == "double":
        return "double"
    if left == "float" or right == "float":
        return "float"
    if left == "long" or right == "long":
        return "long"
    if left in NUMERIC_RANK and right in NUMERIC_RANK:
        return "int"
    return UNKNOWN


def kind_of(node, source, locals_map):
    """Best effort kind of an expression; UNKNOWN when it cannot be inferred."""
    if node is None:
        return UNKNOWN
    if node.type in NUMERIC_LITERAL:
        kind = NUMERIC_LITERAL[node.type]
        if node.type.endswith("integer_literal") and text_of(node, source).lower().endswith("l"):
            return "long"
        if node.type.endswith("floating_point_literal"):
            text = text_of(node, source).lower()
            if text.endswith("f"):
                return "float"
            if text.endswith("d"):
                return "double"
        return kind
    if node.type == "identifier":
        return locals_map.get(text_of(node, source), UNKNOWN)
    if node.type in ("parenthesized_expression", "unary_expression"):
        for child in node.named_children:
            return kind_of(child, source, locals_map)
        return UNKNOWN
    if node.type == "cast_expression":
        return kind_of_type(node.child_by_field_name("type"), source)
    if node.type == "binary_expression":
        children = node.named_children
        if len(children) == 2:
            operator = text_of(node.child_by_field_name("operator") or node, source)
            if operator.strip() in ("<", ">", "<=", ">=", "==", "!=", "&&", "||"):
                return "boolean"
            return promote(kind_of(children[0], source, locals_map), kind_of(children[1], source, locals_map))
        return UNKNOWN
    if node.type == "ternary_expression":
        parts = node.named_children
        if len(parts) == 3:
            left = kind_of(parts[1], source, locals_map)
            right = kind_of(parts[2], source, locals_map)
            if left == right:
                return left
            return promote(left, right)
        return UNKNOWN
    if node.type == "method_invocation":
        # Only our own classes are indexed by name; a bare Math.max(...) has no receiver and must
        # not be resolved against an unrelated method of ours that happens to share the name.
        owner = node.child_by_field_name("object")
        name = node.child_by_field_name("name")
        if owner is not None and name is not None:
            owner_name = text_of(owner, source)
            if "." in owner_name:
                owner_name = owner_name.rsplit(".", 1)[1]
            if owner_name in TARGETS:
                kinds = RETURNS.get(text_of(name, source))
                if kinds and len(kinds) == 1:
                    return next(iter(kinds))
        return UNKNOWN
    if node.type == "object_creation_expression":
        return REFERENCE
    return UNKNOWN


def accepts(param, argument):
    """Whether a parameter of kind ``param`` takes a value of kind ``argument``."""
    if param == UNKNOWN or argument == UNKNOWN or param == REFERENCE or argument == REFERENCE:
        return True
    if param == -1:                    # varargs takes the element type
        return True
    if param == argument:
        return True
    if param in NUMERIC_RANK and argument in NUMERIC_RANK:
        return NUMERIC_RANK[param] >= NUMERIC_RANK[argument]
    return False


# --------------------------------------------------------------------------- checks
def report(problems, path, node, message):
    problems.append(f"{os.path.relpath(path, ROOT)}:{node.start_point[0] + 1} {message}")


def check_file(path, source, tree, arities, signatures, constructors, problems):
    locals_map = declared_kinds_in_file(tree, source)

    def visit(node, enclosing_return=None):
        if node.type == "method_declaration":
            kind = node.child_by_field_name("type")
            value = text_of(kind, source) if kind is not None else None
            enclosing_return = value if value in PRIMITIVES else None

        if node.type == "method_invocation":
            owner = node.child_by_field_name("object")
            owner_name = text_of(owner, source) if owner is not None else None
            if owner_name and "." in owner_name:
                owner_name = owner_name.rsplit(".", 1)[1]
            name_node = node.child_by_field_name("name")
            args = node.child_by_field_name("arguments")
            if owner_name in TARGETS and name_node is not None and args is not None:
                name = text_of(name_node, source)
                actual = list(args.named_children)

                expected = arities.get(name)
                if expected and -1 not in expected and len(actual) not in expected:
                    report(problems, path, node, f"{owner_name}.{name}() called with {len(actual)} "
                                                  f"argument(s), declared with {sorted(expected)}")

                overloads = signatures.get(name)
                if overloads:
                    kinds = [kind_of(arg, source, locals_map) for arg in actual]
                    if any(kind != UNKNOWN for kind in kinds):
                        viable = [sig for sig in overloads if overload_accepts(sig, kinds)]
                        if not viable:
                            index = first_mismatch(overloads, kinds)
                            report(problems, path, node,
                                   f"{owner_name}.{name}(...): argument {index + 1} has kind "
                                   f"{kinds[index]}, but the parameter is "
                                   f"{'/'.join(sorted({str(sig[index]) for sig in overloads if index < len(sig)}))}")

        if node.type == "object_creation_expression":
            type_node = node.child_by_field_name("type")
            args = node.child_by_field_name("arguments")
            if type_node is not None and args is not None:
                name = text_of(type_node, source)
                if name in constructors and args.named_children:
                    allowed = constructors[name] or {0}
                    count = len(args.named_children)
                    if -1 not in allowed and count not in allowed:
                        report(problems, path, node, f"new {name}(...) with {count} argument(s), declared "
                                                      f"with {sorted(allowed)} and no default constructor")

        if node.type == "return_statement" and enclosing_return in NUMERIC_RANK:
            for child in node.named_children:
                kind = kind_of(child, source, locals_map)
                if kind != UNKNOWN and kind != REFERENCE and not accepts(enclosing_return, kind):
                    report(problems, path, child, f"returns {kind}, but the method declares {enclosing_return}")

        for child in node.children:
            visit(child, enclosing_return)

    visit(tree.root_node)


def overload_accepts(signature, kinds):
    if len(signature) != len(kinds):
        return False
    return all(accepts(signature[index], kinds[index]) for index in range(len(kinds)))


def first_mismatch(overloads, kinds):
    """Position of an argument no overload can take - used for the error message."""
    for index, argument in enumerate(kinds):
        if argument == UNKNOWN:
            continue
        if all(len(sig) > index and not accepts(sig[index], argument) for sig in overloads
               if len(sig) == len(kinds)):
            return index
    return 0



def type_index(parsed):
    """class name -> {'members': set, 'nested': set, 'supers': set} for every own class."""
    index = defaultdict(lambda: {"members": set(), "nested": set(), "supers": set()})

    for path, (tree, source) in parsed.items():
        stack = []

        def add_members(node, owner):
            for child in node.named_children:
                if child.type in ("method_declaration", "constructor_declaration"):
                    name = child.child_by_field_name("name")
                    if name is not None:
                        owner["members"].add(text_of(name, source))
                elif child.type in ("field_declaration", "constant_declaration"):
                    for decl in child.named_children:
                        if decl.type == "variable_declarator":
                            name = decl.child_by_field_name("name")
                            if name is not None:
                                owner["members"].add(text_of(name, source))
                elif child.type == "enum_constant":
                    name = child.child_by_field_name("name")
                    if name is not None:
                        owner["members"].add(text_of(name, source))
                elif child.type == "enum_body_declarations":
                    add_members(child, owner)

        def visit(node, current=None):
            if node.type in ("class_declaration", "interface_declaration", "enum_declaration",
                             "record_declaration", "annotation_type_declaration"):
                name_node = node.child_by_field_name("name")
                if name_node is not None:
                    name = text_of(name_node, source)
                    entry = index[name]
                    if current is not None:
                        index[current]["nested"].add(name)
                    body = node.child_by_field_name("body")
                    if body is not None:
                        add_members(body, entry)
                    # supertypes
                    supers = node.child_by_field_name("superclass")
                    if supers is not None:
                        entry["supers"].add(text_of(supers, source).split("<")[0].strip())
                    interfaces = node.child_by_field_name("interfaces")
                    if interfaces is not None:
                        for piece in text_of(interfaces, source).replace("implements", " ").split(","):
                            cleaned = piece.strip().split("<")[0].strip()
                            if cleaned:
                                entry["supers"].add(cleaned)
                    # enums carry the implicit values()/valueOf() helpers
                    if node.type == "enum_declaration":
                        entry["members"].add("values")
                        entry["members"].add("valueOf")
                    # records expose their components as accessors
                    if node.type == "record_declaration":
                        params = node.child_by_field_name("parameters")
                        if params is not None:
                            for param in params.named_children:
                                param_name = param.child_by_field_name("name")
                                if param_name is not None:
                                    entry["members"].add(text_of(param_name, source))
                                    entry["members"].add(text_of(param_name, source) + "()")
                    for child in node.children:
                        visit(child, name)
                    return
            for child in node.children:
                visit(child, current)

        visit(tree.root_node)
    return index


def members_of(index, name, depth=0):
    """Members of a class including everything it inherits (bounded recursion)."""
    if name not in index or depth > 4:
        return set()
    entry = index[name]
    members = set(entry["members"]) | set(entry["nested"])
    for super_name in entry["supers"]:
        members |= members_of(index, super_name.split(".")[-1], depth + 1)
    return members


def check_own_members(parsed, problems):
    """Flags ``Type.member`` chains whose member does not exist on one of our own classes."""
    index = type_index(parsed)
    own = set(index)

    for path, (tree, source) in parsed.items():
        def visit(node):
            segments = None
            if node.type == "method_invocation":
                owner = node.child_by_field_name("object")
                name = node.child_by_field_name("name")
                if owner is not None and name is not None and owner.type == "identifier":
                    segments = [text_of(owner, source), text_of(name, source)]
            elif node.type == "field_access":
                obj = node.child_by_field_name("object")
                field = node.child_by_field_name("field")
                if obj is not None and field is not None and obj.type == "identifier":
                    segments = [text_of(obj, source), text_of(field, source)]
            if segments and segments[0] in own:
                current = segments[0]
                for segment in segments[1:]:
                    if segment not in members_of(index, current):
                        report(problems, path, node,
                               f"{'.'.join(segments)}: '{segment}' does not exist on {current}")
                        break
                    current = segment if segment in index else current
            for child in node.children:
                visit(child)

        visit(tree.root_node)

def main():
    parsed = {}
    for path in source_files():
        source = open(path, "rb").read()
        parsed[path] = (PARSER.parse(source), source)

    arities = defaultdict(set)
    signatures = defaultdict(set)
    returns = defaultdict(set)
    constructors = defaultdict(set)
    for path, (tree, source) in parsed.items():
        simple = os.path.basename(path)[:-5]
        if simple in TARGETS:
            for name, counts in declared_arities(tree, source).items():
                arities[name] |= counts
            for name, sigs in declared_signatures(tree, source).items():
                signatures[name] |= sigs
            for name, kinds in declared_returns(tree, source).items():
                returns[name] |= kinds
        for name, counts in declared_constructors(tree, source).items():
            constructors[name] |= counts

    RETURNS.clear()
    RETURNS.update(returns)

    problems = []
    for path, (tree, source) in parsed.items():
        check_file(path, source, tree, arities, signatures, constructors, problems)
    check_own_members(parsed, problems)

    for problem in sorted(set(problems)):
        print(problem)
    print(f"{len(parsed)} files scanned, {len(set(problems))} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
