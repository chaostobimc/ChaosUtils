#!/usr/bin/env python3
"""Check every ChaosUtils mixin handler against the real Minecraft sources.

Why this exists
---------------
A mixin handler that lists arguments must match the target method's arguments
*exactly* (types, in order, minus the trailing CallbackInfo/CallbackInfoReturnable).
If they do not match, Mixin does not degrade gracefully: it aborts class
transformation and the game dies during startup with an
``InvalidInjectionException: Invalid descriptor ...`` - even with ``require = 0``.

That is exactly what happened once (``GuiMixin`` was missing the
``DeltaTracker`` parameter that ``Gui#renderCrosshair`` gained), so this script
compares all handlers with the real sources instead of trusting memory.

Usage
-----
Provide a directory containing the decompiled, Mojang-mapped Minecraft sources
(one file per class, e.g. ``Gui.java``, ``Camera.java``). Any mirror of the
official ``1.21.11_unobfuscated`` sources works::

    python3 tools/mixin_check.py --mirror /path/to/minecraft-sources

Without ``--mirror`` the script tries ``/tmp/mc`` and then fetches the handful
of files it needs from a public 1.21.11 mirror through the GitHub CLI (``gh``),
caching them in the mirror directory.

Exit code 1 means at least one handler would break the game at startup.
"""

from __future__ import annotations

import argparse
import pathlib
import re
import subprocess
import sys

# mixin class -> (minecraft class file, [methods that must be checked])
MIRROR_REPO = "rrrRex1024/minecraft-1-21-11-source"

TARGETS = {
    "AbstractContainerScreenMixin": ("AbstractContainerScreen.java", "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java"),
    "CameraMixin": ("Camera.java", "net/minecraft/client/Camera.java"),
    "DebugScreenOverlayMixin": ("DebugScreenOverlay.java", "net/minecraft/client/gui/components/DebugScreenOverlay.java"),
    "EntityMixin": ("Entity.java", "net/minecraft/world/entity/Entity.java"),
    "GameRendererMixin": ("GameRenderer.java", "net/minecraft/client/renderer/GameRenderer.java"),
    "GuiMixin": ("Gui.java", "net/minecraft/client/gui/Gui.java"),
    "MouseHandlerMixin": ("MouseHandler.java", "net/minecraft/client/MouseHandler.java"),
    "ParticleEngineMixin": ("ParticleEngine.java", "net/minecraft/client/particle/ParticleEngine.java"),
    "SoundEngineMixin": ("SoundEngine.java", "net/minecraft/client/sounds/SoundEngine.java"),
}

INJECT = re.compile(r"@Inject\((?:[^()]|\([^()]*\))*\)", re.S)
HANDLER = re.compile(r"(?:private|protected|public)\s+void\s+([\w$]+)\s*\(((?:[^()]|\([^()]*\))*)\)", re.S)
TARGET_METHOD = re.compile(
    r"^\s*(?:public|protected|private)(?:\s+final|\s+static|\s+abstract)*\s+"
    r"([\w\.\[\]<>@\s]+?)\s+(\w+)\s*\(([^)]*)\)\s*\{",
    re.M,
)

BOXED = {"int": "Integer", "float": "Float", "double": "Double", "boolean": "Boolean",
         "long": "Long", "short": "Short", "byte": "Byte", "char": "Character"}


def param_type(param: str) -> str:
    tokens = param.split()
    while tokens and tokens[0] in ("final", "@Nullable", "@NotNull"):
        tokens = tokens[1:]
    return " ".join(tokens[:-1]) if len(tokens) >= 2 else (tokens[0] if tokens else "")


def norm(type_name: str) -> str:
    t = param_type(type_name).split("<")[0].strip()
    t = t.split(".")[-1]
    return BOXED.get(t, t)


def params_of(text: str) -> list[str]:
    return [p.strip() for p in text.split(",") if p.strip()]


