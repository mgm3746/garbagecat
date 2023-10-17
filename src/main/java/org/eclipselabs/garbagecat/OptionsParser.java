/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REPORT_CONSOLE_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REPORT_CONSOLE_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERBOSE_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERBOSE_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OUTPUT_FILE_NAME;
import static org.eclipselabs.garbagecat.util.GcUtil.isValidStartDateTime;

import java.io.File;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
public class OptionsParser {

    static Options options;

    static {
        // Declare command line options
        options = new Options();
        options.addOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, false, "help");
        options.addOption(OPTION_JVMOPTIONS_SHORT, OPTION_JVMOPTIONS_LONG, true, "JVM options used during JVM run");
        options.addOption(OPTION_PREPROCESS_SHORT, OPTION_PREPROCESS_LONG, false, "do preprocessing");
        options.addOption(OPTION_STARTDATETIME_SHORT, OPTION_STARTDATETIME_LONG, true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss.SSS) to convert uptime to datestamp");
        options.addOption(OPTION_THRESHOLD_SHORT, OPTION_THRESHOLD_LONG, true,
                "threshold (0-100) for throughput bottleneck reporting");
        options.addOption(OPTION_REORDER_SHORT, OPTION_REORDER_LONG, false, "reorder logging by timestamp");
        options.addOption(OPTION_OUTPUT_SHORT, OPTION_OUTPUT_LONG, true,
                "output file name (default " + OUTPUT_FILE_NAME + ")");
        options.addOption(OPTION_REPORT_CONSOLE_SHORT, OPTION_REPORT_CONSOLE_LONG, false,
                "print report to stdout instead of file");
        options.addOption(OPTION_VERBOSE_SHORT, OPTION_VERBOSE_LONG, false, "verbose output");
    }

    /**
     * @return version string.
     */
    static String getVersion() {
        return ResourceBundle.getBundle("META-INF/maven/garbagecat/garbagecat/pom").getString("version");
    }

    /**
     * Parse command line options.
     * 
     * @param args
     *            The command line options.
     * @return <code>CommnandLine</code> from command line options.
     * @throws ParseException
     *             if the command line options are not valid.
     */
    public static final CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        // Allow user to just specify help.
        if (args.length == 1 && (args[0].equals("-" + OPTION_HELP_SHORT) || args[0].equals("--" + OPTION_HELP_LONG))) {
            return null;
        } else {
            CommandLine cmd = parser.parse(options, args);
            validateOptions(cmd);
            return cmd;
        }
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
    private static void validateOptions(CommandLine cmd) throws ParseException {
        // Ensure command line input.
        if (cmd.getArgList().size() == 0) {
            throw new ParseException("Missing input");
        } else {
            // Ensure file input.
            String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
            if (logFileName == null) {
                throw new ParseException("Missing file");
            } else {
                // Ensure file exists.
                File logFile = new File(logFileName);
                if (!logFile.exists()) {
                    throw new ParseException("Invalid file: '" + logFileName + "'");
                }
            }
        }
        // threshold
        if (cmd.hasOption(OPTION_THRESHOLD_LONG)) {
            String thresholdRegEx = "^\\d{1,3}$";
            String thresholdOptionValue = cmd.getOptionValue(OPTION_THRESHOLD_SHORT);
            Pattern pattern = Pattern.compile(thresholdRegEx);
            Matcher matcher = pattern.matcher(thresholdOptionValue);
            if (!matcher.find()) {
                throw new ParseException("Invalid threshold: '" + thresholdOptionValue + "'");
            }
        }
        // startdatetime
        if (cmd.hasOption(OPTION_STARTDATETIME_LONG)) {
            String startdatetimeOptionValue = cmd.getOptionValue(OPTION_STARTDATETIME_SHORT);
            if (!isValidStartDateTime(startdatetimeOptionValue)) {
                throw new ParseException("Invalid startdatetime: '" + startdatetimeOptionValue + "'");
            }
        }
    }

}
