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
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedPreprocessAction {

    @Test
    void testPauseYoungStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMarkStart() {
        String logLine = "[4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMark() {
        String logLine = "[4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseComputeStart() {
        String logLine = "[4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseCompute() {
        String logLine = "[4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseAdjustStart() {
        String logLine = "[4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseAdjust() {
        String logLine = "[4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMoveStart() {
        String logLine = "[4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMove() {
        String logLine = "[4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testDefNewData() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" DefNew: 1016K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTenuredData() {
        String logLine = "[32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Tenured: 24193K->24195K(25240K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPsYoungGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K->464K(1024K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSYoungGen: 512K->464K(1024K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParNewData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) ParNew: 974K->128K(1152K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" ParNew: 974K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) CMS: 0K->518K(960K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" CMS: 0K->518K(960K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPsOldGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSOldGen: 0K->8K(512K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSOldGen: 0K->8K(512K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceData() {
        String logLine = "[0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K->120K(1056768K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 120K->120K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspace2Spaces() {
        String logLine = "[16.072s][info][gc,metaspace  ] GC(971) Metaspace: 10793K->10793K(1058816K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParOldGenData() {
        String logLine = "[0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K->8K(512K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" ParOldGen: 0K->8K(512K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K->26116K(278528K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungInfo() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(2M) 0.700ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoStandAlone() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1PauseYoungInfo() {
        String logLine = "[0.337s][info][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 25M->4M(254M) 3.523ms";
        String nextLogLine = "[0.337s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 25M->4M(254M) 3.523ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MixedPauseInfo() {
        String logLine = "[16.630s][info][gc            ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "15M->12M(31M) 1.202ms";
        String nextLogLine = "[16.630s][info][gc           ] GC(0) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 15M->12M(31M) 1.202ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalWithDatestamp() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        String nextLogLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 65M->8M(1304M) 57.263ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalTriggerGcLockerWithDatestamp() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        String nextLogLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) User=0.02s Sys=0.00s Real=0.04s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 78M->22M(1304M) 35.722ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfo11SpacesAfterGc() {
        String logLine = "[0.032s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.195ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) 733M->588M(1223M) 105.541ms";
        String nextLogLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 733M->588M(1223M) 105.541ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerG1HumongousAllocation() {
        String logLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) Pause Young (Concurrent Start) "
                + "(G1 Humongous Allocation) 882M->842M(1223M) 19.777ms";
        String nextLogLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) User=0.04s Sys=0.00s Real=0.02s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 882M->842M(1223M) 19.777ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" User=0.00s Sys=0.00s Real=0.00s", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimesData8Spaces() {
        String logLine = "[16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testTimesDataDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormal() {
        String logLine = "[0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormal6Spaces() {
        String logLine = "[16.070s][info][gc,start      ] GC(971) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPrepareMixed() {
        String logLine = "[15.108s][info][gc,start      ] GC(1194) Pause Young (Prepare Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1MixedPause() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseConcurrentStart() {
        String logLine = "[16.600s][info][gc,start     ] GC(1032) Pause Young (Concurrent Start) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNoQualifier() {
        String logLine = "[0.333s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalMillis8Digits() {
        String logLine = "[2019-05-09T04:31:19.449+0000][10344041ms] GC(9) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation() {
        String logLine = "[0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation2Digits() {
        String logLine = "[0.333s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation7Spaces() {
        String logLine = "[16.070s][info][gc,task       ] GC(971) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuationDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Using 1 workers of 1 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForMarking() {
        String logLine = "[16.121s][info][gc,task       ] GC(974) Using 1 workers of 1 for marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUsingWorkersForFullCompaction() {
        String logLine = "[2020-06-24T18:13:47.695-0700][173690ms] GC(74) Using 2 workers of 2 for full compaction";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForFullCompaction() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8651) Using 8 workers of 8 for full "
                + "compaction";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testToSpaceExhausted() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8645) To-space exhausted";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuate5Spaces() {
        String logLine = "[16.071s][info][gc,phases     ] GC(971)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Evacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Evacuate Collection Set: 56.4ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PostEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PostEvacuate5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Post Evacuate Collection Set: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PostEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Post Evacuate Collection Set: 0.5ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Other() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1OtherDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Other: 0.3ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Other5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Eden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Eden3Digits() {
        String logLine = "[0.335s][info][gc,heap      ] GC(0) Eden regions: 24->0(149)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EdenDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Eden regions: 65->0(56)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EdenDatestampMillisRegions4Digits() {
        String logLine = "[2020-09-11T05:33:44.563+0000][1732868ms] GC(42) Eden regions: 307->0(1659)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Survivor() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1SurvivorDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Survivor regions: 0->9(9)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Old() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Old2Digits() {
        String logLine = "[10.989s][info][gc,heap      ] GC(684) Old regions: 15->15";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Old7Spaces() {
        String logLine = "[17.728s][info][gc,heap       ] GC(1098) Old regions: 21->21";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1OldDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Humongous() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1HumongousDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormal() {
        String logLine = "[0.101s][info][gc           ] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) 0M->0M(2M) 1.371ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseConcurrentStart() {
        String logLine = "[16.601s][info][gc           ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) 38M->20M(46M) 0.772ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRemark() {
        String logLine = "[16.051s][info][gc,start     ] GC(969) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRemark6Spaces() {
        String logLine = "[16.175s][info][gc,start      ] GC(974) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRemarkWithDuration() {
        String logLine = "[0.055s][info][gc           ] GC(1) Pause Remark 0M->0M(2M) 0.332ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1StringTable() {
        String logLine = "[16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: "
                + "5786 processed, 4 removed, symbols: 38663 processed, 11 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1StringTableLargeNumbers() {
        String logLine = "[2021-05-25T08:46:20.163-0400][1191010566ms] GC(14942) Cleaned string and symbol table, "
                + "strings: 756258 processed, 688568 removed, symbols: 349887 processed, 34 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1StringTableRemoved6Digits() {
        String logLine = "[2020-09-11T08:23:37.353+0000][11925659ms] GC(194) Cleaned string and symbol table, "
                + "strings: 368294 processed, 9531 removed, symbols: 1054532 processed, 18545 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PauseCleanup() {
        String logLine = "[16.081s][info][gc,start      ] GC(969) Pause Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MarkClosedArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, "
                + "0x00000000fff69ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MarkOpenArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark open archive regions in map: [0x00000000ffe00000, "
                + "0x00000000ffe46ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MmuTargetViolated() {
        String logLine = "[2020-06-24T18:11:22.155-0700][28150ms] GC(24) MMU target violated: 201.0ms "
                + "(200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialNewStart() {
        String logLine = "[0.118s][info][gc,start       ] GC(4) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStart() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStart7SpacesAfterStart() {
        String logLine = "[0.119s][info][gc,start       ] GC(5) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStartTriggerMetadataGcThreshold() {
        String logLine = "[2021-05-06T21:03:33.749+0000][22637ms] GC(11) Pause Full (Metadata GC Threshold) "
                + "58M->53M(236M) 521.443ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStartTriggerSystemGc() {
        String logLine = "[2021-09-22T10:57:20.259-0500][5258404ms] GC(5172) Pause Full (System.gc())";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldInfo() {
        String logLine = "[0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-03-13T03:45:44.425+0530][80337493ms] GC(9216) Pause Full (GCLocker Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerGcLockerInitiatedGcDetails() {
        String logLine = "[2021-03-13T03:45:46.526+0530][80339594ms] GC(9216) Pause Full (GCLocker Initiated GC) "
                + "8184M->8180M(8192M) 2101.341ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldMarkingPhaseStart() {
        String logLine = "[0.083s][info][gc,phases,start] GC(3) Marking Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldMarkingPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOlSummaryPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Summary Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldSummaryPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldAdjustRootsStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Adjust Roots";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldAdjustRoots() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldCompactionPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Compaction Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldCompactionPhase() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldPostStart() {
        String logLine = "[0.087s][info][gc,phases,start] GC(3) Post Compact";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldPostCompact() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldTriggerErgonomics() {
        String logLine = "[2021-10-27T13:03:09.055-0400] GC(3) Pause Full (Ergonomics)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldTriggerErgonomicsDetails() {
        String logLine = "[0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms";
        String nextLogLine = "[0.092s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 0M->0M(3M) 1.849ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldInfoTriggerSystemGc() {
        String logLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) Pause Full (System.gc()) 887M->583M(1223M) "
                + "3460.196ms";
        String nextLogLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) User=1.78s Sys=0.01s Real=3.46s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 887M->583M(1223M) 3460.196ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldInfoTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:37:42.178+0530][79855246ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "8186M->8178M(8192M) 2127.343ms";
        String nextLogLine = "[2021-03-13T03:37:42.179+0530][79855247ms] GC(8646) User=16.40s Sys=0.09s Real=2.13s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 8186M->8178M(8192M) 2127.343ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungSingleLine() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testCmsInitialMark() {
        String logLine = "[0.053s][info][gc,start     ] GC(1) Pause Initial Mark";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsInitialMarkWithDuration() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testCmsOld() {
        String logLine = "[0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSafepointEnteringEnableBiasedLocking() {
        String logLine = "[0.029s][info][safepoint    ] Entering safepoint region: EnableBiasedLocking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringShenandoahInitUpdateRefs() {
        String logLine = "[2021-10-27T13:03:16.666-0400] Entering safepoint region: ShenandoahInitUpdateRefs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringShenandoahInitMark() {
        String logLine = "[2021-10-27T13:03:16.666-0400] Entering safepoint region: ShenandoahInitMark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringG1CollectFull() {
        String logLine = "[2021-09-22T10:31:58.206-0500][3736351ms] Entering safepoint region: G1CollectFull";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointLeaving() {
        String logLine = "[0.029s][info][safepoint    ] Leaving safepoint region";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointApplicationTime() {
        String logLine = "[0.030s][info][safepoint    ] Application time: 0.0012757 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentCycle() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Cycle";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentClearClaimedMarks() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentClearClaimedMarksWithDuration() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks 0.080ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentScanRootRegions() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Scan Root Regions";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentMark() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Mark (79857.381s)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentMarkSpaceAtEnd() {
        String logLine = "[0.053s][info][gc           ] GC(1) Concurrent Mark ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentMarkFromRoots() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857381ms] GC(8652) Concurrent Mark From Roots";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentMarkAbort() {
        String logLine = "[2021-03-13T03:37:46.439+0530][79859507ms] GC(8652) Concurrent Mark Abort";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentPreclean() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Preclean";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentSweep() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentReset() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAttemptingMaxCompactingCollection() {
        String logLine = "[2021-03-13T03:45:44.424+0530][80337492ms] Attempting maximally compacting collection";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicy() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0) AdaptiveSizePolicy::minor_collection_end: "
                + "minor gc cost: 0.000106  average: 0.000106";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyMinorPause() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0)   minor pause: 25.024410 minor period "
                + "237035.231990";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyOldGenCapacity() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0) old_gen_capacity: 14241759232 "
                + "young_gen_capacity: 1879048192";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyDesiredSurvivorSize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Desired survivor size 268435456 bytes, new "
                + "threshold 7 (max threshold 15)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyMinorpause() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Minor_pause: 0.025024 major_pause: 0.000000 "
                + "minor_interval: 237.060257 major_interval: 0.000000pause_goal: 18446744073709552.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyLivespace() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Live_space: 316776736 free_space: 3221225472";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyBasefootprint() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Base_footprint: 268435456 avg_young_live: "
                + "48341272 avg_old_live: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyOldEdensize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Old eden_size: 1610612736 desired_eden_size: "
                + "1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyPsYoungGenResizespaces() {
        String logLine = "[2021-06-15T19:07:24.707-0400][11298.869s] GC(716) PSYoungGen::resize_spaces("
                + "requested_eden_size: 2011168768, requested_survivor_size: 68157440)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEden() {
        String logLine = "[2021-06-15T19:07:24.707-0400][11298.869s] GC(716)     eden: "
                + "[0x0000000780000000..0x00000007f7e00000) 2011168768";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyFrom() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)     from: "
                + "[0x00000007e0000000..0x00000007f0000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyTo() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)       to: "
                + "[0x00000007f0000000..0x0000000800000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenFromTo() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)   Eden, from, to:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyCapacities() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)     capacities are the right sizes, "
                + "returning";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyYoungGenerationSize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Young generation size: desired eden: "
                + "1610612736 survivor: 268435456 used: 48341272 capacity: 1879048192 gen limits: "
                + "2147483648 / 2147483648";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyDoScavenge() {
        String logLine = "[2021-06-15T16:04:45.069-0400][339.230s] Do scavenge: average_promoted 60835652 "
                + "padded_average_promoted 183311856 free in old gen 13997838280";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgSurvived() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_survived: 313898944.000000  avg_deviation:"
                + " 127148224.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgSurvivedPadded() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_survived_padded_avg: 695343616.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgPromoted() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_promoted_avg: 103380024.000000  "
                + "avg_promoted_dev: 66695816.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgPromotedPadded() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_promoted_padded_avg: 303467488.000000  "
                + "avg_pretenured_padded_avg: 0.000000  tenuring_thresh: 6  target_size: 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenToFrom() {
        String logLine = "[2021-06-15T17:26:44.495-0400][5258.656s] GC(197)   Eden, to, from:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [eden_start .. eden_end): "
                + "[0x0000000780000000 .. 0x00000007e0000000) 1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyFromStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [from_start .. from_end): "
                + "[0x00000007e0000000 .. 0x00000007f0000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyToStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [  to_start ..   to_end): "
                + "[0x00000007f0000000 .. 0x0000000800000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyScaledEdenIncrement() {
        String logLine = "[2021-06-15T16:04:57.139-0400][351.300s] GC(5) Scaled eden increment: 1610612736 by "
                + "1.000000 down to 1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAdjustingEden() {
        String logLine = "[2021-06-15T17:26:44.495-0400][5258.656s] GC(197) Adjusting eden for throughput (avg "
                + "0.989786 goal 0.990000). desired_eden_size 2474639360 eden delta 412614656";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizeNoFullAfterScavenge() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] No full after scavenge average_promoted "
                + "103380024 padded_average_promoted 303467488 free in old gen 13724280784";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPsAdaptiveSizePolicy() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) PSAdaptiveSizePolicy::check_gc_overhead_limit:"
                + " promo_limit: 14241759232 max_eden_size: 1610612736 total_free_limit: 15852371968 max_old_gen_size: "
                + "14241759232 max_eden_size: 1610612736 mem_free_limit: 317047439";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepoint() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        Set<String> context = new HashSet<String>();
        UnifiedPreprocessAction preprocessAction = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertEquals(logLine, preprocessAction.getLogEntry());
    }

    @Test
    void testEnteringSafepointCleanup() {
        String logLine = "[2021-09-14T11:38:31.797-0500][2.454s][info][safepoint    ] Entering safepoint region: "
                + "Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepointExit() {
        String logLine = "[2021-09-22T10:59:49.112-0500][5455002ms] Entering safepoint region: Exit";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepointHalt() {
        String logLine = "[0.031s] Entering safepoint region: Halt";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointIcBufferFull() {
        String logLine = "[2021-09-14T11:40:51.508-0500][142.164s][info][safepoint     ] Entering safepoint region: "
                + "ICBufferFull";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointCGCOperation() {
        String logLine = "[0.116s][info][safepoint    ] Entering safepoint region: CGC_Operation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointTime() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time for which "
                + "application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Cleanup() {
        String logLine = "[0.117s][info][gc            ] GC(2) Pause Cleanup 1M->1M(5M) 0.024ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PauseYoungBeginning() {
        String logLine = "[2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young "
                + "(Concurrent Start) (Metadata GC Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testMmuTargetViolated() {
        String logLine = "[2021-09-14T11:41:18.173-0500][168.830s][info][gc,mmu        ] GC(26) MMU target violated: "
                + "201.0ms (200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseFullBeginning() {
        String logLine = "[2021-09-14T06:51:15.478-0500][3.530s][info][gc,start     ] GC(1) Pause Full (Metadata GC "
                + "Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitUpdate() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkProcessWeakRefs() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Pause Init Mark (process weakrefs)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMark() {
        String logLine = "[2021-10-27T13:03:16.646-0400] GC(1) Pause Init Mark";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorksForInitMarking() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Using 4 of 6 workers for init marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahImmediateGarbage() {
        String logLine = "[0.330s][info][gc,ergo      ] GC(0) Immediate Garbage: 4258K (31% of total), 17 regions";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineShenandoahDecoratorPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahDecoratorUsingWorkersInitMark() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalUpdateUsing() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersInitMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for init marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahDecoratorFinalMarkUsingWorkers() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPauseInitMark() {
        String logLine = "[2021-10-27T13:03:16.627-0400] GC(0) Pause Init Mark (process weakrefs) 0.575ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPauseFinalMark() {
        String logLine = "[2021-10-27T13:03:16.630-0400] GC(0) Pause Final Mark (process weakrefs) 0.674ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineShenandoahPauseFinalMarkNoDuration() {
        String logLine = "[2021-10-27T13:03:16.629-0400] GC(0) Pause Final Mark (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineShenandoahPauseFinalUpdate() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs 0.084ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineShenandoahPauseFinalUpdateNoDuration() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineShenandoahFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahPacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahUsingWorkersDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testLogLineShenandoahGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPreprocessingG1YoungPauseNormalCollection() {
        File testFile = TestUtil.getFile("dataset155.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1Remark() {
        File testFile = TestUtil.getFile("dataset156.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1Cleanup() {
        File testFile = TestUtil.getFile("dataset157.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitialMark() {
        File testFile = TestUtil.getFile("dataset160.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalMark() {
        File testFile = TestUtil.getFile("dataset161.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalEvac() {
        File testFile = TestUtil.getFile("dataset162.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitUpdate() {
        File testFile = TestUtil.getFile("dataset163.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalUpdate() {
        File testFile = TestUtil.getFile("dataset164.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1YoungPauseNormalTriggerGcLockerWithDatestamps() {
        File testFile = TestUtil.getFile("dataset170.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialNew() {
        File testFile = TestUtil.getFile("dataset171.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_NEW),

                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialOld() {
        File testFile = TestUtil.getFile("dataset172.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialOldTriggerErgonomics() {
        File testFile = TestUtil.getFile("dataset174.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelScavengeSerialOld() {
        File testFile = TestUtil.getFile("dataset173.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelScavengeParallelCompactingOld() {
        File testFile = TestUtil.getFile("dataset175.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelCompactingOld() {
        File testFile = TestUtil.getFile("dataset176.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParNew() {
        File testFile = TestUtil.getFile("dataset177.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");

        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW),
                JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingCms() {
        File testFile = TestUtil.getFile("dataset178.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CMS_INITIAL_MARK),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW),
                JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    /**
     * Verify that preprocessing logging that does not need preprocessing does not change logging.
     */
    @Test
    void testPreprocessingG1Unecessarily() {
        File testFile = TestUtil.getFile("dataset186.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1FullAndConcurrent() {
        File testFile = TestUtil.getFile("dataset205.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testG1FullTriggerGcLockerInitiatedGc() {
        File testFile = TestUtil.getFile("dataset206.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
    }

    @Test
    void testG1FullTriggerMetadataGcThreshold() {
        File testFile = TestUtil.getFile("dataset210.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testParallelPrintAdaptiveSizePolicy() {
        File testFile = TestUtil.getFile("dataset212.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testParallelPrintAdaptiveSizePolicyDoScavenge() {
        File testFile = TestUtil.getFile("dataset213.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testSafepoint() {
        File testFile = TestUtil.getFile("dataset215.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointG1PauseYoung() {
        File testFile = TestUtil.getFile("dataset216.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testConcurrentBeforeSafepoint() {
        File testFile = TestUtil.getFile("dataset217.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testBackToBackSafepoint() {
        File testFile = TestUtil.getFile("dataset218.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointParallel() {
        File testFile = TestUtil.getFile("dataset219.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointG1CollectFull() {
        File testFile = TestUtil.getFile("dataset220.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_OLD),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitUpdateMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset224.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitMarkMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset225.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointHandshakeFallBack() {
        File testFile = TestUtil.getFile("dataset226.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFinalMarkMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset227.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFinalUpdateMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset228.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testParallelYoungParallelSerialMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset229.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedRemarkMixedSafepoint() {
        File testFile = TestUtil.getFile("dataset230.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }
}
