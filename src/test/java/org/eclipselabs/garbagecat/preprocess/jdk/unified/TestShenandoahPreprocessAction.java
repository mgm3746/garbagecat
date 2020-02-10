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

    public void testLogLineInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersInitMark() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersInitMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
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

    public void testLogLineFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkCollectableGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, "
                + "21 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkImmediateGarbage() {
        String logLine = "[2019-02-05T14:47:52.726-0200][21638ms] GC(7) Immediate Garbage: 767M (97% of total), 1537 "
                + "regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkImmediateGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Immediate Garbage: 110M (37% of total), "
                + "230 regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkPacer() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
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

    public void testLogLineInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitUpdatePacer() {
        String logLine = "[69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 5.4x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
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

    public void testLogLineUsingWorkersConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
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

    public void testLogLineFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
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

    public void testLogLineEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
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

    public void testLogFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testBlankLineUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
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
