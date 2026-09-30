package dev.chaosutils.feature.audio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.SoundTracker;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/**
 * Subtitle HUD enhancer.
 *
 * <p>Replaces the cramped bottom-of-screen subtitle list with a readable panel: one line per
 * distinct sound, grouped repetitions ("Zombie Growl ×3"), colour per sound class, a
 * direction arrow and the distance. Because the list is built from the sounds the client
 * played anyway, it works identically in singleplayer and on servers.
 *
 * <p>The vanilla subtitle list is switched off through the regular vanilla option while this
 * feature is active and switched back afterwards, so you never see both at once.
 */
public final class SubtitlesPlus implements Feature {
	public static final String ID = "subtitles_plus";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Number maxLines;
	private static Setting.Number maxAge;
	private static Setting.Number minVolume;
	private static Setting.Toggle groupDuplicates;
	private static Setting.Toggle showDirection;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle onlyPositional;
	private static Setting.Toggle hideVanilla;
	private static Setting.Toggle importantOnly;
	private static Setting.Number scale;

	private static boolean vanillaWasOn;
	private static boolean vanillaChanged;

	private static final Map<String, Row> ROWS = new LinkedHashMap<>();

	private static final class Row {
		private final String path;
		/** Pretty name, computed once so the HUD does not re-derive it every frame. */
		private final String name;
		private int count;
		private long lastAt;
		private float bearing;
		private double distance;
		private SoundClasses.Kind kind;

