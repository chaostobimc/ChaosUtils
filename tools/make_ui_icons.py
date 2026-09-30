#!/usr/bin/env python3
"""Generates the ChaosUtils interface icon atlas.

The icons are drawn as signed distance fields and supersampled, which keeps the
strokes smooth at every GUI scale.  The atlas is a plain white-on-transparent
sheet; the game tints the cells at draw time, so a single texture covers every
accent colour of the theme.

Output:
    src/main/resources/assets/chaosutils/textures/gui/icons.png

Usage:
    python3 tools/make_ui_icons.py [--preview /tmp/icons_preview.png]
"""

from __future__ import annotations

import os
import struct
import sys
import zlib

import numpy as np

CELL = 20          # cell size in the atlas, in pixels
SS = 4             # supersampling factor
COLS = 11          # cells per row
MAX_INT = 2 ** 31 - 1


# --------------------------------------------------------------------------- sdf primitives
def _grid(n: int):
    xs = (np.arange(n) + 0.5) / n * CELL
    gx, gy = np.meshgrid(xs, xs, indexing="xy")
    return gx, gy


def disk(gx, gy, cx, cy, r):
    return np.hypot(gx - cx, gy - cy) - r


def ring(gx, gy, cx, cy, r, w):
    return np.abs(np.hypot(gx - cx, gy - cy) - r) - w * 0.5


def arc(gx, gy, cx, cy, r, w, a0, a1):
    """Annulus limited to the angular range a0..a1 (degrees, 0 = +x, ccw)."""
    ang = np.degrees(np.arctan2(-(gy - cy), gx - cx)) % 360.0
    lo, hi = a0 % 360.0, a1 % 360.0
    if lo <= hi:
        inside = (ang >= lo) & (ang <= hi)
    else:
        inside = (ang >= lo) | (ang <= hi)
    d = ring(gx, gy, cx, cy, r, w)
    rad = np.radians(ang)
    edge0 = np.stack([cx + r * np.cos(np.radians(lo)), cy - r * np.sin(np.radians(lo))], -1)
    edge1 = np.stack([cx + r * np.cos(np.radians(hi)), cy - r * np.sin(np.radians(hi))], -1)
    p = np.stack([gx, gy], -1)
    cap = np.minimum(np.linalg.norm(p - edge0, axis=-1), np.linalg.norm(p - edge1, axis=-1)) - w * 0.5
    return np.where(inside, d, cap)


def capsule(gx, gy, x0, y0, x1, y1, w):
    px, py = gx - x0, gy - y0
    bx, by = x1 - x0, y1 - y0
    denom = bx * bx + by * by
    h = 0.0 if denom == 0 else np.clip((px * bx + py * by) / denom, 0.0, 1.0)
    return np.hypot(px - bx * h, py - by * h) - w * 0.5


def box(gx, gy, cx, cy, hw, hh, r=0.0):
    qx = np.abs(gx - cx) - (hw - r)
    qy = np.abs(gy - cy) - (hh - r)
    outside = np.hypot(np.maximum(qx, 0.0), np.maximum(qy, 0.0))
    inside = np.minimum(np.maximum(qx, qy), 0.0)
    return outside + inside - r


def outline(gx, gy, cx, cy, hw, hh, r, w):
    return np.abs(box(gx, gy, cx, cy, hw, hh, r)) - w * 0.5


def poly(gx, gy, pts):
    p = np.stack([gx, gy], -1)
    verts = np.array(pts, dtype=float)
    n = len(verts)
    d = np.linalg.norm(p - verts[0], axis=-1) ** 2
    sign = 1.0
    for i in range(n):
        j = (i - 1) % n
        e = verts[j] - verts[i]
        w = p - verts[i]
        b = w - e * np.clip(np.dot(w, e) / max(np.dot(e, e), 1e-9), 0.0, 1.0)
        d = np.minimum(d, np.sum(b * b, axis=-1))
        c = np.array([p[..., 1] >= verts[i][1], p[..., 1] < verts[j][1], e[0] * w[..., 1] > e[1] * w[..., 0]])
        if np.all(c[0] == c[1]) or np.any(c[0] != c[1]):
            if c[0].all() == c[1].all():
                sign = -sign
        if (c[0] & c[1] & c[2]).any() or ((~c[0]) & (~c[1]) & (~c[2])).any():
            sign = -sign
    return sign * np.sqrt(d)


