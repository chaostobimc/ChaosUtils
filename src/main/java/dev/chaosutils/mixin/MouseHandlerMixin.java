package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.SmoothZoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands the raw scroll delta to the zoom feature so the wheel can adjust the zoom level
 * while the zoom key is held. Nothing is cancelled and nothing is injected - the vanilla
 * handler still does exactly what it did before.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), require = 0)
	private void chaosutils$scroll(long window, double horizontal, double vertical, CallbackInfo info) {
		ApiCompat.seen("mouse.scroll");
		try {
			SmoothZoom.onScroll(vertical);
		} catch (Throwable ignored) {
			// zoom simply keeps its current level
		}
	}
}
