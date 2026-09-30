package dev.chaosutils.core;

/**
 * Frame and tick timing without touching any of the render/tick counter types.
 *
 * <p>ChaosUtils only needs two numbers for its animations: how far between two game ticks
 * the current frame sits ({@code partialTick}) and how long the last frame took
 * ({@code frameDelta}). Both are derived from {@link System#nanoTime()} with the tick
 * boundary refreshed in {@code END_CLIENT_TICK}, which keeps overlays perfectly smooth at
 * any frame rate and stays independent of the mapping names that changed in 1.21.9.
 */
public final class TickClock {
	private static final double TICK_NANOS = 50_000_000.0;

	private static long lastTickNanos = System.nanoTime();
	private static long lastFrameNanos = System.nanoTime();
	private static float frameDelta = 0.016F;

	private TickClock() {
	}

	public static void onClientTick() {
		lastTickNanos = System.nanoTime();
	}

	/** Interpolation progress between the previous and the current tick, in [0, 1]. */
	public static float partialTick() {
		double elapsed = System.nanoTime() - lastTickNanos;
		float value = (float) (elapsed / TICK_NANOS);
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}

	/** Seconds since the previous frame, clamped so a hitch cannot blow up an animation. */
	public static float frameDelta() {
		long now = System.nanoTime();
		float delta = (float) ((now - lastFrameNanos) / 1_000_000_000.0);
		lastFrameNanos = now;
		if (delta < 0.0F) {
			delta = 0.0F;
		}
		return Math.min(0.1F, delta);
	}

	public static void reset() {
		lastTickNanos = System.nanoTime();
		lastFrameNanos = System.nanoTime();
		frameDelta = 0.016F;
	}
}
