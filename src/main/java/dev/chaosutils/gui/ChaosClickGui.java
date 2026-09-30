package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * The main ChaosUtils interface.
 *
 * <p>Sidebar with categories on the left, searchable animated module cards on the right.
 * Every setting of every feature is rendered generically from its {@link Setting}
 * description, so nothing can be missing from the GUI.
 */
public final class ChaosClickGui extends ChaosScreen {
	private final Map<String, Boolean> expanded = new HashMap<>();
	private Category selected = Category.HUD;
	private String query = "";
	private EditBox search;

	public ChaosClickGui() {
		super(Component.literal("ChaosUtils"));
	}

	@Override
	protected void buildLayout() {
		UiTheme theme = theme();
		float sidebarWidth = theme.sidebarWidth;
		float topBar = 34.0F;
		float contentX = 12.0F + sidebarWidth + 8.0F;
		float contentWidth = this.width - contentX - 12.0F;

		// --- sidebar
		float y = topBar + 8.0F;
		List<Category> categories = List.of(Category.values());
		for (Category category : categories) {
			SidebarEntry entry = new SidebarEntry(this, category);
			entry.setBounds(12.0F, y, sidebarWidth, 24.0F);
			add(entry);
			y += 26.0F;
		}

		// --- buttons
		float buttonWidth = (sidebarWidth - 6.0F) * 0.5F;
		UiWidgets.Button hudEditor = new UiWidgets.Button(this, "HUD Editor", theme.accent, this::openHudEditor);
		hudEditor.setBounds(12.0F, this.height - 54.0F, buttonWidth, 18.0F);
		hudEditor.setTooltip("Move every overlay with the mouse.");
		add(hudEditor);

		UiWidgets.Button radialEditor = new UiWidgets.Button(this, "Radial", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.RadialEditorScreen(this));
			}
		});
		radialEditor.setBounds(12.0F + buttonWidth + 6.0F, this.height - 54.0F, buttonWidth, 18.0F);
		radialEditor.setTooltip("Create, edit and delete radial menu entries.");
		add(radialEditor);

		UiWidgets.Button waypoints = new UiWidgets.Button(this, "Waypoints", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.WaypointsScreen(this));
			}
		});
		waypoints.setBounds(12.0F, this.height - 76.0F, buttonWidth, 18.0F);
		waypoints.setTooltip("Death markers and manual waypoints.");
		add(waypoints);

		UiWidgets.Button chatHistory = new UiWidgets.Button(this, "Chat", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.ChatHistoryScreen(this));
			}
		});
		chatHistory.setBounds(12.0F + buttonWidth + 6.0F, this.height - 76.0F, buttonWidth, 18.0F);
		chatHistory.setTooltip("Search and copy everything you saw in chat.");
		add(chatHistory);

		UiWidgets.Button panic = new UiWidgets.Button(this, "Hide overlays", 0xFFE05B5B, () -> ChaosUtils.toggleOverlays());
		panic.setBounds(12.0F, this.height - 32.0F, sidebarWidth, 18.0F);
		panic.setTooltip("Panic key: hides every ChaosUtils overlay instantly.");
		add(panic);

		// --- search field
		search = new EditBox(this.font, Math.round(contentX), Math.round(8.0F), Math.round(Math.min(220.0F, contentWidth)), 18, Component.literal("Search"));
		search.setBordered(false);
		search.setTextColor(0xFFF2F2F7);
		search.setHint(Component.literal("Search features…"));
		search.setResponder(value -> {
			this.query = value;
			refreshCards();
		});
		addInput(search);

		// --- module cards
		ScrollPanel panel = new ScrollPanel();
		panel.setBounds(contentX, topBar + 4.0F, contentWidth, this.height - topBar - 16.0F);
		add(panel);
		rebuildCards(panel);
	}

	private ScrollPanel panel() {
		for (UiComponent component : components) {
			if (component instanceof ScrollPanel scrollPanel) {
				return scrollPanel;
			}
		}
		return null;
	}

	private void refreshCards() {
		ScrollPanel panel = panel();
		if (panel != null) {
			rebuildCards(panel);
		}
	}

	private void rebuildCards(ScrollPanel panel) {
		panel.reset();
		panel.clearChildren();
		UiTheme theme = theme();
		float cardHeight = theme.compact ? 24.0F : 28.0F;
		float gap = 6.0F;
		float y = 0.0F;
		List<Module> modules = new ArrayList<>();
		if (query != null && !query.isBlank()) {
			modules.addAll(ModuleManager.search(query));
		} else {
			modules.addAll(ModuleManager.byCategory(selected));
		}
		for (Module module : modules) {
			ModuleCard card = new ModuleCard(this, module);
			card.setBounds(0.0F, y, panel.width(), cardHeight);
			panel.addCard(card, y);
			y += cardHeight + gap + card.extraHeight();
		}
		panel.setContentHeight(y);
	}

	@Override
	protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		super.renderBackdrop(graphics, mouseX, mouseY, deltaTicks);
		UiTheme theme = theme();
		float sidebarWidth = theme.sidebarWidth;
		// Sidebar / content separation with a soft accent glow.
		Render.shadowedPanel(graphics, 12.0F, 34.0F, sidebarWidth, this.height - 34.0F - 62.0F, theme.radius, theme.panel, 0x00000000);
		Render.verticalGradient(graphics, 12.0F, 34.0F, sidebarWidth, this.height - 34.0F - 62.0F,
				Render.alpha(theme.accent, 0.10F), 0x00000000);

		float contentX = 12.0F + sidebarWidth + 8.0F;
		Render.shadowedPanel(graphics, contentX - 4.0F, 30.0F, this.width - contentX - 4.0F, this.height - 46.0F, theme.radius, theme.panelAlt, 0x00000000);

		// Header
		Render.text(graphics, font(), "ChaosUtils", 14.0F, 12.0F, theme.text, false);
		String subtitle = selected.displayName() + "  ·  " + ModuleManager.countEnabled() + "/" + ModuleManager.modules().size() + " active"
				+ (ChaosUtils.overlaysHidden() ? "  ·  OVERLAYS HIDDEN" : "");
		Render.text(graphics, font(), subtitle, 14.0F, 22.0F, theme.textFaint, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		super.render(graphics, mouseX, mouseY, deltaTicks);
		if (search != null && search.getValue().isEmpty() && !search.isFocused()) {
			Render.text(graphics, font(), "Search features…", search.getX() + 2.0F, search.getY() + 5.0F, theme().textFaint, false);
		}
	}

	// ------------------------------------------------------------------ widgets

	private static final class SidebarEntry extends UiComponent {
		private final ChaosClickGui gui;
		private final Category category;

		private SidebarEntry(ChaosClickGui gui, Category category) {
			this.gui = gui;
			this.category = category;
			this.setTooltip(category.displayName());
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			active.set(gui.selected == category ? 1.0F : 0.0F);
			active.update(deltaSeconds);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			float selectedAmount = active.get();
			int background = Render.mix(0x00000000, Render.alpha(theme.accent, 0.30F), selectedAmount);
			background = Render.mix(background, Render.alpha(0xFFFFFFFF, 0.06F), hover.get() * (1.0F - selectedAmount));
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.7F, background);
			if (selectedAmount > 0.01F) {
				Render.roundedRect(graphics, x, y + 4.0F, 2.5F * selectedAmount, height - 8.0F, 1.5F, category.color());
			}
			int textColor = Render.mix(theme.textDim, theme.text, Math.max(selectedAmount, hover.get()));
			Render.text(graphics, gui.font(), category.displayName(), x + 24.0F, y + (height - 8.0F) * 0.5F - 0.5F, textColor, false);
			Render.item(graphics, category.icon(), x + 6.0F, y + (height - 16.0F) * 0.5F);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				gui.selected = category;
				gui.query = "";
				if (gui.search != null) {
					gui.search.setValue("");
				}
				gui.refreshCards();
				gui.playClick(true);
				return true;
			}
			return false;
		}
	}

	/** Scrollable, clipped container that keeps its children's hover state correct. */
	private static final class ScrollPanel extends UiComponent {
		private final Map<UiComponent, Float> baseY = new HashMap<>();
		private float scroll;
		private float content;
		private float targetScroll;

		private void addCard(UiComponent card, float baseOffset) {
			baseY.put(card, baseOffset);
			card.setBounds(card.x(), y + baseOffset, card.width(), card.height());
			addChild(card);
		}

		private void setContentHeight(float contentHeight) {
			this.content = contentHeight;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			float maxScroll = Math.max(0.0F, content - height);
			targetScroll = Anim.clamp(targetScroll, 0.0F, maxScroll);
			scroll = Anim.approach(scroll, targetScroll, UiTheme.get().speed(14.0F), deltaSeconds);
			for (UiComponent child : children()) {
				Float base = baseY.get(child);
				if (base != null) {
					child.setBounds(child.x(), y + base - scroll, child.width(), child.height());
				}
			}
			super.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.scissor(graphics, x, y, width, height);
			renderChildren(graphics, mouseX, mouseY, deltaSeconds);
			Render.unscissor(graphics);
			float maxScroll = Math.max(1.0F, content - height);
			if (content > height) {
				float barHeight = Math.max(24.0F, height * (height / content));
				float barY = y + (height - barHeight) * (scroll / maxScroll);
				Render.roundedRect(graphics, x + width - 3.0F, barY, 3.0F, barHeight, 1.5F, Render.alpha(theme.accent, 0.65F));
			}
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				targetScroll -= (float) amount * 24.0F;
				return true;
			}
			return super.mouseScrolled(mouseX, mouseY, amount);
		}

		@Override
		public void reset() {
			super.reset();
			baseY.clear();
			scroll = 0.0F;
			targetScroll = 0.0F;
			content = 0.0F;
		}
	}

	/** One feature: header with quick toggle plus every setting, animated expansion. */
	private static final class ModuleCard extends UiComponent {
		private final ChaosClickGui gui;
		private final Module module;
		private final Anim.Value expand = new Anim.Value(0.0F, 12.0F);
		private final List<UiComponent> rows = new ArrayList<>();

		private ModuleCard(ChaosClickGui gui, Module module) {
			this.gui = gui;
			this.module = module;
			this.setTooltip(module.description());
			buildRows();
			expand.snap(expanded() ? 1.0F : 0.0F);
		}

		private boolean expanded() {
			return gui.expanded.getOrDefault(module.id(), Boolean.FALSE);
		}

		private void buildRows() {
			UiTheme theme = UiTheme.get();
			float rowHeight = theme.compact ? 18.0F : 20.0F;
			float y = 0.0F;
			for (Setting<?> setting : module.settings()) {
				if (setting == module.enabled()) {
					continue;
				}
				UiComponent row = UiWidgets.forSetting(setting, gui);
				row.setBounds(0.0F, y, width - 16.0F, rowHeight);
				row.setTooltip(setting.description + (setting.description.isEmpty() ? "" : "\n") + "default: " + defaultHint(setting));
				rows.add(row);
				y += rowHeight + 2.0F;
			}
		}

		private String defaultHint(Setting<?> setting) {
			return setting.isDefault() ? "current" : "changed";
		}

		private float extraHeight() {
			return rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size() * expand.get();
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			expand.set(expanded() ? 1.0F : 0.0F);
			expand.update(deltaSeconds);
			float headerHeight = height;
			float rowHeight = rows.isEmpty() ? 0.0F : rows.get(0).height() + 2.0F;
			for (int i = 0; i < rows.size(); i++) {
				UiComponent row = rows.get(i);
				row.setBounds(x + 8.0F, y + headerHeight + 2.0F + i * rowHeight, width - 16.0F, row.height());
			}
			// Only update rows that are inside the visible portion of the card.
			float visibleHeight = expand.get() * rowHeight * rows.size();
			for (int i = 0; i < rows.size(); i++) {
				boolean visibleRow = (i + 1) * rowHeight <= visibleHeight + rowHeight;
				rows.get(i).setVisible(visibleRow);
				if (visibleRow) {
					rows.get(i).update(deltaSeconds, mouseX, mouseY);
				}
			}
			super.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			int accent = module.category().color();
			boolean on = module.isEnabled();
			float visibleHeight = expand.get() * (rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size());
			float totalHeight = height + visibleHeight;

			int background = Render.mix(theme.panel, theme.panelHover, hover.get() * 0.7F);
			Render.roundedRect(graphics, x, y, width, totalHeight, theme.radius, background);
			Render.roundedBorder(graphics, x, y, width, totalHeight, theme.radius, 1.0F,
					Render.alpha(on ? accent : 0xFFFFFFFF, on ? 0.45F : 0.08F), background);
			if (on) {
				Render.roundedRect(graphics, x, y + 5.0F, 2.5F, height - 10.0F, 1.25F, accent);
			}
			Render.item(graphics, module.category().icon(), x + 8.0F, y + (height - 16.0F) * 0.5F);
			Render.text(graphics, gui.font(), module.name(), x + 28.0F, y + 6.0F, on ? theme.text : theme.textDim, false);
			String subtitle = module.description();
			while (gui.font().width(subtitle) > width - 120.0F && subtitle.length() > 6) {
				subtitle = subtitle.substring(0, subtitle.length() - 2) + "…";
			}
			Render.text(graphics, gui.font(), subtitle, x + 28.0F, y + height - 11.0F, theme.textFaint, false);

			// quick toggle
			float switchWidth = 26.0F;
			float sx = x + width - switchWidth - 10.0F;
			float sy = y + (height - 13.0F) * 0.5F;
			int track = Render.mix(0xFF3A3A48, accent, on ? 1.0F : 0.0F);
			Render.roundedRect(graphics, sx, sy, switchWidth, 13.0F, 6.5F, track);
			float knob = 9.0F;
			Render.roundedRect(graphics, sx + 2.0F + (on ? switchWidth - knob - 4.0F : 0.0F), sy + 2.0F, knob, knob, 4.5F, 0xFFFFFFFF);

			Render.text(graphics, gui.font(), expanded() ? "▾" : "▸", x + width - switchWidth - 26.0F, y + (height - 8.0F) * 0.5F - 0.5F,
					theme.textFaint, false);

			if (visibleHeight > 1.0F) {
				Render.scissor(graphics, x, y + height, width, visibleHeight);
				for (UiComponent row : rows) {
					if (row.isVisible()) {
						row.render(graphics, mouseX, mouseY, deltaSeconds);
					}
				}
				Render.unscissor(graphics);
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			float visibleHeight = expand.get() * (rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size());
			if (visibleHeight > 1.0F && mouseY > y + height && mouseY <= y + height + visibleHeight) {
				for (UiComponent row : rows) {
					if (row.isVisible() && row.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
			}
			float switchWidth = 26.0F;
			float sx = x + width - switchWidth - 10.0F;
			if (mouseY >= y && mouseY <= y + height) {
				if (mouseX >= sx - 2.0F && mouseX <= sx + switchWidth + 2.0F) {
					if (button == 0) {
						module.enabled().toggle();
						gui.playClick(module.isEnabled());
						return true;
					}
					if (button == 1) {
						module.enabled().reset();
						return true;
					}
				}
				if (button == 0) {
					gui.expanded.put(module.id(), !expanded());
					gui.refreshCards();
					gui.playClick(true);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseReleased(mouseX, mouseY, button)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			return false;
		}

		@Override
		public String tooltip() {
			return module.description() + "\n§7" + module.category().displayName() + "  ·  " + keybindHint();
		}

		private String keybindHint() {
			String key = switch (module.id()) {
				case "radial_menu" -> dev.chaosutils.core.Keybinds.radialMenu == null ? "" : dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
				case "gui" -> dev.chaosutils.core.Keybinds.openGui == null ? "" : dev.chaosutils.core.Keybinds.openGui.getTranslatedKeyMessage().getString();
				case "zoom" -> dev.chaosutils.core.Keybinds.zoom == null ? "" : dev.chaosutils.core.Keybinds.zoom.getTranslatedKeyMessage().getString();
				default -> "";
			};
			if (key.isEmpty()) {
				return "Rebind in Options → Controls";
			}
			return "default key: " + key;
		}
	}

	/** Small helper so the widget package can query the currently typed search query. */
	public String query() {
		return query == null ? "" : query.toLowerCase(Locale.ROOT);
	}
}
