/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcLockerScavengeFailedEvent {

    @Test
    void testIdentifyEventType() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue(
                JdkUtil.identifyEventType(logLine, null,
                        CollectorFamily.UNKNOWN) == JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED,
                JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED.toString() + " event not identified.");
    }

    @Test
    void testLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertTrue(GcLockerScavengeFailedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED.toString() + ".");
        GcLockerScavengeFailedEvent event = new GcLockerScavengeFailedEvent(logLine);
        assertEquals((long) 0, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        LogEvent event = JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN);
        assertTrue(event instanceof GcLockerScavengeFailedEvent,
                JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED.toString() + " event not identified.");
    }

    @Test
    void testReportable() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.GC_LOCKER_SCAVENGE_FAILED.toString() + " incorrectly indentified as reportable.");
    }
}
