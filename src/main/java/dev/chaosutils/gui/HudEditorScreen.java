package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Drag &amp; drop editor for every movable overlay.
 *
 * <p>Overlays store their position as a fraction of the free space, so this screen simply
 * maps the drag delta onto that fraction. Holding the module list on the left and the live
 * preview of every enabled overlay makes arranging the HUD a two second job.
 */
public final class HudEditorScreen extends Screen {
	private record Entry(Module module, Setting.Position position, String label) {
	}

	private final Screen parent;
	private final List<Entry> entries = new ArrayList<>();
	private Entry dragging;
	private float grabOffsetX;
	private float grabOffsetY;
	private float previewX;
	private float previewY;
	private float previewWidth = 120.0F;
	private float previewHeight = 26.0F;
	private float lastMouseX;
	private float lastMouseY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("ChaosUtils HUD Editor"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		entries.clear();
		for (Module module : ModuleManager.modules()) {
			if (!module.isEnabled()) {
				continue;
			}
			for (Setting<?> setting : module.settings()) {
				if (setting instanceof Setting.Position position) {
					entries.add(new Entry(module, position, module.name() + " · " + setting.label));
				}
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		UiTheme theme = UiTheme.get();
		renderBackground(graphics, mouseX, mouseY, deltaTicks);
		Render.rect(graphics, 0.0F, 0.0F, width, height, theme.background);

		float panelWidth = 190.0F;
		Render.shadowedPanel(graphics, 10.0F, 10.0F, panelWidth, height - 20.0F, theme.radius, theme.panel, theme.accent);
		Render.text(graphics, font, "HUD Editor", 20.0F, 20.0F, theme.text, false);
		Render.text(graphics, font, "Drag the ghost boxes to", 20.0F, 34.0F, theme.textDim, false);
		Render.text(graphics, font, "place overlays. Esc applies.", 20.0F, 44.0F, theme.textDim, false);

		float listY = 62.0F;
		for (Entry entry : entries) {
			boolean hovered = mouseX >= 14.0F && mouseX <= 10.0F + panelWidth - 4.0F && mouseY >= listY && mouseY <= listY + 16.0F;
			if (hovered) {
				Render.roundedRect(graphics, 14.0F, listY - 2.0F, panelWidth - 8.0F, 18.0F, 4.0F, Render.alpha(theme.accent, 0.25F));
			}
			Render.text(graphics, font, trim(entry.label), 20.0F, listY + 2.0F, hovered ? theme.text : theme.textDim, false);
			listY += 18.0F;
			if (listY > height - 40.0F) {
				break;
			}
		}

		// Ghost previews of every movable overlay.
		for (Entry entry : entries) {
			boolean isDragging = dragging == entry;
			float x = entry.position.get().screenX(this.width, Math.round(previewWidth));
			float y = entry.position.get().screenY(this.height, Math.round(previewHeight));
			int accent = entry.module.category().color();
			int fill = isDragging ? Render.alpha(accent, 0.55F) : Render.alpha(accent, 0.25F);
			Render.roundedRect(graphics, x, y, previewWidth, previewHeight, theme.radius, fill);
			Render.roundedBorder(graphics, x, y, previewWidth, previewHeight, theme.radius, 1.0F, Render.alpha(accent, 0.9F), fill);
			Render.text(graphics, font, trim(entry.module.name()), x + 6.0F, y + 6.0F, theme.text, true);
			Render.text(graphics, font, entry.position.display(), x + 6.0F, y + 16.0F, theme.textDim, false);
		}
		super.render(graphics, mouseX, mouseY, deltaTicks);
	}

	private String trim(String value) {
		String result = value;
		while (font.width(result) > 168 && result.length() > 4) {
			result = result.substring(0, result.length() - 2) + "…";
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubled);
		}
		for (Entry entry : entries) {
			float x = entry.position.get().screenX(this.width, Math.round(previewWidth));
			float y = entry.position.get().screenY(this.height, Math.round(previewHeight));
			if (lastMouseX >= x && lastMouseX <= x + previewWidth && lastMouseY >= y && lastMouseY <= y + previewHeight) {
				dragging = entry;
				grabOffsetX = lastMouseX - x;
				grabOffsetY = lastMouseY - y;
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (dragging != null) {
			float targetX = lastMouseX - grabOffsetX;
			float targetY = lastMouseY - grabOffsetY;
			int maxX = Math.max(1, this.width - Math.round(previewWidth));
			int maxY = Math.max(1, this.height - Math.round(previewHeight));
			float fractionX = Math.max(0.0F, Math.min(1.0F, targetX / maxX));
			float fractionY = Math.max(0.0F, Math.min(1.0F, targetY / maxY));
			dragging.position.set(new dev.chaosutils.util.HudPos(fractionX, fractionY));
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
