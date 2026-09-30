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
 * ChaosUtils keeps running, the affected feature silently degrades and this class logs
 * exactly what is missing instead of crashing the game at startup. The handlers are also
 * deliberately parameterless where possible, because a handler that lists arguments has to
 * match the target signature exactly or Mixin refuses to apply the whole class.
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

	/**
	 * Logs which integrations have been observed so far.
	 *
	 * <p>A hook is only marked as seen once the vanilla method it targets has actually run, so this
	 * report is informative, not a verdict: hooks that only fire in game (camera, particles, entity
	 * removal, container screens, the FOV) legitimately show up as "not called yet" while the player
	 * is still in the main menu. Nothing is disabled because of a missing hook - a mixin that did not
	 * apply simply leaves the corresponding feature with nothing to react to.
	 */
	public static void report() {
		int pending = 0;
		StringBuilder summary = new StringBuilder();
		for (Map.Entry<String, String> entry : HOOKS.entrySet()) {
			boolean live = active(entry.getKey());
			if (!live) {
				pending++;
			}
			summary.append(System.lineSeparator())
					.append("  ")
					.append(live ? "[ ok ]  " : "[  ..  ]")
					.append(" ")
					.append(entry.getKey())
					.append(" -> ")
					.append(entry.getValue());
		}
		LOGGER.info("Integration hooks:{}{}", summary,
				pending == 0 ? "" : System.lineSeparator() + "  (" + pending
						+ " not called yet - that is normal for hooks that only fire in game)");
	}
}
