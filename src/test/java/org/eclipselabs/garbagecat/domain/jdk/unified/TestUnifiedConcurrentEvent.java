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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestUnifiedConcurrentEvent {

    @Test
    void testCleanupForNextMark() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCleanupForNextMarkWithDuration() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark 0.428ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testClearClaimedMarks() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testClearClaimedMarksWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks 0.019ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMark() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkAbort() {
        String logLine = "[2020-06-24T18:13:51.156-0700][177151ms] GC(73) Concurrent Mark Abort";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkContinueZGenerationalMinor() {
        String logLine = "[2023-11-16T09:18:11.215-0500] GC(157) y: Concurrent Mark Continue 0.073ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkCycle() {
        String logLine = "[0.062s][info][gc          ] GC(2) Concurrent Mark Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkFree() {
        String logLine = "[0.129s] GC(0) Concurrent Mark Free 0.000ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Mark 1.428ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Mark 1.260ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkZGenerationalMinor() {
        String logLine = "[0.305s][info][gc,phases   ] GC(3) y: Concurrent Mark 8.889ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPreclean() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPrecleanWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPrecleanWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Preclean 0.033ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentProcessNonStrong() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Concurrent Process Non-Strong 0.658ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentProcessNoStrongReferences() {
        String logLine = "[0.130s] GC(0) Concurrent Process Non-Strong References 0.685ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRebuildRememberedSetsAndScrubRegionsWithDuration() {
        String logLine = "[2023-11-16T06:43:27.111-0500] GC(5) Concurrent Rebuild Remembered Sets and Scrub Regions "
                + "2.155ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRelocate() {
        String logLine = "[0.134s] GC(0) Concurrent Relocate 2.550ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRemapRoots() {
        String logLine = "[0.228s][info][gc,phases   ] GC(2) O: Concurrent Remap Roots 12.855ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentReset() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Reset";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetRelocationSet() {
        String logLine = "[0.100s][info][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.000ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetWithDuration() {
        String logLine = "[0.086s][info][gc] GC(1) Concurrent Reset 0.841ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetWithTimesData() {
        String logLine = "[0.056s][info][gc           ] GC(1) Concurrent Reset 0.693ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSelectRelocationSet() {
        String logLine = "[0.131s] GC(0) Concurrent Select Relocation Set 1.444ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentStringDeduplication() {
        String logLine = "[2021-10-08T16:04:26.204-0400][8.937s] Concurrent String Deduplication (8.937s)";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentStringDeduplicationDetails() {
        String logLine = "[2021-10-08T16:04:26.249-0400][8.983s] Concurrent String Deduplication "
                + "3428.0K->2498.6K(929.4K) avg 27.1% (8.937s, 8.983s) 45.667ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweep() {
        String logLine = "[0.084s][info][gc] GC(1) Concurrent Sweep";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweepWithDuration() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweepWithTimesData() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep 0.298ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentUndoCycle() {
        String logLine = "[2023-01-11T16:09:59.244+0000][19084.784s] GC(300) Concurrent Undo Cycle 54.191ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCreateLiveData() {
        String logLine = "[2.730s][info][gc,marking    ] GC(52) Concurrent Create Live Data";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCreateLiveDataWithDuration() {
        String logLine = "[2.731s][info][gc,marking    ] GC(52) Concurrent Create Live Data 0.483ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailed() {
        String logLine = "[16.601s][info][gc           ] GC(1033) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailed12Spaces() {
        String logLine = "[16.121s][info][gc            ] GC(974) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailedWithDuration() {
        String logLine = "[16.082s][info][gc            ] GC(969) Concurrent Cycle 65.746ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testDiscoveredReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Discovered  references: Soft: 0, Weak: 108, Final: 0, "
                + "Phantom: 6";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testEncounteredReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Encountered references: Soft: 3110, Weak: 230, Final: 2, "
                + "Phantom: 8";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testEnqueuedReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Enqueued    references: Soft: 0, Weak: 0, Final: 0, "
                + "Phantom: 0";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertEquals(JdkUtil.LogEventType.UNIFIED_CONCURRENT, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testLogLineCycleWithDuration() {
        String logLine = "[14.904s][info][gc] GC(1083) Concurrent Cycle 45.374ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark    ";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkDoubleTimestampWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Mark (16.017s, 16.050s) 33.614ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkTimestamp() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark (16.601s)";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedConcurrentEvent,
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not parsed.");
    }

    @Test
    void testPrecleanWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Preclean 0.115ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testRebuildRememberedSets() {
        String logLine = "[16.053s][info][gc,marking    ] GC(969) Concurrent Rebuild Remembered Sets";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not indentified as reportable.");
    }

    @Test
    void testScanRootRegions() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testScanRootRegionsWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions 0.283ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not indentified as unified.");
    }

    @Test
    void testUsingWorkersForMarkFromRoots() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark From Roots";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testUsingWorkersForMarking() {
        String logLine = "[16.601s][info][gc,task      ] GC(1033) Using 1 workers of 1 for marking";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
    }
}
