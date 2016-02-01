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
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1CleanupEvent extends TestCase {
    
    public void testCleanup() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".", G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 18650, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 304128, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 241664, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 524288, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1, event.getDuration());
    }
    
    public void testCleanupWhiteSpacesAtEnd() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".", G1CleanupEvent.match(logLine));
    }
    
    public void testLogLineGigabytes() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2972698, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 13631488, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12582912, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 35, event.getDuration());
    }
}



