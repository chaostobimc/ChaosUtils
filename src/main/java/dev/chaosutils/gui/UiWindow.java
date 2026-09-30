package dev.chaosutils.gui;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The floating window every ChaosUtils screen lives in.
 *
 * <p>It owns the geometry, the drag/resize interaction and the position memory, so screens only
 * describe their content. The window can be moved by its title bar (or any empty part of the
 * header), resized from the bottom-right corner and its edges, and is kept on screen at all times.
 * Double-clicking the title bar snaps it back to the centred default size.
 */
public final class UiWindow {
	public static final float TITLE_HEIGHT = 34.0F;
	public static final float MIN_WIDTH = 300.0F;
	public static final float MIN_HEIGHT = 190.0F;
	/** Distance from the window border that starts a resize drag. */
	private static final float EDGE = 5.0F;
	private static final float CORNER = 14.0F;

	private final String stateKey;
	private float x;
	private float y;
	private float width;
	private float height;
	private float defaultWidth;
	private float defaultHeight;
	private int screenWidth;
	private int screenHeight;
	private boolean resizable = true;

	private boolean dragging;
	private boolean resizingWidth;
	private boolean resizingHeight;
	private float dragOffsetX;
	private float dragOffsetY;
	private float resizeStartWidth;
	private float resizeStartHeight;
	private float resizeStartX;
	private float resizeStartY;
	private boolean dirty;
	private boolean resized;
	private long lastClickTime;
	private float lastClickX;
	private float lastClickY;

	private final Anim.Value appear = new Anim.Value(0.0F, 9.0F);
	private final Anim.Value closeAnim = new Anim.Value(0.0F, 13.0F);
	private final Anim.Value gripHover = new Anim.Value(0.0F, 14.0F);
	private boolean fadingOut;

	public UiWindow(String stateKey, float defaultWidth, float defaultHeight) {
		this.stateKey = stateKey;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.width = defaultWidth;
		this.height = defaultHeight;
	}

	// -------------------------------------------------------------- lifecycle

	/** Places the window: remembered position when available, centred otherwise. */
	public void open(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		float maxWidth = screenWidth - 24.0F;
		float maxHeight = screenHeight - 24.0F;
		this.defaultWidth = Math.min(defaultWidth, maxWidth);
		this.defaultHeight = Math.min(defaultHeight, maxHeight);
		float storedWidth = ChaosConfig.uiFloat(stateKey + ".width", defaultWidth);
		float storedHeight = ChaosConfig.uiFloat(stateKey + ".height", defaultHeight);
		this.width = Anim.clamp(storedWidth, MIN_WIDTH, maxWidth);
		this.height = Anim.clamp(storedHeight, MIN_HEIGHT, maxHeight);
		float storedX = ChaosConfig.uiFloat(stateKey + ".x", Float.NaN);
		float storedY = ChaosConfig.uiFloat(stateKey + ".y", Float.NaN);
		this.x = Float.isNaN(storedX) ? Math.round((screenWidth - width) * 0.5F) : storedX;
		this.y = Float.isNaN(storedY) ? Math.round((screenHeight - height) * 0.5F) : storedY;
		clampToScreen();
		appear.snap(0.0F);
		closeAnim.snap(0.0F);
		fadingOut = false;
	}

	public void beginClose() {
		if (!fadingOut) {
			fadingOut = true;
			closeAnim.snap(0.0F);
			closeAnim.set(1.0F);
		}
	}

	/** @return true once the closing animation has finished. */
	public boolean isGone() {
		return fadingOut && closeAnim.get() > 0.985F;
	}

	public void update(float deltaSeconds, float mouseX, float mouseY) {
		appear.set(1.0F);
		appear.update(UiTheme.get().speed(9.0F));
		gripHover.set(isOverResizeGrip(mouseX, mouseY) ? 1.0F : 0.0F);
		gripHover.update(UiTheme.get().speed(14.0F));
		if (fadingOut) {
			closeAnim.update(UiTheme.get().speed(13.0F));
		}
	}

	public void snapOpen() {
		appear.snap(1.0F);
	}

	/** Combined fade (opening and closing) used for every colour the window draws. */
	public float alpha() {
		return Anim.clamp01(appear.get()) * (1.0F - Anim.easeOutQuint(closeAnim.get()));
	}

	public float slide() {
		float opening = (1.0F - Anim.easeOutQuint(Anim.clamp01(appear.get()))) * 10.0F;
		float closing = Anim.easeOutQuint(closeAnim.get()) * 14.0F;
		return opening + closing;
	}

	// --------------------------------------------------------------- geometry

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

	/** Body starts below the title bar. */
	public float bodyX() {
		return x;
	}

	public float bodyY() {
		return y + titleHeight();
	}

	public float bodyWidth() {
		return width;
	}

	public float bodyHeight() {
		return Math.max(0.0F, height - titleHeight());
	}

	public float titleHeight() {
		return TITLE_HEIGHT;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
		clampToScreen();
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		clampToScreen();
	}

	public void center() {
		this.width = Math.min(defaultWidth, screenWidth - 24.0F);
		this.height = Math.min(defaultHeight, screenHeight - 24.0F);
		this.x = Math.round((screenWidth - width) * 0.5F);
		this.y = Math.round((screenHeight - height) * 0.5F);
		save();
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
	}

	public boolean isOverTitleBar(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= y + titleHeight();
	}

