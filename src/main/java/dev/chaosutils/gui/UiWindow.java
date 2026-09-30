package dev.chaosutils.gui;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Geometry, chrome and dragging of a floating ChaosUtils window.
 *
 * <p>The window is a single, opaque, rounded panel: it owns its drop shadow, its body gradient, the
 * hairline outline, the accent strip along the top edge and the close button. Content is laid out in
 * window local coordinates, so a screen never has to think about where the window currently is.
 */
public final class UiWindow {
	public static final float TITLE_HEIGHT = 30.0F;
	public static final float MIN_WIDTH = 360.0F;
	public static final float MIN_HEIGHT = 220.0F;
	private static final float RESIZE_GRIP = 18.0F;
	private static final float CLOSE_SIZE = 20.0F;

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
	private final Anim.Value gripHover = new Anim.Value(0.0F, 16.0F);
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
		gripHover.set(!closing && isOverResizeGrip(mouseX, mouseY) ? 1.0F : 0.0F);
		gripHover.update(deltaSeconds, UiTheme.get().speed(16.0F));
		if (closing) {
			close.update(deltaSeconds, UiTheme.get().speed(13.0F));
		}
	}

	// ----------------------------------------------------------------- hit boxes

	/** Hit box of the title bar close button. */
	public boolean isOverCloseButton(float mouseX, float mouseY) {
		float left = right() - CLOSE_SIZE - 10.0F;
		float top = y + (TITLE_HEIGHT - CLOSE_SIZE) * 0.5F;
		return mouseX >= left && mouseX <= left + CLOSE_SIZE && mouseY >= top && mouseY <= top + CLOSE_SIZE;
	}

	public float closeHover() {
		return closeHover.get();
	}

	/** Centre of the close button - screens may place their own header text around it. */
	public float closeCenterX() {
		return right() - CLOSE_SIZE * 0.5F - 10.0F;
	}

	/** Flat close button: a small rounded square that turns red on hover. */
	public void renderCloseButton(GuiGraphics graphics, UiTheme theme, float alpha) {
		if (alpha <= 0.01F) {
			return;
		}
		float top = y + (TITLE_HEIGHT - CLOSE_SIZE) * 0.5F + slide();
		float left = right() - CLOSE_SIZE - 10.0F;
		float hover = closeHover.get();
		if (hover > 0.01F) {
			Render.roundedRect(graphics, left, top, CLOSE_SIZE, CLOSE_SIZE, 6.0F,
					Render.alpha(theme.negative, 0.16F * hover * alpha));
		}
		UiIcons.CLOSE.drawCentered(graphics, left + CLOSE_SIZE * 0.5F, top + CLOSE_SIZE * 0.5F, 11.0F,
				Render.alpha(Render.mix(theme.textFaint, theme.negative, hover), alpha));
	}

	public void center(int screenWidth, int screenHeight) {
		x = (screenWidth - width) * 0.5F;
		y = (screenHeight - height) * 0.5F;
	}

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

	/** Vertical centre of the title band, in screen coordinates. */
	public float titleCenterY() {
		return y + TITLE_HEIGHT * 0.5F;
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
	}

	public boolean isOverTitleBar(float mouseX, float mouseY) {
		return draggable && contains(mouseX, mouseY) && mouseY <= y + TITLE_HEIGHT;
	}

	public boolean isOverResizeGrip(float mouseX, float mouseY) {
		return resizable && mouseX >= right() - RESIZE_GRIP && mouseX <= right()
				&& mouseY >= bottom() - RESIZE_GRIP && mouseY <= bottom();
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/** Fade-in/out progress of the whole window. */
	public float alpha() {
		return Anim.clamp01(appear.get()) * (1.0F - Anim.clamp01(close.get()));
	}

	/** Vertical offset of the closing animation. */
	public float slide() {
		return Anim.clamp01(close.get()) * 12.0F;
	}

	public boolean wasResized() {
		return resized;
	}

	public boolean wasMoved() {
		return moved;
	}

	/** Forgets a remembered geometry and puts the window back to its default size. */
	public void resetGeometry() {
		resized = false;
		moved = false;
		ChaosConfig.setUi(key + ".x", Float.NaN);
		ChaosConfig.setUi(key + ".y", Float.NaN);
		ChaosConfig.setUi(key + ".width", Float.NaN);
		ChaosConfig.setUi(key + ".height", Float.NaN);
	}

	private void persist() {
		ChaosConfig.setUi(key + ".x", x);
		ChaosConfig.setUi(key + ".y", y);
		ChaosConfig.setUi(key + ".width", width);
		ChaosConfig.setUi(key + ".height", height);
	}

	// ----------------------------------------------------------------- render

	/** Draws the panel: shadow, body, outline, accent strip and the resize hint. */
	public void renderShell(GuiGraphics graphics, UiTheme theme) {
		float alpha = alpha();
		if (alpha <= 0.01F) {
			return;
		}
		float drawY = y + slide();
		if (theme.glow) {
			// A very soft accent bloom behind the panel, driven by the live accent colour.
			Render.glow(graphics, x + width * 0.5F, drawY + height * 0.5F, Math.min(width, height) * 0.62F,
					theme.accent, 0.10F * alpha);
		}
		if (theme.windowShadow) {
			Render.softShadow(graphics, x, drawY, width, height, theme.radius, 1.35F * theme.shadowStrength * alpha);
		}
		Render.roundedRectGradient(graphics, x, drawY, width, height, theme.radius,
				Render.alpha(theme.windowTop, alpha), Render.alpha(theme.windowBottom, alpha));
		Render.ring(graphics, x, drawY, width, height, theme.radius, 1.0F, Render.alpha(theme.outline, alpha));
		Ui.accentStrip(graphics, x, drawY, width, theme.radius, 2.0F, theme, alpha);
		// resize hint in the bottom right corner
		float grip = gripHover.get();
		if (grip > 0.01F) {
			for (int i = 0; i < 3; i++) {
				float offset = 4.0F + i * 4.0F;
				Render.circle(graphics, right() - offset, bottom() - offset, 1.2F,
						Render.alpha(theme.textFaint, (0.35F + 0.45F * grip) * alpha));
			}
		}
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
