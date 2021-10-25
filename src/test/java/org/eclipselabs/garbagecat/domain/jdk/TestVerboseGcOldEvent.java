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
class TestVerboseGcOldEvent {

    @Test
    void testIsBlocking() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals(2143132151L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1606823), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(1409859), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(2976064), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(12085559, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]    ";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
    }

    @Test
    void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "18129.496: [Full GC (Metadata GC Threshold)  629455K->457103K(3128704K), 4.4946967 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 18129496, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD), "Trigger not parsed correctly.");
        assertEquals(kilobytes(629455), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(457103), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(4494696, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerLastDitchCollection() {
        String logLine = "18134.427: [Full GC (Last ditch collection)  457103K->449140K(3128704K), 5.6081071 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 18134427, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION), "Trigger not parsed correctly.");
        assertEquals(kilobytes(457103), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(449140), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(5608107, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2016-06-22T14:04:51.080+0100: 22561.627: [Full GC (Last ditch collection)  "
                + "500269K->500224K(3128704K), 4.2311820 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 22561627, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION), "Trigger not parsed correctly.");
        assertEquals(kilobytes(500269), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(500224), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(3128704), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(4231182, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2016-06-22T14:04:51.080+0100: [Full GC (Last ditch collection)  500269K->500224K(3128704K), "
                + "4.2311820 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals(519897891080L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineG1Sizes() {
        String logLine = "2017-03-20T04:30:01.936+0800: 2950.666: [Full GC 8134M->2349M(8192M), 10.3726320 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 2950666, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == null, "Trigger not parsed correctly.");
        assertEquals(kilobytes(8329216), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(2405376), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(8388608), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(10372632, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerAllocationFailure() {
        String logLine = "2017-04-06T15:22:40.708-0500: 303068.960: [Full GC (Allocation Failure)  "
                + "7455264K->4498878K(7992832K), 13.2445067 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 303068960, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(7455264), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(4498878), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(7992832), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(13244506, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerErgonomics() {
        String logLine = "2412.683: [Full GC (Ergonomics)  728595K->382365K(932352K), 1.2268902 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 2412683, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS), "Trigger not parsed correctly.");
        assertEquals(kilobytes(728595), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(382365), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(932352), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1226890, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerExplicitGc() {
        String logLine = "8453.778: [Full GC (System.gc())  457601K->176797K(939520K), 1.5623937 secs]";
        assertTrue(VerboseGcOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 8453778, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(457601), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(176797), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(939520), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1562393, event.getDuration(), "Duration not parsed correctly.");
    }
}
