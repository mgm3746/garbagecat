/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util;

import static java.lang.Long.parseLong;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.GIGABYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.MEGABYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.forUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
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
            public double convert(double sourceValue, Unit sourceUnit) {
                return sourceUnit.toBytes(sourceValue);
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
            public double convert(double sourceValue, Unit sourceUnit) {
                return sourceUnit.toKiloBytes(sourceValue);
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
            public double convert(double sourceValue, Unit sourceUnit) {
                return sourceUnit.toMegaBytes(sourceValue);
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
            public double convert(double sourceValue, Unit sourceUnit) {
                return sourceUnit.toGigaBytes(sourceValue);
            }
        };

        private static final int K = 1024;

        private String name;

        public abstract double toBytes(double v);

        public abstract double toKiloBytes(double v);

        public abstract double toMegaBytes(double v);

        public abstract double toGigaBytes(double v);

        public abstract double convert(double sourceValue, Unit sourceUnit);

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

    private static final Pattern optionSizePattern = Pattern.compile("(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?");

    public static final Memory ZERO = new Memory(0, BYTES);

    public static Memory memory(long value, Unit unit) {
        return value == 0 ? ZERO : new Memory(value, unit);
    }

    public static Memory memory(String string) {
        return new Memory(Long.parseLong(string.substring(0, string.length() - 1)),
                forUnit(string.charAt(string.length() - 1)));
    }

    public static Memory memory(String value, char unit) {
        return memory(Long.parseLong(value), forUnit(unit));
    }

    public static Memory bytes(long value) {
        return memory(value, BYTES);
    }

    public static Memory bytes(String value) {
        return bytes(parseLong(value));
    }

    public static Memory kilobytes(long value) {
        return memory(value, KILOBYTES);
    }

    public static Memory kilobytes(String value) {
        return kilobytes(parseLong(value));
    }

    public static Memory megabytes(long value) {
        return memory(value, MEGABYTES);
    }

    public static Memory megabytes(String value) {
        return megabytes(parseLong(value));
    }

    public static Memory gigabytes(long value) {
        return memory(value, GIGABYTES);
    }

    public static Memory gigabytes(String value) {
        return gigabytes(parseLong(value));
    }

    private static double get(Memory memory, Unit unit) {
        return unit.convert(memory.value, memory.size);
    }

    /**
     * Convert JVM size option to bytes.
     * 
     * @param size
     *            The size in various units (e.g. 'k').
     * @return The size in bytes.
     */
    public static Memory fromOptionSize(String size) {
        return fromOptionSize(size, BYTES);
    }

    /**
     * Convert JVM size option to bytes.
     * 
     * @param size
     *            The size in various units (e.g. 'k').
     * @param unit
     *            The unit to use if not unit is specified
     * @return The size in bytes.
     */
    public static Memory fromOptionSize(String size, Unit unit) {
        Matcher matcher = optionSizePattern.matcher(size);
        return matcher.find()
                ? memory(parseLong(matcher.group(1)), matcher.group(2) == null ? unit : forUnit(matcher.group(2)))
                : null;
    }

    private final Unit size;

    private final long value;

    public Memory(long value, char units) {
        this(value, forUnit(units));
    }

    public Memory(long value, Unit size) {
        this.value = value;
        this.size = size;
    }

    @Override
    public int compareTo(Memory other) {
        return Double.compare(get(this, BYTES), get(other, BYTES));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Memory && compareTo((Memory) obj) == 0;
    }

    public long getValue(Unit size) {
        return (long) size.convert(value, this.size);
    }

    public boolean greaterThan(Memory other) {
        return compareTo(other) > 0;
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean lessThan(Memory other) {
        return compareTo(other) < 0;
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

    public Memory convertTo(Unit target) {
        return new Memory(getValue(target), target);
    }

    @Override
    public String toString() {
        return String.valueOf(value) + size.getName();
    }

}