def poly_at(gx, gy, pts):
    """Polygon fill using the crossing-number rule (robust, no sign juggling)."""
    verts = np.array(pts, dtype=float)
    inside = np.zeros(gx.shape, dtype=bool)
    n = len(verts)
    for i in range(n):
        x0, y0 = verts[i]
        x1, y1 = verts[(i + 1) % n]
        cond = ((y0 > gy) != (y1 > gy)) & (gx < (x1 - x0) * (gy - y0) / (y1 - y0 + 1e-12) + x0)
        inside ^= cond
    d = np.full(gx.shape, 1e4)
    for i in range(n):
        x0, y0 = verts[i]
        x1, y1 = verts[(i + 1) % n]
        d = np.minimum(d, capsule(gx, gy, x0, y0, x1, y1, 0.0))
    return np.where(inside, -d, d)


def union(*ds):
    out = ds[0]
    for d in ds[1:]:
        out = np.minimum(out, d)
    return out


def cut(a, b):
    return np.maximum(a, -b)


def intersect(a, b):
    return np.maximum(a, b)


# --------------------------------------------------------------------------- icons
def i_layers(gx, gy):
    d = outline(gx, gy, 10, 8.6, 7.0, 5.4, 2.0, 1.5)
    return union(d, capsule(gx, gy, 6.6, 8.0, 13.4, 8.0, 1.3), capsule(gx, gy, 6.6, 11.0, 11.5, 11.0, 1.3),
                 capsule(gx, gy, 4.2, 16.4, 15.8, 16.4, 1.6))


def i_eye(gx, gy):
    # lens = overlap of two circles stacked vertically
    outer = intersect(disk(gx, gy, 10, 2.6, 10.4), disk(gx, gy, 10, 17.4, 10.4))
    inner = intersect(disk(gx, gy, 10, 4.3, 8.1), disk(gx, gy, 10, 15.7, 8.1))
    return union(cut(outer, inner), disk(gx, gy, 10, 10, 2.5))


def i_radial(gx, gy):
    d = ring(gx, gy, 10, 10.4, 6.4, 1.5)
    d = union(d, disk(gx, gy, 10, 10.4, 2.0))
    for a in (0, 90, 180, 270):
        rad = np.radians(a)
        d = union(d, capsule(gx, gy, 10 + 5.4 * np.cos(rad), 10.4 - 5.4 * np.sin(rad),
                             10 + 7.6 * np.cos(rad), 10.4 - 7.6 * np.sin(rad), 1.4))
    return d


def i_chat(gx, gy):
    d = outline(gx, gy, 10, 8.8, 7.0, 5.2, 2.4, 1.5)
    tail = poly_at(gx, gy, [(6.2, 13.4), (6.2, 17.6), (10.6, 13.4)])
    d = union(d, tail)
    for x in (7.2, 10.0, 12.8):
        d = union(d, disk(gx, gy, x, 8.8, 1.05))
    return d


def i_box(gx, gy):
    d = outline(gx, gy, 10, 11.2, 7.0, 5.6, 2.2, 1.5)
    d = union(d, capsule(gx, gy, 3.6, 9.4, 16.4, 9.4, 1.4))
    d = union(d, arc(gx, gy, 10, 6.4, 2.4, 1.4, 20, 160))
    return d


def i_note(gx, gy):
    d = disk(gx, gy, 7.4, 14.2, 3.1)
    d = union(d, capsule(gx, gy, 10.2, 14.2, 10.2, 4.6, 1.5))
    d = union(d, capsule(gx, gy, 10.2, 4.9, 15.4, 6.6, 1.5))
    d = union(d, capsule(gx, gy, 15.0, 6.5, 15.0, 9.4, 1.5))
    return d


def i_sparkle(gx, gy):
    d = poly_at(gx, gy, [(10, 2.6), (12.0, 10.0), (10, 17.4), (8.0, 10.0)])
    d = union(d, poly_at(gx, gy, [(2.6, 10), (10, 8.0), (17.4, 10), (10, 12.0)]))
    d = union(d, disk(gx, gy, 15.4, 4.6, 1.3))
    return d


