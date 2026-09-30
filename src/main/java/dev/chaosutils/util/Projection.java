package dev.chaosutils.util;

import dev.chaosutils.feature.Features;
import dev.chaosutils.feature.visual.PerspectiveLock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * World to screen projection for HUD overlays.
 *
 * <p>The camera basis is rebuilt once per frame from the camera position and the view
 * rotation, so projecting an arbitrary number of points afterwards is a handful of
 * multiplications per point - no matrix objects, no allocations per point and no
 * dependency on the render pipeline API that keeps changing between 1.21.x releases.
 */
public final class Projection {
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static double rightX;
	private static double rightY;
	private static double rightZ;
	private static double upX;
	private static double upY;
	private static double upZ;
	private static double forwardX;
	private static double forwardY;
	private static double forwardZ;
	private static double focalLength;
	private static float screenWidth;
	private static float screenHeight;
	private static float centerX;
	private static float centerY;
	private static boolean ready;

	private Projection() {
	}

	public record Point(float x, float y, float depth, float scale) {
		public boolean onScreen(float margin) {
			return x >= -margin && x <= screenWidth + margin && y >= -margin && y <= screenHeight + margin;
		}
	}

	/**
	 * Must be called once per frame before projecting, with the HUD's own scaled size so
	 * overlay positions match the coordinates {@link net.minecraft.client.gui.GuiGraphics}
	 * draws in.
	 */
	public static void setup(Minecraft client, float screenWidth, float screenHeight, float partialTick) {
		ready = false;
		if (client.player == null || client.level == null) {
			return;
		}
		Vec3 camera = null;
		try {
			camera = client.gameRenderer.getMainCamera().position();
		} catch (Throwable ignored) {
			camera = null;
		}
		if (camera == null) {
			camera = client.player.getEyePosition(partialTick);
		}
		float yaw = PerspectiveLock.cameraYaw(client.player, partialTick);
		float pitch = PerspectiveLock.cameraPitch(client.player, partialTick);

		cameraX = camera.x;
		cameraY = camera.y;
		cameraZ = camera.z;

		double yawRad = Math.toRadians(yaw);
		double pitchRad = Math.toRadians(pitch);
		double cosPitch = Math.cos(pitchRad);
		double sinPitch = Math.sin(pitchRad);
		double sinYaw = Math.sin(yawRad);
		double cosYaw = Math.cos(yawRad);

		forwardX = -sinYaw * cosPitch;
		forwardY = -sinPitch;
		forwardZ = cosYaw * cosPitch;

		rightX = -cosYaw;
		rightY = 0.0;
		rightZ = -sinYaw;

		upX = rightY * forwardZ - rightZ * forwardY;
		upY = rightZ * forwardX - rightX * forwardZ;
		upZ = rightX * forwardY - rightY * forwardX;
		double upLength = Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		if (upLength > 1.0E-6) {
			upX /= upLength;
			upY /= upLength;
			upZ /= upLength;
		}

		double fov = configuredFov(client);
		focalLength = 1.0 / Math.tan(Math.toRadians(fov) * 0.5);
		Projection.screenWidth = Math.max(1.0F, screenWidth);
		Projection.screenHeight = Math.max(1.0F, screenHeight);
		centerX = Projection.screenWidth * 0.5F;
		centerY = Projection.screenHeight * 0.5F;
		ready = true;
	}

	private static double configuredFov(Minecraft client) {
		double fov = 70.0;
		try {
			Object value = client.options.fov().get();
			if (value instanceof Number number) {
				fov = number.doubleValue();
			}
		} catch (Throwable ignored) {
			// keep the default
		}
		fov *= Features.zoomFactor();
		return Math.max(5.0, Math.min(170.0, fov));
	}

	public static boolean isReady() {
		return ready;
	}

	public static float screenWidth() {
		return screenWidth;
	}

	public static float screenHeight() {
		return screenHeight;
	}

	public static double cameraX() {
		return cameraX;
	}

	public static double cameraY() {
		return cameraY;
	}

	public static double cameraZ() {
		return cameraZ;
	}

	public static double distanceTo(double x, double y, double z) {
		double dx = x - cameraX;
		double dy = y - cameraY;
		double dz = z - cameraZ;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Projects a world position into HUD coordinates; {@code depth} is the camera-space Z. */
	public static Point project(double worldX, double worldY, double worldZ) {
		if (!ready) {
			return new Point(Float.NaN, Float.NaN, -1.0F, 0.0F);
		}
		double dx = worldX - cameraX;
		double dy = worldY - cameraY;
		double dz = worldZ - cameraZ;

		double depth = dx * forwardX + dy * forwardY + dz * forwardZ;
		if (depth <= 0.05) {
			return new Point(Float.NaN, Float.NaN, (float) depth, 0.0F);
		}
		double rightAmount = dx * rightX + dy * rightY + dz * rightZ;
		double upAmount = dx * upX + dy * upY + dz * upZ;

		double aspect = screenWidth / screenHeight;
		double ndcX = (rightAmount / depth) * focalLength / aspect;
		double ndcY = (upAmount / depth) * focalLength;

		float screenX = (float) ((ndcX * 0.5 + 0.5) * screenWidth);
		float screenY = (float) ((0.5 - ndcY * 0.5) * screenHeight);
		float scale = (float) (focalLength / Math.max(0.5, depth) * screenHeight * 0.5);
		return new Point(screenX, screenY, (float) depth, scale);
	}

	/** Bearing in degrees relative to the camera view, negative is left. */
	public static float bearingTo(double x, double z) {
		double dx = x - cameraX;
		double dz = z - cameraZ;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-4) {
			return 0.0F;
		}
		double angle = Math.toDegrees(Math.atan2(dx, dz));
		double forwardAngle = Math.toDegrees(Math.atan2(forwardX, forwardZ));
		double relative = angle - forwardAngle;
		while (relative <= -180.0) {
			relative += 360.0;
		}
		while (relative > 180.0) {
			relative -= 360.0;
		}
		return (float) relative;
	}

	public static double forwardX() {
		return forwardX;
	}

	public static double forwardZ() {
		return forwardZ;
	}
}
