package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.util.Constants.Size.BYTES;
import static org.eclipselabs.garbagecat.util.Constants.Size.KILOBYTES;

import org.eclipselabs.garbagecat.util.Constants.Size;

public class Memory implements Comparable<Memory> {

	public static final Memory ZERO = new Memory(0, BYTES);

	private final long value;
	private final Size size;

	public Memory(long value, Size size) {
		this.value = value;
		this.size = size;
	}

	public Memory(long value, char units) {
		this(value, Size.forUnit(units));
	}

	@Override
	public String toString() {
		return String.valueOf(value) + size.getName();
	}

	public static Memory memory(String string) {
		return new Memory(Long.valueOf(string.substring(0, string.length() - 1)),
				Size.forUnit(string.charAt(string.length() - 1)));
	}

	public static Memory memory(String value, char unit) {
		return new Memory(Long.valueOf(value), Size.forUnit(unit));
	}

	public static Memory kilobytes(long value) {
		return value == 0 ? ZERO : new Memory(value, KILOBYTES);
	}

	public static Memory kilobytes(String value) {
		return kilobytes(Long.parseLong(value));
	}

	@Override
	public int compareTo(Memory other) {
		return Double.compare(get(this, BYTES), get(other, BYTES));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Memory && compareTo((Memory) obj) == 0;
	}

	private static double get(Memory memory, Size s) {
		return s.convertTo(memory.value, memory.size);
	}

	public boolean greaterThan(Memory other) {
		return compareTo(other) > 0;
	}

	public long getKilobytes() {
		return getValue(Size.KILOBYTES);
	}

	public long getValue(Size size) {
		return (long) size.convertTo(value, this.size);
	}

	public Memory toKilobytes() {
		return new Memory(getKilobytes(), Size.KILOBYTES);
	}

	public Memory minus(Memory other) {
		Size smaller = smaller(this.size, other.size);
		return new Memory((long) (get(this, smaller) - get(other, smaller)), smaller);
	}

	private Size smaller(Size size1, Size size2) {
		return size1.compareTo(size2) < 0 ? size1 : size2;
	}

}
