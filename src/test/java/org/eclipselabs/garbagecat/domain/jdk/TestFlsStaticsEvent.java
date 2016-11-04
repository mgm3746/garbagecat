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

import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestFlsStaticsEvent extends TestCase {

    public void testLineStatistics() {
        String logLine = "Statistics for BinaryTreeDictionary:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLineDivider() {
        String logLine = "------------------------------------";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineTotalFreeSpace() {
        String logLine = "Total Free Space: 536870912";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineTotalFreeSpaceNegative() {
        String logLine = "Total Free Space: -136285693";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineMaxChunkSize() {
        String logLine = "Max   Chunk Size: 536870912";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineMaxChunkSizeNegative() {
        String logLine = "Max   Chunk Size: -136285693";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineNumberOfBlocks() {
        String logLine = "Number of Blocks: 1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineNumberOfBlocks4Digits() {
        String logLine = "Number of Blocks: 3752";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineAvBlockSize() {
        String logLine = "Av.  Block  Size: 536870912";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineAvBlockSizeNegative() {
        String logLine = "Av.  Block  Size: -328196225";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineTreeHeight() {
        String logLine = "Tree      Height: 1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineTreeHeight2Digits() {
        String logLine = "Tree      Height: 20";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineBeforeGC() {
        String logLine = "Before GC:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineAfterGC() {
        String logLine = "After GC:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineLargeBlock() {
        String logLine = "CMS: Large block 0x00002b79ea830000";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testLogLineLargeBlockWithProximity() {
        String logLine = "CMS: Large Block: 0x00002b79ea830000; Proximity: 0x0000000000000000 -> 0x00002b79ea82fac8";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".",
                FlsStatisticsEvent.match(logLine));
    }

    public void testNotBlocking() {
        String logLine = "Max   Chunk Size: 536870912";
        Assert.assertFalse(JdkUtil.LogEventType.FLS_STATISTICS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testNotReportable() {
        String logLine = "Max   Chunk Size: 536870912";
        Assert.assertFalse(JdkUtil.LogEventType.FLS_STATISTICS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testJdkUtilParseLogLineDoesNotReturnUnknownEvent() {
        String logLine = "Max   Chunk Size: 536870912";
        Assert.assertFalse("JdkUtil.parseLogLine() returns " + JdkUtil.LogEventType.UNKNOWN.toString() + " event.",
                JdkUtil.parseLogLine(logLine) instanceof UnknownEvent);
    }

    public void testJdkUtilParseLogLineReturnsFlsStatisticsEvent() {
        String logLine = "Max   Chunk Size: 536870912";
        Assert.assertTrue(
                "JdkUtil.parseLogLine() does not return " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + " event.",
                JdkUtil.parseLogLine(logLine) instanceof FlsStatisticsEvent);
    }
}
