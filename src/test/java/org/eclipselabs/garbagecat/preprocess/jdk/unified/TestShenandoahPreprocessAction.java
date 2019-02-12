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
public class TestShenandoahPreprocessAction extends TestCase {

    public void testLogLineInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkUsingWorkers() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkPacer() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkPacerPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkUsingWorkers() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkAdaptiveCSet() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Adaptive CSet Selection. Target Free: 6M, Actual "
                + "Free: 14M, Max CSet: 2M, Min Garbage: 0M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkCollectableGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, "
                + "21 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkImmediateGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Immediate Garbage: 9M (33% of total), 37 regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkPacer() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitUpdatePacer() {
        String logLine = "[69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 5.4x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalUpdateUsing() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineTriggerAverageGc() {
        String logLine = "[41.917s][info][gc           ] Trigger: Average GC time (26.32 ms) is above the time for "
                + "allocation rate (324.68 MB/s) to deplete free headroom (8M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineTriggerFree() {
        String logLine = "[49.186s][info][gc           ] Trigger: Free (6M) is below minimum threshold (6M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testPreprocessingInitialMark() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset160.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK));
    }

    public void testPreprocessingFinalMark() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset161.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK));
    }

    public void testPreprocessingFinalEvac() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset162.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC));
    }

    public void testPreprocessingInitUpdate() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset163.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE));
    }

    public void testPreprocessingFinalUpdate() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset164.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE));
    }
}
