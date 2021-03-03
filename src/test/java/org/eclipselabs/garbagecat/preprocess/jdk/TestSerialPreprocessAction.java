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
package org.eclipselabs.garbagecat.preprocess.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestSerialPreprocessAction {

    @Test
    public void testLogLineBeginSerialNew() {
        String logLine = "10.204: [GC 10.204: [DefNew";
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SERIAL.toString() + ".",
                SerialPreprocessAction.match(logLine));
        SerialPreprocessAction event = new SerialPreprocessAction(null, logLine, null, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineEndSerialNew() {
        String logLine = ": 36825K->4352K(39424K), 0.0224830 secs] 44983K->14441K(126848K), 0.0225800 secs]";
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.SERIAL.toString() + ".",
                SerialPreprocessAction.match(logLine));
        SerialPreprocessAction event = new SerialPreprocessAction(null, logLine, null, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testSerialNewPrintTenuringDistributionPreprocessing() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset17.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.SERIAL_NEW));
    }
}
