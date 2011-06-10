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
public class TestParallelScavengeEvent extends TestCase {

    public void testLogLine() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] " + "160183K->133159K(585088K), 0.0225213 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".", ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 19810091, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 27808, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 632, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 28032, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 132375, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 132527, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 557056, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 22, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] " + "160183K->133159K(585088K), 0.0225213 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".", ParallelScavengeEvent.match(logLine));
    }

    public void testStressedJvmLogLine() {
        String logLine = "14112.691: [GC-- [PSYoungGen: 313864K->313864K(326656K)] " + "879670K->1012935K(1025728K), 0.9561947 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".", ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 14112691, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 313864, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 313864, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 326656, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 565806, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 699071, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 699072, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 956, event.getDuration());
    }
}
