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
 * The interactive radial menu.
 *
 * <p>Design rules that keep it readable:
 * <ul>
 *   <li>the ring is pinned where it opened (on the cursor by default) and is scaled down until it
 *       fits the screen with a margin, so slices can never run off-screen,</li>
 *   <li>slices are drawn as true annulus sectors with gaps, drawn scanline by scanline - no
 *       overlapping blocks, no jagged edges,</li>
 *   <li>labels are measured against the chord of their slice, so a long name can never reach into
 *       the neighbouring slice,</li>
 *   <li>nothing else is drawn while it is open: the HUD elements of ChaosUtils and the vanilla HUD
 *       sit behind the frame's blur, and the dim layer removes the rest.</li>
 * </ul>
 *
 * <p>Releasing the radial key runs the hovered slice exactly once. The screen closes itself with a
 * short exit animation, so the player always sees which action was triggered.
 */
public final class RadialMenuScreen extends Screen {
	private static final float ENTRY_SCALE = 0.82F;
	private static final float CLOSE_DELAY = 0.16F;

	private final Screen parent;
	private final Anim.Value appear = new Anim.Value(0.0F, 12.0F);
	private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);

	private Anim.Value[] sliceHover = new Anim.Value[0];
	private int hoveredIndex = -1;
	private boolean hoveredInitialised;

	private float centerX;
	private float centerY;
	private boolean centerLocked;
	private float mouseX;
	private float mouseY;
	private float scale = 1.0F;
	private float inner;
	private float outer;

	private boolean closing;
	private boolean committed;
	private boolean handedOver;
	private boolean closeFinished;
	private float closingFor;

	public RadialMenuScreen(Screen parent) {
		super(Component.literal("ChaosUtils Radial Menu"));
		this.parent = parent;
		this.appear.snap(0.0F);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Runs the hovered slice and closes with feedback. Called when the key is released. */
	public void commitSelection() {
		if (closing) {
			return;
		}
		RadialElement element = hovered();
		if (element != null) {
			Minecraft client = Minecraft.getInstance();
			if (client.player != null) {
				RadialMenuFeature.execute(element, client);
			}
			committed = true;
		}
		beginClose();
	}

	/** Closes without executing anything. */
	public void cancel() {
		if (!closing) {
			beginClose();
		}
	}

	private void beginClose() {
		closing = true;
		exit.set(1.0F);
		closingFor = 0.0F;
	}

	/** True once the exit animation has played out; the feature then switches screens in its tick. */
	public boolean readyToClose() {
		return closeFinished;
	}

	/** Performs the actual hand-over. Called from the client tick, never from inside a frame. */
	public void finishClose() {
		if (handedOver) {
			return;
		}
		handedOver = true;
		RadialMenuFeature.onClosed();
		Minecraft client = Minecraft.getInstance();
		client.setScreen(parent);
	}

	@Override
	public void onClose() {
		cancel();
	}

	private RadialElement hovered() {
		List<RadialElement> entries = RadialMenuFeature.entries();
		return hoveredIndex >= 0 && hoveredIndex < entries.size() ? entries.get(hoveredIndex) : null;
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float frameDelta = dev.chaosutils.core.TickClock.frameDelta();
		if (frameDelta <= 0.0F) {
			frameDelta = 0.016F;
		}

		appear.set(1.0F);
		appear.update(UiThemeSpeed(12.0F));
		if (closing) {
			exit.update(UiThemeSpeed(9.0F));
			closingFor += frameDelta;
			if (closingFor > CLOSE_DELAY) {
				closeFinished = true;
			}
		}
		if (closeFinished) {
			// Nothing is drawn any more; the tick performs the screen switch.
			return;
		}

		float appearance = Anim.easeOutQuint(Anim.clamp01(appear.get()));
		float exitAmount = Anim.easeOutQuint(Anim.clamp01(exit.get()));
		List<RadialElement> entries = RadialMenuFeature.entries();

		if (!centerLocked) {
			centerLocked = true;
			centerX = RadialMenuFeature.centersOnCursor() ? mouseX : this.width * 0.5F;
			centerY = RadialMenuFeature.centersOnCursor() ? mouseY : this.height * 0.5F;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		computeGeometry(appearance);

		// ---- dim layer: hides the world, the HUD and everything else behind the ring
		float dim = RadialMenuFeature.dimsBackground() ? RadialMenuFeature.dimStrength() : 0.0F;
		if (dim > 0.01F) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height,
					Render.alpha(0x05060A, dim * appearance));
		}
		// Extra vignette so the ring always sits on a calm background.
		Render.verticalGradient(graphics, 0.0F, 0.0F, this.width, this.height, 0x33000000, 0x00000000);

		if (entries.isEmpty()) {
			drawEmptyState(graphics, appearance);
			super.render(graphics, mouseX, mouseY, partialTick);
			return;
		}

		ensureSlices(entries.size());

		float span = 360.0F / entries.size();
		float gap = Math.min(RadialMenuFeature.gapDegrees(), span * 0.35F);
		hoveredIndex = closing ? -1 : computeHovered(entries, span);

		// ---- slices
		for (int i = 0; i < entries.size(); i++) {
			Anim.Value hoverValue = sliceHover[i];
			hoverValue.set(i == hoveredIndex ? 1.0F : 0.0F);
			hoverValue.update(Anim.clamp(UiThemeSpeed(18.0F), 4.0F, 40.0F));
			float highlight = hoverValue.get();

			RadialElement element = entries.get(i);
			float start = i * span - span * 0.5F + gap * 0.5F;
			float end = (i + 1) * span - span * 0.5F - gap * 0.5F;
			float expansion = highlight * (RadialMenuFeature.hoverScale() - 1.0F) * outer * 1.6F;
			float rInner = inner;
			float rOuter = outer + expansion;

			int base = element.color | 0xFF000000;
			float baseAlpha = 0.70F;
			int color = Render.alpha(Render.mix(base, RadialMenuFeature.highlightColor(), highlight * 0.45F),
					(baseAlpha + 0.25F * highlight) * appearance * (1.0F - exitAmount));
			if (highlight > 0.05F) {
				Render.glow(graphics, sliceAnchorX(start, end, rOuter), sliceAnchorY(start, end, rOuter),
						(Math.abs(end - start) / 360.0F) * outer * 2.6F + 24.0F, base, 0.30F * highlight * appearance);
			}
			Render.arc(graphics, centerX, centerY, rInner, rOuter, start, end, color);
			// inner shading for a subtle depth gradient (darker towards the centre)
			Render.arc(graphics, centerX, centerY, rInner, rInner + (rOuter - rInner) * 0.45F, start, end,
					Render.alpha(0x000000, 0.20F * appearance * (1.0F - exitAmount)));
			if (highlight > 0.02F) {
				Render.arc(graphics, centerX, centerY, rOuter - 2.0F, rOuter, start, end,
						Render.alpha(0xFFFFFFFF, 0.75F * highlight * appearance * (1.0F - exitAmount)));
			}
		}

		// ---- slice content (drawn after every slice so nothing can cover a label)
		for (int i = 0; i < entries.size(); i++) {
			RadialElement element = entries.get(i);
			float start = i * span - span * 0.5F + gap * 0.5F;
			float end = (i + 1) * span - span * 0.5F - gap * 0.5F;
			float highlight = sliceHover[i].get();
			float expansion = highlight * (RadialMenuFeature.hoverScale() - 1.0F) * outer * 1.6F;
			drawSliceContent(graphics, element, start, end, expansion, highlight, appearance, exitAmount, span);
		}

		drawHub(graphics, entries, appearance, exitAmount);
		drawSelectionLabel(graphics, appearance, exitAmount);
		drawHint(graphics, appearance);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	/** Theme animation speed times the speed the player configured for the radial menu. */
	private float UiThemeSpeed(float base) {
		return dev.chaosutils.gui.UiTheme.get().speed(base) * RadialMenuFeature.animationSpeed();
	}

	private void ensureSlices(int count) {
		if (sliceHover.length == count) {
			return;
		}
		Anim.Value[] replacements = new Anim.Value[count];
		for (int i = 0; i < count; i++) {
			replacements[i] = i < sliceHover.length ? sliceHover[i] : new Anim.Value(0.0F, 18.0F);
		}
		sliceHover = replacements;
	}

	/** Fits the ring to the screen and keeps the centre inside the safe area. */
	private void computeGeometry(float appearance) {
		float wantedInner = Math.max(24.0F, RadialMenuFeature.innerRadius());
		float wantedOuter = Math.max(wantedInner + 20.0F, RadialMenuFeature.outerRadius());
		float margin = RadialMenuFeature.scalesToScreen() ? 28.0F : 0.0F;
		float available = Math.min(Math.min(centerX, this.width - centerX), Math.min(centerY, this.height - centerY)) - margin;
		scale = RadialMenuFeature.scalesToScreen() ? Anim.clamp(available / wantedOuter, 0.5F, 1.0F) : 1.0F;
		float grown = ENTRY_SCALE + (1.0F - ENTRY_SCALE) * appearance;
		outer = wantedOuter * scale * grown;
		inner = wantedInner * scale * grown;
		centerX = Anim.clamp(centerX, margin + outer, this.width - margin - outer);
		centerY = Anim.clamp(centerY, margin + outer, this.height - margin - outer);
	}

	private int computeHovered(List<RadialElement> entries, float span) {
		float dx = mouseX - centerX;
		float dy = mouseY - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		float deadZone = Math.max(RadialMenuFeature.deadZone(), inner * 0.55F);
		if (distance < deadZone || distance > outer + 26.0F) {
			return -1;
		}
		double angle = Math.toDegrees(Math.atan2(dx, -dy)) + span * 0.5;
		while (angle < 0.0) {
			angle += 360.0;
		}
		while (angle >= 360.0) {
			angle -= 360.0;
		}
		return Anim.clamp((int) (angle / span), 0, entries.size() - 1);
	}

	private float sliceAnchorX(float start, float end, float radius) {
		double mid = Math.toRadians((start + end) * 0.5);
		return centerX + (float) Math.sin(mid) * radius;
	}

	private float sliceAnchorY(float start, float end, float radius) {
		double mid = Math.toRadians((start + end) * 0.5);
		return centerY - (float) Math.cos(mid) * radius;
	}

	private void drawSliceContent(GuiGraphics graphics, RadialElement element, float start, float end,
			float expansion, float highlight, float appearance, float exitAmount, float span) {
		float alpha = appearance * (1.0F - exitAmount) * (0.82F + 0.18F * highlight);
		if (alpha <= 0.02F) {
			return;
		}
		double midDegrees = (start + end) * 0.5;
		double mid = Math.toRadians(midDegrees);
		float midRadius = (inner + outer + expansion) * 0.5F;
		float x = centerX + (float) Math.sin(mid) * midRadius;
		float y = centerY - (float) Math.cos(mid) * midRadius;

		// Chord of the slice at the label radius: the hard limit for text and icon width.
		float chord = 2.0F * midRadius * (float) Math.sin(Math.toRadians(Math.abs(span) * 0.5F));
		float available = Math.max(28.0F, chord - 12.0F);
		net.minecraft.world.item.ItemStack icon = element.iconStack();
		boolean hasIcon = RadialMenuFeature.showIcons() && icon != null && !icon.isEmpty();

		if (hasIcon) {
			float iconY = y - (RadialMenuFeature.showLabels() ? 8.0F : 0.0F);
			UiIcon(graphics, icon, x, iconY, 0.82F + 0.22F * highlight);
		}
		if (!RadialMenuFeature.showLabels()) {
			return;
		}
		Font font = this.font;
		String name = Render.ellipsize(font, element.name, available);
		boolean tooTight = chord < 42.0F && highlight < 0.4F;
		if (tooTight) {
			return;
		}
		float textY = hasIcon ? y + 3.0F : y - 4.0F;
		int color = Render.alpha(highlight > 0.5F ? 0xFFFFFFFF : 0xF0FFFFFF, alpha);
		Render.centeredText(graphics, font, name, x, textY, color, false);
	}

	private void UiIcon(GuiGraphics graphics, net.minecraft.world.item.ItemStack icon, float x, float y, float size) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(size, size);
		graphics.pose().translate(-x, -y);
		Render.item(graphics, icon, x - 8.0F, y - 8.0F);
		graphics.pose().popMatrix();
	}

	/** Centre hub: shows the hovered icon, or a compass rose when nothing is selected. */
	private void drawHub(GuiGraphics graphics, List<RadialElement> entries, float appearance, float exitAmount) {
		float alpha = appearance * (1.0F - exitAmount);
		float hubRadius = Math.max(14.0F, inner - 6.0F);
		Render.circle(graphics, centerX, centerY, hubRadius, Render.alpha(0x0C0D14, 0.72F * alpha));
		RadialElement element = hovered();
		boolean hasHover = element != null;
		if (hasHover) {
			Render.glow(graphics, centerX, centerY, hubRadius * 2.1F, element.color | 0xFF000000, 0.35F * alpha);
		}
		Render.ring(graphics, centerX - hubRadius, centerY - hubRadius, hubRadius * 2.0F, hubRadius * 2.0F, hubRadius, 1.4F,
				Render.alpha(hasHover ? 0xFFFFFFFF : 0x66FFFFFF, 0.35F * alpha));
		if (hasHover && RadialMenuFeature.showIcons()) {
			UiIcon(graphics, element.iconStack(), centerX, centerY, 1.55F);
		} else if (RadialMenuFeature.showCenterText()) {
			// small compass rose while nothing is hovered
			int color = Render.alpha(0xFFFFFFFF, 0.45F * alpha);
			Render.line(graphics, centerX, centerY - hubRadius * 0.55F, centerX, centerY + hubRadius * 0.55F, 1.6F, color);
			Render.line(graphics, centerX - hubRadius * 0.55F, centerY, centerX + hubRadius * 0.55F, centerY, 1.6F, color);
			Render.circle(graphics, centerX, centerY, 2.0F, color);
		}
	}

	/** Name and command of the hovered slice, in a pill below the ring. */
	private void drawSelectionLabel(GuiGraphics graphics, float appearance, float exitAmount) {
		float alpha = appearance * (1.0F - exitAmount);
		RadialElement element = hovered();
		float y = centerY + outer + 18.0F;
		if (element == null) {
			String hint = "point at a slice";
			Render.centeredText(graphics, this.font, hint, centerX, y,
					Render.alpha(0xFFFFFFFF, 0.45F * alpha), false);
			return;
		}
		String name = element.name;
		String detail = element.command == null || element.command.isEmpty() ? element.type.label() : element.command;
		Font font = this.font;
		float width = Math.max(font.width(name), font.width(detail)) + 26.0F;
		float top = y - 6.0F;
		Render.softShadow(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F, 0.7F * alpha);
		Render.roundedRect(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F,
				Render.alpha(0x12131C, 0.92F * alpha));
		Render.ring(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F, 1.0F,
				Render.alpha(element.color | 0xFF000000, 0.55F * alpha));
		Render.centeredText(graphics, font, Render.ellipsize(font, name, width - 20.0F), centerX, top + 5.0F,
				Render.alpha(0xFFFFFFFF, alpha), false);
		Render.centeredText(graphics, font, Render.ellipsize(font, detail, width - 20.0F), centerX, top + 15.0F,
				Render.alpha(0xFFB9BBD0, alpha * 0.9F), false);
	}

	private void drawHint(GuiGraphics graphics, float appearance) {
		String hint = "hold " + keyHint() + "  ·  release to run  ·  esc or right click to cancel";
		Render.centeredText(graphics, this.font, hint, this.width * 0.5F, this.height - 22.0F,
				Render.alpha(0xFFD8DAE6, 0.55F * appearance), false);
	}

	private void drawEmptyState(GuiGraphics graphics, float appearance) {
		float width = 240.0F;
		float x = (this.width - width) * 0.5F;
		float y = this.height * 0.5F - 26.0F;
		Render.softShadow(graphics, x, y, width, 52.0F, 10.0F, 0.8F);
		Render.roundedRect(graphics, x, y, width, 52.0F, 10.0F, Render.alpha(0x12131C, 0.95F * appearance));
		Render.ring(graphics, x, y, width, 52.0F, 10.0F, 1.0F, Render.alpha(0x33FFFFFF, appearance));
		Render.centeredText(graphics, this.font, "No radial entries", this.width * 0.5F, y + 12.0F,
				Render.alpha(0xFFFFFFFF, appearance), false);
		Render.centeredText(graphics, this.font, "Add some in the radial editor", this.width * 0.5F, y + 26.0F,
				Render.alpha(0xFFB9BBD0, appearance), false);
	}

	private String keyHint() {
		try {
			return dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "the radial key";
		}
	}

	// ------------------------------------------------------------------- input

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			commitSelection();
			return true;
		}
		if (event.button() == 1) {
			cancel();
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	public boolean wasExecuted() {
		return committed;
	}
}
