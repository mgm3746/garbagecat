/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedSerialNewEvent {

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SERIAL_NEW;
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedSerialNewEvent,
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.035s][info][gc,start    ] GC(0) Pause Young (Allocation Failure) DefNew: "
                + "1022K(1152K)->127K(1152K) Tenured: 0K(768K)->552K(768K) Metaspace: 155K(256K)->155K(256K) "
                + "0M->0M(1M) 0.937ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 35, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLine7SpacesAfterStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure) DefNew: "
                + "1016K->128K(1152K) Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) "
                + "0.700ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s   ";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
    }

    /**
     * Test with uptime decorator.
     */
    @Test
    void testMillis() {
        String logLine = "[3ms] GC(6) Pause Young (Allocation Failure) DefNew: 1016K->128K(1152K) "
                + "Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) 0.700ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 3, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedSerialNewEvent,
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 41, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(983), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(128), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1152), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(0), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(458), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(768), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(246), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(246), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(1393, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW),
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SERIAL_NEW);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as unified.");
    }
}
