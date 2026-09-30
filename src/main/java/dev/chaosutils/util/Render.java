package dev.chaosutils.util;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing helpers on top of {@link GuiGraphics}.
 *
 * <p>Only the long-stable primitive ({@code fill}, scissor and string drawing) is used:
 * rounded corners and gradients are composed from a handful of rectangles instead of
 * shaders or blit calls, which keeps ChaosUtils compatible across 1.21.x render changes
 * and costs nothing measurable (a rounded rect is ~20 quads).
 */
public final class Render {
	private Render() {
	}

	// ------------------------------------------------------------------ colors

	public static int rgba(int r, int g, int b, int a) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	public static int alpha(int color, float alpha) {
		int a = Math.round(Anim.clamp01(alpha) * ((color >>> 24) & 0xFF));
		return (color & 0xFFFFFF) | a << 24;
	}

	public static int scaleAlpha(int color, float factor) {
		int a = Math.round(Anim.clamp(((color >>> 24) & 0xFF) * factor, 0.0F, 255.0F));
		return (color & 0xFFFFFF) | a << 24;
	}

	public static int mix(int from, int to, float t) {
		float x = Anim.clamp01(t);
		int a = Math.round(Anim.lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, x));
		int r = Math.round(Anim.lerp((from >> 16) & 0xFF, (to >> 16) & 0xFF, x));
		int g = Math.round(Anim.lerp((from >> 8) & 0xFF, (to >> 8) & 0xFF, x));
		int b = Math.round(Anim.lerp(from & 0xFF, to & 0xFF, x));
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static int brighten(int color, float amount) {
		return mix(color, 0xFFFFFFFF, amount);
	}

	public static int darken(int color, float amount) {
		return mix(color, 0xFF000000, amount);
	}