def fetch(mirror: pathlib.Path, file_name: str, repo_path: str) -> bool:
    if (mirror / file_name).exists():
        return True
    try:
        raw = subprocess.run(
            ["gh", "api", f"repos/{MIRROR_REPO}/contents/{repo_path}", "--jq", ".content"],
            capture_output=True, check=True,
        ).stdout
        import base64
        (mirror / file_name).write_bytes(base64.b64decode(raw))
        print(f"    fetched {repo_path}")
        return True
    except Exception as exc:  # noqa: BLE001 - the script reports and moves on
        print(f"    could not fetch {repo_path}: {exc}")
        return False


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--mirror", default="/tmp/mc", help="directory with decompiled Minecraft sources")
    parser.add_argument("--mixin-dir", default="src/main/java/dev/chaosutils/mixin", help="directory with the mixin classes")
    args = parser.parse_args()

    mirror = pathlib.Path(args.mirror)
    mixin_dir = pathlib.Path(args.mixin_dir)
    mirror.mkdir(parents=True, exist_ok=True)

    problems = 0
    checked = 0
    print("Mixin descriptor check against the Minecraft sources\n")
    for mixin, (file_name, repo_path) in TARGETS.items():
        mixin_file = mixin_dir / f"{mixin}.java"
        if not mixin_file.exists():
            print(f"!! {mixin}: source file missing")
            problems += 1
            continue
        if not fetch(mirror, file_name, repo_path):
            print(f"?? {mixin}: no reference source, skipped")
            continue

        mixin_text = mixin_file.read_text(encoding="utf-8")
        target_text = (mirror / file_name).read_text(encoding="utf-8", errors="replace")

        for annotation in INJECT.finditer(mixin_text):
            match = re.search(r'method\s*=\s*"([^"]+)"', annotation.group(0))
            if not match:
                continue
            target_method = match.group(1)
            handler = HANDLER.search(mixin_text[annotation.end():])
            if not handler:
                print(f"!! {mixin}: no handler found after @Inject(method={target_method})")
                problems += 1
                continue

            name, raw_params = handler.group(1), handler.group(2)
            handler_params = [p for p in params_of(raw_params) if "CallbackInfo" not in p]
            returns_value = "CallbackInfoReturnable" in raw_params
            checked += 1

            overloads = [
                (m.group(1).strip(), params_of(m.group(3)))
                for m in TARGET_METHOD.finditer(target_text)
                if m.group(2) == target_method
            ]
            if not overloads:
                print(f"!! {mixin}.{name}: '{target_method}' not found in {file_name}")
                problems += 1
                continue

            # A handler may omit trailing parameters, so its list has to be a prefix.
            candidates = [o for o in overloads if len(o[1]) >= len(handler_params)]
            match = None
            for ret, tparams in candidates:
                if all(norm(h) == norm(t) for h, t in zip(handler_params, tparams)):
                    match = (ret, tparams)
                    break
            if match is None:
                print(f"!! {mixin}.{name}: no overload of {target_method} matches the handler")
                for ret, tparams in overloads:
                    print(f"      target:  {target_method}({', '.join(norm(t) for t in tparams)}) -> {norm(ret)}")
                print(f"      handler: ({', '.join(norm(h) for h in handler_params) or 'no arguments'})")
                problems += 1
                continue

            ret, tparams = match
            if returns_value:
                declared = re.search(r"CallbackInfoReturnable<([^>]+)>", raw_params)
                if declared and norm(declared.group(1)) != norm(ret):
                    print(f"!! {mixin}.{name}: CallbackInfoReturnable<{norm(declared.group(1))}> != {norm(ret)}")
                    problems += 1
                    continue

            signature = ", ".join(norm(t) for t in tparams)
            omitted = len(tparams) - len(handler_params)
            note = f" (handler ignores {omitted})" if omitted else ""
            print(f"OK  {mixin}.{name} -> {target_method}({signature}) -> {norm(ret)}{note}")

    print(f"\n{checked} injections checked, {problems} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
