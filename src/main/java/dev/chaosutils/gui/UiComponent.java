package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.util.Anim;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight GUI element of the ChaosUtils interface.
 *
 * <p>ChaosUtils deliberately does not use the vanilla widget hierarchy: the input signatures of
 * {@code AbstractWidget} move around between renderer rewrites, and owning the model is what lets
 * every element animate with shared timing (hover lift, press depth, entrance stagger) without
 * each widget carrying its own tweens.
 */
public abstract class UiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected boolean visible = true;
	protected boolean enabled = true;
	protected String tooltip;

	/** Layer alpha handed down by the owning screen (window fade in/out). */
	protected float layerAlpha = 1.0F;
	/** Entrance animation: 0 hidden, 1 fully placed. */
	protected final Anim.Value appear = new Anim.Value(0.0F, 9.0F);
	protected final Anim.Value hover = new Anim.Value(0.0F, 16.0F);
	protected final Anim.Value active = new Anim.Value(0.0F, 12.0F);
	protected final Anim.Value focus = new Anim.Value(0.0F, 11.0F);

	private float appearDelay;
	private final List<UiComponent> children = new ArrayList<>();

	public UiComponent setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		return this;
	}

	public UiComponent setTooltip(@Nullable String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	/** Seconds to wait before this element animates in; used for staggered lists. */
	public UiComponent setAppearDelay(float seconds) {
		this.appearDelay = Math.max(0.0F, seconds);
		return this;
	}

	public UiComponent snapAppear() {
		this.appearDelay = 0.0F;
		this.appear.snap(1.0F);
		return this;
	}

	public String tooltip() {
		return tooltip;
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

	public float bottom() {
		return y + height;
	}

	public float right() {
		return x + width;
	}

	public float centerX() {
		return x + width * 0.5F;
	}

	public float centerY() {
		return y + height * 0.5F;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setLayerAlpha(float alpha) {
		this.layerAlpha = Anim.clamp01(alpha);
		for (UiComponent child : children) {
			child.setLayerAlpha(alpha);
		}
	}

	/** Combined alpha of this element: layer alpha times entrance animation. */
	protected float alpha() {
		return layerAlpha * Anim.clamp01(appear.get());
	}

	/** Pixel offset applied while the element is still animating in. */
	protected float appearOffset() {
		return (1.0F - Anim.easeOutQuint(appear.get())) * 6.0F;
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public List<UiComponent> children() {
		return children;
	}

	protected void addChild(UiComponent child) {
		children.add(child);
	}

	public void clearChildren() {
		children.clear();
	}

	public void update(float deltaSeconds, float mouseX, float mouseY) {
		if (appearDelay > 0.0F) {
			appearDelay = Math.max(0.0F, appearDelay - deltaSeconds);
		}
		appear.set(appearDelay > 0.0F ? 0.0F : 1.0F);
		appear.update(deltaSeconds, UiTheme.get().speed(9.0F));
		float hovering = enabled && visible && contains(mouseX, mouseY) ? 1.0F : 0.0F;
		hover.set(hovering);
		hover.update(deltaSeconds, UiTheme.get().speed(16.0F));
		active.update(deltaSeconds, UiTheme.get().speed(12.0F));
		focus.update(deltaSeconds, UiTheme.get().speed(11.0F));
		for (UiComponent child : children) {
			child.setLayerAlpha(layerAlpha);
			child.update(deltaSeconds, mouseX, mouseY);
		}
	}

	public abstract void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds);

	public void renderChildren(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
		for (UiComponent child : children) {
			if (child.isVisible()) {
				child.render(graphics, mouseX, mouseY, deltaSeconds);
			}
		}
	}

	public boolean mouseClicked(float mouseX, float mouseY, int button) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(float mouseX, float mouseY, int button) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseReleased(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseScrolled(mouseX, mouseY, amount)) {
				return true;
			}
		}
		return false;
	}

	public boolean keyPressed(int keyCode, int modifiers) {
		for (UiComponent child : children) {
			if (child.isVisible() && child.keyPressed(keyCode, modifiers)) {
				return true;
			}
		}
		return false;
	}

	/** Rebuilds children after a data change while keeping the current scroll position. */
	public void refresh() {
	}

	/** Called when a component is taken out of the tree so it can drop transient state. */
	public void reset() {
		hover.snap(0.0F);
		active.snap(0.0F);
		focus.snap(0.0F);
		appear.snap(0.0F);
		for (UiComponent child : children) {
			child.reset();
		}
	}

	protected boolean isHovered(float mouseX, float mouseY) {
		return enabled && visible && contains(mouseX, mouseY);
	}
}
