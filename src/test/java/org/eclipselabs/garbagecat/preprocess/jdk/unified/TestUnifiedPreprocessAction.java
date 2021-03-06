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
public class TestUnifiedPreprocessAction {

    @Test
    public void testLogLinePauseYoungStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseMarkStart() {
        String logLine = "[4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseMark() {
        String logLine = "[4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseComputeStart() {
        String logLine = "[4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseCompute() {
        String logLine = "[4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseAdjustStart() {
        String logLine = "[4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseAdjust() {
        String logLine = "[4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseMoveStart() {
        String logLine = "[4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePhaseMove() {
        String logLine = "[4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineDefNewData() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" DefNew: 1016K->128K(1152K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineTenuredData() {
        String logLine = "[32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Tenured: 24193K->24195K(25240K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePsYoungGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K->464K(1024K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSYoungGen: 512K->464K(1024K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineParNewData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) ParNew: 974K->128K(1152K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" ParNew: 974K->128K(1152K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineCmsData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) CMS: 0K->518K(960K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" CMS: 0K->518K(960K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePsOldGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSOldGen: 0K->8K(512K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSOldGen: 0K->8K(512K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineMetaspaceData() {
        String logLine = "[0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K->120K(1056768K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 120K->120K(1056768K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineMetaspace2Spaces() {
        String logLine = "[16.072s][info][gc,metaspace  ] GC(971) Metaspace: 10793K->10793K(1058816K)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParOldGenData() {
        String logLine = "[0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K->8K(512K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" ParOldGen: 0K->8K(512K)",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineMetaspaceDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K->26116K(278528K)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePauseYoungInfo() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(2M) 0.700ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePauseYoungInfoStandAlone() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineG1PauseYoungInfo() {
        String logLine = "[0.337s][info][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 25M->4M(254M) 3.523ms";
        String nextLogLine = "[0.337s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 25M->4M(254M) 3.523ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineG1MixedPauseInfo() {
        String logLine = "[16.630s][info][gc            ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "15M->12M(31M) 1.202ms";
        String nextLogLine = "[16.630s][info][gc           ] GC(0) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 15M->12M(31M) 1.202ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePauseYoungInfoNormalWithDatestamp() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        String nextLogLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 65M->8M(1304M) 57.263ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePauseYoungInfoNormalTriggerGcLockerWithDatestamp() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        String nextLogLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) User=0.02s Sys=0.00s Real=0.04s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 78M->22M(1304M) 35.722ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePauseYoungInfo11SpacesAfterGc() {
        String logLine = "[0.032s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.195ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLinePauseYoungInfoConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) 733M->588M(1223M) 105.541ms";
        String nextLogLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 733M->588M(1223M) 105.541ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLinePauseYoungInfoConcurrentStartTriggerG1HumongousAllocation() {
        String logLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) Pause Young (Concurrent Start) "
                + "(G1 Humongous Allocation) 882M->842M(1223M) 19.777ms";
        String nextLogLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) User=0.04s Sys=0.00s Real=0.02s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 882M->842M(1223M) 19.777ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" User=0.00s Sys=0.00s Real=0.00s",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineTimesData8Spaces() {
        String logLine = "[16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineTimesDataDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNormal() {
        String logLine = "[0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNormal6Spaces() {
        String logLine = "[16.070s][info][gc,start      ] GC(971) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPrepareMixed() {
        String logLine = "[15.108s][info][gc,start      ] GC(1194) Pause Young (Prepare Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1MixedPause() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseConcurrentStart() {
        String logLine = "[16.600s][info][gc,start     ] GC(1032) Pause Young (Concurrent Start) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNoQualifier() {
        String logLine = "[0.333s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineStartG1YoungPauseNormalMillis8Digits() {
        String logLine = "[2019-05-09T04:31:19.449+0000][10344041ms] GC(9) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1UsingWorkersForEvacuation() {
        String logLine = "[0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1UsingWorkersForEvacuation2Digits() {
        String logLine = "[0.333s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1UsingWorkersForEvacuation7Spaces() {
        String logLine = "[16.070s][info][gc,task       ] GC(971) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1UsingWorkersForEvacuationDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Using 1 workers of 1 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1UsingWorkersForMarking() {
        String logLine = "[16.121s][info][gc,task       ] GC(974) Using 1 workers of 1 for marking";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PreEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PreEvacuate5Spaces() {
        String logLine = "[16.071s][info][gc,phases     ] GC(971)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PreEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Evacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1EvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Evacuate Collection Set: 56.4ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PostEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PostEvacuate5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Post Evacuate Collection Set: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PostEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Post Evacuate Collection Set: 0.5ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Other() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1OtherDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Other: 0.3ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Other5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Eden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Eden3Digits() {
        String logLine = "[0.335s][info][gc,heap      ] GC(0) Eden regions: 24->0(149)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1EdenDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Eden regions: 65->0(56)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1EdenDatestampMillisRegions4Digits() {
        String logLine = "[2020-09-11T05:33:44.563+0000][1732868ms] GC(42) Eden regions: 307->0(1659)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Survivor() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1SurvivorDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Survivor regions: 0->9(9)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Old() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Old2Digits() {
        String logLine = "[10.989s][info][gc,heap      ] GC(684) Old regions: 15->15";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Old7Spaces() {
        String logLine = "[17.728s][info][gc,heap       ] GC(1098) Old regions: 21->21";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1OldDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Humongous() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1HumongousDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1YoungPauseNormal() {
        String logLine = "[0.101s][info][gc           ] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) 0M->0M(2M) 1.371ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1YoungPauseConcurrentStart() {
        String logLine = "[16.601s][info][gc           ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) 38M->20M(46M) 0.772ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogG1LineRemark() {
        String logLine = "[16.051s][info][gc,start     ] GC(969) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1Remark6Spaces() {
        String logLine = "[16.175s][info][gc,start      ] GC(974) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1StringTable() {
        String logLine = "[16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: "
                + "5786 processed, 4 removed, symbols: 38663 processed, 11 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1StringTableLargeNumbers() {
        String logLine = "[2020-06-24T18:15:29.817-0700][275812ms] GC(93) Cleaned string and symbol table, strings: "
                + "94127 processed, 27 removed, symbols: 437573 processed, 2702 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1StringTableRemoved5Digits() {
        String logLine = "[2020-09-11T08:23:37.353+0000][11925659ms] GC(194) Cleaned string and symbol table, "
                + "strings: 368294 processed, 9531 removed, symbols: 1054532 processed, 18545 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1PauseCleanup() {
        String logLine = "[16.081s][info][gc,start      ] GC(969) Pause Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1MarkClosedArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, "
                + "0x00000000fff69ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1MarkOpenArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark open archive regions in map: [0x00000000ffe00000, "
                + "0x00000000ffe46ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineG1MmuTargetViolated() {
        String logLine = "[2020-06-24T18:11:22.155-0700][28150ms] GC(24) MMU target violated: 201.0ms "
                + "(200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSerialNewStart() {
        String logLine = "[0.118s][info][gc,start       ] GC(4) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSerialOldStart() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSerialOldStart7SpacesAfterStart() {
        String logLine = "[0.119s][info][gc,start       ] GC(5) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSerialOldInfo() {
        String logLine = "[0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldMarkingPhaseStart() {
        String logLine = "[0.083s][info][gc,phases,start] GC(3) Marking Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldMarkingPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOlSummaryPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Summary Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldSummaryPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldAdjustRootsStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Adjust Roots";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldAdjustRoots() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldCompactionPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Compaction Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldCompactionPhase() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldPostStart() {
        String logLine = "[0.087s][info][gc,phases,start] GC(3) Post Compact";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineParallelCompactingOldPostCompact() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSerialOldInfoTriggerErgonomics() {
        String logLine = "[0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms";
        String nextLogLine = "[0.092s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 0M->0M(3M) 1.849ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineSerialOldInfoTriggerSystemGc() {
        String logLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) Pause Full (System.gc()) 887M->583M(1223M) "
                + "3460.196ms";
        String nextLogLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) User=1.78s Sys=0.01s Real=3.46s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 887M->583M(1223M) 3460.196ms",event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineUnifiedYoungSingleLine() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(UnifiedPreprocessAction.match(logLine), "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineCmsInitialMark() {
        String logLine = "[0.053s][info][gc,start     ] GC(1) Pause Initial Mark";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineCmsOld() {
        String logLine = "[0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    public void testLogLineSafepointEnteringl() {
        String logLine = "[0.029s][info][safepoint    ] Entering safepoint region: EnableBiasedLocking";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSafepointLeaving() {
        String logLine = "[0.029s][info][safepoint    ] Leaving safepoint region";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testLogLineSafepointApplicationTime() {
        String logLine = "[0.030s][info][safepoint    ] Application time: 0.0012757 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    public void testPreprocessingG1YoungPauseNormalCollection() {
        File testFile = TestUtil.getFile("dataset155.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE), JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingG1Remark() {
        File testFile = TestUtil.getFile("dataset156.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK), JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingG1Cleanup() {
        File testFile = TestUtil.getFile("dataset157.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP), JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingG1YoungPauseNormalTriggerGcLockerWithDatestamps() {
        File testFile = TestUtil.getFile("dataset170.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE), JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingSerialNew() {
        File testFile = TestUtil.getFile("dataset171.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_NEW), JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingSerialOld() {
        File testFile = TestUtil.getFile("dataset172.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD), JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingSerialOldTriggerErgonomics() {
        File testFile = TestUtil.getFile("dataset174.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD), JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingParallelScavengeSerialOld() {
        File testFile = TestUtil.getFile("dataset173.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE), JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingParallelScavengeParallelCompactingOld() {
        File testFile = TestUtil.getFile("dataset175.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE), JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingParallelCompactingOld() {
        File testFile = TestUtil.getFile("dataset176.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD), JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingParNew() {
        File testFile = TestUtil.getFile("dataset177.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW), JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    @Test
    public void testPreprocessingCms() {
        File testFile = TestUtil.getFile("dataset178.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CMS_INITIAL_MARK), JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT), JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK), JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW), JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    /**
     * Verify that preprocessing logging that does not need preprocessing does not change logging.
     */
    @Test
    public void testPreprocessingG1Unecessarily() {
        File testFile = TestUtil.getFile("dataset186.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE), JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }
}
