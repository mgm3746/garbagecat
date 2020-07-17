/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestUnifiedConcurrentEvent extends TestCase {

    public void testIdentityEventType() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_CONCURRENT + "not identified.",
                JdkUtil.LogEventType.UNIFIED_CONCURRENT, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedConcurrentEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertFalse(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CONCURRENT));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentMark() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentMarkWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Mark 1.428ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentMarkWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Mark 1.260ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentPreclean() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentPrecleanWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentPrecleanWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Preclean 0.033ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentSweep() {
        String logLine = "[0.084s][info][gc] GC(1) Concurrent Sweep";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentSweepWithDuration() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentSweepWithTimesData() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep 0.298ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentReset() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentResetWithDuration() {
        String logLine = "[0.086s][info][gc] GC(1) Concurrent Reset 0.841ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentResetWithTimesData() {
        String logLine = "[0.056s][info][gc           ] GC(1) Concurrent Reset 0.693ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testLogLine() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testLogLineCycleWithDuration() {
        String logLine = "[14.904s][info][gc] GC(1083) Concurrent Cycle 45.374ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCycleDetailed() {
        String logLine = "[16.601s][info][gc           ] GC(1033) Concurrent Cycle";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCycleDetailed12Spaces() {
        String logLine = "[16.121s][info][gc            ] GC(974) Concurrent Cycle";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCycleDetailedWithDuration() {
        String logLine = "[16.082s][info][gc            ] GC(969) Concurrent Cycle 65.746ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testClearClaimedMarks() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testClearClaimedMarksWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks 0.019ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testScanRootRegions() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testScanRootRegionsWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions 0.283ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testMarkTimestamp() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark (16.601s)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testMarkDoubleTimestampWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Mark (16.017s, 16.050s) 33.614ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testUsingWorkersForMarking() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark From Roots";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testUsingWorkersForFullCompaction() {
        String logLine = "[2020-06-24T18:13:47.695-0700][173690ms] GC(74) Using 2 workers of 2 for full compaction";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testMarkFromRoots() {
        String logLine = "[16.601s][info][gc,task      ] GC(1033) Using 1 workers of 1 for marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testPreclean() {
        String logLine = "[16.601s][info][gc,task      ] GC(1033) Using 1 workers of 1 for marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testPrecleanWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Preclean 0.115ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testRebuildRememberedSets() {
        String logLine = "[16.053s][info][gc,marking    ] GC(969) Concurrent Rebuild Remembered Sets";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCleanupForNextMark() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCleanupForNextMarkWithDuration() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark 0.428ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCreateLiveData() {
        String logLine = "[2.730s][info][gc,marking    ] GC(52) Concurrent Create Live Data";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testCreateLiveDataWithDuration() {
        String logLine = "[2.731s][info][gc,marking    ] GC(52) Concurrent Create Live Data 0.483ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }

    public void testConcurrentMarkAbort() {
        String logLine = "[2020-06-24T18:13:51.156-0700][177151ms] GC(73) Concurrent Mark Abort";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".",
                UnifiedConcurrentEvent.match(logLine));
    }
}
