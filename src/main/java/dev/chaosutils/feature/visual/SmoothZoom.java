package dev.chaosutils.feature.visual;

import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.Features;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Seamless smooth zoom.
 *
 * <p>Zooming scales the field of view, which is a pure optical effect: the player, the
 * hitbox and the movement stay exactly as they are and no packet is involved. The zoom
 * level, the smoothing speed and the optional sensitivity compensation are configurable,
 * and the mouse wheel adjusts the zoom level while the key is held.
 */
public final class SmoothZoom implements Feature {
	public static final String ID = "zoom";

	private static Module module;
	private static Setting.Number zoomLevel;
	private static Setting.Number smoothness;
	private static Setting.Toggle sensitivityCompensation;
	private static Setting.Toggle scrollAdjust;
	private static Setting.Number scrollStep;
	private static Setting.Number minZoom;
	private static Setting.Number maxZoom;
	private static Setting.Toggle showIndicator;
	private static Setting.Toggle holdToZoom;

	private static boolean zoomActive;
	private static float current = 1.0F;
	private static float scrollOffset = 1.0F;
	private static boolean sensitivityApplied;
	private static double previousSensitivity;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Smooth Zoom",
				"Optical zoom on a held key, with buttery smooth transitions.", Category.VISUAL, true));
		zoomLevel = (Setting.Number) module.add(new Setting.Number("level", "Zoom level",
				"How far the view zooms in.", 4.0, 1.5, 20.0, 0.5, "x"));
		smoothness = (Setting.Number) module.add(new Setting.Number("smoothness", "Smoothness",
				"Lower is a slower, more cinematic transition.", 12.0, 2.0, 30.0, 0.5, "x"));
		sensitivityCompensation = (Setting.Toggle) module.add(new Setting.Toggle("sensitivity", "Sensitivity compensation",
				"Slows the mouse down while zoomed so aiming stays precise.", true));
		scrollAdjust = (Setting.Toggle) module.add(new Setting.Toggle("scroll", "Mouse wheel adjusts zoom",
				"Scroll while zooming to change the zoom level on the fly.", true));
		scrollStep = (Setting.Number) module.add(new Setting.Number("scroll_step", "Scroll step",
				"Zoom change per wheel notch.", 0.25, 0.05, 2.0, 0.05, "x"));
		minZoom = (Setting.Number) module.add(new Setting.Number("min", "Minimum zoom",
				"Lower bound for the wheel adjustment.", 1.5, 1.0, 10.0, 0.5, "x"));
		maxZoom = (Setting.Number) module.add(new Setting.Number("max", "Maximum zoom",
				"Upper bound for the wheel adjustment.", 20.0, 2.0, 40.0, 0.5, "x"));
		showIndicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Zoom indicator",
				"Show the current zoom level while zooming.", true));
		holdToZoom = (Setting.Toggle) module.add(new Setting.Toggle("hold", "Hold to zoom",
				"Off: the zoom key toggles instead of being held.", true));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (!isEnabled() || client.player == null) {
			stop(client);
			return;
		}
		boolean keyPressed = Keybinds.zoom != null && Keybinds.zoom.isPressed();
		if (holdToZoom.get()) {
			setActive(client, keyPressed);
		} else if (keyPressed && Keybinds.zoom.wasPressed()) {
			setActive(client, !zoomActive);
		}
		applySensitivity(client);
	}

	private static void setActive(Minecraft client, boolean active) {
		if (active == zoomActive) {
			return;
		}
		zoomActive = active;
		if (active) {
			scrollOffset = 1.0F;
		} else {
			Features.setZoomFactor(1.0F);
			restoreSensitivity(client);
		}
	}

	private static void stop(Minecraft client) {
		if (zoomActive) {
			zoomActive = false;
			Features.setZoomFactor(1.0F);
		}
		current = 1.0F;
		restoreSensitivity(client);
	}

	private static void restoreSensitivity(Minecraft client) {
		if (!sensitivityApplied) {
			return;
		}
		sensitivityApplied = false;
		if (client == null) {
			return;
		}
		try {
			client.options.sensitivity().set(previousSensitivity);
		} catch (Throwable ignored) {
			// the option is simply left as it is
		}
	}

	private static void applySensitivity(Minecraft client) {
		boolean wanted = sensitivityCompensation.get() && zoomActive;
		try {
			if (wanted && !sensitivityApplied) {
				previousSensitivity = client.options.sensitivity().get();
				sensitivityApplied = true;
			}
			if (wanted) {
				client.options.sensitivity().set(previousSensitivity * Math.sqrt(Math.max(0.05F, current)));
			} else if (sensitivityApplied) {
				restoreSensitivity(client);
			}
		} catch (Throwable ignored) {
			sensitivityApplied = false;
		}
	}

	/** Called from the mouse handler mixin while the player is in game. */
	public static void onScroll(double verticalAmount) {
		if (!zoomActive || !ModuleManager.enabled(ID) || !scrollAdjust.get()) {
			return;
		}
		float step = scrollStep.getFloat();
		float direction = verticalAmount > 0 ? 1.0F : (verticalAmount < 0 ? -1.0F : 0.0F);
		float base = (float) Math.max(1.0, zoomLevel.get());
		scrollOffset = Anim.clamp(scrollOffset * (1.0F + direction * step * 0.35F),
				(float) (minZoom.get() / base), (float) (maxZoom.get() / base));
	}

	/**
	 * The factor the renderer multiplies the field of view with ({@code < 1} means zoomed
	 * in). Called once per frame from the {@code GameRenderer} hook, which is also where the
	 * smooth transition is advanced so it matches the rendered frames exactly.
	 */
	public static float fovFactor(float partialTick) {
		if (!zoomActive || !ModuleManager.enabled(ID)) {
			current = 1.0F;
			return 1.0F;
		}
		float level = (float) Math.max(1.0, zoomLevel.get()) * scrollOffset;
		float target = 1.0F / level;
		current = Anim.approach(current, target, smoothness.getFloat(), TickClock.frameDelta());
		if (Math.abs(current - target) < 0.0015F) {
			current = target;
		}
		Features.setZoomFactor(current);
		return current;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!showIndicator.get() || !zoomActive || HudPanel.scale() <= 0.0F) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		String text = String.format(Locale.ROOT, "%.1fx", 1.0F / Math.max(0.01F, current));
		float width = font.width(text) + 8.0F;
		float x = (graphics.getScaledWindowWidth() - width) * 0.5F;
		float y = graphics.getScaledWindowHeight() - 68.0F;
		HudPanel.panel(graphics, font, x, y, width, 12.0F, 0xFF4FC3F7);
		HudPanel.text(graphics, font, text, x + 4.0F, y + 2.0F, 0xFFF2F2F7);
	}

	public static boolean isZooming() {
		return zoomActive;
	}

	public static float currentFactor() {
		return current;
	}

	@Override
	public void onDisabled() {
		stop(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		stop(Minecraft.getInstance());
	}
}
