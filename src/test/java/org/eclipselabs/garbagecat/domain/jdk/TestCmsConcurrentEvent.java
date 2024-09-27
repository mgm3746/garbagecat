/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestCmsConcurrentEvent {

    @Test
    void testAbortablePreclean() {
        String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testAbortablePrecleanStart() {
        String logLine = "252.889: [CMS-concurrent-abortable-preclean-start]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testAbortablePrecleanStartWithOtherLoggingAppended() {
        String logLine = "252.889: [CMS-concurrent-abortable-preclean-start]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testAbortablePrecleanWithOtherLoggingAppended() {
        String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testAbortPrecleanDueToTime() {
        String logLine = " CMS: abort preclean due to time 32633.935: "
                + "[CMS-concurrent-abortable-preclean: 0.622/5.054 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testAbortPrecleanDueToTimeWithOtherLoggingAppended() {
        String logLine = " CMS: abort preclean due to time 32633.935: "
                + "[CMS-concurrent-abortable-preclean: 0.622/5.054 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPrefixWithOtherLoggingAppended() {
        String logLine = "2017-04-24T21:08:04.965+0100: 669960.868: [CMS-concurrent-sweep: 13.324/39.970 secs] "
                + "[Times: user=124.31 sys=2.44, real=39.97 secs] 6200850K->1243921K(7848704K), "
                + "[CMS Perm : 481132K->454310K(785120K)], 100.5538981 secs] "
                + "[Times: user=100.68 sys=0.14, real=100.56 secs]";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testDatestamp() {
        String logLine = "2017-06-23T08:12:13.943-0400: [CMS-concurrent-mark: 4.583/35144.874 secs] "
                + "[Times: user=29858.25 sys=2074.63, real=35140.48 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMark() {
        String logLine = "252.707: [CMS-concurrent-mark: 0.796/0.926 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkStart() {
        String logLine = "251.781: [CMS-concurrent-mark-start]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkStartWithOtherLoggingAppended() {
        String logLine = "251.781: [CMS-concurrent-mark-start]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkWithOtherLoggingAppended() {
        String logLine = "252.707: [CMS-concurrent-mark: 0.796/0.926 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkWithTimesData() {
        String logLine = "242107.737: [CMS-concurrent-mark: 0.443/10.257 secs] "
                + "[Times: user=6.00 sys=0.28, real=10.26 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkWithTimesData5Digits() {
        String logLine = "2017-06-23T08:12:13.943-0400: 39034.532: [CMS-concurrent-mark: 4.583/35144.874 secs] "
                + "[Times: user=29858.25 sys=2074.63, real=35140.48 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkWithTimesDataWithOtherLoggingAppended() {
        String logLine = "242107.737: [CMS-concurrent-mark: 0.443/10.257 secs] "
                + "[Times: user=6.00 sys=0.28, real=10.26 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "572289.495: [CMS572304.683: [CMS-concurrent-sweep: 17.692/44.143 secs] "
                + "[Times: user=97.86 sys=1.85, real=44.14 secs]";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.CMS_CONCURRENT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testPreclean() {
        String logLine = "252.888: [CMS-concurrent-preclean: 0.141/0.182 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanConcurrentMarkSweepWithCms() {
        String logLine = "572289.495: [CMS572304.683: [CMS-concurrent-sweep: 17.692/44.143 secs] "
                + "[Times: user=97.86 sys=1.85, real=44.14 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanConcurrentModeFailure() {
        String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs] "
                + "[Times: user=1.23 sys=0.02, real=0.21 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanConcurrentModeFailureWithOtherLoggingAppended() {
        String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs] "
                + "[Times: user=1.23 sys=0.02, real=0.21 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanStart() {
        String logLine = "252.707: [CMS-concurrent-preclean-start]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanStartWithOtherLoggingAppended() {
        String logLine = "252.707: [CMS-concurrent-preclean-start]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testPrecleanWithOtherLoggingAppended() {
        String logLine = "252.888: [CMS-concurrent-preclean: 0.141/0.182 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testReset() {
        String logLine = "258.344: [CMS-concurrent-reset: 0.079/0.079 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testResetStart() {
        String logLine = "258.265: [CMS-concurrent-reset-start]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testResetStartWithOtherLoggingAppended() {
        String logLine = "258.265: [CMS-concurrent-reset-start]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testResetWithOtherLoggingAppended() {
        String logLine = "258.344: [CMS-concurrent-reset: 0.079/0.079 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testSweep() {
        String logLine = "258.265: [CMS-concurrent-sweep: 4.134/5.076 secs]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testSweepStart() {
        String logLine = "253.189: [CMS-concurrent-sweep-start]";
        assertTrue(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testSweepStartWithOtherLoggingAppended() {
        String logLine = "253.189: [CMS-concurrent-sweep-start]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    @Test
    void testSweepWithOtherLoggingAppended() {
        String logLine = "258.265: [CMS-concurrent-sweep: 4.134/5.076 secs]x";
        assertFalse(CmsConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }
}
