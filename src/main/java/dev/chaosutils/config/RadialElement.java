package dev.chaosutils.config;

import dev.chaosutils.util.ItemLookup;
import net.minecraft.world.item.ItemStack;

/**
 * One slice of the radial menu.
 *
 * <p>Every action type is a deliberate, single, user triggered action - ChaosUtils never
 * repeats, schedules or automates anything.
 */
public final class RadialElement {
	public enum ActionType {
		/** Sends a command exactly as if the player typed it (server rules apply). */
		COMMAND("Send command"),
		/** Sends plain chat text. */
		CHAT_TEXT("Send chat"),
		/** Copies the text to the system clipboard (no network involved). */
		COPY_TEXT("Copy to clipboard"),
		/** Opens the ChaosUtils click GUI. */
		OPEN_SETTINGS("Open ChaosUtils"),
		/** Toggles every ChaosUtils overlay off/on (panic key). */
		TOGGLE_HUD("Toggle all overlays");

		private final String label;

		ActionType(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	public String name = "New Action";
	public String command = "/spawn";
	public int color = 0xFF7C5CFF;
	public String icon = "minecraft:compass";
	public boolean enabled = true;
	public ActionType type = ActionType.COMMAND;

	public RadialElement() {
	}

	public RadialElement(String name, String command, int color, String icon, ActionType type) {
		this.name = name;
		this.command = command;
		this.color = color;
		this.icon = icon;
		this.type = type;
	}

	/** Colour with a guaranteed opaque alpha, safe for fills and borders. */
	public int solidColor() {
		return color | 0xFF000000;
	}

	/** Colour with the given alpha, keeping the stored hue. */
	public int colorWithAlpha(float alpha) {
		return dev.chaosutils.util.Render.alpha(color | 0xFF000000, alpha);
	}

	public ItemStack iconStack() {
		return ItemLookup.stack(icon);
	}

	public RadialElement copy() {
		RadialElement copy = new RadialElement();
		copy.name = name;
		copy.command = command;
		copy.color = color;
		copy.icon = icon;
		copy.enabled = enabled;
		copy.type = type;
		return copy;
	}
}
