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
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcLockerEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.LogEventType.GC_LOCKER.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.LogEventType.GC_LOCKER.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_LOCKER.toString() + ".",
                GcLockerEvent.match(logLine));
        GcLockerEvent event = new GcLockerEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 0, event.getTimestamp());
    }

    @Test
    public void testParseLogLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        LogEvent event = JdkUtil.parseLogLine(logLine);
        assertTrue(JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.",
                event instanceof GcLockerEvent);
    }

    @Test
    public void testIdentifyEventType() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue(JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.",
                JdkUtil.identifyEventType(logLine) == JdkUtil.LogEventType.GC_LOCKER);
    }
}
