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
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
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
public class TestUnifiedYoungEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "[1.102s][info][gc] GC(48) Pause Young (Allocation Failure) 23M->3M(25M) 0.409ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 9602 - 1, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 32 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 38 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[1.102s][info][gc] GC(48) Pause Young (Allocation Failure) 23M->3M(25M) 0.409ms     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
    }

    public void testTriggerExplicitGc() {
        String logLine = "[7.487s][info][gc] GC(497) Pause Young (System.gc()) 16M->10M(36M) 0.940ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 7487 - 0, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 16 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 10 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 36 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 0, event.getDuration());
    }

    public void testG1NormalTriggerEvacuationPause() {
        String logLine = "[27.091s][info][gc] GC(1515) Pause Young (Normal) (G1 Evacuation Pause) "
                + "43M->26M(52M) 0.941ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 27091 - 0, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 43 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 26 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 52 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 0, event.getDuration());
    }

    public void testG1ConcurrentStartTriggerEvacuationPause() {
        String logLine = "[22.879s][info][gc] GC(1277) Pause Young (Concurrent Start) (G1 Evacuation Pause)"
                + " 43M->23M(52M) 1.100ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 22879 - 1, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 43 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 23 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 52 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1, event.getDuration());
    }

    public void testUnifiedYoungStandardLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset149.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG));
        Assert.assertFalse(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING));
    }

    public void testUnifiedOldExplictGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset154.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }
}
