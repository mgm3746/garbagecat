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
public class TestParNewConcurrentModeFailurePermDataEvent extends TestCase {

    public void testLogLine() {
        String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]3070.289: "
                + "[CMS (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
                + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3070289, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (6217865 - 6010121),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (6028029 - 6014591), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 6010121, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 6014591, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 206688, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 206662, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 79050, event.getDuration());
    }

    public void testLogLineMetaspaceIcrementalMode() {
        String logLine = "719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), "
                + "0.0000470 secs] (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 719519, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (3925228 - 2542828),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (2702358 - 2658278), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (4040704 - 2658304), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2542828, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2658278, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 72175, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 72175, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 12348, event.getDuration());
    }

    public void testLogLineConcurrentSweep() {
        String logLine = "1202.526: [GC (Allocation Failure) 1202.528: [ParNew: 1355422K->1355422K(1382400K), "
                + "0.0000500 secs]1202.528: [CMS (concurrent mode failure): 2656311K->2658289K(2658304K), "
                + "9.3575580 secs] 4011734K->2725109K(4040704K), [Metaspace: 72111K->72111K(1118208K)], "
                + "9.3610080 secs] [Times: user=9.35 sys=0.01, real=9.36 secs]";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1202526, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (4011734 - 2656311),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (2725109 - 2658289), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (4040704 - 2658304), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2656311, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2658289, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 72111, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 72111, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 9361, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]3070.289: "
                + "[CMS (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
                + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]             ";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
    }

    public void testLogLinePreProcessedClassHistogram() {
        String logLine = "572264.304: [GC 572264.306: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.4978605 secs]572265.804: [Class Histogram:, 23.6901531 secs]"
                + "572289.495: [CMS (concurrent mode failure): 5350445K->891234K(7331840K), 59.8600601 secs]"
                + "572349.355: [Class Histogram, 12.1674045 secs] 5825751K->891234K(7848704K), "
                + "[CMS Perm : 500357K->443269K(1048576K)], 97.2188825 secs] "
                + "[Times: user=100.78 sys=0.18, real=97.22 secs]";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 572264304, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (5825751 - 5350445),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (891234 - 891234), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (7848704 - 7331840), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5350445, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 891234, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 500357, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 443269, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 97218, event.getDuration());
    }

    public void testLogLinePreProcessedClassHistogramConcurrentModeFailure() {
        String logLine = "576460.444: [GC 576460.446: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.9697779 secs]576462.416: [Class Histogram:, 23.3548450 secs]"
                + "576485.771: [CMS: 5074711K->905970K(7331840K), 46.0517345 secs]"
                + "576531.823: [Class Histogram, 12.2976631 secs] 5566845K->905970K(7848704K), "
                + "[CMS Perm : 498279K->443366K(1048576K)], 83.6775207 secs] "
                + "[Times: user=87.62 sys=0.20, real=83.68 secs]";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                ParNewConcurrentModeFailurePermDataEvent.match(logLine));
        ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 576460444, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (5566845 - 5074711),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (905970 - 905970), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (7848704 - 7331840), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5074711, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 905970, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 498279, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 443366, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 83677, event.getDuration());
    }
}
