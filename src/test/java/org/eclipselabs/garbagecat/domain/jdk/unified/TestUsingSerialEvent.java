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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUsingSerialEvent {

    @Test
    void testLine() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertTrue(UsingSerialEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
        UsingSerialEvent event = new UsingSerialEvent(logLine);
        assertEquals((long) 3,event.getTimestamp(),"Time stamp not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertEquals(JdkUtil.LogEventType.USING_SERIAL,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.USING_SERIAL + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UsingSerialEvent, JdkUtil.LogEventType.USING_SERIAL.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.USING_SERIAL.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_SERIAL), JdkUtil.LogEventType.USING_SERIAL.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SERIAL);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.USING_SERIAL.toString() + " not indentified as unified.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.003s][info][gc] Using Serial     ";
        assertTrue(UsingSerialEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
    }

    /**
     * Test logging.
     */
    @Test
    void testLog() {
        File testFile = TestUtil.getFile("dataset147.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL), "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
    }
}
