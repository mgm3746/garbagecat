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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestLogFileEvent {

    @Test
    void testNotBlocking() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testNotReportable() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testLogLineCreated() {
        String logLine = "2016-10-18 01:50:54 GC log file created /path/to/gc.log";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".");
    }

    @Test
    void testLogLineRotations() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".");
    }

    @Test
    void testLogLineRotationRequest() {
        String logLine = "2021-10-09 00:01:02 GC log rotation request has been received. Saved as "
                + "/path/to/gc.log.2021-10-08_21-57-44.0";
        assertTrue(LogFileEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".");
    }

    /**
     * Test preparsing throws event away.
     */
    @Test
    void testPreparsing() {
        File testFile = TestUtil.getFile("dataset88.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }
}
