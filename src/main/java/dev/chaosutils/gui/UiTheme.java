package dev.chaosutils.gui;

import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Render;

/**
 * Cached design tokens of the ChaosUtils interface.
 *
 * <p>Every colour the interface uses is derived here from the live theme settings, so the whole
 * look can be re-tinted from a single place and the click GUI can never end up with a colour that
 * does not exist. The instance is rebuilt whenever a theme setting changes
 * ({@link #invalidate()}), never per frame.
 */
public final class UiTheme {
	// ------------------------------------------------------------------ accent
	public final int accent;
	public final int accentBright;
	public final int accentSoft;
	public final int accentFaint;
	public final int accentGlow;
	public final int onAccent;

	// --------------------------------------------------------------- surfaces
	public final int background;
	public final int windowTop;
	public final int windowBottom;
	public final int sidebarTop;
	public final int sidebarBottom;
	public final int surface;
	public final int surfaceHover;
	public final int card;
	public final int cardHover;
	public final int cardActive;
	public final int track;
	public final int trackHover;

	// ---------------------------------------------------------------- strokes
	public final int outline;
	public final int outlineSoft;
	public final int outlineStrong;

	// ------------------------------------------------------------------- text
	public final int text;
	public final int textDim;
	public final int textFaint;
	public final int positive;
	public final int negative;
	public final int warning;

	// ---------------------------------------------------------------- metrics
	public final float radius;
	public final float radiusCard;
	public final float radiusControl;
	public final float animSpeed;
	public final float shadowStrength;
	public final float sidebarWidth;

	// -------------------------------------------------------------- behaviour
	public final boolean animations;
	public final boolean tooltips;
	public final boolean sounds;
	public final boolean blur;
	public final boolean glow;
	public final boolean windowShadow;
	public final boolean compact;
	public final boolean keybindHints;
	public final int backdropStyle;

	// ------------------------------------------------------- legacy aliases
	/** Kept so HUD code that predates the redesign keeps working unchanged. */
	public final int panel;
	public final int panelAlt;
	public final int panelHover;

	private static UiTheme cached;

	private UiTheme() {
		accent = ThemeModule.accent.get() | 0xFF000000;
		accentBright = Render.mix(accent, 0xFFFFFFFF, 0.22F);
		accentSoft = Render.alpha(accent, 0.30F);
		accentFaint = Render.alpha(accent, 0.13F);
		accentGlow = Render.alpha(accent, 0.55F);
		onAccent = luminance(accent) > 0.62F ? 0xFF0B0B12 : 0xFFFFFFFF;

		int tint = ThemeModule.backgroundColor.get();
		background = Render.alpha(tint, ThemeModule.backgroundOpacity.getFloat());

		int shell = Render.mix(0xFF0B0B12, tint, 0.35F);
		windowTop = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.035F), 0.97F);
		windowBottom = Render.alpha(Render.mix(shell, 0xFF000000, 0.18F), 0.97F);
		sidebarTop = Render.alpha(Render.mix(shell, 0x00000000, 0.35F), 0.92F);
		sidebarBottom = Render.alpha(Render.mix(shell, accent, 0.05F), 0.92F);

		// Surfaces are deliberately solid: nothing in the interface is supposed to look like the
		// world is shining through it, which is what made the panels hard to read.
		surface = 0xEC15151F;
		surfaceHover = 0xF61D1D2A;
		card = 0xF01A1A27;
		cardHover = 0xFA232336;
		cardActive = Render.alpha(Render.mix(0xFF22223A, accent, 0.22F), 1.0F);
		track = 0x992F2F45;
		trackHover = 0xBB3A3A55;

		outline = 0x2AFFFFFF;
		outlineSoft = 0x22FFFFFF;
		outlineStrong = 0x4DFFFFFF;

		text = 0xFFF4F5FA;
		textDim = 0xFFA8AABF;
		textFaint = 0xFF6E7086;
		positive = 0xFF57D98A;
		negative = 0xFFF0686A;
		warning = 0xFFF2B23E;

		radius = ThemeModule.cornerRadius.getFloat() + 6.0F;
		radiusCard = ThemeModule.cornerRadius.getFloat() + 3.0F;
		radiusControl = Math.max(3.0F, ThemeModule.cornerRadius.getFloat());

		animSpeed = ThemeModule.animationSpeed.getFloat();
		shadowStrength = ThemeModule.shadowStrength.getFloat();
		sidebarWidth = ThemeModule.sidebarWidth.getFloat();

		animations = ThemeModule.animations.get();
		tooltips = ThemeModule.tooltips.get();
		sounds = ThemeModule.guiSounds.get();
		blur = ThemeModule.blur.get();
		glow = ThemeModule.glow.get();
		windowShadow = ThemeModule.windowShadow.get();
		compact = ThemeModule.compactCards.get();
		keybindHints = ThemeModule.showKeybindHints.get();
		backdropStyle = ThemeModule.backgroundStyle.get();

		panel = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.05F), 0.92F);
		panelAlt = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.08F), 0.92F);
		panelHover = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.13F), 0.95F);
	}

	public static UiTheme get() {
		if (cached == null) {
			// Defensive: the tokens are read from the theme module, which is registered with the
			// other features. Should anything ask for the theme earlier, register it on demand.
			dev.chaosutils.feature.qol.ThemeModule.register();
			cached = new UiTheme();
		}
		return cached;
	}

	public static void invalidate() {
		cached = null;
	}

	/**
	 * Animation speed multiplier. With animations switched off this collapses to an instant
	 * settle, so every widget keeps its single code path.
	 */
	public float speed(float base) {
		return animations ? base * animSpeed : 2000.0F;
	}

	/** Content scale used to keep the interface readable on every GUI scale. */
	public float density() {
		return compact ? 0.92F : 1.0F;
	}

	private static float luminance(int color) {
		float r = Render.red(color) / 255.0F;
		float g = Render.green(color) / 255.0F;
		float b = Render.blue(color) / 255.0F;
		return 0.2126F * r + 0.7152F * g + 0.0722F * b;
	}
}
