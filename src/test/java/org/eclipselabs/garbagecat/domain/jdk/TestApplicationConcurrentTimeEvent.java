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
}
