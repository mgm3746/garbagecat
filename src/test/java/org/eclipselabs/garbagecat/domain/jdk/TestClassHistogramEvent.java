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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestClassHistogramEvent {

    @Test
    void testNotBlocking() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testColumnsNameLine() {
        String logLine = " num     #instances         #bytes  class name";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testHeaderDividerLine() {
        String logLine = "----------------------------------------------";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassDataWithBracketLine() {
        String logLine = "   1:       9249662      876131272  [C";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassDataWithNumberLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassDataWithInnerClassLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassDataWithUnderscoreLine() {
        String logLine = "27647:             1             16  com.example.Myclass$MyInner_someThing";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassDataWithSemicolonLine() {
        String logLine = "   4:       4149724      232421736  [Ljava.lang.Object;";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testTotalLine() {
        String logLine = "Total      16227637     1059670840";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void test5SpacesBeforeTenDigitBytesLine() {
        String logLine = "   1:       3786335     1564600208  [Ljava.lang.Object;";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void test6DigitLineNumberLine() {
        String logLine = "100000:             1             16  com.msh.rules.regimensearch.Rule_regimen"
                + "SearchRule_841_a1deb60c00004d67b538438881011c7aDefaultConsequenceInvoker";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void test6SpacesBeforeInstancesLine() {
        String logLine = "   1:      98460990     7018731456  [I";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testTotal11DigitBytesLine() {
        String logLine = "Total     159091427    12666890520";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testG1PreprocessedLine() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testJdk6PreprocessedLine() {
        String logLine = "471478.440: [Class Histogram, 15.6352805 secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testClassWithForwardSlash() {
        String logLine = " 116:           318           7632  "
                + "io.micrometer.prometheus.PrometheusMeterRegistry$$Lambda$53/635371680";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testTotal8SpacesBeforeInstances() {
        String logLine = "Total        271481       20043160";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testPreprocessedBeforeFullGc() {
        String logLine = "2021-10-07T10:05:34.135+0100: 69302.241: [Class Histogram (before full gc):, 4.7148918 "
                + "secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testPreprocessedAfterFullGcDatestampTimestamp() {
        String logLine = "2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc):, 4.5682980 secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testPreprocessedAfterFullGcDatestamp() {
        String logLine = "2021-10-07T10:05:58.708+0100: [Class Histogram (after full gc):, 4.5682980 secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }

    @Test
    void testPreprocessedAfterFullGcTimestamp() {
        String logLine = "69326.814: [Class Histogram (after full gc):, 4.5682980 secs]";
        assertTrue(ClassHistogramEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".");
    }
}
