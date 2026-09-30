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
