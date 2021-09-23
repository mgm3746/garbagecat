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
package org.eclipselabs.garbagecat.service;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

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
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.Main;
import org.eclipselabs.garbagecat.dao.JvmDao;
import org.eclipselabs.garbagecat.domain.ApplicationLoggingEvent;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsIncrementalModeCollector;
import org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.FlsStatisticsEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcLockerEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ReferenceGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationConcurrentTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationStoppedTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.CmsPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.DateStampPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ParallelPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.SerialPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ShenandoahPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
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
     * Whether or not the JVM events are from a preprocessed file.
     */
    private boolean preprocessed;

    /**
     * Last log line unprocessed.
     */
    private String lastLogLineUnprocessed;

    /**
     * Default constructor.
     */
    public GcManager() {
        this.jvmDao = new JvmDao();
    }

    public boolean isPreprocessed() {
        return preprocessed;
    }

    public String getLastLogLineUnprocessed() {
        return lastLogLineUnprocessed;
    }

    private static final Date jvmStartDate = GcUtil.parseStartDateTime("2000-01-01 00:00:00.000");

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

            String priorLogEntry = Constants.LINE_SEPARATOR;

            String nextLogLine = bufferedReader.readLine();
            while (nextLogLine != null) {
                preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                        entangledLogLines, context);
                if (preprocessedLogLine != null) {
                    if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                            && !priorLogEntry.endsWith(Constants.LINE_SEPARATOR)) {
                        bufferedWriter.write(Constants.LINE_SEPARATOR + preprocessedLogLine);
                    } else {
                        bufferedWriter.write(preprocessedLogLine);
                    }
                    priorLogEntry = preprocessedLogLine;
                }

                priorLogLine = currentLogLine;
                currentLogLine = nextLogLine;
                nextLogLine = bufferedReader.readLine();

                if (nextLogLine == null) {
                    lastLogLineUnprocessed = currentLogLine;
                }
            }

            // Process last line
            preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                    entangledLogLines, context);
            if (preprocessedLogLine != null) {
                if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                        && !priorLogEntry.endsWith(Constants.LINE_SEPARATOR)) {
                    bufferedWriter.write(Constants.LINE_SEPARATOR + preprocessedLogLine);
                } else {
                    bufferedWriter.write(preprocessedLogLine);
                }
            }

            // output entangled log lines
            if (!entangledLogLines.isEmpty()) {
                for (String logLine : entangledLogLines) {
                    bufferedWriter.write(Constants.LINE_SEPARATOR + logLine);
                }
                // Reset entangled log lines
                entangledLogLines.clear();
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
            preprocessed = true;
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

        if (currentLogLine != null)

            // Convert datestamp to timestamp.
            if (jvmStartDate != null && DateStampPreprocessAction.match(currentLogLine)) {
                DateStampPreprocessAction action = new DateStampPreprocessAction(currentLogLine, jvmStartDate);
                currentLogLine = action.getLogEntry();
            }

        /*
         * Other preprocessing.
         * 
         * Check context collector type to account for common logging patterns across collector families. For example
         * the following logging output is common to CMS and G1:
         * 
         * , 0.0209631 secs]
         */

        if (isThrowawayEvent(currentLogLine)) {
            // Analysis
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING)) {
                if (ClassUnloadingEvent.match(currentLogLine)
                        && !jvmDao.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_TRACE_CLASS_UNLOADING);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_HEAP_AT_GC)) {
                // Only match initial line, as FooterHeapEvent and HeatAtGcEvent share patterns
                if (currentLogLine.matches("^.+Heap (after|before) (gc|GC) invocations.+$")) {
                    jvmDao.getAnalysis().add(Analysis.WARN_PRINT_HEAP_AT_GC);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_CLASS_HISTOGRAM)) {
                if (ClassHistogramEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_CLASS_HISTOGRAM);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS)) {
                if (FlsStatisticsEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.INFO_PRINT_FLS_STATISTICS);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_TENURING_DISTRIBUTION)) {
                if (TenuringDistributionEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_PRINT_TENURING_DISTRIBUTION);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME)) {
                if (ApplicationConcurrentTimeEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_APPLICATION_LOGGING)) {
                if (ApplicationLoggingEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_APPLICATION_LOGGING);
                }
            }
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED)) {
                if (ReferenceGcEvent.match(currentLogLine)) {
                    jvmDao.getAnalysis().add(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED);
                }
            }
            currentLogLine = null;
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN)
                && ShenandoahPreprocessAction.match(currentLogLine)) {
            ShenandoahPreprocessAction action = new ShenandoahPreprocessAction(priorLogLine, currentLogLine,
                    nextLogLine, entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && UnifiedPreprocessAction.match(currentLogLine)) {
            UnifiedPreprocessAction action = new UnifiedPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(UnifiedPreprocessAction.TOKEN)
                && ParallelPreprocessAction.match(currentLogLine)) {
            ParallelPreprocessAction action = new ParallelPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(SerialPreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN)
                && CmsPreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_HEAP_AT_GC)) {
                // Only match initial line, as FooterHeapEvent and HeatAtGcEvent share patterns
                if (currentLogLine.matches("^.+Heap (after|before) (gc|GC) invocations.+$")) {
                    jvmDao.getAnalysis().add(Analysis.WARN_PRINT_HEAP_AT_GC);
                }
            }
            CmsPreprocessAction action = new CmsPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(SerialPreprocessAction.TOKEN)
                && !context.contains(ParallelPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN)
                && ApplicationConcurrentTimePreprocessAction.match(currentLogLine, priorLogLine)) {
            ApplicationConcurrentTimePreprocessAction action = new ApplicationConcurrentTimePreprocessAction(
                    currentLogLine, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                && !context.contains(CmsPreprocessAction.TOKEN) && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN)
                && ApplicationStoppedTimePreprocessAction.match(currentLogLine, priorLogLine)) {
            ApplicationStoppedTimePreprocessAction action = new ApplicationStoppedTimePreprocessAction(currentLogLine,
                    context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(SerialPreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                && !context.contains(CmsPreprocessAction.TOKEN) && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN)
                && G1PreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
            G1PreprocessAction action = new G1PreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else if (!context.contains(ApplicationStoppedTimePreprocessAction.TOKEN)
                && !context.contains(ApplicationConcurrentTimePreprocessAction.TOKEN)
                && !context.contains(ParallelPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ShenandoahPreprocessAction.TOKEN)
                && !context.contains(UnifiedPreprocessAction.TOKEN) && SerialPreprocessAction.match(currentLogLine)) {
            SerialPreprocessAction action = new SerialPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                    entangledLogLines, context);
            if (action.getLogEntry() != null) {
                preprocessedLogLine = action.getLogEntry();
            }
        } else {
            // Output any entangled log lines
            if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
                for (String logLine : entangledLogLines) {
                    if (preprocessedLogLine == null) {
                        preprocessedLogLine = logLine;
                    } else {
                        preprocessedLogLine = preprocessedLogLine + Constants.LINE_SEPARATOR + logLine;
                    }
                }
                // Reset entangled log lines
                entangledLogLines.clear();
            }
            if (preprocessedLogLine == null) {
                preprocessedLogLine = currentLogLine;
            } else {
                preprocessedLogLine = preprocessedLogLine + Constants.LINE_SEPARATOR + currentLogLine;
            }
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
                // If event has no timestamp, use most recent blocking timestamp.
                LogEvent event = JdkUtil.parseLogLine(logLine);
                if (event instanceof BlockingEvent) {

                    // Verify logging in correct order. If overridden, logging will be stored and reordered by timestamp
                    // for analysis.
                    if (!reorder && priorEvent != null && event.getTimestamp() < priorEvent.getTimestamp()) {
                        throw new TimeWarpException("Logging reversed: " + Constants.LINE_SEPARATOR
                                + priorEvent.getLogEntry() + Constants.LINE_SEPARATOR + event.getLogEntry());
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
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1)
                                        && event instanceof G1FullGcEvent) {
                                    jvmDao.addAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1);
                                } else if (!jvmDao.getAnalysis()
                                        .contains(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK)
                                        && event instanceof G1YoungInitialMarkEvent) {
                                    jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK);
                                }
                                break;
                            case CMS:
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS)) {
                                    jvmDao.addAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS);
                                }
                                break;
                            case PARALLEL:
                                if (event instanceof ParallelSerialOldEvent) {
                                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL)) {
                                        jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL);
                                    }
                                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL)) {
                                        jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL);
                                    }
                                } else if (event instanceof ParallelCompactingOldEvent) {
                                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_PARALLEL)) {
                                        jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_PARALLEL);
                                    }
                                }
                                break;
                            case SERIAL:
                                if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL)) {
                                    jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_SERIAL);
                                }
                                break;
                            case SHENANDOAH:
                                break;
                            case UNKNOWN:
                                if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN)) {
                                    jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN);
                                }
                                break;
                            default:
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
                                && !trigger.matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM)
                                && !trigger.matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC)
                                && !trigger.matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC))) {
                            switch (collectorFamily) {
                            case G1:
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1)) {
                                    jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_G1);
                                }
                                break;
                            case CMS:
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS)) {
                                    jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_CMS);
                                }
                                break;
                            case PARALLEL:
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL)) {
                                    jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL);
                                }
                                break;
                            case SERIAL:
                                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC)) {
                                    jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC);
                                }
                                break;
                            case UNKNOWN:
                                break;
                            default:
                                break;
                            }
                        }
                    }

                    // 3) CMS concurrent mode failure
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE)) {
                        if (event instanceof CmsSerialOldEvent) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE)) {
                                jvmDao.addAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE);
                            }
                        }
                    }

                    // 4) CMS concurrent mode interrupted
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED)) {
                        if (event instanceof CmsSerialOldEvent) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED)) {
                                jvmDao.addAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED);
                            }
                        }
                    }

                    // 5) CMS incremental mode
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE)) {
                        if (event instanceof CmsIncrementalModeCollector) {
                            if (((CmsIncrementalModeCollector) event).isIncrementalMode()) {
                                jvmDao.addAnalysis(Analysis.WARN_CMS_INCREMENTAL_MODE);
                            }
                        }
                    }

                    // 6) Heap dump initiated gc
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_INITIATED_GC)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC)) {
                                jvmDao.addAnalysis(Analysis.WARN_HEAP_DUMP_INITIATED_GC);
                            }
                        }
                    }

                    // 7) Heap inspection initiated gc
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC)) {
                                jvmDao.addAnalysis(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC);
                            }
                        }
                    }

                    // 8) Metaspace allocation failure
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION)) {
                                jvmDao.addAnalysis(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE);
                            }
                        }
                    }

                    // 9) JVM TI explicit gc
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_JVMTI)) {
                        if (event instanceof TriggerData) {
                            String trigger = ((TriggerData) event).getTrigger();
                            if (trigger != null
                                    && trigger.matches(JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION)) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_JVMTI);
                            }
                        }
                    }

                    // 10) G1 evacuation failure
                    if (event instanceof TriggerData) {
                        String trigger = ((TriggerData) event).getTrigger();
                        if (trigger != null && (trigger.matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED)
                                || trigger.matches(JdkRegEx.TRIGGER_TO_SPACE_OVERFLOW))) {
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE)) {
                                jvmDao.addAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE);
                            }
                        }
                    }

                    // 11) CMS promotion failure
                    if (event instanceof TriggerData) {
                        String trigger = ((TriggerData) event).getTrigger();
                        if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_PROMOTION_FAILED)) {
                            CollectorFamily collectorFamily = ((GcEvent) event).getCollectorFamily();
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_PROMOTION_FAILED)
                                    && collectorFamily.equals(CollectorFamily.CMS)) {
                                jvmDao.addAnalysis(Analysis.ERROR_CMS_PROMOTION_FAILED);
                            }
                        }
                    }

                    // 12) -XX:+PrintGCCause is essential for troubleshooting G1 full GCs
                    if (event instanceof G1FullGcEvent) {
                        String trigger = ((TriggerData) event).getTrigger();
                        if (trigger == null) {
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                                jvmDao.addAnalysis(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED);
                            }
                        }
                    }

                    // 13) CMS_REMARK class unloading
                    if (event instanceof CmsRemarkEvent && !((CmsRemarkEvent) event).isClassUnloading()
                            && !jvmDao.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)) {
                        jvmDao.addAnalysis(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
                    }

                    // 14) Humongous allocation
                    if (event instanceof G1Collector && event instanceof TriggerData
                            && !jvmDao.getAnalysis().contains(Analysis.INFO_G1_HUMONGOUS_ALLOCATION)) {
                        String trigger = ((TriggerData) event).getTrigger();
                        if (trigger != null && trigger.matches(JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION)) {
                            jvmDao.addAnalysis(Analysis.INFO_G1_HUMONGOUS_ALLOCATION);
                        }
                    }

                    // 15) Inverted parallelism
                    if (event instanceof ParallelEvent && event instanceof TimesData) {
                        if (((TimesData) event).getTimeUser() != TimesData.NO_DATA
                                && ((TimesData) event).getTimeReal() != TimesData.NO_DATA) {
                            jvmDao.setParallelCount(jvmDao.getParallelCount() + 1);
                            if (event instanceof TimesData && ((TimesData) event).getTimeUser() > 0
                                    && JdkMath.isInvertedParallelism(((TimesData) event).getParallelism())) {
                                jvmDao.setInvertedParallelismCount(jvmDao.getInvertedParallelismCount() + 1);
                                if (jvmDao.getWorstInvertedParallelismEvent() == null) {
                                    jvmDao.setWorstInvertedParallelismEvent(event);
                                } else {
                                    if (((TimesData) event)
                                            .getParallelism() < ((TimesData) jvmDao.getWorstInvertedParallelismEvent())
                                                    .getParallelism()) {
                                        // Update lowest "low"
                                        jvmDao.setWorstInvertedParallelismEvent(event);
                                    }
                                }
                            }
                        }
                    }

                    // 16) Check for CMS initial mark low parallelism
                    if (event instanceof CmsInitialMarkEvent && ((TimesData) event).getTimeUser() > 0
                            && ((TimesData) event).getTimeReal() > 0 && ((BlockingEvent) event).getDuration() >= 10000
                            && JdkMath.isLowParallelism(((TimesData) event).getParallelism())) {
                        if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM)) {
                            jvmDao.addAnalysis(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM);
                        }
                    }

                    // 17) Check for CMS remark low parallelism
                    if (event instanceof CmsRemarkEvent && ((TimesData) event).getTimeUser() > 0
                            && ((TimesData) event).getTimeReal() > 0 && ((BlockingEvent) event).getDuration() >= 10000
                            && JdkMath.isLowParallelism(((TimesData) event).getParallelism())) {
                        if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM)) {
                            jvmDao.addAnalysis(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM);
                        }
                    }

                    // 18) Check for old JDKs using perm gen
                    if (event instanceof PermMetaspaceData && event.getLogEntry() != null
                            && event.getLogEntry().matches("^.*Perm.*$")) {
                        if (!jvmDao.getAnalysis().contains(Analysis.INFO_PERM_GEN)) {
                            jvmDao.addAnalysis(Analysis.INFO_PERM_GEN);
                        }
                    }

                    // 19) Shenandoah Full GC
                    if (event instanceof ShenandoahFullGcEvent) {
                        if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SHENANDOAH_FULL_GC)) {
                            jvmDao.addAnalysis(Analysis.ERROR_SHENANDOAH_FULL_GC);
                        }
                    }

                    priorEvent = (BlockingEvent) event;

                } else if (event instanceof ApplicationStoppedTimeEvent) {
                    jvmDao.addStoppedTimeEvent((ApplicationStoppedTimeEvent) event);
                } else if (event instanceof UnifiedSafepointEvent) {
                    jvmDao.addSafepointEvent((UnifiedSafepointEvent) event);
                } else if (event instanceof HeaderCommandLineFlagsEvent) {
                    jvmDao.setOptions(((HeaderCommandLineFlagsEvent) event).getJvmOptions());
                } else if (event instanceof HeaderMemoryEvent) {
                    jvmDao.setMemory(((HeaderMemoryEvent) event).getLogEntry());
                    jvmDao.setPhysicalMemory((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getPhysicalMemory()));
                    jvmDao.setPhysicalMemoryFree(
                            (long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getPhysicalMemoryFree()));
                    jvmDao.setSwap((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getSwap()));
                    jvmDao.setSwapFree((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getSwapFree()));
                } else if (event instanceof HeaderVersionEvent) {
                    jvmDao.setVersion(((HeaderVersionEvent) event).getLogEntry());
                } else if (event instanceof GcOverheadLimitEvent) {
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED)) {
                        jvmDao.getAnalysis().add(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED);
                    }
                } else if (event instanceof GcLockerEvent) {
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED)) {
                        jvmDao.addAnalysis(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED);
                    }
                } else if (event instanceof ShenandoahConcurrentEvent) {
                    if (greater(((CombinedData) event).getCombinedOccupancyInit(),
                            jvmDao.getMaxHeapOccupancyNonBlocking())) {
                        jvmDao.setMaxHeapOccupancyNonBlocking(
                                (int) ((CombinedData) event).getCombinedOccupancyInit().getValue(KILOBYTES));
                    }
                    if (greater(((CombinedData) event).getCombinedSpace(), jvmDao.getMaxHeapSpaceNonBlocking())) {
                        jvmDao.setMaxHeapSpaceNonBlocking(
                                (int) ((CombinedData) event).getCombinedSpace().getValue(KILOBYTES));
                    }
                    if (greater(((PermMetaspaceData) event).getPermOccupancyInit(),
                            jvmDao.getMaxPermOccupancyNonBlocking())) {
                        jvmDao.setMaxPermOccupancyNonBlocking(
                                (int) ((PermMetaspaceData) event).getPermOccupancyInit().getValue(KILOBYTES));
                    }
                    if (greater(((PermMetaspaceData) event).getPermSpace(), jvmDao.getMaxPermSpaceNonBlocking())) {
                        jvmDao.setMaxPermSpaceNonBlocking(
                                (int) ((PermMetaspaceData) event).getPermSpace().getValue(KILOBYTES));
                    }
                } else if (event instanceof UnknownEvent) {
                    // Don't count reportable events with datestamp only as unidentified
                    DateStampPreprocessAction preprocessAction = new DateStampPreprocessAction(logLine, jvmStartDate);
                    LogEvent preprocessedEvent = null;
                    if (preprocessAction.getLogEntry() != null) {
                        preprocessedEvent = JdkUtil.parseLogLine(preprocessAction.getLogEntry());
                    } //
                    if (preprocessedEvent != null
                            && JdkUtil.isReportable(LogEventType.valueOf(preprocessedEvent.getName()))) {
                        if (!jvmDao.getAnalysis().contains(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP)) {
                            jvmDao.getAnalysis().add(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP);
                        }
                    } else {
                        if (jvmDao.getUnidentifiedLogLines().size() < Main.REJECT_LIMIT) {
                            jvmDao.getUnidentifiedLogLines().add(logLine);
                        }
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

                // Check for partial last line
                if (logLine == null) {
                    if (event instanceof UnknownEvent && jvmDao.getUnidentifiedLogLines().size() == 1) {
                        jvmDao.addAnalysis(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST);
                    }
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
        }

    }

    private static boolean greater(Memory memory, int value) {
        return memory != null && memory.getValue(KILOBYTES) > value;
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
        List<String> bottlenecks = new ArrayList<String>();
        List<BlockingEvent> blockingEvents = jvmDao.getBlockingEvents();
        BlockingEvent priorEvent = null;
        for (BlockingEvent event : blockingEvents) {
            if (priorEvent != null && JdkUtil.isBottleneck(event, priorEvent, throughputThreshold)) {
                if (bottlenecks.isEmpty()) {
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
        jvmRun.setPreprocessed(this.preprocessed);
        jvmRun.setLastLogLineUnprocessed(lastLogLineUnprocessed);
        // Override any options passed in on command line
        if (jvmDao.getOptions() != null) {
            jvmRun.getJvm().setOptions(jvmDao.getOptions());
        }
        jvmRun.getJvm().setMemory(jvmDao.getMemory());
        jvmRun.getJvm().setPhysicalMemory(new Memory(jvmDao.getPhysicalMemory(), BYTES));
        jvmRun.getJvm().setPhysicalMemoryFree(new Memory(jvmDao.getPhysicalMemoryFree(), BYTES));
        jvmRun.getJvm().setSwap(new Memory(jvmDao.getSwap(), BYTES));
        jvmRun.getJvm().setSwapFree(new Memory(jvmDao.getSwapFree(), BYTES));
        jvmRun.getJvm().setVersion(jvmDao.getVersion());
        jvmRun.setFirstGcEvent(jvmDao.getFirstGcEvent());
        jvmRun.setLastGcEvent(jvmDao.getLastGcEvent());
        jvmRun.setMaxYoungSpace(kilobytes(jvmDao.getMaxYoungSpace()));
        jvmRun.setMaxOldSpace(kilobytes(jvmDao.getMaxOldSpace()));
        jvmRun.setMaxHeapSpace(kilobytes(jvmDao.getMaxHeapSpace()));
        jvmRun.setMaxHeapOccupancy(kilobytes(jvmDao.getMaxHeapOccupancy()));
        jvmRun.setMaxHeapAfterGc(kilobytes(jvmDao.getMaxHeapAfterGc()));
        jvmRun.setMaxPermSpace(kilobytes(jvmDao.getMaxPermSpace()));
        jvmRun.setMaxPermOccupancy(kilobytes(jvmDao.getMaxPermOccupancy()));
        jvmRun.setMaxPermAfterGc(kilobytes(jvmDao.getMaxPermAfterGc()));
        jvmRun.setGcPauseMax(jvmDao.getMaxGcPause());
        jvmRun.setGcPauseTotal(jvmDao.getGcPauseTotal());
        jvmRun.setBlockingEventCount(jvmDao.getBlockingEventCount());
        jvmRun.setFirstSafepointEvent(jvmDao.getFirstSafepointEvent());
        jvmRun.setLastSafepointEvent(jvmDao.getLastSafepointEvent());
        jvmRun.setStoppedTimeMax(jvmDao.getStoppedTimeMax());
        jvmRun.setStoppedTimeTotal(jvmDao.getStoppedTimeTotal());
        jvmRun.setStoppedTimeEventCount(jvmDao.getStoppedTimeEventCount());
        jvmRun.setUnifiedSafepointTimeMax(jvmDao.getUnifiedSafepointTimeMax());
        jvmRun.setUnifiedSafepointTimeTotal(jvmDao.getUnifiedSafepointTimeTotal());
        jvmRun.setUnifiedSafepointEventCount(jvmDao.getUnifiedSafepointEventCount());
        jvmRun.setSafepointEventSummaries(jvmDao.getSafepointEventSummaries());
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.setEventTypes(jvmDao.getEventTypes());
        jvmRun.setCollectorFamilies(jvmDao.getCollectorFamilies());
        jvmRun.setAnalysis(jvmDao.getAnalysis());
        jvmRun.setBottlenecks(getBottlenecks(jvm, throughputThreshold));
        jvmRun.setParallelCount(jvmDao.getParallelCount());
        jvmRun.setInvertedParallelismCount(jvmDao.getInvertedParallelismCount());
        jvmRun.setWorstInvertedParallelismEvent(jvmDao.getWorstInvertedParallelismEvent());
        jvmRun.setMaxHeapOccupancyNonBlocking(kilobytes(jvmDao.getMaxHeapOccupancyNonBlocking()));
        jvmRun.setMaxHeapSpaceNonBlocking(kilobytes(jvmDao.getMaxHeapSpaceNonBlocking()));
        jvmRun.setMaxPermOccupancyNonBlocking(kilobytes(jvmDao.getMaxPermOccupancyNonBlocking()));
        jvmRun.setMaxPermSpaceNonBlocking(kilobytes(jvmDao.getMaxPermSpaceNonBlocking()));
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
        return JdkUtil.parseLogLine(logLine) instanceof ThrowAwayEvent;
    }
}
