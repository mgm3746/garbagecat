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
package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.OptionsParser.getLatestVersion;
import static org.eclipselabs.garbagecat.OptionsParser.getVersion;
import static org.eclipselabs.garbagecat.OptionsParser.options;
import static org.eclipselabs.garbagecat.OptionsParser.parseOptions;
import static org.eclipselabs.garbagecat.util.Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
import static org.eclipselabs.garbagecat.util.Constants.LINE_SEPARATOR;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_LATEST_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OUTPUT_FILE_NAME;
import static org.eclipselabs.garbagecat.util.GcUtil.parseStartDateTime;
import static org.eclipselabs.garbagecat.util.Memory.ZERO;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.MEGABYTES;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PERM_GEN;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.jdk.unified.SafepointEventSummary;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;

/**
 * <p>
 * garbagecat main class. A controller that prepares the model (by parsing GC log entries) and provides analysis (the
 * report view).
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Main {

    /**
     * The maximum number of rejected log lines to track. A throttle to limit memory consumption.
     */
    public static final int REJECT_LIMIT = 1000;

    /**
     * Report single line break
     */
    private static final String LINEBREAK_SINGLE = "-------------------------------------------------------------------"
            + "----" + LINE_SEPARATOR;

    /**
     * Report double line break
     */
    private static final String LINEBREAK_DOUBLE = "==================================================================="
            + "====" + LINE_SEPARATOR;

    /**
     * @param args
     *            The argument list includes one or more scope options followed by the name of the gc log file to
     *            inspect.
     */
    public static void main(String... args) {
        try {
            CommandLine cmd = parseOptions(args);
            if (cmd == null || cmd.hasOption(OPTION_HELP_LONG) || cmd.hasOption(OPTION_HELP_LONG)) {
                usage();
            } else {
                createReport(cmd);
            }
        } catch (ParseException pe) {
            System.out.println(pe.getMessage());
            usage();
        }
    }

    /**
     * Output usage help.
     * 
     * @param options
     */
    private static void usage() {
        // Use the built in formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("garbagecat [OPTION]... [FILE]", options);
    }

    public static void createReport(CommandLine cmd) {
        // Determine JVM environment information.
        Date jvmStartDate = cmd.hasOption(OPTION_STARTDATETIME_LONG)
                ? parseStartDateTime(cmd.getOptionValue(OPTION_STARTDATETIME_SHORT))
                : null;
        String jvmOptions = cmd.hasOption(OPTION_JVMOPTIONS_LONG) ? cmd.getOptionValue(OPTION_JVMOPTIONS_SHORT) : null;
        String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
        File logFile = new File(logFileName);

        GcManager gcManager = new GcManager();

        // Do preprocessing
        if (cmd.hasOption(OPTION_PREPROCESS_LONG) || cmd.hasOption(OPTION_STARTDATETIME_LONG)) {
            /*
             * Requiring the JVM start date/time for preprocessing is a hack to handle datestamps. When garbagecat was
             * started there was no <code>-XX:+PrintGCDateStamps</code> option. When it was introduced in JDK 1.6 update
             * 4, the easiest thing to do to handle datestamps was to preprocess the datestamps and convert them to
             * timestamps.
             * 
             * TODO: Handle datetimes separately from preprocessing so preprocessing doesn't require passing in the JVM
             * start date/time.
             */
            logFile = gcManager.preprocess(logFile, jvmStartDate);
        }

        // Allow logging to be reordered?
        boolean reorder = cmd.hasOption(OPTION_REORDER_LONG);

        // Store garbage collection logging in data store.
        gcManager.store(logFile, reorder);

        // Create report
        Jvm jvm = new Jvm(jvmOptions, jvmStartDate);
        // Determine report options
        int throughputThreshold = cmd.hasOption(OPTION_THRESHOLD_LONG)
                ? Integer.parseInt(cmd.getOptionValue(OPTION_THRESHOLD_SHORT))
                : DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;

        JvmRun jvmRun = gcManager.getJvmRun(jvm, throughputThreshold);
        String outputFileName = cmd.hasOption(OPTION_OUTPUT_LONG) ? cmd.getOptionValue(OPTION_OUTPUT_SHORT)
                : OUTPUT_FILE_NAME;
        boolean version = cmd.hasOption(OPTION_VERSION_LONG);
        boolean latestVersion = cmd.hasOption(OPTION_LATEST_VERSION_LONG);
        createReport(jvmRun, outputFileName, version, latestVersion, logFileName);
    }

    /**
     * Create Garbage Collection Analysis report.
     * 
     * @param jvmRun
     *            JVM run data.
     * @param reportFileName
     *            Report file name.
     * @param version
     *            Whether or not to report garbagecat version.
     * @param latestVersion
     *            Whether or not to report latest garbagecat version.
     * @param gcLogFileName
     *            The gc log file analyzed.
     */
    public static void createReport(JvmRun jvmRun, String reportFileName, boolean version, boolean latestVersion,
            String gcLogFileName) {
        File reportFile = new File(reportFileName);
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            printWriter = new PrintWriter(fileWriter);
            File gcLogFile = new File(gcLogFileName);
            printWriter.write(gcLogFile.getName());
            printWriter.write(LINE_SEPARATOR);

            if (version || latestVersion) {
                printWriter.write(LINEBREAK_DOUBLE);
                if (version) {
                    printWriter.write(
                            "Running garbagecat version: " + getVersion() + System.getProperty("line.separator"));
                }
                if (latestVersion) {
                    printWriter.write("Latest garbagecat version/tag: " + getLatestVersion()
                            + System.getProperty("line.separator"));
                }
            }

            // GC Bottlenecks
            List<String> gcBottlenecks = jvmRun.getGcBottlenecks();
            if (!gcBottlenecks.isEmpty()) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write("GC throughput less than " + jvmRun.getThroughputThreshold() + "%" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);
                for (String gcBottleneck : gcBottlenecks) {
                    printWriter.write(gcBottleneck + LINE_SEPARATOR);
                }
            }

            // Safepoint Bottlenecks
            List<String> safepointBottlenecks = jvmRun.getSafepointBottlenecks();
            if (!safepointBottlenecks.isEmpty()) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write(
                        "Safepoint throughput less than " + jvmRun.getThroughputThreshold() + "%" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);
                for (String safepointBottleneck : safepointBottlenecks) {
                    printWriter.write(safepointBottleneck + LINE_SEPARATOR);
                }
            }

            // JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null
                    || jvmRun.getJvm().getMemory() != null) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write("JVM:" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);
                if (jvmRun.getJvm().getVersion() != null) {
                    printWriter.write("Version: " + jvmRun.getJvm().getVersion() + LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    printWriter.write("Options: " + jvmRun.getJvm().getOptions() + LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getMemory() != null) {
                    printWriter.write("Memory: " + jvmRun.getJvm().getMemory() + LINE_SEPARATOR);
                }
            }

            // Summary
            printWriter.write(LINEBREAK_DOUBLE);
            printWriter.write("SUMMARY:" + LINE_SEPARATOR);
            printWriter.write(LINEBREAK_SINGLE);

            // First/last timestamps
            if (jvmRun.getBlockingEventCount() > 0 || jvmRun.getStoppedTimeEventCount() > 0) {
                // First event
                String firstEventDatestamp = JdkUtil.getDateStamp(jvmRun.getFirstEvent().getLogEntry());
                if (firstEventDatestamp != null) {
                    printWriter.write("Datestamp First: ");
                    printWriter.write(firstEventDatestamp);
                    printWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getFirstEvent().getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)) {
                    printWriter.write("Timestamp First: ");
                    BigDecimal firstEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getFirstEvent().getTimestamp());
                    printWriter.write(firstEventTimestamp.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
                // Last event
                String lastEventDatestamp = JdkUtil.getDateStamp(jvmRun.getLastEvent().getLogEntry());
                if (lastEventDatestamp != null) {
                    printWriter.write("Datestamp Last: ");
                    printWriter.write(lastEventDatestamp);
                    printWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getLastEvent().getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)) {
                    printWriter.write("Timestamp Last: ");
                    BigDecimal lastEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getLastEvent().getTimestamp());
                    printWriter.write(lastEventTimestamp.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
            }

            // GC stats
            printWriter.write("# GC Events: " + jvmRun.getBlockingEventCount() + LINE_SEPARATOR);
            if (jvmRun.getBlockingEventCount() > 0) {
                printWriter.write("Event Types: ");
                List<LogEventType> eventTypes = jvmRun.getEventTypes();
                boolean firstEvent = true;
                for (LogEventType eventType : eventTypes) {
                    // Only report GC events
                    if (JdkUtil.isReportable(eventType)) {
                        if (!firstEvent) {
                            printWriter.write(", ");
                        }
                        printWriter.write(eventType.toString());
                        firstEvent = false;
                    }
                }
                printWriter.write(LINE_SEPARATOR);
                // Inverted parallelism. Only report if we have Serial/Parallel/CMS/G1 events with times data.
                if (jvmRun.getCollectorFamilies() != null && !jvmRun.getCollectorFamilies().isEmpty()
                        && jvmRun.getParallelCount() > 0) {
                    printWriter.write("# Parallel Events: " + jvmRun.getParallelCount() + LINE_SEPARATOR);
                    printWriter
                            .write("# Inverted Parallelism: " + jvmRun.getInvertedParallelismCount() + LINE_SEPARATOR);
                    if (jvmRun.getInvertedParallelismCount() > 0) {
                        printWriter.write("Inverted Parallelism Max: "
                                + jvmRun.getWorstInvertedParallelismEvent().getLogEntry() + LINE_SEPARATOR);
                    }
                }
                // NewRatio
                if (jvmRun.getMaxYoungSpace() != null && jvmRun.getMaxOldSpace() != null
                        && jvmRun.getMaxYoungSpace().getValue(KILOBYTES) > 0) {
                    printWriter.write("NewRatio: " + jvmRun.getNewRatio() + LINE_SEPARATOR);
                }

                if (jvmRun.getMaxHeapSpace().greaterThan(ZERO)) {
                    // Max heap occupancy.
                    if (jvmRun.getMaxHeapOccupancy() != null) {
                        printWriter.write("Heap Occupancy Max: " + jvmRun.getMaxHeapOccupancy().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    } else if (jvmRun.getMaxHeapOccupancyNonBlocking() != null) {
                        printWriter.write("Heap Occupancy Max: "
                                + jvmRun.getMaxHeapOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    }
                    // Max heap after GC.
                    if (jvmRun.getMaxHeapAfterGc() != null) {
                        printWriter.write("Heap After GC Max: " + jvmRun.getMaxHeapAfterGc().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    }
                    // Max heap space.
                    if (jvmRun.getMaxHeapSpace() != null) {
                        printWriter.write(
                                "Heap Space Max: " + jvmRun.getMaxHeapSpace().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    } else if (jvmRun.getMaxHeapSpaceNonBlocking() != null) {
                        printWriter.write("Heap Space Max: " + jvmRun.getMaxHeapSpaceNonBlocking().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    }
                }

                if (jvmRun.getMaxPermSpace().greaterThan(ZERO)) {
                    if (jvmRun.getAnalysis() != null && jvmRun.getAnalysis().contains(INFO_PERM_GEN)) {
                        // Max perm occupancy.
                        printWriter.write("Perm Gen Occupancy Max: " + jvmRun.getMaxPermOccupancy().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                        // Max perm after GC.
                        printWriter.write("Perm Gen After GC Max: " + jvmRun.getMaxPermAfterGc().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                        // Max perm space.
                        printWriter.write("Perm Gen Space Max: " + jvmRun.getMaxPermSpace().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    } else {
                        // Max metaspace occupancy.
                        printWriter.write("Metaspace Occupancy Max: "
                                + jvmRun.getMaxPermOccupancy().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        // Max metaspace after GC.
                        printWriter.write("Metaspace After GC Max: " + jvmRun.getMaxPermAfterGc().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                        // Max metaspace space.
                        printWriter.write("Metaspace Space Max: " + jvmRun.getMaxPermSpace().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    }
                } else if (jvmRun.getMaxPermSpaceNonBlocking().greaterThan(ZERO)) {
                    if (jvmRun.getAnalysis() != null && jvmRun.getAnalysis().contains(INFO_PERM_GEN)) {
                        // Max perm occupancy.
                        printWriter.write("Perm Gen Occupancy Max: "
                                + jvmRun.getMaxPermOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        // Max perm space.
                        printWriter.write("Perm Gen Space Max: "
                                + jvmRun.getMaxPermSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    } else {
                        // Max metaspace occupancy.
                        printWriter.write("Metaspace Occupancy Max: "
                                + jvmRun.getMaxPermOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        // Max metaspace space.
                        printWriter.write("Metaspace Space Max: "
                                + jvmRun.getMaxPermSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    }
                }
                // GC throughput
                printWriter.write("GC Throughput: ");
                if ((jvmRun.getGcThroughput() == 100 || jvmRun.getGcThroughput() == 0)
                        && jvmRun.getBlockingEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    printWriter.write("~");
                }
                printWriter.write(jvmRun.getGcThroughput() + "%" + LINE_SEPARATOR);

                // As of now the allocation rate is only implemented for G1GC collector.
                if (jvmRun.getJvm().getUseG1Gc() != null
                        || jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE)) {
                    BigDecimal allocationRate = jvmRun.getAllocationRate();
                    if (allocationRate.longValue() > 0) {
                        Memory gbPerSec = Memory.memory(allocationRate.longValue(), KILOBYTES);
                        printWriter.write("Allocation Rate: " + Long.toString(gbPerSec.getValue(MEGABYTES)) + " MB/sec"
                                + LINE_SEPARATOR);
                    }
                }

                // GC max pause
                BigDecimal maxGcPause = JdkMath.convertMicrosToSecs(jvmRun.getMaxGcPause());
                printWriter.write("GC Pause Max: ");
                if (maxGcPause.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(maxGcPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
                // GC total pause time
                BigDecimal totalGcPause = JdkMath.convertMicrosToSecs(jvmRun.getGcPauseTotal());
                printWriter.write("GC Pause Total: ");
                if (totalGcPause.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(totalGcPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
            }
            if (jvmRun.getStoppedTimeEventCount() > 0) {
                // Stopped time throughput
                printWriter.write("Stopped Time Throughput: ");
                if (jvmRun.getStoppedTimeThroughput() == 100 && jvmRun.getStoppedTimeEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    printWriter.write("~");
                }
                printWriter.write(jvmRun.getStoppedTimeThroughput() + "%" + LINE_SEPARATOR);
                // Max stopped time
                BigDecimal maxStoppedPause = JdkMath.convertMicrosToSecs(jvmRun.getStoppedTimeMax());
                printWriter.write("Stopped Time Max: " + maxStoppedPause.toString() + " secs" + LINE_SEPARATOR);
                // Total stopped time
                BigDecimal totalStoppedTime = JdkMath.convertMicrosToSecs(jvmRun.getStoppedTimeTotal());
                printWriter.write("Stopped Time Total: " + totalStoppedTime.toString() + " secs" + LINE_SEPARATOR);
                // Ratio of GC vs. stopped time. 100 means all stopped time due to GC.
                if (jvmRun.getBlockingEventCount() > 0) {
                    printWriter.write("GC/Stopped Ratio: " + jvmRun.getGcStoppedRatio() + "%" + LINE_SEPARATOR);
                }
            }

            if (jvmRun.getUnifiedSafepointEventCount() > 0) {
                // Stopped time throughput
                printWriter.write("Safepoint Throughput: ");
                if (jvmRun.getUnifiedSafepointThroughput() == 100 && jvmRun.getUnifiedSafepointEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    printWriter.write("~");
                }
                printWriter.write(jvmRun.getUnifiedSafepointThroughput() + "%" + LINE_SEPARATOR);
                // Max safepoint time
                BigDecimal maxSafepointPause = JdkMath.convertMicrosToSecs(jvmRun.getUnifiedSafepointTimeMax());
                printWriter.write("Safepoint Pause Max: ");
                if (maxSafepointPause.compareTo(BigDecimal.ZERO) == 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(maxSafepointPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
                // Total safepoint time
                BigDecimal totalSafepointTime = JdkMath.convertMicrosToSecs(jvmRun.getUnifiedSafepointTimeTotal());
                printWriter.write("Safepoint Pause Total: " + totalSafepointTime.toString() + " secs" + LINE_SEPARATOR);
                // Ratio of GC vs. safepoint time. 100 means all stopped time due to GC.
                if (jvmRun.getBlockingEventCount() > 0) {
                    printWriter
                            .write("GC/Safepoint Ratio: " + jvmRun.getGcUnifiedSafepointRatio() + "%" + LINE_SEPARATOR);
                }
            }

            // Safepoint summary
            if (jvmRun.getUnifiedSafepointEventCount() > 0) {
                printWriter.write(LINEBREAK_DOUBLE);

                if (jvmRun.getUnifiedSafepointEventCount() > 0) {
                    printWriter.printf("%-30s%10s%12s%7s%12s%n", "SAFEPOINT:", "#", "Time (s)", "", "Max (s)");
                    printWriter.write(LINEBREAK_SINGLE);
                    List<SafepointEventSummary> summaries = jvmRun.getSafepointEventSummaries();
                    Iterator<SafepointEventSummary> iterator = summaries.iterator();
                    while (iterator.hasNext()) {
                        SafepointEventSummary summary = iterator.next();
                        BigDecimal pauseTotal = JdkMath.convertMicrosToSecs(summary.getPauseTotal());
                        String pauseTotalString = null;
                        if (pauseTotal.toString().equals("0.000")) {
                            // give rounding hint
                            pauseTotalString = "~" + pauseTotal.toString();
                        } else {
                            pauseTotalString = pauseTotal.toString();
                        }
                        BigDecimal percent;
                        if (jvmRun.getUnifiedSafepointTimeTotal() > 0) {
                            percent = new BigDecimal(summary.getPauseTotal());
                            percent = percent.divide(new BigDecimal(jvmRun.getUnifiedSafepointTimeTotal()), 2,
                                    RoundingMode.HALF_EVEN);
                            percent = percent.movePointRight(2);
                        } else {
                            percent = new BigDecimal(100);
                        }
                        String percentString = null;
                        if (percent.intValue() == 0) {
                            // give rounding hint
                            percentString = "~" + percent.toString();
                        } else {
                            percentString = percent.toString();
                        }
                        BigDecimal pauseMax = JdkMath.convertMicrosToSecs(summary.getPauseMax());
                        String pauseMaxString = null;
                        if (pauseMax.toString().equals("0.000")) {
                            // give rounding hint
                            pauseMaxString = "~" + pauseMax.toString();
                        } else {
                            pauseMaxString = pauseMax.toString();
                        }
                        printWriter.printf("%-30s%10s%12s%6s%%%12s%n",
                                UnifiedSafepoint.getTriggerLiteral(summary.getTrigger()), summary.getCount(),
                                pauseTotalString, percentString, pauseMaxString);
                    }
                }
            }

            printWriter.write(LINEBREAK_DOUBLE);

            // Analysis
            List<Analysis> analysis = jvmRun.getAnalysis();
            if (!analysis.isEmpty()) {

                // Determine analysis levels
                List<Analysis> error = new ArrayList<Analysis>();
                List<Analysis> warn = new ArrayList<Analysis>();
                List<Analysis> info = new ArrayList<Analysis>();

                for (Analysis a : analysis) {
                    String level = a.getKey().split("\\.")[0];
                    if (level.equals("error")) {
                        error.add(a);
                    } else if (level.equals("warn")) {
                        warn.add(a);
                    } else if (level.equals("info")) {
                        info.add(a);
                    } else {
                        throw new IllegalArgumentException("Unknown analysis level: " + level);
                    }
                }

                printWriter.write("ANALYSIS:" + LINE_SEPARATOR);

                boolean printHeader = true;
                // ERROR
                for (Analysis a : error) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("error" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    printWriter.write(LINE_SEPARATOR);
                }
                // WARN
                printHeader = true;
                for (Analysis a : warn) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("warn" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    printWriter.write(LINE_SEPARATOR);
                }
                // INFO
                printHeader = true;
                for (Analysis a : info) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("info" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    if (INFO_UNACCOUNTED_OPTIONS_DISABLED.equals(a)) {
                        printWriter.write(jvmRun.getJvm().getUnaccountedDisabledOptions());
                        printWriter.write(".");
                    }
                    printWriter.write(LINE_SEPARATOR);
                }
                printWriter.write(LINEBREAK_DOUBLE);
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                printWriter.write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);

                for (String unidentifiedLogLine : unidentifiedLogLines) {
                    printWriter.write(unidentifiedLogLine);
                    printWriter.write(LINE_SEPARATOR);
                }
                printWriter.write(LINEBREAK_DOUBLE);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
