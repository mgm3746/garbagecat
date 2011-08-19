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
public class TestSerialSerialOldPermDataEvent extends TestCase {

    public void testLogLine() {
        String logLine = "3727.365: [GC 3727.365: [DefNew: 400314K->400314K(400384K), 0.0000550 secs]"
                + "3727.365: [Tenured: 837793K->597490K(889536K), 44.7498530 secs] 1238107K->597490K(1289920K), " + "[Perm : 54745K->54745K(54784K)], 44.7501880 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD_PERM_DATA.toString() + ".", SerialSerialOldPermDataEvent.match(logLine));
        SerialSerialOldPermDataEvent event = new SerialSerialOldPermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3727365, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1238107 - 837793, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 597490 - 597490, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1289920 - 889536, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 837793, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 597490, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 889536, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 54745, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 54745, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 54784, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 44750, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "3727.365: [GC 3727.365: [DefNew: 400314K->400314K(400384K), 0.0000550 secs]"
                + "3727.365: [Tenured: 837793K->597490K(889536K), 44.7498530 secs] 1238107K->597490K(1289920K), "
                + "[Perm : 54745K->54745K(54784K)], 44.7501880 secs] [Times: user=5.32 sys=0.33, real=44.75 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD_PERM_DATA.toString() + ".", SerialSerialOldPermDataEvent.match(logLine));
        SerialSerialOldPermDataEvent event = new SerialSerialOldPermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3727365, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1238107 - 837793, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 597490 - 597490, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1289920 - 889536, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 837793, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 597490, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 889536, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 54745, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 54745, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 54784, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 44750, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "3727.365: [GC 3727.365: [DefNew: 400314K->400314K(400384K), 0.0000550 secs]"
                + "3727.365: [Tenured: 837793K->597490K(889536K), 44.7498530 secs] 1238107K->597490K(1289920K), " + "[Perm : 54745K->54745K(54784K)], 44.7501880 secs]      ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_SERIAL_OLD_PERM_DATA.toString() + ".", SerialSerialOldPermDataEvent.match(logLine));
    }
}
