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

import org.junit.Test;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1CleanupEvent {

    @Test
    public void testLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".",
                UnifiedG1CleanupEvent.match(logLine));
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 15101 - 0, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(30 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(30 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(44 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 0, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1CleanupEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_CLEANUP);
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms     ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".",
                UnifiedG1CleanupEvent.match(logLine));
    }

    @Test
    public void testLogLinePreprocessed() {
        String logLine = "[16.082s][info][gc            ] GC(969) Pause Cleanup 28M->28M(46M) 0.064ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".",
                UnifiedG1CleanupEvent.match(logLine));
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 16082 - 0, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(28 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(28 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(46 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 0, event.getDuration());
        assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }
}
