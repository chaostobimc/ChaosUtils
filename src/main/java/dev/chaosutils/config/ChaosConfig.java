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

	/** Location of the configuration file on disk. */
	public static Path path() {
		return FILE;
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
