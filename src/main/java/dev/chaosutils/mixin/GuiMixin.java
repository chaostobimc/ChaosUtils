package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.CrosshairDesigner;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla crosshair while the ChaosUtils designer is drawing its own.
 *
 * <p>Only the crosshair is cancelled, and only when the feature is enabled - everything
 * else in the HUD keeps running untouched.
 *
 * <p>The handler takes no target arguments on purpose. Vanilla passed the crosshair a
 * {@code GuiGraphics} and a {@code DeltaTracker} in 1.21.11; a handler that lists arguments
 * has to match them exactly or Mixin aborts the whole launch (that is what produced the
 * first in-game crash). With an empty argument list there is nothing left to mismatch -
 * Mixin accepts it for any signature, and {@code info.cancel()} still skips the vanilla
 * crosshair.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$replaceCrosshair(CallbackInfo info) {
		ApiCompat.seen("gui.crosshair");
		try {
			if (CrosshairDesigner.replacesVanilla()) {
				info.cancel();
			}
		} catch (Throwable ignored) {
			// draw the vanilla crosshair instead
		}
	}
}
