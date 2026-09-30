#!/usr/bin/env python3
"""Parses every ChaosUtils source file with tree-sitter and reports syntax errors.

This is the fast pre-flight check before a build: it needs no JDK and no Minecraft jars,
but it catches the mistakes that make javac stop early (missing braces, a broken method
signature, a stray token after an edit).

Usage:  python3 tools/java_syntax_check.py [--all | --changed]
"""

import os
import subprocess
import sys

from tree_sitter import Language, Parser
import tree_sitter_java

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PARSER = Parser(Language(tree_sitter_java.language()))


def targets() -> list[str]:
    if "--all" in sys.argv:
        base = os.path.join(ROOT, "src", "main", "java")
        files = []
        for folder, _, names in os.walk(base):
            files += [os.path.join(folder, n) for n in names if n.endswith(".java")]
        return sorted(files)
    out = subprocess.run(["git", "status", "--porcelain"], cwd=ROOT, capture_output=True, text=True).stdout
    files = []
    for line in out.splitlines():
        path = line[3:].strip().strip('"')
        if path.endswith(".java"):
            files.append(os.path.join(ROOT, path))
    tracked = subprocess.run(["git", "ls-files", "*.java"], cwd=ROOT, capture_output=True, text=True).stdout
    for path in tracked.splitlines():
        full = os.path.join(ROOT, path)
        if full not in files:
            files.append(full)
    return sorted(files)


def walk(node, source: bytes, problems: list[str], path: str) -> None:
    if node.type == "ERROR" or node.is_missing:
        line, column = node.start_point
        snippet = source[node.start_byte:node.start_byte + 90].decode("utf-8", "replace").replace("\n", " ")
        problems.append(f"{os.path.relpath(path, ROOT)}:{line + 1}:{column + 1} "
                        f"{'missing ' + node.type if node.is_missing else 'syntax error'} -> {snippet.strip()}")
    for child in node.children:
        walk(child, source, problems, path)


def main() -> int:
    files = targets()
    problems: list[str] = []
    for path in files:
        if not os.path.exists(path):
            continue
        source = open(path, "rb").read()
        tree = PARSER.parse(source)
        walk(tree.root_node, source, problems, path)
    for problem in problems:
        print(problem)
    print(f"{len(files)} files parsed, {len(problems)} syntax problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