def i_gauge(gx, gy):
    d = arc(gx, gy, 10, 11.6, 6.2, 1.5, 0, 180)
    d = union(d, capsule(gx, gy, 10, 11.6, 13.8, 6.6, 1.6))
    d = union(d, disk(gx, gy, 10, 11.6, 1.6))
    d = union(d, capsule(gx, gy, 4.2, 16.4, 15.8, 16.4, 1.5))
    return d


def i_sliders(gx, gy):
    d = None
    rows = ((5.8, 13.4), (10.0, 6.8), (14.2, 11.8))
    for y, knob in rows:
        c = capsule(gx, gy, 3.8, y, 16.2, y, 1.4)
        d = c if d is None else union(d, c)
        d = union(d, disk(gx, gy, knob, y, 2.9))
    return d


def i_palette(gx, gy):
    d = ring(gx, gy, 10, 10, 6.6, 1.5)
    for a in (200, 250, 300):
        rad = np.radians(a)
        d = union(d, disk(gx, gy, 10 + 3.4 * np.cos(rad), 10 - 3.4 * np.sin(rad), 1.3))
    d = union(d, disk(gx, gy, 13.6, 12.6, 1.3))
    return d


def i_folder(gx, gy):
    d = outline(gx, gy, 10, 12.0, 6.8, 4.6, 1.8, 1.5)
    d = union(d, poly_at(gx, gy, [(4.4, 7.4), (6.0, 4.8), (10.4, 4.8), (11.6, 7.4)]))
    return d


def i_users(gx, gy):
    d = ring(gx, gy, 7.4, 7.6, 2.0, 1.5)
    d = union(d, arc(gx, gy, 7.4, 15.6, 4.4, 1.6, 20, 160))
    d = union(d, arc(gx, gy, 15.0, 6.4, 1.6, 1.4, 300, 60))
    d = union(d, arc(gx, gy, 15.4, 15.6, 3.4, 1.5, 40, 150))
    return d


def i_keyboard(gx, gy):
    d = outline(gx, gy, 10, 10.4, 7.2, 5.0, 2.0, 1.5)
    for x in (6.2, 8.6, 11.2, 13.8):
        d = union(d, disk(gx, gy, x, 8.4, 0.95))
    for x in (6.2, 8.6, 11.2, 13.8):
        d = union(d, disk(gx, gy, x, 11.0, 0.95))
    d = union(d, capsule(gx, gy, 7.4, 13.4, 12.6, 13.4, 1.5))
    return d


def i_search(gx, gy):
    d = ring(gx, gy, 8.8, 8.8, 5.0, 1.6)
    d = union(d, capsule(gx, gy, 12.6, 12.6, 16.6, 16.6, 1.8))
    return d


def i_zoom(gx, gy):
    d = ring(gx, gy, 9.4, 9.4, 5.6, 1.6)
    d = union(d, capsule(gx, gy, 13.6, 13.6, 17.0, 17.0, 1.8))
    d = union(d, capsule(gx, gy, 6.8, 9.4, 12.0, 9.4, 1.4))
    d = union(d, capsule(gx, gy, 9.4, 6.8, 9.4, 12.0, 1.4))
    return d


def i_list(gx, gy):
    d = None
    for y in (6.0, 10.0, 14.0):
        d = disk(gx, gy, 4.6, y, 1.3) if d is None else union(d, disk(gx, gy, 4.6, y, 1.3))
        d = union(d, capsule(gx, gy, 8.0, y, 16.0, y, 1.5))
    return d


def i_grid(gx, gy):
    d = None
    for cx in (6.6, 13.4):
        for cy in (6.6, 13.4):
            b = box(gx, gy, cx, cy, 2.7, 2.7, 1.0)
            d = b if d is None else union(d, b)
    return d


def i_chevron(gx, gy):
    return union(capsule(gx, gy, 7.4, 4.6, 12.6, 10.0, 1.8),
                 capsule(gx, gy, 12.6, 10.0, 7.4, 15.4, 1.8))


def i_chevron_down(gx, gy):
    return union(capsule(gx, gy, 4.6, 7.6, 10.0, 12.8, 1.8),
                 capsule(gx, gy, 10.0, 12.8, 15.4, 7.6, 1.8))


def i_check(gx, gy):
    return union(capsule(gx, gy, 4.4, 10.4, 8.4, 14.6, 1.8),
                 capsule(gx, gy, 8.4, 14.6, 15.6, 5.6, 1.8))


