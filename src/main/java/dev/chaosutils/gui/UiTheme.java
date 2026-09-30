package dev.chaosutils.gui;

import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Render;

/** Cached palette derived from the theme settings; rebuilt only when a value changes. */
public final class UiTheme {
	public final int accent;
	public final int accentSoft;
	public final int background;
	public final int panel;
	public final int panelAlt;
	public final int panelHover;
	public final int outline;
	public final int text;
	public final int textDim;
	public final int textFaint;
	public final int positive;
	public final int negative;
	public final int warning;
	public final float radius;
	public final float animSpeed;
	public final boolean tooltips;
	public final boolean sounds;
	public final boolean blur;
	public final int backdropStyle;
	public final float sidebarWidth;
	public final boolean compact;
	public final boolean keybindHints;

	private static UiTheme cached;

	private UiTheme() {
		accent = ThemeModule.accent.get();
		accentSoft = Render.alpha(accent, 0.22F);
		int tint = ThemeModule.backgroundColor.get();
		float opacity = ThemeModule.backgroundOpacity.getFloat();
		background = Render.alpha(tint, opacity);
		panel = 0xF0171721;
		panelAlt = 0xF01D1D2A;
		panelHover = 0xF0252536;
		outline = 0x40FFFFFF;
		text = 0xFFF2F2F7;
		textDim = 0xFF9E9EB3;
		textFaint = 0xFF6C6C80;
		positive = 0xFF63D471;
		negative = 0xFFE05B5B;
		warning = 0xFFF0B429;
		radius = ThemeModule.cornerRadius.getFloat();
		animSpeed = ThemeModule.animationSpeed.getFloat();
		tooltips = ThemeModule.tooltips.get();
		sounds = ThemeModule.guiSounds.get();
		blur = ThemeModule.blur.get();
		backdropStyle = ThemeModule.backgroundStyle.get();
		sidebarWidth = ThemeModule.sidebarWidth.getFloat();
		compact = ThemeModule.compactCards.get();
		keybindHints = ThemeModule.showKeybindHints.get();
	}

	public static UiTheme get() {
		if (cached == null) {
			cached = new UiTheme();
		}
		return cached;
	}

	public static void invalidate() {
		cached = null;
	}

	/** Multiplier for animation speeds, respecting the user's preference. */
	public float speed(float base) {
		return base * animSpeed;
	}
}
