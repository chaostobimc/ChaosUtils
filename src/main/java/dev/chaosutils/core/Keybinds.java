package dev.chaosutils.core;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * All ChaosUtils hotkeys are real {@link KeyMapping}s, so players rebind them in the
 * vanilla Controls screen and ChaosUtils never steals a key silently.
 *
 * <p>Default keys intentionally avoid every vanilla binding (G was free, C, Y, ALT and
 * the rest are unbound in vanilla); the less common actions default to unbound so they
 * are opt-in.
 */
public final class Keybinds {
	private Keybinds() {
	}

	public static KeyMapping openGui;
	public static KeyMapping radialMenu;
	public static KeyMapping zoom;
	public static KeyMapping freeLook;
	public static KeyMapping searchContainer;
	public static KeyMapping copyCoordinates;
	public static KeyMapping chatHistory;
	public static KeyMapping screenshotPopup;
	public static KeyMapping toggleGamma;
	public static KeyMapping panicToggle;
	public static KeyMapping addWaypoint;

	public static void init() {
		openGui = register("open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT);
		radialMenu = register("radial_menu", GLFW.GLFW_KEY_G);
		zoom = register("zoom", GLFW.GLFW_KEY_C);
		freeLook = register("free_look", GLFW.GLFW_KEY_LEFT_ALT);
		searchContainer = register("search_container", GLFW.GLFW_KEY_Y);
		copyCoordinates = register("copy_coordinates", GLFW.GLFW_KEY_UNKNOWN);
		chatHistory = register("chat_history", GLFW.GLFW_KEY_UNKNOWN);
		screenshotPopup = register("screenshot_popup", GLFW.GLFW_KEY_UNKNOWN);
		toggleGamma = register("toggle_gamma", GLFW.GLFW_KEY_UNKNOWN);
		panicToggle = register("panic_toggle", GLFW.GLFW_KEY_UNKNOWN);
		addWaypoint = register("add_waypoint", GLFW.GLFW_KEY_UNKNOWN);
	}

	/** Every ChaosUtils hotkey, in the order they are shown in the interface. */
	public static java.util.List<KeyMapping> all() {
		java.util.List<KeyMapping> mappings = new java.util.ArrayList<>();
		for (KeyMapping mapping : new KeyMapping[] {openGui, radialMenu, zoom, freeLook, searchContainer,
				copyCoordinates, chatHistory, screenshotPopup, toggleGamma, panicToggle, addWaypoint}) {
			if (mapping != null) {
				mappings.add(mapping);
			}
		}
		return mappings;
	}

	private static KeyMapping register(String name, int defaultKey) {
		KeyMapping mapping = new KeyMapping(
				"key.chaosutils." + name,
				InputConstants.Type.KEYSYM,
				defaultKey,
				KeyMapping.Category.MISC);
		return KeyBindingHelper.registerKeyBinding(mapping);
	}
}