def i_close(gx, gy):
    return union(capsule(gx, gy, 5.2, 5.2, 14.8, 14.8, 1.7),
                 capsule(gx, gy, 14.8, 5.2, 5.2, 14.8, 1.7))


def i_plus(gx, gy):
    return union(capsule(gx, gy, 10, 4.6, 10, 15.4, 1.7),
                 capsule(gx, gy, 4.6, 10, 15.4, 10, 1.7))


def i_minus(gx, gy):
    return capsule(gx, gy, 4.6, 10, 15.4, 10, 1.7)


def i_trash(gx, gy):
    d = outline(gx, gy, 10, 12.6, 5.2, 4.8, 1.6, 1.5)
    d = union(d, capsule(gx, gy, 3.6, 6.6, 16.4, 6.6, 1.6))
    d = union(d, arc(gx, gy, 10, 6.4, 2.2, 1.4, 20, 160))
    d = union(d, capsule(gx, gy, 8.2, 10.0, 8.2, 15.2, 1.3))
    d = union(d, capsule(gx, gy, 11.8, 10.0, 11.8, 15.2, 1.3))
    return d


def i_pencil(gx, gy):
    ang = np.radians(45)
    dx, dy = np.cos(ang), np.sin(ang)
    d = capsule(gx, gy, 10 - 4.4 * dx, 10 + 4.4 * dy, 10 + 4.4 * dx, 10 - 4.4 * dy, 4.6)
    d = union(d, poly_at(gx, gy, [(15.2, 4.8), (16.8, 3.2), (18.0, 4.4), (16.4, 6.0)]))
    d = cut(d, capsule(gx, gy, 13.6, 6.4, 15.4, 8.2, 1.6))
    d = union(d, capsule(gx, gy, 5.2, 14.8, 3.2, 16.8, 1.6))
    return d


def i_copy(gx, gy):
    d = outline(gx, gy, 7.4, 7.4, 4.6, 4.6, 1.6, 1.5)
    d = union(d, outline(gx, gy, 12.6, 12.6, 4.6, 4.6, 1.6, 1.5))
    return d


def i_refresh(gx, gy):
    d = arc(gx, gy, 10, 10, 5.8, 1.6, -60, 220)
    d = union(d, poly_at(gx, gy, [(14.6, 3.4), (17.4, 6.6), (13.2, 7.4)]))
    return d


def i_arrow_up(gx, gy):
    return union(capsule(gx, gy, 10, 15.4, 10, 5.2, 1.7),
                 capsule(gx, gy, 5.2, 10.0, 10, 5.2, 1.7),
                 capsule(gx, gy, 10, 5.2, 14.8, 10.0, 1.7))


def i_arrow_down(gx, gy):
    return union(capsule(gx, gy, 10, 4.6, 10, 14.8, 1.7),
                 capsule(gx, gy, 5.2, 10.0, 10, 14.8, 1.7),
                 capsule(gx, gy, 10, 14.8, 14.8, 10.0, 1.7))


def i_drag(gx, gy):
    d = None
    for cy in (5.6, 10.0, 14.4):
        for cx in (7.6, 12.4):
            b = disk(gx, gy, cx, cy, 1.25)
            d = b if d is None else union(d, b)
    return d


def i_lock(gx, gy):
    d = box(gx, gy, 10, 13.0, 5.4, 4.0, 1.6)
    d = union(d, arc(gx, gy, 10, 9.6, 3.2, 1.6, 10, 170))
    d = union(d, capsule(gx, gy, 10, 12.0, 10, 14.4, 1.4))
    return d


def i_unlock(gx, gy):
    d = box(gx, gy, 10, 13.0, 5.4, 4.0, 1.6)
    d = union(d, arc(gx, gy, 10, 9.6, 3.2, 1.6, 40, 170))
    d = union(d, capsule(gx, gy, 13.2, 9.6, 13.2, 6.4, 1.5))
    d = union(d, capsule(gx, gy, 10, 12.0, 10, 14.4, 1.4))
    return d


def i_eye_off(gx, gy):
    d = i_eye(gx, gy)
    return cut(d, capsule(gx, gy, 4.4, 15.6, 15.6, 4.4, 1.9))


