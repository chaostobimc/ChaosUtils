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

	/** Local clamp used by the alpha/fade helpers below. */
	private static float clamp01(float value) {
		return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
	}

	public static int rgba(int r, int g, int b, int a) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	/**
	 * Draws {@code color} with the given alpha.
	 *
	 * <p>Colours written without an alpha byte ({@code 0xRRGGBB}, and {@code 0x000000}) count as
	 * fully opaque, exactly like they read. This matters: {@code 0x12131C} has an alpha byte of
	 * zero, so multiplying would make every dark surface - panel fills, the modal dim, the radial
	 * hub - completely invisible. Colours that do carry an alpha byte keep it and the value scales
	 * it, which is what all the translucent theme tokens rely on.
	 */
	public static int alpha(int color, float alpha) {
		int base = (color >>> 24) & 0xFF;
		if (base == 0) {
			base = 0xFF;
		}
		int a = Math.round(Anim.clamp01(alpha) * base);
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
	 * Filled ring segment (annulus sector).
	 *
	 * <p>Rendered as a scanline fill of the underlying trapezoid, subdivided every 15° so the
	 * straight inner/outer chords stay within a fraction of a pixel of the true arc. That is
	 * what makes the radial menu look like a drawn shape instead of a fan of blocks: every row
	 * is a single quad and neighbouring slices can never bleed into each other.
	 *
	 * <p>Angles are in degrees, {@code 0} pointing up and growing clockwise.
	 */
	public static void arc(GuiGraphics graphics, float centerX, float centerY, float innerRadius, float outerRadius,
			float startDegrees, float endDegrees, int color) {
		if (outerRadius <= innerRadius || alphaOf(color) == 0) {
			return;
		}
		float span = endDegrees - startDegrees;
		if (Math.abs(span) < 0.02F) {
			return;
		}
		int parts = Math.max(1, (int) Math.ceil(Math.abs(span) / 15.0F));
		float step = span / parts;
		for (int i = 0; i < parts; i++) {
			trapezoid(graphics, centerX, centerY, innerRadius, outerRadius,
					startDegrees + i * step, startDegrees + (i + 1) * step, color);
		}
	}

	private static void trapezoid(GuiGraphics graphics, float centerX, float centerY, float innerRadius,
			float outerRadius, float startDegrees, float endDegrees, int color) {
		double start = Math.toRadians(startDegrees);
		double end = Math.toRadians(endDegrees);
		float[] xs = new float[4];
		float[] ys = new float[4];
		setCorner(xs, ys, 0, centerX, centerY, innerRadius, start);
		setCorner(xs, ys, 1, centerX, centerY, outerRadius, start);
		setCorner(xs, ys, 2, centerX, centerY, outerRadius, end);
		setCorner(xs, ys, 3, centerX, centerY, innerRadius, end);
		float minY = Math.min(Math.min(ys[0], ys[1]), Math.min(ys[2], ys[3]));
		float maxY = Math.max(Math.max(ys[0], ys[1]), Math.max(ys[2], ys[3]));
		if (maxY - minY > 240.0F) {
			// Safety valve: never let a malformed angle flood the screen with rows.
			return;
		}
		int y0 = (int) Math.floor(minY);
		int y1 = (int) Math.ceil(maxY);
		int rows = Math.max(1, y1 - y0);
		int rowStep = rows > 160 ? 3 : (rows > 80 ? 2 : 1);
		float half = rowStep * 0.5F;
		for (int row = y0; row < y1; row += rowStep) {
			float sample = row + half;
			float left = Float.MAX_VALUE;
			float right = -Float.MAX_VALUE;
			for (int edge = 0; edge < 4; edge++) {
				int next = (edge + 1) & 3;
				float ay = ys[edge];
				float by = ys[next];
				if ((ay <= sample && by >= sample) || (by <= sample && ay >= sample)) {
					float t = Math.abs(by - ay) < 1.0E-4F ? 0.5F : (sample - ay) / (by - ay);
					float x = xs[edge] + (xs[next] - xs[edge]) * t;
					left = Math.min(left, x);
					right = Math.max(right, x);
				}
			}
			if (right > left) {
				rect(graphics, left, row, right - left, rowStep + 0.5F, color);
			}
		}
	}

	private static void setCorner(float[] xs, float[] ys, int index, float centerX, float centerY, float radius, double angle) {
		xs[index] = centerX + (float) Math.sin(angle) * radius;
		ys[index] = centerY - (float) Math.cos(angle) * radius;
	}

	/** Filled circle (scanline, so the outline stays smooth at any size). */
	public static void circle(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		if (radius <= 0.5F || alphaOf(color) == 0) {
			return;
		}
		int y0 = Math.round(centerY - radius);
		int y1 = Math.round(centerY + radius);
		for (int row = y0; row <= y1; row++) {
			float dy = row + 0.5F - centerY;
			float half = radius * radius - dy * dy;
			if (half <= 0.0F) {
				continue;
			}
			half = (float) Math.sqrt(half);
			rect(graphics, centerX - half, row, half * 2.0F, 1.0F, color);
		}
	}

	/** Ring outline drawn from four edges and four quarter arcs (cheap, stays crisp). */
	public static void ring(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float thickness, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		float t = Math.max(1.0F, thickness);
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		if (r < 1.0F) {
			rect(graphics, x, y, width, t, color);
			rect(graphics, x, y + height - t, width, t, color);
			rect(graphics, x, y, t, height, color);
			rect(graphics, x + width - t, y, t, height, color);
			return;
		}
		rect(graphics, x + r, y, width - r * 2.0F, t, color);
		rect(graphics, x + r, y + height - t, width - r * 2.0F, t, color);
		rect(graphics, x, y + r, t, height - r * 2.0F, color);
		rect(graphics, x + width - t, y + r, t, height - r * 2.0F, color);
		int steps = Math.max(3, Math.round(r));
		for (int i = 0; i <= steps; i++) {
			double angle = Math.PI * 0.5 * i / steps;
			float ox = (float) Math.cos(angle) * r;
			float oy = (float) Math.sin(angle) * r;
			// top-left, top-right, bottom-right, bottom-left
			rect(graphics, x + r - ox, y + r - oy, t, t, color);
			rect(graphics, x + width - r + ox - t, y + r - oy, t, t, color);
			rect(graphics, x + width - r + ox - t, y + height - r + oy - t, t, t, color);
			rect(graphics, x + r - ox, y + height - r + oy - t, t, t, color);
		}
	}

	/**
	 * Rounded rectangle filled with a vertical gradient.
	 *
	 * <p>Two-pixel scanlines with the corner inset applied per row, which is both smoother and
	 * cheaper than stacking a flat rounded rect and a gradient on top of each other.
	 */
	public static void roundedRectGradient(GuiGraphics graphics, float x, float y, float width, float height,
			float radius, int top, int bottom) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		int rows = Math.max(1, Math.round(height));
		int step = rows > 140 ? 2 : 1;
		for (int row = 0; row < rows; row += step) {
			float inset = Math.max(cornerInset(row, rows, r), cornerInset(rows - 1 - row, rows, r));
			int color = mix(top, bottom, (row + step * 0.5F) / (float) rows);
			rect(graphics, x + inset, y + row, width - inset * 2.0F, step + 0.5F, color);
		}
	}

	private static float cornerInset(int row, int rows, float radius) {
		float r = Math.min(radius, rows * 0.5F);
		if (r < 1.0F || row >= r) {
			return 0.0F;
		}
		float dy = Math.max(0.0F, r - row - 0.5F);
		return (float) (r - Math.sqrt(Math.max(0.0, r * r - dy * dy)));
	}

	/**
	 * Soft drop shadow: a handful of oversized rounded rectangles with a low alpha each.
	 * Much cheaper than a blur pass and it scales with the window without a texture.
	 */
	public static void softShadow(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float strength) {
		float s = clamp01(strength);
		if (s <= 0.01F || width <= 0.0F || height <= 0.0F) {
			return;
		}
		int layers = 6;
		for (int i = layers; i >= 1; i--) {
			float spread = i * 1.8F;
			int alpha = Math.round(11.0F * s * (1.0F - i / (float) (layers + 1)) + 4.0F * s);
			roundedRect(graphics, x - spread, y - spread + 2.5F, width + spread * 2.0F, height + spread * 2.0F,
					radius + spread * 0.8F, (alpha & 0xFF) << 24);
		}
	}

	/**
	 * Accent glow. Drawn with a tinted blit of the radial {@code glow.png} that ships with the
	 * mod (one quad), so highlights stay soft instead of banding like stacked rectangles.
	 * Falls back to concentric circles if the texture cannot be resolved.
	 */
	private static final net.minecraft.resources.Identifier GLOW_TEXTURE =
			net.minecraft.resources.Identifier.fromNamespaceAndPath("chaosutils", "textures/gui/glow.png");
	private static boolean glowTextureAvailable = true;

	public static void glow(GuiGraphics graphics, float centerX, float centerY, float radius, int color, float strength) {
		float s = clamp01(strength);
		if (radius < 2.0F || s <= 0.01F || alphaOf(color) == 0) {
			return;
		}
		int argb = scaleAlpha(color, s);
		if (glowTextureAvailable && blitTexture(graphics, GLOW_TEXTURE, centerX - radius, centerY - radius,
				radius * 2.0F, radius * 2.0F, argb)) {
			return;
		}
		glowTextureAvailable = false;
		int steps = 5;
		for (int i = steps; i >= 1; i--) {
			circle(graphics, centerX, centerY, radius * i / steps, scaleAlpha(color, s * 0.12F));
		}
	}

	/** Soft accent halo behind a rounded panel; cheap and works without any texture. */
	public static void halo(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			int color, float strength) {
		float s = clamp01(strength);
		if (s <= 0.01F || width <= 0.0F || height <= 0.0F) {
			return;
		}
		int layers = 5;
		for (int i = layers; i >= 1; i--) {
			float spread = i * 2.0F;
			roundedRect(graphics, x - spread, y - spread, width + spread * 2.0F, height + spread * 2.0F,
					radius + spread * 0.9F, scaleAlpha(color, s * 0.075F * (1.0F - i / (float) (layers + 1))));
		}
	}

	/**
	 * Tinted texture blit through the vanilla GUI pipeline.
	 *
	 * @return {@code true} when the texture was drawn, {@code false} when it could not be found
	 *         (the caller then uses its geometric fallback).
	 */
	public static boolean blitTexture(GuiGraphics graphics, net.minecraft.resources.Identifier texture,
			float x, float y, float width, float height, int color) {
		if (width < 1.0F || height < 1.0F) {
			return false;
		}
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + width);
		int y1 = Math.round(y + height);
		try {
			if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(texture).isEmpty()) {
				return false;
			}
			graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x0, y0, 0.0F, 0.0F,
					x1 - x0, y1 - y0, 128, 128, 128, 128, color);
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}

	/** Tint colour over an existing area - used for hover highlights that must not hide content. */
	public static void wash(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color, float amount) {
		roundedRect(graphics, x, y, width, height, radius, scaleAlpha(color, amount));
	}

	// ------------------------------------------------------------ text helpers

	/** Truncates with an ellipsis so it fits {@code maxWidth} device pixels. */
	public static String ellipsize(net.minecraft.client.gui.Font font, String value, float maxWidth) {
		if (value == null) {
			return "";
		}
		if (font.width(value) <= maxWidth) {
			return value;
		}
		String result = value;
		while (result.length() > 4 && font.width(result + "…") > maxWidth) {
			result = result.substring(0, result.length() - 2);
		}
		return result + "…";
	}

	/**
	 * Heavier text without giving up the colour control: drawing the string twice with a half
	 * pixel offset is exactly what the vanilla bold style does, but it keeps our own alpha.
	 */
	public static void boldText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float x, float y,
			int color, boolean shadow) {
		text(graphics, font, value, x, y, color, shadow);
		text(graphics, font, value, x + 0.7F, y, color, shadow);
	}

	/** Horizontal gradient text, drawn in three-character runs to keep the draw count tiny. */
	public static void gradientText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value,
			float x, float y, int from, int to, boolean shadow) {
		if (value == null || value.isEmpty()) {
			return;
		}
		int length = value.length();
		float cursor = x;
		int run = 3;
		for (int start = 0; start < length; start += run) {
			String part = value.substring(start, Math.min(length, start + run));
			int color = mix(from, to, start / (float) Math.max(1, length - 1));
			text(graphics, font, part, cursor, y, color, shadow);
			cursor += font.width(part);
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
