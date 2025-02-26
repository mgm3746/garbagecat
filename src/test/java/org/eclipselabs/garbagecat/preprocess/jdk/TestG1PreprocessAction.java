/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction.PreprocessEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestG1PreprocessAction {

    @Test
    void testAgeThreshold() {
        String logLine = "      [Age Threshold: 3]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testAvg() {
        String logLine = "       Avg:   1.1, Min:   0.0, Max:   1.5]   0.0, Min:   0.0, Max:   0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testBeginningG1FullMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "73.164: [Full GC (System.gc()) Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("73.164: [Full GC (System.gc())", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBeginningYoungConcurrent() {
        String logLine = "2016-02-16T01:05:36.945-0500: 16233.809: [GC pause (young)2016-02-16T01:05:37.046-0500: "
                + "16233.910: [GC concurrent-root-region-scan-end, 0.5802520 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testChooseCSet() {
        String logLine = "      [Choose CSet: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testClearCt() {
        String logLine = "   [Clear CT: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCodeRootFixup() {
        String logLine = "   [Code Root Fixup: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCodeRootMarking() {
        String logLine = "      [Code Root Marking (ms): Min: 0.1, Avg: 1.8, Max: 3.7, Diff: 3.7, Sum: 7.2]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCodeRootMigration() {
        String logLine = "   [Code Root Migration: 0.8 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCodeRootPurge() {
        String logLine = "   [Code Root Purge: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCodeRootScanning() {
        String logLine = "      [Code Root Scanning (ms): Min: 0.0, Avg: 0.2, Max: 0.4, Diff: 0.4, Sum: 0.8]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testCompleteCsetMarking() {
        String logLine = "   [Complete CSet Marking:   0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testConcurrent() {
        String logLine = "27744.494: [GC concurrent-mark-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testConcurrentCleanupEndWithDatestamp() {
        String logLine = "2016-02-11T18:15:35.431-0500: 14974.501: [GC concurrent-cleanup-end, 0.0033880 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testConcurrentDatestampDatestamp() {
        String logLine = "2021-10-27T10:13:37.450-0400: 2021-10-27T10:13:37.450-0400: "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-27T10:13:37.450-0400: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampDoubleColon() {
        String logLine = "2021-10-27T10:50:59.400-04002021-10-27T10:50:59.400-0400: : "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-27T10:50:59.400-0400: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampDoubleColonTimestampTimestamp() {
        String logLine = "2021-10-27T08:03:11.806-04002021-10-27T08:03:11.806-0400: : 0.2230.223: : "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-27T08:03:11.806-0400: 0.223: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampNoColon() {
        String logLine = "2021-10-26T09:58:12.120-0400: 2021-10-26T09:58:12.120-0400 "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-26T09:58:12.120-0400: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampTimestamp() {
        String logLine = "2021-10-27T08:03:11.757-0400: 2021-10-27T08:03:11.757-0400: 0.174: "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-27T08:03:11.757-0400: 0.174: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampTimestampDoubleColon() {
        String logLine = "2021-10-27T12:32:11.621-0400: 2021-10-27T12:32:11.621-04000.210: : "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2021-10-27T12:32:11.621-0400: 0.210: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampTimestampDoubleColonTimestamp() {
        String logLine = "2022-10-31T21:51:10.608+0800: 2022-10-31T21:51:10.608+0800494958.042: : 494958.042: "
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-10-31T21:51:10.608+0800: 494958.042: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampDatestampTimestampTimestamp() {
        String logLine = "2022-10-31T23:25:12.197+0800: 2022-10-31T23:25:12.197+0800: 500599.630: 500599.630"
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-10-31T23:25:12.197+0800: 500599.630: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampTimestampDatestamp() {
        String logLine = "2022-10-31T17:20:02.747+0800: 478690.181: 2022-10-31T17:20:02.747+0800: "
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-10-31T17:20:02.747+0800: 478690.181: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampTimestampDatestampDoubleColonTimestamp() {
        String logLine = "2022-11-01T20:32:55.433+0800: 576662.8672022-11-01T20:32:55.433+0800: : 576662.867: "
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-11-01T20:32:55.433+0800: 576662.867: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampTimestampDatestampNoColon() {
        String logLine = "2022-10-31T21:13:16.044+0800: 492683.478: 2022-10-31T21:13:16.044+0800"
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-10-31T21:13:16.044+0800: 492683.478: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampTimestampDatestampTimestamp() {
        String logLine = "2022-10-31T22:59:37.717+0800: 499065.151: 2022-10-31T22:59:37.717+0800: 499065.151: "
                + "[GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2022-10-31T22:59:37.717+0800: 499065.151: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentDatestampTimestampNoColonDatestampDoubleColon() {
        String logLine = "2022-11-02T04:25:44.738+0800: 605031.3382022-11-02T04:25:44.738+0800: : "
                + "[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("2022-11-02T04:25:44.738+0800: 605031.338: [GC concurrent-root-region-scan-start]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMissingDecorator() {
        // Decorator is intermingled in previous log line. Throw away the logging, as it's not essential for analysis,
        // not worth the trouble to untangle it.
        String priorLogLine = "2022-10-30T08:28:23.839-0400: 2022-10-30T08:28:23.839-0400: 0.408: 0.408Total time for "
                + "which application threads were stopped: 0.0078201 seconds, Stopping threads took: 0.0000168 seconds";
        LogEvent priorLogEvent = JdkUtil.parseLogLine(priorLogLine, null, CollectorFamily.UNKNOWN);
        String logLine = ": [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, priorLogEvent),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMixedYoungPauseEndNoToSpaceExhausted() {
        String logLine = "2132.962: [GC concurrent-root-region-scan-end, 0.0001111 secs], 0.1083307 secs]"
                + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B Heap: 3035.6M(3072.0M)->3035.6M(3072.0M)] "
                + "[Times: user=0.09 sys=0.00, real=0.11 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(
                ", 0.1083307 secs]" + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B "
                        + "Heap: 3035.6M(3072.0M)->3035.6M(3072.0M)] [Times: user=0.09 sys=0.00, real=0.11 secs]"
                        + Constants.LINE_SEPARATOR + "2132.962: [GC concurrent-root-region-scan-end, 0.0001111 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMixedYoungPauseToSpaceExhaustedEnd() {
        String logLine = "537.142: [GC concurrent-root-region-scan-end, 0.0189841 secs] (to-space exhausted), "
                + "0.3314995 secs][Eden: 0.0B(151.0M)->0.0B(153.0M) Survivors: 2048.0K->0.0B Heap: "
                + "3038.7M(3072.0M)->3038.7M(3072.0M)] [Times: user=0.20 sys=0.00, real=0.33 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(
                " (to-space exhausted), 0.3314995 secs][Eden: 0.0B(151.0M)->0.0B(153.0M) "
                        + "Survivors: 2048.0K->0.0B Heap: 3038.7M(3072.0M)->3038.7M(3072.0M)] "
                        + "[Times: user=0.20 sys=0.00, real=0.33 secs]" + Constants.LINE_SEPARATOR
                        + "537.142: [GC concurrent-root-region-scan-end, 0.0189841 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    /**
     * Test for G1_CONCURRENT string deduplication.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testConcurrentStringDeduplicatonLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset64.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentTimestampTimestamp() {
        String logLine = "0.218: 0.218[GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("0.218: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentTimestampTimestampDoubleColon() {
        String logLine = "0.2270.227: : [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("0.227: [GC concurrent-root-region-scan-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentWithDatestamp() {
        String logLine = "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testDropped() {
        String logLine = "      [Dropped: 0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testDuplicated() {
        String logLine = "      [Deduplicated:         3304( 49.2%)    197.2K( 37.5%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testEndOfFullCollection() {
        String logLine = " 1831M->1213M(5120M), 5.1353878 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testEntries() {
        String logLine = "      [Entries: 26334, Load: 160.7%, Cached: 0, Added: 26334, Removed: 0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testErgonomicsCsetConstruction() {
        String logLine = "4295945.119: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 122401, "
                + "predicted base time: 65.52 ms, remaining time: 134.48 ms, target pause time: 200.00 ms]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanning() {
        String logLine = "      [Ext Root Scanning (ms): Min: 2.7, Avg: 3.0, Max: 3.5, Diff: 0.8, Sum: 18.1]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("[Ext Root Scanning (ms): 3.5]", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanningEarlyImplementation() {
        String logLine = "      [Ext Root Scanning (ms):  27,4  33,8  26,4  24,8  28,6  19,5  28,4  8,9  18,9  31,9  "
                + "29,6  28,0  28,1";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        // Ignore this old pattern
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanningSingleNumber() {
        String logLine = "      [Ext Root Scanning (ms):  28.7]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, null, entangledLogLines, context, null);
        assertEquals("[Ext Root Scanning (ms): 28.7]", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFreeCSet() {
        String logLine = "      [Free CSet: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testFullCombinedConcurrentRootRegionScanEndWithDuration() {
        String logLine = "93.315: [Full GC (Metadata GC Threshold) 93.315: "
                + "[GC concurrent-root-region-scan-end, 0.0003872 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testFullGC() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testFullGcClassHistogram() {
        String logLine = "2021-10-07T10:05:34.135+0100: 69302.241: [Full GC (Heap Dump Initiated GC) "
                + "2021-10-07T10:05:34.135+0100: 69302.241: [Class Histogram (before full gc):";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testFullGcMixedConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset116.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_STOPPED_TIME.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis not identified.");
    }

    @Test
    void testFullGcNoTrigger() {
        String logLine = "27999.141: [Full GC 18G->4153M(26G), 10.1760410 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testFullGcPrintClassHistogram() {
        String logLine = "49689.217: [Full GC49689.217: [Class Histogram (before full gc):";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("49689.217: [Full GC49689.217: [Class Histogram (before full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testFullMixedConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset134.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_FULL_GC_SERIAL),
                JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_CONCURRENT),
                JdkUtil.EventType.G1_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis not identified.");
    }

    @Test
    void testFullMixedConcurrentMiddle() {
        String logLine = "35420.674: [Full GC (Allocation Failure) 35420.734: "
                + "[GC concurrent-mark-start]3035M->3030M(3072M), 21.7552521 secs]"
                + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B Heap: 3035.5M(3072.0M)->3030.4M(3072.0M)], "
                + "[Metaspace: 93308K->93308K(352256K)] [Times: user=16.39 sys=0.04, real=21.75 secs]";
        String nextLogLine = "2132.960: [GC pause (G1 Evacuation Pause) (young)2132.962: "
                + "[GC concurrent-root-region-scan-start]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(
                "35420.674: [Full GC (Allocation Failure) 3035M->3030M(3072M), 21.7552521 secs]"
                        + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B"
                        + " Heap: 3035.5M(3072.0M)->3030.4M(3072.0M)], [Metaspace: 93308K->93308K(352256K)] "
                        + "[Times: user=16.39 sys=0.04, real=21.75 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFullMixedConcurrentNoSpaceAfterTrigger() {
        String logLine = "2017-02-27T02:55:32.523+0300: 35911.404: [Full GC (Allocation Failure)"
                + "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-root-region-scan-end, 0.0127300 secs]";
        String nextLogLine = "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2017-02-27T02:55:32.523+0300: 35911.404: [Full GC (Allocation Failure)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testG1Cleanup() {
        String logLine = "1.515: [GC cleanup 165M->165M(110G), 0.0028925 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    /**
     * Test to ensure it does not falsely erroneously preprocess.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1CleanupG1InitialMark() throws IOException {
        File testFile = TestUtil.getFile("dataset62.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_CLEANUP),
                JdkUtil.EventType.G1_CLEANUP.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + " event not identified.");
    }

    /**
     * Test preprocessing G1 concurrent missing timestamp.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1Concurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset76.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    @Test
    void testG1ConcurrentWithDatestamp() {
        String logLine = "2016-02-09T06:17:15.377-0500: 27744.139: [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1ErgonomicsWithDatestamp() {
        String logLine = "2016-02-11T17:26:43.599-0500: 12042.669: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target "
                + "pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1EvacuationPause() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1ExtRootScanning() throws IOException {
        File testFile = TestUtil.getFile("dataset281.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    /**
     * Test preprocessing G1_FULL.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1Full() throws IOException {
        File testFile = TestUtil.getFile("dataset79.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test for G1_FULL across 3 lines with details.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1Full3Lines() throws IOException {
        File testFile = TestUtil.getFile("dataset65.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED.getKey()),
                Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_PRINT_GC_CAUSE_MISSING.getKey()),
                Analysis.WARN_PRINT_GC_CAUSE_MISSING + " analysis incorrectly identified.");
    }

    @Test
    void testG1FullCombinedConcurrentRootRegionScanStart() {
        String logLine = "88.123: [Full GC (Metadata GC Threshold) 88.123: [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1FullCombinedConcurrentRootRegionScanStartMissingTimestamp() {
        String logLine = "298.027: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1FullDatestamp() {
        String logLine = "2016-10-31T14:09:15.030-0700: 49689.217: [Full GC"
                + "2016-10-31T14:09:15.030-0700: 49689.217: [Class Histogram (before full gc):";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    /**
     * Test for G1_Full identification without -XX:+PrintGCDetails are.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1FullIdentificationWithoutPrintGcDetails() throws IOException {
        File testFile = TestUtil.getFile("dataset247.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.HEADER_VM_INFO),
                "Log line not recognized as " + JdkUtil.EventType.HEADER_VM_INFO.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertFalse(jvmRun.getEventTypes().contains(EventType.VERBOSE_GC_OLD),
                JdkUtil.EventType.VERBOSE_GC_OLD.toString() + " event identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis not identified.");
    }

    /**
     * Test preprocessing G1_FULL triggered by TRIGGER_JVMTI_FORCED_GARBAGE_COLLECTION.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1FullJvmTiForcedGarbageCollectionTrigger() throws IOException {
        File testFile = TestUtil.getFile("dataset75.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_JVMTI.getKey()),
                GcTrigger.JVMTI_FORCED_GARBAGE_COLLECTION.toString() + " trigger not identified.");
    }

    /**
     * Test preprocessing G1_FULL triggered by LAST_DITCH_COLLECTION.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1FullLastDitchCollectionTrigger() throws IOException {
        File testFile = TestUtil.getFile("dataset74.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE.getKey()),
                GcTrigger.LAST_DITCH_COLLECTION.toString() + " trigger not identified.");
    }

    @Test
    void testG1FullTriggerJvmTiForcedGarbageCollection() {
        String logLine = "102.621: [Full GC (JvmtiEnv ForceGarbageCollection)  1124M->1118M(5120M), 3.8954775 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1FullTriggerLastDitchCollection2SpacesAfterTrigger() {
        String logLine = "98.150: [Full GC (Last ditch collection)  1196M->1118M(5120M), 4.4628626 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1FullTriggerMetaDataGcThreshold() {
        String logLine = "4708.816: [Full GC (Metadata GC Threshold)  801M->801M(5120M), 3.5048336 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1FullTriggerMetaDataGcThresholdMixedConcurrentRootRegionScanEnd() {
        String logLine = "290.944: [Full GC (Metadata GC Threshold) 290.944: "
                + "[GC concurrent-root-region-scan-end, 0.0003793 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    /**
     * Test preprocessing G1_FULL with CLASS_HISTOGRAM.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1FullWithPrintClassHistogram() throws IOException {
        File testFile = TestUtil.getFile("dataset93.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CLASS_HISTOGRAM.getKey()),
                Analysis.WARN_CLASS_HISTOGRAM + " analysis not identified.");
        // G1_FULL is caused by CLASS_HISTOGRAM
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    @Test
    void testG1MixedPauseMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "2017-06-28T18:24:40.453-0400: 12289.351: [GC pause (G1 Evacuation Pause) (mixed)"
                + "Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2017-06-28T18:24:40.453-0400: 12289.351: [GC pause (G1 Evacuation Pause) (mixed)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_CLEANUP.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionCleanupLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset40.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CLEANUP),
                "Log line not recognized as " + JdkUtil.EventType.G1_CLEANUP.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionConcurrentLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset44.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1EvacuationPauseLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset34.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_FULL_GC.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1FullGCLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset36.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getGcTriggers().contains(GcTrigger.SYSTEM_GC),
                GcTrigger.SYSTEM_GC + " trigger not identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1.getKey()),
                Analysis.ERROR_EXPLICIT_GC_SERIAL_G1 + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1InitialMarkWithCodeRootLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset43.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungInitialMarkWithG1ErgonomicsLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset49.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungInitialMarkWithTriggerAndG1ErgonomicsLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset53.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset32.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with TO_SPACE_EXHAUSTED with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseTriggerToSpaceExhaustedWithG1ErgonomicsLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset50.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset48.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging2() throws IOException {
        File testFile = TestUtil.getFile("dataset51.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging3() throws IOException {
        File testFile = TestUtil.getFile("dataset52.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging4() throws IOException {
        File testFile = TestUtil.getFile("dataset54.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging5() throws IOException {
        File testFile = TestUtil.getFile("dataset55.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging6() throws IOException {
        File testFile = TestUtil.getFile("dataset57.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_CONCURRENT),
                JdkUtil.EventType.G1_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with GCLOCKER_INITIATED_GC trigger.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithGCLockerInitiatedGCLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset35.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertEquals((long) 1, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PARALLELISM_INVERTED.getKey()),
                Analysis.WARN_PARALLELISM_INVERTED + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_MIXED_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionMixedPauseLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset39.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_MIXED_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_MIXED_PAUSE with no trigger.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionMixedPauseNoTriggerLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset46.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_MIXED_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_REMARK.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionRemarkLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset38.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_REMARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_REMARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with TO_SPACE_EXHAUSTED trigger.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionToSpaceExhaustedLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset45.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionYoungConcurrentLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset47.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1PreprocessActionYoungInitialMarkLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset37.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    @Test
    void testG1RemarkDatestamps() {
        String logLine = "2016-03-14T16:06:13.991-0700: 5.745: [GC remark 2016-03-14T16:06:13.991-0700: 5.746: "
                + "[Finalize Marking, 0.0068506 secs] 2016-03-14T16:06:13.998-0700: 5.752: [GC ref-proc, "
                + "0.0014064 secs] 2016-03-14T16:06:14.000-0700: 5.754: [Unloading, 0.0057674 secs], 0.0157938 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsAfterRsSummaryHeading() {
        String logLine = "After GC RS summary";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCoarsenings() {
        String logLine = "  Did 0 coarsenings.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCoarseningsDigits3() {
        String logLine = "  Did 239 coarsenings.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRoots() {
        String logLine = "    205 code roots represented.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRootsDigits5() {
        String logLine = "    12153 code roots represented.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRootsFree() {
        String logLine = "            0 (  0.0%) elements by 493 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRootsHumongous() {
        String logLine = "            0 (  0.0%) elements by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRootsOld() {
        String logLine = "          242 ( 97.2%) elements by 15 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCodeRootsYoung() {
        String logLine = "            7 (  2.8%) elements by 4 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCompletedBuffersHeading() {
        String logLine = "  Of 2736 completed buffers:";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsCompletedBuffersHeadingDigits3() {
        String logLine = "  Of 170 completed buffers:";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsHeapTotal() {
        String logLine = "  Total heap region code root sets sizes = 13K.  Max = 3336B.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsMutatorThreads() {
        String logLine = "            0 (  0.0%) by mutator threads.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCards() {
        String logLine = "    0 occupied cards represented.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsDigits9() {
        String logLine = "    122457800 occupied cards represented.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsFree() {
        String logLine = "            0 (  0.0%) entries by 487 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsHumongous() {
        String logLine = "            0 (  0.0%) entries by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsOld() {
        String logLine = "            0 (  0.0%) entries by 0 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsOldDigits9() {
        String logLine = "     122327000 (100.0%) entries by 1171 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsOccupiedCardsYoung() {
        String logLine = "            0 (  0.0%) entries by 25 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsProcessedCards() {
        String logLine = "  Processed 0 cards";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRecentRefinementStats() {
        String logLine = " Recent concurrent refinement statistics";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRegionLargestCodeRoots() {
        String logLine = "    Region with largest amount of code roots = 511:(E)[0x00000000dff00000,"
                + "0x00000000e0000000,0x00000000e0000000], size = 3336B, num_elems = 136.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRegionLargestRset() {
        String logLine = "    Region with largest rem set = 511:(E)[0x00000000dff00000,0x00000000e0000000,"
                + "0x00000000e0000000], size = 6336B, occupied = 0B.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetFree() {
        String logLine = "        1434K ( 94.8%) by 487 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetFreeMDigits4() {
        String logLine = "          36M ( 94.9%) by 3709 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetHeading() {
        String logLine = " Current rem set statistics";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetHumongous() {
        String logLine = "           0B (  0.0%) by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetOld() {
        String logLine = "           0B (  0.0%) by 0 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetTotalKMaxB() {
        String logLine = "  Total per region rem sets sizes = 1513K. Max = 6336B.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetTotalKMaxK() {
        String logLine = "  Total per region rem sets sizes = 1606K. Max = 14K.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetTotalMMaxK() {
        String logLine = "  Total per region rem sets sizes = 38M. Max = 25K.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRSetYoung() {
        String logLine = "          78K (  5.2%) by 25 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRsThreads() {
        String logLine = "         2736 ( 94.3%) by concurrent RS threads.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRsThreadsPercent100() {
        String logLine = "          170 (100.0%) by concurrent RS threads.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRsThreadTimesHeading() {
        String logLine = "  Concurrent RS threads times (s)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsRsThreadTimesOutput() {
        String logLine = "          0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsSamplingThreadTimesHeading() {
        String logLine = "  Concurrent sampling threads times (s)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsSamplingThreadTimesOutput() {
        String logLine = "          0.00";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsSamplingThreadTimesOutputDigits2() {
        String logLine = "         13.33";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1SummarizeRSetStatsStaticStructures() {
        String logLine = "   Static structures = 64K, free_lists = 0B.";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1YoungConcurrentTriggerG1HumongousAllocation() {
        String logLine = "2017-06-22T13:55:45.753+0530: 71574.499: [GC pause (G1 Humongous Allocation) (young)"
                + "2017-06-22T13:55:45.771+0530: 71574.517: [GC concurrent-root-region-scan-end, 0.0181265 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1YoungInitialMark() throws IOException {
        File testFile = TestUtil.getFile("dataset127.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.EventType.G1_YOUNG_INITIAL_MARK.toString() + " event not identified.");
    }

    @Test
    void testG1YoungInitialMarkMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "1.738: [GC pause (Metadata GC Threshold) (young) (initial-mark)Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("1.738: [GC pause (Metadata GC Threshold) (young) (initial-mark)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testG1YoungInitialMarkTriggerGcLockerInitiatedGc() {
        String logLine = "6896.482: [GC pause (GCLocker Initiated GC) (young) (initial-mark), 0.0525160 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1YoungInitialMarkTriggerMetaGcThreshold() {
        String logLine = "87.830: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.2932700 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testG1YoungInitialMarkTriggerSystemGc() {
        String logLine = "2020-02-26T17:18:26.505+0000: 130.241: [GC pause (System.gc()) (young) (initial-mark)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    /**
     * Test preprocessing G1_YOUNG_PAUSE with double trigger and Evacuation Failure details.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1YoungPauseEvacuationFailure() throws IOException {
        File testFile = TestUtil.getFile("dataset100.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    @Test
    void testG1YoungPauseMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "0.449: [GC pause (G1 Evacuation Pause) (young)Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("0.449: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    /**
     * Test preprocessing G1_YOUNG_PAUSE with no size details (whole number units).
     * 
     * @throws IOException
     * 
     */
    @Test
    void testG1YoungPauseNoSizeDetails() throws IOException {
        File testFile = TestUtil.getFile("dataset97.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testGcConcurrentRootRegionScanEndMissingTimestamp() {
        String logLine = "[GC concurrent-root-region-scan-end, 0.6380480 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGCLockerInitiatedGC() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorker() {
        String logLine = "      [GC Worker (ms):  387,2  387,4  386,2  385,9  386,1  386,2  386,9  386,4  386,4  "
                + "386,8  386,1  385,2  386,1";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerEnd() {
        String logLine = "      [GC Worker End Time (ms):  810.1  810.2  810.1  810.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerEndJdk8() {
        String logLine = "      [GC Worker End (ms): Min: 2204.4, Avg: 2204.4, Max: 2204.4, Diff: 0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerOther() {
        String logLine = "      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerStart() {
        String logLine = "      [GC Worker Start Time (ms):  807.5  807.8  807.8  810.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerStartJdk8() {
        String logLine = "      [GC Worker Start (ms): Min: 2191.9, Avg: 2191.9, Max: 2191.9, Diff: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerStartWithCommas() {
        String logLine = "      [GC Worker Start (ms): Min: 6349,9, Avg: 6353,8, Max: 6355,9, Diff: 6,0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testGcWorkerTotal() {
        String logLine = "      [GC Worker Total (ms): Min: 12.5, Avg: 12.5, Max: 12.6, Diff: 0.1, Sum: 75.3]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testHashed() {
        String logLine = "         [Hashed:            3088( 30.5%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testHumongousReclaim() {
        String logLine = "      [Humongous Reclaim: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testHumongousRegister() {
        String logLine = "      [Humongous Register: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testInspected() {
        String logLine = "      [Inspected:           10116]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testInvertedSerialism() throws IOException {
        File testFile = TestUtil.getFile("dataset251.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertEquals((long) 6, jvmRun.getSerialCount(), "Serial event count not correct.");
        assertEquals((long) 3, jvmRun.getInvertedSerialismCount(), "Inverted serialism event count not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_SERIALISM_INVERTED.getKey()),
                Analysis.WARN_SERIALISM_INVERTED + " analysis not identified.");
        LogEvent event = jvmRun.getWorstInvertedSerialismEvent();
        assertEquals(400000, event.getTimestamp(), "Worst inverted serialism event timestamp not correct.");
    }

    @Test
    void testKnown() {
        String logLine = "         [Known:             3404( 33.6%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLastExec() {
        String logLine = "   [Last Exec: 0.0118158 secs, Idle: 0.9330710 secs, Blocked: 0/0.0000000 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogEvacuationFailure() {
        String logLine = "      [Evacuation Failure: 2381.8 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogMixed() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogMixedNoTrigger() {
        String logLine = "3082.652: [GC pause (mixed), 0.0762060 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogRemark() {
        String logLine = "2971.469: [GC remark 2972.470: [GC ref-proc, 0.1656600 secs], 0.2274544 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMarkStackScanning() {
        String logLine = "      [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMemoryUsage() {
        String logLine = "      [Memory Usage: 745.2K]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMiddleG1FullMixedG1SummarizeRSetStatsAfterRsSummary() {
        String logLine = " 390M->119M(512M)After GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(" 390M->119M(512M)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMiddleG1FullWithSizeInformation() {
        String logLine = " 1831M->1213M(5120M), 5.1353878 secs]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        G1PreprocessAction action = new G1PreprocessAction(null, logLine, null, null, context, null);
        String preprocessedLine = " 1831M->1213M(5120M), 5.1353878 secs]";
        assertEquals(preprocessedLine, action.getLogEntry(), "Preprocessing failed.");
    }

    @Test
    void testMiddleInitialMark() {
        String logLine = " (initial-mark), 0.12895600 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMixedHumongousAllocationToSpaceExhausted() {
        String logLine = "2017-06-22T12:25:26.515+0530: 66155.261: [GC pause (G1 Humongous Allocation) (mixed) "
                + "(to-space exhausted), 0.2466797 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMixedWithG1Ergonomics() {
        String logLine = "2016-02-11T16:06:59.987-0500: 7259.058: [GC pause (mixed) 7259.058: [G1Ergonomics (CSet "
                + "Construction) start choosing CSet, _pending_cards: 273214, predicted base time: 74.01 ms, "
                + "remaining time: 425.99 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMixedWithG1ErgonomicsAndDateStamps() {
        String priorLogLine = "2021-12-15T12:19:59.315-0300: 5612.994: Total time for which application threads were "
                + "stopped: 0.0049307 seconds, Stopping threads took: 0.0009825 seconds";
        LogEvent priorLogEvent = JdkUtil.parseLogLine(priorLogLine, null, CollectorFamily.UNKNOWN);
        String logLine = "2021-12-15T12:19:59.319-0300: 5612.998: [GC pause (GCLocker Initiated GC) (mixed) 5612.999: "
                + "[G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 434455, predicted base time: "
                + "121.64 ms, remaining time: 378.36 ms, target pause time: 500.00 ms]";
        String nextLogLine = " 5612.999: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 146 "
                + "regions, survivors: 14 regions, predicted young region time: 71.49 ms]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogEvent),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2021-12-15T12:19:59.319-0300: 5612.998: [GC pause (GCLocker Initiated GC) (mixed)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMixedYoungPauseWithConcurrentCleanupEnd() {
        String logLine = "6554.823: [GC pause (young)6554.824: [GC concurrent-cleanup-end, 0.0029080 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMixedYoungPauseWithConcurrentRootRegionScanEnd() {
        String logLine = "4969.943: [GC pause (young)4970.158: [GC concurrent-root-region-scan-end, 0.5703200 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testMixedYoungPauseWithConcurrentRootRegionScanEndWithDatestamps() {
        String logLine = "2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)"
                + "2017-06-01T03:09:18.081-0400: 3978.888: [GC concurrent-root-region-scan-end, 0.0059070 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMixedYoungPauseWithConcurrentWithDatestamps() {
        String logLine = "2016-02-09T06:17:15.619-0500: 27744.381: [GC pause (young)"
                + "2016-02-09T06:17:15.732-0500: 27744.494: [GC concurrent-root-region-scan-end, 0.3550210 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testNew() {
        String logLine = "         [New:               6712( 66.4%)    526.1K]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testObjectCopy() {
        String logLine = "      [Object Copy (ms): Min: 9.0, Avg: 9.4, Max: 9.8, Diff: 0.8, Sum: 56.7]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testOld() {
        String logLine = "         [Old:                203(  6.1%)     23.4K( 11.9%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testOther6LeadingSpaces() {
        String logLine = "      [Other:   0.9 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        // Toss Parallel block "Other": see dataset21.txt
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testOtherJdk8() {
        String logLine = "   [Other: 8.2 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testParallelTime() {
        String logLine = "   [Parallel Time: 12.6 ms, GC Workers: 6]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testPreprocessingFullMixedConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset145.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_FULL_GC_SERIAL),
                JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_CONCURRENT),
                JdkUtil.EventType.G1_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingWithCommas() throws IOException {
        File testFile = TestUtil.getFile("dataset143.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingYoungMixedConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset144.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_CONCURRENT),
                JdkUtil.EventType.G1_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingYoungMixedErgonomics() throws IOException {
        File testFile = TestUtil.getFile("dataset180.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    @Test
    void testPrintClassHistogramDatestamp() {
        String priorLogLine = "   [Eden: 448.0M(7936.0M)->0.0B(7936.0M) Survivors: 0.0B->0.0B Heap: "
                + "8185.5M(31.0G)->7616.3M(31.0G)], [Metaspace: 668658K->668658K(1169408K)]";
        LogEvent priorLogEvent = JdkUtil.parseLogLine(priorLogLine, null, CollectorFamily.UNKNOWN);
        String logLine = "2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc): ";
        String nextLogLine = " num     #instances         #bytes  class name";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogEvent),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testPrintClassHistogramSpaceAtEnd() {
        String logLine = "49709.036: [Class Histogram (after full gc): ";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("49709.036: [Class Histogram (after full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcDuration() {
        String logLine = "2023-01-30T14:54:56.603-0500: 1394.823: [SoftReference, 0 refs, 0.0017812 secs]"
                + "2023-01-30T14:54:56.604-0500: 1394.825: [WeakReference, 617 refs, 0.0009224 secs]"
                + "2023-01-30T14:54:56.605-0500: 1394.826: [FinalReference, 2059 refs, 0.0013233 secs]"
                + "2023-01-30T14:54:56.607-0500: 1394.827: [PhantomReference, 103 refs, 909 refs, 0.0045834 secs]"
                + "2023-01-30T14:54:56.611-0500: 1394.832: [JNI Weak Reference, 0.0001317 secs], 0.0847598 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertEquals(", 0.0847598 secs]", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyContinueMixedGCs() {
        String logLine = "2023-02-23T13:11:23.327+0800: 4295945.631: [SoftReference, 3 refs, 0.0010842 secs]"
                + "2023-02-23T13:11:23.328+0800: 4295945.632: [WeakReference, 1 refs, 0.0007039 secs]"
                + "2023-02-23T13:11:23.329+0800: 4295945.633: [FinalReference, 6 refs, 0.0006505 secs]"
                + "2023-02-23T13:11:23.330+0800: 4295945.634: [PhantomReference, 0 refs, 0 refs, 0.0014290 secs]"
                + "2023-02-23T13:11:23.331+0800: 4295945.635: [JNI Weak Reference, 0.0001052 secs] "
                + "4295945.642: [G1Ergonomics (Mixed GCs) continue mixed GCs, reason: candidate old regions available, "
                + "candidate old regions: 687 regions, reclaimable: 737569024 bytes (11.45 %), threshold: 5.00 %]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyDoNotContinueMixedGCs() {
        String logLine = "2023-02-23T13:11:23.619+0800: 4295945.923: [SoftReference, 0 refs, 0.0009424 secs]"
                + "2023-02-23T13:11:23.620+0800: 4295945.924: [WeakReference, 3 refs, 0.0007199 secs]"
                + "2023-02-23T13:11:23.621+0800: 4295945.924: [FinalReference, 1038 refs, 0.0008301 secs]"
                + "2023-02-23T13:11:23.622+0800: 4295945.925: [PhantomReference, 0 refs, 0 refs, 0.0020654 secs]"
                + "2023-02-23T13:11:23.624+0800: 4295945.927: [JNI Weak Reference, 0.0001107 secs] "
                + "4295945.935: [G1Ergonomics (Mixed GCs) do not continue mixed GCs, reason: reclaimable percentage "
                + "not over threshold, candidate old regions: 452 regions, reclaimable: 321143560 bytes (4.98 %), "
                + "threshold: 5.00 %]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyDoNotRequestConcurrentCycleInitiation() {
        String logLine = "2023-02-25T18:59:48.170+0800: 103242.969: [SoftReference, 0 refs, 0.0006369 secs]"
                + "2023-02-25T18:59:48.171+0800: 103242.969: [WeakReference, 0 refs, 0.0003788 secs]"
                + "2023-02-25T18:59:48.171+0800: 103242.970: [FinalReference, 4948 refs, 0.0014439 secs]"
                + "2023-02-25T18:59:48.172+0800: 103242.971: [PhantomReference, 139 refs, 0 refs, 0.0009356 secs]"
                + "2023-02-25T18:59:48.173+0800: 103242.972: [JNI Weak Reference, 0.0001313 secs] "
                + "103242.977: [G1Ergonomics (Concurrent Cycles) do not request concurrent cycle initiation, reason: "
                + "still doing mixed collections, occupancy: 3080716288 bytes, allocation request: 0 bytes, threshold: "
                + "2899102905 bytes (45.00 %), source: end of GC]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyPreparsing() throws IOException {
        File testFile = TestUtil.getFile("dataset267.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyRequestConcurrentCycleInitiation() {
        String logLine = "2023-02-25T22:27:49.568+0800: 115724.367: [SoftReference, 0 refs, 0.0008055 secs]"
                + "2023-02-25T22:27:49.569+0800: 115724.368: [WeakReference, 0 refs, 0.0004012 secs]"
                + "2023-02-25T22:27:49.570+0800: 115724.368: [FinalReference, 2688 refs, 0.0009420 secs]"
                + "2023-02-25T22:27:49.571+0800: 115724.369: [PhantomReference, 0 refs, 0 refs, 0.0009824 secs]"
                + "2023-02-25T22:27:49.572+0800: 115724.370: [JNI Weak Reference, 0.0000928 secs] "
                + "115724.373: [G1Ergonomics (Concurrent Cycles) request concurrent cycle initiation, reason: "
                + "occupancy higher than threshold, occupancy: 3057647616 bytes, allocation request: 0 bytes, "
                + "threshold: 2899102905 bytes (45.00 %), source: end of GC]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcPrintAdaptiveSizePolicyStartMixedGCs() {
        String logLine = "2023-02-23T13:11:22.899+0800: 4295945.203: [SoftReference, 0 refs, 0.0012549 secs]"
                + "2023-02-23T13:11:22.901+0800: 4295945.204: [WeakReference, 2 refs, 0.0012014 secs]"
                + "2023-02-23T13:11:22.902+0800: 4295945.205: [FinalReference, 529 refs, 0.0008013 secs]"
                + "2023-02-23T13:11:22.903+0800: 4295945.206: [PhantomReference, 0 refs, 0 refs, 0.0014646 secs]"
                + "2023-02-23T13:11:22.904+0800: 4295945.208: [JNI Weak Reference, 0.0001060 secs] "
                + "4295945.212: [G1Ergonomics (Mixed GCs) start mixed GCs, reason: candidate old regions available, "
                + "candidate old regions: 995 regions, reclaimable: 1382364536 bytes (21.46 %), threshold: 5.00 %]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrintReferenceGcRemark() {
        String logLine = "2023-01-30T14:31:47.254-0500: 5.075: [GC remark 2023-01-30T14:31:47.254-0500: 5.075: "
                + "[Finalize Marking, 0.0013331 secs] 2021-08-20T11:53:44.350+0100: 2377830.400: [GC ref-proc"
                + "2021-08-20T11:53:44.350+0100: 2377830.400: [SoftReference, 18076174 refs, 2.6283514 secs]"
                + "2021-08-20T11:53:46.978+0100: 2377833.028: [WeakReference, 18636 refs, 0.0029750 secs]"
                + "2021-08-20T11:53:46.981+0100: 2377833.031: [FinalReference, 17387271 refs, 2.5263032 secs]"
                + "2021-08-20T11:53:49.507+0100: 2377835.558: [PhantomReference, 0 refs, 2136 refs, 0.0012040 secs]"
                + "2021-08-20T11:53:49.509+0100: 2377835.559: [JNI Weak Reference, 0.0001679 secs], 14.8775199 secs] "
                + "2021-08-20T11:53:59.227+0100: 2377845.278: [Unloading, 0.0178265 secs], 14.9383332 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        List<PreprocessEvent> preprocessEvents = new ArrayList<>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context,
                preprocessEvents);
        assertEquals("2023-01-30T14:31:47.254-0500: 5.075: [GC remark, 14.9383332 secs]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testProcessedBuffers() {
        String logLine = "         [Processed Buffers : 2 1 0 0";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testProcessedBuffersJdk8() {
        String logLine = "         [Processed Buffers: Min: 0, Avg: 8.0, Max: 39, Diff: 39, Sum: 48]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testQueue() {
        String logLine = "   [Queue]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRedirtyCards() {
        String logLine = "      [Redirty Cards: 0.6 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRefEnq() {
        String logLine = "      [Ref Enq: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRefProc() {
        String logLine = "      [Ref Proc: 7.9 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRehashCount() {
        String logLine = "      [Rehash Count: 0, Rehash Threshold: 120, Hash Seed: 0x0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRemarkPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset263.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_REMARK),
                JdkUtil.EventType.G1_REMARK.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_REFERENCE_GC_ENABLED.getKey()),
                "-XX:+PrintReferenceGC not identified.");
    }

    @Test
    void testRemarkPrintReferenceGc() throws IOException {
        File testFile = TestUtil.getFile("dataset214.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_REMARK),
                JdkUtil.EventType.G1_REMARK.toString() + " event not identified.");
    }

    /**
     * Test for G1_REMARK with JDK8 details.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testRemarkWithFinalizeMarkingAndUnloading() throws IOException {
        File testFile = TestUtil.getFile("dataset63.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_REMARK),
                "Log line not recognized as " + JdkUtil.EventType.G1_REMARK.toString() + ".");
    }

    @Test
    void testRemarkWithFinalizeMarkingAndUnloadingLine() {
        String logLine = "5.745: [GC remark 5.746: [Finalize Marking, 0.0068506 secs] 5.752: "
                + "[GC ref-proc, 0.0014064 secs] 5.754: [Unloading, 0.0057674 secs], 0.0157938 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testResizeCount() {
        String logLine = "      [Resize Count: 4, Shrink Threshold: 10922(66.7%), Grow Threshold: 32768(200.0%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRetainMiddleDuration() {
        String logLine = ", 0.0209631 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRetainMiddleDurationWithToSpaceExhaustedTrigger() {
        String logLine = " (to-space exhausted), 0.3857580 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRetainMiddleDurationWithToSpaceOverflowTrigger() {
        String logLine = " (to-space overflow), 0.77121400 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRetainMiddleJdk8() {
        String logLine = "   [Eden: 128.0M(128.0M)->0.0B(112.0M) Survivors: 0.0B->16.0M "
                + "Heap: 128.0M(30.0G)->24.9M(30.0G)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRetainMiddleYoung() {
        String logLine = "   [ 29M->2589K(59M)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testRootRegionScanWaiting() {
        String logLine = "   [Root Region Scan Waiting: 112.3 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSATBFiltering() {
        String logLine = "      [SATB Filtering (ms): Min: 0.0, Avg: 0.1, Max: 0.4, Diff: 0.4, Sum: 0.4]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testScanRs() {
        String logLine = "      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSingleConcurrentMarkStartBlock() {
        String logLine = "[GC concurrent-mark-start]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSize() {
        String logLine = "      [Size: 16384, Min: 1024, Max: 16777216]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSkipped() {
        String logLine = "         [Skipped:              0(  0.0%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSpaceDetailsWithMetaspace() {
        String logLine = "   [Eden: 0.0B(1522.0M)->0.0B(2758.0M) Survivors: 244.0M->0.0B "
                + "Heap: 1831.0M(5120.0M)->1213.5M(5120.0M)], [Metaspace: 396834K->324903K(1511424K)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSpaceDetailsWithPerm() {
        String logLine = "   [Eden: 143.0M(1624.0M)->0.0B(1843.0M) Survivors: 219.0M->0.0B "
                + "Heap: 999.5M(3072.0M)->691.1M(3072.0M)], [Perm: 175031K->175031K(175104K)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSpaceNoDetailsWithoutPerm() {
        String logLine = "   [Eden: 4096M(4096M)->0B(3528M) Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testStringDedupFixup() {
        String logLine = "   [String Dedup Fixup: 1.6 ms, GC Workers: 18]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testStringDedupFixupQueueFixup() {
        String logLine = "      [Queue Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testStringDedupFixupTableFixup() {
        String logLine = "      [Table Fixup (ms): Min: 0.0, Avg: 0.1, Max: 1.3, Diff: 1.3, Sum: 1.3]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSum() {
        String logLine = "          Sum: 4, Avg: 1, Min: 1, Max: 1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testSummarizeRSetStatsPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset139.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_VM_INFO),
                JdkUtil.EventType.HEADER_VM_INFO.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_MEMORY),
                JdkUtil.EventType.HEADER_MEMORY.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.EventType.HEADER_COMMAND_LINE_FLAGS.toString() + " event not identified.");
        assertTrue(jvmRun.getJvmOptions().hasAnalysis(org.github.joa.util.Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT),
                org.github.joa.util.Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT + " analysis not identified.");
    }

    @Test
    void testSysGreaterThanUser() throws IOException {
        File testFile = TestUtil.getFile("dataset252.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertEquals((long) 6, jvmRun.getSerialCount(), "Serial event count not correct.");
        assertEquals((long) 2, jvmRun.getSysGtUserCount(), "sys > user time event count not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_SYS_GT_USER.getKey()),
                Analysis.WARN_SYS_GT_USER + " analysis not identified.");
        LogEvent event = jvmRun.getWorstSysGtUserEvent();
        assertEquals(500000, event.getTimestamp(), "Worst sys > user event timestamp not correct.");
    }

    @Test
    void testTable() {
        String logLine = "   [Table]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTermination() {
        String logLine = "      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTerminationAttempts() {
        String logLine = "         [Termination Attempts : 1 1 1 1";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTerminationAttemptsNoSpaceBeforeColon() {
        String logLine = "         [Termination Attempts: Min: 274, Avg: 618.2, Max: 918, Diff: 644, Sum: 11127]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTimesBlock() {
        String logLine = " [Times: user=0.33 sys=0.04, real=0.17 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTimesBlockWithSpaceAtEnd() {
        String logLine = " [Times: user=0.33 sys=0.04, real=0.17 secs] ";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTotalExec() {
        String logLine = "   [Total Exec: 2/0.0281081 secs, Idle: 2/9.1631547 secs, Blocked: 2/0.0266213 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testTriggerHeapDumpInitiatedGcClassHistogram() throws IOException {
        File testFile = TestUtil.getFile("dataset221.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_FULL_GC_SERIAL),
                JdkUtil.EventType.G1_FULL_GC_SERIAL.toString() + " event not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    @Test
    void testUpdateRs() {
        String logLine = "      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoung() {
        String logLine = "         [Young:             3101( 93.9%)    173.8K( 88.1%)]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungInitialMark() {
        String logLine = "2970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark)";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungInitialMarkTriggerG1HumongousAllocationWithG1Ergonomics() {
        String logLine = "2017-02-02T01:55:56.661-0500: 860.367: [GC pause (G1 Humongous Allocation) (young) "
                + "(initial-mark) 860.367: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: "
                + "3305091, predicted base time: 457.90 ms, remaining time: 42.10 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungInitialMarkWithDatestamp() {
        String logLine = "2017-01-20T23:18:29.561-0500: 1513296.434: [GC pause (young) (initial-mark), "
                + "0.0225230 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungInitialMarkWithG1Ergonomics() {
        String logLine = "2016-02-11T15:22:23.213-0500: 4582.283: [GC pause (young) (initial-mark) 4582.283: "
                + "[G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 6084, predicted base time: "
                + "41.16 ms, remaining time: 458.84 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungPause() {
        String logLine = "785,047: [GC pause (young), 0,73936800 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseDoubleTriggerToSpaceExhausted() {
        String logLine = "6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungPauseMixedConcurrentMarkEnd() {
        String logLine = "188935.313: [GC pause (G1 Evacuation Pause) (young)"
                + "188935.321: [GC concurrent-mark-end, 0.4777427 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("188935.313: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseMixedConcurrentRootRegiaonScanEnd() {
        String logLine = "2021-06-15T13:51:22.274-0600: 39666.928: [GC pause (G1 Evacuation Pause) (young)"
                + "2021-06-15T13:51:22.274-0600: 39666.928: [GC concurrent-root-region-scan-end, 0.0005374 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2021-06-15T13:51:22.274-0600: 39666.928: [GC pause (G1 Evacuation Pause) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseMixedConcurrentRootRegionScanStart() {
        String logLine = "537.122: [GC pause (G1 Evacuation Pause) (young)"
                + "537.123: [GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("537.122: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseTriggerHumongousAllocationWithG1Ergonomics() {
        String logLine = "2020-02-16T23:24:09.668+0000: 880272.698: [GC pause (G1 Humongous Allocation) (young) "
                + "880272.699: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 241090, "
                + "predicted base time: 129.61 ms, remaining time: 70.39 ms, target pause time: 200.00 ms]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2020-02-16T23:24:09.668+0000: 880272.698: [GC pause (G1 Humongous Allocation) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseWithG1Ergonomics() {
        String logLine = "72945.823: [GC pause (young) 72945.823: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 497394, predicted base time: 66.16 ms, remaining time: 433.84 ms, target "
                + "pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungPauseWithG1ErgonomicsAndDateStamps() {
        String priorLogLine = "2021-12-15T10:46:34.200-0300: 7.879: Total time for which application threads were "
                + "stopped: 0.0042523 seconds, Stopping threads took: 0.0009693 seconds";
        LogEvent priorLogEvent = JdkUtil.parseLogLine(priorLogLine, null, CollectorFamily.UNKNOWN);
        String logLine = "2021-12-15T10:46:35.163-0300: 8.842: [GC pause (G1 Evacuation Pause) (young) 8.843: "
                + "[G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 3120, predicted base time: "
                + "180.94 ms, remaining time: 319.06 ms, target pause time: 500.00 ms]";
        String nextLogLine = " 7.883: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 141 regions, "
                + "survivors: 20 regions, predicted young region time: 2463.53 ms]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogEvent),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context, null);
        assertEquals("2021-12-15T10:46:35.163-0300: 8.842: [GC pause (G1 Evacuation Pause) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungPauseWithToSpaceExhaustedTrigger() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testYoungPauseWithTriggerWithG1ErgonomicsDoubleTimestampAndDateStamps() {
        String logLine = "2017-03-21T15:05:53.717+1100: 425001.630: [GC pause (G1 Evacuation Pause) (young)"
                + "2017-03-21T15:05:53.717+1100: 425001.630:  425001.630: [G1Ergonomics (CSet Construction) start "
                + "choosing CSet, _pending_cards: 3, predicted base time: 45.72 ms, remaining time: 304.28 ms, target "
                + "pause time: 350.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }
}
