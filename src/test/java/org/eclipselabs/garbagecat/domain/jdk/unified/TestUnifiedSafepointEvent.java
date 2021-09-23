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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedSafepointEvent {

    @Test
    void testLogLine() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 144036, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SAFEPOINT, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedSafepointEvent,
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SAFEPOINT);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWithSpacesAtEnd() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds   ";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
    }

    @Test
    void testLogLineJdk11Uptime() {
        String logLine = "[144.035s][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144.036s][info][safepoint     ] Leaving safepoint region[144.036s][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 144036, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk11UptimeMillis() {
        String logLine = "[144035ms][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144036ms][info][safepoint     ] Leaving safepoint region[144036ms][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 144036, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");

    }

    @Test
    void testLogLineJdk11Time() {
        String logLine = "[2021-09-14T11:40:53.379-0500][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][info][safepoint     ] Total time for which "
                + "application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 684934853379L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk11TimeUptime() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 144036, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk11TimeUptimeMillis() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144035ms][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 144036, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(658, event.getDuration(), "Duration not parsed correctly.");
    }
}
