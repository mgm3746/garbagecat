/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedCmsInitialMarkEvent {

    @Test
    void testBlocking() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals((long) (178 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedCmsInitialMarkEvent,
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s] GC(4) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms] GC(4) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400] GC(4) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testUnifiedTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s] GC(4) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testUnifiedTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(157, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms     ";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
    }

    @Test
    void testWithTimesData() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals((long) (53 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(278, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}
