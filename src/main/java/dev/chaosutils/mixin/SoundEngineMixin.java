package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.core.SoundTracker;
import dev.chaosutils.feature.Features;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records every sound the client is about to play.
 *
 * <p>This is the single source for the sound direction radar, the enhanced subtitles and
 * the smarter mention/notification logic. It only observes: nothing is cancelled here and
 * no packet is ever sent.
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), require = 0)
	private void chaosutils$captureSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> info) {
		ApiCompat.seen("sound.play");
		if (!Features.anySoundConsumerEnabled()) {
			return;
		}
		try {
			SoundTracker.push(
					sound.getIdentifier().toString(),
					new net.minecraft.world.phys.Vec3(sound.getX(), sound.getY(), sound.getZ()),
					sound.getSource() == null ? "master" : sound.getSource().getName(),
					sound.getVolume(),
					sound.getPitch(),
					sound.isRelative());
		} catch (Throwable ignored) {
			// Never let a HUD helper break sound playback.
		}
	}
}
