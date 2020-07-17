/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationConcurrentTimeEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "Application time: 130.5284640 seconds   ";
        Assert.assertFalse(
                JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        String logLine = "Application time: 130.5284640 seconds   ";
        Assert.assertFalse(
                JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "Application time: 130.5284640 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineWithSpacesAtEnd() {
        String logLine = "Application time: 130.5284640 seconds   ";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineWithTimestamp() {
        String logLine = "0.193: Application time: 0.0430320 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineNoTimestampStartingSemicolon() {
        String logLine = ": Application time: 1.0001619 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestamp() {
        String logLine = "2016-12-21T14:28:11.159-0500: 0.311: Application time: 0.0060964 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestampTimestampDatestamp() {
        String logLine = "2020-02-19T07:18:10.490-0500: 698914.875: 2020-02-19T07:18:10.490-0500: Application time: "
                + "0.0000605 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestampTimestampDatestampMissingColonSpace() {
        String logLine = "2020-02-19T11:46:53.624-0500: 715038.009: 2020-02-19T11:46:53.624-0500Application time: "
                + "0.0001082 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestampDatestampTimestamp() {
        String logLine = "2020-02-19T12:41:32.898-0500: 2020-02-19T12:41:32.898-0500: 718317.283: Application time: "
                + "0.0000496 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestampDatestampTimestampTimestamp() {
        String logLine = "2020-02-19T10:24:29.418-0500: 2020-02-19T10:24:29.418-0500: 710093.803: 710093.803: "
                + "Application time: 0.0000610 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }

    public void testLogLineDatestampDatestampTimestampTimestampMisplacedSemicolons() {
        String logLine = "2020-02-19T10:09:47.141-05002020-02-19T10:09:47.141-0500: : 709211.526: "
                + "709211.526Application time: 0.0000505 seconds";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                ApplicationConcurrentTimeEvent.match(logLine));
    }
}
