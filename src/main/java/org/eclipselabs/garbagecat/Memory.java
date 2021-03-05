package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.Memory.Unit.GIGABYTES;
import static org.eclipselabs.garbagecat.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.Memory.Unit.MEGABYTES;

public class Memory implements Comparable<Memory> {

	public static enum Unit {

		BYTES("B") {
			@Override
			public double toBytes(double v) {
				return v;
			}

			@Override
			public double toKiloBytes(double v) {
				return v / K;
			}

			@Override
			public double toMegaBytes(double v) {
				return v / K / K;
			}

			@Override
			public double toGigaBytes(double v) {
				return v / K / K / K;
			}

			@Override
			public double convertTo(double v, Unit s) {
				return s.toBytes(v);
			}
		},
		KILOBYTES("K") {
			@Override
			public double toBytes(double v) {
				return v * K;
			}

			@Override
			public double toKiloBytes(double v) {
				return v;
			}

			@Override
			public double toMegaBytes(double v) {
				return v / K;
			}

			@Override
			public double toGigaBytes(double v) {
				return v / K / K;
			}

			@Override
			public double convertTo(double v, Unit s) {
				return s.toKiloBytes(v);
			}
		},
		MEGABYTES("M") {
			@Override
			public double toBytes(double v) {
				return v * K * K;
			}

			@Override
			public double toKiloBytes(double v) {
				return v * K;
			}

			@Override
			public double toMegaBytes(double v) {
				return v;
			}

			@Override
			public double toGigaBytes(double v) {
				return v / K;
			}

			@Override
			public double convertTo(double v, Unit s) {
				return s.toMegaBytes(v);
			}
		},
		GIGABYTES("G") {
			@Override
			public double toBytes(double v) {
				return v * K * K * K;
			}

			@Override
			public double toKiloBytes(double v) {
				return v * K * K;
			}

			@Override
			public double toMegaBytes(double v) {
				return v * K;
			}

			@Override
			public double toGigaBytes(double v) {
				return v;
			}

			@Override
			public double convertTo(double v, Unit s) {
				return s.toGigaBytes(v);
			}
		};

		private static final int K = 1024;

		private String name;

		public abstract double toBytes(double v);

		public abstract double toKiloBytes(double v);

		public abstract double toMegaBytes(double v);

		public abstract double toGigaBytes(double v);

		public abstract double convertTo(double v, Unit s);

		private Unit(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static Unit forUnit(char unit) {
			return forUnit(String.valueOf(unit));
		}

		public static Unit forUnit(String unit) {
			for (Unit size : values()) {
				if (size.getName().equalsIgnoreCase(unit)) {
					return size;
				}
			}
			throw new IllegalArgumentException("Unexpected units value: " + unit);
		}

	}

	public static final Memory ZERO = new Memory(0, BYTES);

	private final long value;
	private final Unit size;

	public Memory(long value, Unit size) {
		this.value = value;
		this.size = size;
	}

	public Memory(long value, char units) {
		this(value, Unit.forUnit(units));
	}

	@Override
	public String toString() {
		return String.valueOf(value) + size.getName();
	}

	public static Memory memory(String string) {
		return new Memory(Long.valueOf(string.substring(0, string.length() - 1)),
				Unit.forUnit(string.charAt(string.length() - 1)));
	}

	public static Memory memory(String value, char unit) {
		return new Memory(Long.valueOf(value), Unit.forUnit(unit));
	}

	public static Memory bytes(long value) {
		return value == 0 ? ZERO : new Memory(value, BYTES);
	}
	
	public static Memory kilobytes(long value) {
		return value == 0 ? ZERO : new Memory(value, KILOBYTES);
	}

	public static Memory kilobytes(String value) {
		return kilobytes(Long.parseLong(value));
	}

	public static Memory megabytes(int value) {
		return (long) value == 0 ? ZERO : new Memory(value, MEGABYTES);
	}

	public static Memory gigabytes(int value) {
		return (long) value == 0 ? ZERO : new Memory(value, GIGABYTES);
	}

	@Override
	public int compareTo(Memory other) {
		return Double.compare(get(this, BYTES), get(other, BYTES));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Memory && compareTo((Memory) obj) == 0;
	}

	private static double get(Memory memory, Unit s) {
		return s.convertTo(memory.value, memory.size);
	}

	public boolean greaterThan(Memory other) {
		return compareTo(other) > 0;
	}

	public boolean lessThan(Memory other) {
		return compareTo(other) < 0;
	}

	public boolean isZero() {
		return value == 0;
	}

	public long getKilobytes() {
		return getValue(Unit.KILOBYTES);
	}

	public long getValue(Unit size) {
		return (long) size.convertTo(value, this.size);
	}

	public Memory toKilobytes() {
		return new Memory(getKilobytes(), Unit.KILOBYTES);
	}

	public Memory minus(Memory other) {
		Unit smaller = smaller(this.size, other.size);
		return new Memory((long) (get(this, smaller) - get(other, smaller)), smaller);
	}

	public Memory plus(Memory other) {
		Unit smaller = smaller(this.size, other.size);
		return new Memory((long) (get(this, smaller) + get(other, smaller)), smaller);
	}

	private Unit smaller(Unit size1, Unit size2) {
		return size1.compareTo(size2) < 0 ? size1 : size2;
	}


}
