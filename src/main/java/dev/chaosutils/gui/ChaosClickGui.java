package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The ChaosUtils interface.
 *
 * <p>One opaque, draggable panel: a module rail on the left, a searchable list of module cards on
 * the right. Cards expand in place and render every setting of their feature from its
 * {@link Setting} description, so a new feature appears here without touching this class.
 *
 * <p>Visual rules of the panel: neutral surfaces only, one accent colour, hairline borders, a
 * bundled vector icon for every row and a bundled sans-serif face instead of the vanilla pixel
 * font. Nothing is drawn outside of the rounded body, so the window reads as one clean sheet
 * instead of a pile of boxes.
 */
public final class ChaosClickGui extends ChaosScreen {
	// ----------------------------------------------------------------- metrics
	private static final float PAD = 14.0F;
	private static final float SIDEBAR_WIDTH = 154.0F;
	private static final float COLUMN_GAP = 16.0F;
	private static final float CARD_GAP = 6.0F;
	private static final float ROW_HEIGHT = 24.0F;
	private static final float ROW_GAP = 2.0F;
	private static final float SEARCH_WIDTH = 190.0F;
	private static final float SEARCH_HEIGHT = 22.0F;

	private final Map<String, Boolean> expanded = new HashMap<>();
	private final List<ModuleCard> cards = new ArrayList<>();
	private Category selected = Category.HUD;
	private String query = "";

	private UiWidgets.ScrollList list;
	private UiWidgets.SearchField search;
	private UiWidgets.Label contentTitle;
	private UiWidgets.Label contentSubtitle;
	private UiWidgets.Label listCaption;
	private float contentX;
	private float contentWidth;

	public ChaosClickGui() {
		this(null);
	}

	public ChaosClickGui(Screen parent) {
		super(parent, Component.literal("ChaosUtils"), "window.main", 900.0F, 520.0F);
	}

	// ------------------------------------------------------------------ layout

	@Override
	protected void buildLayout() {
		UiTheme theme = UiTheme.get();
		float winX = window.x();
		float winY = window.y();
		float winW = window.width();
		float winH = window.height();

		buildSidebar(winX, winY, winH);

		contentX = winX + PAD + SIDEBAR_WIDTH + COLUMN_GAP;
		contentWidth = Math.max(240.0F, winX + winW - PAD - contentX);

		// --- content header: title, subtitle and the module counter
		contentTitle = new UiWidgets.Label(titleText(), theme.text).bold().big();
		contentTitle.setBounds(contentX, winY + 24.0F, contentWidth * 0.6F, 16.0F);
		add(contentTitle);

		contentSubtitle = new UiWidgets.Label(subtitleText(), theme.textFaint);
		contentSubtitle.setBounds(contentX, winY + 48.0F, contentWidth * 0.6F, 12.0F);
		contentSubtitle.setAppearDelay(0.03F);
		add(contentSubtitle);

		// --- search field and the two view switches
		float searchX = winX + winW - PAD - SEARCH_WIDTH - 52.0F;
		EditBox searchBox = new EditBox(UiFonts.font(), 0, 0, 150, 14, Component.literal("Search"));
		searchBox.setBordered(false);
		searchBox.setTextColor(0xFFF6F7FB);
		searchBox.setHint(Component.literal("Search modules"));
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
		search.place(searchX, winY + 22.0F, SEARCH_WIDTH, SEARCH_HEIGHT);
		add(search);

		add(viewButton(UiIcons.GRID, searchX + SEARCH_WIDTH + 6.0F, winY + 22.0F, "Group the modules by category."));
		add(viewButton(UiIcons.CHEVRON_UP, searchX + SEARCH_WIDTH + 30.0F, winY + 22.0F, "Collapse every expanded module."));

		// --- hairline under the header, like the separator of the reference layout
		add(new UiWidgets.Hairline(contentX, winY + 64.0F, contentWidth));

		// --- caption above the list
		listCaption = new UiWidgets.Label(captionText(), theme.textDim);
		listCaption.setBounds(contentX, winY + 74.0F, contentWidth, 12.0F);
		listCaption.setAppearDelay(0.05F);
		add(listCaption);

		// --- the module list
		list = new UiWidgets.ScrollList();
		list.setSpacing(CARD_GAP);
		list.setBounds(contentX, winY + 92.0F, contentWidth, Math.max(80.0F, winH - 104.0F));
		list.snapAppear();
		add(list);
		rebuildCards();
	}

