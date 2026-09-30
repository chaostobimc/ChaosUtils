package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Custom crosshair designer.
 *
 * <p>Vanilla's crosshair is cancelled (Gui hook) and replaced by a fully configurable one:
 * four shapes, thickness, gap, length, dot, outline and transparency. The transparency has
 * a dynamic mode: the crosshair fades out while drawing a bow, eating, blocking or while
 * the attack cooldown is recharging, so it never hides what you are aiming at. The
 * crosshair also spreads with movement like the vanilla one.
 *
 * <p>Everything here is drawing only - no gameplay value is read or changed.
 */
public final class CrosshairDesigner implements Feature {
	public static final String ID = "crosshair";

	private static Module module;
	private static Setting.Choice shape;
	private static Setting.Number thickness;
	private static Setting.Number gap;
	private static Setting.Number length;
	private static Setting.Toggle dot;
	private static Setting.Number dotSize;
	private static Setting.Color color;
	private static Setting.Color outlineColor;
	private static Setting.Number opacity;
	private static Setting.Toggle dynamicOpacity;
	private static Setting.Number dynamicOpacityValue;
	private static Setting.Toggle spread;
	private static Setting.Number spreadAmount;
	private static Setting.Toggle hideInThirdPerson;
	private static Setting.Toggle showCooldownRing;

