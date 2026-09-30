package dev.chaosutils.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Central registry of every feature module. Features add themselves during client
 * initialisation; the GUI, the config file and the search index all read from here.
 */
public final class ModuleManager {
	private static final Map<String, Module> BY_ID = new LinkedHashMap<>();
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {
	}

	public static Module register(Module module) {
		if (BY_ID.containsKey(module.id())) {
			throw new IllegalStateException("Duplicate ChaosUtils module id: " + module.id());
		}
		BY_ID.put(module.id(), module);
		MODULES.add(module);
		return module;
	}

	public static List<Module> modules() {
		return Collections.unmodifiableList(MODULES);
	}

	public static List<Module> byCategory(Category category) {
		List<Module> result = new ArrayList<>();
		for (Module module : MODULES) {
			if (module.category() == category) {
				result.add(module);
			}
		}
		return result;
	}

	@Nullable
	public static Module get(String id) {
		return BY_ID.get(id);
	}

	public static boolean enabled(String id) {
		Module module = BY_ID.get(id);
		return module != null && module.isEnabled();
	}

	/** Free text search across names, descriptions and setting labels. */
	public static List<Module> search(String query) {
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		if (needle.isEmpty()) {
			return modules();
		}
		List<Module> results = new ArrayList<>();
		for (Module module : MODULES) {
			if (matches(module, needle)) {
				results.add(module);
			}
		}
		return results;
	}

	private static boolean matches(Module module, String needle) {
		if (module.name().toLowerCase(Locale.ROOT).contains(needle)
				|| module.description().toLowerCase(Locale.ROOT).contains(needle)
				|| module.category().displayName().toLowerCase(Locale.ROOT).contains(needle)) {
			return true;
		}
		for (Setting<?> setting : module.settings()) {
			if (setting.label.toLowerCase(Locale.ROOT).contains(needle)) {
				return true;
			}
		}
		return false;
	}

	public static int countEnabled() {
		int count = 0;
		for (Module module : MODULES) {
			if (module.isEnabled()) {
				count++;
			}
		}
		return count;
	}
}