	/** Left rail: brand block, module categories and the general entries. */
	private void buildSidebar(float winX, float winY, float winH) {
		float railX = winX + PAD;
		float railW = SIDEBAR_WIDTH;

		add(new Brand(railX, winY + 14.0F, railW));

		float cursor = winY + 62.0F;
		add(caption("Modules", railX + 4.0F, cursor, railW - 8.0F));
		cursor += 18.0F;
		for (Category category : Category.values()) {
			SidebarRow row = new SidebarRow(iconOf(category), category.displayName(), category.color(),
					() -> String.valueOf(ModuleManager.byCategory(category).size()),
					() -> selected == category && query.isBlank(),
					() -> selectCategory(category));
			row.setBounds(railX, cursor, railW, ROW_HEIGHT);
			row.setAppearDelay(0.012F * category.ordinal());
			row.setTooltip(category.displayName() + "  ·  " + ModuleManager.byCategory(category).size()
					+ " modules  ·  " + enabledIn(category) + " active");
			add(row);
			cursor += ROW_HEIGHT + ROW_GAP;
		}

		cursor += 12.0F;
		add(caption("General", railX + 4.0F, cursor, railW - 8.0F));
		cursor += 18.0F;

		add(generalRow(railX, cursor, railW, UiIcons.SLIDERS, "Settings", () -> showModule("gui")));
		cursor += ROW_HEIGHT + ROW_GAP;
		add(generalRow(railX, cursor, railW, UiIcons.PALETTE, "Theme", () -> openColorModal(ThemeModule.accent)));
		cursor += ROW_HEIGHT + ROW_GAP;
		add(generalRow(railX, cursor, railW, UiIcons.FOLDER, "Configs", this::openConfigs));
		cursor += ROW_HEIGHT + ROW_GAP;
		add(generalRow(railX, cursor, railW, UiIcons.USERS, "Socials", this::openSocials));
		cursor += ROW_HEIGHT + ROW_GAP;
		add(generalRow(railX, cursor, railW, UiIcons.KEYBOARD, "Keybinds", this::openKeybinds));

		UiWidgets.Label hint = new UiWidgets.Label("Esc closes  ·  " + openKeyName() + " toggles", UiTheme.get().textFaint);
		hint.setBounds(railX + 4.0F, winY + winH - 20.0F, railW - 8.0F, 12.0F);
		add(hint);
	}

	private UiWidgets.Label caption(String text, float x, float y, float width) {
		UiWidgets.Label label = new UiWidgets.Label(text.toUpperCase(Locale.ROOT), UiTheme.get().textFaint);
		label.setBounds(x, y, width, 10.0F);
		return label;
	}

	private SidebarRow generalRow(float x, float y, float width, UiIcons icon, String label, Runnable action) {
		SidebarRow row = new SidebarRow(icon, label, UiTheme.get().textDim, null, () -> false, action);
		row.setBounds(x, y, width, ROW_HEIGHT);
		return row;
	}

	private UiWidgets.IconButton viewButton(UiIcons icon, float x, float y, String tooltip) {
		UiTheme theme = UiTheme.get();
		UiWidgets.IconButton button = new UiWidgets.IconButton(
				(graphics, centerX, centerY, alpha) -> icon.drawCentered(graphics, centerX, centerY, 12.0F,
						Render.alpha(theme.textDim, alpha)),
				theme.accent, this::collapseAll);
		button.setTooltip(tooltip);
		button.setBounds(x, y, SEARCH_HEIGHT, SEARCH_HEIGHT);
		return button;
	}

	/** Icon of a category in the rail. */
	private static UiIcons iconOf(Category category) {
		return switch (category) {
			case HUD -> UiIcons.GAUGE;
			case VISUAL -> UiIcons.EYE;
			case RADIAL -> UiIcons.RADIAL;
			case CHAT -> UiIcons.CHAT;
			case INVENTORY -> UiIcons.BOX;
			case AUDIO -> UiIcons.NOTE;
			case QOL -> UiIcons.SPARKLE;
			case PERFORMANCE -> UiIcons.SLIDERS;
		};
	}

	private static long enabledIn(Category category) {
		return ModuleManager.byCategory(category).stream().filter(Module::isEnabled).count();
	}

	// ------------------------------------------------------------- interactions

	private void selectCategory(Category category) {
		selected = category;
		query = "";
		if (search != null) {
			search.box().setValue("");
		}
		playClick(true);
		refresh();
	}

