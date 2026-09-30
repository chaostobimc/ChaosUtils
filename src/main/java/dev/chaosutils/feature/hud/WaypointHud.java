package dev.chaosutils.feature.hud;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/**
 * Death position marker and client side waypoints.
 *
 * <p>When you die, the exact position and dimension are stored locally (nothing is sent
 * anywhere). Marker beams, on screen labels and off screen arrows then guide you back; the
 * same system handles manually added waypoints. All data lives in the ChaosUtils config
 * file, so it survives restarts.
 */
public final class WaypointHud implements Feature {
	public static final String ID = "waypoints";

	private static Module module;
	private static Setting.Toggle deathMarkers;
	private static Setting.Number deathLifetime;
	private static Setting.Toggle beacons;
	private static Setting.Number beaconRange;
	private static Setting.Toggle showLabels;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle showArrows;
	private static Setting.Number maxDistance;
	private static Setting.Number markerSize;
	private static Setting.Number autoDeleteRadius;
	private static Setting.Toggle expireNotifications;

	private static boolean deathRecorded;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Waypoints & Death Marker",
				"Client side markers with beams, distances and off screen arrows.", Category.HUD, true));
		deathMarkers = (Setting.Toggle) module.add(new Setting.Toggle("death", "Death marker",
				"Store your death position and guide you back to it.", true));
		deathLifetime = (Setting.Number) module.add(new Setting.Number("death_lifetime", "Death marker lifetime",
				"Minutes until the death marker disappears (0 = forever).", 30.0, 0.0, 240.0, 5.0, "min"));
		beacons = (Setting.Toggle) module.add(new Setting.Toggle("beacons", "Beam",
				"Draw a translucent vertical beam at the marker.", true));
		beaconRange = (Setting.Number) module.add(new Setting.Number("beacon_range", "Beam range",
				"Only draw the beam within this distance.", 192.0, 16.0, 512.0, 16.0, "m"));
		showLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Labels",
				"Marker name above the icon.", true));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance", "Distance",
				"Distance in metres next to the label.", true));
		showArrows = (Setting.Toggle) module.add(new Setting.Toggle("arrows", "Off screen arrows",
				"Point towards markers that are outside the screen.", true));
		maxDistance = (Setting.Number) module.add(new Setting.Number("max_distance", "Maximum distance",
				"Markers further away than this are ignored.", 512.0, 32.0, 4096.0, 32.0, "m"));
		markerSize = (Setting.Number) module.add(new Setting.Number("marker_size", "Marker size",
				"Size of the marker icon.", 1.0, 0.5, 2.5, 0.1, "x"));
		autoDeleteRadius = (Setting.Number) module.add(new Setting.Number("arrival", "Auto delete radius",
				"Delete the marker once you are this close.", 4.0, 0.0, 32.0, 1.0, "m"));
		expireNotifications = (Setting.Toggle) module.add(new Setting.Toggle("chat_note", "Chat feedback",
				"Write a short line to chat when a marker is added.", true));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		Player player = client.player;
		if (player == null || client.level == null) {
			return;
		}
		// Death detection: exactly one marker per death, stored locally.
		if (deathMarkers.get() && player.isDeadOrDying()) {
			if (!deathRecorded) {
				deathRecorded = true;
				long lifetime = (long) (deathLifetime.get() * 60_000.0);
				Waypoint waypoint = new Waypoint("Death", player.getX(), player.getY(), player.getZ(),
						client.level.dimension().location().toString(), 0xFFE05B5B, true, lifetime);
				if (lifetime <= 0L) {
					waypoint.expiresAt = 0L;
				}
				ChaosConfig.WAYPOINTS.add(waypoint);
				ChaosConfig.markDirty();
				notify(client, "Death position stored");
			}
		} else if (!player.isDeadOrDying() && player.getHealth() > 0.0F) {
			deathRecorded = false;
		}

		if (Keybinds.addWaypoint != null && Keybinds.addWaypoint.wasPressed()) {
			Waypoint waypoint = new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
					player.getX(), player.getY(), player.getZ(),
					client.level.dimension().location().toString(), 0xFF7C5CFF, false, 0L);
			ChaosConfig.WAYPOINTS.add(waypoint);
			ChaosConfig.markDirty();
			notify(client, "Waypoint added: " + waypoint.name);
		}

		prune(player, client);
	}

	private static void prune(Player player, Minecraft client) {
		double arrival = autoDeleteRadius.get();
		Iterator<Waypoint> iterator = ChaosConfig.WAYPOINTS.iterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			Waypoint waypoint = iterator.next();
			if (waypoint.expired()) {
				iterator.remove();
				changed = true;
				continue;
			}
			if (arrival > 0.0 && waypoint.dimension.equals(currentDimension(client))) {
				double dx = waypoint.x - player.getX();
				double dy = waypoint.y - player.getY();
				double dz = waypoint.z - player.getZ();
				if (dx * dx + dy * dy + dz * dz <= arrival * arrival) {
					iterator.remove();
					changed = true;
					notify(client, "Arrived at " + waypoint.name);
				}
			}
		}
		if (changed) {
			ChaosConfig.markDirty();
		}
	}

	private static void notify(Minecraft client, String text) {
		if (expireNotifications.get() && client.player != null) {
			client.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[ChaosUtils] §f" + text), true);
		}
	}

	private static String currentDimension(Minecraft client) {
		return client.level == null ? "minecraft:overworld" : client.level.dimension().location().toString();
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || client.level == null || !Projection.isReady() || !HudPanel.visibleNow()) {
			return;
		}
		String dimension = currentDimension(client);
		double limit = maxDistance.get();
		double limitSquared = limit * limit;
		Font font = client.font;
		float size = markerSize.getFloat() * HudPanel.scale();
		float delta = dev.chaosutils.core.TickClock.frameDelta();

		for (Waypoint waypoint : List.copyOf(ChaosConfig.WAYPOINTS)) {
			if (!waypoint.dimension.equals(dimension)) {
				continue;
			}
			double distanceSquared = player.distanceToSqr(waypoint.x, waypoint.y, waypoint.z);
			if (distanceSquared > limitSquared) {
				continue;
			}
			Projection.Point point = Projection.project(waypoint.x, waypoint.y, waypoint.z);
			double distance = Math.sqrt(distanceSquared);
			if (!Float.isNaN(point.x()) && point.depth() > 0.1F) {
				if (beacons.get() && distance <= beaconRange.get()) {
					int beamColor = Render.alpha(waypoint.color, 0.16F);
					Render.rect(graphics, point.x() - 1.0F, 0.0F, 2.0F, graphics.getScaledWindowHeight(), beamColor);
				}
				if (point.onScreen(64.0F)) {
					drawMarker(graphics, font, waypoint, point.x(), point.y(), distance, size, delta);
				} else if (showArrows.get()) {
					drawArrow(graphics, font, waypoint, point.x(), point.y(), distance, size);
				}
			} else if (showArrows.get()) {
				drawArrow(graphics, font, waypoint, Float.NaN, Float.NaN, distance, size);
			}
		}
	}

	private static void drawMarker(GuiGraphics graphics, Font font, Waypoint waypoint, float x, float y, double distance,
			float size, float delta) {
		int color = waypoint.color;
		float half = 4.0F * size;
		// Diamond marker drawn from two stacked rectangles - readable at any scale.
		Render.rect(graphics, x - half, y - half * 0.5F, half * 2.0F, half, Render.alpha(color, 0.85F));
		Render.rect(graphics, x - half * 0.5F, y - half, half, half * 2.0F, Render.alpha(color, 0.85F));
		Render.roundedBorder(graphics, x - half, y - half, half * 2.0F, half * 2.0F, 2.0F, 1.0F,
				Render.alpha(0xFFFFFFFF, 0.5F), 0x00000000);
		String label = waypoint.name;
		if (showDistance.get()) {
			label = label + "  " + formatDistance(distance) + "m";
		}
		if (showLabels.get()) {
			HudPanel.text(graphics, font, label, x - font.width(label) * 0.5F, y - 14.0F * size, 0xFFFFFFFF);
		} else if (showDistance.get()) {
			String text = formatDistance(distance) + "m";
			HudPanel.text(graphics, font, text, x - font.width(text) * 0.5F, y - 14.0F * size, 0xFFFFFFFF);
		}
	}

	private static void drawArrow(GuiGraphics graphics, Font font, Waypoint waypoint, float targetX, float targetY,
			double distance, float size) {
		float centerX = graphics.getScaledWindowWidth() * 0.5F;
		float centerY = graphics.getScaledWindowHeight() * 0.5F;
		float bearing;
		if (Float.isNaN(targetX)) {
			bearing = Projection.bearingTo(waypoint.x, waypoint.z);
		} else {
			bearing = (float) Math.toDegrees(Math.atan2(targetX - centerX, -(targetY - centerY)));
		}
		float radians = (float) Math.toRadians(bearing);
		float radius = Math.min(centerX, centerY) - 26.0F;
		float x = centerX + (float) Math.sin(radians) * radius;
		float y = centerY - (float) Math.cos(radians) * radius;
		int color = waypoint.color;
		float arrowLength = 11.0F * size;
		float arrowWidth = 6.0F * size;
		float dirX = (float) Math.sin(radians);
		float dirY = -(float) Math.cos(radians);
		float perpX = -dirY;
		float perpY = dirX;
		// Chevron: two wings from the tip plus a short shaft behind it.
		Render.line(graphics, x, y, x - dirX * arrowLength + perpX * arrowWidth, y - dirY * arrowLength + perpY * arrowWidth,
				2.0F * size, Render.alpha(color, 0.95F));
		Render.line(graphics, x, y, x - dirX * arrowLength - perpX * arrowWidth, y - dirY * arrowLength - perpY * arrowWidth,
				2.0F * size, Render.alpha(color, 0.95F));
		Render.line(graphics, x, y, x - dirX * arrowLength * 0.55F, y - dirY * arrowLength * 0.55F,
				1.5F * size, Render.alpha(color, 0.65F));
		String label = formatDistance(distance) + "m";
		HudPanel.text(graphics, font, label, x - font.width(label) * 0.5F, y + 6.0F * size, Render.alpha(0xFFFFFFFF, 0.85F));
	}

	private static String formatDistance(double distance) {
		if (distance >= 1000.0) {
			return String.format(Locale.ROOT, "%.1fk", distance / 1000.0);
		}
		return Integer.toString((int) Math.round(distance));
	}

	@Override
	public void onWorldJoin() {
		deathRecorded = false;
	}

	@Override
	public void onDisabled() {
		deathRecorded = false;
	}
}
