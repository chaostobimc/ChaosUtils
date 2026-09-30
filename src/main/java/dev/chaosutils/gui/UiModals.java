package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * Modal layer of the interface: dimmed backdrop, floating dialog, entrance animation.
 *
 * <p>Modals are owned by the screen that opened them, animate themselves in and out and are
 * always the first thing that receives input, so a dialog can never end up behind the window it
 * belongs to. All of them are stateless outside their own lifetime - closing one releases its
 * widgets immediately.
 */
public final class UiModals {
	private UiModals() {
	}

	// =================================================================== base

	public abstract static class Modal extends UiComponent {
		private final Anim.Value presence = new Anim.Value(0.0F, 11.0F);
		private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);
		private boolean closing;
		private boolean done;
		private Runnable onClosed;
		/** Screen size handed in by the owning screen (never read from the window directly). */
		protected float viewWidth = 320.0F;
		protected float viewHeight = 240.0F;

		protected Modal() {
			this.visible = true;
		}

		public Modal onClosed(Runnable onClosed) {
			this.onClosed = onClosed;
			return this;
		}

		/** Called by the owning screen every frame so modals never guess the screen size. */
		public void setScreenBounds(float width, float height) {
			this.viewWidth = width;
			this.viewHeight = height;
		}

		/** Starts the close animation; {@link #isFinished()} flips once it has played out. */
		public void close() {
			if (!closing) {
				closing = true;
				exit.set(1.0F);
			}
		}

		public boolean isClosing() {
			return closing;
		}

		public boolean isFinished() {
			return done;
		}

		protected float presence() {
			return Anim.clamp01(presence.get()) * (1.0F - Anim.easeOutQuint(exit.get()));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			presence.set(1.0F);
			presence.update(UiTheme.get().speed(11.0F));
			if (closing) {
				exit.update(UiTheme.get().speed(14.0F));
				if (exit.get() > 0.985F && !done) {
					done = true;
					if (onClosed != null) {
						onClosed.run();
					}
				}
			}
			setLayerAlpha(presence());
			super.update(deltaSeconds, mouseX, mouseY);
		}

