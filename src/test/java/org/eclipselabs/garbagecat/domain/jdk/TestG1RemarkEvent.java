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
public class TestG1RemarkEvent extends TestCase {
    public void testRemark() {
        String logLine = "106.129: [GC remark, 0.0450170 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                G1RemarkEvent.match(logLine));
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 106129, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 45, event.getDuration());
    }

    public void TestG1RemarkPreprocessedEvent() {
        String logLine = "2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                G1RemarkEvent.match(logLine));
    }

    public void TestG1RemarkPreprocessedEventWhiteSpacesAtEnd() {
        String logLine = "2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                G1RemarkEvent.match(logLine));
    }
}
