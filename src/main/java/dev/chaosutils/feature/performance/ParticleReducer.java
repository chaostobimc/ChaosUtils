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
