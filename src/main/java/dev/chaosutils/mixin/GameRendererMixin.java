package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.SmoothZoom;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the smooth zoom by scaling the field of view vanilla just computed.
 *
 * <p>Scaling the FOV instead of moving the camera means the zoom is a pure optical effect:
 * no player state, no hitbox and no movement is involved, and the server never learns about
 * it. Mouse sensitivity stays untouched as well.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
	private void chaosutils$zoomFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> info) {
		ApiCompat.seen("renderer.fov");
		try {
			double factor = SmoothZoom.fovFactor(partialTick);
			if (factor < 0.9999) {
				info.setReturnValue(info.getReturnValue() * factor);
			}
		} catch (Throwable ignored) {
			// keep the vanilla FOV
		}
	}
}
