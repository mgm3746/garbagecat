/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_PRINT_GC_DETAILS
 * </p>
 * 
 * <p>
 * Remove G1 collector verbose logging when <code>-XX:+UseG1GC</code> used in combinations with
 * <code>-XX:+PrintGCDetails</code>.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 0.304: [GC pause (young), 0.00376500 secs]
 *    [Parallel Time:   3.6 ms]
 *       [GC Worker Start Time (ms):  304.3  304.4  305.7  305.7]
 *       [Update RS (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *          [Processed Buffers : 0 0 4 2
 *           Sum: 6, Avg: 1, Min: 0, Max: 4]
 *       [Ext Root Scanning (ms):  1.6  2.3  0.6  0.6
 *        Avg:   1.3, Min:   0.6, Max:   2.3]
 *       [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *       [Scan RS (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *       [Object Copy (ms):  1.9  1.3  1.2  1.2
 *        Avg:   1.4, Min:   1.2, Max:   1.9]
 *       [Termination (ms):  0.1  0.0  0.5  0.5
 *        Avg:   0.2, Min:   0.0, Max:   0.5]
 *          [Termination Attempts : 1 1 1 1
 *           Sum: 4, Avg: 1, Min: 1, Max: 1]
 *       [GC Worker End Time (ms):  308.0  308.0  308.0  308.0]
 *       [Other:   0.7 ms]
 *    [Clear CT:   0.0 ms]
 *    [Other:   0.1 ms]
 *       [Choose CSet:   0.0 ms]
 *    [ 8192K->2112K(59M)]
 *  [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 0.304: [GC pause (young), 0.00376500 secs] [ 8192K->2112K(59M)] [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class G1PrintGcDetailsPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning.
     */
    private static final String REGEX_RETAIN_BEGINNING = "^" + JdkRegEx.TIMESTAMP + ": \\[GC pause \\(young\\), "
            + JdkRegEx.DURATION + "\\]$";

    /**
     * Regular expression for retained middle.
     */
    private static final String REGEX_RETAIN_MIDDLE = "^   \\[ " + JdkRegEx.SIZE_JDK7 + "->" + JdkRegEx.SIZE_JDK7
            + "\\(" + JdkRegEx.SIZE_JDK7 + "\\)]$";

    /**
     * Regular expression for retained end.
     */
    private static final String REGEX_RETAIN_END = "^" + JdkRegEx.TIMES_BLOCK + "$";

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            //
            "^   \\[Parallel Time:.+$",
            //
            "^      \\[GC Worker Start Time \\(ms\\):.+$",
            //
            "^      \\[Update RS \\(ms\\):.+$",
            //
            "^       Avg:.+$",
            //
            "^         \\[Processed Buffers :.+$",
            //
            "^          Sum:.+$",
            //
            "      \\[Ext Root Scanning \\(ms\\):.+$",
            //
            "^      \\[Mark Stack Scanning \\(ms\\):.+$",
            //
            "^      \\[Scan RS \\(ms\\):.+$",
            //
            "^      \\[Object Copy \\(ms\\).+$",
            //
            "^      \\[Termination \\(ms\\):.+$",
            //
            "^         \\[Termination Attempts :.+$",
            //
            "^      \\[GC Worker End Time \\(ms\\):.+$",
            //
            "^([ ]{3}|[ ]{6})\\[Other:.+$",
            //
            "^   \\[Clear CT:.+$",
            //
            "^      \\[Choose CSet:.+$" };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     */
    public G1PrintGcDetailsPreprocessAction(String logEntry, String nextLogEntry) {
        if (logEntry.matches(REGEX_RETAIN_BEGINNING) || logEntry.matches(REGEX_RETAIN_MIDDLE)
                || logEntry.matches(REGEX_RETAIN_END)) {
            this.logEntry = logEntry;
        }
        if (logEntry.matches(REGEX_RETAIN_END)) {
            this.logEntry = this.logEntry + System.getProperty("line.separator");
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_BEGINNING) || logLine.matches(REGEX_RETAIN_MIDDLE)
                || logLine.matches(REGEX_RETAIN_END)) {
            match = true;
        } else {
            for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
                if (logLine.matches(REGEX_THROWAWAY[i])) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

}
