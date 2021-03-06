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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcLockerEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.GC_LOCKER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    public void testReportable() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.GC_LOCKER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    public void testLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue(GcLockerEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.GC_LOCKER.toString() + ".");
        GcLockerEvent event = new GcLockerEvent(logLine);
        assertEquals((long) 0,event.getTimestamp(),"Time stamp not parsed correctly.");
    }

    @Test
    public void testParseLogLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        LogEvent event = JdkUtil.parseLogLine(logLine);
        assertTrue(event instanceof GcLockerEvent, JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.");
    }

    @Test
    public void testIdentifyEventType() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue(JdkUtil.identifyEventType(logLine) == JdkUtil.LogEventType.GC_LOCKER, JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.");
    }
}
