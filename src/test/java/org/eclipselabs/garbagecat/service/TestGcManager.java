/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.util.Constants;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcManager {

    /**
     * Test for NullPointerException caused by Issue 17:
     * http://code.google.com/a/eclipselabs.org/p/garbagecat/issues/detail?id=17
     */
    @Test
    void testCmsConcurrentAbortablePrecleanStartPreprocessing() {
        String currentLogLine = "233307.425: [CMS-concurrent-abortable-preclean-start]" + Constants.LINE_SEPARATOR;
        String priorLogLine = null;
        String nextLogLine = null;
        Date jvmStartDate = null;
        List<String> entangledLogLines = null;
        Set<String> context = new HashSet<String>();
        GcManager gcManager = new GcManager();
        String preprocessedLogLine = gcManager.getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine,
                jvmStartDate, entangledLogLines, context);
        assertEquals(currentLogLine, preprocessedLogLine, "Preprocessing incorrectly changed log line.");
    }

    @Test
    void testCmsConcurrentPrecleanPreprocessing() {
        String currentLogLine = "233307.425: [CMS-concurrent-preclean: 0.137/0.151 secs]" + Constants.LINE_SEPARATOR;
        String priorLogLine = null;
        String nextLogLine = null;
        Date jvmStartDate = null;
        List<String> entangledLogLines = null;
        Set<String> context = new HashSet<String>();
        GcManager gcManager = new GcManager();
        String preprocessedLogLine = gcManager.getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine,
                jvmStartDate, entangledLogLines, context);
        assertEquals(currentLogLine, preprocessedLogLine, "Preprocessing incorrectly changed log line.");
    }

    @Test
    void testCmsConcurrentPrecleanStartPreprocessing() {
        String currentLogLine = "233307.273: [CMS-concurrent-preclean-start]" + Constants.LINE_SEPARATOR;
        String priorLogLine = null;
        String nextLogLine = null;
        Date jvmStartDate = null;
        List<String> entangledLogLines = null;
        Set<String> context = new HashSet<String>();
        GcManager gcManager = new GcManager();
        String preprocessedLogLine = gcManager.getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine,
                jvmStartDate, entangledLogLines, context);
        assertEquals(currentLogLine, preprocessedLogLine, "Preprocessing incorrectly changed log line.");
    }

    /**
     * Test for NullPointerException.
     * 
     * @throws IOException
     */
    @Test
    void testNullPointerExceptionNotRaised() throws IOException {
        File testFile = TestUtil.getFile("dataset31.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
    }
}
