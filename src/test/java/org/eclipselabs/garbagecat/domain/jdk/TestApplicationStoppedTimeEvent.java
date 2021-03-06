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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationStoppedTimeEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    public void testLogLine() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 0,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(96845,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineWithSpacesAtEnd() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds  ";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    public void testLogLineJdk8() {
        String logLine = "1.977: Total time for which application threads were stopped: 0.0002054 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1977,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(205,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineJdk8Update40() {
        String logLine = "4.483: Total time for which application threads were stopped: 0.0018237 seconds, Stopping "
                + "threads took: 0.0017499 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 4483,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(1823,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineWithCommas() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1065,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(161,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineWithNegativeTime() {
        String logLine = "51185.692: Total time for which application threads were stopped: -0.0005950 seconds, "
                + "Stopping threads took: 0.0003310 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 51185692,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(-595,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestamp() {
        String logLine = "2015-05-04T18:08:00.244+0000: 0.964: Total time for which application threads were stopped: "
                + "0.0001390 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 964,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(139,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDoubleDatestamp() {
        String logLine = "2016-11-08T02:06:23.230-0800: 2016-11-08T02:06:23.230-0800: 8290.973: Total time for which "
                + "application threads were stopped: 0.2409380 seconds, Stopping threads took: 0.0001210 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 8290973,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(240938,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestampDatestampTimestampMisplacedSemicolonTimestamp() {
        String logLine = "2016-11-10T06:14:13.216-0500: 2016-11-10T06:14:13.216-0500672303.818: : 672303.818: Total "
                + "time for which application threads were stopped: 0.2934160 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 672303818,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(293416,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineTimestampDatestampTimestamp() {
        String logLine = "1514293.426: 2017-01-20T23:35:06.553-0500: 1514293.426: Total time for which application "
                + "threads were stopped: 0.0233250 seconds, Stopping threads took: 0.0000290 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1514293426,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(23325,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestampTimestampTimestampNoColon() {
        String logLine = "2017-01-20T23:57:51.722-0500: 1515658.594: 2017-01-20T23:57:51.722-0500Total time for which "
                + "application threads were stopped: 0.0245350 seconds, Stopping threads took: 0.0000460 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1515658594,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(24535,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestampDatestampTimestampTimestampNoColon() {
        String logLine = "2017-01-21T00:58:08.000-0500: 2017-01-21T00:58:08.000-0500: 1519274.873: 1519274.873Total "
                + "time for which application threads were stopped: 0.0220410 seconds, Stopping threads took: "
                + "0.0000290 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1519274873,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(22041,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineMislacedColonTimestampDatestampTimestampTimestamp() {
        String logLine = ": 1523468.7792017-01-21T02:08:01.907-0500: : 1523468.779: Total time for which application "
                + "threads were stopped: 0.0268610 seconds, Stopping threads took: 0.0000300 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1523468779,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(26861,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineMislacedColonDatestampTimestampTimestamp() {
        String logLine = ": 2017-01-21T04:11:12.565-0500: 1530859.438: 1530859.438: Total time for which application "
                + "threads were stopped: 0.0292690 seconds, Stopping threads took: 0.0000440 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1530859438,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(29269,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestampDatestampMisplacedColonTimestamp() {
        String logLine = "2017-01-21T07:44:22.453-05002017-01-21T07:44:22.453-0500: : 1543649.325: Total time for "
                + "which application threads were stopped: 0.0293060 seconds, Stopping threads took: "
                + "0.0000460 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1543649325,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(29306,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineDatestampDatestampMisplacedColonTimestampTimestamp() {
        String logLine = "2017-01-21T10:23:10.795-05002017-01-21T10:23:10.795-0500: : 1553177.667: 1553177.667: Total "
                + "time for which application threads were stopped: 0.0416440 seconds, Stopping threads took: "
                + "0.0000540 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 1553177667,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(41644,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineTruncated() {
        String logLine = "2017-02-01T05:58:45.570-0500: 5062.814: Total time for which application threads were "
                + "stopped: 0.0001140 seconds, Stopping thread";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 5062814,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(114,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineScrambledDateTimeStamps() {
        String logLine = "2017-02-20T20:15:54.487-0500: 2017-02-20T20:15:54.487-0500: 40371.69140371.691: : "
                + "Total time for which application threads were stopped: 0.0099233 seconds, Stopping threads took: "
                + "0.0000402 seconds";
        assertTrue(ApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 40371691,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(9923,event.getDuration(),"Duration not parsed correctly.");
    }
}
