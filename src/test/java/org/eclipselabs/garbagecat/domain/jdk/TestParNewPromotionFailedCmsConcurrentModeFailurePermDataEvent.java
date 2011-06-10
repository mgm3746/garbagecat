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
public class TestParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent extends TestCase {

    public void testLogLineMark() {
        String logLine = "2746.109: [GC 2746.109: [ParNew (promotion failed): 242303K->242304K(242304K), " + "1.3009892 secs]2747.410: [CMS2755.518: [CMS-concurrent-mark: 11.734/13.504 secs] "
                + "(concurrent mode failure): 5979868K->5968004K(6014592K), 78.3207206 secs] " + "6205857K->5968004K(6256896K), [CMS Perm : 207397K->207212K(262144K)], 79.6222096 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2746109, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (6205857 - 5979868), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (5968004 - 5968004), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5979868, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 5968004, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 207397, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 207212, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 79622, event.getDuration());
    }

    public void testLogLineSweep() {
        String logLine = "5602.812: [GC 5602.813: [ParNew (promotion failed): 242304K->242304K(242304K), " + "0.5849688 secs]5603.398: [CMS5613.124: [CMS-concurrent-sweep: 13.444/15.478 secs] "
                + "(concurrent mode failure): 12022816K->10250508K(12306048K), 172.2507792 secs] " + "12218490K->10250508K(12548352K), [CMS Perm : 227739K->224001K(262144K)], "
                + "172.8363482 secs] [Times: user=173.65 sys=0.91, real=172.84 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 5602812, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (12218490 - 12022816), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (10250508 - 10250508), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (12548352 - 12306048), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 12022816, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 10250508, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 12306048, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 227739, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 224001, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 172836, event.getDuration());
    }

    public void testLogLinePreclean() {
        String logLine = "47101.598: [GC 47101.599: [ParNew (promotion failed): 242304K->242304K(242304K), " + "3.4779001 secs]47105.077: [CMS47106.206: [CMS-concurrent-preclean: 1.592/5.189 secs] "
                + "(concurrent mode failure): 11581775K->6643433K(12306048K), 116.2015102 secs] " + "11808303K->6643433K(12548352K), [CMS Perm : 225102K->225036K(262144K)], 119.6799427 secs] "
                + "[Times: user=120.78 sys=2.49, real=119.68 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 47101598, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (11808303 - 11581775), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (6643433 - 6643433), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (12548352 - 12306048), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 11581775, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 6643433, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 12306048, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 225102, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 225036, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 119679, event.getDuration());
    }

    public void testLogLineAbortedAbortablePreclean() {
        String logLine = "891197.444: [GC 891197.445: [ParNew (promotion failed): 910978K->910978K(917504K), " + "1.7096800 secs]891199.155: [CMS CMS: abort preclean due to time 891199.474: "
                + "[CMS-concurrent-abortable-preclean: 4.227/6.007 secs] " + "(concurrent mode failure): 1569173K->1458552K(1572864K), 9.7516840 secs] "
                + "2388791K->1458552K(2490368K), [CMS Perm : 46359K->46354K(77352K)], 11.4627030 secs] " + "[Times: user=13.44 sys=0.79, real=11.47 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 891197444, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (2388791 - 1569173), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1458552 - 1458552), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1569173, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1458552, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 46359, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 46354, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77352, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 11462, event.getDuration());
    }

    public void testLogLineAbortedAbortablePrecleanIncrementalMode() {
        String logLine = "4555.706: [GC 4555.706: [ParNew (promotion failed): 1304576K->1304575K(1304576K), " + "4.5501949 secs]4560.256: [CMS CMS: abort preclean due to time 4562.921: "
                + "[CMS-concurrent-abortable-preclean: 2.615/14.874 secs] (concurrent mode failure): " + "924455K->679155K(4886528K), 6.2285220 secs] 1973973K->679155K(6191104K), [CMS Perm : "
                + "198322K->198277K(524288K)] icms_dc=24 , 10.7789303 secs] " + "[Times: user=9.49 sys=1.83, real=10.78 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4555706, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1973973 - 924455), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (679155 - 679155), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6191104 - 4886528), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 924455, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 679155, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 4886528, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 198322, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 198277, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10778, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "2746.109: [GC 2746.109: [ParNew (promotion failed): 242303K->242304K(242304K), " + "1.3009892 secs]2747.410: [CMS2755.518: [CMS-concurrent-mark: 11.734/13.504 secs] "
                + "(concurrent mode failure): 5979868K->5968004K(6014592K), 78.3207206 secs] " + "6205857K->5968004K(6256896K), [CMS Perm : 207397K->207212K(262144K)], 79.6222096 secs] "
                + "[Times: user=98.05 sys=4.32, real=79.62 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2746109, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (6205857 - 5979868), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (5968004 - 5968004), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5979868, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 5968004, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 207397, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 207212, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 79622, event.getDuration());
    }

    public void testLogNoConcurrentModeFailure() {
        String logLine = "88063.609: [GC 88063.610: [ParNew (promotion failed): 513856K->513856K(513856K), " + "4.0911197 secs]88067.701: [CMS88067.742: [CMS-concurrent-reset: 0.309/4.421 secs]: "
                + "10612422K->4373474K(11911168K), 76.7523274 secs] 11075362K->4373474K(12425024K), " + "[CMS Perm : 214530K->213777K(524288K)], 80.8440551 secs] "
                + "[Times: user=80.01 sys=5.57, real=80.84 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 88063609, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (11075362 - 10612422), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (4373474 - 4373474), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (12425024 - 11911168), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 10612422, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 4373474, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 11911168, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 214530, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 213777, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 80844, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2746.109: [GC 2746.109: [ParNew (promotion failed): 242303K->242304K(242304K), " + "1.3009892 secs]2747.410: [CMS2755.518: [CMS-concurrent-mark: 11.734/13.504 secs] "
                + "(concurrent mode failure): 5979868K->5968004K(6014592K), 78.3207206 secs] " + "6205857K->5968004K(6256896K), [CMS Perm : 207397K->207212K(262144K)], 79.6222096 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine));
    }
}
