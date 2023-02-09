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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUsingParallelEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        assertEquals(JdkUtil.LogEventType.USING_PARALLEL, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.USING_PARALLEL + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        assertTrue(UsingParallelEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        UsingParallelEvent event = new UsingParallelEvent(logLine);
        assertEquals((long) 2, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineUptimemillsis() {
        String logLine = "[18ms][info][gc] Using Parallel";
        assertTrue(UsingParallelEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        UsingParallelEvent event = new UsingParallelEvent(logLine);
        assertEquals((long) 18, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.002s][info][gc] Using Parallel     ";
        assertTrue(UsingParallelEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testLog() throws IOException {
        File testFile = TestUtil.getFile("dataset150.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.USING_PARALLEL.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.002s][info][gc] Using Parallel";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UsingParallelEvent,
                JdkUtil.LogEventType.USING_PARALLEL.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_PARALLEL),
                JdkUtil.LogEventType.USING_PARALLEL.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_PARALLEL);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.USING_PARALLEL.toString() + " not indentified as unified.");
    }
}
