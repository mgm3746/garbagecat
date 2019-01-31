/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
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

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1PreprocessAction extends TestCase {

    public void testLogLineStartPauseYoungNormal() {
        String logLine = "[0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineStartPauseYoungNormal6Spaces() {
        String logLine = "[16.070s][info][gc,start      ] GC(971) Pause Young (Normal) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineStartPauseYoungConcurrentStart() {
        String logLine = "[16.600s][info][gc,start     ] GC(1032) Pause Young (Concurrent Start) (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineStartPauseYoungNoTrigger() {
        String logLine = "[0.333s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersForEvacuation() {
        String logLine = "[0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersForEvacuation2Digits() {
        String logLine = "[0.333s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersForEvacuation7Spaces() {
        String logLine = "[16.070s][info][gc,task       ] GC(971) Using 2 workers of 4 for evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersForMarking() {
        String logLine = "[16.121s][info][gc,task       ] GC(974) Using 1 workers of 1 for marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePreEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePreEvacuate5Spaces() {
        String logLine = "[16.071s][info][gc,phases     ] GC(971)   Pre Evacuate Collection Set: 0.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePostEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePostEvacuate5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Post Evacuate Collection Set: 0.1ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineOther() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineOther5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineEden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineEden3Digits() {
        String logLine = "[0.335s][info][gc,heap      ] GC(0) Eden regions: 24->0(149)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineSurvivor() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineOld() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Old regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineOld2Digits() {
        String logLine = "[10.989s][info][gc,heap      ] GC(684) Old regions: 15->15";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineOld7Spaces() {
        String logLine = "[17.728s][info][gc,heap       ] GC(1098) Old regions: 21->21";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineHumongous() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Humongous regions: 0->0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineMetaspace() {
        String logLine = "[0.101s][info][gc,metaspace ] GC(0) Metaspace: 4463K->4463K(1056768K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineMetaspace2Spaces() {
        String logLine = "[16.072s][info][gc,metaspace  ] GC(971) Metaspace: 10793K->10793K(1058816K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineGcPauseYoungNormal() {
        String logLine = "[0.101s][info][gc           ] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) 0M->0M(2M) 1.371ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineGcPauseYoungConcurrentStart() {
        String logLine = "[16.601s][info][gc           ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) 38M->20M(46M) 0.772ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineTimesData() {
        String logLine = "[16.601s][info][gc,cpu       ] GC(1032) User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineTimesData8Spaces() {
        String logLine = "[16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineRemark() {
        String logLine = "[16.051s][info][gc,start     ] GC(969) Pause Remark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineRemark6Spaces() {
        String logLine = "[16.175s][info][gc,start      ] GC(974) Pause Remark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLineStringTable() {
        String logLine = "[16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: "
                + "5786 processed, 4 removed, symbols: 38663 processed, 11 removed";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePauseCleanup() {
        String logLine = "[16.081s][info][gc,start      ] GC(969) Pause Cleanup";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testLogLinePauseCleanupWithData() {
        String logLine = "[16.082s][info][gc            ] GC(969) Pause Cleanup 28M->28M(46M) 0.064ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED_G1.toString() + ".",
                UnifiedG1PreprocessAction.match(logLine));
    }

    public void testPreprocessingYoungNormalCollection() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset155.txt");
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

    public void testPreprocessingRemark() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset156.txt");
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

    public void testPreprocessingCleanup() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset157.txt");
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
}
