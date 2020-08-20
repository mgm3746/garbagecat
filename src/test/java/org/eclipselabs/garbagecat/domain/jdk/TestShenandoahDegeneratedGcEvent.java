/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahDegeneratedGcEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 52937 - 53, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 60 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 30 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 64 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 53697, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahDegeneratedGcMarkEvent);
    }

    public void testBlocking() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        Assert.assertTrue(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK;
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        long timestamp = 521;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahDegeneratedGcMarkEvent);
    }

    public void testReportable() {
        Assert.assertTrue(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_DEGENERATED_GC_MARK);
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) "
                + "53.697ms   ";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
    }

    public void testLogLineNotUnified() {
        String logLine = "2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 854868165, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 93058 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 29873 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 98304 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1285045, event.getDuration());
    }
}
