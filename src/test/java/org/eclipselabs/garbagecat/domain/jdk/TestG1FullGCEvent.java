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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1FullGCEvent extends TestCase {

    public void testFullSystemGC() {
        String logLine = "1302.524: [Full GC (System.gc()) 653M->586M(979M), 1.6364900 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                G1FullGCEvent.match(logLine));
    }

    public void testLogLinePreprocessed() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs] 5820M->1382M(30720M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                G1FullGCEvent.match(logLine));
        G1FullGCEvent event = new G1FullGCEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 105151, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 5959680, event.getCombinedOccupancyInit());
        Assert.assertEquals("Trigger not parsed correctly.", "System.gc()", event.getTrigger());
        Assert.assertEquals("Combined end size not parsed correctly.", 1414144, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5539, event.getDuration());
    }

    public void testLogLinePreprocessedWithTimesData() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs] 5820M->1382M(30720M) "
                + "[Times: user=5.76 sys=1.00, real=5.53 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                G1FullGCEvent.match(logLine));
        G1FullGCEvent event = new G1FullGCEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 105151, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 5959680, event.getCombinedOccupancyInit());
        Assert.assertEquals("Trigger not parsed correctly.", "System.gc()", event.getTrigger());
        Assert.assertEquals("Combined end size not parsed correctly.", 1414144, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5539, event.getDuration());
    }

    public void testLogLinePreprocessedWhitespaceAtEnd() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs] 5820M->1382M(30720M) "
                + "[Times: user=5.76 sys=1.00, real=5.53 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                G1FullGCEvent.match(logLine));
    }

}
