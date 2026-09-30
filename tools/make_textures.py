#!/usr/bin/env python3
"""Generates the tiny procedural textures ChaosUtils ships with.

Everything ChaosUtils draws is geometry, with one exception: the soft radial glow behind hovered
cards, window headers and the radial menu hub. A real texture is much cheaper than dozens of
concentric translucent circles and it is tinted by the accent colour in the shader, so a single
draw call produces a coloured glow.

The script writes straight alpha RGBA PNGs without any third party dependency, so the texture can
be regenerated on any machine:

    python3 tools/make_textures.py
"""

from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "src/main/resources/assets/chaosutils/textures/gui"


def write_png(path: Path, width: int, height: int, rows: list[bytes]) -> None:
    def chunk(tag: bytes, payload: bytes) -> bytes:
        return (
            struct.pack(">I", len(payload))
            + tag
            + payload
            + struct.pack(">I", zlib.crc32(tag + payload) & 0xFFFFFFFF)
        )

    raw = b"".join(b"\x00" + row for row in rows)
    data = b"\x89PNG\r\n\x1a\n"
    data += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    data += chunk(b"IDAT", zlib.compress(raw, 9))
    data += chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
    print(f"wrote {path.relative_to(ROOT)} ({width}x{height}, {len(data)} bytes)")


def radial_glow(size: int, exponent: float) -> list[bytes]:
    """White pixel with a smooth alpha falloff; the shader tints it with the accent colour."""
    rows: list[bytes] = []
    half = (size - 1) / 2.0
    for y in range(size):
        row = bytearray()
        for x in range(size):
            dx = (x - half) / half
            dy = (y - half) / half
            distance = (dx * dx + dy * dy) ** 0.5
            alpha = 0.0 if distance >= 1.0 else (1.0 - distance) ** exponent
            # A slight core boost keeps the middle from looking washed out after tinting.
            alpha = min(1.0, alpha * 1.08)
            row += bytes((255, 255, 255, int(round(alpha * 255.0))))
        rows.append(bytes(row))
    return rows


def main() -> None:
    write_png(TEXTURE_DIR / "glow.png", 128, 128, radial_glow(128, 2.4))


if __name__ == "__main__":
    main()
