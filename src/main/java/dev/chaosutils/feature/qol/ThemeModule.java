package dev.chaosutils.feature.qol;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;

/** Look and feel of the click GUI. Everything here is live-applied. */
public final class ThemeModule {
	public static final String ID = "gui";

	public static Module MODULE;
	public static Setting.Color accent;
	public static Setting.Choice backgroundStyle;
	public static Setting.Color backgroundColor;
	public static Setting.Number backgroundOpacity;
	public static Setting.Number cornerRadius;
	public static Setting.Number animationSpeed;
	public static Setting.Toggle blur;
	public static Setting.Toggle tooltips;
	public static Setting.Toggle guiSounds;
	public static Setting.Number sidebarWidth;
	public static Setting.Toggle showKeybindHints;
	public static Setting.Toggle compactCards;

	private ThemeModule() {
	}

	public static void register() {
		MODULE = ModuleManager.register(new Module(ID, "Click GUI", "Appearance, animations and behaviour of the ChaosUtils interface.", Category.QOL, true));
		accent = (Setting.Color) MODULE.add(new Setting.Color("accent", "Accent colour", "Primary highlight colour used across the UI.", 0xFF7C5CFF));
		backgroundStyle = (Setting.Choice) MODULE.add(new Setting.Choice("background", "Backdrop", "How the GUI background is drawn.", 0, "Dark gradient", "Blur + gradient", "Flat", "Transparent"));
		backgroundColor = (Setting.Color) MODULE.add(new Setting.Color("background_color", "Backdrop tint", "Tint layered on top of the screen behind the GUI.", 0xCC101018));
		backgroundOpacity = (Setting.Number) MODULE.add(new Setting.Number("background_opacity", "Backdrop opacity", "Strength of the backdrop tint.", 0.8, 0.0, 1.0, 0.02));
		cornerRadius = (Setting.Number) MODULE.add(new Setting.Number("corner_radius", "Corner radius", "Roundness of panels and buttons.", 6.0, 0.0, 14.0, 0.5));
		animationSpeed = (Setting.Number) MODULE.add(new Setting.Number("animation_speed", "Animation speed", "How snappy hover, expand and scroll animations feel.", 1.0, 0.3, 2.5, 0.05, "x"));
		blur = (Setting.Toggle) MODULE.add(new Setting.Toggle("blur", "Menu blur", "Blurs the world behind the GUI like the vanilla pause menu.", true));
		tooltips = (Setting.Toggle) MODULE.add(new Setting.Toggle("tooltips", "Tooltips", "Show descriptions when hovering settings.", true));
		guiSounds = (Setting.Toggle) MODULE.add(new Setting.Toggle("sounds", "UI sounds", "Play a soft vanilla click sound on interaction.", true));
		sidebarWidth = (Setting.Number) MODULE.add(new Setting.Number("sidebar_width", "Sidebar width", "Width of the category sidebar.", 104.0, 80.0, 160.0, 1.0, "px"));
		showKeybindHints = (Setting.Toggle) MODULE.add(new Setting.Toggle("keybind_hints", "Show keybinds", "Display the bound hotkey on module cards.", true));
		compactCards = (Setting.Toggle) MODULE.add(new Setting.Toggle("compact", "Compact cards", "Reduce vertical padding so more settings fit on screen.", false));
	}
}
