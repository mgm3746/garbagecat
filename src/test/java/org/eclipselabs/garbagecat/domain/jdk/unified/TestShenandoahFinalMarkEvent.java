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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahFinalMarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 531 - 1, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 1004, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalMarkEvent);
    }

    public void testBlocking() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK;
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        long timestamp = 456;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalMarkEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_MARK);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
    }

    public void testLogLineProcessWeakrefs() {
        String logLine = "[0.472s][info][gc] GC(0) Pause Final Mark (process weakrefs) 1.772ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 472 - 1, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 1772, event.getDuration());
    }

    public void testLogLineUpdateRefs() {
        String logLine = "[10.459s][info][gc] GC(279) Pause Final Mark (update refs) 0.253ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 10459 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 253, event.getDuration());
    }

    public void testLogLineUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Pause Final Mark (update refs) (process weakrefs) 0.200ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 11012 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 200, event.getDuration());
    }

    public void testLogLineDetailed() {
        String logLine = "[41.911s][info][gc           ] GC(1500) Pause Final Mark (update refs) (process weakrefs) "
                + "0.429ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 41911 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 429, event.getDuration());
    }

    public void testLogLineUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pause Final Mark (process weakrefs) 2.517ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3114 - 2, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 2517, event.getDuration());
    }
}