	/** Jumps to a single module: selects its category, expands it and scrolls it into view. */
	private void showModule(String moduleId) {
		Module module = ModuleManager.get(moduleId);
		if (module == null) {
			return;
		}
		selected = module.category();
		query = "";
		if (search != null) {
			search.box().setValue("");
		}
		expanded.put(module.id(), Boolean.TRUE);
		playClick(true);
		refresh();
		for (int index = 0; index < cards.size(); index++) {
			if (cards.get(index).module.id().equals(moduleId)) {
				list.scrollTo(Math.max(0.0F, index * (cards.get(index).headerHeight() + CARD_GAP) - 8.0F));
				break;
			}
		}
	}

	private void openConfigs() {
		UiModals.Dialog dialog = new UiModals.Dialog("Configs",
				List.of("Everything you change is written to\n" + ChaosConfig.path() + "\n\n"
						+ ModuleManager.modules().size() + " modules and " + Keybinds.all().size()
						+ " keybinds are stored there."));
		dialog.add("Copy path", UiTheme.get().accent, d -> {
			Clipboard.copyText(ChaosConfig.path().toString());
			toast("Config path copied");
			d.close();
		});
		dialog.add("Save now", null, d -> {
			ChaosConfig.save();
			toast("Configuration saved");
			d.close();
		});
		dialog.add("Close", null, UiModals.Modal::close);
		pushModal(dialog);
		playClick(true);
	}

	private void openSocials() {
		UiModals.Dialog dialog = new UiModals.Dialog("Socials",
				List.of("ChaosUtils is client side only and open source.\n\n"
						+ "github.com/chaostobimc/ChaosUtils\n\n"
						+ "No telemetry, no packets of its own, nothing\nyour server could ever notice."));
		dialog.add("Copy link", UiTheme.get().accent, d -> {
			Clipboard.copyText("https://github.com/chaostobimc/ChaosUtils");
			toast("Link copied");
			d.close();
		});
		dialog.add("Close", null, UiModals.Modal::close);
		pushModal(dialog);
		playClick(true);
	}

	private void openKeybinds() {
		List<String> lines = new ArrayList<>();
		lines.add("Every ChaosUtils keybind in one place.\n");
		for (KeyMapping mapping : Keybinds.all()) {
			lines.add(mapping.getName() + "   §7" + keyName(mapping));
		}
		UiModals.Dialog dialog = new UiModals.Dialog("Keybinds", lines);
		dialog.add("Close", null, UiModals.Modal::close);
		pushModal(dialog);
		playClick(true);
	}

	private static String keyName(KeyMapping mapping) {
		try {
			return mapping.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "unbound";
		}
	}

	private static String openKeyName() {
		return keyName(Keybinds.openGui);
	}

	private void collapseAll() {
		expanded.replaceAll((key, value) -> Boolean.FALSE);
		rebuildCards();
		toast("All modules collapsed");
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
		return total + (total == 1 ? " module" : " modules") + "  ·  " + enabledIn(selected) + " enabled";
	}

	private String captionText() {
		return query.isBlank() ? "Modules" : "Results";
	}

	// ----------------------------------------------------------------- chrome

