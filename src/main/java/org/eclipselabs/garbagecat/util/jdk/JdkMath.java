/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.Memory.Unit;

/**
 * Math utility methods and constants for OpenJDK and Oracle JDK.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public final class JdkMath {

    /**
     * Calculate parallelism, the ratio of user + sys to wall (real) time.
     * 
     * @param timeUser
     *            The user (non-kernel) time in centiseconds.
     * @param timeSys
     *            The sys (kernel) time in centiseconds.
     * @param timeReal
     *            The wall (clock) time in centiseconds.
     * 
     * @return Percent user+sy:real time rounded up the the nearest whole number.
     */
    public static int calcParallelism(final int timeUser, final int timeSys, final int timeReal) {
        int calc;
        if (timeReal == 0 || timeReal == TimesData.NO_DATA) {
            if ((timeUser == 0 && timeSys == 0) || timeUser == TimesData.NO_DATA || timeSys == TimesData.NO_DATA) {
                // Undefined (no times data) or explicitly equal to zero.
                calc = 100;
            } else {
                calc = Integer.MAX_VALUE;
            }
        } else {
            BigDecimal parallelism = new BigDecimal(timeUser);
            parallelism = parallelism.add(new BigDecimal(timeSys));
            BigDecimal hundred = new BigDecimal("100");
            parallelism = parallelism.multiply(hundred);
            parallelism = parallelism.divide(new BigDecimal(timeReal), 0, RoundingMode.CEILING);
            calc = parallelism.intValue();
        }
        return calc;
    }

    /**
     * Calculate percent.
     * 
     * @param part
     *            The numerator.
     * @param whole
     *            The denominator.
     * 
     * @return Percent part:whole rounded to the nearest whole number.
     */
    public static int calcPercent(final long part, final long whole) {
        int percent;
        if (whole == 0) {
            if (part == 0 && whole == 0) {
                percent = 100;
            } else {
                percent = Integer.MAX_VALUE;
            }
        } else {
            BigDecimal calc = new BigDecimal(part);
            BigDecimal hundred = new BigDecimal("100");
            calc = calc.multiply(hundred);
            calc = calc.divide(new BigDecimal(whole), 0, RoundingMode.HALF_EVEN);
            percent = calc.intValue();
        }
        return percent;
    }

    /**
     * Calculate the throughput between two garbage collection (GC) points. Throughput is the percent of time not spent
     * doing GC.
     * 
     * @param currentDuration
     *            Current collection time spent doing GC (milliseconds) beginning at currentTimestamp.
     * @param currentTimestamp
     *            Current collection timestamp (milliseconds after JVM startup).
     * @param priorDuration
     *            Prior collection time spent doing GC (milliseconds) beginning at priorTimestamp. 0 for the first
     *            collection.
     * @param priorTimestamp
     *            Prior collection timestamp (milliseconds after JVM startup). 0 for the first collection.
     * @return Throughput as a percent. 0 means all time was spent doing GC. 100 means no time was spent doing GC.
     */
    public static int calcThroughput(final int currentDuration, final long currentTimestamp, final int priorDuration,
            final long priorTimestamp) {
        long timeTotal = currentTimestamp + currentDuration - priorTimestamp;
        long timeNotGc = timeTotal - currentDuration - priorDuration;
        BigDecimal throughput = new BigDecimal(timeNotGc);
        throughput = throughput.divide(new BigDecimal(timeTotal), 2, RoundingMode.HALF_EVEN);
        throughput = throughput.movePointRight(2);
        return throughput.intValue();
    }

    /**
     * Convert microseconds to centiseconds.
     * 
     * For example: Convert 123456 to 12.
     * 
     * @param micros
     *            Microseconds as a whole number.
     * @return Centiseconds rounded down to a whole number
     */
    public static BigDecimal convertMicrosToCentis(long micros) {
        BigDecimal centis = new BigDecimal(micros);
        return centis.movePointLeft(4).setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert microseconds to milliseconds.
     * 
     * For example: Convert 987654321 to 987654.321.
     * 
     * @param micros
     *            Microseconds as a whole number.
     * @return Milliseconds rounded to 3 decimal places.
     */
    public static BigDecimal convertMicrosToMillis(long micros) {
        BigDecimal duration = new BigDecimal(micros);
        return duration.movePointLeft(3).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert microseconds to nanoseconds.
     * 
     * For example: Convert 98 to 98000.
     * 
     * @param micros
     *            Microseconds as a whole number.
     * @return Nanoseconds rounded down to a whole.
     */
    public static BigDecimal convertMicrosToNanos(long micros) {
        BigDecimal nanos = new BigDecimal(micros).movePointRight(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return nanos.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert microseconds to seconds.
     * 
     * For example: Convert 987654321 to 987.654.
     * 
     * @param micros
     *            Microseconds as a whole number.
     * @return Seconds rounded to 3 decimal places.
     */
    public static BigDecimal convertMicrosToSecs(long micros) {
        BigDecimal duration = new BigDecimal(micros);
        return duration.movePointLeft(6).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert milliseconds to microseconds.
     * 
     * For example: Convert 0.003 to 3
     * 
     * @param millis
     *            Milliseconds as a whole number or decimal.
     * @return Microseconds rounded to a whole number.
     */
    public static BigDecimal convertMillisToMicros(String millis) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal duration = new BigDecimal(millis.replace(",", ".")).movePointRight(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return duration.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert milliseconds to nanoseconds.
     * 
     * For example: Convert 0.003 to 3000
     * 
     * @param millis
     *            Milliseconds as a whole number or decimal.
     * @return Nanoseconds rounded to a whole number.
     */
    public static BigDecimal convertMillisToNanos(String millis) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal duration = new BigDecimal(millis.replace(",", ".")).movePointRight(6);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return duration.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert milliseconds to seconds.
     * 
     * For example: Convert 123456 to 123.456.
     * 
     * @param millis
     *            Milliseconds as a whole number.
     * @return Seconds rounded to 3 decimal places.
     */
    public static BigDecimal convertMillisToSecs(long millis) {
        BigDecimal secs = new BigDecimal(millis);
        return secs.movePointLeft(3).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert nanoseconds to microseconds.
     * 
     * For example: Convert 987654321 to 987654.321.
     * 
     * @param nanos
     *            Nanoseconds as a whole number.
     * @return Microseconds rounded to 3 decimal places.
     */
    public static BigDecimal convertNanosToMicros(long nanos) {
        BigDecimal duration = new BigDecimal(nanos);
        return duration.movePointLeft(3).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert nanoseconds to microseconds.
     * 
     * For example: Convert 98765 to 98.
     * 
     * @param nanos
     *            Nanoseconds as a whole number.
     * @return Microseconds rounded down to a whole.
     */
    public static BigDecimal convertNanosToMicros(String nanos) {
        BigDecimal micros = new BigDecimal(nanos).movePointLeft(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return micros.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert nanoseconds to milliseconds.
     * 
     * For example: Convert 987654321 to 987.654.
     * 
     * @param nanos
     *            Nanoseconds as a whole number.
     * @return Milliseconds rounded to 3 decimal places.
     */
    public static BigDecimal convertNanosToMillis(long nanos) {
        BigDecimal duration = new BigDecimal(nanos);
        return duration.movePointLeft(6).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert nanoseconds to seconds.
     * 
     * For example: Convert 987654321000 to 987.654.
     * 
     * @param nanos
     *            Nanoseconds as a whole number.
     * @return Seconds rounded to 3 decimal places.
     */
    public static BigDecimal convertNanosToSecs(long nanos) {
        BigDecimal duration = new BigDecimal(nanos);
        return duration.movePointLeft(9).setScale(3, RoundingMode.HALF_EVEN);
    }

    /**
     * Convert seconds to centiseconds.
     * 
     * For example: Convert 1.02 to 102.
     * 
     * @param secs
     *            Seconds as a number with 2 decimal places.
     * @return Centoseconds.
     */
    public static BigDecimal convertSecsToCentis(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal duration = new BigDecimal(secs.replace(",", ".")).movePointRight(2);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return duration.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert seconds to microseconds.
     * 
     * For example: Convert 0.0225213 to 23521
     * 
     * @param secs
     *            Seconds as a whole number or decimal.
     * @return Microseconds rounded to a whole number.
     */
    public static BigDecimal convertSecsToMicros(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal micros = new BigDecimal(secs.replace(",", ".")).movePointRight(6);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return micros.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert seconds to milliseconds.
     * 
     * For example: Convert 0.0225213 to 23.
     * 
     * @param secs
     *            Seconds as a whole number or decimal.
     * @return Milliseconds rounded to a whole number.
     */
    public static BigDecimal convertSecsToMillis(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal millis = new BigDecimal(secs.replace(",", ".")).movePointRight(3);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return millis.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Convert seconds to nanoseconds.
     * 
     * For example: Convert 0.0225213 to 23521000
     * 
     * @param secs
     *            Seconds as a whole number or decimal.
     * @return Nanoseconds rounded to a whole number.
     */
    public static BigDecimal convertSecsToNanos(String secs) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal micros = new BigDecimal(secs.replace(",", ".")).movePointRight(9);
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return micros.setScale(0, RoundingMode.DOWN);
    }

    public static Memory convertSizeToKilobytes(double size, char units) {
        return new Memory(BigDecimal.valueOf(Unit.forUnit(units).toKiloBytes(size)).setScale(0, RoundingMode.HALF_EVEN)
                .longValue(), Unit.KILOBYTES);

    }

    /**
     * Convert SIZE to kilobytes.
     * 
     * @param size
     *            The size (e.g. '128.0', 306,0).
     * @param units
     *            The units (e.g. 'G').
     * @return The size in Kilobytes.
     */
    public static Memory convertSizeToKilobytes(String size, char units) {
        return convertSizeToKilobytes(Double.parseDouble(size.replace(",", ".")), units);
    }

    /**
     * @param parallelism
     *            The parallelism percent (ratio of user + sys to wall (real) time).
     * 
     * @return True if the parallelism is "inverted", false otherwise. Inverted parallelism is &lt; 100. In other words,
     *         the parallel collection performance is less than serial (single-threaded).
     */
    public static boolean isInvertedParallelism(int parallelism) {
        return (parallelism < 100);
    }

    /**
     * @param serialism
     *            The serialism percent (ratio of user + sys to wall (real) time).
     * 
     * @return True if the serialism is "inverted", false otherwise. Inverted serialism is &lt; 90. In other words, real
     *         time is ~11% more than user+sys.
     */
    public static boolean isInvertedSerialism(int serialism) {
        return (serialism < 90);
    }

    /**
     * @param parallelism
     *            The parallelism percent (ratio or user to wall (real time).
     * 
     * @return True if the parallelism is low (&lt; 2 threads, rounded), false otherwise.
     */
    public static boolean isLowParallelism(int parallelism) {
        return (parallelism < 150);
    }

    /**
     * Round milliseconds to whole number.
     * 
     * For example: Convert 2.969 to 2
     * 
     * @param millis
     *            Milliseconds with decimal places.
     * @return Milliseconds rounded to a whole number.
     */
    public static BigDecimal roundMillis(String millis) {
        // BigDecimal does not accept decimal commas, only decimal periods
        BigDecimal rounded = new BigDecimal(millis.replace(",", "."));
        // Round down to avoid TimeWarpExceptions when events are spaced close together
        return rounded.setScale(0, RoundingMode.DOWN);
    }

    /**
     * Add together an array of durations and convert seconds to milliseconds.
     * 
     * Useful for the CMS Remark phase, where a single event can have multiple phases and durations.
     * 
     * For example: 0.0226730 + 0.0624566 + 0.0857010 = .1708306 seconds = 171 milliseconds
     * 
     * @param durations
     *            <code>Array</code> of seconds as whole numbers and/or decimals.
     * @return Total time rounded to a whole number.
     */
    public static int totalDuration(String[] durations) {
        BigDecimal duration = new BigDecimal("0");
        for (int i = 0; i < durations.length; i++) {
            // BigDecimal does not accept decimal commas, only decimal periods
            duration = duration.add(new BigDecimal(durations[i].replace(",", ".")));
        }
        return convertSecsToMillis(duration.toPlainString()).intValue();
    }

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkMath() {
        super();
    }
}
