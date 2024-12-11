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
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestApplicationStoppedTimePreprocessAction {

    @Test
    void testBeginningColon() {
        String logLine = ": Total time for which application threads were stopped: 0.0017109 seconds, Stopping "
                + "threads took: 0.0000136 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals("Total time for which application threads were stopped: 0.0017109 seconds, Stopping threads took: "
                + "0.0000136 seconds", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestamp() {
        String logLine = "2021-10-28T07:39:54.391-0400: 2021-10-28T07:39:54.391-0400: Total time for which "
                + "application threads were stopped: 0.0014232 seconds, Stopping threads took: 0.0000111 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2021-10-28T07:39:54.391-0400: Total time for which application threads were stopped: 0.0014232 "
                        + "seconds, Stopping threads took: 0.0000111 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestampTimestamp() {
        String logLine = "2021-10-27T19:39:02.591-0400: 2021-10-27T19:39:02.591-0400: 0.210: Total time for which "
                + "application threads were stopped: 0.0007018 seconds, Stopping threads took: 0.0000202 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2021-10-27T19:39:02.591-0400: 0.210: Total time for which application threads were stopped: "
                        + "0.0007018 seconds, Stopping threads took: 0.0000202 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestampTimestampDoubleColon() {
        String logLine = "2021-10-27T10:52:38.345-0400: 2021-10-27T10:52:38.345-04000.181: : Total time for which "
                + "application threads were stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2021-10-27T10:52:38.345-0400: 0.181: Total time for which application threads were stopped: "
                        + "0.0013170 seconds, Stopping threads took: 0.0000454 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestampTimestampNoSpaceTimestamp() {
        String logLine = "2022-10-31T21:03:31.384+0800: 2022-10-31T21:03:31.384+0800: 492098.818492098.818: : Total "
                + "time for which application threads were stopped: 0.3765423 seconds, Stopping threads took: "
                + "0.0002408 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2022-10-31T21:03:31.384+0800: 492098.818: Total time for which application threads were stopped: "
                        + "0.3765423 seconds, Stopping threads took: 0.0002408 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestampTimestampTimestamp() {
        String logLine = "2021-10-28T07:41:40.468-0400: 2021-10-28T07:41:40.468-0400: 0.179: 0.179: Total time for "
                + "which application threads were stopped: 0.0012393 seconds, Stopping threads took: 0.0000233 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2021-10-28T07:41:40.468-0400: 0.179: Total time for which application threads were stopped: "
                        + "0.0012393 seconds, Stopping threads took: 0.0000233 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampDatestampTimestampTimestampNoColonNoSpace() {
        String logLine = "2022-10-30T08:28:23.839-0400: 2022-10-30T08:28:23.839-0400: 0.408: 0.408Total time for which "
                + "application threads were stopped: 0.0078201 seconds, Stopping threads took: 0.0000168 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2022-10-30T08:28:23.839-0400: 0.408: Total time for which application threads were stopped: 0.0078201 "
                        + "seconds, Stopping threads took: 0.0000168 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampNoColonDatestampDoubleColonTimestamp() {
        String logLine = "2022-11-01T22:22:52.436+08002022-11-01T22:22:52.436+0800: : 583259.869: Total time for which "
                + "application threads were stopped: 0.0590826 seconds, Stopping threads took: 0.0001473 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2022-11-01T22:22:52.436+0800: 583259.869: Total time for which application threads were stopped: "
                        + "0.0590826 seconds, Stopping threads took: 0.0001473 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampTimestampDatestamp() {
        String logLine = "2021-10-27T12:32:13.753-0400: 0.250: 2021-10-27T12:32:13.753-0400: Total time for which "
                + "application threads were stopped: 0.0012571 seconds, Stopping threads took: 0.0000262 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2021-10-27T12:32:13.753-0400: 0.250: Total time for which application threads were stopped: 0.0012571 "
                        + "seconds, Stopping threads took: 0.0000262 seconds",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDatestampTimestampDatestampTimestamp() {
        String logLine = "2022-11-01T22:19:41.968+0800: 583069.402: 2022-11-01T22:19:41.968+0800: 583069.402: Total "
                + "time for which application threads were stopped: 0.1477543 seconds, Stopping threads took: "
                + "0.0000903 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(
                "2022-11-01T22:19:41.968+0800: 583069.402: Total time for which application threads were stopped: "
                        + "0.1477543 seconds, Stopping threads took: 0.0000903 seconds",
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
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + LogEventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    void testNoPreprocessingNeeded() {
        String logLine = "2017-02-27T02:56:13.203+0300: 35952.084: Total time for which application threads were "
                + "stopped: 40.6810160 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimestamp() {
        String logLine = ": 492683.478: Total time for which application threads were stopped: 0.1442017 seconds, "
                + "Stopping threads took: 0.0001502 seconds";
        Set<String> context = new HashSet<String>();
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ApplicationStoppedTimePreprocessAction event = new ApplicationStoppedTimePreprocessAction(null, logLine, null,
                entangledLogLines, context, null);
        assertEquals("492683.478: Total time for which application threads were stopped: 0.1442017 seconds, Stopping "
                + "threads took: 0.0001502 seconds", event.getLogEntry(), "Log line not parsed correctly.");
    }
}
