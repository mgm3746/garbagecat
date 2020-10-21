/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1RemarkEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "106.129: [GC remark, 0.0450170 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testRemark() {
        String logLine = "106.129: [GC remark, 0.0450170 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                G1RemarkEvent.match(logLine));
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 106129, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 45017, event.getDuration());
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

    public void testRemarkWithTimesDate() {
        String logLine = "2016-11-08T09:40:55.346-0800: 35563.088: [GC remark, 0.0827210 secs] "
                + "[Times: user=0.37 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                G1RemarkEvent.match(logLine));
        G1RemarkEvent event = new G1RemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 35563088, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 82721, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 37, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 8, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 463, event.getParallelism());
    }
}
