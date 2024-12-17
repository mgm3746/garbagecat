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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestLogFileEvent {

    @Test
    void testLogLineCreated() {
        String logLine = "2016-10-18 01:50:54 GC log file created /path/to/gc.log";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.LOG_FILE.toString() + ".");
        LogFileEvent event = new LogFileEvent(logLine);
        assertTrue(event.isCreated(), "Log line not recognized as created event.");
    }

    @Test
    void testLogLineRotationRequest() {
        String logLine = "2021-10-09 00:01:02 GC log rotation request has been received. Saved as "
                + "/path/to/gc.log.2021-10-08_21-57-44.0";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.LOG_FILE.toString() + ".");
        LogFileEvent event = new LogFileEvent(logLine);
        assertFalse(event.isCreated(), "Log line incorrectly recognized as created event.");
    }

    @Test
    void testLogLineRotations() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.LOG_FILE.toString() + ".");
    }

    @Test
    void testNotReportable() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.LOG_FILE.toString() + " incorrectly indentified as reportable.");
    }

    /**
     * Test preparsing throws event away.
     * 
     * @throws IOException
     */
    @Test
    void testPreparsing() throws IOException {
        File testFile = TestUtil.getFile("dataset88.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        Date logFileDate = GcUtil.parseDatetime("2016-10-18 01:50:54");
        assertEquals(logFileDate, jvmRun.getLogFileDate(), "Log file date not correct.");
    }
}
