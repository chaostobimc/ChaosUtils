package dev.chaosutils.feature.qol;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Local screenshot manager.
 *
 * <p>Watches your screenshots folder, shows a calm popup when a new capture appears and opens
 * a small manager with the newest shots: copy the image straight to the clipboard, copy the
 * path, open the folder or create a cropped copy. Everything happens locally with AWT and
 * nothing is uploaded anywhere.
 */
public final class ScreenshotManager implements Feature {
	public static final String ID = "screenshots";

	public enum CropMode {
		CENTER_SQUARE("Centre square"),
		WIDESCREEN("16:9 crop"),
		INSET("Trim borders"),
		HALF("Half size");

		private final String label;

		CropMode(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	private static Module module;
	private static Setting.Toggle watch;
	private static Setting.Number pollTicks;
	private static Setting.Toggle notify;
	private static Setting.Number notifySeconds;
	private static Setting.Toggle sound;
	private static Setting.Text soundId;
	private static Setting.Toggle autoOpen;
	private static Setting.Choice cropMode;
	private static Setting.Number insetPercent;
	private static Setting.Toggle copyAfterCrop;
	private static Setting.Toggle openFolderAfterCrop;
	private static Setting.Toggle cropOnKey;

	private static final List<Path> RECENT = new ArrayList<>(12);
	private static String lastSeen = "";
	private static int pollCounter;
	private static long toastUntil;
	private static String toastText = "";

	private static final Anim.Value toastFade = new Anim.Value(0.0F, 10.0F);

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Screenshot Manager",
				"Instant copy, crop and folder access for your screenshots.", Category.QOL, true));
		watch = (Setting.Toggle) module.add(new Setting.Toggle("watch", "Watch the folder",
				"Notice new screenshots automatically.", true));
		pollTicks = (Setting.Number) module.add(new Setting.Number("interval", "Check interval",
				"Ticks between folder checks.", 40.0, 20.0, 200.0, 10.0, "t"));
		notify = (Setting.Toggle) module.add(new Setting.Toggle("toast", "Popup",
				"Small animated popup when a screenshot was saved.", true));
		notifySeconds = (Setting.Number) module.add(new Setting.Number("duration", "Popup duration",
				"How long the popup stays.", 4.0, 1.0, 15.0, 0.5, "s"));
		sound = (Setting.Toggle) module.add(new Setting.Toggle("sound", "Sound",
				"Soft camera sound when a screenshot is saved.", true));
		soundId = (Setting.Text) module.add(new Setting.Text("sound_id", "Sound",
				"Sound event id.", "minecraft:entity.experience_orb.pickup", 96));
		autoOpen = (Setting.Toggle) module.add(new Setting.Toggle("auto_open", "Open manager automatically",
				"Open the manager as soon as a screenshot is taken.", false));
		cropMode = (Setting.Choice) module.add(new Setting.Choice("crop", "Crop preset",
				"Used by 'crop and copy' in the manager.", 0,
				CropMode.CENTER_SQUARE.label(), CropMode.WIDESCREEN.label(), CropMode.INSET.label(), CropMode.HALF.label()));
		insetPercent = (Setting.Number) module.add(new Setting.Number("inset", "Trim amount",
				"Percentage removed from each side by the trim preset.", 10.0, 1.0, 40.0, 1.0, "%"));
		copyAfterCrop = (Setting.Toggle) module.add(new Setting.Toggle("copy_crop", "Copy after cropping",
				"Put the cropped image on the clipboard right away.", true));
		openFolderAfterCrop = (Setting.Toggle) module.add(new Setting.Toggle("open_folder", "Open folder after crop",
				"Open the screenshots folder once the crop is written.", false));
		cropOnKey = (Setting.Toggle) module.add(new Setting.Toggle("crop_key", "Crop with the hotkey",
				"The screenshot hotkey crops the newest shot instead of opening the manager.", false));
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			return;
		}
		if (Keybinds.screenshotPopup != null && Keybinds.screenshotPopup.consumeClick()) {
			if (cropOnKey.get()) {
				Path cropped = cropLatest();
				if (cropped != null) {
					toast("Cropped: " + cropped.getFileName());
				}
			} else {
				client.setScreen(new dev.chaosutils.gui.ChaosScreens.ScreenshotScreen(client.screen));
			}
		}
		if (!watch.get()) {
			return;
		}
		if (--pollCounter > 0) {
			return;
		}
		pollCounter = Math.max(5, pollTicks.getInt());
		Path newest = newestScreenshot();
		if (newest == null) {
			return;
		}
		String name = newest.getFileName().toString();
		if (name.equals(lastSeen)) {
			return;
		}
		boolean first = lastSeen.isEmpty();
		lastSeen = name;
		if (first) {
			return;
		}
		if (notify.get()) {
			toast("Screenshot saved: " + name);
		}
		if (sound.get()) {
			playSound();
		}
		if (autoOpen.get()) {
			client.setScreen(new dev.chaosutils.gui.ChaosScreens.ScreenshotScreen(client.screen));
		}
	}

	private static void playSound() {
		try {
			var event = SoundLookup.get(soundId.get().trim());
			var instance = SoundLookup.ui(event, 1.8F, 0.5F);
			if (instance != null) {
				Minecraft.getInstance().getSoundManager().play(instance);
			}
		} catch (Throwable ignored) {
			// sound is optional
		}
	}

	private static void toast(String text) {
		toastText = text;
		toastUntil = System.currentTimeMillis() + (long) (notifySeconds.get() * 1000.0);
		toastFade.snap(0.0F);
	}

	// ------------------------------------------------------------------ files

	public static Path screenshotsDirectory() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
	}

	/** Newest PNG in the folder, or {@code null} when there is none. */
	public static Path newestScreenshot() {
		return listScreenshots(1).stream().findFirst().orElse(null);
	}

	/** Newest screenshots first, newest {@code limit} entries. */
	public static List<Path> listScreenshots(int limit) {
		Path directory = screenshotsDirectory();
		if (!Files.isDirectory(directory)) {
			return List.of();
		}
		try (Stream<Path> stream = Files.list(directory)) {
			return stream.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
					.sorted(Comparator.comparingLong((Path path) -> path.toFile().lastModified()).reversed())
					.limit(Math.max(1, limit))
					.toList();
		} catch (Throwable ignored) {
			return List.of();
		}
	}

	/** Fills the cached recent list used by the manager screen. */
	public static List<Path> recent(int limit) {
		RECENT.clear();
		RECENT.addAll(listScreenshots(limit));
		return RECENT;
	}

	public static Path cropLatest() {
		Path newest = newestScreenshot();
		return newest == null ? null : cropWithConfiguredPreset(newest);
	}

	/** Crops the given file with the preset chosen in the settings. */
	public static Path cropWithConfiguredPreset(Path source) {
		if (source == null) {
			return null;
		}
		return crop(source, CropMode.values()[clampMode(cropMode.get())], insetPercent.getInt());
	}

	private static int clampMode(int index) {
		return Math.max(0, Math.min(CropMode.values().length - 1, index));
	}

	/**
	 * Writes a cropped copy next to the original and returns its path.
	 *
	 * <p>Pure local image processing with AWT; on a headless environment (or if anything
	 * fails) it simply returns {@code null} instead of throwing.
	 */
	public static Path crop(Path source, CropMode mode, int insetPercent) {
		try {
			BufferedImage image = ImageIO.read(source.toFile());
			if (image == null) {
				return null;
			}
			BufferedImage result = switch (mode) {
				case CENTER_SQUARE -> {
					int side = Math.min(image.getWidth(), image.getHeight());
					yield image.getSubimage((image.getWidth() - side) / 2, (image.getHeight() - side) / 2, side, side);
				}
				case WIDESCREEN -> {
					int height = Math.max(1, Math.min(image.getHeight(), image.getWidth() * 9 / 16));
					int width = Math.max(1, Math.min(image.getWidth(), height * 16 / 9));
					yield image.getSubimage((image.getWidth() - width) / 2, (image.getHeight() - height) / 2, width, height);
				}
				case INSET -> {
					int inset = Math.max(0, insetPercent);
					int dx = image.getWidth() * inset / 200;
					int dy = image.getHeight() * inset / 200;
					int width = Math.max(1, image.getWidth() - dx * 2);
					int height = Math.max(1, image.getHeight() - dy * 2);
					yield image.getSubimage(dx, dy, width, height);
				}
				case HALF -> {
					int width = Math.max(1, image.getWidth() / 2);
					int height = Math.max(1, image.getHeight() / 2);
					BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					java.awt.Graphics2D graphics = scaled.createGraphics();
					graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
							java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					graphics.drawImage(image, 0, 0, width, height, null);
					graphics.dispose();
					yield scaled;
				}
			};
			String name = source.getFileName().toString();
			String base = name.toLowerCase(Locale.ROOT).endsWith(".png") ? name.substring(0, name.length() - 4) : name;
			Path target = source.resolveSibling(base + "_" + mode.name().toLowerCase(Locale.ROOT) + ".png");
			File file = target.toFile();
			if (!ImageIO.write(result, "png", file)) {
				return null;
			}
			if (copyAfterCrop.get()) {
				Clipboard.copyImage(target);
			}
			if (openFolderAfterCrop.get()) {
				Clipboard.openFile(screenshotsDirectory());
			}
			return target;
		} catch (Throwable ignored) {
			return null;
		}
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!notify.get() || toastText.isEmpty() || !HudPanel.visibleNow()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		boolean active = System.currentTimeMillis() < toastUntil;
		toastFade.set(active ? 1.0F : 0.0F);
		toastFade.update(dev.chaosutils.core.TickClock.frameDelta());
		float appearance = Anim.easeOutCubic(toastFade.get());
		if (appearance <= 0.01F) {
			return;
		}
		Font font = client.font;
		float scaleFactor = HudPanel.scale();
		String text = toastText;
		while (font.width(text) * scaleFactor > graphics.guiWidth() * 0.5F && text.length() > 8) {
			text = text.substring(0, text.length() - 2) + "…";
		}
		String hint = "  [press the screenshot key]";
		float width = (font.width(text) + font.width(hint)) * scaleFactor + HudPanel.padding() * 4.0F;
		float height = 14.0F * scaleFactor + HudPanel.padding() * 2.0F;
		float x = (graphics.guiWidth() - width) * 0.5F;
		float y = graphics.guiHeight() - 90.0F - (1.0F - appearance) * 8.0F;
		int accent = Render.alpha(0xFF4FC3F7, Anim.clamp01(appearance));
		HudPanel.panel(graphics, font, x, y, width, height, accent);
		HudPanel.text(graphics, font, text, x + HudPanel.padding() * 2.0F, y + HudPanel.padding(),
				Render.alpha(0xFFF2F2F7, Anim.clamp01(appearance)));
		HudPanel.text(graphics, font, hint, x + HudPanel.padding() * 2.0F + font.width(text) * scaleFactor,
				y + HudPanel.padding(), Render.alpha(0xFFBFC2CF, Anim.clamp01(appearance)));
	}

	@Override
	public void onDisabled() {
		toastText = "";
		toastUntil = 0L;
	}
}
