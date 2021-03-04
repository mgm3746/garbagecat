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

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestSerialNewEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs]";
        assertTrue(JdkUtil.LogEventType.SERIAL_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                SerialNewEvent.match(logLine));
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 7798, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(37172), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(3631), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(39296), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(4505), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(6683), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(87424), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 21021, event.getDuration());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
                + "41677K->10314K(126720K), 0.0210210 secs] ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                SerialNewEvent.match(logLine));
    }

    @Test
    public void testLogLineNoSpaceAfterGC() {
        String logLine = "4.296: [GC4.296: [DefNew: 68160K->8512K(76672K), 0.0528470 secs] "
                + "68160K->11664K(1325760K), 0.0530640 secs] [Times: user=0.04 sys=0.00, real=0.05 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                SerialNewEvent.match(logLine));
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 4296, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(68160), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(8512), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(76672), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(68160 - 68160), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(11664 - 8512), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(1325760 - 76672), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 53064, event.getDuration());
    }

    @Test
    public void testLogLineDatestamp() {
        String logLine = "2016-11-22T09:07:01.358+0100: 1,319: [GC2016-11-22T09:07:01.359+0100: 1,320: [DefNew: "
                + "68160K->4425K(76672K), 0,0354890 secs] 68160K->4425K(3137216K), 0,0360580 secs] "
                + "[Times: user=0,04 sys=0,00, real=0,03 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                SerialNewEvent.match(logLine));
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1319, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(68160), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(4425), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(76672), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(68160 - 68160), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(4425 - 4425), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(3137216 - 76672), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 36058, event.getDuration());
    }

    @Test
    public void testLogLineWithTrigger() {
        String logLine = "2.218: [GC (Allocation Failure) 2.218: [DefNew: 209792K->15933K(235968K), 0.0848369 secs] "
                + "209792K->15933K(760256K), 0.0849244 secs] [Times: user=0.03 sys=0.06, real=0.08 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                SerialNewEvent.match(logLine));
        SerialNewEvent event = new SerialNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2218, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(209792), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(15933), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(235968), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(209792 - 209792), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(15933 - 15933), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(760256 - 235968), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 84924, event.getDuration());
    }
}
