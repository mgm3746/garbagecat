/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestFooterStatsEvent extends TestCase {

    public void testLine() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertEquals(JdkUtil.LogEventType.FOOTER_STATS + "not identified.", JdkUtil.LogEventType.FOOTER_STATS,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof FooterStatsEvent);
    }

    public void testNotBlocking() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_STATS));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.FOOTER_STATS);
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_STATS.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    /**
     * Test logging.
     */
    public void testUptimeMillis() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset165.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
    }
}
