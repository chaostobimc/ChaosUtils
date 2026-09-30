package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.CrosshairDesigner;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla crosshair while the ChaosUtils designer is drawing its own.
 *
 * <p>Only the crosshair is cancelled, and only when the feature is enabled - everything
 * else in the HUD keeps running untouched.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$replaceCrosshair(GuiGraphics graphics, CallbackInfo info) {
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
