package dev.chaosutils.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Id based item lookup with a lazily built cache.
 *
 * <p>The cache is built by iterating the item registry once, which avoids relying on
 * registry lookup methods whose names changed several times during 1.21.x.
 */
public final class ItemLookup {
	private static final List<Item> ALL_ITEMS = new ArrayList<>();
	private static final Map<String, Item> BY_KEY = new LinkedHashMap<>();
	private static final Map<String, ItemStack> STACK_CACHE = new LinkedHashMap<>();
	private static boolean built;

	private ItemLookup() {
	}

	private static void build() {
		if (built) {
			return;
		}
		built = true;
		for (Item item : BuiltInRegistries.ITEM) {
			ALL_ITEMS.add(item);
			try {
				Identifier id = BuiltInRegistries.ITEM.getKey(item);
				BY_KEY.put(id.toString(), item);
				BY_KEY.put(id.getPath(), item);
			} catch (Throwable ignored) {
				// Registry not ready yet - the cache will simply be smaller.
			}
		}
	}

	public static ItemStack stack(String id) {
		if (id == null || id.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack cached = STACK_CACHE.get(id);
		if (cached != null) {
			return cached;
		}
		Item item = item(id);
		ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
		STACK_CACHE.put(id, stack);
		return stack;
	}

	public static Item item(String id) {
		build();
		String key = id.trim().toLowerCase(Locale.ROOT);
		Item item = BY_KEY.get(key);
		if (item != null) {
			return item;
		}
		return BY_KEY.get(key.startsWith("minecraft:") ? key : "minecraft:" + key);
	}

	public static String idOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return "";
		}
		try {
			return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		} catch (Throwable ignored) {
			return "";
		}
	}

	/** Curated list used by the icon picker in the GUI so it stays usable. */
	public static List<Item> iconCandidates() {
		build();
		List<Item> curated = new ArrayList<>();
		String[] ids = {
				"minecraft:compass", "minecraft:clock", "minecraft:respawn_anchor", "minecraft:red_bed",
				"minecraft:ender_pearl", "minecraft:end_portal_frame", "minecraft:player_head", "minecraft:crafting_table",
				"minecraft:barrier", "minecraft:map", "minecraft:filled_map", "minecraft:spyglass",
				"minecraft:diamond_sword", "minecraft:netherite_sword", "minecraft:bow", "minecraft:crossbow",
				"minecraft:trident", "minecraft:shield", "minecraft:golden_apple", "minecraft:cake",
				"minecraft:potion", "minecraft:experience_bottle", "minecraft:emerald", "minecraft:diamond",
				"minecraft:netherite_ingot", "minecraft:gold_ingot", "minecraft:iron_ingot", "minecraft:redstone",
				"minecraft:torch", "minecraft:lantern", "minecraft:campfire", "minecraft:firework_rocket",
				"minecraft:elytra", "minecraft:leather_boots", "minecraft:chest", "minecraft:shulker_box",
				"minecraft:hopper", "minecraft:anvil", "minecraft:enchanting_table", "minecraft:beacon",
				"minecraft:bell", "minecraft:music_disc_cat", "minecraft:note_block", "minecraft:skull_banner_pattern",
				"minecraft:white_dye", "minecraft:lime_dye", "minecraft:cyan_dye", "minecraft:magenta_dye",
				"minecraft:heart_of_the_sea", "minecraft:nautilus_shell", "minecraft:totem_of_undying", "minecraft:nether_star"
		};
		for (String id : ids) {
			Item item = item(id);
			if (item != null && item != Items.AIR) {
				curated.add(item);
			}
		}
		return Collections.unmodifiableList(curated);
	}

	public static List<Item> allItems() {
		build();
		return Collections.unmodifiableList(ALL_ITEMS);
	}

	public static void invalidate() {
		built = false;
		ALL_ITEMS.clear();
		BY_KEY.clear();
		STACK_CACHE.clear();
	}
}
