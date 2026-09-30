package dev.chaosutils.core;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clipboard and file-system helpers.
 *
 * <p>Text goes through Minecraft's own clipboard handler; image copying uses AWT, which
 * is the only cross platform way to put a PNG on the system clipboard. Every AWT call is
 * guarded because some JVMs run headless.
 */
public final class Clipboard {
	private static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils/Clipboard");

	private Clipboard() {
	}

	public static void copyText(String text) {
		try {
			Minecraft.getInstance().keyboardHandler.setClipboard(text);
		} catch (Throwable throwable) {
			try {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
			} catch (Throwable ignored) {
				LOGGER.warn("Could not copy text to the clipboard");
			}
		}
	}

	public static boolean copyImage(Path png) {
		if (GraphicsEnvironment.isHeadless()) {
			return false;
		}
		try {
			Image image = ImageIO.read(png.toFile());
			if (image == null) {
				return false;
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
			return true;
		} catch (Throwable throwable) {
			LOGGER.warn("Could not copy {} to the clipboard", png.getFileName(), throwable);
			return false;
		}
	}

	public static boolean openFile(Path path) {
		if (GraphicsEnvironment.isHeadless()) {
			return false;
		}
		try {
			File file = path.toFile();
			if (!file.exists()) {
				return false;
			}
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (file.isDirectory()) {
					desktop.open(file);
				} else {
					desktop.open(file);
				}
				return true;
			}
		} catch (Throwable throwable) {
			LOGGER.warn("Could not open {}", path, throwable);
		}
		return false;
	}

	private static final class ImageSelection implements java.awt.datatransfer.Transferable {
		private final Image image;

		private ImageSelection(Image image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws java.awt.datatransfer.UnsupportedFlavorException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}
}
