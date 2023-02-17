/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedParallelCompactingOldEvent {

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedParallelCompactingOldEvent,
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine7SpacesAfterStart() {
        String logLine = "[28.977s][info][gc,start       ] GC(2269) Pause Full (Ergonomics) PSYoungGen: "
                + "64K->0K(20992K) ParOldGen: 26612K->21907K(32768K) Metaspace: 3886K->3886K(1056768K) 26M->21M(52M) "
                + "48.135ms User=0.09s Sys=0.00s Real=0.05s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s    ";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedParallelCompactingOldEvent,
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
        UnifiedParallelCompactingOldEvent event = new UnifiedParallelCompactingOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) 83, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.ERGONOMICS, "Trigger not parsed correctly.");
        assertEquals(kilobytes(502), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(496), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1536), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(472), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(432), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(2048), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(701), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(701), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(4336, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk17() {
        String logLine = "[0.058s][info][gc,start    ] GC(3) Pause Full (Ergonomics) PSYoungGen: "
                + "499K(1536K)->497K(1536K) ParOldGen: 400K(512K)->366K(2048K) Metaspace: 666K(832K)->666K(832K) "
                + "0M->0M(3M) 2.095ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.108+0000][240210707ms] GC(951) Pause Full (Heap Dump Initiated GC) "
                + "PSYoungGen: 17888K->0K(1538048K) ParOldGen: 152353K->163990K(180224K) "
                + "Metaspace: 217673K->217673K(1275904K) 166M->160M(1678M) 189.216ms User=0.80s Sys=0.02s Real=0.19s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcClearSoftReferences() {
        String logLine = "[2022-02-08T07:33:14.187+0000][7732435ms] GC(116) Pause Full (Metadata GC Clear Soft "
                + "References) PSYoungGen: 0K->0K(732672K) ParOldGen: 120523K->120438K(1467904K) Metaspace: "
                + "243732K->243732K(481280K) 117M->117M(2149M) 353.492ms User=0.52s Sys=0.00s Real=0.36s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcThreshold() {
        String logLine = "[2021-05-06T21:03:33.227+0000][22115ms] GC(11) Pause Full (Metadata GC Threshold) "
                + "PSYoungGen: 2160K->0K(66560K) ParOldGen: 57994K->54950K(175104K) "
                + "Metaspace: 88760K->88684K(337920K) 58M->53M(236M) 521.443ms User=0.86s Sys=0.00s Real=0.52s";
        assertTrue(UnifiedParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as unified.");
    }

}
