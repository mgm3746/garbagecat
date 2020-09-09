/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util;

import java.math.BigDecimal;

/**
 * Global constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Constants {

    /**
     * The threshold for the time (seconds) for the first log entry for a GC log to be considered complete. First log
     * entries with timestamps below the threshold may indicate a partial GC log or GC events that were not a
     * recognizable format.
     */
    public static final int FIRST_TIMESTAMP_THRESHOLD = 60;

    /**
     * The minimum throughput (percent of time spent not doing garbage collection for a given time interval) to not be
     * flagged a bottleneck.
     */
    public static final int DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD = 90;

    /**
     * The ratio of GC time vs. Stopped time for reporting excessive Stopped time.
     */
    public static final int GC_STOPPED_RATIO_THRESHOLD = 80;

    /**
     * kilobyte
     */
    public static final BigDecimal KILOBYTE = new BigDecimal("1024");

    /**
     * megabyte
     */
    public static final BigDecimal MEGABYTE = new BigDecimal("1048576");

    /**
     * gigabyte
     */
    public static final BigDecimal GIGABYTE = new BigDecimal("1073741824");

    /**
     * Help command line short option.
     */
    public static final String OPTION_HELP_SHORT = "h";

    /**
     * Help command line long option.
     */
    public static final String OPTION_HELP_LONG = "help";

    /**
     * JVM options command line short option.
     */
    public static final String OPTION_JVMOPTIONS_SHORT = "j";

    /**
     * JVM options command line long option.
     */
    public static final String OPTION_JVMOPTIONS_LONG = "jvmoptions";

    /**
     * Preprocess command line short option.
     */
    public static final String OPTION_PREPROCESS_SHORT = "p";

    /**
     * Preprocess command line long option.
     */
    public static final String OPTION_PREPROCESS_LONG = "preprocess";

    /**
     * JVM start datetime command line short option.
     */
    public static final String OPTION_STARTDATETIME_SHORT = "s";

    /**
     * JVM start datetime command line long option.
     */
    public static final String OPTION_STARTDATETIME_LONG = "startdatetime";

    /**
     * Threshold command line short option.
     */
    public static final String OPTION_THRESHOLD_SHORT = "t";

    /**
     * Threshold command line long option.
     */
    public static final String OPTION_THRESHOLD_LONG = "threshold";

    /**
     * Reorder command line short option.
     */
    public static final String OPTION_REORDER_SHORT = "r";

    /**
     * Reorder command line long option.
     */
    public static final String OPTION_REORDER_LONG = "reorder";

    /**
     * Output (name of report file) command line short option.
     */
    public static final String OPTION_OUTPUT_SHORT = "o";

    /**
     * Output (name of report file) command line long option.
     */
    public static final String OPTION_OUTPUT_LONG = "output";

    /**
     * Version command line short option.
     */
    public static final String OPTION_VERSION_SHORT = "v";

    /**
     * Version command line long option.
     */
    public static final String OPTION_VERSION_LONG = "version";

    /**
     * Latest version command line short option.
     */
    public static final String OPTION_LATEST_VERSION_SHORT = "l";

    /**
     * Latest version command line long option.
     */
    public static final String OPTION_LATEST_VERSION_LONG = "latest";

    /**
     * Default output file name.
     */
    public static final String OUTPUT_FILE_NAME = "report.txt";

    /**
     * Analysis property file.
     */
    public static final String ANALYSIS_PROPERTY_FILE = "analysis";

    /**
     * Line separator used for report and preparsing.
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Test data directory.
     */
    public static final String TEST_DATA_DIR = "src" + System.getProperty("file.separator") + "test"
            + System.getProperty("file.separator") + "data" + System.getProperty("file.separator");

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private Constants() {

    }
}
