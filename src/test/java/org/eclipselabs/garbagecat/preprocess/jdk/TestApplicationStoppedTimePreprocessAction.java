/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestApplicationStoppedTimePreprocessAction {

    @Test
    void testLogLineNoPreprocessingNeeded() {
        String priorLogLine = "";
        String logLine = "2017-02-27T02:56:13.203+0300: 35952.084: Total time for which application threads were "
                + "stopped: 40.6810160 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningColon() {
        String priorLogLine = "";
        String logLine = ": Total time for which application threads were stopped: 0.0017109 seconds, Stopping "
                + "threads took: 0.0000136 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals("Total time for which application threads were stopped: 0.0017109 seconds, Stopping threads took: "
                + "0.0000136 seconds", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDatestampDatestampTimestampDoubleColon() {
        String priorLogLine = "";
        String logLine = "2021-10-27T10:52:38.345-0400: 2021-10-27T10:52:38.345-04000.181: : Total time for which "
                + "application threads were stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(
                "2021-10-27T10:52:38.345-0400: 0.181: Total time for which application threads were stopped: "
                        + "0.0013170 seconds, Stopping threads took: 0.0000454 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDatestampDatestampTimestamp() {
        String priorLogLine = "";
        String logLine = "2021-10-27T19:39:02.591-0400: 2021-10-27T19:39:02.591-0400: 0.210: Total time for which "
                + "application threads were stopped: 0.0007018 seconds, Stopping threads took: 0.0000202 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(
                "2021-10-27T19:39:02.591-0400: 0.210: Total time for which application threads were stopped: "
                        + "0.0007018 seconds, Stopping threads took: 0.0000202 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDatestampTimestampDatestamp() {
        String priorLogLine = "";
        String logLine = "2021-10-27T12:32:13.753-0400: 0.250: 2021-10-27T12:32:13.753-0400: Total time for which "
                + "application threads were stopped: 0.0012571 seconds, Stopping threads took: 0.0000262 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(
                "2021-10-27T12:32:13.753-0400: 0.250: Total time for which application threads were stopped: 0.0012571 "
                        + "seconds, Stopping threads took: 0.0000262 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDatestampDatestampTimestampTimestamp() {
        String priorLogLine = "";
        String logLine = "2021-10-28T07:41:40.468-0400: 2021-10-28T07:41:40.468-0400: 0.179: 0.179: Total time for "
                + "which application threads were stopped: 0.0012393 seconds, Stopping threads took: 0.0000233 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(
                "2021-10-28T07:41:40.468-0400: 0.179: Total time for which application threads were stopped: "
                        + "0.0012393 seconds, Stopping threads took: 0.0000233 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineDatestampDatestamp() {
        String priorLogLine = "";
        String logLine = "2021-10-28T07:39:54.391-0400: 2021-10-28T07:39:54.391-0400: Total time for which "
                + "application threads were stopped: 0.0014232 seconds, Stopping threads took: 0.0000111 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(priorLogLine, logLine,
                nextLogLine, entangledLogLines, context);
        assertEquals(
                "2021-10-28T07:39:54.391-0400: Total time for which application threads were stopped: 0.0014232 "
                        + "seconds, Stopping threads took: 0.0000111 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1() throws IOException {
        File testFile = TestUtil.getFile("dataset223.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + LogEventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }
}
