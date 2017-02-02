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
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1ConcurrentEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "50.101: [GC concurrent-root-region-scan-start]";
        Assert.assertFalse(JdkUtil.LogEventType.G1_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testRootRegionScanStart() {
        String logLine = "50.101: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50101, event.getTimestamp());
    }

    public void testRootRegionScanEnd() {
        String logLine = "50.136: [GC concurrent-root-region-scan-end, 0.0346620 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50136, event.getTimestamp());
    }

    public void testMarkStart() {
        String logLine = "50.136: [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50136, event.getTimestamp());
    }

    public void testMarkEnd() {
        String logLine = "50.655: [GC concurrent-mark-end, 0.5186330 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50655, event.getTimestamp());
    }

    public void testCleanupStart() {
        String logLine = "50.685: [GC concurrent-cleanup-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50685, event.getTimestamp());
    }

    public void testCleanupEnd() {
        String logLine = "50.685: [GC concurrent-cleanup-end, 0.0001080 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50685, event.getTimestamp());
    }

    public void testDateStamp() {
        String logLine = "2016-02-09T06:22:10.399-0500: 28039.161: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 28039161, event.getTimestamp());
    }

    public void testPreprocessed() {
        String logLine = "27744.494: [GC concurrent-mark-start], 0.3349320 secs] 10854M->9765M(26624M) "
                + "[Times: user=0.98 sys=0.00, real=0.33 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27744494, event.getTimestamp());
    }

    public void testLogLineCleanupEndWithDatestamp() {
        String logLine = "2016-02-11T18:15:35.431-0500: 14974.501: [GC concurrent-cleanup-end, 0.0033880 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 14974501, event.getTimestamp());
    }

    public void testLogLineStringDeduplication() {
        String logLine = "8.556: [GC concurrent-string-deduplication, 906.5K->410.2K(496.3K), avg 54.8%, "
                + "0.0162924 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 8556, event.getTimestamp());
    }

    public void testLogLineDoubleTimestamp() {
        String logLine = "23743.632: 23743.632: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 23743632, event.getTimestamp());
    }

    public void testLogLineWithoutTrailingSecs() {
        String logLine = "449391.280: [GC concurrent-root-region-scan-end, 0.0033660]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 449391280, event.getTimestamp());
    }

    public void testLogLineWithTrailingSecNotSecs() {
        String logLine = "449391.442: [GC concurrent-mark-end, 0.1620950 sec]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 449391442, event.getTimestamp());
    }

    public void testLogLineConcurrentMarkResetForOverflow() {
        String logLine = "1048.227: [GC concurrent-mark-reset-for-overflow]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1048227, event.getTimestamp());
    }

    public void testLogLineWithDatestamp() {
        String logLine = "2016-02-09T06:17:15.377-0500: 27744.139: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27744139, event.getTimestamp());
    }

    public void testLogLineWithDoubleDatestamp() {
        String logLine = "2017-01-20T23:18:29.584-0500: 1513296.456: 2017-01-20T23:18:29.584-0500: "
                + "[GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1513296456, event.getTimestamp());
    }

    public void testLogLineDatestampTimestampDatestampMisplacedColon() {
        String logLine = "2017-01-20T23:20:52.028-0500: 1513438.9002017-01-20T23:20:52.028-0500: : "
                + "[GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1513438900, event.getTimestamp());
    }

    public void testLogLineDatestampDatestampTimestampMisplacedColon() {
        String logLine = "2017-01-20T23:49:17.968-0500: 2017-01-20T23:49:17.968-05001515144.840: :"
                + " [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1515144840, event.getTimestamp());
    }

    public void testLogLineDatestampDatestampMisplacedColonTimestamp() {
        String logLine = "2017-01-21T00:58:45.921-05002017-01-21T00:58:45.921-0500: : 1519312.793: "
                + "[GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1519312793, event.getTimestamp());
    }

    public void testLogLineTimestampDatestampMisplacedColonTimestamp() {
        String logLine = "1516186.5322017-01-21T00:06:39.660-0500: : 1516186.532: [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1516186532, event.getTimestamp());
    }

    public void testLogLineMisplacedColonDatestampTimestampTimestampMisplacedColon() {
        String logLine = ": 2017-01-21T09:59:17.908-0500: 1551744.7801551744.780: : [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1551744780, event.getTimestamp());
    }

    public void testLogLineNoTimestamp() {
        String logLine = ": [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 0, event.getTimestamp());
    }
}
