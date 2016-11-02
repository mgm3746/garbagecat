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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.Main;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelOldCompactingEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.hsql.JvmDao;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationConcurrentTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationStoppedTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.CmsPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.DateStampPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ParallelSerialOldPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.PrintTenuringDistributionPreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

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
     * Preprocess log file. Remove extraneous information and format the log file for parsing.
     * 
     * @param logFile
     *            Raw garbage collection log file.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     * @return Preprocessed garbage collection log file.
     */
    public File preprocess(File logFile, Date jvmStartDate) {
        if (logFile == null)
            throw new IllegalArgumentException("logFile == null!!");

        File preprocessFile = new File(logFile.getPath() + ".pp");

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            String currentLogLine = "";
            String priorLogLine = "";
            String preprocessedLogLine = "";

            bufferedReader = new BufferedReader(new FileReader(logFile));
            bufferedWriter = new BufferedWriter(new FileWriter(preprocessFile));

            // Used for detangling intermingled logging events that span multiple lines
            List<String> entangledLogLines = new ArrayList<String>();
            // Used to provide context for preprocessing decisions
            Set<String> context = new HashSet<String>();

            String priorLogEntry = System.getProperty("line.separator");

            String nextLogLine = bufferedReader.readLine();
            while (nextLogLine != null) {
                preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                        entangledLogLines, context);
                if (preprocessedLogLine != null) {
                    if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                            && !priorLogEntry.matches(System.getProperty("line.separator"))) {
                        bufferedWriter.write(System.getProperty("line.separator") + preprocessedLogLine);
                    } else {
                        bufferedWriter.write(preprocessedLogLine);
                    }
                    priorLogEntry = preprocessedLogLine;
                }

                priorLogLine = currentLogLine;
                currentLogLine = nextLogLine;
                nextLogLine = bufferedReader.readLine();
            }

            // Process last line
            preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                    entangledLogLines, context);
            if (preprocessedLogLine != null) {
                if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                        && !priorLogEntry.matches(System.getProperty("line.separator"))) {
                    bufferedWriter.write(System.getProperty("line.separator") + preprocessedLogLine);
                } else {
                    bufferedWriter.write(preprocessedLogLine);
                }
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
        }

        return preprocessFile;
    }

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
     * @param entangledLogLines
     *            Log lines mixed in with other logging events.
     * @param context
     *            Information to make preprocessing decisions.
     * @return The preprocessed log line, or null if it was thrown away.
     */
    private String getPreprocessedLogEntry(String currentLogLine, String priorLogLine, String nextLogLine,
            Date jvmStartDate, List<String> entangledLogLines, Set<String> context) {

        String preprocessedLogLine = null;

        // First convert any datestamps to timestamps
        if (JdkUtil.isLogLineWithDateStamp(currentLogLine)) {
            // The datestamp prefixes or replaces the timestamp
            if (DateStampPrefixPreprocessAction.match(currentLogLine)) {
                // Datestamp + Timestamp combinationdataset83.txt => drop the timestamp
                DateStampPrefixPreprocessAction action = new DateStampPrefixPreprocessAction(currentLogLine);
                currentLogLine = action.getLogEntry();
            } else {
                // Datestamp only. Convert datestamp to timestamp.
                if (jvmStartDate == null) {
                    throw new IllegalArgumentException(
                            "JVM start datetime must be defined to do datestamp to timestamp conversion."
                                    + currentLogLine);
                }
                DateStampPreprocessAction action = new DateStampPreprocessAction(currentLogLine, jvmStartDate);
                currentLogLine = action.getLogEntry();
            }
        }
        // Other preprocessing
        if (isThrowawayEvent(currentLogLine)) {
            // Analysis
            if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_TRACE_CLASS_UNLOADING)) {
                if (ClassUnloadingEvent.match(currentLogLine)) {
                    jvmDao.getAnalysisKeys().add(Analysis.KEY_TRACE_CLASS_UNLOADING);
                }
            }
            if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_PRINT_HEAP_AT_GC)) {
                if (HeapAtGcEvent.match(currentLogLine)) {
                    jvmDao.getAnalysisKeys().add(Analysis.KEY_PRINT_HEAP_AT_GC);
                }
            }
            if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_PRINT_CLASS_HISTOGRAM)) {
                if (ClassHistogramEvent.match(currentLogLine)) {
                    jvmDao.getAnalysisKeys().add(Analysis.KEY_PRINT_CLASS_HISTOGRAM);
                }
            }
            currentLogLine = null;
        } else if (ParallelSerialOldPreprocessAction.match(currentLogLine)
                && !context.contains(CmsPreprocessAction.TOKEN) && !context.contains(G1PreprocessAction.TOKEN)) {
            ParallelSerialOldPreprocessAction action = new ParallelSerialOldPreprocessAction(priorLogLine,
                    currentLogLine, nextLogLine, entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (CmsPreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)
                && !context.contains(G1PreprocessAction.TOKEN)
                && !context.contains(ParallelSerialOldPreprocessAction.TOKEN)) {
            /*
             * ^^^ Verify not in the middle of G1 preprocessing. The following log line is common to both:
             * 
             * , 0.0209631 secs]
             */
            CmsPreprocessAction action = new CmsPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (PrintTenuringDistributionPreprocessAction.match(currentLogLine, priorLogLine)) {
            PrintTenuringDistributionPreprocessAction action = new PrintTenuringDistributionPreprocessAction(
                    currentLogLine);
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
            ApplicationStoppedTimePreprocessAction action = new ApplicationStoppedTimePreprocessAction(currentLogLine);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (G1PreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
            G1PreprocessAction action = new G1PreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else {
            preprocessedLogLine = currentLogLine;
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        }

        return preprocessedLogLine;
    }

    /**
     * Parse the garbage collection logging for the JVM run and store the data in the data store.
     * 
     * @param logFile
     *            The garbage collection log file.
     * @param reorder
     *            Whether or not to allow logging to be reordered by timestamp.
     */
    public void store(File logFile, boolean reorder) {

        if (logFile == null) {
            return;
        }

        // Parse gc log file
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(logFile));
            String logLine = bufferedReader.readLine();
            BlockingEvent priorEvent = null;
            while (logLine != null) {
                // If event has no timestamp, use most recent blocking timestamp in database.
                LogEvent event = JdkUtil.parseLogLine(logLine);
                if (event instanceof BlockingEvent) {

                    // Verify logging in correct order. If overridden, logging will be stored in database and reordered
                    // by timestamp for analysis.
                    if (!reorder && priorEvent != null && event.getTimestamp() < priorEvent.getTimestamp()) {
                        System.out.println("prior event: " + priorEvent.getLogEntry());
                        throw new TimeWarpException("Logging reversed: " + event.getLogEntry());
                    }

                    jvmDao.addBlockingEvent((BlockingEvent) event);

                    // Analysis

                    // 1) Explicit GC
                    if (event instanceof TriggerData) {
                        String trigger = ((TriggerData) event).getTrigger();
                        if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_SYSTEM_GC)) {
                            CollectorFamily collectorFamily = ((GcEvent) event).getCollectorFamily();

                            switch (collectorFamily) {
                            case G1:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL_G1)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_SERIAL_G1);
                                }
                                break;
                            case CMS:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL_CMS)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_SERIAL_CMS);
                                }
                                break;
                            case PARALLEL:
                                if (event instanceof ParallelSerialOldEvent) {
                                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL_PARALLEL)) {
                                        jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_SERIAL_PARALLEL);
                                    }
                                } else if (event instanceof ParallelOldCompactingEvent) {
                                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_PARALLEL)) {
                                        jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_PARALLEL);
                                    }
                                }
                                break;
                            case SERIAL:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_SERIAL);
                                }
                                break;
                            }
                        }
                    }

                    // 2) Serial collections not caused by explicit GC
                    if (event instanceof SerialCollection) {
                        String trigger = null;
                        if (event instanceof TriggerData) {
                            trigger = ((TriggerData) event).getTrigger();
                        }
                        CollectorFamily collectorFamily = ((GcEvent) event).getCollectorFamily();

                        if (trigger == null || (!trigger.matches(JdkRegEx.TRIGGER_SYSTEM_GC)
                                && !trigger.matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM))) {
                            switch (collectorFamily) {
                            case G1:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC_G1)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_SERIAL_GC_G1);
                                }
                                break;
                            case CMS:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC_CMS)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_SERIAL_GC_CMS);
                                }
                                break;
                            case PARALLEL:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC_PARALLEL)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_SERIAL_GC_PARALLEL);
                                }
                                break;
                            case SERIAL:
                                if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC)) {
                                    jvmDao.addAnalysisKey(Analysis.KEY_SERIAL_GC);
                                }
                                break;
                            }
                        }
                    }

                    // 3) CMS concurrent mode failure
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_CMS_CONCURRENT_MODE_FAILURE)) {
                        if (event instanceof CmsSerialOldEvent) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_CMS_CONCURRENT_MODE_FAILURE);
                            }
                        }
                    }

                    // 4) CMS concurrent mode interrupted
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_CMS_CONCURRENT_MODE_INTERRUPTED)) {
                        if (event instanceof CmsSerialOldEvent) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_CMS_CONCURRENT_MODE_INTERRUPTED);
                            }
                        }
                    }

                    // 5) CMS incremental mode
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_CMS_INCREMENTAL_MODE)) {
                        if (event instanceof ParNewEvent) {
                            if (((ParNewEvent) event).isIncrementalMode()) {
                                jvmDao.addAnalysisKey(Analysis.KEY_CMS_INCREMENTAL_MODE);
                            }
                        }
                    }

                    // 6) Heap dump initiated gc
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_HEAP_DUMP_INITIATED_GC)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_HEAP_DUMP_INITIATED_GC);
                            }
                        }
                    }

                    // 7) Heap inspection initiated gc
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_HEAP_INSPECTION_INITIATED_GC)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_HEAP_INSPECTION_INITIATED_GC);
                            }
                        }
                    }

                    // 8) PrintClassHistogram
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_PRINT_CLASS_HISTOGRAM)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_PRINT_CLASS_HISTOGRAM);
                            }
                        }
                    }

                    // 9) Metaspace allocation failure
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_METASPACE_ALLOCATION_FAILURE)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_METASPACE_ALLOCATION_FAILURE);
                            }
                        }
                    }

                    // 10) JVM TI explicit gc
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_JVMTI)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null
                                    && trigger.matches(JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION)) {
                                jvmDao.addAnalysisKey(Analysis.KEY_EXPLICIT_GC_JVMTI);
                            }
                        }
                    }

                    priorEvent = (BlockingEvent) event;

                } else if (event instanceof ApplicationStoppedTimeEvent) {
                    jvmDao.addStoppedTimeEvent((ApplicationStoppedTimeEvent) event);
                } else if (event instanceof HeaderCommandLineFlagsEvent) {
                    jvmDao.setOptions(((HeaderCommandLineFlagsEvent) event).getJvmOptions());
                } else if (event instanceof HeaderMemoryEvent) {
                    jvmDao.setMemory(((HeaderMemoryEvent) event).getLogEntry());
                } else if (event instanceof HeaderVersionEvent) {
                    jvmDao.setVersion(((HeaderVersionEvent) event).getLogEntry());
                } else if (event instanceof GcOverheadLimitEvent) {
                    if (!jvmDao.getAnalysisKeys().contains(Analysis.KEY_GC_OVERHEAD_LIMIT)) {
                        jvmDao.getAnalysisKeys().add(Analysis.KEY_GC_OVERHEAD_LIMIT);
                    }
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

                // Populate collector type list.
                if (event instanceof GcEvent) {
                    List<JdkUtil.CollectorFamily> collectorFamilies = jvmDao.getCollectorFamilies();
                    if (!collectorFamilies.contains(((GcEvent) event).getCollectorFamily())) {
                        collectorFamilies.add(((GcEvent) event).getCollectorFamily());
                    }
                }

                logLine = bufferedReader.readLine();
            }

            // Process final batches
            jvmDao.processBlockingBatch();
            jvmDao.processStoppedTimeBatch();
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

    }

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
                        bottlenecks.add(
                                JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvm.getStartDate()));
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
        // Override any options passed in on command line
        if (jvmDao.getOptions() != null) {
            jvmRun.getJvm().setOptions(jvmDao.getOptions());
        }
        jvmRun.getJvm().setMemory(jvmDao.getMemory());
        jvmRun.getJvm().setVersion(jvmDao.getVersion());
        jvmRun.setFirstGcTimestamp(jvmDao.getFirstGcTimestamp());
        jvmRun.setLastGcTimestamp(jvmDao.getLastGcTimestamp());
        jvmRun.setMaxHeapSpace(jvmDao.getMaxHeapSpace());
        jvmRun.setMaxHeapOccupancy(jvmDao.getMaxHeapOccupancy());
        jvmRun.setMaxPermSpace(jvmDao.getMaxPermSpace());
        jvmRun.setMaxPermOccupancy(jvmDao.getMaxPermOccupancy());
        jvmRun.setMaxPause(jvmDao.getMaxGcPause());
        jvmRun.setTotalGcPause(jvmDao.getTotalGcPause());
        jvmRun.setBlockingEventCount(jvmDao.getBlockingEventCount());
        jvmRun.setLastGcDuration(jvmDao.getLastGcDuration());
        jvmRun.setFirstStoppedTimestamp(jvmDao.getFirstStoppedTimestamp());
        jvmRun.setLastStoppedTimestamp(jvmDao.getLastStoppedTimestamp());
        jvmRun.setMaxStoppedTime(jvmDao.getMaxStoppedTime());
        jvmRun.setTotalStoppedTime(jvmDao.getTotalStoppedTime());
        jvmRun.setStoppedTimeEventCount(jvmDao.getStoppedTimeEventCount());
        jvmRun.setLastStoppedDuration(jvmDao.getLastStoppedDuration());
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.setEventTypes(jvmDao.getEventTypes());
        jvmRun.setCollectorFamiles(jvmDao.getCollectorFamilies());
        jvmRun.setAnalysisKeys(jvmDao.getAnalysisKeys());
        jvmRun.setBottlenecks(getBottlenecks(jvm, throughputThreshold));
        jvmRun.doAnalysis();
        return jvmRun;
    }

    /**
     * Determine whether or not the logging line is essential for GC analysis.
     * 
     * @param logLine
     *            The log line to test.
     * @return True if the logging event can be thrown away, false if it should be kept.
     */
    private boolean isThrowawayEvent(String logLine) {
        LogEvent event = JdkUtil.parseLogLine(logLine);
        return event instanceof ThrowAwayEvent;
    }
}
