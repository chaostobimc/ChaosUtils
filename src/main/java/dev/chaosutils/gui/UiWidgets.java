package dev.chaosutils.gui;

import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.InputUtil;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/** The ChaosUtils widget toolkit: every setting type has an animated control here. */
public final class UiWidgets {
	private UiWidgets() {
	}

	private static Font fontOf(ChaosScreen screen) {
		return screen.font();
	}

	private static void label(GuiGraphics graphics, ChaosScreen screen, UiComponent component, String text, int color) {
		Render.text(graphics, fontOf(screen), text, component.x(), component.y() + (component.height() - 8.0F) * 0.5F - 0.5F, color, false);
	}

	private static void playClick(ChaosScreen screen, boolean on) {
		screen.playClick(on);
	}

	// -------------------------------------------------------------------- button

	public static final class Button extends UiComponent {
		private final ChaosScreen screen;
		private final String label;
		private final Runnable action;
		private final int accent;
		private boolean pressed;

		public Button(ChaosScreen screen, String label, int accent, Runnable action) {
			this.screen = screen;
			this.label = label;
			this.accent = accent;
			this.action = action;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			float radius = theme.radius * 0.7F;
			float drawY = pressed ? y + 1.0F : y;
			int color = Render.mix(theme.panelAlt, Render.brighten(accent, 0.25F), hover.get() * 0.85F);
			Render.roundedRect(graphics, x, drawY, width, height, radius, color);
			Render.roundedBorder(graphics, x, drawY, width, height, radius, 1.0F, Render.alpha(accent, 0.45F), color);
			int textColor = Render.mix(theme.text, 0xFFFFFFFF, hover.get());
			Render.centeredText(graphics, fontOf(screen), label, x + width * 0.5F, drawY + (height - 8.0F) * 0.5F, textColor, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 0 && isHovered(mouseX, mouseY)) {
				pressed = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (pressed) {
				pressed = false;
				if (isHovered(mouseX, mouseY)) {
					playClick(screen, true);
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	// -------------------------------------------------------------------- toggle

	public static final class Toggle extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Toggle setting;

		public Toggle(ChaosScreen screen, Setting.Toggle setting) {
			this.screen = screen;
			this.setting = setting;
			this.active.snap(setting.get() ? 1.0F : 0.0F);
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			active.set(setting.get() ? 1.0F : 0.0F);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float switchWidth = 28.0F;
			float switchHeight = 14.0F;
			float sx = x + width - switchWidth;
			float sy = y + (height - switchHeight) * 0.5F;
			float on = active.get();
			int track = Render.mix(0xFF3A3A48, theme.accent, on);
			Render.roundedRect(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.roundedBorder(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(0xFFFFFFFF, 0.10F + hover.get() * 0.15F), track);
			float knobSize = switchHeight - 4.0F;
			float knobX = sx + 2.0F + on * (switchWidth - knobSize - 4.0F);
			Render.roundedRect(graphics, knobX, sy + 2.0F, knobSize, knobSize, knobSize * 0.5F,
					Render.mix(0xFFBFC2CF, 0xFFFFFFFF, on));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 0 && isHovered(mouseX, mouseY)) {
				setting.toggle();
				playClick(screen, setting.get());
				return true;
			}
			return false;
		}
	}

	// -------------------------------------------------------------------- slider

	public static final class Slider extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Number setting;
		private boolean dragging;

		public Slider(ChaosScreen screen, Setting.Number setting) {
			this.screen = screen;
			this.setting = setting;
		}

		private float trackWidth() {
			return width - 84.0F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float tw = Math.max(20.0F, trackWidth());
			float tx = x + width - tw;
			float fraction = (float) setting.fraction();
			String value = setting.display();
			int valueWidth = fontOf(screen).width(value);
			Render.text(graphics, fontOf(screen), value, x + width - tw - valueWidth - 8.0F,
					y + (height - 8.0F) * 0.5F - 0.5F, Render.mix(theme.textFaint, theme.text, hover.get()), false);
			float ty = y + height * 0.5F - 2.0F;
			Render.roundedRect(graphics, tx, ty, tw, 4.0F, 2.0F, 0xFF3A3A48);
			Render.roundedRect(graphics, tx, ty, tw * fraction, 4.0F, 2.0F, theme.accent);
			float knobSize = dragging || hover.get() > 0.4F ? 11.0F : 9.0F;
			float knobX = tx + tw * fraction;
			Render.roundedRect(graphics, knobX - knobSize * 0.5F, y + height * 0.5F - knobSize * 0.5F, knobSize, knobSize,
					knobSize * 0.5F, Render.brighten(theme.accent, 0.35F));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				playClick(screen, false);
				return true;
			}
			if (button == 0) {
				apply(mouseX);
				dragging = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (dragging) {
				apply(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (dragging) {
				dragging = false;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				setting.nudge(amount > 0 ? 1 : -1);
				return true;
			}
			return false;
		}

		private void apply(float mouseX) {
			float tw = Math.max(20.0F, trackWidth());
			float tx = x + width - tw;
			setting.setFraction((mouseX - tx) / tw);
		}
	}

	// -------------------------------------------------------------------- choice

	public static final class Choice extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Choice setting;
		private final Anim.Value flash = new Anim.Value(0.0F, 8.0F);

		public Choice(ChaosScreen screen, Setting.Choice setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.display();
			float boxWidth = Math.max(74.0F, fontOf(screen).width(value) + 24.0F);
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			background = Render.mix(background, theme.accent, flash.get() * 0.35F);
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), value, bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
			Render.text(graphics, fontOf(screen), "‹", bx + 6.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.textFaint, false);
			Render.text(graphics, fontOf(screen), "›", bx + boxWidth - 11.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.textFaint, false);
			flash.update(deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && (button == 0 || button == 1)) {
				setting.cycle(button == 0 ? 1 : -1);
				flash.snap(1.0F);
				flash.set(0.0F);
				playClick(screen, true);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				setting.cycle(amount > 0 ? 1 : -1);
				return true;
			}
			return false;
		}
	}

	// --------------------------------------------------------------------- color

	public static final class ColorField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Color setting;

		public ColorField(ChaosScreen screen, Setting.Color setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			int color = setting.get();
			Render.roundedRect(graphics, bx + 4.0F, y + 3.0F, height - 6.0F, height - 6.0F, (height - 6.0F) * 0.3F, color | 0xFF000000);
			Render.text(graphics, fontOf(screen), String.format("#%06X", color & 0xFFFFFF), bx + height + 1.0F,
					y + (height - 8.0F) * 0.5F - 0.5F, theme.textDim, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openColorPicker(setting);
					playClick(screen, true);
				}
				return true;
			}
			return false;
		}
	}

