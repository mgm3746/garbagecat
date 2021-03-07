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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestVerboseGcYoungEvent {

    @Test
    void testIsBlocking() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals(2205570508L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1726387), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(773247), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3097984), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(231803, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]        ";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
    }

    @Test
    void testLogLineMissingBeginningOccupancy() {
        String logLine = "90.168: [GC 876593K(1851392K), 0.0701780 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 90168, event.getTimestamp(), "Time stamp not parsed correctly.");
        // We set beginging to end occupancy
        assertEquals(kilobytes(876593), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(876593), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1851392), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(70178, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerAllocationFailure() {
        String logLine = "4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), 0.1028162 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 4970, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(136320), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(18558), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(102816, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerCmsInitialMark() {
        String logLine = "12.915: [GC (CMS Initial Mark)  59894K(3128704K), 0.0058845 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 12915, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK), "Trigger not parsed correctly.");
        // We set beginging to end occupancy
        assertEquals(kilobytes(59894), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(59894), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(5884, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerCmsFinalRemark() {
        String logLine = "70.096: [GC (CMS Final Remark)  521627K(3128704K), 0.2481277 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 70096, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK), "Trigger not parsed correctly.");
        // We set beginging to end occupancy
        assertEquals(kilobytes(521627), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(521627), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(248127, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerGcLockerInitiatedGc() {
        String logLine = "37.357: [GC (GCLocker Initiated GC)  128035K->124539K(3128704K), 0.0713498 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 37357, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(128035), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(124539), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(71349, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWithDashes() {
        String logLine = "1582.746: [GC-- 5524217K->5911480K(5911488K), 1.5564360 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 1582746, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(5524217), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(5911480), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(5911488), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1556436, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerWithDashes() {
        String logLine = "1020971.877: [GC (Allocation Failure) -- 1044870K->1045980K(1046016K), 0.1061259 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 1020971877, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1044870), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(1045980), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1046016), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(106125, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWithDatestamp() {
        String logLine = "2016-07-22T11:49:00.678+0100: 4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), "
                + "0.1028162 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 4970, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(136320), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(18558), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(102816, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "20.748: [GC (Metadata GC Threshold)  288163K->251266K(1253376K), 0.0183041 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 20748, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD), "Trigger not parsed correctly.");
        assertEquals(kilobytes(288163), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(251266), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1253376), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(18304, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerExplicitGc() {
        String logLine = "8453.745: [GC (System.gc())  525225K->457601K(939520K), 0.0325441 secs]";
        assertTrue(VerboseGcYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".");
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 8453745, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(525225), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(457601), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(939520), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(32544, event.getDuration(), "Duration not parsed correctly.");
    }
}
