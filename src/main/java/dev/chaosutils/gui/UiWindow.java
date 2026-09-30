package dev.chaosutils.gui;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The floating window every ChaosUtils screen lives in.
 *
 * <p>The shell owns the geometry (drag, resize, persistence, opening and closing animation) while
 * the screen owns the content. Content is laid out in window local coordinates starting at
 * {@code (0, 0)} - the screen translates the pose by {@link #x()}/{@link #y()} while drawing and
 * subtracts them while hit testing, so dragging never has to rebuild a single widget.
 */
public final class UiWindow {
	public static final float TITLE_HEIGHT = 34.0F;
	public static final float MIN_WIDTH = 360.0F;
	public static final float MIN_HEIGHT = 220.0F;
	private static final float RESIZE_GRIP = 16.0F;
	private static final float EDGE = 5.0F;

	private final String key;
	private float x;
	private float y;
	private float width;
	private float height;

	private boolean draggable = true;
	private boolean resizable = true;
	private boolean dragging;
	private boolean resizing;
	private float grabX;
	private float grabY;
	private float resizeFromWidth;
	private float resizeFromHeight;
	private boolean resized;
	private boolean moved;

	private final Anim.Value appear = new Anim.Value(0.0F, 9.0F);
	private final Anim.Value close = new Anim.Value(0.0F, 13.0F);
	private final Anim.Value closeHover = new Anim.Value(0.0F, 16.0F);
	private boolean closing;

	public UiWindow(String key) {
		this.key = key;
	}

	// ------------------------------------------------------------------ state

	/** Restores the remembered geometry (clamped to this screen) or centres the default size. */
	public void open(int screenWidth, int screenHeight, float preferredWidth, float preferredHeight) {
		float maxWidth = Math.max(MIN_WIDTH, screenWidth - 40.0F);
		float maxHeight = Math.max(MIN_HEIGHT, screenHeight - 40.0F);
		width = Anim.clamp(ChaosConfig.uiFloat(key + ".width", preferredWidth), MIN_WIDTH, maxWidth);
		height = Anim.clamp(ChaosConfig.uiFloat(key + ".height", preferredHeight), MIN_HEIGHT, maxHeight);
		x = ChaosConfig.uiFloat(key + ".x", Float.NaN);
		y = ChaosConfig.uiFloat(key + ".y", Float.NaN);
		if (Float.isNaN(x) || Float.isNaN(y)) {
			center(screenWidth, screenHeight);
		} else {
			x = Anim.clamp(x, 8.0F, Math.max(8.0F, screenWidth - width - 8.0F));
			y = Anim.clamp(y, 8.0F, Math.max(8.0F, screenHeight - height - 8.0F));
		}
		appear.snap(0.0F);
		close.snap(0.0F);
		closing = false;
	}

	/** Instantly shows the window; used by screens that are opened as a tool, not entered. */
	public void snapOpen() {
		appear.snap(1.0F);
		close.snap(0.0F);
		closing = false;
	}

	public void close() {
		if (!closing) {
			closing = true;
			close.set(1.0F);
		}
	}

	public boolean isClosing() {
		return closing;
	}

	public boolean isGone() {
		return closing && Anim.clamp01(close.get()) > 0.98F;
	}

	/** Advances the shell animations. The pointer drives the close button highlight. */
	public void update(float deltaSeconds, float mouseX, float mouseY) {
		appear.set(1.0F);
		appear.update(deltaSeconds, UiTheme.get().speed(9.0F));
		closeHover.set(!closing && isOverCloseButton(mouseX, mouseY) ? 1.0F : 0.0F);
		closeHover.update(deltaSeconds, UiTheme.get().speed(16.0F));
		if (closing) {
			close.update(deltaSeconds, UiTheme.get().speed(13.0F));
		}
	}

	/** Hit box of the title bar close button. */
	public boolean isOverCloseButton(float mouseX, float mouseY) {
		float size = 22.0F;
		float left = right() - size - 12.0F;
		float top = y + (TITLE_HEIGHT - size) * 0.5F;
		return mouseX >= left && mouseX <= left + size && mouseY >= top && mouseY <= top + size;
	}

	public float closeHover() {
		return closeHover.get();
	}

	/** Round close button with an animated hover state; drawn above the header. */
	public void renderCloseButton(GuiGraphics graphics, UiTheme theme, float alpha) {
		if (alpha <= 0.01F) {
			return;
		}
		float size = 22.0F;
		float left = right() - size - 12.0F;
		float top = y + (TITLE_HEIGHT - size) * 0.5F + slide();
		float hover = closeHover.get();
		Render.circle(graphics, left + size * 0.5F, top + size * 0.5F, size * 0.5F,
				Render.alpha(theme.negative, (0.10F + 0.35F * hover) * alpha));
		int color = Render.mix(theme.textDim, theme.negative, hover);
		Render.line(graphics, left + 7.0F, top + 7.0F, left + size - 7.0F, top + size - 7.0F, 1.6F,
				Render.alpha(color, alpha));
		Render.line(graphics, left + size - 7.0F, top + 7.0F, left + 7.0F, top + size - 7.0F, 1.6F,
				Render.alpha(color, alpha));
	}

	public void center(int screenWidth, int screenHeight) {
		x = Math.round((screenWidth - width) * 0.5F);
		y = Math.round((screenHeight - height) * 0.5F);
	}

	// ------------------------------------------------------------- geometry

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float width() {
		return width;
	}

	public float height() {
		return height;
	}

	public float right() {
		return x + width;
	}

	public float bottom() {
		return y + height;
	}

	/** Left edge of the content area in window local coordinates (the body spans the full width). */
	public float bodyX() {
		return 0.0F;
	}

	/** Top of the content area, in window local coordinates. */
	public float bodyY() {
		return TITLE_HEIGHT;
	}

	/** Height of the content area. */
	public float bodyHeight() {
		return height - TITLE_HEIGHT;
	}

	/** Width of the content area. */
	public float bodyWidth() {
		return width;
	}

	public float titleHeight() {
		return TITLE_HEIGHT;
	}

	/** Alias kept for the screens: starts the closing animation. */
	public void beginClose() {
		close();
	}

	public void setSize(float width, float height) {
		this.width = Math.max(MIN_WIDTH, width);
		this.height = Math.max(MIN_HEIGHT, height);
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float titleCenterY() {
		return y + TITLE_HEIGHT * 0.5F;
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
	}

	public boolean isOverTitleBar(float mouseX, float mouseY) {
		return draggable && mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= y + TITLE_HEIGHT;
	}

	public boolean isOverResizeGrip(float mouseX, float mouseY) {
		return resizable && mouseX >= right() - RESIZE_GRIP && mouseX <= right() + 2.0F
				&& mouseY >= bottom() - RESIZE_GRIP && mouseY <= bottom() + 2.0F;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/** Combined alpha: opening animation times closing animation. */
	public float alpha() {
		return Anim.clamp01(appear.get()) * (1.0F - Anim.easeOutQuint(Anim.clamp01(close.get())));
	}

	/** Slide offset of the opening animation, in pixels. */
	public float slide() {
		return (1.0F - Anim.easeOutQuint(Anim.clamp01(appear.get()))) * 14.0F;
	}

	public boolean wasResized() {
		boolean value = resized;
		resized = false;
		return value;
	}

	public boolean wasMoved() {
		boolean value = moved;
		moved = false;
		return value;
	}

	/** Forgets the stored geometry so the next open centres the window again. */
	public void resetGeometry() {
		ChaosConfig.setUi(key + ".x", null);
		ChaosConfig.setUi(key + ".y", null);
		ChaosConfig.setUi(key + ".width", null);
		ChaosConfig.setUi(key + ".height", null);
	}

	private void persist() {
		ChaosConfig.setUi(key + ".x", x);
		ChaosConfig.setUi(key + ".y", y);
		ChaosConfig.setUi(key + ".width", width);
		ChaosConfig.setUi(key + ".height", height);
	}

	// ----------------------------------------------------------------- render

	/** Draws background, ring and title bar separator. Call before the content. */
	public void renderShell(GuiGraphics graphics, UiTheme theme) {
		float alpha = alpha();
		if (alpha <= 0.01F) {
			return;
		}
		float drawY = y + slide();
		Ui.window(graphics, x, drawY, width, height, theme.radius, theme, alpha);
		Ui.header(graphics, x, drawY, width, TITLE_HEIGHT, theme.radius, theme, alpha);
		Render.rect(graphics, x + 1.0F, drawY + TITLE_HEIGHT - 1.0F, width - 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, alpha));
	}

	// ------------------------------------------------------------------ input

	public boolean mouseClicked(float mouseX, float mouseY, int button) {
		if (button != 0 || closing) {
			return false;
		}
		if (isOverResizeGrip(mouseX, mouseY)) {
			resizing = true;
			resizeFromWidth = width;
			resizeFromHeight = height;
			grabX = mouseX;
			grabY = mouseY;
			return true;
		}
		if (isOverTitleBar(mouseX, mouseY)) {
			dragging = true;
			grabX = mouseX - x;
			grabY = mouseY - y;
			return true;
		}
		return contains(mouseX, mouseY);
	}

	public boolean mouseDragged(float mouseX, float mouseY, int button) {
		if (button != 0) {
			return false;
		}
		if (resizing) {
			float newWidth = Math.max(MIN_WIDTH, resizeFromWidth + (mouseX - grabX));
			float newHeight = Math.max(MIN_HEIGHT, resizeFromHeight + (mouseY - grabY));
			if (Math.abs(newWidth - width) > 0.5F || Math.abs(newHeight - height) > 0.5F) {
				width = newWidth;
				height = newHeight;
				resized = true;
			}
			return true;
		}
		if (dragging) {
			x = mouseX - grabX;
			y = mouseY - grabY;
			moved = true;
			return true;
		}
		return false;
	}

	public boolean mouseReleased(int button) {
		if (button != 0) {
			return false;
		}
		boolean wasInteracting = dragging || resizing;
		dragging = false;
		resizing = false;
		if (wasInteracting) {
			persist();
			return true;
		}
		return false;
	}

	public boolean isInteracting() {
		return dragging || resizing;
	}
}
