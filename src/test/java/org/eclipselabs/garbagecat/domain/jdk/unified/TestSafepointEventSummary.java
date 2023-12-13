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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestSafepointEventSummary {

    @Test
    void test() throws IOException {
        File testFile = TestUtil.getFile("dataset280.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SAFEPOINT),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        List<SafepointEventSummary> summaries = jvmRun.getSafepointEventSummaries();
        Iterator<SafepointEventSummary> iterator = summaries.iterator();
        // Summary time is in microseconds
        long safepointTimeMax = Long.MIN_VALUE;
        long safepointTimeTotal = 0;
        while (iterator.hasNext()) {
            SafepointEventSummary summary = iterator.next();
            if (summary.getPauseMax() > safepointTimeMax) {
                safepointTimeMax = summary.getPauseMax();
            }
            safepointTimeTotal += summary.getPauseTotal();
        }
        // assertEquals(15620, safepointTimeMax, "Safepoint Summary safepoint time max not correct.");
        // assertEquals(18314, safepointTimeTotal, "Safepoint Summary safepoint time total not correct.");
        // assertEquals(15620098, jvmRun.getUnifiedSafepointTimeMax(), "JVM Run safepoint time max not correct.");
        // assertEquals(18317754, jvmRun.getUnifiedSafepointTimeTotal(), "JVM Run safepoint time total not correct.");
        // assertEquals(JdkMath.convertMicrosToSecs(safepointTimeMax),
        // JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeMax()),
        // "Safepoint Summary vs. JVM Run max safepoint time mismatch.");
        // assertEquals(JdkMath.convertMicrosToSecs(safepointTimeTotal),
        // JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeTotal()),
        // "Safepoint Summary vs. JVM Run total safepoint time mismatch.");
    }
}
