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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelSerialOldPreprocessAction extends TestCase {

    public void testLogLineEndFull() {
        String logLine = " [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] "
                + "387470K->387085K(1518912K) [PSPermGen: 115215K->115215K(238912K)], 1.5692400 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
        ParallelSerialOldPreprocessAction event = new ParallelSerialOldPreprocessAction(null, logLine, nextLogLine,
                null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineEndTimes() {
        String logLine = ", 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
        ParallelSerialOldPreprocessAction event = new ParallelSerialOldPreprocessAction(null, logLine, nextLogLine,
                null, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineClassUnloading() {
        String logLine = "65.343: [Full GC[Unloading class $Proxy111]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
        ParallelSerialOldPreprocessAction event = new ParallelSerialOldPreprocessAction(null, logLine, nextLogLine,
                null, context);
        Assert.assertEquals("Log line not parsed correctly.", "65.343: [Full GC", event.getLogEntry());
    }

    public void testLogLineGcTimeLimitExceedLineExceed() {
        String logLine = "3743.645: [Full GC [PSYoungGen: 419840K->415020K(839680K)] [PSOldGen: "
                + "5008922K->5008922K(5033984K)] 5428762K->5423942K(5873664K) [PSPermGen: "
                + "193275K->193275K(262144K)]      GC time would exceed GCTimeLimit of 98%";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
    }

    public void testLogLineGcTimeLimitExceeding() {
        String logLine = "3924.453: [Full GC [PSYoungGen: 419840K->418436K(839680K)] [PSOldGen: "
                + "5008601K->5008601K(5033984K)] 5428441K->5427038K(5873664K) [PSPermGen: "
                + "193278K->193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
    }

    public void testLogLineGcTimeLimitExceedMoreSpaces() {
        String logLine = "52843.722: [Full GC [PSYoungGen: 109696K->95191K(184960K)] [ParOldGen: "
                + "1307240K->1307182K(1310720K)] 1416936K->1402374K(1495680K) [PSPermGen: "
                + "113631K->113623K(196608K)]\tGC time is exceeding GCTimeLimit of 98%";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code>.
     */
    public void testSplitParallelSerialOldEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset9.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT));
        Assert.assertTrue(Analysis.KEY_GC_OVERHEAD_LIMIT + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_GC_OVERHEAD_LIMIT));
    }

    /**
     * Test preprocessing <code>UnloadingClassPreprocessAction</code> with underlying
     * <code>ParallelSerialOldEvent</code>.
     */
    public void testUnloadingClassPreprocessActionParallelSerialOldEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset24.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
    }
}
