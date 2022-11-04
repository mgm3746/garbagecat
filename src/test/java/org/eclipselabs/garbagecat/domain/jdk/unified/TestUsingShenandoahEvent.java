/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUsingShenandoahEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        assertEquals(JdkUtil.LogEventType.USING_SHENANDOAH, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.USING_SHENANDOAH + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        assertTrue(UsingShenandoahEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
        UsingShenandoahEvent event = new UsingShenandoahEvent(logLine);
        assertEquals((long) 6, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineDetailedLogging() {
        String logLine = "[0.005s][info][gc     ] Using Shenandoah";
        assertTrue(UsingShenandoahEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.006s][info][gc] Using Shenandoah    ";
        assertTrue(UsingShenandoahEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
    }

    @Test
    void testLineWithTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Using Shenandoah";
        assertTrue(UsingShenandoahEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
        UsingShenandoahEvent event = new UsingShenandoahEvent(logLine);
        assertEquals((long) 4, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testLog() throws IOException {
        File testFile = TestUtil.getFile("dataset159.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SHENANDOAH),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UsingShenandoahEvent,
                JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_SHENANDOAH),
                JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SHENANDOAH);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.USING_SHENANDOAH.toString() + " not indentified as unified.");
    }
}
