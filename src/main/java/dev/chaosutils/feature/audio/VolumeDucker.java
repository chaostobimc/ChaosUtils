package dev.chaosutils.feature.audio;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/**
 * Unfocused volume reducer.
 *
 * <p>When the window loses focus (alt tabbed, second monitor, a browser in front) the volume
 * fades down to a configurable level and fades back the moment you return. Only the vanilla
 * volume options are driven - the values are stored when the transition starts and restored
 * exactly, so your sound settings survive untouched and nothing is written unless the fade is
 * actually running.
 */
public final class VolumeDucker implements Feature {
	public static final String ID = "volume_ducker";

	private static Module module;
	private static Setting.Number duckVolume;
	private static Setting.Number fadeSeconds;
	private static Setting.Choice scope;
	private static Setting.Toggle alsoPauseMusic;
	private static Setting.Toggle restoreOnLeave;
	private static Setting.Toggle indicator;

	private static final SoundSource[] ALL_SOURCES = {SoundSource.MASTER, SoundSource.MUSIC, SoundSource.RECORDS,
			SoundSource.WEATHER, SoundSource.BLOCKS, SoundSource.HOSTILE, SoundSource.NEUTRAL, SoundSource.PLAYERS,
			SoundSource.AMBIENT, SoundSource.VOICE, SoundSource.UI};

	private static final double[] ORIGINAL = new double[ALL_SOURCES.length];
	private static boolean captured;
	private static float currentFactor = 1.0F;
	private static float targetFactor = 1.0F;
	private static boolean lastFocused = true;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Unfocused Volume",
				"Turns the game down while the window is unfocused and back up when you return.",
				Category.AUDIO, true));
		duckVolume = (Setting.Number) module.add(new Setting.Number("volume", "Volume while unfocused",
				"Fraction of your normal volume.", 0.25, 0.0, 1.0, 0.05));
		fadeSeconds = (Setting.Number) module.add(new Setting.Number("fade", "Fade time",
				"How long the fade takes.", 0.6, 0.0, 5.0, 0.1, "s"));
		scope = (Setting.Choice) module.add(new Setting.Choice("scope", "Affected channels",
				"Which sound channels are turned down.", 0, "Master only", "Music & records", "Everything"));
		alsoPauseMusic = (Setting.Toggle) module.add(new Setting.Toggle("pause_music", "Also pause music",
				"Stop the music while unfocused instead of only fading it.", false));
		restoreOnLeave = (Setting.Toggle) module.add(new Setting.Toggle("restore", "Always restore on quit",
				"Make sure the original volumes are written back when the game closes.", true));
		indicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Show indicator",
				"Small badge in the corner of the screen while ducked.", false));
	}

	@Override
	public boolean supportsHud() {
		return indicator.get();
	}

	@Override
	public void onTick(Minecraft client) {
		if (!isEnabled() || client.options == null) {
			restore(client, true);
			return;
		}
		boolean focused = client.isWindowActive();
		if (focused != lastFocused) {
			lastFocused = focused;
			targetFactor = focused ? 1.0F : duckVolume.getFloat();
			if (!captured) {
				capture(client);
			}
		}
		float speed = fadeSeconds.getFloat() <= 0.01F ? 60.0F : 1.0F / Math.max(0.05F, fadeSeconds.getFloat()) * 4.0F;
		currentFactor = Anim.approach(currentFactor, targetFactor, speed, 0.05F);
		if (Math.abs(currentFactor - targetFactor) < 0.002F) {
			currentFactor = targetFactor;
		}
		if (captured) {
			apply(client);
		}
		if (alsoPauseMusic.get() && !focused) {
			try {
				// Vanilla pauses everything except music and UI when the game is paused; here the
				// music is meant to stop as well, so only the UI channel keeps playing.
				client.getSoundManager().pauseAllExcept(SoundSource.UI);
			} catch (Throwable ignored) {
				// nothing to pause
			}
		} else if (alsoPauseMusic.get()) {
			try {
				client.getSoundManager().resume();
			} catch (Throwable ignored) {
				// nothing to resume
			}
		}
	}

	private static void capture(Minecraft client) {
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			ORIGINAL[i] = read(client, ALL_SOURCES[i]);
		}
		captured = true;
	}

	private static double read(Minecraft client, SoundSource source) {
		try {
			Double value = client.options.getSoundSourceOptionInstance(source).get();
			return value == null ? 1.0 : value;
		} catch (Throwable ignored) {
			return 1.0;
		}
	}

	private static void apply(Minecraft client) {
		int selectedScope = scope.get();
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			SoundSource source = ALL_SOURCES[i];
			if (!affected(source, selectedScope)) {
				continue;
			}
			try {
				double value = ORIGINAL[i] * currentFactor;
				client.options.getSoundSourceOptionInstance(source).set(Math.max(0.0, Math.min(1.0, value)));
			} catch (Throwable ignored) {
				// option unavailable on this build - skip it
			}
		}
	}

	private static boolean affected(SoundSource source, int selectedScope) {
		return switch (selectedScope) {
			case 0 -> source == SoundSource.MASTER;
			case 1 -> source == SoundSource.MUSIC || source == SoundSource.RECORDS || source == SoundSource.MASTER;
			default -> true;
		};
	}

	private static void restore(Minecraft client, boolean force) {
		if (!captured) {
			return;
		}
		if (!force && currentFactor == targetFactor && targetFactor == 1.0F) {
			captured = false;
			return;
		}
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			try {
				client.options.getSoundSourceOptionInstance(ALL_SOURCES[i]).set(ORIGINAL[i]);
			} catch (Throwable ignored) {
				// nothing to restore for this channel
			}
		}
		captured = false;
		currentFactor = 1.0F;
		targetFactor = 1.0F;
	}

	@Override
	public void onHudRender(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
		if (!indicator.get() || !captured || currentFactor > 0.999F) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		String text = "Volume " + Math.round(currentFactor * 100.0F) + "%";
		float width = client.font.width(text) + 8.0F;
		dev.chaosutils.feature.hud.HudPanel.panel(graphics, client.font, 6.0F, 20.0F, width, 12.0F, 0xFFF06292);
		dev.chaosutils.feature.hud.HudPanel.text(graphics, client.font, text, 10.0F, 22.0F, 0xFFF2F2F7);
	}

	@Override
	public void onDisabled() {
		restore(Minecraft.getInstance(), true);
	}

	@Override
	public void onWorldLeave() {
		if (restoreOnLeave.get()) {
			restore(Minecraft.getInstance(), true);
		}
	}
}
