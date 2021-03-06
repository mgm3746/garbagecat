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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedRemarkEvent {

    @Test
    public void testLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 7944 - 1, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 1767, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_REMARK + "not identified.", JdkUtil.LogEventType.UNIFIED_REMARK,
                JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedRemarkEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_REMARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_REMARK);
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms           ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
    }

    @Test
    public void testLogLinePreprocessedWithTimesData() {
        String logLine = "[16.053s][info][gc            ] GC(969) Pause Remark 29M->29M(46M) 2.328ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 16053 - 2, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 2328, event.getDuration());
        assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", Integer.MAX_VALUE, event.getParallelism());
    }

    @Test
    public void testLogLinePreprocessedWithTimesData12SpacesAfterGc() {
        String logLine = "[0.091s][info][gc           ] GC(3) Pause Remark 0M->0M(2M) 0.414ms User=0.00s "
                + "Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
    }

}
