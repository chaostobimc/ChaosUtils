package dev.chaosutils.feature.radial;

import java.util.List;

import dev.chaosutils.config.RadialElement;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * The radial menu itself.
 *
 * <p>A transparent, non-pausing screen: the world keeps running behind it, the mouse is
 * released like in any other menu, and the ring is pinned where it appeared (on the cursor by
 * default, in the middle of the screen if the player prefers that) so the slices can be pointed
 * at. Releasing the radial key runs the slice under the pointer once - never twice, never
 * repeatedly.
 *
 * <p>Rendering is done with the shared {@code Render} helpers, so the menu looks identical to
 * the rest of the interface and needs no render pipeline knowledge.
 */
public final class RadialMenuScreen extends Screen {
	private final Screen parent;
	private final Anim.Value openAnim = new Anim.Value(0.0F, 1.0F);
	private int hoveredIndex = -1;
	private float centerX;
	private float centerY;
	private boolean centerLocked;
	private float lastMouseX;
	private float lastMouseY;
	private boolean closing;
	private boolean executed;

	public RadialMenuScreen(Screen parent) {
		super(Component.literal("ChaosUtils Radial Menu"));
		this.parent = parent;
		this.openAnim.snap(0.0F);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Runs the currently hovered slice, then closes. Called when the key is released. */
	public void commitSelection() {
		executed = true;
		runHovered();
		close();
	}

	/** Closes without running anything (feature disabled, world change, ...). */
	public void cancel() {
		if (closing) {
			return;
		}
		closing = true;
		restoreParent();
	}

	private void runHovered() {
		List<RadialElement> entries = RadialMenuFeature.entries();
		if (hoveredIndex < 0 || hoveredIndex >= entries.size()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		RadialMenuFeature.execute(entries.get(hoveredIndex), client);
	}

	private void close() {
		if (closing) {
			return;
		}
		closing = true;
		restoreParent();
	}

	private void restoreParent() {
		RadialMenuFeature.onClosed();
		Minecraft client = Minecraft.getInstance();
		client.setScreen(parent);
	}

	@Override
	public void onClose() {
		// Escape closes the menu without executing anything.
		close();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float delta = dev.chaosutils.core.TickClock.frameDelta();
		openAnim.set(1.0F);
		openAnim.update(delta * Math.max(1.0F, RadialMenuFeature.animationSpeed() * 0.35F));
		float appearance = Anim.easeOutCubic(Anim.clamp01(openAnim.get()));

		List<RadialElement> entries = RadialMenuFeature.entries();
		if (entries.isEmpty()) {
			graphics.drawCenteredString(this.font, "No radial entries - add some in the radial editor", this.width / 2, this.height / 2, 0xFFE05B5B);
			super.render(graphics, mouseX, mouseY, partialTick);
			return;
		}
		// The ring is pinned where it opened: centred on the cursor (default) or on the screen.
		// Pinning is what makes hovering possible - a ring that followed the cursor could never
		// be pointed at.
		if (!centerLocked) {
			centerLocked = true;
			centerX = RadialMenuFeature.centersOnCursor() ? mouseX : this.width * 0.5F;
			centerY = RadialMenuFeature.centersOnCursor() ? mouseY : this.height * 0.5F;
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;

		if (RadialMenuFeature.dimsBackground()) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height,
					Render.alpha(0x000000, RadialMenuFeature.dimStrength() * appearance));
		}

		float inner = RadialMenuFeature.innerRadius() * appearance;
		float outer = RadialMenuFeature.outerRadius() * appearance;
		float span = 360.0F / entries.size();
		float gap = Math.min(RadialMenuFeature.gapDegrees(), span * 0.4F);
		hoveredIndex = computeHovered(entries, centerX, centerY, inner, span);
		// The hovered index is computed every frame from the last mouse position, so a key
		// release between two frames still runs the slice the player saw highlighted.

		for (int i = 0; i < entries.size(); i++) {
			RadialElement element = entries.get(i);
			boolean hovered = i == hoveredIndex;
			float start = i * span - 90.0F + gap * 0.5F;
			float end = (i + 1) * span - 90.0F - gap * 0.5F;
			float expansion = hovered ? (RadialMenuFeature.hoverScale() - 1.0F) * outer : 0.0F;
			int base = element.color;
			int color = hovered
					? Render.alpha(Render.mix(base, RadialMenuFeature.highlightColor(), 0.55F), Anim.clamp01(0.92F * appearance))
					: Render.alpha(base, Anim.clamp01(0.62F * appearance));
			Render.arc(graphics, centerX, centerY, inner, outer + expansion, start, end, color);
			if (hovered) {
				Render.arc(graphics, centerX, centerY, outer + expansion - 1.5F, outer + expansion,
						start, end, Render.alpha(0xFFFFFFFF, Anim.clamp01(0.85F * appearance)));
			}
			drawSliceContent(graphics, element, centerX, centerY, inner, outer, expansion, start, end, hovered, appearance);
		}

		// Centre text: what is currently selected.
		if (RadialMenuFeature.showCenterText()) {
			String label = hoveredIndex >= 0 ? entries.get(hoveredIndex).name : "Select an action";
			String detail = hoveredIndex >= 0 ? entries.get(hoveredIndex).command : "release to cancel";
			Render.centeredText(graphics, this.font, label, centerX, centerY - 12.0F,
					Render.alpha(0xFFFFFFFF, Anim.clamp01(appearance)), true);
			Render.centeredText(graphics, this.font, trim(detail), centerX, centerY - 2.0F,
					Render.alpha(0xFFBFC2CF, Anim.clamp01(appearance)), true);
			Render.centeredText(graphics, this.font, entries.get(Math.max(0, hoveredIndex)).type.label(), centerX, centerY + 9.0F,
					Render.alpha(0xFF8A8A9E, Anim.clamp01(appearance)), true);
		}

		String hint = "hold " + keyHint() + "  ·  release to run  ·  esc to cancel";
		Render.centeredText(graphics, this.font, hint, this.width * 0.5F, this.height - 26.0F, 0xFF9E9EB3, true);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private String keyHint() {
		try {
			return dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "the radial key";
		}
	}

	private int computeHovered(List<RadialElement> entries, float centerX, float centerY, float inner, float span) {
		float dx = lastMouseX - centerX;
		float dy = lastMouseY - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance < Math.max(RadialMenuFeature.deadZone(), inner * 0.6F)) {
			return -1;
		}
		double angle = Math.toDegrees(Math.atan2(dx, -dy)) + 90.0;
		while (angle < 0.0) {
			angle += 360.0;
		}
		while (angle >= 360.0) {
			angle -= 360.0;
		}
		int index = (int) (angle / span);
		return Math.max(0, Math.min(entries.size() - 1, index));
	}

	private void drawSliceContent(GuiGraphics graphics, RadialElement element, float centerX, float centerY,
			float inner, float outer, float expansion, float start, float end, boolean hovered, float appearance) {
		// Content sits on the middle line of the slice: icon above, label below it (or the label
		// alone, vertically centred, when there is no icon).
		double midAngle = Math.toRadians((start + end) * 0.5);
		float midRadius = (inner + outer + expansion) * 0.5F;
		float x = centerX + (float) Math.sin(midAngle) * midRadius;
		float y = centerY - (float) Math.cos(midAngle) * midRadius;
		boolean hasIcon = RadialMenuFeature.showIcons() && !element.iconStack().isEmpty();
		if (hasIcon) {
			float scale = 0.8F + 0.2F * appearance + (hovered ? 0.15F : 0.0F);
			graphics.pose().pushPose();
			graphics.pose().translate(x, y);
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-x, -y);
			Render.item(graphics, element.iconStack(), x - 8.0F, y - 14.0F);
			graphics.pose().popPose();
		}
		if (RadialMenuFeature.showLabels()) {
			Font font = this.font;
			String name = trim(element.name);
			float labelX = x - font.width(name) * 0.5F;
			float labelY = hasIcon ? y + 3.0F : y - 4.0F;
			int color = hovered ? 0xFFFFFFFF : Render.alpha(0xFFFFFFFF, Anim.clamp01(appearance));
			Render.text(graphics, font, name, labelX, labelY, color, true);
		}
	}

	private String trim(String value) {
		String result = value == null ? "" : value;
		while (this.font.width(result) > 96 && result.length() > 4) {
			result = result.substring(0, result.length() - 2) + "…";
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			commitSelection();
			return true;
		}
		if (event.button() == 1) {
			close();
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	public boolean wasExecuted() {
		return executed;
	}
}
