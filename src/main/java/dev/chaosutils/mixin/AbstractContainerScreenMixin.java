package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.inventory.ContainerSearch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the container search overlay on top of any container screen.
 *
 * <p>The mixin only paints (dimming non matching slots, outlining matches). Typing into the
 * search field is handled by the vanilla text field widget that
 * {@code ScreenEvents.AFTER_INIT} adds on screens that accept it.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private void chaosutils$containerSearchOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
		ApiCompat.seen("container.render");
		try {
			ContainerSearch.renderOverlay((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY);
		} catch (Throwable ignored) {
			// A rendering helper must never crash a container screen.
		}
	}
}