def i_link(gx, gy):
    d = outline(gx, gy, 10, 10, 7.0, 5.6, 2.0, 1.5)
    d = union(d, capsule(gx, gy, 10.6, 9.4, 16.0, 4.0, 1.7))
    d = union(d, capsule(gx, gy, 12.4, 4.0, 16.4, 4.0, 1.7))
    d = union(d, capsule(gx, gy, 16.4, 4.0, 16.4, 8.0, 1.7))
    return d


def i_info(gx, gy):
    d = ring(gx, gy, 10, 10, 6.6, 1.5)
    d = union(d, disk(gx, gy, 10, 6.6, 1.15))
    d = union(d, capsule(gx, gy, 10, 9.4, 10, 13.8, 1.7))
    return d


def i_warn(gx, gy):
    d = poly_at(gx, gy, [(10, 2.8), (17.6, 16.4), (2.4, 16.4)])
    inner = poly_at(gx, gy, [(10, 6.4), (14.6, 14.4), (5.4, 14.4)])
    d = union(cut(d, inner), capsule(gx, gy, 10, 13.4, 10, 9.4, 1.6), disk(gx, gy, 10, 14.6, 0.1))
    return d


def i_clock(gx, gy):
    d = ring(gx, gy, 10, 10, 6.6, 1.5)
    d = union(d, capsule(gx, gy, 10, 10, 10, 6.4, 1.5))
    d = union(d, capsule(gx, gy, 10, 10, 13.2, 11.4, 1.5))
    return d


def i_pin(gx, gy):
    d = ring(gx, gy, 10, 8.4, 4.6, 1.6)
    d = union(d, capsule(gx, gy, 10, 12.6, 10, 17.6, 1.6))
    d = union(d, disk(gx, gy, 10, 8.4, 1.4))
    return d


def i_camera(gx, gy):
    d = outline(gx, gy, 10, 11.4, 7.2, 5.0, 2.0, 1.5)
    d = union(d, poly_at(gx, gy, [(3.6, 7.0), (5.2, 4.6), (9.0, 4.6), (10.2, 7.0)]))
    d = union(d, ring(gx, gy, 10, 11.6, 2.7, 1.5))
    return d


def i_bell(gx, gy):
    d = arc(gx, gy, 10, 10.6, 5.4, 1.6, 8, 172)
    d = union(d, capsule(gx, gy, 4.6, 10.6, 4.6, 15.0, 1.6))
    d = union(d, capsule(gx, gy, 15.4, 10.6, 15.4, 15.0, 1.6))
    d = union(d, capsule(gx, gy, 6.0, 15.4, 14.0, 15.4, 1.6))
    d = union(d, capsule(gx, gy, 10, 4.6, 10, 3.4, 1.5))
    return d


def i_shield(gx, gy):
    d = poly_at(gx, gy, [(10, 2.8), (16.4, 5.6), (15.6, 12.0), (10, 17.4), (4.4, 12.0), (3.6, 5.6)])
    inner = poly_at(gx, gy, [(10, 5.6), (14.0, 7.4), (13.4, 11.4), (10, 14.8), (6.6, 11.4), (6.0, 7.4)])
    return cut(d, inner)


def i_home(gx, gy):
    d = poly_at(gx, gy, [(10, 3.0), (17.4, 9.6), (15.0, 9.6), (15.0, 16.6), (5.0, 16.6), (5.0, 9.6), (2.6, 9.6)])
    inner = poly_at(gx, gy, [(10, 6.2), (13.0, 9.6), (13.0, 14.0), (7.0, 14.0), (7.0, 9.6)])
    return cut(d, inner)


def i_sun(gx, gy):
    d = ring(gx, gy, 10, 10, 3.6, 1.6)
    for i in range(8):
        rad = np.radians(i * 45)
        d = union(d, capsule(gx, gy, 10 + 5.6 * np.cos(rad), 10 - 5.6 * np.sin(rad),
                             10 + 7.4 * np.cos(rad), 10 - 7.4 * np.sin(rad), 1.4))
    return d


def i_crosshair(gx, gy):
    d = ring(gx, gy, 10, 10, 5.0, 1.5)
    d = union(d, capsule(gx, gy, 10, 10, 10, 3.4, 1.5))
    d = union(d, capsule(gx, gy, 10, 10, 10, 16.6, 1.5))
    d = union(d, capsule(gx, gy, 10, 10, 3.4, 10, 1.5))
    d = union(d, capsule(gx, gy, 10, 10, 16.6, 10, 1.5))
    return d


