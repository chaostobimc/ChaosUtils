package dev.chaosutils.gui;

import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * The visual vocabulary of the ChaosUtils interface.
 *
 * <p>Everything the interface draws goes through one of these helpers, which is what keeps the look
 * consistent: one place for the window chrome, one for cards, one for hairlines. Shapes are composed
 * from {@code GuiGraphics#fill} scanlines, so nothing here depends on shader or pipeline internals
 * and every panel stays crisp at any GUI scale.
 */
public final class Ui {
	private Ui() {
	}

	// ------------------------------------------------------------------ window

	/**
	 * Floating window: a deep drop shadow, an almost opaque gradient body and a hairline outline.
	 * The accent hairline along the top edge is what gives the panel its identity without adding
	 * another colour to the palette.
	 */
	public static void window(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float appear) {
		if (appear <= 0.01F) {
			return;
		}
		if (theme.windowShadow) {
			Render.softShadow(graphics, x, y, width, height, radius, 1.35F * theme.shadowStrength * appear);
		}
		Render.roundedRectGradient(graphics, x, y, width, height, radius,
				Render.alpha(theme.windowTop, appear), Render.alpha(theme.windowBottom, appear));
		// bottom inner sheen - a single, very soft gradient, no noise
		Render.roundedRectGradient(graphics, x + 1.0F, y + height * 0.55F, width - 2.0F, height * 0.45F - 1.0F,
				Math.max(0.0F, radius - 1.0F), Render.alpha(theme.accent, 0.0F), Render.alpha(theme.accent, 0.045F * appear));
		Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.outline, appear));
		accentStrip(graphics, x, y, width, radius, 2.0F, theme, appear);
	}

	/** Accent hairline along the top edge of the window, fading out towards both corners. */
	public static void accentStrip(GuiGraphics graphics, float x, float y, float width, float radius, float height,
			UiTheme theme, float alpha) {
		float inset = Math.max(1.0F, radius * 0.55F);
		float span = width - inset * 2.0F;
		if (span <= 2.0F || alpha <= 0.01F) {
			return;
		}
		float peak = Render.alpha(theme.accent, 0.85F * alpha);
		float clear = Render.alpha(theme.accent, 0.0F);
		float half = span * 0.5F;
		Render.horizontalGradient(graphics, x + inset, y + 1.0F, half, height, clear, peak);
		Render.horizontalGradient(graphics, x + inset + half, y + 1.0F, half, height, peak, clear);
	}

	/** Sidebar surface: only the corners on {@code left} are rounded (scissor-based). */
	public static void sideSurface(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			boolean left, int top, int bottom) {
		float over = radius + 2.0F;
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, left ? x : x - over, y, width + over, height, radius, top, bottom);
		Render.unscissor(graphics);
	}

	/** Header strip at the top of a window: slightly darker, closing with a hairline. */
	public static void header(GuiGraphics graphics, float x, float y, float width, float height, float radius, UiTheme theme, float appear) {
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, x, y, width, height + radius, radius,
				Render.alpha(0xFF000000, 0.30F * appear), Render.alpha(0x000000, 0.0F));
		Render.unscissor(graphics);
		Render.rect(graphics, x + radius, y + height - 1.0F, width - radius * 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, appear));
	}

	// ------------------------------------------------------------------- cards

	/** Card surface with hover lift; {@code active} draws the accent tint and outline. */
	public static void card(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float hover, boolean active, float alpha) {
		int base = active ? theme.cardActive : Render.mix(theme.card, theme.cardHover, hover);
		Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(0x000000, base, alpha));
		int border = active ? Render.mix(theme.outline, theme.accent, 0.55F)
				: Render.mix(theme.outline, theme.outlineStrong, hover);
		Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.mix(0x000000, border, alpha));
	}

	/** Sidebar entry: flat when idle, filled when hovered, accent-tinted when selected. */
	public static void navRow(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float selected, float hover, UiTheme theme, float alpha) {
		if (selected <= 0.01F && hover <= 0.01F) {
			return;
		}
		int idle = Render.alpha(0xFFFFFFFF, 0.045F * hover * alpha);
		int active = Render.alpha(Render.mix(theme.accent, theme.cardHover, 0.72F), 0.55F * alpha);
		Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(idle, active, selected));
		if (selected > 0.01F) {
			Render.ring(graphics, x, y, width, height, radius, 1.0F,
					Render.alpha(Render.mix(theme.accent, 0xFFFFFFFF, 0.15F), 0.35F * selected * alpha));
		}
	}

	/** Switch track plus knob; {@code progress} animates the knob from left to right. */
	public static void pill(GuiGraphics graphics, float x, float y, float width, float height, float progress,
			boolean enabled, int accent, int off, int knob, float alpha) {
		float radius = height * 0.5F;
		int track = enabled ? Render.mix(off, accent, Math.max(0.35F, progress)) : off;
		Render.roundedRect(graphics, x, y, width, height, radius, Render.alpha(track, alpha));
		float travel = width - height;
		float centerX = x + radius + travel * dev.chaosutils.util.Anim.clamp01(progress);
		Ui.knob(graphics, centerX, y + radius, radius - 2.0F, knob, alpha);
	}

	/** Rounded square behind an item or vector icon, tinted with the owner's accent colour. */
	public static void iconTile(GuiGraphics graphics, float x, float y, float size, float radius, int color, float alpha) {
		Render.roundedRect(graphics, x, y, size, size, radius, Render.alpha(color, 0.14F * alpha));
		Render.ring(graphics, x, y, size, size, radius, 1.0F, Render.alpha(color, 0.26F * alpha));
	}

	/** Draws a vector icon from the bundled atlas inside a tile. */
	public static void icon(GuiGraphics graphics, UiIcons icon, float x, float y, float size, int color, float alpha) {
		icon.draw(graphics, x, y, size, size, Render.alpha(color, alpha));
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
		Render.rect(graphics, x, y, width, 1.0F, Render.alpha(theme.outlineSoft, alpha));
	}

	/** Very small uppercase section caption, the way modern interfaces label groups. */
	public static void sectionLabel(GuiGraphics graphics, Font font, String text, float x, float y, int color, float alpha) {
		Render.text(graphics, font, text.toUpperCase(java.util.Locale.ROOT), x, y, Render.alpha(color, alpha), false);
	}

	/** Chevron used for expanders and dropdowns; {@code rotation} is in degrees (0 = pointing right). */
	public static void chevron(GuiGraphics graphics, float centerX, float centerY, float size, float rotation, int color) {
		if (rotation >= 45.0F && rotation < 135.0F) {
			UiIcons.CHEVRON_DOWN.drawCentered(graphics, centerX, centerY, size, color);
			return;
		}
		if (rotation >= 135.0F && rotation < 225.0F) {
			UiIcons.CHEVRON_LEFT.drawCentered(graphics, centerX, centerY, size, color);
			return;
		}
		if (rotation >= 225.0F && rotation < 315.0F) {
			UiIcons.CHEVRON_UP.drawCentered(graphics, centerX, centerY, size, color);
			return;
		}
		UiIcons.CHEVRON.drawCentered(graphics, centerX, centerY, size, color);
	}

	/** Check mark used by toggles and lists. */
	public static void check(GuiGraphics graphics, float centerX, float centerY, float size, int color) {
		UiIcons.CHECK.drawCentered(graphics, centerX, centerY, size, color);
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
		Render.circle(graphics, centerX - radius * 0.16F, centerY - radius * 0.2F, radius * 0.55F,
				Render.alpha(0xFFFFFFFF, 0.20F * alpha));
	}
}
