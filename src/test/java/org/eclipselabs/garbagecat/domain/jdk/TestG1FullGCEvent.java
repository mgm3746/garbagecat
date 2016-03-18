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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
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
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
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
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 5959680, event.getCombinedOccupancyInit());
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

    public void testLogLinePreprocessedWithNoTrigger() {
        String logLine = "27999.141: [Full GC 18G->4153M(26G), 10.1760410 secs] 19354M->4154M(26624M) "
                + "[Times: user=13.12 sys=0.02, real=10.17 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                G1FullGCEvent.match(logLine));
        G1FullGCEvent event = new G1FullGCEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27999141, event.getTimestamp());
        Assert.assertNull("Trigger not parsed correctly.", event.getTrigger());
        Assert.assertEquals("Combined begin size not parsed correctly.", 18874368, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 4252672, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 27262976, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10176, event.getDuration());
    }
}
