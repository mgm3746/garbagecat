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
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1FullGcEvent {

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL;
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedG1FullGcEvent,
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) 79853119, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(214096), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(214096), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(739328), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(8186), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(8178), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(8192), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(2127343, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1640, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(213, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(775, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedMinimalJdk21() {
        String logLine = "[0.069s][info][gc     ] GC(1) Pause Full (G1 Compaction Pause) 1M->1M(5M) "
                + "6.324ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) (69 - 6), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s   ";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
    }

    /**
     * Test Metadata max size before/after.
     */
    @Test
    void testMetadataMaxBeforeAndAfter() {
        String logLine = "[2023-01-12T07:17:50.709+0000][1110134471ms][gc,start] GC(13141) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 1->1 Metaspace: 519911K(834476K)->519307K(834476K) "
                + "1962M->1929M(1968M) 3371.651ms User=6.35s Sys=0.00s Real=3.38s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 214096K->214096K(739328K) "
                + "8186M->8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedG1FullGcEvent,
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " not parsed.");
    }

    @Test
    void testPreprocessedTriggerG1CompactionPause() {
        String logLine = "[2023-08-22T02:49:10.458-0400][178.582s][gc,start] GC(73) Pause Full (G1 Compaction Pause) "
                + "Humongous regions: 105->81 Metaspace: 71663K(72128K)->71663K(72128K) 2679M->2149M(3072M) 657.941ms "
                + "User=2.04s Sys=0.06s Real=0.66s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[2021-10-29T21:02:24.624+0000][info][gc,start       ] GC(23863) Pause Full "
                + "(G1 Humongous Allocation) Humongous regions: 0->0 Metaspace: 69475K->69475K(153600K) "
                + "16339M->14486M(16384M) 8842.979ms User=52.67s Sys=0.01s Real=8.84s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-03-13T03:45:44.425+0530][80337493ms][gc,start] GC(9216) Pause Full "
                + "(GCLocker Initiated GC) Humongous regions: 0->0 Metaspace: 214103K->214103K(739328K) "
                + "8184M->8180M(8192M) 2101.341ms User=16.34s Sys=0.05s Real=2.10s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerMetadataGcClearSoftReferences() {
        String logLine = "[2024-05-06T13:01:17.696+0300][3619400197ms][gc,start] GC(1018) Pause Full "
                + "(Metadata GC Clear Soft References) Humongous regions: 0->0 Metaspace: 2081045K(2097152K)->"
                + "2080952K(2097152K) 777M->767M(3072M) 1291.658ms User=4.58s Sys=0.00s Real=1.29s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerMetadataGcThreshold() {
        String logLine = "[2024-05-06T13:01:13.881+0300][3619396382ms][gc,start] GC(1013) Pause Full "
                + "(Metadata GC Threshold) Humongous regions: 0->0 Metaspace: 2085985K(2097152K)->2080672K(2097152K) "
                + "1223M->961M(3072M) 2558.763ms User=5.73s Sys=0.16s Real=2.56s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerSystemGc() {
        String logLine = "[2022-10-26T09:02:09.409-0500][284552496ms][gc,start] GC(73591) Pause Full (System.gc()) "
                + "Humongous regions: 0->0 Metaspace: 590830K->590830K(1644544K) 2878M->2837M(3072M) 3952.620ms "
                + "User=13.07s Sys=0.00s Real=3.95s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset203.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = (UnifiedG1FullGcEvent) jvmRun.getFirstGcEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (79853119), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreprocessingTriggerHeapDumpInitiatedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset211.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = (UnifiedG1FullGcEvent) jvmRun.getFirstGcEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (1217172136), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][gc,start] GC(4) Pause Full (G1 Compaction Pause) 1M->1M(5M) "
                + "6.324ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s][gc,start] GC(4) Pause Full (G1 Compaction Pause) "
                + "1M->1M(5M) 6.324ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestampTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms][gc,start] GC(4) Pause Full (G1 Compaction Pause) "
                + "1M->1M(5M) 6.324ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s][gc,start] GC(4) Pause Full (G1 Compaction Pause) 1M->1M(5M) 6.324ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms][gc,start] GC(4) Pause Full (G1 Compaction Pause) 1M->1M(5M) 6.324ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(GcTrigger.G1_COMPACTION_PAUSE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(5), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6324, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testUnifiedSerialOldTriggerSystemGc() throws IOException {
        File testFile = TestUtil.getFile("dataset184.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = (UnifiedG1FullGcEvent) jvmRun.getLastGcEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (173690), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnpreprocessedTriggerG1EvacuationPause() {
        String logLine = "[89968.517s][info][gc] GC(1344) Pause Full (G1 Evacuation Pause) 16382M->13777M(16384M) "
                + "6796.352ms";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) 89968517 - 6796, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(megabytes(16382), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(13777), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(16384), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(6796352, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnpreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[390191.660s][info][gc] GC(1407) Pause Full (G1 Humongous Allocation) "
                + "16334M->15583M(16384M) 6561.965ms";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
    }
}
