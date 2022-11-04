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
class TestUsingCmsEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        assertEquals(JdkUtil.LogEventType.USING_CMS, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.USING_CMS + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        assertTrue(UsingCmsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".");
        UsingCmsEvent event = new UsingCmsEvent(logLine);
        assertEquals((long) 3, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep     ";
        assertTrue(UsingCmsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testLog() throws IOException {
        File testFile = TestUtil.getFile("dataset151.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_CMS),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".");
    }

    /**
     * Test with uptime decorator.
     */
    @Test
    void testMillis() {
        String logLine = "[3ms] GC(6) Using Concurrent Mark Sweep";
        assertTrue(UsingCmsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".");
        UsingCmsEvent event = new UsingCmsEvent(logLine);
        assertEquals(JdkUtil.LogEventType.USING_CMS.toString(), event.getName(), "Event name incorrect.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.USING_CMS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UsingCmsEvent,
                JdkUtil.LogEventType.USING_CMS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_CMS),
                JdkUtil.LogEventType.USING_CMS.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_CMS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.USING_CMS.toString() + " not indentified as unified.");
    }
}
