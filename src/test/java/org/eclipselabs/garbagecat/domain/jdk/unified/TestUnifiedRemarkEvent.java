/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedRemarkEvent {

    /**
     * Test with time, uptime decorator.
     * 
     * @throws IOException
     */
    @Test
    void testDecoratorTimeUptime() throws IOException {
        File testFile = TestUtil.getFile("dataset201.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_REMARK),
                JdkUtil.EventType.UNIFIED_REMARK.toString() + " event not identified.");

    }

    @Test
    void testIdentityEventType() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertEquals(JdkUtil.EventType.UNIFIED_REMARK,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_REMARK + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(UnifiedRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_REMARK.toString() + ".");
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals((long) (7944 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1767, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedWithTimesData() {
        String logLine = "[16.053s][info][gc            ] GC(969) Pause Remark 29M->29M(46M) 2.328ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_REMARK.toString() + ".");
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals((long) 16053, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(2328, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedWithTimesData12SpacesAfterGc() {
        String logLine = "[0.091s][info][gc           ] GC(3) Pause Remark 0M->0M(2M) 0.414ms User=0.00s "
                + "Sys=0.00s Real=0.00s";
        assertTrue(UnifiedRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_REMARK.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms           ";
        assertTrue(UnifiedRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_REMARK.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedRemarkEvent,
                JdkUtil.EventType.UNIFIED_REMARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_REMARK),
                JdkUtil.EventType.UNIFIED_REMARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_REMARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_REMARK.toString() + " not indentified as unified.");
    }
}
