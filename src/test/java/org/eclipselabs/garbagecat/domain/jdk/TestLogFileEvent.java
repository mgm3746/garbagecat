/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestLogFileEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        Assert.assertFalse(JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testNotReportable() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        Assert.assertFalse(JdkUtil.LogEventType.LOG_FILE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLineCreated() {
        String logLine = "2016-10-18 01:50:54 GC log file created /path/to/gc.log";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".",
                LogFileEvent.match(logLine));
    }

    public void testLogLineRotations() {
        String logLine = "2016-03-24 10:28:33 GC log file has reached the maximum size. "
                + "Saved as /path/to/gc.log.0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.LOG_FILE.toString() + ".",
                LogFileEvent.match(logLine));
    }

    /**
     * Test preparsing throws event away.
     */
    public void testPreparsing() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset88.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun.getEventTypes().size());
    }
}
