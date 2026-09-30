package dev.chaosutils.feature;

import dev.chaosutils.feature.audio.SoundRadar;
import dev.chaosutils.feature.audio.SubtitlesPlus;
import dev.chaosutils.feature.audio.VolumeDucker;
import dev.chaosutils.feature.chat.ChatHistory;
import dev.chaosutils.feature.chat.ChatMentions;
import dev.chaosutils.feature.hud.ArmorStatusHud;
import dev.chaosutils.feature.hud.CompactDebugOverlay;
import dev.chaosutils.feature.hud.DurabilityHud;
import dev.chaosutils.feature.hud.EntityHealthOverlay;
import dev.chaosutils.feature.hud.HudStyleModule;
import dev.chaosutils.feature.hud.TpsPingHud;
import dev.chaosutils.feature.hud.WaypointHud;
import dev.chaosutils.feature.inventory.ContainerPreview;
import dev.chaosutils.feature.inventory.ContainerSearch;
import dev.chaosutils.feature.inventory.ItemCounter;
import dev.chaosutils.feature.performance.ParticleReducer;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.feature.radial.RadialMenuFeature;
import dev.chaosutils.feature.visual.CrosshairDesigner;
import dev.chaosutils.feature.visual.GammaModule;
import dev.chaosutils.feature.visual.PerspectiveLock;
import dev.chaosutils.feature.visual.SmoothZoom;

/**
 * Creates every module and feature in a fixed order.
 *
 * <p>Keeping this in one place makes the feature list auditable at a glance: 23 modules, each
 * with its own settings, all of them searchable in the click GUI.
 */
public final class FeatureRegistry {
	private FeatureRegistry() {
	}

	public static void registerAll() {
		// --- shared look and feel (modules without a runtime feature) -------------
		ThemeModule.register();
		HudStyleModule.register();

		// --- radial menu ----------------------------------------------------------
		RadialMenuFeature.register();
		Features.register(new RadialMenuFeature());

		// --- HUD ------------------------------------------------------------------
		EntityHealthOverlay.register();
		Features.register(new EntityHealthOverlay());

		TpsPingHud.register();
		Features.register(new TpsPingHud());

		CompactDebugOverlay.register();
		Features.register(new CompactDebugOverlay());

		ArmorStatusHud.register();
		Features.register(new ArmorStatusHud());

		DurabilityHud.register();
		Features.register(new DurabilityHud());

		WaypointHud.register();
		Features.register(new WaypointHud());

		// --- visuals --------------------------------------------------------------
		CrosshairDesigner.register();
		Features.register(new CrosshairDesigner());

		GammaModule.register();
		Features.register(new GammaModule());

		SmoothZoom.register();
		Features.register(new SmoothZoom());

		PerspectiveLock.register();
		Features.register(new PerspectiveLock());

		// --- inventory ------------------------------------------------------------
		ContainerSearch.register();
		ContainerSearch.initEvents();
		Features.register(new ContainerSearch());

		ContainerPreview.register();
		ContainerPreview.initEvents();
		Features.register(new ContainerPreview());

		ItemCounter.register();
		ItemCounter.initEvents();
		Features.register(new ItemCounter());

		// --- chat -----------------------------------------------------------------
		ChatMentions.register();
		ChatMentions.initEvents();
		Features.register(new ChatMentions());

		ChatHistory.register();
		ChatHistory.initEvents();
		Features.register(new ChatHistory());

		// --- audio ----------------------------------------------------------------
		SoundRadar.register();
		Features.register(new SoundRadar());

		SubtitlesPlus.register();
		Features.register(new SubtitlesPlus());

		VolumeDucker.register();
		Features.register(new VolumeDucker());

		// --- quality of life ------------------------------------------------------
		ScreenshotManager.register();
		Features.register(new ScreenshotManager());

		// --- performance ----------------------------------------------------------
		ParticleReducer.register();
		Features.register(new ParticleReducer());
	}
}
