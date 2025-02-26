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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJdkRegEx {

    @Test
    void testBlankLine() {
        String logLine = "";
        assertTrue(logLine.matches(JdkRegEx.BLANK_LINE), "Blank log line not identified.");
    }

    @Test
    void testDatestampEvent() {
        String logLine = "2020-03-10T08:03:29.311-0400: MGM";
        assertTrue(logLine.matches(JdkRegEx.DATESTAMP_EVENT), "'" + logLine + "' " + "is a valid datestamp event.");
        logLine = "2020-03-10T08:03:29.311-0400: 123.456: MGM";
        assertFalse(logLine.matches(JdkRegEx.DATESTAMP_EVENT), "'" + logLine + "' " + "not a valid datestamp event.");
    }

    @Test
    void testDatestampGmtMinus() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        assertTrue(datestamp.matches(JdkRegEx.DATESTAMP), "Datestamp not recognized.");
    }

    @Test
    void testDatestampGmtPlus() {
        String datestamp = "2010-04-16T12:11:18.979+0200";
        assertTrue(datestamp.matches(JdkRegEx.DATESTAMP), "Datestamp not recognized.");
    }

    @Test
    void testDateTime() {
        String datetime = "2016-10-18 01:50:54";
        assertTrue(datetime.matches(JdkRegEx.DATETIME), "'" + datetime + "' " + "not a valid datetime.");
    }

    @Test
    void testDurationDecimalComma() {
        String duration = "0,0225213 secs";
        assertTrue(duration.matches(JdkRegEx.DURATION), "'0,0225213 secs' is a valid duration.");
    }

    @Test
    void testDurationFractionk5Digits() {
        String durationFraction = "4.583/35144.874 secs";
        assertTrue(durationFraction.matches(JdkRegEx.DURATION_FRACTION),
                "'" + durationFraction + "' " + "is a valid duration fraction.");
    }

    @Test
    void testDurationJdk9() {
        String duration = "2.969ms";
        assertTrue(duration.matches(JdkRegEx.DURATION_MS), "'" + duration + "' is a valid duration.");
    }

    @Test
    void testDurationJdk9WithSpace() {
        String duration = "15.91 ms";
        assertTrue(duration.matches(JdkRegEx.DURATION_MS), "'" + duration + "' is a valid duration.");
    }

    @Test
    void testDurationLessThanOne() {
        String duration = ".0225213 secs";
        assertFalse(duration.matches(JdkRegEx.DURATION), "Durations less than one have a leading zero.");
    }

    @Test
    void testDurationValid7() {
        String duration = "0.0225213 secs";
        assertTrue(duration.matches(JdkRegEx.DURATION), "'0.0225213 secs' is a valid duration.");
    }

    @Test
    void testDurationValid8() {
        String duration = "0.02252132 secs";
        assertTrue(duration.matches(JdkRegEx.DURATION), "'0.02252132 secs' is a valid duration.");
    }

    @Test
    void testDurationWithCharacter() {
        String duration = "0.02A5213 secs";
        assertFalse(duration.matches(JdkRegEx.DURATION), "Duration is a decimal number.");
    }

    @Test
    void testDurationWithFewer7DecimalPlaces() {
        String duration = "0.022521 secs";
        assertFalse(duration.matches(JdkRegEx.DURATION), "Duration has 7-8 decimal places.");
    }

    @Test
    void testDurationWithMore8DecimalPlaces() {
        String duration = "0.022521394 secs";
        assertFalse(duration.matches(JdkRegEx.DURATION), "Duration has 7 decimal places.");
    }

    @Test
    void testDurationWithNoDecimal() {
        String duration = "00225213 secs";
        assertFalse(duration.matches(JdkRegEx.DURATION), "Duration has 7 decimal places.");
    }

    @Test
    void testDurationWithoutUnits() {
        String duration = "0.0225213";
        assertTrue(duration.matches(JdkRegEx.DURATION), "'0.0225213' is a valid duration.");
    }

    @Test
    void testDurationWithSec() {
        String duration = "0.0225213 sec";
        assertTrue(duration.matches(JdkRegEx.DURATION), "'0.0225213 sec' is a valid duration.");
    }

    @Test
    void testJdkReleaseString() {
        String release = "1.8.0_342-b07";
        assertTrue(release.matches(JdkRegEx.RELEASE_STRING), "Release not identified.");
    }

    @Test
    void testMegabytesM() {
        String unit = "M";
        assertTrue(unit.matches(JdkRegEx.MEGABYTES), "'" + unit + "' " + "is a valid unit.");
    }

    @Test
    void testPercent() {
        String percent = "54.8%";
        assertTrue(percent.matches(JdkRegEx.PERCENT), "'" + percent + "' " + "not a valid percent.");
    }

    @Test
    void testPromotionFailure() {
        String promotionFailure = " (0: promotion failure size = 200)  (1: promotion failure size = 8)  "
                + "(2: promotion failure size = 200)  (3: promotion failure size = 200)  "
                + "(4: promotion failure size = 200)  (5: promotion failure size = 200)  "
                + "(6: promotion failure size = 200)  (7: promotion failure size = 200)  "
                + "(8: promotion failure size = 10)  (9: promotion failure size = 10)  "
                + "(10: promotion failure size = 10)  (11: promotion failure size = 200)  "
                + "(12: promotion failure size = 200)  (13: promotion failure size = 10)  "
                + "(14: promotion failure size = 200)  (15: promotion failure size = 200)  "
                + "(16: promotion failure size = 200)  (17: promotion failure size = 200)  "
                + "(18: promotion failure size = 200)  (19: promotion failure size = 200)  "
                + "(20: promotion failure size = 10)  (21: promotion failure size = 200)  "
                + "(22: promotion failure size = 10)  (23: promotion failure size = 45565)  "
                + "(24: promotion failure size = 10)  (25: promotion failure size = 4)  "
                + "(26: promotion failure size = 200)  (27: promotion failure size = 200)  "
                + "(28: promotion failure size = 10)  (29: promotion failure size = 200)  "
                + "(30: promotion failure size = 200)  (31: promotion failure size = 200)  "
                + "(32: promotion failure size = 200) ";
        assertTrue(promotionFailure.matches(JdkRegEx.PRINT_PROMOTION_FAILURE),
                "'" + promotionFailure + "' " + "not a valid PROMOTION_FAILURE.");
    }

    @Test
    void testSizeComma() {
        String size = "306,0M";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid G1 details size.");
    }

    @Test
    void testSizeDecimalBytes() {
        String size = "0.0B";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeDecimalGigabytes() {
        String size = "30.0G";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid G1 details size.");
    }

    @Test
    void testSizeDecimalKilobytes() {
        String size = "8192.0K";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeDecimalMegabytes() {
        String size = "28.0M";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeUnitsCase() {
        String size = "1234k";
        assertFalse(size.matches(JdkRegEx.SIZE), "Size must have capital K (kilobytes).");
    }

    @Test
    void testSizeValid() {
        String size = "1234K";
        assertTrue(size.matches(JdkRegEx.SIZE), "'1234K' is a valid size.");
    }

    @Test
    void testSizeWholeBytes() {
        String size = "0B";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeWholeGigabytes() {
        String size = "30G";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeWholeKilobytes() {
        String size = "8192K";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeWholeMegabytes() {
        String size = "28M";
        assertTrue(size.matches(JdkRegEx.SIZE), "'" + size + "' " + "is a valid size.");
    }

    @Test
    void testSizeWithDecimal() {
        String size = "1.234K";
        assertFalse(size.matches(JdkRegEx.SIZE), "Size is a whole number.");
    }

    @Test
    void testSizeWithInvalidCharacter() {
        String size = "A234K";
        assertFalse(size.matches(JdkRegEx.SIZE), "Size is a decimal number.");
    }

    @Test
    void testSizeWithNineTensPlaces() {
        String size = "129092672K";
        assertTrue(size.matches(JdkRegEx.SIZE), "'129092672K' is a valid size.");
    }

    @Test
    void testSizeWithoutUnits() {
        String size = "1234";
        assertFalse(size.matches(JdkRegEx.SIZE), "Size must have capital K (kilobytes).");
    }

    @Test
    void testTimesBlock5Digits() {
        String timesBlock = " [Times: user=29858.25 sys=2074.63, real=35140.48 secs]";
        assertTrue(timesBlock.matches(TimesData.REGEX), "'" + timesBlock + "' " + "is a valid times block.");
    }

    @Test
    void testTimestampDecimalComma() {
        String timestamp = "1,123";
        assertTrue(timestamp.matches(JdkRegEx.TIMESTAMP), "'" + timestamp + "' is a valid timestamp.");
    }

    @Test
    void testTimestampLessThanOne() {
        String timestamp = ".123";
        assertTrue(timestamp.matches(JdkRegEx.TIMESTAMP), "Timestamps less than one do not have a leading zero.");
    }

    @Test
    void testTimestampTimeUptime() {
        String decorator = "2020-03-10T08:03:29.311-0400: 0.373:";
        assertTrue(decorator.matches(JdkRegEx.DECORATOR), "'" + decorator + "' " + "not a valid decorator.");
    }

    @Test
    void testTimestampValid() {
        String timestamp = "1.123";
        assertTrue(timestamp.matches(JdkRegEx.TIMESTAMP), "'" + timestamp + "' is a valid timestamp.");
    }

    @Test
    void testTimestampWithCharacter() {
        String timestamp = "A.123";
        assertFalse(timestamp.matches(JdkRegEx.TIMESTAMP), "Timestamps are decimal numbers.");
    }

    @Test
    void testTimestampWithFewerDecimalPlaces() {
        String timestamp = "1.12";
        assertFalse(timestamp.matches(JdkRegEx.TIMESTAMP), "Timestamps have 3 decimal places.");
    }

    @Test
    void testTimestampWithMoreDecimalPlaces() {
        String timestamp = "1.1234";
        assertFalse(timestamp.matches(JdkRegEx.TIMESTAMP), "Timestamps have 3 decimal places.");
    }

    @Test
    void testTimestampWithNoDecimal() {
        String timestamp = "11234";
        assertFalse(timestamp.matches(JdkRegEx.TIMESTAMP), "Timestamps have 3 decimal places.");
    }

    @Test
    void testUnloadingClassBlock() {
        String unloadingClassBlock = "[Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]";
        assertTrue(unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK),
                "'" + unloadingClassBlock + "' " + "is a valid class unloading block.");
    }

    @Test
    void testUnloadingClassProxyBlock() {
        String unloadingClassBlock = "[Unloading class $Proxy109]";
        assertTrue(unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK),
                "'" + unloadingClassBlock + "' " + "is a valid class unloading block.");
    }

    @Test
    void testZeroSize() {
        String size = "0K";
        assertTrue(size.matches(JdkRegEx.SIZE), "Zero sizes are valid.");
    }
}
