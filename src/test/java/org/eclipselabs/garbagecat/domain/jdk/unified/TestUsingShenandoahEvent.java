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
public class TestUsingShenandoahEvent extends TestCase {

    public void testLine() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".",
                UsingShenandoahEvent.match(logLine));
        UsingShenandoahEvent event = new UsingShenandoahEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6, event.getTimestamp());
    }

    public void testIdentityEventType() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        Assert.assertEquals(JdkUtil.LogEventType.USING_SHENANDOAH + "not identified.",
                JdkUtil.LogEventType.USING_SHENANDOAH, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        Assert.assertTrue(JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UsingShenandoahEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        Assert.assertFalse(JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.USING_SHENANDOAH));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SHENANDOAH);
        Assert.assertTrue(JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineWithSpaces() {
        String logLine = "[0.006s][info][gc] Using Shenandoah    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".",
                UsingShenandoahEvent.match(logLine));
    }

    public void testLineDetailedLogging() {
        String logLine = "[0.005s][info][gc     ] Using Shenandoah";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".",
                UsingShenandoahEvent.match(logLine));
    }

    public void testLineWithTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Using Shenandoah";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".",
                UsingShenandoahEvent.match(logLine));
        UsingShenandoahEvent event = new UsingShenandoahEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4, event.getTimestamp());
    }

    /**
     * Test logging.
     */
    public void testLog() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset159.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SHENANDOAH));
    }
}
