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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestClassUnloadingEvent {

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

    @Test
    void testNotBlocking() {
        String logLine = " [Unloading class $Proxy225]";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        String logLine = " [Unloading class $Proxy225]";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.CLASS_UNLOADING.toString() + " incorrectly indentified as reportable.");
    }

    /**
     * Test preparsing.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testTraceClassUnloadingPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset84.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD),
                JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_TRACE_CLASS_UNLOADING.getKey()),
                org.github.joa.util.Analysis.INFO_TRACE_CLASS_UNLOADING + " analysis not identified.");
    }
}
