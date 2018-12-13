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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedCmsConcurrentEvent extends TestCase {

    public void testConcurrentMark() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT + "not identified.",
                JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedCmsConcurrentEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        Assert.assertFalse(
                JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_CONCURRENT);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testConcurrentMarkWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Mark 1.428ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentPreclean() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentPrecleanWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentSweep() {
        String logLine = "[0.084s][info][gc] GC(1) Concurrent Sweep";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentSweepWithDuration() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentReset() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Reset";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }

    public void testConcurrentResetWithDuration() {
        String logLine = "[0.086s][info][gc] GC(1) Concurrent Reset 0.841ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString() + ".",
                UnifiedCmsConcurrentEvent.match(logLine));
    }
}
