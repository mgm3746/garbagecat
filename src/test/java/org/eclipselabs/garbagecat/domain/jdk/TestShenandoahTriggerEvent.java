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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahTriggerEvent {

    @Test
    void testLineJdk8TriggerLearning() {
        String logLine = "Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerLearning() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_TRIGGER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahTriggerEvent,
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_TRIGGER);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testLineUnifiedTriggerAverage() {
        String logLine = "[0.864s][info][gc] Trigger: Average GC time (15.91 ms) is above the time for allocation rate "
                + "(829.64 MB/s) to deplete free headroom (11M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineJdkTriggerAverageGc() {
        String logLine = "Trigger: Average GC time (12.56 ms) is above the time for allocation rate (899 MB/s) to "
                + "deplete free headroom (11466K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerAverageGc() {
        String logLine = "[41.917s][info][gc           ] Trigger: Average GC time (26.32 ms) is above the time for "
                + "allocation rate (324.68 MB/s) to deplete free headroom (8M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerAverageGcAllocationRateWholeNumber() {
        String logLine = "[1.757s][info][gc           ] Trigger: Average GC time (9.74 ms) is above the time for "
                + "allocation rate (1244 MB/s) to deplete free headroom (11236K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerAverageGcAllocationRateKb6Digits() {
        String logLine = "[63.328s][info][gc           ] Trigger: Average GC time (77.12 ms) is above the time for "
                + "allocation rate (101894 KB/s) to deplete free headroom (6846K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerAverageGcUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Trigger: Average GC time (52.77 ms) is above the "
                + "time for allocation rate (1313.84 MB/s) to deplete free headroom (67M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerFree() {
        String logLine = "[24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerFreeNoDecorator() {
        String logLine = "Trigger: Free (168M) is below minimum threshold (168M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerFreeSpacesAfterGc() {
        String logLine = "[49.186s][info][gc           ] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:49.297-0200][18209ms] Trigger: Free (128M) is below minimum threshold "
                + "(130M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerFreeLearning() {
        String logLine = "[0.410s][info][gc           ] Trigger: Learning 3 of 5. Free (45613K) is below initial "
                + "threshold (45875K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTriggerHandleAllocationFailure() {
        String logLine = "[52.883s][info][gc           ] Trigger: Handle Allocation Failure";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedFreeTimeSinceLastGcUptimeMillis() {
        String logLine = "[2019-02-05T15:10:00.671-0200][1349583ms] Trigger: Time since last GC (300004 ms) is larger "
                + "than guaranteed interval (300000 ms)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testLineUnifiedTrigger() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Trigger: Learning 1 of 5. Free (912M) is below "
                + "initial threshold (912M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }
}
