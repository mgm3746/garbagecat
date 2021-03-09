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
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PERM_GEN;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

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
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            File gcLogFile = new File(gcLogFileName);
            bufferedWriter.write(gcLogFile.getName());
            bufferedWriter.write(LINE_SEPARATOR);

            if (version || latestVersion) {
                bufferedWriter.write("========================================" + LINE_SEPARATOR);
                if (version) {
                    bufferedWriter.write(
                            "Running garbagecat version: " + getVersion() + System.getProperty("line.separator"));
                }
                if (latestVersion) {
                    bufferedWriter.write("Latest garbagecat version/tag: " + getLatestVersion()
                            + System.getProperty("line.separator"));
                }
            }

            // Bottlenecks
            List<String> bottlenecks = jvmRun.getBottlenecks();
            if (!bottlenecks.isEmpty()) {
                bufferedWriter.write("========================================" + LINE_SEPARATOR);
                bufferedWriter.write("Throughput less than " + jvmRun.getThroughputThreshold() + "%" + LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                for (String bottleneck : bottlenecks) {
                    bufferedWriter.write(bottleneck + LINE_SEPARATOR);
                }
            }

            // JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null
                    || jvmRun.getJvm().getMemory() != null) {
                bufferedWriter.write("========================================" + LINE_SEPARATOR);
                bufferedWriter.write("JVM:" + LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                if (jvmRun.getJvm().getVersion() != null) {
                    bufferedWriter.write("Version: " + jvmRun.getJvm().getVersion() + LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    bufferedWriter.write("Options: " + jvmRun.getJvm().getOptions() + LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getMemory() != null) {
                    bufferedWriter.write("Memory: " + jvmRun.getJvm().getMemory() + LINE_SEPARATOR);
                }
            }

            // Summary
            bufferedWriter.write("========================================" + LINE_SEPARATOR);
            bufferedWriter.write("SUMMARY:" + LINE_SEPARATOR);
            bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);

            // GC stats
            bufferedWriter.write("# GC Events: " + jvmRun.getBlockingEventCount() + LINE_SEPARATOR);
            if (jvmRun.getBlockingEventCount() > 0) {
                bufferedWriter.write("Event Types: ");
                List<LogEventType> eventTypes = jvmRun.getEventTypes();
                boolean firstEvent = true;
                for (LogEventType eventType : eventTypes) {
                    // Only report GC events
                    if (JdkUtil.isReportable(eventType)) {
                        if (!firstEvent) {
                            bufferedWriter.write(", ");
                        }
                        bufferedWriter.write(eventType.toString());
                        firstEvent = false;
                    }
                }
                bufferedWriter.write(LINE_SEPARATOR);
                // Inverted parallelism. Only report if we have Serial/Parallel/CMS/G1 events with times data.
                if (jvmRun.getCollectorFamilies() != null && !jvmRun.getCollectorFamilies().isEmpty()
                        && jvmRun.getParallelCount() > 0) {
                    bufferedWriter.write("# Parallel Events: " + jvmRun.getParallelCount() + LINE_SEPARATOR);
                    bufferedWriter
                            .write("# Inverted Parallelism: " + jvmRun.getInvertedParallelismCount() + LINE_SEPARATOR);
                    if (jvmRun.getInvertedParallelismCount() > 0) {
                        bufferedWriter.write("Max Inverted Parallelism: "
                                + jvmRun.getWorstInvertedParallelismEvent().getLogEntry() + LINE_SEPARATOR);
                    }
                }
                // NewRatio
                if (jvmRun.getMaxYoungSpace() != null && jvmRun.getMaxOldSpace() != null
                        && jvmRun.getMaxYoungSpace().getValue(KILOBYTES) > 0) {
                    bufferedWriter.write("NewRatio: " + jvmRun.getNewRatio() + LINE_SEPARATOR);
                }
                // Max heap occupancy.
                if (jvmRun.getMaxHeapOccupancy() != null) {
                    bufferedWriter.write("Max Heap Occupancy: " + jvmRun.getMaxHeapOccupancy() + "K" + LINE_SEPARATOR);
                } else if (jvmRun.getMaxHeapOccupancyNonBlocking() != null) {
                    bufferedWriter.write("Max Heap Occupancy: "
                            + jvmRun.getMaxHeapOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                }
                // Max heap after GC.
                if (jvmRun.getMaxHeapAfterGc() != null) {
                    bufferedWriter.write(
                            "Max Heap After GC: " + jvmRun.getMaxHeapAfterGc().convertTo(KILOBYTES) + LINE_SEPARATOR);
                }
                // Max heap space.
                if (jvmRun.getMaxHeapSpace() != null) {
                    bufferedWriter
                            .write("Max Heap Space: " + jvmRun.getMaxHeapSpace().convertTo(KILOBYTES) + LINE_SEPARATOR);
                } else if (jvmRun.getMaxHeapSpaceNonBlocking() != null) {
                    bufferedWriter.write("Max Heap Space: " + jvmRun.getMaxHeapSpaceNonBlocking().convertTo(KILOBYTES)
                            + LINE_SEPARATOR);
                }

                if (jvmRun.getMaxPermSpace() > 0) {
                    if (jvmRun.getAnalysis() != null && jvmRun.getAnalysis().contains(INFO_PERM_GEN)) {
                        // Max perm occupancy.
                        bufferedWriter.write(
                                "Max Perm Gen Occupancy: " + jvmRun.getMaxPermOccupancy() + "K" + LINE_SEPARATOR);
                        // Max perm after GC.
                        bufferedWriter
                                .write("Max Perm Gen After GC: " + jvmRun.getMaxPermAfterGc() + "K" + LINE_SEPARATOR);
                        // Max perm space.
                        bufferedWriter.write("Max Perm Gen Space: " + jvmRun.getMaxPermSpace() + "K" + LINE_SEPARATOR);
                    } else {
                        // Max metaspace occupancy.
                        bufferedWriter.write(
                                "Max Metaspace Occupancy: " + jvmRun.getMaxPermOccupancy() + "K" + LINE_SEPARATOR);
                        // Max metaspace after GC.
                        bufferedWriter
                                .write("Max Metaspace After GC: " + jvmRun.getMaxPermAfterGc() + "K" + LINE_SEPARATOR);
                        // Max metaspace space.
                        bufferedWriter.write("Max Metaspace Space: " + jvmRun.getMaxPermSpace() + "K" + LINE_SEPARATOR);
                    }
                } else if (jvmRun.getMaxPermSpaceNonBlocking().greaterThan(ZERO)) {
                    if (jvmRun.getAnalysis() != null && jvmRun.getAnalysis().contains(INFO_PERM_GEN)) {
                        // Max perm occupancy.
                        bufferedWriter.write("Max Perm Gen Occupancy: "
                                + jvmRun.getMaxPermOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        // Max perm space.
                        bufferedWriter.write("Max Perm Gen Space: "
                                + jvmRun.getMaxPermSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    } else {
                        // Max metaspace occupancy.
                        bufferedWriter.write("Max Metaspace Occupancy: "
                                + jvmRun.getMaxPermOccupancyNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                        // Max metaspace space.
                        bufferedWriter.write("Max Metaspace Space: "
                                + jvmRun.getMaxPermSpaceNonBlocking().convertTo(KILOBYTES) + LINE_SEPARATOR);
                    }
                }
                // GC throughput
                bufferedWriter.write("GC Throughput: ");
                if (jvmRun.getGcThroughput() == 100 && jvmRun.getBlockingEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    bufferedWriter.write("~");
                }
                bufferedWriter.write(jvmRun.getGcThroughput() + "%" + LINE_SEPARATOR);
                // GC max pause
                BigDecimal maxGcPause = JdkMath.convertMillisToSecs(jvmRun.getMaxGcPause());
                bufferedWriter.write("GC Max Pause: " + maxGcPause.toString() + " secs" + LINE_SEPARATOR);
                // GC total pause time
                BigDecimal totalGcPause = JdkMath.convertMillisToSecs(jvmRun.getTotalGcPause());
                bufferedWriter.write("GC Total Pause: " + totalGcPause.toString() + " secs" + LINE_SEPARATOR);
            }
            if (jvmRun.getStoppedTimeEventCount() > 0) {
                // Stopped time throughput
                bufferedWriter.write("Stopped Time Throughput: ");
                if (jvmRun.getStoppedTimeThroughput() == 100 && jvmRun.getStoppedTimeEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    bufferedWriter.write("~");
                }
                bufferedWriter.write(jvmRun.getStoppedTimeThroughput() + "%" + LINE_SEPARATOR);
                // Max stopped time
                BigDecimal maxStoppedPause = JdkMath.convertMillisToSecs(jvmRun.getMaxStoppedTime());
                bufferedWriter
                        .write("Stopped Time Max Pause: " + maxStoppedPause.toString() + " secs" + LINE_SEPARATOR);
                // Total stopped time
                BigDecimal totalStoppedTime = JdkMath.convertMillisToSecs(jvmRun.getTotalStoppedTime());
                bufferedWriter.write("Stopped Time Total: " + totalStoppedTime.toString() + " secs" + LINE_SEPARATOR);
                // Ratio of GC vs. stopped time. 100 means all stopped time due to GC.
                if (jvmRun.getBlockingEventCount() > 0) {
                    bufferedWriter.write("GC/Stopped Ratio: " + jvmRun.getGcStoppedRatio() + "%" + LINE_SEPARATOR);
                }
            }
            // First/last timestamps
            if (jvmRun.getBlockingEventCount() > 0 || jvmRun.getStoppedTimeEventCount() > 0) {
                // First event
                String firstEventDatestamp = JdkUtil.getDateStamp(jvmRun.getFirstEvent().getLogEntry());
                if (firstEventDatestamp != null) {
                    bufferedWriter.write("First Datestamp: ");
                    bufferedWriter.write(firstEventDatestamp);
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getFirstEvent().getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)) {
                    bufferedWriter.write("First Timestamp: ");
                    BigDecimal firstEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getFirstEvent().getTimestamp());
                    bufferedWriter.write(firstEventTimestamp.toString());
                    bufferedWriter.write(" secs" + LINE_SEPARATOR);
                }
                // Last event
                String lastEventDatestamp = JdkUtil.getDateStamp(jvmRun.getLastEvent().getLogEntry());
                if (lastEventDatestamp != null) {
                    bufferedWriter.write("Last Datestamp: ");
                    bufferedWriter.write(lastEventDatestamp);
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                if (!jvmRun.getLastEvent().getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)) {
                    bufferedWriter.write("Last Timestamp: ");
                    BigDecimal lastEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getLastEvent().getTimestamp());
                    bufferedWriter.write(lastEventTimestamp.toString());
                    bufferedWriter.write(" secs" + LINE_SEPARATOR);
                }
            }

            bufferedWriter.write("========================================" + LINE_SEPARATOR);

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

                bufferedWriter.write("ANALYSIS:" + LINE_SEPARATOR);

                boolean printHeader = true;
                // ERROR
                for (Analysis a : error) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                        bufferedWriter.write("error" + LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                    }
                    printHeader = false;
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                // WARN
                printHeader = true;
                for (Analysis a : warn) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                        bufferedWriter.write("warn" + LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                    }
                    printHeader = false;
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                // INFO
                printHeader = true;
                for (Analysis a : info) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                        bufferedWriter.write("info" + LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);
                    }
                    printHeader = false;
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    if (INFO_UNACCOUNTED_OPTIONS_DISABLED.equals(a)) {
                        bufferedWriter.write(jvmRun.getJvm().getUnaccountedDisabledOptions());
                        bufferedWriter.write(".");
                    }
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                bufferedWriter.write("========================================" + LINE_SEPARATOR);
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                bufferedWriter.write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):" + LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + LINE_SEPARATOR);

                for (String unidentifiedLogLine : unidentifiedLogLines) {
                    bufferedWriter.write(unidentifiedLogLine);
                    bufferedWriter.write(LINE_SEPARATOR);
                }
                bufferedWriter.write("========================================" + LINE_SEPARATOR);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
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
