package dev.chaosutils.feature;

import java.util.HashSet;
import java.util.Set;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Base contract for every ChaosUtils feature.
 *
 * <p>Features are stateless singletons: they own a {@link Module} (which the GUI and the
 * config file see) and receive a client tick plus, optionally, a HUD render callback.
 * They must never hold on to world objects between ticks - nothing here may leak.
 */
public interface Feature {
	/** Where a feature draws. World overlays sit under the chat, panels above everything. */
	enum Layer {
		WORLD,
		PANEL
	}

	Module module();

	default boolean isEnabled() {
		return !Enablement.isDisabled(this) && module().isEnabled() && !ChaosUtils.overlaysHidden(module().category());
	}

	default Layer layer() {
		return Layer.PANEL;
	}

	/** Whether this feature wants {@link #onHudRender} called every frame. */
	default boolean supportsHud() {
		return false;
	}

	/** Called once per client tick while the game is not paused. */
	default void onTick(Minecraft client) {
	}

	/** Called every frame; {@code partialTick} is the interpolated tick progress. */
	default void onHudRender(GuiGraphics graphics, float partialTick) {
	}

	/** Called when the player toggles the feature off, so it can clean up transient state. */
	default void onDisabled() {
	}

	/** Called when the client joins a world, including when a dimension change reloads it. */
	default void onWorldJoin() {
	}

	/** Called when the client leaves a world or disconnects. Must drop every world reference. */
	default void onWorldLeave() {
	}

	/**
	 * Disables a feature that threw an exception. A broken overlay stays off for the
	 * session instead of spamming the log or the frame budget; the GUI can re-enable it.
	 */
	default void disableForSession() {
		Enablement.disable(this);
	}

	/** Session scoped "this feature is broken" bookkeeping. */
	final class Enablement {
		private static final Set<String> DISABLED = new HashSet<>();

		private Enablement() {
		}

		static void disable(Feature feature) {
			if (DISABLED.add(feature.module().id())) {
				ChaosUtils.LOGGER.warn("Disabling {} for this session after a failure", feature.module().id());
				feature.onDisabled();
			}
		}

		public static boolean isDisabled(Feature feature) {
			return DISABLED.contains(feature.module().id());
		}

		public static void clear() {
			DISABLED.clear();
		}
	}
}
