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
        Assert.assertEquals("Duration not parsed correctly.", 1812, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_YOUNG + "not identified.", JdkUtil.LogEventType.UNIFIED_YOUNG,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedYoungEvent);
    }

    public void testIsBlocking() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_YOUNG));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_YOUNG);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
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
        Assert.assertEquals("Duration not parsed correctly.", 940, event.getDuration());
    }

    public void testTimesData() {
        String logLine = "[0.042s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.393ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                UnifiedYoungEvent.match(logLine));
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 42 - 1, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 0 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 0 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1393, event.getDuration());
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

    public void testUnifiedYoungExplictGc() {
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
