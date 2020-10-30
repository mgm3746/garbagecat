/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahPreprocessAction extends TestCase {

    public void testLogLineUnifiedInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8InitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersInitMark() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersInitMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for init marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedPacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8FinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkUsingWorkers() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkAdaptiveCSet() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Adaptive CSet Selection. Target Free: 6M, Actual "
                + "Free: 14M, Max CSet: 2M, Min Garbage: 0M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkCollectableGarbage() {
        String logLine = "    Collectable Garbage: 6237K (44% of total), 1430K CSet, 30 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkCollectableGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, "
                + "21 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkImmediateGarbage() {
        String logLine = "[2019-02-05T14:47:52.726-0200][21638ms] GC(7) Immediate Garbage: 767M (97% of total), 1537 "
                + "regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkImmediateGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Immediate Garbage: 110M (37% of total), "
                + "230 regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkPacer() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8FinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8InitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitUpdatePacer() {
        String logLine = "[69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 5.4x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8FinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFinalUpdateUsing() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingWorkersConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        Assert.assertFalse(
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8ResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8CleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8PrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8EvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8UpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8ConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8ConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineJdk8ConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineUnifiedConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLinePacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    public void testLogLineBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testUnifiedPreprocessingInitialMark() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset160.txt");
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

    public void testUnifiedPreprocessingFinalMark() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset161.txt");
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

    public void testUnifiedPreprocessingFinalEvac() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset162.txt");
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

    public void testUnifiedPreprocessingInitUpdate() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset163.txt");
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

    public void testUnifiedPreprocessingFinalUpdate() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset164.txt");
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

    public void testPreprocessingConcurrent() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset190.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }

    public void testPreprocessingConcurrentWithMetaspace() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset191.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }

    public void testPreprocessingConcurrentCancellingGc() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset194.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }
}
