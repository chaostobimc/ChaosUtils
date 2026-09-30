# ChaosUtils — complete source (Minecraft 1.21.11, Fabric)
Machine-generated single-file bundle of every source file in this repository, in a readable order. The canonical files live under `src/`; regenerate this document with `python3 tools/bundle_source.py` after changing code.

---

## Build & metadata

### `build.gradle`

```gradle
plugins {
	// Loom 1.14 split the plugin ids: "fabric-loom-remap" is the one for obfuscated
	// Minecraft versions (1.21.11 is the last obfuscated release).
	id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
	id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
	archivesName = project.archives_base_name
}

repositories {
	// Cloth Config etc. would go here - ChaosUtils intentionally ships no third
	// party runtime dependencies (see docs/API_NOTES.md).
}

dependencies {
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	// Official Mojang mappings: since 1.21.9 the ecosystem (Fabric API, NeoForge,
	// most client mods) has moved to them, and 1.21.11 renamed ResourceLocation
	// to Identifier which only the official mappings contain.
	mappings loom.officialMojangMappings()

	modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
	modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}

processResources {
	inputs.property "version", project.version
	filesMatching("fabric.mod.json") {
		expand "version": project.version
	}
}

tasks.withType(JavaCompile).configureEach {
	it.options.release = 21
	it.options.encoding = 'UTF-8'
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}" }
	}
}

publishing {
	publications {
		create("mavenJava", MavenPublication) {
			artifactId = project.archives_base_name
			from components.java
		}
	}
	repositories {
	}
}
```

### `settings.gradle`

```gradle
pluginManagement {
	repositories {
		maven {
			name = 'Fabric'
			url = 'https://maven.fabricmc.net/'
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = 'chaosutils'
```

### `gradle.properties`

```gradle
# Done to increase the memory available to gradle.
org.gradle.jvmargs=-Xmx3G
org.gradle.parallel=true

# Fabric Properties (https://fabricmc.net/develop/)
# 1.21.11 is the last obfuscated Minecraft release; Loom >= 1.14 is required for it.
minecraft_version=1.21.11
loader_version=0.19.5
loom_version=1.14-SNAPSHOT
fabric_version=0.141.6+1.21.11

# Mod Properties
mod_version=1.0.0
maven_group=dev.chaosutils
archives_base_name=chaosutils
```

### `gradle/wrapper/gradle-wrapper.properties`

```gradle
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### `LICENSE`

```java
MIT License

Copyright (c) 2026 ChaosUtils

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### `.gitignore`

```java
# Gradle
.gradle/
build/
out/

# Loom
remappedSrc/
run/

# IDE
.idea/
*.iml
*.ipr
*.iws
.vscode/
.settings/
.classpath
.project

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/
```

### `src/main/resources/fabric.mod.json`

```json
{
	"schemaVersion": 1,
	"id": "chaosutils",
	"version": "${version}",
	"name": "ChaosUtils",
	"description": "A large, fully client-side QoL and visual utility suite: an interactive radial menu, animated click GUI, HUD overlays, chat tools and audio helpers. No cheats, no automation, no packet spam.",
	"authors": [
		"ChaosUtils"
	],
	"contact": {
		"homepage": "https://github.com/chaostobimc/ChaosUtils",
		"sources": "https://github.com/chaostobimc/ChaosUtils",
		"issues": "https://github.com/chaostobimc/ChaosUtils/issues"
	},
	"license": "MIT",
	"icon": "assets/chaosutils/icon.png",
	"environment": "client",
	"entrypoints": {
		"client": [
			"dev.chaosutils.ChaosUtils"
		]
	},
	"mixins": [
		{
			"config": "chaosutils.mixins.json",
			"environment": "client"
		}
	],
	"depends": {
		"fabricloader": ">=0.19.5",
		"fabric-api": "*",
		"minecraft": "~1.21.11",
		"java": ">=21"
	},
	"suggests": {
		"modmenu": "*"
	}
}
```

### `src/main/resources/chaosutils.mixins.json`

```json
{
	"required": true,
	"minVersion": "0.8",
	"package": "dev.chaosutils.mixin",
	"compatibilityLevel": "JAVA_21",
	"injectors": {
		"defaultRequire": 1
	},
	"client": [
		"AbstractContainerScreenAccessor",
		"AbstractContainerScreenMixin",
		"CameraMixin",
		"DebugScreenOverlayMixin",
		"EntityMixin",
		"GameRendererMixin",
		"GuiMixin",
		"MouseHandlerMixin",
		"ParticleEngineMixin",
		"SoundEngineMixin"
	]
}
```

### `src/main/resources/assets/chaosutils/lang/en_us.json`

```json
{
	"key.chaosutils.open_gui": "Open ChaosUtils",
	"key.chaosutils.radial_menu": "Radial menu (hold)",
	"key.chaosutils.zoom": "Smooth zoom (hold)",
	"key.chaosutils.free_look": "Perspective lock (hold)",
	"key.chaosutils.search_container": "Container search",
	"key.chaosutils.copy_coordinates": "Copy coordinates",
	"key.chaosutils.chat_history": "Chat history",
	"key.chaosutils.screenshot_popup": "Screenshot manager",
	"key.chaosutils.toggle_gamma": "Night vision",
	"key.chaosutils.panic_toggle": "Hide all overlays",
	"key.chaosutils.add_waypoint": "Add waypoint",
	"key.categories.chaosutils": "ChaosUtils"
}
```

---

## Entrypoint

### `src/main/java/dev/chaosutils/ChaosUtils.java`

```java
package dev.chaosutils;

import java.util.EnumSet;
import java.util.Set;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TpsEstimator;
import dev.chaosutils.feature.FeatureRegistry;
import dev.chaosutils.feature.Features;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChaosUtils - a client side quality of life mod.
 *
 * <p>Everything in this mod is presentation only: overlays, markers, colours, sounds that only
 * you hear and shortcuts that do exactly what you could type yourself. There is deliberately
 * no automation, no packet of our own and no interaction with the server beyond the commands
 * and messages the player explicitly asks for.
 */
public final class ChaosUtils implements ClientModInitializer {
	public static final String MOD_ID = "chaosutils";
	public static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils");

	private static final Set<Category> HIDDEN_CATEGORIES = EnumSet.noneOf(Category.class);
	private static boolean overlaysHidden;
	private static boolean wasInWorld;

	@Override
	public void onInitializeClient() {
		LOGGER.info("ChaosUtils is starting (client side only)");
		ChaosConfig.load();
		Keybinds.init();
		registerApiHooks();
		FeatureRegistry.registerAll();
		Features.initHud();

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		Thread shutdownHook = new Thread(ChaosConfig::save, "ChaosUtils shutdown save");
		try {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Throwable ignored) {
			// sandboxed environments may refuse; the periodic autosave covers us
		}
		LOGGER.info("ChaosUtils ready: {} modules, {} integration hooks",
				dev.chaosutils.config.ModuleManager.modules().size(), 9);
	}

	private void onEndTick(Minecraft client) {
		boolean inWorld = client.level != null && client.player != null;
		if (inWorld != wasInWorld) {
			wasInWorld = inWorld;
			if (inWorld) {
				Features.onWorldJoin();
			} else {
				Features.onWorldLeave();
				TpsEstimator.reset();
			}
		}
		ChaosConfig.tick();
		Features.tick(client);
		if (Keybinds.openGui != null && Keybinds.openGui.wasPressed()) {
			client.setScreen(new dev.chaosutils.gui.ChaosClickGui());
		}
		if (Keybinds.panicToggle != null && Keybinds.panicToggle.wasPressed()) {
			toggleOverlays();
			if (client.player != null) {
				client.player.displayClientMessage(
						net.minecraft.network.chat.Component.literal(overlaysHidden
								? "§b[ChaosUtils] §fOverlays hidden" : "§b[ChaosUtils] §fOverlays visible"), true);
			}
		}
	}

	/** Declares the integration hooks so the self check can report exactly what applied. */
	private static void registerApiHooks() {
		ApiCompat.register("sound.play", "Sound radar & subtitles", "SoundEngine#play");
		ApiCompat.register("container.render", "Container search", "AbstractContainerScreen#render + layout accessor");
		ApiCompat.register("camera.setup", "Perspective lock", "Camera#setup");
		ApiCompat.register("debug.render", "Compact F3", "DebugScreenOverlay#render");
		ApiCompat.register("entity.remove", "Cache hygiene", "Entity#remove");
		ApiCompat.register("renderer.fov", "Smooth zoom", "GameRenderer#getFov");
		ApiCompat.register("particle.create", "Particle reducer", "ParticleEngine#createParticle");
		ApiCompat.register("gui.crosshair", "Crosshair designer", "Gui#renderCrosshair");
		ApiCompat.register("mouse.scroll", "Zoom wheel", "MouseHandler#onScroll");
	}

	// ------------------------------------------------------------------ overlays

	/** True when the panic toggle hid every overlay. */
	public static boolean overlaysHidden() {
		return overlaysHidden;
	}

	/**
	 * Whether overlays of a category are currently suppressed.
	 *
	 * <p>Quality of life features (the interface itself, keybinds, screenshots) are never
	 * hidden - a panic toggle must not lock you out of the mod.
	 */
	public static boolean overlaysHidden(Category category) {
		if (category == Category.QOL) {
			return false;
		}
		return overlaysHidden || HIDDEN_CATEGORIES.contains(category);
	}

	public static void toggleOverlays() {
		overlaysHidden = !overlaysHidden;
	}

	public static void toggleCategory(Category category) {
		if (!HIDDEN_CATEGORIES.remove(category)) {
			HIDDEN_CATEGORIES.add(category);
		}
	}

	public static boolean categoryHidden(Category category) {
		return HIDDEN_CATEGORIES.contains(category);
	}
}
```

---

## Config

### `src/main/java/dev/chaosutils/config/Category.java`

```java
package dev.chaosutils.config;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Sidebar groups of the click GUI. Colours are ARGB and are reused for accents. */
public enum Category {
	HUD("HUD & Overlays", 0xFF4FC3F7, () -> new ItemStack(Items.CLOCK)),
	VISUAL("Visuals", 0xFFB388FF, () -> new ItemStack(Items.ENDER_EYE)),
	RADIAL("Radial Menu", 0xFFFF8A65, () -> new ItemStack(Items.COMPASS)),
	CHAT("Chat & Social", 0xFF81C784, () -> new ItemStack(Items.PAPER)),
	INVENTORY("Inventory", 0xFFFFD54F, () -> new ItemStack(Items.CHEST)),
	AUDIO("Audio", 0xFFF06292, () -> new ItemStack(Items.NOTE_BLOCK)),
	QOL("Quality of Life", 0xFF64B5F6, () -> new ItemStack(Items.FEATHER)),
	PERFORMANCE("Performance", 0xFF4DB6AC, () -> new ItemStack(Items.REDSTONE));

	private final String displayName;
	private final int color;
	private final Supplier<ItemStack> icon;

	Category(String displayName, int color, Supplier<ItemStack> icon) {
		this.displayName = displayName;
		this.color = color;
		this.icon = icon;
	}

	public String displayName() {
		return displayName;
	}

	public int color() {
		return color;
	}

	public ItemStack icon() {
		return icon.get();
	}
}
```

### `src/main/java/dev/chaosutils/config/ChaosConfig.java`

```java
package dev.chaosutils.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.chaosutils.util.HudPos;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON config store.
 *
 * <p>Values are flatly keyed by {@code moduleId.settingId}, so renaming or moving a
 * feature in code never breaks an existing config file - unknown keys are simply
 * ignored on load and rewritten on the next save.
 */
public final class ChaosConfig {
	public static final int CONFIG_VERSION = 1;
	private static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils/Config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("chaosutils.json");
	private static final long AUTOSAVE_INTERVAL_MS = 5_000L;

	public static final List<RadialElement> RADIAL_ELEMENTS = new ArrayList<>();
	public static final List<Waypoint> WAYPOINTS = new ArrayList<>();

	private static boolean dirty;
	private static boolean loaded;
	private static long lastSave;

	private ChaosConfig() {
	}

	public static void markDirty() {
		dirty = true;
	}

	public static boolean isDirty() {
		return dirty;
	}

	/** Called once per client tick from the main tick hook; writes at most every 5 seconds. */
	public static void tick() {
		if (dirty && System.currentTimeMillis() - lastSave > AUTOSAVE_INTERVAL_MS) {
			save();
		}
	}

	public static synchronized void load() {
		loaded = true;
		createDefaults();
		if (!Files.exists(FILE)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) {
				return;
			}
			JsonObject root = parsed.getAsJsonObject();
			if (root.has("settings")) {
				JsonObject settings = root.getAsJsonObject("settings");
				for (Module module : ModuleManager.modules()) {
					for (Setting<?> setting : module.settings()) {
						JsonElement element = settings.get(setting.id);
						if (element != null && !element.isJsonNull()) {
							readSetting(setting, element);
						}
					}
				}
			}
			if (root.has("radial") && root.get("radial").isJsonArray()) {
				List<RadialElement> parsedElements = new ArrayList<>();
				for (JsonElement element : root.getAsJsonArray("radial")) {
					RadialElement radialElement = GSON.fromJson(element, RadialElement.class);
					if (radialElement != null) {
						parsedElements.add(radialElement);
					}
				}
				if (!parsedElements.isEmpty()) {
					RADIAL_ELEMENTS.clear();
					RADIAL_ELEMENTS.addAll(parsedElements);
				}
			}
			if (root.has("waypoints") && root.get("waypoints").isJsonArray()) {
				List<Waypoint> parsedWaypoints = new ArrayList<>();
				for (JsonElement element : root.getAsJsonArray("waypoints")) {
					Waypoint waypoint = GSON.fromJson(element, Waypoint.class);
					if (waypoint != null) {
						parsedWaypoints.add(waypoint);
					}
				}
				WAYPOINTS.clear();
				WAYPOINTS.addAll(parsedWaypoints);
			}
		} catch (Exception exception) {
			LOGGER.error("Could not read {} - falling back to defaults", FILE.getFileName(), exception);
		}
	}

	public static synchronized void save() {
		if (!loaded && !dirty) {
			return;
		}
		JsonObject root = new JsonObject();
		root.addProperty("version", CONFIG_VERSION);
		JsonObject settings = new JsonObject();
		for (Module module : ModuleManager.modules()) {
			for (Setting<?> setting : module.settings()) {
				JsonElement element = writeSetting(setting);
				if (element != null) {
					settings.add(setting.id, element);
				}
			}
		}
		root.add("settings", settings);
		root.add("radial", GSON.toJsonTree(RADIAL_ELEMENTS));
		root.add("waypoints", GSON.toJsonTree(WAYPOINTS));
		try {
			Files.createDirectories(FILE.getParent());
			Path temp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
			try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
				GSON.toJson(root, writer);
			}
			Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
			dirty = false;
			lastSave = System.currentTimeMillis();
		} catch (IOException exception) {
			LOGGER.error("Could not save {}", FILE.getFileName(), exception);
		}
	}

	private static void createDefaults() {
		if (RADIAL_ELEMENTS.isEmpty()) {
			RADIAL_ELEMENTS.add(new RadialElement("Spawn", "/spawn", 0xFF4FC3F7, "minecraft:respawn_anchor", RadialElement.ActionType.COMMAND));
			RADIAL_ELEMENTS.add(new RadialElement("Home", "/home", 0xFF81C784, "minecraft:red_bed", RadialElement.ActionType.COMMAND));
			RADIAL_ELEMENTS.add(new RadialElement("Back", "/back", 0xFFFFD54F, "minecraft:ender_pearl", RadialElement.ActionType.COMMAND));
			RADIAL_ELEMENTS.add(new RadialElement("Warp", "/warp", 0xFFB388FF, "minecraft:end_portal_frame", RadialElement.ActionType.COMMAND));
			RADIAL_ELEMENTS.add(new RadialElement("Tpa", "/tpa", 0xFFFF8A65, "minecraft:player_head", RadialElement.ActionType.COMMAND));
			RADIAL_ELEMENTS.add(new RadialElement("Coords", "Copy my coordinates", 0xFF4DB6AC, "minecraft:compass", RadialElement.ActionType.COPY_TEXT));
			RADIAL_ELEMENTS.add(new RadialElement("Settings", "Open ChaosUtils", 0xFFF06292, "minecraft:crafting_table", RadialElement.ActionType.OPEN_SETTINGS));
			RADIAL_ELEMENTS.add(new RadialElement("Panic", "Hide all overlays", 0xFFE57373, "minecraft:barrier", RadialElement.ActionType.TOGGLE_HUD));
		}
	}

	// ------------------------------------------------------------- value mapping

	private static JsonElement writeSetting(Setting<?> setting) {
		return switch (setting.kind()) {
			case TOGGLE -> new com.google.gson.JsonPrimitive(((Setting.Toggle) setting).get());
			case NUMBER -> new com.google.gson.JsonPrimitive(((Setting.Number) setting).get());
			case CHOICE -> new com.google.gson.JsonPrimitive(((Setting.Choice) setting).get());
			case COLOR, KEY -> new com.google.gson.JsonPrimitive((int) setting.value());
			case TEXT -> new com.google.gson.JsonPrimitive((String) setting.value());
			case POSITION -> {
				HudPos pos = ((Setting.Position) setting).get();
				JsonArray array = new JsonArray();
				array.add(pos.x());
				array.add(pos.y());
				yield array;
			}
			case ACTION -> null;
		};
	}

	@SuppressWarnings("unchecked")
	private static void readSetting(Setting<?> setting, JsonElement element) {
		try {
			switch (setting.kind()) {
				case TOGGLE -> ((Setting.Toggle) setting).set(element.getAsBoolean());
				case NUMBER -> ((Setting.Number) setting).set(element.getAsDouble());
				case CHOICE -> ((Setting.Choice) setting).set(element.getAsInt());
				case COLOR -> ((Setting.Color) setting).set(element.getAsInt());
				case KEY -> ((Setting.Key) setting).set(element.getAsInt());
				case TEXT -> ((Setting.Text) setting).set(element.getAsString());
				case POSITION -> {
					JsonArray array = element.getAsJsonArray();
					((Setting.Position) setting).set(new HudPos(array.get(0).getAsFloat(), array.get(1).getAsFloat()));
				}
				case ACTION -> {
				}
			}
		} catch (Exception exception) {
			LOGGER.warn("Ignoring malformed config entry {}", setting.id);
		}
	}
}
```

### `src/main/java/dev/chaosutils/config/Module.java`

```java
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
```

### `src/main/java/dev/chaosutils/config/ModuleManager.java`

```java
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
```

### `src/main/java/dev/chaosutils/config/RadialElement.java`

```java
package dev.chaosutils.config;

import dev.chaosutils.util.ItemLookup;
import net.minecraft.world.item.ItemStack;

/**
 * One slice of the radial menu.
 *
 * <p>Every action type is a deliberate, single, user triggered action - ChaosUtils never
 * repeats, schedules or automates anything.
 */
public final class RadialElement {
	public enum ActionType {
		/** Sends a command exactly as if the player typed it (server rules apply). */
		COMMAND("Send command"),
		/** Sends plain chat text. */
		CHAT_TEXT("Send chat"),
		/** Copies the text to the system clipboard (no network involved). */
		COPY_TEXT("Copy to clipboard"),
		/** Opens the ChaosUtils click GUI. */
		OPEN_SETTINGS("Open ChaosUtils"),
		/** Toggles every ChaosUtils overlay off/on (panic key). */
		TOGGLE_HUD("Toggle all overlays");

		private final String label;

		ActionType(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	public String name = "New Action";
	public String command = "/spawn";
	public int color = 0xFF7C5CFF;
	public String icon = "minecraft:compass";
	public boolean enabled = true;
	public ActionType type = ActionType.COMMAND;

	public RadialElement() {
	}

	public RadialElement(String name, String command, int color, String icon, ActionType type) {
		this.name = name;
		this.command = command;
		this.color = color;
		this.icon = icon;
		this.type = type;
	}

	public ItemStack iconStack() {
		return ItemLookup.stack(icon);
	}

	public RadialElement copy() {
		RadialElement copy = new RadialElement();
		copy.name = name;
		copy.command = command;
		copy.color = color;
		copy.icon = icon;
		copy.enabled = enabled;
		copy.type = type;
		return copy;
	}
}
```

### `src/main/java/dev/chaosutils/config/Setting.java`

```java
package dev.chaosutils.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.chaosutils.util.HudPos;

/**
 * A single, self describing configuration value.
 *
 * <p>Settings are intentionally generic: the click GUI renders them without knowing
 * anything about the feature that owns them, which keeps every feature's options
 * automatically exposed, searchable, serialisable and undo-able.
 *
 * @param <T> the value type
 */
public abstract class Setting<T> {
	public enum Kind {
		TOGGLE,
		NUMBER,
		CHOICE,
		TEXT,
		COLOR,
		KEY,
		POSITION,
		ACTION
	}

	public final String id;
	public final String label;
	public final String description;
	private final List<Consumer<T>> listeners = new ArrayList<>();

	protected Setting(String id, String label, String description) {
		this.id = id;
		this.label = label;
		this.description = description == null ? "" : description;
	}

	public abstract Kind kind();

	public abstract T value();

	/** Sets the value, marks the config dirty and notifies listeners when it actually changed. */
	public final void set(T newValue) {
		T old = value();
		if (old != null && old.equals(newValue)) {
			return;
		}
		if (old == null && newValue == null) {
			return;
		}
		write(newValue);
		ChaosConfig.markDirty();
		for (Consumer<T> listener : listeners) {
			listener.accept(newValue);
		}
	}

	protected abstract void write(T newValue);

	public Setting<T> onChanged(Consumer<T> listener) {
		listeners.add(listener);
		return this;
	}

	/** Short, human readable representation used by the GUI. */
	public abstract String display();

	public boolean isDefault() {
		return false;
	}

	public void reset() {
	}

	// ------------------------------------------------------------------ boolean

	public static final class Toggle extends Setting<Boolean> {
		private final boolean defaultValue;
		private boolean current;

		public Toggle(String id, String label, String description, boolean defaultValue) {
			super(id, label, description);
			this.defaultValue = defaultValue;
			this.current = defaultValue;
		}

		public boolean get() {
			return current;
		}

		public void toggle() {
			set(!current);
		}

		@Override
		public Kind kind() {
			return Kind.TOGGLE;
		}

		@Override
		public Boolean value() {
			return current;
		}

		@Override
		protected void write(Boolean newValue) {
			this.current = newValue;
		}

		@Override
		public String display() {
			return current ? "ON" : "OFF";
		}

		@Override
		public boolean isDefault() {
			return current == defaultValue;
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// ------------------------------------------------------------------- number

	public static final class Number extends Setting<Double> {
		private final double defaultValue;
		private final double min;
		private final double max;
		private final double step;
		private final String unit;
		private double current;

		public Number(String id, String label, String description, double defaultValue, double min, double max, double step) {
			this(id, label, description, defaultValue, min, max, step, "");
		}

		public Number(String id, String label, String description, double defaultValue, double min, double max, double step, String unit) {
			super(id, label, description);
			this.defaultValue = clamp(defaultValue);
			this.min = min;
			this.max = max;
			this.step = step <= 0 ? 0.01 : step;
			this.unit = unit == null ? "" : unit;
			this.current = this.defaultValue;
		}

		private double clamp(double v) {
			return Math.max(min, Math.min(max, v));
		}

		public double get() {
			return current;
		}

		public int getInt() {
			return (int) Math.round(current);
		}

		public float getFloat() {
			return (float) current;
		}

		/** Snaps a raw slider value to this setting's step granularity. */
		public void setRaw(double raw) {
			double snapped = Math.round((clamp(raw) - min) / step) * step + min;
			set(snapped);
		}

		public void nudge(int direction) {
			setRaw(current + direction * step);
		}

		public double fraction() {
			return (current - min) / (max - min);
		}

		public void setFraction(double fraction) {
			setRaw(min + Math.max(0, Math.min(1, fraction)) * (max - min));
		}

		public double min() {
			return min;
		}

		public double max() {
			return max;
		}

		public double step() {
			return step;
		}

		@Override
		public Kind kind() {
			return Kind.NUMBER;
		}

		@Override
		public Double value() {
			return current;
		}

		@Override
		protected void write(Double newValue) {
			this.current = clamp(newValue);
		}

		@Override
		public String display() {
			if (step >= 1.0) {
				return (int) current + unit;
			}
			return String.format(Locale.ROOT, "%.2f", current) + unit;
		}

		@Override
		public boolean isDefault() {
			return Math.abs(current - defaultValue) < 1.0E-6;
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// ------------------------------------------------------------------- choice

	public static final class Choice extends Setting<Integer> {
		private final String[] options;
		private final int defaultValue;
		private int current;

		public Choice(String id, String label, String description, int defaultValue, String... options) {
			super(id, label, description);
			this.options = options;
			this.defaultValue = Math.max(0, Math.min(options.length - 1, defaultValue));
			this.current = this.defaultValue;
		}

		public int get() {
			return current;
		}

		public String getOption() {
			return options[Math.max(0, Math.min(options.length - 1, current))];
		}

		public String[] options() {
			return options;
		}

		public void cycle(int direction) {
			int size = options.length;
			set(((current + direction) % size + size) % size);
		}

		@Override
		public Kind kind() {
			return Kind.CHOICE;
		}

		@Override
		public Integer value() {
			return current;
		}

		@Override
		protected void write(Integer newValue) {
			this.current = Math.max(0, Math.min(options.length - 1, newValue));
		}

		@Override
		public String display() {
			return getOption();
		}

		@Override
		public boolean isDefault() {
			return current == defaultValue;
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// --------------------------------------------------------------------- text

	public static final class Text extends Setting<String> {
		private final String defaultValue;
		private final int maxLength;
		private String current;

		public Text(String id, String label, String description, String defaultValue, int maxLength) {
			super(id, label, description);
			this.defaultValue = defaultValue == null ? "" : defaultValue;
			this.maxLength = maxLength;
			this.current = this.defaultValue;
		}

		public String get() {
			return current;
		}

		public int maxLength() {
			return maxLength;
		}

		@Override
		public Kind kind() {
			return Kind.TEXT;
		}

		@Override
		public String value() {
			return current;
		}

		@Override
		protected void write(String newValue) {
			String v = newValue == null ? "" : newValue;
			this.current = v.length() > maxLength ? v.substring(0, maxLength) : v;
		}

		@Override
		public String display() {
			return current.isEmpty() ? "(empty)" : current;
		}

		@Override
		public boolean isDefault() {
			return current.equals(defaultValue);
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// -------------------------------------------------------------------- color

	public static final class Color extends Setting<Integer> {
		private final int defaultValue;
		private final boolean alphaAllowed;
		private int current;

		public Color(String id, String label, String description, int defaultValue) {
			this(id, label, description, defaultValue, true);
		}

		public Color(String id, String label, String description, int defaultValue, boolean alphaAllowed) {
			super(id, label, description);
			this.defaultValue = defaultValue;
			this.alphaAllowed = alphaAllowed;
			this.current = defaultValue;
		}

		public int get() {
			return current;
		}

		public boolean alphaAllowed() {
			return alphaAllowed;
		}

		@Override
		public Kind kind() {
			return Kind.COLOR;
		}

		@Override
		public Integer value() {
			return current;
		}

		@Override
		protected void write(Integer newValue) {
			this.current = alphaAllowed ? newValue : (newValue & 0xFFFFFF) | 0xFF000000;
		}

		@Override
		public String display() {
			return String.format(Locale.ROOT, "#%08X", current);
		}

		@Override
		public boolean isDefault() {
			return current == defaultValue;
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// ---------------------------------------------------------------------- key

	public static final class Key extends Setting<Integer> {
		private final int defaultValue;
		private int current;
		private boolean listening;

		public Key(String id, String label, String description, int defaultValue) {
			super(id, label, description);
			this.defaultValue = defaultValue;
			this.current = defaultValue;
		}

		public int get() {
			return current;
		}

		public boolean isListening() {
			return listening;
		}

		public void listen() {
			this.listening = true;
		}

		public void stopListening() {
			this.listening = false;
		}

		@Override
		public Kind kind() {
			return Kind.KEY;
		}

		@Override
		public Integer value() {
			return current;
		}

		@Override
		protected void write(Integer newValue) {
			this.current = newValue;
			this.listening = false;
		}

		@Override
		public String display() {
			return dev.chaosutils.util.InputUtil.keyName(current);
		}

		@Override
		public boolean isDefault() {
			return current == defaultValue;
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// ----------------------------------------------------------------- position

	public static final class Position extends Setting<HudPos> {
		private final HudPos defaultValue;
		private HudPos current;

		public Position(String id, String label, String description, float x, float y) {
			super(id, label, description);
			this.defaultValue = new HudPos(x, y);
			this.current = this.defaultValue;
		}

		public HudPos get() {
			return current;
		}

		@Override
		public Kind kind() {
			return Kind.POSITION;
		}

		@Override
		public HudPos value() {
			return current;
		}

		@Override
		protected void write(HudPos newValue) {
			this.current = new HudPos(Math.max(0.0F, Math.min(1.0F, newValue.x())), Math.max(0.0F, Math.min(1.0F, newValue.y())));
		}

		@Override
		public String display() {
			return String.format(Locale.ROOT, "%.0f%% / %.0f%%", current.x() * 100.0F, current.y() * 100.0F);
		}

		@Override
		public boolean isDefault() {
			return current.equals(defaultValue);
		}

		@Override
		public void reset() {
			set(defaultValue);
		}
	}

	// ------------------------------------------------------------------- action

	/** A button-only entry: executes a callback when pressed in the GUI. */
	public static final class Action extends Setting<Void> {
		private final Runnable action;

		public Action(String id, String label, String description, Runnable action) {
			super(id, label, description);
			this.action = action;
		}

		public void run() {
			this.action.run();
		}

		@Override
		public Kind kind() {
			return Kind.ACTION;
		}

		@Override
		public Void value() {
			return null;
		}

		@Override
		protected void write(Void newValue) {
		}

		@Override
		public String display() {
			return "Run";
		}
	}
}
```

### `src/main/java/dev/chaosutils/config/Waypoint.java`

```java
package dev.chaosutils.config;

/** A purely client side marker stored in the config file (death markers, manual waypoints). */
public final class Waypoint {
	public String name = "Waypoint";
	public double x;
	public double y;
	public double z;
	public String dimension = "minecraft:overworld";
	public int color = 0xFF7C5CFF;
	public boolean temporary;
	/** Epoch millis when this waypoint should disappear, {@code 0} for "never". */
	public long expiresAt;

	public Waypoint() {
	}

	public Waypoint(String name, double x, double y, double z, String dimension, int color, boolean temporary, long lifetimeMillis) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension;
		this.color = color;
		this.temporary = temporary;
		this.expiresAt = lifetimeMillis <= 0 ? 0L : System.currentTimeMillis() + lifetimeMillis;
	}

	public boolean expired() {
		return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
	}
}
```

---

## Core

### `src/main/java/dev/chaosutils/core/ApiCompat.java`

```java
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
```

### `src/main/java/dev/chaosutils/core/ChatLog.java`

```java
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
```

### `src/main/java/dev/chaosutils/core/Clipboard.java`

```java
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
```

### `src/main/java/dev/chaosutils/core/Keybinds.java`

```java
package dev.chaosutils.core;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * All ChaosUtils hotkeys are real {@link KeyMapping}s, so players rebind them in the
 * vanilla Controls screen and ChaosUtils never steals a key silently.
 *
 * <p>Default keys intentionally avoid every vanilla binding (G was free, C, Y, ALT and
 * the rest are unbound in vanilla); the less common actions default to unbound so they
 * are opt-in.
 */
public final class Keybinds {
	private Keybinds() {
	}

	public static KeyMapping openGui;
	public static KeyMapping radialMenu;
	public static KeyMapping zoom;
	public static KeyMapping freeLook;
	public static KeyMapping searchContainer;
	public static KeyMapping copyCoordinates;
	public static KeyMapping chatHistory;
	public static KeyMapping screenshotPopup;
	public static KeyMapping toggleGamma;
	public static KeyMapping panicToggle;
	public static KeyMapping addWaypoint;

	public static void init() {
		openGui = register("open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT);
		radialMenu = register("radial_menu", GLFW.GLFW_KEY_G);
		zoom = register("zoom", GLFW.GLFW_KEY_C);
		freeLook = register("free_look", GLFW.GLFW_KEY_LEFT_ALT);
		searchContainer = register("search_container", GLFW.GLFW_KEY_Y);
		copyCoordinates = register("copy_coordinates", GLFW.GLFW_KEY_UNKNOWN);
		chatHistory = register("chat_history", GLFW.GLFW_KEY_UNKNOWN);
		screenshotPopup = register("screenshot_popup", GLFW.GLFW_KEY_UNKNOWN);
		toggleGamma = register("toggle_gamma", GLFW.GLFW_KEY_UNKNOWN);
		panicToggle = register("panic_toggle", GLFW.GLFW_KEY_UNKNOWN);
		addWaypoint = register("add_waypoint", GLFW.GLFW_KEY_UNKNOWN);
	}

	private static KeyMapping register(String name, int defaultKey) {
		KeyMapping mapping = new KeyMapping(
				"key.chaosutils." + name,
				InputConstants.Type.KEYSYM,
				defaultKey,
				KeyMapping.Category.MISC);
		return KeyBindingHelper.registerKeyBinding(mapping);
	}
}
```

### `src/main/java/dev/chaosutils/core/SoundTracker.java`

```java
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
```

### `src/main/java/dev/chaosutils/core/TickClock.java`

```java
package dev.chaosutils.core;

/**
 * Frame and tick timing without touching any of the render/tick counter types.
 *
 * <p>ChaosUtils only needs two numbers for its animations: how far between two game ticks
 * the current frame sits ({@code partialTick}) and how long the last frame took
 * ({@code frameDelta}). Both are derived from {@link System#nanoTime()} with the tick
 * boundary refreshed in {@code END_CLIENT_TICK}, which keeps overlays perfectly smooth at
 * any frame rate and stays independent of the mapping names that changed in 1.21.9.
 */
public final class TickClock {
	private static final double TICK_NANOS = 50_000_000.0;

	private static long lastTickNanos = System.nanoTime();
	private static long lastFrameNanos = System.nanoTime();
	private static float frameDelta = 0.016F;

	private TickClock() {
	}

	public static void onClientTick() {
		lastTickNanos = System.nanoTime();
	}

	/** Interpolation progress between the previous and the current tick, in [0, 1]. */
	public static float partialTick() {
		double elapsed = System.nanoTime() - lastTickNanos;
		float value = (float) (elapsed / TICK_NANOS);
		if (value < 0.0F) {
			return 0.0F;
		}
		return Math.min(1.0F, value);
	}

	/** Seconds since the previous frame, clamped so a hitch cannot blow up an animation. */
	public static float frameDelta() {
		long now = System.nanoTime();
		float delta = (float) ((now - lastFrameNanos) / 1_000_000_000.0);
		lastFrameNanos = now;
		if (delta < 0.0F) {
			delta = 0.0F;
		}
		return Math.min(0.1F, delta);
	}

	public static void reset() {
		lastTickNanos = System.nanoTime();
		lastFrameNanos = System.nanoTime();
		frameDelta = 0.016F;
	}
}
```

### `src/main/java/dev/chaosutils/core/TpsEstimator.java`

```java
package dev.chaosutils.core;

import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Estimates the server tick rate from the client's point of view.
 *
 * <p>Purely local measurement: the client counts how fast the world time it receives
 * advances compared to the wall clock. No packet is sent and nothing is queried.
 */
public final class TpsEstimator {
	private static final double SAMPLE_WINDOW_MS = 1_000.0;

	private static long lastSampleTime;
	private static long lastGameTime = Long.MIN_VALUE;
	private static double smoothed = 20.0;
	private static double lastTickMillis = 50.0;

	private TpsEstimator() {
	}

	public static void sample(Minecraft client) {
		ClientLevel level = client.level;
		if (level == null) {
			lastGameTime = Long.MIN_VALUE;
			return;
		}
		long now = System.currentTimeMillis();
		long gameTime = level.getGameTime();
		if (lastGameTime == Long.MIN_VALUE) {
			lastGameTime = gameTime;
			lastSampleTime = now;
			return;
		}
		long elapsed = now - lastSampleTime;
		if (elapsed < SAMPLE_WINDOW_MS) {
			return;
		}
		long advanced = gameTime - lastGameTime;
		double tps = advanced / (elapsed / 1000.0);
		tps = Math.max(0.0, Math.min(20.0, tps));
		smoothed = Anim.lerp(smoothed, tps, 0.25);
		lastTickMillis = Anim.lerp(lastTickMillis, elapsed / Math.max(1.0, advanced), 0.25);
		lastGameTime = gameTime;
		lastSampleTime = now;
	}

	public static double tps() {
		return smoothed;
	}

	/** Average milliseconds one server tick took over the last sample window. */
	public static double tickMillis() {
		return lastTickMillis;
	}

	public static int ping(Minecraft client) {
		if (client.player == null || client.getConnection() == null) {
			return 0;
		}
		try {
			var info = client.getConnection().getPlayerInfo(client.player.getUUID());
			return info == null ? 0 : info.getLatency();
		} catch (Throwable ignored) {
			return 0;
		}
	}

	public static void reset() {
		lastGameTime = Long.MIN_VALUE;
		smoothed = 20.0;
	}
}
```

---

## Utility

### `src/main/java/dev/chaosutils/util/Anim.java`

```java
package dev.chaosutils.util;

/**
 * Frame-rate independent animation helpers.
 *
 * <p>All smoothing is exponential ("lerp towards the target with a time constant") which
 * is stable at any frame rate and never overshoots, so the GUI feels the same at 60 and at
 * 300 FPS. Springs are available for the few places where a tiny overshoot sells the
 * animation (toggles, expanding cards).
 */
public final class Anim {
	private Anim() {
	}

	public static float lerp(float from, float to, float t) {
		return from + (to - from) * t;
	}

	public static double lerp(double from, double to, double t) {
		return from + (to - from) * t;
	}

	/** Exponential smoothing: {@code speed} is roughly "how much of the gap per second". */
	public static float approach(float current, float target, float speed, float deltaSeconds) {
		float t = 1.0F - (float) Math.exp(-Math.max(0.001F, speed) * Math.max(0.0F, deltaSeconds));
		return current + (target - current) * t;
	}

	public static float clamp01(float value) {
		return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static int clamp(int value, int min, int max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static float easeOutCubic(float t) {
		float inverted = 1.0F - clamp01(t);
		return 1.0F - inverted * inverted * inverted;
	}

	public static float easeInOutCubic(float t) {
		float x = clamp01(t);
		return x < 0.5F ? 4.0F * x * x * x : 1.0F - (float) Math.pow(-2.0F * x + 2.0F, 3) / 2.0F;
	}

	public static float easeOutBack(float t) {
		float x = clamp01(t);
		float c1 = 1.70158F;
		float c3 = c1 + 1.0F;
		return 1.0F + c3 * (float) Math.pow(x - 1.0, 3) + c1 * (float) Math.pow(x - 1.0, 2);
	}

	public static float easeOutExpo(float t) {
		float x = clamp01(t);
		return x >= 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, -10.0 * x);
	}

	/** A spring value that keeps a velocity so it can overshoot and settle. */
	public static final class Spring {
		private float value;
		private float velocity;
		private float target;
		private final float stiffness;
		private final float damping;

		public Spring(float initial, float stiffness, float damping) {
			this.value = initial;
			this.target = initial;
			this.stiffness = stiffness;
			this.damping = damping;
		}

		public void set(float target) {
			this.target = target;
		}

		public void snap(float value) {
			this.value = value;
			this.target = value;
			this.velocity = 0.0F;
		}

		public float get() {
			return value;
		}

		public boolean settled(float tolerance) {
			return Math.abs(target - value) < tolerance && Math.abs(velocity) < tolerance;
		}

		public float update(float deltaSeconds) {
			float dt = Math.min(0.05F, Math.max(0.0F, deltaSeconds));
			// Sub-step for stability at low frame rates.
			int steps = Math.max(1, (int) Math.ceil(dt / 0.016F));
			float step = dt / steps;
			for (int i = 0; i < steps; i++) {
				float accel = (target - value) * stiffness - velocity * damping;
				velocity += accel * step;
				value += velocity * step;
			}
			return value;
		}
	}

	/** Simple animated float with an exponential approach, used everywhere in the HUD. */
	public static final class Value {
		private float current;
		private float target;
		private final float speed;

		public Value(float initial, float speed) {
			this.current = initial;
			this.target = initial;
			this.speed = speed;
		}

		public void set(float target) {
			this.target = target;
		}

		public void snap(float value) {
			this.current = value;
			this.target = value;
		}

		public float target() {
			return target;
		}

		public float get() {
			return current;
		}

		public boolean isSettled(float tolerance) {
			return Math.abs(current - target) < tolerance;
		}

		public float update(float deltaSeconds) {
			current = approach(current, target, speed, deltaSeconds);
			return current;
		}
	}
}
```

### `src/main/java/dev/chaosutils/util/EnchantLookup.java`

```java
package dev.chaosutils.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Short names for enchantments ("Prot IV", "Sharp V").
 *
 * <p>Resolved from the enchantment's registry id, so it works for vanilla and for modded
 * enchantments (which fall back to an abbreviated id). The table is a plain, static map -
 * one lookup per enchantment per frame at most.
 */
public final class EnchantLookup {
	private static final Map<String, String> SHORT = new HashMap<>();

	static {
		SHORT.put("protection", "Prot");
		SHORT.put("fire_protection", "FireProt");
		SHORT.put("feather_falling", "Feather");
		SHORT.put("blast_protection", "BlastProt");
		SHORT.put("projectile_protection", "ProjProt");
		SHORT.put("respiration", "Resp");
		SHORT.put("aqua_affinity", "Aqua");
		SHORT.put("thorns", "Thorns");
		SHORT.put("depth_strider", "Depth");
		SHORT.put("frost_walker", "Frost");
		SHORT.put("binding_curse", "Bind");
		SHORT.put("soul_speed", "Soul");
		SHORT.put("swift_sneak", "Sneak");
		SHORT.put("sharpness", "Sharp");
		SHORT.put("smite", "Smite");
		SHORT.put("bane_of_arthropods", "Arthro");
		SHORT.put("knockback", "Knock");
		SHORT.put("fire_aspect", "Fire");
		SHORT.put("looting", "Loot");
		SHORT.put("sweeping_edge", "Sweep");
		SHORT.put("efficiency", "Eff");
		SHORT.put("silk_touch", "Silk");
		SHORT.put("unbreaking", "Unbr");
		SHORT.put("fortune", "Fort");
		SHORT.put("power", "Power");
		SHORT.put("punch", "Punch");
		SHORT.put("flame", "Flame");
		SHORT.put("infinity", "Inf");
		SHORT.put("luck_of_the_sea", "Luck");
		SHORT.put("lure", "Lure");
		SHORT.put("loyalty", "Loyal");
		SHORT.put("impaling", "Impale");
		SHORT.put("riptide", "Riptide");
		SHORT.put("channeling", "Channel");
		SHORT.put("multishot", "Multi");
		SHORT.put("quick_charge", "Charge");
		SHORT.put("piercing", "Pierce");
		SHORT.put("mending", "Mend");
		SHORT.put("vanishing_curse", "Vanish");
		SHORT.put("density", "Density");
		SHORT.put("breach", "Breach");
		SHORT.put("wind_burst", "Wind");
	}

	private EnchantLookup() {
	}

	public static String shortName(Holder<Enchantment> enchantment) {
		if (enchantment == null) {
			return "?";
		}
		try {
			String path = enchantment.unwrapKey().map(key -> key.location().getPath()).orElse(null);
			if (path == null) {
				return "?";
			}
			String known = SHORT.get(path.toLowerCase(Locale.ROOT));
			if (known != null) {
				return known;
			}
			return path.length() <= 6 ? capitalize(path) : capitalize(path.substring(0, 5));
		} catch (Throwable ignored) {
			return "?";
		}
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	/** Roman numerals for the first ten levels, digits above that. */
	public static String level(int level) {
		return switch (level) {
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			case 6 -> "VI";
			case 7 -> "VII";
			case 8 -> "VIII";
			case 9 -> "IX";
			case 10 -> "X";
			default -> Integer.toString(level);
		};
	}
}
```

### `src/main/java/dev/chaosutils/util/HudPos.java`

```java
package dev.chaosutils.util;

/**
 * Resolution independent HUD anchor.
 *
 * <p>{@code x} and {@code y} are fractions of the <em>available</em> space
 * ({@code screenSize - elementSize}), so {@code 0} pins the element to the left/top edge,
 * {@code 1} to the right/bottom edge and {@code 0.5} centers it. This keeps overlays in
 * place across different GUI scales and window sizes.
 *
 * @param x horizontal anchor, clamped to [0, 1]
 * @param y vertical anchor, clamped to [0, 1]
 */
public record HudPos(float x, float y) {
	public static final HudPos TOP_LEFT = new HudPos(0.02F, 0.02F);
	public static final HudPos TOP_RIGHT = new HudPos(0.98F, 0.02F);
	public static final HudPos BOTTOM_LEFT = new HudPos(0.02F, 0.98F);
	public static final HudPos BOTTOM_RIGHT = new HudPos(0.98F, 0.02F);
	public static final HudPos TOP_CENTER = new HudPos(0.5F, 0.02F);
	public static final HudPos CENTER = new HudPos(0.5F, 0.5F);

	public HudPos {
		x = clamp(x);
		y = clamp(y);
	}

	private static float clamp(float v) {
		return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
	}

	public int screenX(int screenWidth, int elementWidth) {
		return Math.round(x * Math.max(0, screenWidth - elementWidth));
	}

	public int screenY(int screenHeight, int elementHeight) {
		return Math.round(y * Math.max(0, screenHeight - elementHeight));
	}

	public HudPos offset(float dx, float dy) {
		return new HudPos(x + dx, y + dy);
	}

	public boolean isLeftHalf() {
		return x < 0.5F;
	}

	public boolean isTopHalf() {
		return y < 0.5F;
	}
}
```

### `src/main/java/dev/chaosutils/util/InputUtil.java`

```java
package dev.chaosutils.util;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

/**
 * Small helpers around GLFW key codes. Everything here is read-only input polling -
 * ChaosUtils never injects synthetic input events.
 */
public final class InputUtil {
	public static final int NO_KEY = -1;

	private InputUtil() {
	}

	public static String keyName(int code) {
		if (code == NO_KEY) {
			return "None";
		}
		if (code <= -100) {
			// Mouse buttons are stored as -100 - button so they can share the numeric field.
			int button = -100 - code;
			return switch (button) {
				case 0 -> "Mouse Left";
				case 1 -> "Mouse Right";
				case 2 -> "Mouse Middle";
				default -> "Mouse " + (button + 1);
			};
		}
		try {
			return InputConstants.Type.KEYSYM.getOrCreate(code).getDisplayName();
		} catch (Throwable ignored) {
			return "Key " + code;
		}
	}

	public static boolean isPressed(int code) {
		if (code == NO_KEY) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		long window = client.getWindow().getWindow();
		try {
			if (code <= -100) {
				int button = -100 - code;
				return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
			}
			return InputConstants.isKeyDown(window, code);
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static int mouseButtonToCode(int button) {
		return -100 - button;
	}

	/**
	 * First input that is currently held down, or {@link #NO_KEY}.
	 * Polling GLFW directly is what makes the "press a key to bind" widgets work without
	 * depending on the 1.21.9+ input event record accessors.
	 */
	public static int currentlyHeld() {
		long window = Minecraft.getInstance().getWindow().getWindow();
		try {
			for (int button = 0; button < 8; button++) {
				if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
					return mouseButtonToCode(button);
				}
			}
			for (int key = 32; key <= 348; key++) {
				if (org.lwjgl.glfw.GLFW.glfwGetKey(window, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
					return key;
				}
			}
		} catch (Throwable ignored) {
			return NO_KEY;
		}
		return NO_KEY;
	}

	/** Returns an input that is held now but was not reported as the previously held input. */
	public static int pollNewInput(int previouslyHeld) {
		int held = currentlyHeld();
		if (held == NO_KEY || held == previouslyHeld) {
			return NO_KEY;
		}
		return held;
	}
}
```

### `src/main/java/dev/chaosutils/util/ItemLookup.java`

```java
package dev.chaosutils.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Id based item lookup with a lazily built cache.
 *
 * <p>The cache is built by iterating the item registry once, which avoids relying on
 * registry lookup methods whose names changed several times during 1.21.x.
 */
public final class ItemLookup {
	private static final List<Item> ALL_ITEMS = new ArrayList<>();
	private static final Map<String, Item> BY_KEY = new LinkedHashMap<>();
	private static final Map<String, ItemStack> STACK_CACHE = new LinkedHashMap<>();
	private static boolean built;

	private ItemLookup() {
	}

	private static void build() {
		if (built) {
			return;
		}
		built = true;
		for (Item item : BuiltInRegistries.ITEM) {
			ALL_ITEMS.add(item);
			try {
				Identifier id = BuiltInRegistries.ITEM.getKey(item);
				BY_KEY.put(id.toString(), item);
				BY_KEY.put(id.getPath(), item);
			} catch (Throwable ignored) {
				// Registry not ready yet - the cache will simply be smaller.
			}
		}
	}

	public static ItemStack stack(String id) {
		if (id == null || id.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack cached = STACK_CACHE.get(id);
		if (cached != null) {
			return cached;
		}
		Item item = item(id);
		ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
		STACK_CACHE.put(id, stack);
		return stack;
	}

	public static Item item(String id) {
		build();
		String key = id.trim().toLowerCase(Locale.ROOT);
		Item item = BY_KEY.get(key);
		if (item != null) {
			return item;
		}
		return BY_KEY.get(key.startsWith("minecraft:") ? key : "minecraft:" + key);
	}

	public static String idOf(ItemStack stack) {
		if (stack.isEmpty()) {
			return "";
		}
		try {
			return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		} catch (Throwable ignored) {
			return "";
		}
	}

	/** Curated list used by the icon picker in the GUI so it stays usable. */
	public static List<Item> iconCandidates() {
		build();
		List<Item> curated = new ArrayList<>();
		String[] ids = {
				"minecraft:compass", "minecraft:clock", "minecraft:respawn_anchor", "minecraft:red_bed",
				"minecraft:ender_pearl", "minecraft:end_portal_frame", "minecraft:player_head", "minecraft:crafting_table",
				"minecraft:barrier", "minecraft:map", "minecraft:filled_map", "minecraft:spyglass",
				"minecraft:diamond_sword", "minecraft:netherite_sword", "minecraft:bow", "minecraft:crossbow",
				"minecraft:trident", "minecraft:shield", "minecraft:golden_apple", "minecraft:cake",
				"minecraft:potion", "minecraft:experience_bottle", "minecraft:emerald", "minecraft:diamond",
				"minecraft:netherite_ingot", "minecraft:gold_ingot", "minecraft:iron_ingot", "minecraft:redstone",
				"minecraft:torch", "minecraft:lantern", "minecraft:campfire", "minecraft:firework_rocket",
				"minecraft:elytra", "minecraft:leather_boots", "minecraft:chest", "minecraft:shulker_box",
				"minecraft:hopper", "minecraft:anvil", "minecraft:enchanting_table", "minecraft:beacon",
				"minecraft:bell", "minecraft:music_disc_cat", "minecraft:note_block", "minecraft:skull_banner_pattern",
				"minecraft:white_dye", "minecraft:lime_dye", "minecraft:cyan_dye", "minecraft:magenta_dye",
				"minecraft:heart_of_the_sea", "minecraft:nautilus_shell", "minecraft:totem_of_undying", "minecraft:nether_star"
		};
		for (String id : ids) {
			Item item = item(id);
			if (item != null && item != Items.AIR) {
				curated.add(item);
			}
		}
		return Collections.unmodifiableList(curated);
	}

	public static List<Item> allItems() {
		build();
		return Collections.unmodifiableList(ALL_ITEMS);
	}

	public static void invalidate() {
		built = false;
		ALL_ITEMS.clear();
		BY_KEY.clear();
		STACK_CACHE.clear();
	}
}
```

### `src/main/java/dev/chaosutils/util/Projection.java`

```java
package dev.chaosutils.util;

import dev.chaosutils.feature.Features;
import dev.chaosutils.feature.visual.PerspectiveLock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * World to screen projection for HUD overlays.
 *
 * <p>The camera basis is rebuilt once per frame from the camera position and the view
 * rotation, so projecting an arbitrary number of points afterwards is a handful of
 * multiplications per point - no matrix objects, no allocations per point and no
 * dependency on the render pipeline API that keeps changing between 1.21.x releases.
 */
public final class Projection {
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static double rightX;
	private static double rightY;
	private static double rightZ;
	private static double upX;
	private static double upY;
	private static double upZ;
	private static double forwardX;
	private static double forwardY;
	private static double forwardZ;
	private static double focalLength;
	private static float screenWidth;
	private static float screenHeight;
	private static float centerX;
	private static float centerY;
	private static boolean ready;

	private Projection() {
	}

	public record Point(float x, float y, float depth, float scale) {
		public boolean onScreen(float margin) {
			return x >= -margin && x <= screenWidth + margin && y >= -margin && y <= screenHeight + margin;
		}
	}

	/**
	 * Must be called once per frame before projecting, with the HUD's own scaled size so
	 * overlay positions match the coordinates {@link net.minecraft.client.gui.GuiGraphics}
	 * draws in.
	 */
	public static void setup(Minecraft client, float screenWidth, float screenHeight, float partialTick) {
		ready = false;
		if (client.player == null || client.level == null) {
			return;
		}
		Vec3 camera = null;
		try {
			camera = client.gameRenderer.getMainCamera().getPosition();
		} catch (Throwable ignored) {
			camera = null;
		}
		if (camera == null) {
			camera = client.player.getEyePosition(partialTick);
		}
		float yaw = PerspectiveLock.cameraYaw(client.player, partialTick);
		float pitch = PerspectiveLock.cameraPitch(client.player, partialTick);

		cameraX = camera.x;
		cameraY = camera.y;
		cameraZ = camera.z;

		double yawRad = Math.toRadians(yaw);
		double pitchRad = Math.toRadians(pitch);
		double cosPitch = Math.cos(pitchRad);
		double sinPitch = Math.sin(pitchRad);
		double sinYaw = Math.sin(yawRad);
		double cosYaw = Math.cos(yawRad);

		forwardX = -sinYaw * cosPitch;
		forwardY = -sinPitch;
		forwardZ = cosYaw * cosPitch;

		rightX = -cosYaw;
		rightY = 0.0;
		rightZ = -sinYaw;

		upX = rightY * forwardZ - rightZ * forwardY;
		upY = rightZ * forwardX - rightX * forwardZ;
		upZ = rightX * forwardY - rightY * forwardX;
		double upLength = Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		if (upLength > 1.0E-6) {
			upX /= upLength;
			upY /= upLength;
			upZ /= upLength;
		}

		double fov = configuredFov(client);
		focalLength = 1.0 / Math.tan(Math.toRadians(fov) * 0.5);
		Projection.screenWidth = Math.max(1.0F, screenWidth);
		Projection.screenHeight = Math.max(1.0F, screenHeight);
		centerX = Projection.screenWidth * 0.5F;
		centerY = Projection.screenHeight * 0.5F;
		ready = true;
	}

	private static double configuredFov(Minecraft client) {
		double fov = 70.0;
		try {
			Object value = client.options.fov().get();
			if (value instanceof Number number) {
				fov = number.doubleValue();
			}
		} catch (Throwable ignored) {
			// keep the default
		}
		fov *= Features.zoomFactor();
		return Math.max(5.0, Math.min(170.0, fov));
	}

	public static boolean isReady() {
		return ready;
	}

	public static float screenWidth() {
		return screenWidth;
	}

	public static float screenHeight() {
		return screenHeight;
	}

	public static double cameraX() {
		return cameraX;
	}

	public static double cameraY() {
		return cameraY;
	}

	public static double cameraZ() {
		return cameraZ;
	}

	public static double distanceTo(double x, double y, double z) {
		double dx = x - cameraX;
		double dy = y - cameraY;
		double dz = z - cameraZ;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Projects a world position into HUD coordinates; {@code depth} is the camera-space Z. */
	public static Point project(double worldX, double worldY, double worldZ) {
		if (!ready) {
			return new Point(Float.NaN, Float.NaN, -1.0F, 0.0F);
		}
		double dx = worldX - cameraX;
		double dy = worldY - cameraY;
		double dz = worldZ - cameraZ;

		double depth = dx * forwardX + dy * forwardY + dz * forwardZ;
		if (depth <= 0.05) {
			return new Point(Float.NaN, Float.NaN, (float) depth, 0.0F);
		}
		double rightAmount = dx * rightX + dy * rightY + dz * rightZ;
		double upAmount = dx * upX + dy * upY + dz * upZ;

		double aspect = screenWidth / screenHeight;
		double ndcX = (rightAmount / depth) * focalLength / aspect;
		double ndcY = (upAmount / depth) * focalLength;

		float screenX = (float) ((ndcX * 0.5 + 0.5) * screenWidth);
		float screenY = (float) ((0.5 - ndcY * 0.5) * screenHeight);
		float scale = (float) (focalLength / Math.max(0.5, depth) * screenHeight * 0.5);
		return new Point(screenX, screenY, (float) depth, scale);
	}

	/** Bearing in degrees relative to the camera view, negative is left. */
	public static float bearingTo(double x, double z) {
		double dx = x - cameraX;
		double dz = z - cameraZ;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-4) {
			return 0.0F;
		}
		double angle = Math.toDegrees(Math.atan2(dx, dz));
		double forwardAngle = Math.toDegrees(Math.atan2(forwardX, forwardZ));
		double relative = angle - forwardAngle;
		while (relative <= -180.0) {
			relative += 360.0;
		}
		while (relative > 180.0) {
			relative -= 360.0;
		}
		return (float) relative;
	}

	public static double forwardX() {
		return forwardX;
	}

	public static double forwardZ() {
		return forwardZ;
	}
}
```

### `src/main/java/dev/chaosutils/util/Render.java`

```java
package dev.chaosutils.util;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing helpers on top of {@link GuiGraphics}.
 *
 * <p>Only the long-stable primitive ({@code fill}, scissor and string drawing) is used:
 * rounded corners and gradients are composed from a handful of rectangles instead of
 * shaders or blit calls, which keeps ChaosUtils compatible across 1.21.x render changes
 * and costs nothing measurable (a rounded rect is ~20 quads).
 */
public final class Render {
	private Render() {
	}

	// ------------------------------------------------------------------ colors

	public static int rgba(int r, int g, int b, int a) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	public static int alpha(int color, float alpha) {
		int a = Math.round(Anim.clamp01(alpha) * ((color >>> 24) & 0xFF));
		return (color & 0xFFFFFF) | a << 24;
	}

	public static int scaleAlpha(int color, float factor) {
		int a = Math.round(Anim.clamp(((color >>> 24) & 0xFF) * factor, 0.0F, 255.0F));
		return (color & 0xFFFFFF) | a << 24;
	}

	public static int mix(int from, int to, float t) {
		float x = Anim.clamp01(t);
		int a = Math.round(Anim.lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, x));
		int r = Math.round(Anim.lerp((from >> 16) & 0xFF, (to >> 16) & 0xFF, x));
		int g = Math.round(Anim.lerp((from >> 8) & 0xFF, (to >> 8) & 0xFF, x));
		int b = Math.round(Anim.lerp(from & 0xFF, to & 0xFF, x));
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static int brighten(int color, float amount) {
		return mix(color, 0xFFFFFFFF, amount);
	}

	public static int darken(int color, float amount) {
		return mix(color, 0xFF000000, amount);
	}

	public static int fromHsv(float hue, float saturation, float value) {
		return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, saturation, value) & 0xFFFFFF;
	}

	public static int withHue(int color, float hue) {
		return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.55F, 0.95F) & 0xFFFFFF;
	}

	public static float[] toHsv(int color) {
		float[] hsv = new float[3];
		java.awt.Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
		return hsv;
	}

	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	public static int blue(int color) {
		return color & 0xFF;
	}

	public static int alphaOf(int color) {
		return (color >>> 24) & 0xFF;
	}

	// ---------------------------------------------------------------- geometry

	public static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + width);
		int y1 = Math.round(y + height);
		graphics.fill(x0, y0, x1, y1, color);
	}

	/** Rounded rectangle built from scanlines; {@code radius} is clamped to half the box. */
	public static void roundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		if (r < 1.0F) {
			rect(graphics, x, y, width, height, color);
			return;
		}
		int steps = Math.max(2, Math.round(r));
		float stepHeight = r / steps;
		// Top corners
		for (int i = 0; i < steps; i++) {
			float dy = r - (i + 0.5F) * stepHeight;
			float inset = r - (float) Math.sqrt(Math.max(0.0, r * r - dy * dy));
			float rowY = y + i * stepHeight;
			rect(graphics, x + inset, rowY, width - inset * 2.0F, stepHeight + 0.5F, color);
		}
		int bodyTop = Math.round(y + r);
		int bodyBottom = Math.round(y + height - r);
		if (bodyBottom > bodyTop) {
			rect(graphics, x, bodyTop, width, bodyBottom - bodyTop, color);
		}
		// Bottom corners
		for (int i = 0; i < steps; i++) {
			float dy = r - (i + 0.5F) * stepHeight;
			float inset = r - (float) Math.sqrt(Math.max(0.0, r * r - dy * dy));
			float rowY = y + height - r + i * stepHeight;
			rect(graphics, x + inset, rowY, width - inset * 2.0F, stepHeight + 0.5F, color);
		}
	}

	/**
	 * Straight line drawn as small squares along the segment.
	 *
	 * <p>Used for rotated HUD shapes (off screen arrows, compass needles) without touching
	 * the render pipeline: one {@code fill} per step, and the step count is bounded.
	 */
	public static void line(GuiGraphics graphics, float x0, float y0, float x1, float y1, float thickness, int color) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if (length < 0.01F || alphaOf(color) == 0) {
			return;
		}
		int steps = Math.max(1, Math.min(48, Math.round(length / Math.max(1.0F, thickness * 0.5F))));
		float half = Math.max(0.5F, thickness * 0.5F);
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			rect(graphics, x0 + dx * t - half, y0 + dy * t - half, thickness, thickness, color);
		}
	}

	/**
	 * Filled ring segment (annulus sector), drawn as a fan of short radial bars.
	 *
	 * <p>This is what the radial menu is built from: the number of bars is derived from the
	 * angle so a 45° slice costs roughly twenty quads and the result looks smooth, while the
	 * whole menu stays a handful of draw calls.
	 */
	public static void arc(GuiGraphics graphics, float centerX, float centerY, float innerRadius, float outerRadius,
			float startDegrees, float endDegrees, int color) {
		if (outerRadius <= innerRadius || alphaOf(color) == 0) {
			return;
		}
		float span = endDegrees - startDegrees;
		if (Math.abs(span) < 0.05F) {
			return;
		}
		int steps = Math.max(2, Math.min(64, Math.round(Math.abs(span) / 3.0F)));
		float stepSpan = span / steps;
		float midRadius = (innerRadius + outerRadius) * 0.5F;
		float thickness = Math.max(2.0F, (float) (Math.abs(Math.toRadians(stepSpan)) * midRadius));
		for (int i = 0; i < steps; i++) {
			double angle = Math.toRadians(startDegrees + (i + 0.5F) * stepSpan);
			float sin = (float) Math.sin(angle);
			float cos = (float) Math.cos(angle);
			line(graphics, centerX + sin * innerRadius, centerY - cos * innerRadius,
					centerX + sin * outerRadius, centerY - cos * outerRadius, thickness, color);
		}
	}

	/** Border drawn by filling the outline colour and punching the background back in. */
	public static void roundedBorder(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float thickness, int borderColor, int backgroundColor) {
		roundedRect(graphics, x, y, width, height, radius, borderColor);
		roundedRect(graphics, x + thickness, y + thickness, width - thickness * 2.0F, height - thickness * 2.0F,
				Math.max(0.0F, radius - thickness), backgroundColor);
	}

	/** Vertical gradient drawn as a small number of strips (no shader, no allocations). */
	public static void verticalGradient(GuiGraphics graphics, float x, float y, float width, float height, int top, int bottom) {
		int strips = Math.max(2, Math.min(32, Math.round(height / 2.0F)));
		float stripHeight = height / strips;
		for (int i = 0; i < strips; i++) {
			int color = mix(top, bottom, (i + 0.5F) / strips);
			rect(graphics, x, y + i * stripHeight, width, stripHeight + 0.5F, color);
		}
	}

	public static void horizontalGradient(GuiGraphics graphics, float x, float y, float width, float height, int left, int right) {
		int strips = Math.max(2, Math.min(32, Math.round(width / 2.0F)));
		float stripWidth = width / strips;
		for (int i = 0; i < strips; i++) {
			int color = mix(left, right, (i + 0.5F) / strips);
			rect(graphics, x + i * stripWidth, y, stripWidth + 0.5F, height, color);
		}
	}

	public static void shadowedPanel(GuiGraphics graphics, float x, float y, float width, float height, float radius, int background, int outline) {
		roundedRect(graphics, x + 1.5F, y + 2.5F, width, height, radius, 0x40000000);
		if (alphaOf(outline) != 0) {
			roundedBorder(graphics, x, y, width, height, radius, 1.0F, outline, background);
		} else {
			roundedRect(graphics, x, y, width, height, radius, background);
		}
	}

	public static void scissor(GuiGraphics graphics, float x, float y, float width, float height) {
		graphics.enableScissor(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height));
	}

	public static void unscissor(GuiGraphics graphics) {
		graphics.disableScissor();
	}

	// ------------------------------------------------------------------ text

	public static void text(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float x, float y, int color, boolean shadow) {
		graphics.drawString(font, value, Math.round(x), Math.round(y), color, shadow);
	}

	public static void text(GuiGraphics graphics, net.minecraft.client.gui.Font font, net.minecraft.network.chat.Component value, float x, float y, int color, boolean shadow) {
		graphics.drawString(font, value, Math.round(x), Math.round(y), color, shadow);
	}

	public static void centeredText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float centerX, float y, int color, boolean shadow) {
		graphics.drawCenteredString(font, value, Math.round(centerX), Math.round(y), color);
	}

	public static int textWidth(net.minecraft.client.gui.Font font, String value) {
		return font.width(value);
	}

	public static void item(GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, float x, float y) {
		if (!stack.isEmpty()) {
			graphics.renderItem(stack, Math.round(x), Math.round(y));
		}
	}

	public static void itemDecorations(GuiGraphics graphics, net.minecraft.client.gui.Font font, net.minecraft.world.item.ItemStack stack, float x, float y) {
		if (!stack.isEmpty()) {
			graphics.renderItemDecorations(font, stack, Math.round(x), Math.round(y));
		}
	}
}
```

### `src/main/java/dev/chaosutils/util/SoundClasses.java`

```java
package dev.chaosutils.util;

import java.util.Locale;

/**
 * Classifies a sound id into a small, meaningful group.
 *
 * <p>Used by the sound radar and the enhanced subtitles so both colour and filter sounds the
 * same way. Classification is a handful of {@code contains} checks on the sound path - no
 * registry lookups, no allocations.
 */
public final class SoundClasses {
	public enum Kind {
		HOSTILE("Hostile", 0xFFE05B5B),
		NEUTRAL("Neutral", 0xFF63D471),
		PLAYER("Player", 0xFF64B5F6),
		BLOCK("Block", 0xFFF0B429),
		AMBIENT("Ambient", 0xFF9E9EB3),
		WEATHER("Weather", 0xFF7FC8F8),
		MUSIC("Music", 0xFFB388FF),
		OTHER("Other", 0xFFBFC2CF);

		private final String label;
		private final int color;

		Kind(String label, int color) {
			this.label = label;
			this.color = color;
		}

		public String label() {
			return label;
		}

		public int color() {
			return color;
		}
	}

	private static final String[] HOSTILE = {"zombie", "skeleton", "creeper", "spider", "enderman", "witch", "blaze",
			"ghast", "slime", "magma", "silverfish", "cave", "wither", "dragon", "warden", "piglin", "hoglin", "vex",
			"phantom", "shulker", "guardian", "ravager", "pillager", "vindicator", "evoker", "illusioner", "zoglin",
			"stray", "husk", "drowned", "breeze", "bogged"};
	private static final String[] NEUTRAL = {"cow", "pig", "sheep", "chicken", "horse", "donkey", "mule", "llama",
			"wolf", "cat", "ocelot", "parrot", "fox", "rabbit", "bee", "goat", "frog", "turtle", "panda", "polar_bear",
			"axolotl", "dolphin", "squid", "cod", "salmon", "tropical_fish", "bat", "camel", "sniffer", "armadillo",
			"allay", "villager", "wandering_trader", "golem", "snow_golem"};
	private static final String[] PLAYER = {"player", "step.", "hurt", "death", "swing", "attack", "fall.", "burp",
			"drink", "eat", "totem", "levelup", "orb", "xp", "shield", "trident", "bow", "crossbow", "arrow", "hit",
			"armor", "equip", "swap", "pickup", "break", "place"};
	private static final String[] BLOCK = {"block.", "block_", "chest", "door", "lever", "button", "pressure_plate",
			"piston", "dispenser", "note_block", "jukebox", "anvil", "grindstone", "smithing", "beacon", "conduit",
			"portal", "furnace", "campfire", "bell", "trapdoor", "fence_gate", "tripwire", "respawn_anchor", "amethyst",
			"sculk_", "bubble_column", "fire.", "extinguish", "fizz"};
	private static final String[] AMBIENT = {"ambient", "cave", "leaves", "grass", "sand", "gravel", "snow", "wind",
			"firefly", "chime", "harp", "bell", "bass", "snare", "click", "hat", "basedrum", "water.", "lava.",
			"swim", "splash", "fishing"};
	private static final String[] WEATHER = {"weather", "rain", "thunder", "lightning"};
	private static final String[] MUSIC = {"music", "record", "disc"};

	private SoundClasses() {
	}

	public static Kind classify(String soundPath) {
		if (soundPath == null || soundPath.isEmpty()) {
			return Kind.OTHER;
		}
		String path = soundPath.toLowerCase(Locale.ROOT);
		if (containsAny(path, MUSIC)) {
			return Kind.MUSIC;
		}
		if (containsAny(path, WEATHER)) {
			return Kind.WEATHER;
		}
		if (containsAny(path, HOSTILE)) {
			return Kind.HOSTILE;
		}
		if (containsAny(path, NEUTRAL)) {
			return Kind.NEUTRAL;
		}
		if (containsAny(path, BLOCK)) {
			return Kind.BLOCK;
		}
		if (containsAny(path, AMBIENT)) {
			return Kind.AMBIENT;
		}
		if (containsAny(path, PLAYER)) {
			return Kind.PLAYER;
		}
		return Kind.OTHER;
	}

	private static boolean containsAny(String path, String[] needles) {
		for (String needle : needles) {
			if (path.contains(needle)) {
				return true;
			}
		}
		return false;
	}

	/** Compact symbol used in front of a subtitle line. */
	public static String symbol(Kind kind) {
		return switch (kind) {
			case HOSTILE -> "!";
			case NEUTRAL -> "~";
			case PLAYER -> "@";
			case BLOCK -> "#";
			case AMBIENT -> ".";
			case WEATHER -> "%";
			case MUSIC -> "&";
			case OTHER -> "-";
		};
	}

	/** Turns "entity.zombie.ambient" into "Zombie Ambient" for subtitle lines. */
	public static String prettyName(String soundPath) {
		if (soundPath == null || soundPath.isEmpty()) {
			return "Sound";
		}
		String path = soundPath;
		int dot = path.lastIndexOf('.');
		String tail = dot >= 0 ? path.substring(dot + 1) : path;
		String[] parts = tail.split("_");
		StringBuilder builder = new StringBuilder(tail.length());
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.length() == 0 ? "Sound" : builder.toString();
	}
}
```

### `src/main/java/dev/chaosutils/util/SoundLookup.java`

```java
package dev.chaosutils.util;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Registry based sound lookup plus a cached UI click sound.
 *
 * <p>Resolving the sound event through the registry (instead of referencing the
 * {@code SoundEvents} constant directly) keeps ChaosUtils working regardless of whether
 * vanilla exposes that field as a {@code SoundEvent} or a {@code Holder}.
 */
public final class SoundLookup {
	private static final Map<String, SoundEvent> CACHE = new LinkedHashMap<>();
	private static boolean built;
	private static SoundEvent uiClick;

	private SoundLookup() {
	}

	private static void build() {
		if (built) {
			return;
		}
		built = true;
		try {
			for (SoundEvent event : BuiltInRegistries.SOUND_EVENT) {
				Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(event);
				CACHE.put(id.toString(), event);
				CACHE.put(id.getPath(), event);
			}
		} catch (Throwable ignored) {
			// Registry not available - sounds are simply skipped.
		}
	}

	public static SoundEvent get(String id) {
		build();
		return CACHE.get(id);
	}

	public static SoundEvent uiClick() {
		if (uiClick == null) {
			uiClick = get("minecraft:ui.button.click");
		}
		return uiClick;
	}

	/** Non-positional UI sound, client only, no packet involved. */
	public static SoundInstance ui(SoundEvent event, float pitch, float volume) {
		if (event == null) {
			return null;
		}
		SimpleSoundInstance instance = SimpleSoundInstance.forUI(event, pitch);
		return instance;
	}

	public static void invalidate() {
		built = false;
		CACHE.clear();
		uiClick = null;
	}
}
```

---

## Feature framework

### `src/main/java/dev/chaosutils/feature/Feature.java`

```java
package dev.chaosutils.feature;

import java.util.HashSet;
import java.util.Set;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Base contract for every ChaosUtils feature.
 *
 * <p>Features are stateless singletons: they own a {@link Module} (which the GUI and the
 * config file see) and receive a client tick plus, optionally, a HUD render callback.
 * They must never hold on to world objects between ticks - nothing here may leak.
 */
public interface Feature {
	/** Where a feature draws. World overlays sit under the chat, panels above everything. */
	enum Layer {
		WORLD,
		PANEL
	}

	Module module();

	default boolean isEnabled() {
		return !Enablement.isDisabled(this) && module().isEnabled() && !ChaosUtils.overlaysHidden(module().category());
	}

	default Layer layer() {
		return Layer.PANEL;
	}

	/** Whether this feature wants {@link #onHudRender} called every frame. */
	default boolean supportsHud() {
		return false;
	}

	/** Called once per client tick while the game is not paused. */
	default void onTick(Minecraft client) {
	}

	/** Called every frame; {@code partialTick} is the interpolated tick progress. */
	default void onHudRender(GuiGraphics graphics, float partialTick) {
	}

	/** Called when the player toggles the feature off, so it can clean up transient state. */
	default void onDisabled() {
	}

	/** Called when the client joins a world, including when a dimension change reloads it. */
	default void onWorldJoin() {
	}

	/** Called when the client leaves a world or disconnects. Must drop every world reference. */
	default void onWorldLeave() {
	}

	/**
	 * Disables a feature that threw an exception. A broken overlay stays off for the
	 * session instead of spamming the log or the frame budget; the GUI can re-enable it.
	 */
	default void disableForSession() {
		Enablement.disable(this);
	}

	/** Session scoped "this feature is broken" bookkeeping. */
	final class Enablement {
		private static final Set<String> DISABLED = new HashSet<>();

		private Enablement() {
		}

		static void disable(Feature feature) {
			if (DISABLED.add(feature.module().id())) {
				ChaosUtils.LOGGER.warn("Disabling {} for this session after a failure", feature.module().id());
				feature.onDisabled();
			}
		}

		public static boolean isDisabled(Feature feature) {
			return DISABLED.contains(feature.module().id());
		}

		public static void clear() {
			DISABLED.clear();
		}
	}
}
```

### `src/main/java/dev/chaosutils/feature/Features.java`

```java
package dev.chaosutils.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.util.Projection;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/**
 * The feature spine.
 *
 * <p>Every ChaosUtils feature registers here exactly once. The registry owns
 * <ul>
 *   <li>the per-tick and per-frame dispatch,</li>
 *   <li>the HUD element registration (Fabric's HUD API, so vanilla ordering and the
 *       "hide HUD" key keep working),</li>
 *   <li>the per-entity cache pruning that the {@code Entity} mixin drives,</li>
 *   <li>the tiny amount of state mixins need before the features are initialised.</li>
 * </ul>
 */
public final class Features {
	private static final List<Feature> FEATURES = new ArrayList<>();
	private static final List<IntConsumer> ENTITY_PRUNE_LISTENERS = new ArrayList<>();
	private static final List<Feature> HUD_FEATURES = new ArrayList<>();

	private static volatile float zoomFactor = 1.0F;
	private static volatile boolean worldReady;
	private static int reportDelay = 40;

	private Features() {
	}

	public static void register(Feature feature) {
		FEATURES.add(feature);
		if (feature.supportsHud()) {
			HUD_FEATURES.add(feature);
		}
	}

	public static List<Feature> features() {
		return FEATURES;
	}

	/** Registers the HUD elements. Called after every feature has been created. */
	public static void initHud() {
		for (Feature feature : HUD_FEATURES) {
			Identifier id = Identifier.fromNamespaceAndPath(ChaosUtils.MOD_ID, feature.module().id());
			HudElementRegistry.addLast(id, (graphics, deltaTracker) -> {
				try {
					if (!feature.isEnabled()) {
						return;
					}
					Minecraft client = Minecraft.getInstance();
					if (client.player == null || client.level == null || client.options.hideGui) {
						return;
					}
					Projection.setup(client, graphics.getScaledWindowWidth(), graphics.getScaledWindowHeight(), TickClock.partialTick());
					feature.onHudRender(graphics, TickClock.partialTick());
				} catch (Throwable throwable) {
					// A failing overlay must never take the game down; report once and skip.
					ChaosUtils.LOGGER.warn("HUD element {} failed to render", feature.module().id(), throwable);
					feature.disableForSession();
				}
			});
		}
	}

	public static void onWorldJoin() {
		worldReady = true;
		TickClock.reset();
		for (Feature feature : FEATURES) {
			feature.onWorldJoin();
		}
	}

	public static void onWorldLeave() {
		worldReady = false;
		zoomFactor = 1.0F;
		for (Feature feature : FEATURES) {
			feature.onWorldLeave();
		}
	}

	public static boolean isWorldReady() {
		return worldReady;
	}

	public static void tick(Minecraft client) {
		TickClock.onClientTick();
		if (reportDelay > 0 && --reportDelay == 0) {
			dev.chaosutils.core.ApiCompat.report();
		}
		for (Feature feature : FEATURES) {
			try {
				feature.onTick(client);
			} catch (Throwable throwable) {
				ChaosUtils.LOGGER.warn("Feature {} failed while ticking", feature.module().id(), throwable);
				feature.disableForSession();
			}
		}
	}

	// ------------------------------------------------------------------ mixin state

	/** Current optical zoom factor applied to the field of view ({@code 1.0} = no zoom). */
	public static float zoomFactor() {
		return zoomFactor;
	}

	public static void setZoomFactor(float factor) {
		zoomFactor = factor <= 0.0F ? 1.0F : factor;
	}

	public static boolean anySoundConsumerEnabled() {
		return dev.chaosutils.config.ModuleManager.enabled("sound_radar")
				|| dev.chaosutils.config.ModuleManager.enabled("subtitles_plus");
	}

	public static void addEntityPruneListener(IntConsumer listener) {
		ENTITY_PRUNE_LISTENERS.add(listener);
	}

	public static void pruneEntityCaches(int entityId) {
		for (IntConsumer listener : ENTITY_PRUNE_LISTENERS) {
			listener.accept(entityId);
		}
	}
}
```

### `src/main/java/dev/chaosutils/feature/FeatureRegistry.java`

```java
package dev.chaosutils.feature;

import dev.chaosutils.feature.audio.SoundRadar;
import dev.chaosutils.feature.audio.SubtitlesPlus;
import dev.chaosutils.feature.audio.VolumeDucker;
import dev.chaosutils.feature.chat.ChatHistory;
import dev.chaosutils.feature.chat.ChatMentions;
import dev.chaosutils.feature.hud.ArmorStatusHud;
import dev.chaosutils.feature.hud.CompactDebugOverlay;
import dev.chaosutils.feature.hud.DurabilityHud;
import dev.chaosutils.feature.hud.EntityHealthOverlay;
import dev.chaosutils.feature.hud.HudStyleModule;
import dev.chaosutils.feature.hud.TpsPingHud;
import dev.chaosutils.feature.hud.WaypointHud;
import dev.chaosutils.feature.inventory.ContainerPreview;
import dev.chaosutils.feature.inventory.ContainerSearch;
import dev.chaosutils.feature.inventory.ItemCounter;
import dev.chaosutils.feature.performance.ParticleReducer;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.feature.radial.RadialMenuFeature;
import dev.chaosutils.feature.visual.CrosshairDesigner;
import dev.chaosutils.feature.visual.GammaModule;
import dev.chaosutils.feature.visual.PerspectiveLock;
import dev.chaosutils.feature.visual.SmoothZoom;

/**
 * Creates every module and feature in a fixed order.
 *
 * <p>Keeping this in one place makes the feature list auditable at a glance: 23 modules, each
 * with its own settings, all of them searchable in the click GUI.
 */
public final class FeatureRegistry {
	private FeatureRegistry() {
	}

	public static void registerAll() {
		// --- shared look and feel (modules without a runtime feature) -------------
		ThemeModule.register();
		HudStyleModule.register();

		// --- radial menu ----------------------------------------------------------
		RadialMenuFeature.register();
		Features.register(new RadialMenuFeature());

		// --- HUD ------------------------------------------------------------------
		EntityHealthOverlay.register();
		Features.register(new EntityHealthOverlay());

		TpsPingHud.register();
		Features.register(new TpsPingHud());

		CompactDebugOverlay.register();
		Features.register(new CompactDebugOverlay());

		ArmorStatusHud.register();
		Features.register(new ArmorStatusHud());

		DurabilityHud.register();
		Features.register(new DurabilityHud());

		WaypointHud.register();
		Features.register(new WaypointHud());

		// --- visuals --------------------------------------------------------------
		CrosshairDesigner.register();
		Features.register(new CrosshairDesigner());

		GammaModule.register();
		Features.register(new GammaModule());

		SmoothZoom.register();
		Features.register(new SmoothZoom());

		PerspectiveLock.register();
		Features.register(new PerspectiveLock());

		// --- inventory ------------------------------------------------------------
		ContainerSearch.register();
		ContainerSearch.initEvents();
		Features.register(new ContainerSearch());

		ContainerPreview.register();
		ContainerPreview.initEvents();
		Features.register(new ContainerPreview());

		ItemCounter.register();
		ItemCounter.initEvents();
		Features.register(new ItemCounter());

		// --- chat -----------------------------------------------------------------
		ChatMentions.register();
		ChatMentions.initEvents();
		Features.register(new ChatMentions());

		ChatHistory.register();
		ChatHistory.initEvents();
		Features.register(new ChatHistory());

		// --- audio ----------------------------------------------------------------
		SoundRadar.register();
		Features.register(new SoundRadar());

		SubtitlesPlus.register();
		Features.register(new SubtitlesPlus());

		VolumeDucker.register();
		Features.register(new VolumeDucker());

		// --- quality of life ------------------------------------------------------
		ScreenshotManager.register();
		Features.register(new ScreenshotManager());

		// --- performance ----------------------------------------------------------
		ParticleReducer.register();
		Features.register(new ParticleReducer());
	}
}
```

---

## Radial menu

### `src/main/java/dev/chaosutils/feature/radial/RadialMenuFeature.java`

```java
package dev.chaosutils.feature.radial;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * The interactive radial menu - ChaosUtils' centrepiece.
 *
 * <p>Hold the radial key and a ring of slices appears around the mouse; move onto a slice and
 * release the key to run it. Slices are the entries from the config (create, rename, recolour,
 * reorder and delete them in the radial editor), and every slice performs exactly one action
 * that you asked for: send a command, send chat text, copy text to the clipboard, open the
 * ChaosUtils interface or toggle all overlays.
 *
 * <p>Nothing here is automated: the menu runs a single action on the single key release the
 * player performed. That is the same as typing the command by hand - which is exactly why it
 * is safe on servers with anti-cheat.
 */
public final class RadialMenuFeature implements Feature {
	public static final String ID = "radial_menu";

	private static Module module;
	private static Setting.Toggle holdMode;
	private static Setting.Number innerRadius;
	private static Setting.Number outerRadius;
	private static Setting.Number gapDegrees;
	private static Setting.Toggle showLabels;
	private static Setting.Toggle showIcons;
	private static Setting.Toggle showCenterText;
	private static Setting.Toggle centerOnCursor;
	private static Setting.Number deadZone;
	private static Setting.Color highlightColor;
	private static Setting.Number hoverScale;
	private static Setting.Number animationSpeed;
	private static Setting.Toggle dimBackground;
	private static Setting.Number dimStrength;
	private static Setting.Toggle chatFeedback;

	private static RadialMenuScreen open;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Radial Menu",
				"Hold a key, point at a slice, release - commands and actions at your fingertips.",
				Category.RADIAL, true));
		holdMode = (Setting.Toggle) module.add(new Setting.Toggle("hold", "Open while held",
				"On: the menu is open as long as the key is held. Off: the key toggles it.", true));
		innerRadius = (Setting.Number) module.add(new Setting.Number("inner", "Inner radius",
				"Where the slices start, relative to the centre.", 30.0, 10.0, 90.0, 1.0, "px"));
		outerRadius = (Setting.Number) module.add(new Setting.Number("outer", "Outer radius",
				"Where the slices end.", 84.0, 40.0, 200.0, 1.0, "px"));
		gapDegrees = (Setting.Number) module.add(new Setting.Number("gap", "Gap between slices",
				"Visual separation between neighbouring slices.", 2.0, 0.0, 10.0, 0.5, "°"));
		showLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Slice labels",
				"Draw the name of every slice inside the ring.", true));
		showIcons = (Setting.Toggle) module.add(new Setting.Toggle("icons", "Slice icons",
				"Draw the configured item icon for every slice.", true));
		showCenterText = (Setting.Toggle) module.add(new Setting.Toggle("center", "Centre text",
				"Show the hovered action and its command in the middle.", true));
		centerOnCursor = (Setting.Toggle) module.add(new Setting.Toggle("center_on_cursor", "Centre on cursor",
				"On: the ring appears where the cursor is. Off: it appears in the middle of the screen.", true));
		deadZone = (Setting.Number) module.add(new Setting.Number("dead_zone", "Dead zone",
				"Pixels around the centre where no slice is selected.", 12.0, 0.0, 60.0, 1.0, "px"));
		highlightColor = (Setting.Color) module.add(new Setting.Color("highlight", "Highlight colour",
				"Colour of the slice under the mouse.", 0xFFFF8A65));
		hoverScale = (Setting.Number) module.add(new Setting.Number("hover_scale", "Hover expansion",
				"How far the hovered slice grows.", 1.12, 1.0, 1.35, 0.01, "x"));
		animationSpeed = (Setting.Number) module.add(new Setting.Number("animation", "Animation speed",
				"How quickly the menu opens and the hover follows.", 14.0, 4.0, 30.0, 1.0, "x"));
		dimBackground = (Setting.Toggle) module.add(new Setting.Toggle("dim", "Dim the world",
				"Darken the screen behind the menu so it reads better.", true));
		dimStrength = (Setting.Number) module.add(new Setting.Number("dim_strength", "Dim strength",
				"Opacity of the dimming layer.", 0.35, 0.0, 0.8, 0.05));
		chatFeedback = (Setting.Toggle) module.add(new Setting.Toggle("feedback", "Chat feedback",
				"Confirm executed actions in your action bar.", true));
	}

	public static boolean isOpen() {
		return open != null;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			return;
		}
		boolean pressed = Keybinds.radialMenu != null && Keybinds.radialMenu.isPressed();
		if (!isEnabled()) {
			if (open != null) {
				open.cancel();
				open = null;
			}
			return;
		}
		if (holdMode.get()) {
			if (pressed && open == null) {
				openMenu(client);
			} else if (!pressed && open != null) {
				// The key was released: run whatever slice the player pointed at.
				RadialMenuScreen screen = open;
				open = null;
				screen.commitSelection();
			}
		} else if (pressed && Keybinds.radialMenu.wasPressed()) {
			if (open == null) {
				openMenu(client);
			} else {
				RadialMenuScreen screen = open;
				open = null;
				screen.commitSelection();
			}
		}
	}

	private static void openMenu(Minecraft client) {
		Screen parent = client.screen;
		RadialMenuScreen screen = new RadialMenuScreen(parent);
		open = screen;
		client.setScreen(screen);
	}

	/** Called by the screen when it closes itself (Escape or a click outside). */
	static void onClosed() {
		open = null;
	}

	public static List<RadialElement> entries() {
		List<RadialElement> result = new ArrayList<>(ChaosConfig.RADIAL_ELEMENTS.size());
		for (RadialElement element : ChaosConfig.RADIAL_ELEMENTS) {
			if (element.enabled) {
				result.add(element);
			}
		}
		return result;
	}

	public static float innerRadius() {
		return innerRadius.getFloat();
	}

	public static float outerRadius() {
		return outerRadius.getFloat();
	}

	public static float gapDegrees() {
		return gapDegrees.getFloat();
	}

	public static boolean showLabels() {
		return showLabels.get();
	}

	public static boolean showIcons() {
		return showIcons.get();
	}

	public static boolean showCenterText() {
		return showCenterText.get();
	}

	public static boolean centersOnCursor() {
		return centerOnCursor.get();
	}

	public static float deadZone() {
		return deadZone.getFloat();
	}

	public static int highlightColor() {
		return highlightColor.get();
	}

	public static float hoverScale() {
		return hoverScale.getFloat();
	}

	public static float animationSpeed() {
		return animationSpeed.getFloat();
	}

	public static boolean dimsBackground() {
		return dimBackground.get();
	}

	public static float dimStrength() {
		return dimStrength.getFloat();
	}

	/** Runs the action of the given element - exactly once, for this one interaction. */
	public static void execute(RadialElement element, Minecraft client) {
		String value = element.command == null ? "" : element.command.trim();
		switch (element.type) {
			case COMMAND -> sendCommand(client, value);
			case CHAT_TEXT -> sendChat(client, value);
			case COPY_TEXT -> {
				dev.chaosutils.core.Clipboard.copyText(value);
				feedback(client, "Copied: " + value);
			}
			case OPEN_SETTINGS -> client.setScreen(new dev.chaosutils.gui.ChaosClickGui());
			case TOGGLE_HUD -> {
				ChaosUtils.toggleOverlays();
				feedback(client, ChaosUtils.overlaysHidden() ? "Overlays hidden" : "Overlays shown");
			}
		}
	}

	private static void sendCommand(Minecraft client, String command) {
		if (command.isEmpty() || client.player == null) {
			return;
		}
		String normalized = command.startsWith("/") ? command.substring(1) : command;
		try {
			client.player.connection.sendCommand(normalized);
			feedback(client, "Sent: /" + normalized);
		} catch (Throwable throwable) {
			feedback(client, "Could not send /" + normalized);
		}
	}

	private static void sendChat(Minecraft client, String text) {
		if (text.isEmpty() || client.player == null) {
			return;
		}
		try {
			if (text.startsWith("/")) {
				client.player.connection.sendCommand(text.substring(1));
				feedback(client, "Sent: " + text);
			} else {
				client.player.connection.sendChat(text);
				feedback(client, "Sent: " + text);
			}
		} catch (Throwable throwable) {
			feedback(client, "Could not send the message");
		}
	}

	private static void feedback(Minecraft client, String text) {
		if (!chatFeedback.get() || client.player == null) {
			return;
		}
		client.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[Radial] §f" + text), true);
	}

	@Override
	public void onDisabled() {
		if (open != null) {
			open.cancel();
			open = null;
		}
	}
}
```

### `src/main/java/dev/chaosutils/feature/radial/RadialMenuScreen.java`

```java
package dev.chaosutils.feature.radial;

import java.util.List;

import dev.chaosutils.config.RadialElement;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * The radial menu itself.
 *
 * <p>A transparent, non-pausing screen: the world keeps running behind it, the mouse is
 * released like in any other menu, and the ring is pinned where it appeared (on the cursor by
 * default, in the middle of the screen if the player prefers that) so the slices can be pointed
 * at. Releasing the radial key runs the slice under the pointer once - never twice, never
 * repeatedly.
 *
 * <p>Rendering is done with the shared {@code Render} helpers, so the menu looks identical to
 * the rest of the interface and needs no render pipeline knowledge.
 */
public final class RadialMenuScreen extends Screen {
	private final Screen parent;
	private final Anim.Value openAnim = new Anim.Value(0.0F, 1.0F);
	private int hoveredIndex = -1;
	private float centerX;
	private float centerY;
	private boolean centerLocked;
	private float lastMouseX;
	private float lastMouseY;
	private boolean closing;
	private boolean executed;

	public RadialMenuScreen(Screen parent) {
		super(Component.literal("ChaosUtils Radial Menu"));
		this.parent = parent;
		this.openAnim.snap(0.0F);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Runs the currently hovered slice, then closes. Called when the key is released. */
	public void commitSelection() {
		executed = true;
		runHovered();
		close();
	}

	/** Closes without running anything (feature disabled, world change, ...). */
	public void cancel() {
		if (closing) {
			return;
		}
		closing = true;
		restoreParent();
	}

	private void runHovered() {
		List<RadialElement> entries = RadialMenuFeature.entries();
		if (hoveredIndex < 0 || hoveredIndex >= entries.size()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		RadialMenuFeature.execute(entries.get(hoveredIndex), client);
	}

	private void close() {
		if (closing) {
			return;
		}
		closing = true;
		restoreParent();
	}

	private void restoreParent() {
		RadialMenuFeature.onClosed();
		Minecraft client = Minecraft.getInstance();
		client.setScreen(parent);
	}

	@Override
	public void onClose() {
		// Escape closes the menu without executing anything.
		close();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float delta = dev.chaosutils.core.TickClock.frameDelta();
		openAnim.set(1.0F);
		openAnim.update(delta * Math.max(1.0F, RadialMenuFeature.animationSpeed() * 0.35F));
		float appearance = Anim.easeOutCubic(Anim.clamp01(openAnim.get()));

		List<RadialElement> entries = RadialMenuFeature.entries();
		if (entries.isEmpty()) {
			graphics.drawCenteredString(this.font, "No radial entries - add some in the radial editor", this.width / 2, this.height / 2, 0xFFE05B5B);
			super.render(graphics, mouseX, mouseY, partialTick);
			return;
		}
		// The ring is pinned where it opened: centred on the cursor (default) or on the screen.
		// Pinning is what makes hovering possible - a ring that followed the cursor could never
		// be pointed at.
		if (!centerLocked) {
			centerLocked = true;
			centerX = RadialMenuFeature.centersOnCursor() ? mouseX : this.width * 0.5F;
			centerY = RadialMenuFeature.centersOnCursor() ? mouseY : this.height * 0.5F;
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;

		if (RadialMenuFeature.dimsBackground()) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height,
					Render.alpha(0x000000, RadialMenuFeature.dimStrength() * appearance));
		}

		float inner = RadialMenuFeature.innerRadius() * appearance;
		float outer = RadialMenuFeature.outerRadius() * appearance;
		float span = 360.0F / entries.size();
		float gap = Math.min(RadialMenuFeature.gapDegrees(), span * 0.4F);
		hoveredIndex = computeHovered(entries, centerX, centerY, inner, span);
		// The hovered index is computed every frame from the last mouse position, so a key
		// release between two frames still runs the slice the player saw highlighted.

		for (int i = 0; i < entries.size(); i++) {
			RadialElement element = entries.get(i);
			boolean hovered = i == hoveredIndex;
			float start = i * span - 90.0F + gap * 0.5F;
			float end = (i + 1) * span - 90.0F - gap * 0.5F;
			float expansion = hovered ? (RadialMenuFeature.hoverScale() - 1.0F) * outer : 0.0F;
			int base = element.color;
			int color = hovered
					? Render.alpha(Render.mix(base, RadialMenuFeature.highlightColor(), 0.55F), Anim.clamp01(0.92F * appearance))
					: Render.alpha(base, Anim.clamp01(0.62F * appearance));
			Render.arc(graphics, centerX, centerY, inner, outer + expansion, start, end, color);
			if (hovered) {
				Render.arc(graphics, centerX, centerY, outer + expansion - 1.5F, outer + expansion,
						start, end, Render.alpha(0xFFFFFFFF, Anim.clamp01(0.85F * appearance)));
			}
			drawSliceContent(graphics, element, centerX, centerY, inner, outer, expansion, start, end, hovered, appearance);
		}

		// Centre text: what is currently selected.
		if (RadialMenuFeature.showCenterText()) {
			String label = hoveredIndex >= 0 ? entries.get(hoveredIndex).name : "Select an action";
			String detail = hoveredIndex >= 0 ? entries.get(hoveredIndex).command : "release to cancel";
			Render.centeredText(graphics, this.font, label, centerX, centerY - 12.0F,
					Render.alpha(0xFFFFFFFF, Anim.clamp01(appearance)), true);
			Render.centeredText(graphics, this.font, trim(detail), centerX, centerY - 2.0F,
					Render.alpha(0xFFBFC2CF, Anim.clamp01(appearance)), true);
			Render.centeredText(graphics, this.font, entries.get(Math.max(0, hoveredIndex)).type.label(), centerX, centerY + 9.0F,
					Render.alpha(0xFF8A8A9E, Anim.clamp01(appearance)), true);
		}

		String hint = "hold " + keyHint() + "  ·  release to run  ·  esc to cancel";
		Render.centeredText(graphics, this.font, hint, this.width * 0.5F, this.height - 26.0F, 0xFF9E9EB3, true);

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private String keyHint() {
		try {
			return dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "the radial key";
		}
	}

	private int computeHovered(List<RadialElement> entries, float centerX, float centerY, float inner, float span) {
		float dx = lastMouseX - centerX;
		float dy = lastMouseY - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance < Math.max(RadialMenuFeature.deadZone(), inner * 0.6F)) {
			return -1;
		}
		double angle = Math.toDegrees(Math.atan2(dx, -dy)) + 90.0;
		while (angle < 0.0) {
			angle += 360.0;
		}
		while (angle >= 360.0) {
			angle -= 360.0;
		}
		int index = (int) (angle / span);
		return Math.max(0, Math.min(entries.size() - 1, index));
	}

	private void drawSliceContent(GuiGraphics graphics, RadialElement element, float centerX, float centerY,
			float inner, float outer, float expansion, float start, float end, boolean hovered, float appearance) {
		// Content sits on the middle line of the slice: icon above, label below it (or the label
		// alone, vertically centred, when there is no icon).
		double midAngle = Math.toRadians((start + end) * 0.5);
		float midRadius = (inner + outer + expansion) * 0.5F;
		float x = centerX + (float) Math.sin(midAngle) * midRadius;
		float y = centerY - (float) Math.cos(midAngle) * midRadius;
		boolean hasIcon = RadialMenuFeature.showIcons() && !element.iconStack().isEmpty();
		if (hasIcon) {
			float scale = 0.8F + 0.2F * appearance + (hovered ? 0.15F : 0.0F);
			graphics.pose().pushPose();
			graphics.pose().translate(x, y);
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-x, -y);
			Render.item(graphics, element.iconStack(), x - 8.0F, y - 14.0F);
			graphics.pose().popPose();
		}
		if (RadialMenuFeature.showLabels()) {
			Font font = this.font;
			String name = trim(element.name);
			float labelX = x - font.width(name) * 0.5F;
			float labelY = hasIcon ? y + 3.0F : y - 4.0F;
			int color = hovered ? 0xFFFFFFFF : Render.alpha(0xFFFFFFFF, Anim.clamp01(appearance));
			Render.text(graphics, font, name, labelX, labelY, color, true);
		}
	}

	private String trim(String value) {
		String result = value == null ? "" : value;
		while (this.font.width(result) > 96 && result.length() > 4) {
			result = result.substring(0, result.length() - 2) + "…";
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			commitSelection();
			return true;
		}
		if (event.button() == 1) {
			close();
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	public boolean wasExecuted() {
		return executed;
	}
}
```

---

## Click GUI

### `src/main/java/dev/chaosutils/gui/ChaosClickGui.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.chaosutils.ChaosUtils;
import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * The main ChaosUtils interface.
 *
 * <p>Sidebar with categories on the left, searchable animated module cards on the right.
 * Every setting of every feature is rendered generically from its {@link Setting}
 * description, so nothing can be missing from the GUI.
 */
public final class ChaosClickGui extends ChaosScreen {
	private final Map<String, Boolean> expanded = new HashMap<>();
	private Category selected = Category.HUD;
	private String query = "";
	private EditBox search;

	public ChaosClickGui() {
		super(Component.literal("ChaosUtils"));
	}

	@Override
	protected void buildLayout() {
		UiTheme theme = theme();
		float sidebarWidth = theme.sidebarWidth;
		float topBar = 34.0F;
		float contentX = 12.0F + sidebarWidth + 8.0F;
		float contentWidth = this.width - contentX - 12.0F;

		// --- sidebar
		float y = topBar + 8.0F;
		List<Category> categories = List.of(Category.values());
		for (Category category : categories) {
			SidebarEntry entry = new SidebarEntry(this, category);
			entry.setBounds(12.0F, y, sidebarWidth, 24.0F);
			add(entry);
			y += 26.0F;
		}

		// --- buttons
		float buttonWidth = (sidebarWidth - 6.0F) * 0.5F;
		UiWidgets.Button hudEditor = new UiWidgets.Button(this, "HUD Editor", theme.accent, this::openHudEditor);
		hudEditor.setBounds(12.0F, this.height - 54.0F, buttonWidth, 18.0F);
		hudEditor.setTooltip("Move every overlay with the mouse.");
		add(hudEditor);

		UiWidgets.Button radialEditor = new UiWidgets.Button(this, "Radial", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.RadialEditorScreen(this));
			}
		});
		radialEditor.setBounds(12.0F + buttonWidth + 6.0F, this.height - 54.0F, buttonWidth, 18.0F);
		radialEditor.setTooltip("Create, edit and delete radial menu entries.");
		add(radialEditor);

		UiWidgets.Button waypoints = new UiWidgets.Button(this, "Waypoints", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.WaypointsScreen(this));
			}
		});
		waypoints.setBounds(12.0F, this.height - 76.0F, buttonWidth, 18.0F);
		waypoints.setTooltip("Death markers and manual waypoints.");
		add(waypoints);

		UiWidgets.Button chatHistory = new UiWidgets.Button(this, "Chat", theme.accent, () -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChaosScreens.ChatHistoryScreen(this));
			}
		});
		chatHistory.setBounds(12.0F + buttonWidth + 6.0F, this.height - 76.0F, buttonWidth, 18.0F);
		chatHistory.setTooltip("Search and copy everything you saw in chat.");
		add(chatHistory);

		UiWidgets.Button panic = new UiWidgets.Button(this, "Hide overlays", 0xFFE05B5B, () -> ChaosUtils.toggleOverlays());
		panic.setBounds(12.0F, this.height - 32.0F, sidebarWidth, 18.0F);
		panic.setTooltip("Panic key: hides every ChaosUtils overlay instantly.");
		add(panic);

		// --- search field
		search = new EditBox(this.font, Math.round(contentX), Math.round(8.0F), Math.round(Math.min(220.0F, contentWidth)), 18, Component.literal("Search"));
		search.setBordered(false);
		search.setTextColor(0xFFF2F2F7);
		search.setHint(Component.literal("Search features…"));
		search.setResponder(value -> {
			this.query = value;
			refreshCards();
		});
		addInput(search);

		// --- module cards
		ScrollPanel panel = new ScrollPanel();
		panel.setBounds(contentX, topBar + 4.0F, contentWidth, this.height - topBar - 16.0F);
		add(panel);
		rebuildCards(panel);
	}

	private ScrollPanel panel() {
		for (UiComponent component : components) {
			if (component instanceof ScrollPanel scrollPanel) {
				return scrollPanel;
			}
		}
		return null;
	}

	private void refreshCards() {
		ScrollPanel panel = panel();
		if (panel != null) {
			rebuildCards(panel);
		}
	}

	private void rebuildCards(ScrollPanel panel) {
		panel.reset();
		panel.clearChildren();
		UiTheme theme = theme();
		float cardHeight = theme.compact ? 24.0F : 28.0F;
		float gap = 6.0F;
		float y = 0.0F;
		List<Module> modules = new ArrayList<>();
		if (query != null && !query.isBlank()) {
			modules.addAll(ModuleManager.search(query));
		} else {
			modules.addAll(ModuleManager.byCategory(selected));
		}
		for (Module module : modules) {
			ModuleCard card = new ModuleCard(this, module);
			card.setBounds(0.0F, y, panel.width(), cardHeight);
			panel.addCard(card, y);
			y += cardHeight + gap + card.extraHeight();
		}
		panel.setContentHeight(y);
	}

	@Override
	protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		super.renderBackdrop(graphics, mouseX, mouseY, deltaTicks);
		UiTheme theme = theme();
		float sidebarWidth = theme.sidebarWidth;
		// Sidebar / content separation with a soft accent glow.
		Render.shadowedPanel(graphics, 12.0F, 34.0F, sidebarWidth, this.height - 34.0F - 62.0F, theme.radius, theme.panel, 0x00000000);
		Render.verticalGradient(graphics, 12.0F, 34.0F, sidebarWidth, this.height - 34.0F - 62.0F,
				Render.alpha(theme.accent, 0.10F), 0x00000000);

		float contentX = 12.0F + sidebarWidth + 8.0F;
		Render.shadowedPanel(graphics, contentX - 4.0F, 30.0F, this.width - contentX - 4.0F, this.height - 46.0F, theme.radius, theme.panelAlt, 0x00000000);

		// Header
		Render.text(graphics, font(), "ChaosUtils", 14.0F, 12.0F, theme.text, false);
		String subtitle = selected.displayName() + "  ·  " + ModuleManager.countEnabled() + "/" + ModuleManager.modules().size() + " active"
				+ (ChaosUtils.overlaysHidden() ? "  ·  OVERLAYS HIDDEN" : "");
		Render.text(graphics, font(), subtitle, 14.0F, 22.0F, theme.textFaint, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		super.render(graphics, mouseX, mouseY, deltaTicks);
		if (search != null && search.getValue().isEmpty() && !search.isFocused()) {
			Render.text(graphics, font(), "Search features…", search.getX() + 2.0F, search.getY() + 5.0F, theme().textFaint, false);
		}
	}

	// ------------------------------------------------------------------ widgets

	private static final class SidebarEntry extends UiComponent {
		private final ChaosClickGui gui;
		private final Category category;

		private SidebarEntry(ChaosClickGui gui, Category category) {
			this.gui = gui;
			this.category = category;
			this.setTooltip(category.displayName());
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			active.set(gui.selected == category ? 1.0F : 0.0F);
			active.update(deltaSeconds);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			float selectedAmount = active.get();
			int background = Render.mix(0x00000000, Render.alpha(theme.accent, 0.30F), selectedAmount);
			background = Render.mix(background, Render.alpha(0xFFFFFFFF, 0.06F), hover.get() * (1.0F - selectedAmount));
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.7F, background);
			if (selectedAmount > 0.01F) {
				Render.roundedRect(graphics, x, y + 4.0F, 2.5F * selectedAmount, height - 8.0F, 1.5F, category.color());
			}
			int textColor = Render.mix(theme.textDim, theme.text, Math.max(selectedAmount, hover.get()));
			Render.text(graphics, gui.font(), category.displayName(), x + 24.0F, y + (height - 8.0F) * 0.5F - 0.5F, textColor, false);
			Render.item(graphics, category.icon(), x + 6.0F, y + (height - 16.0F) * 0.5F);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				gui.selected = category;
				gui.query = "";
				if (gui.search != null) {
					gui.search.setValue("");
				}
				gui.refreshCards();
				gui.playClick(true);
				return true;
			}
			return false;
		}
	}

	/** Scrollable, clipped container that keeps its children's hover state correct. */
	private static final class ScrollPanel extends UiComponent {
		private final Map<UiComponent, Float> baseY = new HashMap<>();
		private float scroll;
		private float content;
		private float targetScroll;

		private void addCard(UiComponent card, float baseOffset) {
			baseY.put(card, baseOffset);
			card.setBounds(card.x(), y + baseOffset, card.width(), card.height());
			addChild(card);
		}

		private void setContentHeight(float contentHeight) {
			this.content = contentHeight;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			float maxScroll = Math.max(0.0F, content - height);
			targetScroll = Anim.clamp(targetScroll, 0.0F, maxScroll);
			scroll = Anim.approach(scroll, targetScroll, UiTheme.get().speed(14.0F), deltaSeconds);
			for (UiComponent child : children()) {
				Float base = baseY.get(child);
				if (base != null) {
					child.setBounds(child.x(), y + base - scroll, child.width(), child.height());
				}
			}
			super.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.scissor(graphics, x, y, width, height);
			renderChildren(graphics, mouseX, mouseY, deltaSeconds);
			Render.unscissor(graphics);
			float maxScroll = Math.max(1.0F, content - height);
			if (content > height) {
				float barHeight = Math.max(24.0F, height * (height / content));
				float barY = y + (height - barHeight) * (scroll / maxScroll);
				Render.roundedRect(graphics, x + width - 3.0F, barY, 3.0F, barHeight, 1.5F, Render.alpha(theme.accent, 0.65F));
			}
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				targetScroll -= (float) amount * 24.0F;
				return true;
			}
			return super.mouseScrolled(mouseX, mouseY, amount);
		}

		@Override
		public void reset() {
			super.reset();
			baseY.clear();
			scroll = 0.0F;
			targetScroll = 0.0F;
			content = 0.0F;
		}
	}

	/** One feature: header with quick toggle plus every setting, animated expansion. */
	private static final class ModuleCard extends UiComponent {
		private final ChaosClickGui gui;
		private final Module module;
		private final Anim.Value expand = new Anim.Value(0.0F, 12.0F);
		private final List<UiComponent> rows = new ArrayList<>();

		private ModuleCard(ChaosClickGui gui, Module module) {
			this.gui = gui;
			this.module = module;
			this.setTooltip(module.description());
			buildRows();
			expand.snap(expanded() ? 1.0F : 0.0F);
		}

		private boolean expanded() {
			return gui.expanded.getOrDefault(module.id(), Boolean.FALSE);
		}

		private void buildRows() {
			UiTheme theme = UiTheme.get();
			float rowHeight = theme.compact ? 18.0F : 20.0F;
			float y = 0.0F;
			for (Setting<?> setting : module.settings()) {
				if (setting == module.enabled()) {
					continue;
				}
				UiComponent row = UiWidgets.forSetting(setting, gui);
				row.setBounds(0.0F, y, width - 16.0F, rowHeight);
				row.setTooltip(setting.description + (setting.description.isEmpty() ? "" : "\n") + "default: " + defaultHint(setting));
				rows.add(row);
				y += rowHeight + 2.0F;
			}
		}

		private String defaultHint(Setting<?> setting) {
			return setting.isDefault() ? "current" : "changed";
		}

		private float extraHeight() {
			return rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size() * expand.get();
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			expand.set(expanded() ? 1.0F : 0.0F);
			expand.update(deltaSeconds);
			float headerHeight = height;
			float rowHeight = rows.isEmpty() ? 0.0F : rows.get(0).height() + 2.0F;
			for (int i = 0; i < rows.size(); i++) {
				UiComponent row = rows.get(i);
				row.setBounds(x + 8.0F, y + headerHeight + 2.0F + i * rowHeight, width - 16.0F, row.height());
			}
			// Only update rows that are inside the visible portion of the card.
			float visibleHeight = expand.get() * rowHeight * rows.size();
			for (int i = 0; i < rows.size(); i++) {
				boolean visibleRow = (i + 1) * rowHeight <= visibleHeight + rowHeight;
				rows.get(i).setVisible(visibleRow);
				if (visibleRow) {
					rows.get(i).update(deltaSeconds, mouseX, mouseY);
				}
			}
			super.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			int accent = module.category().color();
			boolean on = module.isEnabled();
			float visibleHeight = expand.get() * (rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size());
			float totalHeight = height + visibleHeight;

			int background = Render.mix(theme.panel, theme.panelHover, hover.get() * 0.7F);
			Render.roundedRect(graphics, x, y, width, totalHeight, theme.radius, background);
			Render.roundedBorder(graphics, x, y, width, totalHeight, theme.radius, 1.0F,
					Render.alpha(on ? accent : 0xFFFFFFFF, on ? 0.45F : 0.08F), background);
			if (on) {
				Render.roundedRect(graphics, x, y + 5.0F, 2.5F, height - 10.0F, 1.25F, accent);
			}
			Render.item(graphics, module.category().icon(), x + 8.0F, y + (height - 16.0F) * 0.5F);
			Render.text(graphics, gui.font(), module.name(), x + 28.0F, y + 6.0F, on ? theme.text : theme.textDim, false);
			String subtitle = module.description();
			while (gui.font().width(subtitle) > width - 120.0F && subtitle.length() > 6) {
				subtitle = subtitle.substring(0, subtitle.length() - 2) + "…";
			}
			Render.text(graphics, gui.font(), subtitle, x + 28.0F, y + height - 11.0F, theme.textFaint, false);

			// quick toggle
			float switchWidth = 26.0F;
			float sx = x + width - switchWidth - 10.0F;
			float sy = y + (height - 13.0F) * 0.5F;
			int track = Render.mix(0xFF3A3A48, accent, on ? 1.0F : 0.0F);
			Render.roundedRect(graphics, sx, sy, switchWidth, 13.0F, 6.5F, track);
			float knob = 9.0F;
			Render.roundedRect(graphics, sx + 2.0F + (on ? switchWidth - knob - 4.0F : 0.0F), sy + 2.0F, knob, knob, 4.5F, 0xFFFFFFFF);

			Render.text(graphics, gui.font(), expanded() ? "▾" : "▸", x + width - switchWidth - 26.0F, y + (height - 8.0F) * 0.5F - 0.5F,
					theme.textFaint, false);

			if (visibleHeight > 1.0F) {
				Render.scissor(graphics, x, y + height, width, visibleHeight);
				for (UiComponent row : rows) {
					if (row.isVisible()) {
						row.render(graphics, mouseX, mouseY, deltaSeconds);
					}
				}
				Render.unscissor(graphics);
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			float visibleHeight = expand.get() * (rows.isEmpty() ? 0.0F : (rows.get(0).height() + 2.0F) * rows.size());
			if (visibleHeight > 1.0F && mouseY > y + height && mouseY <= y + height + visibleHeight) {
				for (UiComponent row : rows) {
					if (row.isVisible() && row.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
			}
			float switchWidth = 26.0F;
			float sx = x + width - switchWidth - 10.0F;
			if (mouseY >= y && mouseY <= y + height) {
				if (mouseX >= sx - 2.0F && mouseX <= sx + switchWidth + 2.0F) {
					if (button == 0) {
						module.enabled().toggle();
						gui.playClick(module.isEnabled());
						return true;
					}
					if (button == 1) {
						module.enabled().reset();
						return true;
					}
				}
				if (button == 0) {
					gui.expanded.put(module.id(), !expanded());
					gui.refreshCards();
					gui.playClick(true);
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseReleased(mouseX, mouseY, button)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			return false;
		}

		@Override
		public String tooltip() {
			return module.description() + "\n§7" + module.category().displayName() + "  ·  " + keybindHint();
		}

		private String keybindHint() {
			String key = switch (module.id()) {
				case "radial_menu" -> dev.chaosutils.core.Keybinds.radialMenu == null ? "" : dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
				case "gui" -> dev.chaosutils.core.Keybinds.openGui == null ? "" : dev.chaosutils.core.Keybinds.openGui.getTranslatedKeyMessage().getString();
				case "zoom" -> dev.chaosutils.core.Keybinds.zoom == null ? "" : dev.chaosutils.core.Keybinds.zoom.getTranslatedKeyMessage().getString();
				default -> "";
			};
			if (key.isEmpty()) {
				return "Rebind in Options → Controls";
			}
			return "default key: " + key;
		}
	}

	/** Small helper so the widget package can query the currently typed search query. */
	public String query() {
		return query == null ? "" : query.toLowerCase(Locale.ROOT);
	}
}
```

### `src/main/java/dev/chaosutils/gui/ChaosScreen.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Base class for every ChaosUtils screen.
 *
 * <p>Owns the component list, per-frame animation timing, the mouse routing (components
 * first, then vanilla children such as {@link EditBox}es), the shared text/colour popups and
 * a custom tooltip renderer. Input positions are taken from the last rendered frame, which
 * keeps everything independent of the 1.21.9+ {@code MouseButtonEvent} accessors - only
 * {@code button()} is read from the event itself.
 */
public abstract class ChaosScreen extends Screen {
	protected final List<UiComponent> components = new ArrayList<>();
	private final List<EditBox> inputs = new ArrayList<>();
	private Consumer<String> activeInputHandler;
	private EditBox activeInput;
	private UiComponent popup;
	private String popupTitle = "";
	private float lastMouseX;
	private float lastMouseY;
	private long lastFrameNanos;
	protected float deltaSeconds;
	protected final Anim.Value openAnim = new Anim.Value(0.0F, 9.0F);
	private boolean escapeArmed = true;

	protected ChaosScreen(Component title) {
		super(title);
	}

	protected abstract void buildLayout();

	@Override
	protected void init() {
		components.clear();
		inputs.clear();
		popup = null;
		activeInput = null;
		activeInputHandler = null;
		openAnim.snap(0.0F);
		escapeArmed = true;
		buildLayout();
	}

	public UiTheme theme() {
		return UiTheme.get();
	}

	/** Public accessor so widgets can measure text with the screen's font. */
	public net.minecraft.client.gui.Font font() {
		return this.font;
	}

	protected void add(UiComponent component) {
		components.add(component);
	}

	/** Registers a vanilla text field so it participates in vanilla input handling. */
	protected EditBox addInput(EditBox input) {
		inputs.add(input);
		addRenderableWidget(input);
		return input;
	}

	protected void clearInputs() {
		for (EditBox input : inputs) {
			removeWidget(input);
		}
		inputs.clear();
	}

	protected List<EditBox> inputs() {
		return inputs;
	}

	public void playClick(boolean positive) {
		if (!theme().sounds || minecraft == null || minecraft.level == null) {
			return;
		}
		try {
			var sound = SoundLookup.ui(SoundLookup.uiClick(), positive ? 1.7F : 1.2F, 0.35F);
			if (sound != null) {
				minecraft.getSoundManager().play(sound);
			}
		} catch (Throwable ignored) {
			// audio is optional
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		if (popup != null) {
			// ESC closes the popup first, handled in tick() by polling.
			return false;
		}
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		handlePolledKeys();
	}

	/** ESC/Enter inside popups is polled from GLFW so no input-event accessors are needed. */
	private void handlePolledKeys() {
		if (popup == null) {
			escapeArmed = true;
			return;
		}
		if (dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_ESCAPE)) {
			if (escapeArmed) {
				escapeArmed = false;
				closePopup(false);
			}
			return;
		}
		escapeArmed = true;
		if (dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_ENTER) || dev.chaosutils.util.InputUtil.isPressed(GLFW.GLFW_KEY_KP_ENTER)) {
			closePopup(true);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		long now = System.nanoTime();
		deltaSeconds = lastFrameNanos == 0 ? 0.016F : Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
		lastFrameNanos = now;
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		openAnim.set(1.0F);
		openAnim.update(deltaSeconds);

		renderBackdrop(graphics, mouseX, mouseY, deltaTicks);

		float eased = Anim.easeOutCubic(openAnim.get());
		float offsetY = (1.0F - eased) * 12.0F;

		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, offsetY);
		for (UiComponent component : components) {
			if (component.isVisible()) {
				component.render(graphics, mouseX, mouseY, deltaSeconds);
			}
		}
		if (popup != null) {
			renderPopup(graphics, mouseX, mouseY);
		}
		graphics.pose().popPose();

		renderTooltipLayer(graphics, mouseX, mouseY);
		super.render(graphics, mouseX, mouseY, deltaTicks);
	}

	protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		if (theme().backdropStyle == 3) {
			return;
		}
		if (theme().blur) {
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
		}
		Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme().background);
		if (theme().backdropStyle == 0) {
			Render.verticalGradient(graphics, 0.0F, 0.0F, this.width, this.height, 0x00000000, 0x66000000);
		}
	}

	private void renderTooltipLayer(GuiGraphics graphics, float mouseX, float mouseY) {
		if (popup != null || !theme().tooltips) {
			return;
		}
		String tooltip = hoveredTooltip(mouseX, mouseY);
		if (tooltip == null || tooltip.isEmpty()) {
			return;
		}
		String[] lines = tooltip.split("\n");
		int widest = 0;
		for (String line : lines) {
			widest = Math.max(widest, this.font.width(line));
		}
		float boxWidth = widest + 12.0F;
		float boxHeight = lines.length * 10.0F + 8.0F;
		float bx = Math.min(mouseX + 10.0F, this.width - boxWidth - 4.0F);
		float by = Math.min(mouseY + 12.0F, this.height - boxHeight - 4.0F);
		Render.shadowedPanel(graphics, bx, by, boxWidth, boxHeight, 4.0F, 0xF0101018, theme().accent);
		for (int i = 0; i < lines.length; i++) {
			Render.text(graphics, this.font, lines[i], bx + 6.0F, by + 4.0F + i * 10.0F, theme().text, false);
		}
	}

	private String hoveredTooltip(float mouseX, float mouseY) {
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.contains(mouseX, mouseY) && component.tooltip() != null) {
				return component.tooltip();
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int button = event.button();
		if (popup != null) {
			if (popup.mouseClicked(xFromClick(button), yFromClick(button), button) || popup.contains(lastMouseX, lastMouseY)) {
				return true;
			}
			closePopup(false);
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseClicked(lastMouseX, lastMouseY, button)) {
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		int button = event.button();
		if (popup != null && popup.mouseReleased(lastMouseX, lastMouseY, button)) {
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseReleased(lastMouseX, lastMouseY, button)) {
				return true;
			}
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		int button = event.button();
		if (popup != null && popup.mouseDragged(lastMouseX, lastMouseY, button, (float) deltaX, (float) deltaY)) {
			return true;
		}
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseDragged(lastMouseX, lastMouseY, button, (float) deltaX, (float) deltaY)) {
				return true;
			}
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (int i = components.size() - 1; i >= 0; i--) {
			UiComponent component = components.get(i);
			if (component.isVisible() && component.mouseScrolled((float) mouseX, (float) mouseY, verticalAmount)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private float xFromClick(int button) {
		return lastMouseX;
	}

	private float yFromClick(int button) {
		return lastMouseY;
	}

	protected float mouseX() {
		return lastMouseX;
	}

	protected float mouseY() {
		return lastMouseY;
	}

	protected float deltaSeconds() {
		return deltaSeconds;
	}

	// -------------------------------------------------------------------- popups

	protected void openTextEditor(Setting.Text setting) {
		openTextEditor(setting.label, setting.get(), setting.maxLength(), setting::set);
	}

	protected void openTextEditor(String title, String initial, int maxLength, Consumer<String> onAccept) {
		this.popupTitle = title;
		this.activeInputHandler = onAccept;
		this.activeInput = new EditBox(this.font, 0, 0, 200, 16, Component.literal(title));
		this.activeInput.setMaxLength(maxLength);
		this.activeInput.setValue(initial);
		this.activeInput.setBordered(false);
		this.activeInput.setTextColor(0xFFF2F2F7);
		addInput(this.activeInput);
		setFocused(this.activeInput);
		rebuildPopup();
	}

	protected void openColorPicker(Setting.Color setting) {
		this.popupTitle = setting.label;
		UiWidgets.ColorPicker picker = new UiWidgets.ColorPicker(this, setting);
		this.popup = picker;
		this.activeInputHandler = null;
		layoutPopup();
	}

	private void rebuildPopup() {
		if (activeInput == null) {
			return;
		}
		this.popup = new UiWidgets.TextPopup(this);
		layoutPopup();
	}

	private void layoutPopup() {
		if (popup == null) {
			return;
		}
		float popupWidth = Math.max(220.0F, popup.width());
		float popupHeight = Math.max(90.0F, popup.height());
		float px = (this.width - popupWidth) * 0.5F;
		float py = (this.height - popupHeight) * 0.5F;
		popup.setBounds(px, py, popupWidth, popupHeight);
		if (popup instanceof UiWidgets.TextPopup textPopup) {
			textPopup.layout(px, py, popupWidth, popupHeight, activeInput);
		}
		if (popup instanceof UiWidgets.ColorPicker picker) {
			picker.layout(px, py, popupWidth, popupHeight);
		}
	}

	protected void closePopup(boolean accept) {
		if (accept && activeInput != null && activeInputHandler != null) {
			activeInputHandler.accept(activeInput.getValue());
		}
		if (activeInput != null) {
			removeWidget(activeInput);
			inputs.remove(activeInput);
			activeInput = null;
		}
		activeInputHandler = null;
		popup = null;
		setFocused(null);
	}

	private void renderPopup(GuiGraphics graphics, float mouseX, float mouseY) {
		Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, 0x88000000);
		popup.render(graphics, mouseX, mouseY, deltaSeconds);
		Render.centeredText(graphics, this.font, popupTitle, popup.x() + popup.width() * 0.5F, popup.y() + 8.0F, theme().text, false);
	}

	protected String popupTitle() {
		return popupTitle;
	}

	protected EditBox activeInput() {
		return activeInput;
	}

	protected Minecraft client() {
		return this.minecraft;
	}

	@Override
	public void removed() {
		clearInputs();
		super.removed();
	}

	/** Hook used by the click GUI to open the HUD editor; overridden where meaningful. */
	public void openHudEditor() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(new HudEditorScreen(this));
		}
	}
}
```

### `src/main/java/dev/chaosutils/gui/ChaosScreens.java`

```java
package dev.chaosutils.gui;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The smaller ChaosUtils screens: radial editor, chat history, screenshots and waypoints, plus
 * the shared text input dialog they use.
 *
 * <p>All of them are built from the same component toolkit as the main interface, so styling,
 * animations and input handling stay consistent and there is exactly one place to change them.
 */
public final class ChaosScreens {
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
			.withZone(ZoneId.systemDefault());

	private ChaosScreens() {
	}

	/** A simple, modal text dialog with a vanilla text field so typing behaves as expected. */
	public static final class TextInputScreen extends Screen {
		private final Screen parent;
		private final String title;
		private final String initial;
		private final int maxLength;
		private final Consumer<String> onAccept;
		private EditBox field;
		private boolean enterHeld;

		public TextInputScreen(Screen parent, String title, String initial, int maxLength, Consumer<String> onAccept) {
			super(Component.literal(title));
			this.parent = parent;
			this.title = title;
			this.initial = initial == null ? "" : initial;
			this.maxLength = Math.max(1, maxLength);
			this.onAccept = onAccept;
		}

		@Override
		protected void init() {
			int boxWidth = Math.min(260, this.width - 60);
			int x = (this.width - boxWidth) / 2;
			int y = this.height / 2 - 8;
			field = new EditBox(this.font, x, y, boxWidth, 18, Component.literal(title));
			field.setValue(initial);
			field.setMaxLength(maxLength);
			field.setBordered(true);
			addRenderableWidget(field);
			setInitialFocus(field);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			renderBackground(graphics, mouseX, mouseY, partialTick);
			UiTheme theme = UiTheme.get();
			int boxWidth = Math.min(260, this.width - 60) + 16;
			int x = (this.width - boxWidth) / 2;
			int y = this.height / 2 - 30;
			Render.shadowedPanel(graphics, x, y, boxWidth, 68.0F, theme.radius, theme.panel, theme.accent);
			Render.centeredText(graphics, this.font, title, this.width * 0.5F, y + 8.0F, theme.text, false);
			Render.centeredText(graphics, this.font, "Enter to accept  ·  Escape to cancel", this.width * 0.5F, y + 52.0F,
					theme.textFaint, false);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		/**
		 * Enter is polled instead of overriding an input event method, which keeps this screen
		 * independent of the input event record accessors introduced in 1.21.9.
		 */
		@Override
		public void tick() {
			super.tick();
			boolean enter = dev.chaosutils.util.InputUtil.isPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
					|| dev.chaosutils.util.InputUtil.isPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER);
			if (enter && !enterHeld) {
				enterHeld = true;
				accept();
			} else if (!enter) {
				enterHeld = false;
			}
		}

		@Override
		public void onClose() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		private void accept() {
			if (onAccept != null) {
				onAccept.accept(field == null ? "" : field.getValue());
			}
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}
	}

	/** Base for the list-oriented screens: consistent frame, title and close button. */
	abstract static class ListScreen extends ChaosScreen {
		protected final Screen parent;
		private final String heading;

		protected ListScreen(Screen parent, String heading) {
			super(Component.literal("ChaosUtils " + heading));
			this.parent = parent;
			this.heading = heading;
		}

		protected abstract void buildRows(float top, float left, float width, float rowHeight);

		@Override
		protected void buildLayout() {
			UiTheme theme = theme();
			float left = 18.0F;
			float width = this.width - 36.0F;
			float top = 46.0F;
			float rowHeight = 20.0F;

			UiWidgets.Button close = new UiWidgets.Button(this, "Back", theme.accent, this::back);
			close.setBounds(this.width - 76.0F, this.height - 28.0F, 58.0F, 18.0F);
			add(close);

			buildRows(top, left, width, rowHeight);
		}

		protected void back() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}

		@Override
		public void onClose() {
			back();
		}

		@Override
		protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
			UiTheme theme = theme();
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme.background);
			Render.shadowedPanel(graphics, 12.0F, 12.0F, this.width - 24.0F, this.height - 24.0F, theme.radius,
					theme.panel, theme.accent);
			Render.text(graphics, font(), heading, 20.0F, 22.0F, theme.text, false);
			Render.text(graphics, font(), subtitle(), 20.0F, 32.0F, theme.textFaint, false);
		}

		protected String subtitle() {
			return "Changes are saved automatically.";
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}

	// ------------------------------------------------------------- radial editor

	public static final class RadialEditorScreen extends ChaosScreen {
		private final Screen parent;
		private int selected = -1;

		public RadialEditorScreen(Screen parent) {
			super(Component.literal("ChaosUtils Radial Editor"));
			this.parent = parent;
		}

		@Override
		protected void buildLayout() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			if (selected >= entries.size()) {
				selected = entries.isEmpty() ? -1 : entries.size() - 1;
			}
			UiTheme theme = theme();
			float listLeft = 18.0F;
			float listWidth = 190.0F;
			float rowHeight = 20.0F;
			float top = 48.0F;
			int visible = (int) ((this.height - 110.0F) / rowHeight);
			for (int i = 0; i < Math.min(visible, entries.size()); i++) {
				final int index = i;
				RadialElement element = entries.get(i);
				UiWidgets.Button row = new UiWidgets.Button(this, element.name + "  §7" + element.type.label().split(" ")[0],
						i == selected ? theme.accent : 0xFF3A3A48, () -> {
							selected = index;
							refresh();
						});
				row.setBounds(listLeft, top + i * rowHeight, listWidth, rowHeight - 3.0F);
				add(row);
			}

			float buttonsY = this.height - 58.0F;
			UiWidgets.Button add = new UiWidgets.Button(this, "Add", theme.positive, () -> {
				ChaosConfig.RADIAL_ELEMENTS.add(new RadialElement("New Action", "/spawn", theme.accent,
						"minecraft:compass", RadialElement.ActionType.COMMAND));
				ChaosConfig.markDirty();
				selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
				refresh();
			});
			add.setBounds(listLeft, buttonsY, 60.0F, 18.0F);
			add(add);

			UiWidgets.Button duplicate = new UiWidgets.Button(this, "Copy", theme.accent, () -> {
				RadialElement element = current();
				if (element != null) {
					ChaosConfig.RADIAL_ELEMENTS.add(element.copy());
					ChaosConfig.markDirty();
					refresh();
				}
			});
			duplicate.setBounds(listLeft + 64.0F, buttonsY, 60.0F, 18.0F);
			add(duplicate);

			UiWidgets.Button delete = new UiWidgets.Button(this, "Delete", theme.negative, () -> {
				RadialElement element = current();
				if (element != null) {
					ChaosConfig.RADIAL_ELEMENTS.remove(element);
					ChaosConfig.markDirty();
					selected = Math.max(-1, Math.min(selected, ChaosConfig.RADIAL_ELEMENTS.size() - 1));
					refresh();
				}
			});
			delete.setBounds(listLeft + 128.0F, buttonsY, 60.0F, 18.0F);
			add(delete);

			UiWidgets.Button moveUp = new UiWidgets.Button(this, "↑", 0xFF3A3A48, () -> move(-1));
			moveUp.setBounds(listLeft, buttonsY - 22.0F, 28.0F, 18.0F);
			add(moveUp);

			UiWidgets.Button moveDown = new UiWidgets.Button(this, "↓", 0xFF3A3A48, () -> move(1));
			moveDown.setBounds(listLeft + 32.0F, buttonsY - 22.0F, 28.0F, 18.0F);
			add(moveDown);

			UiWidgets.Button back = new UiWidgets.Button(this, "Back", theme.accent, () -> {
				if (this.minecraft != null) {
					this.minecraft.setScreen(parent);
				}
			});
			back.setBounds(this.width - 76.0F, this.height - 28.0F, 58.0F, 18.0F);
			add(back);

			buildEditor(listLeft + listWidth + 14.0F, 48.0F, this.width - listLeft - listWidth - 32.0F);
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new RadialEditorScreen(parent));
			}
		}

		private void move(int direction) {
			RadialElement element = current();
			if (element == null) {
				return;
			}
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			int index = entries.indexOf(element);
			int target = index + direction;
			if (target < 0 || target >= entries.size()) {
				return;
			}
			entries.set(index, entries.get(target));
			entries.set(target, element);
			selected = target;
			ChaosConfig.markDirty();
			refresh();
		}

		private RadialElement current() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
		}

		private void buildEditor(float left, float top, float width) {
			RadialElement element = current();
			float rowHeight = 22.0F;
			UiTheme theme = theme();
			if (element == null) {
				return;
			}
			int row = 0;
			add(valueRow(left, top + row++ * rowHeight, width, "Name", element.name, theme.accent, () ->
					openText("Display name", element.name, 48, value -> {
						element.name = value;
						ChaosConfig.markDirty();
						refresh();
					}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Command", element.command, theme.accent, () ->
					openText(element.type == RadialElement.ActionType.COPY_TEXT ? "Text to copy" : "Command or text",
							element.command, 256, value -> {
								element.command = value;
								ChaosConfig.markDirty();
								refresh();
							}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Action", element.type.label(), theme.accent,
					() -> {
						RadialElement.ActionType[] types = RadialElement.ActionType.values();
						element.type = types[(element.type.ordinal() + 1) % types.length];
						ChaosConfig.markDirty();
						refresh();
					}, null));
			add(valueRow(left, top + row++ * rowHeight, width, "Icon", element.icon, theme.accent, () ->
					openText("Item id", element.icon, 64, value -> {
						element.icon = value;
						ChaosConfig.markDirty();
						refresh();
					}), null));
			add(valueRow(left, top + row++ * rowHeight, width, "Colour", String.format("#%06X", element.color & 0xFFFFFF),
					element.color, () -> {
						int[] palette = {0xFFFF8A65, 0xFF7C5CFF, 0xFF4FC3F7, 0xFF81C784, 0xFFF0B429, 0xFFF06292,
								0xFF64B5F6, 0xFF4DB6AC};
						int index = 0;
						for (int i = 0; i < palette.length; i++) {
							if (palette[i] == element.color) {
								index = (i + 1) % palette.length;
								break;
							}
						}
						element.color = palette[index];
						ChaosConfig.markDirty();
						refresh();
					}, () -> openPicker(element)));
			add(valueRow(left, top + row++ * rowHeight, width, "Enabled", element.enabled ? "yes" : "no",
					element.enabled ? theme.positive : theme.negative, () -> {
						element.enabled = !element.enabled;
						ChaosConfig.markDirty();
						refresh();
					}, null));
			add(valueRow(left, top + row++ * rowHeight, width, "Slice", (selected + 1) + " of "
					+ ChaosConfig.RADIAL_ELEMENTS.size(), theme.textFaint, null, null));
		}

		private UiComponent valueRow(float x, float y, float width, String label, String value, int accent,
				Runnable onLeft, Runnable onRight) {
			ValueRow row = new ValueRow(label, value, accent, onLeft, onRight);
			row.setBounds(x, y, width, 18.0F);
			return row;
		}

		private void openText(String title, String initial, int maxLength, Consumer<String> onAccept) {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new TextInputScreen(this, title, initial, maxLength, onAccept));
			}
		}

		private void openPicker(RadialElement element) {
			dev.chaosutils.config.Setting.Color temporary =
					new dev.chaosutils.config.Setting.Color("radial_colour", "Slice colour", "", element.color);
			temporary.onChanged(value -> {
				element.color = value;
				ChaosConfig.markDirty();
			});
			openColorPicker(temporary);
		}

		@Override
		protected void renderBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
			UiTheme theme = theme();
			renderBackground(graphics, mouseX, mouseY, deltaTicks);
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, theme.background);
			Render.shadowedPanel(graphics, 12.0F, 12.0F, this.width - 24.0F, this.height - 24.0F, theme.radius,
					theme.panel, theme.accent);
			Render.text(graphics, font(), "Radial Menu", 20.0F, 22.0F, theme.text, false);
			Render.text(graphics, font(), "Click a slice to edit it · " + ChaosConfig.RADIAL_ELEMENTS.size() + " entries",
					20.0F, 32.0F, theme.textFaint, false);
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}

	// ------------------------------------------------------------- chat history

	public static final class ChatHistoryScreen extends ListScreen {
		private String query = "";
		private EditBox search;
		private final List<ChatLog.Entry> visible = new ArrayList<>();

		public ChatHistoryScreen(Screen parent) {
			super(parent, "Chat History");
		}

		@Override
		protected String subtitle() {
			return ChatLog.recent(400).size() + " buffered lines · click a line to copy it";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			refreshEntries();
			int visibleRows = (int) ((this.height - top - 70.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, visible.size()); i++) {
				ChatLog.Entry entry = visible.get(i);
				String text = "§7[" + TIME.format(entry.time()) + "] §r" + entry.plain();
				ChatLog.Entry captured = entry;
				UiWidgets.Button row = new UiWidgets.Button(this, trim(text, width - 20.0F), 0xFF3A3A48,
						() -> Clipboard.copyText(captured.plain()));
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}
			search = new EditBox(this.font, Math.round(left), Math.round(top - 24.0F), Math.round(Math.min(220.0F, width)),
					16, Component.literal("Search"));
			search.setValue(query);
			search.setResponder(value -> {
				query = value;
				refresh();
			});
			addInput(search);

			UiWidgets.Button copyAll = new UiWidgets.Button(this, "Copy all", theme.accent, () -> {
				StringBuilder builder = new StringBuilder();
				for (ChatLog.Entry entry : visible) {
					builder.append('[').append(TIME.format(entry.time())).append("] ").append(entry.plain()).append('\n');
				}
				Clipboard.copyText(builder.toString());
			});
			copyAll.setBounds(left, this.height - 28.0F, 70.0F, 18.0F);
			add(copyAll);

			UiWidgets.Button clear = new UiWidgets.Button(this, "Clear", theme.negative, () -> {
				ChatLog.clear();
				refresh();
			});
			clear.setBounds(left + 76.0F, this.height - 28.0F, 60.0F, 18.0F);
			add(clear);
		}

		private void refreshEntries() {
			visible.clear();
			visible.addAll(query == null || query.isBlank() ? ChatLog.recent(200) : ChatLog.search(query, null, 200));
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ChatHistoryScreen(parent));
			}
		}

		private String trim(String value, float maxWidth) {
			String result = value;
			while (font().width(result) > maxWidth && result.length() > 6) {
				result = result.substring(0, result.length() - 2) + "…";
			}
			return result;
		}
	}

	// --------------------------------------------------------------- screenshots

	public static final class ScreenshotScreen extends ListScreen {
		private int selected;
		private final List<Path> files = new ArrayList<>();

		public ScreenshotScreen(Screen parent) {
			super(parent, "Screenshots");
		}

		@Override
		protected String subtitle() {
			return "Everything happens locally - nothing is uploaded.";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			files.clear();
			files.addAll(ScreenshotManager.recent(16));
			int visibleRows = (int) ((this.height - top - 90.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, files.size()); i++) {
				final int index = i;
				Path path = files.get(i);
				String name = path.getFileName().toString();
				String stamp = TIME.format(Instant.ofEpochMilli(path.toFile().lastModified()));
				String label = (i == selected ? "§f" : "§7") + stamp + "  §r" + name;
				UiWidgets.Button row = new UiWidgets.Button(this, label, i == selected ? theme.accent : 0xFF3A3A48,
						() -> {
							selected = index;
							refresh();
						});
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}

			float buttonsY = this.height - 28.0F;
			UiWidgets.Button copyImage = new UiWidgets.Button(this, "Copy image", theme.accent, () -> {
				Path path = currentPath();
				if (path != null) {
					Clipboard.copyImage(path);
				}
			});
			copyImage.setBounds(left, buttonsY, 78.0F, 18.0F);
			add(copyImage);

			UiWidgets.Button copyPath = new UiWidgets.Button(this, "Copy path", 0xFF4FC3F7, () -> {
				Path path = currentPath();
				if (path != null) {
					Clipboard.copyText(path.toAbsolutePath().toString());
				}
			});
			copyPath.setBounds(left + 84.0F, buttonsY, 74.0F, 18.0F);
			add(copyPath);

			UiWidgets.Button crop = new UiWidgets.Button(this, "Crop & copy", theme.warning, () -> {
				Path path = currentPath();
				if (path != null) {
					ScreenshotManager.cropWithConfiguredPreset(path);
				}
			});
			crop.setBounds(left + 164.0F, buttonsY, 86.0F, 18.0F);
			add(crop);

			UiWidgets.Button openFolder = new UiWidgets.Button(this, "Open folder", 0xFF81C784, () ->
					Clipboard.openFile(ScreenshotManager.screenshotsDirectory()));
			openFolder.setBounds(left + 256.0F, buttonsY, 86.0F, 18.0F);
			add(openFolder);
		}

		private Path currentPath() {
			return selected >= 0 && selected < files.size() ? files.get(selected) : null;
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new ScreenshotScreen(parent));
			}
		}
	}

	// ---------------------------------------------------------------- waypoints

	public static final class WaypointsScreen extends ListScreen {
		private final List<Waypoint> listed = new ArrayList<>();

		public WaypointsScreen(Screen parent) {
			super(parent, "Waypoints");
		}

		@Override
		protected String subtitle() {
			return "Left click a waypoint to copy its coordinates, right click to delete it.";
		}

		@Override
		protected void buildRows(float top, float left, float width, float rowHeight) {
			UiTheme theme = theme();
			listed.clear();
			listed.addAll(ChaosConfig.WAYPOINTS);
			int visibleRows = (int) ((this.height - top - 90.0F) / rowHeight);
			for (int i = 0; i < Math.min(visibleRows, listed.size()); i++) {
				Waypoint waypoint = listed.get(i);
				String dimension = waypoint.dimension.contains(":") ? waypoint.dimension.split(":")[1] : waypoint.dimension;
				String label = (waypoint.temporary ? "§7*" : "§f") + waypoint.name + " §8· §7" + dimension + " §8· "
						+ (int) waypoint.x + " " + (int) waypoint.y + " " + (int) waypoint.z;
				WaypointRow row = new WaypointRow(label, waypoint.color, waypoint,
						() -> Clipboard.copyText(
								String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z)),
						() -> {
							ChaosConfig.WAYPOINTS.remove(waypoint);
							ChaosConfig.markDirty();
							refresh();
						});
				row.setBounds(left, top + i * rowHeight, width, rowHeight - 3.0F);
				add(row);
			}

			UiWidgets.Button addHere = new UiWidgets.Button(this, "Add current position", theme.accent, () -> {
				net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
				if (client.player == null || client.level == null) {
					return;
				}
				ChaosConfig.WAYPOINTS.add(new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
						client.player.getX(), client.player.getY(), client.player.getZ(),
						client.level.dimension().location().toString(), theme.accent, false, 0L));
				ChaosConfig.markDirty();
				refresh();
			});
			addHere.setBounds(left, this.height - 28.0F, 120.0F, 18.0F);
			add(addHere);

			UiWidgets.Button clearTemporary = new UiWidgets.Button(this, "Clear temporary", theme.warning, () -> {
				ChaosConfig.WAYPOINTS.removeIf(waypoint -> waypoint.temporary);
				ChaosConfig.markDirty();
				refresh();
			});
			clearTemporary.setBounds(left + 126.0F, this.height - 28.0F, 100.0F, 18.0F);
			add(clearTemporary);
		}

		private void refresh() {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new WaypointsScreen(parent));
			}
		}
	}

	/** Label / value row with optional left and right click actions. */
	static final class ValueRow extends UiComponent {
		private final String label;
		private final String value;
		private final int accent;
		private final Runnable onLeft;
		private final Runnable onRight;

		ValueRow(String label, String value, int accent, Runnable onLeft, Runnable onRight) {
			this.label = label;
			this.value = value;
			this.accent = accent;
			this.onLeft = onLeft;
			this.onRight = onRight;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Font font = net.minecraft.client.Minecraft.getInstance().font;
			Render.text(graphics, font, label, x, y + 5.0F, theme.textDim, false);
			float boxWidth = width - 96.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			String text = value == null ? "" : value;
			while (font.width(text) > boxWidth - 10.0F && text.length() > 4) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			Render.text(graphics, font, text, bx + 5.0F, y + 5.0F, theme.text, false);
			Render.rect(graphics, bx + 1.0F, y + height - 2.0F, boxWidth - 2.0F, 1.0F, Render.alpha(accent, 0.6F));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0 && onLeft != null) {
				onLeft.run();
				return true;
			}
			if (button == 1 && onRight != null) {
				onRight.run();
				return true;
			}
			return onLeft != null || onRight != null;
		}
	}

	/** Waypoint row: the usual look with right click delete. */
	static final class WaypointRow extends UiComponent {
		private final String label;
		private final int accent;
		private final Waypoint waypoint;
		private final Runnable onLeft;
		private final Runnable onRight;

		WaypointRow(String label, int accent, Waypoint waypoint, Runnable onLeft, Runnable onRight) {
			this.label = label;
			this.accent = accent;
			this.waypoint = waypoint;
			this.onLeft = onLeft;
			this.onRight = onRight;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Font font = net.minecraft.client.Minecraft.getInstance().font;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.6F, background);
			Render.rect(graphics, x + 2.0F, y + 3.0F, 2.0F, height - 6.0F, accent);
			String text = label;
			while (font.width(text) > width - 14.0F && text.length() > 6) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			Render.text(graphics, font, text, x + 8.0F, y + 5.0F, theme.text, false);
			Render.text(graphics, font, waypoint.expired() ? "expired" : "", x + width - 44.0F, y + 5.0F,
					theme.textFaint, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0) {
				onLeft.run();
				return true;
			}
			if (button == 1) {
				onRight.run();
				return true;
			}
			return false;
		}
	}

}
```

### `src/main/java/dev/chaosutils/gui/HudEditorScreen.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Drag &amp; drop editor for every movable overlay.
 *
 * <p>Overlays store their position as a fraction of the free space, so this screen simply
 * maps the drag delta onto that fraction. Holding the module list on the left and the live
 * preview of every enabled overlay makes arranging the HUD a two second job.
 */
public final class HudEditorScreen extends Screen {
	private record Entry(Module module, Setting.Position position, String label) {
	}

	private final Screen parent;
	private final List<Entry> entries = new ArrayList<>();
	private Entry dragging;
	private float grabOffsetX;
	private float grabOffsetY;
	private float previewX;
	private float previewY;
	private float previewWidth = 120.0F;
	private float previewHeight = 26.0F;
	private float lastMouseX;
	private float lastMouseY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("ChaosUtils HUD Editor"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		entries.clear();
		for (Module module : ModuleManager.modules()) {
			if (!module.isEnabled()) {
				continue;
			}
			for (Setting<?> setting : module.settings()) {
				if (setting instanceof Setting.Position position) {
					entries.add(new Entry(module, position, module.name() + " · " + setting.label));
				}
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		UiTheme theme = UiTheme.get();
		renderBackground(graphics, mouseX, mouseY, deltaTicks);
		Render.rect(graphics, 0.0F, 0.0F, width, height, theme.background);

		float panelWidth = 190.0F;
		Render.shadowedPanel(graphics, 10.0F, 10.0F, panelWidth, height - 20.0F, theme.radius, theme.panel, theme.accent);
		Render.text(graphics, font, "HUD Editor", 20.0F, 20.0F, theme.text, false);
		Render.text(graphics, font, "Drag the ghost boxes to", 20.0F, 34.0F, theme.textDim, false);
		Render.text(graphics, font, "place overlays. Esc applies.", 20.0F, 44.0F, theme.textDim, false);

		float listY = 62.0F;
		for (Entry entry : entries) {
			boolean hovered = mouseX >= 14.0F && mouseX <= 10.0F + panelWidth - 4.0F && mouseY >= listY && mouseY <= listY + 16.0F;
			if (hovered) {
				Render.roundedRect(graphics, 14.0F, listY - 2.0F, panelWidth - 8.0F, 18.0F, 4.0F, Render.alpha(theme.accent, 0.25F));
			}
			Render.text(graphics, font, trim(entry.label), 20.0F, listY + 2.0F, hovered ? theme.text : theme.textDim, false);
			listY += 18.0F;
			if (listY > height - 40.0F) {
				break;
			}
		}

		// Ghost previews of every movable overlay.
		for (Entry entry : entries) {
			boolean isDragging = dragging == entry;
			float x = entry.position.get().screenX(this.width, Math.round(previewWidth));
			float y = entry.position.get().screenY(this.height, Math.round(previewHeight));
			int accent = entry.module.category().color();
			int fill = isDragging ? Render.alpha(accent, 0.55F) : Render.alpha(accent, 0.25F);
			Render.roundedRect(graphics, x, y, previewWidth, previewHeight, theme.radius, fill);
			Render.roundedBorder(graphics, x, y, previewWidth, previewHeight, theme.radius, 1.0F, Render.alpha(accent, 0.9F), fill);
			Render.text(graphics, font, trim(entry.module.name()), x + 6.0F, y + 6.0F, theme.text, true);
			Render.text(graphics, font, entry.position.display(), x + 6.0F, y + 16.0F, theme.textDim, false);
		}
		super.render(graphics, mouseX, mouseY, deltaTicks);
	}

	private String trim(String value) {
		String result = value;
		while (font.width(result) > 168 && result.length() > 4) {
			result = result.substring(0, result.length() - 2) + "…";
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubled);
		}
		for (Entry entry : entries) {
			float x = entry.position.get().screenX(this.width, Math.round(previewWidth));
			float y = entry.position.get().screenY(this.height, Math.round(previewHeight));
			if (lastMouseX >= x && lastMouseX <= x + previewWidth && lastMouseY >= y && lastMouseY <= y + previewHeight) {
				dragging = entry;
				grabOffsetX = lastMouseX - x;
				grabOffsetY = lastMouseY - y;
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (dragging != null) {
			float targetX = lastMouseX - grabOffsetX;
			float targetY = lastMouseY - grabOffsetY;
			int maxX = Math.max(1, this.width - Math.round(previewWidth));
			int maxY = Math.max(1, this.height - Math.round(previewHeight));
			float fractionX = Math.max(0.0F, Math.min(1.0F, targetX / maxX));
			float fractionY = Math.max(0.0F, Math.min(1.0F, targetY / maxY));
			dragging.position.set(new dev.chaosutils.util.HudPos(fractionX, fractionY));
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiComponent.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.util.Anim;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight GUI element.
 *
 * <p>ChaosUtils deliberately uses its own component model instead of vanilla's widget
 * hierarchy: the input signatures of {@code AbstractWidget} changed with the 1.21.9 input
 * rework, and owning the model lets every element animate smoothly (hover glow, press
 * depth, expand/collapse) with shared timing instead of per-widget tweens.
 */
public abstract class UiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected boolean visible = true;
	protected boolean enabled = true;
	protected String tooltip;

	/** 0..1 hover amount, animated. */
	protected final Anim.Value hover = new Anim.Value(0.0F, 14.0F);
	/** 0..1 "activated" amount used for press feedback and toggles. */
	protected final Anim.Value active = new Anim.Value(0.0F, 10.0F);

	private final List<UiComponent> children = new ArrayList<>();

	public UiComponent setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		return this;
	}

	public UiComponent setTooltip(@Nullable String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public String tooltip() {
		return tooltip;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float width() {
		return width;
	}

	public float height() {
		return height;
	}

	public float bottom() {
		return y + height;
	}

	public float right() {
		return x + width;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public List<UiComponent> children() {
		return children;
	}

	protected void addChild(UiComponent child) {
		children.add(child);
	}

	public void clearChildren() {
		children.clear();
	}

	public void update(float deltaSeconds, float mouseX, float mouseY) {
		float hovering = enabled && visible && contains(mouseX, mouseY) ? 1.0F : 0.0F;
		hover.set(hovering);
		hover.update(deltaSeconds);
		active.update(deltaSeconds);
		for (UiComponent child : children) {
			child.update(deltaSeconds, mouseX, mouseY);
		}
	}

	public abstract void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds);

	public void renderChildren(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
		for (UiComponent child : children) {
			if (child.isVisible()) {
				child.render(graphics, mouseX, mouseY, deltaSeconds);
			}
		}
	}

	public boolean mouseClicked(float mouseX, float mouseY, int button) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(float mouseX, float mouseY, int button) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseReleased(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
		for (int i = children.size() - 1; i >= 0; i--) {
			UiComponent child = children.get(i);
			if (child.isVisible() && child.mouseScrolled(mouseX, mouseY, amount)) {
				return true;
			}
		}
		return false;
	}

	public boolean keyPressed(int keyCode, int modifiers) {
		for (UiComponent child : children) {
			if (child.isVisible() && child.keyPressed(keyCode, modifiers)) {
				return true;
			}
		}
		return false;
	}

	/** Content height used by scroll layouts; may exceed {@link #height}. */
	public float contentHeight() {
		float max = height;
		for (UiComponent child : children) {
			max = Math.max(max, child.y() - y + child.contentHeight());
		}
		return max;
	}

	/** Called when a component becomes hidden so it can drop transient state (keeps memory flat). */
	public void reset() {
		hover.snap(0.0F);
		active.snap(0.0F);
		for (UiComponent child : children) {
			child.reset();
		}
	}

	protected boolean isHovered(float mouseX, float mouseY) {
		return enabled && visible && contains(mouseX, mouseY);
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiTheme.java`

```java
package dev.chaosutils.gui;

import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Render;

/** Cached palette derived from the theme settings; rebuilt only when a value changes. */
public final class UiTheme {
	public final int accent;
	public final int accentSoft;
	public final int background;
	public final int panel;
	public final int panelAlt;
	public final int panelHover;
	public final int outline;
	public final int text;
	public final int textDim;
	public final int textFaint;
	public final int positive;
	public final int negative;
	public final int warning;
	public final float radius;
	public final float animSpeed;
	public final boolean tooltips;
	public final boolean sounds;
	public final boolean blur;
	public final int backdropStyle;
	public final float sidebarWidth;
	public final boolean compact;
	public final boolean keybindHints;

	private static UiTheme cached;

	private UiTheme() {
		accent = ThemeModule.accent.get();
		accentSoft = Render.alpha(accent, 0.22F);
		int tint = ThemeModule.backgroundColor.get();
		float opacity = ThemeModule.backgroundOpacity.getFloat();
		background = Render.alpha(tint, opacity);
		panel = 0xF0171721;
		panelAlt = 0xF01D1D2A;
		panelHover = 0xF0252536;
		outline = 0x40FFFFFF;
		text = 0xFFF2F2F7;
		textDim = 0xFF9E9EB3;
		textFaint = 0xFF6C6C80;
		positive = 0xFF63D471;
		negative = 0xFFE05B5B;
		warning = 0xFFF0B429;
		radius = ThemeModule.cornerRadius.getFloat();
		animSpeed = ThemeModule.animationSpeed.getFloat();
		tooltips = ThemeModule.tooltips.get();
		sounds = ThemeModule.guiSounds.get();
		blur = ThemeModule.blur.get();
		backdropStyle = ThemeModule.backgroundStyle.get();
		sidebarWidth = ThemeModule.sidebarWidth.getFloat();
		compact = ThemeModule.compactCards.get();
		keybindHints = ThemeModule.showKeybindHints.get();
	}

	public static UiTheme get() {
		if (cached == null) {
			cached = new UiTheme();
		}
		return cached;
	}

	public static void invalidate() {
		cached = null;
	}

	/** Multiplier for animation speeds, respecting the user's preference. */
	public float speed(float base) {
		return base * animSpeed;
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiWidgets.java`

```java
package dev.chaosutils.gui;

import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.InputUtil;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/** The ChaosUtils widget toolkit: every setting type has an animated control here. */
public final class UiWidgets {
	private UiWidgets() {
	}

	private static Font fontOf(ChaosScreen screen) {
		return screen.font();
	}

	private static void label(GuiGraphics graphics, ChaosScreen screen, UiComponent component, String text, int color) {
		Render.text(graphics, fontOf(screen), text, component.x(), component.y() + (component.height() - 8.0F) * 0.5F - 0.5F, color, false);
	}

	private static void playClick(ChaosScreen screen, boolean on) {
		screen.playClick(on);
	}

	// -------------------------------------------------------------------- button

	public static final class Button extends UiComponent {
		private final ChaosScreen screen;
		private final String label;
		private final Runnable action;
		private final int accent;
		private boolean pressed;

		public Button(ChaosScreen screen, String label, int accent, Runnable action) {
			this.screen = screen;
			this.label = label;
			this.accent = accent;
			this.action = action;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			float radius = theme.radius * 0.7F;
			float drawY = pressed ? y + 1.0F : y;
			int color = Render.mix(theme.panelAlt, Render.brighten(accent, 0.25F), hover.get() * 0.85F);
			Render.roundedRect(graphics, x, drawY, width, height, radius, color);
			Render.roundedBorder(graphics, x, drawY, width, height, radius, 1.0F, Render.alpha(accent, 0.45F), color);
			int textColor = Render.mix(theme.text, 0xFFFFFFFF, hover.get());
			Render.centeredText(graphics, fontOf(screen), label, x + width * 0.5F, drawY + (height - 8.0F) * 0.5F, textColor, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 0 && isHovered(mouseX, mouseY)) {
				pressed = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (pressed) {
				pressed = false;
				if (isHovered(mouseX, mouseY)) {
					playClick(screen, true);
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	// -------------------------------------------------------------------- toggle

	public static final class Toggle extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Toggle setting;

		public Toggle(ChaosScreen screen, Setting.Toggle setting) {
			this.screen = screen;
			this.setting = setting;
			this.active.snap(setting.get() ? 1.0F : 0.0F);
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			active.set(setting.get() ? 1.0F : 0.0F);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float switchWidth = 28.0F;
			float switchHeight = 14.0F;
			float sx = x + width - switchWidth;
			float sy = y + (height - switchHeight) * 0.5F;
			float on = active.get();
			int track = Render.mix(0xFF3A3A48, theme.accent, on);
			Render.roundedRect(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.roundedBorder(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(0xFFFFFFFF, 0.10F + hover.get() * 0.15F), track);
			float knobSize = switchHeight - 4.0F;
			float knobX = sx + 2.0F + on * (switchWidth - knobSize - 4.0F);
			Render.roundedRect(graphics, knobX, sy + 2.0F, knobSize, knobSize, knobSize * 0.5F,
					Render.mix(0xFFBFC2CF, 0xFFFFFFFF, on));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 0 && isHovered(mouseX, mouseY)) {
				setting.toggle();
				playClick(screen, setting.get());
				return true;
			}
			return false;
		}
	}

	// -------------------------------------------------------------------- slider

	public static final class Slider extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Number setting;
		private boolean dragging;

		public Slider(ChaosScreen screen, Setting.Number setting) {
			this.screen = screen;
			this.setting = setting;
		}

		private float trackWidth() {
			return width - 84.0F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float tw = Math.max(20.0F, trackWidth());
			float tx = x + width - tw;
			float fraction = (float) setting.fraction();
			String value = setting.display();
			int valueWidth = fontOf(screen).width(value);
			Render.text(graphics, fontOf(screen), value, x + width - tw - valueWidth - 8.0F,
					y + (height - 8.0F) * 0.5F - 0.5F, Render.mix(theme.textFaint, theme.text, hover.get()), false);
			float ty = y + height * 0.5F - 2.0F;
			Render.roundedRect(graphics, tx, ty, tw, 4.0F, 2.0F, 0xFF3A3A48);
			Render.roundedRect(graphics, tx, ty, tw * fraction, 4.0F, 2.0F, theme.accent);
			float knobSize = dragging || hover.get() > 0.4F ? 11.0F : 9.0F;
			float knobX = tx + tw * fraction;
			Render.roundedRect(graphics, knobX - knobSize * 0.5F, y + height * 0.5F - knobSize * 0.5F, knobSize, knobSize,
					knobSize * 0.5F, Render.brighten(theme.accent, 0.35F));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				playClick(screen, false);
				return true;
			}
			if (button == 0) {
				apply(mouseX);
				dragging = true;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (dragging) {
				apply(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (dragging) {
				dragging = false;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				setting.nudge(amount > 0 ? 1 : -1);
				return true;
			}
			return false;
		}

		private void apply(float mouseX) {
			float tw = Math.max(20.0F, trackWidth());
			float tx = x + width - tw;
			setting.setFraction((mouseX - tx) / tw);
		}
	}

	// -------------------------------------------------------------------- choice

	public static final class Choice extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Choice setting;
		private final Anim.Value flash = new Anim.Value(0.0F, 8.0F);

		public Choice(ChaosScreen screen, Setting.Choice setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.display();
			float boxWidth = Math.max(74.0F, fontOf(screen).width(value) + 24.0F);
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			background = Render.mix(background, theme.accent, flash.get() * 0.35F);
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), value, bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
			Render.text(graphics, fontOf(screen), "‹", bx + 6.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.textFaint, false);
			Render.text(graphics, fontOf(screen), "›", bx + boxWidth - 11.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.textFaint, false);
			flash.update(deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && (button == 0 || button == 1)) {
				setting.cycle(button == 0 ? 1 : -1);
				flash.snap(1.0F);
				flash.set(0.0F);
				playClick(screen, true);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (isHovered(mouseX, mouseY)) {
				setting.cycle(amount > 0 ? 1 : -1);
				return true;
			}
			return false;
		}
	}

	// --------------------------------------------------------------------- color

	public static final class ColorField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Color setting;

		public ColorField(ChaosScreen screen, Setting.Color setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			int color = setting.get();
			Render.roundedRect(graphics, bx + 4.0F, y + 3.0F, height - 6.0F, height - 6.0F, (height - 6.0F) * 0.3F, color | 0xFF000000);
			Render.text(graphics, fontOf(screen), String.format("#%06X", color & 0xFFFFFF), bx + height + 1.0F,
					y + (height - 8.0F) * 0.5F - 0.5F, theme.textDim, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openColorPicker(setting);
					playClick(screen, true);
				}
				return true;
			}
			return false;
		}
	}

	// ----------------------------------------------------------------------- key

	public static final class KeyField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Key setting;
		private int lastPolled = InputUtil.NO_KEY;

		public KeyField(ChaosScreen screen, Setting.Key setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (!setting.isListening()) {
				lastPolled = InputUtil.NO_KEY;
				return;
			}
			int pressed = InputUtil.pollNewInput(lastPolled);
			lastPolled = InputUtil.currentlyHeld();
			if (pressed == InputUtil.NO_KEY) {
				return;
			}
			if (pressed == GLFW.GLFW_KEY_ESCAPE || pressed == GLFW.GLFW_KEY_BACKSPACE) {
				setting.stopListening();
			} else if (pressed == GLFW.GLFW_KEY_DELETE) {
				setting.set(InputUtil.NO_KEY);
			} else {
				setting.set(pressed);
			}
			playClick(screen, true);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.isListening() ? "press a key…" : setting.display();
			float boxWidth = Math.max(70.0F, fontOf(screen).width(value) + 18.0F);
			float bx = x + width - boxWidth;
			int accent = setting.isListening() ? theme.warning : theme.accent;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.roundedBorder(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, 1.0F, Render.alpha(accent, 0.7F), background);
			Render.centeredText(graphics, fontOf(screen), value, bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				setting.listen();
				return true;
			}
			return false;
		}
	}

	// ------------------------------------------------------------------ position

	public static final class PositionField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Position setting;

		public PositionField(ChaosScreen screen, Setting.Position setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, theme.panelHover, hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), "Adjust", bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
			float px = bx + 6.0F + setting.get().x() * 8.0F;
			float py = y + height - 5.0F - setting.get().y() * 4.0F;
			Render.rect(graphics, px, py, 3.0F, 3.0F, theme.accent);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openHudEditor();
				}
				return true;
			}
			return false;
		}
	}

	// --------------------------------------------------------------------- action

	public static final class ActionField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Action setting;

		public ActionField(ChaosScreen screen, Setting.Action setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			float boxWidth = 78.0F;
			float bx = x + width - boxWidth;
			int background = Render.mix(theme.panelAlt, Render.brighten(theme.accent, 0.1F), hover.get());
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, background);
			Render.centeredText(graphics, fontOf(screen), "Run", bx + boxWidth * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				playClick(screen, true);
				setting.run();
				return true;
			}
			return false;
		}
	}

	// ---------------------------------------------------------------------- text

	public static final class TextField extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Text setting;

		public TextField(ChaosScreen screen, Setting.Text setting) {
			this.screen = screen;
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			label(graphics, screen, this, setting.label, Render.mix(theme.textDim, theme.text, hover.get()));
			String value = setting.display();
			float boxWidth = Math.min(width * 0.55F, 160.0F);
			float bx = x + width - boxWidth;
			Render.roundedRect(graphics, bx, y, boxWidth, height, theme.radius * 0.6F, Render.mix(theme.panelAlt, theme.panelHover, hover.get()));
			float maxText = boxWidth - 10.0F;
			while (fontOf(screen).width(value) > maxText && value.length() > 3) {
				value = value.substring(0, value.length() - 2) + "…";
			}
			Render.text(graphics, fontOf(screen), value, bx + 5.0F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY)) {
				if (button == 1) {
					setting.reset();
				} else {
					screen.openTextEditor(setting);
				}
				return true;
			}
			return false;
		}
	}

	/** Copies fixed text to the clipboard; used for generated values (coordinates, IPs). */
	public static final class CopyField extends UiComponent {
		private final ChaosScreen screen;
		private final java.util.function.Supplier<String> textSupplier;

		public CopyField(ChaosScreen screen, String label, java.util.function.Supplier<String> textSupplier) {
			this.screen = screen;
			this.textSupplier = textSupplier;
			this.setTooltip(label);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.roundedRect(graphics, x, y, width, height, theme.radius * 0.6F, Render.mix(theme.panelAlt, theme.panelHover, hover.get()));
			Render.centeredText(graphics, fontOf(screen), "Copy", x + width * 0.5F, y + (height - 8.0F) * 0.5F - 0.5F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				Clipboard.copyText(textSupplier.get());
				playClick(screen, true);
				return true;
			}
			return false;
		}
	}

	/** Creates the control matching a setting, laid out for a settings row. */
	public static UiComponent forSetting(Setting<?> setting, ChaosScreen screen) {
		return switch (setting.kind()) {
			case TOGGLE -> new Toggle(screen, (Setting.Toggle) setting);
			case NUMBER -> new Slider(screen, (Setting.Number) setting);
			case CHOICE -> new Choice(screen, (Setting.Choice) setting);
			case COLOR -> new ColorField(screen, (Setting.Color) setting);
			case KEY -> new KeyField(screen, (Setting.Key) setting);
			case POSITION -> new PositionField(screen, (Setting.Position) setting);
			case TEXT -> new TextField(screen, (Setting.Text) setting);
			case ACTION -> new ActionField(screen, (Setting.Action) setting);
		};
	}

	// ------------------------------------------------------------------- popups

	/** Modal editor for text settings, reusing a single {@link net.minecraft.client.gui.components.EditBox}. */
	public static final class TextPopup extends UiComponent {
		private final ChaosScreen screen;
		private float buttonY;

		public TextPopup(ChaosScreen screen) {
			this.screen = screen;
		}

		public void layout(float px, float py, float width, float height, net.minecraft.client.gui.components.EditBox input) {
			setBounds(px, py, width, height);
			if (input != null) {
				input.setX(Math.round(px + 14.0F));
				input.setY(Math.round(py + 34.0F));
				input.setWidth(Math.round(width - 28.0F));
			}
			buttonY = py + height - 30.0F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.shadowedPanel(graphics, x, y, width, height, theme.radius, theme.panel, theme.accent);
			Render.roundedRect(graphics, x + 14.0F, y + 32.0F, width - 28.0F, 20.0F, 4.0F, 0xFF0E0E14);
			renderButton(graphics, mouseX, mouseY, "Accept", x + width - 176.0F, buttonY, theme.accent);
			renderButton(graphics, mouseX, mouseY, "Cancel", x + width - 88.0F, buttonY, 0xFF4A4A5A);
		}

		private void renderButton(GuiGraphics graphics, float mouseX, float mouseY, String label, float bx, float by, int accent) {
			UiTheme theme = UiTheme.get();
			boolean hovered = mouseX >= bx && mouseX <= bx + 80.0F && mouseY >= by && mouseY <= by + 20.0F;
			int color = Render.mix(theme.panelAlt, Render.brighten(accent, 0.2F), hovered ? 0.9F : 0.15F);
			Render.roundedRect(graphics, bx, by, 80.0F, 20.0F, 5.0F, color);
			Render.centeredText(graphics, screen.font(), label, bx + 40.0F, by + 6.0F, theme.text, false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button != 0) {
				return false;
			}
			if (mouseX >= x + width - 176.0F && mouseX <= x + width - 96.0F && mouseY >= buttonY && mouseY <= buttonY + 20.0F) {
				screen.closePopup(true);
				return true;
			}
			if (mouseX >= x + width - 88.0F && mouseX <= x + width - 8.0F && mouseY >= buttonY && mouseY <= buttonY + 20.0F) {
				screen.closePopup(false);
				return true;
			}
			return contains(mouseX, mouseY);
		}
	}

	/** Compact HSV + alpha picker used for every colour setting. */
	public static final class ColorPicker extends UiComponent {
		private final ChaosScreen screen;
		private final Setting.Color setting;
		private float hue;
		private float saturation;
		private float brightness;
		private float alpha;
		private int draggingRow = -1;
		private float rowX;
		private float rowWidth;
		private float firstRowY;
		private float rowHeight;
		private float rowGap;

		public ColorPicker(ChaosScreen screen, Setting.Color setting) {
			this.screen = screen;
			this.setting = setting;
			int color = setting.get();
			float[] hsv = Render.toHsv(color);
			this.hue = hsv[0];
			this.saturation = hsv[1];
			this.brightness = hsv[2];
			this.alpha = Render.alphaOf(color) / 255.0F;
		}

		public void layout(float px, float py, float width, float height) {
			setBounds(px, py, width, height);
			this.rowX = px + 14.0F;
			this.rowWidth = width - 28.0F;
			this.firstRowY = py + 34.0F;
			this.rowHeight = 12.0F;
			this.rowGap = 8.0F;
		}

		private int argb() {
			return Render.fromHsv(hue, saturation, brightness) & 0xFFFFFF | (Math.round(alpha * 255.0F) << 24);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			UiTheme theme = UiTheme.get();
			Render.shadowedPanel(graphics, x, y, width, height, theme.radius, theme.panel, theme.accent);
			renderRow(graphics, 0, "Hue", hue, 0.0F, 1.0F);
			renderRow(graphics, 1, "Saturation", saturation, 0.0F, 1.0F);
			renderRow(graphics, 2, "Brightness", brightness, 0.0F, 1.0F);
			if (setting.alphaAllowed()) {
				renderRow(graphics, 3, "Opacity", alpha, 0.0F, 1.0F);
			}
			float previewY = y + height - 34.0F;
			Render.roundedRect(graphics, x + 14.0F, previewY, width - 100.0F, 20.0F, 5.0F, argb());
			Render.text(graphics, screen.font(), String.format("#%08X", argb()), x + 20.0F, previewY + 6.0F, 0xFFF2F2F7, true);
			boolean hovered = mouseX >= x + width - 80.0F && mouseX <= x + width - 8.0F && mouseY >= previewY && mouseY <= previewY + 20.0F;
			int buttonColor = Render.mix(theme.panelAlt, theme.accent, hovered ? 0.75F : 0.2F);
			Render.roundedRect(graphics, x + width - 80.0F, previewY, 72.0F, 20.0F, 5.0F, buttonColor);
			Render.centeredText(graphics, screen.font(), "Done", x + width - 44.0F, previewY + 6.0F, theme.text, false);
		}

		private void renderRow(GuiGraphics graphics, int index, String label, float value, float min, float max) {
			UiTheme theme = UiTheme.get();
			float rowY = firstRowY + index * (rowHeight + rowGap);
			Render.text(graphics, screen.font(), label, rowX, rowY - 1.0F, theme.textDim, false);
			float trackX = rowX + 76.0F;
			float trackWidth = rowWidth - 76.0F;
			// Gradient track composed of strips - cheap and shader free.
			int strips = 24;
			for (int i = 0; i < strips; i++) {
				float t = i / (float) strips;
				int color = switch (index) {
					case 0 -> Render.fromHsv(t, 0.9F, 1.0F);
					case 1 -> Render.fromHsv(hue, t, brightness);
					case 2 -> Render.fromHsv(hue, saturation, t);
					default -> Render.fromHsv(hue, saturation, brightness) & 0xFFFFFF | (Math.round(t * 255.0F) << 24);
				};
				Render.rect(graphics, trackX + t * trackWidth, rowY, trackWidth / strips + 1.0F, rowHeight, color);
			}
			float knob = (value - min) / (max - min);
			float knobX = trackX + knob * trackWidth;
			Render.roundedRect(graphics, knobX - 2.0F, rowY - 2.0F, 4.0F, rowHeight + 4.0F, 2.0F, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!contains(mouseX, mouseY) || button != 0) {
				return false;
			}
			float previewY = y + height - 34.0F;
			if (mouseY >= previewY && mouseY <= previewY + 20.0F && mouseX >= x + width - 80.0F) {
				screen.closePopup(true);
				return true;
			}
			for (int index = 0; index < 4; index++) {
				float rowY = firstRowY + index * (rowHeight + rowGap);
				if (mouseY >= rowY - 3.0F && mouseY <= rowY + rowHeight + 3.0F) {
					draggingRow = index;
					applyDrag(mouseX);
					return true;
				}
			}
			return true;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (draggingRow >= 0) {
				applyDrag(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (draggingRow >= 0) {
				draggingRow = -1;
				commit();
				return true;
			}
			return false;
		}

		private void applyDrag(float mouseX) {
			float trackX = rowX + 76.0F;
			float trackWidth = rowWidth - 76.0F;
			float value = Anim.clamp01((mouseX - trackX) / trackWidth);
			switch (draggingRow) {
				case 0 -> hue = value;
				case 1 -> saturation = value;
				case 2 -> brightness = value;
				case 3 -> alpha = value;
				default -> {
				}
			}
			commit();
		}

		private void commit() {
			setting.set(argb());
		}
	}
}
```

---

## HUD features

### `src/main/java/dev/chaosutils/feature/hud/ArmorStatusHud.java`

```java
package dev.chaosutils.feature.hud;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.EnchantLookup;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;

/**
 * Armour and held item status HUD.
 *
 * <p>Shows every equipped piece with its durability and the enchantments in short form
 * ("Prot IV", "Unbr III"), plus the two hands. It is freely positionable and can be laid out
 * horizontally or vertically, which makes it a natural companion to the vanilla hotbar.
 */
public final class ArmorStatusHud implements Feature {
	public static final String ID = "armor_hud";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Toggle vertical;
	private static Setting.Toggle showArmor;
	private static Setting.Toggle showHands;
	private static Setting.Toggle showDurabilityBar;
	private static Setting.Toggle showDurabilityNumbers;
	private static Setting.Toggle showEnchants;
	private static Setting.Toggle showEmptySlots;
	private static Setting.Toggle maxEnchants;
	private static Setting.Number scale;
	private static Setting.Number spacing;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Armour & Hand Status",
				"Equipped armour, both hands, durability and enchantments at a glance.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the equipment panel sits.", HudPos.BOTTOM_LEFT.x(), HudPos.BOTTOM_LEFT.y()));
		vertical = (Setting.Toggle) module.add(new Setting.Toggle("vertical", "Vertical layout",
				"Stack the slots vertically instead of in a row.", false));
		showArmor = (Setting.Toggle) module.add(new Setting.Toggle("armor", "Show armour",
				"Helmet, chestplate, leggings and boots.", true));
		showHands = (Setting.Toggle) module.add(new Setting.Toggle("hands", "Show hands",
				"Main hand and off hand.", true));
		showDurabilityBar = (Setting.Toggle) module.add(new Setting.Toggle("bars", "Durability bar",
				"Thin bar under each item.", true));
		showDurabilityNumbers = (Setting.Toggle) module.add(new Setting.Toggle("numbers", "Durability numbers",
				"Remaining uses, e.g. 231.", false));
		showEnchants = (Setting.Toggle) module.add(new Setting.Toggle("enchantments", "Enchantment names",
				"Short names of every enchantment on the item.", true));
		showEmptySlots = (Setting.Toggle) module.add(new Setting.Toggle("empty", "Show empty slots",
				"Draw a placeholder frame for empty equipment slots.", false));
		maxEnchants = (Setting.Toggle) module.add(new Setting.Toggle("compact_enchants", "Compact enchantments",
				"Only the first two enchantments per item.", true));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the panel.", 1.0, 0.6, 1.6, 0.05, "x"));
		spacing = (Setting.Number) module.add(new Setting.Number("spacing", "Slot spacing",
				"Space between the slots.", 20.0, 16.0, 40.0, 1.0, "px"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || !HudPanel.visibleNow()) {
			return;
		}
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		float slotSpacing = spacing.getFloat() * scaleFactor;
		List<ItemStack> stacks = new ArrayList<>(6);
		if (showArmor.get()) {
			stacks.add(player.getItemBySlot(EquipmentSlot.HEAD));
			stacks.add(player.getItemBySlot(EquipmentSlot.CHEST));
			stacks.add(player.getItemBySlot(EquipmentSlot.LEGS));
			stacks.add(player.getItemBySlot(EquipmentSlot.FEET));
		}
		if (showHands.get()) {
			stacks.add(player.getMainHandItem());
			stacks.add(player.getOffhandItem());
		}
		if (stacks.isEmpty()) {
			return;
		}
		Font font = client.font;
		float padding = HudPanel.padding();
		float iconSize = 16.0F * scaleFactor;
		float labelHeight = showEnchants.get() ? 18.0F * scaleFactor : 9.0F * scaleFactor;
		float boxWidth;
		float boxHeight;
		if (vertical.get()) {
			boxWidth = iconSize + 6.0F + 54.0F * scaleFactor + padding * 2.0F;
			boxHeight = stacks.size() * Math.max(slotSpacing, labelHeight) + padding * 2.0F;
		} else {
			boxWidth = stacks.size() * slotSpacing + padding * 2.0F;
			boxHeight = Math.max(iconSize, labelHeight) + padding * 2.0F;
		}
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(boxHeight));
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF64B5F6);

		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			float slotX = vertical.get() ? x + padding : x + padding + i * slotSpacing;
			float slotY = vertical.get() ? y + padding + i * Math.max(slotSpacing, labelHeight) : y + padding;
			if (stack.isEmpty()) {
				if (showEmptySlots.get()) {
					Render.roundedBorder(graphics, slotX, slotY, iconSize, iconSize, 2.0F, 1.0F, 0x30FFFFFF, 0x00000000);
				}
				continue;
			}
			Render.item(graphics, stack, slotX, slotY);
			float textX = vertical.get() ? slotX + iconSize + 4.0F : slotX;
			float textY = vertical.get() ? slotY + 1.0F : y + padding + iconSize - 2.0F;
			String line = enchantLine(stack);
			if (!vertical.get() && showEnchants.get() && !line.isEmpty()) {
				HudPanel.text(graphics, font, line, textX, y + padding + iconSize + 1.0F, 0xFFBFC2CF);
			} else if (vertical.get() && showEnchants.get() && !line.isEmpty()) {
				HudPanel.text(graphics, font, line, textX, textY + 10.0F, 0xFFBFC2CF);
			}
			if (showDurabilityBar.get() && stack.isDamageableItem()) {
				float fraction = remainingFraction(stack);
				float barY = slotY + iconSize - 1.0F;
				float barWidth = iconSize;
				Render.rect(graphics, slotX, barY, barWidth, 2.0F * scaleFactor, 0x88000000);
				Render.rect(graphics, slotX, barY, barWidth * fraction, 2.0F * scaleFactor, HudPanel.dangerColor(fraction));
				if (showDurabilityNumbers.get()) {
					String remaining = Integer.toString(Math.max(0, stack.getMaxDamage() - stack.getDamageValue()));
					HudPanel.text(graphics, font, remaining, slotX + iconSize + 3.0F, slotY + 1.0F, 0xFFBFC2CF);
				}
			}
		}
	}

	private static String enchantLine(ItemStack stack) {
		if (!showEnchants.get()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(24);
		int shown = 0;
		int limit = maxEnchants.get() ? 2 : Integer.MAX_VALUE;
		try {
			var enchantments = stack.getEnchantments();
			for (Holder<Enchantment> enchantment : enchantments.keySet()) {
				if (shown >= limit) {
					builder.append("…");
					break;
				}
				int level = enchantments.getLevel(enchantment);
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(EnchantLookup.shortName(enchantment)).append(' ').append(EnchantLookup.level(level));
				shown++;
			}
		} catch (Throwable ignored) {
			return "";
		}
		return builder.toString();
	}

	private static float remainingFraction(ItemStack stack) {
		int max = Math.max(1, stack.getMaxDamage());
		int remaining = max - stack.getDamageValue();
		return Math.max(0.0F, Math.min(1.0F, remaining / (float) max));
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/CompactDebugOverlay.java`

```java
package dev.chaosutils.feature.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.HudPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

/**
 * Compact F3 replacement.
 *
 * <p>Instead of twenty lines of text it shows the things you actually read while playing:
 * coordinates, facing direction, biome, chunk, light level, the time of day (plus the real
 * time) and the current dimension. The vanilla overlay is suppressed through the debug
 * overlay hook and can be brought back with a single setting. Holding the copy key puts the
 * coordinates on the clipboard.
 */
public final class CompactDebugOverlay implements Feature {
	public static final String ID = "compact_debug";

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
	private static final String[] COMPASS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

	private static Module module;
	private static Setting.Position position;
	private static Setting.Toggle replaceVanilla;
	private static Setting.Toggle alwaysShow;
	private static Setting.Toggle showCoords;
	private static Setting.Toggle showPreciseCoords;
	private static Setting.Toggle showFacing;
	private static Setting.Toggle showBiome;
	private static Setting.Toggle showChunk;
	private static Setting.Toggle showLight;
	private static Setting.Toggle showTime;
	private static Setting.Toggle showRealTime;
	private static Setting.Toggle showDimension;
	private static Setting.Toggle showFps;
	private static Setting.Toggle showServer;
	private static Setting.Toggle showCopyHint;
	private static Setting.Number scale;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Compact Coordinates",
				"A compact F3 panel with coordinates, direction, biome, light and time.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the panel is drawn.", HudPos.TOP_LEFT.x(), HudPos.TOP_LEFT.y()));
		replaceVanilla = (Setting.Toggle) module.add(new Setting.Toggle("replace", "Replace vanilla F3",
				"Hide the long vanilla debug text and show this panel instead.", true));
		alwaysShow = (Setting.Toggle) module.add(new Setting.Toggle("always", "Always visible",
				"Show the panel even when F3 is closed.", false));
		showCoords = (Setting.Toggle) module.add(new Setting.Toggle("coords", "Coordinates",
				"X / Y / Z of your feet.", true));
		showPreciseCoords = (Setting.Toggle) module.add(new Setting.Toggle("precise", "Precise coordinates",
				"Two decimals instead of rounded blocks.", false));
		showFacing = (Setting.Toggle) module.add(new Setting.Toggle("facing", "Facing",
				"Compass direction plus the exact yaw/pitch.", true));
		showBiome = (Setting.Toggle) module.add(new Setting.Toggle("biome", "Biome",
				"Name of the biome you are standing in.", true));
		showChunk = (Setting.Toggle) module.add(new Setting.Toggle("chunk", "Chunk",
				"Chunk coordinates and your position inside the chunk.", false));
		showLight = (Setting.Toggle) module.add(new Setting.Toggle("light", "Light level",
				"Block and sky light at your position.", false));
		showTime = (Setting.Toggle) module.add(new Setting.Toggle("time", "Time of day",
				"In game clock derived from the world time.", true));
		showRealTime = (Setting.Toggle) module.add(new Setting.Toggle("real_time", "Real time",
				"Your local wall clock time.", false));
		showDimension = (Setting.Toggle) module.add(new Setting.Toggle("dimension", "Dimension",
				"Which dimension you are in.", true));
		showFps = (Setting.Toggle) module.add(new Setting.Toggle("fps", "FPS", "Frame rate.", false));
		showServer = (Setting.Toggle) module.add(new Setting.Toggle("server", "Server",
				"Address of the server you are on.", false));
		showCopyHint = (Setting.Toggle) module.add(new Setting.Toggle("copy_hint", "Copy hint",
				"Remind about the copy coordinates key.", false));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of this panel.", 1.0, 0.6, 1.8, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	/** True when the vanilla debug overlay should be suppressed. */
	public static boolean replacesVanilla() {
		if (!ModuleManager.enabled(ID) || !replaceVanilla.get()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.level != null;
	}

	@Override
	public void onTick(Minecraft client) {
		if (Keybinds.copyCoordinates != null && Keybinds.copyCoordinates.wasPressed() && client.player != null) {
			Player player = client.player;
			Clipboard.copyText(String.format(Locale.ROOT, "%.1f %.1f %.1f", player.getX(), player.getY(), player.getZ()));
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || !HudPanel.visibleNow()) {
			return;
		}
		boolean debugOpen = client.getDebugOverlay() != null && client.getDebugOverlay().showDebugScreen();
		if (debugOpen) {
			// Drawn by the debug overlay hook instead, exactly where F3 belongs.
			return;
		}
		if (!alwaysShow.get()) {
			return;
		}
		draw(graphics, client);
	}

	/**
	 * Called from the debug overlay hook: replaces the vanilla text when that setting is on,
	 * and otherwise adds the compact panel next to it so nothing is lost.
	 */
	public static void renderExtra(GuiGraphics graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || !ModuleManager.enabled(ID)) {
			return;
		}
		if (client.getDebugOverlay() == null || !client.getDebugOverlay().showDebugScreen()) {
			return;
		}
		draw(graphics, client);
	}

	private static void draw(GuiGraphics graphics, Minecraft client) {
		Font font = client.font;
		Player player = client.player;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		float lineHeight = 10.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		int lines = 0;

		String coords = null;
		if (showCoords.get()) {
			if (showPreciseCoords.get()) {
				coords = String.format(Locale.ROOT, "XYZ %.2f / %.2f / %.2f", player.getX(), player.getY(), player.getZ());
			} else {
				coords = "XYZ " + (int) Math.floor(player.getX()) + " / " + (int) Math.floor(player.getY()) + " / "
						+ (int) Math.floor(player.getZ());
			}
		}
		String facing = showFacing.get() ? "Facing " + compass(player.getYRot()) + "  (" + String.format(Locale.ROOT, "%.0f / %.0f", player.getYRot(), player.getXRot()) + ")" : null;
		String dimension = showDimension.get() && client.level != null
				? "Dim " + client.level.dimension().location().getPath() : null;
		String time = null;
		if (showTime.get() && client.level != null) {
			long dayTime = client.level.getDayTime() % 24000L;
			long hours = (dayTime / 1000L + 6L) % 24L;
			long minutes = (long) ((dayTime % 1000L) / 1000.0 * 60.0);
			time = String.format(Locale.ROOT, "Time %02d:%02d  day %d", hours, minutes, client.level.getDayTime() / 24000L);
		}
		String realTime = showRealTime.get() ? "Clock " + LocalTime.now().format(TIME_FORMAT) : null;
		String chunk = null;
		if (showChunk.get()) {
			BlockPos pos = player.blockPosition();
			chunk = "Chunk " + (pos.getX() >> 4) + " / " + (pos.getZ() >> 4)
					+ "  in " + (pos.getX() & 15) + "/" + (pos.getZ() & 15);
		}
		String light = null;
		if (showLight.get() && client.level != null) {
			BlockPos pos = player.blockPosition();
			light = "Light " + client.level.getBrightness(LightLayer.BLOCK, pos) + " / "
					+ client.level.getBrightness(LightLayer.SKY, pos);
		}
		String biome = null;
		if (showBiome.get() && client.level != null) {
			try {
				biome = "Biome " + client.level.getBiome(player.blockPosition()).unwrapKey()
						.map(key -> prettify(key.location().getPath()))
						.orElse("unknown");
			} catch (Throwable ignored) {
				biome = null;
			}
		}
		String fps = showFps.get() ? client.getFps() + " fps" : null;
		String server = null;
		if (showServer.get()) {
			server = client.getCurrentServer() != null ? "Server " + client.getCurrentServer().ip : "Singleplayer";
		}
		String hint = showCopyHint.get() && Keybinds.copyCoordinates != null && !Keybinds.copyCoordinates.isUnbound()
				? "Copy: " + Keybinds.copyCoordinates.getTranslatedKeyMessage().getString() : null;

		String[] values = {coords, facing, biome, dimension, chunk, light, time, realTime, server, fps, hint};
		for (String value : values) {
			if (value != null) {
				lines++;
			}
		}
		if (lines == 0) {
			return;
		}
		float width = 0.0F;
		for (String value : values) {
			if (value != null) {
				width = Math.max(width, font.width(value));
			}
		}
		float boxWidth = width * scaleFactor + padding * 2.0F;
		float boxHeight = lines * lineHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(boxHeight));
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF4FC3F7);
		float cursorY = y + padding;
		for (String value : values) {
			if (value == null) {
				continue;
			}
			HudPanel.text(graphics, font, value, x + padding, cursorY, 0xFFF2F2F7);
			cursorY += lineHeight;
		}
		builder.setLength(0);
	}

	private static String compass(float yaw) {
		float normalized = yaw % 360.0F;
		if (normalized < 0.0F) {
			normalized += 360.0F;
		}
		int index = Math.round(normalized / 45.0F) % 8;
		return COMPASS[index] + " (" + String.format(Locale.ROOT, "%.0f°", normalized) + ")";
	}

	private static String prettify(String path) {
		String[] parts = path.split("_");
		StringBuilder pretty = new StringBuilder(path.length());
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (pretty.length() > 0) {
				pretty.append(' ');
			}
			pretty.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return pretty.toString();
	}

}
```

### `src/main/java/dev/chaosutils/feature/hud/DurabilityHud.java`

```java
package dev.chaosutils.feature.hud;

import java.util.ArrayList;
import java.util.List;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Item durability status &amp; break warning.
 *
 * <p>Your inventory is scanned on a slow interval (ten ticks by default), not every frame,
 * and only items below the warning threshold are kept in an already allocated list - so the
 * overlay costs nothing while everything is intact. When something is about to break the
 * panel appears, the icon flashes and (optionally) a soft click sound plays once.
 */
public final class DurabilityHud implements Feature {
	public static final String ID = "durability";

	private static final int MAX_ROWS = 6;

	private static Module module;
	private static Setting.Position position;
	private static Setting.Number warnPercent;
	private static Setting.Number warnAbsolute;
	private static Setting.Toggle includeArmor;
	private static Setting.Toggle includeTools;
	private static Setting.Toggle includeEverything;
	private static Setting.Toggle showIcons;
	private static Setting.Toggle showNumbers;
	private static Setting.Toggle warnSound;
	private static Setting.Number rescanInterval;
	private static Setting.Number scale;

	private static final List<Row> ROWS = new ArrayList<>(MAX_ROWS);
	private static int tickCounter;
	private static long lastSoundAt;

	private record Row(ItemStack stack, int remaining, int max) {
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Durability Warning",
				"Warns before a tool, weapon or armour piece breaks.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the warning panel appears.", HudPos.BOTTOM_RIGHT.x(), HudPos.BOTTOM_RIGHT.y()));
		warnPercent = (Setting.Number) module.add(new Setting.Number("percent", "Warn below",
				"Warning threshold as a percentage of the maximal durability.", 5.0, 1.0, 50.0, 1.0, "%"));
		warnAbsolute = (Setting.Number) module.add(new Setting.Number("absolute", "Also warn below",
				"Absolute number of remaining uses.", 12.0, 0.0, 100.0, 1.0, "uses"));
		includeArmor = (Setting.Toggle) module.add(new Setting.Toggle("armor", "Include armour",
				"Watch the four armour slots.", true));
		includeTools = (Setting.Toggle) module.add(new Setting.Toggle("tools", "Include tools & weapons",
				"Anything with a digging or attack speed.", true));
		includeEverything = (Setting.Toggle) module.add(new Setting.Toggle("everything", "Include everything",
				"Also watch rods, shears, elytra and every other damageable item.", true));
		showIcons = (Setting.Toggle) module.add(new Setting.Toggle("icons", "Show icons",
				"Draw the item icon for every warning.", true));
		showNumbers = (Setting.Toggle) module.add(new Setting.Toggle("numbers", "Show numbers",
				"Remaining uses in brackets.", true));
		warnSound = (Setting.Toggle) module.add(new Setting.Toggle("sound", "Warning sound",
				"A soft click when an item first drops below the threshold.", true));
		rescanInterval = (Setting.Number) module.add(new Setting.Number("interval", "Rescan interval",
				"Ticks between inventory scans (higher = cheaper).", 10.0, 2.0, 40.0, 1.0, "t"));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the warning panel.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			ROWS.clear();
			return;
		}
		if (--tickCounter > 0) {
			return;
		}
		tickCounter = Math.max(1, rescanInterval.getInt());
		scan(client.player);
	}

	private static void scan(Player player) {
		ROWS.clear();
		float percentLimit = warnPercent.getFloat() / 100.0F;
		int absoluteLimit = warnAbsolute.getInt();
		Inventory inventory = player.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size && ROWS.size() < MAX_ROWS; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty() || !stack.isDamageableItem()) {
				continue;
			}
			int max = stack.getMaxDamage();
			int remaining = max - stack.getDamageValue();
			if (remaining <= 0 || remaining > max) {
				continue;
			}
			boolean belowPercent = remaining <= Math.max(1, (int) (max * percentLimit));
			boolean belowAbsolute = absoluteLimit > 0 && remaining <= absoluteLimit;
			if (!belowPercent && !belowAbsolute) {
				continue;
			}
			if (!isWatched(stack, i)) {
				continue;
			}
			ROWS.add(new Row(stack, remaining, max));
		}
	}

	private static boolean isWatched(ItemStack stack, int slot) {
		boolean armorSlot = slot >= 36 && slot <= 39;
		if (armorSlot) {
			return includeArmor.get();
		}
		if (includeEverything.get()) {
			return true;
		}
		if (includeTools.get()) {
			return stack.isDamageableItem();
		}
		return false;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !HudPanel.visibleNow() || ROWS.isEmpty()) {
			return;
		}
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		Font font = client.font;
		float padding = HudPanel.padding() * scaleFactor;
		float rowHeight = 12.0F * scaleFactor;
		float iconSize = 16.0F * scaleFactor;
		float width = 76.0F * scaleFactor;
		for (Row row : ROWS) {
			width = Math.max(width, font.width(label(row)) * scaleFactor + iconSize + padding * 3.0F);
		}
		float boxWidth = width + padding;
		float boxHeight = ROWS.size() * rowHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(boxHeight));
		boolean critical = ROWS.get(0).remaining() <= 3;
		int accent = critical ? 0xFFE05B5B : 0xFFF0B429;
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, accent);

		float cursorY = y + padding;
		for (Row row : ROWS) {
			float fraction = row.remaining() / (float) Math.max(1, row.max());
			if (showIcons.get()) {
				Render.item(graphics, row.stack(), x + padding, cursorY - 2.0F);
			}
			float textX = showIcons.get() ? x + padding + iconSize : x + padding;
			HudPanel.text(graphics, font, label(row), textX, cursorY, 0xFFF2F2F7);
			HudPanel.bar(graphics, textX, cursorY + 9.0F * scaleFactor, 48.0F * scaleFactor, 2.0F * scaleFactor,
					fraction, HudPanel.dangerColor(fraction));
			cursorY += rowHeight;
		}
		if (warnSound.get() && critical && System.currentTimeMillis() - lastSoundAt > 15_000L) {
			lastSoundAt = System.currentTimeMillis();
			try {
				var sound = SoundLookup.ui(SoundLookup.uiClick(), 0.6F, 0.4F);
				if (sound != null) {
					client.getSoundManager().play(sound);
				}
			} catch (Throwable ignored) {
				// sound is optional
			}
		}
	}

	private static String label(Row row) {
		String name = row.stack().getHoverName().getString();
		if (name.length() > 18) {
			name = name.substring(0, 17) + "…";
		}
		return showNumbers.get() ? name + " (" + row.remaining() + ")" : name;
	}

	@Override
	public void onWorldLeave() {
		ROWS.clear();
	}

	@Override
	public void onDisabled() {
		ROWS.clear();
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/EntityHealthOverlay.java`

```java
package dev.chaosutils.feature.hud;

import java.util.HashMap;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.Features;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Dynamic entity health indicators.
 *
 * <p>The health of a tracked entity is part of vanilla's synced entity data, so this feature
 * reads exactly the value the game already received - no packet, no extra server load, and
 * therefore nothing an anti-cheat could ever object to. Bars are drawn in screen space above
 * each entity, animate smoothly and flash on damage; the numbers are the exact health and
 * maximum health.
 */
public final class EntityHealthOverlay implements Feature {
	public static final String ID = "health_indicators";

	private static final int COLOR_SELF = 0xFF63D471;
	private static final int COLOR_HOSTILE = 0xFFE05B5B;
	private static final int COLOR_PASSIVE = 0xFF63D471;
	private static final int COLOR_PLAYER = 0xFF64B5F6;
	private static final int COLOR_OTHER = 0xFFB388FF;

	private static Module module;
	private static Setting.Number range;
	private static Setting.Toggle showBars;
	private static Setting.Toggle showNumbers;
	private static Setting.Toggle showNames;
	private static Setting.Toggle showPercent;
	private static Setting.Toggle onlyDamaged;
	private static Setting.Toggle showSelf;
	private static Setting.Toggle showPlayers;
	private static Setting.Toggle showHostile;
	private static Setting.Toggle showPassive;
	private static Setting.Toggle showOthers;
	private static Setting.Number maxEntities;
	private static Setting.Number barWidth;
	private static Setting.Number barHeight;
	private static Setting.Number verticalOffset;
	private static Setting.Toggle damageFlash;
	private static Setting.Toggle damageNumbers;
	private static Setting.Choice colorMode;

	private static final Map<Integer, Tracked> TRACKED = new HashMap<>();

	private static final class Tracked {
		float displayed = -1.0F;
		float lastHealth = -1.0F;
		long lastDamageAt;
		float lastDamageAmount;
		float flash;
	}

	public EntityHealthOverlay() {
		Features.addEntityPruneListener(TRACKED::remove);
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Entity Health Bars",
				"Exact health bars and numbers above every mob, straight from vanilla's synced entity data.",
				Category.HUD, false));
		range = (Setting.Number) module.add(new Setting.Number("range", "Range",
				"Only entities within this distance get a bar.", 32.0, 4.0, 96.0, 1.0, "m"));
		showBars = (Setting.Toggle) module.add(new Setting.Toggle("bars", "Health bar",
				"Animated bar showing the health fraction.", true));
		showNumbers = (Setting.Toggle) module.add(new Setting.Toggle("numbers", "Exact numbers",
				"Show health / max health.", true));
		showPercent = (Setting.Toggle) module.add(new Setting.Toggle("percent", "Show percentage",
				"Additional percentage next to the numbers.", false));
		showNames = (Setting.Toggle) module.add(new Setting.Toggle("names", "Entity name",
				"Draw the name above the bar too.", false));
		onlyDamaged = (Setting.Toggle) module.add(new Setting.Toggle("only_damaged", "Only damaged",
				"Hide entities that are at full health.", false));
		showSelf = (Setting.Toggle) module.add(new Setting.Toggle("self", "Include yourself",
				"Also draw a bar on your own player model.", false));
		showPlayers = (Setting.Toggle) module.add(new Setting.Toggle("players", "Other players",
				"Show bars above other players.", true));
		showHostile = (Setting.Toggle) module.add(new Setting.Toggle("hostile", "Hostile mobs",
				"Monsters and other dangerous entities.", true));
		showPassive = (Setting.Toggle) module.add(new Setting.Toggle("passive", "Passive mobs",
				"Animals and other peaceful entities.", true));
		showOthers = (Setting.Toggle) module.add(new Setting.Toggle("others", "Everything else",
				"Armour stands, minecarts with mobs, villagers and so on.", false));
		maxEntities = (Setting.Number) module.add(new Setting.Number("limit", "Entity limit",
				"Hard cap on drawn bars, keeps the frame time flat in crowded fights.", 32.0, 1.0, 128.0, 1.0));
		barWidth = (Setting.Number) module.add(new Setting.Number("bar_width", "Bar width",
				"Width of the health bar in pixels.", 46.0, 20.0, 120.0, 1.0, "px"));
		barHeight = (Setting.Number) module.add(new Setting.Number("bar_height", "Bar height",
				"Height of the health bar in pixels.", 4.0, 2.0, 10.0, 0.5, "px"));
		verticalOffset = (Setting.Number) module.add(new Setting.Number("offset", "Vertical offset",
				"Distance above the entity's head.", 0.55, 0.0, 2.0, 0.05, "m"));
		damageFlash = (Setting.Toggle) module.add(new Setting.Toggle("flash", "Damage flash",
				"Briefly flash the bar when the entity loses health.", true));
		damageNumbers = (Setting.Toggle) module.add(new Setting.Toggle("damage_numbers", "Damage numbers",
				"Show a small floating number with the damage taken.", true));
		colorMode = (Setting.Choice) module.add(new Setting.Choice("colors", "Colour mode",
				"How bars are coloured.", 0, "Health gradient", "Entity type", "Accent"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.level == null) {
			TRACKED.clear();
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null || !Projection.isReady()) {
			return;
		}
		double maxDistance = range.get();
		double maxDistanceSquared = maxDistance * maxDistance;
		int limit = Math.max(1, maxEntities.getInt());
		float delta = dev.chaosutils.core.TickClock.frameDelta();
		int drawn = 0;

		for (Entity entity : client.level.entitiesForRendering()) {
			if (drawn >= limit) {
				break;
			}
			if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
				continue;
			}
			if (living == client.player && !showSelf.get()) {
				continue;
			}
			if (living.isInvisible() && living != client.player) {
				continue;
			}
			if (!passesFilter(living)) {
				continue;
			}
			double distanceSquared = living.distanceToSqr(client.player);
			if (distanceSquared > maxDistanceSquared) {
				continue;
			}
			float health = living.getHealth();
			float maxHealth = Math.max(1.0F, living.getMaxHealth());
			if (onlyDamaged.get() && health >= maxHealth - 0.01F) {
				continue;
			}

			Tracked tracked = TRACKED.computeIfAbsent(living.getId(), id -> new Tracked());
			if (tracked.displayed < 0.0F) {
				tracked.displayed = health;
				tracked.lastHealth = health;
			}
			if (health < tracked.lastHealth - 0.05F) {
				tracked.lastDamageAmount = tracked.lastHealth - health;
				tracked.lastDamageAt = System.currentTimeMillis();
			}
			tracked.lastHealth = health;
			tracked.displayed = Anim.approach(tracked.displayed, health, 6.0F, delta);
			if (Math.abs(tracked.displayed - health) < 0.02F) {
				tracked.displayed = health;
			}
			float flashTarget = damageFlash.get() && System.currentTimeMillis() - tracked.lastDamageAt < 420L ? 1.0F : 0.0F;
			tracked.flash = Anim.approach(tracked.flash, flashTarget, 9.0F, delta);

			float height = living.getBbHeight();
			Projection.Point point = Projection.project(living.getX(), living.getY() + height + verticalOffset.get(), living.getZ());
			if (Float.isNaN(point.x()) || point.depth() <= 0.2F || !point.onScreen(48.0F)) {
				continue;
			}
			drawIndicator(graphics, client.font, living, tracked, point, health, maxHealth, distanceSquared);
			drawn++;
		}
	}

	private static boolean passesFilter(LivingEntity entity) {
		if (entity instanceof Player) {
			return showPlayers.get();
		}
		if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
			return showHostile.get();
		}
		if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE
				|| entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.WATER_CREATURE
				|| entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.AMBIENT
				|| entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.AXOLOTLS
				|| entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE) {
			return showPassive.get();
		}
		return showOthers.get();
	}

	private static void drawIndicator(GuiGraphics graphics, Font font, LivingEntity entity, Tracked tracked,
			Projection.Point point, float health, float maxHealth, double distanceSquared) {
		float scale = HudPanel.scale() * Anim.clamp(96.0F / Math.max(24.0F, point.depth()), 0.55F, 1.35F);
		float barW = barWidth.getFloat() * scale;
		float barH = Math.max(2.0F, barHeight.getFloat() * scale);
		float flash = tracked.flash;

		float textScale = scale;
		String numbers = showNumbers.get() ? trimFloat(health) + " / " + trimFloat(maxHealth) : "";
		if (showPercent.get()) {
			numbers = numbers.isEmpty() ? Math.round(health / maxHealth * 100.0F) + "%"
					: numbers + "  (" + Math.round(health / maxHealth * 100.0F) + "%)";
		}
		String name = showNames.get() ? entity.getName().getString() : "";

		float textWidth = Math.max(font.width(numbers), font.width(name));
		float totalWidth = Math.max(barW, textWidth) + 4.0F;
		float x = point.x() - totalWidth * 0.5F;
		float y = point.y();
		float textLineHeight = 9.0F;

		float fraction = Anim.clamp01(health / maxHealth);
		float displayedFraction = Anim.clamp01(tracked.displayed / maxHealth);
		int color = barColor(entity, fraction, colorMode.get());
		boolean panel = HudStyleModule.backgroundOpacity.getFloat() > 0.01F
				|| (name.isEmpty() && numbers.isEmpty() && !showBars.get());
		if (panel && (showBars.get() || !numbers.isEmpty() || !name.isEmpty())) {
			float panelHeight = (showBars.get() ? barH + 1.0F : 0.0F) + (numbers.isEmpty() ? 0.0F : textLineHeight)
					+ (name.isEmpty() ? 0.0F : textLineHeight) + 2.0F;
			HudPanel.panel(graphics, font, x - 2.0F, y - 1.0F, totalWidth + 4.0F, panelHeight, color);
		}

		float cursorY = y;
		if (!name.isEmpty()) {
			HudPanel.text(graphics, font, name, point.x() - font.width(name) * 0.5F, cursorY, 0xFFFFFFFF);
			cursorY += textLineHeight;
		}
		if (showBars.get()) {
			// Losing-health chunk (bright red) behind the animated current value.
			Render.roundedRect(graphics, x, cursorY, barW, barH, barH * 0.5F, 0x88000000);
			float lostFraction = Math.max(fraction, displayedFraction);
			Render.roundedRect(graphics, x, cursorY, barW * lostFraction, barH, barH * 0.5F,
					Render.mix(color, 0xFFFF4D4D, flash * 0.7F));
			Render.roundedRect(graphics, x, cursorY, barW * fraction, barH, barH * 0.5F, color);
			if (flash > 0.02F) {
				Render.roundedBorder(graphics, x, cursorY, barW, barH, barH * 0.5F, 1.0F,
						Render.alpha(0xFFFFFFFF, flash * 0.7F), 0x00000000);
			}
			cursorY += barH + 1.0F;
		}
		if (!numbers.isEmpty()) {
			HudPanel.text(graphics, font, numbers, point.x() - font.width(numbers) * 0.5F, cursorY, 0xFFF2F2F7);
		}
		if (damageNumbers.get() && System.currentTimeMillis() - tracked.lastDamageAt < 900L) {
			float age = (System.currentTimeMillis() - tracked.lastDamageAt) / 900.0F;
			int alpha = (int) ((1.0F - age) * 220.0F);
			String damage = "-" + trimFloat(tracked.lastDamageAmount);
			int color2 = (Math.max(0, alpha) << 24) | 0xFF5555;
			HudPanel.text(graphics, font, damage, point.x() + barW * 0.55F, y - 9.0F - age * 6.0F, color2);
		}
	}

	private static int barColor(LivingEntity entity, float fraction, int mode) {
		if (mode == 1) {
			int base;
			if (entity == Minecraft.getInstance().player) {
				base = COLOR_SELF;
			} else if (entity instanceof Player) {
				base = COLOR_PLAYER;
			} else if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
				base = COLOR_HOSTILE;
			} else if (entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE) {
				base = COLOR_PASSIVE;
			} else {
				base = COLOR_OTHER;
			}
			return Render.mix(base, 0xFFFF4D4D, (1.0F - fraction) * 0.35F);
		}
		if (mode == 2) {
			return dev.chaosutils.gui.UiTheme.get().accent;
		}
		return HudPanel.healthColor(fraction);
	}

	private static String trimFloat(float value) {
		if (Math.abs(value - Math.round(value)) < 0.05F) {
			return Integer.toString(Math.round(value));
		}
		return String.format(java.util.Locale.ROOT, "%.1f", value);
	}

	@Override
	public void onWorldLeave() {
		TRACKED.clear();
	}

	@Override
	public void onDisabled() {
		TRACKED.clear();
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/HudPanel.java`

```java
package dev.chaosutils.feature.hud;

import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Drawing helpers shared by every overlay: a consistent panel background, progress bars and
 * text with the user's preferred shadow setting.
 *
 * <p>Everything is plain quad/text drawing, so overlays cost a handful of draw calls and
 * never touch the render pipeline API.
 */
public final class HudPanel {
	private HudPanel() {
	}

	public static float scale() {
		return HudStyleModule.hudScale.getFloat();
	}

	public static float padding() {
		return HudStyleModule.padding.getFloat();
	}

	public static boolean visibleNow() {
		if (!HudStyleModule.hideInScreens.get()) {
			return true;
		}
		return net.minecraft.client.Minecraft.getInstance().screen == null;
	}

	public static void panel(GuiGraphics graphics, Font font, float x, float y, float width, float height, int accent) {
		float opacity = HudStyleModule.backgroundOpacity.getFloat();
		int background = Render.alpha(HudStyleModule.backgroundColor.get() & 0xFFFFFF | 0xFF000000, opacity);
		float radius = HudStyleModule.cornerRadius.getFloat();
		if (opacity > 0.01F) {
			Render.roundedRect(graphics, x, y, width, height, radius, background);
		}
		if (HudStyleModule.border.get()) {
			Render.roundedBorder(graphics, x, y, width, height, radius, 1.0F, Render.alpha(accent, 0.55F), background);
		}
	}

	public static void text(GuiGraphics graphics, Font font, String value, float x, float y, int color) {
		Render.text(graphics, font, value, x, y, color, HudStyleModule.textShadow.get());
	}

	public static void text(GuiGraphics graphics, Font font, net.minecraft.network.chat.Component value, float x, float y, int color) {
		Render.text(graphics, font, value, x, y, color, HudStyleModule.textShadow.get());
	}

	public static void bar(GuiGraphics graphics, float x, float y, float width, float height, float fraction, int color) {
		float clamped = Anim.clamp01(fraction);
		if (HudStyleModule.accentBars.get()) {
			Render.roundedRect(graphics, x, y, width, height, height * 0.5F, 0x66000000);
			Render.roundedRect(graphics, x, y, width * clamped, height, height * 0.5F, color);
			return;
		}
		Render.rect(graphics, x, y, width, height, 0x66000000);
		Render.rect(graphics, x, y, width * clamped, height, color);
	}

	/** Colour ramp: green at full, amber in the middle, red when low. */
	public static int healthColor(float fraction) {
		float clamped = Anim.clamp01(fraction);
		if (clamped > 0.5F) {
			return Render.mix(0xFFFFC93C, 0xFF63D471, (clamped - 0.5F) * 2.0F);
		}
		return Render.mix(0xFFE05B5B, 0xFFFFC93C, clamped * 2.0F);
	}

	public static int dangerColor(float fraction) {
		float clamped = Anim.clamp01(fraction);
		return Render.mix(0xFFE05B5B, 0xFFF2F2F7, clamped);
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/HudStyleModule.java`

```java
package dev.chaosutils.feature.hud;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;

/**
 * Shared look and feel of every HUD overlay.
 *
 * <p>One place to make all overlays match: background strength, border, text shadow and
 * global scaling. Individual overlays only add what is specific to them.
 */
public final class HudStyleModule {
	public static final String ID = "hud_style";

	public static Module MODULE;
	public static Setting.Color backgroundColor;
	public static Setting.Number backgroundOpacity;
	public static Setting.Toggle border;
	public static Setting.Number cornerRadius;
	public static Setting.Toggle textShadow;
	public static Setting.Number hudScale;
	public static Setting.Number padding;
	public static Setting.Toggle hideInScreens;
	public static Setting.Toggle accentBars;

	private HudStyleModule() {
	}

	public static void register() {
		MODULE = ModuleManager.register(new Module(ID, "HUD Style",
				"Shared appearance of every ChaosUtils overlay.", Category.HUD, true));
		backgroundColor = (Setting.Color) MODULE.add(new Setting.Color("background", "Background",
				"Panel colour behind overlay text.", 0xFF0E0E16));
		backgroundOpacity = (Setting.Number) MODULE.add(new Setting.Number("opacity", "Background opacity",
				"0 makes every overlay text only.", 0.55, 0.0, 1.0, 0.05));
		border = (Setting.Toggle) MODULE.add(new Setting.Toggle("border", "Border",
				"Thin outline in the feature's accent colour.", true));
		cornerRadius = (Setting.Number) MODULE.add(new Setting.Number("radius", "Corner radius",
				"Roundness of overlay panels.", 4.0, 0.0, 10.0, 0.5, "px"));
		textShadow = (Setting.Toggle) MODULE.add(new Setting.Toggle("text_shadow", "Text shadow",
				"Improves readability on bright backgrounds.", true));
		hudScale = (Setting.Number) MODULE.add(new Setting.Number("scale", "Overlay scale",
				"Scales all ChaosUtils overlays at once.", 1.0, 0.6, 1.6, 0.05, "x"));
		padding = (Setting.Number) MODULE.add(new Setting.Number("padding", "Panel padding",
				"Space between the panel edge and its content.", 3.0, 1.0, 8.0, 0.5, "px"));
		hideInScreens = (Setting.Toggle) MODULE.add(new Setting.Toggle("hide_in_screens", "Hide in menus",
				"Hide overlays while a screen (inventory, chat, ...) is open.", false));
		accentBars = (Setting.Toggle) MODULE.add(new Setting.Toggle("accent_bars", "Accent bars",
				"Draw a coloured bar for health, durability and progress values.", true));
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/TpsPingHud.java`

```java
package dev.chaosutils.feature.hud;

import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.TpsEstimator;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Ping and estimated server TPS indicator.
 *
 * <p>Ping is the latency vanilla already measured for the player list entry. The TPS value is
 * an <em>estimate</em> computed from how fast the world time advances compared to the wall
 * clock - it is read only, never measured by sending anything, and is labelled as an
 * estimate in the tooltip so nobody mistakes it for a server side guarantee.
 */
public final class TpsPingHud implements Feature {
	public static final String ID = "tps_ping";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Toggle showPing;
	private static Setting.Toggle showTps;
	private static Setting.Toggle showTickTime;
	private static Setting.Toggle showFps;
	private static Setting.Toggle showBars;
	private static Setting.Toggle colorCoding;
	private static Setting.Toggle hideInSingleplayer;
	private static Setting.Number scale;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Ping & TPS",
				"Latency, estimated server TPS and tick time in one compact overlay.", Category.HUD, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the overlay sits on screen.", HudPos.TOP_RIGHT.x(), HudPos.TOP_RIGHT.y()));
		showPing = (Setting.Toggle) module.add(new Setting.Toggle("ping", "Show ping",
				"Latency to the server in milliseconds.", true));
		showTps = (Setting.Toggle) module.add(new Setting.Toggle("tps", "Show estimated TPS",
				"Ticks per second estimated from the world time.", true));
		showTickTime = (Setting.Toggle) module.add(new Setting.Toggle("tick_time", "Show tick time",
				"Average server tick duration in milliseconds.", false));
		showFps = (Setting.Toggle) module.add(new Setting.Toggle("fps", "Show FPS",
				"Your own frame rate.", false));
		showBars = (Setting.Toggle) module.add(new Setting.Toggle("bars", "Show bars",
				"Visual quality bars next to the numbers.", true));
		colorCoding = (Setting.Toggle) module.add(new Setting.Toggle("colors", "Colour coding",
				"Green / amber / red depending on the value.", true));
		hideInSingleplayer = (Setting.Toggle) module.add(new Setting.Toggle("multiplayer_only", "Multiplayer only",
				"Hide the overlay in singleplayer.", true));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Overlay scale",
				"Relative size of this overlay.", 1.0, 0.6, 1.8, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		TpsEstimator.sample(client);
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || !HudPanel.visibleNow()) {
			return;
		}
		if (hideInSingleplayer.get() && client.getCurrentServer() == null) {
			return;
		}
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		Font font = client.font;
		int ping = TpsEstimator.ping(client);
		double tps = TpsEstimator.tps();
		double tickMillis = TpsEstimator.tickMillis();
		int fps = client.getFps();

		String pingText = showPing.get() ? (ping < 0 ? "-- ms" : ping + " ms") : null;
		String tpsText = showTps.get() ? String.format(Locale.ROOT, "%.1f tps", tps) : null;
		String tickText = showTickTime.get() ? String.format(Locale.ROOT, "%.1f ms/tick", tickMillis) : null;
		String fpsText = showFps.get() ? fps + " fps" : null;

		float width = 44.0F;
		float padding = HudPanel.padding();
		float lineHeight = 10.0F;
		int lines = 0;
		for (String value : new String[] {pingText, tpsText, tickText, fpsText}) {
			if (value != null) {
				width = Math.max(width, font.width(value) + 26.0F);
				lines++;
			}
		}
		if (lines == 0) {
			return;
		}
		float boxWidth = width * scaleFactor;
		float boxHeight = (lines * lineHeight + padding * 2.0F) * scaleFactor;
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(boxHeight));

		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF4DB6AC);
		float cursorY = y + padding * scaleFactor;
		if (showPing.get()) {
			int color = colorCoding.get() ? pingColor(ping) : 0xFFF2F2F7;
			if (showBars.get()) {
				float fraction = ping < 0 ? 0.0F : Anim.clamp01(1.0F - ping / 300.0F);
				HudPanel.bar(graphics, x + padding * scaleFactor, cursorY + 3.0F, 10.0F * scaleFactor, 3.0F * scaleFactor,
						fraction, color);
			}
			HudPanel.text(graphics, font, pingText, x + 16.0F * scaleFactor, cursorY, color);
			cursorY += lineHeight * scaleFactor;
		}
		if (showTps.get()) {
			int color = colorCoding.get() ? tpsColor(tps) : 0xFFF2F2F7;
			if (showBars.get()) {
				HudPanel.bar(graphics, x + padding * scaleFactor, cursorY + 3.0F, 10.0F * scaleFactor, 3.0F * scaleFactor,
						Anim.clamp01((float) (tps / 20.0)), color);
			}
			HudPanel.text(graphics, font, tpsText, x + 16.0F * scaleFactor, cursorY, color);
			cursorY += lineHeight * scaleFactor;
		}
		if (showTickTime.get()) {
			HudPanel.text(graphics, font, tickText, x + 16.0F * scaleFactor, cursorY, 0xFFBFC2CF);
			cursorY += lineHeight * scaleFactor;
		}
		if (showFps.get()) {
			HudPanel.text(graphics, font, fpsText, x + 16.0F * scaleFactor, cursorY, 0xFFBFC2CF);
		}
	}

	private static int pingColor(int ping) {
		if (ping < 0) {
			return 0xFF9E9EB3;
		}
		if (ping < 80) {
			return 0xFF63D471;
		}
		if (ping < 180) {
			return 0xFFF0B429;
		}
		return 0xFFE05B5B;
	}

	private static int tpsColor(double tps) {
		if (tps >= 19.5) {
			return 0xFF63D471;
		}
		if (tps >= 15.0) {
			return 0xFFF0B429;
		}
		return 0xFFE05B5B;
	}

	/** Kept so the panic toggle can dim the overlay without disabling the feature. */
	public static int accent() {
		return Render.mix(0xFF4DB6AC, 0xFF63D471, 0.5F);
	}
}
```

### `src/main/java/dev/chaosutils/feature/hud/WaypointHud.java`

```java
package dev.chaosutils.feature.hud;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/**
 * Death position marker and client side waypoints.
 *
 * <p>When you die, the exact position and dimension are stored locally (nothing is sent
 * anywhere). Marker beams, on screen labels and off screen arrows then guide you back; the
 * same system handles manually added waypoints. All data lives in the ChaosUtils config
 * file, so it survives restarts.
 */
public final class WaypointHud implements Feature {
	public static final String ID = "waypoints";

	private static Module module;
	private static Setting.Toggle deathMarkers;
	private static Setting.Number deathLifetime;
	private static Setting.Toggle beacons;
	private static Setting.Number beaconRange;
	private static Setting.Toggle showLabels;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle showArrows;
	private static Setting.Number maxDistance;
	private static Setting.Number markerSize;
	private static Setting.Number autoDeleteRadius;
	private static Setting.Toggle expireNotifications;

	private static boolean deathRecorded;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Waypoints & Death Marker",
				"Client side markers with beams, distances and off screen arrows.", Category.HUD, true));
		deathMarkers = (Setting.Toggle) module.add(new Setting.Toggle("death", "Death marker",
				"Store your death position and guide you back to it.", true));
		deathLifetime = (Setting.Number) module.add(new Setting.Number("death_lifetime", "Death marker lifetime",
				"Minutes until the death marker disappears (0 = forever).", 30.0, 0.0, 240.0, 5.0, "min"));
		beacons = (Setting.Toggle) module.add(new Setting.Toggle("beacons", "Beam",
				"Draw a translucent vertical beam at the marker.", true));
		beaconRange = (Setting.Number) module.add(new Setting.Number("beacon_range", "Beam range",
				"Only draw the beam within this distance.", 192.0, 16.0, 512.0, 16.0, "m"));
		showLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Labels",
				"Marker name above the icon.", true));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance", "Distance",
				"Distance in metres next to the label.", true));
		showArrows = (Setting.Toggle) module.add(new Setting.Toggle("arrows", "Off screen arrows",
				"Point towards markers that are outside the screen.", true));
		maxDistance = (Setting.Number) module.add(new Setting.Number("max_distance", "Maximum distance",
				"Markers further away than this are ignored.", 512.0, 32.0, 4096.0, 32.0, "m"));
		markerSize = (Setting.Number) module.add(new Setting.Number("marker_size", "Marker size",
				"Size of the marker icon.", 1.0, 0.5, 2.5, 0.1, "x"));
		autoDeleteRadius = (Setting.Number) module.add(new Setting.Number("arrival", "Auto delete radius",
				"Delete the marker once you are this close.", 4.0, 0.0, 32.0, 1.0, "m"));
		expireNotifications = (Setting.Toggle) module.add(new Setting.Toggle("chat_note", "Chat feedback",
				"Write a short line to chat when a marker is added.", true));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		Player player = client.player;
		if (player == null || client.level == null) {
			return;
		}
		// Death detection: exactly one marker per death, stored locally.
		if (deathMarkers.get() && player.isDeadOrDying()) {
			if (!deathRecorded) {
				deathRecorded = true;
				long lifetime = (long) (deathLifetime.get() * 60_000.0);
				Waypoint waypoint = new Waypoint("Death", player.getX(), player.getY(), player.getZ(),
						client.level.dimension().location().toString(), 0xFFE05B5B, true, lifetime);
				if (lifetime <= 0L) {
					waypoint.expiresAt = 0L;
				}
				ChaosConfig.WAYPOINTS.add(waypoint);
				ChaosConfig.markDirty();
				notify(client, "Death position stored");
			}
		} else if (!player.isDeadOrDying() && player.getHealth() > 0.0F) {
			deathRecorded = false;
		}

		if (Keybinds.addWaypoint != null && Keybinds.addWaypoint.wasPressed()) {
			Waypoint waypoint = new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
					player.getX(), player.getY(), player.getZ(),
					client.level.dimension().location().toString(), 0xFF7C5CFF, false, 0L);
			ChaosConfig.WAYPOINTS.add(waypoint);
			ChaosConfig.markDirty();
			notify(client, "Waypoint added: " + waypoint.name);
		}

		prune(player, client);
	}

	private static void prune(Player player, Minecraft client) {
		double arrival = autoDeleteRadius.get();
		Iterator<Waypoint> iterator = ChaosConfig.WAYPOINTS.iterator();
		boolean changed = false;
		while (iterator.hasNext()) {
			Waypoint waypoint = iterator.next();
			if (waypoint.expired()) {
				iterator.remove();
				changed = true;
				continue;
			}
			if (arrival > 0.0 && waypoint.dimension.equals(currentDimension(client))) {
				double dx = waypoint.x - player.getX();
				double dy = waypoint.y - player.getY();
				double dz = waypoint.z - player.getZ();
				if (dx * dx + dy * dy + dz * dz <= arrival * arrival) {
					iterator.remove();
					changed = true;
					notify(client, "Arrived at " + waypoint.name);
				}
			}
		}
		if (changed) {
			ChaosConfig.markDirty();
		}
	}

	private static void notify(Minecraft client, String text) {
		if (expireNotifications.get() && client.player != null) {
			client.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[ChaosUtils] §f" + text), true);
		}
	}

	private static String currentDimension(Minecraft client) {
		return client.level == null ? "minecraft:overworld" : client.level.dimension().location().toString();
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || client.level == null || !Projection.isReady() || !HudPanel.visibleNow()) {
			return;
		}
		String dimension = currentDimension(client);
		double limit = maxDistance.get();
		double limitSquared = limit * limit;
		Font font = client.font;
		float size = markerSize.getFloat() * HudPanel.scale();
		float delta = dev.chaosutils.core.TickClock.frameDelta();

		for (Waypoint waypoint : List.copyOf(ChaosConfig.WAYPOINTS)) {
			if (!waypoint.dimension.equals(dimension)) {
				continue;
			}
			double distanceSquared = player.distanceToSqr(waypoint.x, waypoint.y, waypoint.z);
			if (distanceSquared > limitSquared) {
				continue;
			}
			Projection.Point point = Projection.project(waypoint.x, waypoint.y, waypoint.z);
			double distance = Math.sqrt(distanceSquared);
			if (!Float.isNaN(point.x()) && point.depth() > 0.1F) {
				if (beacons.get() && distance <= beaconRange.get()) {
					int beamColor = Render.alpha(waypoint.color, 0.16F);
					Render.rect(graphics, point.x() - 1.0F, 0.0F, 2.0F, graphics.getScaledWindowHeight(), beamColor);
				}
				if (point.onScreen(64.0F)) {
					drawMarker(graphics, font, waypoint, point.x(), point.y(), distance, size, delta);
				} else if (showArrows.get()) {
					drawArrow(graphics, font, waypoint, point.x(), point.y(), distance, size);
				}
			} else if (showArrows.get()) {
				drawArrow(graphics, font, waypoint, Float.NaN, Float.NaN, distance, size);
			}
		}
	}

	private static void drawMarker(GuiGraphics graphics, Font font, Waypoint waypoint, float x, float y, double distance,
			float size, float delta) {
		int color = waypoint.color;
		float half = 4.0F * size;
		// Diamond marker drawn from two stacked rectangles - readable at any scale.
		Render.rect(graphics, x - half, y - half * 0.5F, half * 2.0F, half, Render.alpha(color, 0.85F));
		Render.rect(graphics, x - half * 0.5F, y - half, half, half * 2.0F, Render.alpha(color, 0.85F));
		Render.roundedBorder(graphics, x - half, y - half, half * 2.0F, half * 2.0F, 2.0F, 1.0F,
				Render.alpha(0xFFFFFFFF, 0.5F), 0x00000000);
		String label = waypoint.name;
		if (showDistance.get()) {
			label = label + "  " + formatDistance(distance) + "m";
		}
		if (showLabels.get()) {
			HudPanel.text(graphics, font, label, x - font.width(label) * 0.5F, y - 14.0F * size, 0xFFFFFFFF);
		} else if (showDistance.get()) {
			String text = formatDistance(distance) + "m";
			HudPanel.text(graphics, font, text, x - font.width(text) * 0.5F, y - 14.0F * size, 0xFFFFFFFF);
		}
	}

	private static void drawArrow(GuiGraphics graphics, Font font, Waypoint waypoint, float targetX, float targetY,
			double distance, float size) {
		float centerX = graphics.getScaledWindowWidth() * 0.5F;
		float centerY = graphics.getScaledWindowHeight() * 0.5F;
		float bearing;
		if (Float.isNaN(targetX)) {
			bearing = Projection.bearingTo(waypoint.x, waypoint.z);
		} else {
			bearing = (float) Math.toDegrees(Math.atan2(targetX - centerX, -(targetY - centerY)));
		}
		float radians = (float) Math.toRadians(bearing);
		float radius = Math.min(centerX, centerY) - 26.0F;
		float x = centerX + (float) Math.sin(radians) * radius;
		float y = centerY - (float) Math.cos(radians) * radius;
		int color = waypoint.color;
		float arrowLength = 11.0F * size;
		float arrowWidth = 6.0F * size;
		float dirX = (float) Math.sin(radians);
		float dirY = -(float) Math.cos(radians);
		float perpX = -dirY;
		float perpY = dirX;
		// Chevron: two wings from the tip plus a short shaft behind it.
		Render.line(graphics, x, y, x - dirX * arrowLength + perpX * arrowWidth, y - dirY * arrowLength + perpY * arrowWidth,
				2.0F * size, Render.alpha(color, 0.95F));
		Render.line(graphics, x, y, x - dirX * arrowLength - perpX * arrowWidth, y - dirY * arrowLength - perpY * arrowWidth,
				2.0F * size, Render.alpha(color, 0.95F));
		Render.line(graphics, x, y, x - dirX * arrowLength * 0.55F, y - dirY * arrowLength * 0.55F,
				1.5F * size, Render.alpha(color, 0.65F));
		String label = formatDistance(distance) + "m";
		HudPanel.text(graphics, font, label, x - font.width(label) * 0.5F, y + 6.0F * size, Render.alpha(0xFFFFFFFF, 0.85F));
	}

	private static String formatDistance(double distance) {
		if (distance >= 1000.0) {
			return String.format(Locale.ROOT, "%.1fk", distance / 1000.0);
		}
		return Integer.toString((int) Math.round(distance));
	}

	@Override
	public void onWorldJoin() {
		deathRecorded = false;
	}

	@Override
	public void onDisabled() {
		deathRecorded = false;
	}
}
```

---

## Visual features

### `src/main/java/dev/chaosutils/feature/visual/CrosshairDesigner.java`

```java
package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Custom crosshair designer.
 *
 * <p>Vanilla's crosshair is cancelled (Gui hook) and replaced by a fully configurable one:
 * four shapes, thickness, gap, length, dot, outline and transparency. The transparency has
 * a dynamic mode: the crosshair fades out while drawing a bow, eating, blocking or while
 * the attack cooldown is recharging, so it never hides what you are aiming at. The
 * crosshair also spreads with movement like the vanilla one.
 *
 * <p>Everything here is drawing only - no gameplay value is read or changed.
 */
public final class CrosshairDesigner implements Feature {
	public static final String ID = "crosshair";

	private static Module module;
	private static Setting.Choice shape;
	private static Setting.Number thickness;
	private static Setting.Number gap;
	private static Setting.Number length;
	private static Setting.Toggle dot;
	private static Setting.Number dotSize;
	private static Setting.Color color;
	private static Setting.Color outlineColor;
	private static Setting.Number opacity;
	private static Setting.Toggle dynamicOpacity;
	private static Setting.Number dynamicOpacityValue;
	private static Setting.Toggle spread;
	private static Setting.Number spreadAmount;
	private static Setting.Toggle hideInThirdPerson;
	private static Setting.Toggle showCooldownRing;

	private final Anim.Value currentSpread = new Anim.Value(0.0F, 12.0F);

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Crosshair Designer",
				"Fully custom crosshair: shape, colours, gap, dot and smart transparency.", Category.VISUAL, false));
		shape = (Setting.Choice) module.add(new Setting.Choice("shape", "Shape",
				"Geometry of the crosshair.", 0, "Cross", "Cross + dot", "Dot only", "Circle", "Bracket", "None"));
		thickness = (Setting.Number) module.add(new Setting.Number("thickness", "Thickness",
				"Line thickness in pixels.", 1.0, 1.0, 4.0, 0.5, "px"));
		gap = (Setting.Number) module.add(new Setting.Number("gap", "Gap",
				"Space between the centre and the four lines.", 3.0, 0.0, 12.0, 0.5, "px"));
		length = (Setting.Number) module.add(new Setting.Number("length", "Length",
				"Length of each line.", 5.0, 1.0, 16.0, 0.5, "px"));
		dot = (Setting.Toggle) module.add(new Setting.Toggle("dot", "Centre dot",
				"Draw a dot in the exact centre.", false));
		dotSize = (Setting.Number) module.add(new Setting.Number("dot_size", "Dot size",
				"Size of the centre dot.", 1.0, 1.0, 4.0, 0.5, "px"));
		color = (Setting.Color) module.add(new Setting.Color("color", "Colour",
				"Main crosshair colour.", 0xFFFFFFFF));
		outlineColor = (Setting.Color) module.add(new Setting.Color("outline", "Outline colour",
				"Dark outline that keeps the crosshair readable.", 0x40000000));
		opacity = (Setting.Number) module.add(new Setting.Number("opacity", "Opacity",
				"Base opacity of the crosshair.", 1.0, 0.1, 1.0, 0.05));
		dynamicOpacity = (Setting.Toggle) module.add(new Setting.Toggle("dynamic", "Fade while aiming",
				"Fade out while drawing a bow, eating, blocking or while aiming at a player.", true));
		dynamicOpacityValue = (Setting.Number) module.add(new Setting.Number("dynamic_opacity", "Faded opacity",
				"Opacity used while the dynamic fade is active.", 0.15, 0.0, 0.9, 0.05));
		spread = (Setting.Toggle) module.add(new Setting.Toggle("spread", "Movement spread",
				"Open the crosshair while walking and attacking.", true));
		spreadAmount = (Setting.Number) module.add(new Setting.Number("spread_amount", "Spread amount",
				"How far the lines open up.", 3.0, 0.0, 10.0, 0.5, "px"));
		hideInThirdPerson = (Setting.Toggle) module.add(new Setting.Toggle("hide_third_person", "Hide in third person",
				"Do not draw the crosshair in third person view.", false));
		showCooldownRing = (Setting.Toggle) module.add(new Setting.Toggle("cooldown", "Attack indicator ring",
				"Draw a thin ring while the attack cooldown recharges.", false));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	/** True when the mixin may cancel the vanilla crosshair. */
	public static boolean replacesVanilla() {
		if (!ModuleManager.enabled(ID)) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui) {
			return false;
		}
		if (client.screen != null) {
			return false;
		}
		if (hideInThirdPerson.get() && !client.options.getCameraType().isFirstPerson()) {
			return true;
		}
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || HudPanel.scale() <= 0.0F) {
			return;
		}
		if (hideInThirdPerson.get() && !client.options.getCameraType().isFirstPerson()) {
			return;
		}
		int selected = shape.get();
		if (selected == 5) {
			return;
		}
		float centerX = graphics.getScaledWindowWidth() * 0.5F;
		float centerY = graphics.getScaledWindowHeight() * 0.5F;

		float targetSpread = 0.0F;
		if (spread.get()) {
			double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
			boolean attacking = player.getAttackAnim(partialTick) > 0.05F && player.getAttackAnim(partialTick) < 0.95F;
			targetSpread = (float) Math.min(1.0, horizontalSpeed * 1.4) * spreadAmount.getFloat();
			if (attacking || player.isSprinting()) {
				targetSpread += spreadAmount.getFloat() * 0.45F;
			}
		}
		currentSpread.set(targetSpread);
		currentSpread.update(dev.chaosutils.core.TickClock.frameDelta());
		float spreadPixels = currentSpread.get();

		float alpha = opacity.getFloat();
		if (dynamicOpacity.get() && fadesOut(client, player)) {
			alpha = dynamicOpacityValue.getFloat();
		}
		alpha *= Math.max(0.15F, HudPanel.scale());
		int mainColor = Render.alpha(color.get(), Anim.clamp01(alpha));
		int borderColor = Render.alpha(outlineColor.get(), Anim.clamp01(alpha * 0.85F));

		float lineThickness = thickness.getFloat();
		float lineLength = length.getFloat();
		float gapSize = gap.getFloat() + spreadPixels;

		if (selected == 3) {
			drawCircle(graphics, centerX, centerY, gapSize + lineLength * 0.5F, lineThickness, mainColor);
		} else if (selected == 4) {
			drawBracket(graphics, centerX, centerY, gapSize, lineLength, lineThickness, mainColor);
		} else {
			float offset = gapSize + lineLength * 0.5F;
			rect(graphics, centerX - lineThickness * 0.5F, centerY - offset - lineLength * 0.5F, lineThickness, lineLength, borderColor);
			rect(graphics, centerX - lineThickness * 0.5F, centerY + offset - lineLength * 0.5F, lineThickness, lineLength, borderColor);
			rect(graphics, centerX - offset - lineLength * 0.5F, centerY - lineThickness * 0.5F, lineLength, lineThickness, borderColor);
			rect(graphics, centerX + offset - lineLength * 0.5F, centerY - lineThickness * 0.5F, lineLength, lineThickness, borderColor);
			float inner = Math.max(1.0F, lineThickness - 2.0F);
			float innerOffset = offset;
			rect(graphics, centerX - inner * 0.5F, centerY - innerOffset - lineLength * 0.5F, inner, lineLength, mainColor);
			rect(graphics, centerX - inner * 0.5F, centerY + innerOffset - lineLength * 0.5F, inner, lineLength, mainColor);
			rect(graphics, centerX - innerOffset - lineLength * 0.5F, centerY - inner * 0.5F, lineLength, inner, mainColor);
			rect(graphics, centerX + innerOffset - lineLength * 0.5F, centerY - inner * 0.5F, lineLength, inner, mainColor);
		}

		if (dot.get() || selected == 1 || selected == 2) {
			float size = dot.get() ? dotSize.getFloat() : 1.0F;
			rect(graphics, centerX - size * 0.5F, centerY - size * 0.5F, size, size, mainColor);
		}

		if (showCooldownRing.get()) {
			drawCooldownRing(graphics, centerX, centerY, gapSize + lineLength + 3.0F, player, partialTick, alpha);
		}
	}

	private static boolean fadesOut(Minecraft client, Player player) {
		if (player.isUsingItem()) {
			return true;
		}
		try {
			HitResult hit = client.hitResult;
			if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof Player) {
				return true;
			}
		} catch (Throwable ignored) {
			// lookup target is unavailable - keep the normal opacity
		}
		return false;
	}

	private static void drawCooldownRing(GuiGraphics graphics, float centerX, float centerY, float radius, Player player,
			float partialTick, float alpha) {
		float progress;
		try {
			progress = Anim.clamp01(player.getAttackStrengthScale(partialTick));
		} catch (Throwable ignored) {
			return;
		}
		if (progress >= 1.0F) {
			return;
		}
		int color = Render.alpha(0xFFFFFFFF, Anim.clamp01(alpha * 0.5F));
		int segments = 24;
		int visible = Math.max(1, (int) (segments * progress));
		for (int i = 0; i < visible; i++) {
			double angle = (i / (double) segments) * Math.PI * 2.0 - Math.PI * 0.5;
			float x = centerX + (float) Math.cos(angle) * radius;
			float y = centerY + (float) Math.sin(angle) * radius;
			rect(graphics, x, y, 1.5F, 1.5F, color);
		}
	}

	private static void drawCircle(GuiGraphics graphics, float centerX, float centerY, float radius, float thickness, int color) {
		int segments = 48;
		for (int i = 0; i < segments; i++) {
			double angle = (i / (double) segments) * Math.PI * 2.0;
			float x = centerX + (float) Math.cos(angle) * radius;
			float y = centerY + (float) Math.sin(angle) * radius;
			rect(graphics, x, y, thickness, thickness, color);
		}
	}

	private static void drawBracket(GuiGraphics graphics, float centerX, float centerY, float gapSize, float length, float thickness, int color) {
		float h = thickness;
		float corner = Math.max(2.0F, length * 0.5F);
		rect(graphics, centerX - gapSize - corner, centerY - gapSize - h, corner, h, color);
		rect(graphics, centerX - gapSize - h, centerY - gapSize - corner, h, corner, color);
		rect(graphics, centerX + gapSize, centerY - gapSize - h, corner, h, color);
		rect(graphics, centerX + gapSize, centerY - gapSize - corner, h, corner, color);
		rect(graphics, centerX - gapSize - corner, centerY + gapSize, corner, h, color);
		rect(graphics, centerX - gapSize - h, centerY + gapSize, h, corner, color);
		rect(graphics, centerX + gapSize, centerY + gapSize, corner, h, color);
		rect(graphics, centerX + gapSize, centerY + gapSize - corner + h, h, corner, color);
	}

	private static void rect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		Render.rect(graphics, x, y, width, height, color);
	}
}
```

### `src/main/java/dev/chaosutils/feature/visual/GammaModule.java`

```java
package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Night vision / brightness modifier.
 *
 * <p>The vanilla brightness option is exactly the value the lightmap uses when it builds
 * the world lighting, so ChaosUtils drives that option instead of patching the lightmap
 * texture: the result is identical, it applies instantly to the world, it cannot break the
 * render pipeline and it is trivially reversible. Your own brightness value is stored when
 * the feature activates and restored the moment it is turned off, on disconnect and when
 * the config is reloaded.
 *
 * <p>An optional, very subtle tint sells the "night vision" look without touching colours
 * the server ever sees.
 */
public final class GammaModule implements Feature {
	public static final String ID = "gamma";

	private static Module module;
	private static Setting.Toggle override;
	private static Setting.Number gamma;
	private static Setting.Number transitionSpeed;
	private static Setting.Toggle boostKey;
	private static Setting.Toggle boostToggle;
	private static Setting.Number boostValue;
	private static Setting.Toggle tintEnabled;
	private static Setting.Color tintColor;
	private static Setting.Number tintStrength;
	private static Setting.Toggle indicator;

	private static boolean applied;
	private static double savedGamma = Double.NaN;
	private static float renderedGamma = 0.5F;
	private static boolean boostHeld;
	private static boolean boostLatched;

	public GammaModule() {
		// The animated brightness starts wherever the player's own setting is.
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Night Vision & Gamma",
				"Lift the world brightness, optionally on a hotkey, and put it back when you are done.",
				Category.VISUAL, false));
		override = (Setting.Toggle) module.add(new Setting.Toggle("override", "Override brightness",
				"While enabled, the world brightness is set to the value below.", true));
		gamma = (Setting.Number) module.add(new Setting.Number("gamma", "Brightness",
				"1.00 is the maximum vanilla brightness setting.", 1.0, 0.0, 1.0, 0.05));
		transitionSpeed = (Setting.Number) module.add(new Setting.Number("smoothing", "Transition speed",
				"Fade speed when entering or leaving the override.", 3.0, 0.5, 10.0, 0.5, "x"));
		boostKey = (Setting.Toggle) module.add(new Setting.Toggle("boost_key", "Hotkey boost",
				"Temporarily go to maximum brightness with the bound key.", true));
		boostToggle = (Setting.Toggle) module.add(new Setting.Toggle("boost_toggle", "Boost is a toggle",
				"Off: the key has to be held.", false));
		boostValue = (Setting.Number) module.add(new Setting.Number("boost_value", "Boost brightness",
				"Brightness used while the hotkey is active.", 1.0, 0.0, 1.0, 0.05));
		tintEnabled = (Setting.Toggle) module.add(new Setting.Toggle("tint", "Night vision tint",
				"A soft colour wash while night vision is active.", false));
		tintColor = (Setting.Color) module.add(new Setting.Color("tint_color", "Tint colour",
				"Colour of the overlay, alpha is ignored.", 0xFF6DF0A0));
		tintStrength = (Setting.Number) module.add(new Setting.Number("tint_strength", "Tint strength",
				"Opacity of the colour wash.", 0.08, 0.01, 0.4, 0.01));
		indicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Show indicator",
				"Small badge while the brightness override is active.", false));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null || client.options == null) {
			return;
		}
		if (!isEnabled()) {
			release(client);
			return;
		}
		boolean wanted = override.get() || boostActive();
		float target = boostActive() ? boostValue.getFloat() : gamma.getFloat();
		renderedGamma = Anim.approach(renderedGamma, target, transitionSpeed.getFloat(), 0.05F);
		if (!wanted) {
			release(client);
			return;
		}
		if (!applied) {
			savedGamma = client.options.gamma().get();
			applied = true;
			renderedGamma = (float) savedGamma;
		}
		try {
			client.options.gamma().set((double) renderedGamma);
		} catch (Throwable ignored) {
			applied = false;
		}
	}

	private boolean boostActive() {
		if (!boostKey.get() || Keybinds.toggleGamma == null) {
			return false;
		}
		if (boostToggle.get()) {
			if (Keybinds.toggleGamma.wasPressed()) {
				boostLatched = !boostLatched;
			}
			return boostLatched;
		}
		boostHeld = Keybinds.toggleGamma.isPressed();
		return boostHeld;
	}

	private static void release(Minecraft client) {
		if (boostLatched || boostHeld) {
			return;
		}
		if (!applied) {
			return;
		}
		applied = false;
		try {
			if (!Double.isNaN(savedGamma)) {
				client.options.gamma().set(savedGamma);
			}
		} catch (Throwable ignored) {
			// nothing to restore
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!isEnabled()) {
			return;
		}
		if (tintEnabled.get() && (override.get() || boostActive())) {
			int color = Render.alpha(tintColor.get(), tintStrength.getFloat());
			Render.rect(graphics, 0.0F, 0.0F, graphics.getScaledWindowWidth(), graphics.getScaledWindowHeight(), color);
		}
		if (indicator.get() && (override.get() || boostActive())) {
			Minecraft client = Minecraft.getInstance();
			String text = "Night Vision";
			float width = client.font.width(text) + 8.0F;
			dev.chaosutils.feature.hud.HudPanel.panel(graphics, client.font, 6.0F, 6.0F, width, 12.0F, 0xFF6DF0A0);
			dev.chaosutils.feature.hud.HudPanel.text(graphics, client.font, text, 10.0F, 8.0F, 0xFFF2F2F7);
		}
	}

	@Override
	public void onDisabled() {
		release(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		release(Minecraft.getInstance());
	}
}
```

### `src/main/java/dev/chaosutils/feature/visual/PerspectiveLock.java`

```java
package dev.chaosutils.feature.visual;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Perspective lock / camera preview.
 *
 * <p>While active the camera keeps the rotation you activated it with instead of following
 * your body. The player, the hitbox and every movement packet stay untouched - the server
 * sees an ordinary player; only the local camera angle is stabilised, and (optionally) the
 * vanilla third person view is used so the pulled back preview feels like a freecam. This
 * is the variant of "freecam" that cannot be used to gain information about chunks the
 * player cannot see, because the camera never leaves the player's own position.
 */
public final class PerspectiveLock implements Feature {
	public static final String ID = "perspective_lock";

	public enum Mode {
		FREEZE("Fully locked"),
		SLOW_FOLLOW("Slow follow"),
		YAW_LOCK("Yaw locked, free pitch");

		private final String label;

		Mode(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	private static Module module;
	private static Setting.Choice mode;
	private static Setting.Number followSpeed;
	private static Setting.Toggle thirdPerson;
	private static Setting.Toggle toggleMode;
	private static Setting.Number transitionSpeed;
	private static Setting.Toggle rememberRotation;

	private static boolean active;
	private static float lockedYaw;
	private static float lockedPitch;
	private static float renderedYaw;
	private static float renderedPitch;
	private static boolean initialised;
	private static CameraType previousCameraType;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Perspective Lock",
				"Lock the camera angle while your body keeps moving - perfect for screenshots and long walks.",
				Category.VISUAL, false));
		mode = (Setting.Choice) module.add(new Setting.Choice("mode", "Lock mode",
				"How the camera behaves while the key is held.", 0,
				Mode.FREEZE.label(), Mode.SLOW_FOLLOW.label(), Mode.YAW_LOCK.label()));
		followSpeed = (Setting.Number) module.add(new Setting.Number("follow_speed", "Follow speed",
				"Degrees per second the camera may drift back to your view in slow-follow mode.", 25.0, 1.0, 180.0, 1.0, "°/s"));
		thirdPerson = (Setting.Toggle) module.add(new Setting.Toggle("third_person", "Third person preview",
				"Switch to vanilla third person while locked (restores your camera afterwards).", true));
		toggleMode = (Setting.Toggle) module.add(new Setting.Toggle("toggle", "Toggle instead of hold",
				"The key toggles the lock instead of being held.", false));
		transitionSpeed = (Setting.Number) module.add(new Setting.Number("smoothing", "Smoothing",
				"How smoothly the camera returns when you release the key.", 8.0, 1.0, 20.0, 0.5, "x"));
		rememberRotation = (Setting.Toggle) module.add(new Setting.Toggle("remember", "Keep rotation on release",
				"Off: the camera snaps back to your body rotation.", true));
	}

	/** True while the camera rotation is being overridden. */
	public static boolean isActive() {
		return active && ModuleManager.enabled(ID);
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			active = false;
			return;
		}
		if (!isEnabled()) {
			deactivate(client);
			return;
		}
		boolean pressed = Keybinds.freeLook != null && Keybinds.freeLook.isPressed();
		if (toggleMode.get()) {
			if (pressed && Keybinds.freeLook.wasPressed()) {
				if (active) {
					deactivate(client);
				} else {
					activate(client);
				}
			}
		} else if (pressed) {
			if (!active) {
				activate(client);
			}
		} else if (active) {
			deactivate(client);
		}
		if (active) {
			updateRotation(client);
		}
	}

	private static void activate(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			return;
		}
		active = true;
		initialised = false;
		lockedYaw = player.getViewYRot(0.0F);
		lockedPitch = player.getViewXRot(0.0F);
		renderedYaw = lockedYaw;
		renderedPitch = lockedPitch;
		if (thirdPerson.get()) {
			try {
				previousCameraType = client.options.getCameraType();
				client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			} catch (Throwable ignored) {
				previousCameraType = null;
			}
		}
	}

	private static void deactivate(Minecraft client) {
		if (!active) {
			return;
		}
		active = false;
		if (previousCameraType != null) {
			try {
				client.options.setCameraType(previousCameraType);
			} catch (Throwable ignored) {
				// nothing to restore
			}
			previousCameraType = null;
		}
	}

	/** Only slow-follow mode moves the locked angles; the other modes keep them frozen. */
	private static void updateRotation(Minecraft client) {
		Player player = client.player;
		if (player == null || selectedMode() != Mode.SLOW_FOLLOW) {
			return;
		}
		float yawStep = followSpeed.getFloat() * (1.0F / 20.0F);
		lockedYaw = approachAngle(lockedYaw, player.getViewYRot(0.0F), yawStep);
		lockedPitch = Anim.approach(lockedPitch, player.getViewXRot(0.0F), yawStep * 0.5F, 1.0F / 20.0F);
	}

	private static Mode selectedMode() {
		Mode[] values = Mode.values();
		return values[Math.max(0, Math.min(values.length - 1, mode.get()))];
	}

	private static float approachAngle(float from, float to, float maxStep) {
		float delta = to - from;
		while (delta > 180.0F) {
			delta -= 360.0F;
		}
		while (delta < -180.0F) {
			delta += 360.0F;
		}
		if (Math.abs(delta) <= maxStep) {
			return to;
		}
		return from + Math.signum(delta) * maxStep;
	}

	/** Applied from the camera mixin after vanilla computed the camera. */
	public static void applyToCamera(Camera camera, Entity entity, float partialTick) {
		if (!active || camera == null) {
			return;
		}
		Mode selected = selectedMode();
		if (!initialised) {
			initialised = true;
			renderedYaw = camera.getYRot();
			renderedPitch = camera.getXRot();
		}
		float targetYaw = lockedYaw;
		// "Yaw locked, free pitch" keeps the vertical look free for a natural preview.
		float targetPitch = selected == Mode.YAW_LOCK ? camera.getXRot() : lockedPitch;
		float speed = rememberRotation.get() ? transitionSpeed.getFloat() : 30.0F;
		float delta = TickClock.frameDelta();
		renderedYaw = approachWrapped(renderedYaw, targetYaw, speed, delta);
		renderedPitch = Anim.approach(renderedPitch, targetPitch, speed, delta);
		camera.setRotation(renderedYaw, renderedPitch);
	}

	private static float approachWrapped(float current, float target, float speed, float deltaSeconds) {
		float difference = target - current;
		while (difference > 180.0F) {
			difference -= 360.0F;
		}
		while (difference < -180.0F) {
			difference += 360.0F;
		}
		float factor = 1.0F - (float) Math.exp(-Math.max(0.001F, speed) * Math.max(0.0F, deltaSeconds));
		return current + difference * factor;
	}

	/**
	 * Camera yaw used by HUD projections. When the lock is active the overlay markers follow
	 * the locked camera instead of the player's body, which is what the player sees.
	 */
	public static float cameraYaw(Player player, float partialTick) {
		if (player == null) {
			return 0.0F;
		}
		if (isActive()) {
			return renderedYaw;
		}
		try {
			return player.getViewYRot(partialTick);
		} catch (Throwable ignored) {
			return player.getYRot();
		}
	}

	public static float cameraPitch(Player player, float partialTick) {
		if (player == null) {
			return 0.0F;
		}
		if (isActive()) {
			return renderedPitch;
		}
		try {
			return player.getViewXRot(partialTick);
		} catch (Throwable ignored) {
			return player.getXRot();
		}
	}

	@Override
	public void onDisabled() {
		deactivate(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		active = false;
		previousCameraType = null;
		initialised = false;
	}
}
```

### `src/main/java/dev/chaosutils/feature/visual/SmoothZoom.java`

```java
package dev.chaosutils.feature.visual;

import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.TickClock;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.Features;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Seamless smooth zoom.
 *
 * <p>Zooming scales the field of view, which is a pure optical effect: the player, the
 * hitbox and the movement stay exactly as they are and no packet is involved. The zoom
 * level, the smoothing speed and the optional sensitivity compensation are configurable,
 * and the mouse wheel adjusts the zoom level while the key is held.
 */
public final class SmoothZoom implements Feature {
	public static final String ID = "zoom";

	private static Module module;
	private static Setting.Number zoomLevel;
	private static Setting.Number smoothness;
	private static Setting.Toggle sensitivityCompensation;
	private static Setting.Toggle scrollAdjust;
	private static Setting.Number scrollStep;
	private static Setting.Number minZoom;
	private static Setting.Number maxZoom;
	private static Setting.Toggle showIndicator;
	private static Setting.Toggle holdToZoom;

	private static boolean zoomActive;
	private static float current = 1.0F;
	private static float scrollOffset = 1.0F;
	private static boolean sensitivityApplied;
	private static double previousSensitivity;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Smooth Zoom",
				"Optical zoom on a held key, with buttery smooth transitions.", Category.VISUAL, true));
		zoomLevel = (Setting.Number) module.add(new Setting.Number("level", "Zoom level",
				"How far the view zooms in.", 4.0, 1.5, 20.0, 0.5, "x"));
		smoothness = (Setting.Number) module.add(new Setting.Number("smoothness", "Smoothness",
				"Lower is a slower, more cinematic transition.", 12.0, 2.0, 30.0, 0.5, "x"));
		sensitivityCompensation = (Setting.Toggle) module.add(new Setting.Toggle("sensitivity", "Sensitivity compensation",
				"Slows the mouse down while zoomed so aiming stays precise.", true));
		scrollAdjust = (Setting.Toggle) module.add(new Setting.Toggle("scroll", "Mouse wheel adjusts zoom",
				"Scroll while zooming to change the zoom level on the fly.", true));
		scrollStep = (Setting.Number) module.add(new Setting.Number("scroll_step", "Scroll step",
				"Zoom change per wheel notch.", 0.25, 0.05, 2.0, 0.05, "x"));
		minZoom = (Setting.Number) module.add(new Setting.Number("min", "Minimum zoom",
				"Lower bound for the wheel adjustment.", 1.5, 1.0, 10.0, 0.5, "x"));
		maxZoom = (Setting.Number) module.add(new Setting.Number("max", "Maximum zoom",
				"Upper bound for the wheel adjustment.", 20.0, 2.0, 40.0, 0.5, "x"));
		showIndicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Zoom indicator",
				"Show the current zoom level while zooming.", true));
		holdToZoom = (Setting.Toggle) module.add(new Setting.Toggle("hold", "Hold to zoom",
				"Off: the zoom key toggles instead of being held.", true));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (!isEnabled() || client.player == null) {
			stop(client);
			return;
		}
		boolean keyPressed = Keybinds.zoom != null && Keybinds.zoom.isPressed();
		if (holdToZoom.get()) {
			setActive(client, keyPressed);
		} else if (keyPressed && Keybinds.zoom.wasPressed()) {
			setActive(client, !zoomActive);
		}
		applySensitivity(client);
	}

	private static void setActive(Minecraft client, boolean active) {
		if (active == zoomActive) {
			return;
		}
		zoomActive = active;
		if (active) {
			scrollOffset = 1.0F;
		} else {
			Features.setZoomFactor(1.0F);
			restoreSensitivity(client);
		}
	}

	private static void stop(Minecraft client) {
		if (zoomActive) {
			zoomActive = false;
			Features.setZoomFactor(1.0F);
		}
		current = 1.0F;
		restoreSensitivity(client);
	}

	private static void restoreSensitivity(Minecraft client) {
		if (!sensitivityApplied) {
			return;
		}
		sensitivityApplied = false;
		if (client == null) {
			return;
		}
		try {
			client.options.sensitivity().set(previousSensitivity);
		} catch (Throwable ignored) {
			// the option is simply left as it is
		}
	}

	private static void applySensitivity(Minecraft client) {
		boolean wanted = sensitivityCompensation.get() && zoomActive;
		try {
			if (wanted && !sensitivityApplied) {
				previousSensitivity = client.options.sensitivity().get();
				sensitivityApplied = true;
			}
			if (wanted) {
				client.options.sensitivity().set(previousSensitivity * Math.sqrt(Math.max(0.05F, current)));
			} else if (sensitivityApplied) {
				restoreSensitivity(client);
			}
		} catch (Throwable ignored) {
			sensitivityApplied = false;
		}
	}

	/** Called from the mouse handler mixin while the player is in game. */
	public static void onScroll(double verticalAmount) {
		if (!zoomActive || !ModuleManager.enabled(ID) || !scrollAdjust.get()) {
			return;
		}
		float step = scrollStep.getFloat();
		float direction = verticalAmount > 0 ? 1.0F : (verticalAmount < 0 ? -1.0F : 0.0F);
		float base = (float) Math.max(1.0, zoomLevel.get());
		scrollOffset = Anim.clamp(scrollOffset * (1.0F + direction * step * 0.35F),
				(float) (minZoom.get() / base), (float) (maxZoom.get() / base));
	}

	/**
	 * The factor the renderer multiplies the field of view with ({@code < 1} means zoomed
	 * in). Called once per frame from the {@code GameRenderer} hook, which is also where the
	 * smooth transition is advanced so it matches the rendered frames exactly.
	 */
	public static float fovFactor(float partialTick) {
		if (!zoomActive || !ModuleManager.enabled(ID)) {
			current = 1.0F;
			return 1.0F;
		}
		float level = (float) Math.max(1.0, zoomLevel.get()) * scrollOffset;
		float target = 1.0F / level;
		current = Anim.approach(current, target, smoothness.getFloat(), TickClock.frameDelta());
		if (Math.abs(current - target) < 0.0015F) {
			current = target;
		}
		Features.setZoomFactor(current);
		return current;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!showIndicator.get() || !zoomActive || HudPanel.scale() <= 0.0F) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		String text = String.format(Locale.ROOT, "%.1fx", 1.0F / Math.max(0.01F, current));
		float width = font.width(text) + 8.0F;
		float x = (graphics.getScaledWindowWidth() - width) * 0.5F;
		float y = graphics.getScaledWindowHeight() - 68.0F;
		HudPanel.panel(graphics, font, x, y, width, 12.0F, 0xFF4FC3F7);
		HudPanel.text(graphics, font, text, x + 4.0F, y + 2.0F, 0xFFF2F2F7);
	}

	public static boolean isZooming() {
		return zoomActive;
	}

	public static float currentFactor() {
		return current;
	}

	@Override
	public void onDisabled() {
		stop(Minecraft.getInstance());
	}

	@Override
	public void onWorldLeave() {
		stop(Minecraft.getInstance());
	}
}
```

---

## Inventory features

### `src/main/java/dev/chaosutils/feature/inventory/ContainerPreview.java`

```java
package dev.chaosutils.feature.inventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Shulker box and bundle content preview.
 *
 * <p>Adds the contents of a container item to its tooltip - grouped, counted and with the
 * fill level. The data comes from the item's own components, which the client already has,
 * so this is a pure display feature with zero server interaction. It also works for items
 * lying in a chest, without opening anything.
 */
public final class ContainerPreview implements Feature {
	public static final String ID = "container_preview";

	/** Vanilla container size of a shulker box. */
	private static final int SHULKER_SLOTS = 27;
	/** A bundle holds 64 weight in total; a full stack always weighs 64. */
	private static final int BUNDLE_CAPACITY = 64;

	private static Module module;
	private static Setting.Toggle shulkers;
	private static Setting.Toggle bundles;
	private static Setting.Toggle groupItems;
	private static Setting.Toggle showFillLevel;
	private static Setting.Toggle showFreeSlots;
	private static Setting.Number maxLines;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Container Preview",
				"See what is inside shulker boxes and bundles without opening them.", Category.INVENTORY, true));
		shulkers = (Setting.Toggle) module.add(new Setting.Toggle("shulkers", "Shulker boxes",
				"List the contents of shulker boxes and other container items.", true));
		bundles = (Setting.Toggle) module.add(new Setting.Toggle("bundles", "Bundles",
				"List the contents of bundles.", true));
		groupItems = (Setting.Toggle) module.add(new Setting.Toggle("group", "Group identical items",
				"Show 12× Diamond instead of twelve lines.", true));
		showFillLevel = (Setting.Toggle) module.add(new Setting.Toggle("fill", "Show fill level",
				"Occupied slots (or bundle weight) in the header line.", true));
		showFreeSlots = (Setting.Toggle) module.add(new Setting.Toggle("free", "Show free space",
				"How many slots (or how much weight) are still free.", false));
		maxLines = (Setting.Number) module.add(new Setting.Number("lines", "Maximum lines",
				"Cap the preview so long tooltips stay readable.", 9.0, 3.0, 27.0, 1.0, " lines"));
	}

	/** Wires the tooltip event; called once during client initialisation. */
	public static void initEvents() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (!ModuleManager.enabled(ID)) {
				return;
			}
			try {
				appendPreview(stack, lines);
			} catch (Throwable ignored) {
				// a broken preview must never break the tooltip
			}
		});
	}

	private static void appendPreview(ItemStack stack, List<Component> lines) {
		try {
			ItemContainerContents container = stack.get(DataComponents.CONTAINER);
			if (container != null && shulkers.get()) {
				Map<String, Entry> grouped = new LinkedHashMap<>();
				for (ItemStack inner : container.nonEmptyItems()) {
					addEntry(grouped, inner);
				}
				if (!grouped.isEmpty()) {
					appendPreviewLines(lines, grouped, grouped.size(), SHULKER_SLOTS, -1, "Shulker");
					return;
				}
			}
		} catch (Throwable ignored) {
			// container component unavailable
		}
		try {
			BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundle != null && bundles.get()) {
				Map<String, Entry> grouped = new LinkedHashMap<>();
				int weight = 0;
				for (ItemStack inner : bundle.items()) {
					if (inner.isEmpty()) {
						continue;
					}
					addEntry(grouped, inner);
					weight += bundleWeight(inner);
				}
				if (!grouped.isEmpty()) {
					appendPreviewLines(lines, grouped, weight, BUNDLE_CAPACITY, weight, "Bundle");
				}
			}
		} catch (Throwable ignored) {
			// bundle component unavailable
		}
	}

	/** Vanilla bundle weight: a full stack weighs 64, so one item weighs 64 / maxStackSize. */
	private static int bundleWeight(ItemStack stack) {
		int maxStackSize = Math.max(1, stack.getMaxStackSize());
		return Math.max(1, stack.getCount() * 64 / maxStackSize);
	}

	private static void addEntry(Map<String, Entry> grouped, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		String key = stack.getHoverName().getString();
		Entry existing = grouped.get(key);
		if (existing == null) {
			grouped.put(key, new Entry(key, stack.getCount()));
		} else {
			grouped.put(key, new Entry(key, existing.count() + stack.getCount()));
		}
	}

	private record Entry(String name, int count) {
	}

	private static void appendPreviewLines(List<Component> lines, Map<String, Entry> grouped, int used,
			int capacity, int weight, String label) {
		int linesLimit = maxLines.getInt();
		if (showFillLevel.get()) {
			MutableComponent header = Component.literal(label + " ").withStyle(ChatFormatting.GRAY);
			if (weight >= 0) {
				header.append(Component.literal(weight + " / " + capacity).withStyle(
						weight >= capacity ? ChatFormatting.RED : ChatFormatting.AQUA));
			} else {
				header.append(Component.literal(used + " / " + capacity).withStyle(ChatFormatting.AQUA));
			}
			lines.add(header);
		}
		int index = 0;
		int shown = 0;
		for (Entry entry : grouped.values()) {
			if (index >= linesLimit) {
				lines.add(Component.literal("  … and " + (grouped.size() - index) + " more")
						.withStyle(ChatFormatting.DARK_GRAY));
				break;
			}
			shown += entry.count();
			MutableComponent line = Component.literal("  • ").withStyle(ChatFormatting.DARK_GRAY);
			line.append(Component.literal(entry.name()).withStyle(ChatFormatting.GRAY));
			if (groupItems.get() && entry.count() > 1) {
				line.append(Component.literal(" ×" + entry.count()).withStyle(ChatFormatting.WHITE));
			}
			lines.add(line);
			index++;
		}
		if (showFreeSlots.get()) {
			if (weight >= 0) {
				lines.add(Component.literal("  " + Math.max(0, capacity - weight) + " weight free")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else {
				lines.add(Component.literal("  " + Math.max(0, capacity - grouped.size()) + " free slots")
						.withStyle(ChatFormatting.DARK_GRAY));
			}
		}
		if (!groupItems.get()) {
			lines.add(Component.literal("  " + shown + " items").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
```

### `src/main/java/dev/chaosutils/feature/inventory/ContainerSearch.java`

```java
package dev.chaosutils.feature.inventory;

import java.lang.ref.WeakReference;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.mixin.AbstractContainerScreenAccessor;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.EnchantLookup;
import dev.chaosutils.util.Render;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Smart container search.
 *
 * <p>Adds a search field to every container screen: matching slots are outlined in the accent
 * colour and everything else is dimmed, so a full double chest becomes readable in a second.
 * The search runs purely on the client against the item data the server already sent.
 *
 * <p>The field is a vanilla text box (borderless, so ChaosUtils can style it) added through
 * Fabric's screen API, which means typing behaves exactly like vanilla and rebinding keys is
 * never an issue.
 */
public final class ContainerSearch implements Feature {
	public static final String ID = "container_search";

	private static Module module;
	private static Setting.Toggle autoFocus;
	private static Setting.Toggle showField;
	private static Setting.Toggle dimNonMatches;
	private static Setting.Number dimStrength;
	private static Setting.Color highlightColor;
	private static Setting.Toggle pulse;
	private static Setting.Choice matchMode;
	private static Setting.Toggle highlightEmpty;
	private static Setting.Number fieldWidth;

	private static EditBox field;
	private static final WeakReference<AbstractContainerScreen<?>> NONE = new WeakReference<>(null);
	private static WeakReference<AbstractContainerScreen<?>> owner = NONE;
	private static String query = "";

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Container Search",
				"Search any container: matches are highlighted, everything else is dimmed.", Category.INVENTORY, true));
		autoFocus = (Setting.Toggle) module.add(new Setting.Toggle("focus", "Focus on open",
				"Start typing right away when the container opens.", false));
		showField = (Setting.Toggle) module.add(new Setting.Toggle("field", "Show search field",
				"Draw the styled search box under the container.", true));
		dimNonMatches = (Setting.Toggle) module.add(new Setting.Toggle("dim", "Dim non-matches",
				"Darken every slot that does not match.", true));
		dimStrength = (Setting.Number) module.add(new Setting.Number("dim_strength", "Dim strength",
				"Opacity of the dimming layer.", 0.55, 0.1, 0.9, 0.05));
		highlightColor = (Setting.Color) module.add(new Setting.Color("highlight", "Highlight colour",
				"Outline colour for matching slots.", 0xFF7C5CFF));
		pulse = (Setting.Toggle) module.add(new Setting.Toggle("pulse", "Pulse",
				"Softly pulse the highlight so matches are easy to spot.", true));
		matchMode = (Setting.Choice) module.add(new Setting.Choice("match", "Match against",
				"What the query is compared with.", 0, "Item name", "Name + item id", "Name + enchantments"));
		highlightEmpty = (Setting.Toggle) module.add(new Setting.Toggle("empty", "Dim empty slots",
				"Also dim slots that hold nothing.", false));
		fieldWidth = (Setting.Number) module.add(new Setting.Number("width", "Field width",
				"Width of the search box.", 120.0, 60.0, 240.0, 5.0, "px"));
	}

	/** Wires the screen events; called once during client initialisation. */
	public static void initEvents() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof AbstractContainerScreen<?> container)) {
				return;
			}
			if (ModuleManager.enabled(ID)) {
				attach(client, container, width, height);
			}
		});
	}

	private static void attach(Minecraft client, AbstractContainerScreen<?> container, int width, int height) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) container;
		int boxWidth = fieldWidth.getInt();
		int boxHeight = 14;
		int x = accessor.chaosutils$leftPos() + (accessor.chaosutils$imageWidth() - boxWidth) / 2;
		int y = accessor.chaosutils$topPos() + accessor.chaosutils$imageHeight() + 4;
		EditBox box = new EditBox(client.font, x, y, boxWidth, boxHeight, Component.literal("Search"));
		box.setBordered(false);
		box.setTextColor(0xFFF2F2F7);
		box.setMaxLength(64);
		box.setValue(query);
		box.setResponder(value -> query = value == null ? "" : value);
		Screens.getButtons(container).add(box);
		field = box;
		owner = new WeakReference<>(container);
		if (autoFocus.get()) {
			container.setFocused(box);
			box.setFocused(true);
		}
	}

	private static void detach() {
		field = null;
		owner = NONE;
		query = "";
	}

	/** Repositions the field when the window is resized and keeps the query alive. */
	@Override
	public void onTick(Minecraft client) {
		if (client.screen instanceof AbstractContainerScreen<?> container) {
			if (field == null && ModuleManager.enabled(ID)) {
				attach(client, container, container.width, container.height);
			}
			if (field != null && Keybinds.searchContainer != null && Keybinds.searchContainer.wasPressed()) {
				container.setFocused(field);
				field.setFocused(true);
				field.setValue("");
			}
		} else if (field != null) {
			detach();
		}
	}

	/** Called from the container screen mixin after vanilla drew everything. */
	public static void renderOverlay(AbstractContainerScreen<?> container, GuiGraphics graphics, int mouseX, int mouseY) {
		if (!ModuleManager.enabled(ID)) {
			return;
		}
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) container;
		int left = accessor.chaosutils$leftPos();
		int top = accessor.chaosutils$topPos();
		int imageWidth = accessor.chaosutils$imageWidth();
		int imageHeight = accessor.chaosutils$imageHeight();
		Font font = Minecraft.getInstance().font;
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

		if (showField.get()) {
			int boxWidth = fieldWidth.getInt();
			int x = left + (imageWidth - boxWidth) / 2;
			int y = top + imageHeight + 4;
			Render.roundedRect(graphics, x - 2.0F, y - 3.0F, boxWidth + 4.0F, 16.0F, 3.0F, 0xC0101018);
			Render.roundedBorder(graphics, x - 2.0F, y - 3.0F, boxWidth + 4.0F, 16.0F, 3.0F, 1.0F,
					Render.alpha(highlightColor.get(), 0.55F), 0xC0101018);
			if (field != null && field.getValue().isEmpty() && !field.isFocused()) {
				Render.text(graphics, font, "Search…", x, y + 1.0F, 0xFF6C6C80, false);
			}
		}
		if (needle.isEmpty()) {
			return;
		}

		float pulse = pulse.get()
				? 0.65F + 0.35F * (float) Math.sin(System.nanoTime() / 400_000_000.0)
				: 1.0F;
		int highlight = Render.alpha(highlightColor.get(), Anim.clamp01(pulse));
		int dim = (int) (Anim.clamp01(dimStrength.getFloat()) * 255.0F) << 24;

		for (Slot slot : container.getMenu().slots) {
			int slotX = left + slot.x;
			int slotY = top + slot.y;
			if (slotX < left - 1 || slotX > left + imageWidth || slotY < top - 1 || slotY > top + imageHeight) {
				continue;
			}
			ItemStack stack = slot.getItem();
			boolean empty = stack.isEmpty();
			boolean matches = matches(stack, needle);
			if (matches) {
				Render.roundedRect(graphics, slotX - 1.0F, slotY - 1.0F, 18.0F, 18.0F, 3.0F,
						Render.alpha(highlightColor.get(), 0.25F * pulse));
				Render.roundedBorder(graphics, slotX - 1.0F, slotY - 1.0F, 18.0F, 18.0F, 3.0F, 1.0F, highlight, 0x00000000);
			} else if (dimNonMatches.get() && (!empty || highlightEmpty.get())) {
				Render.rect(graphics, slotX, slotY, 16.0F, 16.0F, dim);
			}
		}
	}

	private static boolean matches(ItemStack stack, String needle) {
		if (stack.isEmpty()) {
			return false;
		}
		String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
		if (name.contains(needle)) {
			return true;
		}
		int mode = matchMode.get();
		if (mode >= 1) {
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
			if (id.contains(needle)) {
				return true;
			}
		}
		if (mode >= 2) {
			try {
				for (net.minecraft.core.Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
					if (EnchantLookup.shortName(enchantment).toLowerCase(Locale.ROOT).contains(needle)) {
						return true;
					}
					String path = enchantment.unwrapKey().map(key -> key.location().getPath()).orElse("");
					if (path.toLowerCase(Locale.ROOT).contains(needle)) {
						return true;
					}
				}
			} catch (Throwable ignored) {
				// no enchantment data available
			}
		}
		return false;
	}

	@Override
	public void onDisabled() {
		query = "";
		if (field != null && owner.get() != null) {
			Screens.getButtons(owner.get()).remove(field);
		}
		detach();
	}
}
```

### `src/main/java/dev/chaosutils/feature/inventory/ItemCounter.java`

```java
package dev.chaosutils.feature.inventory;

import java.util.HashMap;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Render;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Item total counter.
 *
 * <p>Counts how many of an item you carry - optionally including the contents of every
 * shulker box and bundle - and shows the total in the item's tooltip and (optionally) above
 * each hotbar slot. The scan runs on a slow interval and writes into a reused map, so it
 * costs a few microseconds every five ticks and nothing in between.
 */
public final class ItemCounter implements Feature {
	public static final String ID = "item_counter";

	private static Module module;
	private static Setting.Toggle inTooltip;
	private static Setting.Toggle onHotbar;
	private static Setting.Toggle countContainers;
	private static Setting.Toggle typeOnly;
	private static Setting.Toggle colorCoded;
	private static Setting.Number rescanInterval;
	private static Setting.Number scale;

	private static final Map<String, Integer> COUNTS = new HashMap<>(64);
	private static final Map<String, Integer> CONTAINER_COUNTS = new HashMap<>(32);
	private static int tickCounter;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Item Counter",
				"Total count of every item you carry, in the tooltip and on the hotbar.", Category.INVENTORY, false));
		inTooltip = (Setting.Toggle) module.add(new Setting.Toggle("tooltip", "Show in tooltip",
				"Add the total to the item tooltip.", true));
		onHotbar = (Setting.Toggle) module.add(new Setting.Toggle("hotbar", "Show on hotbar",
				"Draw the total above every hotbar slot.", true));
		countContainers = (Setting.Toggle) module.add(new Setting.Toggle("containers", "Include containers",
				"Also count items inside carried shulker boxes and bundles.", true));
		typeOnly = (Setting.Toggle) module.add(new Setting.Toggle("type_only", "Count by type",
				"Ignore enchantments, names and durability when matching.", true));
		colorCoded = (Setting.Toggle) module.add(new Setting.Toggle("colors", "Colour coding",
				"Green below a stack, amber above it, red above a shulker box worth.", true));
		rescanInterval = (Setting.Number) module.add(new Setting.Number("interval", "Rescan interval",
				"Ticks between inventory scans.", 5.0, 1.0, 40.0, 1.0, "t"));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Hotbar text scale",
				"Relative size of the counts on the hotbar.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	/** Wires the tooltip event; called once during client initialisation. */
	public static void initEvents() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) -> {
			if (!ModuleManager.enabled(ID) || !inTooltip.get() || stack.isEmpty()) {
				return;
			}
			int count = countOf(stack);
			if (count <= stack.getCount()) {
				return;
			}
			MutableComponent line = Component.literal("In inventory: ").withStyle(ChatFormatting.GRAY);
			line.append(Component.literal(Integer.toString(count)).withStyle(ChatFormatting.AQUA));
			if (countContainers.get()) {
				int inside = CONTAINER_COUNTS.getOrDefault(key(stack), 0);
				if (inside > 0) {
					line.append(Component.literal("  (+" + inside + " in containers)").withStyle(ChatFormatting.DARK_GRAY));
				}
			}
			lines.add(line);
		});
	}

	private static String key(ItemStack stack) {
		if (typeOnly.get()) {
			return dev.chaosutils.util.ItemLookup.idOf(stack);
		}
		return System.identityHashCode(stack.getItem()) + "|" + stack.getComponents().toString();
	}

	private static int countOf(ItemStack stack) {
		return COUNTS.getOrDefault(key(stack), 0) + CONTAINER_COUNTS.getOrDefault(key(stack), 0);
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			COUNTS.clear();
			CONTAINER_COUNTS.clear();
			return;
		}
		if (--tickCounter > 0) {
			return;
		}
		tickCounter = Math.max(1, rescanInterval.getInt());
		scan(player);
	}

	private static void scan(Player player) {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
		Inventory inventory = player.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			COUNTS.merge(key(stack), stack.getCount(), Integer::sum);
			if (countContainers.get()) {
				scanContainer(stack, 0);
			}
		}
	}

	private static void scanContainer(ItemStack stack, int depth) {
		if (depth > 2) {
			return;
		}
		try {
			ItemContainerContents container = stack.get(DataComponents.CONTAINER);
			if (container != null) {
				for (ItemStack inner : container.nonEmptyItems()) {
					if (inner.isEmpty()) {
						continue;
					}
					CONTAINER_COUNTS.merge(key(inner), inner.getCount(), Integer::sum);
					scanContainer(inner, depth + 1);
				}
			}
		} catch (Throwable ignored) {
			// not a container item
		}
		try {
			BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
			if (bundle != null) {
				for (ItemStack inner : bundle.items()) {
					if (inner.isEmpty()) {
						continue;
					}
					CONTAINER_COUNTS.merge(key(inner), inner.getCount(), Integer::sum);
					scanContainer(inner, depth + 1);
				}
			}
		} catch (Throwable ignored) {
			// not a bundle
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || !onHotbar.get() || !dev.chaosutils.feature.hud.HudPanel.visibleNow()) {
			return;
		}
		if (client.screen != null) {
			return;
		}
		Font font = client.font;
		float scaleFactor = dev.chaosutils.feature.hud.HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		int hotbarLeft = graphics.getScaledWindowWidth() / 2 - 91;
		int hotbarTop = graphics.getScaledWindowHeight() - 22;
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			int total = countOf(stack);
			if (total <= stack.getCount()) {
				continue;
			}
			String text = Integer.toString(total);
			float x = hotbarLeft + 3 + slot * 20 + 8.0F;
			float y = hotbarTop - 9.0F * scaleFactor;
			int color = colorCoded.get() ? totalColor(total) : 0xFFF2F2F7;
			Render.text(graphics, font, text, x - font.width(text) * 0.5F, y, color, true);
		}
	}

	private static int totalColor(int total) {
		if (total >= 1728) {
			return 0xFFE05B5B;
		}
		if (total >= 64) {
			return 0xFFF0B429;
		}
		return 0xFF63D471;
	}

	@Override
	public void onWorldLeave() {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
	}

	@Override
	public void onDisabled() {
		COUNTS.clear();
		CONTAINER_COUNTS.clear();
	}
}
```

---

## Chat features

### `src/main/java/dev/chaosutils/feature/chat/ChatHistory.java`

```java
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
		if (Keybinds.chatHistory != null && Keybinds.chatHistory.wasPressed() && client.player != null) {
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
```

### `src/main/java/dev/chaosutils/feature/chat/ChatMentions.java`

```java
package dev.chaosutils.feature.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundLookup;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Chat mention highlighter.
 *
 * <p>Watches incoming chat for your name and your own keywords. A mention shows a calm,
 * animated toast with the message and plays a soft, client side notification sound - handy
 * when you are building, mining or reading a book. Nothing is intercepted, cancelled or sent:
 * the vanilla chat keeps working exactly as before, this only adds a notification layer.
 */
public final class ChatMentions implements Feature {
	public static final String ID = "chat_mentions";

	private static Module module;
	private static Setting.Text keywords;
	private static Setting.Toggle ownName;
	private static Setting.Toggle wholeWord;
	private static Setting.Toggle caseSensitive;
	private static Setting.Toggle ignoreOwnMessages;
	private static Setting.Toggle toastEnabled;
	private static Setting.Position position;
	private static Setting.Number toastSeconds;
	private static Setting.Number maxToasts;
	private static Setting.Number scale;
	private static Setting.Color accent;
	private static Setting.Toggle sound;
	private static Setting.Text soundId;
	private static Setting.Number soundPitch;
	private static Setting.Toggle alsoWhispers;

	private static final List<Toast> TOASTS = new ArrayList<>(4);

	private static final class Toast {
		private final String text;
		private final long createdAt;
		private final Anim.Value fade = new Anim.Value(0.0F, 10.0F);

		private Toast(String text) {
			this.text = text;
			this.createdAt = System.currentTimeMillis();
			this.fade.snap(0.0F);
		}
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Chat Mentions",
				"Never miss a message that mentions you - toast, sound and a keyword list.",
				Category.CHAT, true));
		keywords = (Setting.Text) module.add(new Setting.Text("keywords", "Keywords",
				"Comma separated extra words that count as a mention.", "", 256));
		ownName = (Setting.Toggle) module.add(new Setting.Toggle("own_name", "My name",
				"Treat your own player name as a keyword.", true));
		wholeWord = (Setting.Toggle) module.add(new Setting.Toggle("whole_word", "Whole words only",
				"Avoid matching your name inside other words.", true));
		caseSensitive = (Setting.Toggle) module.add(new Setting.Toggle("case", "Case sensitive",
				"Off is recommended.", false));
		ignoreOwnMessages = (Setting.Toggle) module.add(new Setting.Toggle("ignore_own", "Ignore my messages",
				"Do not notify for messages you sent yourself.", true));
		toastEnabled = (Setting.Toggle) module.add(new Setting.Toggle("toast", "Show notification",
				"Animated toast with the message that mentioned you.", true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Toast position",
				"Where mentions appear.", 0.02F, 0.35F));
		toastSeconds = (Setting.Number) module.add(new Setting.Number("duration", "Toast duration",
				"How long a mention stays on screen.", 8.0, 2.0, 30.0, 0.5, "s"));
		maxToasts = (Setting.Number) module.add(new Setting.Number("max", "Max toasts",
				"Keep the list short so it never covers the screen.", 3.0, 1.0, 8.0, 1.0));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Toast scale",
				"Relative size of the toast.", 1.0, 0.6, 1.6, 0.05, "x"));
		accent = (Setting.Color) module.add(new Setting.Color("accent", "Accent colour",
				"Colour of the toast border.", 0xFF81C784));
		sound = (Setting.Toggle) module.add(new Setting.Toggle("sound", "Notification sound",
				"Soft click when you are mentioned.", true));
		soundId = (Setting.Text) module.add(new Setting.Text("sound_id", "Sound",
				"Sound event id played for a mention.", "minecraft:entity.experience_orb.pickup", 96));
		soundPitch = (Setting.Number) module.add(new Setting.Number("pitch", "Sound pitch",
				"Pitch of the notification sound.", 1.6, 0.5, 2.0, 0.05));
		alsoWhispers = (Setting.Toggle) module.add(new Setting.Toggle("whispers", "Whisper detection",
				"Also notify for common private message wording.", true));
	}

	/** Wires the chat events; called once during client initialisation. */
	public static void initEvents() {
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			if (!ModuleManager.enabled(ID) || message == null) {
				return;
			}
			try {
				String text = message.getString();
				if (ignoreOwnMessages.get() && sender != null && Minecraft.getInstance().player != null
						&& sender.getName() != null
						&& sender.getName().equals(Minecraft.getInstance().player.getGameProfile().getName())) {
					return;
				}
				if (matches(text)) {
					notify(message);
				}
			} catch (Throwable ignored) {
				// never break chat
			}
		});
	}

	private static boolean matches(String text) {
		String haystack = caseSensitive.get() ? text : text.toLowerCase(Locale.ROOT);
		Minecraft client = Minecraft.getInstance();
		if (ownName.get() && client.player != null) {
			String name = client.player.getGameProfile().getName();
			if (name != null && !name.isEmpty() && contains(haystack, caseSensitive.get() ? name : name.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		for (String keyword : keywords.get().split(",")) {
			String trimmed = keyword.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			String needle = caseSensitive.get() ? trimmed : trimmed.toLowerCase(Locale.ROOT);
			if (contains(haystack, needle)) {
				return true;
			}
		}
		if (alsoWhispers.get()) {
			for (String marker : new String[] {"whispers to you", "whisper to you", "flüstert dir", "msg from",
					"you for help"}) {
				if (haystack.contains(caseSensitive.get() ? marker : marker.toLowerCase(Locale.ROOT))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean contains(String haystack, String needle) {
		if (needle.isEmpty()) {
			return false;
		}
		if (!wholeWord.get()) {
			return haystack.contains(needle);
		}
		int index = haystack.indexOf(needle);
		while (index >= 0) {
			boolean startOk = index == 0 || !Character.isLetterOrDigit(haystack.charAt(index - 1));
			int end = index + needle.length();
			boolean endOk = end >= haystack.length() || !Character.isLetterOrDigit(haystack.charAt(end));
			if (startOk && endOk) {
				return true;
			}
			index = haystack.indexOf(needle, index + 1);
		}
		return false;
	}

	private static void notify(Component message) {
		String plain = message.getString();
		if (plain.length() > 120) {
			plain = plain.substring(0, 119) + "…";
		}
		List<Toast> toasts = TOASTS;
		toasts.add(new Toast(plain));
		int max = Math.max(1, maxToasts.getInt());
		while (toasts.size() > max) {
			toasts.remove(0);
		}
		ChatLog.add(message, ChatLog.Kind.PLAYER, false);
		if (sound.get()) {
			playSound();
		}
	}

	private static void playSound() {
		try {
			var event = SoundLookup.get(soundId.get().trim());
			var instance = SoundLookup.ui(event, soundPitch.getFloat(), 0.6F);
			if (instance != null) {
				Minecraft.getInstance().getSoundManager().play(instance);
			}
		} catch (Throwable ignored) {
			// sound is optional
		}
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		long lifetime = (long) (toastSeconds.get() * 1000.0);
		long now = System.currentTimeMillis();
		for (int i = TOASTS.size() - 1; i >= 0; i--) {
			if (now - TOASTS.get(i).createdAt > lifetime) {
				TOASTS.remove(i);
			}
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		if (!toastEnabled.get() || TOASTS.isEmpty() || !HudPanel.visibleNow()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		Font font = client.font;
		float delta = dev.chaosutils.core.TickClock.frameDelta();
		long lifetime = (long) (toastSeconds.get() * 1000.0);
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		float lineHeight = 11.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		int index = 0;
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(TOASTS.size() * (lineHeight + 4.0F * scaleFactor)));
		for (Toast toast : TOASTS) {
			long age = System.currentTimeMillis() - toast.createdAt;
			float target = age > lifetime - 1200L ? 0.0F : 1.0F;
			toast.fade.set(target);
			toast.fade.update(delta);
			float appearance = Anim.easeOutCubic(toast.fade.get());
			if (appearance <= 0.01F) {
				continue;
			}
			String text = toast.text;
			float maxWidth = graphics.getScaledWindowWidth() * 0.45F;
			while (font.width(text) * scaleFactor > maxWidth && text.length() > 8) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			float width = font.width(text) * scaleFactor + padding * 2.0F;
			float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(width));
			float offset = (1.0F - appearance) * 8.0F;
			int color = Render.alpha(accent.get(), Anim.clamp01(appearance));
			HudPanel.panel(graphics, font, x, y + offset + index * (lineHeight + 4.0F * scaleFactor), width,
					lineHeight + padding, color);
			HudPanel.text(graphics, font, text, x + padding, y + offset + index * (lineHeight + 4.0F * scaleFactor) + padding * 0.5F,
					Render.alpha(0xFFF2F2F7, Anim.clamp01(appearance)));
			index++;
		}
	}

	@Override
	public void onWorldLeave() {
		TOASTS.clear();
	}

	@Override
	public void onDisabled() {
		TOASTS.clear();
	}
}
```

---

## Audio features

### `src/main/java/dev/chaosutils/feature/audio/SoundRadar.java`

```java
package dev.chaosutils.feature.audio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.SoundTracker;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/**
 * Advanced sound direction visualizer.
 *
 * <p>A circular radar around the crosshair showing where every positional sound of the last
 * few seconds came from: direction from the sound position, distance scaled to the ring,
 * colour by sound class, size and opacity by volume and age. A list mode is available for
 * players who prefer text.
 *
 * <p>The data comes from sounds the client is already playing - no packet, no server side
 * anything. Only positional sounds are shown, so music and UI clicks never clutter the radar.
 */
public final class SoundRadar implements Feature {
	public static final String ID = "sound_radar";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Choice style;
	private static Setting.Number radius;
	private static Setting.Number maxDistance;
	private static Setting.Number maxEntries;
	private static Setting.Number maxAge;
	private static Setting.Number minVolume;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle directionLabels;
	private static Setting.Toggle pulse;
	private static Setting.Number scale;

	private static final List<SoundTracker.Entry> CACHE = new ArrayList<>(32);

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Sound Radar",
				"A circular display that shows where important sounds came from.", Category.AUDIO, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the radar is drawn.", 0.5F, 0.62F));
		style = (Setting.Choice) module.add(new Setting.Choice("style", "Style",
				"Radar, text list or both.", 0, "Radar", "List", "Both"));
		radius = (Setting.Number) module.add(new Setting.Number("radius", "Radius",
				"Size of the radar ring in pixels.", 46.0, 20.0, 120.0, 2.0, "px"));
		maxDistance = (Setting.Number) module.add(new Setting.Number("distance", "Range",
				"Distance mapped to the outer ring.", 24.0, 4.0, 128.0, 2.0, "m"));
		maxEntries = (Setting.Number) module.add(new Setting.Number("entries", "Max sounds",
				"Maximum number of sounds shown at once.", 12.0, 1.0, 32.0, 1.0));
		maxAge = (Setting.Number) module.add(new Setting.Number("age", "Fade after",
				"Seconds a sound stays on the radar.", 3.0, 0.5, 6.0, 0.5, "s"));
		minVolume = (Setting.Number) module.add(new Setting.Number("volume", "Minimum volume",
				"Ignore sounds quieter than this.", 0.15, 0.0, 1.0, 0.05));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance_text", "Show distances",
				"Distance in metres next to the closest sounds.", true));
		directionLabels = (Setting.Toggle) module.add(new Setting.Toggle("labels", "Show directions",
				"Front / back / left / right markers on the ring.", true));
		pulse = (Setting.Toggle) module.add(new Setting.Toggle("pulse", "Pulse",
				"Very subtle pulse on the closest sound.", true));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Compact list scale",
				"Relative size of the list mode.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		if (!ModuleManager.enabled(ID) || client.player == null || !HudPanel.visibleNow()) {
			return;
		}
		float radarRadius = radius.getFloat() * HudPanel.scale();
		float centerX = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(radarRadius * 2.0F)) + radarRadius;
		float centerY = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(radarRadius * 2.0F)) + radarRadius;
		int styleValue = style.get();
		List<SoundTracker.Entry> entries = collect(client.player);
		if (entries.isEmpty() && styleValue == 1) {
			return;
		}
		if (styleValue == 0 || styleValue == 2) {
			drawRadar(graphics, entries, centerX, centerY, radarRadius);
		}
		if (styleValue == 1 || styleValue == 2) {
			drawList(graphics, entries, centerX, centerY, radarRadius);
		}
	}

	private static List<SoundTracker.Entry> collect(Player player) {
		CACHE.clear();
		List<SoundTracker.Entry> recent = SoundTracker.recent(64, true);
		double minVolumeValue = minVolume.get();
		double maxAgeValue = maxAge.get();
		double maxDistanceValue = maxDistance.get();
		for (SoundTracker.Entry entry : recent) {
			if (entry.volume() < minVolumeValue || entry.ageSeconds() > maxAgeValue) {
				continue;
			}
			if (entry.position().distanceTo(player.position()) > maxDistanceValue) {
				continue;
			}
			CACHE.add(entry);
			if (CACHE.size() >= Math.max(1, maxEntries.getInt())) {
				break;
			}
		}
		CACHE.sort(Comparator.comparingDouble(entry -> entry.position().distanceToSqr(player.position())));
		return CACHE;
	}

	private static void drawRadar(GuiGraphics graphics, List<SoundTracker.Entry> entries, float centerX, float centerY,
			float radarRadius) {
		Render.roundedBorder(graphics, centerX - radarRadius, centerY - radarRadius, radarRadius * 2.0F, radarRadius * 2.0F,
				radarRadius, Math.max(1.0F, radarRadius * 0.04F), 0x40FFFFFF, 0x00000000);
		Render.roundedBorder(graphics, centerX - radarRadius * 0.5F, centerY - radarRadius * 0.5F, radarRadius,
				radarRadius, radarRadius * 0.5F, 1.0F, 0x20FFFFFF, 0x00000000);
		Render.rect(graphics, centerX - 1.0F, centerY - 1.0F, 2.0F, 2.0F, 0x80FFFFFF);
		if (directionLabels.get()) {
			Font font = Minecraft.getInstance().font;
			Render.text(graphics, font, "F", centerX - 2.0F, centerY - radarRadius - 9.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "B", centerX - 2.0F, centerY + radarRadius + 2.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "L", centerX - radarRadius - 9.0F, centerY - 4.0F, 0x80FFFFFF, false);
			Render.text(graphics, font, "R", centerX + radarRadius + 3.0F, centerY - 4.0F, 0x80FFFFFF, false);
		}
		double maxDistance = maxDistance.get();
		Font font = Minecraft.getInstance().font;
		int index = 0;
		for (SoundTracker.Entry entry : entries) {
			float bearing = Projection.bearingTo(entry.position().x, entry.position().z);
			double distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
			float fraction = (float) Math.min(1.0, distance / Math.max(1.0, maxDistance));
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			float ageFade = Anim.clamp01(1.0F - entry.ageSeconds() / (float) Math.max(0.5, maxAge.get()));
			float size = 3.0F + Anim.clamp01(entry.volume()) * 2.5F;
			if (pulse.get() && index == 0) {
				size += 1.0F + (float) Math.sin(System.nanoTime() / 250_000_000.0);
			}
			float radians = (float) Math.toRadians(bearing);
			float x = centerX + (float) Math.sin(radians) * radarRadius * fraction;
			float y = centerY - (float) Math.cos(radians) * radarRadius * fraction;
			int color = Render.alpha(kind.color(), Anim.clamp01(0.25F + ageFade * 0.75F));
			Render.rect(graphics, x - size * 0.5F, y - size * 0.5F, size, size, color);
			if (showDistance.get() && index < 3) {
				String text = Math.round(distance) + "m";
				Render.text(graphics, font, text, x + size, y - 4.0F, Render.alpha(0xFFFFFFFF, Anim.clamp01(ageFade)), true);
			}
			index++;
		}
	}

	private static void drawList(GuiGraphics graphics, List<SoundTracker.Entry> entries, float centerX, float centerY,
			float radarRadius) {
		if (entries.isEmpty()) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		Font font = client.font;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		float lineHeight = 10.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		int limit = Math.min(entries.size(), Math.max(1, maxEntries.getInt()));
		float width = 60.0F * scaleFactor;
		for (SoundTracker.Entry entry : entries) {
			String text = SoundClasses.prettyName(entry.path());
			width = Math.max(width, font.width(text) * scaleFactor + 32.0F * scaleFactor);
		}
		float height = limit * lineHeight + padding * 2.0F;
		float x = centerX + radarRadius + 6.0F * scaleFactor;
		float y = centerY - height * 0.5F;
		if (style.get() == 1) {
			x = centerX - width * 0.5F;
		}
		HudPanel.panel(graphics, font, x, y, width, height, 0xFF4FC3F7);
		float cursorY = y + padding;
		int index = 0;
		for (SoundTracker.Entry entry : entries) {
			if (index >= limit) {
				break;
			}
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			float ageFade = Anim.clamp01(1.0F - entry.ageSeconds() / (float) Math.max(0.5, maxAge.get()));
			int color = Render.alpha(kind.color(), Anim.clamp01(0.35F + ageFade * 0.65F));
			double distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
			String text = SoundClasses.symbol(kind) + " " + SoundClasses.prettyName(entry.path());
			HudPanel.text(graphics, font, text, x + padding, cursorY, color);
			String range = Math.round(distance) + "m";
			HudPanel.text(graphics, font, range, x + width - padding - font.width(range), cursorY, 0xFFBFC2CF);
			cursorY += lineHeight;
			index++;
		}
	}

	@Override
	public void onWorldLeave() {
		SoundTracker.clear();
	}
}
```

### `src/main/java/dev/chaosutils/feature/audio/SubtitlesPlus.java`

```java
package dev.chaosutils.feature.audio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.core.SoundTracker;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.feature.hud.HudPanel;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Projection;
import dev.chaosutils.util.Render;
import dev.chaosutils.util.SoundClasses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

/**
 * Subtitle HUD enhancer.
 *
 * <p>Replaces the cramped bottom-of-screen subtitle list with a readable panel: one line per
 * distinct sound, grouped repetitions ("Zombie Growl ×3"), colour per sound class, a
 * direction arrow and the distance. Because the list is built from the sounds the client
 * played anyway, it works identically in singleplayer and on servers.
 *
 * <p>The vanilla subtitle list is switched off through the regular vanilla option while this
 * feature is active and switched back afterwards, so you never see both at once.
 */
public final class SubtitlesPlus implements Feature {
	public static final String ID = "subtitles_plus";

	private static Module module;
	private static Setting.Position position;
	private static Setting.Number maxLines;
	private static Setting.Number maxAge;
	private static Setting.Number minVolume;
	private static Setting.Toggle groupDuplicates;
	private static Setting.Toggle showDirection;
	private static Setting.Toggle showDistance;
	private static Setting.Toggle onlyPositional;
	private static Setting.Toggle hideVanilla;
	private static Setting.Toggle importantOnly;
	private static Setting.Number scale;

	private static boolean vanillaWasOn;
	private static boolean vanillaChanged;

	private static final Map<String, Row> ROWS = new LinkedHashMap<>();

	private static final class Row {
		private final String path;
		private final String name;
		private int count;
		private long lastAt;
		private float bearing;
		private double distance;
		private SoundClasses.Kind kind;
	}

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Enhanced Subtitles",
				"Readable subtitle panel with colours, grouping, direction and distance.", Category.AUDIO, true));
		position = (Setting.Position) module.add(new Setting.Position("position", "Position",
				"Where the subtitle panel is drawn.", 0.02F, 0.78F));
		maxLines = (Setting.Number) module.add(new Setting.Number("lines", "Maximum lines",
				"How many distinct sounds are listed at once.", 5.0, 1.0, 12.0, 1.0));
		maxAge = (Setting.Number) module.add(new Setting.Number("age", "Fade after",
				"Seconds a line stays visible.", 2.5, 0.5, 8.0, 0.5, "s"));
		minVolume = (Setting.Number) module.add(new Setting.Number("volume", "Minimum volume",
				"Ignore very quiet sounds.", 0.2, 0.0, 1.0, 0.05));
		groupDuplicates = (Setting.Toggle) module.add(new Setting.Toggle("group", "Group repeats",
				"Show a counter instead of the same line several times.", true));
		showDirection = (Setting.Toggle) module.add(new Setting.Toggle("direction", "Direction arrow",
				"Where the sound came from, relative to where you look.", true));
		showDistance = (Setting.Toggle) module.add(new Setting.Toggle("distance", "Distance",
				"Distance in metres.", true));
		onlyPositional = (Setting.Toggle) module.add(new Setting.Toggle("positional", "Positional only",
				"Ignore sounds without a position in the world.", true));
		hideVanilla = (Setting.Toggle) module.add(new Setting.Toggle("hide_vanilla", "Hide vanilla subtitles",
				"Turn the vanilla subtitle list off while this panel is active.", true));
		importantOnly = (Setting.Toggle) module.add(new Setting.Toggle("important", "Important sounds only",
				"Skip ambient and unclassified sounds.", false));
		scale = (Setting.Number) module.add(new Setting.Number("scale", "Scale",
				"Relative size of the panel.", 1.0, 0.6, 1.6, 0.05, "x"));
	}

	@Override
	public boolean supportsHud() {
		return true;
	}

	@Override
	public void onTick(Minecraft client) {
		if (client.player == null) {
			ROWS.clear();
			return;
		}
		if (!isEnabled()) {
			restoreVanilla(client);
			ROWS.clear();
			return;
		}
		applyVanilla(client);
		long now = System.currentTimeMillis();
		double ageLimit = maxAge.get() * 1000.0;
		List<SoundTracker.Entry> recent = SoundTracker.recent(48, onlyPositional.get());
		for (SoundTracker.Entry entry : recent) {
			if (entry.volume() < minVolume.get()) {
				continue;
			}
			SoundClasses.Kind kind = SoundClasses.classify(entry.path());
			if (importantOnly.get() && (kind == SoundClasses.Kind.AMBIENT || kind == SoundClasses.Kind.OTHER
					|| kind == SoundClasses.Kind.MUSIC)) {
				continue;
			}
			long age = now - entry.timeMillis();
			if (age > ageLimit) {
				continue;
			}
			Row row = ROWS.get(entry.path());
			if (row == null) {
				if (ROWS.size() >= 24) {
					continue;
				}
				row = new Row(entry.path());
				ROWS.put(entry.path(), row);
			}
			row.count++;
			row.lastAt = entry.timeMillis();
			row.kind = kind;
			row.bearing = Projection.bearingTo(entry.position().x, entry.position().z);
			row.distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
		}
		// Drop expired rows so the map can never grow.
		ROWS.values().removeIf(row -> now - row.lastAt > ageLimit);
	}

	private static void applyVanilla(Minecraft client) {
		if (!hideVanilla.get() || vanillaChanged) {
			if (!hideVanilla.get() && vanillaChanged) {
				restoreVanilla(client);
			}
			return;
		}
		try {
			vanillaWasOn = client.options.showSubtitles().get();
			if (vanillaWasOn) {
				client.options.showSubtitles().set(false);
				vanillaChanged = true;
			}
		} catch (Throwable ignored) {
			vanillaChanged = false;
		}
	}

	private static void restoreVanilla(Minecraft client) {
		if (!vanillaChanged) {
			return;
		}
		vanillaChanged = false;
		try {
			client.options.showSubtitles().set(vanillaWasOn);
		} catch (Throwable ignored) {
			// leave the option as it is
		}
	}

	@Override
	public void onHudRender(GuiGraphics graphics, float partialTick) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (player == null || ROWS.isEmpty() || !HudPanel.visibleNow()) {
			return;
		}
		Font font = client.font;
		float scaleFactor = HudPanel.scale() * scale.getFloat();
		if (scaleFactor <= 0.05F) {
			return;
		}
		float lineHeight = 11.0F * scaleFactor;
		float padding = HudPanel.padding() * scaleFactor;
		long now = System.currentTimeMillis();
		double ageLimit = maxAge.get() * 1000.0;

		List<Row> rows = new ArrayList<>(ROWS.values());
		rows.sort((a, b) -> Long.compare(b.lastAt, a.lastAt));
		int limit = Math.max(1, maxLines.getInt());
		if (rows.size() > limit) {
			rows = rows.subList(0, limit);
		}
		float width = 80.0F * scaleFactor;
		for (Row row : rows) {
			width = Math.max(width, font.width(caption(row)) * scaleFactor + padding * 2.0F + 26.0F * scaleFactor);
		}
		float height = rows.size() * lineHeight + padding * 2.0F;
		float x = position.get().screenX(graphics.getScaledWindowWidth(), Math.round(width));
		float y = position.get().screenY(graphics.getScaledWindowHeight(), Math.round(height));
		HudPanel.panel(graphics, font, x, y, width, height, 0xFF4FC3F7);
		float cursorY = y + padding;
		for (Row row : rows) {
			float ageFade = Anim.clamp01(1.0F - (now - row.lastAt) / (float) ageLimit);
			int color = Render.alpha(row.kind == null ? 0xFFF2F2F7 : row.kind.color(),
					Anim.clamp01(0.35F + ageFade * 0.65F));
			String caption = caption(row);
			HudPanel.text(graphics, font, caption, x + padding, cursorY, color);
			String suffix = suffix(row);
			HudPanel.text(graphics, font, suffix, x + width - padding - font.width(suffix), cursorY,
					Render.alpha(0xFFBFC2CF, Anim.clamp01(ageFade)));
			cursorY += lineHeight;
		}
	}

	private static String caption(Row row) {
		String symbol = row.kind == null ? "-" : SoundClasses.symbol(row.kind);
		String text = symbol + " " + SoundClasses.prettyName(row.path);
		if (groupDuplicates.get() && row.count > 1) {
			text = text + " ×" + row.count;
		}
		return text;
	}

	private static String suffix(Row row) {
		StringBuilder builder = new StringBuilder(12);
		if (showDirection.get()) {
			builder.append(arrow(row.bearing));
		}
		if (showDistance.get()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(Math.round(row.distance)).append('m');
		}
		return builder.toString();
	}

	private static String arrow(float bearing) {
		float normalized = bearing;
		while (normalized <= -180.0F) {
			normalized += 360.0F;
		}
		while (normalized > 180.0F) {
			normalized -= 360.0F;
		}
		if (normalized >= -22.5F && normalized < 22.5F) {
			return "^";
		}
		if (normalized >= 22.5F && normalized < 67.5F) {
			return "/";
		}
		if (normalized >= 67.5F && normalized < 112.5F) {
			return ">";
		}
		if (normalized >= 112.5F && normalized < 157.5F) {
			return "\\";
		}
		if (normalized >= -67.5F && normalized < -22.5F) {
			return "\\";
		}
		if (normalized >= -112.5F && normalized < -67.5F) {
			return "<";
		}
		if (normalized >= -157.5F && normalized < -112.5F) {
			return "/";
		}
		return "v";
	}

	@Override
	public void onDisabled() {
		restoreVanilla(Minecraft.getInstance());
		ROWS.clear();
	}

	@Override
	public void onWorldLeave() {
		ROWS.clear();
	}
}
```

### `src/main/java/dev/chaosutils/feature/audio/VolumeDucker.java`

```java
package dev.chaosutils.feature.audio;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import dev.chaosutils.util.Anim;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/**
 * Unfocused volume reducer.
 *
 * <p>When the window loses focus (alt tabbed, second monitor, a browser in front) the volume
 * fades down to a configurable level and fades back the moment you return. Only the vanilla
 * volume options are driven - the values are stored when the transition starts and restored
 * exactly, so your sound settings survive untouched and nothing is written unless the fade is
 * actually running.
 */
public final class VolumeDucker implements Feature {
	public static final String ID = "volume_ducker";

	private static Module module;
	private static Setting.Number duckVolume;
	private static Setting.Number fadeSeconds;
	private static Setting.Choice scope;
	private static Setting.Toggle alsoPauseMusic;
	private static Setting.Toggle restoreOnLeave;
	private static Setting.Toggle indicator;

	private static final SoundSource[] ALL_SOURCES = {SoundSource.MASTER, SoundSource.MUSIC, SoundSource.RECORDS,
			SoundSource.WEATHER, SoundSource.BLOCKS, SoundSource.HOSTILE, SoundSource.NEUTRAL, SoundSource.PLAYERS,
			SoundSource.AMBIENT, SoundSource.VOICE, SoundSource.UI};

	private static final double[] ORIGINAL = new double[ALL_SOURCES.length];
	private static boolean captured;
	private static float currentFactor = 1.0F;
	private static float targetFactor = 1.0F;
	private static boolean lastFocused = true;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Unfocused Volume",
				"Turns the game down while the window is unfocused and back up when you return.",
				Category.AUDIO, true));
		duckVolume = (Setting.Number) module.add(new Setting.Number("volume", "Volume while unfocused",
				"Fraction of your normal volume.", 0.25, 0.0, 1.0, 0.05));
		fadeSeconds = (Setting.Number) module.add(new Setting.Number("fade", "Fade time",
				"How long the fade takes.", 0.6, 0.0, 5.0, 0.1, "s"));
		scope = (Setting.Choice) module.add(new Setting.Choice("scope", "Affected channels",
				"Which sound channels are turned down.", 0, "Master only", "Music & records", "Everything"));
		alsoPauseMusic = (Setting.Toggle) module.add(new Setting.Toggle("pause_music", "Also pause music",
				"Stop the music while unfocused instead of only fading it.", false));
		restoreOnLeave = (Setting.Toggle) module.add(new Setting.Toggle("restore", "Always restore on quit",
				"Make sure the original volumes are written back when the game closes.", true));
		indicator = (Setting.Toggle) module.add(new Setting.Toggle("indicator", "Show indicator",
				"Small badge in the corner of the screen while ducked.", false));
	}

	@Override
	public boolean supportsHud() {
		return indicator.get();
	}

	@Override
	public void onTick(Minecraft client) {
		if (!isEnabled() || client.options == null) {
			restore(client, true);
			return;
		}
		boolean focused = client.isWindowActive();
		if (focused != lastFocused) {
			lastFocused = focused;
			targetFactor = focused ? 1.0F : duckVolume.getFloat();
			if (!captured) {
				capture(client);
			}
		}
		float speed = fadeSeconds.getFloat() <= 0.01F ? 60.0F : 1.0F / Math.max(0.05F, fadeSeconds.getFloat()) * 4.0F;
		currentFactor = Anim.approach(currentFactor, targetFactor, speed, 0.05F);
		if (Math.abs(currentFactor - targetFactor) < 0.002F) {
			currentFactor = targetFactor;
		}
		if (captured) {
			apply(client);
		}
		if (alsoPauseMusic.get() && !focused) {
			try {
				client.getSoundManager().pause();
			} catch (Throwable ignored) {
				// nothing to pause
			}
		} else if (alsoPauseMusic.get()) {
			try {
				client.getSoundManager().resume();
			} catch (Throwable ignored) {
				// nothing to resume
			}
		}
	}

	private static void capture(Minecraft client) {
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			ORIGINAL[i] = read(client, ALL_SOURCES[i]);
		}
		captured = true;
	}

	private static double read(Minecraft client, SoundSource source) {
		try {
			Double value = client.options.getSoundSourceOptionInstance(source).get();
			return value == null ? 1.0 : value;
		} catch (Throwable ignored) {
			return 1.0;
		}
	}

	private static void apply(Minecraft client) {
		int selectedScope = scope.get();
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			SoundSource source = ALL_SOURCES[i];
			if (!affected(source, selectedScope)) {
				continue;
			}
			try {
				double value = ORIGINAL[i] * currentFactor;
				client.options.getSoundSourceOptionInstance(source).set(Math.max(0.0, Math.min(1.0, value)));
			} catch (Throwable ignored) {
				// option unavailable on this build - skip it
			}
		}
	}

	private static boolean affected(SoundSource source, int selectedScope) {
		return switch (selectedScope) {
			case 0 -> source == SoundSource.MASTER;
			case 1 -> source == SoundSource.MUSIC || source == SoundSource.RECORDS || source == SoundSource.MASTER;
			default -> true;
		};
	}

	private static void restore(Minecraft client, boolean force) {
		if (!captured) {
			return;
		}
		if (!force && currentFactor == targetFactor && targetFactor == 1.0F) {
			captured = false;
			return;
		}
		for (int i = 0; i < ALL_SOURCES.length; i++) {
			try {
				client.options.getSoundSourceOptionInstance(ALL_SOURCES[i]).set(ORIGINAL[i]);
			} catch (Throwable ignored) {
				// nothing to restore for this channel
			}
		}
		captured = false;
		currentFactor = 1.0F;
		targetFactor = 1.0F;
	}

	@Override
	public void onHudRender(net.minecraft.client.gui.GuiGraphics graphics, float partialTick) {
		if (!indicator.get() || !captured || currentFactor > 0.999F) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		String text = "Volume " + Math.round(currentFactor * 100.0F) + "%";
		float width = client.font.width(text) + 8.0F;
		dev.chaosutils.feature.hud.HudPanel.panel(graphics, client.font, 6.0F, 20.0F, width, 12.0F, 0xFFF06292);
		dev.chaosutils.feature.hud.HudPanel.text(graphics, client.font, text, 10.0F, 22.0F, 0xFFF2F2F7);
	}

	@Override
	public void onDisabled() {
		restore(Minecraft.getInstance(), true);
	}

	@Override
	public void onWorldLeave() {
		if (restoreOnLeave.get()) {
			restore(Minecraft.getInstance(), true);
		}
	}
}
```

---

## QoL & performance

### `src/main/java/dev/chaosutils/feature/qol/ScreenshotManager.java`

```java
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
		if (Keybinds.screenshotPopup != null && Keybinds.screenshotPopup.wasPressed()) {
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
		while (font.width(text) * scaleFactor > graphics.getScaledWindowWidth() * 0.5F && text.length() > 8) {
			text = text.substring(0, text.length() - 2) + "…";
		}
		String hint = "  [press the screenshot key]";
		float width = (font.width(text) + font.width(hint)) * scaleFactor + HudPanel.padding() * 4.0F;
		float height = 14.0F * scaleFactor + HudPanel.padding() * 2.0F;
		float x = (graphics.getScaledWindowWidth() - width) * 0.5F;
		float y = graphics.getScaledWindowHeight() - 90.0F - (1.0F - appearance) * 8.0F;
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
```

### `src/main/java/dev/chaosutils/feature/qol/ThemeModule.java`

```java
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
```

### `src/main/java/dev/chaosutils/feature/performance/ParticleReducer.java`

```java
package dev.chaosutils.feature.performance;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import dev.chaosutils.config.Category;
import dev.chaosutils.config.Module;
import dev.chaosutils.config.ModuleManager;
import dev.chaosutils.config.Setting;
import dev.chaosutils.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Particle &amp; explosion reducer.
 *
 * <p>Hiding particles happens after the server told the client about them, purely on the
 * client: no packet is delayed, dropped or altered. It is the single most effective
 * client side FPS win in busy fights, where explosions and smoke dominate the particle
 * budget.
 *
 * <p>The lookup set is rebuilt only when a setting actually changes and is stored as plain
 * path strings, so the check inside the particle engine costs one hash lookup.
 */
public final class ParticleReducer implements Feature {
	public static final String ID = "particle_reducer";

	private static Module module;
	private static Setting.Toggle reduceExplosions;
	private static Setting.Toggle hideSmoke;
	private static Setting.Toggle hideFlame;
	private static Setting.Toggle hideBubbles;
	private static Setting.Toggle hideCrit;
	private static Setting.Toggle hideAmbient;
	private static Setting.Toggle hideItemBreak;
	private static Setting.Number densityLimit;
	private static Setting.Text extraHidden;

	private static Set<String> hidden = new HashSet<>();
	private static volatile String signature = "\u0000";
	private static int budget;
	private static int budgetUsed;

	@Override
	public Module module() {
		return module;
	}

	public static void register() {
		module = ModuleManager.register(new Module(ID, "Particle Reducer",
				"Hide particle types you never look at - big FPS win, zero gameplay impact.", Category.PERFORMANCE, false));
		reduceExplosions = add(new Setting.Toggle("explosions", "Reduce explosions",
				"Hide the huge explosion and poof clouds.", true));
		hideSmoke = add(new Setting.Toggle("smoke", "Hide smoke",
				"Smoke, campfire smoke and cloud puffs.", true));
		hideFlame = add(new Setting.Toggle("flame", "Hide flame & soul fire",
				"Torch, fire and soul fire flames.", false));
		hideBubbles = add(new Setting.Toggle("bubbles", "Hide bubbles",
				"Water bubbles and splash particles.", false));
		hideCrit = add(new Setting.Toggle("crit", "Hide crit & sweep",
				"Critical hit and sweep attack sparks.", false));
		hideAmbient = add(new Setting.Toggle("ambient", "Hide ambient",
				"Falling leaves, spores, ash and dust motes.", true));
		hideItemBreak = add(new Setting.Toggle("item_break", "Hide item break",
				"The tiny item-break and totem particles.", false));
		densityLimit = (Setting.Number) module.add(new Setting.Number("density", "Density limit",
				"Maximum particles spawned per tick (0 = unlimited). Excess particles are simply not created.",
				0.0, 0.0, 2000.0, 50.0));
		extraHidden = (Setting.Text) module.add(new Setting.Text("extra", "Extra hidden types",
				"Comma separated particle ids, e.g. minecraft:heart,minecraft:note", "", 512));
		for (Setting<?> setting : new Setting<?>[] {reduceExplosions, hideSmoke, hideFlame, hideBubbles, hideCrit, hideAmbient,
				hideItemBreak, densityLimit, extraHidden}) {
			setting.onChanged(value -> signature = "\u0000");
		}
	}

	private static Setting<?> add(Setting<?> setting) {
		return module.add(setting);
	}

	private static void rebuild() {
		StringBuilder builder = new StringBuilder(64);
		builder.append(reduceExplosions.get()).append(hideSmoke.get()).append(hideFlame.get()).append(hideBubbles.get())
				.append(hideCrit.get()).append(hideAmbient.get()).append(hideItemBreak.get()).append(extraHidden.get());
		String newSignature = builder.toString();
		if (newSignature.equals(signature)) {
			return;
		}
		signature = newSignature;
		Set<String> types = new HashSet<>();
		if (reduceExplosions.get()) {
			types.add("explosion");
			types.add("explosion_emitter");
			types.add("poof");
			types.add("flash");
			types.add("sonic_boom");
		}
		if (hideSmoke.get()) {
			types.add("smoke");
			types.add("large_smoke");
			types.add("campfire_cosy_smoke");
			types.add("campfire_signal_smoke");
			types.add("cloud");
			types.add("squid_ink");
			types.add("white_smoke");
		}
		if (hideFlame.get()) {
			types.add("flame");
			types.add("soul_fire_flame");
			types.add("small_flame");
			types.add("soul");
		}
		if (hideBubbles.get()) {
			types.add("bubble");
			types.add("bubble_pop");
			types.add("splash");
			types.add("fishing");
			types.add("nautilus");
			types.add("dripping_water");
			types.add("falling_water");
		}
		if (hideCrit.get()) {
			types.add("crit");
			types.add("enchanted_hit");
			types.add("sweep_attack");
			types.add("damage_indicator");
			types.add("attack_indicator");
		}
		if (hideAmbient.get()) {
			types.add("falling_leaves");
			types.add("spore_blossom_air");
			types.add("ash");
			types.add("white_ash");
			types.add("crimson_spore");
			types.add("warped_spore");
			types.add("dust");
			types.add("dust_color_transition");
		}
		if (hideItemBreak.get()) {
			types.add("item");
			types.add("item_slime");
			types.add("item_snowball");
			types.add("totem_of_undying");
		}
		for (String extra : extraHidden.get().split(",")) {
			String trimmed = extra.trim().toLowerCase(Locale.ROOT);
			if (trimmed.isEmpty()) {
				continue;
			}
			int colon = trimmed.indexOf(':');
			types.add(colon >= 0 ? trimmed.substring(colon + 1) : trimmed);
		}
		hidden = types;
		double limit = densityLimit.get();
		budget = limit < 1.0 ? 0 : (int) Math.round(limit);
	}

	/** Called from the particle engine mixin; must stay allocation free in the common case. */
	public static boolean hides(ParticleOptions options) {
		if (!ModuleManager.enabled(ID)) {
			return false;
		}
		try {
			if (budget > 0) {
				if (budgetUsed >= budget) {
					return true;
				}
				budgetUsed++;
			}
			rebuild();
			if (hidden.isEmpty() || options == null) {
				return false;
			}
			Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
			return id != null && hidden.contains(id.getPath());
		} catch (Throwable ignored) {
			return false;
		}
	}

	/** Called from the feature's world join so a stale budget can never block all particles. */
	public static void resetBudget() {
		budgetUsed = 0;
	}

	@Override
	public void onTick(Minecraft client) {
		resetBudget();
		if (isEnabled()) {
			rebuild();
		}
	}

	@Override
	public void onWorldJoin() {
		signature = "\u0000";
		resetBudget();
	}

	@Override
	public void onWorldLeave() {
		resetBudget();
	}

	/** Exposed for the GUI: how many particle types are currently hidden. */
	public static int hiddenTypeCount() {
		rebuild();
		return hidden.size();
	}
}
```

---

## Mixins

### `src/main/java/dev/chaosutils/mixin/AbstractContainerScreenAccessor.java`

```java
package dev.chaosutils.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads the container screen layout (GUI origin and image size).
 *
 * <p>These fields are {@code protected} in vanilla, and the search overlay is drawn from a
 * mixin outside the class hierarchy, so an accessor is the clean way to get at them
 * without widening anything else.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int chaosutils$leftPos();

	@Accessor("topPos")
	int chaosutils$topPos();

	@Accessor("imageWidth")
	int chaosutils$imageWidth();

	@Accessor("imageHeight")
	int chaosutils$imageHeight();
}
```

### `src/main/java/dev/chaosutils/mixin/AbstractContainerScreenMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.inventory.ContainerSearch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the container search overlay on top of any container screen.
 *
 * <p>The mixin only paints (dimming non matching slots, outlining matches). Typing into the
 * search field is handled by the vanilla text field widget that
 * {@code ScreenEvents.AFTER_INIT} adds on screens that accept it.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private void chaosutils$containerSearchOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
		ApiCompat.seen("container.render");
		try {
			ContainerSearch.renderOverlay((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY);
		} catch (Throwable ignored) {
			// A rendering helper must never crash a container screen.
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/CameraMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.PerspectiveLock;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the perspective lock / camera preview rotation.
 *
 * <p>The camera is rotated after vanilla computed it; the player's own body, hitbox and
 * movement are never touched, which keeps the feature purely cosmetic and safe on servers.
 * The rotation is also remembered so {@code Projection} can keep HUD markers aligned with
 * what the player actually sees.
 */
@Mixin(Camera.class)
public class CameraMixin {
	@Inject(method = "setup", at = @At("TAIL"), require = 0)
	private void chaosutils$lockRotation(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo info) {
		ApiCompat.seen("camera.setup");
		if (!PerspectiveLock.isActive()) {
			return;
		}
		try {
			PerspectiveLock.applyToCamera((Camera) (Object) this, entity, partialTick);
		} catch (Throwable ignored) {
			// Keep the vanilla camera if anything goes wrong.
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/DebugScreenOverlayMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.hud.CompactDebugOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the wall of text that F3 produces with the compact ChaosUtils overlay.
 *
 * <p>When the feature is off nothing changes; when it is on, vanilla is cancelled and the
 * compact panel (coordinates, direction, biome, live time, TPS, ping) is drawn instead.
 * The full overlay is one key press away, so nothing is lost.
 */
@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$compactOverlay(GuiGraphics graphics, CallbackInfo info) {
		ApiCompat.seen("debug.render");
		try {
			if (CompactDebugOverlay.replacesVanilla()) {
				info.cancel();
			}
		} catch (Throwable ignored) {
			// fall back to the vanilla overlay
		}
	}

	@Inject(method = "render", at = @At("TAIL"), require = 0)
	private void chaosutils$compactOverlayDraw(GuiGraphics graphics, CallbackInfo info) {
		try {
			CompactDebugOverlay.renderExtra(graphics);
		} catch (Throwable ignored) {
			// nothing to do
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/EntityMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.Features;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cache hygiene: every ChaosUtils feature that keeps per-entity animation state drops it
 * here, so a long session in a busy world can never grow the caches without bound.
 */
@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "remove", at = @At("HEAD"), require = 0)
	private void chaosutils$dropEntityState(Entity.RemovalReason reason, CallbackInfo info) {
		ApiCompat.seen("entity.remove");
		try {
			Features.pruneEntityCaches(((Entity) (Object) this).getId());
		} catch (Throwable ignored) {
			// caches are best-effort
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/GameRendererMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.SmoothZoom;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the smooth zoom by scaling the field of view vanilla just computed.
 *
 * <p>Scaling the FOV instead of moving the camera means the zoom is a pure optical effect:
 * no player state, no hitbox and no movement is involved, and the server never learns about
 * it. Mouse sensitivity stays untouched as well.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
	private void chaosutils$zoomFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> info) {
		ApiCompat.seen("renderer.fov");
		try {
			double factor = SmoothZoom.fovFactor(partialTick);
			if (factor < 0.9999) {
				info.setReturnValue(info.getReturnValue() * factor);
			}
		} catch (Throwable ignored) {
			// keep the vanilla FOV
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/GuiMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.CrosshairDesigner;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla crosshair while the ChaosUtils designer is drawing its own.
 *
 * <p>Only the crosshair is cancelled, and only when the feature is enabled - everything
 * else in the HUD keeps running untouched.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$replaceCrosshair(GuiGraphics graphics, CallbackInfo info) {
		ApiCompat.seen("gui.crosshair");
		try {
			if (CrosshairDesigner.replacesVanilla()) {
				info.cancel();
			}
		} catch (Throwable ignored) {
			// draw the vanilla crosshair instead
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/MouseHandlerMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.SmoothZoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands the raw scroll delta to the zoom feature so the wheel can adjust the zoom level
 * while the zoom key is held. Nothing is cancelled and nothing is injected - the vanilla
 * handler still does exactly what it did before.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), require = 0)
	private void chaosutils$scroll(long window, double horizontal, double vertical, CallbackInfo info) {
		ApiCompat.seen("mouse.scroll");
		try {
			SmoothZoom.onScroll(vertical);
		} catch (Throwable ignored) {
			// zoom simply keeps its current level
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/ParticleEngineMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.performance.ParticleReducer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the particle reducer drop particles the user does not want to see.
 *
 * <p>Purely visual: hiding a particle client side changes nothing about the world state the
 * server knows, and it happens after the server told us about the particle - no packets are
 * sent or suppressed.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$filterParticle(ParticleOptions options, ClientLevel level, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, RandomSource random, CallbackInfoReturnable<Particle> info) {
		ApiCompat.seen("particle.create");
		try {
			if (ParticleReducer.hides(options)) {
				info.setReturnValue(null);
			}
		} catch (Throwable ignored) {
			// never break particle creation
		}
	}
}
```

### `src/main/java/dev/chaosutils/mixin/SoundEngineMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.core.SoundTracker;
import dev.chaosutils.feature.Features;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records every sound the client is about to play.
 *
 * <p>This is the single source for the sound direction radar, the enhanced subtitles and
 * the smarter mention/notification logic. It only observes: nothing is cancelled here and
 * no packet is ever sent.
 */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {
	@Inject(method = "play", at = @At("HEAD"), require = 0)
	private void chaosutils$captureSound(SoundInstance sound, CallbackInfo info) {
		ApiCompat.seen("sound.play");
		if (!Features.anySoundConsumerEnabled()) {
			return;
		}
		try {
			SoundTracker.push(
					sound.getLocation().toString(),
					new net.minecraft.world.phys.Vec3(sound.getX(), sound.getY(), sound.getZ()),
					sound.getSource() == null ? "master" : sound.getSource().getName(),
					sound.getVolume(),
					sound.getPitch(),
					sound.isRelative());
		} catch (Throwable ignored) {
			// Never let a HUD helper break sound playback.
		}
	}
}
```
