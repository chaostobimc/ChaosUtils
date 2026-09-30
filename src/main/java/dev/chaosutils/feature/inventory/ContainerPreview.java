package dev.chaosutils.feature.inventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Shulker box and bundle content preview.
 *
 * <p>Adds the contents of a container item to its tooltip - grouped, counted and with the
 * fill level. The data comes from the item's own components, which the client already has,
 * so this is a pure display feature with zero server interaction. It also works for items
 * lying in a chest, without opening anything.
 */
public final class ContainerPreview implements Feature {
	public static final String ID = "container_preview";

	/** Vanilla container size of a shulker box. */
	private static final int SHULKER_SLOTS = 27;
	/** A bundle holds 64 weight in total; a full stack always weighs 64. */
	private static final int BUNDLE_CAPACITY = 64;

	private static Module module;
	private static Setting.Toggle shulkers;
	private static Setting.Toggle bundles;
	private static Setting.Toggle groupItems;
	private static Setting.Toggle showFillLevel;
	private static Setting.Toggle showFreeSlots;
	private static Setting.Number maxLines;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Container Preview",
				"See what is inside shulker boxes and bundles without opening them.", Category.INVENTORY, true));
		shulkers = (Setting.Toggle) module.add(new Setting.Toggle("shulkers", "Shulker boxes",
				"List the contents of shulker boxes and other container items.", true));
		bundles = (Setting.Toggle) module.add(new Setting.Toggle("bundles", "Bundles",
				"List the contents of bundles.", true));
		groupItems = (Setting.Toggle) module.add(new Setting.Toggle("group", "Group identical items",
				"Show 12× Diamond instead of twelve lines.", true));
		showFillLevel = (Setting.Toggle) module.add(new Setting.Toggle("fill", "Show fill level",
				"Occupied slots (or bundle weight) in the header line.", true));
		showFreeSlots = (Setting.Toggle) module.add(new Setting.Toggle("free", "Show free space",
				"How many slots (or how much weight) are still free.", false));
		maxLines = (Setting.Number) module.add(new Setting.Number("lines", "Maximum lines",
				"Cap the preview so long tooltips stay readable.", 9.0, 3.0, 27.0, 1.0, " lines"));
	}

	/** Wires the tooltip event; called once during client initialisation. */
	public static void initEvents() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (!ModuleManager.enabled(ID)) {
				return;
			}
			try {
				appendPreview(stack, lines);
			} catch (Throwable ignored) {
				// a broken preview must never break the tooltip
			}
		});
	}

	private static void appendPreview(ItemStack stack, List<Component> lines) {
		try {
			ItemContainerContents container = stack.get(DataComponents.CONTAINER);
			if (container != null && shulkers.get()) {
				Map<String, Entry> grouped = new LinkedHashMap<>();
				for (ItemStack inner : container.nonEmptyItems()) {
					addEntry(grouped, inner);
				}
				if (!grouped.isEmpty()) {
					appendPreviewLines(lines, grouped, grouped.size(), SHULKER_SLOTS, -1, "Shulker");
					return;
				}
			}
		} catch (Throwable ignored) {
			// container component unavailable
		}
		try {
			BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundle != null && bundles.get()) {
				Map<String, Entry> grouped = new LinkedHashMap<>();
				int weight = 0;
				for (ItemStack inner : bundle.items()) {
					if (inner.isEmpty()) {
						continue;
					}
					addEntry(grouped, inner);
					weight += bundleWeight(inner);
				}
				if (!grouped.isEmpty()) {
					appendPreviewLines(lines, grouped, weight, BUNDLE_CAPACITY, weight, "Bundle");
				}
			}
		} catch (Throwable ignored) {
			// bundle component unavailable
		}
	}

	/** Vanilla bundle weight: a full stack weighs 64, so one item weighs 64 / maxStackSize. */
	private static int bundleWeight(ItemStack stack) {
		int maxStackSize = Math.max(1, stack.getMaxStackSize());
		return Math.max(1, stack.getCount() * 64 / maxStackSize);
	}

	private static void addEntry(Map<String, Entry> grouped, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		String key = stack.getHoverName().getString();
		Entry existing = grouped.get(key);
		if (existing == null) {
			grouped.put(key, new Entry(key, stack.getCount()));
		} else {
			grouped.put(key, new Entry(key, existing.count() + stack.getCount()));
		}
	}

	private record Entry(String name, int count) {
	}

	private static void appendPreviewLines(List<Component> lines, Map<String, Entry> grouped, int used,
			int capacity, int weight, String label) {
		int linesLimit = maxLines.getInt();
		if (showFillLevel.get()) {
			MutableComponent header = Component.literal(label + " ").withStyle(ChatFormatting.GRAY);
			if (weight >= 0) {
				header.append(Component.literal(weight + " / " + capacity).withStyle(
						weight >= capacity ? ChatFormatting.RED : ChatFormatting.AQUA));
			} else {
				header.append(Component.literal(used + " / " + capacity).withStyle(ChatFormatting.AQUA));
			}
			lines.add(header);
		}
		int index = 0;
		int shown = 0;
		for (Entry entry : grouped.values()) {
			if (index >= linesLimit) {
				lines.add(Component.literal("  … and " + (grouped.size() - index) + " more")
						.withStyle(ChatFormatting.DARK_GRAY));
				break;
			}
			shown += entry.count();
			MutableComponent line = Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY);
			line.append(Component.literal(entry.name()).withStyle(ChatFormatting.GRAY));
			if (groupItems.get() && entry.count() > 1) {
				line.append(Component.literal(" ×" + entry.count()).withStyle(ChatFormatting.WHITE));
			}
			lines.add(line);
			index++;
		}
		if (showFreeSlots.get()) {
			if (weight >= 0) {
				lines.add(Component.literal("  " + Math.max(0, capacity - weight) + " weight free")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else {
				lines.add(Component.literal("  " + Math.max(0, capacity - grouped.size()) + " free slots")
						.withStyle(ChatFormatting.DARK_GRAY));
			}
		}
		if (!groupItems.get()) {
			lines.add(Component.literal("  " + shown + " items").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
