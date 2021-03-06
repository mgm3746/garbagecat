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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUsingSerialEvent {

    @Test
    public void testLine() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".",
                UsingSerialEvent.match(logLine));
        UsingSerialEvent event = new UsingSerialEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3, event.getTimestamp());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertEquals(JdkUtil.LogEventType.USING_SERIAL + "not identified.", JdkUtil.LogEventType.USING_SERIAL,
                JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertTrue(JdkUtil.LogEventType.USING_SERIAL.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UsingSerialEvent);
    }

    @Test
    public void testNotBlocking() {
        String logLine = "[0.003s][info][gc] Using Serial";
        assertFalse(JdkUtil.LogEventType.USING_SERIAL.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.USING_SERIAL.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.USING_SERIAL));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SERIAL);
        assertTrue(JdkUtil.LogEventType.USING_SERIAL.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLineWithSpaces() {
        String logLine = "[0.003s][info][gc] Using Serial     ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".",
                UsingSerialEvent.match(logLine));
    }

    /**
     * Test logging.
     */
    @Test
    public void testLog() {
        File testFile = TestUtil.getFile("dataset147.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL));
    }
}
