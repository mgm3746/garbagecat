/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestG1RemarkEvent {

    public void TestG1RemarkPreprocessedEvent() {
        String logLine = "2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]";
        assertTrue(G1RemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
    }

    public void TestG1RemarkPreprocessedEventWhiteSpacesAtEnd() {
        String logLine = "2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]     ";
        assertTrue(G1RemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
    }

    @Test
    void testIsBlocking() {
        String logLine = "106.129: [GC remark, 0.0450170 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.G1_REMARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testRemark() {
        String logLine = "106.129: [GC remark, 0.0450170 secs]";
        assertTrue(G1RemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        assertEquals((long) 106129, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(45017, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testRemarkDatestamp() {
        String logLine = "2016-11-08T09:40:55.346-0800: [GC remark, 0.0827210 secs] "
                + "[Times: user=0.37 sys=0.00, real=0.08 secs]";
        assertTrue(G1RemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        assertEquals(531924055346L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testRemarkDatestampTimestamp() {
        String logLine = "2016-11-08T09:40:55.346-0800: 35563.088: [GC remark, 0.0827210 secs] "
                + "[Times: user=0.37 sys=0.00, real=0.08 secs]";
        assertTrue(G1RemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        assertEquals((long) 35563088, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(82721, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(37, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(8, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(463, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}