	private boolean isOverResizeGrip(float mouseX, float mouseY) {
		if (!resizable) {
			return false;
		}
		boolean corner = mouseX >= right() - CORNER && mouseX <= right() + 1.0F
				&& mouseY >= bottom() - CORNER && mouseY <= bottom() + 1.0F;
		return corner;
	}

	// ---------------------------------------------------------------- drawing

	public void renderShell(GuiGraphics graphics, UiTheme theme) {
		float alpha = alpha();
		if (alpha <= 0.01F) {
			return;
		}
		float slide = slide();
		Ui.window(graphics, x, y + slide, width, height, theme.radius, theme, alpha);
		// Title bar: subtle separation from the body plus a hairline.
		Render.scissor(graphics, x, y + slide, width, titleHeight());
		Render.roundedRectGradient(graphics, x, y + slide, width, titleHeight() + theme.radius, theme.radius,
				Render.alpha(0xFFFFFFFF, 0.045F * alpha), Render.alpha(0xFFFFFFFF, 0.0F));
		Render.unscissor(graphics);
		Render.rect(graphics, x + theme.radius, y + slide + titleHeight() - 1.0F, width - theme.radius * 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, alpha));
		renderResizeGrip(graphics, theme, alpha);
	}

	/** Three diagonal lines in the corner, brightening while the pointer is near. */
	private void renderResizeGrip(GuiGraphics graphics, UiTheme theme, float alpha) {
		if (!resizable) {
			return;
		}
		float intensity = 0.18F + gripHover.get() * 0.5F;
		int color = Render.alpha(theme.glow && gripHover.get() > 0.4F ? theme.accent : 0xFFFFFFFF, intensity * alpha);
		for (int i = 0; i < 3; i++) {
			float offset = 3.0F + i * 4.0F;
			Render.line(graphics, right() - offset, bottom() - 3.0F, right() - 3.0F, bottom() - offset, 1.6F, color);
		}
		if (gripHover.get() > 0.05F) {
			Render.glow(graphics, right() - 4.0F, bottom() - 4.0F, 16.0F + gripHover.get() * 10.0F, theme.accent,
					0.28F * gripHover.get() * alpha);
		}
	}

	// ------------------------------------------------------------------ input

	/** @return true when the click was consumed by the window chrome (drag or resize). */
	public boolean mouseClicked(float mouseX, float mouseY, int button) {
		if (button != 0 || !contains(mouseX, mouseY)) {
			return false;
		}
		long now = System.currentTimeMillis();
		if (isOverTitleBar(mouseX, mouseY)) {
			boolean doubleClick = now - lastClickTime < 320L
					&& Math.abs(mouseX - lastClickX) < 6.0F && Math.abs(mouseY - lastClickY) < 6.0F;
			lastClickTime = now;
			lastClickX = mouseX;
			lastClickY = mouseY;
			if (doubleClick) {
				center();
				return true;
			}
			dragging = true;
			dragOffsetX = mouseX - x;
			dragOffsetY = mouseY - y;
			return true;
		}
		if (isOverResizeGrip(mouseX, mouseY)) {
			resizingWidth = true;
			resizingHeight = true;
			resizeStartWidth = width;
			resizeStartHeight = height;
			resizeStartX = mouseX;
			resizeStartY = mouseY;
			return true;
		}
		return false;
	}

	public boolean mouseDragged(float mouseX, float mouseY, int button) {
		if (button != 0) {
			return false;
		}
		if (dragging) {
			x = mouseX - dragOffsetX;
			y = mouseY - dragOffsetY;
			clampToScreen();
			dirty = true;
			return true;
		}
		if (resizingWidth || resizingHeight) {
			if (resizingWidth) {
				width = Math.max(MIN_WIDTH, resizeStartWidth + (mouseX - resizeStartX));
			}
			if (resizingHeight) {
				height = Math.max(MIN_HEIGHT, resizeStartHeight + (mouseY - resizeStartY));
			}
			clampToScreen();
			dirty = true;
			return true;
		}
		return false;
	}

	public boolean mouseReleased(int button) {
		if (button != 0) {
			return false;
		}
		boolean wasActive = dragging || resizingWidth || resizingHeight;
		resized = resizingWidth || resizingHeight;
		dragging = false;
		resizingWidth = false;
		resizingHeight = false;
		if (wasActive && dirty) {
			dirty = false;
			save();
		}
		return wasActive;
	}

	/** True for exactly one call after a resize drag ended (used to rebuild a layout). */
	public boolean wasResized() {
		boolean value = resized;
		resized = false;
		return value;
	}

	public boolean isInteracting() {
		return dragging || resizingWidth || resizingHeight;
	}

	private void clampToScreen() {
		float minVisible = 70.0F;
		width = Math.min(width, screenWidth - 8.0F);
		height = Math.min(height, screenHeight - 8.0F);
		x = Anim.clamp(x, minVisible - width, screenWidth - minVisible);
		y = Anim.clamp(y, 2.0F, Math.max(2.0F, screenHeight - TITLE_HEIGHT - 4.0F));
	}

	private void save() {
		ChaosConfig.setUi(stateKey + ".x", x);
		ChaosConfig.setUi(stateKey + ".y", y);
		ChaosConfig.setUi(stateKey + ".width", width);
		ChaosConfig.setUi(stateKey + ".height", height);
	}
}
