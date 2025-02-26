/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.OptionsParser.options;
import static org.eclipselabs.garbagecat.OptionsParser.parseOptions;
import static org.eclipselabs.garbagecat.util.Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
import static org.eclipselabs.garbagecat.util.Constants.LINE_SEPARATOR;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REPORT_CONSOLE_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERBOSE_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OUTPUT_FILE_NAME;
import static org.eclipselabs.garbagecat.util.GcUtil.parseStartDateTime;
import static org.eclipselabs.garbagecat.util.Memory.ZERO;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.MEGABYTES;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PERM_GEN;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;

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
     * Report double line break
     */
    private static final String LINEBREAK_DOUBLE = "==================================================================="
            + "====" + LINE_SEPARATOR;

    /**
     * Report single line break
     */
    private static final String LINEBREAK_SINGLE = "-------------------------------------------------------------------"
            + "----" + LINE_SEPARATOR;

    /**
     * The maximum number of rejected log lines to track. A throttle to limit memory consumption.
     */
    public static final int REJECT_LIMIT = 1000;

    public static void createReport(CommandLine cmd) throws IOException {
        String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
        File logFile = new File(logFileName);
        String outputFileName = cmd.hasOption(OPTION_OUTPUT_LONG) ? cmd.getOptionValue(OPTION_OUTPUT_SHORT)
                : OUTPUT_FILE_NAME;
        File reportFile = new File(outputFileName);
        if (logFile.equals(reportFile)) {
            throw new IllegalArgumentException("Log file and report are the same file.");
        }

        // Requiring the JVM start date/time for preprocessing was originally a hack to handle datestamps introduced
        // with <code>-XX:+PrintGCDateStamps</code> was introduced in JDK 1.6 update 4. It is now to
        // convert uptime to a datestamp in reporting (e.g. bottlenecks).
        Date jvmStartDate = cmd.hasOption(OPTION_STARTDATETIME_LONG)
                ? parseStartDateTime(cmd.getOptionValue(OPTION_STARTDATETIME_SHORT))
                : null;

        String jvmOptions = cmd.hasOption(OPTION_JVMOPTIONS_LONG) ? cmd.getOptionValue(OPTION_JVMOPTIONS_SHORT) : null;

        URI logFileUri = logFile.toURI();

        boolean verbose = cmd.hasOption(OPTION_VERBOSE_LONG);
        if (verbose) {
            System.out.println("reading gc logging...");
        }
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));

        GcManager gcManager = new GcManager(jvmStartDate);

        // Do preprocessing
        if (cmd.hasOption(OPTION_PREPROCESS_LONG) || cmd.hasOption(OPTION_STARTDATETIME_LONG)) {
            if (verbose) {
                System.out.println("preprocessing...");
            }
            logLines = gcManager.preprocess(logLines);
        }

        // Allow logging to be reordered?
        boolean reorder = cmd.hasOption(OPTION_REORDER_LONG);

        // Store garbage collection logging in data store.
        gcManager.store(logLines, reorder);

        // Create report
        // Determine report options
        int throughputThreshold = cmd.hasOption(OPTION_THRESHOLD_LONG)
                ? Integer.parseInt(cmd.getOptionValue(OPTION_THRESHOLD_SHORT))
                : DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, throughputThreshold);
        boolean reportConsole = cmd.hasOption(OPTION_REPORT_CONSOLE_LONG);
        createReport(jvmRun, reportConsole, reportFile, logFileName);
        if (verbose) {
            System.out.println("done");
        }
    }

    /**
     * Create Garbage Collection Analysis report.
     *
     * @param jvmRun
     *            JVM run data.
     * @param reportConsole
     *            Whether print the report to the console or to a file.
     * @param reportFile
     *            Report file.
     * @param gcLogFileName
     *            The gc log file analyzed.
     */
    public static void createReport(JvmRun jvmRun, boolean reportConsole, File reportFile, String gcLogFileName) {
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            if (reportConsole) {
                printWriter = new PrintWriter(System.out);
            } else {
                printWriter = new PrintWriter(fileWriter);
            }
            File gcLogFile = new File(gcLogFileName);
            printWriter.write(gcLogFile.getName());
            printWriter.write(LINE_SEPARATOR);

            // JVM information
            if (jvmRun.getJvmOptions().getJvmContext().getVersionMajor() > 0
                    || jvmRun.getJvmOptions().getJvmContext().getOptions() != null || jvmRun.getMemory() != null) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write("JVM:" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);
                if (jvmRun.getVmInfo() != null) {
                    printWriter.write("Version: " + jvmRun.getVmInfo() + LINE_SEPARATOR);
                }
                if (jvmRun.getJvmOptions().getJvmContext().getOptions() != null) {
                    printWriter
                            .write("Options: " + jvmRun.getJvmOptions().getJvmContext().getOptions() + LINE_SEPARATOR);
                }
                if (jvmRun.getMemory() != null) {
                    printWriter.write("Memory: " + jvmRun.getMemory() + LINE_SEPARATOR);
                }
            }

            // Summary
            printWriter.write(LINEBREAK_DOUBLE);
            printWriter.write("SUMMARY:" + LINE_SEPARATOR);
            printWriter.write(LINEBREAK_SINGLE);

            // First/last timestamps
            if (jvmRun.getBlockingEventCount() > 0 || jvmRun.getStoppedTimeEventCount() > 0) {
                // First event
                if (jvmRun.getFirstEventDatestamp() != null) {
                    printWriter.write("Datestamp First: ");
                    printWriter.write(jvmRun.getFirstEventDatestamp());
                    printWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getFirstEvent().getLogEntry().matches(UnifiedRegEx.TIME_DECORATOR)) {
                    printWriter.write("Timestamp First: ");
                    BigDecimal firstEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getFirstEvent().getTimestamp());
                    printWriter.write(firstEventTimestamp.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
                // Last event
                if (jvmRun.getLastEventDatestamp() != null) {
                    printWriter.write("Datestamp Last: ");
                    printWriter.write(jvmRun.getLastEventDatestamp());
                    printWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getLastEvent().getLogEntry().matches(UnifiedRegEx.TIME_DECORATOR)) {
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
                List<EventType> eventTypes = jvmRun.getEventTypes();
                boolean firstEvent = true;
                for (EventType eventType : eventTypes) {
                    if (!firstEvent) {
                        printWriter.write(", ");
                    }
                    printWriter.write(eventType.toString());
                    firstEvent = false;
                }
                printWriter.write(LINE_SEPARATOR);
                // Inverted parallelism
                if (!jvmRun.getJvmOptions().getJvmContext().getGarbageCollectors().isEmpty()
                        && jvmRun.getParallelCount() > 0) {
                    printWriter.write("# Parallel Events: " + jvmRun.getParallelCount() + LINE_SEPARATOR);
                    if (jvmRun.getInvertedParallelismCount() > 0) {
                        printWriter.write(
                                "# Inverted Parallelism: " + jvmRun.getInvertedParallelismCount() + LINE_SEPARATOR);
                        printWriter.write("Inverted Parallelism Max: ");
                        if (jvmRun.getStartDate() != null) {
                            printWriter.write(JdkUtil.convertLogEntryTimestampsToDateStamp(
                                    jvmRun.getWorstInvertedParallelismEvent().getLogEntry(), jvmRun.getStartDate()));
                        } else {
                            printWriter.write(jvmRun.getWorstInvertedParallelismEvent().getLogEntry());
                        }
                        printWriter.write(LINE_SEPARATOR);
                    }
                }
                // Inverted serialism
                if (!jvmRun.getJvmOptions().getJvmContext().getGarbageCollectors().isEmpty()
                        && jvmRun.getSerialCount() > 0) {
                    printWriter.write("# Serial Events: " + jvmRun.getSerialCount() + LINE_SEPARATOR);
                    if (jvmRun.getInvertedSerialismCount() > 0) {
                        printWriter
                                .write("# Inverted Serialism: " + jvmRun.getInvertedSerialismCount() + LINE_SEPARATOR);
                        printWriter.write("Inverted Serialism Max: ");
                        if (jvmRun.getStartDate() != null) {
                            printWriter.write(JdkUtil.convertLogEntryTimestampsToDateStamp(
                                    jvmRun.getWorstInvertedSerialismEvent().getLogEntry(), jvmRun.getStartDate()));
                        } else {
                            printWriter.write(jvmRun.getWorstInvertedSerialismEvent().getLogEntry());
                        }
                        printWriter.write(LINE_SEPARATOR);
                    }
                }
                // sys > user
                if (!jvmRun.getJvmOptions().getJvmContext().getGarbageCollectors().isEmpty()
                        && jvmRun.getSysGtUserCount() > 0) {
                    printWriter.write("# sys > user: " + jvmRun.getSysGtUserCount() + LINE_SEPARATOR);
                    printWriter.write("sys > user Max: ");
                    if (jvmRun.getStartDate() != null) {
                        printWriter.write(JdkUtil.convertLogEntryTimestampsToDateStamp(
                                jvmRun.getWorstSysGtUserEvent().getLogEntry(), jvmRun.getStartDate()));
                    } else {
                        printWriter.write(jvmRun.getWorstSysGtUserEvent().getLogEntry());
                    }
                    printWriter.write(LINE_SEPARATOR);
                }
                // NewRatio
                if (jvmRun.getMaxYoungSpace() != null && jvmRun.getMaxOldSpace() != null
                        && jvmRun.getMaxYoungSpace().getValue(KILOBYTES) > 0) {
                    printWriter.write("NewRatio: " + jvmRun.getNewRatio() + LINE_SEPARATOR);
                }

                // Heap stats
                if (jvmRun.getMaxHeap().greaterThan(ZERO)) {
                    printWriter.write(
                            "Heap Used Max: " + jvmRun.getMaxHeapOccupancy().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    printWriter.write(
                            "Heap After GC Max: " + jvmRun.getMaxHeapAfterGc().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    printWriter
                            .write("Heap Allocation Max: " + jvmRun.getMaxHeap().convertTo(KILOBYTES) + LINE_SEPARATOR);
                } else if (jvmRun.getMaxHeapNonBlocking().greaterThan(ZERO)) {
                    printWriter.write("Heap Used Max: " + jvmRun.getMaxHeapOccupancyNonBlocking().convertTo(KILOBYTES)
                            + LINE_SEPARATOR);
                    printWriter.write("Heap After GC Max: " + jvmRun.getMaxHeapAfterGcNonBlocking().convertTo(KILOBYTES)
                            + LINE_SEPARATOR);
                    printWriter.write("Heap Allocation Max: " + jvmRun.getMaxHeapNonBlocking().convertTo(KILOBYTES)
                            + LINE_SEPARATOR);
                }

                // Perm/Metaspace stats
                if (jvmRun.getMaxClassSpace().greaterThan(ZERO)) {
                    if (jvmRun.getAnalysis() != null && jvmRun.hasAnalysis(INFO_PERM_GEN.getKey())) {
                        printWriter.write("Perm Gen Used Max: "
                                + jvmRun.getMaxClassSpaceOccupancy().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Perm Gen After GC Max: "
                                + jvmRun.getMaxClassSpaceAfterGc().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Perm Gen Allocation Max: " + jvmRun.getMaxClassSpace().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    } else {
                        printWriter.write("Metaspace Used Max: "
                                + jvmRun.getMaxClassSpaceOccupancy().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Metaspace After GC Max: "
                                + jvmRun.getMaxClassSpaceAfterGc().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Metaspace Allocation Max: " + jvmRun.getMaxClassSpace().convertTo(KILOBYTES)
                                + LINE_SEPARATOR);
                    }
                } else if (jvmRun.getMaxClassSpaceNonBlocking().greaterThan(ZERO)) {
                    if (jvmRun.getAnalysis() != null && jvmRun.hasAnalysis(INFO_PERM_GEN.getKey())) {
                        printWriter.write("Perm Gen Used Max: "
                                + jvmRun.getMaxClassSpaceOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Perm Gen After GC Max: "
                                + jvmRun.getMaxClassSpaceAfterGcNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Perm Gen Allocation Max: "
                                + jvmRun.getMaxClassSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    } else {
                        printWriter.write("Metaspace Used Max: "
                                + jvmRun.getMaxClassSpaceOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Metaspace After GC Max: "
                                + jvmRun.getMaxClassSpaceAfterGcNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        printWriter.write("Metaspace Allocation Max: "
                                + jvmRun.getMaxClassSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
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
                if (jvmRun.getJvmOptions().getUseG1Gc() != null
                        || jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE)) {
                    BigDecimal allocationRate = jvmRun.getAllocationRate();
                    if (allocationRate.longValue() > 0) {
                        Memory gbPerSec = Memory.memory(allocationRate.longValue(), KILOBYTES);
                        printWriter.write("Allocation Rate: " + Long.toString(gbPerSec.getValue(MEGABYTES)) + " MB/sec"
                                + LINE_SEPARATOR);
                    }
                }

                // GC max pause
                BigDecimal maxGcPause = JdkMath.convertMicrosToSecs(jvmRun.getDurationMax());
                printWriter.write("GC Pause Max: ");
                if (maxGcPause.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(maxGcPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
                // G1 external root scanning max
                if (jvmRun.getExtRootScanningTimeMax() > 0) {
                    BigDecimal extRootScanningMax = JdkMath.convertMicrosToSecs(jvmRun.getExtRootScanningTimeMax());
                    printWriter.write("|--Ext Root Scanning: ");
                    if (extRootScanningMax.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                        // Provide rounding clue
                        printWriter.write("~");
                    }
                    printWriter.write(extRootScanningMax.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
                // GC total pause time
                BigDecimal totalGcPause = JdkMath.convertMicrosToSecs(jvmRun.getDurationTotal());
                printWriter.write("GC Pause Total: ");
                if (totalGcPause.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(totalGcPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
                // G1 external root scanning total
                if (jvmRun.getExtRootScanningTimeTotal() > 0) {
                    BigDecimal extRootScanningTotal = JdkMath.convertMicrosToSecs(jvmRun.getExtRootScanningTimeTotal());
                    printWriter.write("|--Ext Root Scanning: ");
                    if (extRootScanningTotal.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                        // Provide rounding clue
                        printWriter.write("~");
                    }
                    printWriter.write(extRootScanningTotal.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
                // G1 "Other"
                if (jvmRun.getOtherTimeTotal() > 0) {
                    // max
                    BigDecimal otherMax = JdkMath.convertMicrosToSecs(jvmRun.getOtherTimeMax());
                    printWriter.write("Other Max: ");
                    if (otherMax.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                        // Provide rounding clue
                        printWriter.write("~");
                    }
                    printWriter.write(otherMax.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                    // total
                    BigDecimal otherTotal = JdkMath.convertMicrosToSecs(jvmRun.getOtherTimeTotal());
                    printWriter.write("Other Total: ");
                    if (otherTotal.compareTo(BigDecimal.ZERO) == 0 && jvmRun.getBlockingEventCount() > 0) {
                        // Provide rounding clue
                        printWriter.write("~");
                    }
                    printWriter.write(otherTotal.toString());
                    printWriter.write(" secs" + LINE_SEPARATOR);
                }
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
                BigDecimal maxSafepointPause = JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeMax());
                printWriter.write("Safepoint Pause Max: ");
                if (maxSafepointPause.compareTo(BigDecimal.ZERO) == 0) {
                    // Provide rounding clue
                    printWriter.write("~");
                }
                printWriter.write(maxSafepointPause.toString());
                printWriter.write(" secs" + LINE_SEPARATOR);
                // Total safepoint time
                BigDecimal totalSafepointTime = JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeTotal());
                printWriter.write("Safepoint Pause Total: " + totalSafepointTime.toString() + " secs" + LINE_SEPARATOR);
                // Ratio of GC vs. safepoint time. 100 means all stopped time due to GC.
                if (jvmRun.getBlockingEventCount() > 0) {
                    printWriter
                            .write("GC/Safepoint Ratio: " + jvmRun.getGcUnifiedSafepointRatio() + "%" + LINE_SEPARATOR);
                }
                // Safepoint summary
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.printf("%-30s%10s%12s%7s%12s%n", "SAFEPOINT:", "#", "Time (s)", "", "Max (s)");
                printWriter.write(LINEBREAK_SINGLE);
                List<Map.Entry<Trigger, LongSummaryStatistics>> metrics = jvmRun.getSafepointMetrics();
                Iterator<Map.Entry<Trigger, LongSummaryStatistics>> iterator = metrics.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Trigger, LongSummaryStatistics> entry = iterator.next();
                    BigDecimal pauseTotal = JdkMath.convertMicrosToSecs(entry.getValue().getSum());
                    String pauseTotalString = null;
                    if (pauseTotal.toString().equals("0.000")) {
                        // give rounding hint
                        pauseTotalString = "~" + pauseTotal.toString();
                    } else {
                        pauseTotalString = pauseTotal.toString();
                    }
                    BigDecimal percent;
                    if (jvmRun.getUnifiedSafepointTimeTotal() > 0) {
                        percent = new BigDecimal(entry.getValue().getSum());
                        percent = percent.divide(JdkMath.convertNanosToMicros(jvmRun.getUnifiedSafepointTimeTotal()), 2,
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
                    BigDecimal pauseMax = JdkMath.convertMicrosToSecs(entry.getValue().getMax());
                    String pauseMaxString = null;
                    if (pauseMax.toString().equals("0.000")) {
                        // give rounding hint
                        pauseMaxString = "~" + pauseMax.toString();
                    } else {
                        pauseMaxString = pauseMax.toString();
                    }
                    printWriter.printf("%-30s%10s%12s%6s%%%12s%n", UnifiedSafepoint.getTriggerLiteral(entry.getKey()),
                            entry.getValue().getCount(), pauseTotalString, percentString, pauseMaxString);
                }
            }

            // Analysis
            List<String[]> analysis = jvmRun.getAnalysis();
            if (!analysis.isEmpty()) {
                printWriter.write(LINEBREAK_DOUBLE);

                // Determine analysis levels
                List<String[]> error = new ArrayList<String[]>();
                List<String[]> warn = new ArrayList<String[]>();
                List<String[]> info = new ArrayList<String[]>();

                Iterator<String[]> iteratorAnalysis = analysis.iterator();
                while (iteratorAnalysis.hasNext()) {
                    String[] a = iteratorAnalysis.next();
                    String level = a[0].split("\\.")[0];
                    if (level.equals("error")) {
                        error.add(a);
                    } else if (level.equals("warn")) {
                        warn.add(a);
                    } else if (level.equals("info")) {
                        info.add(a);
                    }
                }

                printWriter.write("ANALYSIS:" + LINE_SEPARATOR);

                boolean printHeader = true;
                // ERROR
                for (String[] a : error) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("error" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a[1]);
                    printWriter.write(LINE_SEPARATOR);
                }
                // WARN
                printHeader = true;
                for (String[] a : warn) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("warn" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a[1]);
                    printWriter.write(LINE_SEPARATOR);
                }
                // INFO
                printHeader = true;
                for (String[] a : info) {
                    if (printHeader) {
                        printWriter.write(LINEBREAK_SINGLE);
                        printWriter.write("info" + LINE_SEPARATOR);
                        printWriter.write(LINEBREAK_SINGLE);
                    }
                    printHeader = false;
                    printWriter.write("*");
                    printWriter.write(a[1]);
                    printWriter.write(LINE_SEPARATOR);
                }
            }

            // GC Bottlenecks
            List<String> gcBottlenecks = jvmRun.getGcBottlenecks();
            if (!gcBottlenecks.isEmpty()) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write("GC throughput less than " + jvmRun.getThroughputThreshold() + "%" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);
                for (String gcBottleneck : gcBottlenecks) {
                    if (jvmRun.getStartDate() != null) {
                        printWriter
                                .write(JdkUtil.convertLogEntryTimestampsToDateStamp(gcBottleneck, jvmRun.getStartDate())
                                        + LINE_SEPARATOR);
                    } else {
                        printWriter.write(gcBottleneck + LINE_SEPARATOR);
                    }
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
                    if (jvmRun.getStartDate() != null) {
                        printWriter.write(
                                JdkUtil.convertLogEntryTimestampsToDateStamp(safepointBottleneck, jvmRun.getStartDate())
                                        + LINE_SEPARATOR);
                    } else {
                        printWriter.write(safepointBottleneck + LINE_SEPARATOR);
                    }
                }
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                printWriter.write(LINEBREAK_DOUBLE);
                printWriter.write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):" + LINE_SEPARATOR);
                printWriter.write(LINEBREAK_SINGLE);

                for (String unidentifiedLogLine : unidentifiedLogLines) {
                    printWriter.write(unidentifiedLogLine);
                    printWriter.write(LINE_SEPARATOR);
                }
            }
            printWriter.write(LINEBREAK_DOUBLE);
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

    /**
     * @param args
     *            The argument list includes one or more scope options followed by the name of the gc log file to
     *            inspect.
     * @throws IOException
     *             if gc log file cannot be read.
     */
    public static void main(String... args) throws IOException {
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
     */
    private static void usage() {
        // Use the built in formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("garbagecat [OPTION]... [FILE]", options);
    }

}
