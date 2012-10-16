/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestSerialSerialOldEvent extends TestCase {

    public void testLogLine() {
        String logLine = "160.678: [GC 160.678: [DefNew: 450682K->450682K(471872K), 0.0000099 secs]" + "160.678: [Tenured: 604639K->552856K(1048576K), 1.1178810 secs] "
                + "1055322K->552856K(1520448K), 1.1180562 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD.toString() + ".", SerialSerialOldEvent.match(logLine));
        SerialSerialOldEvent event = new SerialSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 160678, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1055322 - 604639, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 552856 - 552856, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1520448 - 1048576, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 604639, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 552856, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1048576, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1118, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "160.678: [GC 160.678: [DefNew: 450682K->450682K(471872K), 0.0000099 secs]" + "160.678: [Tenured: 604639K->552856K(1048576K), 1.1178810 secs] "
                + "1055322K->552856K(1520448K), 1.1180562 secs] " + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD.toString() + ".", SerialSerialOldEvent.match(logLine));
        SerialSerialOldEvent event = new SerialSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 160678, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1055322 - 604639, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 552856 - 552856, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1520448 - 1048576, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 604639, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 552856, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1048576, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1118, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "160.678: [GC 160.678: [DefNew: 450682K->450682K(471872K), 0.0000099 secs]" + "160.678: [Tenured: 604639K->552856K(1048576K), 1.1178810 secs] "
                + "1055322K->552856K(1520448K), 1.1180562 secs]          ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD.toString() + ".", SerialSerialOldEvent.match(logLine));
    }
}
