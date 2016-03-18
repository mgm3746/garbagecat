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
package org.eclipselabs.garbagecat.preprocess.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestDateStampPrefixPreprocessAction extends TestCase {

    public void testLogLine() {
        String logLine = "2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), 0.0030008 "
                + "secs] 273152K->858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), 0.0030008 secs] "
                + "273152K->858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1ErgonomicsLogLine() {
        String logLine = "2016-02-11T17:26:43.599-0500:  12042.669: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target "
                + "pause time: 500.00 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "12042.669: [G1Ergonomics (CSet Construction) start choosing CSet, "
                + "_pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target pause "
                + "time: 500.00 ms]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1ErgonomicsLogLineWithoutColon() {
        String logLine = "2016-02-11T18:50:24.070-0500 16705.217: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 273946, predicted base time: 242.44 ms, remaining time: 257.56 ms, target "
                + "pause time: 500.00 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "16705.217: [G1Ergonomics (CSet Construction) start choosing CSet, "
                + "_pending_cards: 273946, predicted base time: 242.44 ms, remaining time: 257.56 ms, target pause "
                + "time: 500.00 ms]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDoubltestG1LogLineDoubleDateStampDoubleTimestampeDatestamp() {
        String logLine = "2016-02-16T03:13:56.897-0500: 2016-02-16T03:13:56.897-0500: 23934.242: 23934.242: "
                + "[GC concurrent-root-region-scan-start]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "23934.242: [GC concurrent-root-region-scan-start]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDoubleDateStampSingleTimestamp() {
        String logLine = "2016-02-16T02:18:42.458-0500: 2016-02-16T02:18:42.458-0500: 20619.758: "
                + "[GC concurrent-root-region-scan-start]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "20619.758: [GC concurrent-root-region-scan-start]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDoubleDateStampSingleTimestampDoubleColon() {
        String logLine = "2016-02-16T02:48:24.028-0500: 2016-02-16T02:48:24.028-050022401.327: : "
                + "[GC concurrent-root-region-scan-start]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "22401.327: [GC concurrent-root-region-scan-start]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDatestampTimestampDatestamp() {
        String logLine = "2016-02-16T02:30:43.081-0500: 21340.380: 2016-02-16T02:30:43.081-0500: "
                + "[GC concurrent-root-region-scan-start]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "21340.380: [GC concurrent-root-region-scan-start]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDatestampTimestampTimestamp() {
        String logLine = "2016-02-16T02:14:10.329-0500: 20349.312: [GC pause (young)2016-02-16T02:14:10.367-0500: "
                + "20349.349:  20349.349: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: "
                + "28333, predicted base time: 108.90 ms, remaining time: 891.10 ms, target pause time: 1000.00 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "20349.312: [GC pause (young)20349.349: [G1Ergonomics (CSet Construction) start "
                + "choosing CSet, _pending_cards: 28333, predicted base time: 108.90 ms, remaining time: 891.10 ms, "
                + "target pause time: 1000.00 ms]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }

    public void testG1LogLineDatestampCmsRemarkWithClassUnloadingJdk8() {
        String logLine = "2016-02-18T17:23:09.738+0100: 13.749: [GC (CMS Final Remark)[YG occupancy: "
                + "149636 K (153600 K)]2016-02-18T17:23:09.738+0100: 13.749: [Rescan (parallel) , 0.0216980 secs]"
                + "2016-02-18T17:23:09.760+0100: 13.771: [weak refs processing, 0.0005180 secs]"
                + "2016-02-18T17:23:09.760+0100: 13.772: [scrub string table, 0.0015820 secs] "
                + "[1 CMS-remark: 217008K(341376K)] 366644K(494976K), 0.0239510 secs] "
                + "[Times: user=0.18 sys=0.00, real=0.02 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString() + ".",
                DateStampPrefixPreprocessAction.match(logLine));
        DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(logLine);
        String preprocessedLogLine = "13.749: [GC (CMS Final Remark)[YG occupancy: 149636 K (153600 K)]13.749: "
                + "[Rescan (parallel) , 0.0216980 secs]13.771: [weak refs processing, 0.0005180 secs]13.772: "
                + "[scrub string table, 0.0015820 secs] [1 CMS-remark: 217008K(341376K)] 366644K(494976K), "
                + "0.0239510 secs] [Times: user=0.18 sys=0.00, real=0.02 secs]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }
}
