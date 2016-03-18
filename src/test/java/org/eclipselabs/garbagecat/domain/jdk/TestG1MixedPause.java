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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1MixedPause extends TestCase {

    public void testMixedPause() {
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

    public void testMixedPauseWithTimesData() {
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

    public void testMixedPauseSpacesAtEnd() {
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

    public void testMixedPausePreprocessedWithTriggerBeforeMixed() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed), 0.0457502 secs] 13210M->11571M(30720M)"
                + " [Times: user=0.19 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2973338, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 13527040, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 11848704, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 45, event.getDuration());
    }

    public void testMixedPausePreprocessedWithTriggerAfterMixed() {
        String logLine = "17161.927: [GC pause (mixed) (to-space exhausted), 2.7599360 secs] 25805M->26317M(26624M) "
                + "[Times: user=6.73 sys=0.23, real=2.76 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 17161927, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Combined begin size not parsed correctly.", 25805 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 26317 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26624 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2759, event.getDuration());
    }

    public void testMixedPausePreprocessedWithoutTrigger() {
        String logLine = "3082.652: [GC pause (mixed), 0.0762060 secs] 12083M->9058M(26624M) "
                + "[Times: user=0.30 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPause.match(logLine));
        G1MixedPause event = new G1MixedPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3082652, event.getTimestamp());
        Assert.assertNull("Trigger not parsed correctly.", event.getTrigger());
        Assert.assertEquals("Combined begin size not parsed correctly.", 12372992, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9275392, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 27262976, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 76, event.getDuration());
    }
}
