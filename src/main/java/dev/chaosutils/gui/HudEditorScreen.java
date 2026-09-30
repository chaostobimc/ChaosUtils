package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Drag &amp; drop editor for every movable overlay.
 *
 * <p>The window lists the overlays with their switches while the ghost boxes themselves live in
 * screen space, exactly where the overlays will appear in game. Positions are stored as a fraction
 * of the free space, so the drag delta is mapped straight onto that fraction and the layout survives
 * every GUI scale and window size.
 */
public final class HudEditorScreen extends ChaosScreen {
	private static final float GHOST_WIDTH = 132.0F;
	private static final float GHOST_HEIGHT = 26.0F;

	private record Entry(Module module, Setting.Position position) {
	}

	private final List<Entry> entries = new ArrayList<>();
	private Entry dragging;
	private float grabOffsetX;
	private float grabOffsetY;
	private float snapFlash;
	private boolean showGrid = true;

	public HudEditorScreen(Screen parent) {
		super(parent, Component.literal("HUD Editor"), "window.hudeditor", 520.0F, 360.0F);
	}

	@Override
	protected void buildLayout() {
		UiTheme theme = UiTheme.get();
		entries.clear();
		for (Module module : ModuleManager.modules()) {
			for (Setting<?> setting : module.settings()) {
				if (setting instanceof Setting.Position position) {
					entries.add(new Entry(module, position));
				}
			}
		}

		float contentX = window().x() + 20.0F;
		float contentWidth = window().width() - 40.0F;
		float top = window().y() + window().titleHeight() + 16.0F;

		UiWidgets.Label hint = new UiWidgets.Label("Drag the ghost boxes to place your overlays.", theme.textFaint);
		hint.setBounds(contentX, top, contentWidth, 12.0F);
		add(hint);

		UiWidgets.ScrollList list = new UiWidgets.ScrollList();
		list.setSpacing(6.0F);
		list.setBounds(contentX, top + 18.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 74.0F));
		list.snapAppear();
		add(list);

		int index = 0;
		for (Entry entry : entries) {
			list.addItem(new OverlayRow(entry, index));
			index++;
		}
		list.layout();

		UiWidgets.Button grid = new UiWidgets.Button(showGrid ? "Grid: on" : "Grid: off",
				UiWidgets.Button.Variant.GHOST, theme.accent, () -> {
					showGrid = !showGrid;
					refresh();
				});
		grid.setBounds(contentX, window().bottom() - 30.0F, 90.0F, 20.0F);
		grid.setTooltip("Toggle the alignment grid.");
		add(grid);

		UiWidgets.Button reset = new UiWidgets.Button("Reset all", UiWidgets.Button.Variant.GHOST, theme.warning, () -> {
			for (Entry entry : entries) {
				entry.position.reset();
			}
			toast("All overlays reset");
			snapFlash = 1.0F;
		});
		reset.setBounds(contentX + 98.0F, window().bottom() - 30.0F, 88.0F, 20.0F);
		reset.setTooltip("Move every overlay back to its default corner.");
		add(reset);

