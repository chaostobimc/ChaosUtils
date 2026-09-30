package dev.chaosutils.util;

/**
 * Resolution independent HUD anchor.
 *
 * <p>{@code x} and {@code y} are fractions of the <em>available</em> space
 * ({@code screenSize - elementSize}), so {@code 0} pins the element to the left/top edge,
 * {@code 1} to the right/bottom edge and {@code 0.5} centers it. This keeps overlays in
 * place across different GUI scales and window sizes.
 *
 * @param x horizontal anchor, clamped to [0, 1]
 * @param y vertical anchor, clamped to [0, 1]
 */
public record HudPos(float x, float y) {
	public static final HudPos TOP_LEFT = new HudPos(0.02F, 0.02F);
	public static final HudPos TOP_RIGHT = new HudPos(0.98F, 0.02F);
	public static final HudPos BOTTOM_LEFT = new HudPos(0.02F, 0.98F);
	public static final HudPos BOTTOM_RIGHT = new HudPos(0.98F, 0.02F);
	public static final HudPos TOP_CENTER = new HudPos(0.5F, 0.02F);
	public static final HudPos CENTER = new HudPos(0.5F, 0.5F);

	public HudPos {
		x = clamp(x);
		y = clamp(y);
	}

	private static float clamp(float v) {
		return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
	}

	public int screenX(int screenWidth, int elementWidth) {
		return Math.round(x * Math.max(0, screenWidth - elementWidth));
	}

	public int screenY(int screenHeight, int elementHeight) {
		return Math.round(y * Math.max(0, screenHeight - elementHeight));
	}

	public HudPos offset(float dx, float dy) {
		return new HudPos(x + dx, y + dy);
	}

	public boolean isLeftHalf() {
		return x < 0.5F;
	}

	public boolean isTopHalf() {
		return y < 0.5F;
	}
}
