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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1YoungPauseEvent extends TestCase {

    public void testLogLinePreprocessed() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 15086 - 0, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 3771, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 3771, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1056768, event.getPermSpace());
        Assert.assertEquals("Combined begin size not parsed correctly.", 24 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 13 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 31 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 401, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungPauseEvent);
    }

    public void testIsBlocking() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE;
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedG1YoungPauseEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_PAUSE);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
    }

    public void testLogLinePreprocessedDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms User=0.02s Sys=0.01s Real=0.06s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 5355, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 26116, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 26116, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 278528, event.getPermSpace());
        Assert.assertEquals("Combined begin size not parsed correctly.", 65 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 8 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1304 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 57263, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 2, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 6, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 50, event.getParallelism());
    }

    public void testLogLinePreprocessedTimeUptimemillisTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "Metaspace: 35318K->35318K(288768K) 78M->22M(1304M) 35.722ms User=0.02s Sys=0.00s Real=0.04s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 11728, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 35318, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 35318, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 288768, event.getPermSpace());
        Assert.assertEquals("Combined begin size not parsed correctly.", 78 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 22 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1304 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 35722, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 2, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 50, event.getParallelism());
    }

    public void testUnifiedG1YoungPauseJdk9() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset158.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }

    public void testUnifiedG1YoungPauseDatestampMillis() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset166.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }
}