	@Override
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		// The brand lives in the rail; the title band only carries the window hint.
		UiTheme theme = theme();
		String hint = "drag to move  ·  corner to resize";
		Render.text(graphics, UiFonts.font(), hint,
				window.right() - 40.0F - UiFonts.width(hint, false), window.y() + 12.0F,
				Render.alpha(theme.textFaint, alpha * 0.55F), false);
	}

	/** Status line below the list. */
	@Override
	protected void renderFooter(GuiGraphics graphics, float alpha) {
		if (list == null) {
			return;
		}
		UiTheme theme = theme();
		String status = cards.size() + (cards.size() == 1 ? " module" : " modules") + " shown   ·   "
				+ ModuleManager.countEnabled() + "/" + ModuleManager.modules().size() + " active"
				+ (ChaosUtils.overlaysHidden() ? "   ·   overlays hidden" : "");
		float x = contentX;
		float y = window.bottom() - 14.0F;
		Render.text(graphics, UiFonts.font(), status, x, y, Render.alpha(theme.textFaint, alpha * 0.8F), false);
		Render.text(graphics, UiFonts.bold(), "build " + ChaosUtils.BUILD_TAG, window.right() - PAD
						- UiFonts.width("build " + ChaosUtils.BUILD_TAG, true), y,
				Render.alpha(Render.mix(theme.textFaint, theme.accent, 0.65F), alpha * 0.9F), false);
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
			card.setAppearDelay(Math.min(0.2F, 0.012F * index));
			cards.add(card);
			list.addItem(card);
			index++;
		}
		list.layout();
		if (contentTitle != null) {
			contentTitle.setText(titleText());
		}
		if (contentSubtitle != null) {
			contentSubtitle.setText(subtitleText());
		}
		if (listCaption != null) {
			listCaption.setText(captionText());
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

	// ------------------------------------------------------------------- rail

	/** Brand block: accent tile with the spark, name and version line. */
	private static final class Brand extends UiComponent {
		private Brand(float x, float y, float width) {
			setBounds(x, y, width, 30.0F);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float tile = 26.0F;
			Render.roundedRect(graphics, x, y, tile, tile, 8.0F, Render.alpha(theme.accent, alpha));
			UiIcons.SPARKLE.drawCentered(graphics, x + tile * 0.5F, y + tile * 0.5F, 15.0F,
					Render.alpha(theme.onAccent, alpha));
			if (theme.glow) {
				Render.glow(graphics, x + tile * 0.5F, y + tile * 0.5F, 26.0F, theme.accent, 0.22F * alpha);
			}
			Render.boldText(graphics, UiFonts.bold(), "ChaosUtils", x + tile + 9.0F, y + 3.0F,
					Render.alpha(theme.text, alpha), false);
			Render.text(graphics, UiFonts.font(), "BETA RELEASE " + ChaosUtils.BUILD_TAG, x + tile + 9.0F, y + 15.0F,
					Render.alpha(theme.textFaint, alpha), false);
		}
	}

	/** A single rail entry: icon, label and an optional counter on the right. */
	private final class SidebarRow extends UiComponent {
		private final UiIcons icon;
		private final String label;
		private final int accent;
		private final Supplier<String> counter;
		private final BooleanSupplier active;
		private final Runnable action;
		private final Anim.Value selectedAnim = new Anim.Value(0.0F, 16.0F);

		private SidebarRow(UiIcons icon, String label, int accent, Supplier<String> counter, BooleanSupplier active,
				Runnable action) {
			this.icon = icon;
			this.label = label;
			this.accent = accent;
			this.counter = counter;
			this.active = active;
			this.action = action;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			selectedAnim.set(active.getAsBoolean() ? 1.0F : 0.0F);
			selectedAnim.update(deltaSeconds, UiTheme.get().speed(16.0F));
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
			Ui.navRow(graphics, x, y, width, height, 7.0F, selectedAmount, hoverAmount, theme, alpha);
			int iconColor = Render.mix(Render.mix(theme.textFaint, theme.textDim, hoverAmount), accent,
					Math.max(selectedAmount, 0.0F));
			icon.drawCentered(graphics, x + 16.0F, centerY(), 13.0F, Render.alpha(iconColor, alpha));
			int textColor = Render.mix(Render.mix(theme.textDim, theme.text, hoverAmount * 0.8F), theme.text, selectedAmount);
			Render.text(graphics, UiFonts.font(), Render.ellipsize(UiFonts.font(), label, width - 58.0F),
					x + 29.0F, centerY() - 4.0F, Render.alpha(textColor, alpha), false);
			if (counter != null) {
				String value = counter.get();
				Render.text(graphics, UiFonts.font(), value,
						right() - 9.0F - UiFonts.width(value, false), centerY() - 4.0F,
						Render.alpha(Render.mix(theme.textFaint, accent, Math.max(selectedAmount, hoverAmount * 0.5F)), alpha),
						false);
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button != 0 || !isHovered(mouseX, mouseY)) {
				return false;
			}
			action.run();
			return true;
		}
	}

	// ------------------------------------------------------------------ cards

	/** One feature: header with an animated switch and the settings that expand below it. */
	private static final class ModuleCard extends UiComponent {
		private static final float HEADER_ROOMY = 42.0F;
		private static final float HEADER_COMPACT = 34.0F;

		private final ChaosClickGui gui;
		private final Module module;
		private final String badge;
		private final List<UiComponent> rows = new ArrayList<>();
		private final Anim.Value expand = new Anim.Value(0.0F, 14.0F);
		private final Anim.Value switchAnim = new Anim.Value(0.0F, 18.0F);
		private final Anim.Value switchHover = new Anim.Value(0.0F, 16.0F);
		private boolean rowsBuilt;
		private boolean expandedBefore;

		private ModuleCard(ChaosClickGui gui, Module module, String badge) {
			this.gui = gui;
			this.module = module;
			this.badge = badge;
			this.setTooltip("§f" + module.description() + "\n§7" + module.category().displayName()
					+ (keybindHint() == null ? "" : "  ·  §fkey " + keybindHint()));
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
			return total + 12.0F;
		}

		private void buildRows(float rowWidth) {
			rows.clear();
			clearChildren();
			float offset = 0.0F;
			for (Setting<?> setting : module.settings()) {
				if (setting == module.enabled()) {
					continue;
				}
				UiComponent row = UiWidgets.forSetting(setting, gui);
				if (row == null) {
					continue;
				}
				row.setBounds(0.0F, offset, rowWidth, UiWidgets.rowHeight(setting));
				row.setTooltip(setting.description
						+ (setting.description == null || setting.description.isEmpty() ? "" : "\n")
						+ "§7right-click to reset  ·  default " + setting.display());
				rows.add(row);
				addChild(row);
				offset += row.height() + 2.0F;
			}
			rowsBuilt = true;
		}

		private String keybindHint() {
			if (!UiTheme.get().keybindHints) {
				return null;
			}
			try {
				return switch (module.id()) {
					case "radial_menu" -> Keybinds.radialMenu.getTranslatedKeyMessage().getString();
					case "gui" -> Keybinds.openGui.getTranslatedKeyMessage().getString();
					case "zoom" -> Keybinds.zoom.getTranslatedKeyMessage().getString();
					case "perspective_lock" -> Keybinds.freeLook.getTranslatedKeyMessage().getString();
					case "container_search" -> Keybinds.searchContainer.getTranslatedKeyMessage().getString();
					case "chat_history" -> Keybinds.chatHistory.getTranslatedKeyMessage().getString();
					case "screenshots" -> Keybinds.screenshotPopup.getTranslatedKeyMessage().getString();
					case "gamma" -> Keybinds.toggleGamma.getTranslatedKeyMessage().getString();
					case "waypoint" -> Keybinds.addWaypoint.getTranslatedKeyMessage().getString();
					default -> null;
				};
			} catch (Throwable ignored) {
				return null;
			}
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			boolean isExpanded = expanded();
			if (isExpanded && !rowsBuilt) {
				buildRows(width - 28.0F);
			}
			if (isExpanded) {
				expandedBefore = true;
			}
			expand.set(isExpanded ? 1.0F : 0.0F);
			expand.update(deltaSeconds, UiTheme.get().speed(14.0F));
			switchAnim.set(module.isEnabled() ? 1.0F : 0.0F);
			switchAnim.update(deltaSeconds, UiTheme.get().speed(18.0F));
			float extra = expandedBefore ? rowsHeight() * Anim.easeOutQuint(expand.get()) : 0.0F;
			height = headerHeight() + extra;
			super.update(deltaSeconds, mouseX, mouseY);
			switchHover.set(isOverSwitch(mouseX, mouseY) ? 1.0F : 0.0F);
			switchHover.update(deltaSeconds, UiTheme.get().speed(16.0F));
			float rowY = headerHeight() + 8.0F;
			for (UiComponent row : rows) {
				row.setBounds(x + 14.0F, y + rowY, width - 28.0F, row.height());
				row.setLayerAlpha(layerAlpha * Anim.clamp01(expand.get()));
				row.setVisible(expand.get() > 0.6F);
				if (row.isVisible()) {
					row.update(deltaSeconds, mouseX, mouseY);
				}
				rowY += row.height() + 2.0F;
			}
		}

		private float switchX() {
			return right() - 16.0F - 30.0F;
		}

		private boolean isOverSwitch(float mouseX, float mouseY) {
			return mouseX >= switchX() - 2.0F && mouseX <= switchX() + 32.0F && mouseY >= y && mouseY <= y + headerHeight();
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
			float hoverAmount = hover.get();
			float onAmount = switchAnim.get();

			graphics.pose().pushMatrix();
			graphics.pose().translate(0.0F, offset);

			Ui.card(graphics, x, y, width, height, theme.radiusCard, theme, hoverAmount, on && onAmount > 0.5F, alpha);

			if (on) {
				// accent rail on the left edge of an active card
				Render.roundedRect(graphics, x, y + 9.0F, 2.0F, headerHeight() - 18.0F, 1.0F,
						Render.alpha(accent, alpha * onAmount));
			}

			float textX = x + 14.0F;
			float textRight = switchX() - 34.0F;
			if (badge != null) {
				int badgeWidth = UiFonts.width(badge, false) + 12;
				Render.roundedRect(graphics, textX, y + 8.0F, badgeWidth, 12.0F, 6.0F,
						Render.alpha(accent, 0.16F * alpha));
				Render.text(graphics, UiFonts.font(), badge, textX + 6.0F, y + 10.0F, Render.alpha(accent, alpha), false);
				textX += badgeWidth + 6.0F;
			}
			Render.boldText(graphics, UiFonts.bold(),
					Render.ellipsize(UiFonts.bold(), module.name(), textRight - textX),
					textX, y + 8.0F, Render.alpha(on ? theme.text : Render.mix(theme.text, theme.textDim, 0.35F), alpha), false);

			Render.text(graphics, UiFonts.font(),
					Render.ellipsize(UiFonts.font(), module.description(), width - (textX - x) - 60.0F),
					textX, y + headerHeight() - 16.0F,
					Render.alpha(Render.mix(theme.textFaint, theme.textDim, hoverAmount * 0.9F), alpha), false);

			String keybind = keybindHint();
			if (keybind != null && !keybind.isEmpty()) {
				int chipWidth = UiFonts.width(keybind, false) + 12;
				Render.roundedRect(graphics, textRight - chipWidth, y + 8.0F, chipWidth, 13.0F, 6.5F,
						Render.alpha(0xFFFFFFFF, 0.06F * alpha));
				Render.text(graphics, UiFonts.font(), keybind, textRight - chipWidth + 6.0F, y + 10.5F,
						Render.alpha(theme.textFaint, alpha), false);
			}

			// expander
			float chevronX = switchX() - 18.0F;
			UiIcons icon = expand.get() > 0.5F ? UiIcons.CHEVRON_DOWN : UiIcons.CHEVRON;
			icon.drawCentered(graphics, chevronX, y + headerHeight() * 0.5F, 11.0F,
					Render.alpha(Render.mix(theme.textFaint, theme.text, hoverAmount), alpha));

			// switch
			float switchY = y + (headerHeight() - 16.0F) * 0.5F;
			Ui.pill(graphics, switchX(), switchY, 30.0F, 16.0F, onAmount, on,
					Render.mix(accent, 0xFFFFFFFF, switchHover.get() * 0.15F),
					Render.mix(theme.track, theme.trackHover, switchHover.get() * 0.6F),
					Render.mix(0xFF8089A0, 0xFFFFFFFF, onAmount), alpha);

			// expanded settings
			if (expandedBefore && expand.get() > 0.02F) {
				Render.scissor(graphics, x + 1.0F, y + headerHeight(), width - 2.0F, height - headerHeight());
				Ui.divider(graphics, x + 14.0F, y + headerHeight(), width - 28.0F, theme, alpha * expand.get());
				renderChildren(graphics, mouseX, mouseY, deltaSeconds);
				Render.unscissor(graphics);
			}

			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (expandedBefore && expand.get() > 0.6F && mouseY > y + headerHeight()) {
				for (UiComponent row : rows) {
					if (row.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
				return true;
			}
			if (button != 0) {
				return false;
			}
			if (isOverSwitch(mouseX, mouseY)) {
				module.enabled().set(!module.isEnabled());
				gui.playClick(module.isEnabled());
				gui.refresh();
				return true;
			}
			gui.expanded.put(module.id(), !expanded());
			gui.playClick(true);
			return true;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (!expandedBefore) {
				return false;
			}
			boolean handled = false;
			for (UiComponent row : rows) {
				if (row.mouseReleased(mouseX, mouseY, button)) {
					handled = true;
				}
			}
			return handled;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (!expandedBefore) {
				return false;
			}
			for (UiComponent row : rows) {
				if (row.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (!expandedBefore) {
				return false;
			}
			for (UiComponent row : rows) {
				if (row.mouseScrolled(mouseX, mouseY, amount)) {
					return true;
				}
			}
			return false;
		}
	}

	/** Module list for a query, used by the search field of the interface. */
	public static List<Module> searchResults(String query) {
		return ModuleManager.search(query);
	}
}