		UiWidgets.Button done = new UiWidgets.Button("Done", UiWidgets.Button.Variant.PRIMARY, theme.accent, this::requestClose);
		done.setBounds(window().right() - 110.0F, window().bottom() - 30.0F, 90.0F, 20.0F);
		done.setTooltip("Save and go back.");
		add(done);
	}

	@Override
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		UiTheme theme = theme();
		Render.boldText(graphics, UiWidgets.font(), "HUD Editor", window().x() + 16.0F, window().y() + 13.0F,
				Render.alpha(theme.text, alpha), false);
		String subtitle = entries.size() + " movable overlays";
		Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle), window().y() + 13.0F,
				Render.alpha(theme.textFaint, alpha), false);
	}

	// ------------------------------------------------------------ screen space

	@Override
	protected void renderScreenSpace(GuiGraphics graphics, float mouseX, float mouseY) {
		UiTheme theme = theme();
		float alpha = alpha();
		if (alpha <= 0.02F) {
			return;
		}
		if (showGrid) {
			for (float x = 0.0F; x <= this.width; x += 40.0F) {
				Render.rect(graphics, x, 0.0F, 1.0F, this.height, Render.alpha(0xFFFFFF, 0.022F * alpha));
			}
			for (float y = 0.0F; y <= this.height; y += 40.0F) {
				Render.rect(graphics, 0.0F, y, this.width, 1.0F, Render.alpha(0xFFFFFF, 0.022F * alpha));
			}
		}
		if (snapFlash > 0.01F) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, Render.alpha(theme.accent, 0.06F * snapFlash * alpha));
			snapFlash = Math.max(0.0F, snapFlash - deltaSeconds * 2.4F);
		}
		for (Entry entry : entries) {
			boolean enabled = entry.module.isEnabled();
			float x = entry.position.get().screenX(this.width, Math.round(GHOST_WIDTH));
			float y = entry.position.get().screenY(this.height, Math.round(GHOST_HEIGHT));
			boolean hovered = mouseX >= x && mouseX <= x + GHOST_WIDTH && mouseY >= y && mouseY <= y + GHOST_HEIGHT;
			boolean active = dragging == entry;
			int accent = entry.module.category().color();
			float presence = enabled ? 1.0F : 0.45F;
			float emphasis = active ? 1.0F : (hovered ? 0.85F : 0.55F);

			Render.softShadow(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F, 0.7F * presence * alpha);
			Render.roundedRect(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F,
					Render.mix(Render.alpha(0x12131C, 0.85F * presence * alpha),
							Render.alpha(accent, 0.28F * presence * alpha), emphasis * 0.6F));
			Render.ring(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F, active ? 1.6F : 1.0F,
					Render.alpha(accent, presence * alpha * (active ? 1.0F : 0.6F)));
			Ui.dot(graphics, x + 10.0F, y + GHOST_HEIGHT * 0.5F, 3.0F,
					Render.alpha(enabled ? accent : theme.textFaint, presence * alpha));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), entry.module.name(), GHOST_WIDTH - 46.0F),
					x + 18.0F, y + 5.0F, Render.alpha(theme.text, presence * alpha), false);
			Render.text(graphics, UiWidgets.font(), entry.position.display(), x + 18.0F, y + 15.0F,
					Render.alpha(theme.textFaint, presence * alpha), false);
		}
	}

	@Override
	protected boolean mouseClickedScreenSpace(float mouseX, float mouseY, int button) {
		if (button != 0) {
			return false;
		}
		for (Entry entry : entries) {
			float x = entry.position.get().screenX(this.width, Math.round(GHOST_WIDTH));
			float y = entry.position.get().screenY(this.height, Math.round(GHOST_HEIGHT));
			if (mouseX >= x && mouseX <= x + GHOST_WIDTH && mouseY >= y && mouseY <= y + GHOST_HEIGHT) {
				dragging = entry;
				grabOffsetX = mouseX - x;
				grabOffsetY = mouseY - y;
				playClick(true);
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean mouseDraggedScreenSpace(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		if (dragging == null || button != 0) {
			return false;
		}
		float targetX = mouseX - grabOffsetX;
		float targetY = mouseY - grabOffsetY;
		// Snap to a 20 pixel grid while shift is not held, which is what makes a tidy HUD.
		if (!isShiftDown()) {
			targetX = Math.round(targetX / 20.0F) * 20.0F;
			targetY = Math.round(targetY / 20.0F) * 20.0F;
		}
		int maxX = Math.max(1, this.width - Math.round(GHOST_WIDTH));
		int maxY = Math.max(1, this.height - Math.round(GHOST_HEIGHT));
		dragging.position.set(new HudPos(
				Anim.clamp(targetX / maxX, 0.0F, 1.0F),
				Anim.clamp(targetY / maxY, 0.0F, 1.0F)));
		return true;
	}

	@Override
	protected boolean mouseReleasedScreenSpace(float mouseX, float mouseY, int button) {
		if (dragging != null && button == 0) {
			dragging = null;
			snapFlash = 1.0F;
			return true;
		}
		return false;
	}

	private static boolean isShiftDown() {
		try {
			return org.lwjgl.glfw.GLFW.glfwGetKey(net.minecraft.client.Minecraft.getInstance().getWindow().handle(),
					org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
					|| org.lwjgl.glfw.GLFW.glfwGetKey(
							net.minecraft.client.Minecraft.getInstance().getWindow().handle(),
							org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
		} catch (Throwable ignored) {
			return false;
		}
	}

	/** One overlay row: name, live switch and a "drag me" hint. */
	private final class OverlayRow extends UiComponent {
		private final Entry entry;
		private final UiWidgets.Toggle toggle;
		private final int index;

		private OverlayRow(Entry entry, int index) {
			this.entry = entry;
			this.index = index;
			this.toggle = new UiWidgets.Toggle(() -> entry.module.isEnabled(), value -> {
				if (entry.module.isEnabled() != value) {
					entry.module.enabled().toggle();
					playClick(value);
				}
			}, UiTheme.get().accent);
			this.setTooltip(entry.module.description() + "\n§7drag the ghost box on screen to move it");
			this.setAppearDelay(Math.min(0.2F, 0.02F * index));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			toggle.setBounds(right() - 40.0F, y, 30.0F, height);
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
			float offset = appearOffset();
			boolean enabled = entry.module.isEnabled();
			int accent = entry.module.category().color();
			Render.roundedRect(graphics, x, y + offset, width, height, 8.0F,
					Render.mix(Render.alpha(0xFFFFFF, 0.02F * alpha), Render.alpha(0xFFFFFF, 0.06F * alpha), hover.get()));
			Ui.dot(graphics, x + 10.0F, y + offset + height * 0.5F, 3.0F,
					Render.alpha(enabled ? accent : theme.textFaint, alpha));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), entry.module.name(), width - 60.0F),
					x + 18.0F, y + offset + (height - 8.0F) * 0.5F,
					Render.alpha(enabled ? theme.text : theme.textFaint, alpha), false);
			toggle.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (x + width - 44.0F <= mouseX) {
				return toggle.mouseClicked(mouseX, mouseY, button);
			}
			// clicking the row highlights the matching ghost box
			snapFlash = 1.0F;
			return true;
		}
	}
}
