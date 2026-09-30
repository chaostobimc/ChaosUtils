package dev.chaosutils.feature.inventory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.mixin.AbstractContainerScreenAccessor;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.EnchantLookup;
import dev.chaosutils.util.Render;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Smart container search.
 *
 * <p>Adds a search field to every container screen: matching slots are outlined in the accent
 * colour and everything else is dimmed, so a full double chest becomes readable in a second.
 * The search runs purely on the client against the item data the server already sent.
 *
 * <p>The field is a vanilla text box (borderless, so ChaosUtils can style it) added through
 * Fabric's screen API, which means typing behaves exactly like vanilla and rebinding keys is
 * never an issue.
 */
public final class ContainerSearch implements Feature {
	public static final String ID = "container_search";

	private static Module module;
	private static Setting.Toggle autoFocus;
	private static Setting.Toggle showField;
	private static Setting.Toggle dimNonMatches;
	private static Setting.Number dimStrength;
	private static Setting.Color highlightColor;
	private static Setting.Toggle pulse;
	private static Setting.Choice matchMode;
	private static Setting.Toggle highlightEmpty;
	private static Setting.Number fieldWidth;

	private static EditBox field;
	private static final WeakReference<AbstractContainerScreen<?>> NONE = new WeakReference<>(null);
	private static WeakReference<AbstractContainerScreen<?>> owner = NONE;
	private static String query = "";

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Container Search",
				"Search any container: matches are highlighted, everything else is dimmed.", Category.INVENTORY, true));
		autoFocus = (Setting.Toggle) module.add(new Setting.Toggle("focus", "Focus on open",
				"Start typing right away when the container opens.", false));
		showField = (Setting.Toggle) module.add(new Setting.Toggle("field", "Show search field",
				"Draw the styled search box under the container.", true));
		dimNonMatches = (Setting.Toggle) module.add(new Setting.Toggle("dim", "Dim non-matches",
				"Darken every slot that does not match.", true));
		dimStrength = (Setting.Number) module.add(new Setting.Number("dim_strength", "Dim strength",
				"Opacity of the dimming layer.", 0.55, 0.1, 0.9, 0.05));
		highlightColor = (Setting.Color) module.add(new Setting.Color("highlight", "Highlight colour",
				"Outline colour for matching slots.", 0xFF7C5CFF));
		pulse = (Setting.Toggle) module.add(new Setting.Toggle("pulse", "Pulse",
				"Softly pulse the highlight so matches are easy to spot.", true));
		matchMode = (Setting.Choice) module.add(new Setting.Choice("match", "Match against",
				"What the query is compared with.", 0, "Item name", "Name + item id", "Name + enchantments"));
		highlightEmpty = (Setting.Toggle) module.add(new Setting.Toggle("empty", "Dim empty slots",
				"Also dim slots that hold nothing.", false));
		fieldWidth = (Setting.Number) module.add(new Setting.Number("width", "Field width",
				"Width of the search box.", 120.0, 60.0, 240.0, 5.0, "px"));
	}

	/** Wires the screen events; called once during client initialisation. */
	public static void initEvents() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof AbstractContainerScreen<?> container)) {
				return;
			}
			if (ModuleManager.enabled(ID)) {
				attach(client, container, width, height);
			}
		});
	}

	private static void attach(Minecraft client, AbstractContainerScreen<?> container, int width, int height) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) container;
		int boxWidth = fieldWidth.getInt();
		int boxHeight = 14;
		int x = accessor.chaosutils$leftPos() + (accessor.chaosutils$imageWidth() - boxWidth) / 2;
		int y = accessor.chaosutils$topPos() + accessor.chaosutils$imageHeight() + 4;
		EditBox box = new EditBox(client.font, x, y, boxWidth, boxHeight, Component.literal("Search"));
		box.setBordered(false);
		box.setTextColor(0xFFF2F2F7);
		box.setMaxLength(64);
		box.setValue(query);
		box.setResponder(value -> query = value == null ? "" : value);
		Screens.getButtons(container).add(box);
		field = box;
		owner = new WeakReference<>(container);
		if (autoFocus.get()) {
			container.setFocused(box);
			box.setFocused(true);
		}
	}

	private static void detach() {
		field = null;
		owner = NONE;
		query = "";
	}

	/** Repositions the field when the window is resized and keeps the query alive. */
	@Override
	public void onTick(Minecraft client) {
		if (client.screen instanceof AbstractContainerScreen<?> container) {
			if (field == null && ModuleManager.enabled(ID)) {
				attach(client, container, container.width, container.height);
			}
			if (field != null && Keybinds.searchContainer != null && Keybinds.searchContainer.wasPressed()) {
				container.setFocused(field);
				field.setFocused(true);
				field.setValue("");
			}
		} else if (field != null) {
			detach();
		}
	}

	/** Called from the container screen mixin after vanilla drew everything. */
	public static void renderOverlay(AbstractContainerScreen<?> container, GuiGraphics graphics, int mouseX, int mouseY) {
		if (!ModuleManager.enabled(ID)) {
			return;
		}
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) container;
		int left = accessor.chaosutils$leftPos();
		int top = accessor.chaosutils$topPos();
		int imageWidth = accessor.chaosutils$imageWidth();
		int imageHeight = accessor.chaosutils$imageHeight();
		Font font = Minecraft.getInstance().font;
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

		if (showField.get()) {
			int boxWidth = fieldWidth.getInt();
			int x = left + (imageWidth - boxWidth) / 2;
			int y = top + imageHeight + 4;
			Render.roundedRect(graphics, x - 2.0F, y - 3.0F, boxWidth + 4.0F, 16.0F, 3.0F, 0xC0101018);
			Render.roundedBorder(graphics, x - 2.0F, y - 3.0F, boxWidth + 4.0F, 16.0F, 3.0F, 1.0F,
					Render.alpha(highlightColor.get(), 0.55F), 0xC0101018);
			if (field != null && field.getValue().isEmpty() && !field.isFocused()) {
				Render.text(graphics, font, "Search…", x, y + 1.0F, 0xFF6C6C80, false);
			}
		}
		if (needle.isEmpty()) {
			return;
		}

		float pulse = pulse.get()
				? 0.65F + 0.35F * (float) Math.sin(System.nanoTime() / 400_000_000.0)
				: 1.0F;
		int highlight = Render.alpha(highlightColor.get(), Anim.clamp01(pulse));
		int dim = (int) (Anim.clamp01(dimStrength.getFloat()) * 255.0F) << 24;

		for (Slot slot : container.getMenu().slots) {
			int slotX = left + slot.x;
			int slotY = top + slot.y;
			if (slotX < left - 1 || slotX > left + imageWidth || slotY < top - 1 || slotY > top + imageHeight) {
				continue;
			}
			ItemStack stack = slot.getItem();
			boolean empty = stack.isEmpty();
			boolean matches = matches(stack, needle);
			if (matches) {
				Render.roundedRect(graphics, slotX - 1.0F, slotY - 1.0F, 18.0F, 18.0F, 3.0F,
						Render.alpha(highlightColor.get(), 0.25F * pulse));
				Render.roundedBorder(graphics, slotX - 1.0F, slotY - 1.0F, 18.0F, 18.0F, 3.0F, 1.0F, highlight, 0x00000000);
			} else if (dimNonMatches.get() && (!empty || highlightEmpty.get())) {
				Render.rect(graphics, slotX, slotY, 16.0F, 16.0F, dim);
			}
		}
	}

	private static boolean matches(ItemStack stack, String needle) {
		if (stack.isEmpty()) {
			return false;
		}
		String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
		if (name.contains(needle)) {
			return true;
		}
		int mode = matchMode.get();
		if (mode >= 1) {
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
			if (id.contains(needle)) {
				return true;
			}
		}
		if (mode >= 2) {
			try {
				for (net.minecraft.core.Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
					if (EnchantLookup.shortName(enchantment).toLowerCase(Locale.ROOT).contains(needle)) {
						return true;
					}
					String path = enchantment.unwrapKey().map(key -> key.location().getPath()).orElse("");
					if (path.toLowerCase(Locale.ROOT).contains(needle)) {
						return true;
					}
				}
			} catch (Throwable ignored) {
				// no enchantment data available
			}
		}
		return false;
	}

	@Override
	public void onDisabled() {
		query = "";
		if (field != null && owner.get() != null) {
			Screens.getButtons(owner.get()).remove(field);
		}
		detach();
	}
}
