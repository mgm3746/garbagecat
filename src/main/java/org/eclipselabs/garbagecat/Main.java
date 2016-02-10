/******************************************************************************
 * Garbage Cat * * Copyright (c) 2008-2010 Red Hat, Inc. * All rights reserved. This program and the accompanying
 * materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this
 * distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: * Red Hat, Inc. -
 * initial API and implementation *
 ******************************************************************************/
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
import org.eclipselabs.garbagecat.domain.Jvm;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

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
        options.addOption("h", "help", false, "help");
        options.addOption("o", "options", true, "JVM options used during JVM run");
        options.addOption("p", "preprocess", false, "preprocessing flag");
        options.addOption("s", "startdatetime", true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS) for converting GC logging timestamps to datetime");
        options.addOption("t", "threshold", true, "threshold (0-100) for throughput bottleneck reporting");
    }

    /**
     * @param args
     *            The argument list includes one or more scope options followed by the name of the gc log file to
     *            inspect.
     */
    public static void main(String[] args) {

        CommandLine cmd = parseOptions(args);

        if (cmd != null) {
            if (cmd.hasOption("help")) {
                usage(options);
            } else {

                // Determine JVM environment information.
                Date jvmStartDate = null;
                if (cmd.hasOption("startdatetime")) {
                    jvmStartDate = GcUtil.parseStartDateTime(cmd.getOptionValue('s'));
                }
                String jvmOptions = null;
                if (cmd.hasOption("options")) {
                    jvmOptions = cmd.getOptionValue('o');
                }

                String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
                File logFile = new File(logFileName);

                GcManager jvmManager = new GcManager();

                // Do preprocessing
                if (cmd.hasOption("preprocess")) {
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

                // Store garbage collection logging in data store.
                jvmManager.store(logFile);

                // Create report
                Jvm jvm = new Jvm(jvmOptions, jvmStartDate);
                // Determine report options
                int throughputThreshold = Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
                if (cmd.hasOption("threshold")) {
                    throughputThreshold = Integer.parseInt(cmd.getOptionValue('t'));
                }
                JvmRun jvmRun = jvmManager.getJvmRun(jvm, throughputThreshold);
                createReport(jvmRun);
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
            if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
                usage(options);
            } else {
                cmd = parser.parse(options, args);
                validateOptions(cmd);
            }
        } catch (ParseException pe) {
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
        if (cmd.hasOption("threshold")) {
            String thresholdRegEx = "^\\d{1,3}$";
            String thresholdOptionValue = cmd.getOptionValue('t');
            Pattern pattern = Pattern.compile(thresholdRegEx);
            Matcher matcher = pattern.matcher(thresholdOptionValue);
            if (!matcher.find()) {
                throw new IllegalArgumentException(
                        "'" + thresholdOptionValue + "' is not a valid threshold: " + thresholdRegEx);
            }
        }
        // startdatetime
        if (cmd.hasOption("startdatetime")) {
            String startdatetimeOptionValue = cmd.getOptionValue('s');
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
     */
    public static void createReport(JvmRun jvmRun) {
        File reportFile = new File("report.txt");
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            
            // Print JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null
                    || jvmRun.getJvm().getMemory() != null) {
                bufferedWriter.write("========================================\n");
                bufferedWriter.write("JVM:\n");
                bufferedWriter.write("========================================\n");
                if (jvmRun.getJvm().getVersion() != null) {
                    bufferedWriter.write("Version: " + jvmRun.getJvm().getVersion() + "\n");
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    bufferedWriter.write("Options: " + jvmRun.getJvm().getOptions() + "\n");
                }
                if (jvmRun.getJvm().getMemory() != null) {
                    bufferedWriter.write("Memory: " + jvmRun.getJvm().getMemory() + "\n");
                }
            }

            // Print bottleneck information
            List<String> bottlenecks = jvmRun.getBottlenecks();
            if (bottlenecks.size() > 0) {
                bufferedWriter.write("========================================\n");
                bufferedWriter.write("Throughput less than " + jvmRun.getThroughputThreshold() + "%\n");
                bufferedWriter.write("========================================\n");
                Iterator<String> iterator = bottlenecks.iterator();
                while (iterator.hasNext()) {
                    bufferedWriter.write(iterator.next() + "\n");
                }
            }

            // Print summary information
            bufferedWriter.write("========================================\n");
            bufferedWriter.write("SUMMARY:\n");
            bufferedWriter.write("========================================\n");
            if (jvmRun.getBlockingEventCount() > 0) {
                bufferedWriter.write("# GC Events: " + jvmRun.getBlockingEventCount() + "\n");
                bufferedWriter.write("Event Types: ");
                List<LogEventType> eventTypes = jvmRun.getEventTypes();
                Iterator<LogEventType> iterator = eventTypes.iterator();
                boolean firstEvent = true;
                while (iterator.hasNext()) {
                    LogEventType eventType = iterator.next();
                    if (!eventType.equals(LogEventType.UNKNOWN)) {
                        if (!firstEvent) {
                            bufferedWriter.write(", ");
                        }
                        bufferedWriter.write(eventType.toString());
                        firstEvent = false;
                    }
                }
                bufferedWriter.write("\n");
                // Max heap space.
                bufferedWriter.write("Max Heap Space: " + jvmRun.getMaxHeapSpace() + "K\n");
                // Max heap occupancy.
                bufferedWriter.write("Max Heap Occupancy: " + jvmRun.getMaxHeapOccupancy() + "K\n");
                if (jvmRun.getMaxPermSpace() > 0) {
                    // Max perm space.
                    bufferedWriter.write("Max Perm/Metaspace Space: " + jvmRun.getMaxPermSpace() + "K\n");
                    // Max perm occupancy.
                    bufferedWriter.write("Max Perm/Metaspace Occupancy: " + jvmRun.getMaxPermOccupancy() + "K\n");
                }
                // GC throughput
                bufferedWriter.write("GC Throughput: " + jvmRun.getGcThroughput() + "%\n");
                // GC max pause
                bufferedWriter.write("GC Max Pause: " + jvmRun.getMaxGcPause() + " ms\n");
                // GC total pause time
                bufferedWriter.write("GC Total Pause: " + jvmRun.getTotalGcPause() + " ms\n");
                if (jvmRun.getStoppedTimeEventCount() > 0) {
                    // Stopped time events
                    bufferedWriter.write("# Stopped Time Events: " + jvmRun.getStoppedTimeEventCount() + "\n");
                    // Stopped time throughput
                    bufferedWriter.write("Stopped Time Throughput: " + jvmRun.getStoppedTimeThroughput() + "%\n");
                    // Max stopped time
                    bufferedWriter.write("Stopped Time Max Pause: " + jvmRun.getMaxStoppedTime() + " ms\n");
                    // Total stopped time
                    bufferedWriter.write("Stopped Time Total: " + jvmRun.getTotalStoppedTime() + " ms\n");
                    // Ratio of GC vs. stopped time. 100 means all stopped time due to GC.
                    bufferedWriter.write("GC/Stopped Ratio: " + jvmRun.getGcStoppedRatio() + "%\n");
                }
                // First Timestamp
                bufferedWriter.write("First Timestamp: " + jvmRun.getFirstTimestamp() + " ms\n");
                // Last Timestamp
                bufferedWriter.write("Last Timestamp: " + jvmRun.getLastTimestamp() + " ms\n");
            } else {
                bufferedWriter.write("ERROR: No GC events found.\n");
            }
            bufferedWriter.write("========================================\n");

            // Print any analysis information
            List<String> analysis = jvmRun.getAnalysis();
            if (!analysis.isEmpty()) {
                bufferedWriter.write("ANALYSIS:\n");
                bufferedWriter.write("========================================\n");

                Iterator<String> iterator = analysis.iterator();
                while (iterator.hasNext()) {
                    String bullet = iterator.next();
                    bufferedWriter.write("*");
                    bufferedWriter.write(bullet);
                    bufferedWriter.write("\n");
                }
                bufferedWriter.write("========================================\n");
            }

            // Print any Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                bufferedWriter.write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):\n");
                bufferedWriter.write("========================================\n");

                Iterator<String> iterator = unidentifiedLogLines.iterator();
                while (iterator.hasNext()) {
                    String unidentifiedLogLine = iterator.next();
                    bufferedWriter.write(unidentifiedLogLine);
                    bufferedWriter.write("\n");
                }
                bufferedWriter.write("========================================\n");
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
