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
public class TestUnifiedOldEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".",
                UnifiedOldEvent.match(logLine));
        UnifiedOldEvent event = new UnifiedOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 231 - 2, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        Assert.assertEquals("Combined begin size not parsed correctly.", 1 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 1 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 7 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2969, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_OLD + "not identified.", JdkUtil.LogEventType.UNIFIED_OLD,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedOldEvent);
    }

    public void testIsBlocking() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_OLD));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_OLD);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".",
                UnifiedOldEvent.match(logLine));
    }

    public void testUnifiedOldStandardLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset148.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD));
        Assert.assertFalse(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING));
    }

    public void testUnifiedOldExplictGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset153.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }
}
