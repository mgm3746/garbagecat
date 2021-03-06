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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelPreprocessAction {

    @Test
    public void testLogLineEndFull() {
        String logLine = " [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] "
                + "387470K->387085K(1518912K) [PSPermGen: 115215K->115215K(238912K)], 1.5692400 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineEndTimes() {
        String logLine = ", 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    @Test
    public void testLogLineClassUnloading() {
        String logLine = "65.343: [Full GC[Unloading class $Proxy111]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
        ParallelPreprocessAction event = new ParallelPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals("Log line not parsed correctly.", "65.343: [Full GC", event.getLogEntry());
    }

    @Test
    public void testLogLineGcTimeLimitExceedLineExceed() {
        String logLine = "3743.645: [Full GC [PSYoungGen: 419840K->415020K(839680K)] [PSOldGen: "
                + "5008922K->5008922K(5033984K)] 5428762K->5423942K(5873664K) [PSPermGen: "
                + "193275K->193275K(262144K)]      GC time would exceed GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineGcTimeLimitExceeding() {
        String logLine = "3924.453: [Full GC [PSYoungGen: 419840K->418436K(839680K)] [PSOldGen: "
                + "5008601K->5008601K(5033984K)] 5428441K->5427038K(5873664K) [PSPermGen: "
                + "193278K->193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineGcTimeLimitExceedMoreSpaces() {
        String logLine = "52843.722: [Full GC [PSYoungGen: 109696K->95191K(184960K)] [ParOldGen: "
                + "1307240K->1307182K(1310720K)] 1416936K->1402374K(1495680K) [PSPermGen: "
                + "113631K->113623K(196608K)]\tGC time is exceeding GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineGcTimeLimitExceedWithDatestamp() {
        String logLine = "2017-06-02T11:11:29.244+0530: 165944.630: [Full GC [PSYoungGen: 230400K->217423K(268800K)] "
                + "[PSOldGen: 1789951K->1789951K(1789952K)] 2020351K->2007375K(2058752K) "
                + "[PSPermGen: 188837K->188837K(524288K)]      GC time would exceed GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
    }

    @Test
    public void testLogLineBeginningParallelScavenge() {
        String logLine = "10.392: [GC";
        assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL.toString() + ".",
                ParallelPreprocessAction.match(logLine));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code>.
     */
    @Test
    public void testSplitParallelSerialOldEventLogging() {
        File testFile = TestUtil.getFile("dataset9.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT));
        assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
    }

    /**
     * Test preprocessing <code>UnloadingClassPreprocessAction</code> with underlying
     * <code>ParallelSerialOldEvent</code>.
     */
    @Test
    public void testUnloadingClassPreprocessActionParallelSerialOldEventLogging() {
        File testFile = TestUtil.getFile("dataset24.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
    }

    /**
     * Test preprocessing <code>PrintTenuringDistributionPreprocessAction</code> with underlying
     * <code>ParallelScavengeEvent</code>.
     */
    @Test
    public void testSplitParallelScavengeEventLogging() {
        File testFile = TestUtil.getFile("dataset30.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE));
        assertTrue(Analysis.WARN_PRINT_TENURING_DISTRIBUTION + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_TENURING_DISTRIBUTION));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with logging mixed across multiple lines.
     */
    @Test
    public void testParallelSerialOldAcrossMultipleLinesMixedGcTimeLimitLogging() {
        File testFile = TestUtil.getFile("dataset132.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT));
        assertTrue(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
        assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
    }
}