		private Row(String path) {
			this.path = path;
			this.name = SoundClasses.prettyName(path);
		}
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Enhanced Subtitles",
				"Readable subtitle panel with colours, grouping, direction and distance.", Category.AUDIO, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the subtitle panel is drawn.", 0.02F, 0.78F));
		maxLines = (Setting.Number) module.add(new Setting.Number("lines", "Maximum lines",
				"How many distinct sounds are listed at once.", 5.0, 1.0, 12.0, 1.0));
		maxAge = (Setting.Number) module.add(new Setting.Number("age", "Fade after",
				"Seconds a line stays visible.", 2.5, 0.5, 8.0, 0.5, "s"));
		minVolume = (Setting.Number) module.add(new Setting.Number("volume", "Minimum volume",
				"Ignore very quiet sounds.", 0.2, 0.0, 1.0, 0.05));
		groupDuplicates = (Setting.Toggle) module.add(new Setting.Toggle("group", "Group repeats",
				"Show a counter instead of the same line several times.", true));
		showDirection = (Setting.Toggle) module.add(new Setting.Toggle("direction", "Direction arrow",
				"Where the sound came from, relative to where you look.", true));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance", "Distance",
				"Distance in metres.", true));
		onlyPositional = (Setting.Toggle) module.add(new Setting.Toggle("positional", "Positional only",
				"Ignore sounds without a position in the world.", true));
		hideVanilla = (Setting.Toggle) module.add(new Setting.Toggle("hide_vanilla", "Hide vanilla subtitles",
				"Turn the vanilla subtitle list off while this panel is active.", true));
		importantOnly = (Setting.Toggle) module.add(new Setting.Toggle("important", "Important sounds only",
				"Skip ambient and unclassified sounds.", false));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the panel.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			ROWS.clear();
			return;
		}
		if (!isEnabled()) {
			restoreVanilla(client);
			ROWS.clear();
			return;
		}
		applyVanilla(client);
		long now = System.currentTimeMillis();
		double ageLimit = maxAge.get() * 1000.0;
		List<SoundTracker.Entry> recent = SoundTracker.recent(48, onlyPositional.get());
		for (SoundTracker.Entry entry : recent) {
			if (entry.volume() < minVolume.get()) {
				continue;
			}
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			if (importantOnly.get() && (kind == SoundClasses.Kind.AMBIENT || kind == SoundClasses.Kind.OTHER
					|| kind == SoundClasses.Kind.MUSIC)) {
				continue;
			}
			long age = now - entry.timeMillis();
			if (age > ageLimit) {
				continue;
			}
			Row row = ROWS.get(entry.path());
			if (row == null) {
				if (ROWS.size() >= 24) {
					continue;
				}
				row = new Row(entry.path());
				ROWS.put(entry.path(), row);
			}
			row.count++;
			row.lastAt = entry.timeMillis();
			row.kind = kind;
			row.bearing = Projection.bearingTo(entry.position().x, entry.position().z);
			row.distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
		}
		// Drop expired rows so the map can never grow.
		ROWS.values().removeIf(row -> now - row.lastAt > ageLimit);
	}

	private static void applyVanilla(Minecraft client) {
		if (!hideVanilla.get() || vanillaChanged) {
			if (!hideVanilla.get() && vanillaChanged) {
				restoreVanilla(client);
			}
			return;
		}
		try {
			vanillaWasOn = client.options.showSubtitles().get();
			if (vanillaWasOn) {
				client.options.showSubtitles().set(false);
				vanillaChanged = true;
			}
		} catch (Throwable ignored) {
			vanillaChanged = false;
		}
	}

	private static void restoreVanilla(Minecraft client) {
		if (!vanillaChanged) {
			return;
		}
		vanillaChanged = false;
		try {
			client.options.showSubtitles().set(vanillaWasOn);
		} catch (Throwable ignored) {
			// leave the option as it is
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || ROWS.isEmpty() || !HudPanel.visibleNow()) {
			return;
		}
		Font font = client.font;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		float lineHeight = 11.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		long now = System.currentTimeMillis();
		double ageLimit = maxAge.get() * 1000.0;

		List<Row> rows = new ArrayList<>(ROWS.values());
		rows.sort((a, b) -> Long.compare(b.lastAt, a.lastAt));
		int limit = Math.max(1, maxLines.getInt());
		if (rows.size() > limit) {
			rows = rows.subList(0, limit);
		}
		float width = 80.0F * scaleFactor;
		for (Row row : rows) {
			width = Math.max(width, font.width(caption(row)) * scaleFactor + padding * 2.0F + 26.0F * scaleFactor);
		}
		float height = rows.size() * lineHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.guiWidth(), Math.round(width));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(height));
		HudPanel.panel(graphics, font, x, y, width, height, 0xFF4FC3F7);
		float cursorY = y + padding;
		for (Row row : rows) {
			float ageFade = Anim.clamp01(1.0F - (now - row.lastAt) / (float) ageLimit);
			int color = Render.alpha(row.kind == null ? 0xFFF2F2F7 : row.kind.color(),
					Anim.clamp01(0.35F + ageFade * 0.65F));
			String caption = caption(row);
			HudPanel.text(graphics, font, caption, x + padding, cursorY, color);
			String suffix = suffix(row);
			HudPanel.text(graphics, font, suffix, x + width - padding - font.width(suffix), cursorY,
					Render.alpha(0xFFBFC2CF, Anim.clamp01(ageFade)));
			cursorY += lineHeight;
		}
	}

	private static String caption(Row row) {
		String symbol = row.kind == null ? "-" : SoundClasses.symbol(row.kind);
		String text = symbol + " " + row.name;
		if (groupDuplicates.get() && row.count > 1) {
			text = text + " ×" + row.count;
		}
		return text;
	}

	private static String suffix(Row row) {
		StringBuilder builder = new StringBuilder(12);
		if (showDirection.get()) {
			builder.append(arrow(row.bearing));
		}
		if (showDistance.get()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Math.round(row.distance)).append('m');
		}
		return builder.toString();
	}

	private static String arrow(float bearing) {
		float normalized = bearing;
		while (normalized <= -180.0F) {
			normalized += 360.0F;
		}
		while (normalized > 180.0F) {
			normalized -= 360.0F;
		}
		if (normalized >= -22.5F && normalized < 22.5F) {
			return "^";
		}
		if (normalized >= 22.5F && normalized < 67.5F) {
			return "/";
		}
		if (normalized >= 67.5F && normalized < 112.5F) {
			return ">";
		}
		if (normalized >= 112.5F && normalized < 157.5F) {
			return "\\";
		}
		if (normalized >= -67.5F && normalized < -22.5F) {
			return "\\";
		}
		if (normalized >= -112.5F && normalized < -67.5F) {
			return "<";
		}
		if (normalized >= -157.5F && normalized < -112.5F) {
			return "/";
		}
		return "v";
	}

	@Override
	public void onDisabled() {
		restoreVanilla(Minecraft.getInstance());
		ROWS.clear();
	}

	@Override
	public void onWorldLeave() {
		ROWS.clear();
	}
}
