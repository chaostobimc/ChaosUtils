package dev.chaosutils.gui;

import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Render;

/**
 * Cached design tokens of the ChaosUtils interface.
 *
 * <p>Every colour the interface uses is derived here from the live theme settings, so the whole look
 * can be re-tinted from a single place and the click GUI can never end up with a colour that does
 * not exist. The instance is rebuilt whenever a theme setting changes ({@link #invalidate()}), never
 * per frame.
 *
 * <p>The palette itself is deliberately narrow: near-black neutral surfaces, one accent colour and
 * three text weights. Contrast comes from the surfaces and the hairlines, not from extra colours -
 * that is what keeps a window with dozens of controls readable.
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
		accentBright = Render.mix(accent, 0xFFFFFFFF, 0.30F);
		accentSoft = Render.alpha(accent, 0.26F);
		accentFaint = Render.alpha(accent, 0.11F);
		accentGlow = Render.alpha(accent, 0.45F);
		onAccent = luminance(accent) > 0.62F ? 0xFF0A0B10 : 0xFFFFFFFF;

		int tint = ThemeModule.backgroundColor.get();
		background = Render.alpha(tint, ThemeModule.backgroundOpacity.getFloat());

		// Opaque, near-black surfaces. Nothing behind the window should bleed through the panels:
		// translucency is reserved for hover states and the backdrop.
		windowTop = 0xFC0D1017;
		windowBottom = 0xFB0A0C12;
		sidebarTop = 0xFF090B11;
		sidebarBottom = 0xFF0A0D13;

		surface = 0xFF0C0F16;
		surfaceHover = 0xFF121620;
		card = 0xFF10141C;
		cardHover = 0xFF161B26;
		cardActive = Render.mix(0xFF161B26, accent, 0.22F) | 0xFF000000;
		track = 0xFF212736;
		trackHover = 0xFF2A3143;

		outline = 0xFF1A2030;
		outlineSoft = 0xFF141924;
		outlineStrong = 0xFF2C3448;

		text = 0xFFF6F7FB;
		textDim = 0xFFA6ADC0;
		textFaint = 0xFF6A7286;
		positive = 0xFF4ADE80;
		negative = 0xFFF87171;
		warning = 0xFFFBBF24;

		float corner = ThemeModule.cornerRadius.getFloat();
		radius = corner + 2.0F;
		radiusCard = corner;
		radiusControl = Math.max(4.0F, corner - 2.0F);

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

		panel = 0xFF0C0F16;
		panelAlt = 0xFF10141C;
		panelHover = 0xFF161B26;
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
