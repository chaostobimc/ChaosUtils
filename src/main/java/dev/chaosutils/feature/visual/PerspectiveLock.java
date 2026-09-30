package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Perspective lock / camera preview.
 *
 * <p>While active the camera keeps the rotation you activated it with instead of following
 * your body. The player, the hitbox and every movement packet stay untouched - the server
 * sees an ordinary player; only the local camera angle is stabilised, and (optionally) the
 * vanilla third person view is used so the pulled back preview feels like a freecam. This
 * is the variant of "freecam" that cannot be used to gain information about chunks the
 * player cannot see, because the camera never leaves the player's own position.
 */
public final class PerspectiveLock implements Feature {
	public static final String ID = "perspective_lock";

	public enum Mode {
		FREEZE("Fully locked"),
		SLOW_FOLLOW("Slow follow"),
		YAW_LOCK("Yaw locked, free pitch");

		private final String label;

		Mode(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	private static Module module;
	private static Setting.Choice mode;
	private static Setting.Number followSpeed;
	private static Setting.Toggle thirdPerson;
	private static Setting.Toggle toggleMode;
	private static Setting.Number transitionSpeed;
	private static Setting.Toggle rememberRotation;

	private static boolean active;
	private static float lockedYaw;
	private static float lockedPitch;
	private static float renderedYaw;
	private static float renderedPitch;
	private static boolean initialised;
	private static CameraType previousCameraType;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Perspective Lock",
				"Lock the camera angle while your body keeps moving - perfect for screenshots and long walks.",
				Category.VISUAL, false));
		mode = (Setting.Choice) module.add(new Setting.Choice("mode", "Lock mode",
				"How the camera behaves while the key is held.", 0,
				Mode.FREEZE.label(), Mode.SLOW_FOLLOW.label(), Mode.YAW_LOCK.label()));
		followSpeed = (Setting.Number) module.add(new Setting.Number("follow_speed", "Follow speed",
				"Degrees per second the camera may drift back to your view in slow-follow mode.", 25.0, 1.0, 180.0, 1.0, "°/s"));
		thirdPerson = (Setting.Toggle) module.add(new Setting.Toggle("third_person", "Third person preview",
				"Switch to vanilla third person while locked (restores your camera afterwards).", true));
		toggleMode = (Setting.Toggle) module.add(new Setting.Toggle("toggle", "Toggle instead of hold",
				"The key toggles the lock instead of being held.", false));
		transitionSpeed = (Setting.Number) module.add(new Setting.Number("smoothing", "Smoothing",
				"How smoothly the camera returns when you release the key.", 8.0, 1.0, 20.0, 0.5, "x"));
		rememberRotation = (Setting.Toggle) module.add(new Setting.Toggle("remember", "Keep rotation on release",
				"Off: the camera snaps back to your body rotation.", true));
	}

	/** True while the camera rotation is being overridden. */
	public static boolean isActive() {
		return active && ModuleManager.enabled(ID);
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			active = false;
			return;
		}
		if (!isEnabled()) {
			deactivate(client);
			return;
		}
		boolean pressed = Keybinds.freeLook != null && Keybinds.freeLook.isPressed();
		if (toggleMode.get()) {
			if (pressed && Keybinds.freeLook.wasPressed()) {
				if (active) {
					deactivate(client);
				} else {
					activate(client);
				}
			}
		} else if (pressed) {
			if (!active) {
				activate(client);
			}
		} else if (active) {
			deactivate(client);
		}
		if (active) {
			updateRotation(client);
		}
	}

	private static void activate(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			return;
		}
		active = true;
		initialised = false;
		lockedYaw = player.getViewYRot(0.0F);
		lockedPitch = player.getViewXRot(0.0F);
		renderedYaw = lockedYaw;
		renderedPitch = lockedPitch;
		if (thirdPerson.get()) {
			try {
				previousCameraType = client.options.getCameraType();
				client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			} catch (Throwable ignored) {
				previousCameraType = null;
			}
		}
	}

	private static void deactivate(Minecraft client) {
		if (!active) {
			return;
		}
		active = false;
		if (previousCameraType != null) {
			try {
				client.options.setCameraType(previousCameraType);
			} catch (Throwable ignored) {
				// nothing to restore
			}
			previousCameraType = null;
		}
	}

	/** Only slow-follow mode moves the locked angles; the other modes keep them frozen. */
	private static void updateRotation(Minecraft client) {
		Player player = client.player;
		if (player == null || selectedMode() != Mode.SLOW_FOLLOW) {
			return;
		}
		float yawStep = followSpeed.getFloat() * (1.0F / 20.0F);
		lockedYaw = approachAngle(lockedYaw, player.getViewYRot(0.0F), yawStep);
		lockedPitch = Anim.approach(lockedPitch, player.getViewXRot(0.0F), yawStep * 0.5F, 1.0F / 20.0F);
	}

	private static Mode selectedMode() {
		Mode[] values = Mode.values();
		return values[Math.max(0, Math.min(values.length - 1, mode.get()))];
	}

	private static float approachAngle(float from, float to, float maxStep) {
		float delta = to - from;
		while (delta > 180.0F) {
			delta -= 360.0F;
		}
		while (delta < -180.0F) {
			delta += 360.0F;
		}
		if (Math.abs(delta) <= maxStep) {
			return to;
		}
		return from + Math.signum(delta) * maxStep;
	}

	/** Applied from the camera mixin after vanilla computed the camera. */
	public static void applyToCamera(Camera camera, Entity entity, float partialTick) {
		if (!active || camera == null) {
			return;
		}
		Mode selected = selectedMode();
		if (!initialised) {
			initialised = true;
			renderedYaw = camera.getYRot();
			renderedPitch = camera.getXRot();
		}
		float targetYaw = lockedYaw;
		// "Yaw locked, free pitch" keeps the vertical look free for a natural preview.
		float targetPitch = selected == Mode.YAW_LOCK ? camera.getXRot() : lockedPitch;
		float speed = rememberRotation.get() ? transitionSpeed.getFloat() : 30.0F;
		float delta = TickClock.frameDelta();
		renderedYaw = approachWrapped(renderedYaw, targetYaw, speed, delta);
		renderedPitch = Anim.approach(renderedPitch, targetPitch, speed, delta);
		camera.setRotation(renderedYaw, renderedPitch);
	}

	private static float approachWrapped(float current, float target, float speed, float deltaSeconds) {
		float difference = target - current;
		while (difference > 180.0F) {
			difference -= 360.0F;
		}
		while (difference < -180.0F) {
			difference += 360.0F;
		}
		float factor = 1.0F - (float) Math.exp(-Math.max(0.001F, speed) * Math.max(0.0F, deltaSeconds));
		return current + difference * factor;
	}

	/**
	 * Camera yaw used by HUD projections. When the lock is active the overlay markers follow
	 * the locked camera instead of the player's body, which is what the player sees.
	 */
	public static float cameraYaw(Player player, float partialTick) {
		if (player == null) {
			return 0.0F;
		}
		if (isActive()) {
			return renderedYaw;
		}
		try {
			return player.getViewYRot(partialTick);
		} catch (Throwable ignored) {
			return player.getYRot();
		}
	}

	public static float cameraPitch(Player player, float partialTick) {
		if (player == null) {
			return 0.0F;
		}
		if (isActive()) {
			return renderedPitch;
		}
		try {
			return player.getViewXRot(partialTick);
		} catch (Throwable ignored) {
			return player.getXRot();
		}
	}

	@Override
	public void onDisabled() {
		deactivate(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		active = false;
		previousCameraType = null;
		initialised = false;
	}
}
