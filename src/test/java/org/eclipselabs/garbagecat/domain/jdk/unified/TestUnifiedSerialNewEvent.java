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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedSerialNewEvent {

    @Test
    public void testPreprocessed() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 41, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", 983, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 128, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 1152, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 0, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 458, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 768, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 246, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 246, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 1056768, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 1393, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW + "not identified.",
                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedSerialNewEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SERIAL_NEW;
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedSerialNewEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SERIAL_NEW);
        assertTrue(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s   ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
    }

    @Test
    public void testLogLine7SpacesAfterStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure) DefNew: "
                + "1016K->128K(1152K) Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) "
                + "0.700ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".",
                UnifiedSerialNewEvent.match(logLine));
    }
}
