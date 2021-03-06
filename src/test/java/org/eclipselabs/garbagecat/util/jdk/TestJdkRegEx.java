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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.domain.TimesData;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkRegEx {

    @Test
    public void testTimestampWithCharacter() {
        String timestamp = "A.123";
        assertFalse("Timestamps are decimal numbers.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampWithFewerDecimalPlaces() {
        String timestamp = "1.12";
        assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampWithMoreDecimalPlaces() {
        String timestamp = "1.1234";
        assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampWithNoDecimal() {
        String timestamp = "11234";
        assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampLessThanOne() {
        String timestamp = ".123";
        assertTrue("Timestamps less than one do not have a leading zero.",
                timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampValid() {
        String timestamp = "1.123";
        assertTrue("'" + timestamp + "' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testTimestampDecimalComma() {
        String timestamp = "1,123";
        assertTrue("'" + timestamp + "' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    @Test
    public void testSizeWithoutUnits() {
        String size = "1234";
        assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testZeroSize() {
        String size = "0K";
        assertTrue("Zero sizes are valid.", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testSizeUnitsCase() {
        String size = "1234k";
        assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testSizeWithDecimal() {
        String size = "1.234K";
        assertFalse("Size is a whole number.", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testSizeValid() {
        String size = "1234K";
        assertTrue("'1234K' is a valid size.", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testSizeWithInvalidCharacter() {
        String size = "A234K";
        assertFalse("Size is a decimal number.", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testSizeWithNineTensPlaces() {
        String size = "129092672K";
        assertTrue("'129092672K' is a valid size.", size.matches(JdkRegEx.SIZE_K));
    }

    @Test
    public void testDurationWithCharacter() {
        String duration = "0.02A5213 secs";
        assertFalse("Duration is a decimal number.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationWithFewer7DecimalPlaces() {
        String duration = "0.022521 secs";
        assertFalse("Duration has 7-8 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationWithMore8DecimalPlaces() {
        String duration = "0.022521394 secs";
        assertFalse("Duration has 7 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationWithNoDecimal() {
        String duration = "00225213 secs";
        assertFalse("Duration has 7 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationLessThanOne() {
        String duration = ".0225213 secs";
        assertFalse("Durations less than one have a leading zero.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationWithSec() {
        String duration = "0.0225213 sec";
        assertTrue("'0.0225213 sec' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationWithoutUnits() {
        String duration = "0.0225213";
        assertTrue("'0.0225213' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationValid7() {
        String duration = "0.0225213 secs";
        assertTrue("'0.0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationValid8() {
        String duration = "0.02252132 secs";
        assertTrue("'0.02252132 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testDurationDecimalComma() {
        String duration = "0,0225213 secs";
        assertTrue("'0,0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    @Test
    public void testUnloadingClassBlock() {
        String unloadingClassBlock = "[Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]";
        assertTrue("'" + unloadingClassBlock + "' " + "is a valid class unloading block.",
                unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK));
    }

    @Test
    public void testUnloadingClassProxyBlock() {
        String unloadingClassBlock = "[Unloading class $Proxy109]";
        assertTrue("'" + unloadingClassBlock + "' " + "is a valid class unloading block.",
                unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK));
    }

    @Test
    public void testDatestampGmtMinus() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    @Test
    public void testDatestampGmtPlus() {
        String datestamp = "2010-04-16T12:11:18.979+0200";
        assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    @Test
    public void testTimesBlock5Digits() {
        String timesBlock = " [Times: user=29858.25 sys=2074.63, real=35140.48 secs]";
        assertTrue("'" + timesBlock + "' " + "is a valid times block.", timesBlock.matches(TimesData.REGEX));
    }

    @Test
    public void testDurationFractionk5Digits() {
        String durationFraction = "4.583/35144.874 secs";
        assertTrue("'" + durationFraction + "' " + "is a valid duration fraction.",
                durationFraction.matches(JdkRegEx.DURATION_FRACTION));
    }

    @Test
    public void testSizeWholeBytes() {
        String size = "0B";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeWholeKilobytes() {
        String size = "8192K";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeWholeMegabytes() {
        String size = "28M";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeWholeGigabytes() {
        String size = "30G";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeDecimalBytes() {
        String size = "0.0B";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeDecimalKilobytes() {
        String size = "8192.0K";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeDecimalMegabytes() {
        String size = "28.0M";
        assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testMegabytesM() {
        String unit = "M";
        assertTrue("'" + unit + "' " + "is a valid unit.", unit.matches(JdkRegEx.MEGABYTES));
    }

    @Test
    public void testSizeDecimalGigabytes() {
        String size = "30.0G";
        assertTrue("'" + size + "' " + "is a valid G1 details size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testSizeComma() {
        String size = "306,0M";
        assertTrue("'" + size + "' " + "is a valid G1 details size.", size.matches(JdkRegEx.SIZE));
    }

    @Test
    public void testPercent() {
        String percent = "54.8%";
        assertTrue("'" + percent + "' " + "not a valid percent.", percent.matches(JdkRegEx.PERCENT));
    }

    @Test
    public void testDateTime() {
        String datetime = "2016-10-18 01:50:54";
        assertTrue("'" + datetime + "' " + "not a valid datetime.", datetime.matches(JdkRegEx.DATETIME));
    }

    @Test
    public void testPromotionFailure() {
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
        assertTrue("'" + promotionFailure + "' " + "not a valid PROMOTION_FAILURE.",
                promotionFailure.matches(JdkRegEx.PRINT_PROMOTION_FAILURE));
    }

    @Test
    public void testDecoratorTimeUptime() {
        String decorator = "2020-03-10T08:03:29.311-0400: 0.373:";
        assertTrue("'" + decorator + "' " + "not a valid decorator.", decorator.matches(JdkRegEx.DECORATOR));
    }
}
