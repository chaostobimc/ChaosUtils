package dev.chaosutils.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker for the camera rotation.
 *
 * <p>{@code Camera#setRotation(float, float)} is {@code protected} in 1.21.11, and the
 * perspective lock changes the camera from outside the class hierarchy. A mixin invoker is
 * the supported way to reach it - no reflection, no duck-typed casting and no access
 * widening on the vanilla class itself.
 *
 * <p>The invoker only ever rotates the already computed camera. The player entity, its
 * hitbox and its movement stay untouched, so this stays a purely visual feature.
 */
@Mixin(Camera.class)
public interface CameraAccessor {
	@Invoker("setRotation")
	void chaosutils$setRotation(float yRot, float xRot);
}
