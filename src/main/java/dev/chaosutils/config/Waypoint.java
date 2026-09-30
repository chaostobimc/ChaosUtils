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
