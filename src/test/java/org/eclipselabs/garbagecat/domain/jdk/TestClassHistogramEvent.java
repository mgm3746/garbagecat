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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestClassHistogramEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertFalse(JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertFalse(JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testColumnsNameLine() {
        String logLine = " num     #instances         #bytes  class name";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testHeaderDividerLine() {
        String logLine = "----------------------------------------------";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassDataWithBracketLine() {
        String logLine = "   1:       9249662      876131272  [C";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassDataWithNumberLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassDataWithInnerClassLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassDataWithUnderscoreLine() {
        String logLine = "27647:             1             16  com.example.Myclass$MyInner_someThing";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassDataWithSemicolonLine() {
        String logLine = "   4:       4149724      232421736  [Ljava.lang.Object;";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testTotalLine() {
        String logLine = "Total      16227637     1059670840";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void test5SpacesBeforeTenDigitBytesLine() {
        String logLine = "   1:       3786335     1564600208  [Ljava.lang.Object;";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void test6DigitLineNumberLine() {
        String logLine = "100000:             1             16  com.msh.rules.regimensearch.Rule_regimen"
                + "SearchRule_841_a1deb60c00004d67b538438881011c7aDefaultConsequenceInvoker";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void test6SpacesBeforeInstancesLine() {
        String logLine = "   1:      98460990     7018731456  [I";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testTotal11DigitBytesLine() {
        String logLine = "Total     159091427    12666890520";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testG1PreprocessedLine() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testJdk6PreprocessedLine() {
        String logLine = "471478.440: [Class Histogram, 15.6352805 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testClassWithForwardSlash() {
        String logLine = " 116:           318           7632  "
                + "io.micrometer.prometheus.PrometheusMeterRegistry$$Lambda$53/635371680";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    @Test
    public void testTotal8SpacesBeforeInstances() {
        String logLine = "Total        271481       20043160";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }
}
