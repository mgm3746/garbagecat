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
public class TestShenandoahInitUpdateEvent extends TestCase {

    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:46.284-0400: 17.346: [Pause Init Update Refs, 0.017 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 17346, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }

    public void testLogLineUnified() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4766 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 4, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahInitUpdateEvent);
    }

    public void testBlocking() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE;
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        long timestamp = 521;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahInitUpdateEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_UPDATE);
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
    }

    public void testLogLineUnifiedDetailed() {
        String logLine = "[69.612s][info][gc           ] GC(2582) Pause Init Update Refs 0.036ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 69612 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 36, event.getDuration());
    }

    public void testLogLineUnifiedTimeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs 0.092ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3141 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 92, event.getDuration());
    }

    public void testLogLineUnifiedTimeUptime() {
        String logLine = "[2019-02-05T14:47:34.229-0200][4.766s] GC(0) Pause Init Update Refs 0.092ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4766 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 92, event.getDuration());
    }

    public void testLogLineUnifiedTime() {
        String logLine = "[2019-02-05T14:47:34.229-0200] GC(0) Pause Init Update Refs 0.092ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 602693254229L - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 92, event.getDuration());
    }
}
