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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestApplicationStoppedTimeEvent {

    @Test
    void testLogLine() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(96845, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2015-05-04T18:08:00.244+0000: Total time for which application threads were stopped: "
                + "0.0001390 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals(484060080244L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2015-05-04T18:08:00.244+0000: 0.964: Total time for which application threads were stopped: "
                + "0.0001390 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 964, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(139, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "1.977: Total time for which application threads were stopped: 0.0002054 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1977, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(205, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Update40() {
        String logLine = "4.483: Total time for which application threads were stopped: 0.0018237 seconds, Stopping "
                + "threads took: 0.0017499 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 4482, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1823, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTimestamp() {
        String logLine = "0.964: Total time for which application threads were stopped: 0.0001390 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 964, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWithCommas() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1065, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(161, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWithNegativeTime() {
        String logLine = "51185.692: Total time for which application threads were stopped: -0.0005950 seconds, "
                + "Stopping threads took: 0.0003310 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 51185692, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(-595, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWithSpacesAtEnd() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds  ";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " incorrectly indentified as blocking.");
    }
}
