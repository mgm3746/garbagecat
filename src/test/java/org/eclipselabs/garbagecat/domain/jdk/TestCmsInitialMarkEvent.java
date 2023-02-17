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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestCmsInitialMarkEvent {

    @Test
    void testIsBlocking() {
        String logLine = "8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), "
                + "0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs]";
        assertTrue(CmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".");
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        assertEquals((long) 251763, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17443, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2016-10-10T18:43:50.728-0700: [GC (CMS Initial Mark) [1 CMS-initial-mark: "
                + "6993K(8218240K)] 26689K(8371584K), 0.0091989 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]";
        assertTrue(CmsInitialMarkEvent.match(logLine), "Log line not recognized as CMS Initial Mark event.");
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        assertEquals(529447430728L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2016-10-10T18:43:50.728-0700: 3.065: [GC (CMS Initial Mark) [1 CMS-initial-mark: "
                + "6993K(8218240K)] 26689K(8371584K), 0.0091989 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]";
        assertTrue(CmsInitialMarkEvent.match(logLine), "Log line not recognized as CMS Initial Mark event.");
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        assertEquals((long) 3065, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.CMS_INITIAL_MARK, "Trigger not parsed correctly.");
        assertEquals(9198, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(3, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(300, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineJdk8WithTrigger() {
        String logLine = "8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), "
                + "0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        assertTrue(CmsInitialMarkEvent.match(logLine), "Log line not recognized as CMS Initial Mark event.");
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        assertEquals((long) 8722, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.CMS_INITIAL_MARK, "Trigger not parsed correctly.");
        assertEquals(15789, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(6, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(300, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs]         ";
        assertTrue(CmsInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".");
    }

    @Test
    void testLogLineWithTimesData() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs] " + "[Times: user=0.02 sys=0.00, real=0.02 secs]";
        assertTrue(CmsInitialMarkEvent.match(logLine), "Log line not recognized as CMS Initial Mark event.");
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        assertEquals((long) 251763, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17443, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(2, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}
