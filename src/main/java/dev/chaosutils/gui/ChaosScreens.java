package dev.chaosutils.gui;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Setting;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The secondary ChaosUtils windows.
 *
 * <p>All four share the same shell ({@link ChaosScreen}: floating, draggable, animated) and only
 * differ in their content, so the interface feels like one product instead of a pile of screens.
 * Every window opens on top of the previous one and returns there when it is closed.
 */
public final class ChaosScreens {
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
			.withZone(ZoneId.systemDefault());

	private ChaosScreens() {
	}

	// ============================================================ radial editor

	/** Create, edit, reorder and preview the radial menu entries. */
	public static final class RadialEditorScreen extends ChaosScreen {
		private int selected = -1;
		private UiWidgets.ScrollList list;

		public RadialEditorScreen(Screen parent) {
			super(parent, Component.literal("Radial Menu"), "window.radial", 720.0F, 430.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			if (selected >= entries.size()) {
				selected = entries.isEmpty() ? -1 : entries.size() - 1;
			}
			float contentX = window().x() + 18.0F;
			float top = window().y() + window().titleHeight() + 14.0F;
			float listWidth = 220.0F;

			UiWidgets.Label entriesLabel = new UiWidgets.Label("Slices", theme.textFaint);
			entriesLabel.setBounds(contentX, top, listWidth, 10.0F);
			add(entriesLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(4.0F);
			list.setBounds(contentX, top + 14.0F, listWidth, Math.max(80.0F, window().height() - window().titleHeight() - 62.0F));
			list.snapAppear();
			add(list);
			for (int i = 0; i < entries.size(); i++) {
				EntryRow row = new EntryRow(this, entries.get(i), i);
				row.setBounds(0.0F, 0.0F, listWidth, 30.0F);
				list.addItem(row);
			}
			list.layout();

			// ---- editor of the selected slice
			float editorX = contentX + listWidth + 20.0F;
			float editorWidth = Math.max(180.0F, window().right() - 18.0F - editorX);
			RadialElement element = current();
			if (element == null) {
				UiWidgets.Label empty = new UiWidgets.Label("Select a slice on the left, or add a new one.",
						theme.textDim);
				empty.setBounds(editorX, top + 20.0F, editorWidth, 12.0F);
				add(empty);
			} else {
				float previewHeight = 150.0F;
				RingPreview preview = new RingPreview();
				preview.setBounds(editorX, top, editorWidth, previewHeight);
				add(preview);

				float rowY = top + previewHeight + 6.0F;
				add(fieldRow(element, "Name", element.name, editorX, rowY, editorWidth, () ->
						openTextModal("Display name", element.name, 48, value -> {
							element.name = value;
							ChaosConfig.markDirty();
							refresh();
						})));
				rowY += 24.0F;
				add(fieldRow(element, "Action", element.command, editorX, rowY, editorWidth, () ->
						openTextModal(element.type == RadialElement.ActionType.COPY_TEXT
								? "Text to copy" : "Command or chat text", element.command, 256, value -> {
							element.command = value;
							ChaosConfig.markDirty();
							refresh();
						})));
				rowY += 24.0F;

				UiWidgets.Button type = new UiWidgets.Button(element.type.label(),
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () -> {
							RadialElement.ActionType[] types = RadialElement.ActionType.values();
							element.type = types[(element.type.ordinal() + 1) % types.length];
							ChaosConfig.markDirty();
							refresh();
						});
				type.setLeftAligned(true);
				type.setBounds(editorX, rowY, editorWidth, 18.0F);
				type.setTooltip("Click to switch what this slice does.");
				add(type);
				rowY += 24.0F;

				UiWidgets.Button icon = new UiWidgets.Button("Icon: " + element.icon,
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () ->
						openTextModal("Item id", element.icon, 64, value -> {
							element.icon = value;
							ChaosConfig.markDirty();
							refresh();
						}));
				icon.setLeftAligned(true);
				icon.setIcon(element.iconStack());
				icon.setBounds(editorX, rowY, editorWidth, 18.0F);
				icon.setTooltip("Any item id, for example minecraft:compass.");
				add(icon);
				rowY += 24.0F;

				UiWidgets.Button colour = new UiWidgets.Button("Colour  " + String.format("#%06X", element.color & 0xFFFFFF),
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () -> {
							Setting.Color temporary = new Setting.Color("radial_colour", "Slice colour",
									"Colour of this radial slice.", element.color);
							temporary.onChanged(value -> {
								element.color = value;
								ChaosConfig.markDirty();
							});
							openColorModal(temporary);
						});
				colour.setLeftAligned(true);
				colour.setBounds(editorX, rowY, editorWidth, 18.0F);
				add(colour);
				rowY += 22.0F;

				UiWidgets.Toggle enabled = new UiWidgets.Toggle(() -> element.enabled, value -> {
					element.enabled = value;
					ChaosConfig.markDirty();
					refresh();
				}, theme.accent);
				enabled.setBounds(editorX + editorWidth - 34.0F, rowY, 30.0F, 16.0F);
				enabled.setTooltip("Hide a slice without deleting it.");
				add(enabled);
				UiWidgets.Label enabledLabel = new UiWidgets.Label("Visible in the menu", theme.textDim);
				enabledLabel.setBounds(editorX, rowY, editorWidth - 40.0F, 12.0F);
				add(enabledLabel);
			}

			// ---- bottom bar
			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button add = new UiWidgets.Button("Add slice", UiWidgets.Button.Variant.PRIMARY, theme.accent, () -> {
				ChaosConfig.RADIAL_ELEMENTS.add(new RadialElement("New action", "/spawn", theme.accent,
						"minecraft:compass", RadialElement.ActionType.COMMAND));
				ChaosConfig.markDirty();
				selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
				refresh();
				toast("Slice added");
			});
			add.setBounds(contentX, bottom, 90.0F, 20.0F);
			add(add);

			UiWidgets.Button duplicate = new UiWidgets.Button("Duplicate", UiWidgets.Button.Variant.SOFT, theme.accent, () -> {
				RadialElement slice = current();
				if (slice != null) {
					ChaosConfig.RADIAL_ELEMENTS.add(slice.copy());
					ChaosConfig.markDirty();
					selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
					refresh();
				}
			});
			duplicate.setBounds(contentX + 96.0F, bottom, 82.0F, 20.0F);
			add(duplicate);

			UiWidgets.Button delete = new UiWidgets.Button("Delete", UiWidgets.Button.Variant.DANGER, theme.negative, () -> {
				RadialElement slice = current();
				if (slice != null) {
					openConfirm("Delete slice", "Remove \"" + slice.name + "\" from the radial menu?", "Delete", () -> {
						ChaosConfig.RADIAL_ELEMENTS.remove(slice);
						ChaosConfig.markDirty();
						selected = Math.max(0, Math.min(selected, ChaosConfig.RADIAL_ELEMENTS.size() - 1));
						refresh();
					});
				}
			});
			delete.setBounds(contentX + 184.0F, bottom, 74.0F, 20.0F);
			add(delete);
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Radial Menu", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = ChaosConfig.RADIAL_ELEMENTS.size() + " slices  ·  hold " + radialKeyName() + " in game";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		/** The key the player bound to the radial menu, as a readable name. */
		private static String radialKeyName() {
			try {
				return Keybinds.radialMenu.getTranslatedKeyMessage().getString();
			} catch (Throwable ignored) {
				return "the radial key";
			}
		}

		private RadialElement current() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
		}

		private UiComponent fieldRow(RadialElement element, String label, String value, float x, float y, float width,
				Runnable onEdit) {
			UiWidgets.Button button = new UiWidgets.Button(label + ":  " + Render.ellipsize(UiWidgets.font(), value, width - 90.0F),
					UiWidgets.Button.Variant.SOFT, element.solidColor(), onEdit);
			button.setLeftAligned(true);
			button.setBounds(x, y, width, 18.0F);
			return button;
		}

		private void move(int index, int direction) {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			int target = index + direction;
			if (index < 0 || index >= entries.size() || target < 0 || target >= entries.size()) {
				return;
			}
			RadialElement element = entries.get(index);
			entries.set(index, entries.get(target));
			entries.set(target, element);
			selected = target;
			ChaosConfig.markDirty();
			refresh();
		}

		/** One row in the slice list: colour dot, name, action type and reorder buttons. */
		private static final class EntryRow extends UiComponent {
			private final RadialEditorScreen screen;
			private final RadialElement element;
			private final int index;
			private final UiWidgets.IconButton up;
			private final UiWidgets.IconButton down;

			private EntryRow(RadialEditorScreen screen, RadialElement element, int index) {
				this.screen = screen;
				this.element = element;
				this.index = index;
				this.up = new UiWidgets.IconButton(
						(graphics, cx, cy, alpha) -> Ui.chevron(graphics, cx, cy, 5.0F, -90.0F,
								Render.alpha(0xFFFFFF, alpha)), 0xFFFFFFFF, () -> screen.move(index, -1));
				this.down = new UiWidgets.IconButton(
						(graphics, cx, cy, alpha) -> Ui.chevron(graphics, cx, cy, 5.0F, 90.0F,
								Render.alpha(0xFFFFFF, alpha)), 0xFFFFFFFF, () -> screen.move(index, 1));
				this.setTooltip(element.type.label());
				this.setAppearDelay(Math.min(0.2F, 0.02F * index));
			}

			@Override
			public void update(float deltaSeconds, float mouseX, float mouseY) {
				super.update(deltaSeconds, mouseX, mouseY);
				up.setBounds(right() - 42.0F, y + (height - 14.0F) * 0.5F, 14.0F, 14.0F);
				down.setBounds(right() - 24.0F, y + (height - 14.0F) * 0.5F, 14.0F, 14.0F);
				up.setLayerAlpha(layerAlpha);
				down.setLayerAlpha(layerAlpha);
				up.update(deltaSeconds, mouseX, mouseY);
				down.update(deltaSeconds, mouseX, mouseY);
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				boolean active = screen.selected == index;
				Render.roundedRect(graphics, x, y + offset, width, height, 8.0F,
						Render.mix(Render.alpha(0xFFFFFF, active ? 0.07F : 0.03F * alpha),
								Render.alpha(0xFFFFFF, 0.08F * alpha), hover.get()));
				if (active) {
					Render.ring(graphics, x, y + offset, width, height, 8.0F, 1.0F,
							Render.alpha(element.solidColor(), alpha * 0.55F));
				}
				Ui.dot(graphics, x + 10.0F, y + offset + 11.0F, 3.5F,
						Render.alpha(element.enabled ? element.solidColor() : theme.textFaint, alpha));
				Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), element.name, width - 60.0F),
						x + 18.0F, y + offset + 4.0F, Render.alpha(theme.text, alpha), false);
				Render.text(graphics, UiWidgets.font(), element.type.label(), x + 18.0F, y + offset + 15.0F,
						Render.alpha(theme.textFaint, alpha), false);
				up.render(graphics, mouseX, mouseY, deltaSeconds);
				down.render(graphics, mouseX, mouseY, deltaSeconds);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (!isHovered(mouseX, mouseY)) {
					return false;
				}
				if (up.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
				if (down.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
				if (button == 0) {
					screen.selected = index;
					screen.playClick(true);
					screen.refresh();
					return true;
				}
				return false;
			}
		}

		/** Live preview of the ring with the real geometry of the menu. */
		private final class RingPreview extends UiComponent {
			private final Anim.Value spin = new Anim.Value(0.0F, 0.4F);

			@Override
			public void update(float deltaSeconds, float mouseX, float mouseY) {
				super.update(deltaSeconds, mouseX, mouseY);
				spin.set(spin.target() + deltaSeconds * 4.0F);
				spin.update(UiTheme.get().speed(0.6F));
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
				float cx = centerX();
				float cy = centerY();
				float outer = Math.min(width, height) * 0.42F;
				float inner = outer * 0.42F;
				Render.roundedRect(graphics, x, y, width, height, UiTheme.get().radiusCard,
						Render.alpha(0x000000, 0.20F * alpha));
				UiTheme theme = UiTheme.get();
				if (entries.isEmpty()) {
					Render.centeredText(graphics, UiWidgets.font(), "No slices yet", cx, cy - 4.0F,
							Render.alpha(theme.textFaint, alpha), false);
					return;
				}
				float span = 360.0F / entries.size();
				float gap = Math.min(5.0F, span * 0.2F);
				for (int i = 0; i < entries.size(); i++) {
					RadialElement element = entries.get(i);
					float start = i * span - span * 0.5F + gap;
					float end = (i + 1) * span - span * 0.5F - gap;
					boolean active = selected == i;
					Render.arc(graphics, cx, cy, inner, active ? outer + 4.0F : outer, start, end,
							Render.alpha(element.solidColor(), (element.enabled ? 0.85F : 0.30F) * alpha));
					double mid = Math.toRadians((start + end) * 0.5);
					float radius = inner + (outer - inner) * 0.55F;
					float iconX = cx + (float) Math.sin(mid) * radius;
					float iconY = cy - (float) Math.cos(mid) * radius;
					Ui.itemIcon(graphics, element.iconStack(), iconX, iconY, 0.75F, alpha);
				}
				Render.circle(graphics, cx, cy, inner - 5.0F, Render.alpha(0x0C0D14, 0.8F * alpha));
				Render.ring(graphics, cx - inner + 5.0F, cy - inner + 5.0F, (inner - 5.0F) * 2.0F, (inner - 5.0F) * 2.0F,
						inner - 5.0F, 1.2F, Render.alpha(0x66FFFFFF, alpha * 0.6F));
				Render.centeredText(graphics, UiWidgets.font(), "preview", cx, cy - 4.0F,
						Render.alpha(theme.textFaint, alpha * 0.8F), false);
			}
		}
	}

