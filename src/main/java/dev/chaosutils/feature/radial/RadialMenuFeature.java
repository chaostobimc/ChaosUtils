package dev.chaosutils.feature.radial;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * The interactive radial menu - ChaosUtils' centrepiece.
 *
 * <p>Hold the radial key and a ring of slices appears around the mouse; move onto a slice and
 * release the key to run it. Slices are the entries from the config (create, rename, recolour,
 * reorder and delete them in the radial editor), and every slice performs exactly one action
 * that you asked for: send a command, send chat text, copy text to the clipboard, open the
 * ChaosUtils interface or toggle all overlays.
 *
 * <p>Nothing here is automated: the menu runs a single action on the single key release the
 * player performed. That is the same as typing the command by hand - which is exactly why it
 * is safe on servers with anti-cheat.
 */
public final class RadialMenuFeature implements Feature {
	public static final String ID = "radial_menu";

	private static Module module;
	private static Setting.Toggle holdMode;
	private static Setting.Number innerRadius;
	private static Setting.Number outerRadius;
	private static Setting.Number gapDegrees;
	private static Setting.Toggle showLabels;
	private static Setting.Toggle showIcons;
	private static Setting.Toggle showCenterText;
	private static Setting.Toggle centerOnCursor;
	private static Setting.Number deadZone;
	private static Setting.Color highlightColor;
	private static Setting.Number hoverScale;
	private static Setting.Number animationSpeed;
	private static Setting.Toggle dimBackground;
	private static Setting.Number dimStrength;
	private static Setting.Toggle chatFeedback;

