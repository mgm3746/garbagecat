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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestSerialNewEvent {

    @Test
    void test() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals((long) 7798, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(37172), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(3631), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(39296), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(4505), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(6683), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(87424), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(21021, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testDatestamp() {
        String logLine = "2016-11-22T09:07:01.358+0100: [GC2016-11-22T09:07:01.359+0100: [DefNew: "
                + "68160K->4425K(76672K), 0,0354890 secs] 68160K->4425K(3137216K), 0,0360580 secs] "
                + "[Times: user=0,04 sys=0,00, real=0,03 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals(533099221358L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testDatestampTimestamp() {
        String logLine = "2016-11-22T09:07:01.358+0100: 1,319: [GC2016-11-22T09:07:01.359+0100: 1,320: [DefNew: "
                + "68160K->4425K(76672K), 0,0354890 secs] 68160K->4425K(3137216K), 0,0360580 secs] "
                + "[Times: user=0,04 sys=0,00, real=0,03 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals((long) 1319, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(68160), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(4425), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(76672), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(68160 - 68160), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(4425 - 4425), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(3137216 - 76672), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(36058, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SERIAL_NEW.toString() + " not indentified as blocking.");
    }

    @Test
    void testNoSpaceAfterGC() {
        String logLine = "4.296: [GC4.296: [DefNew: 68160K->8512K(76672K), 0.0528470 secs] "
                + "68160K->11664K(1325760K), 0.0530640 secs] [Times: user=0.04 sys=0.00, real=0.05 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals((long) 4296, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(68160), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(8512), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(76672), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(68160 - 68160), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(11664 - 8512), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(1325760 - 76672), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(53064, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(4, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(5, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(80, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestamp() {
        String logLine = "1,319: [GC1,320: [DefNew: 68160K->4425K(76672K), 0,0354890 secs] 68160K->4425K(3137216K), "
                + "0,0360580 secs] [Times: user=0,04 sys=0,00, real=0,03 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals((long) 1319, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs] ";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
    }

    @Test
    void testWithTrigger() {
        String logLine = "2.218: [GC (Allocation Failure) 2.218: [DefNew: 209792K->15933K(235968K), 0.0848369 secs] "
                + "209792K->15933K(760256K), 0.0849244 secs] [Times: user=0.03 sys=0.06, real=0.08 secs]";
        assertTrue(SerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals((long) 2218, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(209792), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(15933), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(235968), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(209792 - 209792), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(15933 - 15933), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(760256 - 235968), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(84924, event.getDurationMicros(), "Duration not parsed correctly.");
    }
}
