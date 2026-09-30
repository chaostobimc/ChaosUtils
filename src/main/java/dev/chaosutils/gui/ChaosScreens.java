package dev.chaosutils.gui;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The smaller ChaosUtils screens: radial editor, chat history, screenshots and waypoints, plus
 * the shared text input dialog they use.
 *
 * <p>All of them are built from the same component toolkit as the main interface, so styling,
 * animations and input handling stay consistent and there is exactly one place to change them.
 */
public final class ChaosScreens {
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
			.withZone(ZoneId.systemDefault());

	private ChaosScreens() {
	}

	/** A simple, modal text dialog with a vanilla text field so typing behaves as expected. */
	public static final class TextInputScreen extends Screen {
		private final Screen parent;
		private final String title;
		private final String initial;
		private final int maxLength;
		private final Consumer<String> onAccept;
		private EditBox field;
		private boolean enterHeld;

		public TextInputScreen(Screen parent, String title, String initial, int maxLength, Consumer<String> onAccept) {
			super(Component.literal(title));
			this.parent = parent;
			this.title = title;
			this.initial = initial == null ? "" : initial;
			this.maxLength = Math.max(1, maxLength);
			this.onAccept = onAccept;
		}

		@Override
		protected void init() {
			int boxWidth = Math.min(260, this.width - 60);
			int x = (this.width - boxWidth) / 2;
			int y = this.height / 2 - 8;
			field = new EditBox(this.font, x, y, boxWidth, 18, Component.literal(title));
			field.setValue(initial);
			field.setMaxLength(maxLength);
			field.setBordered(true);
			addRenderableWidget(field);
			setInitialFocus(field);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			renderBackground(graphics, mouseX, mouseY, partialTick);
			UiTheme theme = UiTheme.get();
			int boxWidth = Math.min(260, this.width - 60) + 16;
			int x = (this.width - boxWidth) / 2;
			int y = this.height / 2 - 30;
			Render.shadowedPanel(graphics, x, y, boxWidth, 68.0F, theme.radius, theme.panel, theme.accent);
			Render.centeredText(graphics, this.font, title, this.width * 0.5F, y + 8.0F, theme.text, false);
			Render.centeredText(graphics, this.font, "Enter to accept  ·  Escape to cancel", this.width * 0.5F, y + 52.0F,
					theme.textFaint, false);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		/**
		 * Enter is polled instead of overriding an input event method, which keeps this screen
		 * independent of the input event record accessors introduced in 1.21.9.
		 */
		@Override
		public void tick() {
			super.tick();
			boolean enter = dev.chaosutils.util.InputUtil.isPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
					|| dev.chaosutils.util.InputUtil.isPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER);
			if (enter && !enterHeld) {
				enterHeld = true;
				accept();
			} else if (!enter) {
				enterHeld = false;
			}
		}

		@Override
		public void onClose() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		private void accept() {
			if (onAccept != null) {
				onAccept.accept(field == null ? "" : field.getValue());
			}
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}
	}

	/** Base for the list-oriented screens: consistent frame, title and close button. */
	abstract static class ListScreen extends ChaosScreen {
		protected final Screen parent;
		private final String heading;

		protected ListScreen(Screen parent, String heading) {
			super(Component.literal("ChaosUtils " + heading));
			this.parent = parent;
			this.heading = heading;
		}

		protected abstract void buildRows(float top, float left, float width, float rowHeight);

		@Override
		protected void buildLayout() {
			UiTheme theme = theme();
			float left = 18.0F;
			float width = this.width - 36.0F;
			float top = 46.0F;
			float rowHeight = 20.0F;

			UiWidgets.Button close = new UiWidgets.Button(this, "Back", theme.accent, this::back);
			close.setBounds(this.width - 76.0F, this.height - 28.0F, 58.0F, 18.0F);
			add(close);

			buildRows(top, left, width, rowHeight);
		}

		protected void back() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}

		@Override
		public void onClose() {
			back();
		}

