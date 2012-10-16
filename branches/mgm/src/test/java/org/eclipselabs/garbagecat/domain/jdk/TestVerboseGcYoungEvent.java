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
public class TestVerboseGcYoungEvent extends TestCase {

    public void testLogLine() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".", VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2205570508L, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 1726387, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 773247, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3097984, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 231, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]        ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".", VerboseGcYoungEvent.match(logLine));
    }
}
