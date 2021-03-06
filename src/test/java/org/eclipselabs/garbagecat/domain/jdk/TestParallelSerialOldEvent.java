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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelSerialOldEvent {

    @Test
    public void testLogLine() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldEvent.match(logLine));
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3600, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(5424), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(38208), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(488), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(5786), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(87424), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(13092), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(13094), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(131072), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 69936, event.getDuration());
    }

    @Test
    public void testLogLineWhiteSpaceAtEnd() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldEvent.match(logLine));
    }

    @Test
    public void testLogLineJdk16() {
        String logLine = "4.165: [Full GC (System) [PSYoungGen: 1784K->0K(12736K)] "
                + "[PSOldGen: 1081K->2855K(116544K)] 2865K->2855K(129280K) "
                + "[PSPermGen: 8600K->8600K(131072K)], 0.0427680 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldEvent.match(logLine));
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 4165, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.SYSTEM_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1784), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(12736), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1081), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(2855), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(116544), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(8600), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(8600), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(131072), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 42768, event.getDuration());
    }

    @Test
    public void testLogLineTriggerErgonomicsWithMetaspace() {
        String logLine = "2018-12-06T19:04:46.807-0500: 0.122: [Full GC (Ergonomics) [PSYoungGen: 508K->385K(1536K)] "
                + "[PSOldGen: 408K->501K(2048K)] 916K->887K(3584K), "
                + "[Metaspace: 3680K->3680K(1056768K)], 0.0030057 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldEvent.match(logLine));
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 122, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.ERGONOMICS.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        assertEquals("Young begin size not parsed correctly.", kilobytes(508), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(385), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1536), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(408), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(501), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(2048), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(3680), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(3680), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 3005, event.getDuration());
    }

    @Test
    public void testIsBlocking() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]";
        assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }
}
