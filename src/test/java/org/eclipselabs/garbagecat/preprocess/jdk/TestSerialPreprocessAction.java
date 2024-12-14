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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestSerialPreprocessAction {

    @Test
    void testLogLineBeginSerialNew() {
        String logLine = "10.204: [GC 10.204: [DefNew";
        Set<String> context = new HashSet<String>();
        assertTrue(SerialPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SERIAL.toString() + ".");
        SerialPreprocessAction event = new SerialPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndSerialNew() {
        String logLine = ": 36825K->4352K(39424K), 0.0224830 secs] 44983K->14441K(126848K), 0.0225800 secs]";
        Set<String> context = new HashSet<String>();
        assertTrue(SerialPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SERIAL.toString() + ".");
        SerialPreprocessAction event = new SerialPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialNewPrintTenuringDistributionPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset17.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.SERIAL_NEW),
                "Log line not recognized as " + JdkUtil.EventType.SERIAL_NEW.toString() + ".");
    }
}
