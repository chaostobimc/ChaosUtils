package dev.chaosutils.core;

import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Estimates the server tick rate from the client's point of view.
 *
 * <p>Purely local measurement: the client counts how fast the world time it receives
 * advances compared to the wall clock. No packet is sent and nothing is queried.
 */
public final class TpsEstimator {
	private static final double SAMPLE_WINDOW_MS = 1_000.0;

	private static long lastSampleTime;
	private static long lastGameTime = Long.MIN_VALUE;
	private static double smoothed = 20.0;
	private static double lastTickMillis = 50.0;

	private TpsEstimator() {
	}

	public static void sample(Minecraft client) {
		ClientLevel level = client.level;
		if (level == null) {
			lastGameTime = Long.MIN_VALUE;
			return;
		}
		long now = System.currentTimeMillis();
		long gameTime = level.getGameTime();
		if (lastGameTime == Long.MIN_VALUE) {
			lastGameTime = gameTime;
			lastSampleTime = now;
			return;
		}
		long elapsed = now - lastSampleTime;
		if (elapsed < SAMPLE_WINDOW_MS) {
			return;
		}
		long advanced = gameTime - lastGameTime;
		double tps = advanced / (elapsed / 1000.0);
		tps = Math.max(0.0, Math.min(20.0, tps));
		smoothed = Anim.lerp(smoothed, tps, 0.25);
		lastTickMillis = Anim.lerp(lastTickMillis, elapsed / Math.max(1.0, advanced), 0.25);
		lastGameTime = gameTime;
		lastSampleTime = now;
	}

	public static double tps() {
		return smoothed;
	}

	/** Average milliseconds one server tick took over the last sample window. */
	public static double tickMillis() {
		return lastTickMillis;
	}

	public static int ping(Minecraft client) {
		if (client.player == null || client.getConnection() == null) {
			return 0;
		}
		try {
			var info = client.getConnection().getPlayerInfo(client.player.getUUID());
			return info == null ? 0 : info.getLatency();
		} catch (Throwable ignored) {
			return 0;
		}
	}

	public static void reset() {
		lastGameTime = Long.MIN_VALUE;
		smoothed = 20.0;
	}
}