	// ----------------------------------------------------------------------- key

	public static final class KeyField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Key setting;
		private int lastPolled = InputUtil.NO_KEY;

		public KeyField(ChaosScreen screen, Setting.Key setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (!setting.isListening()) {
				lastPolled = InputUtil.NO_KEY;
				return;
			}
			int pressed = InputUtil.pollNewInput(lastPolled);
			lastPolled = InputUtil.currentlyHeld();
			if (pressed == InputUtil.NO_KEY) {
				return;
			}
			if (pressed == GLFW.GLFW_KEY_ESCAPE || pressed == GLFW.GLFW_KEY_BACKSPACE) {
				setting.stopListening();
			} else if (pressed == GLFW.GLFW_KEY_DELETE) {
				setting.set(InputUtil.NO_KEY);
			} else {
				setting.set(pressed);
			}
			playClick(screen, true);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.isListening() ? "press a key…" : setting.display();
			float boxWidth = Math.max(70.0F, fontOf(screen).width(value) + 18.0F);
			float bx = x + width - boxWidth;
			int accent = setting.isListening() ? theme.warning : theme.accent;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.roundedBorder(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, 1.0F, Render.alpha(accent, 0.7F), background);
			Render.centeredText(graphics, fontOf(screen), value, bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				setting.listen();
				return true;
			}
			return false;
		}
	}

	// ------------------------------------------------------------------ position

	public static final class PositionField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Position setting;