	public static int fromHsv(float hue, float saturation, float value) {
		return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, saturation, value) & 0xFFFFFF;
	}

	public static int withHue(int color, float hue) {
		return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.55F, 0.95F) & 0xFFFFFF;
	}

	public static float[] toHsv(int color) {
		float[] hsv = new float[3];
		java.awt.Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
		return hsv;
	}

	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	public static int blue(int color) {
		return color & 0xFF;
	}

	public static int alphaOf(int color) {
		return (color >>> 24) & 0xFF;
	}

	// ---------------------------------------------------------------- geometry

	public static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + width);
		int y1 = Math.round(y + height);
		graphics.fill(x0, y0, x1, y1, color);
	}

	/** Rounded rectangle built from scanlines; {@code radius} is clamped to half the box. */
	public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		if (r < 1.0F) {
			rect(graphics, x, y, width, height, color);
			return;
		}
		int steps = Math.max(2, Math.round(r));
		float stepHeight = r / steps;
		// Top corners
		for (int i = 0; i < steps; i++) {
			float dy = r - (i + 0.5F) * stepHeight;
			float inset = r - (float) Math.sqrt(Math.max(0.0, r * r - dy * dy));
			float rowY = y + i * stepHeight;
			rect(graphics, x + inset, rowY, width - inset * 2.0F, stepHeight + 0.5F, color);
		}
		int bodyTop = Math.round(y + r);
		int bodyBottom = Math.round(y + height - r);
		if (bodyBottom > bodyTop) {
			rect(graphics, x, bodyTop, width, bodyBottom - bodyTop, color);
		}
		// Bottom corners
		for (int i = 0; i < steps; i++) {
			float dy = r - (i + 0.5F) * stepHeight;
			float inset = r - (float) Math.sqrt(Math.max(0.0, r * r - dy * dy));
			float rowY = y + height - r + i * stepHeight;
			rect(graphics, x + inset, rowY, width - inset * 2.0F, stepHeight + 0.5F, color);
		}
	}

	/**
	 * Straight line drawn as small squares along the segment.
	 *
	 * <p>Used for rotated HUD shapes (off screen arrows, compass needles) without touching
	 * the render pipeline: one {@code fill} per step, and the step count is bounded.
	 */
	public static void line(GuiGraphics graphics, float x0, float y0, float x1, float y1, float thickness, int color) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if (length < 0.01F || alphaOf(color) == 0) {
			return;
		}
		int steps = Math.max(1, Math.min(48, Math.round(length / Math.max(1.0F, thickness * 0.5F))));
		float half = Math.max(0.5F, thickness * 0.5F);
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			rect(graphics, x0 + dx * t - half, y0 + dy * t - half, thickness, thickness, color);
		}
	}

	/**
	 * Filled ring segment (annulus sector), drawn as a fan of short radial bars.
	 *
	 * <p>This is what the radial menu is built from: the number of bars is derived from the
	 * angle so a 45° slice costs roughly twenty quads and the result looks smooth, while the
	 * whole menu stays a handful of draw calls.
	 */
	public static void arc(GuiGraphics graphics, float centerX, float centerY, float innerRadius, float outerRadius,
			float startDegrees, float endDegrees, int color) {
		if (outerRadius <= innerRadius || alphaOf(color) == 0) {
			return;
		}
		float span = endDegrees - startDegrees;
		if (Math.abs(span) < 0.05F) {
			return;
		}
		int steps = Math.max(2, Math.min(64, Math.round(Math.abs(span) / 3.0F)));
		float stepSpan = span / steps;
		float midRadius = (innerRadius + outerRadius) * 0.5F;
		float thickness = Math.max(2.0F, (float) (Math.abs(Math.toRadians(stepSpan)) * midRadius));
		for (int i = 0; i < steps; i++) {
			double angle = Math.toRadians(startDegrees + (i + 0.5F) * stepSpan);
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			line(graphics, centerX + sin * innerRadius, centerY - cos * innerRadius,
					centerX + sin * outerRadius, centerY - cos * outerRadius, thickness, color);
		}
	}

	/** Border drawn by filling the outline colour and punching the background back in. */
	public static void roundedBorder(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float thickness, int borderColor, int backgroundColor) {
		roundedRect(graphics, x, y, width, height, radius, borderColor);
		roundedRect(graphics, x + thickness, y + thickness, width - thickness * 2.0F, height - thickness * 2.0F,
				Math.max(0.0F, radius - thickness), backgroundColor);
	}

	/** Vertical gradient drawn as a small number of strips (no shader, no allocations). */
	public static void verticalGradient(GuiGraphics graphics, float x, float y, float width, float height, int top, int bottom) {
		int strips = Math.max(2, Math.min(32, Math.round(height / 2.0F)));
		float stripHeight = height / strips;
		for (int i = 0; i < strips; i++) {
			int color = mix(top, bottom, (i + 0.5F) / strips);
			rect(graphics, x, y + i * stripHeight, width, stripHeight + 0.5F, color);
		}
	}

	public static void horizontalGradient(GuiGraphics graphics, float x, float y, float width, float height, int left, int right) {
		int strips = Math.max(2, Math.min(32, Math.round(width / 2.0F)));
		float stripWidth = width / strips;
		for (int i = 0; i < strips; i++) {
			int color = mix(left, right, (i + 0.5F) / strips);
			rect(graphics, x + i * stripWidth, y, stripWidth + 0.5F, height, color);
		}
	}

	public static void shadowedPanel(GuiGraphics graphics, float x, float y, float width, float height, float radius, int background, int outline) {
		roundedRect(graphics, x + 1.5F, y + 2.5F, width, height, radius, 0x40000000);
		if (alphaOf(outline) != 0) {
			roundedBorder(graphics, x, y, width, height, radius, 1.0F, outline, background);
		} else {
			roundedRect(graphics, x, y, width, height, radius, background);
		}
	}

	public static void scissor(GuiGraphics graphics, float x, float y, float width, float height) {
		graphics.enableScissor(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height));
	}

	public static void unscissor(GuiGraphics graphics) {
		graphics.disableScissor();
	}

	// ------------------------------------------------------------------ text

	public static void text(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float x, float y, int color, boolean shadow) {
		graphics.drawString(font, value, Math.round(x), Math.round(y), color, shadow);
	}

	public static void text(GuiGraphics graphics, net.minecraft.client.gui.Font font, net.minecraft.network.chat.Component value, float x, float y, int color, boolean shadow) {
		graphics.drawString(font, value, Math.round(x), Math.round(y), color, shadow);
	}

	public static void centeredText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float centerX, float y, int color, boolean shadow) {
		graphics.drawCenteredString(font, value, Math.round(centerX), Math.round(y), color);
	}

	public static int textWidth(net.minecraft.client.gui.Font font, String value) {
		return font.width(value);
	}

	public static void item(GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, float x, float y) {
		if (!stack.isEmpty()) {
			graphics.renderItem(stack, Math.round(x), Math.round(y));
		}
	}

	public static void itemDecorations(GuiGraphics graphics, net.minecraft.client.gui.Font font, net.minecraft.world.item.ItemStack stack, float x, float y) {
		if (!stack.isEmpty()) {
			graphics.renderItemDecorations(font, stack, Math.round(x), Math.round(y));
		}
	}
}
