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
public class TestShenandoahFinalEvacEvent extends TestCase {

    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:46.251-0400: 17.313: [Pause Final Evac, 0.009 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 17313, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 9, event.getDuration());
    }

    public void testLogLineUnified() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 10486 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 2, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalEvacEvent);
    }

    public void testBlocking() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC;
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        long timestamp = 521;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalEvacEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_EVAC);
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
    }

    public void testLogLineUnifiedDetailed() {
        String logLine = "[41.912s][info][gc           ] GC(1500) Pause Final Evac 0.022ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 41912 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 22, event.getDuration());
    }
}