		public PositionField(ChaosScreen screen, Setting.Position setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), "Adjust", bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
			float px = bx + 6.0F + setting.get().x() * 8.0F;
			float py = y + height - 5.0F - setting.get().y() * 4.0F;
			Render.rect(graphics, px, py, 3.0F, 3.0F, theme.accent);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openHudEditor();
				}
				return true;
			}
			return false;
		}
	}

	// --------------------------------------------------------------------- action

	public static final class ActionField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Action setting;

		public ActionField(ChaosScreen screen, Setting.Action setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, Render.brighten(theme.accent, 0.1F), hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), "Run", bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				playClick(screen, true);
				setting.run();
				return true;
			}
			return false;
		}
	}

	// ---------------------------------------------------------------------- text

	public static final class TextField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Text setting;

		public TextField(ChaosScreen screen, Setting.Text setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.display();
			float boxWidth = Math.min(width * 0.55F, 160.0F);
			float bx = x + width - boxWidth;
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, Render.mix(theme.panelAlt, theme.panelHover, hover.get()));
			float maxText = boxWidth - 10.0F;
			while (fontOf(screen).width(value) > maxText && value.length() > 3) {
				value = value.substring(0, value.length() - 2) + "…";
			}
			Render.text(graphics, fontOf(screen), value, bx + 5.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openTextEditor(setting);
				}
				return true;
			}
			return false;
		}
	}

	/** Copies fixed text to the clipboard; used for generated values (coordinates, IPs). */
	public static final class CopyField extends UiComponent {
		private final ChaosScreen screen;
		private final java.util.function.Supplier<String> textSupplier;

		public CopyField(ChaosScreen screen, String label, java.util.function.Supplier<String> textSupplier) {
			this.screen = screen;
			this.textSupplier = textSupplier;
			this.setTooltip(label);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.6F, Render.mix(theme.panelAlt, theme.panelHover, hover.get()));
			Render.centeredText(graphics, fontOf(screen), "Copy", x + width * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				Clipboard.copyText(textSupplier.get());
				playClick(screen, true);
				return true;
			}
			return false;
		}
	}

	/** Creates the control matching a setting, laid out for a settings row. */
	public static UiComponent forSetting(Setting<?> setting, ChaosScreen screen) {
		return switch (setting.kind()) {
			case TOGGLE -> new Toggle(screen, (Setting.Toggle) setting);
			case NUMBER -> new Slider(screen, (Setting.Number) setting);
			case CHOICE -> new Choice(screen, (Setting.Choice) setting);
			case COLOR -> new ColorField(screen, (Setting.Color) setting);
			case KEY -> new KeyField(screen, (Setting.Key) setting);
			case POSITION -> new PositionField(screen, (Setting.Position) setting);
			case TEXT -> new TextField(screen, (Setting.Text) setting);
			case ACTION -> new ActionField(screen, (Setting.Action) setting);
		};
	}

	// ------------------------------------------------------------------- popups

	/** Modal editor for text settings, reusing a single {@link net.minecraft.client.gui.components.EditBox}. */
	public static final class TextPopup extends UiComponent {
		private final ChaosScreen screen;
		private float buttonY;

		public TextPopup(ChaosScreen screen) {
			this.screen = screen;
		}

		public void layout(float px, float py, float width, float height, net.minecraft.client.gui.components.EditBox input) {
			setBounds(px, py, width, height);
			if (input != null) {
				input.setX(Math.round(px + 14.0F));
				input.setY(Math.round(py + 34.0F));
				input.setWidth(Math.round(width - 28.0F));
			}
			buttonY = py + height - 30.0F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.shadowedPanel(graphics, x, y, width, height, theme.radius, theme.panel, theme.accent);
			Render.roundedRect(graphics, x + 14.0F, y + 32.0F, width - 28.0F, 20.0F, 4.0F, 0xFF0E0E14);
			renderButton(graphics, mouseX, mouseY, "Accept", x + width - 176.0F, buttonY, theme.accent);
			renderButton(graphics, mouseX, mouseY, "Cancel", x + width - 88.0F, buttonY, 0xFF4A4A5A);
		}

		private void renderButton(GuiGraphics graphics, float mouseX, float mouseY, String label, float bx, float by, int accent) {
			UiTheme theme = UiTheme.get();
			boolean hovered = mouseX >= bx && mouseX <= bx + 80.0F && mouseY >= by && mouseY <= by + 20.0F;
			int color = Render.mix(theme.panelAlt, Render.brighten(accent, 0.2F), hovered ? 0.9F : 0.15F);
			Render.roundedRect(graphics, bx, by, 80.0F, 20.0F, 5.0F, color);
			Render.centeredText(graphics, screen.font(), label, bx + 40.0F, by + 6.0F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button != 0) {
				return false;
			}
			if (mouseX >= x + width - 176.0F && mouseX <= x + width - 96.0F && mouseY >= buttonY && mouseY <= buttonY + 20.0F) {
				screen.closePopup(true);
				return true;
			}
			if (mouseX >= x + width - 88.0F && mouseX <= x + width - 8.0F && mouseY >= buttonY && mouseY <= buttonY + 20.0F) {
				screen.closePopup(false);
				return true;
			}
			return contains(mouseX, mouseY);
		}
	}

	/** Compact HSV + alpha picker used for every colour setting. */
	public static final class ColorPicker extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Color setting;
		private float hue;
		private float saturation;
		private float brightness;
		private float alpha;
		private int draggingRow = -1;
		private float rowX;
		private float rowWidth;
		private float firstRowY;
		private float rowHeight;
		private float rowGap;

		public ColorPicker(ChaosScreen screen, Setting.Color setting) {
			this.screen = screen;
			this.setting = setting;
			int color = setting.get();
			float[] hsv = Render.toHsv(color);
			this.hue = hsv[0];
			this.saturation = hsv[1];
			this.brightness = hsv[2];
			this.alpha = Render.alphaOf(color) / 255.0F;
		}

		public void layout(float px, float py, float width, float height) {
			setBounds(px, py, width, height);
			this.rowX = px + 14.0F;
			this.rowWidth = width - 28.0F;
			this.firstRowY = py + 34.0F;
			this.rowHeight = 12.0F;
			this.rowGap = 8.0F;
		}

		private int argb() {
			return Render.fromHsv(hue, saturation, brightness) & 0xFFFFFF | (Math.round(alpha * 255.0F) << 24);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.shadowedPanel(graphics, x, y, width, height, theme.radius, theme.panel, theme.accent);
			renderRow(graphics, 0, "Hue", hue, 0.0F, 1.0F);
			renderRow(graphics, 1, "Saturation", saturation, 0.0F, 1.0F);
			renderRow(graphics, 2, "Brightness", brightness, 0.0F, 1.0F);
			if (setting.alphaAllowed()) {
				renderRow(graphics, 3, "Opacity", alpha, 0.0F, 1.0F);
			}
			float previewY = y + height - 34.0F;
			Render.roundedRect(graphics, x + 14.0F, previewY, width - 100.0F, 20.0F, 5.0F, argb());
			Render.text(graphics, screen.font(), String.format("#%08X", argb()), x + 20.0F, previewY + 6.0F, 0xFFF2F2F7, true);
			boolean hovered = mouseX >= x + width - 80.0F && mouseX <= x + width - 8.0F && mouseY >= previewY && mouseY <= previewY + 20.0F;
			int buttonColor = Render.mix(theme.panelAlt, theme.accent, hovered ? 0.75F : 0.2F);
			Render.roundedRect(graphics, x + width - 80.0F, previewY, 72.0F, 20.0F, 5.0F, buttonColor);
			Render.centeredText(graphics, screen.font(), "Done", x + width - 44.0F, previewY + 6.0F, theme.text, false);
		}

		private void renderRow(GuiGraphics graphics, int index, String label, float value, float min, float max) {
			UiTheme theme = UiTheme.get();
			float rowY = firstRowY + index * (rowHeight + rowGap);
			Render.text(graphics, screen.font(), label, rowX, rowY - 1.0F, theme.textDim, false);
			float trackX = rowX + 76.0F;
			float trackWidth = rowWidth - 76.0F;
			// Gradient track composed of strips - cheap and shader free.
			int strips = 24;
			for (int i = 0; i < strips; i++) {
				float t = i / (float) strips;
				int color = switch (index) {
					case 0 -> Render.fromHsv(t, 0.9F, 1.0F);
					case 1 -> Render.fromHsv(hue, t, brightness);
					case 2 -> Render.fromHsv(hue, saturation, t);
					default -> Render.fromHsv(hue, saturation, brightness) & 0xFFFFFF | (Math.round(t * 255.0F) << 24);
				};
				Render.rect(graphics, trackX + t * trackWidth, rowY, trackWidth / strips + 1.0F, rowHeight, color);
			}
			float knob = (value - min) / (max - min);
			float knobX = trackX + knob * trackWidth;
			Render.roundedRect(graphics, knobX - 2.0F, rowY - 2.0F, 4.0F, rowHeight + 4.0F, 2.0F, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!contains(mouseX, mouseY) || button != 0) {
				return false;
			}
			float previewY = y + height - 34.0F;
			if (mouseY >= previewY && mouseY <= previewY + 20.0F && mouseX >= x + width - 80.0F) {
				screen.closePopup(true);
				return true;
			}
			for (int index = 0; index < 4; index++) {
				float rowY = firstRowY + index * (rowHeight + rowGap);
				if (mouseY >= rowY - 3.0F && mouseY <= rowY + rowHeight + 3.0F) {
					draggingRow = index;
					applyDrag(mouseX);
					return true;
				}
			}
			return true;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (draggingRow >= 0) {
				applyDrag(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (draggingRow >= 0) {
				draggingRow = -1;
				commit();
				return true;
			}
			return false;
		}

		private void applyDrag(float mouseX) {
			float trackX = rowX + 76.0F;
			float trackWidth = rowWidth - 76.0F;
			float value = Anim.clamp01((mouseX - trackX) / trackWidth);
			switch (draggingRow) {
				case 0 -> hue = value;
				case 1 -> saturation = value;
				case 2 -> brightness = value;
				case 3 -> alpha = value;
				default -> {
				}
			}
			commit();
		}

		private void commit() {
			setting.set(argb());
		}
	}
}
