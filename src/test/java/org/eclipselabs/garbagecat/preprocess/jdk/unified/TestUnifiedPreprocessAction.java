/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedPreprocessAction extends TestCase {

    public void testLogLinePauseYoungStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMarkStart() {
        String logLine = "[4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMark() {
        String logLine = "[4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseComputeStart() {
        String logLine = "[4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseCompute() {
        String logLine = "[4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseAdjustStart() {
        String logLine = "[4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseAdjust() {
        String logLine = "[4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMoveStart() {
        String logLine = "[4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMove() {
        String logLine = "[4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineDefNewData() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " DefNew: 1016K->128K(1152K)", event.getLogEntry());
    }

    public void testLogLineTenuredData() {
        String logLine = "[32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " Tenured: 24193K->24195K(25240K)", event.getLogEntry());
    }

    public void testLogLinePsYoungGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K->464K(1024K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " PSYoungGen: 512K->464K(1024K)", event.getLogEntry());
    }

    public void testLogLineParNewData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) ParNew: 974K->128K(1152K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " ParNew: 974K->128K(1152K)", event.getLogEntry());
    }

    public void testLogLineCmsData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) CMS: 0K->518K(960K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " CMS: 0K->518K(960K)", event.getLogEntry());
    }

    public void testLogLinePsOldGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSOldGen: 0K->8K(512K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " PSOldGen: 0K->8K(512K)", event.getLogEntry());
    }

    public void testLogLineMetaspaceData() {
        String logLine = "[0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K->120K(1056768K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " Metaspace: 120K->120K(1056768K)", event.getLogEntry());
    }

    public void testLogLineMetaspace2Spaces() {
        String logLine = "[16.072s][info][gc,metaspace  ] GC(971) Metaspace: 10793K->10793K(1058816K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParOldGenData() {
        String logLine = "[0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K->8K(512K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " ParOldGen: 0K->8K(512K)", event.getLogEntry());
    }

    public void testLogLineMetaspaceDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K->26116K(278528K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePauseYoungInfo() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        context.add(UnifiedPreprocessAction.TOKEN);
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 1M->1M(2M) 0.700ms", event.getLogEntry());
    }

    public void testLogLinePauseYoungInfoStandAlone() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", Constants.LINE_SEPARATOR + logLine, event.getLogEntry());
    }

    public void testLogLineG1PauseYoungInfo() {
        String logLine = "[0.337s][info][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 25M->4M(254M) 3.523ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 25M->4M(254M) 3.523ms", event.getLogEntry());
    }

    public void testLogLineG1MixedPauseInfo() {
        String logLine = "[16.630s][info][gc            ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "15M->12M(31M) 1.202ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 15M->12M(31M) 1.202ms", event.getLogEntry());
    }

    public void testLogLinePauseYoungInfoNormalWithDatestamp() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 65M->8M(1304M) 57.263ms", event.getLogEntry());
    }

    public void testLogLinePauseYoungInfoNormalTriggerGcLockerWithDatestamp() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 78M->22M(1304M) 35.722ms", event.getLogEntry());
    }

    public void testLogLinePauseYoungInfo11SpacesAfterGc() {
        String logLine = "[0.032s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.195ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " User=0.00s Sys=0.00s Real=0.00s", event.getLogEntry());
    }

    public void testLogLineTimesData8Spaces() {
        String logLine = "[16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineTimesDataDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNormal() {
        String logLine = "[0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNormal6Spaces() {
        String logLine = "[16.070s][info][gc,start      ] GC(971) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPrepareMixed() {
        String logLine = "[15.108s][info][gc,start      ] GC(1194) Pause Young (Prepare Mixed) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1MixedPause() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseConcurrentStart() {
        String logLine = "[16.600s][info][gc,start     ] GC(1032) Pause Young (Concurrent Start) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNoQualifier() {
        String logLine = "[0.333s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineStartG1YoungPauseNormalMillis8Digits() {
        String logLine = "[2019-05-09T04:31:19.449+0000][10344041ms] GC(9) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1UsingWorkersForEvacuation() {
        String logLine = "[0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1UsingWorkersForEvacuation2Digits() {
        String logLine = "[0.333s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1UsingWorkersForEvacuation7Spaces() {
        String logLine = "[16.070s][info][gc,task       ] GC(971) Using 2 workers of 4 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1UsingWorkersForEvacuationDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Using 1 workers of 1 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1UsingWorkersForMarking() {
        String logLine = "[16.121s][info][gc,task       ] GC(974) Using 1 workers of 1 for marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PreEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PreEvacuate5Spaces() {
        String logLine = "[16.071s][info][gc,phases     ] GC(971)   Pre Evacuate Collection Set: 0.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PreEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Evacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1EvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Evacuate Collection Set: 56.4ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PostEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PostEvacuate5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Post Evacuate Collection Set: 0.1ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PostEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Post Evacuate Collection Set: 0.5ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Other() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1OtherDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Other: 0.3ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Other5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Eden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Eden3Digits() {
        String logLine = "[0.335s][info][gc,heap      ] GC(0) Eden regions: 24->0(149)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1EdenDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Eden regions: 65->0(56)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Survivor() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1SurvivorDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Survivor regions: 0->9(9)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Old() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Old regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Old2Digits() {
        String logLine = "[10.989s][info][gc,heap      ] GC(684) Old regions: 15->15";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Old7Spaces() {
        String logLine = "[17.728s][info][gc,heap       ] GC(1098) Old regions: 21->21";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1OldDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Old regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Humongous() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Humongous regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1HumongousDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Humongous regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1YoungPauseNormal() {
        String logLine = "[0.101s][info][gc           ] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) 0M->0M(2M) 1.371ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1YoungPauseConcurrentStart() {
        String logLine = "[16.601s][info][gc           ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) 38M->20M(46M) 0.772ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogG1LineRemark() {
        String logLine = "[16.051s][info][gc,start     ] GC(969) Pause Remark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1Remark6Spaces() {
        String logLine = "[16.175s][info][gc,start      ] GC(974) Pause Remark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1StringTable() {
        String logLine = "[16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: "
                + "5786 processed, 4 removed, symbols: 38663 processed, 11 removed";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1PauseCleanup() {
        String logLine = "[16.081s][info][gc,start      ] GC(969) Pause Cleanup";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1MarkClosedArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, "
                + "0x00000000fff69ff8]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineG1MarkOpenArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark open archive regions in map: [0x00000000ffe00000, "
                + "0x00000000ffe46ff8]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSerialNewStart() {
        String logLine = "[0.118s][info][gc,start       ] GC(4) Pause Young (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSerialOldStart() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSerialOldStart7SpacesAfterStart() {
        String logLine = "[0.119s][info][gc,start       ] GC(5) Pause Full (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSerialOldInfo() {
        String logLine = "[0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldMarkingPhaseStart() {
        String logLine = "[0.083s][info][gc,phases,start] GC(3) Marking Phase";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldMarkingPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOlSummaryPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Summary Phase";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldSummaryPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldAdjustRootsStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Adjust Roots";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldAdjustRoots() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldCompactionPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Compaction Phase";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldCompactionPhase() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldPostStart() {
        String logLine = "[0.087s][info][gc,phases,start] GC(3) Post Compact";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineParallelCompactingOldPostCompact() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSerialOldInfoTriggerErgonomics() {
        String logLine = "[0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms";
        String nextLogLine = "[0.092s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", " 0M->0M(3M) 1.849ms", event.getLogEntry());
    }

    public void testLogLineUnifiedYoungSingleLine() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", Constants.LINE_SEPARATOR + logLine, event.getLogEntry());
    }

    public void testLogLineUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        Assert.assertFalse(
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineCmsInitialMark() {
        String logLine = "[0.053s][info][gc,start     ] GC(1) Pause Initial Mark";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertNull("Log line not parsed correctly.", event.getLogEntry());
    }

    public void testLogLineCmsOld() {
        String logLine = "[0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertNull("Log line not parsed correctly.", event.getLogEntry());
    }

    public void testLogLineSafepointEnteringl() {
        String logLine = "[0.029s][info][safepoint    ] Entering safepoint region: EnableBiasedLocking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSafepointLeaving() {
        String logLine = "[0.029s][info][safepoint    ] Leaving safepoint region";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineSafepointApplicationTime() {
        String logLine = "[0.030s][info][safepoint    ] Application time: 0.0012757 seconds";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testPreprocessingG1YoungPauseNormalCollection() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset155.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }

    public void testPreprocessingG1Remark() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset156.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK));
    }

    public void testPreprocessingG1Cleanup() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset157.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP));
    }

    public void testPreprocessingG1YoungPauseNormalTriggerGcLockerWithDatestamps() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset170.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }

    public void testPreprocessingSerialNew() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset171.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_NEW));
    }

    public void testPreprocessingSerialOld() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset172.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD));
    }

    public void testPreprocessingSerialOldTriggerErgonomics() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset174.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD));
    }

    public void testPreprocessingParallelScavengeSerialOld() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset173.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE));
    }

    public void testPreprocessingParallelScavengeParallelCompactingOld() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset175.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE));
    }

    public void testPreprocessingParallelCompactingOld() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset176.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD));
    }

    public void testPreprocessingParNew() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset177.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW));
    }

    public void testPreprocessingCms() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset178.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 4, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CMS_INITIAL_MARK));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CMS_CONCURRENT));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW));
    }
}
