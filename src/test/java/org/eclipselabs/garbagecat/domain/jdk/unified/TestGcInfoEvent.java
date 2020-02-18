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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcInfoEvent extends TestCase {

    public void testHumongousObjectThreshold() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testMaxTlabSize() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Max TLAB size: 512K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testGcThreads() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] GC threads: 4 parallel, 4 concurrent";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testGcThreadsInfo() {
        String logLine = "[0.006s][info][gc,init] GC threads: 2 parallel, 1 concurrent";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testReferenceProcessing() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Reference processing: parallel";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testReferenceProcessingInfo() {
        String logLine = "[0.006s][info][gc,init] Reference processing: parallel";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testHeuristicsExplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testHeuristicsShenandoahImplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets "
                + "-XX:+ShenandoahImplicitGCInvokesConcurrent";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testShanandoahHeuristics() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Shenandoah heuristics: adaptive";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testShanandoahHeuristicsInfo() {
        String logLine = "[0.006s][info][gc,init] Shenandoah heuristics: adaptive";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testInitializeShenandoahHeap() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Initialize Shenandoah heap with initial size "
                + "1366294528 bytes";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testInitializeShenandoahHeapInfo() {
        String logLine = "[0.007s][info][gc,init] Initialize Shenandoah heap: 32768K initial, 32768K min, 65536K max";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testPacerForIdle() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Pacer for Idle. Initial: 26M, Alloc Tax Rate: 1.0x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testPacerForIdleInfo() {
        String logLine = "[0.007s][info][gc,ergo] Pacer for Idle. Initial: 1310K, Alloc Tax Rate: 1.0x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testSafepointingMechanism() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Safepointing mechanism: global-page poll";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testSafepointingMechanismInfo() {
        String logLine = "[0.007s][info][gc,init] Safepointing mechanism: global-page poll";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testFree() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Free: 912M (1824 regions), Max regular: 512K, Max "
                + "humongous: 933376K, External frag: 1%, Internal frag: 0%";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testEvacuationReserve() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Evacuation Reserve: 65M (131 regions), Max regular: "
                + "512K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testRegions() {
        String logLine = "[0.006s][info][gc,init] Regions: 256 x 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testHumongous() {
        String logLine = "[0.006s][info][gc,init] Humongous object threshold: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testMaxTlab() {
        String logLine = "[0.006s][info][gc,init] Max TLAB size: 256K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".",
                GcInfoEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        Assert.assertEquals(JdkUtil.LogEventType.GC_INFO + "not identified.", JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        Assert.assertTrue(JdkUtil.LogEventType.GC_INFO.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof GcInfoEvent);
    }

    public void testNotBlocking() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        Assert.assertFalse(JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.GC_INFO));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.GC_INFO);
        Assert.assertTrue(JdkUtil.LogEventType.GC_INFO.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }
}
