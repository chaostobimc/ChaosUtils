package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.PerspectiveLock;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the perspective lock / camera preview rotation.
 *
 * <p>The camera is rotated after vanilla computed it; the player's own body, hitbox and
 * movement are never touched, which keeps the feature purely cosmetic and safe on servers.
 * The rotation is also remembered so {@code Projection} can keep HUD markers aligned with
 * what the player actually sees.
 */
@Mixin(Camera.class)
public class CameraMixin {
	@Inject(method = "setup", at = @At("TAIL"), require = 0)
	private void chaosutils$lockRotation(Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo info) {
		ApiCompat.seen("camera.setup");
		if (!PerspectiveLock.isActive()) {
			return;
		}
		try {
			PerspectiveLock.applyToCamera((Camera) (Object) this, entity, partialTick);
		} catch (Throwable ignored) {
			// Keep the vanilla camera if anything goes wrong.
		}
	}
}
