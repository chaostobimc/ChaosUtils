package dev.chaosutils.feature.inventory;

import java.util.HashMap;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Render;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Item total counter.
 *
 * <p>Counts how many of an item you carry - optionally including the contents of every
 * shulker box and bundle - and shows the total in the item's tooltip and (optionally) above
 * each hotbar slot. The scan runs on a slow interval and writes into a reused map, so it
 * costs a few microseconds every five ticks and nothing in between.
 */
public final class ItemCounter implements Feature {
	public static final String ID = "item_counter";

	private static Module module;
	private static Setting.Toggle inTooltip;
	private static Setting.Toggle onHotbar;
	private static Setting.Toggle countContainers;
	private static Setting.Toggle typeOnly;
	private static Setting.Toggle colorCoded;
	private static Setting.Number rescanInterval;
	private static Setting.Number scale;

	private static final Map<String, Integer> COUNTS = new HashMap<>(64);
	private static final Map<String, Integer> CONTAINER_COUNTS = new HashMap<>(32);
	private static int tickCounter;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Item Counter",
				"Total count of every item you carry, in the tooltip and on the hotbar.", Category.INVENTORY, false));
		inTooltip = (Setting.Toggle) module.add(new Setting.Toggle("tooltip", "Show in tooltip",
				"Add the total to the item tooltip.", true));
		onHotbar = (Setting.Toggle) module.add(new Setting.Toggle("hotbar", "Show on hotbar",
				"Draw the total above every hotbar slot.", true));
		countContainers = (Setting.Toggle) module.add(new Setting.Toggle("containers", "Include containers",
				"Also count items inside carried shulker boxes and bundles.", true));
		typeOnly = (Setting.Toggle) module.add(new Setting.Toggle("type_only", "Count by type",
				"Ignore enchantments, names and durability when matching.", true));
		colorCoded = (Setting.Toggle) module.add(new Setting.Toggle("colors", "Colour coding",
				"Green below a stack, amber above it, red above a shulker box worth.", true));
		rescanInterval = (Setting.Number) module.add(new Setting.Number("interval", "Rescan interval",
				"Ticks between inventory scans.", 5.0, 1.0, 40.0, 1.0, "t"));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Hotbar text scale",
				"Relative size of the counts on the hotbar.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	/** Wires the tooltip event; called once during client initialisation. */
	public static void initEvents() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (!ModuleManager.enabled(ID) || !inTooltip.get() || stack.isEmpty()) {
				return;
			}
			int count = countOf(stack);
			if (count <= stack.getCount()) {
				return;
			}
			MutableComponent line = Component.literal("In inventory: ").withStyle(ChatFormatting.GRAY);
			line.append(Component.literal(Integer.toString(count)).withStyle(ChatFormatting.AQUA));
			if (countContainers.get()) {
				int inside = CONTAINER_COUNTS.getOrDefault(key(stack), 0);
				if (inside > 0) {
					line.append(Component.literal("  (+" + inside + " in containers)").withStyle(ChatFormatting.DARK_GRAY));
				}
			}
			lines.add(line);
		});
	}

	private static String key(ItemStack stack) {
		if (typeOnly.get()) {
			return dev.chaosutils.util.ItemLookup.idOf(stack);
		}
		return System.identityHashCode(stack.getItem()) + "|" + stack.getComponents().toString();
	}

	private static int countOf(ItemStack stack) {
		return COUNTS.getOrDefault(key(stack), 0) + CONTAINER_COUNTS.getOrDefault(key(stack), 0);
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			COUNTS.clear();
			CONTAINER_COUNTS.clear();
			return;
		}
		if (--tickCounter > 0) {
			return;
		}
		tickCounter = Math.max(1, rescanInterval.getInt());
		scan(player);
	}

	private static void scan(Player player) {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
		Inventory inventory = player.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			COUNTS.merge(key(stack), stack.getCount(), Integer::sum);
			if (countContainers.get()) {
				scanContainer(stack, 0);
			}
		}
	}

	private static void scanContainer(ItemStack stack, int depth) {
		if (depth > 2) {
			return;
		}
		try {
			ItemContainerContents container = stack.get(DataComponents.CONTAINER);
			if (container != null) {
				for (ItemStack inner : container.nonEmptyItems()) {
					if (inner.isEmpty()) {
						continue;
					}
					CONTAINER_COUNTS.merge(key(inner), inner.getCount(), Integer::sum);
					scanContainer(inner, depth + 1);
				}
			}
		} catch (Throwable ignored) {
			// not a container item
		}
		try {
			BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundle != null) {
				for (ItemStack inner : bundle.items()) {
					if (inner.isEmpty()) {
						continue;
					}
					CONTAINER_COUNTS.merge(key(inner), inner.getCount(), Integer::sum);
					scanContainer(inner, depth + 1);
				}
			}
		} catch (Throwable ignored) {
			// not a bundle
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || !onHotbar.get() || !dev.chaosutils.feature.hud.HudPanel.visibleNow()) {
			return;
		}
		if (client.screen != null) {
			return;
		}
		Font font = client.font;
		float scaleFactor = dev.chaosutils.feature.hud.HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		int hotbarLeft = graphics.guiWidth() / 2 - 91;
		int hotbarTop = graphics.guiHeight() - 22;
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			int total = countOf(stack);
			if (total <= stack.getCount()) {
				continue;
			}
			String text = Integer.toString(total);
			float x = hotbarLeft + 3 + slot * 20 + 8.0F;
			float y = hotbarTop - 9.0F * scaleFactor;
			int color = colorCoded.get() ? totalColor(total) : 0xFFF2F2F7;
			Render.text(graphics, font, text, x - font.width(text) * 0.5F, y, color, true);
		}
	}

	private static int totalColor(int total) {
		if (total >= 1728) {
			return 0xFFE05B5B;
		}
		if (total >= 64) {
			return 0xFFF0B429;
		}
		return 0xFF63D471;
	}

	@Override
	public void onWorldLeave() {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
	}

	@Override
	public void onDisabled() {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
	}
}
