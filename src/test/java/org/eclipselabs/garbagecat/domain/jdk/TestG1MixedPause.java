/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2012 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1MixedPause extends TestCase {
    
    public void testMixedPause() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".", G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }
    
    public void testMixedPauseWithTimesData() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".", G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }
    
    public void testMixedPauseSpacesAtEnd() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".", G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }
    
    public void testMixedPausePreprocessed() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed), 0.0457502 secs] 13210M->11571M(30720M)"
                + " [Times: user=0.19 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".", G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2973338, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 13527040, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 11848704, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 45, event.getDuration());
    }
}
