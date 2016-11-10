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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationStoppedTimeEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        Assert.assertFalse(
                JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 96845, event.getDuration());
    }

    public void testLogLineWithSpacesAtEnd() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds  ";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
    }

    public void testLogLineJdk8() {
        String logLine = "1.977: Total time for which application threads were stopped: 0.0002054 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1977, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 205, event.getDuration());
    }

    public void testLogLineJdk8Update40() {
        String logLine = "4.483: Total time for which application threads were stopped: 0.0018237 seconds, Stopping "
                + "threads took: 0.0017499 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4483, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 1823, event.getDuration());
    }

    public void testLogLineWithCommas() {
        String logLine = "1,065: Total time for which application threads were stopped: 0,0001610 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1065, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 161, event.getDuration());
    }

    public void testLogLineWithNegativeTime() {
        String logLine = "51185.692: Total time for which application threads were stopped: -0.0005950 seconds, "
                + "Stopping threads took: 0.0003310 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 51185692, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", -595, event.getDuration());
    }

    public void testLogLineWithDoubleTimestamp() {
        String logLine = "54974.551: 54974.551: Total time for which application threads were stopped: "
                + "0.0044650 seconds, Stopping threads took: 0.0013870 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                ApplicationStoppedTimeEvent.match(logLine));
        ApplicationStoppedTimeEvent event = new ApplicationStoppedTimeEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 54974551, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 4465, event.getDuration());
    }
}
