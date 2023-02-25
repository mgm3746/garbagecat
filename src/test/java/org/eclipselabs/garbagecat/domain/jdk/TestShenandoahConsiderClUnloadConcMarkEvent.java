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
class TestShenandoahConsiderClUnloadConcMarkEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK,
                JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK + "not identified.");
    }

    @Test
    void testLineTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.090-0200][2ms] Consider -XX:+ClassUnloadingWithConcurrentMark if large "
                + "pause times are observed on class-unloading sensitive workloads";
        assertTrue(ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".");
        ShenandoahConsiderClassUnloadingConcMarkEvent event = new ShenandoahConsiderClassUnloadingConcMarkEvent(
                logLine);
        assertEquals((long) 2, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineUnified() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        assertTrue(ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".");
        ShenandoahConsiderClassUnloadingConcMarkEvent event = new ShenandoahConsiderClassUnloadingConcMarkEvent(
                logLine);
        assertEquals((long) 1, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads     ";
        assertTrue(ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                        + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahConsiderClassUnloadingConcMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK),
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                        + " indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                        + " incorrectly indentified as unified.");
    }
}
