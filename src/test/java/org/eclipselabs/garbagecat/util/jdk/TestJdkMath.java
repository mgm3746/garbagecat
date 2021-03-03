/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.domain.TimesData;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkMath {

    @Test
    public void testConvertDurationToMillis() {
        String secs = "0.0225213";
        assertEquals("Secs not converted to milliseconds properly.", 22,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    @Test
    public void testConvertDurationDecimalCommaToMillis() {
        String secs = "0,0225213";
        assertEquals("Secs not converted to milliseconds properly.", 22,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    /**
     * Durations are always rounded down.
     */
    @Test
    public void testConvertDurationToMillisRoundDownOddFive() {
        String secs = "0.0975";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 97,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    @Test
    public void testConvertDurationToMillisRoundDownEvenFive() {
        String secs = "0.0985";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 98,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    @Test
    public void testConvertDurationToMicrosRoundUp() {
        String secs = "0.0968475";
        assertEquals("Secs not converted to microseconds with expected rounding mode.", 96847,
                JdkMath.convertSecsToMicros(secs).intValue());
    }

    @Test
    public void testConvertDurationToMicrosRoundDown() {
        String secs = "0.0968485";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 96848,
                JdkMath.convertSecsToMicros(secs).intValue());
    }

    @Test
    public void testConvertMillisToMicros() {
        String millis = "0.0975";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 97,
                JdkMath.convertMillisToMicros(millis).intValue());
    }

    @Test
    public void testRoundMillis() {
        String millis = "2.169";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 2,
                JdkMath.roundMillis(millis).intValue());
    }

    @Test
    public void testRoundMillisDown() {
        String millis = "2.969";
        assertEquals("Secs not converted to milliseconds with expected rounding mode.", 2,
                JdkMath.roundMillis(millis).intValue());
    }

    @Test
    public void testThroughput() {
        int duration = 81;
        long timestamp = 1000;
        int priorDuration = 10;
        long priorTimestamp = 900;
        assertEquals(50, JdkMath.calcThroughput(duration, timestamp, priorDuration, priorTimestamp));
    }

    @Test
    public void testTotalCmsRemarkDuration() {
        String[] durations = new String[3];
        durations[0] = "0.0226730";
        durations[1] = "0.0624566";
        durations[2] = "0.0857010";
        assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    @Test
    public void testTotalCmsRemarkDecimalCommaDuration() {
        String[] durations = new String[3];
        durations[0] = "0,0226730";
        durations[1] = "0,0624566";
        durations[2] = "0,0857010";
        assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    @Test
    public void testConvertDurationMillisToSecs() {
        long duration = 123456;
        assertEquals("Millis not converted to seconds with expected rounding mode.", "123.456",
                JdkMath.convertMillisToSecs(duration).toString());
    }

    @Test
    public void testCalcKilobytesMegabytes() {
        int size = 1;
        char units = 'M';
        assertEquals("Megabytes not converted to kilobytes.", 1024, JdkMath.calcKilobytes(size, units));
    }

    @Test
    public void testCalcKilobytesGigabytes() {
        int size = 1;
        char units = 'G';
        assertEquals("Megabytes not converted to kilobytes.", 1048576, JdkMath.calcKilobytes(size, units));
    }

    @Test
    public void testConvertSizeG1DetailsToKilobytesB() {
        String size = "102400";
        char units = 'B';
        assertEquals("G1 details not converted to kilobytes.", 100, JdkMath.convertSizeToKilobytes(size, units));
    }

    @Test
    public void testConvertSizeG1DetailsToKilobytesK() {
        String size = "1234567";
        char units = 'K';
        assertEquals("G1 details not converted to kilobytes.", 1234567,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    @Test
    public void testConvertSizeG1DetailsToKilobytesM() {
        String size = "10";
        char units = 'M';
        assertEquals("G1 details not converted to kilobytes.", 10240,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    @Test
    public void testConvertSizeG1DetailsToKilobytesMWithComma() {
        String size = "306,0";
        char units = 'M';
        assertEquals("G1 details not converted to kilobytes.", 313344,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    @Test
    public void testConvertSizeG1DetailsToKilobytesG() {
        String size = "100";
        char units = 'G';
        assertEquals("G1 details not converted to kilobytes.", 104857600,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    @Test
    public void testCalcParallelism() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 10;
        assertEquals("Parallelism not calculated correctly.", 1000,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismRounded() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 1000;
        assertEquals("Parallelism not calculated correctly.", 10,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismRoundedUp() {
        int timeUser = 90;
        int timeSys = 10;
        int timeReal = 199;
        assertEquals("Parallelism not calculated correctly.", 51,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismUserZero() {
        int timeUser = 0;
        int timeSys = 0;
        int timeReal = 100;
        assertEquals("Parallelism not calculated correctly.", 0,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismRealZero() {
        int timeUser = 100;
        int timeSys = 0;
        int timeReal = 0;
        assertEquals("Parallelism not calculated correctly.", Integer.MAX_VALUE,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismUserZeroRealZero() {
        int timeUser = 0;
        int timeSys = 0;
        int timeReal = 0;
        assertEquals("Parallelism not calculated correctly.", 100,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testCalcParallelismNoData() {
        int timeUser = TimesData.NO_DATA;
        int timeSys = TimesData.NO_DATA;
        int timeReal = TimesData.NO_DATA;
        assertEquals("Parallelism not calculated correctly.", 100,
                JdkMath.calcParallelism(timeUser, timeSys, timeReal));
    }

    @Test
    public void testParallelism() {
        assertTrue("Parallism not calculated correctly.", JdkMath.isInvertedParallelism(0));
        assertTrue("Parallism not calculated correctly.", JdkMath.isInvertedParallelism(99));
        assertFalse("Parallism not calculated correctly.", JdkMath.isInvertedParallelism(100));
    }
}
