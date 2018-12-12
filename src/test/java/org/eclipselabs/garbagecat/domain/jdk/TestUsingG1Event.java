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
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUsingG1Event extends TestCase {

    public void testNotBlocking() {
        String logLine = "[0.005s][info][gc] Using G1";
        Assert.assertFalse(JdkUtil.LogEventType.USING_G1.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLine() {
        String logLine = "[0.005s][info][gc] Using G1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".",
                UsingG1Event.match(logLine));
        UsingG1Event event = new UsingG1Event(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 5, event.getTimestamp());
    }

    public void testLineWithSpaces() {
        String logLine = "[0.005s][info][gc] Using G1   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".",
                UsingG1Event.match(logLine));
    }

    /**
     * Test logging.
     */
    public void testLog() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset152.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_G1));
    }
}
