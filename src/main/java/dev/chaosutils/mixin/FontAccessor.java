package dev.chaosutils.mixin;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads the glyph provider of the vanilla font.
 *
 * <p>The client never exposes its {@code FontManager}, but the provider of the default font is
 * everything needed to build a second {@link Font} that renders a bundled font instead - see
 * {@code dev.chaosutils.gui.UiFonts}. Reading the private field through an accessor keeps the
 * interface free of reflection.
 */
@Mixin(Font.class)
public interface FontAccessor {
	@Accessor("provider")
	Font.Provider chaosutils$provider();
}
