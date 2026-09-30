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
