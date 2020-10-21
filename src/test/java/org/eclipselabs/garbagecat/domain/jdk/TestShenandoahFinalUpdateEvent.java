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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahFinalUpdateEvent extends TestCase {

    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:47.442-0400: 18.504: [Pause Final Update Refs, 0.206 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 18504, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 206, event.getDuration());
    }

    public void testLogLineUnified() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1030 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 97, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalUpdateEvent);
    }

    public void testBlocking() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE;
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        long timestamp = 521;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalUpdateEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_UPDATE);
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
    }

    public void testLogLineUnifiedDetailed() {
        String logLine = "[69.644s][info][gc           ] GC(2582) Pause Final Update Refs 0.302ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 69644 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 302, event.getDuration());
    }

    public void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.249-0200][3161ms] GC(0) Pause Final Update Refs 0.998ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3161 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 998, event.getDuration());
    }
}
