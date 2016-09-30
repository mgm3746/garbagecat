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
public class TestCmsSerialOldConcurrentModeFailureEvent extends TestCase {

    public void testLogLineNoCMS() {
        String logLine = "28282.075: [Full GC 28282.075 (concurrent mode failure): "
                + "1179601K->1179648K(1179648K), 10.7510650 secs] 1441361K->1180553K(1441600K), "
                + "[CMS Perm : 71172K->71171K(262144K)], 10.7515460 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
                CmsSerialOldConcurrentModeFailureEvent.match(logLine));
        CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 28282075, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1441361 - 1179601),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1180553 - 1179648), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1179601, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1179648, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 71172, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 71171, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10751, event.getDuration());
    }

    public void testLogLineWithCMS() {
        String logLine = "6942.991: [Full GC 6942.991: [CMS (concurrent mode failure): "
                + "907264K->907262K(907264K), 11.8579830 secs] 1506304K->1202006K(1506304K), "
                + "[CMS Perm : 92801K->92800K(157352K)], 11.8585290 secs] "
                + "[Times: user=11.80 sys=0.06, real=11.85 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
                CmsSerialOldConcurrentModeFailureEvent.match(logLine));
        CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6942991, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1506304 - 907264),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1202006 - 907262), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1506304 - 907264), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 907264, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 907262, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 907264, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 92801, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 92800, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 157352, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 11858, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "28282.075: [Full GC 28282.075 (concurrent mode failure): "
                + "1179601K->1179648K(1179648K), 10.7510650 secs] 1441361K->1180553K(1441600K), "
                + "[CMS Perm : 71172K->71171K(262144K)], 10.7515460 secs]"
                + " [Times: user=0.29 sys=0.02, real=3.97 secs]";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
                CmsSerialOldConcurrentModeFailureEvent.match(logLine));
        CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 28282075, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1441361 - 1179601),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1180553 - 1179648), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1179601, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1179648, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 71172, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 71171, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10751, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "6942.991: [Full GC 6942.991: [CMS (concurrent mode failure): "
                + "907264K->907262K(907264K), 11.8579830 secs] 1506304K->1202006K(1506304K), "
                + "[CMS Perm : 92801K->92800K(157352K)], 11.8585290 secs] "
                + "[Times: user=11.80 sys=0.06, real=11.85 secs]     ";
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
                CmsSerialOldConcurrentModeFailureEvent.match(logLine));
    }
}
