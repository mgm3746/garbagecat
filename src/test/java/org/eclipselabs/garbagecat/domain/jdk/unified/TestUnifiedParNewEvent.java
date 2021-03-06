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
class TestUnifiedParNewEvent {

    @Test
    void testPreprocessed() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(UnifiedParNewEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".");
        UnifiedParNewEvent event = new UnifiedParNewEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString(),event.getName(),"Event name incorrect.");
        assertEquals((long) 49,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(974),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(128),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1152),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(0),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(518),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(960),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(250),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(250),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(3544,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(1,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(1,event.getTimeSys(),"Sys time not parsed correctly.");
        assertEquals(1,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(200,event.getParallelism(),"Parallelism not calculated correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_PAR_NEW,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.UNIFIED_PAR_NEW + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedParNewEvent, JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PAR_NEW;
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedParNewEvent, JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PAR_NEW), JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PAR_NEW);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s    ";
        assertTrue(UnifiedParNewEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".");
    }
}
