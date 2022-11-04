/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.GIGABYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.MEGABYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.util.Memory;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJdkMath {

    @Test
    void testCalcKilobytesGigabytes() {
        assertEquals(kilobytes(1024 * 1024), new Memory(1, GIGABYTES), "Megabytes not converted to kilobytes.");
    }

    @Test
    void testCalcKilobytesMegabytes() {
        assertEquals(kilobytes(1024), new Memory(1, MEGABYTES), "Megabytes not converted to kilobytes.");
    }

    @Test
    void testCalcParallelism() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 10;
        assertEquals(1000, JdkMath.calcParallelism(timeUser, timeSys, timeReal),
                "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismNoData() {
        int timeUser = TimesData.NO_DATA;
        int timeSys = TimesData.NO_DATA;
        int timeReal = TimesData.NO_DATA;
        assertEquals(100, JdkMath.calcParallelism(timeUser, timeSys, timeReal),
                "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismRealZero() {
        int timeUser = 100;
        int timeSys = 0;
        int timeReal = 0;
        assertEquals(Integer.MAX_VALUE, JdkMath.calcParallelism(timeUser, timeSys, timeReal),
                "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismRounded() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 1000;
        assertEquals(10, JdkMath.calcParallelism(timeUser, timeSys, timeReal), "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismRoundedUp() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 199;
        assertEquals(51, JdkMath.calcParallelism(timeUser, timeSys, timeReal), "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismUserZero() {
        int timeUser = 0;
        int timeSys = 0;
        int timeReal = 100;
        assertEquals(0, JdkMath.calcParallelism(timeUser, timeSys, timeReal), "Parallelism not calculated correctly.");
    }

    @Test
    void testCalcParallelismUserZeroRealZero() {
        int timeUser = 0;
        int timeSys = 0;
        int timeReal = 0;
        assertEquals(100, JdkMath.calcParallelism(timeUser, timeSys, timeReal),
                "Parallelism not calculated correctly.");
    }

    @Test
    void testConvertDurationDecimalCommaToMillis() {
        String secs = "0,0225213";
        assertEquals(22, JdkMath.convertSecsToMillis(secs).intValue(), "Secs not converted to milliseconds properly.");
    }

    @Test
    void testConvertDurationMillisToSecs() {
        long duration = 123456;
        assertEquals("123.456", JdkMath.convertMillisToSecs(duration).toString(),
                "Millis not converted to seconds with expected rounding mode.");
    }

    @Test
    void testConvertDurationToMicrosRoundDown() {
        String secs = "0.0968485";
        assertEquals(96848, JdkMath.convertSecsToMicros(secs).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    @Test
    void testConvertDurationToMicrosRoundUp() {
        String secs = "0.0968475";
        assertEquals(96847, JdkMath.convertSecsToMicros(secs).intValue(),
                "Secs not converted to microseconds with expected rounding mode.");
    }

    @Test
    void testConvertDurationToMillis() {
        String secs = "0.0225213";
        assertEquals(22, JdkMath.convertSecsToMillis(secs).intValue(), "Secs not converted to milliseconds properly.");
    }

    @Test
    void testConvertDurationToMillisRoundDownEvenFive() {
        String secs = "0.0985";
        assertEquals(98, JdkMath.convertSecsToMillis(secs).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    /**
     * Durations are always rounded down.
     */
    @Test
    void testConvertDurationToMillisRoundDownOddFive() {
        String secs = "0.0975";
        assertEquals(97, JdkMath.convertSecsToMillis(secs).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    @Test
    void testConvertMicrosToCentis() {
        long micros = 123456;
        assertEquals(12, JdkMath.convertMicrosToCentis(micros).longValue(),
                "Micros not converted to centiseconds with expected rounding mode.");
    }

    @Test
    void testConvertMillisToMicros() {
        String millis = "0.0975";
        assertEquals(97, JdkMath.convertMillisToMicros(millis).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    @Test
    void testConvertSizeG1DetailsToKilobytesB() {
        String size = "102400";
        char units = 'B';
        assertEquals(kilobytes(100), JdkMath.convertSizeToKilobytes(size, units),
                "G1 details not converted to kilobytes.");
    }

    @Test
    void testConvertSizeG1DetailsToKilobytesG() {
        String size = "100";
        char units = 'G';
        assertEquals(kilobytes(104857600), JdkMath.convertSizeToKilobytes(size, units),
                "G1 details not converted to kilobytes.");
    }

    @Test
    void testConvertSizeG1DetailsToKilobytesK() {
        String size = "1234567";
        char units = 'K';
        assertEquals(kilobytes(1234567), JdkMath.convertSizeToKilobytes(size, units),
                "G1 details not converted to kilobytes.");
    }

    @Test
    void testConvertSizeG1DetailsToKilobytesM() {
        String size = "10";
        char units = 'M';
        assertEquals(kilobytes(10240), JdkMath.convertSizeToKilobytes(size, units),
                "G1 details not converted to kilobytes.");
    }

    @Test
    void testConvertSizeG1DetailsToKilobytesMWithComma() {
        String size = "306,0";
        char units = 'M';
        assertEquals(kilobytes(313344), JdkMath.convertSizeToKilobytes(size, units),
                "G1 details not converted to kilobytes.");
    }

    @Test
    void testInvertedParallelism() {
        assertTrue(JdkMath.isInvertedParallelism(0), "Inverted parallism not calculated correctly.");
        assertTrue(JdkMath.isInvertedParallelism(99), "Inverted parallism not calculated correctly.");
        assertFalse(JdkMath.isInvertedParallelism(100), "Inverted parallism not calculated correctly.");
    }

    @Test
    void testInvertedSerialism() {
        assertTrue(JdkMath.isInvertedSerialism(0), "Inverted serialism not calculated correctly.");
        assertTrue(JdkMath.isInvertedSerialism(89), "Inverted serialism not calculated correctly.");
        assertFalse(JdkMath.isInvertedSerialism(90), "Inverted serialism not calculated correctly.");
    }

    @Test
    void testRoundMillis() {
        String millis = "2.169";
        assertEquals(2, JdkMath.roundMillis(millis).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    @Test
    void testRoundMillisDown() {
        String millis = "2.969";
        assertEquals(2, JdkMath.roundMillis(millis).intValue(),
                "Secs not converted to milliseconds with expected rounding mode.");
    }

    @Test
    void testThroughput() {
        int duration = 81;
        long timestamp = 1000;
        int priorDuration = 10;
        long priorTimestamp = 900;
        assertEquals(50, JdkMath.calcThroughput(duration, timestamp, priorDuration, priorTimestamp));
    }

    @Test
    void testTotalCmsRemarkDecimalCommaDuration() {
        String[] durations = new String[3];
        durations[0] = "0,0226730";
        durations[1] = "0,0624566";
        durations[2] = "0,0857010";
        assertEquals(170, JdkMath.totalDuration(durations), "CMS Remark times not added properly.");
    }

    @Test
    void testTotalCmsRemarkDuration() {
        String[] durations = new String[3];
        durations[0] = "0.0226730";
        durations[1] = "0.0624566";
        durations[2] = "0.0857010";
        assertEquals(170, JdkMath.totalDuration(durations), "CMS Remark times not added properly.");
    }
}
