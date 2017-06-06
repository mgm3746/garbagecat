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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
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
        options.addOption(Constants.OPTION_VERSION_SHORT, Constants.OPTION_VERSION_LONG, false, "version");
        options.addOption(Constants.OPTION_JVMOPTIONS_SHORT, Constants.OPTION_JVMOPTIONS_LONG, true,
                "JVM options used during JVM run");
        options.addOption(Constants.OPTION_PREPROCESS_SHORT, Constants.OPTION_PREPROCESS_LONG, false,
                "do preprocessing");
        options.addOption(Constants.OPTION_STARTDATETIME_SHORT, Constants.OPTION_STARTDATETIME_LONG, true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS) required for handling datestamp-only logging");
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

        CommandLine cmd = null;

        try {
            cmd = parseOptions(args);
        } catch (ParseException pe) {
            System.out.println(pe.getMessage());
            usage(options);
        }

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

                GcManager gcManager = new GcManager();

                // Do preprocessing
                if (cmd.hasOption(Constants.OPTION_PREPROCESS_LONG)
                        || cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG)) {
                    /*
                     * Requiring the JVM start date/time for preprocessing is a hack to handle datestamps. When
                     * garbagecat was started there was no <code>-XX:+PrintGCDateStamps</code> option. When it was
                     * introduced in JDK 1.6 update 4, the easiest thing to do to handle datestamps was to preprocess
                     * the datestamps and convert them to timestamps.
                     * 
                     * TODO: Handle datetimes separately from preprocessing so preprocessing doesn't require passing in
                     * the JVM start date/time.
                     */
                    logFile = gcManager.preprocess(logFile, jvmStartDate);
                }

                // Allow logging to be reordered?
                boolean reorder = false;
                if (cmd.hasOption(Constants.OPTION_REORDER_LONG)) {
                    reorder = true;
                }

                // Store garbage collection logging in data store.
                gcManager.store(logFile, reorder);

                // Create report
                Jvm jvm = new Jvm(jvmOptions, jvmStartDate);
                // Determine report options
                int throughputThreshold = Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
                if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
                    throughputThreshold = Integer.parseInt(cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT));
                }
                JvmRun jvmRun = gcManager.getJvmRun(jvm, throughputThreshold);
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
    private static final CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        // Allow user to just specify help or version.
        if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_HELP_SHORT)
                || args[0].equals("--" + Constants.OPTION_HELP_LONG))) {
            usage(options);
        } else if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_VERSION_SHORT)
                || args[0].equals("--" + Constants.OPTION_VERSION_LONG))) {
            System.out.println("garbagecat v" + getVersion());
        } else {
            cmd = parser.parse(options, args);
            validateOptions(cmd);
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
     * 
     * @throws ParseException
     *             Command line options not valid.
     */
    public static void validateOptions(CommandLine cmd) throws ParseException {
        // Ensure log file specified.
        if (cmd.getArgList().size() == 0) {
            throw new ParseException("Missing log file");
        }
        String logFileName = null;
        if (cmd.getArgList().size() > 0) {
            logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
        }
        // Ensure gc log file exists.
        if (logFileName == null) {
            throw new ParseException("Missing log file not");
        }
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            throw new ParseException("Invalid log file: '" + logFileName + "'");
        }
        // threshold
        if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
            String thresholdRegEx = "^\\d{1,3}$";
            String thresholdOptionValue = cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT);
            Pattern pattern = Pattern.compile(thresholdRegEx);
            Matcher matcher = pattern.matcher(thresholdOptionValue);
            if (!matcher.find()) {
                throw new ParseException("Invalid threshold: '" + thresholdOptionValue + "'");
            }
        }
        // startdatetime
        if (cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG)) {
            String startdatetimeOptionValue = cmd.getOptionValue(Constants.OPTION_STARTDATETIME_SHORT);
            Pattern pattern = Pattern.compile(GcUtil.START_DATE_TIME_REGEX);
            Matcher matcher = pattern.matcher(startdatetimeOptionValue);
            if (!matcher.find()) {
                throw new ParseException("Invalid startdatetime: '" + startdatetimeOptionValue + "'");
            }
        }
    }

    /**
     * Create Garbage Collection Analysis report.
     * 
     * TODO: Move to JvmRun to facilitate testing.
     * 
     * @param jvmRun
     *            JVM run data.
     * @param reportFileName
     *            Output report file name.
     * @param reportFileName
     *            Whether or not preparsing is enabled.
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
                bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);
                bufferedWriter.write(
                        "Throughput less than " + jvmRun.getThroughputThreshold() + "%" + Constants.LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                Iterator<String> iterator = bottlenecks.iterator();
                while (iterator.hasNext()) {
                    bufferedWriter.write(iterator.next() + Constants.LINE_SEPARATOR);
                }
            }

            // JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null
                    || jvmRun.getJvm().getMemory() != null) {
                bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);
                bufferedWriter.write("JVM:" + Constants.LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                if (jvmRun.getJvm().getVersion() != null) {
                    bufferedWriter.write("Version: " + jvmRun.getJvm().getVersion() + Constants.LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    bufferedWriter.write("Options: " + jvmRun.getJvm().getOptions() + Constants.LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getMemory() != null) {
                    bufferedWriter.write("Memory: " + jvmRun.getJvm().getMemory() + Constants.LINE_SEPARATOR);
                }
            }

            // Summary
            bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);
            bufferedWriter.write("SUMMARY:" + Constants.LINE_SEPARATOR);
            bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);

            // GC stats
            bufferedWriter.write("# GC Events: " + jvmRun.getBlockingEventCount() + Constants.LINE_SEPARATOR);
            if (jvmRun.getBlockingEventCount() > 0) {
                bufferedWriter.write("Event Types: ");
                List<LogEventType> eventTypes = jvmRun.getEventTypes();
                Iterator<LogEventType> iterator = eventTypes.iterator();
                boolean firstEvent = true;
                while (iterator.hasNext()) {
                    LogEventType eventType = iterator.next();
                    // Only report GC events
                    if (JdkUtil.isReportable(eventType)) {
                        if (!firstEvent) {
                            bufferedWriter.write(", ");
                        }
                        bufferedWriter.write(eventType.toString());
                        firstEvent = false;
                    }
                }
                bufferedWriter.write(Constants.LINE_SEPARATOR);
                // Inverted parallelism. Only report if we have Serial/Parallel/CMS/G1 events.
                if (jvmRun.getCollectorFamilies() != null && jvmRun.getCollectorFamilies().size() > 0) {
                    bufferedWriter.write("# Parallel Events: " + jvmRun.getParallelCount() + Constants.LINE_SEPARATOR);
                    bufferedWriter.write("# Inverted Parallelism: " + jvmRun.getInvertedParallelismCount()
                            + Constants.LINE_SEPARATOR);
                    if (jvmRun.getInvertedParallelismCount() > 0) {
                        bufferedWriter.write("Max Inverted Parallelism: "
                                + jvmRun.getWorstInvertedParallelismEvent().getLogEntry() + Constants.LINE_SEPARATOR);
                    }
                }
                // NewRatio
                if (jvmRun.getMaxYoungSpace() > 0 && jvmRun.getMaxOldSpace() > 0) {
                    bufferedWriter.write("NewRatio: " + jvmRun.getNewRatio() + Constants.LINE_SEPARATOR);
                }
                // Max heap occupancy.
                bufferedWriter
                        .write("Max Heap Occupancy: " + jvmRun.getMaxHeapOccupancy() + "K" + Constants.LINE_SEPARATOR);
                // Max heap space.
                bufferedWriter.write("Max Heap Space: " + jvmRun.getMaxHeapSpace() + "K" + Constants.LINE_SEPARATOR);
                if (jvmRun.getMaxPermSpace() > 0) {
                    // Max perm occupancy.
                    bufferedWriter.write("Max Perm/Metaspace Occupancy: " + jvmRun.getMaxPermOccupancy() + "K"
                            + Constants.LINE_SEPARATOR);
                    // Max perm space.
                    bufferedWriter.write(
                            "Max Perm/Metaspace Space: " + jvmRun.getMaxPermSpace() + "K" + Constants.LINE_SEPARATOR);
                }
                // GC throughput
                bufferedWriter.write("GC Throughput: " + jvmRun.getGcThroughput() + "%" + Constants.LINE_SEPARATOR);
                // GC max pause
                BigDecimal maxGcPause = JdkMath.convertMillisToSecs(jvmRun.getMaxGcPause());
                bufferedWriter.write("GC Max Pause: " + maxGcPause.toString() + " secs" + Constants.LINE_SEPARATOR);
                // GC total pause time
                BigDecimal totalGcPause = JdkMath.convertMillisToSecs(jvmRun.getTotalGcPause());
                bufferedWriter.write("GC Total Pause: " + totalGcPause.toString() + " secs" + Constants.LINE_SEPARATOR);
            }
            if (jvmRun.getStoppedTimeEventCount() > 0) {
                // Stopped time throughput
                bufferedWriter.write("Stopped Time Throughput: " + jvmRun.getStoppedTimeThroughput() + "%"
                        + Constants.LINE_SEPARATOR);
                // Max stopped time
                BigDecimal maxStoppedPause = JdkMath.convertMillisToSecs(jvmRun.getMaxStoppedTime());
                bufferedWriter.write(
                        "Stopped Time Max Pause: " + maxStoppedPause.toString() + " secs" + Constants.LINE_SEPARATOR);
                // Total stopped time
                BigDecimal totalStoppedTime = JdkMath.convertMillisToSecs(jvmRun.getTotalStoppedTime());
                bufferedWriter.write(
                        "Stopped Time Total: " + totalStoppedTime.toString() + " secs" + Constants.LINE_SEPARATOR);
                // Ratio of GC vs. stopped time. 100 means all stopped time due to GC.
                if (jvmRun.getBlockingEventCount() > 0) {
                    bufferedWriter
                            .write("GC/Stopped Ratio: " + jvmRun.getGcStoppedRatio() + "%" + Constants.LINE_SEPARATOR);
                }
            }
            // First/last timestamps
            if (jvmRun.getBlockingEventCount() > 0 || jvmRun.getStoppedTimeEventCount() > 0) {
                // First event
                String firstEventDatestamp = JdkUtil.getDateStamp(jvmRun.getFirstEvent().getLogEntry());
                if (firstEventDatestamp != null) {
                    bufferedWriter.write("First Datestamp: ");
                    bufferedWriter.write(firstEventDatestamp);
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                bufferedWriter.write("First Timestamp: ");
                BigDecimal firstEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getFirstEvent().getTimestamp());
                bufferedWriter.write(firstEventTimestamp.toString());
                bufferedWriter.write(" secs" + Constants.LINE_SEPARATOR);
                // Last event
                String lastEventDatestamp = JdkUtil.getDateStamp(jvmRun.getLastEvent().getLogEntry());
                if (lastEventDatestamp != null) {
                    bufferedWriter.write("Last Datestamp: ");
                    bufferedWriter.write(lastEventDatestamp);
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                bufferedWriter.write("Last Timestamp: ");
                BigDecimal lastEventTimestamp = JdkMath.convertMillisToSecs(jvmRun.getLastEvent().getTimestamp());
                bufferedWriter.write(lastEventTimestamp.toString());
                bufferedWriter.write(" secs" + Constants.LINE_SEPARATOR);
            }

            bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);

            // Analysis
            List<Analysis> analysis = jvmRun.getAnalysis();
            if (!analysis.isEmpty()) {

                // Determine analysis levels
                List<Analysis> error = new ArrayList<Analysis>();
                List<Analysis> warn = new ArrayList<Analysis>();
                List<Analysis> info = new ArrayList<Analysis>();

                Iterator<Analysis> iterator = analysis.iterator();
                while (iterator.hasNext()) {
                    Analysis a = iterator.next();
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

                bufferedWriter.write("ANALYSIS:" + Constants.LINE_SEPARATOR);

                iterator = error.iterator();
                boolean printHeader = true;
                // ERROR
                while (iterator.hasNext()) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("error" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                // WARN
                iterator = warn.iterator();
                printHeader = true;
                while (iterator.hasNext()) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("warn" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                // INFO
                iterator = info.iterator();
                printHeader = true;
                while (iterator.hasNext()) {
                    if (printHeader) {
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("info" + Constants.LINE_SEPARATOR);
                        bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    bufferedWriter.write("*");
                    bufferedWriter.write(a.getValue());
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                bufferedWriter
                        .write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):" + Constants.LINE_SEPARATOR);
                bufferedWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);

                Iterator<String> iterator = unidentifiedLogLines.iterator();
                while (iterator.hasNext()) {
                    String unidentifiedLogLine = iterator.next();
                    bufferedWriter.write(unidentifiedLogLine);
                    bufferedWriter.write(Constants.LINE_SEPARATOR);
                }
                bufferedWriter.write("========================================" + Constants.LINE_SEPARATOR);
            }
        } catch (

        FileNotFoundException e) {
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

    /**
     * @return version string.
     */
    private static String getVersion() {
        ResourceBundle rb = ResourceBundle.getBundle("META-INF/maven/garbagecat/garbagecat/pom");
        return rb.getString("version");
    }

}
