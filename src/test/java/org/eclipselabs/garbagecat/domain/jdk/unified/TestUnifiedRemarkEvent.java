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
public class TestUnifiedRemarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 7944 - 1, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 1, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_REMARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_REMARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedRemarkEvent);
    }

    public void testIsBlocking() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_REMARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_REMARK);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms           ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
    }

    public void testLogLineDetailedPreprocessed() {
        String logLine = "[16.053s][info][gc            ] GC(969) Pause Remark 29M->29M(46M) 2.328ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 16053 - 2, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 2, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", Integer.MAX_VALUE, event.getParallelism());
    }
}