		/** Dim layer + scaled dialog frame shared by all modals. */
		protected void renderChrome(GuiGraphics graphics, float radius) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			Render.rect(graphics, 0.0F, 0.0F, viewWidth, viewHeight, Render.alpha(0x000000, 0.55F * presence));
			UiTheme theme = UiTheme.get();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().pushMatrix();
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Ui.window(graphics, x, y, width, height, radius, theme, presence);
			graphics.pose().popMatrix();
		}

		/** Content is drawn inside the same scale transform as the chrome. */
		protected void renderContent(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().pushMatrix();
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			renderChildren(graphics, mouseX, mouseY, deltaSeconds);
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (super.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			// Clicks inside the dialog are swallowed, clicks outside dismiss it.
			if (contains(mouseX, mouseY)) {
				return true;
			}
			close();
			return true;
		}
	}

	// ================================================================= dialog

	/** Question with up to three answers. */
	public static final class Dialog extends Modal {
		private final String title;
		private final List<String> lines;
		private final List<Button> buttons = new ArrayList<>();

		public Dialog(String title, List<String> lines) {
			this.title = title;
			this.lines = lines;
		}

		public static Dialog confirm(String title, String message, String confirmLabel, Runnable onConfirm) {
			Dialog dialog = new Dialog(title, List.of(message));
			dialog.add(confirmLabel, UiTheme.get().negative, dialog2 -> {
				if (onConfirm != null) {
					onConfirm.run();
				}
				dialog2.close();
			});
			dialog.add("Cancel", null, Modal::close);
			return dialog;
		}

		public Dialog add(String label, Integer color, Consumer<Dialog> action) {
			Button button = new Button(label, color == null ? Button.Variant.GHOST : Button.Variant.SOFT,
					color == null ? UiTheme.get().accent : color, () -> action.accept(this));
			buttons.add(button);
			return this;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			float buttonWidth = 96.0F;
			float total = buttons.size() * buttonWidth + Math.max(0, buttons.size() - 1) * 8.0F;
			float startX = x + (width - total) * 0.5F;
			float buttonY = y + height - 34.0F;
			for (int i = 0; i < buttons.size(); i++) {
				Button button = buttons.get(i);
				button.setBounds(startX + i * (buttonWidth + 8.0F), buttonY, buttonWidth, 22.0F);
				button.setLayerAlpha(presence());
				button.update(deltaSeconds, mouseX, mouseY);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Render.text(graphics, UiWidgets.font(), title, x + 18.0F, y + 16.0F, Render.alpha(theme.text, presence), false);
			float cursor = y + 36.0F;
			for (String line : lines) {
				for (String wrapped : wrap(line, width - 36.0F)) {
					Render.text(graphics, UiWidgets.font(), wrapped, x + 18.0F, cursor, Render.alpha(theme.textDim, presence), false);
					cursor += 11.0F;
				}
			}
			for (Button button : buttons) {
				button.render(graphics, mouseX, mouseY, deltaSeconds);
			}
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isClosing()) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			boolean handled = false;
			for (Button entry : buttons) {
				handled |= entry.mouseReleased(mouseX, mouseY, button);
			}
			return handled;
		}

		static List<String> wrap(String text, float maxWidth) {
			List<String> result = new ArrayList<>();
			if (text == null || text.isEmpty()) {
				result.add("");
				return result;
			}
			StringBuilder line = new StringBuilder();
			for (String word : text.split(" ")) {
				String candidate = line.length() == 0 ? word : line + " " + word;
				if (UiWidgets.font().width(candidate) > maxWidth && line.length() > 0) {
					result.add(line.toString());
					line = new StringBuilder(word);
				} else {
					line = new StringBuilder(candidate);
				}
			}
			result.add(line.toString());
			return result;
		}
	}

	// ============================================================ text prompt

	/** Single-line text editor with live preview and enter/escape support. */
	public static final class TextPrompt extends Modal {
		private final String title;
		private final String hint;
		private final EditBox box;
		private final Consumer<String> onAccept;
		private final float initialWidth;

		public TextPrompt(String title, String hint, String initial, int maxLength, Consumer<String> onAccept, EditBox box) {
			this.title = title;
			this.hint = hint;
			this.box = box;
			this.onAccept = onAccept;
			this.initialWidth = Math.max(240.0F, UiWidgets.font().width(initial) + 90.0F);
			this.box.setValue(initial == null ? "" : initial);
			this.box.setMaxLength(Math.max(1, maxLength));
		}

		public float preferredWidth() {
			return Math.min(420.0F, initialWidth);
		}

		public void place(float centerX, float centerY) {
			float boxWidth = preferredWidth();
			float boxHeight = 116.0F;
			setBounds(centerX - boxWidth * 0.5F, centerY - boxHeight * 0.5F, boxWidth, boxHeight);
			box.setX(Math.round(x + 16.0F));
			box.setY(Math.round(y + 44.0F));
			box.setWidth(Math.round(boxWidth - 32.0F));
		}

		public void accept() {
			if (onAccept != null) {
				onAccept.accept(box.getValue());
			}
			close();
		}

		public EditBox box() {
			return box;
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isClosing()) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Render.text(graphics, UiWidgets.font(), title, x + 16.0F, y + 14.0F, Render.alpha(theme.text, presence), false);
			Render.roundedRect(graphics, x + 16.0F, y + 42.0F, width - 32.0F, 20.0F, 8.0F,
					Render.alpha(theme.track, presence));
			Render.ring(graphics, x + 16.0F, y + 42.0F, width - 32.0F, 20.0F, 8.0F, 1.0F,
					Render.alpha(theme.accent, presence * 0.6F));
			if (box.getValue().isEmpty()) {
				Render.text(graphics, UiWidgets.font(), hint, x + 22.0F, y + 48.0F,
						Render.alpha(theme.textFaint, presence), false);
			}
			Render.text(graphics, UiWidgets.font(), "Enter to apply  ·  Escape to cancel", x + 16.0F, y + 70.0F,
					Render.alpha(theme.textFaint, presence), false);
			graphics.pose().popMatrix();
		}

	}

	// ============================================================ colour picker

	/** Hue/saturation/value picker with an alpha slider, hex field and presets. */
	public static final class ColorPicker extends Modal {
		private static final int[] PRESETS = {
				0xFF7C5CFF, 0xFF4FC3F7, 0xFF57D98A, 0xFFF2B23E,
				0xFFF0686A, 0xFFF06292, 0xFFFFFFFF, 0xFF8B8FA3
		};

		private final Setting.Color setting;
		private final int originalValue;
		private int hueColor;
		private float hue;
		private float saturation;
		private float value;
		private float alphaValue;
		private boolean draggingSquare;
		private boolean draggingHue;
		private boolean draggingAlpha;
		private EditBox hexBox;

		public ColorPicker(Setting.Color setting) {
			this.setting = setting;
			this.originalValue = setting.get();
			float[] hsv = Render.toHsv(originalValue);
			this.hue = hsv[0];
			this.saturation = hsv[1];
			this.value = hsv[2];
			this.alphaValue = Render.alphaOf(originalValue) / 255.0F;
			this.hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
		}

		public void attachHexBox(EditBox box) {
			this.hexBox = box;
			box.setValue(hex());
		}

		public void place(float centerX, float centerY) {
			float boxWidth = 260.0F;
			float boxHeight = 232.0F;
			setBounds(centerX - boxWidth * 0.5F, centerY - boxHeight * 0.5F, boxWidth, boxHeight);
			if (hexBox != null) {
				hexBox.setX(Math.round(x + boxWidth - 92.0F));
				hexBox.setY(Math.round(y + boxHeight - 34.0F));
				hexBox.setWidth(78);
			}
		}

		private float squareX() {
			return x + 16.0F;
		}

		private float squareY() {
			return y + 34.0F;
		}

		private float squareWidth() {
			return width - 32.0F - 22.0F;
		}

		private float squareHeight() {
			return 110.0F;
		}

		private String hex() {
			return String.format("#%06X", currentColor() & 0xFFFFFF);
		}

		private int currentColor() {
			int rgb = Render.fromHsv(hue, saturation, value) | 0xFF000000;
			int alpha = Math.round(alphaValue * 255.0F);
			return (rgb & 0xFFFFFF) | alpha << 24;
		}

		private void applyFromSquare(float mouseX, float mouseY) {
			saturation = Anim.clamp01((mouseX - squareX()) / Math.max(1.0F, squareWidth()));
			value = 1.0F - Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			apply();
		}

		private void applyFromHue(float mouseY) {
			hue = Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
			apply();
		}

		private void applyFromAlpha(float mouseY) {
			alphaValue = 1.0F - Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			apply();
		}

		private void apply() {
			int color = currentColor();
			setting.set(setting.alphaAllowed() ? color : (color | 0xFF000000));
			if (hexBox != null) {
				hexBox.setValue(hex());
			}
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (draggingSquare) {
				applyFromSquare(mouseX, mouseY);
			} else if (draggingHue) {
				applyFromHue(mouseY);
			} else if (draggingAlpha) {
				applyFromAlpha(mouseY);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());

			Render.text(graphics, UiWidgets.font(), setting.label, x + 16.0F, y + 14.0F,
					Render.alpha(theme.text, presence), false);
			Render.text(graphics, UiWidgets.font(), hex(), x + width - 100.0F, y + 14.0F,
					Render.alpha(theme.textDim, presence), false);

			// Saturation/value square: hue gradient with a white-to-transparent wash on top.
			Render.roundedRectGradient(graphics, squareX(), squareY(), squareWidth(), squareHeight(), 8.0F,
					Render.alpha(hueColor, presence), Render.alpha(0xFF000000, presence));
			Render.horizontalGradient(graphics, squareX(), squareY(), squareWidth(), squareHeight(),
					Render.alpha(0xFFFFFFFF, presence), Render.alpha(0xFFFFFFFF, 0.0F));
			Render.ring(graphics, squareX(), squareY(), squareWidth(), squareHeight(), 8.0F, 1.0F,
					Render.alpha(theme.outlineStrong, presence));
			float markerX = squareX() + saturation * squareWidth();
			float markerY = squareY() + (1.0F - value) * squareHeight();
			Ui.knob(graphics, markerX, markerY, 4.0F, 0xFFFFFFFF, presence);

			// Hue strip.
			float hueX = squareX() + squareWidth() + 8.0F;
			int bands = 24;
			float bandHeight = squareHeight() / bands;
			for (int i = 0; i < bands; i++) {
				int color = Render.fromHsv(i / (float) bands, 1.0F, 1.0F);
				Render.rect(graphics, hueX, squareY() + i * bandHeight, 12.0F, bandHeight + 0.6F,
						Render.alpha(color, presence));
			}
			Render.ring(graphics, hueX, squareY(), 12.0F, squareHeight(), 6.0F, 1.0F,
					Render.alpha(theme.outlineStrong, presence));
			float hueMarkerY = squareY() + hue * squareHeight();
			Render.roundedRect(graphics, hueX - 2.0F, hueMarkerY - 1.5F, 16.0F, 3.0F, 1.5F,
					Render.alpha(0xFFFFFFFF, presence));

			// Alpha strip.
			if (setting.alphaAllowed()) {
				float alphaY = squareY() + squareHeight() + 10.0F;
				Render.roundedRect(graphics, squareX(), alphaY, squareWidth(), 12.0F, 6.0F,
						Render.alpha(0xFF20202C, presence));
				Render.horizontalGradient(graphics, squareX(), alphaY, squareWidth(), 12.0F,
						Render.alpha(currentColor() | 0xFF000000, presence * 0.25F), Render.alpha(currentColor() | 0xFF000000, presence));
				Render.ring(graphics, squareX(), alphaY, squareWidth(), 12.0F, 6.0F, 1.0F,
						Render.alpha(theme.outlineStrong, presence));
			}

			// Presets.
			float presetY = y + height - 66.0F;
			for (int i = 0; i < PRESETS.length; i++) {
				float presetX = x + 16.0F + i * 22.0F;
				boolean hovered = mouseX >= presetX - 2.0F && mouseX <= presetX + 18.0F
						&& mouseY >= presetY - 2.0F && mouseY <= presetY + 18.0F;
				Render.roundedRect(graphics, presetX, presetY, 16.0F, 16.0F, 5.0F,
						Render.alpha(PRESETS[i], presence));
				Render.ring(graphics, presetX, presetY, 16.0F, 16.0F, 5.0F, hovered ? 1.6F : 1.0F,
						Render.alpha(hovered ? 0xFFFFFFFF : theme.outlineStrong, presence));
			}
			Render.text(graphics, UiWidgets.font(), "Alpha", x + 16.0F, y + height - 30.0F,
					Render.alpha(theme.textFaint, presence), false);
			Render.text(graphics, UiWidgets.font(), "Right click a preset to reset", x + width - 130.0F, y + height - 30.0F,
					Render.alpha(theme.textFaint, presence * 0.7F), false);
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button != 0) {
				return true;
			}
			if (hexBox != null && hexBox.isFocused()) {
				setFocusedElsewhere();
			}
			if (mouseX >= squareX() && mouseX <= squareX() + squareWidth()
					&& mouseY >= squareY() && mouseY <= squareY() + squareHeight()) {
				draggingSquare = true;
				applyFromSquare(mouseX, mouseY);
				return true;
			}
			float hueX = squareX() + squareWidth() + 8.0F;
			if (mouseX >= hueX && mouseX <= hueX + 12.0F && mouseY >= squareY() && mouseY <= squareY() + squareHeight()) {
				draggingHue = true;
				applyFromHue(mouseY);
				return true;
			}
			if (setting.alphaAllowed()) {
				float alphaY = squareY() + squareHeight() + 10.0F;
				if (mouseX >= squareX() && mouseX <= squareX() + squareWidth() && mouseY >= alphaY && mouseY <= alphaY + 12.0F) {
					draggingAlpha = true;
					applyFromAlpha(mouseY);
					return true;
				}
			}
			float presetY = y + height - 66.0F;
			for (int i = 0; i < PRESETS.length; i++) {
				float presetX = x + 16.0F + i * 22.0F;
				if (mouseX >= presetX && mouseX <= presetX + 16.0F && mouseY >= presetY && mouseY <= presetY + 16.0F) {
					int preset = PRESETS[i] | 0xFF000000;
					float[] hsv = Render.toHsv(setting.get());
					hue = hsv[0];
					saturation = hsv[1];
					value = hsv[2];
					hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
					if (hexBox != null) {
						hexBox.setValue(hex());
					}
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		private void setFocusedElsewhere() {
			net.minecraft.client.gui.screens.Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
			if (screen != null) {
				screen.setFocused(null);
			}
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (draggingSquare) {
				applyFromSquare(mouseX, mouseY);
				return true;
			}
			if (draggingHue) {
				applyFromHue(mouseY);
				return true;
			}
			if (draggingAlpha) {
				applyFromAlpha(mouseY);
				return true;
			}
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (button == 0 && (draggingSquare || draggingHue || draggingAlpha)) {
				draggingSquare = false;
				draggingHue = false;
				draggingAlpha = false;
				return true;
			}
			return super.mouseReleased(mouseX, mouseY, button);
		}

		/** Called by the "Reset" button. */
		public void resetToOriginal() {
			setting.set(originalValue);
			float[] hsv = Render.toHsv(originalValue);
			hue = hsv[0];
			saturation = hsv[1];
			value = hsv[2];
			hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
			alphaValue = Render.alphaOf(originalValue) / 255.0F;
			if (hexBox != null) {
				hexBox.setValue(hex());
			}
		}

	}

	/**
	 * Builds the colour picker with its hex field wired up.
	 *
	 * @param registerInput callback that registers the text field on the *modal* layer, so it is
	 *                      drawn above the dimming layer instead of behind it
	 */
	public static ColorPicker colorPicker(Setting.Color setting, Consumer<EditBox> registerInput) {
		ColorPicker picker = new ColorPicker(setting);
		EditBox hexBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 78, 16,
				Component.literal(setting.label));
		hexBox.setBordered(false);
		hexBox.setTextColor(0xFFF4F5FA);
		hexBox.setMaxLength(9);
		hexBox.setResponder(value -> {
			String text = value.trim();
			if (text.startsWith("#")) {
				text = text.substring(1);
			}
			if (text.length() == 6 || text.length() == 8) {
				try {
					int parsed = (int) Long.parseLong(text, 16);
					if (text.length() == 6) {
						parsed |= 0xFF000000;
					}
					setting.set(parsed);
				} catch (NumberFormatException ignored) {
					// still typing
				}
			}
		});
		picker.attachHexBox(hexBox);
		registerInput.accept(hexBox);
		return picker;
	}
}