def i_particles(gx, gy):
    d = union(disk(gx, gy, 6.0, 6.4, 1.9), disk(gx, gy, 13.4, 8.0, 1.4),
              disk(gx, gy, 8.6, 13.2, 1.5), disk(gx, gy, 14.6, 14.4, 1.9),
              disk(gx, gy, 4.6, 15.6, 1.1))
    return d


def i_person(gx, gy):
    d = ring(gx, gy, 10, 7.4, 2.8, 1.6)
    d = union(d, arc(gx, gy, 10, 17.0, 5.8, 1.7, 20, 160))
    return d


def i_screenshot(gx, gy):
    d = outline(gx, gy, 10, 10.4, 7.0, 5.4, 2.0, 1.5)
    d = union(d, ring(gx, gy, 10, 10.4, 2.6, 1.5))
    return d


def i_star(gx, gy):
    pts = []
    for i in range(10):
        r = 7.6 if i % 2 == 0 else 3.3
        a = np.radians(90 + i * 36)
        pts.append((10 + r * np.cos(a), 10 - r * np.sin(a)))
    d = poly_at(gx, gy, pts)
    inner = poly_at(gx, gy, [(x * 0.72 + 10 * 0.28, y * 0.72 + 10 * 0.28) for x, y in pts])
    return cut(d, inner)


def i_dot(gx, gy):
    return disk(gx, gy, 10, 10, 2.7)


def i_server(gx, gy):
    d = None
    for cy in (5.6, 10.0, 14.4):
        b = outline(gx, gy, 10, cy, 6.6, 1.9, 1.2, 1.4)
        d = b if d is None else union(d, b)
        d = union(d, disk(gx, gy, 5.6, cy, 0.95))
    return d


def i_volume(gx, gy):
    d = poly_at(gx, gy, [(3.6, 8.0), (7.0, 8.0), (11.0, 4.6), (11.0, 15.4), (7.0, 12.0), (3.6, 12.0)])
    d = union(d, arc(gx, gy, 10.6, 10, 3.4, 1.5, -55, 55))
    d = union(d, arc(gx, gy, 10.6, 10, 5.8, 1.5, -50, 50))
    return d


def i_filter(gx, gy):
    d = poly_at(gx, gy, [(2.8, 4.4), (17.2, 4.4), (11.6, 10.6), (11.6, 16.0), (8.4, 17.4), (8.4, 10.6)])
    return d


def i_sort(gx, gy):
    d = None
    for y, x1 in ((5.4, 16.4), (10.0, 13.6), (14.6, 10.8)):
        c = capsule(gx, gy, 3.6, y, x1, y, 1.5)
        d = c if d is None else union(d, c)
    return d


def i_compass(gx, gy):
    d = ring(gx, gy, 10, 10, 6.6, 1.5)
    d = union(d, poly_at(gx, gy, [(13.6, 6.4), (8.6, 8.6), (6.4, 13.6), (11.4, 11.4)]))
    return d


def i_download(gx, gy):
    return union(capsule(gx, gy, 10, 3.6, 10, 12.4, 1.7),
                 capsule(gx, gy, 5.6, 8.4, 10, 12.8, 1.7),
                 capsule(gx, gy, 10, 12.8, 14.4, 8.4, 1.7),
                 capsule(gx, gy, 4.2, 16.6, 15.8, 16.6, 1.7))


def i_upload(gx, gy):
    return union(capsule(gx, gy, 10, 12.4, 10, 3.6, 1.7),
                 capsule(gx, gy, 5.6, 7.6, 10, 3.2, 1.7),
                 capsule(gx, gy, 10, 3.2, 14.4, 7.6, 1.7),
                 capsule(gx, gy, 4.2, 16.6, 15.8, 16.6, 1.7))


def i_chevron_left(gx, gy):
    return union(capsule(gx, gy, 12.6, 4.6, 7.4, 10.0, 1.8),
                 capsule(gx, gy, 7.4, 10.0, 12.6, 15.4, 1.8))


def i_chevron_up(gx, gy):
    return union(capsule(gx, gy, 4.6, 12.4, 10.0, 7.2, 1.8),
                 capsule(gx, gy, 10.0, 7.2, 15.4, 12.4, 1.8))


