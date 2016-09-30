/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParNewCmsSerialOldEvent extends TestCase {

    public void testLogLine() {
        String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
                + "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
                + "1310721K->934941K(1336548K), 6.5587770 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
                ParNewCmsSerialOldEvent.match(logLine));
        ParNewCmsSerialOldEvent event = new ParNewCmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 42782086, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 254464, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 254464, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1082057, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 934941, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1082084, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 6558, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
                + "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
                + "1310721K->934941K(1336548K), 6.5587770 secs] " + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
                ParNewCmsSerialOldEvent.match(logLine));
        ParNewCmsSerialOldEvent event = new ParNewCmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 42782086, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 254464, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 254464, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1082057, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 934941, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1082084, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 6558, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
                + "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
                + "1310721K->934941K(1336548K), 6.5587770 secs]    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
                ParNewCmsSerialOldEvent.match(logLine));
    }

    public void testLogLineWithPermData() {
        String logLine = "6.102: [GC6.102: [ParNew: 19648K->2176K(19648K), 0.0184470 secs]6.121: "
                + "[Tenured: 44849K->25946K(44864K), 0.2586250 secs] 60100K->25946K(64512K), "
                + "[Perm : 43759K->43759K(262144K)], 0.2773070 secs] [Times: user=0.16 sys=0.01, real=0.28 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
                ParNewCmsSerialOldEvent.match(logLine));
        ParNewCmsSerialOldEvent event = new ParNewCmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6102, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 19648, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 25946 - 25946, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 64512 - 44864, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 44849, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 25946, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 44864, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 43759, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 43759, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 277, event.getDuration());
    }
}
