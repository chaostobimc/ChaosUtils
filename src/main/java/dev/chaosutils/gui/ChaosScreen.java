package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Base class for every ChaosUtils screen.
 *
 * <p>Owns the component list, per-frame animation timing, the mouse routing (components
 * first, then vanilla children such as {@link EditBox}es), the shared text/colour popups and
 * a custom tooltip renderer. Input positions are taken from the last rendered frame, which
 * keeps everything independent of the 1.21.9+ {@code MouseButtonEvent} accessors - only
 * {@code button()} is read from the event itself.
 */
public abstract class ChaosScreen extends Screen {
	protected final List<UiComponent> components = new ArrayList<>();
	private final List<EditBox> inputs = new ArrayList<>();
	private Consumer<String> activeInputHandler;
	private EditBox activeInput;
	private UiComponent popup;
	private String popupTitle = "";
	private float lastMouseX;
	private float lastMouseY;
	private long lastFrameNanos;
	protected float deltaSeconds;
	protected final Anim.Value openAnim = new Anim.Value(0.0F, 9.0F);
	private boolean escapeArmed = true;

	protected ChaosScreen(Component title) {
		super(title);
	}

	protected abstract void buildLayout();

	@Override
	protected void init() {
		components.clear();
		inputs.clear();
		popup = null;
		activeInput = null;
		activeInputHandler = null;
		openAnim.snap(0.0F);
		escapeArmed = true;
		buildLayout();
	}

	public UiTheme theme() {
		return UiTheme.get();
	}

	/** Public accessor so widgets can measure text with the screen's font. */
	public net.minecraft.client.gui.Font font() {
		return this.font;
	}

	protected void add(UiComponent component) {
		components.add(component);
	}

	/** Registers a vanilla text field so it participates in vanilla input handling. */
	protected EditBox addInput(EditBox input) {
		inputs.add(input);
		addRenderableWidget(input);
		return input;
	}

	protected void clearInputs() {
		for (EditBox input : inputs) {
			removeWidget(input);
		}
		inputs.clear();
	}

	protected List<EditBox> inputs() {
		return inputs;
	}

