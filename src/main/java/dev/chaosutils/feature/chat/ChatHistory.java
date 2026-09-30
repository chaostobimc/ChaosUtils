package dev.chaosutils.feature.chat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Chat history preserver and filter.
 *
 * <p>Keeps a bounded copy of everything you saw, can restore it after a reconnect (useful on
 * servers where the chat clears on every world change), filters noise out of the chat and can
 * optionally write a plain text log next to the config. Filtering only hides lines on your
 * own client - the messages still arrive normally, nothing is blocked or reported.
 */
public final class ChatHistory implements Feature {
	public static final String ID = "chat_history";

	private static Module module;
	private static Setting.Toggle persistent;
	private static Setting.Number restoreCount;
	private static Setting.Toggle restoreHeader;
	private static Setting.Toggle hideSystem;
	private static Setting.Toggle hideJoinLeave;
	private static Setting.Text blockList;
	private static Setting.Toggle regexMode;
	private static Setting.Toggle collapseDuplicates;
	private static Setting.Number duplicateWindow;
	private static Setting.Toggle logToFile;
	private static Setting.Number logFlushInterval;

	private static BufferedWriter logWriter;
	private static int linesSinceFlush;
	private static long lastFlushAt;

	// Duplicate collapsing state (bounded: one entry + counter).
	private static String lastDuplicateText = "";
	private static long lastDuplicateAt;
	private static int duplicateCount;
	private static int pendingDuplicateReport;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Chat History & Filter",
				"Keep chat across reconnects, filter noise and search everything you saw.", Category.CHAT, true));
		persistent = (Setting.Toggle) module.add(new Setting.Toggle("persistent", "Restore after reconnect",
				"Re-add the last messages when you join a world again.", true));
		restoreCount = (Setting.Number) module.add(new Setting.Number("restore_count", "Restore lines",
				"How many lines are restored.", 30.0, 5.0, 200.0, 5.0, " lines"));
		restoreHeader = (Setting.Toggle) module.add(new Setting.Toggle("restore_header", "Restore header",
				"Show a separator line before the restored messages.", true));
		hideSystem = (Setting.Toggle) module.add(new Setting.Toggle("hide_system", "Hide system messages",
				"Hide locally generated system notices.", false));
		hideJoinLeave = (Setting.Toggle) module.add(new Setting.Toggle("hide_join_leave", "Hide join/leave",
				"Hide 'player joined the game' style messages.", false));
		blockList = (Setting.Text) module.add(new Setting.Text("block", "Block list",
				"Comma separated words; any message containing one is hidden.", "", 512));
		regexMode = (Setting.Toggle) module.add(new Setting.Toggle("regex", "Treat block list as regex",
				"Each entry is a regular expression instead of a plain word.", false));
		collapseDuplicates = (Setting.Toggle) module.add(new Setting.Toggle("duplicates", "Collapse duplicates",
				"Hide repeated identical messages and report a count instead.", false));
		duplicateWindow = (Setting.Number) module.add(new Setting.Number("duplicate_window", "Duplicate window",
				"Seconds within which identical messages count as one.", 6.0, 1.0, 60.0, 1.0, "s"));
		logToFile = (Setting.Toggle) module.add(new Setting.Toggle("log_file", "Write a chat log file",
				"Append everything to config/chaosutils-chat.log.", false));
		logFlushInterval = (Setting.Number) module.add(new Setting.Number("flush_interval", "Flush interval",
				"Seconds between writing the buffered log to disk.", 5.0, 1.0, 30.0, 1.0, "s"));
	}

	/** Wires the chat events; called once during client initialisation. */
	public static void initEvents() {
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			if (message == null) {
				return;
			}
			ChatLog.add(message, ChatLog.Kind.PLAYER, false);
			writeFileLine(message.getString());
		});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (message == null) {
				return;
			}
			ChatLog.add(message, overlay ? ChatLog.Kind.SERVER : ChatLog.Kind.SYSTEM, overlay);
			writeFileLine(message.getString());
		});
		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			if (!ModuleManager.enabled(ID) || message == null) {
				return true;
			}
			try {
				return !shouldHide(message.getString(), false);
			} catch (Throwable ignored) {
				return true;
			}
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (!ModuleManager.enabled(ID) || message == null || overlay) {
				return true;
			}
			try {
				return !shouldHide(message.getString(), true);
			} catch (Throwable ignored) {
				return true;
			}
		});
	}

	private static boolean shouldHide(String plain, boolean system) {
		if (plain == null || plain.isEmpty()) {
			return false;
		}
		if (system && hideSystem.get()) {
			return true;
		}
		String lower = plain.toLowerCase(Locale.ROOT);
		if (hideJoinLeave.get() && (lower.contains("joined the game") || lower.contains("left the game"))) {
			return true;
		}
		String block = blockList.get();
		if (!block.isBlank()) {
			if (regexMode.get()) {
				for (String entry : block.split(",")) {
					String pattern = entry.trim();
					if (pattern.isEmpty()) {
						continue;
					}
					try {
						if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(plain).find()) {
							return true;
						}
					} catch (PatternSyntaxException ignored) {
						// a broken pattern is skipped, never fatal
					}
				}
			} else {
				for (String entry : block.split(",")) {
					String needle = entry.trim().toLowerCase(Locale.ROOT);
					if (!needle.isEmpty() && lower.contains(needle)) {
						return true;
					}
				}
			}
		}
		if (collapseDuplicates.get()) {
			long now = System.currentTimeMillis();
			long window = (long) (duplicateWindow.get() * 1000.0);
			if (plain.equals(lastDuplicateText) && now - lastDuplicateAt <= window) {
				lastDuplicateAt = now;
				duplicateCount++;
				pendingDuplicateReport = duplicateCount;
				return true;
			}
			lastDuplicateText = plain;
			lastDuplicateAt = now;
			duplicateCount = 1;
		}
		return false;
	}

	private static void writeFileLine(String line) {
		if (!logToFile.get()) {
			if (logWriter != null) {
				closeLog();
			}
			return;
		}
		try {
			if (logWriter == null) {
				Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("chaosutils-chat.log");
				Files.createDirectories(path.getParent());
				logWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);
			}
			logWriter.write("[" + java.time.LocalTime.now().withNano(0) + "] " + line);
			logWriter.newLine();
			linesSinceFlush++;
			if (linesSinceFlush >= 20) {
				logWriter.flush();
				linesSinceFlush = 0;
				lastFlushAt = System.currentTimeMillis();
			}
		} catch (Throwable ignored) {
			closeLog();
		}
	}

	private static void closeLog() {
		if (logWriter != null) {
			try {
				logWriter.flush();
				logWriter.close();
			} catch (IOException ignored) {
				// nothing else to do
			}
			logWriter = null;
		}
	}

	@Override
	public void onTick(Minecraft client) {
		if (logWriter != null) {
			long interval = (long) (logFlushInterval.get() * 1000.0);
			if (linesSinceFlush > 0 && System.currentTimeMillis() - lastFlushAt > interval) {
				try {
					logWriter.flush();
					linesSinceFlush = 0;
					lastFlushAt = System.currentTimeMillis();
				} catch (IOException ignored) {
					closeLog();
				}
			}
		}
		if (pendingDuplicateReport > 1 && client.player != null) {
			long window = (long) (duplicateWindow.get() * 1000.0);
			if (System.currentTimeMillis() - lastDuplicateAt > window) {
				int count = pendingDuplicateReport;
				pendingDuplicateReport = 0;
				client.player.displayClientMessage(
						Component.literal("§8[ChaosUtils] §7previous message repeated §f" + count + "×"), false);
			}
		}
		if (Keybinds.chatHistory != null && Keybinds.chatHistory.consumeClick() && client.player != null) {
			client.setScreen(new dev.chaosutils.gui.ChaosScreens.ChatHistoryScreen(client.screen));
		}
	}

	@Override
	public void onWorldJoin() {
		if (!persistent.get()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.gui == null || client.player == null) {
			return;
		}
		List<ChatLog.Entry> entries = ChatLog.recent(Math.max(1, restoreCount.getInt()));
		if (entries.isEmpty()) {
			return;
		}
		try {
			if (restoreHeader.get()) {
				client.gui.getChat().addMessage(Component.literal("§8§m          §r §7ChaosUtils restored chat §8§m          "));
			}
			for (int i = entries.size() - 1; i >= 0; i--) {
				ChatLog.Entry entry = entries.get(i);
				client.gui.getChat().addMessage(Component.literal("§8[old] ").append(entry.message()));
			}
		} catch (Throwable ignored) {
			// if the chat component is unavailable nothing is restored
		}
	}

	@Override
	public void onWorldLeave() {
		pendingDuplicateReport = 0;
		duplicateCount = 0;
		lastDuplicateText = "";
	}

	@Override
	public void onDisabled() {
		closeLog();
	}
}
