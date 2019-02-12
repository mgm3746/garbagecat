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
public class TestShenandoahInitMarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 521 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 453, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahInitMarkEvent);
    }

    public void testBlocking() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_MARK;
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        long timestamp = 521;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahInitMarkEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_MARK);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
    }

    public void testLogLineProcessWeakrefs() {
        String logLine = "[0.456s][info][gc] GC(0) Pause Init Mark (process weakrefs) 0.868ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 456 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 868, event.getDuration());
    }

    public void testLogLineUpdateRefs() {
        String logLine = "[10.453s][info][gc] GC(279) Pause Init Mark (update refs) 0.244ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 10453 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 244, event.getDuration());
    }

    public void testLogLineUpdateRefsProcessWeakrefs() {
        String logLine = "[11.006s][info][gc] GC(300) Pause Init Mark (update refs) (process weakrefs) 0.266ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 11006 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 266, event.getDuration());
    }

    public void testLogLineDetailed() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Pause Init Mark (update refs) "
                + "(process weakrefs) 0.295ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 41893 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 295, event.getDuration());
    }
}
