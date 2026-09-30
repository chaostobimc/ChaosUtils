package dev.chaosutils.core;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import net.minecraft.network.chat.Component;

/**
 * Bounded client side chat ring buffer.
 *
 * <p>Used for the chat history tool, the "restore chat after reconnect" option and the
 * mention search. The buffer is fixed size, so it can never grow without bound.
 */
public final class ChatLog {
	public enum Kind {
		PLAYER,
		SYSTEM,
		SERVER
	}

	public record Entry(Component message, String plain, Kind kind, boolean overlay, Instant time) {
	}

	private static final int MAX_ENTRIES = 400;
	private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

	private ChatLog() {
	}

	public static synchronized void add(Component message, Kind kind, boolean overlay) {
		if (message == null) {
			return;
		}
		String plain = message.getString();
		ENTRIES.addFirst(new Entry(message, plain, kind, overlay, Instant.now()));
		while (ENTRIES.size() > MAX_ENTRIES) {
			ENTRIES.removeLast();
		}
	}

	public static synchronized List<Entry> recent(int limit) {
		List<Entry> result = new ArrayList<>(Math.min(limit, ENTRIES.size()));
		int i = 0;
		for (Entry entry : ENTRIES) {
			if (i++ >= limit) {
				break;
			}
			result.add(entry);
		}
		return result;
	}

	public static synchronized void clear() {
		ENTRIES.clear();
	}

	/** Matches a query against the plain text of every buffered line. */
	public static synchronized List<Entry> search(String query, Kind filter, int limit) {
		String needle = query == null ? "" : query.toLowerCase();
		List<Entry> result = new ArrayList<>();
		for (Entry entry : ENTRIES) {
			if (filter != null && entry.kind() != filter) {
				continue;
			}
			if (!needle.isEmpty() && !entry.plain().toLowerCase().contains(needle)) {
				continue;
			}
			result.add(entry);
			if (result.size() >= limit) {
				break;
			}
		}
		return result;
	}
}
