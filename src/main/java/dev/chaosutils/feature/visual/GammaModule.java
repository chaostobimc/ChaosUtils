package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Night vision / brightness modifier.
 *
 * <p>The vanilla brightness option is exactly the value the lightmap uses when it builds
 * the world lighting, so ChaosUtils drives that option instead of patching the lightmap
 * texture: the result is identical, it applies instantly to the world, it cannot break the
 * render pipeline and it is trivially reversible. Your own brightness value is stored when
 * the feature activates and restored the moment it is turned off, on disconnect and when
 * the config is reloaded.
 *
 * <p>An optional, very subtle tint sells the "night vision" look without touching colours
 * the server ever sees.
 */
public final class GammaModule implements Feature {
	public static final String ID = "gamma";

	private static Module module;
	private static Setting.Toggle override;
	private static Setting.Number gamma;
	private static Setting.Number transitionSpeed;
	private static Setting.Toggle boostKey;
	private static Setting.Toggle boostToggle;
	private static Setting.Number boostValue;
	private static Setting.Toggle tintEnabled;
	private static Setting.Color tintColor;
	private static Setting.Number tintStrength;
	private static Setting.Toggle indicator;

	private static boolean applied;
	private static double savedGamma = Double.NaN;
	private static float renderedGamma = 0.5F;
	private static boolean boostHeld;
	private static boolean boostLatched;

	public GammaModule() {
		// The animated brightness starts wherever the player's own setting is.
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Night Vision & Gamma",
				"Lift the world brightness, optionally on a hotkey, and put it back when you are done.",
				Category.VISUAL, false));
		override = (Setting.Toggle) module.add(new Setting.Toggle("override", "Override brightness",
				"While enabled, the world brightness is set to the value below.", true));
		gamma = (Setting.Number) module.add(new Setting.Number("gamma", "Brightness",
				"1.00 is the maximum vanilla brightness setting.", 1.0, 0.0, 1.0, 0.05));
		transitionSpeed = (Setting.Number) module.add(new Setting.Number("smoothing", "Transition speed",
				"Fade speed when entering or leaving the override.", 3.0, 0.5, 10.0, 0.5, "x"));
		boostKey = (Setting.Toggle) module.add(new Setting.Toggle("boost_key", "Hotkey boost",
				"Temporarily go to maximum brightness with the bound key.", true));
		boostToggle = (Setting.Toggle) module.add(new Setting.Toggle("boost_toggle", "Boost is a toggle",
				"Off: the key has to be held.", false));
		boostValue = (Setting.Number) module.add(new Setting.Number("boost_value", "Boost brightness",
				"Brightness used while the hotkey is active.", 1.0, 0.0, 1.0, 0.05));
		tintEnabled = (Setting.Toggle) module.add(new Setting.Toggle("tint", "Night vision tint",
				"A soft colour wash while night vision is active.", false));
		tintColor = (Setting.Color) module.add(new Setting.Color("tint_color", "Tint colour",
				"Colour of the overlay, alpha is ignored.", 0xFF6DF0A0));
		tintStrength = (Setting.Number) module.add(new Setting.Number("tint_strength", "Tint strength",
				"Opacity of the colour wash.", 0.08, 0.01, 0.4, 0.01));
		indicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Show indicator",
				"Small badge while the brightness override is active.", false));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null || client.options == null) {
			return;
		}
		if (!isEnabled()) {
			release(client);
			return;
		}
		boolean wanted = override.get() || boostActive();
		float target = boostActive() ? boostValue.getFloat() : gamma.getFloat();
		renderedGamma = Anim.approach(renderedGamma, target, transitionSpeed.getFloat(), 0.05F);
		if (!wanted) {
			release(client);
			return;
		}
		if (!applied) {
			savedGamma = client.options.gamma().get();
			applied = true;
			renderedGamma = (float) savedGamma;
		}
		try {
			client.options.gamma().set((double) renderedGamma);
		} catch (Throwable ignored) {
			applied = false;
		}
	}

	private boolean boostActive() {
		if (!boostKey.get() || Keybinds.toggleGamma == null) {
			return false;
		}
		if (boostToggle.get()) {
			if (Keybinds.toggleGamma.wasPressed()) {
				boostLatched = !boostLatched;
			}
			return boostLatched;
		}
		boostHeld = Keybinds.toggleGamma.isPressed();
		return boostHeld;
	}

	private static void release(Minecraft client) {
		if (boostLatched || boostHeld) {
			return;
		}
		if (!applied) {
			return;
		}
		applied = false;
		try {
			if (!Double.isNaN(savedGamma)) {
				client.options.gamma().set(savedGamma);
			}
		} catch (Throwable ignored) {
			// nothing to restore
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!isEnabled()) {
			return;
		}
		if (tintEnabled.get() && (override.get() || boostActive())) {
			int color = Render.alpha(tintColor.get(), tintStrength.getFloat());
			Render.rect(graphics, 0.0F, 0.0F, graphics.getScaledWindowWidth(), graphics.getScaledWindowHeight(), color);
		}
		if (indicator.get() && (override.get() || boostActive())) {
			Minecraft client = Minecraft.getInstance();
			String text = "Night Vision";
			float width = client.font.width(text) + 8.0F;
			dev.chaosutils.feature.hud.HudPanel.panel(graphics, client.font, 6.0F, 6.0F, width, 12.0F, 0xFF6DF0A0);
			dev.chaosutils.feature.hud.HudPanel.text(graphics, client.font, text, 10.0F, 8.0F, 0xFFF2F2F7);
		}
	}

	@Override
	public void onDisabled() {
		release(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		release(Minecraft.getInstance());
	}
}
