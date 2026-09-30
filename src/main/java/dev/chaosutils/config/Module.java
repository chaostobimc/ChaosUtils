package dev.chaosutils.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A feature definition. A module groups an on/off toggle with its settings and is
 * what the click GUI, the search index and the config file iterate over.
 */
public final class Module {
	private final String id;
	private final String name;
	private final String description;
	private final Category category;
	private final Setting.Toggle enabled;
	private final List<Setting<?>> settings = new ArrayList<>();

	public Module(String id, String name, String description, Category category, boolean defaultEnabled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.enabled = new Setting.Toggle(id + ".enabled", "Enabled", "Turns the feature on or off.", defaultEnabled);
		this.settings.add(this.enabled);
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public Category category() {
		return category;
	}

	public Setting.Toggle enabled() {
		return enabled;
	}

	public boolean isEnabled() {
		return enabled.get();
	}

	public List<Setting<?>> settings() {
		return Collections.unmodifiableList(settings);
	}

	public Module add(Setting<?> setting) {
		settings.add(setting);
		return this;
	}

	public Setting<?> setting(String settingId) {
		for (Setting<?> setting : settings) {
			if (setting.id.equals(settingId)) {
				return setting;
			}
		}
		return null;
	}

	public List<Setting<?>> visibleSettings() {
		return settings;
	}
}
