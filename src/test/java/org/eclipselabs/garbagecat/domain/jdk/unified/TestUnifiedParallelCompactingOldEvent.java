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
public class TestUnifiedParallelCompactingOldEvent extends TestCase {

    public void testPreprocessed() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
        UnifiedParallelCompactingOldEvent event = new UnifiedParallelCompactingOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 83, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        Assert.assertEquals("Young begin size not parsed correctly.", 502, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 496, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1536, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 472, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 432, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2048, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 701, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 701, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1056768, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 4336, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testIdentityEventType() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD + "not identified.",
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedParallelCompactingOldEvent);
    }

    public void testIsBlocking() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        long timestamp = 27091;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedParallelCompactingOldEvent);
    }

    public void testReportable() {
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD);
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->496K(1536K) "
                + "ParOldGen: 472K->432K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 4.336ms "
                + "User=0.01s Sys=0.00s Real=0.01s    ";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
    }

    public void testLogLine7SpacesAfterStart() {
        String logLine = "[28.977s][info][gc,start       ] GC(2269) Pause Full (Ergonomics) PSYoungGen: "
                + "64K->0K(20992K) ParOldGen: 26612K->21907K(32768K) Metaspace: 3886K->3886K(1056768K) 26M->21M(52M) "
                + "48.135ms User=0.09s Sys=0.00s Real=0.05s";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".",
                UnifiedParallelCompactingOldEvent.match(logLine));
    }
}
