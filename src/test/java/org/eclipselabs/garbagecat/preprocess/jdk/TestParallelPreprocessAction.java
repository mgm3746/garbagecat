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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestParallelPreprocessAction {

    @Test
    void testLogLineAdaptiveAvgSurvivedPaddedAvg() {
        String logLine = "  avg_survived_padded_avg: 99027432.000000  avg_promoted_padded_avg: 6855161.000000  "
                + "avg_pretenured_padded_avg: 0.000000  tenuring_thresh: 15  target_size: 99090432";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineAdaptiveSizePolicy() {
        String logLine = "AdaptiveSizePolicy::survivor space sizes: collection: 817 (101187584, 102236160) -> "
                + "(101187584, 99090432)";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineAdaptiveSizeStart() {
        String logLine = "AdaptiveSizeStart: 48509.477 collection: 817";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineAdaptiveSizeStop() {
        String logLine = "AdaptiveSizeStop: collection: 817";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineBeginningParallelScavenge() {
        String logLine = "10.392: [GC";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineClassUnloading() {
        String logLine = "65.343: [Full GC[Unloading class $Proxy111]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("65.343: [Full GC", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDesiredEdenSize() {
        String logLine = "5561122816 desired_eden_size: 5561122816";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineDesiredSurvivorSize() {
        String logLine = "Desired survivor size 99090432 bytes, new threshold 15 (max 15)";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineEndFull() {
        String logLine = " [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] "
                + "387470K->387085K(1518912K) [PSPermGen: 115215K->115215K(238912K)], 1.5692400 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndTimes() {
        String logLine = ", 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineGcTimeLimitExceeding() {
        String logLine = "3924.453: [Full GC [PSYoungGen: 419840K->418436K(839680K)] [PSOldGen: "
                + "5008601K->5008601K(5033984K)] 5428441K->5427038K(5873664K) [PSPermGen: "
                + "193278K->193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineGcTimeLimitExceedLineExceed() {
        String logLine = "3743.645: [Full GC [PSYoungGen: 419840K->415020K(839680K)] [PSOldGen: "
                + "5008922K->5008922K(5033984K)] 5428762K->5423942K(5873664K) [PSPermGen: "
                + "193275K->193275K(262144K)]      GC time would exceed GCTimeLimit of 98%";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineGcTimeLimitExceedMoreSpaces() {
        String logLine = "52843.722: [Full GC [PSYoungGen: 109696K->95191K(184960K)] [ParOldGen: "
                + "1307240K->1307182K(1310720K)] 1416936K->1402374K(1495680K) [PSPermGen: "
                + "113631K->113623K(196608K)]\tGC time is exceeding GCTimeLimit of 98%";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineGcTimeLimitExceedWithDatestamp() {
        String logLine = "2017-06-02T11:11:29.244+0530: 165944.630: [Full GC [PSYoungGen: 230400K->217423K(268800K)] "
                + "[PSOldGen: 1789951K->1789951K(1789952K)] 2020351K->2007375K(2058752K) "
                + "[PSPermGen: 188837K->188837K(524288K)]      GC time would exceed GCTimeLimit of 98%";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineParallelOldAdaptiveSizePolicyBegin() {
        String logLine = "2021-04-09T07:19:43.692-0400: 74865.313: [Full GC (Ergonomics) AdaptiveSizeStart: 74869.165 "
                + "collection: 1223 ";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("2021-04-09T07:19:43.692-0400: 74865.313: [Full GC (Ergonomics) ", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParallelOldAdaptiveSizePolicyEnd() {
        String logLine = "[PSYoungGen: 115174K->0K(5651968K)] [ParOldGen: 5758620K->1232841K(5767168K)] "
                + "5873794K->1232841K(11419136K), [Metaspace: 214025K->213385K(1257472K)], 3.8546414 secs] "
                + "[Times: user=10.71 sys=0.72, real=3.86 secs]";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    @Test
    void testLogLineParallelScavengeAdaptiveSizePolicy() {
        String logLine = "2021-04-09T00:00:27.785-0400: 48509.406: [GC (Allocation Failure) AdaptiveSizePolicy::"
                + "update_averages:  survived: 51216232  promoted: 106256  overflow: false";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("2021-04-09T00:00:27.785-0400: 48509.406: [GC (Allocation Failure) ", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParallelScavengeAdaptiveSizePolicyOverflowTrue() {
        String logLine = "2021-04-09T00:30:10.485-0400: 50292.105: [GC (Allocation Failure) AdaptiveSizePolicy::"
                + "update_averages:  survived: 76006984  promoted: 7100224  overflow: true";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("2021-04-09T00:30:10.485-0400: 50292.105: [GC (Allocation Failure) ", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParallelScavengeTriggerGcLockerAdaptiveSizePolicy() {
        String logLine = "2021-04-09T00:14:14.347-0400: 49335.968: [GC (GCLocker Initiated GC) AdaptiveSizePolicy::"
                + "update_averages:  survived: 56481056  promoted: 722928  overflow: false";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("2021-04-09T00:14:14.347-0400: 49335.968: [GC (GCLocker Initiated GC) ", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLinePsAdaptiveSizePolicy() {
        String logLine = "PSAdaptiveSizePolicy::compute_eden_space_size: costs minor_time: 0.000881 major_cost: "
                + "0.020394 mutator_cost: 0.978725 throughput_goal: 0.990000 live_space: 542122880 free_space: "
                + "11428429824 old_eden_size:";
        assertTrue(ParallelPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".");
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with logging mixed across multiple lines.
     * 
     * @throws IOException
     */
    @Test
    void testParallelSerialOldAcrossMultipleLinesMixedGcTimeLimitLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset132.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL.getKey()),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED.getKey()),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
    }

    /**
     * Test preprocessing -XX:+PrintAdaptiveSizePolicy logging.
     * 
     * @throws IOException
     */
    @Test
    void testPreprocessingPrintAdaptiveSizePolicy() throws IOException {
        File testFile = TestUtil.getFile("dataset209.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
    }

    /**
     * Test preprocessing <code>PrintTenuringDistributionPreprocessAction</code> with underlying
     * <code>ParallelScavengeEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitParallelScavengeEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset30.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_TENURING_DISTRIBUTION.getKey()),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_TENURING_DISTRIBUTION + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitParallelSerialOldEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset9.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED.getKey()),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>UnloadingClassPreprocessAction</code> with underlying
     * <code>ParallelSerialOldEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testUnloadingClassPreprocessActionParallelSerialOldEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset24.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CLASS_UNLOADING),
                JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " not identified.");
    }
}
