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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestClassUnloadingEvent {

    @Test
    void testNotBlocking() {
        String logLine = " [Unloading class $Proxy225]";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        String logLine = " [Unloading class $Proxy225]";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testLine() {
        String logLine = "[Unloading class $Proxy61]";
        assertTrue(ClassUnloadingEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".");
    }

    @Test
    void testLineWithUnderline() {
        String logLine = "[Unloading class MyClass_1234153487841_717989]";
        assertTrue(ClassUnloadingEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".");
    }

    @Test
    void testLogLineWithBeginningSpace() {
        String logLine = " [Unloading class $Proxy225]";
        assertTrue(ClassUnloadingEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".");
    }

    /**
     * Test preparsing.
     * 
     */
    @Test
    void testTraceClassUnloadingPreprocessing() {
        File testFile = TestUtil.getFile("dataset84.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD),
                JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING),
                Analysis.WARN_TRACE_CLASS_UNLOADING + " analysis not identified.");
    }
}
