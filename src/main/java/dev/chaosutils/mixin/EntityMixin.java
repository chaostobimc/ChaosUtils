package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.Features;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cache hygiene: every ChaosUtils feature that keeps per-entity animation state drops it
 * here, so a long session in a busy world can never grow the caches without bound.
 */
@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "remove", at = @At("HEAD"), require = 0)
	private void chaosutils$dropEntityState(CallbackInfo info) {
		ApiCompat.seen("entity.remove");
		try {
			Features.pruneEntityCaches(((Entity) (Object) this).getId());
		} catch (Throwable ignored) {
			// caches are best-effort
		}
	}
}
