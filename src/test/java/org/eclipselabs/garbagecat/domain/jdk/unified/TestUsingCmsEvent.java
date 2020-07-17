/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUsingCmsEvent extends TestCase {

    public void testLine() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".",
                UsingCmsEvent.match(logLine));
        UsingCmsEvent event = new UsingCmsEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3, event.getTimestamp());
    }

    public void testIdentityEventType() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        Assert.assertEquals(JdkUtil.LogEventType.USING_CMS + "not identified.", JdkUtil.LogEventType.USING_CMS,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        Assert.assertTrue(JdkUtil.LogEventType.USING_CMS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UsingCmsEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep";
        Assert.assertFalse(JdkUtil.LogEventType.USING_CMS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.USING_CMS.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.USING_CMS));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_CMS);
        Assert.assertTrue(JdkUtil.LogEventType.USING_CMS.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineWithSpaces() {
        String logLine = "[0.003s][info][gc] Using Concurrent Mark Sweep     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".",
                UsingCmsEvent.match(logLine));
    }

    /**
     * Test logging.
     */
    public void testLog() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset151.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_CMS.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_CMS));
    }
}
