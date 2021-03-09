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

import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_LATEST_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_LATEST_VERSION_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERSION_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OUTPUT_FILE_NAME;
import static org.eclipselabs.garbagecat.util.GcUtil.isValidStartDateTime;

import java.io.File;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
public class OptionsParser {

    static Options options;

    static {
        // Declare command line options
        options = new Options();
        options.addOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, false, "help");
        options.addOption(OPTION_VERSION_SHORT, OPTION_VERSION_LONG, false, "version");
        options.addOption(OPTION_LATEST_VERSION_SHORT, OPTION_LATEST_VERSION_LONG, false, "latest version");
        options.addOption(OPTION_JVMOPTIONS_SHORT, OPTION_JVMOPTIONS_LONG, true, "JVM options used during JVM run");
        options.addOption(OPTION_PREPROCESS_SHORT, OPTION_PREPROCESS_LONG, false, "do preprocessing");
        options.addOption(OPTION_STARTDATETIME_SHORT, OPTION_STARTDATETIME_LONG, true,
                "JVM start datetime (yyyy-MM-dd HH:mm:ss.SSS) required for handling datestamp-only logging");
        options.addOption(OPTION_THRESHOLD_SHORT, OPTION_THRESHOLD_LONG, true,
                "threshold (0-100) for throughput bottleneck reporting");
        options.addOption(OPTION_REORDER_SHORT, OPTION_REORDER_LONG, false, "reorder logging by timestamp");
        options.addOption(OPTION_OUTPUT_SHORT, OPTION_OUTPUT_LONG, true,
                "output file name (default " + OUTPUT_FILE_NAME + ")");
    }

    /**
     * Parse command line options.
     * 
     * @return
     */
    public static final CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        // Allow user to just specify help or version.
        if (args.length == 1 && (args[0].equals("-" + OPTION_HELP_SHORT) || args[0].equals("--" + OPTION_HELP_LONG))) {
            return null;
        } else if (args.length == 1
                && (args[0].equals("-" + OPTION_VERSION_SHORT) || args[0].equals("--" + OPTION_VERSION_LONG))) {
            System.out.println("Running garbagecat version: " + getVersion());
        } else if (args.length == 1 && (args[0].equals("-" + OPTION_LATEST_VERSION_SHORT)
                || args[0].equals("--" + OPTION_LATEST_VERSION_LONG))) {
            System.out.println("Latest garbagecat version/tag: " + getLatestVersion());
        } else if (args.length == 2
                && (((args[0].equals("-" + OPTION_VERSION_SHORT) || args[0].equals("--" + OPTION_VERSION_LONG))
                        && (args[1].equals("-" + OPTION_LATEST_VERSION_SHORT)
                                || args[1].equals("--" + OPTION_LATEST_VERSION_LONG)))
                        || ((args[1].equals("-" + OPTION_VERSION_SHORT) || args[1].equals("--" + OPTION_VERSION_LONG))
                                && (args[0].equals("-" + OPTION_LATEST_VERSION_SHORT)
                                        || args[0].equals("--" + OPTION_LATEST_VERSION_LONG))))) {
            System.out.println("Running garbagecat version: " + getVersion());
            System.out.println("Latest garbagecat version/tag: " + getLatestVersion());
        } else {
            CommandLine cmd = parser.parse(options, args);
            validateOptions(cmd);
            return cmd;
        }
        return null;
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
        // Ensure log file specified.
        if (cmd.getArgList().isEmpty()) {
            throw new ParseException("Missing log file");
        }
        if (cmd.getArgList().isEmpty()) {
            throw new ParseException("Missing log file not");
        }
        String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            throw new ParseException("Invalid log file: '" + logFileName + "'");
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

    /**
     * @return version string.
     */
    static String getVersion() {
        return ResourceBundle.getBundle("META-INF/maven/garbagecat/garbagecat/pom").getString("version");
    }

    /**
     * @return version string.
     */
    static String getLatestVersion() {
        String url = "https://github.com/mgm3746/garbagecat/releases/latest";
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "application/json");
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return new JSONObject(json).getString("tag_name");
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return "Unable to retrieve";
        }
    }

}