	private final Anim.Value currentSpread = new Anim.Value(0.0F, 12.0F);

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Crosshair Designer",
				"Fully custom crosshair: shape, colours, gap, dot and smart transparency.", Category.VISUAL, false));
		shape = (Setting.Choice) module.add(new Setting.Choice("shape", "Shape",
				"Geometry of the crosshair.", 0, "Cross", "Cross + dot", "Dot only", "Circle", "Bracket", "None"));
		thickness = (Setting.Number) module.add(new Setting.Number("thickness", "Thickness",
				"Line thickness in pixels.", 1.0, 1.0, 4.0, 0.5, "px"));
		gap = (Setting.Number) module.add(new Setting.Number("gap", "Gap",
				"Space between the centre and the four lines.", 3.0, 0.0, 12.0, 0.5, "px"));
		length = (Setting.Number) module.add(new Setting.Number("length", "Length",
				"Length of each line.", 5.0, 1.0, 16.0, 0.5, "px"));
		dot = (Setting.Toggle) module.add(new Setting.Toggle("dot", "Centre dot",
				"Draw a dot in the exact centre.", false));
		dotSize = (Setting.Number) module.add(new Setting.Number("dot_size", "Dot size",
				"Size of the centre dot.", 1.0, 1.0, 4.0, 0.5, "px"));
		color = (Setting.Color) module.add(new Setting.Color("color", "Colour",
				"Main crosshair colour.", 0xFFFFFFFF));
		outlineColor = (Setting.Color) module.add(new Setting.Color("outline", "Outline colour",
				"Dark outline that keeps the crosshair readable.", 0x40000000));
		opacity = (Setting.Number) module.add(new Setting.Number("opacity", "Opacity",
				"Base opacity of the crosshair.", 1.0, 0.1, 1.0, 0.05));
		dynamicOpacity = (Setting.Toggle) module.add(new Setting.Toggle("dynamic", "Fade while aiming",
				"Fade out while drawing a bow, eating, blocking or while aiming at a player.", true));
		dynamicOpacityValue = (Setting.Number) module.add(new Setting.Number("dynamic_opacity", "Faded opacity",
				"Opacity used while the dynamic fade is active.", 0.15, 0.0, 0.9, 0.05));
		spread = (Setting.Toggle) module.add(new Setting.Toggle("spread", "Movement spread",
				"Open the crosshair while walking and attacking.", true));
		spreadAmount = (Setting.Number) module.add(new Setting.Number("spread_amount", "Spread amount",
				"How far the lines open up.", 3.0, 0.0, 10.0, 0.5, "px"));
		hideInThirdPerson = (Setting.Toggle) module.add(new Setting.Toggle("hide_third_person", "Hide in third person",
				"Do not draw the crosshair in third person view.", false));
		showCooldownRing = (Setting.Toggle) module.add(new Setting.Toggle("cooldown", "Attack indicator ring",
				"Draw a thin ring while the attack cooldown recharges.", false));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	/** True when the mixin may cancel the vanilla crosshair. */
	public static boolean replacesVanilla() {
		if (!ModuleManager.enabled(ID)) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui) {
			return false;
		}
		if (client.screen != null) {
			return false;
		}
		if (hideInThirdPerson.get() && !client.options.getCameraType().isFirstPerson()) {
			return true;
		}
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || HudPanel.scale() <= 0.0F) {
			return;
		}
		if (hideInThirdPerson.get() && !client.options.getCameraType().isFirstPerson()) {
			return;
		}
		int selected = shape.get();
		if (selected == 5) {
			return;
		}
		float centerX = graphics.getScaledWindowWidth() * 0.5F;
		float centerY = graphics.getScaledWindowHeight() * 0.5F;

		float targetSpread = 0.0F;
		if (spread.get()) {
			double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
			boolean attacking = player.getAttackAnim(partialTick) > 0.05F && player.getAttackAnim(partialTick) < 0.95F;
			targetSpread = (float) Math.min(1.0, horizontalSpeed * 1.4) * spreadAmount.getFloat();
			if (attacking || player.isSprinting()) {
				targetSpread += spreadAmount.getFloat() * 0.45F;
			}
		}
		currentSpread.set(targetSpread);
		currentSpread.update(dev.chaosutils.core.TickClock.frameDelta());
		float spreadPixels = currentSpread.get();

		float alpha = opacity.getFloat();
		if (dynamicOpacity.get() && fadesOut(client, player)) {
			alpha = dynamicOpacityValue.getFloat();
		}
		alpha *= Math.max(0.15F, HudPanel.scale());
		int mainColor = Render.alpha(color.get(), Anim.clamp01(alpha));
		int borderColor = Render.alpha(outlineColor.get(), Anim.clamp01(alpha * 0.85F));

		float lineThickness = thickness.getFloat();
		float lineLength = length.getFloat();
		float gapSize = gap.getFloat() + spreadPixels;

		if (selected == 3) {
			drawCircle(graphics, centerX, centerY, gapSize + lineLength * 0.5F, lineThickness, mainColor);
		} else if (selected == 4) {
			drawBracket(graphics, centerX, centerY, gapSize, lineLength, lineThickness, mainColor);
		} else {
			float offset = gapSize + lineLength * 0.5F;
			rect(graphics, centerX - lineThickness * 0.5F, centerY - offset - lineLength * 0.5F, lineThickness, lineLength, borderColor);
			rect(graphics, centerX - lineThickness * 0.5F, centerY + offset - lineLength * 0.5F, lineThickness, lineLength, borderColor);
			rect(graphics, centerX - offset - lineLength * 0.5F, centerY - lineThickness * 0.5F, lineLength, lineThickness, borderColor);
			rect(graphics, centerX + offset - lineLength * 0.5F, centerY - lineThickness * 0.5F, lineLength, lineThickness, borderColor);
			float inner = Math.max(1.0F, lineThickness - 2.0F);
			float innerOffset = offset;
			rect(graphics, centerX - inner * 0.5F, centerY - innerOffset - lineLength * 0.5F, inner, lineLength, mainColor);
			rect(graphics, centerX - inner * 0.5F, centerY + innerOffset - lineLength * 0.5F, inner, lineLength, mainColor);
			rect(graphics, centerX - innerOffset - lineLength * 0.5F, centerY - inner * 0.5F, lineLength, inner, mainColor);
			rect(graphics, centerX + innerOffset - lineLength * 0.5F, centerY - inner * 0.5F, lineLength, inner, mainColor);
		}

		if (dot.get() || selected == 1 || selected == 2) {
			float size = dot.get() ? dotSize.getFloat() : 1.0F;
			rect(graphics, centerX - size * 0.5F, centerY - size * 0.5F, size, size, mainColor);
		}

		if (showCooldownRing.get()) {
			drawCooldownRing(graphics, centerX, centerY, gapSize + lineLength + 3.0F, player, partialTick, alpha);
		}
	}

	private static boolean fadesOut(Minecraft client, Player player) {
		if (player.isUsingItem()) {
			return true;
		}
		try {
			HitResult hit = client.hitResult;
			if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof Player) {
				return true;
			}
		} catch (Throwable ignored) {
			// lookup target is unavailable - keep the normal opacity
		}
		return false;
	}

	private static void drawCooldownRing(GuiGraphics graphics, float centerX, float centerY, float radius, Player player,
			float partialTick, float alpha) {
		float progress;
		try {
			progress = Anim.clamp01(player.getAttackStrengthScale(partialTick));
		} catch (Throwable ignored) {
			return;
		}
		if (progress >= 1.0F) {
			return;
		}
		int color = Render.alpha(0xFFFFFFFF, Anim.clamp01(alpha * 0.5F));
		int segments = 24;
		int visible = Math.max(1, (int) (segments * progress));
		for (int i = 0; i < visible; i++) {
			double angle = (i / (double) segments) * Math.PI * 2.0 - Math.PI * 0.5;
			float x = centerX + (float) Math.cos(angle) * radius;
			float y = centerY + (float) Math.sin(angle) * radius;
			rect(graphics, x, y, 1.5F, 1.5F, color);
		}
	}

	private static void drawCircle(GuiGraphics graphics, float centerX, float centerY, float radius, float thickness, int color) {
		int segments = 48;
		for (int i = 0; i < segments; i++) {
			double angle = (i / (double) segments) * Math.PI * 2.0;
			float x = centerX + (float) Math.cos(angle) * radius;
			float y = centerY + (float) Math.sin(angle) * radius;
			rect(graphics, x, y, thickness, thickness, color);
		}
	}

	private static void drawBracket(GuiGraphics graphics, float centerX, float centerY, float gapSize, float length, float thickness, int color) {
		float h = thickness;
		float corner = Math.max(2.0F, length * 0.5F);
		rect(graphics, centerX - gapSize - corner, centerY - gapSize - h, corner, h, color);
		rect(graphics, centerX - gapSize - h, centerY - gapSize - corner, h, corner, color);
		rect(graphics, centerX + gapSize, centerY - gapSize - h, corner, h, color);
		rect(graphics, centerX + gapSize, centerY - gapSize - corner, h, corner, color);
		rect(graphics, centerX - gapSize - corner, centerY + gapSize, corner, h, color);
		rect(graphics, centerX - gapSize - h, centerY + gapSize, h, corner, color);
		rect(graphics, centerX + gapSize, centerY + gapSize, corner, h, color);
		rect(graphics, centerX + gapSize, centerY + gapSize - corner + h, h, corner, color);
	}

	private static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		Render.rect(graphics, x, y, width, height, color);
	}
}
