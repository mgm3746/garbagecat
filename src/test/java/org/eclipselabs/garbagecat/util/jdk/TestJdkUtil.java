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
package org.eclipselabs.garbagecat.util.jdk;

import static org.eclipselabs.garbagecat.util.Memory.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.util.Memory;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJdkUtil {

    @Test
    void testConvertLogEntryTimestampsToDate() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        String logLineConverted = "1966-08-18 19:22:04,201: [GC 1966-08-18 19:22:04,202: "
                + "[ParNew: 86199K->8454K(91712K), 0.0375060 secs] 89399K->11655K(907328K), 0.0387074 secs]";
        assertEquals(logLineConverted,JdkUtil.convertLogEntryTimestampsToDateStamp(logLine, calendar.getTime()),"Timestamps not converted to date/time correctly");
    }

    @Test
    void testBottleneckDetectionWholeNumbers() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 500000;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 1 second between GCs with duration of .5 seconds
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int duration2 = 500000;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 50;
        assertFalse(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event incorrectly flagged as a bottleneck.");

        // Test bottleneck
        duration2 = 501000;
        gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);
        assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");

    }

    @Test
    void testBottleneckDetectionFractions() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 100000;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 123 ms between GCs with duration of 33 ms
        String logLine2 = "test2";
        long timestamp2 = 10123L;
        int duration2 = 33000;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 41;
        assertFalse(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event incorrectly flagged as a bottleneck.");

        // Test boundary
        throughputThreshold = 42;
        assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");
    }

    @Test
    void testBottleneckDetectionParNew() {
        String previousLogLine = "56.462: [GC 56.462: [ParNew: 64768K->7168K(64768K), 0.0823950 secs] "
                + "142030K->88353K(567808K), 0.0826320 secs] [Times: user=0.10 sys=0.00, real=0.08 secs]";
        BlockingEvent priorEvent = new ParNewEvent(previousLogLine);
        String logLine = "57.026: [GC 57.026: [ParNew: 64768K->7168K(64768K), 0.1763320 secs] "
                + "145953K->98916K(567808K), 0.1765710 secs] [Times: user=0.30 sys=0.00, real=0.17 secs]";
        BlockingEvent gcEvent = new ParNewEvent(logLine);
        // Test boundary
        int throughputThreshold = 90;
        assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");
    }

    @Test
    void testTimeWarp() {
        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 1000000;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 2nd event starts immediately after the first
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int duration2 = 500000;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 100;

        assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");

        // Decrease timestamp by 1 ms to 2nd event start before 1st event finishes
        timestamp2 = 10999L;
        gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);
        try {
            assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");
        } catch (Exception e) {
            assertTrue(e instanceof TimeWarpException, "Expected TimeWarpException not thrown.");
        }
    }

    /**
     * Test small overlap of .001 is not reported.
     */
    @Test
    void testNoTimeWarpExceptionOneThounsandthOverlap() {
        String logLine1 = "test1";
        long timestamp1 = 1000L;
        int duration1 = 101;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 2nd event starts .001 before the first collection ends
        String logLine2 = "test2";
        long timestamp2 = 1100L;
        int duration2 = 200;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        int throughputThreshold = 100;

        JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold);
    }

    @Test
    void testTimeWarpLoggingReverseOrder() {
        String previousLogLine = "26536.942: [GC26536.943: [ParNew: 792678K->4248K(917504K), 0.0170310 secs] "
                + "1139860K->351466K(6160384K), 0.0172140 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        BlockingEvent priorEvent = new ParNewEvent(previousLogLine);

        // 2nd event starts before first
        String logLine = "26509.631: [GC26509.631: [ParNew: 791446K->4818K(917504K), 0.0255680 secs] "
                + "1096208K->309629K(6160384K), 0.0257810 secs] [Times: user=0.07 sys=0.01, real=0.03 secs]";
        BlockingEvent gcEvent = new ParNewEvent(logLine);

        // Test boundary
        int throughputThreshold = 100;

        try {
            assertTrue(JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold), "Event should have been flagged as a bottleneck.");
        } catch (Exception e) {
            assertTrue(e instanceof TimeWarpException, "Expected TimeWarpException not thrown.");
        }
    }

    @Test
    void testGetOptionValue() {
        assertEquals("256k",JdkUtil.getOptionValue("-Xss256k"),"Option value not correct.");
        assertEquals("2G",JdkUtil.getOptionValue("-Xmx2G"),"Option value not correct.");
        assertEquals("128M",JdkUtil.getOptionValue("-XX:MaxPermSize=128M"),"Option value not correct.");
        assertEquals("3865051136",JdkUtil.getOptionValue("-XX:InitialHeapSize=3865051136"),"Option value not correct.");
        assertEquals("7730102272",JdkUtil.getOptionValue("-XX:MaxHeapSize=7730102272"),"Option value not correct.");
        assertEquals("268435456",JdkUtil.getOptionValue("-XX:MaxPermSize=268435456"),"Option value not correct.");
        assertEquals("67108864",JdkUtil.getOptionValue("-XX:PermSize=67108864"),"Option value not correct.");
        assertNull(JdkUtil.getOptionValue(null),"Option value not correct.");
    }

    @Test
    void testDateStampInMiddle() {
        String logLine = "85030.389: [Full GC 85030.390: [CMS2012-06-20T12:29:58.094+0200: 85030.443: "
                + "[CMS-concurrent-preclean: 0.108/0.139 secs] [Times: user=0.14 sys=0.01, real=0.14 secs]";
        assertTrue(JdkUtil.isLogLineWithDateStamp(logLine), "Datestamp not found.");
    }

    @Test
    void testDoubleDateStampOddFormat() {
        String logLine = "2016-10-12T09:53:31.818+02002016-10-12T09:53:31.818+0200: : 290.944: "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(JdkUtil.isLogLineWithDateStamp(logLine), "Datestamp not found.");
    }

    @Test
    void testConvertOptionSizeToBytesNoUnits() {
        String optionSize = "45097156608";
        assertEquals(bytes(45097156608L),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesLowercaseB() {
        String optionSize = "12345678b";
        assertEquals(bytes(12345678),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesUppercaseB() {
        String optionSize = "12345678B";
        assertEquals(bytes(12345678),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesLowercaseK() {
        String optionSize = "1k";
        assertEquals(bytes(1024),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesUppercaseK() {
        String optionSize = "1K";
        assertEquals(bytes(1024),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesLowercaseM() {
        String optionSize = "1m";
        assertEquals(bytes(1048576),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesUppercaseM() {
        String optionSize = "1M";
        assertEquals(bytes(1048576),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesLowercaseG() {
        String optionSize = "1g";
        assertEquals(bytes(1073741824),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testConvertOptionSizeToBytesUppercaseG() {
        String optionSize = "1G";
        assertEquals(bytes(1073741824),Memory.fromOptionSize(optionSize),"'" + optionSize + "' not converted to expected bytes.");
    }

    @Test
    void testDateStampBeginning() {
        String logLine = "2017-01-30T10:06:50.070+0400: 2232356.357: [GC [PSYoungGen: 242595K->5980K(1324544K)] "
                + "1264815K->1037853K(4121088K), 0.0173240 secs] [Times: user=0.08 sys=0.00, real=0.02 secs]";
        assertEquals("2017-01-30T10:06:50.070+0400",JdkUtil.getDateStamp(logLine),"Datestamp not parsed correctly.");
    }

    @Test
    void testDateStampMiddle() {
        String logLine = "85030.389: [Full GC 85030.390: [CMS2012-06-20T12:29:58.094+0200: 85030.443: "
                + "[CMS-concurrent-preclean: 0.108/0.139 secs] [Times: user=0.14 sys=0.01, real=0.14 secs]";
        assertEquals("2012-06-20T12:29:58.094+0200",JdkUtil.getDateStamp(logLine),"Datestamp not parsed correctly.");
    }
}
