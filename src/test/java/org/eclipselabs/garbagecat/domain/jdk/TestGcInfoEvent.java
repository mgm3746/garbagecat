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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcInfoEvent {

    @Test
    void testUnifiedHumongousObjectThreshold() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedMaxTlabSize() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Max TLAB size: 512K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedGcThreads() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] GC threads: 4 parallel, 4 concurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedGcThreadsInfo() {
        String logLine = "[0.006s][info][gc,init] GC threads: 2 parallel, 1 concurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedReferenceProcessing() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Reference processing: parallel";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedReferenceProcessingInfo() {
        String logLine = "[0.006s][info][gc,init] Reference processing: parallel";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testJdk8HeuristicsExplicitGcInvokesConcurrent() {
        String logLine = "Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedHeuristicsExplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testJdk8HeuristicsShenandoahImplicitGcInvokesConcurrent() {
        String logLine = "Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedHeuristicsShenandoahImplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets "
                + "-XX:+ShenandoahImplicitGCInvokesConcurrent";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testNonUnifiedShanandoahHeuristics() {
        String logLine = "Shenandoah heuristics: Adaptive";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedShanandoahHeuristics() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Shenandoah heuristics: adaptive";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedShanandoahHeuristicsInfo() {
        String logLine = "[0.006s][info][gc,init] Shenandoah heuristics: adaptive";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedInitializeShenandoahHeap() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Initialize Shenandoah heap with initial size "
                + "1366294528 bytes";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedInitializeShenandoahHeapInfo() {
        String logLine = "[0.007s][info][gc,init] Initialize Shenandoah heap: 32768K initial, 32768K min, 65536K max";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedPacerForIdle() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Pacer for Idle. Initial: 26M, Alloc Tax Rate: 1.0x";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedPacerForIdleInfo() {
        String logLine = "[0.007s][info][gc,ergo] Pacer for Idle. Initial: 1310K, Alloc Tax Rate: 1.0x";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedSafepointingMechanism() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Safepointing mechanism: global-page poll";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedSafepointingMechanismInfo() {
        String logLine = "[0.007s][info][gc,init] Safepointing mechanism: global-page poll";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testNonUnifiedFree() {
        String logLine = "Free: 12838K, Max: 256K regular, 10752K humongous, Frag: 7% external, 12% internal; "
                + "Reserve: 6656K, Max: 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedFree() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Free: 912M (1824 regions), Max regular: 512K, Max "
                + "humongous: 933376K, External frag: 1%, Internal frag: 0%";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedEvacuationReserve() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Evacuation Reserve: 65M (131 regions), Max regular: "
                + "512K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedRegions() {
        String logLine = "[0.006s][info][gc,init] Regions: 256 x 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedHumongous() {
        String logLine = "[0.006s][info][gc,init] Humongous object threshold: 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnifiedMaxTlab() {
        String logLine = "[0.006s][info][gc,init] Max TLAB size: 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof GcInfoEvent,
                JdkUtil.LogEventType.GC_INFO.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.GC_INFO);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testShenandoahGcMode() {
        String logLine = "Shenandoah GC mode: Snapshot-At-The-Beginning (SATB)";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testReferenceProcessing() {
        String logLine = "Reference processing: parallel discovery, parallel processing";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testRegions4Digits() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Regions: 2606 x 512K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }
}
