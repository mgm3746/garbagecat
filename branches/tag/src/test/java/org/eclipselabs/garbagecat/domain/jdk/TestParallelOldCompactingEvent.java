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
public class TestParallelOldCompactingEvent extends TestCase {

    public void testLogLine() {
        String logLine = "2182.541: [Full GC [PSYoungGen: 1940K->0K(98560K)] " + "[ParOldGen: 813929K->422305K(815616K)] 815869K->422305K(914176K) "
                + "[PSPermGen: 81960K->81783K(164352K)], 2.4749181 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".", ParallelOldCompactingEvent.match(logLine));
        ParallelOldCompactingEvent event = new ParallelOldCompactingEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2182541, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1940, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 98560, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 813929, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 422305, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 815616, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 81960, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 81783, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 164352, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2474, event.getDuration());
    }

    public void testLogLineWhiteSpaceAtEnd() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] " + "[ParOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) " + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".", ParallelOldCompactingEvent.match(logLine));
    }

    public void testLogLineJdk16() {
        String logLine = "2.417: [Full GC (System) [PSYoungGen: 1788K->0K(12736K)] " + "[ParOldGen: 1084K->2843K(116544K)] 2872K->2843K(129280K) "
                + "[PSPermGen: 8602K->8593K(131072K)], 0.1028360 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".", ParallelOldCompactingEvent.match(logLine));
        ParallelOldCompactingEvent event = new ParallelOldCompactingEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2417, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1788, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 12736, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1084, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2843, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 116544, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 8602, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 8593, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 102, event.getDuration());
    }
}
