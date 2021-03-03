/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestLogFileEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertFalse(JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testNotReportable() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertFalse(JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLineCreated() {
        String logLine = "2016-10-18 01:50:54 GC log file created /path/to/gc.log";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".",
                LogFileEvent.match(logLine));
    }

    @Test
    public void testLogLineRotations() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".",
                LogFileEvent.match(logLine));
    }

    /**
     * Test preparsing throws event away.
     */
    @Test
    public void testPreparsing() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset88.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 0, jvmRun.getEventTypes().size());
    }
}
