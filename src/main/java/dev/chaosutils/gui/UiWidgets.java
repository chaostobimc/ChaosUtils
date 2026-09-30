package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.InputUtil;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * The ChaosUtils control set.
 *
 * <p>Every control is drawn from the same vocabulary ({@link Ui}, {@link Render}), animates with
 * shared timing and carries no state that survives its screen, so nothing can leak between two
 * openings of the interface. Rows are built generically from {@link Setting} descriptions
 * ({@link #forSetting}), which means a new feature setting can never be missing from the GUI.
 */
public final class UiWidgets {
	private UiWidgets() {
	}

	public static Font font() {
		return Minecraft.getInstance().font;
	}

	// =============================================================== primitives

	/** Flat button with four visual weights and an animated hover glow. */
	public static final class Button extends UiComponent {
		public enum Variant {
			/** Accent filled - one per screen. */
			PRIMARY,
			/** Transparent with a hairline, brightens on hover. */
			GHOST,
			/** Filled surface, the workhorse. */
			SOFT,
			/** Red tint for destructive actions. */
			DANGER
		}

		private String label;
		private Variant variant;
		private Runnable action;
		private int accent;
		private boolean pressed;
		private float padding = 8.0F;
		private boolean leftAligned;
		private ItemStack icon;

		public Button(String label, Variant variant, int accent, Runnable action) {
			this.label = label;
			this.variant = variant;
			this.accent = accent;
			this.action = action;
		}

		public Button label(String value) {
			this.label = value;
			return this;
		}

		public void setAction(Runnable action) {
			this.action = action;
		}

		public void setLeftAligned(boolean leftAligned) {
			this.leftAligned = leftAligned;
		}

		public void setIcon(ItemStack icon) {
			this.icon = icon;
		}

		public void setPadding(float padding) {
			this.padding = padding;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (!isHovered(mouseX, mouseY)) {
				active.set(0.0F);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			float offset = appearOffset();
			float top = y + offset;
			float hoverAmount = hover.get();
			float press = active.get();
			UiTheme theme = UiTheme.get();
			float radius = Math.min(theme.radiusControl, height * 0.5F);

			int background;
			int textColor;
			switch (variant) {
				case PRIMARY -> {
					background = Render.mix(Render.scaleAlpha(accent, 0.92F), 0xFFFFFFFF, hoverAmount * 0.18F);
					textColor = theme.onAccent;
				}
				case DANGER -> {
					background = Render.mix(Render.alpha(theme.negative, 0.16F), Render.alpha(theme.negative, 0.30F), hoverAmount);
					textColor = theme.negative;
				}
				case GHOST -> {
					background = Render.mix(0x00000000, Render.alpha(0xFFFFFFFF, 0.09F), hoverAmount);
					textColor = Render.mix(theme.textDim, theme.text, hoverAmount);
				}
				default -> {
					background = Render.mix(Render.alpha(0xFFFFFFFF, 0.055F), Render.alpha(0xFFFFFFFF, 0.115F), hoverAmount);
					textColor = Render.mix(theme.textDim, theme.text, hoverAmount);
				}
			}
			if (theme.glow && variant == Variant.PRIMARY) {
				Render.glow(graphics, centerX(), top + height * 0.5F, width * 0.75F, accent, 0.20F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, x, top, width, height, radius, Render.mix(0x00000000, background, alpha));
			if (variant != Variant.PRIMARY) {
				Render.ring(graphics, x, top, width, height, radius, 1.0F,
						Render.mix(Render.alpha(theme.outlineSoft, alpha),
								Render.alpha(variant == Variant.DANGER ? theme.negative : accent, alpha * 0.5F), hoverAmount));
			}
			float textY = top + (height - 8.0F) * 0.5F - press * 0.5F;
			String shown = Render.ellipsize(font(), label == null ? "" : label, width - padding * 2.0F);
			if (icon != null && !icon.isEmpty()) {
				Ui.itemIcon(graphics, icon, x + padding + 6.0F, top + height * 0.5F, 0.7F, alpha);
			}
			if (leftAligned) {
				Render.text(graphics, font(), shown, x + padding + (icon != null ? 16.0F : 0.0F), textY,
						Render.alpha(textColor, alpha), false);
			} else {
				Render.centeredText(graphics, font(), shown, centerX(), textY, Render.alpha(textColor, alpha), false);
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				pressed = true;
				active.set(1.0F);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (button == 0 && pressed) {
				pressed = false;
				active.set(0.0F);
				if (isHovered(mouseX, mouseY) && action != null) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	/** Square button that paints a custom glyph. */
	public static final class IconButton extends UiComponent {
		public interface Glyph {
			void paint(GuiGraphics graphics, float centerX, float centerY, float alpha);
		}

		private final Glyph glyph;
		private final Runnable action;
		private int color;
		private boolean round;

		public IconButton(Glyph glyph, int color, Runnable action) {
			this.glyph = glyph;
			this.color = color;
			this.action = action;
		}

		public IconButton round() {
			this.round = true;
			return this;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float hoverAmount = hover.get();
			float radius = round ? height * 0.5F : Math.min(theme.radiusControl, height * 0.4F);
			int background = Render.mix(Render.alpha(0xFFFFFFFF, 0.05F), Render.alpha(0xFFFFFFFF, 0.13F), hoverAmount);
			if (hoverAmount > 0.02F && theme.glow) {
				Render.glow(graphics, centerX(), centerY(), width * 0.9F, color, 0.22F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(0x00000000, background, alpha));
			if (glyph != null) {
				glyph.paint(graphics, centerX(), centerY(), alpha * Math.max(0.55F, hoverAmount));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				active.set(1.0F);
				if (action != null) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	/** Animated on/off switch. */
	public static final class Toggle extends UiComponent {
		private final BooleanSupplier getter;
		private final Consumer<Boolean> setter;
		private final Anim.Value on = new Anim.Value(0.0F, 16.0F);
		private int accent;
		private float switchWidth = 26.0F;
		private float switchHeight = 14.0F;
		private boolean snapped;

		public Toggle(BooleanSupplier getter, Consumer<Boolean> setter, int accent) {
			this.getter = getter;
			this.setter = setter;
			this.accent = accent;
		}

		public Toggle size(float width, float height) {
			this.switchWidth = width;
			this.switchHeight = height;
			return this;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			boolean value = getter != null && getter.getAsBoolean();
			on.set(value ? 1.0F : 0.0F);
			if (!snapped) {
				on.snap(value ? 1.0F : 0.0F);
				snapped = true;
			}
			on.update(UiTheme.get().speed(16.0F));
		}

		private float switchX() {
			return x + width - switchWidth;
		}

		private float switchY() {
			return y + (height - switchHeight) * 0.5F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float amount = on.get();
			float sx = switchX();
			float sy = switchY() + appearOffset();
			float hoverAmount = hover.get();
			int track = Render.mix(Render.alpha(theme.track, alpha),
					Render.alpha(Render.mix(accent, 0xFFFFFFFF, hoverAmount * 0.12F), alpha), amount);
			if (amount > 0.05F && theme.glow) {
				Render.glow(graphics, sx + switchWidth * 0.5F, sy + switchHeight * 0.5F, switchWidth * 1.1F, accent,
						0.30F * amount * alpha);
			}
			Render.roundedRect(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.ring(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(theme.outline, alpha));
			float knobRadius = switchHeight * 0.5F - 1.6F;
			float travel = switchWidth - switchHeight;
			float knobX = sx + switchHeight * 0.5F + travel * Anim.easeOutQuint(amount);
			Ui.knob(graphics, knobX, sy + switchHeight * 0.5F, knobRadius,
					Render.mix(0xFFD7D8E4, 0xFFFFFFFF, amount), alpha);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				flip();
				return true;
			}
			if (isHovered(mouseX, mouseY) && button == 1) {
				// Right click resets the bound setting through the row below (handled there).
				return false;
			}
			return false;
		}

		/** Flips the bound value; also used by the surrounding row. */
		public void flip() {
			if (setter != null && getter != null) {
				setter.accept(!getter.getAsBoolean());
			}
		}
	}

	/** Row with a label, a value and a draggable track - used for every numeric setting. */
	public static final class Slider extends UiComponent {
		private final Setting.Number setting;
		private boolean dragging;
		private float trackHeight = 4.0F;

		public Slider(Setting.Number setting) {
			this.setting = setting;
		}

		private float trackX() {
			return x + 2.0F;
		}

		private float trackWidth() {
			return width - 4.0F;
		}

		private float trackY() {
			return y + height - 9.0F;
		}

		private float fraction() {
			return (float) Anim.clamp01((float) setting.fraction());
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			float valueAmount = fraction();
			float hoverAmount = Math.max(hover.get(), dragging ? 1.0F : 0.0F);
			int labelColor = Render.mix(theme.textDim, theme.text, hoverAmount * 0.8F);
			Render.text(graphics, font(), setting.label, x, y + offset, Render.alpha(labelColor, alpha), false);
			String value = setting.display();
			Render.text(graphics, font(), value, right() - font().width(value), y + offset,
					Render.alpha(Render.mix(theme.textFaint, theme.accent, hoverAmount), alpha), false);

			float ty = trackY() + offset;
			Render.roundedRect(graphics, trackX(), ty, trackWidth(), trackHeight, trackHeight * 0.5F,
					Render.mix(0x00000000, Render.alpha(theme.trackHover, alpha), 1.0F));
			float filled = Math.max(trackHeight, trackWidth() * valueAmount);
			int fill = Render.mix(theme.accent, theme.accentBright, hoverAmount);
			if (theme.glow && hoverAmount > 0.05F) {
				Render.glow(graphics, trackX() + filled, ty + trackHeight * 0.5F, 16.0F + hoverAmount * 6.0F, theme.accent,
						0.35F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, trackX(), ty, filled, trackHeight, trackHeight * 0.5F, Render.alpha(fill, alpha));
			// Knob grows slightly while dragging, which is the whole trick to make sliders feel alive.
			float knobRadius = 3.4F + hoverAmount * 1.4F + (dragging ? 0.8F : 0.0F);
			Ui.knob(graphics, trackX() + filled, ty + trackHeight * 0.5F, knobRadius, 0xFFFFFFFF, alpha);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0) {
				dragging = true;
				applyFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (dragging && button == 0) {
				applyFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (dragging && button == 0) {
				dragging = false;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			setting.nudge(amount > 0 ? 1 : -1);
			return true;
		}

		private void applyFromMouse(float mouseX) {
			float fraction = (mouseX - trackX()) / Math.max(1.0F, trackWidth());
			setting.setFraction(Anim.clamp01(fraction));
		}
	}

	/** Segmented control for option settings; falls back to a cycler when there are many options. */
	public static final class Choice extends UiComponent {
		private final Setting.Choice setting;
		private final Anim.Value indicatorX = new Anim.Value(0.0F, 18.0F);
		private final Anim.Value indicatorWidth = new Anim.Value(0.0F, 18.0F);
		private boolean snapped;

		public Choice(Setting.Choice setting) {
			this.setting = setting;
		}

		private boolean segmented() {
			String[] options = setting.options();
			if (options.length == 0 || options.length > 4) {
				return false;
			}
			int total = 0;
			for (String option : options) {
				total += font().width(option) + 14;
			}
			return total <= width * 0.62F;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (segmented()) {
				float[] bounds = segmentBounds();
				indicatorX.set(bounds[0]);
				indicatorWidth.set(bounds[1]);
				if (!snapped) {
					indicatorX.snap(bounds[0]);
					indicatorWidth.snap(bounds[1]);
					snapped = true;
				}
				indicatorX.update(UiTheme.get().speed(18.0F));
				indicatorWidth.update(UiTheme.get().speed(18.0F));
			}
		}

		/** @return {x, width} of the selected segment relative to the row. */
		private float[] segmentBounds() {
			String[] options = setting.options();
			int totalWidth = 0;
			for (String option : options) {
				totalWidth += font().width(option) + 14;
			}
			float startX = right() - totalWidth;
			int selected = Anim.clamp(setting.get(), 0, Math.max(0, options.length - 1));
			float offset = startX;
			for (int i = 0; i < selected; i++) {
				offset += font().width(options[i]) + 14;
			}
			return new float[] {offset, font().width(options[selected]) + 14.0F};
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String[] options = setting.options();
			int selected = Anim.clamp(setting.get(), 0, Math.max(0, options.length - 1));
			if (segmented()) {
				float totalWidth = 0;
				for (String option : options) {
					totalWidth += font().width(option) + 14;
				}
				float startX = right() - totalWidth;
				float controlY = y + offset + (height - 18.0F) * 0.5F;
				Render.roundedRect(graphics, startX, controlY, totalWidth, 18.0F, 9.0F,
						Render.alpha(theme.track, alpha * 0.7F));
				Render.roundedRect(graphics, indicatorX.get(), controlY, indicatorWidth.get(), 18.0F, 9.0F,
						Render.alpha(Render.mix(theme.accent, theme.accentBright, hover.get() * 0.3F), alpha));
				float cursor = startX;
				for (int i = 0; i < options.length; i++) {
					int textColor = i == selected ? theme.onAccent : Render.mix(theme.textDim, theme.text, hover.get() * 0.6F);
					Render.centeredText(graphics, font(), options[i], cursor + (font().width(options[i]) + 14) * 0.5F,
							controlY + 5.0F, Render.alpha(textColor, alpha), false);
					cursor += font().width(options[i]) + 14;
				}
			} else {
				String value = setting.display();
				float chipWidth = Math.min(width * 0.55F, font().width(value) + 26.0F);
				float chipX = right() - chipWidth;
				float controlY = y + offset + (height - 17.0F) * 0.5F;
				Render.roundedRect(graphics, chipX, controlY, chipWidth, 17.0F, 8.5F,
						Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.5F), hover.get()));
				Render.centeredText(graphics, font(), Render.ellipsize(font(), value, chipWidth - 16.0F),
						chipX + chipWidth * 0.5F, controlY + 4.5F,
						Render.alpha(Render.mix(theme.text, 0xFFFFFFFF, hover.get()), alpha), false);
				Ui.chevron(graphics, chipX + chipWidth - 8.0F, controlY + 8.5F, 5.0F, hover.get() > 0.5F ? 0.0F : -180.0F,
						Render.alpha(theme.textFaint, alpha));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button != 0) {
				return false;
			}
			if (segmented()) {
				String[] options = setting.options();
				int totalWidth = 0;
				for (String option : options) {
					totalWidth += font().width(option) + 14;
				}
				float cursor = right() - totalWidth;
				for (int i = 0; i < options.length; i++) {
					float segmentWidth = font().width(options[i]) + 14;
					if (mouseX >= cursor && mouseX <= cursor + segmentWidth) {
						setting.set(i);
						return true;
					}
					cursor += segmentWidth;
				}
			}
			setting.cycle(1);
			return true;
		}
	}

	/** Colour swatch that opens the picker. */
	public static final class ColorField extends UiComponent {
		private final Setting.Color setting;
		private final ChaosScreen screen;

		public ColorField(Setting.Color setting, ChaosScreen screen) {
			this.setting = setting;
			this.screen = screen;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			float swatchHeight = 15.0F;
			float swatchWidth = 30.0F;
			float swatchX = right() - swatchWidth;
			float swatchY = y + offset + (height - swatchHeight) * 0.5F;
			String hex = String.format("#%06X", setting.get() & 0xFFFFFF);
			Render.text(graphics, font(), hex, swatchX - 6.0F - font().width(hex), y + offset + (height - 8.0F) * 0.5F,
					Render.alpha(theme.textFaint, alpha), false);
			Render.roundedRect(graphics, swatchX, swatchY, swatchWidth, swatchHeight, 5.0F,
					Render.mix(0x00000000, Render.alpha(setting.get() | 0xFF000000, alpha), 1.0F));
			Render.ring(graphics, swatchX, swatchY, swatchWidth, swatchHeight, 5.0F, 1.0F,
					Render.mix(Render.alpha(theme.outlineSoft, alpha),
							Render.alpha(0xFFFFFFFF, alpha * 0.55F), hover.get()));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0 && screen != null) {
				screen.openColorModal(setting);
				return true;
			}
			return false;
		}
	}

	/** Hotkey field: click, press a key, done. */
	public static final class KeyField extends UiComponent {
		private final Setting.Key setting;
		private boolean listening;
		private int heldBefore;

		public KeyField(Setting.Key setting) {
			this.setting = setting;
		}

		public boolean isListening() {
			return listening;
		}

		public void cancelListening() {
			listening = false;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (listening) {
				int pressed = InputUtil.pollNewInput(heldBefore);
				if (pressed == InputUtil.NO_KEY) {
					heldBefore = InputUtil.currentlyHeld();
				} else if (pressed == 256) {
					listening = false;
				} else {
					setting.set(pressed);
					listening = false;
				}
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String value = listening ? "press a key…" : InputUtil.keyName(setting.get());
			float chipWidth = font().width(value) + 18.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 17.0F) * 0.5F;
			boolean highlight = listening || hover.get() > 0.3F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 17.0F, 8.5F,
					Render.mix(Render.alpha(theme.track, alpha),
							Render.alpha(listening ? theme.accent : 0xFFFFFFFF, alpha * (listening ? 0.45F : 0.12F)), highlight ? 1.0F : 0.0F));
			if (listening) {
				Render.ring(graphics, chipX, chipY, chipWidth, 17.0F, 8.5F, 1.0F,
						Render.alpha(theme.accent, alpha * (0.5F + 0.5F * Anim.pulse((float) (System.nanoTime() / 1.0E9), 1.4F))));
			}
			Render.centeredText(graphics, font(), value, chipX + chipWidth * 0.5F, chipY + 4.5F,
					Render.alpha(listening ? theme.accent : theme.text, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.set(InputUtil.NO_KEY);
				listening = false;
				return true;
			}
			if (button == 0) {
				listening = !listening;
				heldBefore = InputUtil.currentlyHeld();
				return true;
			}
			return false;
		}
	}

	/** Text setting: opens the shared text modal. */
	public static final class TextField extends UiComponent {
		private final Setting.Text setting;
		private final ChaosScreen screen;

		public TextField(Setting.Text setting, ChaosScreen screen) {
			this.setting = setting;
			this.screen = screen;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			float boxX = x + Math.min(width * 0.45F, font().width(setting.label) + 12.0F);
			float boxWidth = right() - boxX;
			float boxY = y + offset + (height - 17.0F) * 0.5F;
			Render.roundedRect(graphics, boxX, boxY, boxWidth, 17.0F, 8.5F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(0xFFFFFFFF, alpha * 0.10F), hover.get()));
			String value = Render.ellipsize(font(), setting.get(), boxWidth - 16.0F);
			Render.text(graphics, font(), value, boxX + 8.0F, boxY + 4.5F, Render.alpha(theme.text, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY) || screen == null) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0) {
				screen.openTextModal(setting.label, setting.get(), setting.maxLength(), setting::set);
				return true;
			}
			return false;
		}
	}

	/** Position setting: the actual pinning happens in the HUD editor. */
	public static final class PositionField extends UiComponent {
		private final String label;
		private final Runnable openEditor;

		public PositionField(String label, Runnable openEditor) {
			this.label = label;
			this.openEditor = openEditor;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String value = "open HUD editor";
			float chipWidth = font().width(value) + 16.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 16.0F) * 0.5F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 16.0F, 8.0F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.45F), hover.get()));
			Render.centeredText(graphics, font(), value, chipX + chipWidth * 0.5F, chipY + 4.0F,
					Render.alpha(Render.mix(theme.textDim, 0xFFFFFFFF, hover.get()), alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0 && openEditor != null) {
				openEditor.run();
				return true;
			}
			return false;
		}
	}

	/** Action setting (a one-shot button described by the module). */
	public static final class ActionField extends UiComponent {
		private final Setting<?> setting;

		public ActionField(Setting<?> setting) {
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			boolean hovered = hover.get() > 0.05F;
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String hint = "run";
			float chipWidth = font().width(hint) + 16.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 16.0F) * 0.5F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 16.0F, 8.0F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.5F), hover.get()));
			Render.centeredText(graphics, font(), hint, chipX + chipWidth * 0.5F, chipY + 4.0F,
					Render.alpha(hovered ? 0xFFFFFFFF : theme.textDim, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				if (setting instanceof Setting.Action action) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	// ============================================================ row assembly

	/**
	 * Builds the control row for a setting, or {@code null} for settings that are shown as part of
	 * another control (the module toggle lives in the card header).
	 */
	public static UiComponent forSetting(Setting<?> setting, ChaosScreen screen) {
		return switch (setting.kind()) {
			case TOGGLE -> {
				Setting.Toggle toggle = (Setting.Toggle) setting;
				Toggle widget = new Toggle(toggle::get, toggle::set, UiTheme.get().accent);
				widget.setTooltip(setting.description);
				yield new LabeledSwitch(setting.label, widget);
			}
			case NUMBER -> new Slider((Setting.Number) setting);
			case CHOICE -> new Choice((Setting.Choice) setting);
			case COLOR -> new ColorField((Setting.Color) setting, screen);
			case KEY -> new KeyField((Setting.Key) setting);
			case TEXT -> new TextField((Setting.Text) setting, screen);
			case POSITION -> new PositionField(setting.label,
					screen == null ? null : screen::openHudEditor);
			case ACTION -> new ActionField(setting);
		};
	}

	/** Label on the left, switch on the right - the row shape used for every boolean setting. */
	public static final class LabeledSwitch extends UiComponent {
		private final String label;
		private final Toggle toggle;

		public LabeledSwitch(String label, Toggle toggle) {
			this.label = label;
			this.toggle = toggle;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			toggle.setBounds(x + width - toggle.width(), y, toggle.width(), height);
			toggle.setLayerAlpha(layerAlpha);
			toggle.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Render.text(graphics, font(), Render.ellipsize(font(), label, width - toggle.width() - 12.0F),
					x, y + appearOffset() + (height - 8.0F) * 0.5F,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			toggle.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 1 && isHovered(mouseX, mouseY)) {
				toggle.flip();
				return true;
			}
			return toggle.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			return toggle.mouseReleased(mouseX, mouseY, button);
		}
	}

	// ================================================================ container

	/** Smoothly scrolling list with a fading scrollbar and clipped content. */
	public static final class ScrollList extends UiComponent {
		private final List<Float> baseY = new ArrayList<>();
		private float scroll;
		private float targetScroll;
		private float contentHeight;
		private float spacing = 6.0F;
		private final Anim.Value barOpacity = new Anim.Value(0.0F, 6.0F);

		public void setSpacing(float spacing) {
			this.spacing = spacing;
		}

		public void clearItems() {
			clearChildren();
			baseY.clear();
			targetScroll = 0.0F;
			contentHeight = 0.0F;
		}

		public void addItem(UiComponent component) {
			addChild(component);
			baseY.add(0.0F);
		}

		/** Lays items out vertically; called after every rebuild. */
		public void layout() {
			List<UiComponent> items = children();
			float cursor = 0.0F;
			for (int i = 0; i < items.size(); i++) {
				if (i < baseY.size()) {
					baseY.set(i, cursor);
				} else {
					baseY.add(cursor);
				}
				cursor += items.get(i).height() + spacing;
			}
			contentHeight = items.isEmpty() ? 0.0F : Math.max(0.0F, cursor - spacing);
			targetScroll = Anim.clamp(targetScroll, 0.0F, maxScroll());
			scroll = Anim.clamp(scroll, 0.0F, maxScroll());
		}

		public float contentHeight() {
			return contentHeight;
		}

		private float maxScroll() {
			return Math.max(0.0F, contentHeight - height);
		}

		public void scrollTo(float value) {
			targetScroll = Anim.clamp(value, 0.0F, maxScroll());
		}

		public float scroll() {
			return scroll;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			appear.snap(1.0F);
			List<UiComponent> items = children();
			float before = scroll;
			scroll = Anim.approach(scroll, targetScroll, UiTheme.get().speed(13.0F), deltaSeconds);
			barOpacity.set(Math.abs(scroll - before) > 0.4F || isHovered(mouseX, mouseY) ? 1.0F : 0.0F);
			barOpacity.update(UiTheme.get().speed(4.0F));
			for (int i = 0; i < items.size(); i++) {
				UiComponent item = items.get(i);
				float base = i < baseY.size() ? baseY.get(i) : 0.0F;
				item.setBounds(x, y + base - scroll, width, item.height());
				item.setLayerAlpha(layerAlpha);
				if (item.bottom() > y - 6.0F && item.y() < bottom() + 6.0F) {
					item.update(deltaSeconds, mouseX, mouseY);
				}
			}
			// Items may have changed height while animating (expanding cards), so the offsets are
			// recomputed right away instead of one frame later.
			layout();
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F || height <= 0.0F || width <= 0.0F) {
				return;
			}
			Render.scissor(graphics, x, y, width, height);
			for (UiComponent item : children()) {
				if (item.isVisible() && item.bottom() > y - 6.0F && item.y() < bottom() + 6.0F) {
					item.render(graphics, mouseX, mouseY, deltaSeconds);
				}
			}
			Render.unscissor(graphics);
			float max = maxScroll();
			if (max > 1.0F && barOpacity.get() > 0.02F) {
				float trackHeight = height - 4.0F;
				float barHeight = Math.max(24.0F, trackHeight * (height / Math.max(height, contentHeight)));
				float barY = y + 2.0F + (trackHeight - barHeight) * (scroll / max);
				Render.roundedRect(graphics, right() - 3.0F, barY, 3.0F, barHeight, 1.5F,
						Render.alpha(UiTheme.get().accent, 0.55F * barOpacity.get() * alpha));
			}
		}

		/** Only items that are actually inside the viewport receive input. */
		private boolean itemHandles(UiComponent item) {
			return item.isVisible() && item.bottom() > y && item.y() < bottom();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			List<UiComponent> items = children();
			for (int i = items.size() - 1; i >= 0; i--) {
				UiComponent item = items.get(i);
				if (itemHandles(item) && item.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			boolean handled = false;
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseReleased(mouseX, mouseY, button)) {
					handled = true;
				}
			}
			return handled;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
					return true;
				}
			}
			if (button == 0 && isHovered(mouseX, mouseY) && deltaY != 0.0F) {
				targetScroll = Anim.clamp(targetScroll - (float) deltaY, 0.0F, maxScroll());
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseScrolled(mouseX, mouseY, amount)) {
					return true;
				}
			}
			targetScroll = Anim.clamp(targetScroll - (float) amount * 32.0F, 0.0F, maxScroll());
			return true;
		}

		@Override
		public void reset() {
			super.reset();
			scroll = 0.0F;
			targetScroll = 0.0F;
		}
	}

	/** Rounded search field around a vanilla text box (so IME, clipboard and selection work). */
	public static final class SearchField extends UiComponent {
		private final EditBox box;
		private Runnable onClear;

		public SearchField(EditBox box) {
			this.box = box;
		}

		public EditBox box() {
			return box;
		}

		public void setOnClear(Runnable onClear) {
			this.onClear = onClear;
		}

		public void place(float x, float y, float width, float height) {
			setBounds(x, y, width, height);
			box.setX(Math.round(x + 26.0F));
			box.setY(Math.round(y + (height - 8.0F) * 0.5F));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			// Reserve room for the magnifier and the clear button.
			box.setWidth(Math.round(width - (box.getValue().isEmpty() ? 36.0F : 52.0F)));
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float focusAmount = box.isFocused() ? 1.0F : 0.0F;
			focus.set(focusAmount);
			focus.update(UiTheme.get().speed(14.0F));
			float focusValue = focus.get();
			float radius = Math.min(theme.radiusControl + 2.0F, height * 0.5F);
			Render.roundedRect(graphics, x, y, width, height, radius,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.surfaceHover, alpha), hover.get() * 0.6F));
			if (focusValue > 0.02F) {
				Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.accent, alpha * 0.7F * focusValue));
				if (theme.glow) {
					Render.glow(graphics, centerX(), centerY(), width * 0.6F, theme.accent, 0.12F * focusValue * alpha);
				}
			} else {
				Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			}
			// Magnifier glyph.
			float glyphX = x + 10.0F;
			float glyphY = centerY();
			int glyphColor = Render.alpha(theme.textFaint, alpha);
			Render.ring(graphics, glyphX - 5.0F, glyphY - 6.0F, 10.0F, 10.0F, 5.0F, 1.5F, glyphColor);
			Render.line(graphics, glyphX + 3.2F, glyphY + 2.2F, glyphX + 6.2F, glyphY + 5.2F, 1.6F, glyphColor);
			if (!box.getValue().isEmpty() && hover.get() > 0.05F) {
				float clearX = right() - 14.0F;
				Render.circle(graphics, clearX, centerY(), 6.0F, Render.alpha(theme.textFaint, alpha * 0.35F * hover.get()));
				Render.line(graphics, clearX - 2.4F, centerY() - 2.4F, clearX + 2.4F, centerY() + 2.4F, 1.4F,
						Render.alpha(0xFFFFFFFF, alpha * 0.8F));
				Render.line(graphics, clearX + 2.4F, centerY() - 2.4F, clearX - 2.4F, centerY() + 2.4F, 1.4F,
						Render.alpha(0xFFFFFFFF, alpha * 0.8F));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0 && !box.getValue().isEmpty() && mouseX >= right() - 22.0F) {
				box.setValue("");
				if (onClear != null) {
					onClear.run();
				}
				return true;
			}
			return button == 0;
		}
	}

	/** Small caption used between groups of settings. */
	public static final class SectionHeader extends UiComponent {
		private final String text;

		public SectionHeader(String text) {
			this.text = text;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Ui.sectionLabel(graphics, font(), text, x, y + appearOffset() + 2.0F, theme.textFaint, alpha);
			int labelWidth = font().width(text.toUpperCase(java.util.Locale.ROOT));
			Ui.divider(graphics, x + labelWidth + 8.0F, y + appearOffset() + 6.0F, Math.max(0.0F, width - labelWidth - 8.0F),
					theme, alpha);
		}
	}

	/** Small static text. */
	public static final class Label extends UiComponent {
		private String text;
		private int color;
		private boolean centered;
		private boolean bold;

		public Label(String text, int color) {
			this.text = text;
			this.color = color;
		}

		public Label centered() {
			this.centered = true;
			return this;
		}

		public Label bold() {
			this.bold = true;
			return this;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setColor(int color) {
			this.color = color;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			String value = Render.ellipsize(font(), text, width);
			int argb = Render.alpha(color, alpha);
			if (centered) {
				if (bold) {
					Render.boldText(graphics, font(), value, centerX() - font().width(value) * 0.5F, y + appearOffset(), argb, false);
				} else {
					Render.centeredText(graphics, font(), value, centerX(), y + appearOffset(), argb, false);
				}
			} else if (bold) {
				Render.boldText(graphics, font(), value, x, y + appearOffset(), argb, false);
			} else {
				Render.text(graphics, font(), value, x, y + appearOffset(), argb, false);
			}
		}
	}

	/** Thin progress/status element used by the footer and by list screens. */
	public static final class StatusChip extends UiComponent {
		private final String text;
		private int color;
		private boolean pulsing;

		public StatusChip(String text, int color) {
			this.text = text;
			this.color = color;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public void setPulsing(boolean pulsing) {
			this.pulsing = pulsing;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float pulse = pulsing ? 0.5F + 0.5F * Anim.pulse((float) (System.nanoTime() / 1.0E9), 2.0F) : 1.0F;
			float dotRadius = 3.0F;
			Ui.dot(graphics, x + dotRadius + 1.0F, centerY(), dotRadius,
					Render.alpha(color, alpha * pulse));
			Render.text(graphics, font(), Render.ellipsize(font(), text, width - 14.0F), x + 12.0F, centerY() - 4.0F,
					Render.alpha(theme.textDim, alpha), false);
		}
	}

	/** Returns a vertical list of the settings of a module, wrapped in their rows. */
	public static List<UiComponent> rowsFor(List<Setting<?>> settings, ChaosScreen screen, float width) {
		List<UiComponent> rows = new ArrayList<>();
		for (Setting<?> setting : settings) {
			UiComponent row = forSetting(setting, screen);
			if (row == null) {
				continue;
			}
			row.setBounds(0.0F, 0.0F, width, rowHeight(setting));
			row.setTooltip(setting.description);
			rows.add(row);
		}
		return rows;
	}

	public static float rowHeight(Setting<?> setting) {
		return switch (setting.kind()) {
			case NUMBER -> 26.0F;
			case TOGGLE, CHOICE, COLOR, KEY, TEXT, POSITION, ACTION -> 22.0F;
		};
	}

}
