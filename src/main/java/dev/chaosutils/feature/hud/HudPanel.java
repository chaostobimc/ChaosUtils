package dev.chaosutils.feature.hud;

import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing helpers shared by every overlay: a consistent panel background, progress bars and
 * text with the user's preferred shadow setting.
 *
 * <p>Everything is plain quad/text drawing, so overlays cost a handful of draw calls and
 * never touch the render pipeline API.
 */
public final class HudPanel {
	private HudPanel() {
	}

	public static float scale() {
		return HudStyleModule.hudScale.getFloat();
	}

	public static float padding() {
		return HudStyleModule.padding.getFloat();
	}

	public static boolean visibleNow() {
		if (!HudStyleModule.hideInScreens.get()) {
			return true;
		}
		return net.minecraft.client.Minecraft.getInstance().screen == null;
	}

	public static void panel(GuiGraphics graphics, Font font, float x, float y, float width, float height, int accent) {
		float opacity = HudStyleModule.backgroundOpacity.getFloat();
		int background = Render.alpha(HudStyleModule.backgroundColor.get() & 0xFFFFFF | 0xFF000000, opacity);
		float radius = HudStyleModule.cornerRadius.getFloat();
		if (opacity > 0.01F) {
			Render.roundedRect(graphics, x, y, width, height, radius, background);
		}
		if (HudStyleModule.border.get()) {
			Render.roundedBorder(graphics, x, y, width, height, radius, 1.0F, Render.alpha(accent, 0.55F), background);
		}
	}

	public static void text(GuiGraphics graphics, Font font, String value, float x, float y, int color) {
		Render.text(graphics, font, value, x, y, color, HudStyleModule.textShadow.get());
	}

	public static void text(GuiGraphics graphics, Font font, net.minecraft.network.chat.Component value, float x, float y, int color) {
		Render.text(graphics, font, value, x, y, color, HudStyleModule.textShadow.get());
	}

	public static void bar(GuiGraphics graphics, float x, float y, float width, float height, float fraction, int color) {
		float clamped = Anim.clamp01(fraction);
		if (HudStyleModule.accentBars.get()) {
			Render.roundedRect(graphics, x, y, width, height, height * 0.5F, 0x66000000);
			Render.roundedRect(graphics, x, y, width * clamped, height, height * 0.5F, color);
			return;
		}
		Render.rect(graphics, x, y, width, height, 0x66000000);
		Render.rect(graphics, x, y, width * clamped, height, color);
	}

	/** Colour ramp: green at full, amber in the middle, red when low. */
	public static int healthColor(float fraction) {
		float clamped = Anim.clamp01(fraction);
		if (clamped > 0.5F) {
			return Render.mix(0xFFFFC93C, 0xFF63D471, (clamped - 0.5F) * 2.0F);
		}
		return Render.mix(0xFFE05B5B, 0xFFFFC93C, clamped * 2.0F);
	}

	public static int dangerColor(float fraction) {
		float clamped = Anim.clamp01(fraction);
		return Render.mix(0xFFE05B5B, 0xFFF2F2F7, clamped);
	}
}
