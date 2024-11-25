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
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestOomeMetaspaceEvent {

    @Test
    void testClassAllocation() {
        String logLine = "[2024-05-06T13:01:18.988+0300][3619401490ms] Metaspace (class) allocation failed for size "
                + "1459";
        assertTrue(OomeMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.OOME_METASPACE.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertEquals(JdkUtil.LogEventType.OOME_METASPACE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.OOME_METASPACE + "not identified.");
    }

    @Test
    void testJdk17() throws IOException {
        File testFile = TestUtil.getFile("dataset288.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.OOME_METASPACE),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " event not identified.");
    }

    @Test
    void testLineTimeUptimeMillis() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(OomeMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.OOME_METASPACE.toString() + ".");
    }

    @Test
    void testLineUptimeMillis() {
        String logLine = "[7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(OomeMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.OOME_METASPACE.toString() + ".");
    }

    @Test
    void testLogLines() throws IOException {
        File testFile = TestUtil.getFile("dataset245.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.OOME_METASPACE),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " event not identified.");
    }

    @Test
    void testMetaspaceReclaimPolicy() {
        LogEvent priorLogEvent = new OomeMetaspaceEvent(null);
        String logLine = "[2024-05-06T13:40:30.238+0300][3621752739ms] MetaspaceReclaimPolicy: balanced";
        assertEquals(JdkUtil.LogEventType.OOME_METASPACE,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.OOME_METASPACE + "not identified.");
    }

    @Test
    void testBlankLine() {
        LogEvent priorLogEvent = new OomeMetaspaceEvent(null);
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms]";
        assertEquals(JdkUtil.LogEventType.OOME_METASPACE,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.OOME_METASPACE + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof OomeMetaspaceEvent,
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.OOME_METASPACE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " not indentified as unified.");
    }

    @Test
    void testUsage() {
        LogEvent priorLogEvent = new OomeMetaspaceEvent(null);
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Usage:";
        assertEquals(JdkUtil.LogEventType.OOME_METASPACE,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.OOME_METASPACE + "not identified.");
    }
}
