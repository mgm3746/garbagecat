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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestVerboseGcOldEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        assertTrue(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 2143132151L, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(1606823), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(1409859), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(2976064), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 12085559, event.getDuration());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]    ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
    }

    @Test
    public void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "18129.496: [Full GC (Metadata GC Threshold)  629455K->457103K(3128704K), 4.4946967 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 18129496, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(629455), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(457103), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(3128704), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 4494696, event.getDuration());
    }

    @Test
    public void testLogLineTriggerLastDitchCollection() {
        String logLine = "18134.427: [Full GC (Last ditch collection)  457103K->449140K(3128704K), 5.6081071 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 18134427, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(457103), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(449140), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(3128704), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 5608107, event.getDuration());
    }

    @Test
    public void testLogLineDatestamp() {
        String logLine = "2016-06-22T14:04:51.080+0100: 22561.627: [Full GC (Last ditch collection)  "
                + "500269K->500224K(3128704K), 4.2311820 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 22561627, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(500269), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(500224), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(3128704), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 4231182, event.getDuration());
    }

    @Test
    public void testLogLineG1Sizes() {
        String logLine = "2017-03-20T04:30:01.936+0800: 2950.666: [Full GC 8134M->2349M(8192M), 10.3726320 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 2950666, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        assertEquals("Combined begin size not parsed correctly.", kilobytes(8329216), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(2405376), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(8388608), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 10372632, event.getDuration());
    }

    @Test
    public void testLogLineTriggerAllocationFailure() {
        String logLine = "2017-04-06T15:22:40.708-0500: 303068.960: [Full GC (Allocation Failure)  "
                + "7455264K->4498878K(7992832K), 13.2445067 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 303068960, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(7455264), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(4498878), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(7992832), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 13244506, event.getDuration());
    }

    @Test
    public void testLogLineTriggerErgonomics() {
        String logLine = "2412.683: [Full GC (Ergonomics)  728595K->382365K(932352K), 1.2268902 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 2412683, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(728595), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(382365), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(932352), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 1226890, event.getDuration());
    }

    @Test
    public void testLogLineTriggerExplicitGc() {
        String logLine = "8453.778: [Full GC (System.gc())  457601K->176797K(939520K), 1.5623937 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 8453778, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(457601), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(176797), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(939520), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 1562393, event.getDuration());
    }
}
