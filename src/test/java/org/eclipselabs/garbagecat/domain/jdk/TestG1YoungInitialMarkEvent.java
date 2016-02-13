/******************************************************************************
 * Garbage Cat * * Copyright (c) 2008-2012 Red Hat, Inc. * All rights reserved. This program and the accompanying
 * materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this
 * distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: * Red Hat, Inc. -
 * initial API and implementation *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1YoungInitialMarkEvent extends TestCase {
    public void testInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1244357, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 867328, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 613376, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 993280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 56, event.getDuration());
    }

    public void testNotYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
    }
    
    public void testInitialMarkPreprocessed() {
        String logLine = "12970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0698627 secs] "
                + "13926M->13824M(30720M) [Times: user=0.28 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 12970268, event.getTimestamp());
        Assert.assertNull("Trigger not parsed correctly.", event.getTrigger()); 
        Assert.assertEquals("Combined begin size not parsed correctly.", 14260224, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 14155776, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 69, event.getDuration());
    }
    
    public void testInitialMarkWithoutEvacuationPauseTriggerPreprocessed() {
        String logLine = "27474.176: [GC pause (young) (initial-mark), 0.4234530 secs] 14131M->8821M(26624M) "
                + "[Times: user=1.66 sys=0.02, real=0.43 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27474176, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 14470144, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9032704, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 27262976, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 423, event.getDuration());
    }
    
    public void testInitialMarkPreprocessedWhiteSpacesAtEnd() {
        String logLine = "2970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0698627 secs] "
                + "13926M->13824M(30720M) [Times: user=0.28 sys=0.00, real=0.08 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
    }
    
    public void testInitialMarkWithToSpaceExhaustedTriggerPreprocessed() {
        String logLine = "60346.050: [GC pause (young) (initial-mark) (to-space exhausted), 1.0224350 secs] "
                + "23450M->19661M(26624M) [Times: user=3.03 sys=0.02, real=1.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 60346050, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 24012800, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 20132864, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 27262976, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1022, event.getDuration());
    }
}
