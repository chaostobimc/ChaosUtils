package dev.chaosutils.feature.audio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
 * Advanced sound direction visualizer.
 *
 * <p>A circular radar around the crosshair showing where every positional sound of the last
 * few seconds came from: direction from the sound position, distance scaled to the ring,
 * colour by sound class, size and opacity by volume and age. A list mode is available for
 * players who prefer text.
 *
 * <p>The data comes from sounds the client is already playing - no packet, no server side
 * anything. Only positional sounds are shown, so music and UI clicks never clutter the radar.
 */
public final class SoundRadar implements Feature {
	public static final String ID = "sound_radar";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Choice style;
	private static Setting.Number radius;
	private static Setting.Number maxDistance;
	private static Setting.Number maxEntries;
	private static Setting.Number maxAge;
	private static Setting.Number minVolume;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle directionLabels;
	private static Setting.Toggle pulse;
	private static Setting.Number scale;

	private static final List<SoundTracker.Entry> CACHE = new ArrayList<>(32);

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Sound Radar",
				"A circular display that shows where important sounds came from.", Category.AUDIO, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the radar is drawn.", 0.5F, 0.62F));
		style = (Setting.Choice) module.add(new Setting.Choice("style", "Style",
				"Radar, text list or both.", 0, "Radar", "List", "Both"));
		radius = (Setting.Number) module.add(new Setting.Number("radius", "Radius",
				"Size of the radar ring in pixels.", 46.0, 20.0, 120.0, 2.0, "px"));
		maxDistance = (Setting.Number) module.add(new Setting.Number("distance", "Range",
				"Distance mapped to the outer ring.", 24.0, 4.0, 128.0, 2.0, "m"));
		maxEntries = (Setting.Number) module.add(new Setting.Number("entries", "Max sounds",
				"Maximum number of sounds shown at once.", 12.0, 1.0, 32.0, 1.0));
		maxAge = (Setting.Number) module.add(new Setting.Number("age", "Fade after",
				"Seconds a sound stays on the radar.", 3.0, 0.5, 6.0, 0.5, "s"));
		minVolume = (Setting.Number) module.add(new Setting.Number("volume", "Minimum volume",
				"Ignore sounds quieter than this.", 0.15, 0.0, 1.0, 0.05));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance_text", "Show distances",
				"Distance in metres next to the closest sounds.", true));
		directionLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Show directions",
				"Front / back / left / right markers on the ring.", true));
		pulse = (Setting.Toggle) module.add(new Setting.Toggle("pulse", "Pulse",
				"Very subtle pulse on the closest sound.", true));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Compact list scale",
				"Relative size of the list mode.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (!ModuleManager.enabled(ID) || client.player == null || !HudPanel.visibleNow()) {
			return;
		}
		float radarRadius = radius.getFloat() * HudPanel.scale();
		float centerX = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(radarRadius * 2.0F)) + radarRadius;
		float centerY = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(radarRadius * 2.0F)) + radarRadius;
		int styleValue = style.get();
		List<SoundTracker.Entry> entries = collect(client.player);
		if (entries.isEmpty() && styleValue == 1) {
			return;
		}
		if (styleValue == 0 || styleValue == 2) {
			drawRadar(graphics, entries, centerX, centerY, radarRadius);
		}
		if (styleValue == 1 || styleValue == 2) {
			drawList(graphics, entries, centerX, centerY, radarRadius);
		}
	}

	private static List<SoundTracker.Entry> collect(Player player) {
		CACHE.clear();
		List<SoundTracker.Entry> recent = SoundTracker.recent(64, true);
		double minVolumeValue = minVolume.get();
		double maxAgeValue = maxAge.get();
		double maxDistanceValue = maxDistance.get();
		for (SoundTracker.Entry entry : recent) {
			if (entry.volume() < minVolumeValue || entry.ageSeconds() > maxAgeValue) {
				continue;
			}
			if (entry.position().distanceTo(player.position()) > maxDistanceValue) {
				continue;
			}
			CACHE.add(entry);
			if (CACHE.size() >= Math.max(1, maxEntries.getInt())) {
				break;
			}
		}
		CACHE.sort(Comparator.comparingDouble(entry -> entry.position().distanceToSqr(player.position())));
		return CACHE;
	}

	private static void drawRadar(GuiGraphics graphics, List<SoundTracker.Entry> entries, float centerX, float centerY,
			float radarRadius) {
		Render.roundedBorder(graphics, centerX - radarRadius, centerY - radarRadius, radarRadius * 2.0F, radarRadius * 2.0F,
				radarRadius, Math.max(1.0F, radarRadius * 0.04F), 0x40FFFFFF, 0x00000000);
		Render.roundedBorder(graphics, centerX - radarRadius * 0.5F, centerY - radarRadius * 0.5F, radarRadius,
				radarRadius, radarRadius * 0.5F, 1.0F, 0x20FFFFFF, 0x00000000);
		Render.rect(graphics, centerX - 1.0F, centerY - 1.0F, 2.0F, 2.0F, 0x80FFFFFF);
		if (directionLabels.get()) {
			Font font = Minecraft.getInstance().font;
			Render.text(graphics, font, "F", centerX - 2.0F, centerY - radarRadius - 9.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "B", centerX - 2.0F, centerY + radarRadius + 2.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "L", centerX - radarRadius - 9.0F, centerY - 4.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "R", centerX + radarRadius + 3.0F, centerY - 4.0F, 0x80FFFFFF, false);
		}
		double maxDistance = maxDistance.get();
		Font font = Minecraft.getInstance().font;
		int index = 0;
		for (SoundTracker.Entry entry : entries) {
			float bearing = Projection.bearingTo(entry.position().x, entry.position().z);
			double distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
			float fraction = (float) Math.min(1.0, distance / Math.max(1.0, maxDistance));
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			float ageFade = Anim.clamp01(1.0F - entry.ageSeconds() / (float) Math.max(0.5, maxAge.get()));
			float size = 3.0F + Anim.clamp01(entry.volume()) * 2.5F;
			if (pulse.get() && index == 0) {
				size += 1.0F + (float) Math.sin(System.nanoTime() / 250_000_000.0);
			}
			float radians = (float) Math.toRadians(bearing);
			float x = centerX + (float) Math.sin(radians) * radarRadius * fraction;
			float y = centerY - (float) Math.cos(radians) * radarRadius * fraction;
			int color = Render.alpha(kind.color(), Anim.clamp01(0.25F + ageFade * 0.75F));
			Render.rect(graphics, x - size * 0.5F, y - size * 0.5F, size, size, color);
			if (showDistance.get() && index < 3) {
				String text = Math.round(distance) + "m";
				Render.text(graphics, font, text, x + size, y - 4.0F, Render.alpha(0xFFFFFFFF, Anim.clamp01(ageFade)), true);
			}
			index++;
		}
	}

	private static void drawList(GuiGraphics graphics, List<SoundTracker.Entry> entries, float centerX, float centerY,
			float radarRadius) {
		if (entries.isEmpty()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		float lineHeight = 10.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		int limit = Math.min(entries.size(), Math.max(1, maxEntries.getInt()));
		float width = 60.0F * scaleFactor;
		for (SoundTracker.Entry entry : entries) {
			String text = SoundClasses.prettyName(entry.path());
			width = Math.max(width, font.width(text) * scaleFactor + 32.0F * scaleFactor);
		}
		float height = limit * lineHeight + padding * 2.0F;
		float x = centerX + radarRadius + 6.0F * scaleFactor;
		float y = centerY - height * 0.5F;
		if (style.get() == 1) {
			x = centerX - width * 0.5F;
		}
		HudPanel.panel(graphics, font, x, y, width, height, 0xFF4FC3F7);
		float cursorY = y + padding;
		int index = 0;
		for (SoundTracker.Entry entry : entries) {
			if (index >= limit) {
				break;
			}
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			float ageFade = Anim.clamp01(1.0F - entry.ageSeconds() / (float) Math.max(0.5, maxAge.get()));
			int color = Render.alpha(kind.color(), Anim.clamp01(0.35F + ageFade * 0.65F));
			double distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
			String text = SoundClasses.symbol(kind) + " " + SoundClasses.prettyName(entry.path());
			HudPanel.text(graphics, font, text, x + padding, cursorY, color);
			String range = Math.round(distance) + "m";
			HudPanel.text(graphics, font, range, x + width - padding - font.width(range), cursorY, 0xFFBFC2CF);
			cursorY += lineHeight;
			index++;
		}
	}

	@Override
	public void onWorldLeave() {
		SoundTracker.clear();
	}
}
