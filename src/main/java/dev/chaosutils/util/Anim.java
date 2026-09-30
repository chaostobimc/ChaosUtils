package dev.chaosutils.util;

/**
 * Frame-rate independent animation helpers.
 *
 * <p>All smoothing is exponential ("lerp towards the target with a time constant") which
 * is stable at any frame rate and never overshoots, so the GUI feels the same at 60 and at
 * 300 FPS. Springs are available for the few places where a tiny overshoot sells the
 * animation (toggles, expanding cards).
 */
public final class Anim {
	private Anim() {
	}

	public static float lerp(float from, float to, float t) {
		return from + (to - from) * t;
	}

	public static double lerp(double from, double to, double t) {
		return from + (to - from) * t;
	}

	/** Exponential smoothing: {@code speed} is roughly "how much of the gap per second". */
	public static float approach(float current, float target, float speed, float deltaSeconds) {
		float t = 1.0F - (float) Math.exp(-Math.max(0.001F, speed) * Math.max(0.0F, deltaSeconds));
		return current + (target - current) * t;
	}

	public static float clamp01(float value) {
		return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static int clamp(int value, int min, int max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static float easeOutCubic(float t) {
		float inverted = 1.0F - clamp01(t);
		return 1.0F - inverted * inverted * inverted;
	}

	public static float easeInOutCubic(float t) {
		float x = clamp01(t);
		return x < 0.5F ? 4.0F * x * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 3) / 2.0F;
	}

	public static float easeOutBack(float t) {
		float x = clamp01(t);
		float c1 = 1.70158F;
		float c3 = c1 + 1.0F;
		return 1.0F + c3 * (float) Math.pow(x - 1.0, 3) + c1 * (float) Math.pow(x - 1.0, 2);
	}

	public static float easeOutExpo(float t) {
		float x = clamp01(t);
		return x >= 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, -10.0 * x);
	}

	/** A spring value that keeps a velocity so it can overshoot and settle. */
	public static final class Spring {
		private float value;
		private float velocity;
		private float target;
		private final float stiffness;
		private final float damping;

		public Spring(float initial, float stiffness, float damping) {
			this.value = initial;
			this.target = initial;
			this.stiffness = stiffness;
			this.damping = damping;
		}

		public void set(float target) {
			this.target = target;
		}

		public void snap(float value) {
			this.value = value;
			this.target = value;
			this.velocity = 0.0F;
		}

		public float get() {
			return value;
		}

		public boolean settled(float tolerance) {
			return Math.abs(target - value) < tolerance && Math.abs(velocity) < tolerance;
		}

		public float update(float deltaSeconds) {
			float dt = Math.min(0.05F, Math.max(0.0F, deltaSeconds));
			// Sub-step for stability at low frame rates.
			int steps = Math.max(1, (int) Math.ceil(dt / 0.016F));
			float step = dt / steps;
			for (int i = 0; i < steps; i++) {
				float accel = (target - value) * stiffness - velocity * damping;
				velocity += accel * step;
				value += velocity * step;
			}
			return value;
		}
	}

	/** Simple animated float with an exponential approach, used everywhere in the HUD. */
	public static final class Value {
		private float current;
		private float target;
		private final float speed;

		public Value(float initial, float speed) {
			this.current = initial;
			this.target = initial;
			this.speed = speed;
		}

		public void set(float target) {
			this.target = target;
		}

		public void snap(float value) {
			this.current = value;
			this.target = value;
		}

		public float target() {
			return target;
		}

		public float get() {
			return current;
		}

		public boolean isSettled(float tolerance) {
			return Math.abs(current - target) < tolerance;
		}

		public float update(float deltaSeconds) {
			current = approach(current, target, speed, deltaSeconds);
			return current;
		}
	}
}
