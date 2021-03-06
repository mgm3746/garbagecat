/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestFlsStaticsEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "Max   Chunk Size: 536870912";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.FLS_STATISTICS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    public void testNotReportable() {
        String logLine = "Max   Chunk Size: 536870912";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.FLS_STATISTICS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    public void testJdkUtilParseLogLineDoesNotReturnUnknownEvent() {
        String logLine = "Max   Chunk Size: 536870912";
        assertFalse(JdkUtil.parseLogLine(logLine) instanceof UnknownEvent, "JdkUtil.parseLogLine() returns " + JdkUtil.LogEventType.UNKNOWN.toString() + " event.");
    }

    @Test
    public void testJdkUtilParseLogLineReturnsFlsStatisticsEvent() {
        String logLine = "Max   Chunk Size: 536870912";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof FlsStatisticsEvent, "JdkUtil.parseLogLine() does not return " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + " event.");
    }

    @Test
    public void testLineStatistics() {
        String logLine = "Statistics for BinaryTreeDictionary:";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLineDivider() {
        String logLine = "------------------------------------";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineTotalFreeSpace() {
        String logLine = "Total Free Space: 536870912";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineTotalFreeSpaceNegative() {
        String logLine = "Total Free Space: -136285693";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineMaxChunkSize() {
        String logLine = "Max   Chunk Size: 536870912";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineMaxChunkSizeNegative() {
        String logLine = "Max   Chunk Size: -136285693";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks() {
        String logLine = "Number of Blocks: 1";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks4Digits() {
        String logLine = "Number of Blocks: 3752";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks5Digits() {
        String logLine = "Number of Blocks: 68082";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks6Digits() {
        String logLine = "Number of Blocks: 218492";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks7Digits() {
        String logLine = "Number of Blocks: 6455862";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineNumberOfBlocks8Digits() {
        String logLine = "Number of Blocks: 64558627";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineAvBlockSize() {
        String logLine = "Av.  Block  Size: 536870912";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineAvBlockSizeNegative() {
        String logLine = "Av.  Block  Size: -328196225";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineTreeHeight() {
        String logLine = "Tree      Height: 1";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineTreeHeight2Digits() {
        String logLine = "Tree      Height: 20";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineTreeHeight3Digits() {
        String logLine = "Tree      Height: 130";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineBeforeGC() {
        String logLine = "Before GC:";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineAfterGC() {
        String logLine = "After GC:";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineLargeBlock() {
        String logLine = "CMS: Large block 0x00002b79ea830000";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineLargeBlockWithProximity() {
        String logLine = "CMS: Large Block: 0x00002b79ea830000; Proximity: 0x0000000000000000 -> 0x00002b79ea82fac8";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineStatisticsForIndexedFreeLists() {
        String logLine = "Statistics for IndexedFreeLists:";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineDivider() {
        String logLine = "--------------------------------";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineFrag() {
        String logLine = " free=1161964910 frag=0.8232";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineSweepSize() {
        String logLine = "size[256] : demand: 0, old_rate: 0.000000, current_rate: 0.000000, new_rate: 0.000000, "
                + "old_desired: 0, new_desired: 0";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineSweepDemand() {
        String logLine = "demand: 1, old_rate: 0.000000, current_rate: 0.000282, new_rate: 0.000282, old_desired: 0, "
                + "new_desired: 2";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineSweepSizeDemand8DigitsRate3Digits() {
        String logLine = "size[3] : demand: 11798284, old_rate: 717.797668, current_rate: 743.942383, new_rate: "
                + "742.511902, old_desired: 15402672, new_desired: 13570211";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }

    @Test
    public void testLogLineSweepSizeDemand9DigitsRate4Digits() {
        String logLine = "size[4] : demand: 81603741, old_rate: 5028.634277, current_rate: 5145.535156, new_rate: "
                + "5146.810547, old_desired: 107905624, new_desired: 94063552";
        assertTrue(FlsStatisticsEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
    }
}
