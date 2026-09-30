package dev.chaosutils.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.util.Projection;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/**
 * The feature spine.
 *
 * <p>Every ChaosUtils feature registers here exactly once. The registry owns
 * <ul>
 *   <li>the per-tick and per-frame dispatch,</li>
 *   <li>the HUD element registration (Fabric's HUD API, so vanilla ordering and the
 *       "hide HUD" key keep working),</li>
 *   <li>the per-entity cache pruning that the {@code Entity} mixin drives,</li>
 *   <li>the tiny amount of state mixins need before the features are initialised.</li>
 * </ul>
 */
public final class Features {
	private static final List<Feature> FEATURES = new ArrayList<>();
	private static final List<IntConsumer> ENTITY_PRUNE_LISTENERS = new ArrayList<>();
	private static final List<Feature> HUD_FEATURES = new ArrayList<>();

	private static volatile float zoomFactor = 1.0F;
	private static volatile boolean worldReady;
	private static int reportDelay = 40;

	private Features() {
	}

	public static void register(Feature feature) {
		FEATURES.add(feature);
		if (feature.supportsHud()) {
			HUD_FEATURES.add(feature);
		}
	}

	public static List<Feature> features() {
		return FEATURES;
	}

	/** Registers the HUD elements. Called after every feature has been created. */
	public static void initHud() {
		for (Feature feature : HUD_FEATURES) {
			Identifier id = Identifier.fromNamespaceAndPath(ChaosUtils.MOD_ID, feature.module().id());
			HudElementRegistry.addLast(id, (graphics, deltaTracker) -> {
				try {
					if (!feature.isEnabled()) {
						return;
					}
					Minecraft client = Minecraft.getInstance();
					if (client.player == null || client.level == null || client.options.hideGui) {
						return;
					}
					Projection.setup(client, graphics.getScaledWindowWidth(), graphics.getScaledWindowHeight(), TickClock.partialTick());
					feature.onHudRender(graphics, TickClock.partialTick());
				} catch (Throwable throwable) {
					// A failing overlay must never take the game down; report once and skip.
					ChaosUtils.LOGGER.warn("HUD element {} failed to render", feature.module().id(), throwable);
					feature.disableForSession();
				}
			});
		}
	}

	public static void onWorldJoin() {
		worldReady = true;
		TickClock.reset();
		for (Feature feature : FEATURES) {
			feature.onWorldJoin();
		}
	}

	public static void onWorldLeave() {
		worldReady = false;
		zoomFactor = 1.0F;
		for (Feature feature : FEATURES) {
			feature.onWorldLeave();
		}
	}

	public static boolean isWorldReady() {
		return worldReady;
	}

	public static void tick(Minecraft client) {
		TickClock.onClientTick();
		if (reportDelay > 0 && --reportDelay == 0) {
			dev.chaosutils.core.ApiCompat.report();
		}
		for (Feature feature : FEATURES) {
			try {
				feature.onTick(client);
			} catch (Throwable throwable) {
				ChaosUtils.LOGGER.warn("Feature {} failed while ticking", feature.module().id(), throwable);
				feature.disableForSession();
			}
		}
	}

	// ------------------------------------------------------------------ mixin state

	/** Current optical zoom factor applied to the field of view ({@code 1.0} = no zoom). */
	public static float zoomFactor() {
		return zoomFactor;
	}

	public static void setZoomFactor(float factor) {
		zoomFactor = factor <= 0.0F ? 1.0F : factor;
	}

	public static boolean anySoundConsumerEnabled() {
		return dev.chaosutils.config.ModuleManager.enabled("sound_radar")
				|| dev.chaosutils.config.ModuleManager.enabled("subtitles_plus");
	}

	public static void addEntityPruneListener(IntConsumer listener) {
		ENTITY_PRUNE_LISTENERS.add(listener);
	}

	public static void pruneEntityCaches(int entityId) {
		for (IntConsumer listener : ENTITY_PRUNE_LISTENERS) {
			listener.accept(entityId);
		}
	}
}
