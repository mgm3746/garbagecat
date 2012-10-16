/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkMath extends TestCase {

    public void testConvertDurationToMillis() {
        String secs = "0.0225213";
        Assert.assertEquals("Secs not converted to milliseconds properly.", 22, JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testConvertDurationDecimalCommaToMillis() {
        String secs = "0,0225213";
        Assert.assertEquals("Secs not converted to milliseconds properly.", 22, JdkMath.convertSecsToMillis(secs).intValue());
    }

    /**
     * Durations are always rounded down.
     */
    public void testConvertDurationToMillisRoundUp() {
        String secs = "0.0975";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 97, JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testConvertDurationToMillisRoundDown() {
        String secs = "0.0985";
        Assert.assertEquals("Secs not converted to milliseconds with expected rounding mode.", 98, JdkMath.convertSecsToMillis(secs).intValue());
    }

    public void testThroughput() {
        int duration = 81;
        long timestamp = 1000;
        int priorDuration = 10;
        long priorTimestamp = 900;
        Assert.assertEquals(50, JdkMath.calcThroughput(duration, timestamp, priorDuration, priorTimestamp));
    }

    public void testTotalCmsRemarkDuration() {
        String[] durations = new String[3];
        durations[0] = "0.0226730";
        durations[1] = "0.0624566";
        durations[2] = "0.0857010";
        Assert.assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    public void testTotalCmsRemarkDecimalCommaDuration() {
        String[] durations = new String[3];
        durations[0] = "0,0226730";
        durations[1] = "0,0624566";
        durations[2] = "0,0857010";
        Assert.assertEquals("CMS Remark times not added properly.", 170, JdkMath.totalDuration(durations));
    }

    public void testConvertDurationMillisToSecs() {
        long duration = 123456;
        Assert.assertEquals("Millis not converted to seconds with expected rounding mode.", "123.456", JdkMath.convertMillisToSecs(duration).toString());
    }

}
