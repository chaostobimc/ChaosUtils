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
