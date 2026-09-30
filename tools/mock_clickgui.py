#!/usr/bin/env python3
"""Renders a mockup of the ChaosUtils click GUI.

The mock uses the very same metrics, colours, icons and fonts as the Java code, so the design can
be reviewed without launching the game. It is a development aid, not part of the build.

Usage: python3 tools/mock_clickgui.py [out.png]
"""

from __future__ import annotations

import os
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ATLAS = os.path.join(ROOT, "src", "main", "resources", "assets", "chaosutils", "textures", "gui", "icons.png")
FONT_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "chaosutils", "font")

# ---------------------------------------------------------------- theme (UiTheme)
ACCENT = (0x8B, 0x5C, 0xF6)
ACCENT_BRIGHT = (0xA9, 0x82, 0xF9)
WINDOW_TOP = (0x0D, 0x10, 0x17)
WINDOW_BOTTOM = (0x0A, 0x0C, 0x12)
CARD = (0x10, 0x14, 0x1C)
CARD_HOVER = (0x16, 0x1B, 0x26)
CATEGORY_HOVER = (0x14, 0x19, 0x23)
OUTLINE = (0x1A, 0x20, 0x30)
OUTLINE_SOFT = (0x14, 0x19, 0x24)
TEXT = (0xF6, 0xF7, 0xFB)
TEXT_DIM = (0xA6, 0xAD, 0xC0)
TEXT_FAINT = (0x6A, 0x72, 0x86)
TRACK = (0x21, 0x27, 0x36)
SURFACE = (0x0C, 0x0F, 0x16)
SIDEBAR = (0x0A, 0x0D, 0x13)

CELL = 20
COLS = 11
PAD = 14.0
SIDEBAR_WIDTH = 154.0
COLUMN_GAP = 16.0
ICON_ORDER = [
    "layers", "eye", "radial", "chat", "box", "note", "sparkle", "gauge", "sliders", "palette", "folder",
    "users", "keyboard", "search", "zoom", "list", "grid", "chevron", "chevron_down", "chevron_left", "chevron_up",
    "check", "close", "plus", "minus", "trash", "pencil", "copy", "refresh", "arrow_up", "arrow_down", "drag",
    "lock", "unlock", "eye_off", "link", "info", "warn", "clock", "pin", "camera", "bell", "shield",
    "home", "sun", "crosshair", "particles", "person", "screenshot", "star", "dot", "server", "volume", "filter",
    "sort", "compass", "download", "upload",
]


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(os.path.join(FONT_DIR, name), size)


def text(draw, xy, value, fnt, fill, baseline=True):
    draw.text(xy, value, font=fnt, fill=fill, anchor="ls" if baseline else "la")


def icon(img, name: str, x: float, y: float, size: float, color, alpha: float = 1.0):
    index = ICON_ORDER.index(name)
    col = index % COLS
    row = index // COLS
    sheet = img if isinstance(img, Image.Image) else None
    cell = ATLAS_IMAGE.crop((col * CELL, row * CELL, col * CELL + CELL, row * CELL + CELL))
    cell = cell.resize((max(1, int(round(size))), max(1, int(round(size)))), Image.LANCZOS)
    tinted = Image.new("RGBA", cell.size, (0, 0, 0, 0))
    tinted.paste(color + (int(255 * alpha),), (0, 0, cell.size[0], cell.size[1]))
    # use the glyph as an alpha mask
    alpha_channel = cell.split()[3].point(lambda value: int(value * alpha))
    tinted.putalpha(alpha_channel)
    img.alpha_composite(tinted, (int(round(x)), int(round(y))))


