package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.hud.CompactDebugOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the wall of text that F3 produces with the compact ChaosUtils overlay.
 *
 * <p>When the feature is off nothing changes; when it is on, vanilla is cancelled and the
 * compact panel (coordinates, direction, biome, live time, TPS, ping) is drawn instead.
 * The full overlay is one key press away, so nothing is lost.
 */
@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$compactOverlay(GuiGraphics graphics, CallbackInfo info) {
		ApiCompat.seen("debug.render");
		try {
			if (CompactDebugOverlay.replacesVanilla()) {
				info.cancel();
			}
		} catch (Throwable ignored) {
			// fall back to the vanilla overlay
		}
	}

	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private void chaosutils$compactOverlayDraw(GuiGraphics graphics, CallbackInfo info) {
		try {
			CompactDebugOverlay.renderExtra(graphics);
		} catch (Throwable ignored) {
			// nothing to do
		}
	}
}
