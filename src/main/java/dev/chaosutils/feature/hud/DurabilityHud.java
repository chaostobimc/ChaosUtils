package dev.chaosutils.feature.hud;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Item durability status &amp; break warning.
 *
 * <p>Your inventory is scanned on a slow interval (ten ticks by default), not every frame,
 * and only items below the warning threshold are kept in an already allocated list - so the
 * overlay costs nothing while everything is intact. When something is about to break the
 * panel appears, the icon flashes and (optionally) a soft click sound plays once.
 */
public final class DurabilityHud implements Feature {
	public static final String ID = "durability";

	private static final int MAX_ROWS = 6;

	private static Module module;
	private static Setting.Position position;
	private static Setting.Number warnPercent;
	private static Setting.Number warnAbsolute;
	private static Setting.Toggle includeArmor;
	private static Setting.Toggle includeTools;
	private static Setting.Toggle includeEverything;
	private static Setting.Toggle showIcons;
	private static Setting.Toggle showNumbers;
	private static Setting.Toggle warnSound;
	private static Setting.Number rescanInterval;
	private static Setting.Number scale;

	private static final List<Row> ROWS = new ArrayList<>(MAX_ROWS);
	private static int tickCounter;
	private static long lastSoundAt;

	private record Row(ItemStack stack, int remaining, int max) {
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Durability Warning",
				"Warns before a tool, weapon or armour piece breaks.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the warning panel appears.", HudPos.BOTTOM_RIGHT.x(), HudPos.BOTTOM_RIGHT.y()));
		warnPercent = (Setting.Number) module.add(new Setting.Number("percent", "Warn below",
				"Warning threshold as a percentage of the maximal durability.", 5.0, 1.0, 50.0, 1.0, "%"));
		warnAbsolute = (Setting.Number) module.add(new Setting.Number("absolute", "Also warn below",
				"Absolute number of remaining uses.", 12.0, 0.0, 100.0, 1.0, "uses"));
		includeArmor = (Setting.Toggle) module.add(new Setting.Toggle("armor", "Include armour",
				"Watch the four armour slots.", true));
		includeTools = (Setting.Toggle) module.add(new Setting.Toggle("tools", "Include tools & weapons",
				"Anything with a digging or attack speed.", true));
		includeEverything = (Setting.Toggle) module.add(new Setting.Toggle("everything", "Include everything",
				"Also watch rods, shears, elytra and every other damageable item.", true));
		showIcons = (Setting.Toggle) module.add(new Setting.Toggle("icons", "Show icons",
				"Draw the item icon for every warning.", true));
		showNumbers = (Setting.Toggle) module.add(new Setting.Toggle("numbers", "Show numbers",
				"Remaining uses in brackets.", true));
		warnSound = (Setting.Toggle) module.add(new Setting.Toggle("sound", "Warning sound",
				"A soft click when an item first drops below the threshold.", true));
		rescanInterval = (Setting.Number) module.add(new Setting.Number("interval", "Rescan interval",
				"Ticks between inventory scans (higher = cheaper).", 10.0, 2.0, 40.0, 1.0, "t"));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the warning panel.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			ROWS.clear();
			return;
		}
		if (--tickCounter > 0) {
			return;
		}
		tickCounter = Math.max(1, rescanInterval.getInt());
		scan(client.player);
	}

	private static void scan(Player player) {
		ROWS.clear();
		float percentLimit = warnPercent.getFloat() / 100.0F;
		int absoluteLimit = warnAbsolute.getInt();
		Inventory inventory = player.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size && ROWS.size() < MAX_ROWS; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty() || !stack.isDamageableItem()) {
				continue;
			}
			int max = stack.getMaxDamage();
			int remaining = max - stack.getDamageValue();
			if (remaining <= 0 || remaining > max) {
				continue;
			}
			boolean belowPercent = remaining <= Math.max(1, (int) (max * percentLimit));
			boolean belowAbsolute = absoluteLimit > 0 && remaining <= absoluteLimit;
			if (!belowPercent && !belowAbsolute) {
				continue;
			}
			if (!isWatched(stack, i)) {
				continue;
			}
			ROWS.add(new Row(stack, remaining, max));
		}
	}

	private static boolean isWatched(ItemStack stack, int slot) {
		boolean armorSlot = slot >= 36 && slot <= 39;
		if (armorSlot) {
			return includeArmor.get();
		}
		if (includeEverything.get()) {
			return true;
		}
		if (includeTools.get()) {
			return stack.isDamageableItem();
		}
		return false;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !HudPanel.visibleNow() || ROWS.isEmpty()) {
			return;
		}
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		Font font = client.font;
		float padding = HudPanel.padding() * scaleFactor;
		float rowHeight = 12.0F * scaleFactor;
		float iconSize = 16.0F * scaleFactor;
		float width = 76.0F * scaleFactor;
		for (Row row : ROWS) {
			width = Math.max(width, font.width(label(row)) * scaleFactor + iconSize + padding * 3.0F);
		}
		float boxWidth = width + padding;
		float boxHeight = ROWS.size() * rowHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));
		boolean critical = ROWS.get(0).remaining() <= 3;
		int accent = critical ? 0xFFE05B5B : 0xFFF0B429;
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, accent);

		float cursorY = y + padding;
		for (Row row : ROWS) {
			float fraction = row.remaining() / (float) Math.max(1, row.max());
			if (showIcons.get()) {
				Render.item(graphics, row.stack(), x + padding, cursorY - 2.0F);
			}
			float textX = showIcons.get() ? x + padding + iconSize : x + padding;
			HudPanel.text(graphics, font, label(row), textX, cursorY, 0xFFF2F2F7);
			HudPanel.bar(graphics, textX, cursorY + 9.0F * scaleFactor, 48.0F * scaleFactor, 2.0F * scaleFactor,
					fraction, HudPanel.dangerColor(fraction));
			cursorY += rowHeight;
		}
		if (warnSound.get() && critical && System.currentTimeMillis() - lastSoundAt > 15_000L) {
			lastSoundAt = System.currentTimeMillis();
			try {
				var sound = SoundLookup.ui(SoundLookup.uiClick(), 0.6F, 0.4F);
				if (sound != null) {
					client.getSoundManager().play(sound);
				}
			} catch (Throwable ignored) {
				// sound is optional
			}
		}
	}

	private static String label(Row row) {
		String name = row.stack().getHoverName().getString();
		if (name.length() > 18) {
			name = name.substring(0, 17) + "…";
		}
		return showNumbers.get() ? name + " (" + row.remaining() + ")" : name;
	}

	@Override
	public void onWorldLeave() {
		ROWS.clear();
	}

	@Override
	public void onDisabled() {
		ROWS.clear();
	}
}
