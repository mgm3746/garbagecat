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
    void testIdentityEventType() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SAFEPOINT, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT + "not identified.");
    }

    @Test
    void testJdk11Time() {
        String logLine = "[2021-09-14T11:40:53.379-0500][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][info][safepoint     ] Total time for which "
                + "application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(684934853379L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk11TimeUptime() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk11TimeUptimeMillis() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144035ms][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk11Uptime() {
        String logLine = "[144.035s][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144.036s][info][safepoint     ] Leaving safepoint region[144.036s][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk11UptimeMillis() {
        String logLine = "[144035ms][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144036ms][info][safepoint     ] Leaving safepoint region[144036ms][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17CleanClassLoaderDataMetaspaces() {
        String logLine = "[0.192s][info][safepoint   ] Safepoint \"CleanClassLoaderDataMetaspaces\", Time since last: "
                + "1223019 ns, Reaching safepoint: 138450 ns, At safepoint: 73766 ns, Total: 212216 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.CLEAN_CLASSLOADER_DATA_METASPACES, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(192, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(138450, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(73766, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17G1Concurrent() {
        String logLine = "[0.064s][info][safepoint   ] Safepoint \"G1Concurrent\", Time since last: 1666947 ns, "
                + "Reaching safepoint: 79150 ns, At safepoint: 349999 ns, Total: 429149 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_CONCURRENT, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(64, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(79150, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(349999, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17G1TryInitiateConcMark() {
        String logLine = "[2023-01-11T16:09:59.190+0000][19084.729s] Safepoint \"G1TryInitiateConcMark\", Time since "
                + "last: 720212197 ns, Reaching safepoint: 477437 ns, At safepoint: 119597295 ns, Total: 120074732 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_TRY_INITIATE_CONC_MARK, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(19084729, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(477437, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(119597295, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17ICBufferFull() {
        String logLine = "[2022-12-29T10:13:49.155+0000][0.433s] Safepoint \"ICBufferFull\", Time since last: "
                + "392373271 ns, Reaching safepoint: 553351 ns, At safepoint: 23698 ns, Total: 577049 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.IC_BUFFER_FULL, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(433, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(553351, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(23698, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17Uptime() {
        String logLine = "[0.061s][info][safepoint   ] Safepoint \"GenCollectForAllocation\", Time since last: "
                + "24548411 ns, Reaching safepoint: 69521 ns, At safepoint: 779732 ns, Total: 849253 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.GEN_COLLECT_FOR_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(61, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(69521, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(779732, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17ZMarkEnd() {
        String logLine = "[0.129s] Safepoint \"ZMarkEnd\", Time since last: 4051145 ns, Reaching safepoint: 79105 ns, "
                + "At safepoint: 16082 ns, Total: 95187 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_END, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(129, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(79105, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(16082, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17ZMarkStart() {
        String logLine = "[0.124s][info][safepoint   ] Safepoint \"ZMarkStart\", Time since last: 103609844 ns, "
                + "Reaching safepoint: 99888 ns, At safepoint: 30677 ns, Total: 130565 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(124, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(99888, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(30677, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testJdk17ZRelocateStart() {
        String logLine = "[0.132s] Safepoint \"ZRelocateStart\", Time since last: 1366138 ns, Reaching safepoint: "
                + "138018 ns, At safepoint: 15653 ns, Total: 153671 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_RELOCATE_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(132, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(138018, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(15653, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

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
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedSafepointEvent,
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " not parsed.");
    }

    @Test
    void testPreprocessedTriggerCgcOperation() {
        String logLine = "[2022-06-06T08:27:45.926-0500] Entering safepoint region: CGC_Operation[2022-06-06T"
                + "08:27:46.004-0500] Leaving safepoint region[2022-06-06T08:27:46.004-0500] Total time for which "
                + "application threads were stopped: 0.0778110 seconds, Stopping threads took: 0.0003865 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.CGC_OPERATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(707819265926L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(386500, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(77811000L, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testPreprocessedTriggerGcHeapInspection() {
        String logLine = "[2022-06-16T10:19:46.929-0400][72053295ms] Entering safepoint region: GC_HeapInspection"
                + "[2022-06-16T10:20:00.205-0400][72066571ms] Leaving safepoint region"
                + "[2022-06-16T10:20:00.205-0400][72066571ms] Total time for which application threads were stopped: "
                + "13.2756956 seconds, Stopping threads took: 0.0000456 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.GC_HEAP_INSPECTION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(72053295, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(45600, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(13275695600L, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testPreprocessedTriggerHeapDumper() {
        String logLine = "[2022-11-08T16:34:57.002-0500] Entering safepoint region: HeapDumper"
                + "[2022-11-08T16:34:57.010-0500] Leaving safepoint region"
                + "[2022-11-08T16:34:57.010-0500] Total time for which application threads were stopped: 0.0076837 "
                + "seconds, Stopping threads took: 0.0000163 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.HEAP_DUMPER, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(721240497002L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(16300, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(7683700, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testPreprocessedTriggerShenandoahDegeneratedGc() {
        String logLine = "[2.909s] Entering safepoint region: ShenandoahDegeneratedGC[2.926s] Leaving safepoint region"
                + "[2.926s] Total time for which application threads were stopped: 0.0171471 seconds, Stopping threads "
                + "took: 0.0000157 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.SHENANDOAH_DEGENERATED_GC, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(2909, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(15700, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(17147100, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testTriggerGcHeapInspection() throws IOException {
        File testFile = TestUtil.getFile("dataset248.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SAFEPOINT);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " not indentified as unified.");
    }

    @Test
    void testWithSpacesAtEnd() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds   ";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
    }

}
