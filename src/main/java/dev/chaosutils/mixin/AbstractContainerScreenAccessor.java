package dev.chaosutils.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads the container screen layout (GUI origin and image size).
 *
 * <p>These fields are {@code protected} in vanilla, and the search overlay is drawn from a
 * mixin outside the class hierarchy, so an accessor is the clean way to get at them
 * without widening anything else.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int chaosutils$leftPos();

	@Accessor("topPos")
	int chaosutils$topPos();

	@Accessor("imageWidth")
	int chaosutils$imageWidth();

	@Accessor("imageHeight")
	int chaosutils$imageHeight();
}
