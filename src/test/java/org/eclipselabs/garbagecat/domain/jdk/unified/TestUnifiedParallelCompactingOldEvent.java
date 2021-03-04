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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.junit.Test;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedParallelCompactingOldEvent {

    @Test
    public void testPreprocessed() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
        UnifiedParallelCompactingOldEvent event = new UnifiedParallelCompactingOldEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 83, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        assertEquals("Young begin size not parsed correctly.", kilobytes(502), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(496), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1536), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(472), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(432), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(2048), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(701), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(701), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 4336, event.getDuration());
        assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD + "not identified.",
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedParallelCompactingOldEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedParallelCompactingOldEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD);
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s    ";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
    }

    @Test
    public void testLogLine7SpacesAfterStart() {
        String logLine = "[28.977s][info][gc,start       ] GC(2269) Pause Full (Ergonomics) PSYoungGen: "
                + "64K->0K(20992K) ParOldGen: 26612K->21907K(32768K) Metaspace: 3886K->3886K(1056768K) 26M->21M(52M) "
                + "48.135ms User=0.09s Sys=0.00s Real=0.05s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
    }
}
