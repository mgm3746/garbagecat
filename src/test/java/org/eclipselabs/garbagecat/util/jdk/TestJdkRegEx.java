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
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkRegEx extends TestCase {

    public void testTimestampWithCharacter() {
        String timestamp = "A.123";
        Assert.assertFalse("Timestamps are decimal numbers.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithFewerDecimalPlaces() {
        String timestamp = "1.12";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithMoreDecimalPlaces() {
        String timestamp = "1.1234";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampWithNoDecimal() {
        String timestamp = "11234";
        Assert.assertFalse("Timestamps have 3 decimal places.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampLessThanOne() {
        String timestamp = ".123";
        Assert.assertTrue("Timestamps less than one do not have a leading zero.",
                timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampValid() {
        String timestamp = "1.123";
        Assert.assertTrue("'1.123' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testTimestampDecimalComma() {
        String timestamp = "1,123";
        Assert.assertTrue("'1,123' is a valid timestamp.", timestamp.matches(JdkRegEx.TIMESTAMP));
    }

    public void testSizeWithoutUnits() {
        String size = "1234";
        Assert.assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE_K));
    }

    public void testZeroSize() {
        String size = "0K";
        Assert.assertTrue("Zero sizes are valid.", size.matches(JdkRegEx.SIZE_K));
    }

    public void testSizeUnitsCase() {
        String size = "1234k";
        Assert.assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE_K));
    }

    public void testSizeWithDecimal() {
        String size = "1.234K";
        Assert.assertFalse("Size is a whole number.", size.matches(JdkRegEx.SIZE_K));
    }

    public void testSizeValid() {
        String size = "1234K";
        Assert.assertTrue("'1234K' is a valid size.", size.matches(JdkRegEx.SIZE_K));
    }

    public void testSizeWithInvalidCharacter() {
        String size = "A234K";
        Assert.assertFalse("Size is a decimal number.", size.matches(JdkRegEx.SIZE_K));
    }

    public void testSizeWithNineTensPlaces() {
        String size = "129092672K";
        Assert.assertTrue("'129092672K' is a valid size.", size.matches(JdkRegEx.SIZE_K));
    }

    public void testDurationWithCharacter() {
        String duration = "0.02A5213 secs";
        Assert.assertFalse("Duration is a decimal number.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithFewer7DecimalPlaces() {
        String duration = "0.022521 secs";
        Assert.assertFalse("Duration has 7-8 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithMore8DecimalPlaces() {
        String duration = "0.022521394 secs";
        Assert.assertFalse("Duration has 7 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithNoDecimal() {
        String duration = "00225213 secs";
        Assert.assertFalse("Duration has 7 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationLessThanOne() {
        String duration = ".0225213 secs";
        Assert.assertFalse("Durations less than one have a leading zero.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithSec() {
        String duration = "0.0225213 sec";
        Assert.assertTrue("'0.0225213 sec' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithoutUnits() {
        String duration = "0.0225213";
        Assert.assertTrue("'0.0225213' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationValid7() {
        String duration = "0.0225213 secs";
        Assert.assertTrue("'0.0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationValid8() {
        String duration = "0.02252132 secs";
        Assert.assertTrue("'0.02252132 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationDecimalComma() {
        String duration = "0,0225213 secs";
        Assert.assertTrue("'0,0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationJdk9() {
        String duration = "2.969ms";
        Assert.assertTrue("'" + duration + "' is a valid duration.", duration.matches(JdkRegEx.DURATION_JDK9));
    }

    public void testGcEventId() {
        String id = "GC(1326)";
        Assert.assertTrue("'" + id + "' is a valid GC event id.", id.matches(JdkRegEx.GC_EVENT_NUMBER));
    }

    public void testGcEventId7Digits() {
        String id = "GC(1234567)";
        Assert.assertTrue("'" + id + "' is a valid GC event id.", id.matches(JdkRegEx.GC_EVENT_NUMBER));
    }

    public void testParallelScavengeValid() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]";
        Assert.assertTrue("'19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]' " + "is a valid parallel scavenge log entry.",
                ParallelScavengeEvent.match(logLine));
    }

    public void testUnloadingClassBlock() {
        String unloadingClassBlock = "[Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]";
        Assert.assertTrue("'" + unloadingClassBlock + "' " + "is a valid class unloading block.",
                unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK));
    }

    public void testUnloadingClassProxyBlock() {
        String unloadingClassBlock = "[Unloading class $Proxy109]";
        Assert.assertTrue("'" + unloadingClassBlock + "' " + "is a valid class unloading block.",
                unloadingClassBlock.matches(JdkRegEx.UNLOADING_CLASS_BLOCK));
    }

    public void testDatestampGmtMinus() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        Assert.assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    public void testDatestampGmtPlus() {
        String datestamp = "2010-04-16T12:11:18.979+0200";
        Assert.assertTrue("Datestamp not recognized.", datestamp.matches(JdkRegEx.DATESTAMP));
    }

    public void testTimesBlock5Digits() {
        String timesBlock = " [Times: user=29858.25 sys=2074.63, real=35140.48 secs]";
        Assert.assertTrue("'" + timesBlock + "' " + "is a valid times block.", timesBlock.matches(TimesData.REGEX));
    }

    public void testDurationFractionk5Digits() {
        String durationFraction = "4.583/35144.874 secs";
        Assert.assertTrue("'" + durationFraction + "' " + "is a valid duration fraction.",
                durationFraction.matches(JdkRegEx.DURATION_FRACTION));
    }

    public void testSizeG1WholeBytes() {
        String size = "0B";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1WholeKilobytes() {
        String size = "8192K";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1WholeMegabytes() {
        String size = "28M";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1WholeGigabytes() {
        String size = "30G";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1DecimalBytes() {
        String size = "0.0B";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1DecimalKilobytes() {
        String size = "8192.0K";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1DecimalMegabytes() {
        String size = "28.0M";
        Assert.assertTrue("'" + size + "' " + "is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1DecimalGigabytes() {
        String size = "30.0G";
        Assert.assertTrue("'" + size + "' " + "is a valid G1 details size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeG1Comma() {
        String size = "306,0M";
        Assert.assertTrue("'" + size + "' " + "is a valid G1 details size.", size.matches(JdkRegEx.SIZE));
    }

    public void testPercent() {
        String percent = "54.8%";
        Assert.assertTrue("'" + percent + "' " + "not a valid percent.", percent.matches(JdkRegEx.PERCENT));
    }

    public void testDateTime() {
        String datetime = "2016-10-18 01:50:54";
        Assert.assertTrue("'" + datetime + "' " + "not a valid datetime.", datetime.matches(JdkRegEx.DATETIME));
    }

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
        Assert.assertTrue("'" + promotionFailure + "' " + "not a valid PROMOTION_FAILURE.",
                promotionFailure.matches(JdkRegEx.PRINT_PROMOTION_FAILURE));
    }
}
