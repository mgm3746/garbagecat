/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedSafepointEvent {

    @Test
    void testClassLoaderStatsOperation() {
        String logLine = "[0.064s][info][safepoint   ] Safepoint \"ClassLoaderStatsOperation\", "
                + "Time since last: 4597148 ns, Reaching safepoint: 19270 ns, Cleanup: 47719 ns, At safepoint: "
                + "586473 ns, Total: 653462 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.CLASSLOADER_STATS_OPERATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(64, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19270, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(47719, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(586473, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testCleanClassLoaderDataMetaspacesJdk17() {
        String logLine = "[0.192s][info][safepoint   ] Safepoint \"CleanClassLoaderDataMetaspaces\", Time since last: "
                + "1223019 ns, Reaching safepoint: 138450 ns, At safepoint: 73766 ns, Total: 212216 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.CLEAN_CLASSLOADER_DATA_METASPACES, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(192, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(138450, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(73766, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testCleanupJdk17Update8() {
        String logLine = "[1.708s] JDK17U8 Safepoint \"G1CollectForAllocation\", Time since last: 11990384 ns, "
                + "Reaching safepoint: 2496 ns, Cleanup: 11042 ns, At safepoint: 623787 ns, Total: 637325 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_COLLECT_FOR_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(1708, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(2496, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(11042, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(623787, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testDecoratorTimeUptimeMillisJdk11() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144035ms][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144036ms][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testG1ConcurrentJdk17() {
        String logLine = "[0.064s][info][safepoint   ] Safepoint \"G1Concurrent\", Time since last: 1666947 ns, "
                + "Reaching safepoint: 79150 ns, At safepoint: 349999 ns, Total: 429149 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_CONCURRENT, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(64, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(79150, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(349999, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testG1TryInitiateConcMarkJdk17() {
        String logLine = "[2023-01-11T16:09:59.190+0000][19084.729s] Safepoint \"G1TryInitiateConcMark\", Time since "
                + "last: 720212197 ns, Reaching safepoint: 477437 ns, At safepoint: 119597295 ns, Total: 120074732 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_TRY_INITIATE_CONC_MARK, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(19084729L - 120, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(477437, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(119597295, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
        assertEquals(120074, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testICBufferFullJdk17() {
        String logLine = "[2022-12-29T10:13:49.155+0000][0.433s] Safepoint \"ICBufferFull\", Time since last: "
                + "392373271 ns, Reaching safepoint: 553351 ns, At safepoint: 23698 ns, Total: 577049 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.IC_BUFFER_FULL, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(433, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(553351, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(23698, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertEquals(JdkUtil.EventType.UNIFIED_SAFEPOINT,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SAFEPOINT + "not identified.");
    }

    @Test
    void testJfrCheckPoint() {
        String logLine = "[0.433s][info][safepoint   ] Safepoint \"JFRCheckpoint\", Time since "
                + "last: 89423866 ns, Reaching safepoint: 81442 ns, Cleanup: 113246 ns, At safepoint: 25191 ns, "
                + "Total: 219879 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.JFR_CHECKPOINT, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(433 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(81442, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(25191, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
        assertEquals(113246, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(106, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testJfrOldObject() {
        String logLine = "[144.035s][info][safepoint   ] Safepoint \"JFROldObject\", Time since "
                + "last: 4554564 ns, Reaching safepoint: 87172 ns, Cleanup: 50531 ns, At safepoint: 31967 ns, "
                + "Total: 169670 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.JFR_OLD_OBJECT, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(87172, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(50531, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(31967, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testLogLine() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedSafepointEvent,
                JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + " not parsed.");
    }

    @Test
    void testPauseCleanupJdk17Update8() {
        String logLine = "[0.069s] Safepoint \"G1PauseCleanup\", Time since last: 399905 ns, Reaching safepoint: "
                + "63366 ns, Cleanup: 7340 ns, At safepoint: 22046 ns, Total: 92752 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_PAUSE_CLEANUP, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(69, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(63366, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(7340, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(22046, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testPauseRemarkJdk17Update8() {
        String logLine = "[0.069s] Safepoint \"G1PauseRemark\", Time since last: 1333915 ns, Reaching safepoint: "
                + "64336 ns, Cleanup: 6649 ns, At safepoint: 156813 ns, Total: 227798 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.G1_PAUSE_REMARK, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(69, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(64336, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(6649, event.getTimeCleanup(), "Time cleanup not parsed correctly.");
        assertEquals(156813, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testPreprocessedTriggerCgcOperation() {
        String logLine = "[2022-06-06T08:27:45.926-0500] Entering safepoint region: CGC_Operation[2022-06-06T"
                + "08:27:46.004-0500] Leaving safepoint region[2022-06-06T08:27:46.004-0500] Total time for which "
                + "application threads were stopped: 0.0778110 seconds, Stopping threads took: 0.0003865 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
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
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
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
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
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
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.SHENANDOAH_DEGENERATED_GC, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(2909, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(15700, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(17147100, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testSetNotifyJvmtiEventsMode() {
        String logLine = "[0.433s][info][safepoint   ] Safepoint \"SetNotifyJvmtiEventsMode\", Time since last: "
                + "95823672 ns, Reaching safepoint: 82429 ns, Cleanup: 112026 ns, At safepoint: 10659 ns, "
                + "Total: 205114 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.SET_NOTIFY_JVMTI_EVENTS_MODE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(433 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(82429, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(10659, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
        assertEquals(93, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTimeJdk11() {
        String logLine = "[2021-09-14T11:40:53.379-0500][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][info][safepoint     ] Total time for which "
                + "application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(684934853379L - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testTimeUptimeJdk11() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testTriggerGcHeapInspection() throws IOException {
        File testFile = TestUtil.getFile("dataset248.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_SAFEPOINT),
                JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SAFEPOINT);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + " not indentified as unified.");
    }

    @Test
    void testUptimeJdk11() {
        String logLine = "[144.035s][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144.036s][info][safepoint     ] Leaving safepoint region[144.036s][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testUptimeJdk17() {
        String logLine = "[0.061s][info][safepoint   ] Safepoint \"GenCollectForAllocation\", Time since last: "
                + "24548411 ns, Reaching safepoint: 69521 ns, At safepoint: 779732 ns, Total: 849253 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.GEN_COLLECT_FOR_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(61, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(69521, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(779732, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testUptimeMillisJdk11() {
        String logLine = "[144035ms][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation"
                + "[144036ms][info][safepoint     ] Leaving safepoint region[144036ms][info][safepoint     ] Total "
                + "time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: "
                + "0.0002048 seconds";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.COLLECT_FOR_METADATA_ALLOCATION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(144035, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(204800, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(454600, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testWithSpacesAtEnd() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] "
                + "Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time "
                + "for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 "
                + "seconds   ";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
    }

    @Test
    void testXMarkEndJdk21() {
        String logLine = "[2023-11-16T07:13:24.719-0500] Safepoint \"XMarkEnd\", Time since last: 9298521 ns, Reaching "
                + "safepoint: 64558 ns, Cleanup: 4188 ns, At safepoint: 19474 ns, Total: 88220 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.X_MARK_END, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(753434004719L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(64558, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(19474, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testXMarkStartJdk21() {
        String logLine = "[2023-11-16T07:13:24.709-0500] Safepoint \"XMarkStart\", Time since last: 104262770 ns, "
                + "Reaching safepoint: 313632 ns, Cleanup: 16571 ns, At safepoint: 21875 ns, Total: 352078 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.X_MARK_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(753434004709L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(313632, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(21875, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testXRelocateStartJdk21() {
        String logLine = "[2023-11-16T07:13:24.721-0500] Safepoint \"XRelocateStart\", Time since last: 1668402 ns, "
                + "Reaching safepoint: 605 ns, Cleanup: 580 ns, At safepoint: 12417 ns, Total: 13602 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.X_RELOCATE_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(753434004721L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(605, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(12417, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZMarkEndJdk17() {
        String logLine = "[0.129s] Safepoint \"ZMarkEnd\", Time since last: 4051145 ns, Reaching safepoint: 79105 ns, "
                + "At safepoint: 16082 ns, Total: 95187 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_END, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(129, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(79105, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(16082, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZMarkEndOldJdk21() {
        String logLine = "[0.213s][info][safepoint   ] Safepoint \"ZMarkEndOld\", Time since last: 4611261 ns, "
                + "Reaching safepoint: 63490 ns, Cleanup: 1818 ns, At safepoint: 13075 ns, Total: 78383 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_END_OLD, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(213, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(63490, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(13075, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZMarkEndYoungJdk21() {
        String logLine = "[0.100s][info][safepoint   ] Safepoint \"ZMarkEndYoung\", Time since last: 2606548 ns, "
                + "Reaching safepoint: 65220 ns, Cleanup: 1425 ns, At safepoint: 16873 ns, Total: 83518 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_END_YOUNG, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(100, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(65220, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(16873, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZMarkStartJdk17() {
        String logLine = "[0.124s][info][safepoint   ] Safepoint \"ZMarkStart\", Time since last: 103609844 ns, "
                + "Reaching safepoint: 99888 ns, At safepoint: 30677 ns, Total: 130565 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(124, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(99888, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(30677, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZMarkStartYoungJdk21() {
        String logLine = "[3.127s][info][safepoint   ] Safepoint \"ZMarkStartYoung\", Time since last: 169417 ns, "
                + "Reaching safepoint: 415 ns, Cleanup: 300 ns, At safepoint: 10118 ns, Total: 10833 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_MARK_START_YOUNG, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(3127, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(415, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(10118, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZRelocateStartJdk17() {
        String logLine = "[0.132s] Safepoint \"ZRelocateStart\", Time since last: 1366138 ns, Reaching safepoint: "
                + "138018 ns, At safepoint: 15653 ns, Total: 153671 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_RELOCATE_START, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(132, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(138018, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(15653, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZRelocateStartOldJdk21() {
        String logLine = "[0.228s][info][safepoint   ] Safepoint \"ZRelocateStartOld\", Time since last: 14731632 ns, "
                + "Reaching safepoint: 64726 ns, Cleanup: 3517 ns, At safepoint: 14142 ns, Total: 82385 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_RELOCATE_START_OLD, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(228, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(64726, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(14142, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }

    @Test
    void testZRelocateStartYoungJdk21() {
        String logLine = "[3.117s][info][safepoint   ] Safepoint \"ZRelocateStartYoung\", Time since last: 1794436 ns, "
                + "Reaching safepoint: 406 ns, Cleanup: 312 ns, At safepoint: 7040 ns, Total: 7758 ns";
        assertTrue(UnifiedSafepointEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        UnifiedSafepointEvent event = new UnifiedSafepointEvent(logLine);
        assertEquals(Trigger.Z_RELOCATE_START_YOUNG, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals(3117, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(406, event.getTimeToStopThreads(), "Time to stop threads not parsed correctly.");
        assertEquals(7040, event.getTimeThreadsStopped(), "Time threads stopped not parsed correctly.");
    }
}
