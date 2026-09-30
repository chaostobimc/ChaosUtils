package dev.chaosutils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.mixin.FontAccessor;

/**
 * Typography of the ChaosUtils interface.
 *
 * <p>Minecraft draws its text from a bitmap atlas of 8x8 glyphs, which is what gives the vanilla
 * interface its pixelated look. This class builds a second {@link Font} that resolves the glyphs of
 * a bundled TrueType face (Poppins, SIL Open Font License) through the game's own font manager.
 * Everything else - glyph atlas, caching, advance widths, the caret position inside edit boxes -
 * stays vanilla, so typed text, tooltips and our widgets all agree on the metrics.
 *
 * <p>The bundled font is only used when it really resolved: a probe compares the advance of a
 * sample string against the same string drawn with a font id that deliberately does not exist.
 * Equal advances mean both fell back to the "missing" glyph set, and the interface quietly keeps
 * the vanilla font instead of painting a wall of empty boxes.
 */
public final class UiFonts {
	/** Font id of the bundled regular face; the definition lives in {@code assets/chaosutils/font/slim.json}. */
	public static final Identifier SLIM_ID = Identifier.fromNamespaceAndPath(ChaosUtils.MOD_ID, "slim");
	/** Font id of the bundled semibold face. */
	public static final Identifier SLIM_BOLD_ID = Identifier.fromNamespaceAndPath(ChaosUtils.MOD_ID, "slim_bold");
	/** Font description used when a caller has to state a font explicitly. */
	public static final FontDescription SLIM = new FontDescription.Resource(SLIM_ID);
	/** Font description of the semibold face. */
	public static final FontDescription SLIM_BOLD = new FontDescription.Resource(SLIM_BOLD_ID);
	/** Font id that is guaranteed to be absent - the control sample of the probe. */
	private static final FontDescription MISSING = new FontDescription.Resource(
			Identifier.fromNamespaceAndPath(ChaosUtils.MOD_ID, "not_installed"));

	/** Highest code point the bundled face is expected to cover (Latin Extended-B). */
	private static final int LATIN_LIMIT = 0x024F;

	private static Font custom;
	private static Font customBold;
	private static boolean resolved;
	private static boolean announced;
	private static long nextProbe;

	private UiFonts() {
	}

	/** The regular font of the ChaosUtils interface; falls back to the vanilla font. */
	public static Font font() {
		if (ChaosUtils.USE_CUSTOM_FONT && usable() && custom != null) {
			return custom;
		}
		return Minecraft.getInstance().font;
	}

	/** Small captions; currently the same face, kept as its own hook for later tuning. */
	public static Font small() {
		return font();
	}

	/** The semibold face used for titles, module names and buttons. */
	public static Font bold() {
		if (ChaosUtils.USE_CUSTOM_FONT && usable()) {
			if (customBold != null) {
				return customBold;
			}
			if (custom != null) {
				return custom;
			}
		}
		return Minecraft.getInstance().font;
	}

	/** Width of a string in the bundled face, measured through the vanilla font metrics. */
	public static int width(String text, boolean bold) {
		return (bold ? bold() : font()).width(text);
	}

	/** The font to use for a concrete string - exotic text stays on the vanilla font. */
	public static Font pick(String text) {
		Font vanilla = Minecraft.getInstance().font;
		if (!ChaosUtils.USE_CUSTOM_FONT || !usable()) {
			return vanilla;
		}
		Font resolvedFont = custom;
		if (resolvedFont == null) {
			return vanilla;
		}
		return covers(text) ? resolvedFont : vanilla;
	}

	/** True when every code point of the text is covered by the bundled face. */
	private static boolean covers(String text) {
		if (text == null || text.isEmpty()) {
			return true;
		}
		for (int index = 0; index < text.length(); ) {
			int codePoint = text.codePointAt(index);
			index += Character.charCount(codePoint);
			if (codePoint <= LATIN_LIMIT) {
				continue;
			}
			switch (codePoint) {
				// punctuation the interface relies on, all of it present in the bundled face
				case 0x2013, 0x2014, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022, 0x2026, 0x20AC, 0x00D7, 0x2192, 0x2190,
						0x25CF, 0x2714, 0x2716, 0x00B7 ->
						continue;
				default -> {
					return false;
				}
			}
		}
		return true;
	}

	/** Builds the bundled fonts from the vanilla glyph provider. */
	private static boolean build() {
		Font vanilla = Minecraft.getInstance().font;
		if (vanilla == null) {
			return false;
		}
		Font.Provider source = ((FontAccessor) vanilla).chaosutils$provider();
		if (source == null) {
			return false;
		}
		custom = new Font(new ForcedProvider(source, SLIM));
		customBold = new Font(new ForcedProvider(source, SLIM_BOLD));
		return true;
	}

	/** Probes once whether the bundled face is really behind {@link #SLIM_ID}. */
	private static boolean usable() {
		if (resolved) {
			return custom != null;
		}
		long now = System.nanoTime();
		if (now < nextProbe) {
			return false;
		}
		// resources may still be loading while the first screen opens, so retry now and then
		nextProbe = now + 5_000_000_000L;
		try {
			if (custom == null && !build()) {
				return false;
			}
			if (custom == null) {
				return false;
			}
			int sample = custom.width("Wavy");
			int control = custom.width(Component.literal("Wavy").withStyle(Style.EMPTY.withFont(MISSING))
					.getVisualOrderText());
			if (sample <= 0 || sample == control) {
				return false;
			}
			resolved = true;
			return true;
		} catch (Throwable throwable) {
			if (!announced) {
				announced = true;
				ChaosUtils.LOGGER.warn("ChaosUtils keeps the vanilla font: the bundled face could not be used", throwable);
			}
			return false;
		}
	}

	/** Drops the cached fonts; the next screen rebuilds them from the reloaded resources. */
	public static void invalidate() {
		custom = null;
		customBold = null;
		resolved = false;
		nextProbe = 0L;
	}

	/** Name of the bundled face for the settings screen. */
	public static String description() {
		return resolved && custom != null ? "Poppins (bundled)" : "Minecraft default";
	}

	/**
	 * Wraps the vanilla provider and redirects every request for the default font to the bundled
	 * one. Fonts that a text explicitly asks for (for example {@code minecraft:alt}) stay untouched.
	 */
	private record ForcedProvider(Font.Provider delegate, FontDescription face) implements Font.Provider {
		@Override
		public GlyphSource glyphs(FontDescription font) {
			FontDescription requested = font == null || FontDescription.DEFAULT.equals(font) ? face : font;
			return delegate.glyphs(requested);
		}

		@Override
		public EffectGlyph effect() {
			return delegate.effect();
		}
	}
}
