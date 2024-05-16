/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcInfoEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "Pacer for Idle. Initial: 122M, Alloc Tax Rate: 1.0x";
        assertEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testJdk8HeuristicsExplicitGcInvokesConcurrent() {
        String logLine = "Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent";
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
    void testNonUnifiedShanandoahHeuristics() {
        String logLine = "Shenandoah heuristics: Adaptive";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testNotUnifiedFrag100() {
        String logLine = "Free: 88400K, Max: 256K regular, 768K humongous, Frag: 100% external, 5% internal; "
                + "Reserve: 6624K, Max: 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testNotUnifiedFree() {
        String logLine = "Free: 12838K, Max: 256K regular, 10752K humongous, Frag: 7% external, 12% internal; "
                + "Reserve: 6656K, Max: 256K";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "Pacer for Idle. Initial: 122M, Alloc Tax Rate: 1.0x";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof GcInfoEvent,
                JdkUtil.LogEventType.GC_INFO.toString() + " not parsed.");
    }

    @Test
    void testReferenceProcessing() {
        String logLine = "Reference processing: parallel discovery, parallel processing";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testShenandoahGcMode() {
        String logLine = "Shenandoah GC mode: Snapshot-At-The-Beginning (SATB)";
        assertTrue(GcInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_INFO.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.GC_INFO);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly indentified as unified.");
    }
}
