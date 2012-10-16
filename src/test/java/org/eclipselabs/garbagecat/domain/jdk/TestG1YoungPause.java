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
public class TestG1YoungPause extends TestCase {
    public void testYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".", G1YoungPause.match(logLine));
    }

    public void testNotInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".", G1YoungPause.match(logLine));
    }
    
    public void testLogLineKilobytes() {
        String logLine = "0.308: [GC pause (young) 8192K->2028K(59M), 0.0078140 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPause.match(logLine));
        G1YoungPause event = new G1YoungPause(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 308, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 8192, event.getCombinedOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 2028, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7, event.getDuration());
    }

}
