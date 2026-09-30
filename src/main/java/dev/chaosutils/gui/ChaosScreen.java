package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Base class of every ChaosUtils screen.
 *
 * <p>Owns the floating window, the component list, the modal layer, the toast stack and the custom
 * tooltip renderer. Screens only describe their layout in {@link #buildLayout()}.
 *
 * <p>Two details matter for the renderer: content is clipped to the window body with a scissor, and
 * vanilla widgets (text fields) are only registered while no modal is open, so a dialog can never
 * end up behind a shadow of a text field. Everything is drawn through {@link Render} - the game's
 * own {@code renderBackground} is never called from here, because the screen pipeline already ran
 * it (including the frame's single blur) before {@code render} is reached.
 */
public abstract class ChaosScreen extends Screen {
	protected final Screen parent;
	protected final UiWindow window;
	private final float defaultWidth;
	private final float defaultHeight;

	private final List<UiComponent> content = new ArrayList<>();
	private final List<EditBox> contentInputs = new ArrayList<>();
	private final List<EditBox> modalInputs = new ArrayList<>();
	private final List<UiModals.Modal> modals = new ArrayList<>();
	private final List<Toast> toasts = new ArrayList<>();

	private final Anim.Value appear = new Anim.Value(0.0F, 10.0F);
	private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);
	private boolean closing;
	private boolean closeDelivered;
	private long closeRequestedAt;
	private long lastFrameNanos;
	protected float deltaSeconds;
	private float mouseX;
	private float mouseY;
	/** Window position the current layout was built for; dragging shifts the drawing instead of
	 *  rebuilding (which would restart every entrance animation every frame). */
	private float layoutX;
	private float layoutY;
	private float offsetX;
	private float offsetY;
	private UiWidgets.KeyField listeningField;

	protected ChaosScreen(Screen parent, Component title, String windowKey, float defaultWidth, float defaultHeight) {
		super(title);
		this.parent = parent;
		this.window = new UiWindow(windowKey);
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	// -------------------------------------------------------------- lifecycle

	/** Builds the component list; called on init and whenever the layout changes. */
	protected abstract void buildLayout();

	@Override
	protected void init() {
		content.clear();
		contentInputs.clear();
		modalInputs.clear();
		modals.clear();
		listeningField = null;
		closing = false;
		closeDelivered = false;
		appear.snap(0.0F);
		exit.snap(0.0F);
		window.open(this.width, this.height, defaultWidth, defaultHeight);
		layoutX = window.x();
		layoutY = window.y();
		offsetX = 0.0F;
		offsetY = 0.0F;
		buildLayoutSafely();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Animated close: the screen asks the game to switch back once the fade has played out. */
	public void requestClose() {
		if (!closing) {
			closing = true;
			closeRequestedAt = System.nanoTime();
			exit.set(1.0F);
			for (UiModals.Modal modal : modals) {
				modal.close();
			}
			window.beginClose();
			clearModalInputs();
		}
	}

	/** Called once the close animation has finished, before the parent screen is restored. */
	protected void onClosed() {
	}

	@Override
	public void onClose() {
		requestClose();
	}

	@Override
	public void removed() {
		clearInputs();
		super.removed();
	}

	@Override
	public void tick() {
		super.tick();
		if (!closing || closeDelivered) {
			return;
		}
		// Normally the exit animation decides; the time limit is the guarantee that the player can
		// always leave the screen, whatever happens to the animation.
		boolean timedOut = System.nanoTime() - closeRequestedAt > CLOSE_TIMEOUT_NANOS;
		if (window.isGone() || timedOut) {
			closeDelivered = true;
			onClosed();
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}
	}

	/** Hard limit for the closing animation, in nanoseconds. */
	private static final long CLOSE_TIMEOUT_NANOS = 500_000_000L;

	// -------------------------------------------------------------- accessors

	public UiTheme theme() {
		return UiTheme.get();
	}

	/** Font accessor for widgets: the bundled interface face, or vanilla as a fallback. */
	public Font font() {
		return UiFonts.font();
	}

	/** Font that can render the given text (exotic chat text stays on the vanilla font). */
	public Font font(String text) {
		return UiFonts.pick(text);
	}

	protected UiWindow window() {
		return window;
	}

	protected float bodyX() {
		return window.bodyX();
	}

	protected float bodyY() {
		return window.bodyY();
	}

	protected float bodyWidth() {
		return window.bodyWidth();
	}

	protected float bodyHeight() {
		return window.bodyHeight();
	}

	protected float alpha() {
		return Anim.clamp01(appear.get()) * window.alpha();
	}

	protected float mouseX() {
		return mouseX;
	}

	protected float mouseY() {
		return mouseY;
	}

	protected boolean hasModal() {
		return !modals.isEmpty();
	}

	protected void add(UiComponent component) {
		content.add(component);
	}

	protected void clearContent() {
		content.clear();
	}

	protected List<UiComponent> components() {
		return content;
	}

	/**
	 * Builds the layout and turns a failure into a visible message instead of an empty window.
	 *
	 * <p>A screen whose {@code buildLayout} throws would otherwise render nothing but its frame -
	 * which is impossible to tell apart from "the interface is broken", so the error is logged and
	 * shown.
	 */
	private void buildLayoutSafely() {
		try {
			buildLayout();
		} catch (Throwable throwable) {
			dev.chaosutils.ChaosUtils.LOGGER.error("ChaosUtils: layout of {} failed",
					getClass().getSimpleName(), throwable);
			content.clear();
			UiWidgets.Label error = new UiWidgets.Label("Layout error - see latest.log", theme().negative);
			error.setBounds(24.0F, window.y() + window.titleHeight() + 24.0F, 320.0F, 14.0F);
			content.add(error);
		}
	}

	/**
	 * Rebuilds the layout while keeping the window position and size.
	 *
	 * <p>The registered text fields are dropped first: a rebuild creates new ones, and a screen
	 * that refreshes on every toggle would otherwise pile up dead widgets (and draw them).
	 */
	protected void refresh() {
		clearInputs();
		content.clear();
		layoutX = window.x();
		layoutY = window.y();
		offsetX = 0.0F;
		offsetY = 0.0F;
		buildLayoutSafely();
		for (UiComponent component : content) {
			component.snapAppear();
		}
	}

	/** Content coordinates of the pointer (the layout is anchored to the window position). */
	protected float contentMouseX() {
		return mouseX - offsetX;
	}

	protected float contentMouseY() {
		return mouseY - offsetY;
	}

	// ---------------------------------------------------------------- inputs

	/** Registers a vanilla text field so IME, clipboard and selection behave like vanilla. */
	public EditBox addInput(EditBox input) {
		contentInputs.add(input);
		addRenderableWidget(input);
		return input;
	}

	public void removeInput(EditBox input) {
		contentInputs.remove(input);
		removeWidget(input);
	}

	/** Registers a text field that belongs to a modal (stays visible above the dim layer). */
	public EditBox addModalInput(EditBox input) {
		modalInputs.add(input);
		addRenderableWidget(input);
		setFocused(input);
		return input;
	}

	protected void clearInputs() {
		for (EditBox input : contentInputs) {
			removeWidget(input);
		}
		for (EditBox input : modalInputs) {
			removeWidget(input);
		}
		contentInputs.clear();
		modalInputs.clear();
	}

	private void hideContentInputs() {
		for (EditBox input : contentInputs) {
			removeWidget(input);
		}
	}

	private void showContentInputs() {
		for (EditBox input : contentInputs) {
			addRenderableWidget(input);
		}
		setFocused(null);
	}

	private void clearModalInputs() {
		for (EditBox input : modalInputs) {
			removeWidget(input);
		}
		modalInputs.clear();
		setFocused(null);
	}

	// ----------------------------------------------------------------- modals

	protected void pushModal(UiModals.Modal modal) {
		modal.setScreenBounds(this.width, this.height);
		hideContentInputs();
		modals.add(modal);
	}

	/** Hook for screens that need to react when one of their dialogs is done. */
	protected void modalClosed(UiModals.Modal modal) {
	}

	/** Text editor dialog used by string settings and by the list screens. */
	public void openTextModal(String title, String initial, int maxLength, Consumer<String> onAccept) {
		EditBox box = new EditBox(UiFonts.font(), 0, 0, 200, 16, Component.literal(title));
		box.setBordered(false);
		box.setTextColor(0xFFF4F5FA);
		UiModals.TextPrompt prompt = new UiModals.TextPrompt(title, "Type here…", initial, maxLength, onAccept, box);
		prompt.place(this.width * 0.5F, this.height * 0.5F);
		addModalInput(box);
		pushModal(prompt);
	}

	/** Colour picker for any colour setting. */
	public void openColorModal(Setting.Color setting) {
		UiModals.ColorPicker picker = UiModals.colorPicker(setting, this::addModalInput);
		picker.place(this.width * 0.5F, this.height * 0.5F);
		pushModal(picker);
	}

	public void openConfirm(String title, String message, String confirmLabel, Runnable onConfirm) {
		pushModal(UiModals.confirm(title, message, confirmLabel, onConfirm));
	}

	public void openInfo(String title, String message) {
		pushModal(UiModals.info(title, message));
	}

	// ----------------------------------------------------------------- toasts

	public void toast(String message) {
		toast(null, message, theme().accent);
	}

	public void toast(String title, String message, int color) {
		Toast toast = new Toast(title, message, color);
		toast.slide.snap(0.0F);
		toasts.add(toast);
		while (toasts.size() > 4) {
			toasts.remove(0);
		}
	}

	// ------------------------------------------------------------------ sound

	public void playClick(boolean positive) {
		if (!theme().sounds || this.minecraft == null || this.minecraft.level == null) {
			return;
		}
		try {
			var sound = SoundLookup.ui(SoundLookup.uiClick(), positive ? 1.7F : 1.2F, 0.35F);
			if (sound != null) {
				this.minecraft.getSoundManager().play(sound);
			}
		} catch (Throwable ignored) {
			// UI sounds are optional
		}
	}

	// ----------------------------------------------------------------- render

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		long now = System.nanoTime();
		deltaSeconds = lastFrameNanos == 0 ? 0.016F : Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
		lastFrameNanos = now;
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		appear.set(1.0F);
		appear.update(deltaSeconds, theme().speed(10.0F));
		if (closing) {
			exit.update(deltaSeconds, theme().speed(14.0F));
		}
		tickAnimations();

		Backdrop.render(graphics, this, theme());

		// ------- window and content
		window.update(deltaSeconds, mouseX, mouseY);
		offsetX = window.x() - layoutX;
		offsetY = window.y() - layoutY;
		float windowAlpha = alpha();
		boolean modalOpen = !modals.isEmpty();
		float contentAlpha = windowAlpha * (modalOpen ? 0.35F : 1.0F);
		window.renderShell(graphics, theme());
		renderHeader(graphics, windowAlpha);
		window.renderCloseButton(graphics, theme(), windowAlpha);

		Render.scissor(graphics, layoutX + offsetX + 1.0F, layoutY + offsetY + 1.0F,
				window.width() - 2.0F, window.height() - 2.0F);
		graphics.pose().pushMatrix();
		graphics.pose().translate(offsetX, offsetY);
		for (UiComponent component : content) {
			if (component.isVisible()) {
				component.setLayerAlpha(contentAlpha);
				component.render(graphics, contentMouseX(), contentMouseY(), deltaSeconds);
			}
		}
		graphics.pose().popMatrix();
		Render.unscissor(graphics);

		// ------- vanilla widgets (text fields)
		super.render(graphics, mouseX, mouseY, deltaTicks);

		// ------- content that lives in screen space instead of inside the window
		renderScreenSpace(graphics, mouseX, mouseY);

		// ------- modals
		for (int i = 0; i < modals.size(); i++) {
			UiModals.Modal modal = modals.get(i);
			modal.setLayerAlpha(1.0F);
			modal.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		// ------- toasts (always on top of the interface)
		renderToasts(graphics);

		// ------- tooltip + footer
		renderFooter(graphics, windowAlpha);
		renderTooltip(graphics);
	}

	/**
	 * Screens may draw and interact outside of the window (the HUD editor drags its overlay ghosts
	 * across the whole screen). Called after the window, before the modals.
	 */
	protected void renderScreenSpace(GuiGraphics graphics, float mouseX, float mouseY) {
	}

	/** Screen-space click. Returning {@code true} consumes the click. */
	protected boolean mouseClickedScreenSpace(float mouseX, float mouseY, int button) {
		return false;
	}

	/** Screen-space drag, checked before the window so a drag can leave the window. */
	protected boolean mouseDraggedScreenSpace(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		return false;
	}

	/** Screen-space release, checked before the window for the same reason. */
	protected boolean mouseReleasedScreenSpace(float mouseX, float mouseY, int button) {
		return false;
	}

	/** Window header text; screens can replace it to show context. */
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		String text = this.title.getString();
		Render.boldText(graphics, font(text), text, window.x() + 18.0F, window.titleCenterY() - 4.0F,
				Render.alpha(theme().text, alpha), false);
	}

	private float layoutBodyX() {
		return layoutX;
	}

	private float layoutBodyY() {
		return layoutY + window.titleHeight();
	}

	/** Optional footer line at the bottom of the content area. */
	protected void renderFooter(GuiGraphics graphics, float alpha) {
	}

	private void renderToasts(GuiGraphics graphics) {
		if (toasts.isEmpty()) {
			return;
		}
		UiTheme theme = theme();
		float baseY = this.height - 30.0F;
		for (int i = toasts.size() - 1; i >= 0; i--) {
			Toast toast = toasts.get(i);
			float alpha = toast.alpha();
			if (alpha <= 0.01F) {
				continue;
			}
			String text = toast.title == null || toast.title.isEmpty()
					? toast.message
					: toast.title + "  ·  " + toast.message;
			float width = Math.min(300.0F, this.font.width(text) + 40.0F);
			float x = (this.width - width) * 0.5F;
			float y = baseY - (toasts.size() - 1 - i) * 26.0F + toast.offset();
			Render.softShadow(graphics, x, y, width, 22.0F, 11.0F, 0.8F * alpha);
			Render.roundedRect(graphics, x, y, width, 22.0F, 11.0F, Render.alpha(theme.windowBottom, alpha));
			Render.ring(graphics, x, y, width, 22.0F, 11.0F, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			Ui.dot(graphics, x + 12.0F, y + 11.0F, 3.0F, Render.alpha(toast.color, alpha));
			Render.text(graphics, this.font, Render.ellipsize(this.font, text, width - 34.0F), x + 22.0F, y + 7.0F,
					Render.alpha(theme.text, alpha), false);
		}
	}

	private void renderTooltip(GuiGraphics graphics) {
		if (!theme().tooltips || hasModal()) {
			return;
		}
		String tooltip = hoveredTooltip();
		if (tooltip == null || tooltip.isEmpty()) {
			return;
		}
		UiTheme theme = theme();
		List<String> lines = new ArrayList<>();
		for (String raw : tooltip.split("\n")) {
			lines.addAll(UiModals.Dialog.wrap(raw, 190.0F));
		}
		int widest = 0;
		for (String line : lines) {
			widest = Math.max(widest, this.font.width(line));
		}
		float boxWidth = widest + 18.0F;
		float boxHeight = lines.size() * 11.0F + 12.0F;
		float x = Anim.clamp(mouseX + 12.0F, 4.0F, this.width - boxWidth - 4.0F);
		float y = Anim.clamp(mouseY + 14.0F, 4.0F, this.height - boxHeight - 4.0F);
		Render.softShadow(graphics, x, y, boxWidth, boxHeight, 7.0F, 1.0F);
		Render.roundedRect(graphics, x, y, boxWidth, boxHeight, 7.0F, Render.alpha(0xF4141420, 0.98F));
		Render.ring(graphics, x, y, boxWidth, boxHeight, 7.0F, 1.0F, Render.alpha(theme.outlineStrong, 1.0F));
		for (int i = 0; i < lines.size(); i++) {
			int color = i == 0 ? theme.text : theme.textDim;
			Render.text(graphics, this.font, lines.get(i), x + 9.0F, y + 6.0F + i * 11.0F, color, false);
		}
	}

	private String hoveredTooltip() {
		if (!modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			return top.contains(mouseX, mouseY) ? top.tooltip() : null;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (!component.isVisible() || !component.contains(mouseX, mouseY)) {
				continue;
			}
			String tip = deepestTooltip(component);
			if (tip != null) {
				return tip;
			}
		}
		return null;
	}

	/** Walks the component tree so a hovered setting row wins over its card. */
	private String deepestTooltip(UiComponent component) {
		for (UiComponent child : component.children()) {
			if (child.isVisible() && child.contains(mouseX, mouseY)) {
				String tip = deepestTooltip(child);
				if (tip != null) {
					return tip;
				}
			}
		}
		return component.tooltip();
	}

	// ------------------------------------------------------------------ input

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		this.mouseX = clickX;
		this.mouseY = clickY;
		if (closing) {
			return true;
		}
		if (!modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			top.setScreenBounds(this.width, this.height);
			return top.mouseClicked(clickX, clickY, button) || super.mouseClicked(event, doubled);
		}
		// A key field that is waiting for a key must not be disturbed by plain clicks elsewhere.
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseClicked(clickX - offsetX, clickY - offsetY, button)) {
				return true;
			}
		}
		if (window.isOverCloseButton(clickX, clickY)) {
			playClick(false);
			requestClose();
			return true;
		}
		if (window.mouseClicked(clickX, clickY, button)) {
			return true;
		}
		if (mouseClickedScreenSpace(clickX, clickY, button)) {
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		if (!modals.isEmpty()) {
			return modals.get(modals.size() - 1).mouseReleased(clickX, clickY, button) || super.mouseReleased(event);
		}
		boolean handled = mouseReleasedScreenSpace(clickX, clickY, button);
		if (window.mouseReleased(button)) {
			handled = true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseReleased(clickX - offsetX, clickY - offsetY, button)) {
				handled = true;
			}
		}
		if (window.wasResized()) {
			// A resize changes the available width, so the layout is rebuilt once at the end.
			refresh();
			handled = true;
		}
		return handled || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		if (!modals.isEmpty()) {
			return modals.get(modals.size() - 1).mouseDragged(clickX, clickY, button, (float) deltaX, (float) deltaY)
					|| super.mouseDragged(event, deltaX, deltaY);
		}
		if (mouseDraggedScreenSpace(clickX, clickY, button, (float) deltaX, (float) deltaY)) {
			return true;
		}
		if (window.mouseDragged(clickX, clickY, button)) {
			// Dragging only shifts the drawing (see offsetX/offsetY); no rebuild needed.
			return true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseDragged(clickX - offsetX, clickY - offsetY, button,
					(float) deltaX, (float) deltaY)) {
				return true;
			}
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!modals.isEmpty()) {
			return true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible()
					&& component.mouseScrolled((float) mouseX - offsetX, (float) mouseY - offsetY, verticalAmount)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (closing) {
			return true;
		}
		// Escape closes the top modal first, then the screen - never the game menu.
		if (event.isEscape()) {
			if (listeningField != null) {
				listeningField.cancelListening();
				listeningField = null;
				return true;
			}
			if (!modals.isEmpty()) {
				modals.get(modals.size() - 1).close();
				return true;
			}
			requestClose();
			return true;
		}
		// Enter accepts the top modal (text prompts and colour pickers).
		if ((event.key() == 257 || event.key() == 335) && !modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			if (top instanceof UiModals.TextPrompt prompt) {
				prompt.accept();
				return true;
			}
		}
		return super.keyPressed(event);
	}

	/** Screens call this while a hotkey field is capturing input. */
	protected void setListeningField(UiWidgets.KeyField field) {
		this.listeningField = field;
	}

	/** Tracks modal completion so inputs can be restored exactly once. */
	protected final void updateModals(float deltaSeconds) {
		for (int i = modals.size() - 1; i >= 0; i--) {
			UiModals.Modal modal = modals.get(i);
			modal.setScreenBounds(this.width, this.height);
			modal.update(deltaSeconds, mouseX, mouseY);
			if (modal.isFinished()) {
				modals.remove(i);
				clearModalInputs();
				if (modals.isEmpty()) {
					showContentInputs();
				}
				modalClosed(modal);
			}
		}
	}

	/** Runs the per-frame update pass of every content component. */
	protected final void updateContent(float deltaSeconds) {
		for (UiComponent component : content) {
			if (component.isVisible()) {
				component.setLayerAlpha(alpha());
				component.update(deltaSeconds, contentMouseX(), contentMouseY());
			}
		}
	}

	public void openHudEditor() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(new HudEditorScreen(this));
		}
	}

	// ------------------------------------------------------------------ toasts

	private static final class Toast {
		private final String title;
		private final String message;
		private final int color;
		private final Anim.Value life = new Anim.Value(0.0F, 6.0F);
		private final Anim.Value slide = new Anim.Value(0.0F, 12.0F);
		private float age;

		private Toast(String title, String message, int color) {
			this.title = title;
			this.message = message;
			this.color = color;
		}

		private float alpha() {
			return Anim.clamp01(slide.get()) * life.get();
		}

		private float offset() {
			return (1.0F - Anim.easeOutQuint(slide.get())) * 14.0F;
		}

		private void update(float deltaSeconds) {
			age += deltaSeconds;
			slide.set(1.0F);
			slide.update(deltaSeconds, UiTheme.get().speed(13.0F));
			life.set(age > 3.0F ? 0.0F : 1.0F);
			life.update(deltaSeconds, age > 3.0F ? UiTheme.get().speed(6.0F) : 60.0F);
		}

		private boolean expired() {
			return age > 3.6F;
		}
	}

	/** Updates the toast stack; part of the screen's per-frame update pass. */
	protected final void updateToasts(float deltaSeconds) {
		for (int i = toasts.size() - 1; i >= 0; i--) {
			Toast toast = toasts.get(i);
			toast.update(deltaSeconds);
			if (toast.expired()) {
				toasts.remove(i);
			}
		}
	}

	/** Single entry point for the per-frame update of everything this screen owns. */
	protected final void tickAnimations() {
		updateModals(deltaSeconds);
		updateContent(deltaSeconds);
		updateToasts(deltaSeconds);
	}
}