	public void playClick(boolean positive) {
		if (!theme().sounds || minecraft == null || minecraft.level == null) {
			return;
		}
		try {
			var sound = SoundLookup.ui(SoundLookup.uiClick(), positive ? 1.7F : 1.2F, 0.35F);
			if (sound != null) {
				minecraft.getSoundManager().play(sound);
			}
		} catch (Throwable ignored) {
			// audio is optional
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		if (popup != null) {
			// ESC closes the popup first, handled in tick() by polling.
			return false;
		}
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		handlePolledKeys();
	}

	/** ESC/Enter inside popups is polled from GLFW so no input-event accessors are needed. */
	private void handlePolledKeys() {
		if (popup == null) {
			escapeArmed = true;
			return;
		}
		if (dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_ESCAPE)) {
			if (escapeArmed) {
				escapeArmed = false;
				closePopup(false);
			}
			return;
		}
		escapeArmed = true;
		if (dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_ENTER) || dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_KP_ENTER)) {
			closePopup(true);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		long now = System.nanoTime();
		deltaSeconds = lastFrameNanos == 0 ? 0.016F : Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
		lastFrameNanos = now;
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		openAnim.set(1.0F);
		openAnim.update(deltaSeconds);

		renderBackdrop(graphics, mouseX, mouseY, deltaTicks);

		float eased = Anim.easeOutCubic(openAnim.get());
		float offsetY = (1.0F - eased) * 12.0F;

		graphics.pose().pushMatrix();
		graphics.pose().translate(0.0F, offsetY);
		for (UiComponent component : components) {
			if (component.isVisible()) {
				component.render(graphics, mouseX, mouseY, deltaSeconds);
			}
		}
		if (popup != null) {
			renderPopup(graphics, mouseX, mouseY);
		}
		graphics.pose().popMatrix();

		renderTooltipLayer(graphics, mouseX, mouseY);
		super.render(graphics, mouseX, mouseY, deltaTicks);
	}

	protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		if (theme().backdropStyle == 3) {
			return;
		}
		if (theme().blur) {
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
		}
		Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme().background);
		if (theme().backdropStyle == 0) {
			Render.verticalGradient(graphics, 0.0F, 0.0F, this.width, this.height, 0x00000000, 0x66000000);
		}
	}

	private void renderTooltipLayer(GuiGraphics graphics, float mouseX, float mouseY) {
		if (popup != null || !theme().tooltips) {
			return;
		}
		String tooltip = hoveredTooltip(mouseX, mouseY);
		if (tooltip == null || tooltip.isEmpty()) {
			return;
		}
		String[] lines = tooltip.split("\n");
		int widest = 0;
		for (String line : lines) {
			widest = Math.max(widest, this.font.width(line));
		}
		float boxWidth = widest + 12.0F;
		float boxHeight = lines.length * 10.0F + 8.0F;
		float bx = Math.min(mouseX + 10.0F, this.width - boxWidth - 4.0F);
		float by = Math.min(mouseY + 12.0F, this.height - boxHeight - 4.0F);
		Render.shadowedPanel(graphics, bx, by, boxWidth, boxHeight, 4.0F, 0xF0101018, theme().accent);
		for (int i = 0; i < lines.length; i++) {
			Render.text(graphics, this.font, lines[i], bx + 6.0F, by + 4.0F + i * 10.0F, theme().text, false);
		}
	}

	private String hoveredTooltip(float mouseX, float mouseY) {
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.contains(mouseX, mouseY) && component.tooltip() != null) {
				return component.tooltip();
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int button = event.button();
		if (popup != null) {
			if (popup.mouseClicked(xFromClick(button), yFromClick(button), button) || popup.contains(lastMouseX, lastMouseY)) {
				return true;
			}
			closePopup(false);
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseClicked(lastMouseX, lastMouseY, button)) {
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		int button = event.button();
		if (popup != null && popup.mouseReleased(lastMouseX, lastMouseY, button)) {
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseReleased(lastMouseX, lastMouseY, button)) {
				return true;
			}
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		int button = event.button();
		if (popup != null && popup.mouseDragged(lastMouseX, lastMouseY, button, (float) deltaX, (float) deltaY)) {
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseDragged(lastMouseX, lastMouseY, button, (float) deltaX, (float) deltaY)) {
				return true;
			}
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseScrolled((float) mouseX, (float) mouseY, verticalAmount)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private float xFromClick(int button) {
		return lastMouseX;
	}

	private float yFromClick(int button) {
		return lastMouseY;
	}

	protected float mouseX() {
		return lastMouseX;
	}

	protected float mouseY() {
		return lastMouseY;
	}

	protected float deltaSeconds() {
		return deltaSeconds;
	}

	// -------------------------------------------------------------------- popups

	protected void openTextEditor(Setting.Text setting) {
		openTextEditor(setting.label, setting.get(), setting.maxLength(), setting::set);
	}

	protected void openTextEditor(String title, String initial, int maxLength, Consumer<String> onAccept) {
		this.popupTitle = title;
		this.activeInputHandler = onAccept;
		this.activeInput = new EditBox(this.font, 0, 0, 200, 16, Component.literal(title));
		this.activeInput.setMaxLength(maxLength);
		this.activeInput.setValue(initial);
		this.activeInput.setBordered(false);
		this.activeInput.setTextColor(0xFFF2F2F7);
		addInput(this.activeInput);
		setFocused(this.activeInput);
		rebuildPopup();
	}

	protected void openColorPicker(Setting.Color setting) {
		this.popupTitle = setting.label;
		UiWidgets.ColorPicker picker = new UiWidgets.ColorPicker(this, setting);
		this.popup = picker;
		this.activeInputHandler = null;
		layoutPopup();
	}

	private void rebuildPopup() {
		if (activeInput == null) {
			return;
		}
		this.popup = new UiWidgets.TextPopup(this);
		layoutPopup();
	}

	private void layoutPopup() {
		if (popup == null) {
			return;
		}
		float popupWidth = Math.max(220.0F, popup.width());
		float popupHeight = Math.max(90.0F, popup.height());
		float px = (this.width - popupWidth) * 0.5F;
		float py = (this.height - popupHeight) * 0.5F;
		popup.setBounds(px, py, popupWidth, popupHeight);
		if (popup instanceof UiWidgets.TextPopup textPopup) {
			textPopup.layout(px, py, popupWidth, popupHeight, activeInput);
		}
		if (popup instanceof UiWidgets.ColorPicker picker) {
			picker.layout(px, py, popupWidth, popupHeight);
		}
	}

	protected void closePopup(boolean accept) {
		if (accept && activeInput != null && activeInputHandler != null) {
			activeInputHandler.accept(activeInput.getValue());
		}
		if (activeInput != null) {
			removeWidget(activeInput);
			inputs.remove(activeInput);
			activeInput = null;
		}
		activeInputHandler = null;
		popup = null;
		setFocused(null);
	}

	private void renderPopup(GuiGraphics graphics, float mouseX, float mouseY) {
		Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, 0x88000000);
		popup.render(graphics, mouseX, mouseY, deltaSeconds);
		Render.centeredText(graphics, this.font, popupTitle, popup.x() + popup.width() * 0.5F, popup.y() + 8.0F, theme().text, false);
	}

	protected String popupTitle() {
		return popupTitle;
	}

	protected EditBox activeInput() {
		return activeInput;
	}

	protected Minecraft client() {
		return this.minecraft;
	}

	@Override
	public void removed() {
		clearInputs();
		super.removed();
	}

	/** Hook used by the click GUI to open the HUD editor; overridden where meaningful. */
	public void openHudEditor() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(new HudEditorScreen(this));
		}
	}
}
