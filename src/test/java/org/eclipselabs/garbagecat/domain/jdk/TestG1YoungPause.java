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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1YoungPause extends TestCase {
    public void testYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
    }

    public void testNotInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
    }

    public void testLogLineKilobytes() {
        String logLine = "0.308: [GC pause (young) 8192K->2028K(59M), 0.0078140 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 308, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 8192, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2028, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7, event.getDuration());
    }

    public void testLogLinePreprocessed() {
        String logLine = "1.807: [GC pause (young), 0.00290200 secs] 29M->2589K(59M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1807, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29696, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2589, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2, event.getDuration());
    }

    public void testLogLinePreprocessedWithTimesData() {
        String logLine = "1.807: [GC pause (young), 0.00290200 secs] 29M->2589K(59M) "
                + "[Times: user=0.01 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1807, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29696, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2589, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2, event.getDuration());
    }

    public void testLogLinePreprocessedWhitespaceAtEnd() {
        String logLine = "1.807: [GC pause (young), 0.00290200 secs] 29M->2589K(59M) "
                + "[Times: user=0.01 sys=0.00, real=0.01 secs]       ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
    }

    public void testLogLinePreprocessedG1EvacuationPause() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young), 0.0209631 secs] 128M->25M(30M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2192, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 131072, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 25600, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30720, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 20, event.getDuration());
    }

    public void testLogLinePreprocessedG1EvacuationPauseWithTimesData() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young), 0.0209631 secs] 128M->25M(30M) "
                + "[Times: user=0.09 sys=0.02, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2192, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 131072, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 25600, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30720, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 20, event.getDuration());
    }

    public void testLogLinePreprocessedG1EvacuationPauseWhitespaceAtEnd() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young), 0.0209631 secs] 128M->25M(30M) "
                + "[Times: user=0.09 sys=0.02, real=0.03 secs]      ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
    }

    public void testLogLinePreprocessedGCLockerInitiatedGC() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs] 415M->313M(30720M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 5293, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));    
        Assert.assertEquals("Combined begin size not parsed correctly.", 424960, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 320512, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }
    
    public void testLogLinePreprocessedGCLockerInitiatedGCWithTimesData() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs] 415M->313M(30720M) "
                + "[Times: user=0.01 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 5293, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));    
        Assert.assertEquals("Combined begin size not parsed correctly.", 424960, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 320512, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }

    public void testLogLinePreprocessedGCLockerInitiatedGCWhitespaceAtEnd() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs] 415M->313M(30720M) "
                + "[Times: user=0.01 sys=0.00, real=0.02 secs]      ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
    }
    
    public void testLogLinePreprocessedToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs] 19354M->18227M(26624M) "
                + "[Times: user=0.41 sys=0.02, real=0.12 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27997968, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));    
        Assert.assertEquals("Combined begin size not parsed correctly.", 19818496, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18664448, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 27262976, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 120, event.getDuration());
    }
}
