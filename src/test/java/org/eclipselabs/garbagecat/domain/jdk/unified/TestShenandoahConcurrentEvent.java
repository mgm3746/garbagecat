/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
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
 */
public class TestShenandoahConcurrentEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahConcurrentEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONCURRENT);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testMarking() {
        String logLine = "[0.528s][info][gc] GC(1) Concurrent marking 16M->17M(64M) 7.045ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testMarkingProcessWeakrefs() {
        String logLine = "[0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M->19M(64M) 15.264ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testMarkingUpdateRefs() {
        String logLine = "[10.458s][info][gc] GC(279) Concurrent marking (update refs) 47M->48M(64M) 5.559ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Concurrent marking (update refs) (process weakrefs) "
                + "49M->49M(64M) 5.416ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testPrecleaning() {
        String logLine = "[0.455s][info][gc] GC(0) Concurrent precleaning 19M->19M(64M) 0.202ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testEvacuation() {
        String logLine = "[0.465s][info][gc] GC(0) Concurrent evacuation 17M->19M(64M) 6.528ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testUpdateReferences() {
        String logLine = "[0.470s][info][gc] GC(0) Concurrent update references 19M->19M(64M) 4.708ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    public void testCleanup() {
        String logLine = "[0.472s][info][gc] GC(0) Concurrent cleanup 18M->15M(64M) 0.036ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }
}
