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
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedSerialNewEvent extends TestCase {

    public void testPreprocessed() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString(),
                event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 41, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 983, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 128, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1152, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 0, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 458, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 768, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 246, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 246, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1056768, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1393, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW + "not identified.",
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedSerialNewEvent);
    }

    public void testIsBlocking() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SERIAL_NEW;
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedSerialNewEvent);
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SERIAL_NEW);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
    }

    public void testLogLine7SpacesAfterStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure) DefNew: "
                + "1016K->128K(1152K) Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) "
                + "0.700ms User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
    }
}
