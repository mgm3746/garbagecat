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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahPreprocessAction {

    @Test
    public void testLogLineUnifiedInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8InitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersInitMark() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersInitMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for init marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLinePacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedPacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8FinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkUsingWorkers() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkAdaptiveCSet() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Adaptive CSet Selection. Target Free: 6M, Actual "
                + "Free: 14M, Max CSet: 2M, Min Garbage: 0M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFinalMarkCollectableGarbage() {
        String logLine = "    Collectable Garbage: 6237K (44% of total), 1430K CSet, 30 CSet regions";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkCollectableGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, "
                + "21 CSet regions";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkImmediateGarbage() {
        String logLine = "[2019-02-05T14:47:52.726-0200][21638ms] GC(7) Immediate Garbage: 767M (97% of total), 1537 "
                + "regions";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkImmediateGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Immediate Garbage: 110M (37% of total), "
                + "230 regions";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkPacer() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 1.1x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8FinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8InitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitUpdatePacer() {
        String logLine = "[69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 5.4x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8FinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFinalUpdateUsing() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingWorkersConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8ResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8CleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8PrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8EvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8UpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8ConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8ConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineJdk8ConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineUnifiedConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLinePacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLinePacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLinePacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLinePacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".",
                ShenandoahPreprocessAction.match(logLine));
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testUnifiedPreprocessingInitialMark() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset160.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK));
    }

    @Test
    public void testUnifiedPreprocessingFinalMark() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset161.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK));
    }

    @Test
    public void testUnifiedPreprocessingFinalEvac() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset162.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC));
    }

    @Test
    public void testUnifiedPreprocessingInitUpdate() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset163.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE));
    }

    @Test
    public void testUnifiedPreprocessingFinalUpdate() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset164.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE));
    }

    @Test
    public void testPreprocessingConcurrent() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset190.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }

    @Test
    public void testPreprocessingConcurrentWithMetaspace() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset191.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }

    @Test
    public void testPreprocessingConcurrentCancellingGc() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset194.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT));
    }
}
