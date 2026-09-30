package dev.chaosutils.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.phys.Vec3;

/**
 * Rolling record of sounds the client actually plays.
 *
 * <p>Filled by the {@code SoundEngine} mixin (hook {@code sound.play}). Entries expire
 * after a few seconds, so the radar, the enhanced subtitles and the "which creeper is
 * hissing at me" use case all share one cheap, allocation-free list.
 */
public final class SoundTracker {
	public record Entry(String id, String path, Vec3 position, String category, float volume, float pitch, long timeMillis, boolean positional) {
		public float ageSeconds() {
			return (System.currentTimeMillis() - timeMillis) / 1000.0F;
		}
	}

	private static final long TTL_MILLIS = 6_000L;
	private static final int MAX_ENTRIES = 64;
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private SoundTracker() {
	}

	public static synchronized void push(String id, Vec3 position, String category, float volume, float pitch, boolean positional) {
		long now = System.currentTimeMillis();
		ENTRIES.add(new Entry(id, pathOf(id), position, category, volume, pitch, now, positional));
		prune(now);
	}

	private static String pathOf(String id) {
		int colon = id.indexOf(':');
		return colon >= 0 ? id.substring(colon + 1) : id;
	}

	public static synchronized List<Entry> recent(int limit, boolean withPositionOnly) {
		prune(System.currentTimeMillis());
		List<Entry> result = new ArrayList<>();
		for (int i = ENTRIES.size() - 1; i >= 0 && result.size() < limit; i--) {
			Entry entry = ENTRIES.get(i);
			if (withPositionOnly && !entry.positional()) {
				continue;
			}
			result.add(entry);
		}
		return result;
	}

	private static void prune(long now) {
		Iterator<Entry> iterator = ENTRIES.iterator();
		while (iterator.hasNext()) {
			if (now - iterator.next().timeMillis() > TTL_MILLIS) {
				iterator.remove();
			}
		}
		while (ENTRIES.size() > MAX_ENTRIES) {
			ENTRIES.remove(0);
		}
	}

	public static synchronized void clear() {
		ENTRIES.clear();
	}
}
