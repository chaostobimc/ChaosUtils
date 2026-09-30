package dev.chaosutils;

import java.util.EnumSet;
import java.util.Set;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TpsEstimator;
import dev.chaosutils.feature.FeatureRegistry;
import dev.chaosutils.feature.Features;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChaosUtils - a client side quality of life mod.
 *
 * <p>Everything in this mod is presentation only: overlays, markers, colours, sounds that only
 * you hear and shortcuts that do exactly what you could type yourself. There is deliberately
 * no automation, no packet of our own and no interaction with the server beyond the commands
 * and messages the player explicitly asks for.
 */
public final class ChaosUtils implements ClientModInitializer {
	public static final String MOD_ID = "chaosutils";
	public static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils");

	private static final Set<Category> HIDDEN_CATEGORIES = EnumSet.noneOf(Category.class);
	private static boolean overlaysHidden;
	private static boolean wasInWorld;

	@Override
	public void onInitializeClient() {
		LOGGER.info("ChaosUtils is starting (client side only)");
		ChaosConfig.load();
		Keybinds.init();
		registerApiHooks();
		FeatureRegistry.registerAll();
		Features.initHud();

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		Thread shutdownHook = new Thread(ChaosConfig::save, "ChaosUtils shutdown save");
		try {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Throwable ignored) {
			// sandboxed environments may refuse; the periodic autosave covers us
		}
		LOGGER.info("ChaosUtils ready: {} modules, {} integration hooks",
				dev.chaosutils.config.ModuleManager.modules().size(), 9);
	}

	private void onEndTick(Minecraft client) {
		boolean inWorld = client.level != null && client.player != null;
		if (inWorld != wasInWorld) {
			wasInWorld = inWorld;
			if (inWorld) {
				Features.onWorldJoin();
			} else {
				Features.onWorldLeave();
				TpsEstimator.reset();
			}
		}
		ChaosConfig.tick();
		Features.tick(client);
		if (Keybinds.openGui != null && Keybinds.openGui.consumeClick()) {
			client.setScreen(new dev.chaosutils.gui.ChaosClickGui());
		}
		if (Keybinds.panicToggle != null && Keybinds.panicToggle.consumeClick()) {
			toggleOverlays();
			if (client.player != null) {
				client.player.displayClientMessage(
						net.minecraft.network.chat.Component.literal(overlaysHidden
								? "§b[ChaosUtils] §fOverlays hidden" : "§b[ChaosUtils] §fOverlays visible"), true);
			}
		}
	}

	/** Declares the integration hooks so the self check can report exactly what applied. */
	private static void registerApiHooks() {
		ApiCompat.register("sound.play", "Sound radar & subtitles", "SoundEngine#play");
		ApiCompat.register("container.render", "Container search", "AbstractContainerScreen#render + layout accessor");
		ApiCompat.register("camera.setup", "Perspective lock", "Camera#setup");
		ApiCompat.register("debug.render", "Compact F3", "DebugScreenOverlay#render");
		ApiCompat.register("entity.remove", "Cache hygiene", "Entity#remove");
		ApiCompat.register("renderer.fov", "Smooth zoom", "GameRenderer#getFov");
		ApiCompat.register("particle.create", "Particle reducer", "ParticleEngine#createParticle");
		ApiCompat.register("gui.crosshair", "Crosshair designer", "Gui#renderCrosshair");
		ApiCompat.register("mouse.scroll", "Zoom wheel", "MouseHandler#onScroll");
	}

	// ------------------------------------------------------------------ overlays

	/** True when the panic toggle hid every overlay. */
	public static boolean overlaysHidden() {
		return overlaysHidden;
	}

	/**
	 * Whether overlays of a category are currently suppressed.
	 *
	 * <p>Quality of life features (the interface itself, keybinds, screenshots) are never
	 * hidden - a panic toggle must not lock you out of the mod.
	 */
	public static boolean overlaysHidden(Category category) {
		if (category == Category.QOL) {
			return false;
		}
		return overlaysHidden || HIDDEN_CATEGORIES.contains(category);
	}

	public static void toggleOverlays() {
		overlaysHidden = !overlaysHidden;
	}

	public static void toggleCategory(Category category) {
		if (!HIDDEN_CATEGORIES.remove(category)) {
			HIDDEN_CATEGORIES.add(category);
		}
	}

	public static boolean categoryHidden(Category category) {
		return HIDDEN_CATEGORIES.contains(category);
	}
}
