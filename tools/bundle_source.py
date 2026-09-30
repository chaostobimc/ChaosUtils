#!/usr/bin/env python3
"""Regenerates docs/ChaosUtils-source.md: every source file of this repo in one document.

Usage:  python3 tools/bundle_source.py
"""

import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
J = "src/main/java/dev/chaosutils/"


def ls(package):
    return sorted(J + package + "/" + name for name in os.listdir(os.path.join(ROOT, J + package)))


GROUPS = [
    ("Build & metadata", [
        "build.gradle", "settings.gradle", "gradle.properties",
        "gradle/wrapper/gradle-wrapper.properties", "LICENSE", ".gitignore",
        "src/main/resources/fabric.mod.json",
        "src/main/resources/chaosutils.mixins.json",
        "src/main/resources/assets/chaosutils/lang/en_us.json",
    ]),
    ("Entrypoint", [J + "ChaosUtils.java"]),
    ("Config", ls("config")),
    ("Core", ls("core")),
    ("Utility", ls("util")),
    ("Feature framework", [
        J + "feature/Feature.java",
        J + "feature/Features.java",
        J + "feature/FeatureRegistry.java",
    ]),
    ("Radial menu", ls("feature/radial")),
    ("Click GUI", ls("gui")),
    ("HUD features", ls("feature/hud")),
    ("Visual features", ls("feature/visual")),
    ("Inventory features", ls("feature/inventory")),
    ("Chat features", ls("feature/chat")),
    ("Audio features", ls("feature/audio")),
    ("QoL & performance", ls("feature/qol") + ls("feature/performance")),
    ("Mixins", ls("mixin")),
]


def language(rel):
    if rel.endswith(".json"):
        return "json"
    if rel.endswith((".gradle", ".properties")):
        return "gradle"
    return "java"


def main():
    seen, total, out = set(), 0, []
    out.append("# ChaosUtils — complete source (Minecraft 1.21.11, Fabric)\n")
    out.append("Machine-generated single-file bundle of every source file in this repository, "
               "in a readable order. The canonical files live under `src/`; regenerate this "
               "document with `python3 tools/bundle_source.py` after changing code.\n")
    for title, paths in GROUPS:
        out.append(f"\n---\n\n## {title}\n")
        for rel in paths:
            if rel in seen or not os.path.isfile(os.path.join(ROOT, rel)):
                continue
            seen.add(rel)
            with open(os.path.join(ROOT, rel), encoding="utf-8") as handle:
                text = handle.read()
            total += text.count("\n") + 1
            out.append(f"\n### `{rel}`\n\n```{language(rel)}\n{text.rstrip()}\n```\n")
    document = "".join(out)
    target = os.path.join(ROOT, "docs/ChaosUtils-source.md")
    with open(target, "w", encoding="utf-8") as handle:
        handle.write(document)
    print(f"wrote {target}: {len(seen)} files, {total} source lines, {document.count(chr(10)) + 1} document lines")


if __name__ == "__main__":
    main()
