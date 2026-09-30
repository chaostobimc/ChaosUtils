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