	// =============================================================== chat log

	/** Searchable history of everything that happened in chat. */
	public static final class ChatHistoryScreen extends ChaosScreen {
		private String query = "";
		private int kindFilter = 0;
		private UiWidgets.ScrollList list;
		private UiWidgets.Label countLabel;
		private UiWidgets.SearchField search;

		public ChatHistoryScreen(Screen parent) {
			super(parent, Component.literal("Chat History"), "window.chat", 640.0F, 420.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			EditBox box = new EditBox(UiWidgets.font(), 0, 0, 200, 14, Component.literal("Search"));
			box.setBordered(false);
			box.setTextColor(0xFFF4F5FA);
			box.setHint(Component.literal("Search chat…"));
			box.setMaxLength(64);
			box.setValue(query);
			box.setResponder(value -> {
				if (!value.equals(query)) {
					query = value;
					rebuild();
				}
			});
			addInput(box);
			search = new UiWidgets.SearchField(box);
			search.place(contentX, top, Math.min(240.0F, contentWidth * 0.45F), 22.0F);
			search.setOnClear(() -> {
				query = "";
				rebuild();
			});
			add(search);

			String[] filters = {"All", "Chat", "Server", "System"};
			float chipX = search.right() + 10.0F;
			for (int i = 0; i < filters.length; i++) {
				int index = i;
				int width = UiWidgets.font().width(filters[i]) + 18;
				UiWidgets.Button chip = new UiWidgets.Button(filters[i],
						kindFilter == index ? UiWidgets.Button.Variant.PRIMARY : UiWidgets.Button.Variant.GHOST,
						theme.accent, () -> {
							kindFilter = index;
							rebuild();
						});
				chip.setBounds(chipX, top, width, 22.0F);
				add(chip);
				chipX += width + 6.0F;
			}

			countLabel = new UiWidgets.Label("", theme.textFaint);
			countLabel.setBounds(chipX + 6.0F, top + 7.0F, Math.max(10.0F, window().right() - 18.0F - chipX), 12.0F);
			add(countLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(2.0F);
			list.setBounds(contentX, top + 30.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 76.0F));
			list.snapAppear();
			add(list);
			rebuildList();

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button copyAll = new UiWidgets.Button("Copy everything", UiWidgets.Button.Variant.SOFT, theme.accent, () -> {
				this.minecraft.keyboardHandler.setClipboard(allText());
				toast("Copied", entries().size() + " lines", theme.accent);
			});
			copyAll.setBounds(contentX, bottom, 110.0F, 20.0F);
			add(copyAll);

			UiWidgets.Button clear = new UiWidgets.Button("Clear", UiWidgets.Button.Variant.DANGER, theme.negative, () ->
					openConfirm("Clear chat history", "Delete every stored chat line? This cannot be undone.",
							"Clear", () -> {
								ChatLog.clear();
								rebuild();
								toast("Chat history cleared");
							}));
			clear.setBounds(contentX + 116.0F, bottom, 70.0F, 20.0F);
			add(clear);
		}

		private ChatLog.Kind filter() {
			return switch (kindFilter) {
				case 1 -> ChatLog.Kind.PLAYER;
				case 2 -> ChatLog.Kind.SERVER;
				case 3 -> ChatLog.Kind.SYSTEM;
				default -> null;
			};
		}

		private List<ChatLog.Entry> entries() {
			return ChatLog.search(query, filter(), 200);
		}

		private void rebuild() {
			if (list == null) {
				return;
			}
			rebuildList();
		}

		private void rebuildList() {
			list.clearItems();
			for (ChatLog.Entry entry : entries()) {
				list.addItem(new ChatRow(entry));
			}
			list.layout();
			if (countLabel != null) {
				countLabel.setText(entries().size() + " lines");
			}
		}

		private String allText() {
			StringBuilder builder = new StringBuilder();
			for (ChatLog.Entry entry : entries()) {
				builder.append('[').append(TIME.format(entry.time())).append("] ").append(entry.plain()).append('\n');
			}
			return builder.toString();
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Chat History", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "survives reconnects  ·  stored locally";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		/** One chat line with kind badge, timestamp and a copy action. */
		private final class ChatRow extends UiComponent {
			private final ChatLog.Entry entry;

			private ChatRow(ChatLog.Entry entry) {
				this.entry = entry;
				this.setTooltip("Click to copy this line");
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				int accent = switch (entry.kind()) {
					case PLAYER -> 0xFF57D98A;
					case SERVER -> 0xFFF2B23E;
					case SYSTEM -> 0xFF7C9CFF;
				};
				Render.roundedRect(graphics, x, y + offset, width, height, 6.0F,
						Render.mix(Render.alpha(0xFFFFFF, 0.0F), Render.alpha(0xFFFFFF, 0.055F * alpha), hover.get()));
				Render.text(graphics, UiWidgets.font(), TIME.format(entry.time()), x + 4.0F, y + offset + 3.0F,
						Render.alpha(theme.textFaint, alpha), false);
				Ui.dot(graphics, x + 52.0F, y + offset + 7.0F, 2.5F, Render.alpha(accent, alpha));
				Render.text(graphics, UiWidgets.font(),
						Render.ellipsize(UiWidgets.font(), entry.plain(), width - 68.0F), x + 60.0F, y + offset + 3.0F,
						Render.alpha(theme.text, alpha), false);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (isHovered(mouseX, mouseY) && button == 0) {
					Clipboard.copyText(entry.plain());
					playClick(true);
					toast("Copied", "chat line", theme().accent);
					return true;
				}
				return false;
			}
		}
	}

	// ============================================================ screenshots

	/** Local screenshot manager: browse, copy, crop. Nothing ever leaves the machine. */
	public static final class ScreenshotScreen extends ChaosScreen {
		private final List<Path> files = new ArrayList<>();
		private int selected;
		private UiWidgets.ScrollList list;
		private UiWidgets.Label info;

		public ScreenshotScreen(Screen parent) {
			super(parent, Component.literal("Screenshots"), "window.shots", 620.0F, 420.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			files.clear();
			files.addAll(ScreenshotManager.recent(50));

			list = new UiWidgets.ScrollList();
			list.setSpacing(2.0F);
			list.setBounds(contentX, top + 14.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 78.0F));
			list.snapAppear();
			add(list);
			for (int i = 0; i < files.size(); i++) {
				list.addItem(new ShotRow(i));
			}
			list.layout();

			if (selected >= files.size()) {
				selected = Math.max(0, files.size() - 1);
			}

			info = new UiWidgets.Label(currentName(), theme.textDim);
			info.setBounds(contentX, top, contentWidth, 12.0F);
			add(info);

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button copyImage = new UiWidgets.Button("Copy image", UiWidgets.Button.Variant.PRIMARY, theme.accent, () -> {
				Path path = current();
				if (path != null) {
					Clipboard.copyImage(path);
					toast("Screenshot copied to the clipboard");
				}
			});
			copyImage.setBounds(contentX, bottom, 96.0F, 20.0F);
			add(copyImage);

			UiWidgets.Button crop = new UiWidgets.Button("Crop & copy", UiWidgets.Button.Variant.SOFT, theme.warning, () -> {
				Path path = current();
				if (path != null) {
					ScreenshotManager.cropWithConfiguredPreset(path);
					toast("Cropped and copied");
				}
			});
			crop.setBounds(contentX + 102.0F, bottom, 96.0F, 20.0F);
			crop.setTooltip("Uses the crop preset from the Screenshot Manager settings.");
			add(crop);

			UiWidgets.Button copyPath = new UiWidgets.Button("Copy path", UiWidgets.Button.Variant.GHOST, theme.accent, () -> {
				Path path = current();
				if (path != null) {
					Clipboard.copyText(path.toAbsolutePath().toString());
					toast("Path copied");
				}
			});
			copyPath.setBounds(contentX + 204.0F, bottom, 84.0F, 20.0F);
			add(copyPath);

			UiWidgets.Button folder = new UiWidgets.Button("Open folder", UiWidgets.Button.Variant.GHOST, theme.positive, () ->
					Clipboard.openFile(ScreenshotManager.screenshotsDirectory()));
			folder.setBounds(contentX + 294.0F, bottom, 92.0F, 20.0F);
			add(folder);
		}

		private Path current() {
			return selected >= 0 && selected < files.size() ? files.get(selected) : null;
		}

		private String currentName() {
			Path path = current();
			return path == null ? "No screenshots found yet" : path.getFileName().toString();
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Screenshots", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "everything stays on this machine";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		private final class ShotRow extends UiComponent {
			private final int index;

			private ShotRow(int index) {
				this.index = index;
				this.setTooltip(files.get(index).toAbsolutePath().toString());
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				Path path = files.get(index);
				boolean active = selected == index;
				float offset = appearOffset();
				Render.roundedRect(graphics, x, y + offset, width, height, 6.0F,
						Render.mix(Render.alpha(0xFFFFFF, active ? 0.07F : 0.02F * alpha),
								Render.alpha(0xFFFFFF, 0.07F * alpha), hover.get()));
				String stamp = TIME.format(Instant.ofEpochMilli(path.toFile().lastModified()));
				Render.text(graphics, UiWidgets.font(), stamp, x + 4.0F, y + offset + 3.0F,
						Render.alpha(theme.textFaint, alpha), false);
				Render.text(graphics, UiWidgets.font(),
						Render.ellipsize(UiWidgets.font(), path.getFileName().toString(), width - 62.0F),
						x + 54.0F, y + offset + 3.0F,
						Render.alpha(active ? theme.text : theme.textDim, alpha), false);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (isHovered(mouseX, mouseY) && button == 0) {
					selected = index;
					if (info != null) {
						info.setText(currentName());
					}
					playClick(true);
					return true;
				}
				return false;
			}
		}
	}

	// ============================================================= waypoints

	/** Death markers and manual waypoints, client side only. */
	public static final class WaypointsScreen extends ChaosScreen {
		private final List<Waypoint> listed = new ArrayList<>();
		private UiWidgets.ScrollList list;
		private UiWidgets.Label countLabel;

		public WaypointsScreen(Screen parent) {
			super(parent, Component.literal("Waypoints"), "window.waypoints", 620.0F, 400.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			countLabel = new UiWidgets.Label("", theme.textFaint);
			countLabel.setBounds(contentX, top, contentWidth, 12.0F);
			add(countLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(3.0F);
			list.setBounds(contentX, top + 16.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 62.0F));
			list.snapAppear();
			add(list);
			rebuildList();

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button add = new UiWidgets.Button("Add current position", UiWidgets.Button.Variant.PRIMARY,
					theme.accent, () -> {
						if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) {
							return;
						}
						ChaosConfig.WAYPOINTS.add(new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
								this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(),
								this.minecraft.level.dimension().identifier().toString(), theme.accent, false, 0L));
						ChaosConfig.markDirty();
						rebuildList();
						toast("Waypoint added");
					});
			add.setBounds(contentX, bottom, 130.0F, 20.0F);
			add(add);

			UiWidgets.Button clearTemporary = new UiWidgets.Button("Clear temporary", UiWidgets.Button.Variant.GHOST,
					theme.warning, () -> {
						ChaosConfig.WAYPOINTS.removeIf(waypoint -> waypoint.temporary);
						ChaosConfig.markDirty();
						rebuildList();
						toast("Temporary markers cleared");
					});
			clearTemporary.setBounds(contentX + 136.0F, bottom, 112.0F, 20.0F);
			add(clearTemporary);
		}

		private void rebuildList() {
			list.clearItems();
			listed.clear();
			listed.addAll(ChaosConfig.WAYPOINTS);
			for (Waypoint waypoint : listed) {
				list.addItem(new WaypointRow(waypoint));
			}
			list.layout();
			if (countLabel != null) {
				countLabel.setText(listed.size() + " waypoints  ·  left click copies, right click deletes");
			}
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Waypoints", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "drawn client-side only";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		private final class WaypointRow extends UiComponent {
			private final Waypoint waypoint;

			private WaypointRow(Waypoint waypoint) {
				this.waypoint = waypoint;
				this.setTooltip(String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z)
						+ "\n§7" + waypoint.dimension);
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				Render.roundedRect(graphics, x, y + offset, width, height, 7.0F,
						Render.mix(Render.alpha(0xFFFFFF, 0.025F * alpha), Render.alpha(0xFFFFFF, 0.07F * alpha), hover.get()));
				Ui.dot(graphics, x + 10.0F, y + offset + height * 0.5F, 3.5F,
						Render.alpha(waypoint.color, alpha));
				Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), waypoint.name, 130.0F),
						x + 20.0F, y + offset + 5.0F, Render.alpha(theme.text, alpha), false);
				String dimension = waypoint.dimension.contains(":")
						? waypoint.dimension.substring(waypoint.dimension.indexOf(':') + 1) : waypoint.dimension;
				String coords = String.format(Locale.ROOT, "%s  ·  %.0f %.0f %.0f", dimension,
						waypoint.x, waypoint.y, waypoint.z);
				Render.text(graphics, UiWidgets.font(), coords, x + 160.0F, y + offset + 5.0F,
						Render.alpha(theme.textDim, alpha), false);
				if (waypoint.temporary) {
					Render.text(graphics, UiWidgets.font(), "temp", right() - 30.0F, y + offset + 5.0F,
							Render.alpha(theme.warning, alpha), false);
				}
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (!isHovered(mouseX, mouseY)) {
					return false;
				}
				if (button == 0) {
					Clipboard.copyText(String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z));
					toast("Coordinates copied");
					return true;
				}
				if (button == 1) {
					ChaosConfig.WAYPOINTS.remove(waypoint);
					ChaosConfig.markDirty();
					rebuildList();
					return true;
				}
				return false;
			}
		}
	}
}
