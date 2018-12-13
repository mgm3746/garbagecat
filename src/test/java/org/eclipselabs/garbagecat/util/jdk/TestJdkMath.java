/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import org.eclipselabs.garbagecat.domain.TimesData;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkMath extends TestCase {

    public void testConvertDurationToMillis() {
        String secs = "0.0225213";
        Assert.assertEquals("Secs not converted to milliseconds properly.", 22,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testConvertDurationDecimalCommaToMillis() {
        String secs = "0,0225213";
        Assert.assertEquals("Secs not converted to milliseconds properly.", 22,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    /**
     * Durations are always rounded down.
     */
    public void testConvertDurationToMillisRoundDownOddFive() {
        String secs = "0.0975";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 97,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testConvertDurationToMillisRoundDownEvenFive() {
        String secs = "0.0985";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 98,
                JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testConvertDurationToMicrosRoundUp() {
        String secs = "0.0968475";
        Assert.assertEquals("Secs not converted to microseconds with expected rounding mode.", 96847,
                JdkMath.convertSecsToMicros(secs).intValue());
    }

    public void testConvertDurationToMicrosRoundDown() {
        String secs = "0.0968485";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 96848,
                JdkMath.convertSecsToMicros(secs).intValue());
    }

    public void testRoundMillis() {
        String millis = "2.169";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 2,
                JdkMath.roundMillis(millis).intValue());
    }

    public void testRoundMillisDown() {
        String millis = "2.969";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 2,
                JdkMath.roundMillis(millis).intValue());
    }

    public void testThroughput() {
        int duration = 81;
        long timestamp = 1000;
        int priorDuration = 10;
        long priorTimestamp = 900;
        Assert.assertEquals(50, JdkMath.calcThroughput(duration, timestamp, priorDuration, priorTimestamp));
    }

    public void testTotalCmsRemarkDuration() {
        String[] durations = new String[3];
        durations[0] = "0.0226730";
        durations[1] = "0.0624566";
        durations[2] = "0.0857010";
        Assert.assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    public void testTotalCmsRemarkDecimalCommaDuration() {
        String[] durations = new String[3];
        durations[0] = "0,0226730";
        durations[1] = "0,0624566";
        durations[2] = "0,0857010";
        Assert.assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    public void testConvertDurationMillisToSecs() {
        long duration = 123456;
        Assert.assertEquals("Millis not converted to seconds with expected rounding mode.", "123.456",
                JdkMath.convertMillisToSecs(duration).toString());
    }

    public void testCalcKilobytesMegabytes() {
        int size = 1;
        char units = 'M';
        Assert.assertEquals("Megabytes not converted to kilobytes.", 1024, JdkMath.calcKilobytes(size, units));
    }

    public void testCalcKilobytesGigabytes() {
        int size = 1;
        char units = 'G';
        Assert.assertEquals("Megabytes not converted to kilobytes.", 1048576, JdkMath.calcKilobytes(size, units));
    }

    public void testConvertSizeG1DetailsToKilobytesB() {
        String size = "102400";
        char units = 'B';
        Assert.assertEquals("G1 details not converted to kilobytes.", 100, JdkMath.convertSizeToKilobytes(size, units));
    }

    public void testConvertSizeG1DetailsToKilobytesK() {
        String size = "1234567";
        char units = 'K';
        Assert.assertEquals("G1 details not converted to kilobytes.", 1234567,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    public void testConvertSizeG1DetailsToKilobytesM() {
        String size = "10";
        char units = 'M';
        Assert.assertEquals("G1 details not converted to kilobytes.", 10240,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    public void testConvertSizeG1DetailsToKilobytesMWithComma() {
        String size = "306,0";
        char units = 'M';
        Assert.assertEquals("G1 details not converted to kilobytes.", 313344,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    public void testConvertSizeG1DetailsToKilobytesG() {
        String size = "100";
        char units = 'G';
        Assert.assertEquals("G1 details not converted to kilobytes.", 104857600,
                JdkMath.convertSizeToKilobytes(size, units));
    }

    public void testCalcParallelism() {
        int timeUser = 100;
        int timeReal = 10;
        Assert.assertEquals("Parallelism not calculated correctly.", 1000, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismRounded() {
        int timeUser = 100;
        int timeReal = 1000;
        Assert.assertEquals("Parallelism not calculated correctly.", 10, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismRoundedUp() {
        int timeUser = 100;
        int timeReal = 199;
        Assert.assertEquals("Parallelism not calculated correctly.", 51, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismUserZero() {
        int timeUser = 0;
        int timeReal = 100;
        Assert.assertEquals("Parallelism not calculated correctly.", 0, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismRealZero() {
        int timeUser = 100;
        int timeReal = 0;
        Assert.assertEquals("Parallelism not calculated correctly.", Integer.MAX_VALUE,
                JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismUserZeroRealZero() {
        int timeUser = 0;
        int timeReal = 0;
        Assert.assertEquals("Parallelism not calculated correctly.", 100, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testCalcParallelismNoData() {
        int timeUser = TimesData.NO_DATA;
        int timeReal = TimesData.NO_DATA;
        Assert.assertEquals("Parallelism not calculated correctly.", 100, JdkMath.calcParallelism(timeUser, timeReal));
    }

    public void testParallelism() {
        Assert.assertTrue("Parallism not calculated correctly.", JdkMath.isInvertedParallelism((int) 0));
        Assert.assertTrue("Parallism not calculated correctly.", JdkMath.isInvertedParallelism((int) 99));
        Assert.assertFalse("Parallism not calculated correctly.", JdkMath.isInvertedParallelism((int) 100));
    }
}
