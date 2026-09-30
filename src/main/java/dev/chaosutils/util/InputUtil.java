package dev.chaosutils.util;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/**
 * Small helpers around GLFW key codes. Everything here is read-only input polling -
 * ChaosUtils never injects synthetic input events.
 */
public final class InputUtil {
	public static final int NO_KEY = -1;

	private InputUtil() {
	}

	public static String keyName(int code) {
		if (code == NO_KEY) {
			return "None";
		}
		if (code <= -100) {
			// Mouse buttons are stored as -100 - button so they can share the numeric field.
			int button = -100 - code;
			return switch (button) {
				case 0 -> "Mouse Left";
				case 1 -> "Mouse Right";
				case 2 -> "Mouse Middle";
				default -> "Mouse " + (button + 1);
			};
		}
		try {
			return InputConstants.Type.KEYSYM.getOrCreate(code).getDisplayName();
		} catch (Throwable ignored) {
			return "Key " + code;
		}
	}

	public static boolean isPressed(int code) {
		if (code == NO_KEY) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		long window = client.getWindow().handle();
		try {
			if (code <= -100) {
				int button = -100 - code;
				return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
			}
			return InputConstants.isKeyDown(window, code);
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static int mouseButtonToCode(int button) {
		return -100 - button;
	}

	/**
	 * First input that is currently held down, or {@link #NO_KEY}.
	 * Polling GLFW directly is what makes the "press a key to bind" widgets work without
	 * depending on the 1.21.9+ input event record accessors.
	 */
	public static int currentlyHeld() {
		long window = Minecraft.getInstance().getWindow().handle();
		try {
			for (int button = 0; button < 8; button++) {
				if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
					return mouseButtonToCode(button);
				}
			}
			for (int key = 32; key <= 348; key++) {
				if (org.lwjgl.glfw.GLFW.glfwGetKey(window, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
					return key;
				}
			}
		} catch (Throwable ignored) {
			return NO_KEY;
		}
		return NO_KEY;
	}

	/** Returns an input that is held now but was not reported as the previously held input. */
	public static int pollNewInput(int previouslyHeld) {
		int held = currentlyHeld();
		if (held == NO_KEY || held == previouslyHeld) {
			return NO_KEY;
		}
		return held;
	}
}
