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
