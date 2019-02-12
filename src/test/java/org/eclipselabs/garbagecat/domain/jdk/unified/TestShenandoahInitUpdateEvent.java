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
public class TestShenandoahInitUpdateEvent extends TestCase {

    public void testLogLine() {
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
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
    }

    public void testLogLineDetailed() {
        String logLine = "[69.612s][info][gc           ] GC(2582) Pause Init Update Refs 0.036ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".",
                ShenandoahInitUpdateEvent.match(logLine));
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 69612 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 36, event.getDuration());
    }
}
