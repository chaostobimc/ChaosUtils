package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.util.Anim;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight GUI element.
 *
 * <p>ChaosUtils deliberately uses its own component model instead of vanilla's widget
 * hierarchy: the input signatures of {@code AbstractWidget} changed with the 1.21.9 input
 * rework, and owning the model lets every element animate smoothly (hover glow, press
 * depth, expand/collapse) with shared timing instead of per-widget tweens.
 */
public abstract class UiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected boolean visible = true;
	protected boolean enabled = true;
	protected String tooltip;

	/** 0..1 hover amount, animated. */
	protected final Anim.Value hover = new Anim.Value(0.0F, 14.0F);
	/** 0..1 "activated" amount used for press feedback and toggles. */
	protected final Anim.Value active = new Anim.Value(0.0F, 10.0F);

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
		float hovering = enabled && visible && contains(mouseX, mouseY) ? 1.0F : 0.0F;
		hover.set(hovering);
		hover.update(deltaSeconds);
		active.update(deltaSeconds);
		for (UiComponent child : children) {
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

	/** Content height used by scroll layouts; may exceed {@link #height}. */
	public float contentHeight() {
		float max = height;
		for (UiComponent child : children) {
			max = Math.max(max, child.y() - y + child.contentHeight());
		}
		return max;
	}

	/** Called when a component becomes hidden so it can drop transient state (keeps memory flat). */
	public void reset() {
		hover.snap(0.0F);
		active.snap(0.0F);
		for (UiComponent child : children) {
			child.reset();
		}
	}

	protected boolean isHovered(float mouseX, float mouseY) {
		return enabled && visible && contains(mouseX, mouseY);
	}
}
