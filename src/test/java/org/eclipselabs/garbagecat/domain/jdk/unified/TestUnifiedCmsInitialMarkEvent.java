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
public class TestUnifiedCmsInitialMarkEvent {

    @Test
    public void testLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".",
                UnifiedCmsInitialMarkEvent.match(logLine));
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 178 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 157, event.getDuration());
        assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedCmsInitialMarkEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms     ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".",
                UnifiedCmsInitialMarkEvent.match(logLine));
    }

    @Test
    public void testLogLineWithTimesData() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".",
                UnifiedCmsInitialMarkEvent.match(logLine));
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 53 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 278, event.getDuration());
        assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }
}
