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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The ChaosUtils interface.
 *
 * <p>A floating window (drag the top bar, resize from the corner, double-click the bar to re-centre)
 * with a sidebar of categories on the left and a searchable list of module cards on the right.
 * Every setting of every feature is rendered generically from its {@link Setting} description, so
 * a newly added feature shows up in the interface without touching this class.
 */
public final class ChaosClickGui extends ChaosScreen {
	private static final float SIDEBAR_MIN = 138.0F;
	private static final float CARD_GAP = 6.0F;

	private final Map<String, Boolean> expanded = new HashMap<>();
	private final List<ModuleCard> cards = new ArrayList<>();
	private Category selected = Category.HUD;
	private String query = "";
	private float selectedPulse;

	private UiWidgets.ScrollList list;
	private UiWidgets.SearchField search;
	private UiWidgets.Label headerTitle;
	private UiWidgets.Label headerSubtitle;
	private float sidebarWidth;

	public ChaosClickGui() {
		this(null);
	}

	public ChaosClickGui(Screen parent) {
		super(parent, Component.literal("ChaosUtils"), "window.main", 680.0F, 440.0F);
	}

	// ------------------------------------------------------------------ layout

	@Override
	protected void buildLayout() {
		UiTheme theme = UiTheme.get();
		float winX = window.x();
		float winY = window.y();
		float sidebarTop = winY + window.titleHeight() + 10.0F;
		float sidebarHeight = window.height() - window.titleHeight() - 20.0F;
		this.sidebarWidth = Math.max(SIDEBAR_MIN, theme.sidebarWidth + 44.0F);

		float contentX = winX + sidebarWidth + 24.0F;
		float contentWidth = Math.max(220.0F, window.right() - 14.0F - contentX);

		SidebarPanel panel = new SidebarPanel();
		panel.setBounds(winX + 8.0F, sidebarTop - 4.0F, sidebarWidth - 4.0F, sidebarHeight + 4.0F);
		add(panel);

		// --- sidebar: categories
		float cursor = sidebarTop + 22.0F;
		for (Category category : Category.values()) {
			SidebarItem item = new SidebarItem(this, category);
			item.setBounds(winX + 14.0F, cursor, sidebarWidth - 12.0F, 26.0F);
			item.setAppearDelay(0.02F * category.ordinal());
			add(item);
			cursor += 28.0F;
		}

		// --- sidebar: quick actions
		float actionsBottom = winY + window.height() - 12.0F;
		UiWidgets.Label general = new UiWidgets.Label("General", UiTheme.get().textFaint);
		general.setBounds(winX + 22.0F, actionsBottom - 76.0F, sidebarWidth - 20.0F, 10.0F);
		add(general);
		float half = (sidebarWidth - 18.0F) * 0.5F;
		add(smallButton("HUD Editor", winX + 14.0F, actionsBottom - 62.0F, half,
				() -> openHudEditor(), "Move every overlay with the mouse."));
		add(smallButton("Radial", winX + 14.0F + half + 6.0F, actionsBottom - 62.0F, half,
				() -> open(new ChaosScreens.RadialEditorScreen(this)), "Create, edit and reorder the radial menu."));
		add(smallButton("Waypoints", winX + 14.0F, actionsBottom - 40.0F, half,
				() -> open(new ChaosScreens.WaypointsScreen(this)), "Death markers and manual waypoints."));
		add(smallButton("Chat", winX + 14.0F + half + 6.0F, actionsBottom - 40.0F, half,
				() -> open(new ChaosScreens.ChatHistoryScreen(this)), "Search and copy everything you saw in chat."));
		add(smallButton("Screenshots", winX + 14.0F, actionsBottom - 18.0F, half,
				() -> open(new ChaosScreens.ScreenshotScreen(this)), "Browse, copy and crop local screenshots."));

		UiWidgets.Button panic = new UiWidgets.Button(
				ChaosUtils.overlaysHidden() ? "Show overlays" : "Hide overlays",
				UiWidgets.Button.Variant.DANGER, theme.negative, () -> {
					ChaosUtils.toggleOverlays();
					toast(ChaosUtils.overlaysHidden() ? "Overlays hidden" : "Overlays visible");
					refresh();
				});
		panic.setBounds(winX + 14.0F + half + 6.0F, actionsBottom - 18.0F, half, 18.0F);
		panic.setTooltip("Panic switch: hides every ChaosUtils overlay instantly.");
		add(panic);

		// --- content header
		headerTitle = new UiWidgets.Label(titleText(), theme.text).bold();
		headerTitle.setBounds(contentX, winY + window.titleHeight() + 12.0F, contentWidth * 0.5F, 14.0F);
		add(headerTitle);

		headerSubtitle = new UiWidgets.Label(subtitleText(), theme.textFaint);
		headerSubtitle.setBounds(contentX, winY + window.titleHeight() + 27.0F, contentWidth * 0.5F, 12.0F);
		add(headerSubtitle);

		// --- search (vanilla text field, styled field around it)
		EditBox searchBox = new EditBox(this.font, 0, 0, 180, 14, Component.literal("Search"));
		searchBox.setBordered(false);
		searchBox.setTextColor(0xFFF4F5FA);
		searchBox.setHint(Component.literal("Search modules…"));
		searchBox.setMaxLength(48);
		searchBox.setValue(query);
		searchBox.setResponder(value -> {
			if (!value.equals(query)) {
				query = value;
				rebuildCards();
			}
		});
		addInput(searchBox);
		search = new UiWidgets.SearchField(searchBox);
		search.setOnClear(() -> {
			query = "";
			rebuildCards();
		});
		search.place(contentX + contentWidth - 200.0F, winY + window.titleHeight() + 14.0F, 200.0F, 22.0F);
		add(search);

		UiWidgets.IconButton collapse = new UiWidgets.IconButton(
				(graphics, cx, cy, alpha) -> {
					// Double chevron pointing up: "collapse everything".
					for (int i = 0; i < 2; i++) {
						float offset = (i - 0.5F) * 4.0F;
						Ui.chevron(graphics, cx, cy + offset, 6.0F, -90.0F, Render.alpha(theme.textDim, alpha));
					}
				}, theme.accent, this::collapseAll);
		collapse.setTooltip("Collapse every expanded module.");
		collapse.setBounds(contentX + contentWidth - 26.0F, winY + window.titleHeight() + 14.0F, 22.0F, 22.0F);
		collapse.setAppearDelay(0.05F);
		add(collapse);

		// --- module list
		list = new UiWidgets.ScrollList();
		list.setSpacing(CARD_GAP);
		list.setBounds(contentX, winY + window.titleHeight() + 44.0F, contentWidth,
				Math.max(80.0F, window.height() - window.titleHeight() - 56.0F));
		list.snapAppear();
		add(list);
		rebuildCards();
	}

