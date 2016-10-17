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
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1MixedPause extends TestCase {

    public void testLogLine() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLineSpacesAtEnd() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLinePreprocessedTriggerG1EvacuationPause() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed), 0.0457502 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 12.9G(30.0G)->11.3G(30.0G)]"
                + " [Times: user=0.19 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2973338, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 13526630, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 11848909, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 45, event.getDuration());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "3082.652: [GC pause (mixed), 0.0762060 secs]"
                + "[Eden: 1288.0M(1288.0M)->0.0B(1288.0M) Survivors: 40.0M->40.0M Heap: 11.8G(26.0G)->9058.4M(26.0G)]"
                + " [Times: user=0.30 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3082652, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 12373197, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9275802, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 76, event.getDuration());
    }
}
