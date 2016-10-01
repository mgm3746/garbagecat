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
}