	private UiWidgets.Button smallButton(String label, float x, float y, float width, Runnable action, String tooltip) {
		UiWidgets.Button button = new UiWidgets.Button(label, UiWidgets.Button.Variant.GHOST, UiTheme.get().accent, action);
		button.setBounds(x, y, width, 18.0F);
		button.setTooltip(tooltip);
		button.setPadding(2.0F);
		return button;
	}

	private void open(Screen screen) {
		if (this.minecraft != null) {
			this.minecraft.setScreen(screen);
		}
	}

	private String titleText() {
		if (!query.isBlank()) {
			return "Search";
		}
		return selected.displayName();
	}

	private String subtitleText() {
		if (!query.isBlank()) {
			int matches = ModuleManager.search(query).size();
			return matches + (matches == 1 ? " match" : " matches") + " for \"" + query + "\"";
		}
		int total = ModuleManager.byCategory(selected).size();
		long enabled = ModuleManager.byCategory(selected).stream().filter(Module::isEnabled).count();
		return total + (total == 1 ? " module" : " modules") + "   ·   " + enabled + " enabled";
	}

	private void collapseAll() {
		expanded.replaceAll((key, value) -> Boolean.FALSE);
		rebuildCards();
		toast("All modules collapsed");
	}

	@Override
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		UiTheme theme = theme();
		Render.gradientText(graphics, this.font, "ChaosUtils", window.x() + 16.0F, window.y() + 13.0F,
				theme.text, theme.accent, false);
		String version = modVersion();
		Render.text(graphics, this.font, version, window.x() + 20.0F + this.font.width("ChaosUtils"),
				window.y() + 14.0F, Render.alpha(theme.textFaint, alpha * 0.9F), false);
		// Window controls: re-centre and close.
		String hint = "drag to move  ·  double-click to re-centre";
		Render.text(graphics, this.font, hint, window.right() - 46.0F - this.font.width(hint), window.y() + 14.0F,
				Render.alpha(theme.textFaint, alpha * 0.7F), false);
	}

	private static String modVersion() {
		try {
			return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(ChaosUtils.MOD_ID)
					.map(container -> "beta " + container.getMetadata().getVersion().getFriendlyString())
					.orElse("beta");
		} catch (Throwable ignored) {
			return "beta";
		}
	}

	/** Status line under the module list. */
	@Override
	protected void renderFooter(GuiGraphics graphics, float alpha) {
		if (list == null) {
			return;
		}
		UiTheme theme = theme();
		float y = window.bottom() - 14.0F;
		String status = cards.size() + (cards.size() == 1 ? " module" : " modules") + " shown   ·   "
				+ ModuleManager.countEnabled() + "/" + ModuleManager.modules().size() + " active"
				+ (ChaosUtils.overlaysHidden() ? "   ·   overlays hidden" : "");
		Render.text(graphics, this.font, status, window.x() + sidebarWidth + 24.0F, y,
				Render.alpha(theme.textFaint, alpha * 0.85F), false);
	}

	// ------------------------------------------------------------------- cards

	private void rebuildCards() {
		if (list == null) {
			return;
		}
		list.clearItems();
		cards.clear();
		List<Module> modules = query.isBlank() ? ModuleManager.byCategory(selected) : ModuleManager.search(query);
		float width = list.width();
		int index = 0;
		for (Module module : modules) {
			ModuleCard card = new ModuleCard(this, module, query.isBlank() ? null : module.category().displayName());
			card.setBounds(0.0F, 0.0F, width, card.headerHeight());
			card.setAppearDelay(Math.min(0.22F, 0.014F * index));
			cards.add(card);
			list.addItem(card);
			index++;
		}
		list.layout();
		if (headerTitle != null) {
			headerTitle.setText(titleText());
		}
		if (headerSubtitle != null) {
			headerSubtitle.setText(subtitleText());
		}
	}

	@Override
	protected void modalClosed(UiModals.Modal modal) {
		refresh();
	}

	@Override
	public void requestClose() {
		if (search != null && search.box().isFocused()) {
			setFocused(null);
		}
		super.requestClose();
	}

	// --------------------------------------------------------------- sidebar

	/** Inset surface behind the sidebar entries. */
	private static final class SidebarPanel extends UiComponent {
		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Render.roundedRect(graphics, x, y, width, height, theme.radiusCard, Render.alpha(0x000000, 0.22F * alpha));
			Render.ring(graphics, x, y, width, height, theme.radiusCard, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			Ui.sectionLabel(graphics, UiWidgets.font(), "Modules", x + 12.0F, y + 12.0F, theme.textFaint, alpha);
		}
	}

	/** One category entry: accent pill, item icon, label and module count. */
	private static final class SidebarItem extends UiComponent {
		private final ChaosClickGui gui;
		private final Category category;
		private final Anim.Value selectedAnim = new Anim.Value(0.0F, 16.0F);

		private SidebarItem(ChaosClickGui gui, Category category) {
			this.gui = gui;
			this.category = category;
			this.setTooltip(category.displayName() + " — " + ModuleManager.byCategory(category).size() + " modules");
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			selectedAnim.set(gui.selected == category && gui.query.isBlank() ? 1.0F : 0.0F);
			selectedAnim.update(UiTheme.get().speed(16.0F));
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float selectedAmount = selectedAnim.get();
			float hoverAmount = hover.get();
			float radius = 9.0F;
			int background = Render.mix(Render.alpha(0xFFFFFFFF, 0.0F),
					Render.alpha(category.color(), 0.16F * alpha), selectedAmount);
			background = Render.mix(background, Render.alpha(0xFFFFFFFF, 0.06F * alpha), hoverAmount * (1.0F - selectedAmount));
			if (selectedAmount > 0.03F && theme.glow) {
				Render.glow(graphics, x + width * 0.5F, centerY(), width * 0.55F, category.color(),
						0.18F * selectedAmount * alpha);
			}
			Render.roundedRect(graphics, x, y, width, height, radius, background);
			if (selectedAmount > 0.05F) {
				Render.roundedRect(graphics, x, y + 5.0F, 2.5F, height - 10.0F, 1.25F,
						Render.alpha(category.color(), alpha * selectedAmount));
			}
			Ui.iconTile(graphics, x + 6.0F, centerY() - 10.0F, 20.0F, 6.0F, category.color(),
					alpha * (0.55F + 0.45F * Math.max(selectedAmount, hoverAmount * 0.8F)));
			Ui.itemIcon(graphics, category.icon(), x + 16.0F, centerY(), 0.8F, alpha);
			int textColor = Render.mix(theme.textDim, theme.text, Math.max(selectedAmount, hoverAmount * 0.7F));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), category.displayName(),
					width - 60.0F), x + 32.0F, centerY() - 4.0F, Render.alpha(textColor, alpha), false);
			String count = String.valueOf(ModuleManager.byCategory(category).size());
			Render.text(graphics, UiWidgets.font(), count, right() - 8.0F - UiWidgets.font().width(count), centerY() - 4.0F,
					Render.alpha(Render.mix(theme.textFaint, category.color(), Math.max(selectedAmount, 0.25F)), alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY) || button != 0) {
				return false;
			}
			gui.selected = category;
			gui.query = "";
			if (gui.search != null) {
				gui.search.box().setValue("");
			}
			gui.playClick(true);
			gui.refresh();
			return true;
		}
	}

	// ------------------------------------------------------------------ cards

	/** One feature: header with switch and expanding settings. */
	private static final class ModuleCard extends UiComponent {
		private static final float HEADER_ROOMY = 46.0F;
		private static final float HEADER_COMPACT = 40.0F;

		private final ChaosClickGui gui;
		private final Module module;
		private final String badge;
		private final List<UiComponent> rows = new ArrayList<>();
		private final Anim.Value expand = new Anim.Value(0.0F, 14.0F);
		private final Anim.Value switchHover = new Anim.Value(0.0F, 16.0F);
		private boolean rowsBuilt;
		private boolean expandedBefore;

		private ModuleCard(ChaosClickGui gui, Module module, String badge) {
			this.gui = gui;
			this.module = module;
			this.badge = badge;
			this.setTooltip(module.description() + "\n§7" + module.category().displayName()
					+ (keybindHint() == null ? "" : "  ·  key: " + keybindHint()));
		}

		private float headerHeight() {
			return UiTheme.get().compact ? HEADER_COMPACT : HEADER_ROOMY;
		}

		private boolean expanded() {
			return gui.expanded.getOrDefault(module.id(), Boolean.FALSE);
		}

		private float rowsHeight() {
			if (!expandedBefore || rows.isEmpty()) {
				return 0.0F;
			}
			float total = 0.0F;
			for (UiComponent row : rows) {
				total += row.height() + 2.0F;
			}
			return total + 10.0F;
		}

		private void buildRows(float rowWidth) {
			rows.clear();
			clearChildren();
			float y = 0.0F;
			for (Setting<?> setting : module.settings()) {
				if (setting == module.enabled()) {
					continue;
				}
				UiComponent row = UiWidgets.forSetting(setting, gui);
				if (row == null) {
					continue;
				}
				row.setBounds(0.0F, y, rowWidth, UiWidgets.rowHeight(setting));
				row.setTooltip(setting.description
						+ (setting.description == null || setting.description.isEmpty() ? "" : "\n")
						+ "§7right-click to reset  ·  default " + setting.display());
				rows.add(row);
				addChild(row);
				y += row.height() + 2.0F;
			}
			rowsBuilt = true;
		}

		private String keybindHint() {
			if (!UiTheme.get().keybindHints) {
				return null;
			}
			try {
				return switch (module.id()) {
					case "radial_menu" -> dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
					case "gui" -> dev.chaosutils.core.Keybinds.openGui.getTranslatedKeyMessage().getString();
					case "zoom" -> dev.chaosutils.core.Keybinds.zoom.getTranslatedKeyMessage().getString();
					case "perspective_lock" -> dev.chaosutils.core.Keybinds.freeLook.getTranslatedKeyMessage().getString();
					case "container_search" -> dev.chaosutils.core.Keybinds.searchContainer.getTranslatedKeyMessage().getString();
					case "chat_history" -> dev.chaosutils.core.Keybinds.chatHistory.getTranslatedKeyMessage().getString();
					case "screenshots" -> dev.chaosutils.core.Keybinds.screenshotPopup.getTranslatedKeyMessage().getString();
					case "gamma" -> dev.chaosutils.core.Keybinds.toggleGamma.getTranslatedKeyMessage().getString();
					case "waypoint" -> dev.chaosutils.core.Keybinds.addWaypoint.getTranslatedKeyMessage().getString();
					default -> null;
				};
			} catch (Throwable ignored) {
				return null;
			}
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			boolean expanded = expanded();
			if (expanded && !rowsBuilt) {
				buildRows(width - 24.0F);
			}
			if (expanded) {
				expandedBefore = true;
			}
			expand.set(expanded ? 1.0F : 0.0F);
			expand.update(UiTheme.get().speed(14.0F));
			float extra = expandedBefore ? rowsHeight() * Anim.easeOutQuint(expand.get()) : 0.0F;
			height = headerHeight() + extra;
			super.update(deltaSeconds, mouseX, mouseY);
			switchHover.set(isOverSwitch(mouseX, mouseY) ? 1.0F : 0.0F);
			switchHover.update(UiTheme.get().speed(16.0F));
			float rowY = headerHeight() + 6.0F;
			for (UiComponent row : rows) {
				row.setBounds(x + 12.0F, y + rowY, width - 24.0F, row.height());
				row.setLayerAlpha(layerAlpha * Anim.clamp01(expand.get()));
				row.setVisible(expand.get() > 0.6F);
				if (row.isVisible()) {
					row.update(deltaSeconds, mouseX, mouseY);
				}
				rowY += row.height() + 2.0F;
			}
		}

		private boolean isOverSwitch(float mouseX, float mouseY) {
			float switchX = right() - 44.0F;
			return mouseX >= switchX && mouseX <= switchX + 32.0F && mouseY >= y && mouseY <= y + headerHeight();
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			int accent = module.category().color();
			boolean on = module.isEnabled();
			float radius = theme.radiusCard;
			float hoverAmount = hover.get();
			float expandAmount = expand.get();

			graphics.pose().pushMatrix();
			graphics.pose().translate(0.0F, offset);

			Ui.card(graphics, x, y, width, height, radius, theme, hoverAmount, on, alpha);

			// Icon tile
			Ui.iconTile(graphics, x + 12.0F, y + (headerHeight() - 26.0F) * 0.5F, 26.0F, 8.0F, accent, alpha);
			Ui.itemIcon(graphics, module.category().icon(), x + 25.0F, y + headerHeight() * 0.5F, 0.95F, alpha);

			float textX = x + 48.0F;
			float textRight = x + width - 92.0F;
			if (badge != null) {
				int badgeWidth = UiWidgets.font().width(badge) + 12;
				Render.roundedRect(graphics, textX, y + 9.0F, badgeWidth, 12.0F, 6.0F,
						Render.alpha(accent, 0.18F * alpha));
				Render.text(graphics, UiWidgets.font(), badge, textX + 6.0F, y + 11.0F, Render.alpha(accent, alpha), false);
				textX += badgeWidth + 6.0F;
			}
			Render.boldText(graphics, UiWidgets.font(),
					Render.ellipsize(UiWidgets.font(), module.name(), textRight - textX),
					textX, y + 10.0F, Render.alpha(on ? theme.text : theme.textDim, alpha), false);

			String keybind = keybindHint();
			float descriptionRight = textRight;
			if (keybind != null && !keybind.isEmpty()) {
				int chipWidth = UiWidgets.font().width(keybind) + 12;
				float chipX = textX + Math.min(UiWidgets.font().width(module.name()) + 8.0F, textRight - textX - chipWidth);
				Render.roundedRect(graphics, chipX, y + 8.0F, chipWidth, 13.0F, 6.5F,
						Render.alpha(0xFFFFFFFF, 0.07F * alpha));
				Render.text(graphics, UiWidgets.font(), keybind, chipX + 6.0F, y + 10.5F,
						Render.alpha(theme.textFaint, alpha), false);
			}
			Render.text(graphics, UiWidgets.font(),
					Render.ellipsize(UiWidgets.font(), module.description(), descriptionRight - textX),
					textX, y + headerHeight() - 18.0F,
					Render.alpha(Render.mix(theme.textFaint, theme.textDim, hoverAmount), alpha), false);

			// Switch (drawn here, not as a child, so the whole card stays one component).
			float switchWidth = 30.0F;
			float switchHeight = 16.0F;
			float switchX = right() - switchWidth - 44.0F;
			float switchY = y + (headerHeight() - switchHeight) * 0.5F;
			float onAmount = on ? 1.0F : 0.0F;
			int track = Render.mix(Render.alpha(theme.track, alpha),
					Render.alpha(Render.mix(accent, 0xFFFFFFFF, switchHover.get() * 0.12F), alpha), onAmount);
			if (onAmount > 0.05F && theme.glow) {
				Render.glow(graphics, switchX + switchWidth * 0.5F, switchY + switchHeight * 0.5F, switchWidth * 1.15F,
						accent, 0.30F * onAmount * alpha);
			}
			Render.roundedRect(graphics, switchX, switchY, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.ring(graphics, switchX, switchY, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(theme.outline, alpha));
			float knobRadius = switchHeight * 0.5F - 1.6F;
			float knobX = switchX + switchHeight * 0.5F + (switchWidth - switchHeight) * onAmount;
			Ui.knob(graphics, knobX, switchY + switchHeight * 0.5F, knobRadius,
					Render.mix(0xFFD7D8E4, 0xFFFFFFFF, onAmount), alpha);

			// Expander chevron
			float chevronX = right() - 22.0F;
			float chevronY = y + headerHeight() * 0.5F;
			Ui.chevron(graphics, chevronX, chevronY, 7.0F, 90.0F * expandAmount,
					Render.alpha(Render.mix(theme.textFaint, theme.text, hoverAmount), alpha));

			// Settings
			if (expandAmount > 0.02F && expandedBefore) {
				float clipTop = y + headerHeight();
				float clipHeight = Math.max(0.0F, height - headerHeight());
				Render.scissor(graphics, x, clipTop, width, clipHeight);
				Ui.divider(graphics, x + 12.0F, y + headerHeight() + 2.0F, width - 24.0F, theme, alpha * 0.8F);
				for (UiComponent row : rows) {
					if (!row.isVisible()) {
						continue;
					}
					boolean rowHovered = row.contains(mouseX, mouseY);
					if (rowHovered) {
						Render.roundedRect(graphics, row.x() - 4.0F, row.y() - 2.0F, row.width() + 8.0F, row.height() + 2.0F,
								6.0F, Render.alpha(0xFFFFFFFF, 0.045F * alpha));
					}
					row.render(graphics, mouseX, mouseY, deltaSeconds);
				}
				Render.unscissor(graphics);
			}
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			boolean overSwitch = isOverSwitch(mouseX, mouseY);
			float rowsTop = y + headerHeight();
			if (button == 0 && expand.get() > 0.6F && mouseY > rowsTop) {
				for (UiComponent row : rows) {
					if (row.isVisible() && row.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
				return true;
			}
			if (mouseY > y + headerHeight()) {
				return false;
			}
			if (button == 0 && overSwitch) {
				module.enabled().toggle();
				gui.playClick(module.isEnabled());
				return true;
			}
			if (button == 1) {
				if (module.isEnabled()) {
					module.enabled().toggle();
				}
				for (Setting<?> setting : module.settings()) {
					if (setting != module.enabled()) {
						setting.reset();
					}
				}
				gui.playClick(false);
				gui.toast(module.name(), "reset to defaults", UiTheme.get().warning);
				return true;
			}
			if (button == 0) {
				gui.expanded.put(module.id(), !expanded());
				gui.playClick(true);
				return true;
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
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseScrolled(mouseX, mouseY, amount)) {
					return true;
				}
			}
			return false;
		}
	}

	/** Search helper used by the list screens. */
	public static List<Module> searchResults(String query) {
		return ModuleManager.search(query == null ? "" : query.toLowerCase(Locale.ROOT));
	}
}