ICONS = [
    ("layers", i_layers), ("eye", i_eye), ("radial", i_radial), ("chat", i_chat),
    ("box", i_box), ("note", i_note), ("sparkle", i_sparkle), ("gauge", i_gauge),
    ("sliders", i_sliders), ("palette", i_palette), ("folder", i_folder), ("users", i_users),
    ("keyboard", i_keyboard), ("search", i_search), ("zoom", i_zoom), ("list", i_list),
    ("grid", i_grid), ("chevron", i_chevron), ("chevron_down", i_chevron_down),
    ("chevron_left", i_chevron_left), ("chevron_up", i_chevron_up), ("check", i_check),
    ("close", i_close), ("plus", i_plus), ("minus", i_minus), ("trash", i_trash),
    ("pencil", i_pencil), ("copy", i_copy), ("refresh", i_refresh), ("arrow_up", i_arrow_up),
    ("arrow_down", i_arrow_down), ("drag", i_drag), ("lock", i_lock), ("unlock", i_unlock),
    ("eye_off", i_eye_off), ("link", i_link), ("info", i_info), ("warn", i_warn),
    ("clock", i_clock), ("pin", i_pin), ("camera", i_camera), ("bell", i_bell),
    ("shield", i_shield), ("home", i_home), ("sun", i_sun), ("crosshair", i_crosshair),
    ("particles", i_particles), ("person", i_person), ("screenshot", i_screenshot), ("star", i_star),
    ("dot", i_dot), ("server", i_server), ("volume", i_volume), ("filter", i_filter),
    ("sort", i_sort), ("compass", i_compass), ("download", i_download), ("upload", i_upload),
]


def render_cell(fn) -> np.ndarray:
    size = CELL * SS
    gx, gy = _grid(size)
    gx = gx * (CELL / size) * SS / SS * 1.0
    gy = gy * 1.0
    gx, gy = _grid(size)
    # _grid already returns cell units (0..CELL)
    d = fn(gx, gy)
    alpha = np.clip(0.5 - d, 0.0, 1.0)
    return alpha.reshape(size, size)


def build_sheet():
    rows = (len(ICONS) + COLS - 1) // COLS
    sheet = np.zeros((rows * CELL, COLS * CELL), dtype=float)
    for index, (name, fn) in enumerate(ICONS):
        cell = render_cell(fn)
        # supersample down to the final cell size
        cell = cell.reshape(CELL, SS, CELL, SS).mean(axis=(1, 3))
        cx = (index % COLS) * CELL
        cy = (index // COLS) * CELL
        sheet[cy:cy + CELL, cx:cx + CELL] = cell
    return sheet


def write_png(path: str, rgba: np.ndarray) -> None:
    height, width = rgba.shape[0], rgba.shape[1]
    raw = b"".join(b"\x00" + rgba[y].tobytes() for y in range(height))

    def chunk(tag: bytes, data: bytes) -> bytes:
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF))

    header = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", header)
           + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)


def main() -> None:
    sheet = build_sheet()
    alpha = np.clip(sheet * 255.0, 0, 255).astype(np.uint8)
    rgba = np.zeros((*alpha.shape, 4), dtype=np.uint8)
    rgba[..., 0] = 255
    rgba[..., 1] = 255
    rgba[..., 2] = 255
    rgba[..., 3] = alpha
    out = os.path.join("src", "main", "resources", "assets", "chaosutils",
                       "textures", "gui", "icons.png")
    write_png(out, rgba)
    print(f"wrote {out}  {rgba.shape[1]}x{rgba.shape[0]}  cells={len(ICONS)} cols={COLS} cell={CELL}")

    if "--preview" in sys.argv:
        path = sys.argv[sys.argv.index("--preview") + 1]
        scale = 5
        rows = (len(ICONS) + COLS - 1) // COLS
        img = np.zeros((rows * CELL * scale, COLS * CELL * scale, 4), dtype=np.uint8)
        img[..., 0:3] = 14
        img[..., 3] = 255
        big = np.repeat(np.repeat(alpha, scale, axis=0), scale, axis=1)
        base = np.zeros_like(img)
        base[..., 0:3] = 190
        mask = big[..., None] / 255.0
        img = (base * mask + img * (1 - mask)).astype(np.uint8)
        write_png(path, img)
        print(f"wrote {path}")


if __name__ == "__main__":
    main()
