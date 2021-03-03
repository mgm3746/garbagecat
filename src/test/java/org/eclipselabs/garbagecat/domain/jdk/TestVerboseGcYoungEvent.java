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
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestVerboseGcYoungEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        assertTrue(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 2205570508L, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", 1726387, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 773247, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3097984, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 231803, event.getDuration());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]        ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
    }

    @Test
    public void testLogLineMissingBeginningOccupancy() {
        String logLine = "90.168: [GC 876593K(1851392K), 0.0701780 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 90168, event.getTimestamp());
        // We set beginging to end occupancy
        assertEquals("Combined begin size not parsed correctly.", 876593, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 876593, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 1851392, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 70178, event.getDuration());
    }

    @Test
    public void testLogLineTriggerAllocationFailure() {
        String logLine = "4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), 0.1028162 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 4970, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Combined begin size not parsed correctly.", 136320, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 18558, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 102816, event.getDuration());
    }

    @Test
    public void testLogLineTriggerCmsInitialMark() {
        String logLine = "12.915: [GC (CMS Initial Mark)  59894K(3128704K), 0.0058845 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 12915, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK));
        // We set beginging to end occupancy
        assertEquals("Combined begin size not parsed correctly.", 59894, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 59894, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 5884, event.getDuration());
    }

    @Test
    public void testLogLineTriggerCmsFinalRemark() {
        String logLine = "70.096: [GC (CMS Final Remark)  521627K(3128704K), 0.2481277 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 70096, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        // We set beginging to end occupancy
        assertEquals("Combined begin size not parsed correctly.", 521627, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 521627, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 248127, event.getDuration());
    }

    @Test
    public void testLogLineTriggerGcLockerInitiatedGc() {
        String logLine = "37.357: [GC (GCLocker Initiated GC)  128035K->124539K(3128704K), 0.0713498 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 37357, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        assertEquals("Combined begin size not parsed correctly.", 128035, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 124539, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 71349, event.getDuration());
    }

    @Test
    public void testLogLineWithDashes() {
        String logLine = "1582.746: [GC-- 5524217K->5911480K(5911488K), 1.5564360 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 1582746, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", 5524217, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 5911480, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 5911488, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 1556436, event.getDuration());
    }

    @Test
    public void testLogLineTriggerWithDashes() {
        String logLine = "1020971.877: [GC (Allocation Failure) -- 1044870K->1045980K(1046016K), 0.1061259 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 1020971877, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", 1044870, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 1045980, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 1046016, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 106125, event.getDuration());
    }

    @Test
    public void testLogLineWithDatestamp() {
        String logLine = "2016-07-22T11:49:00.678+0100: 4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), "
                + "0.1028162 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 4970, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", 136320, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 18558, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 102816, event.getDuration());
    }

    @Test
    public void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "20.748: [GC (Metadata GC Threshold)  288163K->251266K(1253376K), 0.0183041 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 20748, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        assertEquals("Combined begin size not parsed correctly.", 288163, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 251266, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 1253376, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 18304, event.getDuration());
    }

    @Test
    public void testLogLineTriggerExplicitGc() {
        String logLine = "8453.745: [GC (System.gc())  525225K->457601K(939520K), 0.0325441 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 8453745, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Combined begin size not parsed correctly.", 525225, event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", 457601, event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", 939520, event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 32544, event.getDuration());
    }
}