	private static RadialMenuScreen open;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Radial Menu",
				"Hold a key, point at a slice, release - commands and actions at your fingertips.",
				Category.RADIAL, true));
		holdMode = (Setting.Toggle) module.add(new Setting.Toggle("hold", "Open while held",
				"On: the menu is open as long as the key is held. Off: the key toggles it.", true));
		innerRadius = (Setting.Number) module.add(new Setting.Number("inner", "Inner radius",
				"Where the slices start, relative to the centre.", 30.0, 10.0, 90.0, 1.0, "px"));
		outerRadius = (Setting.Number) module.add(new Setting.Number("outer", "Outer radius",
				"Where the slices end.", 84.0, 40.0, 200.0, 1.0, "px"));
		gapDegrees = (Setting.Number) module.add(new Setting.Number("gap", "Gap between slices",
				"Visual separation between neighbouring slices.", 2.0, 0.0, 10.0, 0.5, "°"));
		showLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Slice labels",
				"Draw the name of every slice inside the ring.", true));
		showIcons = (Setting.Toggle) module.add(new Setting.Toggle("icons", "Slice icons",
				"Draw the configured item icon for every slice.", true));
		showCenterText = (Setting.Toggle) module.add(new Setting.Toggle("center", "Centre text",
				"Show the hovered action and its command in the middle.", true));
		centerOnCursor = (Setting.Toggle) module.add(new Setting.Toggle("center_on_cursor", "Centre on cursor",
				"On: the ring appears where the cursor is. Off: it appears in the middle of the screen.", true));
		deadZone = (Setting.Number) module.add(new Setting.Number("dead_zone", "Dead zone",
				"Pixels around the centre where no slice is selected.", 12.0, 0.0, 60.0, 1.0, "px"));
		highlightColor = (Setting.Color) module.add(new Setting.Color("highlight", "Highlight colour",
				"Colour of the slice under the mouse.", 0xFFFF8A65));
		hoverScale = (Setting.Number) module.add(new Setting.Number("hover_scale", "Hover expansion",
				"How far the hovered slice grows.", 1.12, 1.0, 1.35, 0.01, "x"));
		animationSpeed = (Setting.Number) module.add(new Setting.Number("animation", "Animation speed",
				"How quickly the menu opens and the hover follows.", 14.0, 4.0, 30.0, 1.0, "x"));
		dimBackground = (Setting.Toggle) module.add(new Setting.Toggle("dim", "Dim the world",
				"Darken the screen behind the menu so it reads better.", true));
		dimStrength = (Setting.Number) module.add(new Setting.Number("dim_strength", "Dim strength",
				"Opacity of the dimming layer.", 0.35, 0.0, 0.8, 0.05));
		chatFeedback = (Setting.Toggle) module.add(new Setting.Toggle("feedback", "Chat feedback",
				"Confirm executed actions in your action bar.", true));
	}

	public static boolean isOpen() {
		return open != null;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			return;
		}
		boolean pressed = Keybinds.radialMenu != null && Keybinds.radialMenu.isPressed();
		if (!isEnabled()) {
			if (open != null) {
				open.cancel();
				open = null;
			}
			return;
		}
		if (holdMode.get()) {
			if (pressed && open == null) {
				openMenu(client);
			} else if (!pressed && open != null) {
				// The key was released: run whatever slice the player pointed at.
				RadialMenuScreen screen = open;
				open = null;
				screen.commitSelection();
			}
		} else if (pressed && Keybinds.radialMenu.wasPressed()) {
			if (open == null) {
				openMenu(client);
			} else {
				RadialMenuScreen screen = open;
				open = null;
				screen.commitSelection();
			}
		}
	}

	private static void openMenu(Minecraft client) {
		Screen parent = client.screen;
		RadialMenuScreen screen = new RadialMenuScreen(parent);
		open = screen;
		client.setScreen(screen);
	}

	/** Called by the screen when it closes itself (Escape or a click outside). */
	static void onClosed() {
		open = null;
	}

	public static List<RadialElement> entries() {
		List<RadialElement> result = new ArrayList<>(ChaosConfig.RADIAL_ELEMENTS.size());
		for (RadialElement element : ChaosConfig.RADIAL_ELEMENTS) {
			if (element.enabled) {
				result.add(element);
			}
		}
		return result;
	}

	public static float innerRadius() {
		return innerRadius.getFloat();
	}

	public static float outerRadius() {
		return outerRadius.getFloat();
	}

	public static float gapDegrees() {
		return gapDegrees.getFloat();
	}

	public static boolean showLabels() {
		return showLabels.get();
	}

	public static boolean showIcons() {
		return showIcons.get();
	}

	public static boolean showCenterText() {
		return showCenterText.get();
	}

	public static boolean centersOnCursor() {
		return centerOnCursor.get();
	}

	public static float deadZone() {
		return deadZone.getFloat();
	}

	public static int highlightColor() {
		return highlightColor.get();
	}

	public static float hoverScale() {
		return hoverScale.getFloat();
	}

	public static float animationSpeed() {
		return animationSpeed.getFloat();
	}

	public static boolean dimsBackground() {
		return dimBackground.get();
	}

	public static float dimStrength() {
		return dimStrength.getFloat();
	}

	/** Runs the action of the given element - exactly once, for this one interaction. */
	public static void execute(RadialElement element, Minecraft client) {
		String value = element.command == null ? "" : element.command.trim();
		switch (element.type) {
			case COMMAND -> sendCommand(client, value);
			case CHAT_TEXT -> sendChat(client, value);
			case COPY_TEXT -> {
				dev.chaosutils.core.Clipboard.copyText(value);
				feedback(client, "Copied: " + value);
			}
			case OPEN_SETTINGS -> client.setScreen(new dev.chaosutils.gui.ChaosClickGui());
			case TOGGLE_HUD -> {
				ChaosUtils.toggleOverlays();
				feedback(client, ChaosUtils.overlaysHidden() ? "Overlays hidden" : "Overlays shown");
			}
		}
	}

	private static void sendCommand(Minecraft client, String command) {
		if (command.isEmpty() || client.player == null) {
			return;
		}
		String normalized = command.startsWith("/") ? command.substring(1) : command;
		try {
			client.player.connection.sendCommand(normalized);
			feedback(client, "Sent: /" + normalized);
		} catch (Throwable throwable) {
			feedback(client, "Could not send /" + normalized);
		}
	}

	private static void sendChat(Minecraft client, String text) {
		if (text.isEmpty() || client.player == null) {
			return;
		}
		try {
			if (text.startsWith("/")) {
				client.player.connection.sendCommand(text.substring(1));
				feedback(client, "Sent: " + text);
			} else {
				client.player.connection.sendChat(text);
				feedback(client, "Sent: " + text);
			}
		} catch (Throwable throwable) {
			feedback(client, "Could not send the message");
		}
	}

	private static void feedback(Minecraft client, String text) {
		if (!chatFeedback.get() || client.player == null) {
			return;
		}
		client.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[Radial] §f" + text), true);
	}

	@Override
	public void onDisabled() {
		if (open != null) {
			open.cancel();
			open = null;
		}
	}
}
