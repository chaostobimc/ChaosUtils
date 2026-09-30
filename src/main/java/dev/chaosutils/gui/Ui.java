package dev.chaosutils.gui;

import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * The visual vocabulary of the ChaosUtils interface.
 *
 * <p>Everything the interface draws goes through one of these helpers, which is what keeps the
 * look consistent: one place for the window chrome, one for cards, one for hairlines. Shapes are
 * composed from {@code GuiGraphics#fill} scanlines and two small tinted textures, so nothing here
 * depends on shader or pipeline internals.
 */
public final class Ui {
	private Ui() {
	}

	// ------------------------------------------------------------------ window

	/** Floating window: drop shadow, gradient body, top sheen and a hairline outline. */
	public static void window(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float appear) {
		if (appear <= 0.01F) {
			return;
		}
		if (theme.glow) {
			Render.glow(graphics, x + width * 0.5F, y + 10.0F, Math.min(width, height) * 0.58F, theme.accent,
					0.16F * appear);
		}
		if (theme.windowShadow) {
			Render.softShadow(graphics, x, y, width, height, radius, theme.shadowStrength * 1.1F * appear);
		}
		Render.roundedRectGradient(graphics, x, y, width, height, radius,
				Render.alpha(theme.windowTop, appear), Render.alpha(theme.windowBottom, appear));
		Render.roundedRectGradient(graphics, x + 1.0F, y + 1.0F, width - 2.0F, Math.min(30.0F, height * 0.25F),
				Math.max(0.0F, radius - 1.0F), Render.alpha(0xFFFFFFFF, 0.05F * appear),
				Render.alpha(0xFFFFFFFF, 0.0F));
		Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.outlineStrong, appear));
	}

	/** Sidebar surface: only the corners on {@code left} are rounded (scissor-based). */
	public static void sideSurface(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			boolean left, int top, int bottom) {
		float over = radius + 2.0F;
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, left ? x : x - over, y, width + over, height, radius, top, bottom);
		Render.unscissor(graphics);
	}

	/** Header strip at the top of a window: slightly lighter, fades into the body. */
	public static void header(GuiGraphics graphics, float x, float y, float width, float height, float radius, UiTheme theme, float appear) {
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, x, y, width, height + radius, radius,
				Render.alpha(0xFFFFFFFF, 0.028F * appear), Render.alpha(0xFFFFFFFF, 0.0F));
		Render.unscissor(graphics);
		Render.rect(graphics, x + radius, y + height - 1.0F, width - radius * 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, appear));
	}

	// ------------------------------------------------------------------- cards

	/** Card surface with hover lift; {@code active} draws the accent tint and outline. */
	public static void card(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float hover, boolean active, float alpha) {
		int base = active ? theme.cardActive : Render.mix(theme.card, theme.cardHover, hover);
		if (theme.glow && (hover > 0.02F || active)) {
			Render.halo(graphics, x, y, width, height, radius, theme.accent, (active ? 0.55F : 0.35F) * hover + (active ? 0.2F : 0.0F));
		}
		Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(0x00000000, base, alpha));
		Render.ring(graphics, x, y, width, height, radius, 1.0F,
				Render.mix(Render.alpha(theme.outlineSoft, alpha),
						Render.alpha(active ? theme.accentSoft : theme.outlineStrong, alpha), Math.max(hover, active ? 1.0F : 0.0F)));
	}

	/** Rounded square behind an item icon, tinted with the owner's accent colour. */
	public static void iconTile(GuiGraphics graphics, float x, float y, float size, float radius, int color, float alpha) {
		Render.roundedRect(graphics, x, y, size, size, radius, Render.alpha(color, 0.16F * alpha));
		Render.ring(graphics, x, y, size, size, radius, 1.0F, Render.alpha(color, 0.34F * alpha));
	}

	public static void itemIcon(GuiGraphics graphics, ItemStack stack, float centerX, float centerY, float scale, float alpha) {
		if (stack == null || stack.isEmpty() || alpha <= 0.02F) {
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().scale(scale, scale);
		graphics.pose().translate(-centerX, -centerY);
		Render.item(graphics, stack, centerX - 8.0F, centerY - 8.0F);
		graphics.pose().popMatrix();
	}

	// ----------------------------------------------------------------- strokes

	public static void divider(GuiGraphics graphics, float x, float y, float width, UiTheme theme, float alpha) {
		Render.roundedRect(graphics, x, y, width, 1.0F, 0.5F, Render.alpha(theme.outlineSoft, alpha));
	}

	/** Very small uppercase section caption, the way modern interfaces label groups. */
	public static void sectionLabel(GuiGraphics graphics, Font font, String text, float x, float y, int color, float alpha) {
		Render.text(graphics, font, text.toUpperCase(java.util.Locale.ROOT), x, y, Render.alpha(color, alpha), false);
	}

	/** Chevron used for expanders and dropdowns; {@code rotation} is in degrees (0 = pointing right). */
	public static void chevron(GuiGraphics graphics, float centerX, float centerY, float size, float rotation, int color) {
		float half = size * 0.5F;
		float thickness = Math.max(1.4F, size * 0.24F);
		double angle = Math.toRadians(rotation);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		// Two arms of a ">" rotated around the centre.
		float ax = -half * cos;
		float ay = -half * sin;
		float bx = half * cos;
		float by = half * sin;
		float cx = bx - size * sin * 0.55F;
		float cy = by + size * cos * 0.55F;
		Render.line(graphics, centerX + ax, centerY + ay, centerX + bx, centerY + by, thickness, color);
		Render.line(graphics, centerX + bx, centerY + by, centerX + cx, centerY + cy, thickness, color);
	}

	/** Check mark used by toggles and lists. */
	public static void check(GuiGraphics graphics, float centerX, float centerY, float size, int color) {
		float thickness = Math.max(1.5F, size * 0.22F);
		Render.line(graphics, centerX - size * 0.42F, centerY + size * 0.02F, centerX - size * 0.10F, centerY + size * 0.34F,
				thickness, color);
		Render.line(graphics, centerX - size * 0.10F, centerY + size * 0.34F, centerX + size * 0.44F, centerY - size * 0.34F,
				thickness, color);
	}

	public static void dot(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		Render.circle(graphics, centerX, centerY, radius, color);
	}

	/** Thin horizontal progress track; {@code fraction} fills it from the left. */
	public static void track(GuiGraphics graphics, float x, float y, float width, float height, float fraction,
			int fill, int background) {
		Render.roundedRect(graphics, x, y, width, height, height * 0.5F, background);
		float filled = Math.max(0.0F, Math.min(1.0F, fraction)) * width;
		if (filled > 0.5F) {
			Render.roundedRect(graphics, x, y, Math.max(height, filled), height, height * 0.5F, fill);
		}
	}

	/**
	 * Glossy knob used by toggles and sliders: a filled circle with a soft inner highlight so it
	 * reads as a physical object instead of a flat dot.
	 */
	public static void knob(GuiGraphics graphics, float centerX, float centerY, float radius, int color, float alpha) {
		Render.circle(graphics, centerX, centerY, radius, Render.alpha(color, alpha));
		Render.circle(graphics, centerX - radius * 0.18F, centerY - radius * 0.22F, radius * 0.62F,
				Render.alpha(0xFFFFFFFF, 0.22F * alpha));
	}
}