		@Override
		protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
			UiTheme theme = theme();
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme.background);
			Render.shadowedPanel(graphics, 12.0F, 12.0F, this.width - 24.0F, this.height - 24.0F, theme.radius,
					theme.panel, theme.accent);
			Render.text(graphics, font(), heading, 20.0F, 22.0F, theme.text, false);
			Render.text(graphics, font(), subtitle(), 20.0F, 32.0F, theme.textFaint, false);
		}

		protected String subtitle() {
			return "Changes are saved automatically.";
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}

	// ------------------------------------------------------------- radial editor

	public static final class RadialEditorScreen extends ChaosScreen {
		private final Screen parent;
		private int selected = -1;

		public RadialEditorScreen(Screen parent) {
			super(Component.literal("ChaosUtils Radial Editor"));
			this.parent = parent;
		}

		@Override
		protected void buildLayout() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			if (selected >= entries.size()) {
				selected = entries.isEmpty() ? -1 : entries.size() - 1;
			}
			UiTheme theme = theme();
			float listLeft = 18.0F;
			float listWidth = 190.0F;
			float rowHeight = 20.0F;
			float top = 48.0F;
			int visible = (int) ((this.height - 110.0F) / rowHeight);
			for (int i = 0; i < Math.min(visible, entries.size()); i++) {
				final int index = i;
				RadialElement element = entries.get(i);
				UiWidgets.Button row = new UiWidgets.Button(this, element.name + "  §7" + element.type.label().split(" ")[0],
						i == selected ? theme.accent : 0xFF3A3A48, () -> {
							selected = index;
							refresh();
						});
				row.setBounds(listLeft, top + i * rowHeight, listWidth, rowHeight - 3.0F);
				add(row);
			}

			float buttonsY = this.height - 58.0F;
			UiWidgets.Button add = new UiWidgets.Button(this, "Add", theme.positive, () -> {
				ChaosConfig.RADIAL_ELEMENTS.add(new RadialElement("New Action", "/spawn", theme.accent,
						"minecraft:compass", RadialElement.ActionType.COMMAND));
				ChaosConfig.markDirty();
				selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
				refresh();
			});
			add.setBounds(listLeft, buttonsY, 60.0F, 18.0F);
			add(add);

			UiWidgets.Button duplicate = new UiWidgets.Button(this, "Copy", theme.accent, () -> {
				RadialElement element = current();
				if (element != null) {
					ChaosConfig.RADIAL_ELEMENTS.add(element.copy());
					ChaosConfig.markDirty();
					refresh();
				}
			});
			duplicate.setBounds(listLeft + 64.0F, buttonsY, 60.0F, 18.0F);
			add(duplicate);

			UiWidgets.Button delete = new UiWidgets.Button(this, "Delete", theme.negative, () -> {
				RadialElement element = current();
				if (element != null) {
					ChaosConfig.RADIAL_ELEMENTS.remove(element);
					ChaosConfig.markDirty();
					selected = Math.max(-1, Math.min(selected, ChaosConfig.RADIAL_ELEMENTS.size() - 1));
					refresh();
				}
			});
			delete.setBounds(listLeft + 128.0F, buttonsY, 60.0F, 18.0F);
			add(delete);

			UiWidgets.Button moveUp = new UiWidgets.Button(this, "↑", 0xFF3A3A48, () -> move(-1));
			moveUp.setBounds(listLeft, buttonsY - 22.0F, 28.0F, 18.0F);
			add(moveUp);

			UiWidgets.Button moveDown = new UiWidgets.Button(this, "↓", 0xFF3A3A48, () -> move(1));
			moveDown.setBounds(listLeft + 32.0F, buttonsY - 22.0F, 28.0F, 18.0F);
			add(moveDown);

			UiWidgets.Button back = new UiWidgets.Button(this, "Back", theme.accent, () -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(parent);
				}
			});
			back.setBounds(this.width - 76.0F, this.height - 28.0F, 58.0F, 18.0F);
			add(back);

			buildEditor(listLeft + listWidth + 14.0F, 48.0F, this.width - listLeft - listWidth - 32.0F);
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new RadialEditorScreen(parent));
			}
		}

		private void move(int direction) {
			RadialElement element = current();
			if (element == null) {
				return;
			}
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			int index = entries.indexOf(element);
			int target = index + direction;
			if (target < 0 || target >= entries.size()) {
				return;
			}
			entries.set(index, entries.get(target));
			entries.set(target, element);
			selected = target;
			ChaosConfig.markDirty();
			refresh();
		}

		private RadialElement current() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
		}

		private void buildEditor(float left, float top, float width) {
			RadialElement element = current();
			float rowHeight = 22.0F;
			UiTheme theme = theme();
			if (element == null) {
				return;
			}
			int row = 0;
			add(valueRow(left, top + row++ * rowHeight, width, "Name", element.name, theme.accent, () ->
					openText("Display name", element.name, 48, value -> {
						element.name = value;
						ChaosConfig.markDirty();
						refresh();
					}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Command", element.command, theme.accent, () ->
					openText(element.type == RadialElement.ActionType.COPY_TEXT ? "Text to copy" : "Command or text",
							element.command, 256, value -> {
								element.command = value;
								ChaosConfig.markDirty();
								refresh();
							}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Action", element.type.label(), theme.accent,
					() -> {
						RadialElement.ActionType[] types = RadialElement.ActionType.values();
						element.type = types[(element.type.ordinal() + 1) % types.length];
						ChaosConfig.markDirty();
						refresh();
					}, null));
			add(valueRow(left, top + row++ * rowHeight, width, "Icon", element.icon, theme.accent, () ->
					openText("Item id", element.icon, 64, value -> {
						element.icon = value;
						ChaosConfig.markDirty();
						refresh();
					}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Colour", String.format("#%06X", element.color & 0xFFFFFF),
					element.color, () -> {
						int[] palette = {0xFFFF8A65, 0xFF7C5CFF, 0xFF4FC3F7, 0xFF81C784, 0xFFF0B429, 0xFFF06292,
								0xFF64B5F6, 0xFF4DB6AC};
						int index = 0;
						for (int i = 0; i < palette.length; i++) {
							if (palette[i] == element.color) {
								index = (i + 1) % palette.length;
								break;
							}
						}
						element.color = palette[index];
						ChaosConfig.markDirty();
						refresh();
					}, () -> openPicker(element)));
			add(valueRow(left, top + row++ * rowHeight, width, "Enabled", element.enabled ? "yes" : "no",
					element.enabled ? theme.positive : theme.negative, () -> {
						element.enabled = !element.enabled;
						ChaosConfig.markDirty();
						refresh();
					}, null));
			add(valueRow(left, top + row++ * rowHeight, width, "Slice", (selected + 1) + " of "
					+ ChaosConfig.RADIAL_ELEMENTS.size(), theme.textFaint, null, null));
		}

		private UiComponent valueRow(float x, float y, float width, String label, String value, int accent,
				Runnable onLeft, Runnable onRight) {
			ValueRow row = new ValueRow(label, value, accent, onLeft, onRight);
			row.setBounds(x, y, width, 18.0F);
			return row;
		}

		private void openText(String title, String initial, int maxLength, Consumer<String> onAccept) {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TextInputScreen(this, title, initial, maxLength, onAccept));
			}
		}

		private void openPicker(RadialElement element) {
			dev.chaosutils.config.Setting.Color temporary =
					new dev.chaosutils.config.Setting.Color("radial_colour", "Slice colour", "", element.color);
			temporary.onChanged(value -> {
				element.color = value;
				ChaosConfig.markDirty();
			});
			openColorPicker(temporary);
		}

		@Override
		protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
			UiTheme theme = theme();
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme.background);
			Render.shadowedPanel(graphics, 12.0F, 12.0F, this.width - 24.0F, this.height - 24.0F, theme.radius,
					theme.panel, theme.accent);
			Render.text(graphics, font(), "Radial Menu", 20.0F, 22.0F, theme.text, false);
			Render.text(graphics, font(), "Click a slice to edit it · " + ChaosConfig.RADIAL_ELEMENTS.size() + " entries",
					20.0F, 32.0F, theme.textFaint, false);
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}

	// ------------------------------------------------------------- chat history

	public static final class ChatHistoryScreen extends ListScreen {
		private String query = "";
		private EditBox search;
		private final List<ChatLog.Entry> visible = new ArrayList<>();

		public ChatHistoryScreen(Screen parent) {
			super(parent, "Chat History");
		}

		@Override
		protected String subtitle() {
			return ChatLog.recent(400).size() + " buffered lines · click a line to copy it";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			refreshEntries();
			int visibleRows = (int) ((this.height - top - 70.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, visible.size()); i++) {
				ChatLog.Entry entry = visible.get(i);
				String text = "§7[" + TIME.format(entry.time()) + "] §r" + entry.plain();
				ChatLog.Entry captured = entry;
				UiWidgets.Button row = new UiWidgets.Button(this, trim(text, width - 20.0F), 0xFF3A3A48,
						() -> Clipboard.copyText(captured.plain()));
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}
			search = new EditBox(this.font, Math.round(left), Math.round(top - 24.0F), Math.round(Math.min(220.0F, width)),
					16, Component.literal("Search"));
			search.setValue(query);
			search.setResponder(value -> {
				query = value;
				refresh();
			});
			addInput(search);

			UiWidgets.Button copyAll = new UiWidgets.Button(this, "Copy all", theme.accent, () -> {
				StringBuilder builder = new StringBuilder();
				for (ChatLog.Entry entry : visible) {
					builder.append('[').append(TIME.format(entry.time())).append("] ").append(entry.plain()).append('\n');
				}
				Clipboard.copyText(builder.toString());
			});
			copyAll.setBounds(left, this.height - 28.0F, 70.0F, 18.0F);
			add(copyAll);

			UiWidgets.Button clear = new UiWidgets.Button(this, "Clear", theme.negative, () -> {
				ChatLog.clear();
				refresh();
			});
			clear.setBounds(left + 76.0F, this.height - 28.0F, 60.0F, 18.0F);
			add(clear);
		}

		private void refreshEntries() {
			visible.clear();
			visible.addAll(query == null || query.isBlank() ? ChatLog.recent(200) : ChatLog.search(query, null, 200));
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChatHistoryScreen(parent));
			}
		}

		private String trim(String value, float maxWidth) {
			String result = value;
			while (font().width(result) > maxWidth && result.length() > 6) {
				result = result.substring(0, result.length() - 2) + "…";
			}
			return result;
		}
	}

	// --------------------------------------------------------------- screenshots

	public static final class ScreenshotScreen extends ListScreen {
		private int selected;
		private final List<Path> files = new ArrayList<>();

		public ScreenshotScreen(Screen parent) {
			super(parent, "Screenshots");
		}

		@Override
		protected String subtitle() {
			return "Everything happens locally - nothing is uploaded.";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			files.clear();
			files.addAll(ScreenshotManager.recent(16));
			int visibleRows = (int) ((this.height - top - 90.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, files.size()); i++) {
				final int index = i;
				Path path = files.get(i);
				String name = path.getFileName().toString();
				String stamp = TIME.format(Instant.ofEpochMilli(path.toFile().lastModified()));
				String label = (i == selected ? "§f" : "§7") + stamp + "  §r" + name;
				UiWidgets.Button row = new UiWidgets.Button(this, label, i == selected ? theme.accent : 0xFF3A3A48,
						() -> {
							selected = index;
							refresh();
						});
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}

			float buttonsY = this.height - 28.0F;
			UiWidgets.Button copyImage = new UiWidgets.Button(this, "Copy image", theme.accent, () -> {
				Path path = currentPath();
				if (path != null) {
					Clipboard.copyImage(path);
				}
			});
			copyImage.setBounds(left, buttonsY, 78.0F, 18.0F);
			add(copyImage);

			UiWidgets.Button copyPath = new UiWidgets.Button(this, "Copy path", 0xFF4FC3F7, () -> {
				Path path = currentPath();
				if (path != null) {
					Clipboard.copyText(path.toAbsolutePath().toString());
				}
			});
			copyPath.setBounds(left + 84.0F, buttonsY, 74.0F, 18.0F);
			add(copyPath);

			UiWidgets.Button crop = new UiWidgets.Button(this, "Crop & copy", theme.warning, () -> {
				Path path = currentPath();
				if (path != null) {
					ScreenshotManager.cropWithConfiguredPreset(path);
				}
			});
			crop.setBounds(left + 164.0F, buttonsY, 86.0F, 18.0F);
			add(crop);

			UiWidgets.Button openFolder = new UiWidgets.Button(this, "Open folder", 0xFF81C784, () ->
					Clipboard.openFile(ScreenshotManager.screenshotsDirectory()));
			openFolder.setBounds(left + 256.0F, buttonsY, 86.0F, 18.0F);
			add(openFolder);
		}

		private Path currentPath() {
			return selected >= 0 && selected < files.size() ? files.get(selected) : null;
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ScreenshotScreen(parent));
			}
		}
	}

	// ---------------------------------------------------------------- waypoints

	public static final class WaypointsScreen extends ListScreen {
		private final List<Waypoint> listed = new ArrayList<>();

		public WaypointsScreen(Screen parent) {
			super(parent, "Waypoints");
		}

		@Override
		protected String subtitle() {
			return "Left click a waypoint to copy its coordinates, right click to delete it.";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			listed.clear();
			listed.addAll(ChaosConfig.WAYPOINTS);
			int visibleRows = (int) ((this.height - top - 90.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, listed.size()); i++) {
				Waypoint waypoint = listed.get(i);
				String dimension = waypoint.dimension.contains(":") ? waypoint.dimension.split(":")[1] : waypoint.dimension;
				String label = (waypoint.temporary ? "§7*" : "§f") + waypoint.name + " §8· §7" + dimension + " §8· "
						+ (int) waypoint.x + " " + (int) waypoint.y + " " + (int) waypoint.z;
				WaypointRow row = new WaypointRow(label, waypoint.color, waypoint,
						() -> Clipboard.copyText(
								String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z)),
						() -> {
							ChaosConfig.WAYPOINTS.remove(waypoint);
							ChaosConfig.markDirty();
							refresh();
						});
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}

			UiWidgets.Button addHere = new UiWidgets.Button(this, "Add current position", theme.accent, () -> {
				net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
				if (client.player == null || client.level == null) {
					return;
				}
				ChaosConfig.WAYPOINTS.add(new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
						client.player.getX(), client.player.getY(), client.player.getZ(),
						client.level.dimension().identifier().toString(), theme.accent, false, 0L));
				ChaosConfig.markDirty();
				refresh();
			});
			addHere.setBounds(left, this.height - 28.0F, 120.0F, 18.0F);
			add(addHere);

			UiWidgets.Button clearTemporary = new UiWidgets.Button(this, "Clear temporary", theme.warning, () -> {
				ChaosConfig.WAYPOINTS.removeIf(waypoint -> waypoint.temporary);
				ChaosConfig.markDirty();
				refresh();
			});
			clearTemporary.setBounds(left + 126.0F, this.height - 28.0F, 100.0F, 18.0F);
			add(clearTemporary);
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new WaypointsScreen(parent));
			}
		}
	}

	/** Label / value row with optional left and right click actions. */
	static final class ValueRow extends UiComponent {
		private final String label;
		private final String value;
		private final int accent;
		private final Runnable onLeft;
		private final Runnable onRight;

		ValueRow(String label, String value, int accent, Runnable onLeft, Runnable onRight) {
			this.label = label;
			this.value = value;
			this.accent = accent;
			this.onLeft = onLeft;
			this.onRight = onRight;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Font font = net.minecraft.client.Minecraft.getInstance().font;
			Render.text(graphics, font, label, x, y + 5.0F, theme.textDim, false);
			float boxWidth = width - 96.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			String text = value == null ? "" : value;
			while (font.width(text) > boxWidth - 10.0F && text.length() > 4) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			Render.text(graphics, font, text, bx + 5.0F, y + 5.0F, theme.text, false);
			Render.rect(graphics, bx + 1.0F, y + height - 2.0F, boxWidth - 2.0F, 1.0F, Render.alpha(accent, 0.6F));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0 && onLeft != null) {
				onLeft.run();
				return true;
			}
			if (button == 1 && onRight != null) {
				onRight.run();
				return true;
			}
			return onLeft != null || onRight != null;
		}
	}

	/** Waypoint row: the usual look with right click delete. */
	static final class WaypointRow extends UiComponent {
		private final String label;
		private final int accent;
		private final Waypoint waypoint;
		private final Runnable onLeft;
		private final Runnable onRight;

		WaypointRow(String label, int accent, Waypoint waypoint, Runnable onLeft, Runnable onRight) {
			this.label = label;
			this.accent = accent;
			this.waypoint = waypoint;
			this.onLeft = onLeft;
			this.onRight = onRight;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Font font = net.minecraft.client.Minecraft.getInstance().font;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.6F, background);
			Render.rect(graphics, x + 2.0F, y + 3.0F, 2.0F, height - 6.0F, accent);
			String text = label;
			while (font.width(text) > width - 14.0F && text.length() > 6) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			Render.text(graphics, font, text, x + 8.0F, y + 5.0F, theme.text, false);
			Render.text(graphics, font, waypoint.expired() ? "expired" : "", x + width - 44.0F, y + 5.0F,
					theme.textFaint, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0) {
				onLeft.run();
				return true;
			}
			if (button == 1) {
				onRight.run();
				return true;
			}
			return false;
		}
	}

}
