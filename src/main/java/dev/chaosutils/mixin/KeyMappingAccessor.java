package dev.chaosutils.mixin;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads the physical key of a {@link KeyMapping}.
 *
 * <p>Needed because the game releases every key mapping when a screen opens
 * ({@code Minecraft#setScreen} calls {@code KeyMapping.releaseAll()}), while the keyboard handler
 * only feeds new key states to the mappings when <em>no</em> screen is open. Inside a screen the
 * mapping state therefore says "not pressed" even while the player is still holding the key - which
 * is exactly what a hold-to-open radial menu has to know. Polling GLFW for the mapping's key is the
 * only reliable answer, and the key itself is {@code protected} in 1.21.11, so it is read through
 * this accessor instead of reflection.
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
	@Accessor("key")
	InputConstants.Key chaosutils$key();
}
