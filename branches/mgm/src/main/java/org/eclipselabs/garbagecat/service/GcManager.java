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
package org.eclipselabs.garbagecat.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipselabs.garbagecat.Main;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.Jvm;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.hsql.JvmDao;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationConcurrentTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationStoppedTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.DateStampPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.G1PrintGcDetailsPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.GcTimeLimitExceededPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ParNewCmsConcurrentPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.PrintHeapAtGcPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.PrintTenuringDistributionPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.UnloadingClassPreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * Provides garbage collection analysis services to other layers.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class GcManager {

    /**
     * The JVM data access object.
     */
    private JvmDao jvmDao;

    /**
     * Default constructor.
     */
    public GcManager() {
        this.jvmDao = new JvmDao();
        ;
    }

    /**
     * Remove extraneous information and format the log file for parsing.
     * 
     * @param logFile
     *            Raw garbage collection log file.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     * @return Preprocessed garbage collection log file. fixed Issue 17:
     *         https://code.google.com/a/eclipselabs.org/p/garbagecat/issues/detail?id=17
     */
    public File preprocess(File logFile, Date jvmStartDate) {
        if (logFile == null)
            throw new IllegalArgumentException("logFile == null!!");

        File preprocessFile = new File(logFile.getPath() + ".pp");
        long lLineCounter = 0;// will be used in statistics in near future

        // Preprocess log file

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            String currentLogLine = "";
            String priorLogLine = "";
            String preprocessedLogLine = "";

            bufferedReader = new BufferedReader(new FileReader(logFile));
            bufferedWriter = new BufferedWriter(new FileWriter(preprocessFile));

            String nextLogLine = bufferedReader.readLine();
            while (nextLogLine != null) {
                lLineCounter++;
                preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate);
                if (preprocessedLogLine != null) {
                    bufferedWriter.write(preprocessedLogLine);
                }

                priorLogLine = currentLogLine;
                currentLogLine = nextLogLine;
                nextLogLine = bufferedReader.readLine();
            }// while()

            // Process last line
            preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate);
            if (preprocessedLogLine != null) {
                bufferedWriter.write(preprocessedLogLine);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            // Close streams
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }// finally

        return preprocessFile;
    }// preprocess()

    /**
     * Determine the preprocessed log entry given the current, previous, and next log lines.
     * 
     * The previous log line is needed to prevent preprocessing overlap where preprocessors have common patterns that
     * are treated in different ways (e.g. removing vs. keeping matches, line break at end vs. no line break, etc.). For
     * example, there is overlap between the <code>CmsConcurrentModeFailurePreprocessEvent</code> and the
     * <code>PrintHeapAtGcPreprocessEvent</code>.
     * 
     * The next log line is needed to distinguish between truncated and split logging. A truncated log entry can look
     * exactly the same as the initial line of split logging.
     * 
     * @param currentLogLine
     *            The current log line.
     * @param priorLogLine
     *            The previous log line.
     * @param nextLogLine
     *            The next log line.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     * @return
     */
    private String getPreprocessedLogEntry(String currentLogLine, String priorLogLine, String nextLogLine,
            Date jvmStartDate) {
        String preprocessedLogLine = null;
        if (!JdkUtil.discardLogLine(currentLogLine)) {
            // First convert any datestamps to timestamps
            if (DateStampPreprocessAction.match(currentLogLine)) {
                // The datestamp prefixes or replaces the timestamp
                if (DateStampPrefixPreprocessAction.match(currentLogLine)) {
                    DateStampPrefixPreprocessAction action = new DateStampPrefixPreprocessAction(currentLogLine);
                    currentLogLine = action.getLogEntry();
                } else {
                    if (jvmStartDate == null) {
                        throw new IllegalArgumentException(
                                "JVM start datetime must be defined to do datestamp to timestamp conversion.");
                    }
                    DateStampPreprocessAction action = new DateStampPreprocessAction(currentLogLine, jvmStartDate);
                    currentLogLine = action.getLogEntry();
                }
            }
            // Other preprocessing
            if (UnloadingClassPreprocessAction.match(currentLogLine)) {
                UnloadingClassPreprocessAction action = new UnloadingClassPreprocessAction(currentLogLine, nextLogLine);
                preprocessedLogLine = action.getLogEntry();
            } else if (CmsConcurrentModeFailurePreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
                CmsConcurrentModeFailurePreprocessAction action = new CmsConcurrentModeFailurePreprocessAction(
                        currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (GcTimeLimitExceededPreprocessAction.match(currentLogLine, priorLogLine)) {
                GcTimeLimitExceededPreprocessAction action = new GcTimeLimitExceededPreprocessAction(currentLogLine);
                preprocessedLogLine = action.getLogEntry();
            } else if (PrintHeapAtGcPreprocessAction.match(currentLogLine)) {
                PrintHeapAtGcPreprocessAction action = new PrintHeapAtGcPreprocessAction(currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (PrintTenuringDistributionPreprocessAction.match(currentLogLine)) {
                PrintTenuringDistributionPreprocessAction action = new PrintTenuringDistributionPreprocessAction(
                        currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (ParNewCmsConcurrentPreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
                ParNewCmsConcurrentPreprocessAction action = new ParNewCmsConcurrentPreprocessAction(currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (ApplicationConcurrentTimePreprocessAction.match(currentLogLine, priorLogLine)) {
                ApplicationConcurrentTimePreprocessAction action = new ApplicationConcurrentTimePreprocessAction(
                        currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (ApplicationStoppedTimePreprocessAction.match(currentLogLine, priorLogLine)) {
                ApplicationStoppedTimePreprocessAction action = new ApplicationStoppedTimePreprocessAction(
                        currentLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (G1PrintGcDetailsPreprocessAction.match(currentLogLine)) {
                G1PrintGcDetailsPreprocessAction action = new G1PrintGcDetailsPreprocessAction(currentLogLine,
                        nextLogLine);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else {
                preprocessedLogLine = currentLogLine + System.getProperty("line.separator");
            }
        }
        return preprocessedLogLine;
    }

    /**
     * Parse the garbage collection logging for the JVM run and store the data in the data store.
     * 
     * @param logFile
     *            The garbage collection log file.
     */
    public void store(File logFile) {

        if (logFile == null) {
            return;
        }

        // Parse gc log file
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(logFile));
            String logLine = bufferedReader.readLine();
            while (logLine != null) {
                // If event has no timestamp, use most recent blocking
                // timestamp in database.
                LogEvent event = JdkUtil.parseLogLine(logLine);
                if (event instanceof BlockingEvent) {
                    jvmDao.addBlockingEvent((BlockingEvent) event);
                } else if (event instanceof UnknownEvent) {
                    if (jvmDao.getUnidentifiedLogLines().size() < Main.REJECT_LIMIT) {
                        jvmDao.getUnidentifiedLogLines().add(logLine);
                    }
                }

                // Populate events list.
                List<JdkUtil.LogEventType> eventTypes = jvmDao.getEventTypes();
                JdkUtil.LogEventType eventType = JdkUtil.determineEventType(event.getName());
                if (!eventTypes.contains(eventType)) {
                    eventTypes.add(eventType);
                }

                logLine = bufferedReader.readLine();
            }// while()

            // Process final batch
            jvmDao.processBatch();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }// store()

    /**
     * Determine <code>BlockingEvent</code>s where throughput since last event does not meet the throughput goal.
     * 
     * @param jvm
     *            The JVM environment information.
     * @param throughputThreshold
     *            The bottleneck reporting throughput threshold.
     * @return A <code>List</code> of <code>BlockingEvent</code>s where the throughput between events is less than the
     *         throughput threshold goal.
     */
    private List<String> getBottlenecks(Jvm jvm, int throughputThreshold) {
        ArrayList<String> bottlenecks = new ArrayList<String>();
        List<BlockingEvent> blockingEvents = jvmDao.getBlockingEvents();
        Iterator<BlockingEvent> iterator = blockingEvents.iterator();
        BlockingEvent priorEvent = null;
        while (iterator.hasNext()) {
            BlockingEvent event = iterator.next();
            if (priorEvent != null && JdkUtil.isBottleneck(event, priorEvent, throughputThreshold)) {
                if (bottlenecks.size() == 0) {
                    // Add current and prior event
                    if (jvm.getStartDate() != null) {
                        // Convert timestamps to date/time
                        bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                jvm.getStartDate()));
                        bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(),
                                jvm.getStartDate()));
                    } else {
                        bottlenecks.add(priorEvent.getLogEntry());
                        bottlenecks.add(event.getLogEntry());
                    }
                } else {
                    if (jvm.getStartDate() != null) {
                        // Compare datetime, since bottleneck has datetime
                        if (!JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvm.getStartDate())
                                .equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                    jvm.getStartDate()));
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(),
                                    jvm.getStartDate()));
                        } else {
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(),
                                    jvm.getStartDate()));
                        }
                    } else {
                        // Compare timestamps, since bottleneck has timestamp
                        if (!priorEvent.getLogEntry().equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(priorEvent.getLogEntry());
                            bottlenecks.add(event.getLogEntry());
                        } else {
                            bottlenecks.add(event.getLogEntry());
                        }
                    }
                }
            }
            priorEvent = event;
        }
        return bottlenecks;
    }

    /**
     * @param jvmOptions
     *            The JVM options used for the JVM run.
     * @return A <code>List</code> of analysis points based on the JVM options and data.
     */
    private List<String> doAnalysis(Jvm jvm) {
        List<String> analysis = new ArrayList<String>();

        // 1) Check for partial log
        if (GcUtil.isPartialLog(jvmDao.getFirstTimestamp())) {
            analysis.add(Constants.WARNING_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }

        // JVM options analysis
        if (jvm.getOptions() != null) {

            // 2) Check to see if thread stack size explicitly set
            if (jvm.getThreadStackSizeOption() == null) {
                analysis.add(GcUtil.getPropertyValue("analysis", "thread.stack.size.not.set"));
            }

            // 3) Check to see if min and max heap sizes are the same
            if (!jvm.isMinAndMaxHeapSpaceEqual()) {
                analysis.add(GcUtil.getPropertyValue("analysis", "min.heap.not.equal.max.heap"));
            }

            // 4) Check to see if min and max perm gen sizes are the same
            if (!jvm.isMinAndMaxPermSpaceEqual()) {
                analysis.add(GcUtil.getPropertyValue("analysis", "min.perm.not.equal.max.perm"));
            }

            // TODO: Check to see if explicit GC interval is disabled or set.

            // TODO: If explicit GC interval is set, try disabling explicit GC.

            // TODO: Check for instrumentation.

            // TODO: -Xbatch warning

        }
        return analysis;
    }

    /**
     * Get JVM run data.
     * 
     * @param jvm
     *            JVM environment information.
     * @param throughputThreshold
     *            The throughput threshold for bottleneck reporting.
     * @return The JVM run data.
     */
    public JvmRun getJvmRun(Jvm jvm, int throughputThreshold) {
        JvmRun jvmRun = new JvmRun(jvm, throughputThreshold);
        jvmRun.setFirstTimestamp(jvmDao.getFirstTimestamp());
        jvmRun.setLastTimestamp(jvmDao.getLastTimestamp());
        jvmRun.setMaxHeapSpace(jvmDao.getMaxHeapSpace());
        jvmRun.setMaxHeapOccupancy(jvmDao.getMaxHeapOccupancy());
        jvmRun.setMaxPermSpace(jvmDao.getMaxPermSpace());
        jvmRun.setMaxPermOccupancy(jvmDao.getMaxPermOccupancy());
        jvmRun.setMaxPause(jvmDao.getMaxPause());
        jvmRun.setTotalPause(jvmDao.getTotalPause());
        jvmRun.setBlockingEventCount(jvmDao.getBlockingEventCount());
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.setEventTypes(jvmDao.getEventTypes());
        jvmRun.setBottlenecks(getBottlenecks(jvm, throughputThreshold));
        // Can still do some anlysis, even without GC data
        jvmRun.setAnalysis(doAnalysis(jvm));
        // Calculate throughput if there are blocking events
        if (jvmRun.getBlockingEventCount() > 0) {
            jvmRun.setThroughput(jvmDao.getThroughput());
        } else {
            jvmRun.setThroughput(100L);
        }
        return jvmRun;
    }
}
