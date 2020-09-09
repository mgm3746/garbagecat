/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
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
public class TestUsingParallelEvent extends TestCase {

    public void testLine() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                UsingParallelEvent.match(logLine));
        UsingParallelEvent event = new UsingParallelEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2, event.getTimestamp());
    }

    public void testIdentityEventType() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        Assert.assertEquals(JdkUtil.LogEventType.USING_PARALLEL + "not identified.",
                JdkUtil.LogEventType.USING_PARALLEL, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        Assert.assertTrue(JdkUtil.LogEventType.USING_PARALLEL.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UsingParallelEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        Assert.assertFalse(JdkUtil.LogEventType.USING_PARALLEL.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.USING_PARALLEL.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.USING_PARALLEL));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_PARALLEL);
        Assert.assertTrue(JdkUtil.LogEventType.USING_PARALLEL.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineWithSpaces() {
        String logLine = "[0.002s][info][gc] Using Parallel     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                UsingParallelEvent.match(logLine));
    }

    public void testLineUptimemillsis() {
        String logLine = "[18ms][info][gc] Using Parallel";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                UsingParallelEvent.match(logLine));
        UsingParallelEvent event = new UsingParallelEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 18, event.getTimestamp());
    }

    /**
     * Test logging.
     */
    public void testLog() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset150.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL));
    }
}
