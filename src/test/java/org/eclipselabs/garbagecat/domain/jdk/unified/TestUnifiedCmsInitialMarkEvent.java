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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedCmsInitialMarkEvent {

    @Test
    public void testLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals((long) (178 - 0),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(157,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(0,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(0,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(100,event.getParallelism(),"Parallelism not calculated correctly.");
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK + "not identified.");
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedCmsInitialMarkEvent, JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not parsed.");
    }

    @Test
    public void testBlocking() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK), JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as unified.");
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms     ";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
    }

    @Test
    public void testLogLineWithTimesData() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedCmsInitialMarkEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".");
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        assertEquals((long) (53 - 0),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(278,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(0,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(0,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(100,event.getParallelism(),"Parallelism not calculated correctly.");
    }
}
