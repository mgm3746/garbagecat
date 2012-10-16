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
public class TestG1YoungPreprocessedEvent extends TestCase {

    public void testLogLine() {
        String logLine = "0.304: [GC pause (young), 0.00376500 secs]   [ 8192K->2112K(59M)]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG.toString() + ".",
                G1YoungPreprocessedEvent.match(logLine));
        G1YoungPreprocessedEvent event = new G1YoungPreprocessedEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 304, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 8192, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 2112, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 60416, event.getYoungSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "0.304: [GC pause (young), 0.00376500 secs]   [ 8192K->2112K(59M)] "
                + "[Times: user=0.01 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG.toString() + ".",
                G1YoungPreprocessedEvent.match(logLine));
        G1YoungPreprocessedEvent event = new G1YoungPreprocessedEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 304, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 8192, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 2112, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 60416, event.getYoungSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "0.304: [GC pause (young), 0.00376500 secs]   [ 8192K->2112K(59M)] "
                + "[Times: user=0.01 sys=0.00, real=0.01 secs]           ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG.toString() + ".",
                G1YoungPreprocessedEvent.match(logLine));
    }
}
