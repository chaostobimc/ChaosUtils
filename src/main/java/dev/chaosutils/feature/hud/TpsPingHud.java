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
