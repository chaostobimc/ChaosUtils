package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.performance.ParticleReducer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the particle reducer drop particles the user does not want to see.
 *
 * <p>Purely visual: hiding a particle client side changes nothing about the world state the
 * server knows, and it happens after the server told us about the particle - no packets are
 * sent or suppressed.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$filterParticle(ParticleOptions options, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> info) {
		ApiCompat.seen("particle.create");
		try {
			if (ParticleReducer.hides(options)) {
				info.setReturnValue(null);
			}
		} catch (Throwable ignored) {
			// never break particle creation
		}
	}
}
