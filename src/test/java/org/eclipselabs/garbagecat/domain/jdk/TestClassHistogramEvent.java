/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
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
public class TestClassHistogramEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        Assert.assertFalse(JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        Assert.assertFalse(JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testColumnsNameLine() {
        String logLine = " num     #instances         #bytes  class name";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testHeaderDividerLine() {
        String logLine = "----------------------------------------------";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testClassDataWithBracketLine() {
        String logLine = "   1:       9249662      876131272  [C";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testClassDataWithNumberLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testClassDataWithInnerClassLine() {
        String logLine = "27714:             1             16  sun.reflect.GeneratedMethodAccessor1500";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testClassDataWithUnderscoreLine() {
        String logLine = "27647:             1             16  com.example.Myclass$MyInner_someThing";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testClassDataWithSemicolonLine() {
        String logLine = "   4:       4149724      232421736  [Ljava.lang.Object;";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testTotalLine() {
        String logLine = "Total      16227637     1059670840";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void test5SpacesBeforeTenDigitBytesLine() {
        String logLine = "   1:       3786335     1564600208  [Ljava.lang.Object;";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void test6DigitLineNumberLine() {
        String logLine = "100000:             1             16  com.msh.rules.regimensearch.Rule_regimen"
                + "SearchRule_841_a1deb60c00004d67b538438881011c7aDefaultConsequenceInvoker";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void test65SpacesBeforeInstancesLine() {
        String logLine = "   1:      98460990     7018731456  [I";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testTotal11DigitBytesLine() {
        String logLine = "Total     159091427    12666890520";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testG1PreprocessedLine() {
        String logLine = "49709.036: [Class Histogram (after full gc):, 2.4232900 secs] "
                + "[Times: user=29.91 sys=0.08, real=22.24 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }

    public void testJdk6PreprocessedLine() {
        String logLine = "471478.440: [Class Histogram, 15.6352805 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_HISTOGRAM.toString() + ".",
                ClassHistogramEvent.match(logLine));
    }
}
