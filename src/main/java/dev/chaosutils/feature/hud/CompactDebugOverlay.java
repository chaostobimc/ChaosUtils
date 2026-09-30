package dev.chaosutils.feature.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.HudPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

/**
 * Compact F3 replacement.
 *
 * <p>Instead of twenty lines of text it shows the things you actually read while playing:
 * coordinates, facing direction, biome, chunk, light level, the time of day (plus the real
 * time) and the current dimension. The vanilla overlay is suppressed through the debug
 * overlay hook and can be brought back with a single setting. Holding the copy key puts the
 * coordinates on the clipboard.
 */
public final class CompactDebugOverlay implements Feature {
	public static final String ID = "compact_debug";

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
	private static final String[] COMPASS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

	private static Module module;
	private static Setting.Position position;
	private static Setting.Toggle replaceVanilla;
	private static Setting.Toggle alwaysShow;
	private static Setting.Toggle showCoords;
	private static Setting.Toggle showPreciseCoords;
	private static Setting.Toggle showFacing;
	private static Setting.Toggle showBiome;
	private static Setting.Toggle showChunk;
	private static Setting.Toggle showLight;
	private static Setting.Toggle showTime;
	private static Setting.Toggle showRealTime;
	private static Setting.Toggle showDimension;
	private static Setting.Toggle showFps;
	private static Setting.Toggle showServer;
	private static Setting.Toggle showCopyHint;
	private static Setting.Number scale;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Compact Coordinates",
				"A compact F3 panel with coordinates, direction, biome, light and time.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the panel is drawn.", HudPos.TOP_LEFT.x(), HudPos.TOP_LEFT.y()));
		replaceVanilla = (Setting.Toggle) module.add(new Setting.Toggle("replace", "Replace vanilla F3",
				"Hide the long vanilla debug text and show this panel instead.", true));
		alwaysShow = (Setting.Toggle) module.add(new Setting.Toggle("always", "Always visible",
				"Show the panel even when F3 is closed.", false));
		showCoords = (Setting.Toggle) module.add(new Setting.Toggle("coords", "Coordinates",
				"X / Y / Z of your feet.", true));
		showPreciseCoords = (Setting.Toggle) module.add(new Setting.Toggle("precise", "Precise coordinates",
				"Two decimals instead of rounded blocks.", false));
		showFacing = (Setting.Toggle) module.add(new Setting.Toggle("facing", "Facing",
				"Compass direction plus the exact yaw/pitch.", true));
		showBiome = (Setting.Toggle) module.add(new Setting.Toggle("biome", "Biome",
				"Name of the biome you are standing in.", true));
		showChunk = (Setting.Toggle) module.add(new Setting.Toggle("chunk", "Chunk",
				"Chunk coordinates and your position inside the chunk.", false));
		showLight = (Setting.Toggle) module.add(new Setting.Toggle("light", "Light level",
				"Block and sky light at your position.", false));
		showTime = (Setting.Toggle) module.add(new Setting.Toggle("time", "Time of day",
				"In game clock derived from the world time.", true));
		showRealTime = (Setting.Toggle) module.add(new Setting.Toggle("real_time", "Real time",
				"Your local wall clock time.", false));
		showDimension = (Setting.Toggle) module.add(new Setting.Toggle("dimension", "Dimension",
				"Which dimension you are in.", true));
		showFps = (Setting.Toggle) module.add(new Setting.Toggle("fps", "FPS", "Frame rate.", false));
		showServer = (Setting.Toggle) module.add(new Setting.Toggle("server", "Server",
				"Address of the server you are on.", false));
		showCopyHint = (Setting.Toggle) module.add(new Setting.Toggle("copy_hint", "Copy hint",
				"Remind about the copy coordinates key.", false));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of this panel.", 1.0, 0.6, 1.8, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	/** True when the vanilla debug overlay should be suppressed. */
	public static boolean replacesVanilla() {
		if (!ModuleManager.enabled(ID) || !replaceVanilla.get()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.level != null;
	}

	@Override
	public void onTick(Minecraft client) {
		if (Keybinds.copyCoordinates != null && Keybinds.copyCoordinates.wasPressed() && client.player != null) {
			Player player = client.player;
			Clipboard.copyText(String.format(Locale.ROOT, "%.1f %.1f %.1f", player.getX(), player.getY(), player.getZ()));
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || !HudPanel.visibleNow()) {
			return;
		}
		boolean debugOpen = client.getDebugOverlay() != null && client.getDebugOverlay().showDebugScreen();
		if (debugOpen) {
			// Drawn by the debug overlay hook instead, exactly where F3 belongs.
			return;
		}
		if (!alwaysShow.get()) {
			return;
		}
		draw(graphics, client);
	}

	/**
	 * Called from the debug overlay hook: replaces the vanilla text when that setting is on,
	 * and otherwise adds the compact panel next to it so nothing is lost.
	 */
	public static void renderExtra(GuiGraphics graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || !ModuleManager.enabled(ID)) {
			return;
		}
		if (client.getDebugOverlay() == null || !client.getDebugOverlay().showDebugScreen()) {
			return;
		}
		draw(graphics, client);
	}

	private static void draw(GuiGraphics graphics, Minecraft client) {
		Font font = client.font;
		Player player = client.player;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		float lineHeight = 10.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		int lines = 0;

		String coords = null;
		if (showCoords.get()) {
			if (showPreciseCoords.get()) {
				coords = String.format(Locale.ROOT, "XYZ %.2f / %.2f / %.2f", player.getX(), player.getY(), player.getZ());
			} else {
				coords = "XYZ " + (int) Math.floor(player.getX()) + " / " + (int) Math.floor(player.getY()) + " / "
						+ (int) Math.floor(player.getZ());
			}
		}
		String facing = showFacing.get() ? "Facing " + compass(player.getYRot()) + "  (" + String.format(Locale.ROOT, "%.0f / %.0f", player.getYRot(), player.getXRot()) + ")" : null;
		String dimension = showDimension.get() && client.level != null
				? "Dim " + client.level.dimension().location().getPath() : null;
		String time = null;
		if (showTime.get() && client.level != null) {
			long dayTime = client.level.getDayTime() % 24000L;
			long hours = (dayTime / 1000L + 6L) % 24L;
			long minutes = (long) ((dayTime % 1000L) / 1000.0 * 60.0);
			time = String.format(Locale.ROOT, "Time %02d:%02d  day %d", hours, minutes, client.level.getDayTime() / 24000L);
		}
		String realTime = showRealTime.get() ? "Clock " + LocalTime.now().format(TIME_FORMAT) : null;
		String chunk = null;
		if (showChunk.get()) {
			BlockPos pos = player.blockPosition();
			chunk = "Chunk " + (pos.getX() >> 4) + " / " + (pos.getZ() >> 4)
					+ "  in " + (pos.getX() & 15) + "/" + (pos.getZ() & 15);
		}
		String light = null;
		if (showLight.get() && client.level != null) {
			BlockPos pos = player.blockPosition();
			light = "Light " + client.level.getBrightness(LightLayer.BLOCK, pos) + " / "
					+ client.level.getBrightness(LightLayer.SKY, pos);
		}
		String biome = null;
		if (showBiome.get() && client.level != null) {
			try {
				biome = "Biome " + client.level.getBiome(player.blockPosition()).unwrapKey()
						.map(key -> prettify(key.location().getPath()))
						.orElse("unknown");
			} catch (Throwable ignored) {
				biome = null;
			}
		}
		String fps = showFps.get() ? client.getFps() + " fps" : null;
		String server = null;
		if (showServer.get()) {
			server = client.getCurrentServer() != null ? "Server " + client.getCurrentServer().ip : "Singleplayer";
		}
		String hint = showCopyHint.get() && Keybinds.copyCoordinates != null && !Keybinds.copyCoordinates.isUnbound()
				? "Copy: " + Keybinds.copyCoordinates.getTranslatedKeyMessage().getString() : null;

		String[] values = {coords, facing, biome, dimension, chunk, light, time, realTime, server, fps, hint};
		for (String value : values) {
			if (value != null) {
				lines++;
			}
		}
		if (lines == 0) {
			return;
		}
		float width = 0.0F;
		for (String value : values) {
			if (value != null) {
				width = Math.max(width, font.width(value));
			}
		}
		float boxWidth = width * scaleFactor + padding * 2.0F;
		float boxHeight = lines * lineHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(boxHeight));
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF4FC3F7);
		float cursorY = y + padding;
		for (String value : values) {
			if (value == null) {
				continue;
			}
			HudPanel.text(graphics, font, value, x + padding, cursorY, 0xFFF2F2F7);
			cursorY += lineHeight;
		}
		builder.setLength(0);
	}

	private static String compass(float yaw) {
		float normalized = yaw % 360.0F;
		if (normalized < 0.0F) {
			normalized += 360.0F;
		}
		int index = Math.round(normalized / 45.0F) % 8;
		return COMPASS[index] + " (" + String.format(Locale.ROOT, "%.0f°", normalized) + ")";
	}

	private static String prettify(String path) {
		String[] parts = path.split("_");
		StringBuilder pretty = new StringBuilder(path.length());
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (pretty.length() > 0) {
				pretty.append(' ');
			}
			pretty.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return pretty.toString();
	}

}
