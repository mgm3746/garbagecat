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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahTriggerEvent extends TestCase {

    public void testLineTriggerLearning() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_TRIGGER + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahTriggerEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_TRIGGER));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_TRIGGER);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineTriggerAverage() {
        String logLine = "[0.864s][info][gc] Trigger: Average GC time (15.91 ms) is above the time for allocation rate "
                + "(829.64 MB/s) to deplete free headroom (11M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerAverageGc() {
        String logLine = "[41.917s][info][gc           ] Trigger: Average GC time (26.32 ms) is above the time for "
                + "allocation rate (324.68 MB/s) to deplete free headroom (8M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerAverageGcAllocationRateWholeNumber() {
        String logLine = "[1.757s][info][gc           ] Trigger: Average GC time (9.74 ms) is above the time for "
                + "allocation rate (1244 MB/s) to deplete free headroom (11236K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerAverageGcAllocationRateKb6Digits() {
        String logLine = "[63.328s][info][gc           ] Trigger: Average GC time (77.12 ms) is above the time for "
                + "allocation rate (101894 KB/s) to deplete free headroom (6846K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerAverageGcUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Trigger: Average GC time (52.77 ms) is above the "
                + "time for allocation rate (1313.84 MB/s) to deplete free headroom (67M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLineTriggerFree() {
        String logLine = "[24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerFree() {
        String logLine = "[49.186s][info][gc           ] Trigger: Free (6M) is below minimum threshold (6M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:49.297-0200][18209ms] Trigger: Free (128M) is below minimum threshold "
                + "(130M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerFreeLearning() {
        String logLine = "[0.410s][info][gc           ] Trigger: Learning 3 of 5. Free (45613K) is below initial "
                + "threshold (45875K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogLineTriggerHandleAllocationFailure() {
        String logLine = "[52.883s][info][gc           ] Trigger: Handle Allocation Failure";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLogFreeTimeSinceLastGcUptimeMillis() {
        String logLine = "[2019-02-05T15:10:00.671-0200][1349583ms] Trigger: Time since last GC (300004 ms) is larger "
                + "than guaranteed interval (300000 ms)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testTrigger() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Trigger: Learning 1 of 5. Free (912M) is below "
                + "initial threshold (912M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }
}
