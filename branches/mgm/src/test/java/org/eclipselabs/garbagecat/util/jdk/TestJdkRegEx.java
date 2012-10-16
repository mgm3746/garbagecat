/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;

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
        Assert.assertTrue("Timestamps less than one do not have a leading zero.", timestamp.matches(JdkRegEx.TIMESTAMP));
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
        Assert.assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE));
    }

    public void testZeroSize() {
        String size = "0K";
        Assert.assertTrue("Zero sizes are valid.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeUnitsCase() {
        String size = "1234k";
        Assert.assertFalse("Size must have capital K (kilobytes).", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeWithDecimal() {
        String size = "1.234K";
        Assert.assertFalse("Size is a whole number.", size.matches(JdkRegEx.SIZE));
    }

    public void testSizeValid() {
        String size = "1234K";
        Assert.assertTrue("'1234K' is a valid size.", size.matches(JdkRegEx.SIZE));
    }

    public void testSize7ValidKilobytes() {
        String size = "1234K";
        Assert.assertTrue("'1234K' is a valid size.", size.matches(JdkRegEx.SIZE_JDK7));
    }

    public void testSize7ValidMegabytes() {
        String size = "20M";
        Assert.assertTrue("'30<' is a valid size.", size.matches(JdkRegEx.SIZE_JDK7));
    }

    public void testSizeWithInvalidCharacter() {
        String size = "A234K";
        Assert.assertFalse("Size is a decimal number.", size.matches(JdkRegEx.SIZE));
    }

    public void testDurationWithCharacter() {
        String duration = "0.02A5213 secs";
        Assert.assertFalse("Duration is a decimal number.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithFewerDecimalPlaces() {
        String duration = "0.022521 secs";
        Assert.assertFalse("Duration has 7-8 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithMoreDecimalPlaces() {
        String duration = "0.022521399 secs";
        Assert.assertFalse("Duration has 7-8 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithNoDecimal() {
        String duration = "00225213 secs";
        Assert.assertFalse("Duration has 7-8 decimal places.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationLessThanOne() {
        String duration = ".0225213 secs";
        Assert.assertFalse("Durations less than one have a leading zero.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationWithoutUnits() {
        String duration = "0.0225213 sec";
        Assert.assertFalse("Durations have 'secs' for units.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationValid() {
        String duration = "0.0225213 secs";
        Assert.assertTrue("'0.0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
    }

    public void testDurationDecimalComma() {
        String duration = "0,0225213 secs";
        Assert.assertTrue("'0,0225213 secs' is a valid duration.", duration.matches(JdkRegEx.DURATION));
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

    public void testTimesBlock4Digits() {
        String timesBlock = " [Times: user=2889.80 sys=2.42, real=2891.01 secs]";
        Assert.assertTrue("'" + timesBlock + "' " + "is a valid times block.", timesBlock.matches(JdkRegEx.TIMES_BLOCK));
    }
}
