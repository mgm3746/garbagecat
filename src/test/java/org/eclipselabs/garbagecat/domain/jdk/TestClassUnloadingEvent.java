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

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestClassUnloadingEvent extends TestCase {

    public void testLine() {
        String logLine = "[Unloading class $Proxy61]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".",
                ClassUnloadingEvent.match(logLine));
    }

    public void testLineWithUnderline() {
        String logLine = "[Unloading class MyClass_1234153487841_717989]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".",
                ClassUnloadingEvent.match(logLine));
    }

    public void testLogLineWithBeginningSpace() {
        String logLine = " [Unloading class $Proxy225]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".",
                ClassUnloadingEvent.match(logLine));
    }

    public void testNotBlocking() {
        String logLine = " [Unloading class $Proxy225]";
        Assert.assertFalse(JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    /**
     * Test preparsing.
     * 
     */
    public void testTraceClassUnloadingPreprocessing() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset84.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_TRACE_CLASS_UNLOADING + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_TRACE_CLASS_UNLOADING));
    }
}
