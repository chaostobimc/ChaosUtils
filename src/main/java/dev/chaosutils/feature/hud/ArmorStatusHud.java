package dev.chaosutils.feature.hud;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.EnchantLookup;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;

/**
 * Armour and held item status HUD.
 *
 * <p>Shows every equipped piece with its durability and the enchantments in short form
 * ("Prot IV", "Unbr III"), plus the two hands. It is freely positionable and can be laid out
 * horizontally or vertically, which makes it a natural companion to the vanilla hotbar.
 */
public final class ArmorStatusHud implements Feature {
	public static final String ID = "armor_hud";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Toggle vertical;
	private static Setting.Toggle showArmor;
	private static Setting.Toggle showHands;
	private static Setting.Toggle showDurabilityBar;
	private static Setting.Toggle showDurabilityNumbers;
	private static Setting.Toggle showEnchants;
	private static Setting.Toggle showEmptySlots;
	private static Setting.Toggle maxEnchants;
	private static Setting.Number scale;
	private static Setting.Number spacing;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Armour & Hand Status",
				"Equipped armour, both hands, durability and enchantments at a glance.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the equipment panel sits.", HudPos.BOTTOM_LEFT.x(), HudPos.BOTTOM_LEFT.y()));
		vertical = (Setting.Toggle) module.add(new Setting.Toggle("vertical", "Vertical layout",
				"Stack the slots vertically instead of in a row.", false));
		showArmor = (Setting.Toggle) module.add(new Setting.Toggle("armor", "Show armour",
				"Helmet, chestplate, leggings and boots.", true));
		showHands = (Setting.Toggle) module.add(new Setting.Toggle("hands", "Show hands",
				"Main hand and off hand.", true));
		showDurabilityBar = (Setting.Toggle) module.add(new Setting.Toggle("bars", "Durability bar",
				"Thin bar under each item.", true));
		showDurabilityNumbers = (Setting.Toggle) module.add(new Setting.Toggle("numbers", "Durability numbers",
				"Remaining uses, e.g. 231.", false));
		showEnchants = (Setting.Toggle) module.add(new Setting.Toggle("enchantments", "Enchantment names",
				"Short names of every enchantment on the item.", true));
		showEmptySlots = (Setting.Toggle) module.add(new Setting.Toggle("empty", "Show empty slots",
				"Draw a placeholder frame for empty equipment slots.", false));
		maxEnchants = (Setting.Toggle) module.add(new Setting.Toggle("compact_enchants", "Compact enchantments",
				"Only the first two enchantments per item.", true));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the panel.", 1.0, 0.6, 1.6, 0.05, "x"));
		spacing = (Setting.Number) module.add(new Setting.Number("spacing", "Slot spacing",
				"Space between the slots.", 20.0, 16.0, 40.0, 1.0, "px"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || !HudPanel.visibleNow()) {
			return;
		}
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		float slotSpacing = spacing.getFloat() * scaleFactor;
		List<ItemStack> stacks = new ArrayList<>(6);
		if (showArmor.get()) {
			stacks.add(player.getItemBySlot(EquipmentSlot.HEAD));
			stacks.add(player.getItemBySlot(EquipmentSlot.CHEST));
			stacks.add(player.getItemBySlot(EquipmentSlot.LEGS));
			stacks.add(player.getItemBySlot(EquipmentSlot.FEET));
		}
		if (showHands.get()) {
			stacks.add(player.getMainHandItem());
			stacks.add(player.getOffhandItem());
		}
		if (stacks.isEmpty()) {
			return;
		}
		Font font = client.font;
		float padding = HudPanel.padding();
		float iconSize = 16.0F * scaleFactor;
		float labelHeight = showEnchants.get() ? 18.0F * scaleFactor : 9.0F * scaleFactor;
		float boxWidth;
		float boxHeight;
		if (vertical.get()) {
			boxWidth = iconSize + 6.0F + 54.0F * scaleFactor + padding * 2.0F;
			boxHeight = stacks.size() * Math.max(slotSpacing, labelHeight) + padding * 2.0F;
		} else {
			boxWidth = stacks.size() * slotSpacing + padding * 2.0F;
			boxHeight = Math.max(iconSize, labelHeight) + padding * 2.0F;
		}
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF64B5F6);

		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			float slotX = vertical.get() ? x + padding : x + padding + i * slotSpacing;
			float slotY = vertical.get() ? y + padding + i * Math.max(slotSpacing, labelHeight) : y + padding;
			if (stack.isEmpty()) {
				if (showEmptySlots.get()) {
					Render.roundedBorder(graphics, slotX, slotY, iconSize, iconSize, 2.0F, 1.0F, 0x30FFFFFF, 0x00000000);
				}
				continue;
			}
			Render.item(graphics, stack, slotX, slotY);
			float textX = vertical.get() ? slotX + iconSize + 4.0F : slotX;
			float textY = vertical.get() ? slotY + 1.0F : y + padding + iconSize - 2.0F;
			String line = enchantLine(stack);
			if (!vertical.get() && showEnchants.get() && !line.isEmpty()) {
				HudPanel.text(graphics, font, line, textX, y + padding + iconSize + 1.0F, 0xFFBFC2CF);
			} else if (vertical.get() && showEnchants.get() && !line.isEmpty()) {
				HudPanel.text(graphics, font, line, textX, textY + 10.0F, 0xFFBFC2CF);
			}
			if (showDurabilityBar.get() && stack.isDamageableItem()) {
				float fraction = remainingFraction(stack);
				float barY = slotY + iconSize - 1.0F;
				float barWidth = iconSize;
				Render.rect(graphics, slotX, barY, barWidth, 2.0F * scaleFactor, 0x88000000);
				Render.rect(graphics, slotX, barY, barWidth * fraction, 2.0F * scaleFactor, HudPanel.dangerColor(fraction));
				if (showDurabilityNumbers.get()) {
					String remaining = Integer.toString(Math.max(0, stack.getMaxDamage() - stack.getDamageValue()));
					HudPanel.text(graphics, font, remaining, slotX + iconSize + 3.0F, slotY + 1.0F, 0xFFBFC2CF);
				}
			}
		}
	}

	private static String enchantLine(ItemStack stack) {
		if (!showEnchants.get()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(24);
		int shown = 0;
		int limit = maxEnchants.get() ? 2 : Integer.MAX_VALUE;
		try {
			var enchantments = stack.getEnchantments();
			for (Holder<Enchantment> enchantment : enchantments.keySet()) {
				if (shown >= limit) {
					builder.append("…");
					break;
				}
				int level = enchantments.getLevel(enchantment);
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(EnchantLookup.shortName(enchantment)).append(' ').append(EnchantLookup.level(level));
				shown++;
			}
		} catch (Throwable ignored) {
			return "";
		}
		return builder.toString();
	}

	private static float remainingFraction(ItemStack stack) {
		int max = Math.max(1, stack.getMaxDamage());
		int remaining = max - stack.getDamageValue();
		return Math.max(0.0F, Math.min(1.0F, remaining / (float) max));
	}
}
