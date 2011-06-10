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
public class TestParNewPromotionFailedCmsConcurrentModeFailureEvent extends TestCase {

    public void testLogLine() {
        String logLine = "25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K->261760K(261952K), "
                + "0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K->1015603K(1179648K), " + "7.6767910 secs] 1292923K->1015603K(1441600K), 7.8557660 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailureEvent event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 25281015, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1292923 - 1048384), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1015603 - 1015603), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1048384, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1015603, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7855, event.getDuration());
    }

    public void testLogLinewithAbortablePreclean() {
        String logLine = "233333.318: [GC 233333.319: [ParNew (promotion failed): " + "673108K->673108K(707840K), 1.5366054 secs]233334.855: [CMS233334.856: "
                + "[CMS-concurrent-abortable-preclean: 12.033/27.431 secs] (concurrent mode failure): "
                + "1125100K->1156809K(1310720K), 36.8003032 secs] 1791073K->1156809K(2018560K), 38.3378201 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailureEvent event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 233333318, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1791073 - 1125100), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1156809 - 1156809), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (2018560 - 1310720), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1125100, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1156809, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1310720, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 38337, event.getDuration());
    }

    public void testLogLinewithPrecleanIncrementalMode() {
        String logLine = "3272.568: [GC 3272.568: [ParNew (promotion failed): 261760K->261760K(261952K), " + "2.3020620 secs]3274.871: [CMS3277.648: [CMS-concurrent-preclean: 3.454/7.725 secs] "
                + "(concurrent mode failure): 1805342K->1161654K(1835008K), 61.9672360 secs] " + "2062255K->1161654K(2096960K) icms_dc=100 , 64.2703940 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailureEvent event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3272568, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (2062255 - 1805342), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1161654 - 1161654), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (2096960 - 1835008), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1805342, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1161654, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1835008, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 64270, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K->261760K(261952K), "
                + "0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K->1015603K(1179648K), " + "7.6767910 secs] 1292923K->1015603K(1441600K), 7.8557660 secs]"
                + " [Times: user=0.29 sys=0.02, real=3.97 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailureEvent event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 25281015, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1292923 - 1048384), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1015603 - 1015603), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1048384, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1015603, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7855, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K->261760K(261952K), "
                + "0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K->1015603K(1179648K), " + "7.6767910 secs] 1292923K->1015603K(1441600K), 7.8557660 secs]      ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine));
    }
}
