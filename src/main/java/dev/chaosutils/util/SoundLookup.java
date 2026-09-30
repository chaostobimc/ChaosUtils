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
