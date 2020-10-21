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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1YoungInitialMarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
        UnifiedG1YoungInitialMarkEvent event = new UnifiedG1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2752 - 1, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION));
        Assert.assertEquals("Combined begin size not parsed correctly.", 562 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 5 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1250 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1212, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungInitialMarkEvent);
    }

    public void testIsBlocking() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK);
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
    }

    public void testLogLine11Spaces() {
        String logLine = "[2.727s][info][gc           ] GC(51) Pause Initial Mark (G1 Humongous Allocation) "
                + "1162M->5M(1250M) 1.336ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
    }
}
