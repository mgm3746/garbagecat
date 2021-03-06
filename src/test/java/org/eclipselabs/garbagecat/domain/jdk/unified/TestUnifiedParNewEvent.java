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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedParNewEvent {

    @Test
    public void testPreprocessed() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".",
                UnifiedParNewEvent.match(logLine));
        UnifiedParNewEvent event = new UnifiedParNewEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString(), event.getName());
        assertEquals("Time stamp not parsed correctly.", 49, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(974), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(128), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1152), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(0), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(518), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(960), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(250), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(250), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 3544, event.getDuration());
        assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 200, event.getParallelism());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_PAR_NEW + "not identified.", JdkUtil.LogEventType.UNIFIED_PAR_NEW,
                JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedParNewEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PAR_NEW;
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedParNewEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PAR_NEW));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PAR_NEW);
        assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s    ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".",
                UnifiedParNewEvent.match(logLine));
    }
}
