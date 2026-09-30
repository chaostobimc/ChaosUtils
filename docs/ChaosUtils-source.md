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
	// Report every error in one run (javac stops at 100 by default) and show the detail lines -
	// this makes the first build after a Minecraft update a single pass instead of several.
	it.options.compilerArgs += ['-Xmaxerrs', '2000', '-Xdiags:verbose']
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
loom_version=1.14.10
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
distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f
networkTimeout=60000
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
		"CameraAccessor",
		"CameraMixin",
		"DebugScreenOverlayMixin",
		"EntityMixin",
		"GameRendererMixin",
		"GuiMixin",
		"KeyMappingAccessor",
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
import dev.chaosutils.util.InputUtil;
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
	/** Shown in the GUI header and logged on startup, so the running build is identifiable. */
	public static final String BUILD_TAG = "ui-3";
	public static final Logger LOGGER = LoggerFactory.getLogger("ChaosUtils");

	private static final Set<Category> HIDDEN_CATEGORIES = EnumSet.noneOf(Category.class);
	private static boolean overlaysHidden;
	private static boolean wasInWorld;
	/** Edge detection for the interface key; polled physically so it also fires inside a screen. */
	private static boolean wasGuiKeyDown;

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
		LOGGER.info("ChaosUtils ready: build {}, {} modules, 9 integration hooks", BUILD_TAG,
				dev.chaosutils.config.ModuleManager.modules().size());
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
		handleInterfaceKey(client);
		if (Keybinds.panicToggle != null && Keybinds.panicToggle.consumeClick()) {
			toggleOverlays();
			if (client.player != null) {
				client.player.displayClientMessage(
						net.minecraft.network.chat.Component.literal(overlaysHidden
								? "§b[ChaosUtils] §fOverlays hidden" : "§b[ChaosUtils] §fOverlays visible"), true);
			}
		}
	}

	/**
	 * Opens and closes the interface with the same key.
	 *
	 * <p>The key state is polled physically ({@link InputUtil#isPhysicallyDown}): as soon as a screen
	 * is open the game releases every key mapping and stops feeding new states to them, so
	 * {@code KeyMapping#isDown()} - and therefore {@code consumeClick()} - can never report the key
	 * while the interface is up. That is also why the same key closes the interface again instead of
	 * leaving the player stuck in it.
	 */
	private static void handleInterfaceKey(Minecraft client) {
		if (Keybinds.openGui == null) {
			return;
		}
		boolean down = InputUtil.isPhysicallyDown(Keybinds.openGui);
		boolean justPressed = down && !wasGuiKeyDown;
		wasGuiKeyDown = down;
		if (!justPressed) {
			return;
		}
		if (client.screen instanceof dev.chaosutils.gui.ChaosScreen open) {
			LOGGER.info("ChaosUtils: closing {} again (key pressed)", open.getClass().getSimpleName());
			open.requestClose();
			return;
		}
		if (client.screen == null) {
			LOGGER.info("ChaosUtils: opening the click GUI (build {})", BUILD_TAG);
			client.setScreen(new dev.chaosutils.gui.ChaosClickGui());
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
	/** Free-form UI state (window position, sizes, remembered selections). */
	private static final JsonObject UI_STATE = new JsonObject();

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

	// ------------------------------------------------------------- UI state

	/** Reads a remembered number (window position, size, ...) with a safe fallback. */
	public static float uiFloat(String key, float fallback) {
		try {
			JsonElement element = UI_STATE.get(key);
			return element == null || element.isJsonNull() ? fallback : element.getAsFloat();
		} catch (Exception exception) {
			return fallback;
		}
	}

	public static int uiInt(String key, int fallback) {
		return Math.round(uiFloat(key, fallback));
	}

	public static boolean uiBoolean(String key, boolean fallback) {
		try {
			JsonElement element = UI_STATE.get(key);
			return element == null || element.isJsonNull() ? fallback : element.getAsBoolean();
		} catch (Exception exception) {
			return fallback;
		}
	}

	public static String uiString(String key, String fallback) {
		try {
			JsonElement element = UI_STATE.get(key);
			return element == null || element.isJsonNull() ? fallback : element.getAsString();
		} catch (Exception exception) {
			return fallback;
		}
	}

	/** Stores a value; the 5-second autosave writes it to disk. A {@code null} removes the key. */
	public static void setUi(String key, Object value) {
		if (value == null) {
			UI_STATE.remove(key);
			markDirty();
			return;
		}
		if (value instanceof Number number) {
			UI_STATE.addProperty(key, number);
		} else if (value instanceof Boolean flag) {
			UI_STATE.addProperty(key, flag);
		} else {
			UI_STATE.addProperty(key, String.valueOf(value));
		}
		markDirty();
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
			if (root.has("ui") && root.get("ui").isJsonObject()) {
				for (String key : new ArrayList<>(UI_STATE.keySet())) {
					UI_STATE.remove(key);
				}
				for (java.util.Map.Entry<String, JsonElement> entry : root.getAsJsonObject("ui").entrySet()) {
					UI_STATE.add(entry.getKey(), entry.getValue());
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
		root.add("ui", UI_STATE);
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

	/**
	 * Registers a setting and returns it, so a feature can write
	 * {@code toggle = module.add(new Setting.Toggle(...))} without a cast. (Returning the module
	 * instead would force every call site to cast and would break as soon as a setting type is
	 * added.)
	 */
	public <T extends Setting<?>> T add(T setting) {
		settings.add(setting);
		return setting;
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

	/** Colour with a guaranteed opaque alpha, safe for fills and borders. */
	public int solidColor() {
		return color | 0xFF000000;
	}

	/** Colour with the given alpha, keeping the stored hue. */
	public int colorWithAlpha(float alpha) {
		return dev.chaosutils.util.Render.alpha(color | 0xFF000000, alpha);
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

	public static float easeOutQuad(float t) {
		float x = clamp01(t);
		return 1.0F - (1.0F - x) * (1.0F - x);
	}

	public static float easeOutQuint(float t) {
		float x = clamp01(t);
		float inverted = 1.0F - x;
		return 1.0F - inverted * inverted * inverted * inverted * inverted;
	}

	public static float easeInOutSine(float t) {
		float x = clamp01(t);
		return (float) (-(Math.cos(Math.PI * x) - 1.0) * 0.5);
	}

	public static float smoothstep(float t) {
		float x = clamp01(t);
		return x * x * (3.0F - 2.0F * x);
	}

	/** No-jitter approach with a half-life; ideal for mouse-following UI elements. */
	public static float damped(float current, float target, float halfLifeSeconds, float deltaSeconds) {
		float hl = Math.max(0.001F, halfLifeSeconds);
		float factor = 1.0F - (float) Math.pow(0.5, Math.max(0.0F, deltaSeconds) / hl);
		return current + (target - current) * factor;
	}

	/** 0 -> 1 -> 0 over {@code period} seconds, used for pulsing highlights. */
	public static float pulse(float timeSeconds, float period) {
		float p = Math.max(0.05F, period);
		float phase = (timeSeconds % p) / p;
		return (float) (0.5 - 0.5 * Math.cos(phase * Math.PI * 2.0));
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

		/**
		 * Advances the value with an explicit rate in "gap closed per second".
		 *
		 * <p>Use this when the rate comes from the theme (see {@code UiTheme#speed}): the value's
		 * own speed is only a fallback, and passing the rate as {@code deltaSeconds} would make
		 * every animation finish within a single frame.
		 */
		public float update(float deltaSeconds, float rate) {
			current = approach(current, target, rate, deltaSeconds);
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
			String path = enchantment.unwrapKey().map(key -> key.identifier().getPath()).orElse(null);
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
	public static final HudPos BOTTOM_RIGHT = new HudPos(0.98F, 0.98F);
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
			// 1.21.11: getDisplayName() returns a Component, so it is flattened to text here.
			return InputConstants.Type.KEYSYM.getOrCreate(code).getDisplayName().getString();
		} catch (Throwable ignored) {
			return "Key " + code;
		}
	}

	public static boolean isPressed(int code) {
		if (code == NO_KEY) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		try {
			if (code <= -100) {
				int button = -100 - code;
				long window = client.getWindow().handle();
				return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
			}
			// 1.21.11: isKeyDown(Window, int) takes the Window object, not the GLFW handle.
			return InputConstants.isKeyDown(client.getWindow(), code);
		} catch (Throwable ignored) {
			return false;
		}
	}

	/**
	 * Whether the physical key of a key binding is held right now.
	 *
	 * <p>{@code KeyMapping#isDown()} is not usable inside a screen: opening one makes the game call
	 * {@code KeyMapping.releaseAll()} and, from then on, the keyboard handler stops feeding key
	 * states to key bindings. GLFW is the only source that still knows the truth, so the mapping's
	 * key is read through a mixin accessor and polled directly.
	 */
	public static boolean isPhysicallyDown(net.minecraft.client.KeyMapping mapping) {
		if (mapping == null) {
			return false;
		}
		try {
			com.mojang.blaze3d.platform.InputConstants.Key key =
					((dev.chaosutils.mixin.KeyMappingAccessor) (Object) mapping).chaosutils$key();
			if (key == null || key == com.mojang.blaze3d.platform.InputConstants.UNKNOWN) {
				return false;
			}
			long window = Minecraft.getInstance().getWindow().handle();
			if (key.getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE) {
				return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, key.getValue()) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
			}
			return com.mojang.blaze3d.platform.InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
		} catch (Throwable ignored) {
			// If the accessor or the window is unavailable, fall back to the vanilla state.
			return mapping.isDown();
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
		long window = Minecraft.getInstance().getWindow().handle();
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
			camera = client.gameRenderer.getMainCamera().position();
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

	/** Local clamp used by the alpha/fade helpers below. */
	private static float clamp01(float value) {
		return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
	}

	public static int rgba(int r, int g, int b, int a) {
		return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
	}

	/**
	 * Draws {@code color} with the given alpha.
	 *
	 * <p>Colours written without an alpha byte ({@code 0xRRGGBB}, and {@code 0x000000}) count as
	 * fully opaque, exactly like they read. This matters: {@code 0x12131C} has an alpha byte of
	 * zero, so multiplying would make every dark surface - panel fills, the modal dim, the radial
	 * hub - completely invisible. Colours that do carry an alpha byte keep it and the value scales
	 * it, which is what all the translucent theme tokens rely on.
	 */
	public static int alpha(int color, float alpha) {
		int base = (color >>> 24) & 0xFF;
		if (base == 0) {
			base = 0xFF;
		}
		int a = Math.round(Anim.clamp01(alpha) * base);
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
	 * Filled ring segment (annulus sector).
	 *
	 * <p>Rendered as a scanline fill of the underlying trapezoid, subdivided every 15° so the
	 * straight inner/outer chords stay within a fraction of a pixel of the true arc. That is
	 * what makes the radial menu look like a drawn shape instead of a fan of blocks: every row
	 * is a single quad and neighbouring slices can never bleed into each other.
	 *
	 * <p>Angles are in degrees, {@code 0} pointing up and growing clockwise.
	 */
	public static void arc(GuiGraphics graphics, float centerX, float centerY, float innerRadius, float outerRadius,
			float startDegrees, float endDegrees, int color) {
		if (outerRadius <= innerRadius || alphaOf(color) == 0) {
			return;
		}
		float span = endDegrees - startDegrees;
		if (Math.abs(span) < 0.02F) {
			return;
		}
		int parts = Math.max(1, (int) Math.ceil(Math.abs(span) / 15.0F));
		float step = span / parts;
		for (int i = 0; i < parts; i++) {
			trapezoid(graphics, centerX, centerY, innerRadius, outerRadius,
					startDegrees + i * step, startDegrees + (i + 1) * step, color);
		}
	}

	private static void trapezoid(GuiGraphics graphics, float centerX, float centerY, float innerRadius,
			float outerRadius, float startDegrees, float endDegrees, int color) {
		double start = Math.toRadians(startDegrees);
		double end = Math.toRadians(endDegrees);
		float[] xs = new float[4];
		float[] ys = new float[4];
		setCorner(xs, ys, 0, centerX, centerY, innerRadius, start);
		setCorner(xs, ys, 1, centerX, centerY, outerRadius, start);
		setCorner(xs, ys, 2, centerX, centerY, outerRadius, end);
		setCorner(xs, ys, 3, centerX, centerY, innerRadius, end);
		float minY = Math.min(Math.min(ys[0], ys[1]), Math.min(ys[2], ys[3]));
		float maxY = Math.max(Math.max(ys[0], ys[1]), Math.max(ys[2], ys[3]));
		if (maxY - minY > 240.0F) {
			// Safety valve: never let a malformed angle flood the screen with rows.
			return;
		}
		int y0 = (int) Math.floor(minY);
		int y1 = (int) Math.ceil(maxY);
		int rows = Math.max(1, y1 - y0);
		int rowStep = rows > 160 ? 3 : (rows > 80 ? 2 : 1);
		float half = rowStep * 0.5F;
		for (int row = y0; row < y1; row += rowStep) {
			float sample = row + half;
			float left = Float.MAX_VALUE;
			float right = -Float.MAX_VALUE;
			for (int edge = 0; edge < 4; edge++) {
				int next = (edge + 1) & 3;
				float ay = ys[edge];
				float by = ys[next];
				if ((ay <= sample && by >= sample) || (by <= sample && ay >= sample)) {
					float t = Math.abs(by - ay) < 1.0E-4F ? 0.5F : (sample - ay) / (by - ay);
					float x = xs[edge] + (xs[next] - xs[edge]) * t;
					left = Math.min(left, x);
					right = Math.max(right, x);
				}
			}
			if (right > left) {
				rect(graphics, left, row, right - left, rowStep + 0.5F, color);
			}
		}
	}

	private static void setCorner(float[] xs, float[] ys, int index, float centerX, float centerY, float radius, double angle) {
		xs[index] = centerX + (float) Math.sin(angle) * radius;
		ys[index] = centerY - (float) Math.cos(angle) * radius;
	}

	/** Filled circle (scanline, so the outline stays smooth at any size). */
	public static void circle(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		if (radius <= 0.5F || alphaOf(color) == 0) {
			return;
		}
		int y0 = Math.round(centerY - radius);
		int y1 = Math.round(centerY + radius);
		for (int row = y0; row <= y1; row++) {
			float dy = row + 0.5F - centerY;
			float half = radius * radius - dy * dy;
			if (half <= 0.0F) {
				continue;
			}
			half = (float) Math.sqrt(half);
			rect(graphics, centerX - half, row, half * 2.0F, 1.0F, color);
		}
	}

	/** Ring outline drawn from four edges and four quarter arcs (cheap, stays crisp). */
	public static void ring(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float thickness, int color) {
		if (width <= 0.0F || height <= 0.0F || alphaOf(color) == 0) {
			return;
		}
		float t = Math.max(1.0F, thickness);
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		if (r < 1.0F) {
			rect(graphics, x, y, width, t, color);
			rect(graphics, x, y + height - t, width, t, color);
			rect(graphics, x, y, t, height, color);
			rect(graphics, x + width - t, y, t, height, color);
			return;
		}
		rect(graphics, x + r, y, width - r * 2.0F, t, color);
		rect(graphics, x + r, y + height - t, width - r * 2.0F, t, color);
		rect(graphics, x, y + r, t, height - r * 2.0F, color);
		rect(graphics, x + width - t, y + r, t, height - r * 2.0F, color);
		int steps = Math.max(3, Math.round(r));
		for (int i = 0; i <= steps; i++) {
			double angle = Math.PI * 0.5 * i / steps;
			float ox = (float) Math.cos(angle) * r;
			float oy = (float) Math.sin(angle) * r;
			// top-left, top-right, bottom-right, bottom-left
			rect(graphics, x + r - ox, y + r - oy, t, t, color);
			rect(graphics, x + width - r + ox - t, y + r - oy, t, t, color);
			rect(graphics, x + width - r + ox - t, y + height - r + oy - t, t, t, color);
			rect(graphics, x + r - ox, y + height - r + oy - t, t, t, color);
		}
	}

	/**
	 * Rounded rectangle filled with a vertical gradient.
	 *
	 * <p>Two-pixel scanlines with the corner inset applied per row, which is both smoother and
	 * cheaper than stacking a flat rounded rect and a gradient on top of each other.
	 */
	public static void roundedRectGradient(GuiGraphics graphics, float x, float y, float width, float height,
			float radius, int top, int bottom) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}
		float r = Math.min(radius, Math.min(width, height) * 0.5F);
		int rows = Math.max(1, Math.round(height));
		int step = rows > 140 ? 2 : 1;
		for (int row = 0; row < rows; row += step) {
			float inset = Math.max(cornerInset(row, rows, r), cornerInset(rows - 1 - row, rows, r));
			int color = mix(top, bottom, (row + step * 0.5F) / (float) rows);
			rect(graphics, x + inset, y + row, width - inset * 2.0F, step + 0.5F, color);
		}
	}

	private static float cornerInset(int row, int rows, float radius) {
		float r = Math.min(radius, rows * 0.5F);
		if (r < 1.0F || row >= r) {
			return 0.0F;
		}
		float dy = Math.max(0.0F, r - row - 0.5F);
		return (float) (r - Math.sqrt(Math.max(0.0, r * r - dy * dy)));
	}

	/**
	 * Soft drop shadow: a handful of oversized rounded rectangles with a low alpha each.
	 * Much cheaper than a blur pass and it scales with the window without a texture.
	 */
	public static void softShadow(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			float strength) {
		float s = clamp01(strength);
		if (s <= 0.01F || width <= 0.0F || height <= 0.0F) {
			return;
		}
		int layers = 6;
		for (int i = layers; i >= 1; i--) {
			float spread = i * 1.8F;
			int alpha = Math.round(11.0F * s * (1.0F - i / (float) (layers + 1)) + 4.0F * s);
			roundedRect(graphics, x - spread, y - spread + 2.5F, width + spread * 2.0F, height + spread * 2.0F,
					radius + spread * 0.8F, (alpha & 0xFF) << 24);
		}
	}

	/**
	 * Accent glow. Drawn with a tinted blit of the radial {@code glow.png} that ships with the
	 * mod (one quad), so highlights stay soft instead of banding like stacked rectangles.
	 * Falls back to concentric circles if the texture cannot be resolved.
	 */
	private static final net.minecraft.resources.Identifier GLOW_TEXTURE =
			net.minecraft.resources.Identifier.fromNamespaceAndPath("chaosutils", "textures/gui/glow.png");
	private static boolean glowTextureAvailable = true;

	public static void glow(GuiGraphics graphics, float centerX, float centerY, float radius, int color, float strength) {
		float s = clamp01(strength);
		if (radius < 2.0F || s <= 0.01F || alphaOf(color) == 0) {
			return;
		}
		int argb = scaleAlpha(color, s);
		if (glowTextureAvailable && blitTexture(graphics, GLOW_TEXTURE, centerX - radius, centerY - radius,
				radius * 2.0F, radius * 2.0F, argb)) {
			return;
		}
		glowTextureAvailable = false;
		int steps = 5;
		for (int i = steps; i >= 1; i--) {
			circle(graphics, centerX, centerY, radius * i / steps, scaleAlpha(color, s * 0.12F));
		}
	}

	/** Soft accent halo behind a rounded panel; cheap and works without any texture. */
	public static void halo(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			int color, float strength) {
		float s = clamp01(strength);
		if (s <= 0.01F || width <= 0.0F || height <= 0.0F) {
			return;
		}
		int layers = 5;
		for (int i = layers; i >= 1; i--) {
			float spread = i * 2.0F;
			roundedRect(graphics, x - spread, y - spread, width + spread * 2.0F, height + spread * 2.0F,
					radius + spread * 0.9F, scaleAlpha(color, s * 0.075F * (1.0F - i / (float) (layers + 1))));
		}
	}

	/**
	 * Tinted texture blit through the vanilla GUI pipeline.
	 *
	 * @return {@code true} when the texture was drawn, {@code false} when it could not be found
	 *         (the caller then uses its geometric fallback).
	 */
	public static boolean blitTexture(GuiGraphics graphics, net.minecraft.resources.Identifier texture,
			float x, float y, float width, float height, int color) {
		if (width < 1.0F || height < 1.0F) {
			return false;
		}
		int x0 = Math.round(x);
		int y0 = Math.round(y);
		int x1 = Math.round(x + width);
		int y1 = Math.round(y + height);
		try {
			if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(texture).isEmpty()) {
				return false;
			}
			graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x0, y0, 0.0F, 0.0F,
					x1 - x0, y1 - y0, 128, 128, 128, 128, color);
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}

	/** Tint colour over an existing area - used for hover highlights that must not hide content. */
	public static void wash(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color, float amount) {
		roundedRect(graphics, x, y, width, height, radius, scaleAlpha(color, amount));
	}

	// ------------------------------------------------------------ text helpers

	/** Truncates with an ellipsis so it fits {@code maxWidth} device pixels. */
	public static String ellipsize(net.minecraft.client.gui.Font font, String value, float maxWidth) {
		if (value == null) {
			return "";
		}
		if (font.width(value) <= maxWidth) {
			return value;
		}
		String result = value;
		while (result.length() > 4 && font.width(result + "…") > maxWidth) {
			result = result.substring(0, result.length() - 2);
		}
		return result + "…";
	}

	/**
	 * Heavier text without giving up the colour control: drawing the string twice with a half
	 * pixel offset is exactly what the vanilla bold style does, but it keeps our own alpha.
	 */
	public static void boldText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value, float x, float y,
			int color, boolean shadow) {
		text(graphics, font, value, x, y, color, shadow);
		text(graphics, font, value, x + 0.7F, y, color, shadow);
	}

	/** Horizontal gradient text, drawn in three-character runs to keep the draw count tiny. */
	public static void gradientText(GuiGraphics graphics, net.minecraft.client.gui.Font font, String value,
			float x, float y, int from, int to, boolean shadow) {
		if (value == null || value.isEmpty()) {
			return;
		}
		int length = value.length();
		float cursor = x;
		int run = 3;
		for (int start = 0; start < length; start += run) {
			String part = value.substring(start, Math.min(length, start + run));
			int color = mix(from, to, start / (float) Math.max(1, length - 1));
			text(graphics, font, part, cursor, y, color, shadow);
			cursor += font.width(part);
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
		SimpleSoundInstance instance = SimpleSoundInstance.forUI(event, pitch, volume);
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
	/** Ticks after a world join before the hook report is printed; -1 means "nothing armed". */
	private static int reportDelay = -1;
	private static boolean hookReportPrinted;

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
					Projection.setup(client, graphics.guiWidth(), graphics.guiHeight(), TickClock.partialTick());
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
		// Give the in-world hooks (camera, particles, containers, FOV, ...) a chance to run before
		// reporting which of them are live - at that point they have actually been exercised.
		if (!hookReportPrinted && reportDelay < 0) {
			reportDelay = 60;
		}
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
			hookReportPrinted = true;
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
import dev.chaosutils.util.InputUtil;
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
	private static Setting.Toggle autoScale;
	/** Previous physical key state, used for edge detection that also works inside a screen. */
	private static boolean wasHeld;

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
		autoScale = (Setting.Toggle) module.add(new Setting.Toggle("auto_scale", "Fit to screen",
				"Shrink the ring so it always fits on screen, even when it opens near an edge.", true));
	}

	@Override
	public void onTick(Minecraft client) {
		// Switching screens has to happen in the tick: doing it while a frame is being drawn would
		// leave the game rendering a screen that is already gone.
		if (open != null && open.readyToClose()) {
			RadialMenuScreen screen = open;
			open = null;
			screen.finishClose();
			return;
		}
		if (client.player == null) {
			wasHeld = false;
			if (open != null) {
				open.cancel();
			}
			return;
		}
		if (!isEnabled()) {
			if (open != null) {
				open.cancel();
			}
			return;
		}
		// The physical key is polled instead of KeyMapping#isDown: opening a screen makes the game
		// release every key mapping (Minecraft#setScreen -> KeyMapping.releaseAll) and the keyboard
		// handler stops feeding key bindings while a screen is open. Relying on the mapping state
		// would close the menu again on the very next tick - before a single frame was drawn.
		boolean held = InputUtil.isPhysicallyDown(Keybinds.radialMenu);
		boolean justPressed = held && !wasHeld;
		wasHeld = held;

		if (open != null) {
			if (holdMode.get() ? !held : justPressed) {
				// The key was released: run the slice the player pointed at, then play the exit.
				open.commitSelection();
			}
			return;
		}
		if (holdMode.get()) {
			if (held && canOpen(client)) {
				openMenu(client);
			}
		} else if (justPressed && canOpen(client)) {
			openMenu(client);
		}
	}

	/** The menu only opens in-game, or on top of another ChaosUtils window. */
	private static boolean canOpen(Minecraft client) {
		return client.screen == null || client.screen instanceof dev.chaosutils.gui.ChaosScreen;
	}

	private static void openMenu(Minecraft client) {
		Screen parent = client.screen;
		RadialMenuScreen screen = new RadialMenuScreen(parent);
		open = screen;
		client.setScreen(screen);
	}

	/** Accessor for the overlay gate: HUD elements hide while the menu is up. */
	public static boolean isOpen() {
		return open != null;
	}

	public static boolean scalesToScreen() {
		return autoScale == null || autoScale.get();
	}

	/** Called by the screen once its exit animation finished. */
	static void onClosed() {
		open = null;
		wasHeld = false;
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
		wasHeld = false;
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
 * The interactive radial menu.
 *
 * <p>Design rules that keep it readable:
 * <ul>
 *   <li>the ring is pinned where it opened (on the cursor by default) and is scaled down until it
 *       fits the screen with a margin, so slices can never run off-screen,</li>
 *   <li>slices are drawn as true annulus sectors with gaps, drawn scanline by scanline - no
 *       overlapping blocks, no jagged edges,</li>
 *   <li>labels are measured against the chord of their slice, so a long name can never reach into
 *       the neighbouring slice,</li>
 *   <li>nothing else is drawn while it is open: the HUD elements of ChaosUtils and the vanilla HUD
 *       sit behind the frame's blur, and the dim layer removes the rest.</li>
 * </ul>
 *
 * <p>Releasing the radial key runs the hovered slice exactly once. The screen closes itself with a
 * short exit animation, so the player always sees which action was triggered.
 */
public final class RadialMenuScreen extends Screen {
	private static final float ENTRY_SCALE = 0.82F;
	private static final float CLOSE_DELAY = 0.16F;

	private final Screen parent;
	private final Anim.Value appear = new Anim.Value(0.0F, 12.0F);
	private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);

	private Anim.Value[] sliceHover = new Anim.Value[0];
	private int hoveredIndex = -1;
	private boolean hoveredInitialised;

	private float centerX;
	private float centerY;
	private boolean centerLocked;
	private float mouseX;
	private float mouseY;
	private float scale = 1.0F;
	private float inner;
	private float outer;

	private boolean closing;
	private boolean committed;
	private boolean handedOver;
	private boolean closeFinished;
	private float closingFor;

	public RadialMenuScreen(Screen parent) {
		super(Component.literal("ChaosUtils Radial Menu"));
		this.parent = parent;
		this.appear.snap(0.0F);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Runs the hovered slice and closes with feedback. Called when the key is released. */
	public void commitSelection() {
		if (closing) {
			return;
		}
		RadialElement element = hovered();
		if (element != null) {
			Minecraft client = Minecraft.getInstance();
			if (client.player != null) {
				RadialMenuFeature.execute(element, client);
			}
			committed = true;
		}
		beginClose();
	}

	/** Closes without executing anything. */
	public void cancel() {
		if (!closing) {
			beginClose();
		}
	}

	private void beginClose() {
		closing = true;
		exit.set(1.0F);
		closingFor = 0.0F;
	}

	/** True once the exit animation has played out; the feature then switches screens in its tick. */
	public boolean readyToClose() {
		return closeFinished;
	}

	/** Performs the actual hand-over. Called from the client tick, never from inside a frame. */
	public void finishClose() {
		if (handedOver) {
			return;
		}
		handedOver = true;
		RadialMenuFeature.onClosed();
		Minecraft client = Minecraft.getInstance();
		client.setScreen(parent);
	}

	@Override
	public void onClose() {
		cancel();
	}

	private RadialElement hovered() {
		List<RadialElement> entries = RadialMenuFeature.entries();
		return hoveredIndex >= 0 && hoveredIndex < entries.size() ? entries.get(hoveredIndex) : null;
	}

	// ------------------------------------------------------------------ render

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float frameDelta = dev.chaosutils.core.TickClock.frameDelta();
		if (frameDelta <= 0.0F) {
			frameDelta = 0.016F;
		}

		appear.set(1.0F);
		appear.update(frameDelta, UiThemeSpeed(12.0F));
		if (closing) {
			exit.update(frameDelta, UiThemeSpeed(9.0F));
			closingFor += frameDelta;
			if (closingFor > CLOSE_DELAY) {
				closeFinished = true;
			}
		}
		if (closeFinished) {
			// Nothing is drawn any more; the tick performs the screen switch.
			return;
		}

		float appearance = Anim.easeOutQuint(Anim.clamp01(appear.get()));
		float exitAmount = Anim.easeOutQuint(Anim.clamp01(exit.get()));
		List<RadialElement> entries = RadialMenuFeature.entries();

		if (!centerLocked) {
			centerLocked = true;
			centerX = RadialMenuFeature.centersOnCursor() ? mouseX : this.width * 0.5F;
			centerY = RadialMenuFeature.centersOnCursor() ? mouseY : this.height * 0.5F;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		computeGeometry(appearance);

		// ---- dim layer: hides the world, the HUD and everything else behind the ring
		float dim = RadialMenuFeature.dimsBackground() ? RadialMenuFeature.dimStrength() : 0.0F;
		if (dim > 0.01F) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height,
					Render.alpha(0x05060A, dim * appearance));
		}
		// Extra vignette so the ring always sits on a calm background.
		Render.verticalGradient(graphics, 0.0F, 0.0F, this.width, this.height, 0x33000000, 0x00000000);

		if (entries.isEmpty()) {
			drawEmptyState(graphics, appearance);
			super.render(graphics, mouseX, mouseY, partialTick);
			return;
		}

		ensureSlices(entries.size());

		float span = 360.0F / entries.size();
		float gap = Math.min(RadialMenuFeature.gapDegrees(), span * 0.35F);
		hoveredIndex = closing ? -1 : computeHovered(entries, span);

		// ---- slices
		for (int i = 0; i < entries.size(); i++) {
			Anim.Value hoverValue = sliceHover[i];
			hoverValue.set(i == hoveredIndex ? 1.0F : 0.0F);
			hoverValue.update(Anim.clamp(UiThemeSpeed(18.0F), 4.0F, 40.0F));
			float highlight = hoverValue.get();

			RadialElement element = entries.get(i);
			float start = i * span - span * 0.5F + gap * 0.5F;
			float end = (i + 1) * span - span * 0.5F - gap * 0.5F;
			float expansion = highlight * (RadialMenuFeature.hoverScale() - 1.0F) * outer * 1.6F;
			float rInner = inner;
			float rOuter = outer + expansion;

			int base = element.color | 0xFF000000;
			float baseAlpha = 0.70F;
			int color = Render.alpha(Render.mix(base, RadialMenuFeature.highlightColor(), highlight * 0.45F),
					(baseAlpha + 0.25F * highlight) * appearance * (1.0F - exitAmount));
			if (highlight > 0.05F) {
				Render.glow(graphics, sliceAnchorX(start, end, rOuter), sliceAnchorY(start, end, rOuter),
						(Math.abs(end - start) / 360.0F) * outer * 2.6F + 24.0F, base, 0.30F * highlight * appearance);
			}
			Render.arc(graphics, centerX, centerY, rInner, rOuter, start, end, color);
			// inner shading for a subtle depth gradient (darker towards the centre)
			Render.arc(graphics, centerX, centerY, rInner, rInner + (rOuter - rInner) * 0.45F, start, end,
					Render.alpha(0x000000, 0.20F * appearance * (1.0F - exitAmount)));
			if (highlight > 0.02F) {
				Render.arc(graphics, centerX, centerY, rOuter - 2.0F, rOuter, start, end,
						Render.alpha(0xFFFFFFFF, 0.75F * highlight * appearance * (1.0F - exitAmount)));
			}
		}

		// ---- slice content (drawn after every slice so nothing can cover a label)
		for (int i = 0; i < entries.size(); i++) {
			RadialElement element = entries.get(i);
			float start = i * span - span * 0.5F + gap * 0.5F;
			float end = (i + 1) * span - span * 0.5F - gap * 0.5F;
			float highlight = sliceHover[i].get();
			float expansion = highlight * (RadialMenuFeature.hoverScale() - 1.0F) * outer * 1.6F;
			drawSliceContent(graphics, element, start, end, expansion, highlight, appearance, exitAmount, span);
		}

		drawHub(graphics, entries, appearance, exitAmount);
		drawSelectionLabel(graphics, appearance, exitAmount);
		drawHint(graphics, appearance);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	/** Theme animation speed times the speed the player configured for the radial menu. */
	private float UiThemeSpeed(float base) {
		return dev.chaosutils.gui.UiTheme.get().speed(base) * RadialMenuFeature.animationSpeed();
	}

	private void ensureSlices(int count) {
		if (sliceHover.length == count) {
			return;
		}
		Anim.Value[] replacements = new Anim.Value[count];
		for (int i = 0; i < count; i++) {
			replacements[i] = i < sliceHover.length ? sliceHover[i] : new Anim.Value(0.0F, 18.0F);
		}
		sliceHover = replacements;
	}

	/** Fits the ring to the screen and keeps the centre inside the safe area. */
	private void computeGeometry(float appearance) {
		float wantedInner = Math.max(24.0F, RadialMenuFeature.innerRadius());
		float wantedOuter = Math.max(wantedInner + 20.0F, RadialMenuFeature.outerRadius());
		float margin = RadialMenuFeature.scalesToScreen() ? 28.0F : 0.0F;
		float available = Math.min(Math.min(centerX, this.width - centerX), Math.min(centerY, this.height - centerY)) - margin;
		scale = RadialMenuFeature.scalesToScreen() ? Anim.clamp(available / wantedOuter, 0.5F, 1.0F) : 1.0F;
		float grown = ENTRY_SCALE + (1.0F - ENTRY_SCALE) * appearance;
		outer = wantedOuter * scale * grown;
		inner = wantedInner * scale * grown;
		centerX = Anim.clamp(centerX, margin + outer, this.width - margin - outer);
		centerY = Anim.clamp(centerY, margin + outer, this.height - margin - outer);
	}

	private int computeHovered(List<RadialElement> entries, float span) {
		float dx = mouseX - centerX;
		float dy = mouseY - centerY;
		double distance = Math.sqrt(dx * dx + dy * dy);
		float deadZone = Math.max(RadialMenuFeature.deadZone(), inner * 0.55F);
		if (distance < deadZone || distance > outer + 26.0F) {
			return -1;
		}
		double angle = Math.toDegrees(Math.atan2(dx, -dy)) + span * 0.5;
		while (angle < 0.0) {
			angle += 360.0;
		}
		while (angle >= 360.0) {
			angle -= 360.0;
		}
		return Anim.clamp((int) (angle / span), 0, entries.size() - 1);
	}

	private float sliceAnchorX(float start, float end, float radius) {
		double mid = Math.toRadians((start + end) * 0.5);
		return centerX + (float) Math.sin(mid) * radius;
	}

	private float sliceAnchorY(float start, float end, float radius) {
		double mid = Math.toRadians((start + end) * 0.5);
		return centerY - (float) Math.cos(mid) * radius;
	}

	private void drawSliceContent(GuiGraphics graphics, RadialElement element, float start, float end,
			float expansion, float highlight, float appearance, float exitAmount, float span) {
		float alpha = appearance * (1.0F - exitAmount) * (0.82F + 0.18F * highlight);
		if (alpha <= 0.02F) {
			return;
		}
		double midDegrees = (start + end) * 0.5;
		double mid = Math.toRadians(midDegrees);
		float midRadius = (inner + outer + expansion) * 0.5F;
		float x = centerX + (float) Math.sin(mid) * midRadius;
		float y = centerY - (float) Math.cos(mid) * midRadius;

		// Chord of the slice at the label radius: the hard limit for text and icon width.
		float chord = 2.0F * midRadius * (float) Math.sin(Math.toRadians(Math.abs(span) * 0.5F));
		float available = Math.max(28.0F, chord - 12.0F);
		net.minecraft.world.item.ItemStack icon = element.iconStack();
		boolean hasIcon = RadialMenuFeature.showIcons() && icon != null && !icon.isEmpty();

		if (hasIcon) {
			float iconY = y - (RadialMenuFeature.showLabels() ? 8.0F : 0.0F);
			UiIcon(graphics, icon, x, iconY, 0.82F + 0.22F * highlight);
		}
		if (!RadialMenuFeature.showLabels()) {
			return;
		}
		Font font = this.font;
		String name = Render.ellipsize(font, element.name, available);
		boolean tooTight = chord < 42.0F && highlight < 0.4F;
		if (tooTight) {
			return;
		}
		float textY = hasIcon ? y + 3.0F : y - 4.0F;
		int color = Render.alpha(highlight > 0.5F ? 0xFFFFFFFF : 0xF0FFFFFF, alpha);
		Render.centeredText(graphics, font, name, x, textY, color, false);
	}

	private void UiIcon(GuiGraphics graphics, net.minecraft.world.item.ItemStack icon, float x, float y, float size) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(size, size);
		graphics.pose().translate(-x, -y);
		Render.item(graphics, icon, x - 8.0F, y - 8.0F);
		graphics.pose().popMatrix();
	}

	/** Centre hub: shows the hovered icon, or a compass rose when nothing is selected. */
	private void drawHub(GuiGraphics graphics, List<RadialElement> entries, float appearance, float exitAmount) {
		float alpha = appearance * (1.0F - exitAmount);
		float hubRadius = Math.max(14.0F, inner - 6.0F);
		Render.circle(graphics, centerX, centerY, hubRadius, Render.alpha(0x0C0D14, 0.72F * alpha));
		RadialElement element = hovered();
		boolean hasHover = element != null;
		if (hasHover) {
			Render.glow(graphics, centerX, centerY, hubRadius * 2.1F, element.color | 0xFF000000, 0.35F * alpha);
		}
		Render.ring(graphics, centerX - hubRadius, centerY - hubRadius, hubRadius * 2.0F, hubRadius * 2.0F, hubRadius, 1.4F,
				Render.alpha(hasHover ? 0xFFFFFFFF : 0x66FFFFFF, 0.35F * alpha));
		if (hasHover && RadialMenuFeature.showIcons()) {
			UiIcon(graphics, element.iconStack(), centerX, centerY, 1.55F);
		} else if (RadialMenuFeature.showCenterText()) {
			// small compass rose while nothing is hovered
			int color = Render.alpha(0xFFFFFFFF, 0.45F * alpha);
			Render.line(graphics, centerX, centerY - hubRadius * 0.55F, centerX, centerY + hubRadius * 0.55F, 1.6F, color);
			Render.line(graphics, centerX - hubRadius * 0.55F, centerY, centerX + hubRadius * 0.55F, centerY, 1.6F, color);
			Render.circle(graphics, centerX, centerY, 2.0F, color);
		}
	}

	/** Name and command of the hovered slice, in a pill below the ring. */
	private void drawSelectionLabel(GuiGraphics graphics, float appearance, float exitAmount) {
		float alpha = appearance * (1.0F - exitAmount);
		RadialElement element = hovered();
		float y = centerY + outer + 18.0F;
		if (element == null) {
			String hint = "point at a slice";
			Render.centeredText(graphics, this.font, hint, centerX, y,
					Render.alpha(0xFFFFFFFF, 0.45F * alpha), false);
			return;
		}
		String name = element.name;
		String detail = element.command == null || element.command.isEmpty() ? element.type.label() : element.command;
		Font font = this.font;
		float width = Math.max(font.width(name), font.width(detail)) + 26.0F;
		float top = y - 6.0F;
		Render.softShadow(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F, 0.7F * alpha);
		Render.roundedRect(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F,
				Render.alpha(0x12131C, 0.92F * alpha));
		Render.ring(graphics, centerX - width * 0.5F, top, width, 28.0F, 9.0F, 1.0F,
				Render.alpha(element.color | 0xFF000000, 0.55F * alpha));
		Render.centeredText(graphics, font, Render.ellipsize(font, name, width - 20.0F), centerX, top + 5.0F,
				Render.alpha(0xFFFFFFFF, alpha), false);
		Render.centeredText(graphics, font, Render.ellipsize(font, detail, width - 20.0F), centerX, top + 15.0F,
				Render.alpha(0xFFB9BBD0, alpha * 0.9F), false);
	}

	private void drawHint(GuiGraphics graphics, float appearance) {
		String hint = "hold " + keyHint() + "  ·  release to run  ·  esc or right click to cancel";
		Render.centeredText(graphics, this.font, hint, this.width * 0.5F, this.height - 22.0F,
				Render.alpha(0xFFD8DAE6, 0.55F * appearance), false);
	}

	private void drawEmptyState(GuiGraphics graphics, float appearance) {
		float width = 240.0F;
		float x = (this.width - width) * 0.5F;
		float y = this.height * 0.5F - 26.0F;
		Render.softShadow(graphics, x, y, width, 52.0F, 10.0F, 0.8F);
		Render.roundedRect(graphics, x, y, width, 52.0F, 10.0F, Render.alpha(0x12131C, 0.95F * appearance));
		Render.ring(graphics, x, y, width, 52.0F, 10.0F, 1.0F, Render.alpha(0x33FFFFFF, appearance));
		Render.centeredText(graphics, this.font, "No radial entries", this.width * 0.5F, y + 12.0F,
				Render.alpha(0xFFFFFFFF, appearance), false);
		Render.centeredText(graphics, this.font, "Add some in the radial editor", this.width * 0.5F, y + 26.0F,
				Render.alpha(0xFFB9BBD0, appearance), false);
	}

	private String keyHint() {
		try {
			return dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "the radial key";
		}
	}

	// ------------------------------------------------------------------- input

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			commitSelection();
			return true;
		}
		if (event.button() == 1) {
			cancel();
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	public boolean wasExecuted() {
		return committed;
	}
}
```

---

## Click GUI

### `src/main/java/dev/chaosutils/gui/Backdrop.java`

```java
package dev.chaosutils.gui;

import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * Draws the ChaosUtils backdrop for a screen without touching vanilla's background pass.
 *
 * <h2>Why this class exists</h2>
 * Since 1.21.9 the game paints a screen's background itself:
 *
 * <pre>
 * public final void renderWithTooltipAndSubtitles(GuiGraphics graphics, ...) {
 *     graphics.nextStratum();
 *     this.renderBackground(graphics, mouseX, mouseY, a);   // blur happens here
 *     graphics.nextStratum();
 *     this.render(graphics, mouseX, mouseY, a);             // our code runs here
 * }
 * </pre>
 *
 * <p>{@code renderBackground(...)} ends up in {@code Screen#renderBlurredBackground}, which calls
 * {@code GuiGraphics#blurBeforeThisStratum()}. The render state allows exactly one blur per frame
 * and throws {@code IllegalStateException("Can only blur once per frame")} on the second attempt.
 * A screen that calls {@code renderBackground(...)} from its own {@code render()} therefore kills
 * the game on the very first frame it is shown - that is exactly what happened the first time the
 * click GUI was opened.
 *
 * <p>So this helper never calls {@code renderBackground}: it only paints the ChaosUtils tint on top
 * of the background vanilla already drew, and asks for the blur itself <em>only</em> when the
 * vanilla option has it switched off. Everything is wrapped so that a backdrop can never take the
 * game down.
 */
public final class Backdrop {
	/** "Dark gradient" - tint plus a soft gradient towards the bottom. */
	public static final int STYLE_DARK_GRADIENT = 0;
	/** "Blur + gradient" - same tint, the blur comes from the vanilla blur option. */
	public static final int STYLE_BLUR_GRADIENT = 1;
	/** "Flat" - tint only. */
	public static final int STYLE_FLAT = 2;
	/** "Transparent" - nothing at all, the world stays fully visible. */
	public static final int STYLE_TRANSPARENT = 3;

	private Backdrop() {
	}

	/**
	 * Paints the backdrop of {@code screen}. Call this from {@code render(...)} or from
	 * {@code renderBackdrop(...)} - never call {@link Screen#renderBackground} yourself.
	 */
	public static void render(GuiGraphics graphics, Screen screen, UiTheme theme) {
		if (theme.backdropStyle == STYLE_TRANSPARENT) {
			return;
		}
		requestBlur(graphics, theme);
		Render.rect(graphics, 0.0F, 0.0F, screen.width, screen.height, theme.background);
		if (theme.backdropStyle == STYLE_DARK_GRADIENT) {
			Render.verticalGradient(graphics, 0.0F, 0.0F, screen.width, screen.height, 0x00000000, 0x66000000);
		}
	}

	/**
	 * Uses the frame's single blur slot when vanilla left it free.
	 *
	 * <p>Vanilla blurs whenever the "Menu background blurriness" option is 1 or higher, which is the
	 * default; in that case the world behind the GUI is already blurred and there is nothing to do.
	 * Only when the player switched that option off does ChaosUtils claim the slot - and even then
	 * a failure is harmless, because another screen or mod may have used it already.
	 */
	private static void requestBlur(GuiGraphics graphics, UiTheme theme) {
		if (!theme.blur) {
			return;
		}
		try {
			Minecraft client = Minecraft.getInstance();
			if (client.options.getMenuBackgroundBlurriness() >= 1) {
				return;
			}
			graphics.blurBeforeThisStratum();
		} catch (Throwable ignored) {
			// The blur slot is taken (or the API moved) - draw the GUI without the blur.
		}
	}
}
```

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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The ChaosUtils interface.
 *
 * <p>A floating window (drag the top bar, resize from the corner, double-click the bar to re-centre)
 * with a sidebar of categories on the left and a searchable list of module cards on the right.
 * Every setting of every feature is rendered generically from its {@link Setting} description, so
 * a newly added feature shows up in the interface without touching this class.
 */
public final class ChaosClickGui extends ChaosScreen {
	private static final float SIDEBAR_MIN = 138.0F;
	private static final float CARD_GAP = 6.0F;

	private final Map<String, Boolean> expanded = new HashMap<>();
	private final List<ModuleCard> cards = new ArrayList<>();
	private Category selected = Category.HUD;
	private String query = "";
	private float selectedPulse;

	private UiWidgets.ScrollList list;
	private UiWidgets.SearchField search;
	private UiWidgets.Label headerTitle;
	private UiWidgets.Label headerSubtitle;
	private float sidebarWidth;

	public ChaosClickGui() {
		this(null);
	}

	public ChaosClickGui(Screen parent) {
		super(parent, Component.literal("ChaosUtils"), "window.main", 680.0F, 440.0F);
	}

	// ------------------------------------------------------------------ layout

	@Override
	protected void buildLayout() {
		UiTheme theme = UiTheme.get();
		float winX = window.x();
		float winY = window.y();
		float sidebarTop = winY + window.titleHeight() + 10.0F;
		float sidebarHeight = window.height() - window.titleHeight() - 20.0F;
		this.sidebarWidth = Math.max(SIDEBAR_MIN, theme.sidebarWidth + 44.0F);

		float contentX = winX + sidebarWidth + 24.0F;
		float contentWidth = Math.max(220.0F, window.right() - 14.0F - contentX);

		SidebarPanel panel = new SidebarPanel();
		panel.setBounds(winX + 8.0F, sidebarTop - 4.0F, sidebarWidth - 4.0F, sidebarHeight + 4.0F);
		add(panel);

		// --- sidebar: categories
		float cursor = sidebarTop + 22.0F;
		for (Category category : Category.values()) {
			SidebarItem item = new SidebarItem(this, category);
			item.setBounds(winX + 14.0F, cursor, sidebarWidth - 12.0F, 26.0F);
			item.setAppearDelay(0.02F * category.ordinal());
			add(item);
			cursor += 28.0F;
		}

		// --- sidebar: quick actions
		float actionsBottom = winY + window.height() - 12.0F;
		UiWidgets.Label general = new UiWidgets.Label("General", UiTheme.get().textFaint);
		general.setBounds(winX + 22.0F, actionsBottom - 76.0F, sidebarWidth - 20.0F, 10.0F);
		add(general);
		float half = (sidebarWidth - 18.0F) * 0.5F;
		add(smallButton("HUD Editor", winX + 14.0F, actionsBottom - 62.0F, half,
				() -> openHudEditor(), "Move every overlay with the mouse."));
		add(smallButton("Radial", winX + 14.0F + half + 6.0F, actionsBottom - 62.0F, half,
				() -> open(new ChaosScreens.RadialEditorScreen(this)), "Create, edit and reorder the radial menu."));
		add(smallButton("Waypoints", winX + 14.0F, actionsBottom - 40.0F, half,
				() -> open(new ChaosScreens.WaypointsScreen(this)), "Death markers and manual waypoints."));
		add(smallButton("Chat", winX + 14.0F + half + 6.0F, actionsBottom - 40.0F, half,
				() -> open(new ChaosScreens.ChatHistoryScreen(this)), "Search and copy everything you saw in chat."));
		add(smallButton("Screenshots", winX + 14.0F, actionsBottom - 18.0F, half,
				() -> open(new ChaosScreens.ScreenshotScreen(this)), "Browse, copy and crop local screenshots."));

		UiWidgets.Button panic = new UiWidgets.Button(
				ChaosUtils.overlaysHidden() ? "Show overlays" : "Hide overlays",
				UiWidgets.Button.Variant.DANGER, theme.negative, () -> {
					ChaosUtils.toggleOverlays();
					toast(ChaosUtils.overlaysHidden() ? "Overlays hidden" : "Overlays visible");
					refresh();
				});
		panic.setBounds(winX + 14.0F + half + 6.0F, actionsBottom - 18.0F, half, 18.0F);
		panic.setTooltip("Panic switch: hides every ChaosUtils overlay instantly.");
		add(panic);

		// --- content header
		headerTitle = new UiWidgets.Label(titleText(), theme.text).bold();
		headerTitle.setBounds(contentX, winY + window.titleHeight() + 12.0F, contentWidth * 0.5F, 14.0F);
		add(headerTitle);

		headerSubtitle = new UiWidgets.Label(subtitleText(), theme.textFaint);
		headerSubtitle.setBounds(contentX, winY + window.titleHeight() + 27.0F, contentWidth * 0.5F, 12.0F);
		add(headerSubtitle);

		// --- search (vanilla text field, styled field around it)
		EditBox searchBox = new EditBox(this.font, 0, 0, 180, 14, Component.literal("Search"));
		searchBox.setBordered(false);
		searchBox.setTextColor(0xFFF4F5FA);
		searchBox.setHint(Component.literal("Search modules…"));
		searchBox.setMaxLength(48);
		searchBox.setValue(query);
		searchBox.setResponder(value -> {
			if (!value.equals(query)) {
				query = value;
				rebuildCards();
			}
		});
		addInput(searchBox);
		search = new UiWidgets.SearchField(searchBox);
		search.setOnClear(() -> {
			query = "";
			rebuildCards();
		});
		search.place(contentX + contentWidth - 200.0F, winY + window.titleHeight() + 14.0F, 200.0F, 22.0F);
		add(search);

		UiWidgets.IconButton collapse = new UiWidgets.IconButton(
				(graphics, cx, cy, alpha) -> {
					// Double chevron pointing up: "collapse everything".
					for (int i = 0; i < 2; i++) {
						float offset = (i - 0.5F) * 4.0F;
						Ui.chevron(graphics, cx, cy + offset, 6.0F, -90.0F, Render.alpha(theme.textDim, alpha));
					}
				}, theme.accent, this::collapseAll);
		collapse.setTooltip("Collapse every expanded module.");
		collapse.setBounds(contentX + contentWidth - 26.0F, winY + window.titleHeight() + 14.0F, 22.0F, 22.0F);
		collapse.setAppearDelay(0.05F);
		add(collapse);

		// --- module list
		list = new UiWidgets.ScrollList();
		list.setSpacing(CARD_GAP);
		list.setBounds(contentX, winY + window.titleHeight() + 44.0F, contentWidth,
				Math.max(80.0F, window.height() - window.titleHeight() - 56.0F));
		list.snapAppear();
		add(list);
		rebuildCards();
	}

	private UiWidgets.Button smallButton(String label, float x, float y, float width, Runnable action, String tooltip) {
		UiWidgets.Button button = new UiWidgets.Button(label, UiWidgets.Button.Variant.GHOST, UiTheme.get().accent, action);
		button.setBounds(x, y, width, 18.0F);
		button.setTooltip(tooltip);
		button.setPadding(2.0F);
		return button;
	}

	private void open(Screen screen) {
		if (this.minecraft != null) {
			this.minecraft.setScreen(screen);
		}
	}

	private String titleText() {
		if (!query.isBlank()) {
			return "Search";
		}
		return selected.displayName();
	}

	private String subtitleText() {
		if (!query.isBlank()) {
			int matches = ModuleManager.search(query).size();
			return matches + (matches == 1 ? " match" : " matches") + " for \"" + query + "\"";
		}
		int total = ModuleManager.byCategory(selected).size();
		long enabled = ModuleManager.byCategory(selected).stream().filter(Module::isEnabled).count();
		return total + (total == 1 ? " module" : " modules") + "   ·   " + enabled + " enabled";
	}

	private void collapseAll() {
		expanded.replaceAll((key, value) -> Boolean.FALSE);
		rebuildCards();
		toast("All modules collapsed");
	}

	@Override
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		UiTheme theme = theme();
		Render.gradientText(graphics, this.font, "ChaosUtils", window.x() + 16.0F, window.y() + 13.0F,
				theme.text, theme.accent, false);
		String version = modVersion();
		Render.text(graphics, this.font, version, window.x() + 20.0F + this.font.width("ChaosUtils"),
				window.y() + 14.0F, Render.alpha(theme.textFaint, alpha * 0.9F), false);
		// Window controls: the key hint next to the close button.
		String hint = "press " + chaosKeyName() + " again or Esc to close  ·  drag to move";
		Render.text(graphics, this.font, hint, window.right() - 44.0F - this.font.width(hint), window.y() + 14.0F,
				Render.alpha(theme.textFaint, alpha * 0.75F), false);
	}

	private static String modVersion() {
		return "build " + ChaosUtils.BUILD_TAG;
	}

	/** Name of the key that opens this interface, for the header hint. */
	private static String chaosKeyName() {
		try {
			return dev.chaosutils.core.Keybinds.openGui.getTranslatedKeyMessage().getString();
		} catch (Throwable ignored) {
			return "the interface key";
		}
	}

	/** Status line under the module list. */
	@Override
	protected void renderFooter(GuiGraphics graphics, float alpha) {
		if (list == null) {
			return;
		}
		UiTheme theme = theme();
		float y = window.bottom() - 14.0F;
		String status = cards.size() + (cards.size() == 1 ? " module" : " modules") + " shown   ·   "
				+ ModuleManager.countEnabled() + "/" + ModuleManager.modules().size() + " active   ·   Esc closes"
				+ (ChaosUtils.overlaysHidden() ? "   ·   overlays hidden" : "");
		Render.text(graphics, this.font, status, window.x() + sidebarWidth + 24.0F, y,
				Render.alpha(theme.textFaint, alpha * 0.85F), false);
	}

	// ------------------------------------------------------------------- cards

	private void rebuildCards() {
		if (list == null) {
			return;
		}
		list.clearItems();
		cards.clear();
		List<Module> modules = query.isBlank() ? ModuleManager.byCategory(selected) : ModuleManager.search(query);
		float width = list.width();
		int index = 0;
		for (Module module : modules) {
			ModuleCard card = new ModuleCard(this, module, query.isBlank() ? null : module.category().displayName());
			card.setBounds(0.0F, 0.0F, width, card.headerHeight());
			card.setAppearDelay(Math.min(0.22F, 0.014F * index));
			cards.add(card);
			list.addItem(card);
			index++;
		}
		list.layout();
		if (headerTitle != null) {
			headerTitle.setText(titleText());
		}
		if (headerSubtitle != null) {
			headerSubtitle.setText(subtitleText());
		}
	}

	@Override
	protected void modalClosed(UiModals.Modal modal) {
		refresh();
	}

	@Override
	public void requestClose() {
		if (search != null && search.box().isFocused()) {
			setFocused(null);
		}
		super.requestClose();
	}

	// --------------------------------------------------------------- sidebar

	/** Inset surface behind the sidebar entries. */
	private static final class SidebarPanel extends UiComponent {
		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Render.roundedRect(graphics, x, y, width, height, theme.radiusCard, Render.alpha(0x000000, 0.22F * alpha));
			Render.ring(graphics, x, y, width, height, theme.radiusCard, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			Ui.sectionLabel(graphics, UiWidgets.font(), "Modules", x + 12.0F, y + 12.0F, theme.textFaint, alpha);
		}
	}

	/** One category entry: accent pill, item icon, label and module count. */
	private static final class SidebarItem extends UiComponent {
		private final ChaosClickGui gui;
		private final Category category;
		private final Anim.Value selectedAnim = new Anim.Value(0.0F, 16.0F);

		private SidebarItem(ChaosClickGui gui, Category category) {
			this.gui = gui;
			this.category = category;
			this.setTooltip(category.displayName() + " — " + ModuleManager.byCategory(category).size() + " modules");
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			selectedAnim.set(gui.selected == category && gui.query.isBlank() ? 1.0F : 0.0F);
			selectedAnim.update(deltaSeconds, UiTheme.get().speed(16.0F));
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float selectedAmount = selectedAnim.get();
			float hoverAmount = hover.get();
			float radius = 9.0F;
			int background = Render.mix(Render.alpha(0xFFFFFFFF, 0.0F),
					Render.alpha(category.color(), 0.16F * alpha), selectedAmount);
			background = Render.mix(background, Render.alpha(0xFFFFFFFF, 0.06F * alpha), hoverAmount * (1.0F - selectedAmount));
			if (selectedAmount > 0.03F && theme.glow) {
				Render.glow(graphics, x + width * 0.5F, centerY(), width * 0.55F, category.color(),
						0.18F * selectedAmount * alpha);
			}
			Render.roundedRect(graphics, x, y, width, height, radius, background);
			if (selectedAmount > 0.05F) {
				Render.roundedRect(graphics, x, y + 5.0F, 2.5F, height - 10.0F, 1.25F,
						Render.alpha(category.color(), alpha * selectedAmount));
			}
			Ui.iconTile(graphics, x + 6.0F, centerY() - 10.0F, 20.0F, 6.0F, category.color(),
					alpha * (0.55F + 0.45F * Math.max(selectedAmount, hoverAmount * 0.8F)));
			Ui.itemIcon(graphics, category.icon(), x + 16.0F, centerY(), 0.8F, alpha);
			int textColor = Render.mix(theme.textDim, theme.text, Math.max(selectedAmount, hoverAmount * 0.7F));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), category.displayName(),
					width - 60.0F), x + 32.0F, centerY() - 4.0F, Render.alpha(textColor, alpha), false);
			String count = String.valueOf(ModuleManager.byCategory(category).size());
			Render.text(graphics, UiWidgets.font(), count, right() - 8.0F - UiWidgets.font().width(count), centerY() - 4.0F,
					Render.alpha(Render.mix(theme.textFaint, category.color(), Math.max(selectedAmount, 0.25F)), alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY) || button != 0) {
				return false;
			}
			gui.selected = category;
			gui.query = "";
			if (gui.search != null) {
				gui.search.box().setValue("");
			}
			gui.playClick(true);
			gui.refresh();
			return true;
		}
	}

	// ------------------------------------------------------------------ cards

	/** One feature: header with switch and expanding settings. */
	private static final class ModuleCard extends UiComponent {
		private static final float HEADER_ROOMY = 46.0F;
		private static final float HEADER_COMPACT = 40.0F;

		private final ChaosClickGui gui;
		private final Module module;
		private final String badge;
		private final List<UiComponent> rows = new ArrayList<>();
		private final Anim.Value expand = new Anim.Value(0.0F, 14.0F);
		private final Anim.Value switchHover = new Anim.Value(0.0F, 16.0F);
		private boolean rowsBuilt;
		private boolean expandedBefore;

		private ModuleCard(ChaosClickGui gui, Module module, String badge) {
			this.gui = gui;
			this.module = module;
			this.badge = badge;
			this.setTooltip(module.description() + "\n§7" + module.category().displayName()
					+ (keybindHint() == null ? "" : "  ·  key: " + keybindHint()));
		}

		private float headerHeight() {
			return UiTheme.get().compact ? HEADER_COMPACT : HEADER_ROOMY;
		}

		private boolean expanded() {
			return gui.expanded.getOrDefault(module.id(), Boolean.FALSE);
		}

		private float rowsHeight() {
			if (!expandedBefore || rows.isEmpty()) {
				return 0.0F;
			}
			float total = 0.0F;
			for (UiComponent row : rows) {
				total += row.height() + 2.0F;
			}
			return total + 10.0F;
		}

		private void buildRows(float rowWidth) {
			rows.clear();
			clearChildren();
			float y = 0.0F;
			for (Setting<?> setting : module.settings()) {
				if (setting == module.enabled()) {
					continue;
				}
				UiComponent row = UiWidgets.forSetting(setting, gui);
				if (row == null) {
					continue;
				}
				row.setBounds(0.0F, y, rowWidth, UiWidgets.rowHeight(setting));
				row.setTooltip(setting.description
						+ (setting.description == null || setting.description.isEmpty() ? "" : "\n")
						+ "§7right-click to reset  ·  default " + setting.display());
				rows.add(row);
				addChild(row);
				y += row.height() + 2.0F;
			}
			rowsBuilt = true;
		}

		private String keybindHint() {
			if (!UiTheme.get().keybindHints) {
				return null;
			}
			try {
				return switch (module.id()) {
					case "radial_menu" -> dev.chaosutils.core.Keybinds.radialMenu.getTranslatedKeyMessage().getString();
					case "gui" -> dev.chaosutils.core.Keybinds.openGui.getTranslatedKeyMessage().getString();
					case "zoom" -> dev.chaosutils.core.Keybinds.zoom.getTranslatedKeyMessage().getString();
					case "perspective_lock" -> dev.chaosutils.core.Keybinds.freeLook.getTranslatedKeyMessage().getString();
					case "container_search" -> dev.chaosutils.core.Keybinds.searchContainer.getTranslatedKeyMessage().getString();
					case "chat_history" -> dev.chaosutils.core.Keybinds.chatHistory.getTranslatedKeyMessage().getString();
					case "screenshots" -> dev.chaosutils.core.Keybinds.screenshotPopup.getTranslatedKeyMessage().getString();
					case "gamma" -> dev.chaosutils.core.Keybinds.toggleGamma.getTranslatedKeyMessage().getString();
					case "waypoint" -> dev.chaosutils.core.Keybinds.addWaypoint.getTranslatedKeyMessage().getString();
					default -> null;
				};
			} catch (Throwable ignored) {
				return null;
			}
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			boolean expanded = expanded();
			if (expanded && !rowsBuilt) {
				buildRows(width - 24.0F);
			}
			if (expanded) {
				expandedBefore = true;
			}
			expand.set(expanded ? 1.0F : 0.0F);
			expand.update(deltaSeconds, UiTheme.get().speed(14.0F));
			float extra = expandedBefore ? rowsHeight() * Anim.easeOutQuint(expand.get()) : 0.0F;
			height = headerHeight() + extra;
			super.update(deltaSeconds, mouseX, mouseY);
			switchHover.set(isOverSwitch(mouseX, mouseY) ? 1.0F : 0.0F);
			switchHover.update(deltaSeconds, UiTheme.get().speed(16.0F));
			float rowY = headerHeight() + 6.0F;
			for (UiComponent row : rows) {
				row.setBounds(x + 12.0F, y + rowY, width - 24.0F, row.height());
				row.setLayerAlpha(layerAlpha * Anim.clamp01(expand.get()));
				row.setVisible(expand.get() > 0.6F);
				if (row.isVisible()) {
					row.update(deltaSeconds, mouseX, mouseY);
				}
				rowY += row.height() + 2.0F;
			}
		}

		private boolean isOverSwitch(float mouseX, float mouseY) {
			float switchX = right() - 44.0F;
			return mouseX >= switchX && mouseX <= switchX + 32.0F && mouseY >= y && mouseY <= y + headerHeight();
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			int accent = module.category().color();
			boolean on = module.isEnabled();
			float radius = theme.radiusCard;
			float hoverAmount = hover.get();
			float expandAmount = expand.get();

			graphics.pose().pushMatrix();
			graphics.pose().translate(0.0F, offset);

			Ui.card(graphics, x, y, width, height, radius, theme, hoverAmount, on, alpha);

			// Icon tile
			Ui.iconTile(graphics, x + 12.0F, y + (headerHeight() - 26.0F) * 0.5F, 26.0F, 8.0F, accent, alpha);
			Ui.itemIcon(graphics, module.category().icon(), x + 25.0F, y + headerHeight() * 0.5F, 0.95F, alpha);

			float textX = x + 48.0F;
			float textRight = x + width - 92.0F;
			if (badge != null) {
				int badgeWidth = UiWidgets.font().width(badge) + 12;
				Render.roundedRect(graphics, textX, y + 9.0F, badgeWidth, 12.0F, 6.0F,
						Render.alpha(accent, 0.18F * alpha));
				Render.text(graphics, UiWidgets.font(), badge, textX + 6.0F, y + 11.0F, Render.alpha(accent, alpha), false);
				textX += badgeWidth + 6.0F;
			}
			Render.boldText(graphics, UiWidgets.font(),
					Render.ellipsize(UiWidgets.font(), module.name(), textRight - textX),
					textX, y + 10.0F, Render.alpha(on ? theme.text : theme.textDim, alpha), false);

			String keybind = keybindHint();
			float descriptionRight = textRight;
			if (keybind != null && !keybind.isEmpty()) {
				int chipWidth = UiWidgets.font().width(keybind) + 12;
				float chipX = textX + Math.min(UiWidgets.font().width(module.name()) + 8.0F, textRight - textX - chipWidth);
				Render.roundedRect(graphics, chipX, y + 8.0F, chipWidth, 13.0F, 6.5F,
						Render.alpha(0xFFFFFFFF, 0.07F * alpha));
				Render.text(graphics, UiWidgets.font(), keybind, chipX + 6.0F, y + 10.5F,
						Render.alpha(theme.textFaint, alpha), false);
			}
			Render.text(graphics, UiWidgets.font(),
					Render.ellipsize(UiWidgets.font(), module.description(), descriptionRight - textX),
					textX, y + headerHeight() - 18.0F,
					Render.alpha(Render.mix(theme.textFaint, theme.textDim, hoverAmount), alpha), false);

			// Switch (drawn here, not as a child, so the whole card stays one component).
			float switchWidth = 30.0F;
			float switchHeight = 16.0F;
			float switchX = right() - switchWidth - 44.0F;
			float switchY = y + (headerHeight() - switchHeight) * 0.5F;
			float onAmount = on ? 1.0F : 0.0F;
			int track = Render.mix(Render.alpha(theme.track, alpha),
					Render.alpha(Render.mix(accent, 0xFFFFFFFF, switchHover.get() * 0.12F), alpha), onAmount);
			if (onAmount > 0.05F && theme.glow) {
				Render.glow(graphics, switchX + switchWidth * 0.5F, switchY + switchHeight * 0.5F, switchWidth * 1.15F,
						accent, 0.30F * onAmount * alpha);
			}
			Render.roundedRect(graphics, switchX, switchY, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.ring(graphics, switchX, switchY, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(theme.outline, alpha));
			float knobRadius = switchHeight * 0.5F - 1.6F;
			float knobX = switchX + switchHeight * 0.5F + (switchWidth - switchHeight) * onAmount;
			Ui.knob(graphics, knobX, switchY + switchHeight * 0.5F, knobRadius,
					Render.mix(0xFFD7D8E4, 0xFFFFFFFF, onAmount), alpha);

			// Expander chevron
			float chevronX = right() - 22.0F;
			float chevronY = y + headerHeight() * 0.5F;
			Ui.chevron(graphics, chevronX, chevronY, 7.0F, 90.0F * expandAmount,
					Render.alpha(Render.mix(theme.textFaint, theme.text, hoverAmount), alpha));

			// Settings
			if (expandAmount > 0.02F && expandedBefore) {
				float clipTop = y + headerHeight();
				float clipHeight = Math.max(0.0F, height - headerHeight());
				Render.scissor(graphics, x, clipTop, width, clipHeight);
				Ui.divider(graphics, x + 12.0F, y + headerHeight() + 2.0F, width - 24.0F, theme, alpha * 0.8F);
				for (UiComponent row : rows) {
					if (!row.isVisible()) {
						continue;
					}
					boolean rowHovered = row.contains(mouseX, mouseY);
					if (rowHovered) {
						Render.roundedRect(graphics, row.x() - 4.0F, row.y() - 2.0F, row.width() + 8.0F, row.height() + 2.0F,
								6.0F, Render.alpha(0xFFFFFFFF, 0.045F * alpha));
					}
					row.render(graphics, mouseX, mouseY, deltaSeconds);
				}
				Render.unscissor(graphics);
			}
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			boolean overSwitch = isOverSwitch(mouseX, mouseY);
			float rowsTop = y + headerHeight();
			if (button == 0 && expand.get() > 0.6F && mouseY > rowsTop) {
				for (UiComponent row : rows) {
					if (row.isVisible() && row.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
				return true;
			}
			if (mouseY > y + headerHeight()) {
				return false;
			}
			if (button == 0 && overSwitch) {
				module.enabled().toggle();
				gui.playClick(module.isEnabled());
				return true;
			}
			if (button == 1) {
				if (module.isEnabled()) {
					module.enabled().toggle();
				}
				for (Setting<?> setting : module.settings()) {
					if (setting != module.enabled()) {
						setting.reset();
					}
				}
				gui.playClick(false);
				gui.toast(module.name(), "reset to defaults", UiTheme.get().warning);
				return true;
			}
			if (button == 0) {
				gui.expanded.put(module.id(), !expanded());
				gui.playClick(true);
				return true;
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
			for (UiComponent row : rows) {
				if (row.isVisible() && row.mouseScrolled(mouseX, mouseY, amount)) {
					return true;
				}
			}
			return false;
		}
	}

	/** Search helper used by the list screens. */
	public static List<Module> searchResults(String query) {
		return ModuleManager.search(query == null ? "" : query.toLowerCase(Locale.ROOT));
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Base class of every ChaosUtils screen.
 *
 * <p>Owns the floating window, the component list, the modal layer, the toast stack and the custom
 * tooltip renderer. Screens only describe their layout in {@link #buildLayout()}.
 *
 * <p>Two details matter for the renderer: content is clipped to the window body with a scissor, and
 * vanilla widgets (text fields) are only registered while no modal is open, so a dialog can never
 * end up behind a shadow of a text field. Everything is drawn through {@link Render} - the game's
 * own {@code renderBackground} is never called from here, because the screen pipeline already ran
 * it (including the frame's single blur) before {@code render} is reached.
 */
public abstract class ChaosScreen extends Screen {
	protected final Screen parent;
	protected final UiWindow window;
	private final float defaultWidth;
	private final float defaultHeight;

	private final List<UiComponent> content = new ArrayList<>();
	private final List<EditBox> contentInputs = new ArrayList<>();
	private final List<EditBox> modalInputs = new ArrayList<>();
	private final List<UiModals.Modal> modals = new ArrayList<>();
	private final List<Toast> toasts = new ArrayList<>();

	private final Anim.Value appear = new Anim.Value(0.0F, 10.0F);
	private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);
	private boolean closing;
	private boolean closeDelivered;
	private long closeRequestedAt;
	private long lastFrameNanos;
	protected float deltaSeconds;
	private float mouseX;
	private float mouseY;
	/** Window position the current layout was built for; dragging shifts the drawing instead of
	 *  rebuilding (which would restart every entrance animation every frame). */
	private float layoutX;
	private float layoutY;
	private float offsetX;
	private float offsetY;
	private UiWidgets.KeyField listeningField;

	protected ChaosScreen(Screen parent, Component title, String windowKey, float defaultWidth, float defaultHeight) {
		super(title);
		this.parent = parent;
		this.window = new UiWindow(windowKey);
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	// -------------------------------------------------------------- lifecycle

	/** Builds the component list; called on init and whenever the layout changes. */
	protected abstract void buildLayout();

	@Override
	protected void init() {
		content.clear();
		contentInputs.clear();
		modalInputs.clear();
		modals.clear();
		listeningField = null;
		closing = false;
		closeDelivered = false;
		appear.snap(0.0F);
		exit.snap(0.0F);
		window.open(this.width, this.height, defaultWidth, defaultHeight);
		layoutX = window.x();
		layoutY = window.y();
		offsetX = 0.0F;
		offsetY = 0.0F;
		buildLayoutSafely();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	/** Animated close: the screen asks the game to switch back once the fade has played out. */
	public void requestClose() {
		if (!closing) {
			closing = true;
			closeRequestedAt = System.nanoTime();
			exit.set(1.0F);
			for (UiModals.Modal modal : modals) {
				modal.close();
			}
			window.beginClose();
			clearModalInputs();
		}
	}

	/** Called once the close animation has finished, before the parent screen is restored. */
	protected void onClosed() {
	}

	@Override
	public void onClose() {
		requestClose();
	}

	@Override
	public void removed() {
		clearInputs();
		super.removed();
	}

	@Override
	public void tick() {
		super.tick();
		if (!closing || closeDelivered) {
			return;
		}
		// Normally the exit animation decides; the time limit is the guarantee that the player can
		// always leave the screen, whatever happens to the animation.
		boolean timedOut = System.nanoTime() - closeRequestedAt > CLOSE_TIMEOUT_NANOS;
		if (window.isGone() || timedOut) {
			closeDelivered = true;
			onClosed();
			if (this.minecraft != null) {
				this.minecraft.setScreen(parent);
			}
		}
	}

	/** Hard limit for the closing animation, in nanoseconds. */
	private static final long CLOSE_TIMEOUT_NANOS = 500_000_000L;

	// -------------------------------------------------------------- accessors

	public UiTheme theme() {
		return UiTheme.get();
	}

	/** Font accessor for widgets. */
	public Font font() {
		return this.font;
	}

	protected UiWindow window() {
		return window;
	}

	protected float bodyX() {
		return window.bodyX();
	}

	protected float bodyY() {
		return window.bodyY();
	}

	protected float bodyWidth() {
		return window.bodyWidth();
	}

	protected float bodyHeight() {
		return window.bodyHeight();
	}

	protected float alpha() {
		return Anim.clamp01(appear.get()) * window.alpha();
	}

	protected float mouseX() {
		return mouseX;
	}

	protected float mouseY() {
		return mouseY;
	}

	protected boolean hasModal() {
		return !modals.isEmpty();
	}

	protected void add(UiComponent component) {
		content.add(component);
	}

	protected void clearContent() {
		content.clear();
	}

	protected List<UiComponent> components() {
		return content;
	}

	/**
	 * Builds the layout and turns a failure into a visible message instead of an empty window.
	 *
	 * <p>A screen whose {@code buildLayout} throws would otherwise render nothing but its frame -
	 * which is impossible to tell apart from "the interface is broken", so the error is logged and
	 * shown.
	 */
	private void buildLayoutSafely() {
		try {
			buildLayout();
		} catch (Throwable throwable) {
			dev.chaosutils.ChaosUtils.LOGGER.error("ChaosUtils: layout of {} failed",
					getClass().getSimpleName(), throwable);
			content.clear();
			UiWidgets.Label error = new UiWidgets.Label("Layout error - see latest.log", theme().negative);
			error.setBounds(24.0F, window.y() + window.titleHeight() + 24.0F, 320.0F, 14.0F);
			content.add(error);
		}
	}

	/** Rebuilds the layout while keeping the window position and size. */
	protected void refresh() {
		content.clear();
		layoutX = window.x();
		layoutY = window.y();
		offsetX = 0.0F;
		offsetY = 0.0F;
		buildLayoutSafely();
		for (UiComponent component : content) {
			component.snapAppear();
		}
	}

	/** Content coordinates of the pointer (the layout is anchored to the window position). */
	protected float contentMouseX() {
		return mouseX - offsetX;
	}

	protected float contentMouseY() {
		return mouseY - offsetY;
	}

	// ---------------------------------------------------------------- inputs

	/** Registers a vanilla text field so IME, clipboard and selection behave like vanilla. */
	public EditBox addInput(EditBox input) {
		contentInputs.add(input);
		addRenderableWidget(input);
		return input;
	}

	public void removeInput(EditBox input) {
		contentInputs.remove(input);
		removeWidget(input);
	}

	/** Registers a text field that belongs to a modal (stays visible above the dim layer). */
	public EditBox addModalInput(EditBox input) {
		modalInputs.add(input);
		addRenderableWidget(input);
		setFocused(input);
		return input;
	}

	protected void clearInputs() {
		for (EditBox input : contentInputs) {
			removeWidget(input);
		}
		for (EditBox input : modalInputs) {
			removeWidget(input);
		}
		contentInputs.clear();
		modalInputs.clear();
	}

	private void hideContentInputs() {
		for (EditBox input : contentInputs) {
			removeWidget(input);
		}
	}

	private void showContentInputs() {
		for (EditBox input : contentInputs) {
			addRenderableWidget(input);
		}
		setFocused(null);
	}

	private void clearModalInputs() {
		for (EditBox input : modalInputs) {
			removeWidget(input);
		}
		modalInputs.clear();
		setFocused(null);
	}

	// ----------------------------------------------------------------- modals

	protected void pushModal(UiModals.Modal modal) {
		modal.setScreenBounds(this.width, this.height);
		hideContentInputs();
		modals.add(modal);
	}

	/** Hook for screens that need to react when one of their dialogs is done. */
	protected void modalClosed(UiModals.Modal modal) {
	}

	/** Text editor dialog used by string settings and by the list screens. */
	public void openTextModal(String title, String initial, int maxLength, Consumer<String> onAccept) {
		EditBox box = new EditBox(this.font, 0, 0, 200, 16, Component.literal(title));
		box.setBordered(false);
		box.setTextColor(0xFFF4F5FA);
		UiModals.TextPrompt prompt = new UiModals.TextPrompt(title, "Type here…", initial, maxLength, onAccept, box);
		prompt.place(this.width * 0.5F, this.height * 0.5F);
		addModalInput(box);
		pushModal(prompt);
	}

	/** Colour picker for any colour setting. */
	public void openColorModal(Setting.Color setting) {
		UiModals.ColorPicker picker = UiModals.colorPicker(setting, this::addModalInput);
		picker.place(this.width * 0.5F, this.height * 0.5F);
		pushModal(picker);
	}

	public void openConfirm(String title, String message, String confirmLabel, Runnable onConfirm) {
		pushModal(UiModals.confirm(title, message, confirmLabel, onConfirm));
	}

	public void openInfo(String title, String message) {
		pushModal(UiModals.info(title, message));
	}

	// ----------------------------------------------------------------- toasts

	public void toast(String message) {
		toast(null, message, theme().accent);
	}

	public void toast(String title, String message, int color) {
		Toast toast = new Toast(title, message, color);
		toast.slide.snap(0.0F);
		toasts.add(toast);
		while (toasts.size() > 4) {
			toasts.remove(0);
		}
	}

	// ------------------------------------------------------------------ sound

	public void playClick(boolean positive) {
		if (!theme().sounds || this.minecraft == null || this.minecraft.level == null) {
			return;
		}
		try {
			var sound = SoundLookup.ui(SoundLookup.uiClick(), positive ? 1.7F : 1.2F, 0.35F);
			if (sound != null) {
				this.minecraft.getSoundManager().play(sound);
			}
		} catch (Throwable ignored) {
			// UI sounds are optional
		}
	}

	// ----------------------------------------------------------------- render

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
		long now = System.nanoTime();
		deltaSeconds = lastFrameNanos == 0 ? 0.016F : Math.min(0.1F, (now - lastFrameNanos) / 1_000_000_000.0F);
		lastFrameNanos = now;
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		appear.set(1.0F);
		appear.update(deltaSeconds, theme().speed(10.0F));
		if (closing) {
			exit.update(deltaSeconds, theme().speed(14.0F));
		}
		tickAnimations();

		Backdrop.render(graphics, this, theme());

		// ------- window and content
		window.update(deltaSeconds, mouseX, mouseY);
		offsetX = window.x() - layoutX;
		offsetY = window.y() - layoutY;
		float windowAlpha = alpha();
		boolean modalOpen = !modals.isEmpty();
		float contentAlpha = windowAlpha * (modalOpen ? 0.35F : 1.0F);
		window.renderShell(graphics, theme());
		renderHeader(graphics, windowAlpha);
		window.renderCloseButton(graphics, theme(), windowAlpha);

		Render.scissor(graphics, layoutBodyX() + offsetX, layoutBodyY() + offsetY, window.bodyWidth(), window.bodyHeight());
		graphics.pose().pushMatrix();
		graphics.pose().translate(offsetX, offsetY);
		for (UiComponent component : content) {
			if (component.isVisible()) {
				component.setLayerAlpha(contentAlpha);
				component.render(graphics, contentMouseX(), contentMouseY(), deltaSeconds);
			}
		}
		graphics.pose().popMatrix();
		Render.unscissor(graphics);

		// ------- vanilla widgets (text fields)
		super.render(graphics, mouseX, mouseY, deltaTicks);

		// ------- content that lives in screen space instead of inside the window
		renderScreenSpace(graphics, mouseX, mouseY);

		// ------- modals
		for (int i = 0; i < modals.size(); i++) {
			UiModals.Modal modal = modals.get(i);
			modal.setLayerAlpha(1.0F);
			modal.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		// ------- toasts (always on top of the interface)
		renderToasts(graphics);

		// ------- tooltip + footer
		renderFooter(graphics, windowAlpha);
		renderTooltip(graphics);
	}

	/**
	 * Screens may draw and interact outside of the window (the HUD editor drags its overlay ghosts
	 * across the whole screen). Called after the window, before the modals.
	 */
	protected void renderScreenSpace(GuiGraphics graphics, float mouseX, float mouseY) {
	}

	/** Screen-space click. Returning {@code true} consumes the click. */
	protected boolean mouseClickedScreenSpace(float mouseX, float mouseY, int button) {
		return false;
	}

	/** Screen-space drag, checked before the window so a drag can leave the window. */
	protected boolean mouseDraggedScreenSpace(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		return false;
	}

	/** Screen-space release, checked before the window for the same reason. */
	protected boolean mouseReleasedScreenSpace(float mouseX, float mouseY, int button) {
		return false;
	}

	/** Window header text; screens can replace it to show context. */
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		Render.text(graphics, this.font, this.title.getString(), window.x() + 16.0F, window.y() + 13.0F,
				Render.alpha(theme().text, alpha), false);
	}

	private float layoutBodyX() {
		return layoutX;
	}

	private float layoutBodyY() {
		return layoutY + window.titleHeight();
	}

	/** Optional footer line at the bottom of the content area. */
	protected void renderFooter(GuiGraphics graphics, float alpha) {
	}

	private void renderToasts(GuiGraphics graphics) {
		if (toasts.isEmpty()) {
			return;
		}
		UiTheme theme = theme();
		float baseY = this.height - 30.0F;
		for (int i = toasts.size() - 1; i >= 0; i--) {
			Toast toast = toasts.get(i);
			float alpha = toast.alpha();
			if (alpha <= 0.01F) {
				continue;
			}
			String text = toast.title == null || toast.title.isEmpty()
					? toast.message
					: toast.title + "  ·  " + toast.message;
			float width = Math.min(300.0F, this.font.width(text) + 40.0F);
			float x = (this.width - width) * 0.5F;
			float y = baseY - (toasts.size() - 1 - i) * 26.0F + toast.offset();
			Render.softShadow(graphics, x, y, width, 22.0F, 11.0F, 0.8F * alpha);
			Render.roundedRect(graphics, x, y, width, 22.0F, 11.0F, Render.alpha(theme.windowBottom, alpha));
			Render.ring(graphics, x, y, width, 22.0F, 11.0F, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			Ui.dot(graphics, x + 12.0F, y + 11.0F, 3.0F, Render.alpha(toast.color, alpha));
			Render.text(graphics, this.font, Render.ellipsize(this.font, text, width - 34.0F), x + 22.0F, y + 7.0F,
					Render.alpha(theme.text, alpha), false);
		}
	}

	private void renderTooltip(GuiGraphics graphics) {
		if (!theme().tooltips || hasModal()) {
			return;
		}
		String tooltip = hoveredTooltip();
		if (tooltip == null || tooltip.isEmpty()) {
			return;
		}
		UiTheme theme = theme();
		List<String> lines = new ArrayList<>();
		for (String raw : tooltip.split("\n")) {
			lines.addAll(UiModals.Dialog.wrap(raw, 190.0F));
		}
		int widest = 0;
		for (String line : lines) {
			widest = Math.max(widest, this.font.width(line));
		}
		float boxWidth = widest + 18.0F;
		float boxHeight = lines.size() * 11.0F + 12.0F;
		float x = Anim.clamp(mouseX + 12.0F, 4.0F, this.width - boxWidth - 4.0F);
		float y = Anim.clamp(mouseY + 14.0F, 4.0F, this.height - boxHeight - 4.0F);
		Render.softShadow(graphics, x, y, boxWidth, boxHeight, 7.0F, 1.0F);
		Render.roundedRect(graphics, x, y, boxWidth, boxHeight, 7.0F, Render.alpha(0xF4141420, 0.98F));
		Render.ring(graphics, x, y, boxWidth, boxHeight, 7.0F, 1.0F, Render.alpha(theme.outlineStrong, 1.0F));
		for (int i = 0; i < lines.size(); i++) {
			int color = i == 0 ? theme.text : theme.textDim;
			Render.text(graphics, this.font, lines.get(i), x + 9.0F, y + 6.0F + i * 11.0F, color, false);
		}
	}

	private String hoveredTooltip() {
		if (!modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			return top.contains(mouseX, mouseY) ? top.tooltip() : null;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (!component.isVisible() || !component.contains(mouseX, mouseY)) {
				continue;
			}
			String tip = deepestTooltip(component);
			if (tip != null) {
				return tip;
			}
		}
		return null;
	}

	/** Walks the component tree so a hovered setting row wins over its card. */
	private String deepestTooltip(UiComponent component) {
		for (UiComponent child : component.children()) {
			if (child.isVisible() && child.contains(mouseX, mouseY)) {
				String tip = deepestTooltip(child);
				if (tip != null) {
					return tip;
				}
			}
		}
		return component.tooltip();
	}

	// ------------------------------------------------------------------ input

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		this.mouseX = clickX;
		this.mouseY = clickY;
		if (closing) {
			return true;
		}
		if (!modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			top.setScreenBounds(this.width, this.height);
			return top.mouseClicked(clickX, clickY, button) || super.mouseClicked(event, doubled);
		}
		// A key field that is waiting for a key must not be disturbed by plain clicks elsewhere.
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseClicked(clickX - offsetX, clickY - offsetY, button)) {
				return true;
			}
		}
		if (window.isOverCloseButton(clickX, clickY)) {
			playClick(false);
			requestClose();
			return true;
		}
		if (window.mouseClicked(clickX, clickY, button)) {
			return true;
		}
		if (mouseClickedScreenSpace(clickX, clickY, button)) {
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		if (!modals.isEmpty()) {
			return modals.get(modals.size() - 1).mouseReleased(clickX, clickY, button) || super.mouseReleased(event);
		}
		boolean handled = mouseReleasedScreenSpace(clickX, clickY, button);
		if (window.mouseReleased(button)) {
			handled = true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseReleased(clickX - offsetX, clickY - offsetY, button)) {
				handled = true;
			}
		}
		if (window.wasResized()) {
			// A resize changes the available width, so the layout is rebuilt once at the end.
			refresh();
			handled = true;
		}
		return handled || super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		float clickX = (float) event.x();
		float clickY = (float) event.y();
		int button = event.button();
		if (!modals.isEmpty()) {
			return modals.get(modals.size() - 1).mouseDragged(clickX, clickY, button, (float) deltaX, (float) deltaY)
					|| super.mouseDragged(event, deltaX, deltaY);
		}
		if (mouseDraggedScreenSpace(clickX, clickY, button, (float) deltaX, (float) deltaY)) {
			return true;
		}
		if (window.mouseDragged(clickX, clickY, button)) {
			// Dragging only shifts the drawing (see offsetX/offsetY); no rebuild needed.
			return true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible() && component.mouseDragged(clickX - offsetX, clickY - offsetY, button,
					(float) deltaX, (float) deltaY)) {
				return true;
			}
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!modals.isEmpty()) {
			return true;
		}
		for (int i = content.size() - 1; i >= 0; i--) {
			UiComponent component = content.get(i);
			if (component.isVisible()
					&& component.mouseScrolled((float) mouseX - offsetX, (float) mouseY - offsetY, verticalAmount)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (closing) {
			return true;
		}
		// Escape closes the top modal first, then the screen - never the game menu.
		if (event.isEscape()) {
			if (listeningField != null) {
				listeningField.cancelListening();
				listeningField = null;
				return true;
			}
			if (!modals.isEmpty()) {
				modals.get(modals.size() - 1).close();
				return true;
			}
			requestClose();
			return true;
		}
		// Enter accepts the top modal (text prompts and colour pickers).
		if ((event.key() == 257 || event.key() == 335) && !modals.isEmpty()) {
			UiModals.Modal top = modals.get(modals.size() - 1);
			if (top instanceof UiModals.TextPrompt prompt) {
				prompt.accept();
				return true;
			}
		}
		return super.keyPressed(event);
	}

	/** Screens call this while a hotkey field is capturing input. */
	protected void setListeningField(UiWidgets.KeyField field) {
		this.listeningField = field;
	}

	/** Tracks modal completion so inputs can be restored exactly once. */
	protected final void updateModals(float deltaSeconds) {
		for (int i = modals.size() - 1; i >= 0; i--) {
			UiModals.Modal modal = modals.get(i);
			modal.setScreenBounds(this.width, this.height);
			modal.update(deltaSeconds, mouseX, mouseY);
			if (modal.isFinished()) {
				modals.remove(i);
				clearModalInputs();
				if (modals.isEmpty()) {
					showContentInputs();
				}
				modalClosed(modal);
			}
		}
	}

	/** Runs the per-frame update pass of every content component. */
	protected final void updateContent(float deltaSeconds) {
		for (UiComponent component : content) {
			if (component.isVisible()) {
				component.setLayerAlpha(alpha());
				component.update(deltaSeconds, contentMouseX(), contentMouseY());
			}
		}
	}

	public void openHudEditor() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(new HudEditorScreen(this));
		}
	}

	// ------------------------------------------------------------------ toasts

	private static final class Toast {
		private final String title;
		private final String message;
		private final int color;
		private final Anim.Value life = new Anim.Value(0.0F, 6.0F);
		private final Anim.Value slide = new Anim.Value(0.0F, 12.0F);
		private float age;

		private Toast(String title, String message, int color) {
			this.title = title;
			this.message = message;
			this.color = color;
		}

		private float alpha() {
			return Anim.clamp01(slide.get()) * life.get();
		}

		private float offset() {
			return (1.0F - Anim.easeOutQuint(slide.get())) * 14.0F;
		}

		private void update(float deltaSeconds) {
			age += deltaSeconds;
			slide.set(1.0F);
			slide.update(deltaSeconds, UiTheme.get().speed(13.0F));
			life.set(age > 3.0F ? 0.0F : 1.0F);
			life.update(deltaSeconds, age > 3.0F ? UiTheme.get().speed(6.0F) : 60.0F);
		}

		private boolean expired() {
			return age > 3.6F;
		}
	}

	/** Updates the toast stack; part of the screen's per-frame update pass. */
	protected final void updateToasts(float deltaSeconds) {
		for (int i = toasts.size() - 1; i >= 0; i--) {
			Toast toast = toasts.get(i);
			toast.update(deltaSeconds);
			if (toast.expired()) {
				toasts.remove(i);
			}
		}
	}

	/** Single entry point for the per-frame update of everything this screen owns. */
	protected final void tickAnimations() {
		updateModals(deltaSeconds);
		updateContent(deltaSeconds);
		updateToasts(deltaSeconds);
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

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.config.RadialElement;
import dev.chaosutils.config.Setting;
import dev.chaosutils.config.Waypoint;
import dev.chaosutils.core.ChatLog;
import dev.chaosutils.core.Keybinds;
import dev.chaosutils.core.Clipboard;
import dev.chaosutils.feature.qol.ScreenshotManager;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The secondary ChaosUtils windows.
 *
 * <p>All four share the same shell ({@link ChaosScreen}: floating, draggable, animated) and only
 * differ in their content, so the interface feels like one product instead of a pile of screens.
 * Every window opens on top of the previous one and returns there when it is closed.
 */
public final class ChaosScreens {
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
			.withZone(ZoneId.systemDefault());

	private ChaosScreens() {
	}

	// ============================================================ radial editor

	/** Create, edit, reorder and preview the radial menu entries. */
	public static final class RadialEditorScreen extends ChaosScreen {
		private int selected = -1;
		private UiWidgets.ScrollList list;

		public RadialEditorScreen(Screen parent) {
			super(parent, Component.literal("Radial Menu"), "window.radial", 720.0F, 430.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			if (selected >= entries.size()) {
				selected = entries.isEmpty() ? -1 : entries.size() - 1;
			}
			float contentX = window().x() + 18.0F;
			float top = window().y() + window().titleHeight() + 14.0F;
			float listWidth = 220.0F;

			UiWidgets.Label entriesLabel = new UiWidgets.Label("Slices", theme.textFaint);
			entriesLabel.setBounds(contentX, top, listWidth, 10.0F);
			add(entriesLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(4.0F);
			list.setBounds(contentX, top + 14.0F, listWidth, Math.max(80.0F, window().height() - window().titleHeight() - 62.0F));
			list.snapAppear();
			add(list);
			for (int i = 0; i < entries.size(); i++) {
				EntryRow row = new EntryRow(this, entries.get(i), i);
				row.setBounds(0.0F, 0.0F, listWidth, 30.0F);
				list.addItem(row);
			}
			list.layout();

			// ---- editor of the selected slice
			float editorX = contentX + listWidth + 20.0F;
			float editorWidth = Math.max(180.0F, window().right() - 18.0F - editorX);
			RadialElement element = current();
			if (element == null) {
				UiWidgets.Label empty = new UiWidgets.Label("Select a slice on the left, or add a new one.",
						theme.textDim);
				empty.setBounds(editorX, top + 20.0F, editorWidth, 12.0F);
				add(empty);
			} else {
				float previewHeight = 150.0F;
				RingPreview preview = new RingPreview();
				preview.setBounds(editorX, top, editorWidth, previewHeight);
				add(preview);

				float rowY = top + previewHeight + 6.0F;
				add(fieldRow(element, "Name", element.name, editorX, rowY, editorWidth, () ->
						openTextModal("Display name", element.name, 48, value -> {
							element.name = value;
							ChaosConfig.markDirty();
							refresh();
						})));
				rowY += 24.0F;
				add(fieldRow(element, "Action", element.command, editorX, rowY, editorWidth, () ->
						openTextModal(element.type == RadialElement.ActionType.COPY_TEXT
								? "Text to copy" : "Command or chat text", element.command, 256, value -> {
							element.command = value;
							ChaosConfig.markDirty();
							refresh();
						})));
				rowY += 24.0F;

				UiWidgets.Button type = new UiWidgets.Button(element.type.label(),
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () -> {
							RadialElement.ActionType[] types = RadialElement.ActionType.values();
							element.type = types[(element.type.ordinal() + 1) % types.length];
							ChaosConfig.markDirty();
							refresh();
						});
				type.setLeftAligned(true);
				type.setBounds(editorX, rowY, editorWidth, 18.0F);
				type.setTooltip("Click to switch what this slice does.");
				add(type);
				rowY += 24.0F;

				UiWidgets.Button icon = new UiWidgets.Button("Icon: " + element.icon,
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () ->
						openTextModal("Item id", element.icon, 64, value -> {
							element.icon = value;
							ChaosConfig.markDirty();
							refresh();
						}));
				icon.setLeftAligned(true);
				icon.setIcon(element.iconStack());
				icon.setBounds(editorX, rowY, editorWidth, 18.0F);
				icon.setTooltip("Any item id, for example minecraft:compass.");
				add(icon);
				rowY += 24.0F;

				UiWidgets.Button colour = new UiWidgets.Button("Colour  " + String.format("#%06X", element.color & 0xFFFFFF),
						UiWidgets.Button.Variant.SOFT, element.solidColor(), () -> {
							Setting.Color temporary = new Setting.Color("radial_colour", "Slice colour",
									"Colour of this radial slice.", element.color);
							temporary.onChanged(value -> {
								element.color = value;
								ChaosConfig.markDirty();
							});
							openColorModal(temporary);
						});
				colour.setLeftAligned(true);
				colour.setBounds(editorX, rowY, editorWidth, 18.0F);
				add(colour);
				rowY += 22.0F;

				UiWidgets.Toggle enabled = new UiWidgets.Toggle(() -> element.enabled, value -> {
					element.enabled = value;
					ChaosConfig.markDirty();
					refresh();
				}, theme.accent);
				enabled.setBounds(editorX + editorWidth - 34.0F, rowY, 30.0F, 16.0F);
				enabled.setTooltip("Hide a slice without deleting it.");
				add(enabled);
				UiWidgets.Label enabledLabel = new UiWidgets.Label("Visible in the menu", theme.textDim);
				enabledLabel.setBounds(editorX, rowY, editorWidth - 40.0F, 12.0F);
				add(enabledLabel);
			}

			// ---- bottom bar
			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button add = new UiWidgets.Button("Add slice", UiWidgets.Button.Variant.PRIMARY, theme.accent, () -> {
				ChaosConfig.RADIAL_ELEMENTS.add(new RadialElement("New action", "/spawn", theme.accent,
						"minecraft:compass", RadialElement.ActionType.COMMAND));
				ChaosConfig.markDirty();
				selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
				refresh();
				toast("Slice added");
			});
			add.setBounds(contentX, bottom, 90.0F, 20.0F);
			add(add);

			UiWidgets.Button duplicate = new UiWidgets.Button("Duplicate", UiWidgets.Button.Variant.SOFT, theme.accent, () -> {
				RadialElement slice = current();
				if (slice != null) {
					ChaosConfig.RADIAL_ELEMENTS.add(slice.copy());
					ChaosConfig.markDirty();
					selected = ChaosConfig.RADIAL_ELEMENTS.size() - 1;
					refresh();
				}
			});
			duplicate.setBounds(contentX + 96.0F, bottom, 82.0F, 20.0F);
			add(duplicate);

			UiWidgets.Button delete = new UiWidgets.Button("Delete", UiWidgets.Button.Variant.DANGER, theme.negative, () -> {
				RadialElement slice = current();
				if (slice != null) {
					openConfirm("Delete slice", "Remove \"" + slice.name + "\" from the radial menu?", "Delete", () -> {
						ChaosConfig.RADIAL_ELEMENTS.remove(slice);
						ChaosConfig.markDirty();
						selected = Math.max(0, Math.min(selected, ChaosConfig.RADIAL_ELEMENTS.size() - 1));
						refresh();
					});
				}
			});
			delete.setBounds(contentX + 184.0F, bottom, 74.0F, 20.0F);
			add(delete);
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Radial Menu", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = ChaosConfig.RADIAL_ELEMENTS.size() + " slices  ·  hold " + radialKeyName() + " in game";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		/** The key the player bound to the radial menu, as a readable name. */
		private static String radialKeyName() {
			try {
				return Keybinds.radialMenu.getTranslatedKeyMessage().getString();
			} catch (Throwable ignored) {
				return "the radial key";
			}
		}

		private RadialElement current() {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			return selected >= 0 && selected < entries.size() ? entries.get(selected) : null;
		}

		private UiComponent fieldRow(RadialElement element, String label, String value, float x, float y, float width,
				Runnable onEdit) {
			UiWidgets.Button button = new UiWidgets.Button(label + ":  " + Render.ellipsize(UiWidgets.font(), value, width - 90.0F),
					UiWidgets.Button.Variant.SOFT, element.solidColor(), onEdit);
			button.setLeftAligned(true);
			button.setBounds(x, y, width, 18.0F);
			return button;
		}

		private void move(int index, int direction) {
			List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
			int target = index + direction;
			if (index < 0 || index >= entries.size() || target < 0 || target >= entries.size()) {
				return;
			}
			RadialElement element = entries.get(index);
			entries.set(index, entries.get(target));
			entries.set(target, element);
			selected = target;
			ChaosConfig.markDirty();
			refresh();
		}

		/** One row in the slice list: colour dot, name, action type and reorder buttons. */
		private static final class EntryRow extends UiComponent {
			private final RadialEditorScreen screen;
			private final RadialElement element;
			private final int index;
			private final UiWidgets.IconButton up;
			private final UiWidgets.IconButton down;

			private EntryRow(RadialEditorScreen screen, RadialElement element, int index) {
				this.screen = screen;
				this.element = element;
				this.index = index;
				this.up = new UiWidgets.IconButton(
						(graphics, cx, cy, alpha) -> Ui.chevron(graphics, cx, cy, 5.0F, -90.0F,
								Render.alpha(0xFFFFFF, alpha)), 0xFFFFFFFF, () -> screen.move(index, -1));
				this.down = new UiWidgets.IconButton(
						(graphics, cx, cy, alpha) -> Ui.chevron(graphics, cx, cy, 5.0F, 90.0F,
								Render.alpha(0xFFFFFF, alpha)), 0xFFFFFFFF, () -> screen.move(index, 1));
				this.setTooltip(element.type.label());
				this.setAppearDelay(Math.min(0.2F, 0.02F * index));
			}

			@Override
			public void update(float deltaSeconds, float mouseX, float mouseY) {
				super.update(deltaSeconds, mouseX, mouseY);
				up.setBounds(right() - 42.0F, y + (height - 14.0F) * 0.5F, 14.0F, 14.0F);
				down.setBounds(right() - 24.0F, y + (height - 14.0F) * 0.5F, 14.0F, 14.0F);
				up.setLayerAlpha(layerAlpha);
				down.setLayerAlpha(layerAlpha);
				up.update(deltaSeconds, mouseX, mouseY);
				down.update(deltaSeconds, mouseX, mouseY);
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				boolean active = screen.selected == index;
				Render.roundedRect(graphics, x, y + offset, width, height, 8.0F,
						Render.mix(Render.alpha(0xFFFFFF, active ? 0.07F : 0.03F * alpha),
								Render.alpha(0xFFFFFF, 0.08F * alpha), hover.get()));
				if (active) {
					Render.ring(graphics, x, y + offset, width, height, 8.0F, 1.0F,
							Render.alpha(element.solidColor(), alpha * 0.55F));
				}
				Ui.dot(graphics, x + 10.0F, y + offset + 11.0F, 3.5F,
						Render.alpha(element.enabled ? element.solidColor() : theme.textFaint, alpha));
				Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), element.name, width - 60.0F),
						x + 18.0F, y + offset + 4.0F, Render.alpha(theme.text, alpha), false);
				Render.text(graphics, UiWidgets.font(), element.type.label(), x + 18.0F, y + offset + 15.0F,
						Render.alpha(theme.textFaint, alpha), false);
				up.render(graphics, mouseX, mouseY, deltaSeconds);
				down.render(graphics, mouseX, mouseY, deltaSeconds);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (!isHovered(mouseX, mouseY)) {
					return false;
				}
				if (up.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
				if (down.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
				if (button == 0) {
					screen.selected = index;
					screen.playClick(true);
					screen.refresh();
					return true;
				}
				return false;
			}
		}

		/** Live preview of the ring with the real geometry of the menu. */
		private final class RingPreview extends UiComponent {
			private final Anim.Value spin = new Anim.Value(0.0F, 0.4F);

			@Override
			public void update(float deltaSeconds, float mouseX, float mouseY) {
				super.update(deltaSeconds, mouseX, mouseY);
				spin.set(spin.target() + deltaSeconds * 4.0F);
				spin.update(deltaSeconds, UiTheme.get().speed(0.6F));
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				List<RadialElement> entries = ChaosConfig.RADIAL_ELEMENTS;
				float cx = centerX();
				float cy = centerY();
				float outer = Math.min(width, height) * 0.42F;
				float inner = outer * 0.42F;
				Render.roundedRect(graphics, x, y, width, height, UiTheme.get().radiusCard,
						Render.alpha(0x000000, 0.20F * alpha));
				UiTheme theme = UiTheme.get();
				if (entries.isEmpty()) {
					Render.centeredText(graphics, UiWidgets.font(), "No slices yet", cx, cy - 4.0F,
							Render.alpha(theme.textFaint, alpha), false);
					return;
				}
				float span = 360.0F / entries.size();
				float gap = Math.min(5.0F, span * 0.2F);
				for (int i = 0; i < entries.size(); i++) {
					RadialElement element = entries.get(i);
					float start = i * span - span * 0.5F + gap;
					float end = (i + 1) * span - span * 0.5F - gap;
					boolean active = selected == i;
					Render.arc(graphics, cx, cy, inner, active ? outer + 4.0F : outer, start, end,
							Render.alpha(element.solidColor(), (element.enabled ? 0.85F : 0.30F) * alpha));
					double mid = Math.toRadians((start + end) * 0.5);
					float radius = inner + (outer - inner) * 0.55F;
					float iconX = cx + (float) Math.sin(mid) * radius;
					float iconY = cy - (float) Math.cos(mid) * radius;
					Ui.itemIcon(graphics, element.iconStack(), iconX, iconY, 0.75F, alpha);
				}
				Render.circle(graphics, cx, cy, inner - 5.0F, Render.alpha(0x0C0D14, 0.8F * alpha));
				Render.ring(graphics, cx - inner + 5.0F, cy - inner + 5.0F, (inner - 5.0F) * 2.0F, (inner - 5.0F) * 2.0F,
						inner - 5.0F, 1.2F, Render.alpha(0x66FFFFFF, alpha * 0.6F));
				Render.centeredText(graphics, UiWidgets.font(), "preview", cx, cy - 4.0F,
						Render.alpha(theme.textFaint, alpha * 0.8F), false);
			}
		}
	}

	// =============================================================== chat log

	/** Searchable history of everything that happened in chat. */
	public static final class ChatHistoryScreen extends ChaosScreen {
		private String query = "";
		private int kindFilter = 0;
		private UiWidgets.ScrollList list;
		private UiWidgets.Label countLabel;
		private UiWidgets.SearchField search;

		public ChatHistoryScreen(Screen parent) {
			super(parent, Component.literal("Chat History"), "window.chat", 640.0F, 420.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			EditBox box = new EditBox(UiWidgets.font(), 0, 0, 200, 14, Component.literal("Search"));
			box.setBordered(false);
			box.setTextColor(0xFFF4F5FA);
			box.setHint(Component.literal("Search chat…"));
			box.setMaxLength(64);
			box.setValue(query);
			box.setResponder(value -> {
				if (!value.equals(query)) {
					query = value;
					rebuild();
				}
			});
			addInput(box);
			search = new UiWidgets.SearchField(box);
			search.place(contentX, top, Math.min(240.0F, contentWidth * 0.45F), 22.0F);
			search.setOnClear(() -> {
				query = "";
				rebuild();
			});
			add(search);

			String[] filters = {"All", "Chat", "Server", "System"};
			float chipX = search.right() + 10.0F;
			for (int i = 0; i < filters.length; i++) {
				int index = i;
				int width = UiWidgets.font().width(filters[i]) + 18;
				UiWidgets.Button chip = new UiWidgets.Button(filters[i],
						kindFilter == index ? UiWidgets.Button.Variant.PRIMARY : UiWidgets.Button.Variant.GHOST,
						theme.accent, () -> {
							kindFilter = index;
							rebuild();
						});
				chip.setBounds(chipX, top, width, 22.0F);
				add(chip);
				chipX += width + 6.0F;
			}

			countLabel = new UiWidgets.Label("", theme.textFaint);
			countLabel.setBounds(chipX + 6.0F, top + 7.0F, Math.max(10.0F, window().right() - 18.0F - chipX), 12.0F);
			add(countLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(2.0F);
			list.setBounds(contentX, top + 30.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 76.0F));
			list.snapAppear();
			add(list);
			rebuildList();

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button copyAll = new UiWidgets.Button("Copy everything", UiWidgets.Button.Variant.SOFT, theme.accent, () -> {
				this.minecraft.keyboardHandler.setClipboard(allText());
				toast("Copied", entries().size() + " lines", theme.accent);
			});
			copyAll.setBounds(contentX, bottom, 110.0F, 20.0F);
			add(copyAll);

			UiWidgets.Button clear = new UiWidgets.Button("Clear", UiWidgets.Button.Variant.DANGER, theme.negative, () ->
					openConfirm("Clear chat history", "Delete every stored chat line? This cannot be undone.",
							"Clear", () -> {
								ChatLog.clear();
								rebuild();
								toast("Chat history cleared");
							}));
			clear.setBounds(contentX + 116.0F, bottom, 70.0F, 20.0F);
			add(clear);
		}

		private ChatLog.Kind filter() {
			return switch (kindFilter) {
				case 1 -> ChatLog.Kind.PLAYER;
				case 2 -> ChatLog.Kind.SERVER;
				case 3 -> ChatLog.Kind.SYSTEM;
				default -> null;
			};
		}

		private List<ChatLog.Entry> entries() {
			return ChatLog.search(query, filter(), 200);
		}

		private void rebuild() {
			if (list == null) {
				return;
			}
			rebuildList();
		}

		private void rebuildList() {
			list.clearItems();
			for (ChatLog.Entry entry : entries()) {
				list.addItem(new ChatRow(entry));
			}
			list.layout();
			if (countLabel != null) {
				countLabel.setText(entries().size() + " lines");
			}
		}

		private String allText() {
			StringBuilder builder = new StringBuilder();
			for (ChatLog.Entry entry : entries()) {
				builder.append('[').append(TIME.format(entry.time())).append("] ").append(entry.plain()).append('\n');
			}
			return builder.toString();
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Chat History", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "survives reconnects  ·  stored locally";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		/** One chat line with kind badge, timestamp and a copy action. */
		private final class ChatRow extends UiComponent {
			private final ChatLog.Entry entry;

			private ChatRow(ChatLog.Entry entry) {
				this.entry = entry;
				this.setTooltip("Click to copy this line");
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				int accent = switch (entry.kind()) {
					case PLAYER -> 0xFF57D98A;
					case SERVER -> 0xFFF2B23E;
					case SYSTEM -> 0xFF7C9CFF;
				};
				Render.roundedRect(graphics, x, y + offset, width, height, 6.0F,
						Render.mix(Render.alpha(0xFFFFFF, 0.0F), Render.alpha(0xFFFFFF, 0.055F * alpha), hover.get()));
				Render.text(graphics, UiWidgets.font(), TIME.format(entry.time()), x + 4.0F, y + offset + 3.0F,
						Render.alpha(theme.textFaint, alpha), false);
				Ui.dot(graphics, x + 52.0F, y + offset + 7.0F, 2.5F, Render.alpha(accent, alpha));
				Render.text(graphics, UiWidgets.font(),
						Render.ellipsize(UiWidgets.font(), entry.plain(), width - 68.0F), x + 60.0F, y + offset + 3.0F,
						Render.alpha(theme.text, alpha), false);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (isHovered(mouseX, mouseY) && button == 0) {
					Clipboard.copyText(entry.plain());
					playClick(true);
					toast("Copied", "chat line", theme().accent);
					return true;
				}
				return false;
			}
		}
	}

	// ============================================================ screenshots

	/** Local screenshot manager: browse, copy, crop. Nothing ever leaves the machine. */
	public static final class ScreenshotScreen extends ChaosScreen {
		private final List<Path> files = new ArrayList<>();
		private int selected;
		private UiWidgets.ScrollList list;
		private UiWidgets.Label info;

		public ScreenshotScreen(Screen parent) {
			super(parent, Component.literal("Screenshots"), "window.shots", 620.0F, 420.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			files.clear();
			files.addAll(ScreenshotManager.recent(50));

			list = new UiWidgets.ScrollList();
			list.setSpacing(2.0F);
			list.setBounds(contentX, top + 14.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 78.0F));
			list.snapAppear();
			add(list);
			for (int i = 0; i < files.size(); i++) {
				list.addItem(new ShotRow(i));
			}
			list.layout();

			if (selected >= files.size()) {
				selected = Math.max(0, files.size() - 1);
			}

			info = new UiWidgets.Label(currentName(), theme.textDim);
			info.setBounds(contentX, top, contentWidth, 12.0F);
			add(info);

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button copyImage = new UiWidgets.Button("Copy image", UiWidgets.Button.Variant.PRIMARY, theme.accent, () -> {
				Path path = current();
				if (path != null) {
					Clipboard.copyImage(path);
					toast("Screenshot copied to the clipboard");
				}
			});
			copyImage.setBounds(contentX, bottom, 96.0F, 20.0F);
			add(copyImage);

			UiWidgets.Button crop = new UiWidgets.Button("Crop & copy", UiWidgets.Button.Variant.SOFT, theme.warning, () -> {
				Path path = current();
				if (path != null) {
					ScreenshotManager.cropWithConfiguredPreset(path);
					toast("Cropped and copied");
				}
			});
			crop.setBounds(contentX + 102.0F, bottom, 96.0F, 20.0F);
			crop.setTooltip("Uses the crop preset from the Screenshot Manager settings.");
			add(crop);

			UiWidgets.Button copyPath = new UiWidgets.Button("Copy path", UiWidgets.Button.Variant.GHOST, theme.accent, () -> {
				Path path = current();
				if (path != null) {
					Clipboard.copyText(path.toAbsolutePath().toString());
					toast("Path copied");
				}
			});
			copyPath.setBounds(contentX + 204.0F, bottom, 84.0F, 20.0F);
			add(copyPath);

			UiWidgets.Button folder = new UiWidgets.Button("Open folder", UiWidgets.Button.Variant.GHOST, theme.positive, () ->
					Clipboard.openFile(ScreenshotManager.screenshotsDirectory()));
			folder.setBounds(contentX + 294.0F, bottom, 92.0F, 20.0F);
			add(folder);
		}

		private Path current() {
			return selected >= 0 && selected < files.size() ? files.get(selected) : null;
		}

		private String currentName() {
			Path path = current();
			return path == null ? "No screenshots found yet" : path.getFileName().toString();
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Screenshots", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "everything stays on this machine";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		private final class ShotRow extends UiComponent {
			private final int index;

			private ShotRow(int index) {
				this.index = index;
				this.setTooltip(files.get(index).toAbsolutePath().toString());
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				Path path = files.get(index);
				boolean active = selected == index;
				float offset = appearOffset();
				Render.roundedRect(graphics, x, y + offset, width, height, 6.0F,
						Render.mix(Render.alpha(0xFFFFFF, active ? 0.07F : 0.02F * alpha),
								Render.alpha(0xFFFFFF, 0.07F * alpha), hover.get()));
				String stamp = TIME.format(Instant.ofEpochMilli(path.toFile().lastModified()));
				Render.text(graphics, UiWidgets.font(), stamp, x + 4.0F, y + offset + 3.0F,
						Render.alpha(theme.textFaint, alpha), false);
				Render.text(graphics, UiWidgets.font(),
						Render.ellipsize(UiWidgets.font(), path.getFileName().toString(), width - 62.0F),
						x + 54.0F, y + offset + 3.0F,
						Render.alpha(active ? theme.text : theme.textDim, alpha), false);
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (isHovered(mouseX, mouseY) && button == 0) {
					selected = index;
					if (info != null) {
						info.setText(currentName());
					}
					playClick(true);
					return true;
				}
				return false;
			}
		}
	}

	// ============================================================= waypoints

	/** Death markers and manual waypoints, client side only. */
	public static final class WaypointsScreen extends ChaosScreen {
		private final List<Waypoint> listed = new ArrayList<>();
		private UiWidgets.ScrollList list;
		private UiWidgets.Label countLabel;

		public WaypointsScreen(Screen parent) {
			super(parent, Component.literal("Waypoints"), "window.waypoints", 620.0F, 400.0F);
		}

		@Override
		protected void buildLayout() {
			UiTheme theme = UiTheme.get();
			float contentX = window().x() + 18.0F;
			float contentWidth = window().width() - 36.0F;
			float top = window().y() + window().titleHeight() + 14.0F;

			countLabel = new UiWidgets.Label("", theme.textFaint);
			countLabel.setBounds(contentX, top, contentWidth, 12.0F);
			add(countLabel);

			list = new UiWidgets.ScrollList();
			list.setSpacing(3.0F);
			list.setBounds(contentX, top + 16.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 62.0F));
			list.snapAppear();
			add(list);
			rebuildList();

			float bottom = window().bottom() - 30.0F;
			UiWidgets.Button add = new UiWidgets.Button("Add current position", UiWidgets.Button.Variant.PRIMARY,
					theme.accent, () -> {
						if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) {
							return;
						}
						ChaosConfig.WAYPOINTS.add(new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
								this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(),
								this.minecraft.level.dimension().identifier().toString(), theme.accent, false, 0L));
						ChaosConfig.markDirty();
						rebuildList();
						toast("Waypoint added");
					});
			add.setBounds(contentX, bottom, 130.0F, 20.0F);
			add(add);

			UiWidgets.Button clearTemporary = new UiWidgets.Button("Clear temporary", UiWidgets.Button.Variant.GHOST,
					theme.warning, () -> {
						ChaosConfig.WAYPOINTS.removeIf(waypoint -> waypoint.temporary);
						ChaosConfig.markDirty();
						rebuildList();
						toast("Temporary markers cleared");
					});
			clearTemporary.setBounds(contentX + 136.0F, bottom, 112.0F, 20.0F);
			add(clearTemporary);
		}

		private void rebuildList() {
			list.clearItems();
			listed.clear();
			listed.addAll(ChaosConfig.WAYPOINTS);
			for (Waypoint waypoint : listed) {
				list.addItem(new WaypointRow(waypoint));
			}
			list.layout();
			if (countLabel != null) {
				countLabel.setText(listed.size() + " waypoints  ·  left click copies, right click deletes");
			}
		}

		@Override
		protected void renderHeader(GuiGraphics graphics, float alpha) {
			UiTheme theme = theme();
			Render.boldText(graphics, UiWidgets.font(), "Waypoints", window().x() + 16.0F, window().y() + 13.0F,
					Render.alpha(theme.text, alpha), false);
			String subtitle = "drawn client-side only";
			Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle),
					window().y() + 13.0F, Render.alpha(theme.textFaint, alpha), false);
		}

		private final class WaypointRow extends UiComponent {
			private final Waypoint waypoint;

			private WaypointRow(Waypoint waypoint) {
				this.waypoint = waypoint;
				this.setTooltip(String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z)
						+ "\n§7" + waypoint.dimension);
			}

			@Override
			public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
				float alpha = alpha();
				if (alpha <= 0.01F) {
					return;
				}
				UiTheme theme = UiTheme.get();
				float offset = appearOffset();
				Render.roundedRect(graphics, x, y + offset, width, height, 7.0F,
						Render.mix(Render.alpha(0xFFFFFF, 0.025F * alpha), Render.alpha(0xFFFFFF, 0.07F * alpha), hover.get()));
				Ui.dot(graphics, x + 10.0F, y + offset + height * 0.5F, 3.5F,
						Render.alpha(waypoint.color, alpha));
				Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), waypoint.name, 130.0F),
						x + 20.0F, y + offset + 5.0F, Render.alpha(theme.text, alpha), false);
				String dimension = waypoint.dimension.contains(":")
						? waypoint.dimension.substring(waypoint.dimension.indexOf(':') + 1) : waypoint.dimension;
				String coords = String.format(Locale.ROOT, "%s  ·  %.0f %.0f %.0f", dimension,
						waypoint.x, waypoint.y, waypoint.z);
				Render.text(graphics, UiWidgets.font(), coords, x + 160.0F, y + offset + 5.0F,
						Render.alpha(theme.textDim, alpha), false);
				if (waypoint.temporary) {
					Render.text(graphics, UiWidgets.font(), "temp", right() - 30.0F, y + offset + 5.0F,
							Render.alpha(theme.warning, alpha), false);
				}
			}

			@Override
			public boolean mouseClicked(float mouseX, float mouseY, int button) {
				if (!isHovered(mouseX, mouseY)) {
					return false;
				}
				if (button == 0) {
					Clipboard.copyText(String.format(Locale.ROOT, "%.1f %.1f %.1f", waypoint.x, waypoint.y, waypoint.z));
					toast("Coordinates copied");
					return true;
				}
				if (button == 1) {
					ChaosConfig.WAYPOINTS.remove(waypoint);
					ChaosConfig.markDirty();
					rebuildList();
					return true;
				}
				return false;
			}
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
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.HudPos;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Drag &amp; drop editor for every movable overlay.
 *
 * <p>The window lists the overlays with their switches while the ghost boxes themselves live in
 * screen space, exactly where the overlays will appear in game. Positions are stored as a fraction
 * of the free space, so the drag delta is mapped straight onto that fraction and the layout survives
 * every GUI scale and window size.
 */
public final class HudEditorScreen extends ChaosScreen {
	private static final float GHOST_WIDTH = 132.0F;
	private static final float GHOST_HEIGHT = 26.0F;

	private record Entry(Module module, Setting.Position position) {
	}

	private final List<Entry> entries = new ArrayList<>();
	private Entry dragging;
	private float grabOffsetX;
	private float grabOffsetY;
	private float snapFlash;
	private boolean showGrid = true;

	public HudEditorScreen(Screen parent) {
		super(parent, Component.literal("HUD Editor"), "window.hudeditor", 520.0F, 360.0F);
	}

	@Override
	protected void buildLayout() {
		UiTheme theme = UiTheme.get();
		entries.clear();
		for (Module module : ModuleManager.modules()) {
			for (Setting<?> setting : module.settings()) {
				if (setting instanceof Setting.Position position) {
					entries.add(new Entry(module, position));
				}
			}
		}

		float contentX = window().x() + 20.0F;
		float contentWidth = window().width() - 40.0F;
		float top = window().y() + window().titleHeight() + 16.0F;

		UiWidgets.Label hint = new UiWidgets.Label("Drag the ghost boxes to place your overlays.", theme.textFaint);
		hint.setBounds(contentX, top, contentWidth, 12.0F);
		add(hint);

		UiWidgets.ScrollList list = new UiWidgets.ScrollList();
		list.setSpacing(6.0F);
		list.setBounds(contentX, top + 18.0F, contentWidth, Math.max(80.0F, window().height() - window().titleHeight() - 74.0F));
		list.snapAppear();
		add(list);

		int index = 0;
		for (Entry entry : entries) {
			list.addItem(new OverlayRow(entry, index));
			index++;
		}
		list.layout();

		UiWidgets.Button grid = new UiWidgets.Button(showGrid ? "Grid: on" : "Grid: off",
				UiWidgets.Button.Variant.GHOST, theme.accent, () -> {
					showGrid = !showGrid;
					refresh();
				});
		grid.setBounds(contentX, window().bottom() - 30.0F, 90.0F, 20.0F);
		grid.setTooltip("Toggle the alignment grid.");
		add(grid);

		UiWidgets.Button reset = new UiWidgets.Button("Reset all", UiWidgets.Button.Variant.GHOST, theme.warning, () -> {
			for (Entry entry : entries) {
				entry.position.reset();
			}
			toast("All overlays reset");
			snapFlash = 1.0F;
		});
		reset.setBounds(contentX + 98.0F, window().bottom() - 30.0F, 88.0F, 20.0F);
		reset.setTooltip("Move every overlay back to its default corner.");
		add(reset);

		UiWidgets.Button done = new UiWidgets.Button("Done", UiWidgets.Button.Variant.PRIMARY, theme.accent, this::requestClose);
		done.setBounds(window().right() - 110.0F, window().bottom() - 30.0F, 90.0F, 20.0F);
		done.setTooltip("Save and go back.");
		add(done);
	}

	@Override
	protected void renderHeader(GuiGraphics graphics, float alpha) {
		UiTheme theme = theme();
		Render.boldText(graphics, UiWidgets.font(), "HUD Editor", window().x() + 16.0F, window().y() + 13.0F,
				Render.alpha(theme.text, alpha), false);
		String subtitle = entries.size() + " movable overlays";
		Render.text(graphics, UiWidgets.font(), subtitle, window().right() - 16.0F - UiWidgets.font().width(subtitle), window().y() + 13.0F,
				Render.alpha(theme.textFaint, alpha), false);
	}

	// ------------------------------------------------------------ screen space

	@Override
	protected void renderScreenSpace(GuiGraphics graphics, float mouseX, float mouseY) {
		UiTheme theme = theme();
		float alpha = alpha();
		if (alpha <= 0.02F) {
			return;
		}
		if (showGrid) {
			for (float x = 0.0F; x <= this.width; x += 40.0F) {
				Render.rect(graphics, x, 0.0F, 1.0F, this.height, Render.alpha(0xFFFFFF, 0.022F * alpha));
			}
			for (float y = 0.0F; y <= this.height; y += 40.0F) {
				Render.rect(graphics, 0.0F, y, this.width, 1.0F, Render.alpha(0xFFFFFF, 0.022F * alpha));
			}
		}
		if (snapFlash > 0.01F) {
			Render.rect(graphics, 0.0F, 0.0F, this.width, this.height, Render.alpha(theme.accent, 0.06F * snapFlash * alpha));
			snapFlash = Math.max(0.0F, snapFlash - deltaSeconds * 2.4F);
		}
		for (Entry entry : entries) {
			boolean enabled = entry.module.isEnabled();
			float x = entry.position.get().screenX(this.width, Math.round(GHOST_WIDTH));
			float y = entry.position.get().screenY(this.height, Math.round(GHOST_HEIGHT));
			boolean hovered = mouseX >= x && mouseX <= x + GHOST_WIDTH && mouseY >= y && mouseY <= y + GHOST_HEIGHT;
			boolean active = dragging == entry;
			int accent = entry.module.category().color();
			float presence = enabled ? 1.0F : 0.45F;
			float emphasis = active ? 1.0F : (hovered ? 0.85F : 0.55F);

			Render.softShadow(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F, 0.7F * presence * alpha);
			Render.roundedRect(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F,
					Render.mix(Render.alpha(0x12131C, 0.85F * presence * alpha),
							Render.alpha(accent, 0.28F * presence * alpha), emphasis * 0.6F));
			Render.ring(graphics, x, y, GHOST_WIDTH, GHOST_HEIGHT, 8.0F, active ? 1.6F : 1.0F,
					Render.alpha(accent, presence * alpha * (active ? 1.0F : 0.6F)));
			Ui.dot(graphics, x + 10.0F, y + GHOST_HEIGHT * 0.5F, 3.0F,
					Render.alpha(enabled ? accent : theme.textFaint, presence * alpha));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), entry.module.name(), GHOST_WIDTH - 46.0F),
					x + 18.0F, y + 5.0F, Render.alpha(theme.text, presence * alpha), false);
			Render.text(graphics, UiWidgets.font(), entry.position.display(), x + 18.0F, y + 15.0F,
					Render.alpha(theme.textFaint, presence * alpha), false);
		}
	}

	@Override
	protected boolean mouseClickedScreenSpace(float mouseX, float mouseY, int button) {
		if (button != 0) {
			return false;
		}
		for (Entry entry : entries) {
			float x = entry.position.get().screenX(this.width, Math.round(GHOST_WIDTH));
			float y = entry.position.get().screenY(this.height, Math.round(GHOST_HEIGHT));
			if (mouseX >= x && mouseX <= x + GHOST_WIDTH && mouseY >= y && mouseY <= y + GHOST_HEIGHT) {
				dragging = entry;
				grabOffsetX = mouseX - x;
				grabOffsetY = mouseY - y;
				playClick(true);
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean mouseDraggedScreenSpace(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
		if (dragging == null || button != 0) {
			return false;
		}
		float targetX = mouseX - grabOffsetX;
		float targetY = mouseY - grabOffsetY;
		// Snap to a 20 pixel grid while shift is not held, which is what makes a tidy HUD.
		if (!isShiftDown()) {
			targetX = Math.round(targetX / 20.0F) * 20.0F;
			targetY = Math.round(targetY / 20.0F) * 20.0F;
		}
		int maxX = Math.max(1, this.width - Math.round(GHOST_WIDTH));
		int maxY = Math.max(1, this.height - Math.round(GHOST_HEIGHT));
		dragging.position.set(new HudPos(
				Anim.clamp(targetX / maxX, 0.0F, 1.0F),
				Anim.clamp(targetY / maxY, 0.0F, 1.0F)));
		return true;
	}

	@Override
	protected boolean mouseReleasedScreenSpace(float mouseX, float mouseY, int button) {
		if (dragging != null && button == 0) {
			dragging = null;
			snapFlash = 1.0F;
			return true;
		}
		return false;
	}

	private static boolean isShiftDown() {
		try {
			return org.lwjgl.glfw.GLFW.glfwGetKey(net.minecraft.client.Minecraft.getInstance().getWindow().handle(),
					org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
					|| org.lwjgl.glfw.GLFW.glfwGetKey(
							net.minecraft.client.Minecraft.getInstance().getWindow().handle(),
							org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
		} catch (Throwable ignored) {
			return false;
		}
	}

	/** One overlay row: name, live switch and a "drag me" hint. */
	private final class OverlayRow extends UiComponent {
		private final Entry entry;
		private final UiWidgets.Toggle toggle;
		private final int index;

		private OverlayRow(Entry entry, int index) {
			this.entry = entry;
			this.index = index;
			this.toggle = new UiWidgets.Toggle(() -> entry.module.isEnabled(), value -> {
				if (entry.module.isEnabled() != value) {
					entry.module.enabled().toggle();
					playClick(value);
				}
			}, UiTheme.get().accent);
			this.setTooltip(entry.module.description() + "\n§7drag the ghost box on screen to move it");
			this.setAppearDelay(Math.min(0.2F, 0.02F * index));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			toggle.setBounds(right() - 40.0F, y, 30.0F, height);
			toggle.setLayerAlpha(layerAlpha);
			toggle.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			boolean enabled = entry.module.isEnabled();
			int accent = entry.module.category().color();
			Render.roundedRect(graphics, x, y + offset, width, height, 8.0F,
					Render.mix(Render.alpha(0xFFFFFF, 0.02F * alpha), Render.alpha(0xFFFFFF, 0.06F * alpha), hover.get()));
			Ui.dot(graphics, x + 10.0F, y + offset + height * 0.5F, 3.0F,
					Render.alpha(enabled ? accent : theme.textFaint, alpha));
			Render.text(graphics, UiWidgets.font(), Render.ellipsize(UiWidgets.font(), entry.module.name(), width - 60.0F),
					x + 18.0F, y + offset + (height - 8.0F) * 0.5F,
					Render.alpha(enabled ? theme.text : theme.textFaint, alpha), false);
			toggle.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (x + width - 44.0F <= mouseX) {
				return toggle.mouseClicked(mouseX, mouseY, button);
			}
			// clicking the row highlights the matching ghost box
			snapFlash = 1.0F;
			return true;
		}
	}
}
```

### `src/main/java/dev/chaosutils/gui/Ui.java`

```java
package dev.chaosutils.gui;

import dev.chaosutils.util.Render;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * The visual vocabulary of the ChaosUtils interface.
 *
 * <p>Everything the interface draws goes through one of these helpers, which is what keeps the
 * look consistent: one place for the window chrome, one for cards, one for hairlines. Shapes are
 * composed from {@code GuiGraphics#fill} scanlines and two small tinted textures, so nothing here
 * depends on shader or pipeline internals.
 */
public final class Ui {
	private Ui() {
	}

	// ------------------------------------------------------------------ window

	/** Floating window: drop shadow, gradient body, top sheen and a hairline outline. */
	public static void window(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float appear) {
		if (appear <= 0.01F) {
			return;
		}
		if (theme.glow) {
			Render.glow(graphics, x + width * 0.5F, y + 10.0F, Math.min(width, height) * 0.58F, theme.accent,
					0.16F * appear);
		}
		if (theme.windowShadow) {
			Render.softShadow(graphics, x, y, width, height, radius, theme.shadowStrength * 1.1F * appear);
		}
		Render.roundedRectGradient(graphics, x, y, width, height, radius,
				Render.alpha(theme.windowTop, appear), Render.alpha(theme.windowBottom, appear));
		Render.roundedRectGradient(graphics, x + 1.0F, y + 1.0F, width - 2.0F, Math.min(30.0F, height * 0.25F),
				Math.max(0.0F, radius - 1.0F), Render.alpha(0xFFFFFFFF, 0.05F * appear),
				Render.alpha(0xFFFFFFFF, 0.0F));
		Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.outlineStrong, appear));
	}

	/** Sidebar surface: only the corners on {@code left} are rounded (scissor-based). */
	public static void sideSurface(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			boolean left, int top, int bottom) {
		float over = radius + 2.0F;
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, left ? x : x - over, y, width + over, height, radius, top, bottom);
		Render.unscissor(graphics);
	}

	/** Header strip at the top of a window: slightly lighter, fades into the body. */
	public static void header(GuiGraphics graphics, float x, float y, float width, float height, float radius, UiTheme theme, float appear) {
		Render.scissor(graphics, x, y, width, height);
		Render.roundedRectGradient(graphics, x, y, width, height + radius, radius,
				Render.alpha(0xFFFFFFFF, 0.028F * appear), Render.alpha(0xFFFFFFFF, 0.0F));
		Render.unscissor(graphics);
		Render.rect(graphics, x + radius, y + height - 1.0F, width - radius * 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, appear));
	}

	// ------------------------------------------------------------------- cards

	/** Card surface with hover lift; {@code active} draws the accent tint and outline. */
	public static void card(GuiGraphics graphics, float x, float y, float width, float height, float radius,
			UiTheme theme, float hover, boolean active, float alpha) {
		int base = active ? theme.cardActive : Render.mix(theme.card, theme.cardHover, hover);
		if (theme.glow && (hover > 0.02F || active)) {
			Render.halo(graphics, x, y, width, height, radius, theme.accent, (active ? 0.55F : 0.35F) * hover + (active ? 0.2F : 0.0F));
		}
		Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(0x00000000, base, alpha));
		Render.ring(graphics, x, y, width, height, radius, 1.0F,
				Render.mix(Render.alpha(theme.outlineSoft, alpha),
						Render.alpha(active ? theme.accentSoft : theme.outlineStrong, alpha), Math.max(hover, active ? 1.0F : 0.0F)));
	}

	/** Rounded square behind an item icon, tinted with the owner's accent colour. */
	public static void iconTile(GuiGraphics graphics, float x, float y, float size, float radius, int color, float alpha) {
		Render.roundedRect(graphics, x, y, size, size, radius, Render.alpha(color, 0.16F * alpha));
		Render.ring(graphics, x, y, size, size, radius, 1.0F, Render.alpha(color, 0.34F * alpha));
	}

	public static void itemIcon(GuiGraphics graphics, ItemStack stack, float centerX, float centerY, float scale, float alpha) {
		if (stack == null || stack.isEmpty() || alpha <= 0.02F) {
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(centerX, centerY);
		graphics.pose().scale(scale, scale);
		graphics.pose().translate(-centerX, -centerY);
		Render.item(graphics, stack, centerX - 8.0F, centerY - 8.0F);
		graphics.pose().popMatrix();
	}

	// ----------------------------------------------------------------- strokes

	public static void divider(GuiGraphics graphics, float x, float y, float width, UiTheme theme, float alpha) {
		Render.roundedRect(graphics, x, y, width, 1.0F, 0.5F, Render.alpha(theme.outlineSoft, alpha));
	}

	/** Very small uppercase section caption, the way modern interfaces label groups. */
	public static void sectionLabel(GuiGraphics graphics, Font font, String text, float x, float y, int color, float alpha) {
		Render.text(graphics, font, text.toUpperCase(java.util.Locale.ROOT), x, y, Render.alpha(color, alpha), false);
	}

	/** Chevron used for expanders and dropdowns; {@code rotation} is in degrees (0 = pointing right). */
	public static void chevron(GuiGraphics graphics, float centerX, float centerY, float size, float rotation, int color) {
		float half = size * 0.5F;
		float thickness = Math.max(1.4F, size * 0.24F);
		double angle = Math.toRadians(rotation);
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		// Two arms of a ">" rotated around the centre.
		float ax = -half * cos;
		float ay = -half * sin;
		float bx = half * cos;
		float by = half * sin;
		float cx = bx - size * sin * 0.55F;
		float cy = by + size * cos * 0.55F;
		Render.line(graphics, centerX + ax, centerY + ay, centerX + bx, centerY + by, thickness, color);
		Render.line(graphics, centerX + bx, centerY + by, centerX + cx, centerY + cy, thickness, color);
	}

	/** Check mark used by toggles and lists. */
	public static void check(GuiGraphics graphics, float centerX, float centerY, float size, int color) {
		float thickness = Math.max(1.5F, size * 0.22F);
		Render.line(graphics, centerX - size * 0.42F, centerY + size * 0.02F, centerX - size * 0.10F, centerY + size * 0.34F,
				thickness, color);
		Render.line(graphics, centerX - size * 0.10F, centerY + size * 0.34F, centerX + size * 0.44F, centerY - size * 0.34F,
				thickness, color);
	}

	public static void dot(GuiGraphics graphics, float centerX, float centerY, float radius, int color) {
		Render.circle(graphics, centerX, centerY, radius, color);
	}

	/** Thin horizontal progress track; {@code fraction} fills it from the left. */
	public static void track(GuiGraphics graphics, float x, float y, float width, float height, float fraction,
			int fill, int background) {
		Render.roundedRect(graphics, x, y, width, height, height * 0.5F, background);
		float filled = Math.max(0.0F, Math.min(1.0F, fraction)) * width;
		if (filled > 0.5F) {
			Render.roundedRect(graphics, x, y, Math.max(height, filled), height, height * 0.5F, fill);
		}
	}

	/**
	 * Glossy knob used by toggles and sliders: a filled circle with a soft inner highlight so it
	 * reads as a physical object instead of a flat dot.
	 */
	public static void knob(GuiGraphics graphics, float centerX, float centerY, float radius, int color, float alpha) {
		Render.circle(graphics, centerX, centerY, radius, Render.alpha(color, alpha));
		Render.circle(graphics, centerX - radius * 0.18F, centerY - radius * 0.22F, radius * 0.62F,
				Render.alpha(0xFFFFFFFF, 0.22F * alpha));
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
 * Lightweight GUI element of the ChaosUtils interface.
 *
 * <p>ChaosUtils deliberately does not use the vanilla widget hierarchy: the input signatures of
 * {@code AbstractWidget} move around between renderer rewrites, and owning the model is what lets
 * every element animate with shared timing (hover lift, press depth, entrance stagger) without
 * each widget carrying its own tweens.
 */
public abstract class UiComponent {
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected boolean visible = true;
	protected boolean enabled = true;
	protected String tooltip;

	/** Layer alpha handed down by the owning screen (window fade in/out). */
	protected float layerAlpha = 1.0F;
	/** Entrance animation: 0 hidden, 1 fully placed. */
	protected final Anim.Value appear = new Anim.Value(0.0F, 9.0F);
	protected final Anim.Value hover = new Anim.Value(0.0F, 16.0F);
	protected final Anim.Value active = new Anim.Value(0.0F, 12.0F);
	protected final Anim.Value focus = new Anim.Value(0.0F, 11.0F);

	private float appearDelay;
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

	/** Seconds to wait before this element animates in; used for staggered lists. */
	public UiComponent setAppearDelay(float seconds) {
		this.appearDelay = Math.max(0.0F, seconds);
		return this;
	}

	public UiComponent snapAppear() {
		this.appearDelay = 0.0F;
		this.appear.snap(1.0F);
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

	public float centerX() {
		return x + width * 0.5F;
	}

	public float centerY() {
		return y + height * 0.5F;
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

	public void setLayerAlpha(float alpha) {
		this.layerAlpha = Anim.clamp01(alpha);
		for (UiComponent child : children) {
			child.setLayerAlpha(alpha);
		}
	}

	/** Combined alpha of this element: layer alpha times entrance animation. */
	protected float alpha() {
		return layerAlpha * Anim.clamp01(appear.get());
	}

	/** Pixel offset applied while the element is still animating in. */
	protected float appearOffset() {
		return (1.0F - Anim.easeOutQuint(appear.get())) * 6.0F;
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
		if (appearDelay > 0.0F) {
			appearDelay = Math.max(0.0F, appearDelay - deltaSeconds);
		}
		appear.set(appearDelay > 0.0F ? 0.0F : 1.0F);
		appear.update(deltaSeconds, UiTheme.get().speed(9.0F));
		float hovering = enabled && visible && contains(mouseX, mouseY) ? 1.0F : 0.0F;
		hover.set(hovering);
		hover.update(deltaSeconds, UiTheme.get().speed(16.0F));
		active.update(deltaSeconds, UiTheme.get().speed(12.0F));
		focus.update(deltaSeconds, UiTheme.get().speed(11.0F));
		for (UiComponent child : children) {
			child.setLayerAlpha(layerAlpha);
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

	/** Rebuilds children after a data change while keeping the current scroll position. */
	public void refresh() {
	}

	/** Called when a component is taken out of the tree so it can drop transient state. */
	public void reset() {
		hover.snap(0.0F);
		active.snap(0.0F);
		focus.snap(0.0F);
		appear.snap(0.0F);
		for (UiComponent child : children) {
			child.reset();
		}
	}

	protected boolean isHovered(float mouseX, float mouseY) {
		return enabled && visible && contains(mouseX, mouseY);
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiModals.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * Modal layer of the interface: dimmed backdrop, floating dialog, entrance animation.
 *
 * <p>Modals are owned by the screen that opened them, animate themselves in and out and are
 * always the first thing that receives input, so a dialog can never end up behind the window it
 * belongs to. All of them are stateless outside their own lifetime - closing one releases its
 * widgets immediately.
 */
public final class UiModals {
	private UiModals() {
	}

	// =================================================================== base

	public abstract static class Modal extends UiComponent {
		private final Anim.Value presence = new Anim.Value(0.0F, 11.0F);
		private final Anim.Value exit = new Anim.Value(0.0F, 14.0F);
		private boolean closing;
		private boolean done;
		private Runnable onClosed;
		/** Screen size handed in by the owning screen (never read from the window directly). */
		protected float viewWidth = 320.0F;
		protected float viewHeight = 240.0F;

		protected Modal() {
			this.visible = true;
		}

		public Modal onClosed(Runnable onClosed) {
			this.onClosed = onClosed;
			return this;
		}

		/** Called by the owning screen every frame so modals never guess the screen size. */
		public void setScreenBounds(float width, float height) {
			this.viewWidth = width;
			this.viewHeight = height;
		}

		/** Starts the close animation; {@link #isFinished()} flips once it has played out. */
		public void close() {
			if (!closing) {
				closing = true;
				exit.set(1.0F);
			}
		}

		public boolean isClosing() {
			return closing;
		}

		public boolean isFinished() {
			return done;
		}

		protected float presence() {
			return Anim.clamp01(presence.get()) * (1.0F - Anim.easeOutQuint(exit.get()));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			presence.set(1.0F);
			presence.update(deltaSeconds, UiTheme.get().speed(11.0F));
			if (closing) {
				exit.update(deltaSeconds, UiTheme.get().speed(14.0F));
				if (exit.get() > 0.985F && !done) {
					done = true;
					if (onClosed != null) {
						onClosed.run();
					}
				}
			}
			setLayerAlpha(presence());
			super.update(deltaSeconds, mouseX, mouseY);
		}

		/** Dim layer + scaled dialog frame shared by all modals. */
		protected void renderChrome(GuiGraphics graphics, float radius) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			Render.rect(graphics, 0.0F, 0.0F, viewWidth, viewHeight, Render.alpha(0x000000, 0.55F * presence));
			UiTheme theme = UiTheme.get();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().pushMatrix();
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Ui.window(graphics, x, y, width, height, radius, theme, presence);
			graphics.pose().popMatrix();
		}

		/** Content is drawn inside the same scale transform as the chrome. */
		protected void renderContent(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().pushMatrix();
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			renderChildren(graphics, mouseX, mouseY, deltaSeconds);
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (super.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			// Clicks inside the dialog are swallowed, clicks outside dismiss it.
			if (contains(mouseX, mouseY)) {
				return true;
			}
			close();
			return true;
		}
	}

	// ================================================================= dialog

	/** Question with up to three answers. */
	/** Confirmation dialog with a destructive primary action. */
	public static Dialog confirm(String title, String message, String confirmLabel, Runnable onConfirm) {
		return Dialog.confirm(title, message, confirmLabel, onConfirm);
	}

	/** Plain information dialog with a single dismiss button. */
	public static Dialog info(String title, String message) {
		return Dialog.info(title, message);
	}

	public static final class Dialog extends Modal {
		private final String title;
		private final List<String> lines;
		private final List<UiWidgets.Button> buttons = new ArrayList<>();

		public Dialog(String title, List<String> lines) {
			this.title = title;
			this.lines = lines;
		}

		public static Dialog confirm(String title, String message, String confirmLabel, Runnable onConfirm) {
			Dialog dialog = new Dialog(title, List.of(message));
			dialog.add(confirmLabel, UiTheme.get().negative, dialog2 -> {
				if (onConfirm != null) {
					onConfirm.run();
				}
				dialog2.close();
			});
			dialog.add("Cancel", null, Modal::close);
			return dialog;
		}

		public static Dialog info(String title, String message) {
			Dialog dialog = new Dialog(title, List.of(message.split("\n")));
			dialog.add("Got it", null, Modal::close);
			return dialog;
		}

		public Dialog add(String label, Integer color, Consumer<Dialog> action) {
			UiWidgets.Button button = new UiWidgets.Button(label, color == null ? UiWidgets.Button.Variant.GHOST : UiWidgets.Button.Variant.SOFT,
					color == null ? UiTheme.get().accent : color, () -> action.accept(this));
			buttons.add(button);
			return this;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			float buttonWidth = 96.0F;
			float total = buttons.size() * buttonWidth + Math.max(0, buttons.size() - 1) * 8.0F;
			float startX = x + (width - total) * 0.5F;
			float buttonY = y + height - 34.0F;
			for (int i = 0; i < buttons.size(); i++) {
				UiWidgets.Button button = buttons.get(i);
				button.setBounds(startX + i * (buttonWidth + 8.0F), buttonY, buttonWidth, 22.0F);
				button.setLayerAlpha(presence());
				button.update(deltaSeconds, mouseX, mouseY);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Render.text(graphics, UiWidgets.font(), title, x + 18.0F, y + 16.0F, Render.alpha(theme.text, presence), false);
			float cursor = y + 36.0F;
			for (String line : lines) {
				for (String wrapped : wrap(line, width - 36.0F)) {
					Render.text(graphics, UiWidgets.font(), wrapped, x + 18.0F, cursor, Render.alpha(theme.textDim, presence), false);
					cursor += 11.0F;
				}
			}
			for (UiWidgets.Button button : buttons) {
				button.render(graphics, mouseX, mouseY, deltaSeconds);
			}
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isClosing()) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			boolean handled = false;
			for (UiWidgets.Button entry : buttons) {
				handled |= entry.mouseReleased(mouseX, mouseY, button);
			}
			return handled;
		}

		static List<String> wrap(String text, float maxWidth) {
			List<String> result = new ArrayList<>();
			if (text == null || text.isEmpty()) {
				result.add("");
				return result;
			}
			StringBuilder line = new StringBuilder();
			for (String word : text.split(" ")) {
				String candidate = line.length() == 0 ? word : line + " " + word;
				if (UiWidgets.font().width(candidate) > maxWidth && line.length() > 0) {
					result.add(line.toString());
					line = new StringBuilder(word);
				} else {
					line = new StringBuilder(candidate);
				}
			}
			result.add(line.toString());
			return result;
		}
	}

	// ============================================================ text prompt

	/** Single-line text editor with live preview and enter/escape support. */
	public static final class TextPrompt extends Modal {
		private final String title;
		private final String hint;
		private final EditBox box;
		private final Consumer<String> onAccept;
		private final float initialWidth;

		public TextPrompt(String title, String hint, String initial, int maxLength, Consumer<String> onAccept, EditBox box) {
			this.title = title;
			this.hint = hint;
			this.box = box;
			this.onAccept = onAccept;
			this.initialWidth = Math.max(240.0F, UiWidgets.font().width(initial) + 90.0F);
			this.box.setValue(initial == null ? "" : initial);
			this.box.setMaxLength(Math.max(1, maxLength));
		}

		public float preferredWidth() {
			return Math.min(420.0F, initialWidth);
		}

		public void place(float centerX, float centerY) {
			float boxWidth = preferredWidth();
			float boxHeight = 116.0F;
			setBounds(centerX - boxWidth * 0.5F, centerY - boxHeight * 0.5F, boxWidth, boxHeight);
			box.setX(Math.round(x + 16.0F));
			box.setY(Math.round(y + 44.0F));
			box.setWidth(Math.round(boxWidth - 32.0F));
		}

		public void accept() {
			if (onAccept != null) {
				onAccept.accept(box.getValue());
			}
			close();
		}

		public EditBox box() {
			return box;
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isClosing()) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());
			Render.text(graphics, UiWidgets.font(), title, x + 16.0F, y + 14.0F, Render.alpha(theme.text, presence), false);
			Render.roundedRect(graphics, x + 16.0F, y + 42.0F, width - 32.0F, 20.0F, 8.0F,
					Render.alpha(theme.track, presence));
			Render.ring(graphics, x + 16.0F, y + 42.0F, width - 32.0F, 20.0F, 8.0F, 1.0F,
					Render.alpha(theme.accent, presence * 0.6F));
			if (box.getValue().isEmpty()) {
				Render.text(graphics, UiWidgets.font(), hint, x + 22.0F, y + 48.0F,
						Render.alpha(theme.textFaint, presence), false);
			}
			Render.text(graphics, UiWidgets.font(), "Enter to apply  ·  Escape to cancel", x + 16.0F, y + 70.0F,
					Render.alpha(theme.textFaint, presence), false);
			graphics.pose().popMatrix();
		}

	}

	// ============================================================ colour picker

	/** Hue/saturation/value picker with an alpha slider, hex field and presets. */
	public static final class ColorPicker extends Modal {
		private static final int[] PRESETS = {
				0xFF7C5CFF, 0xFF4FC3F7, 0xFF57D98A, 0xFFF2B23E,
				0xFFF0686A, 0xFFF06292, 0xFFFFFFFF, 0xFF8B8FA3
		};

		private final Setting.Color setting;
		private final int originalValue;
		private int hueColor;
		private float hue;
		private float saturation;
		private float value;
		private float alphaValue;
		private boolean draggingSquare;
		private boolean draggingHue;
		private boolean draggingAlpha;
		private EditBox hexBox;

		public ColorPicker(Setting.Color setting) {
			this.setting = setting;
			this.originalValue = setting.get();
			float[] hsv = Render.toHsv(originalValue);
			this.hue = hsv[0];
			this.saturation = hsv[1];
			this.value = hsv[2];
			this.alphaValue = Render.alphaOf(originalValue) / 255.0F;
			this.hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
		}

		public void attachHexBox(EditBox box) {
			this.hexBox = box;
			box.setValue(hex());
		}

		public void place(float centerX, float centerY) {
			float boxWidth = 260.0F;
			float boxHeight = 232.0F;
			setBounds(centerX - boxWidth * 0.5F, centerY - boxHeight * 0.5F, boxWidth, boxHeight);
			if (hexBox != null) {
				hexBox.setX(Math.round(x + boxWidth - 92.0F));
				hexBox.setY(Math.round(y + boxHeight - 34.0F));
				hexBox.setWidth(78);
			}
		}

		private float squareX() {
			return x + 16.0F;
		}

		private float squareY() {
			return y + 34.0F;
		}

		private float squareWidth() {
			return width - 32.0F - 22.0F;
		}

		private float squareHeight() {
			return 110.0F;
		}

		private String hex() {
			return String.format("#%06X", currentColor() & 0xFFFFFF);
		}

		private int currentColor() {
			int rgb = Render.fromHsv(hue, saturation, value) | 0xFF000000;
			int alpha = Math.round(alphaValue * 255.0F);
			return (rgb & 0xFFFFFF) | alpha << 24;
		}

		private void applyFromSquare(float mouseX, float mouseY) {
			saturation = Anim.clamp01((mouseX - squareX()) / Math.max(1.0F, squareWidth()));
			value = 1.0F - Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			apply();
		}

		private void applyFromHue(float mouseY) {
			hue = Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
			apply();
		}

		private void applyFromAlpha(float mouseY) {
			alphaValue = 1.0F - Anim.clamp01((mouseY - squareY()) / Math.max(1.0F, squareHeight()));
			apply();
		}

		private void apply() {
			int color = currentColor();
			setting.set(setting.alphaAllowed() ? color : (color | 0xFF000000));
			if (hexBox != null) {
				hexBox.setValue(hex());
			}
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (draggingSquare) {
				applyFromSquare(mouseX, mouseY);
			} else if (draggingHue) {
				applyFromHue(mouseY);
			} else if (draggingAlpha) {
				applyFromAlpha(mouseY);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float presence = presence();
			if (presence <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			renderChrome(graphics, theme.radius);
			graphics.pose().pushMatrix();
			float scale = 0.96F + 0.04F * Anim.easeOutQuint(presence);
			graphics.pose().translate(centerX(), centerY());
			graphics.pose().scale(scale, scale);
			graphics.pose().translate(-centerX(), -centerY());

			Render.text(graphics, UiWidgets.font(), setting.label, x + 16.0F, y + 14.0F,
					Render.alpha(theme.text, presence), false);
			Render.text(graphics, UiWidgets.font(), hex(), x + width - 100.0F, y + 14.0F,
					Render.alpha(theme.textDim, presence), false);

			// Saturation/value square: hue gradient with a white-to-transparent wash on top.
			Render.roundedRectGradient(graphics, squareX(), squareY(), squareWidth(), squareHeight(), 8.0F,
					Render.alpha(hueColor, presence), Render.alpha(0xFF000000, presence));
			Render.horizontalGradient(graphics, squareX(), squareY(), squareWidth(), squareHeight(),
					Render.alpha(0xFFFFFFFF, presence), Render.alpha(0xFFFFFFFF, 0.0F));
			Render.ring(graphics, squareX(), squareY(), squareWidth(), squareHeight(), 8.0F, 1.0F,
					Render.alpha(theme.outlineStrong, presence));
			float markerX = squareX() + saturation * squareWidth();
			float markerY = squareY() + (1.0F - value) * squareHeight();
			Ui.knob(graphics, markerX, markerY, 4.0F, 0xFFFFFFFF, presence);

			// Hue strip.
			float hueX = squareX() + squareWidth() + 8.0F;
			int bands = 24;
			float bandHeight = squareHeight() / bands;
			for (int i = 0; i < bands; i++) {
				int color = Render.fromHsv(i / (float) bands, 1.0F, 1.0F);
				Render.rect(graphics, hueX, squareY() + i * bandHeight, 12.0F, bandHeight + 0.6F,
						Render.alpha(color, presence));
			}
			Render.ring(graphics, hueX, squareY(), 12.0F, squareHeight(), 6.0F, 1.0F,
					Render.alpha(theme.outlineStrong, presence));
			float hueMarkerY = squareY() + hue * squareHeight();
			Render.roundedRect(graphics, hueX - 2.0F, hueMarkerY - 1.5F, 16.0F, 3.0F, 1.5F,
					Render.alpha(0xFFFFFFFF, presence));

			// Alpha strip.
			if (setting.alphaAllowed()) {
				float alphaY = squareY() + squareHeight() + 10.0F;
				Render.roundedRect(graphics, squareX(), alphaY, squareWidth(), 12.0F, 6.0F,
						Render.alpha(0xFF20202C, presence));
				Render.horizontalGradient(graphics, squareX(), alphaY, squareWidth(), 12.0F,
						Render.alpha(currentColor() | 0xFF000000, presence * 0.25F), Render.alpha(currentColor() | 0xFF000000, presence));
				Render.ring(graphics, squareX(), alphaY, squareWidth(), 12.0F, 6.0F, 1.0F,
						Render.alpha(theme.outlineStrong, presence));
			}

			// Presets.
			float presetY = y + height - 66.0F;
			for (int i = 0; i < PRESETS.length; i++) {
				float presetX = x + 16.0F + i * 22.0F;
				boolean hovered = mouseX >= presetX - 2.0F && mouseX <= presetX + 18.0F
						&& mouseY >= presetY - 2.0F && mouseY <= presetY + 18.0F;
				Render.roundedRect(graphics, presetX, presetY, 16.0F, 16.0F, 5.0F,
						Render.alpha(PRESETS[i], presence));
				Render.ring(graphics, presetX, presetY, 16.0F, 16.0F, 5.0F, hovered ? 1.6F : 1.0F,
						Render.alpha(hovered ? 0xFFFFFFFF : theme.outlineStrong, presence));
			}
			Render.text(graphics, UiWidgets.font(), "Alpha", x + 16.0F, y + height - 30.0F,
					Render.alpha(theme.textFaint, presence), false);
			Render.text(graphics, UiWidgets.font(), "Right click a preset to reset", x + width - 130.0F, y + height - 30.0F,
					Render.alpha(theme.textFaint, presence * 0.7F), false);
			graphics.pose().popMatrix();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button != 0) {
				return true;
			}
			if (hexBox != null && hexBox.isFocused()) {
				setFocusedElsewhere();
			}
			if (mouseX >= squareX() && mouseX <= squareX() + squareWidth()
					&& mouseY >= squareY() && mouseY <= squareY() + squareHeight()) {
				draggingSquare = true;
				applyFromSquare(mouseX, mouseY);
				return true;
			}
			float hueX = squareX() + squareWidth() + 8.0F;
			if (mouseX >= hueX && mouseX <= hueX + 12.0F && mouseY >= squareY() && mouseY <= squareY() + squareHeight()) {
				draggingHue = true;
				applyFromHue(mouseY);
				return true;
			}
			if (setting.alphaAllowed()) {
				float alphaY = squareY() + squareHeight() + 10.0F;
				if (mouseX >= squareX() && mouseX <= squareX() + squareWidth() && mouseY >= alphaY && mouseY <= alphaY + 12.0F) {
					draggingAlpha = true;
					applyFromAlpha(mouseY);
					return true;
				}
			}
			float presetY = y + height - 66.0F;
			for (int i = 0; i < PRESETS.length; i++) {
				float presetX = x + 16.0F + i * 22.0F;
				if (mouseX >= presetX && mouseX <= presetX + 16.0F && mouseY >= presetY && mouseY <= presetY + 16.0F) {
					int preset = PRESETS[i] | 0xFF000000;
					float[] hsv = Render.toHsv(setting.get());
					hue = hsv[0];
					saturation = hsv[1];
					value = hsv[2];
					hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
					if (hexBox != null) {
						hexBox.setValue(hex());
					}
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		private void setFocusedElsewhere() {
			net.minecraft.client.gui.screens.Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
			if (screen != null) {
				screen.setFocused(null);
			}
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (draggingSquare) {
				applyFromSquare(mouseX, mouseY);
				return true;
			}
			if (draggingHue) {
				applyFromHue(mouseY);
				return true;
			}
			if (draggingAlpha) {
				applyFromAlpha(mouseY);
				return true;
			}
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (button == 0 && (draggingSquare || draggingHue || draggingAlpha)) {
				draggingSquare = false;
				draggingHue = false;
				draggingAlpha = false;
				return true;
			}
			return super.mouseReleased(mouseX, mouseY, button);
		}

		/** Called by the "Reset" button. */
		public void resetToOriginal() {
			setting.set(originalValue);
			float[] hsv = Render.toHsv(originalValue);
			hue = hsv[0];
			saturation = hsv[1];
			value = hsv[2];
			hueColor = Render.fromHsv(hue, 1.0F, 1.0F);
			alphaValue = Render.alphaOf(originalValue) / 255.0F;
			if (hexBox != null) {
				hexBox.setValue(hex());
			}
		}

	}

	/**
	 * Builds the colour picker with its hex field wired up.
	 *
	 * @param registerInput callback that registers the text field on the *modal* layer, so it is
	 *                      drawn above the dimming layer instead of behind it
	 */
	public static ColorPicker colorPicker(Setting.Color setting, Consumer<EditBox> registerInput) {
		ColorPicker picker = new ColorPicker(setting);
		EditBox hexBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 78, 16,
				Component.literal(setting.label));
		hexBox.setBordered(false);
		hexBox.setTextColor(0xFFF4F5FA);
		hexBox.setMaxLength(9);
		hexBox.setResponder(value -> {
			String text = value.trim();
			if (text.startsWith("#")) {
				text = text.substring(1);
			}
			if (text.length() == 6 || text.length() == 8) {
				try {
					int parsed = (int) Long.parseLong(text, 16);
					if (text.length() == 6) {
						parsed |= 0xFF000000;
					}
					setting.set(parsed);
				} catch (NumberFormatException ignored) {
					// still typing
				}
			}
		});
		picker.attachHexBox(hexBox);
		registerInput.accept(hexBox);
		return picker;
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiTheme.java`

```java
package dev.chaosutils.gui;

import dev.chaosutils.feature.qol.ThemeModule;
import dev.chaosutils.util.Render;

/**
 * Cached design tokens of the ChaosUtils interface.
 *
 * <p>Every colour the interface uses is derived here from the live theme settings, so the whole
 * look can be re-tinted from a single place and the click GUI can never end up with a colour that
 * does not exist. The instance is rebuilt whenever a theme setting changes
 * ({@link #invalidate()}), never per frame.
 */
public final class UiTheme {
	// ------------------------------------------------------------------ accent
	public final int accent;
	public final int accentBright;
	public final int accentSoft;
	public final int accentFaint;
	public final int accentGlow;
	public final int onAccent;

	// --------------------------------------------------------------- surfaces
	public final int background;
	public final int windowTop;
	public final int windowBottom;
	public final int sidebarTop;
	public final int sidebarBottom;
	public final int surface;
	public final int surfaceHover;
	public final int card;
	public final int cardHover;
	public final int cardActive;
	public final int track;
	public final int trackHover;

	// ---------------------------------------------------------------- strokes
	public final int outline;
	public final int outlineSoft;
	public final int outlineStrong;

	// ------------------------------------------------------------------- text
	public final int text;
	public final int textDim;
	public final int textFaint;
	public final int positive;
	public final int negative;
	public final int warning;

	// ---------------------------------------------------------------- metrics
	public final float radius;
	public final float radiusCard;
	public final float radiusControl;
	public final float animSpeed;
	public final float shadowStrength;
	public final float sidebarWidth;

	// -------------------------------------------------------------- behaviour
	public final boolean animations;
	public final boolean tooltips;
	public final boolean sounds;
	public final boolean blur;
	public final boolean glow;
	public final boolean windowShadow;
	public final boolean compact;
	public final boolean keybindHints;
	public final int backdropStyle;

	// ------------------------------------------------------- legacy aliases
	/** Kept so HUD code that predates the redesign keeps working unchanged. */
	public final int panel;
	public final int panelAlt;
	public final int panelHover;

	private static UiTheme cached;

	private UiTheme() {
		accent = ThemeModule.accent.get() | 0xFF000000;
		accentBright = Render.mix(accent, 0xFFFFFFFF, 0.22F);
		accentSoft = Render.alpha(accent, 0.30F);
		accentFaint = Render.alpha(accent, 0.13F);
		accentGlow = Render.alpha(accent, 0.55F);
		onAccent = luminance(accent) > 0.62F ? 0xFF0B0B12 : 0xFFFFFFFF;

		int tint = ThemeModule.backgroundColor.get();
		background = Render.alpha(tint, ThemeModule.backgroundOpacity.getFloat());

		int shell = Render.mix(0xFF0B0B12, tint, 0.35F);
		windowTop = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.035F), 0.97F);
		windowBottom = Render.alpha(Render.mix(shell, 0xFF000000, 0.18F), 0.97F);
		sidebarTop = Render.alpha(Render.mix(shell, 0x00000000, 0.35F), 0.92F);
		sidebarBottom = Render.alpha(Render.mix(shell, accent, 0.05F), 0.92F);

		// Surfaces are deliberately solid: nothing in the interface is supposed to look like the
		// world is shining through it, which is what made the panels hard to read.
		surface = 0xEC15151F;
		surfaceHover = 0xF61D1D2A;
		card = 0xF01A1A27;
		cardHover = 0xFA232336;
		cardActive = Render.alpha(Render.mix(0xFF22223A, accent, 0.22F), 1.0F);
		track = 0x992F2F45;
		trackHover = 0xBB3A3A55;

		outline = 0x2AFFFFFF;
		outlineSoft = 0x22FFFFFF;
		outlineStrong = 0x4DFFFFFF;

		text = 0xFFF4F5FA;
		textDim = 0xFFA8AABF;
		textFaint = 0xFF6E7086;
		positive = 0xFF57D98A;
		negative = 0xFFF0686A;
		warning = 0xFFF2B23E;

		radius = ThemeModule.cornerRadius.getFloat() + 6.0F;
		radiusCard = ThemeModule.cornerRadius.getFloat() + 3.0F;
		radiusControl = Math.max(3.0F, ThemeModule.cornerRadius.getFloat());

		animSpeed = ThemeModule.animationSpeed.getFloat();
		shadowStrength = ThemeModule.shadowStrength.getFloat();
		sidebarWidth = ThemeModule.sidebarWidth.getFloat();

		animations = ThemeModule.animations.get();
		tooltips = ThemeModule.tooltips.get();
		sounds = ThemeModule.guiSounds.get();
		blur = ThemeModule.blur.get();
		glow = ThemeModule.glow.get();
		windowShadow = ThemeModule.windowShadow.get();
		compact = ThemeModule.compactCards.get();
		keybindHints = ThemeModule.showKeybindHints.get();
		backdropStyle = ThemeModule.backgroundStyle.get();

		panel = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.05F), 0.92F);
		panelAlt = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.08F), 0.92F);
		panelHover = Render.alpha(Render.mix(shell, 0xFFFFFFFF, 0.13F), 0.95F);
	}

	public static UiTheme get() {
		if (cached == null) {
			// Defensive: the tokens are read from the theme module, which is registered with the
			// other features. Should anything ask for the theme earlier, register it on demand.
			dev.chaosutils.feature.qol.ThemeModule.register();
			cached = new UiTheme();
		}
		return cached;
	}

	public static void invalidate() {
		cached = null;
	}

	/**
	 * Animation speed multiplier. With animations switched off this collapses to an instant
	 * settle, so every widget keeps its single code path.
	 */
	public float speed(float base) {
		return animations ? base * animSpeed : 2000.0F;
	}

	/** Content scale used to keep the interface readable on every GUI scale. */
	public float density() {
		return compact ? 0.92F : 1.0F;
	}

	private static float luminance(int color) {
		float r = Render.red(color) / 255.0F;
		float g = Render.green(color) / 255.0F;
		float b = Render.blue(color) / 255.0F;
		return 0.2126F * r + 0.7152F * g + 0.0722F * b;
	}
}
```

### `src/main/java/dev/chaosutils/gui/UiWidgets.java`

```java
package dev.chaosutils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.chaosutils.config.Setting;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.InputUtil;
import dev.chaosutils.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * The ChaosUtils control set.
 *
 * <p>Every control is drawn from the same vocabulary ({@link Ui}, {@link Render}), animates with
 * shared timing and carries no state that survives its screen, so nothing can leak between two
 * openings of the interface. Rows are built generically from {@link Setting} descriptions
 * ({@link #forSetting}), which means a new feature setting can never be missing from the GUI.
 */
public final class UiWidgets {
	private UiWidgets() {
	}

	public static Font font() {
		return Minecraft.getInstance().font;
	}

	// =============================================================== primitives

	/** Flat button with four visual weights and an animated hover glow. */
	public static final class Button extends UiComponent {
		public enum Variant {
			/** Accent filled - one per screen. */
			PRIMARY,
			/** Transparent with a hairline, brightens on hover. */
			GHOST,
			/** Filled surface, the workhorse. */
			SOFT,
			/** Red tint for destructive actions. */
			DANGER
		}

		private String label;
		private Variant variant;
		private Runnable action;
		private int accent;
		private boolean pressed;
		private float padding = 8.0F;
		private boolean leftAligned;
		private ItemStack icon;

		public Button(String label, Variant variant, int accent, Runnable action) {
			this.label = label;
			this.variant = variant;
			this.accent = accent;
			this.action = action;
		}

		public Button label(String value) {
			this.label = value;
			return this;
		}

		public void setAction(Runnable action) {
			this.action = action;
		}

		public void setLeftAligned(boolean leftAligned) {
			this.leftAligned = leftAligned;
		}

		public void setIcon(ItemStack icon) {
			this.icon = icon;
		}

		public void setPadding(float padding) {
			this.padding = padding;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (!isHovered(mouseX, mouseY)) {
				active.set(0.0F);
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			float offset = appearOffset();
			float top = y + offset;
			float hoverAmount = hover.get();
			float press = active.get();
			UiTheme theme = UiTheme.get();
			float radius = Math.min(theme.radiusControl, height * 0.5F);

			int background;
			int textColor;
			switch (variant) {
				case PRIMARY -> {
					background = Render.mix(Render.scaleAlpha(accent, 0.92F), 0xFFFFFFFF, hoverAmount * 0.18F);
					textColor = theme.onAccent;
				}
				case DANGER -> {
					background = Render.mix(Render.alpha(theme.negative, 0.16F), Render.alpha(theme.negative, 0.30F), hoverAmount);
					textColor = theme.negative;
				}
				case GHOST -> {
					background = Render.mix(0x00000000, Render.alpha(0xFFFFFFFF, 0.09F), hoverAmount);
					textColor = Render.mix(theme.textDim, theme.text, hoverAmount);
				}
				default -> {
					background = Render.mix(Render.alpha(0xFFFFFFFF, 0.055F), Render.alpha(0xFFFFFFFF, 0.115F), hoverAmount);
					textColor = Render.mix(theme.textDim, theme.text, hoverAmount);
				}
			}
			if (theme.glow && variant == Variant.PRIMARY) {
				Render.glow(graphics, centerX(), top + height * 0.5F, width * 0.75F, accent, 0.20F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, x, top, width, height, radius, Render.mix(0x00000000, background, alpha));
			if (variant != Variant.PRIMARY) {
				Render.ring(graphics, x, top, width, height, radius, 1.0F,
						Render.mix(Render.alpha(theme.outlineSoft, alpha),
								Render.alpha(variant == Variant.DANGER ? theme.negative : accent, alpha * 0.5F), hoverAmount));
			}
			float textY = top + (height - 8.0F) * 0.5F - press * 0.5F;
			String shown = Render.ellipsize(font(), label == null ? "" : label, width - padding * 2.0F);
			if (icon != null && !icon.isEmpty()) {
				Ui.itemIcon(graphics, icon, x + padding + 6.0F, top + height * 0.5F, 0.7F, alpha);
			}
			if (leftAligned) {
				Render.text(graphics, font(), shown, x + padding + (icon != null ? 16.0F : 0.0F), textY,
						Render.alpha(textColor, alpha), false);
			} else {
				Render.centeredText(graphics, font(), shown, centerX(), textY, Render.alpha(textColor, alpha), false);
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				pressed = true;
				active.set(1.0F);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (button == 0 && pressed) {
				pressed = false;
				active.set(0.0F);
				if (isHovered(mouseX, mouseY) && action != null) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	/** Square button that paints a custom glyph. */
	public static final class IconButton extends UiComponent {
		public interface Glyph {
			void paint(GuiGraphics graphics, float centerX, float centerY, float alpha);
		}

		private final Glyph glyph;
		private final Runnable action;
		private int color;
		private boolean round;

		public IconButton(Glyph glyph, int color, Runnable action) {
			this.glyph = glyph;
			this.color = color;
			this.action = action;
		}

		public IconButton round() {
			this.round = true;
			return this;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float hoverAmount = hover.get();
			float radius = round ? height * 0.5F : Math.min(theme.radiusControl, height * 0.4F);
			int background = Render.mix(Render.alpha(0xFFFFFFFF, 0.05F), Render.alpha(0xFFFFFFFF, 0.13F), hoverAmount);
			if (hoverAmount > 0.02F && theme.glow) {
				Render.glow(graphics, centerX(), centerY(), width * 0.9F, color, 0.22F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, x, y, width, height, radius, Render.mix(0x00000000, background, alpha));
			if (glyph != null) {
				glyph.paint(graphics, centerX(), centerY(), alpha * Math.max(0.55F, hoverAmount));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				active.set(1.0F);
				if (action != null) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	/** Animated on/off switch. */
	public static final class Toggle extends UiComponent {
		private final BooleanSupplier getter;
		private final Consumer<Boolean> setter;
		private final Anim.Value on = new Anim.Value(0.0F, 16.0F);
		private int accent;
		private float switchWidth = 26.0F;
		private float switchHeight = 14.0F;
		private boolean snapped;

		public Toggle(BooleanSupplier getter, Consumer<Boolean> setter, int accent) {
			this.getter = getter;
			this.setter = setter;
			this.accent = accent;
		}

		public Toggle size(float width, float height) {
			this.switchWidth = width;
			this.switchHeight = height;
			return this;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			boolean value = getter != null && getter.getAsBoolean();
			on.set(value ? 1.0F : 0.0F);
			if (!snapped) {
				on.snap(value ? 1.0F : 0.0F);
				snapped = true;
			}
			on.update(deltaSeconds, UiTheme.get().speed(16.0F));
		}

		private float switchX() {
			return x + width - switchWidth;
		}

		private float switchY() {
			return y + (height - switchHeight) * 0.5F;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float amount = on.get();
			float sx = switchX();
			float sy = switchY() + appearOffset();
			float hoverAmount = hover.get();
			int track = Render.mix(Render.alpha(theme.track, alpha),
					Render.alpha(Render.mix(accent, 0xFFFFFFFF, hoverAmount * 0.12F), alpha), amount);
			if (amount > 0.05F && theme.glow) {
				Render.glow(graphics, sx + switchWidth * 0.5F, sy + switchHeight * 0.5F, switchWidth * 1.1F, accent,
						0.30F * amount * alpha);
			}
			Render.roundedRect(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, track);
			Render.ring(graphics, sx, sy, switchWidth, switchHeight, switchHeight * 0.5F, 1.0F,
					Render.alpha(theme.outline, alpha));
			float knobRadius = switchHeight * 0.5F - 1.6F;
			float travel = switchWidth - switchHeight;
			float knobX = sx + switchHeight * 0.5F + travel * Anim.easeOutQuint(amount);
			Ui.knob(graphics, knobX, sy + switchHeight * 0.5F, knobRadius,
					Render.mix(0xFFD7D8E4, 0xFFFFFFFF, amount), alpha);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				flip();
				return true;
			}
			if (isHovered(mouseX, mouseY) && button == 1) {
				// Right click resets the bound setting through the row below (handled there).
				return false;
			}
			return false;
		}

		/** Flips the bound value; also used by the surrounding row. */
		public void flip() {
			if (setter != null && getter != null) {
				setter.accept(!getter.getAsBoolean());
			}
		}
	}

	/** Row with a label, a value and a draggable track - used for every numeric setting. */
	public static final class Slider extends UiComponent {
		private final Setting.Number setting;
		private boolean dragging;
		private float trackHeight = 4.0F;

		public Slider(Setting.Number setting) {
			this.setting = setting;
		}

		private float trackX() {
			return x + 2.0F;
		}

		private float trackWidth() {
			return width - 4.0F;
		}

		private float trackY() {
			return y + height - 9.0F;
		}

		private float fraction() {
			return (float) Anim.clamp01((float) setting.fraction());
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			float valueAmount = fraction();
			float hoverAmount = Math.max(hover.get(), dragging ? 1.0F : 0.0F);
			int labelColor = Render.mix(theme.textDim, theme.text, hoverAmount * 0.8F);
			Render.text(graphics, font(), setting.label, x, y + offset, Render.alpha(labelColor, alpha), false);
			String value = setting.display();
			Render.text(graphics, font(), value, right() - font().width(value), y + offset,
					Render.alpha(Render.mix(theme.textFaint, theme.accent, hoverAmount), alpha), false);

			float ty = trackY() + offset;
			Render.roundedRect(graphics, trackX(), ty, trackWidth(), trackHeight, trackHeight * 0.5F,
					Render.mix(0x00000000, Render.alpha(theme.trackHover, alpha), 1.0F));
			float filled = Math.max(trackHeight, trackWidth() * valueAmount);
			int fill = Render.mix(theme.accent, theme.accentBright, hoverAmount);
			if (theme.glow && hoverAmount > 0.05F) {
				Render.glow(graphics, trackX() + filled, ty + trackHeight * 0.5F, 16.0F + hoverAmount * 6.0F, theme.accent,
						0.35F * hoverAmount * alpha);
			}
			Render.roundedRect(graphics, trackX(), ty, filled, trackHeight, trackHeight * 0.5F, Render.alpha(fill, alpha));
			// Knob grows slightly while dragging, which is the whole trick to make sliders feel alive.
			float knobRadius = 3.4F + hoverAmount * 1.4F + (dragging ? 0.8F : 0.0F);
			Ui.knob(graphics, trackX() + filled, ty + trackHeight * 0.5F, knobRadius, 0xFFFFFFFF, alpha);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0) {
				dragging = true;
				applyFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			if (dragging && button == 0) {
				applyFromMouse(mouseX);
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			if (dragging && button == 0) {
				dragging = false;
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			setting.nudge(amount > 0 ? 1 : -1);
			return true;
		}

		private void applyFromMouse(float mouseX) {
			float fraction = (mouseX - trackX()) / Math.max(1.0F, trackWidth());
			setting.setFraction(Anim.clamp01(fraction));
		}
	}

	/** Segmented control for option settings; falls back to a cycler when there are many options. */
	public static final class Choice extends UiComponent {
		private final Setting.Choice setting;
		private final Anim.Value indicatorX = new Anim.Value(0.0F, 18.0F);
		private final Anim.Value indicatorWidth = new Anim.Value(0.0F, 18.0F);
		private boolean snapped;

		public Choice(Setting.Choice setting) {
			this.setting = setting;
		}

		private boolean segmented() {
			String[] options = setting.options();
			if (options.length == 0 || options.length > 4) {
				return false;
			}
			int total = 0;
			for (String option : options) {
				total += font().width(option) + 14;
			}
			return total <= width * 0.62F;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (segmented()) {
				float[] bounds = segmentBounds();
				indicatorX.set(bounds[0]);
				indicatorWidth.set(bounds[1]);
				if (!snapped) {
					indicatorX.snap(bounds[0]);
					indicatorWidth.snap(bounds[1]);
					snapped = true;
				}
				indicatorX.update(deltaSeconds, UiTheme.get().speed(18.0F));
				indicatorWidth.update(deltaSeconds, UiTheme.get().speed(18.0F));
			}
		}

		/** @return {x, width} of the selected segment relative to the row. */
		private float[] segmentBounds() {
			String[] options = setting.options();
			int totalWidth = 0;
			for (String option : options) {
				totalWidth += font().width(option) + 14;
			}
			float startX = right() - totalWidth;
			int selected = Anim.clamp(setting.get(), 0, Math.max(0, options.length - 1));
			float offset = startX;
			for (int i = 0; i < selected; i++) {
				offset += font().width(options[i]) + 14;
			}
			return new float[] {offset, font().width(options[selected]) + 14.0F};
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String[] options = setting.options();
			int selected = Anim.clamp(setting.get(), 0, Math.max(0, options.length - 1));
			if (segmented()) {
				float totalWidth = 0;
				for (String option : options) {
					totalWidth += font().width(option) + 14;
				}
				float startX = right() - totalWidth;
				float controlY = y + offset + (height - 18.0F) * 0.5F;
				Render.roundedRect(graphics, startX, controlY, totalWidth, 18.0F, 9.0F,
						Render.alpha(theme.track, alpha * 0.7F));
				Render.roundedRect(graphics, indicatorX.get(), controlY, indicatorWidth.get(), 18.0F, 9.0F,
						Render.alpha(Render.mix(theme.accent, theme.accentBright, hover.get() * 0.3F), alpha));
				float cursor = startX;
				for (int i = 0; i < options.length; i++) {
					int textColor = i == selected ? theme.onAccent : Render.mix(theme.textDim, theme.text, hover.get() * 0.6F);
					Render.centeredText(graphics, font(), options[i], cursor + (font().width(options[i]) + 14) * 0.5F,
							controlY + 5.0F, Render.alpha(textColor, alpha), false);
					cursor += font().width(options[i]) + 14;
				}
			} else {
				String value = setting.display();
				float chipWidth = Math.min(width * 0.55F, font().width(value) + 26.0F);
				float chipX = right() - chipWidth;
				float controlY = y + offset + (height - 17.0F) * 0.5F;
				Render.roundedRect(graphics, chipX, controlY, chipWidth, 17.0F, 8.5F,
						Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.5F), hover.get()));
				Render.centeredText(graphics, font(), Render.ellipsize(font(), value, chipWidth - 16.0F),
						chipX + chipWidth * 0.5F, controlY + 4.5F,
						Render.alpha(Render.mix(theme.text, 0xFFFFFFFF, hover.get()), alpha), false);
				Ui.chevron(graphics, chipX + chipWidth - 8.0F, controlY + 8.5F, 5.0F, hover.get() > 0.5F ? 0.0F : -180.0F,
						Render.alpha(theme.textFaint, alpha));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button != 0) {
				return false;
			}
			if (segmented()) {
				String[] options = setting.options();
				int totalWidth = 0;
				for (String option : options) {
					totalWidth += font().width(option) + 14;
				}
				float cursor = right() - totalWidth;
				for (int i = 0; i < options.length; i++) {
					float segmentWidth = font().width(options[i]) + 14;
					if (mouseX >= cursor && mouseX <= cursor + segmentWidth) {
						setting.set(i);
						return true;
					}
					cursor += segmentWidth;
				}
			}
			setting.cycle(1);
			return true;
		}
	}

	/** Colour swatch that opens the picker. */
	public static final class ColorField extends UiComponent {
		private final Setting.Color setting;
		private final ChaosScreen screen;

		public ColorField(Setting.Color setting, ChaosScreen screen) {
			this.setting = setting;
			this.screen = screen;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			float swatchHeight = 15.0F;
			float swatchWidth = 30.0F;
			float swatchX = right() - swatchWidth;
			float swatchY = y + offset + (height - swatchHeight) * 0.5F;
			String hex = String.format("#%06X", setting.get() & 0xFFFFFF);
			Render.text(graphics, font(), hex, swatchX - 6.0F - font().width(hex), y + offset + (height - 8.0F) * 0.5F,
					Render.alpha(theme.textFaint, alpha), false);
			Render.roundedRect(graphics, swatchX, swatchY, swatchWidth, swatchHeight, 5.0F,
					Render.mix(0x00000000, Render.alpha(setting.get() | 0xFF000000, alpha), 1.0F));
			Render.ring(graphics, swatchX, swatchY, swatchWidth, swatchHeight, 5.0F, 1.0F,
					Render.mix(Render.alpha(theme.outlineSoft, alpha),
							Render.alpha(0xFFFFFFFF, alpha * 0.55F), hover.get()));
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0 && screen != null) {
				screen.openColorModal(setting);
				return true;
			}
			return false;
		}
	}

	/** Hotkey field: click, press a key, done. */
	public static final class KeyField extends UiComponent {
		private final Setting.Key setting;
		private boolean listening;
		private int heldBefore;

		public KeyField(Setting.Key setting) {
			this.setting = setting;
		}

		public boolean isListening() {
			return listening;
		}

		public void cancelListening() {
			listening = false;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			if (listening) {
				int pressed = InputUtil.pollNewInput(heldBefore);
				if (pressed == InputUtil.NO_KEY) {
					heldBefore = InputUtil.currentlyHeld();
				} else if (pressed == 256) {
					listening = false;
				} else {
					setting.set(pressed);
					listening = false;
				}
			}
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String value = listening ? "press a key…" : InputUtil.keyName(setting.get());
			float chipWidth = font().width(value) + 18.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 17.0F) * 0.5F;
			boolean highlight = listening || hover.get() > 0.3F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 17.0F, 8.5F,
					Render.mix(Render.alpha(theme.track, alpha),
							Render.alpha(listening ? theme.accent : 0xFFFFFFFF, alpha * (listening ? 0.45F : 0.12F)), highlight ? 1.0F : 0.0F));
			if (listening) {
				Render.ring(graphics, chipX, chipY, chipWidth, 17.0F, 8.5F, 1.0F,
						Render.alpha(theme.accent, alpha * (0.5F + 0.5F * Anim.pulse((float) (System.nanoTime() / 1.0E9), 1.4F))));
			}
			Render.centeredText(graphics, font(), value, chipX + chipWidth * 0.5F, chipY + 4.5F,
					Render.alpha(listening ? theme.accent : theme.text, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 1) {
				setting.set(InputUtil.NO_KEY);
				listening = false;
				return true;
			}
			if (button == 0) {
				listening = !listening;
				heldBefore = InputUtil.currentlyHeld();
				return true;
			}
			return false;
		}
	}

	/** Text setting: opens the shared text modal. */
	public static final class TextField extends UiComponent {
		private final Setting.Text setting;
		private final ChaosScreen screen;

		public TextField(Setting.Text setting, ChaosScreen screen) {
			this.setting = setting;
			this.screen = screen;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			float boxX = x + Math.min(width * 0.45F, font().width(setting.label) + 12.0F);
			float boxWidth = right() - boxX;
			float boxY = y + offset + (height - 17.0F) * 0.5F;
			Render.roundedRect(graphics, boxX, boxY, boxWidth, 17.0F, 8.5F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(0xFFFFFFFF, alpha * 0.10F), hover.get()));
			String value = Render.ellipsize(font(), setting.get(), boxWidth - 16.0F);
			Render.text(graphics, font(), value, boxX + 8.0F, boxY + 4.5F, Render.alpha(theme.text, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY) || screen == null) {
				return false;
			}
			if (button == 1) {
				setting.reset();
				return true;
			}
			if (button == 0) {
				screen.openTextModal(setting.label, setting.get(), setting.maxLength(), setting::set);
				return true;
			}
			return false;
		}
	}

	/** Position setting: the actual pinning happens in the HUD editor. */
	public static final class PositionField extends UiComponent {
		private final String label;
		private final Runnable openEditor;

		public PositionField(String label, Runnable openEditor) {
			this.label = label;
			this.openEditor = openEditor;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			Render.text(graphics, font(), label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String value = "open HUD editor";
			float chipWidth = font().width(value) + 16.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 16.0F) * 0.5F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 16.0F, 8.0F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.45F), hover.get()));
			Render.centeredText(graphics, font(), value, chipX + chipWidth * 0.5F, chipY + 4.0F,
					Render.alpha(Render.mix(theme.textDim, 0xFFFFFFFF, hover.get()), alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0 && openEditor != null) {
				openEditor.run();
				return true;
			}
			return false;
		}
	}

	/** Action setting (a one-shot button described by the module). */
	public static final class ActionField extends UiComponent {
		private final Setting<?> setting;

		public ActionField(Setting<?> setting) {
			this.setting = setting;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float offset = appearOffset();
			boolean hovered = hover.get() > 0.05F;
			Render.text(graphics, font(), setting.label, x, y + offset,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			String hint = "run";
			float chipWidth = font().width(hint) + 16.0F;
			float chipX = right() - chipWidth;
			float chipY = y + offset + (height - 16.0F) * 0.5F;
			Render.roundedRect(graphics, chipX, chipY, chipWidth, 16.0F, 8.0F,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.accent, alpha * 0.5F), hover.get()));
			Render.centeredText(graphics, font(), hint, chipX + chipWidth * 0.5F, chipY + 4.0F,
					Render.alpha(hovered ? 0xFFFFFFFF : theme.textDim, alpha), false);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (isHovered(mouseX, mouseY) && button == 0) {
				if (setting instanceof Setting.Action action) {
					action.run();
				}
				return true;
			}
			return false;
		}
	}

	// ============================================================ row assembly

	/**
	 * Builds the control row for a setting, or {@code null} for settings that are shown as part of
	 * another control (the module toggle lives in the card header).
	 */
	public static UiComponent forSetting(Setting<?> setting, ChaosScreen screen) {
		return switch (setting.kind()) {
			case TOGGLE -> {
				Setting.Toggle toggle = (Setting.Toggle) setting;
				Toggle widget = new Toggle(toggle::get, toggle::set, UiTheme.get().accent);
				widget.setTooltip(setting.description);
				yield new LabeledSwitch(setting.label, widget);
			}
			case NUMBER -> new Slider((Setting.Number) setting);
			case CHOICE -> new Choice((Setting.Choice) setting);
			case COLOR -> new ColorField((Setting.Color) setting, screen);
			case KEY -> new KeyField((Setting.Key) setting);
			case TEXT -> new TextField((Setting.Text) setting, screen);
			case POSITION -> new PositionField(setting.label,
					screen == null ? null : screen::openHudEditor);
			case ACTION -> new ActionField(setting);
		};
	}

	/** Label on the left, switch on the right - the row shape used for every boolean setting. */
	public static final class LabeledSwitch extends UiComponent {
		private final String label;
		private final Toggle toggle;

		public LabeledSwitch(String label, Toggle toggle) {
			this.label = label;
			this.toggle = toggle;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			toggle.setBounds(x + width - toggle.width(), y, toggle.width(), height);
			toggle.setLayerAlpha(layerAlpha);
			toggle.update(deltaSeconds, mouseX, mouseY);
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Render.text(graphics, font(), Render.ellipsize(font(), label, width - toggle.width() - 12.0F),
					x, y + appearOffset() + (height - 8.0F) * 0.5F,
					Render.alpha(Render.mix(theme.textDim, theme.text, hover.get() * 0.8F), alpha), false);
			toggle.render(graphics, mouseX, mouseY, deltaSeconds);
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (button == 1 && isHovered(mouseX, mouseY)) {
				toggle.flip();
				return true;
			}
			return toggle.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			return toggle.mouseReleased(mouseX, mouseY, button);
		}
	}

	// ================================================================ container

	/** Smoothly scrolling list with a fading scrollbar and clipped content. */
	public static final class ScrollList extends UiComponent {
		private final List<Float> baseY = new ArrayList<>();
		private float scroll;
		private float targetScroll;
		private float contentHeight;
		private float spacing = 6.0F;
		private final Anim.Value barOpacity = new Anim.Value(0.0F, 6.0F);

		public void setSpacing(float spacing) {
			this.spacing = spacing;
		}

		public void clearItems() {
			clearChildren();
			baseY.clear();
			targetScroll = 0.0F;
			contentHeight = 0.0F;
		}

		public void addItem(UiComponent component) {
			addChild(component);
			baseY.add(0.0F);
		}

		/** Lays items out vertically; called after every rebuild. */
		public void layout() {
			List<UiComponent> items = children();
			float cursor = 0.0F;
			for (int i = 0; i < items.size(); i++) {
				if (i < baseY.size()) {
					baseY.set(i, cursor);
				} else {
					baseY.add(cursor);
				}
				cursor += items.get(i).height() + spacing;
			}
			contentHeight = items.isEmpty() ? 0.0F : Math.max(0.0F, cursor - spacing);
			targetScroll = Anim.clamp(targetScroll, 0.0F, maxScroll());
			scroll = Anim.clamp(scroll, 0.0F, maxScroll());
		}

		public float contentHeight() {
			return contentHeight;
		}

		private float maxScroll() {
			return Math.max(0.0F, contentHeight - height);
		}

		public void scrollTo(float value) {
			targetScroll = Anim.clamp(value, 0.0F, maxScroll());
		}

		public float scroll() {
			return scroll;
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			appear.snap(1.0F);
			List<UiComponent> items = children();
			float before = scroll;
			scroll = Anim.approach(scroll, targetScroll, UiTheme.get().speed(13.0F), deltaSeconds);
			barOpacity.set(Math.abs(scroll - before) > 0.4F || isHovered(mouseX, mouseY) ? 1.0F : 0.0F);
			barOpacity.update(deltaSeconds, UiTheme.get().speed(4.0F));
			for (int i = 0; i < items.size(); i++) {
				UiComponent item = items.get(i);
				float base = i < baseY.size() ? baseY.get(i) : 0.0F;
				item.setBounds(x, y + base - scroll, width, item.height());
				item.setLayerAlpha(layerAlpha);
				if (item.bottom() > y - 6.0F && item.y() < bottom() + 6.0F) {
					item.update(deltaSeconds, mouseX, mouseY);
				}
			}
			// Items may have changed height while animating (expanding cards), so the offsets are
			// recomputed right away instead of one frame later.
			layout();
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F || height <= 0.0F || width <= 0.0F) {
				return;
			}
			Render.scissor(graphics, x, y, width, height);
			for (UiComponent item : children()) {
				if (item.isVisible() && item.bottom() > y - 6.0F && item.y() < bottom() + 6.0F) {
					item.render(graphics, mouseX, mouseY, deltaSeconds);
				}
			}
			Render.unscissor(graphics);
			float max = maxScroll();
			if (max > 1.0F && barOpacity.get() > 0.02F) {
				float trackHeight = height - 4.0F;
				float barHeight = Math.max(24.0F, trackHeight * (height / Math.max(height, contentHeight)));
				float barY = y + 2.0F + (trackHeight - barHeight) * (scroll / max);
				Render.roundedRect(graphics, right() - 3.0F, barY, 3.0F, barHeight, 1.5F,
						Render.alpha(UiTheme.get().accent, 0.55F * barOpacity.get() * alpha));
			}
		}

		/** Only items that are actually inside the viewport receive input. */
		private boolean itemHandles(UiComponent item) {
			return item.isVisible() && item.bottom() > y && item.y() < bottom();
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			List<UiComponent> items = children();
			for (int i = items.size() - 1; i >= 0; i--) {
				UiComponent item = items.get(i);
				if (itemHandles(item) && item.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean mouseReleased(float mouseX, float mouseY, int button) {
			boolean handled = false;
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseReleased(mouseX, mouseY, button)) {
					handled = true;
				}
			}
			return handled;
		}

		@Override
		public boolean mouseDragged(float mouseX, float mouseY, int button, float deltaX, float deltaY) {
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
					return true;
				}
			}
			if (button == 0 && isHovered(mouseX, mouseY) && deltaY != 0.0F) {
				targetScroll = Anim.clamp(targetScroll - (float) deltaY, 0.0F, maxScroll());
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(float mouseX, float mouseY, double amount) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			for (UiComponent item : children()) {
				if (itemHandles(item) && item.mouseScrolled(mouseX, mouseY, amount)) {
					return true;
				}
			}
			targetScroll = Anim.clamp(targetScroll - (float) amount * 32.0F, 0.0F, maxScroll());
			return true;
		}

		@Override
		public void reset() {
			super.reset();
			scroll = 0.0F;
			targetScroll = 0.0F;
		}
	}

	/** Rounded search field around a vanilla text box (so IME, clipboard and selection work). */
	public static final class SearchField extends UiComponent {
		private final EditBox box;
		private Runnable onClear;

		public SearchField(EditBox box) {
			this.box = box;
		}

		public EditBox box() {
			return box;
		}

		public void setOnClear(Runnable onClear) {
			this.onClear = onClear;
		}

		public void place(float x, float y, float width, float height) {
			setBounds(x, y, width, height);
			box.setX(Math.round(x + 26.0F));
			box.setY(Math.round(y + (height - 8.0F) * 0.5F));
		}

		@Override
		public void update(float deltaSeconds, float mouseX, float mouseY) {
			super.update(deltaSeconds, mouseX, mouseY);
			// Reserve room for the magnifier and the clear button.
			box.setWidth(Math.round(width - (box.getValue().isEmpty() ? 36.0F : 52.0F)));
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float focusAmount = box.isFocused() ? 1.0F : 0.0F;
			focus.set(focusAmount);
			focus.update(deltaSeconds, UiTheme.get().speed(14.0F));
			float focusValue = focus.get();
			float radius = Math.min(theme.radiusControl + 2.0F, height * 0.5F);
			Render.roundedRect(graphics, x, y, width, height, radius,
					Render.mix(Render.alpha(theme.track, alpha), Render.alpha(theme.surfaceHover, alpha), hover.get() * 0.6F));
			if (focusValue > 0.02F) {
				Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.accent, alpha * 0.7F * focusValue));
				if (theme.glow) {
					Render.glow(graphics, centerX(), centerY(), width * 0.6F, theme.accent, 0.12F * focusValue * alpha);
				}
			} else {
				Render.ring(graphics, x, y, width, height, radius, 1.0F, Render.alpha(theme.outlineSoft, alpha));
			}
			// Magnifier glyph.
			float glyphX = x + 10.0F;
			float glyphY = centerY();
			int glyphColor = Render.alpha(theme.textFaint, alpha);
			Render.ring(graphics, glyphX - 5.0F, glyphY - 6.0F, 10.0F, 10.0F, 5.0F, 1.5F, glyphColor);
			Render.line(graphics, glyphX + 3.2F, glyphY + 2.2F, glyphX + 6.2F, glyphY + 5.2F, 1.6F, glyphColor);
			if (!box.getValue().isEmpty() && hover.get() > 0.05F) {
				float clearX = right() - 14.0F;
				Render.circle(graphics, clearX, centerY(), 6.0F, Render.alpha(theme.textFaint, alpha * 0.35F * hover.get()));
				Render.line(graphics, clearX - 2.4F, centerY() - 2.4F, clearX + 2.4F, centerY() + 2.4F, 1.4F,
						Render.alpha(0xFFFFFFFF, alpha * 0.8F));
				Render.line(graphics, clearX + 2.4F, centerY() - 2.4F, clearX - 2.4F, centerY() + 2.4F, 1.4F,
						Render.alpha(0xFFFFFFFF, alpha * 0.8F));
			}
		}

		@Override
		public boolean mouseClicked(float mouseX, float mouseY, int button) {
			if (!isHovered(mouseX, mouseY)) {
				return false;
			}
			if (button == 0 && !box.getValue().isEmpty() && mouseX >= right() - 22.0F) {
				box.setValue("");
				if (onClear != null) {
					onClear.run();
				}
				return true;
			}
			return button == 0;
		}
	}

	/** Small caption used between groups of settings. */
	public static final class SectionHeader extends UiComponent {
		private final String text;

		public SectionHeader(String text) {
			this.text = text;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			Ui.sectionLabel(graphics, font(), text, x, y + appearOffset() + 2.0F, theme.textFaint, alpha);
			int labelWidth = font().width(text.toUpperCase(java.util.Locale.ROOT));
			Ui.divider(graphics, x + labelWidth + 8.0F, y + appearOffset() + 6.0F, Math.max(0.0F, width - labelWidth - 8.0F),
					theme, alpha);
		}
	}

	/** Small static text. */
	public static final class Label extends UiComponent {
		private String text;
		private int color;
		private boolean centered;
		private boolean bold;

		public Label(String text, int color) {
			this.text = text;
			this.color = color;
		}

		public Label centered() {
			this.centered = true;
			return this;
		}

		public Label bold() {
			this.bold = true;
			return this;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setColor(int color) {
			this.color = color;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			String value = Render.ellipsize(font(), text, width);
			int argb = Render.alpha(color, alpha);
			if (centered) {
				if (bold) {
					Render.boldText(graphics, font(), value, centerX() - font().width(value) * 0.5F, y + appearOffset(), argb, false);
				} else {
					Render.centeredText(graphics, font(), value, centerX(), y + appearOffset(), argb, false);
				}
			} else if (bold) {
				Render.boldText(graphics, font(), value, x, y + appearOffset(), argb, false);
			} else {
				Render.text(graphics, font(), value, x, y + appearOffset(), argb, false);
			}
		}
	}

	/** Thin progress/status element used by the footer and by list screens. */
	public static final class StatusChip extends UiComponent {
		private String text;
		private int color;
		private boolean pulsing;

		public StatusChip(String text, int color) {
			this.text = text;
			this.color = color;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public void setPulsing(boolean pulsing) {
			this.pulsing = pulsing;
		}

		@Override
		public void render(GuiGraphics graphics, float mouseX, float mouseY, float deltaSeconds) {
			float alpha = alpha();
			if (alpha <= 0.01F) {
				return;
			}
			UiTheme theme = UiTheme.get();
			float pulse = pulsing ? 0.5F + 0.5F * Anim.pulse((float) (System.nanoTime() / 1.0E9), 2.0F) : 1.0F;
			float dotRadius = 3.0F;
			Ui.dot(graphics, x + dotRadius + 1.0F, centerY(), dotRadius,
					Render.alpha(color, alpha * pulse));
			Render.text(graphics, font(), Render.ellipsize(font(), text, width - 14.0F), x + 12.0F, centerY() - 4.0F,
					Render.alpha(theme.textDim, alpha), false);
		}
	}

	/** Returns a vertical list of the settings of a module, wrapped in their rows. */
	public static List<UiComponent> rowsFor(List<Setting<?>> settings, ChaosScreen screen, float width) {
		List<UiComponent> rows = new ArrayList<>();
		for (Setting<?> setting : settings) {
			UiComponent row = forSetting(setting, screen);
			if (row == null) {
				continue;
			}
			row.setBounds(0.0F, 0.0F, width, rowHeight(setting));
			row.setTooltip(setting.description);
			rows.add(row);
		}
		return rows;
	}

	public static float rowHeight(Setting<?> setting) {
		return switch (setting.kind()) {
			case NUMBER -> 26.0F;
			case TOGGLE, CHOICE, COLOR, KEY, TEXT, POSITION, ACTION -> 22.0F;
		};
	}

}
```

### `src/main/java/dev/chaosutils/gui/UiWindow.java`

```java
package dev.chaosutils.gui;

import dev.chaosutils.config.ChaosConfig;
import dev.chaosutils.util.Anim;
import dev.chaosutils.util.Render;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The floating window every ChaosUtils screen lives in.
 *
 * <p>The shell owns the geometry (drag, resize, persistence, opening and closing animation) while
 * the screen owns the content. Content is laid out in window local coordinates starting at
 * {@code (0, 0)} - the screen translates the pose by {@link #x()}/{@link #y()} while drawing and
 * subtracts them while hit testing, so dragging never has to rebuild a single widget.
 */
public final class UiWindow {
	public static final float TITLE_HEIGHT = 34.0F;
	public static final float MIN_WIDTH = 360.0F;
	public static final float MIN_HEIGHT = 220.0F;
	private static final float RESIZE_GRIP = 16.0F;
	private static final float EDGE = 5.0F;

	private final String key;
	private float x;
	private float y;
	private float width;
	private float height;

	private boolean draggable = true;
	private boolean resizable = true;
	private boolean dragging;
	private boolean resizing;
	private float grabX;
	private float grabY;
	private float resizeFromWidth;
	private float resizeFromHeight;
	private boolean resized;
	private boolean moved;

	private final Anim.Value appear = new Anim.Value(0.0F, 9.0F);
	private final Anim.Value close = new Anim.Value(0.0F, 13.0F);
	private final Anim.Value closeHover = new Anim.Value(0.0F, 16.0F);
	private boolean closing;

	public UiWindow(String key) {
		this.key = key;
	}

	// ------------------------------------------------------------------ state

	/** Restores the remembered geometry (clamped to this screen) or centres the default size. */
	public void open(int screenWidth, int screenHeight, float preferredWidth, float preferredHeight) {
		float maxWidth = Math.max(MIN_WIDTH, screenWidth - 40.0F);
		float maxHeight = Math.max(MIN_HEIGHT, screenHeight - 40.0F);
		width = Anim.clamp(ChaosConfig.uiFloat(key + ".width", preferredWidth), MIN_WIDTH, maxWidth);
		height = Anim.clamp(ChaosConfig.uiFloat(key + ".height", preferredHeight), MIN_HEIGHT, maxHeight);
		x = ChaosConfig.uiFloat(key + ".x", Float.NaN);
		y = ChaosConfig.uiFloat(key + ".y", Float.NaN);
		if (Float.isNaN(x) || Float.isNaN(y)) {
			center(screenWidth, screenHeight);
		} else {
			x = Anim.clamp(x, 8.0F, Math.max(8.0F, screenWidth - width - 8.0F));
			y = Anim.clamp(y, 8.0F, Math.max(8.0F, screenHeight - height - 8.0F));
		}
		appear.snap(0.0F);
		close.snap(0.0F);
		closing = false;
	}

	/** Instantly shows the window; used by screens that are opened as a tool, not entered. */
	public void snapOpen() {
		appear.snap(1.0F);
		close.snap(0.0F);
		closing = false;
	}

	public void close() {
		if (!closing) {
			closing = true;
			close.set(1.0F);
		}
	}

	public boolean isClosing() {
		return closing;
	}

	public boolean isGone() {
		return closing && Anim.clamp01(close.get()) > 0.98F;
	}

	/** Advances the shell animations. The pointer drives the close button highlight. */
	public void update(float deltaSeconds, float mouseX, float mouseY) {
		appear.set(1.0F);
		appear.update(deltaSeconds, UiTheme.get().speed(9.0F));
		closeHover.set(!closing && isOverCloseButton(mouseX, mouseY) ? 1.0F : 0.0F);
		closeHover.update(deltaSeconds, UiTheme.get().speed(16.0F));
		if (closing) {
			close.update(deltaSeconds, UiTheme.get().speed(13.0F));
		}
	}

	/** Hit box of the title bar close button. */
	public boolean isOverCloseButton(float mouseX, float mouseY) {
		float size = 22.0F;
		float left = right() - size - 12.0F;
		float top = y + (TITLE_HEIGHT - size) * 0.5F;
		return mouseX >= left && mouseX <= left + size && mouseY >= top && mouseY <= top + size;
	}

	public float closeHover() {
		return closeHover.get();
	}

	/** Round close button with an animated hover state; drawn above the header. */
	public void renderCloseButton(GuiGraphics graphics, UiTheme theme, float alpha) {
		if (alpha <= 0.01F) {
			return;
		}
		float size = 22.0F;
		float left = right() - size - 12.0F;
		float top = y + (TITLE_HEIGHT - size) * 0.5F + slide();
		float hover = closeHover.get();
		Render.circle(graphics, left + size * 0.5F, top + size * 0.5F, size * 0.5F,
				Render.alpha(theme.negative, (0.10F + 0.35F * hover) * alpha));
		int color = Render.mix(theme.textDim, theme.negative, hover);
		Render.line(graphics, left + 7.0F, top + 7.0F, left + size - 7.0F, top + size - 7.0F, 1.6F,
				Render.alpha(color, alpha));
		Render.line(graphics, left + size - 7.0F, top + 7.0F, left + 7.0F, top + size - 7.0F, 1.6F,
				Render.alpha(color, alpha));
	}

	public void center(int screenWidth, int screenHeight) {
		x = Math.round((screenWidth - width) * 0.5F);
		y = Math.round((screenHeight - height) * 0.5F);
	}

	// ------------------------------------------------------------- geometry

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

	public float right() {
		return x + width;
	}

	public float bottom() {
		return y + height;
	}

	/** Left edge of the content area in window local coordinates (the body spans the full width). */
	public float bodyX() {
		return 0.0F;
	}

	/** Top of the content area, in window local coordinates. */
	public float bodyY() {
		return TITLE_HEIGHT;
	}

	/** Height of the content area. */
	public float bodyHeight() {
		return height - TITLE_HEIGHT;
	}

	/** Width of the content area. */
	public float bodyWidth() {
		return width;
	}

	public float titleHeight() {
		return TITLE_HEIGHT;
	}

	/** Alias kept for the screens: starts the closing animation. */
	public void beginClose() {
		close();
	}

	public void setSize(float width, float height) {
		this.width = Math.max(MIN_WIDTH, width);
		this.height = Math.max(MIN_HEIGHT, height);
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float titleCenterY() {
		return y + TITLE_HEIGHT * 0.5F;
	}

	public boolean contains(float mouseX, float mouseY) {
		return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
	}

	public boolean isOverTitleBar(float mouseX, float mouseY) {
		return draggable && mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= y + TITLE_HEIGHT;
	}

	public boolean isOverResizeGrip(float mouseX, float mouseY) {
		return resizable && mouseX >= right() - RESIZE_GRIP && mouseX <= right() + 2.0F
				&& mouseY >= bottom() - RESIZE_GRIP && mouseY <= bottom() + 2.0F;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/** Combined alpha: opening animation times closing animation. */
	public float alpha() {
		return Anim.clamp01(appear.get()) * (1.0F - Anim.easeOutQuint(Anim.clamp01(close.get())));
	}

	/** Slide offset of the opening animation, in pixels. */
	public float slide() {
		return (1.0F - Anim.easeOutQuint(Anim.clamp01(appear.get()))) * 14.0F;
	}

	public boolean wasResized() {
		boolean value = resized;
		resized = false;
		return value;
	}

	public boolean wasMoved() {
		boolean value = moved;
		moved = false;
		return value;
	}

	/** Forgets the stored geometry so the next open centres the window again. */
	public void resetGeometry() {
		ChaosConfig.setUi(key + ".x", null);
		ChaosConfig.setUi(key + ".y", null);
		ChaosConfig.setUi(key + ".width", null);
		ChaosConfig.setUi(key + ".height", null);
	}

	private void persist() {
		ChaosConfig.setUi(key + ".x", x);
		ChaosConfig.setUi(key + ".y", y);
		ChaosConfig.setUi(key + ".width", width);
		ChaosConfig.setUi(key + ".height", height);
	}

	// ----------------------------------------------------------------- render

	/** Draws background, ring and title bar separator. Call before the content. */
	public void renderShell(GuiGraphics graphics, UiTheme theme) {
		float alpha = alpha();
		if (alpha <= 0.01F) {
			return;
		}
		float drawY = y + slide();
		Ui.window(graphics, x, drawY, width, height, theme.radius, theme, alpha);
		Ui.header(graphics, x, drawY, width, TITLE_HEIGHT, theme.radius, theme, alpha);
		Render.rect(graphics, x + 1.0F, drawY + TITLE_HEIGHT - 1.0F, width - 2.0F, 1.0F,
				Render.alpha(theme.outlineSoft, alpha));
	}

	// ------------------------------------------------------------------ input

	public boolean mouseClicked(float mouseX, float mouseY, int button) {
		if (button != 0 || closing) {
			return false;
		}
		if (isOverResizeGrip(mouseX, mouseY)) {
			resizing = true;
			resizeFromWidth = width;
			resizeFromHeight = height;
			grabX = mouseX;
			grabY = mouseY;
			return true;
		}
		if (isOverTitleBar(mouseX, mouseY)) {
			dragging = true;
			grabX = mouseX - x;
			grabY = mouseY - y;
			return true;
		}
		return contains(mouseX, mouseY);
	}

	public boolean mouseDragged(float mouseX, float mouseY, int button) {
		if (button != 0) {
			return false;
		}
		if (resizing) {
			float newWidth = Math.max(MIN_WIDTH, resizeFromWidth + (mouseX - grabX));
			float newHeight = Math.max(MIN_HEIGHT, resizeFromHeight + (mouseY - grabY));
			if (Math.abs(newWidth - width) > 0.5F || Math.abs(newHeight - height) > 0.5F) {
				width = newWidth;
				height = newHeight;
				resized = true;
			}
			return true;
		}
		if (dragging) {
			x = mouseX - grabX;
			y = mouseY - grabY;
			moved = true;
			return true;
		}
		return false;
	}

	public boolean mouseReleased(int button) {
		if (button != 0) {
			return false;
		}
		boolean wasInteracting = dragging || resizing;
		dragging = false;
		resizing = false;
		if (wasInteracting) {
			persist();
			return true;
		}
		return false;
	}

	public boolean isInteracting() {
		return dragging || resizing;
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
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));
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
		if (Keybinds.copyCoordinates != null && Keybinds.copyCoordinates.consumeClick() && client.player != null) {
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
				? "Dim " + client.level.dimension().identifier().getPath() : null;
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
						.map(key -> prettify(key.identifier().getPath()))
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
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));
		HudPanel.panel(graphics, font, x, y, boxWidth, boxHeight, 0xFF4FC3F7);
		float cursorY = y + padding;
		for (String value : values) {
			if (value == null) {
				continue;
			}
			HudPanel.text(graphics, font, value, x + padding, cursorY, 0xFFF2F2F7);
			cursorY += lineHeight;
		}
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
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));
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
		float x = position.get().screenX(graphics.guiWidth(), Math.round(boxWidth));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(boxHeight));

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
						client.level.dimension().identifier().toString(), 0xFFE05B5B, true, lifetime);
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

		if (Keybinds.addWaypoint != null && Keybinds.addWaypoint.consumeClick()) {
			Waypoint waypoint = new Waypoint("Waypoint " + (ChaosConfig.WAYPOINTS.size() + 1),
					player.getX(), player.getY(), player.getZ(),
					client.level.dimension().identifier().toString(), 0xFF7C5CFF, false, 0L);
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
		return client.level == null ? "minecraft:overworld" : client.level.dimension().identifier().toString();
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
					Render.rect(graphics, point.x() - 1.0F, 0.0F, 2.0F, graphics.guiHeight(), beamColor);
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
		float centerX = graphics.guiWidth() * 0.5F;
		float centerY = graphics.guiHeight() * 0.5F;
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
		float centerX = graphics.guiWidth() * 0.5F;
		float centerY = graphics.guiHeight() * 0.5F;

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
			if (Keybinds.toggleGamma.consumeClick()) {
				boostLatched = !boostLatched;
			}
			return boostLatched;
		}
		boostHeld = Keybinds.toggleGamma.isDown();
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
			Render.rect(graphics, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), color);
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
import dev.chaosutils.mixin.CameraAccessor;
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
		boolean pressed = Keybinds.freeLook != null && Keybinds.freeLook.isDown();
		if (toggleMode.get()) {
			if (pressed && Keybinds.freeLook.consumeClick()) {
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
	public static void applyToCamera(Camera camera) {
		if (!active || camera == null) {
			return;
		}
		Mode selected = selectedMode();
		if (!initialised) {
			initialised = true;
			renderedYaw = camera.yRot();
			renderedPitch = camera.xRot();
		}
		float targetYaw = lockedYaw;
		// "Yaw locked, free pitch" keeps the vertical look free for a natural preview.
		float targetPitch = selected == Mode.YAW_LOCK ? camera.xRot() : lockedPitch;
		float speed = rememberRotation.get() ? transitionSpeed.getFloat() : 30.0F;
		float delta = TickClock.frameDelta();
		renderedYaw = approachWrapped(renderedYaw, targetYaw, speed, delta);
		renderedPitch = Anim.approach(renderedPitch, targetPitch, speed, delta);
		((CameraAccessor) camera).chaosutils$setRotation(renderedYaw, renderedPitch);
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
		boolean keyPressed = Keybinds.zoom != null && Keybinds.zoom.isDown();
		if (holdToZoom.get()) {
			setActive(client, keyPressed);
		} else if (keyPressed && Keybinds.zoom.consumeClick()) {
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
		float x = (graphics.guiWidth() - width) * 0.5F;
		float y = graphics.guiHeight() - 68.0F;
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
			if (field != null && Keybinds.searchContainer != null && Keybinds.searchContainer.consumeClick()) {
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

		// Renamed so the local value cannot shadow the "pulse" setting of this module.
		float pulseFactor = pulse.get()
				? 0.65F + 0.35F * (float) Math.sin(System.nanoTime() / 400_000_000.0)
				: 1.0F;
		int highlight = Render.alpha(highlightColor.get(), Anim.clamp01(pulseFactor));
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
						Render.alpha(highlightColor.get(), 0.25F * pulseFactor));
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
					String path = enchantment.unwrapKey().map(key -> key.identifier().getPath()).orElse("");
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
		int hotbarLeft = graphics.guiWidth() / 2 - 91;
		int hotbarTop = graphics.guiHeight() - 22;
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
				Minecraft client = Minecraft.getInstance();
				// Comparing the profiles themselves avoids GameProfile accessors entirely, which
				// keeps this independent of the authlib accessor names.
				if (ignoreOwnMessages.get() && sender != null && client.player != null
						&& sender.equals(client.player.getGameProfile())) {
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
			// The player's own display name, straight from the entity - no profile accessors.
			String name = client.player.getName().getString();
			if (!name.isEmpty() && contains(haystack, caseSensitive.get() ? name : name.toLowerCase(Locale.ROOT))) {
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
		float y = position.get().screenY(graphics.guiHeight(), Math.round(TOASTS.size() * (lineHeight + 4.0F * scaleFactor)));
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
			float maxWidth = graphics.guiWidth() * 0.45F;
			while (font.width(text) * scaleFactor > maxWidth && text.length() > 8) {
				text = text.substring(0, text.length() - 2) + "…";
			}
			float width = font.width(text) * scaleFactor + padding * 2.0F;
			float x = position.get().screenX(graphics.guiWidth(), Math.round(width));
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
		float centerX = position.get().screenX(graphics.guiWidth(), Math.round(radarRadius * 2.0F)) + radarRadius;
		float centerY = position.get().screenY(graphics.guiHeight(), Math.round(radarRadius * 2.0F)) + radarRadius;
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
		// Note: "range" instead of "maxDistance" - a local variable with the field's name would
		// shadow the setting inside its own initialiser.
		double range = maxDistance.get();
		Font font = Minecraft.getInstance().font;
		int index = 0;
		for (SoundTracker.Entry entry : entries) {
			float bearing = Projection.bearingTo(entry.position().x, entry.position().z);
			double distance = Projection.distanceTo(entry.position().x, entry.position().y, entry.position().z);
			float fraction = (float) Math.min(1.0, distance / Math.max(1.0, range));
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
		/** Pretty name, computed once so the HUD does not re-derive it every frame. */
		private final String name;
		private int count;
		private long lastAt;
		private float bearing;
		private double distance;
		private SoundClasses.Kind kind;

		private Row(String path) {
			this.path = path;
			this.name = SoundClasses.prettyName(path);
		}
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
		float x = position.get().screenX(graphics.guiWidth(), Math.round(width));
		float y = position.get().screenY(graphics.guiHeight(), Math.round(height));
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
		String text = symbol + " " + row.name;
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
				// Vanilla pauses everything except music and UI when the game is paused; here the
				// music is meant to stop as well, so only the UI channel keeps playing.
				client.getSoundManager().pauseAllExcept(SoundSource.UI);
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
	public static Setting.Toggle glow;
	public static Setting.Number shadowStrength;
	public static Setting.Toggle windowShadow;
	public static Setting.Toggle animations;

	private ThemeModule() {
	}

	public static void register() {
		if (MODULE != null) {
			// Registering twice would duplicate every setting; the UI can ask for this lazily.
			return;
		}
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
		glow = (Setting.Toggle) MODULE.add(new Setting.Toggle("glow", "Accent glow", "Soft coloured glow behind the window, active cards and sliders.", true));
		windowShadow = (Setting.Toggle) MODULE.add(new Setting.Toggle("window_shadow", "Drop shadow", "Draw a soft shadow under floating windows so they lift off the world.", true));
		shadowStrength = (Setting.Number) MODULE.add(new Setting.Number("shadow_strength", "Shadow strength", "Opacity of the drop shadow.", 1.0, 0.0, 2.0, 0.05, "x"));
		animations = (Setting.Toggle) MODULE.add(new Setting.Toggle("animations", "Animations", "Smooth opening, hover, expand and scroll animations.", true));

		// The theme tokens are cached for the whole frame, so every change has to invalidate them.
		for (Setting<?> setting : MODULE.settings()) {
			setting.onChanged(value -> dev.chaosutils.gui.UiTheme.invalidate());
		}
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

	private static <T extends Setting<?>> T add(T setting) {
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

### `src/main/java/dev/chaosutils/mixin/CameraAccessor.java`

```java
package dev.chaosutils.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker for the camera rotation.
 *
 * <p>{@code Camera#setRotation(float, float)} is {@code protected} in 1.21.11, and the
 * perspective lock changes the camera from outside the class hierarchy. A mixin invoker is
 * the supported way to reach it - no reflection, no duck-typed casting and no access
 * widening on the vanilla class itself.
 *
 * <p>The invoker only ever rotates the already computed camera. The player entity, its
 * hitbox and its movement stay untouched, so this stays a purely visual feature.
 */
@Mixin(Camera.class)
public interface CameraAccessor {
	@Invoker("setRotation")
	void chaosutils$setRotation(float yRot, float xRot);
}
```

### `src/main/java/dev/chaosutils/mixin/CameraMixin.java`

```java
package dev.chaosutils.mixin;

import dev.chaosutils.core.ApiCompat;
import dev.chaosutils.feature.visual.PerspectiveLock;
import net.minecraft.client.Camera;
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
 *
 * <p>The handler deliberately takes no target arguments: everything it needs can be read
 * from the camera itself. A handler without parameters can never fail the descriptor check
 * that {@code setup(...)} argument lists are subject to, so a future Minecraft update cannot
 * turn this into a startup crash.
 */
@Mixin(Camera.class)
public class CameraMixin {
	@Inject(method = "setup", at = @At("TAIL"), require = 0)
	private void chaosutils$lockRotation(CallbackInfo info) {
		ApiCompat.seen("camera.setup");
		if (!PerspectiveLock.isActive()) {
			return;
		}
		try {
			PerspectiveLock.applyToCamera((Camera) (Object) this);
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
	private void chaosutils$dropEntityState(CallbackInfo info) {
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
	private void chaosutils$zoomFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> info) {
		ApiCompat.seen("renderer.fov");
		try {
			double factor = SmoothZoom.fovFactor(partialTick);
			if (factor < 0.9999 && info.getReturnValue() != null) {
				info.setReturnValue((float) (info.getReturnValue() * factor));
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla crosshair while the ChaosUtils designer is drawing its own.
 *
 * <p>Only the crosshair is cancelled, and only when the feature is enabled - everything
 * else in the HUD keeps running untouched.
 *
 * <p>The handler takes no target arguments on purpose. Vanilla passed the crosshair a
 * {@code GuiGraphics} and a {@code DeltaTracker} in 1.21.11; a handler that lists arguments
 * has to match them exactly or Mixin aborts the whole launch (that is what produced the
 * first in-game crash). With an empty argument list there is nothing left to mismatch -
 * Mixin accepts it for any signature, and {@code info.cancel()} still skips the vanilla
 * crosshair.
 */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
	private void chaosutils$replaceCrosshair(CallbackInfo info) {
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

### `src/main/java/dev/chaosutils/mixin/KeyMappingAccessor.java`

```java
package dev.chaosutils.mixin;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads the physical key of a {@link KeyMapping}.
 *
 * <p>Needed because the game releases every key mapping when a screen opens
 * ({@code Minecraft#setScreen} calls {@code KeyMapping.releaseAll()}), while the keyboard handler
 * only feeds new key states to the mappings when <em>no</em> screen is open. Inside a screen the
 * mapping state therefore says "not pressed" even while the player is still holding the key - which
 * is exactly what a hold-to-open radial menu has to know. Polling GLFW for the mapping's key is the
 * only reliable answer, and the key itself is {@code protected} in 1.21.11, so it is read through
 * this accessor instead of reflection.
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
	@Accessor("key")
	InputConstants.Key chaosutils$key();
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
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
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
	private void chaosutils$filterParticle(ParticleOptions options, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> info) {
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
	private void chaosutils$captureSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> info) {
		ApiCompat.seen("sound.play");
		if (!Features.anySoundConsumerEnabled()) {
			return;
		}
		try {
			SoundTracker.push(
					sound.getIdentifier().toString(),
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
