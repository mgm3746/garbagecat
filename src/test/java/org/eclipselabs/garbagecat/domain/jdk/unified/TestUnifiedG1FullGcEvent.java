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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1FullGcEvent {

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 79853119, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(214096), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(214096), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(739328), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(megabytes(8186), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(8178), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(8192), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(2127343, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(1640, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(213, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(775, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        assertEquals(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedG1FullGcEvent,
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.G1_FULL_GC_PARALLEL;
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedG1FullGcEvent,
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.G1_FULL_GC_PARALLEL);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s   ";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853.119s] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8178M(8192M) 2127.343ms "
                + "User=16.40s Sys=0.09s Real=2.13s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString(), event.getName(), "Event name incorrect.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-03-13T03:45:44.425+0530][80337493ms] GC(9216) Pause Full (GCLocker Initiated GC) "
                + "Metaspace: 214103K->214103K(739328K) 8184M->8180M(8192M) 2101.341ms "
                + "User=16.34s Sys=0.05s Real=2.10s";
        assertTrue(UnifiedG1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
        UnifiedG1FullGcEvent event = new UnifiedG1FullGcEvent(logLine);
        assertEquals(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString(), event.getName(), "Event name incorrect.");
    }

    @Test
    void testPreprocessing() {
        File testFile = TestUtil.getFile("dataset203.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
    }
}
