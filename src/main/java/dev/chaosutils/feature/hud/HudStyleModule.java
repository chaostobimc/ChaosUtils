package dev.chaosutils.feature.hud;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;

/**
 * Shared look and feel of every HUD overlay.
 *
 * <p>One place to make all overlays match: background strength, border, text shadow and
 * global scaling. Individual overlays only add what is specific to them.
 */
public final class HudStyleModule {
	public static final String ID = "hud_style";

	public static Module MODULE;
	public static Setting.Color backgroundColor;
	public static Setting.Number backgroundOpacity;
	public static Setting.Toggle border;
	public static Setting.Number cornerRadius;
	public static Setting.Toggle textShadow;
	public static Setting.Number hudScale;
	public static Setting.Number padding;
	public static Setting.Toggle hideInScreens;
	public static Setting.Toggle accentBars;

	private HudStyleModule() {
	}

	public static void register() {
		MODULE = ModuleManager.register(new Module(ID, "HUD Style",
				"Shared appearance of every ChaosUtils overlay.", Category.HUD, true));
		backgroundColor = (Setting.Color) MODULE.add(new Setting.Color("background", "Background",
				"Panel colour behind overlay text.", 0xFF0E0E16));
		backgroundOpacity = (Setting.Number) MODULE.add(new Setting.Number("opacity", "Background opacity",
				"0 makes every overlay text only.", 0.55, 0.0, 1.0, 0.05));
		border = (Setting.Toggle) MODULE.add(new Setting.Toggle("border", "Border",
				"Thin outline in the feature's accent colour.", true));
		cornerRadius = (Setting.Number) MODULE.add(new Setting.Number("radius", "Corner radius",
				"Roundness of overlay panels.", 4.0, 0.0, 10.0, 0.5, "px"));
		textShadow = (Setting.Toggle) MODULE.add(new Setting.Toggle("text_shadow", "Text shadow",
				"Improves readability on bright backgrounds.", true));
		hudScale = (Setting.Number) MODULE.add(new Setting.Number("scale", "Overlay scale",
				"Scales all ChaosUtils overlays at once.", 1.0, 0.6, 1.6, 0.05, "x"));
		padding = (Setting.Number) MODULE.add(new Setting.Number("padding", "Panel padding",
				"Space between the panel edge and its content.", 3.0, 1.0, 8.0, 0.5, "px"));
		hideInScreens = (Setting.Toggle) MODULE.add(new Setting.Toggle("hide_in_screens", "Hide in menus",
				"Hide overlays while a screen (inventory, chat, ...) is open.", false));
		accentBars = (Setting.Toggle) MODULE.add(new Setting.Toggle("accent_bars", "Accent bars",
				"Draw a coloured bar for health, durability and progress values.", true));
	}
}
