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
package org.eclipselabs.garbagecat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

/**
 * <p>
 * Garbage Cat main class. A controller that prepares the model (by parsing GC log entries) and provides analysis (the
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

    private static Options options;

    static {
        // Declare command line options
        options = new Options();
        options.addOption(Constants.OPTION_HELP_SHORT, Constants.OPTION_HELP_LONG, false, "help");
        options.addOption(Constants.OPTION_JVMOPTIONS_SHORT, Constants.OPTION_JVMOPTIONS_LONG, true,
                "JVM options used during JVM run");
        options.addOption(Constants.OPTION_PREPROCESS_SHORT, Constants.OPTION_PREPROCESS_LONG, false,
                "preprocessing flag");
        options.addOption(Constants.OPTION_STARTDATETIME_SHORT, Constants.OPTION_STARTDATETIME_LONG, true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS) for converting GC logging timestamps to datetime");
        options.addOption(Constants.OPTION_THRESHOLD_SHORT, Constants.OPTION_THRESHOLD_LONG, true,
                "threshold (0-100) for throughput bottleneck reporting");
        options.addOption(Constants.OPTION_REORDER_SHORT, Constants.OPTION_REORDER_LONG, false,
                "reorder logging by timestamp");
        options.addOption(Constants.OPTION_OUTPUT_SHORT, Constants.OPTION_OUTPUT_LONG, true,
                "output file name (default " + Constants.OUTPUT_FILE_NAME + ")");
    }

    /**
     * @param args
     *            The argument list includes one or more scope options followed by the name of the gc log file to
     *            inspect.
     */
    public static void main(String[] args) {

        CommandLine cmd = parseOptions(args);

        if (cmd != null) {
            if (cmd.hasOption(Constants.OPTION_HELP_LONG)) {
                usage(options);
            } else {

                // Determine JVM environment information.
                Date jvmStartDate = null;
                if (cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG)) {
                    jvmStartDate = GcUtil.parseStartDateTime(cmd.getOptionValue(Constants.OPTION_STARTDATETIME_SHORT));
                }
                String jvmOptions = null;
                if (cmd.hasOption(Constants.OPTION_JVMOPTIONS_LONG)) {
                    jvmOptions = cmd.getOptionValue(Constants.OPTION_JVMOPTIONS_SHORT);
                }

                String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
                File logFile = new File(logFileName);

                GcManager jvmManager = new GcManager();

                // Do preprocessing
                if (cmd.hasOption(Constants.OPTION_PREPROCESS_LONG)) {
                    /*
                     * Requiring the JVM start date/time for preprocessing is a hack to handle datestamps. When
                     * garbagecat was started there was no <code>-XX:+PrintGCDateStamps</code> option. When it was
                     * introduced in JDK 1.6 update 4, the easiest thing to do to handle datestamps was to preprocess
                     * the datestamps and convert them to timestamps.
                     * 
                     * TODO: Handle datetimes separately from preprocessing so preprocessing doesn't require passing in
                     * the JVM start date/time.
                     */
                    logFile = jvmManager.preprocess(logFile, jvmStartDate);
                }

                // Allow logging to be reordered?
                boolean reorder = false;
                if (cmd.hasOption(Constants.OPTION_REORDER_LONG)) {
                    reorder = true;
                }

                // Store garbage collection logging in data store.
                jvmManager.store(logFile, reorder);

                // Create report
                Jvm jvm = new Jvm(jvmOptions, jvmStartDate);
                // Determine report options
                int throughputThreshold = Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
                if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
                    throughputThreshold = Integer.parseInt(cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT));
                }
                JvmRun jvmRun = jvmManager.getJvmRun(jvm, throughputThreshold);
                String outputFileName;
                if (cmd.hasOption(Constants.OPTION_OUTPUT_LONG)) {
                    outputFileName = cmd.getOptionValue(Constants.OPTION_OUTPUT_SHORT);
                } else {
                    outputFileName = Constants.OUTPUT_FILE_NAME;
                }
                createReport(jvmRun, outputFileName);
            }
        }
    }

    /**
     * Parse command line options.
     * 
     * @return
     */
    private static final CommandLine parseOptions(String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            // Allow user to just specify help.
            if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_HELP_SHORT)
                    || args[0].equals("--" + Constants.OPTION_HELP_LONG))) {
                usage(options);
            } else {
                cmd = parser.parse(options, args);
                validateOptions(cmd);
            }
        } catch (

        ParseException pe) {
            usage(options);
        }
        return cmd;

    }

    /**
     * Output usage help.
     * 
     * @param options
     */
    private static void usage(Options options) {
        // Use the built in formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("garbagecat [OPTION]... [FILE]", options);
    }

    /**
     * Validate command line options.
     * 
     * @param cmd
     *            The command line options.
     */
    public static void validateOptions(CommandLine cmd) {
        // Ensure log file specified.
        if (cmd.getArgList().size() == 0) {
            throw new IllegalArgumentException("Log file not specified.");
        }
        String logFileName = null;
        if (cmd.getArgList().size() > 0) {
            logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
        }
        // Ensure gc log file exists.
        if (logFileName == null) {
            throw new IllegalArgumentException("Log file not specified.");
        }
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            throw new IllegalArgumentException("Log file does not exist.");
        }
        // threshold
        if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
            String thresholdRegEx = "^\\d{1,3}$";
            String thresholdOptionValue = cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT);
            Pattern pattern = Pattern.compile(thresholdRegEx);
            Matcher matcher = pattern.matcher(thresholdOptionValue);
            if (!matcher.find()) {
                throw new IllegalArgumentException(
                        "'" + thresholdOptionValue + "' is not a valid threshold: " + thresholdRegEx);
            }
        }
        // startdatetime
        if (cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG)) {
            String startdatetimeOptionValue = cmd.getOptionValue(Constants.OPTION_STARTDATETIME_SHORT);
            Pattern pattern = Pattern.compile(GcUtil.START_DATE_TIME_REGEX);
            Matcher matcher = pattern.matcher(startdatetimeOptionValue);
            if (!matcher.find()) {
                throw new IllegalArgumentException("'" + startdatetimeOptionValue + "' is not a valid startdatetime: "
                        + GcUtil.START_DATE_TIME_REGEX);
            }
        }
        // JVM options
        if (cmd.hasOption("options")) {
            if (cmd.getOptionValue('o') == null) {
                throw new IllegalArgumentException("JVM options not specified.");
            }
        }
    }

    /**
     * Create Garbage Collection Analysis report.
     * 
     * @param jvmRun
     *            JVM run data.
     * @param reportFileName
     *            Output report file name.
     * 
     */
    public static void createReport(JvmRun jvmRun, String reportFileName) {
        File reportFile = new File(reportFileName);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            // Bottlenecks
            List<String> bottlenecks = jvmRun.getBottlenecks();
            if (bottlenecks.size() > 0) {
                bufferedWriter.write("========================================" + System.getProperty("line.separator"));
                bufferedWriter.write("Throughput less than " + jvmRun.getThroughputThreshold() + "%"
                        + System.getProperty("line.separator"));
                bufferedWriter.write("----------------------------------------" + System.getProperty("line.separator"));
                Iterator<String> iterator = bottlenecks.iterator();
                while (iterator.hasNext()) {
                    bufferedWriter.write(iterator.next() + System.getProperty("line.separator"));
                }
            }

            // JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null
                    || jvmRun.getJvm().getMemory() != null) {
                bufferedWriter.write("========================================" + System.getProperty("line.separator"));
                bufferedWriter.write("JVM:" + System.getProperty("line.separator"));
                bufferedWriter.write("----------------------------------------" + System.getProperty("line.separator"));
                if (jvmRun.getJvm().getVersion() != null) {
                    bufferedWriter
                            .write("Version: " + jvmRun.getJvm().getVersion() + System.getProperty("line.separator"));
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    bufferedWriter
                            .write("Options: " + jvmRun.getJvm().getOptions() + System.getProperty("line.separator"));
                }
                if (jvmRun.getJvm().getMemory() != null) {
                    bufferedWriter
                            .write("Memory: " + jvmRun.getJvm().getMemory() + System.getProperty("line.separator"));
                }
            }

            // Summary
            bufferedWriter.write("========================================" + System.getProperty("line.separator"));
            bufferedWriter.write("SUMMARY:" + System.getProperty("line.separator"));
            bufferedWriter.write("----------------------------------------" + System.getProperty("line.separator"));

            // GC stats
            if (jvmRun.getBlockingEventCount() > 0) {
                bufferedWriter
                        .write("# GC Events: " + jvmRun.getBlockingEventCount() + System.getProperty("line.separator"));
                bufferedWriter.write("Event Types: ");
                List<LogEventType> eventTypes = jvmRun.getEventTypes();
                Iterator<LogEventType> iterator = eventTypes.iterator();
                boolean firstEvent = true;
                while (iterator.hasNext()) {
                    LogEventType eventType = iterator.next();
                    // Don't report header or unknown events
                    if (!eventType.equals(LogEventType.HEADER_COMMAND_LINE_FLAGS)
                            && !eventType.equals(LogEventType.HEADER_MEMORY)
                            && !eventType.equals(LogEventType.HEADER_VERSION)
                            && !eventType.equals(LogEventType.UNKNOWN)) {
                        if (!firstEvent) {
                            bufferedWriter.write(", ");
                        }
                        bufferedWriter.write(eventType.toString());
                        firstEvent = false;
                    }
                }
                bufferedWriter.write(System.getProperty("line.separator"));
                // Max heap space.
                bufferedWriter.write(
                        "Max Heap Space: " + jvmRun.getMaxHeapSpace() + "K" + System.getProperty("line.separator"));
                // Max heap occupancy.
                bufferedWriter.write("Max Heap Occupancy: " + jvmRun.getMaxHeapOccupancy() + "K"
                        + System.getProperty("line.separator"));
                if (jvmRun.getMaxPermSpace() > 0) {
                    // Max perm space.
                    bufferedWriter.write("Max Perm/Metaspace Space: " + jvmRun.getMaxPermSpace() + "K"
                            + System.getProperty("line.separator"));
                    // Max perm occupancy.
                    bufferedWriter.write("Max Perm/Metaspace Occupancy: " + jvmRun.getMaxPermOccupancy() + "K"
                            + System.getProperty("line.separator"));
                }
                // GC throughput
                bufferedWriter.write(
                        "GC Throughput: " + jvmRun.getGcThroughput() + "%" + System.getProperty("line.separator"));
                // GC max pause
                bufferedWriter.write(
                        "GC Max Pause: " + jvmRun.getMaxGcPause() + " ms" + System.getProperty("line.separator"));
                // GC total pause time
                bufferedWriter.write(
                        "GC Total Pause: " + jvmRun.getTotalGcPause() + " ms" + System.getProperty("line.separator"));
                if (jvmRun.getStoppedTimeEventCount() > 0) {
                    // Stopped time events
                    bufferedWriter.write("# Stopped Time Events: " + jvmRun.getStoppedTimeEventCount()
                            + System.getProperty("line.separator"));
                    // Stopped time throughput
                    bufferedWriter.write("Stopped Time Throughput: " + jvmRun.getStoppedTimeThroughput() + "%"
                            + System.getProperty("line.separator"));
                    // Max stopped time
                    bufferedWriter.write("Stopped Time Max Pause: " + jvmRun.getMaxStoppedTime() + " ms"
                            + System.getProperty("line.separator"));
                    // Total stopped time
                    bufferedWriter.write("Stopped Time Total: " + jvmRun.getTotalStoppedTime() + " ms"
                            + System.getProperty("line.separator"));
                    // Ratio of GC vs. stopped time. 100 means all stopped time due to GC.
                    bufferedWriter.write("GC/Stopped Ratio: " + jvmRun.getGcStoppedRatio() + "%"
                            + System.getProperty("line.separator"));
                }
                // First Timestamp
                bufferedWriter.write("First Timestamp: " + jvmRun.getFirstTimestamp() + " ms"
                        + System.getProperty("line.separator"));
                // Last Timestamp
                bufferedWriter.write(
                        "Last Timestamp: " + jvmRun.getLastTimestamp() + " ms" + System.getProperty("line.separator"));
            } else {
                bufferedWriter.write("ERROR: No GC events found." + System.getProperty("line.separator"));
            }
            bufferedWriter.write("========================================" + System.getProperty("line.separator"));

            // Analysis
            List<String> analysisKeys = jvmRun.getAnalysisKeys();
            if (!analysisKeys.isEmpty()) {
                bufferedWriter.write("ANALYSIS:" + System.getProperty("line.separator"));
                bufferedWriter.write("----------------------------------------" + System.getProperty("line.separator"));

                Iterator<String> iterator = analysisKeys.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    bufferedWriter.write("*");
                    bufferedWriter.write(GcUtil.getPropertyValue(Analysis.PROPERTY_FILE, key));
                    bufferedWriter.write(System.getProperty("line.separator"));
                }
                bufferedWriter.write("========================================" + System.getProperty("line.separator"));
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                bufferedWriter.write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):"
                        + System.getProperty("line.separator"));
                bufferedWriter.write("----------------------------------------" + System.getProperty("line.separator"));

                Iterator<String> iterator = unidentifiedLogLines.iterator();
                while (iterator.hasNext()) {
                    String unidentifiedLogLine = iterator.next();
                    bufferedWriter.write(unidentifiedLogLine);
                    bufferedWriter.write(System.getProperty("line.separator"));
                }
                bufferedWriter.write("========================================" + System.getProperty("line.separator"));
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
