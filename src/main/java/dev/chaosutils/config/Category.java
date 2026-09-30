package dev.chaosutils.config;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Sidebar groups of the click GUI. Colours are ARGB and are reused for accents. */
public enum Category {
	HUD("HUD & Overlays", 0xFF4FC3F7, () -> new ItemStack(Items.CLOCK)),
	VISUAL("Visuals", 0xFFB388FF, () -> new ItemStack(Items.ENDER_EYE)),
	RADIAL("Radial Menu", 0xFFFF8A65, () -> new ItemStack(Items.COMPASS)),
	CHAT("Chat & Social", 0xFF81C784, () -> new ItemStack(Items.PAPER)),
	INVENTORY("Inventory", 0xFFFFD54F, () -> new ItemStack(Items.CHEST)),
	AUDIO("Audio", 0xFFF06292, () -> new ItemStack(Items.NOTE_BLOCK)),
	QOL("Quality of Life", 0xFF64B5F6, () -> new ItemStack(Items.FEATHER)),
	PERFORMANCE("Performance", 0xFF4DB6AC, () -> new ItemStack(Items.REDSTONE));

	private final String displayName;
	private final int color;
	private final Supplier<ItemStack> icon;

	Category(String displayName, int color, Supplier<ItemStack> icon) {
		this.displayName = displayName;
		this.color = color;
		this.icon = icon;
	}

	public String displayName() {
		return displayName;
	}

	public int color() {
		return color;
	}

	public ItemStack icon() {
		return icon.get();
	}
}
