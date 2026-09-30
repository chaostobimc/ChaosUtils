package dev.chaosutils.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime self check for the Mixin based integrations.
 *
 * <p>Every injector that targets a method whose name is not guaranteed across Minecraft
 * versions is declared with {@code require = 0}: if a future patch release moves it,
 * ChaosUtils keeps running, the affected feature silently degrades and this class - which
 * is polled on the first few ticks - logs exactly what is missing instead of crashing the
 * game at startup.
 */
public final class ApiCompat {
	private static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils/Compat");
	private static final Map<String, String> HOOKS = new LinkedHashMap<>();
	private static final Map<String, Boolean> SEEN = new LinkedHashMap<>();

	private ApiCompat() {
	}

	/** Called from inside an injected method: proves that the hook really is live. */
	public static void seen(String hook) {
		SEEN.put(hook, Boolean.TRUE);
	}

	public static void register(String hook, String feature, String description) {
		HOOKS.put(hook, feature + " (" + description + ")");
	}

	public static boolean active(String hook) {
		return SEEN.containsKey(hook);
	}

	public static void report() {
		int missing = 0;
		StringBuilder summary = new StringBuilder();
		for (Map.Entry<String, String> entry : HOOKS.entrySet()) {
			boolean live = active(entry.getKey());
			if (!live) {
				missing++;
			}
			summary.append(System.lineSeparator())
					.append("  ")
					.append(live ? "[ ok ]   " : "[ MISS ] ")
					.append(entry.getKey())
					.append(" -> ")
					.append(entry.getValue());
		}
		LOGGER.info("Integration hooks:{}", summary);
		if (missing > 0) {
			LOGGER.warn("{} integration hook(s) did not apply on this Minecraft build. "
					+ "Affected features are disabled automatically; everything else keeps working.", missing);
		}
	}
}
