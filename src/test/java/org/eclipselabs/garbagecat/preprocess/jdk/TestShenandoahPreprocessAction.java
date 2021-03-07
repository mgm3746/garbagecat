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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahPreprocessAction {

    @Test
    void testLogLineUnifiedInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8InitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersInitMark() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersInitMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for init marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedPacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8FinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkUsingWorkers() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkAdaptiveCSet() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Adaptive CSet Selection. Target Free: 6M, Actual "
                + "Free: 14M, Max CSet: 2M, Min Garbage: 0M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkCollectableGarbage() {
        String logLine = "    Collectable Garbage: 6237K (44% of total), 1430K CSet, 30 CSet regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkCollectableGarbage() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, "
                + "21 CSet regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkImmediateGarbage() {
        String logLine = "[2019-02-05T14:47:52.726-0200][21638ms] GC(7) Immediate Garbage: 767M (97% of total), 1537 "
                + "regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkImmediateGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Immediate Garbage: 110M (37% of total), "
                + "230 regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkPacer() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8FinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8InitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitUpdatePacer() {
        String logLine = "[69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, "
                + "Non-Taxable: 1M, Alloc Tax Rate: 5.4x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8FinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFinalUpdateUsing() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingWorkersConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(ShenandoahPreprocessAction.match(logLine), "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8ResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8CleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8PrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8EvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8UpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8ConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8ConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineJdk8ConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine,event.getLogEntry(),"Log line not parsed correctly.");
    }

    @Test
    void testUnifiedPreprocessingInitialMark() {
        File testFile = TestUtil.getFile("dataset160.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK), JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedPreprocessingFinalMark() {
        File testFile = TestUtil.getFile("dataset161.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK), JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedPreprocessingFinalEvac() {
        File testFile = TestUtil.getFile("dataset162.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC), JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedPreprocessingInitUpdate() {
        File testFile = TestUtil.getFile("dataset163.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE), JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedPreprocessingFinalUpdate() {
        File testFile = TestUtil.getFile("dataset164.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE), JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrent() {
        File testFile = TestUtil.getFile("dataset190.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT), JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentWithMetaspace() {
        File testFile = TestUtil.getFile("dataset191.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT), JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentCancellingGc() {
        File testFile = TestUtil.getFile("dataset194.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT), JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }
}