def rounded(draw, box, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def main() -> None:
    global ATLAS_IMAGE
    ATLAS_IMAGE = Image.open(ATLAS).convert("RGBA")

    width, height = 900, 520
    scale = 2
    image = Image.new("RGBA", (width * scale, height * scale), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    S = scale

    def z(value):
        return int(round(value * S))

    parts = []

    def part(x, y, w, h):
        layer = Image.new("RGBA", (z(w), z(h)), (0, 0, 0, 0))
        parts.append((x, y, layer, ImageDraw.Draw(layer)))
        return layer, ImageDraw.Draw(layer)

    def commit(layer, x, y):
        image.alpha_composite(layer, (z(x), z(y)))

    # ---------------------------------------------------------------- window
    body, body_draw = part(0, 0, width, height)
    for row in range(z(height)):
        t = row / z(height)
        color = tuple(int(WINDOW_TOP[i] + (WINDOW_BOTTOM[i] - WINDOW_TOP[i]) * t) for i in range(3))
        body_draw.line([(0, row), (z(width), row)], fill=color + (255,))
    mask = Image.new("L", body.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, z(width) - 1, z(height) - 1], radius=z(10), fill=255)
    body.putalpha(mask)
    commit(body, 0, 0)

    shell, shell_draw = part(0, 0, width, height)
    rounded(shell_draw, [0, 0, z(width) - 1, z(height) - 1], z(10), outline=OUTLINE + (255,), width=max(1, S))
    # accent strip along the top edge
    strip_x = z(6)
    strip_w = z(width) - strip_x * 2
    for i in range(strip_w):
        t = i / strip_w
        edge = min(t, 1 - t) / 0.5
        alpha = int(220 * edge * edge)
        shell_draw.line([(strip_x + i, z(1)), (strip_x + i, z(3))], fill=ACCENT + (alpha,))
    shell.putalpha(shell.split()[3])
    commit(shell, 0, 0)

    # ---------------------------------------------------------------- sidebar
    side, side_draw = part(0, 0, width, height)
    rounded(side_draw, [0, 0, z(PAD + SIDEBAR_WIDTH + 8), z(height)], z(10), fill=SIDEBAR + (255,))
    commit(side, 0, 0)

    layer, draw2 = part(0, 0, width, height)
    # brand
    rounded(draw2, [z(PAD), z(14), z(PAD + 26), z(40)], z(8), fill=ACCENT + (255,))
    icon(layer, "sparkle", PAD + 5.5, 19.5, 15, (255, 255, 255))
    text(draw2, (z(PAD + 35), z(14 + 7)), "ChaosUtils", font("poppins_semibold.ttf", z(10)), TEXT + (255,))
    text(draw2, (z(PAD + 35), z(14 + 22)), "BETA RELEASE ui-4", font("poppins_medium.ttf", z(7)), TEXT_FAINT + (255,))

    rows = [
        ("gauge", "HUD & Overlays", None),
        ("eye", "Visuals", None),
        ("radial", "Radial Menu", None),
        ("chat", "Chat & Social", None),
        ("box", "Inventory", None),
        ("note", "Audio", None),
        ("sparkle", "Quality of Life", None),
        ("sliders", "Performance", None),
    ]
    counts = ["12", "6", "3", "4", "3", "3", "5", "5"]
    selected = 5

    text(draw2, (z(PAD + 4), z(62 + 8)), "MODULES", font("poppins_semibold.ttf", z(8)), TEXT_FAINT + (255,))
    cursor = 80
    for index, (ic, label, _) in enumerate(rows):
        rect = [z(PAD), z(cursor), z(PAD + SIDEBAR_WIDTH), z(cursor + 24)]
        if index == selected:
            rounded(draw2, rect, z(7), fill=(0x38, 0x2B, 0x5E, 255), outline=(0x6D, 0x4B, 0xC8, 255), width=S)
        elif index == 1:
            rounded(draw2, rect, z(7), fill=CATEGORY_HOVER + (255,))
        color = TEXT + (255,) if index == selected else TEXT_DIM + (255,)
        icon(layer, ic, PAD + 9.5, cursor + 5.5, 13, ACCENT if index == selected else (TEXT_FAINT if index != 1 else TEXT_DIM))
        text(draw2, (z(PAD + 29), z(cursor + 12 + 4)), label, font("poppins_medium.ttf", z(9)), color)
        count_color = ACCENT_BRIGHT if index == selected else TEXT_FAINT
        text(draw2, (z(PAD + SIDEBAR_WIDTH - 9 - 10), z(cursor + 12 + 4)), counts[index],
             font("poppins_medium.ttf", z(8)), count_color)
        cursor += 26

    cursor += 12
    text(draw2, (z(PAD + 4), z(cursor + 8)), "GENERAL", font("poppins_semibold.ttf", z(8)), TEXT_FAINT + (255,))
    cursor += 18
    for ic, label in [("sliders", "Settings"), ("palette", "Theme"), ("folder", "Configs"),
                      ("users", "Socials"), ("keyboard", "Keybinds")]:
        icon(layer, ic, PAD + 9.5, cursor + 5.5, 13, TEXT_FAINT)
        text(draw2, (z(PAD + 29), z(cursor + 12 + 4)), label, font("poppins_medium.ttf", z(9)), TEXT_DIM + (255,))
        cursor += 26

    text(draw2, (z(PAD + 4), z(height - 20 + 8)), "Esc closes  ·  RShift toggles",
         font("poppins_medium.ttf", z(7)), TEXT_FAINT + (255,))
    commit(layer, 0, 0)

    # ---------------------------------------------------------------- content
    layer, draw2 = part(0, 0, width, height)
    cx = PAD + SIDEBAR_WIDTH + COLUMN_GAP
    cw = width - PAD - cx

    text(draw2, (z(cx), z(24 + 12)), "Audio", font("poppins_semibold.ttf", z(13)), TEXT + (255,))
    text(draw2, (z(cx), z(43 + 8)), "3 modules  ·  1 enabled", font("poppins_medium.ttf", z(8)),
         TEXT_FAINT + (255,))

    # search pill + view buttons
    search_x = width - PAD - 190 - 52
    rounded(draw2, [z(search_x), z(22), z(search_x + 190), z(44)], z(11), fill=SURFACE + (255,),
            outline=OUTLINE + (255,), width=S)
    icon(layer, "search", search_x + 8, 28, 10, TEXT_FAINT)
    text(draw2, (z(search_x + 24), z(22 + 11 + 4)), "Search modules", font("poppins_medium.ttf", z(8)),
         TEXT_FAINT + (255,))
    for i, ic in enumerate(["grid", "chevron_up"]):
        bx = search_x + 190 + 6 + i * 24
        rounded(draw2, [z(bx), z(22), z(bx + 22), z(44)], z(7), fill=SURFACE + (255,),
                outline=OUTLINE + (255,), width=S)
        icon(layer, ic, bx + 5, 27, 12, TEXT_DIM)

    text(draw2, (z(cx), z(72 + 8)), "MODULES", font("poppins_semibold.ttf", z(8)), TEXT_DIM + (255,))

    cards = [
        ("Ambient Sound Radar", "Paints the direction of important sounds on a circular HUD.", True, False),
        ("Chat Mentions", "Highlights your name and keywords, with a soft ping.", True, True),
        ("Unfocused Volume Ducking", "Lowers the volume while the window is not focused.", False, False),
        ("Subtitles Plus", "Nicer formatting, colours and positioning for the subtitle overlay.", False, False),
        ("Volume Mixer", "Per category volumes that follow the situation.", False, False),
    ]
    cursor = 90
    for index, (title, desc, on, hover) in enumerate(cards):
        card_h = 42
        rounded(draw2, [z(cx), z(cursor), z(cx + cw), z(cursor + card_h)], z(8),
                fill=(CARD_HOVER if hover else CARD) + (255,),
                outline=(OUTLINE if not hover else (0x2A, 0x33, 0x46)) + (255,), width=S)
        if on:
            rounded(draw2, [z(cx), z(cursor + 9), z(cx + 2), z(cursor + card_h - 9)], z(1),
                    fill=ACCENT + (255,))
        text(draw2, (z(cx + 14), z(cursor + 8 + 7)), title, font("poppins_semibold.ttf", z(9.2)), TEXT + (255,))
        text(draw2, (z(cx + 14), z(cursor + card_h - 16 + 7)), desc, font("poppins_medium.ttf", z(8)),
             (0x7B, 0x84, 0x99, 255))
        icon(layer, "chevron_down" if index == 1 else "chevron", cx + cw - 30, cursor + 15.5, 11,
             (TEXT_DIM if hover else TEXT_FAINT))
        # pill switch
        sx = cx + cw - 46
        sy = cursor + (card_h - 16) / 2
        track_color = ACCENT if on else TRACK
        rounded(draw2, [z(sx), z(sy), z(sx + 30), z(sy + 16)], z(8), fill=track_color + (255,))
        knob_x = sx + (22 if on else 8)
        draw2.ellipse([z(knob_x - 6), z(sy + 2), z(knob_x + 6), z(sy + 14)], fill=(255, 255, 255, 255))
        cursor += card_h + 6
    commit(layer, 0, 0)

    output = sys.argv[1] if len(sys.argv) > 1 else "/tmp/mock_clickgui.png"
    image.resize((width, height), Image.LANCZOS).save(output)
    image.save(output.replace(".png", "@2x.png"))
    print(f"wrote {output}")


if __name__ == "__main__":
    main()
