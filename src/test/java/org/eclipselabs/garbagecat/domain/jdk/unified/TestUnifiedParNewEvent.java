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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedParNewEvent extends TestCase {

    public void testPreprocessed() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".",
                UnifiedParNewEvent.match(logLine));
        UnifiedParNewEvent event = new UnifiedParNewEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 49, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 974, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 128, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1152, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 0, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 518, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 960, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 250, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 250, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1056768, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3544, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 200, event.getParallelism());
    }

    public void testIdentityEventType() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_PAR_NEW + "not identified.",
                JdkUtil.LogEventType.UNIFIED_PAR_NEW, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedParNewEvent);
    }

    public void testIsBlocking() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PAR_NEW;
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedParNewEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PAR_NEW));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PAR_NEW);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + ".",
                UnifiedParNewEvent.match(logLine));
    }
}
