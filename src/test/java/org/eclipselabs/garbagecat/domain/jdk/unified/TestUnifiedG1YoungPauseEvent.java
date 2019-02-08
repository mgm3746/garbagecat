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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1YoungPauseEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 27091 - 0, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 43 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 26 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 52 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 941, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungPauseEvent);
    }

    public void testIsBlocking() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_PAUSE);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
    }

    public void testConcurrentStartCollectionTriggerEvacuationPause() {
        String logLine = "[22.879s][info][gc] GC(1277) Pause Young (Concurrent Start) (G1 Evacuation Pause)"
                + " 43M->23M(52M) 1.100ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 22879 - 1, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 43 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 23 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 52 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1100, event.getDuration());
    }

    public void testLogLinePreprocessed() {
        String logLine = "[0.370s][info][gc           ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 3M->2M(7M) "
                + "0.929ms User=0.01s Sys=0.00s Real=0.01s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 370 - 0, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 3 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 7 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 929, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testLogLinePreprocessed12Spaces() {
        String logLine = "[16.072s][info][gc            ] GC(971) Pause Young (Normal) (G1 Evacuation Pause) "
                + "38M->20M(46M) 2.040ms User=0.00s Sys=0.00s Real=0.01s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".",
                UnifiedG1YoungPauseEvent.match(logLine));
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 16072 - 2, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 38 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 20 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 46 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2040, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 0, event.getParallelism());
    }

    public void testUnifiedG1YoungPauseJdk9() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset158.txt");
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
