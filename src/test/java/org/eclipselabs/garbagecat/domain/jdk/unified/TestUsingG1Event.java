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
class TestUsingG1Event {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.005s][info][gc] Using G1";
        assertEquals(JdkUtil.LogEventType.USING_G1, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.USING_G1 + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.005s][info][gc] Using G1";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
        UsingG1Event event = new UsingG1Event(logLine);
        assertEquals((long) 5, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineDatestampMillisNoLevelNoGc() {
        String logLine = "[2019-05-09T01:38:55.426+0000][18ms] Using G1";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
        UsingG1Event event = new UsingG1Event(logLine);
        assertEquals((long) 18, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineDatestampNoLevelNoGc() {
        String logLine = "[2019-05-09T01:38:55.426+0000] Using G1";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
        UsingG1Event event = new UsingG1Event(logLine);
        assertEquals(610663135426L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineDetailedLogging() {
        String logLine = "[0.003s][info][gc     ] Using G1";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
    }

    @Test
    void testLineUtcNoInfo() {
        String logLine = "[2022-08-03T06:58:37.056+0000][gc] Using G1";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
        UsingG1Event event = new UsingG1Event(logLine);
        assertEquals(712807117056L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.005s][info][gc] Using G1   ";
        assertTrue(UsingG1Event.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testLog() throws IOException {
        File testFile = TestUtil.getFile("dataset152.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_G1),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.005s][info][gc] Using G1";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.USING_G1.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.005s][info][gc] Using G1";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UsingG1Event,
                JdkUtil.LogEventType.USING_G1.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_G1),
                JdkUtil.LogEventType.USING_G1.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_G1);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.USING_G1.toString() + " not indentified as unified.");
    }
}
