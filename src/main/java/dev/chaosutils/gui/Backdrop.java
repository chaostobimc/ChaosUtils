package dev.chaosutils.gui;

import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * Draws the ChaosUtils backdrop for a screen without touching vanilla's background pass.
 *
 * <h2>Why this class exists</h2>
 * Since 1.21.9 the game paints a screen's background itself:
 *
 * <pre>
 * public final void renderWithTooltipAndSubtitles(GuiGraphics graphics, ...) {
 *     graphics.nextStratum();
 *     this.renderBackground(graphics, mouseX, mouseY, a);   // blur happens here
 *     graphics.nextStratum();
 *     this.render(graphics, mouseX, mouseY, a);             // our code runs here
 * }
 * </pre>
 *
 * <p>{@code renderBackground(...)} ends up in {@code Screen#renderBlurredBackground}, which calls
 * {@code GuiGraphics#blurBeforeThisStratum()}. The render state allows exactly one blur per frame
 * and throws {@code IllegalStateException("Can only blur once per frame")} on the second attempt.
 * A screen that calls {@code renderBackground(...)} from its own {@code render()} therefore kills
 * the game on the very first frame it is shown - that is exactly what happened the first time the
 * click GUI was opened.
 *
 * <p>So this helper never calls {@code renderBackground}: it only paints the ChaosUtils tint on top
 * of the background vanilla already drew, and asks for the blur itself <em>only</em> when the
 * vanilla option has it switched off. Everything is wrapped so that a backdrop can never take the
 * game down.
 */
public final class Backdrop {
	/** "Dark gradient" - tint plus a soft gradient towards the bottom. */
	public static final int STYLE_DARK_GRADIENT = 0;
	/** "Blur + gradient" - same tint, the blur comes from the vanilla blur option. */
	public static final int STYLE_BLUR_GRADIENT = 1;
	/** "Flat" - tint only. */
	public static final int STYLE_FLAT = 2;
	/** "Transparent" - nothing at all, the world stays fully visible. */
	public static final int STYLE_TRANSPARENT = 3;

	private Backdrop() {
	}

	/**
	 * Paints the backdrop of {@code screen}. Call this from {@code render(...)} or from
	 * {@code renderBackdrop(...)} - never call {@link Screen#renderBackground} yourself.
	 */
	public static void render(GuiGraphics graphics, Screen screen, UiTheme theme) {
		if (theme.backdropStyle == STYLE_TRANSPARENT) {
			return;
		}
		requestBlur(graphics, theme);
		Render.rect(graphics, 0.0F, 0.0F, screen.width, screen.height, theme.background);
		if (theme.backdropStyle == STYLE_DARK_GRADIENT) {
			Render.verticalGradient(graphics, 0.0F, 0.0F, screen.width, screen.height, 0x00000000, 0x66000000);
		}
	}

	/**
	 * Uses the frame's single blur slot when vanilla left it free.
	 *
	 * <p>Vanilla blurs whenever the "Menu background blurriness" option is 1 or higher, which is the
	 * default; in that case the world behind the GUI is already blurred and there is nothing to do.
	 * Only when the player switched that option off does ChaosUtils claim the slot - and even then
	 * a failure is harmless, because another screen or mod may have used it already.
	 */
	private static void requestBlur(GuiGraphics graphics, UiTheme theme) {
		if (!theme.blur) {
			return;
		}
		try {
			Minecraft client = Minecraft.getInstance();
			if (client.options.getMenuBackgroundBlurriness() >= 1) {
				return;
			}
			graphics.blurBeforeThisStratum();
		} catch (Throwable ignored) {
			// The blur slot is taken (or the API moved) - draw the GUI without the blur.
		}
	}
}
