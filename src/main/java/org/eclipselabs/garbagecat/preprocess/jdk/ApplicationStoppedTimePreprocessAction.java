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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;

/**
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent} preprocessing.
 * </p>
 *
 * <p>
 * Fix issues with APPLICATION_STOPPED_TIME logging.
 * </p>
 *
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Normal (no preprocessing needed):
 * </p>
 *
 * <pre>
 * 2017-02-27T02:56:13.203+0300: 35952.084: Total time for which application threads were stopped: 40.6810160 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2021-10-27T10:52:38.345-0400: 0.181: Total time for which application threads were stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds
 * </pre>
 * 
 * <p>
 * 2) No decorator (e.g. appended to the previous line):
 * </p>
 *
 * <pre>
 * 2021-10-27T10:52:38.344-0400: 0.180: [GC pause (G1 Evacuation Pause) (young) (initial-mark) 4917K-&gt;4561K(7168K), 0.0012213 secs]
 * : Total time for which application threads were stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2021-10-27T10:52:38.344-0400: 0.180: [GC pause (G1 Evacuation Pause) (young) (initial-mark) 4917K-&gt;4561K(7168K), 0.0012213 secs]
 * Total time for which application threads were stopped: 0.0017109 seconds, Stopping threads took: 0.0000136 seconds
 * </pre>
 * 
 * <p>
 * 3) DATESTAMP: DATESTAMP:
 * </p>
 *
 * <pre>
 * 2021-10-28T07:39:54.391-0400: 2021-10-28T07:39:54.391-0400: Total time for which application threads were stopped: 0.0014232 seconds, Stopping threads took: 0.0000111 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2021-10-28T07:39:54.391-0400: Total time for which application threads were stopped: 0.0014232 seconds, Stopping threads took: 0.0000111 seconds
 * </pre>
 * 
 * <p>
 * 4) DATESTAMP: DATESTAMP: TIMESTAMP:
 * </p>
 *
 * <pre>
 * 2021-10-27T10:52:38.345-0400: 2021-10-27T10:52:38.345-04000.181: : Total time for which application threads were stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds
 * 2021-10-27T19:39:02.591-0400: 2021-10-27T19:39:02.591-0400: 0.210: Total time for which application threads were stopped: 0.0007018 seconds, Stopping threads took: 0.0000202 seconds
 * 2022-11-01T22:22:52.436+08002022-11-01T22:22:52.436+0800: : 583259.869: Total time for which application threads were stopped: 0.0590826 seconds, Stopping threads took: 0.0001473 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2021-10-27T19:39:02.591-0400: 0.210: Total time for which application threads were stopped: 0.0007018 seconds, Stopping threads took: 0.0000202 seconds
 * </pre>
 * 
 * <p>
 * 5) DATESTAMP: DATESTAMP: TIMESTAMP: TIMESTAMP:
 * </p>
 *
 * <pre>
 * 2021-10-28T07:41:40.468-0400: 2021-10-28T07:41:40.468-0400: 0.179: 0.179: Total time for which application threads were stopped: 0.0012393 seconds, Stopping threads took: 0.0000233 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2021-10-28T07:41:40.468-0400: 0.179: Total time for which application threads were stopped: 0.0012393 seconds, Stopping threads took: 0.0000233 seconds
 * </pre>
 * 
 * <p>
 * 6) DATESTAMP: TIMESTAMP: DATESTAMP: TIMESTAMP:
 * </p>
 *
 * <pre>
 * 2022-11-01T22:19:41.968+0800: 583069.402: 2022-11-01T22:19:41.968+0800: 583069.402: Total time for which application threads were stopped: 0.1477543 seconds, Stopping threads took: 0.0000903 seconds
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <p>
 * 7) TIMESTAMP:
 * </p>
 *
 * <pre>
 * : 492683.478: Total time for which application threads were stopped: 0.1442017 seconds, Stopping threads took: 0.0001502 seconds
 * </pre>
 *
 * <pre>
 * 492683.478: Total time for which application threads were stopped: 0.1442017 seconds, Stopping threads took: 0.0001502 seconds
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class ApplicationStoppedTimePreprocessAction implements PreprocessAction {

    /**
     * Regular expression DATESTAMP: DATESTAMP:.
     *
     * 2021-10-28T07:39:54.391-0400: 2021-10-28T07:39:54.391-0400: Total time for which application threads were
     * stopped: 0.0014232 seconds, Stopping threads took: 0.0000111 seconds
     */
    private static final String REGEX_DATESTAMP_DATESTAMP = "^" + JdkRegEx.DATESTAMP + ": " + JdkRegEx.DATESTAMP
            + ": (Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DATESTAMP_DATESTAMP_PATTERN = Pattern.compile(REGEX_DATESTAMP_DATESTAMP);

    /**
     * Regular expression DATESTAMP: DATESTAMP: TIMESTAMP:.
     *
     * 2021-10-27T10:52:38.345-0400: 2021-10-27T10:52:38.345-04000.181: : Total time for which application threads were
     * stopped: 0.0013170 seconds, Stopping threads took: 0.0000454 seconds
     */
    private static final String REGEX_DATESTAMP_DATESTAMP_TIMESTAMP = "^" + JdkRegEx.DATESTAMP + "(: )?"
            + JdkRegEx.DATESTAMP + "(: ){0,2}" + JdkRegEx.TIMESTAMP
            + "(: ){0,2}(Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_PATTERN = Pattern
            .compile(REGEX_DATESTAMP_DATESTAMP_TIMESTAMP);

    /**
     * Regular expression DATESTAMP: DATESTAMP: TIMESTAMP: TIMESTAMP:.
     *
     * 2021-10-28T07:41:40.468-0400: 2021-10-28T07:41:40.468-0400: 0.179: 0.179: Total time for which application
     * threads were stopped: 0.0012393 seconds, Stopping threads took: 0.0000233 seconds
     */
    private static final String REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP = "^" + JdkRegEx.DATESTAMP + ": "
            + JdkRegEx.DATESTAMP + ": " + JdkRegEx.TIMESTAMP + "(: )?" + JdkRegEx.TIMESTAMP
            + "(: ){0,2}(Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP_PATTERN = Pattern
            .compile(REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP);

    /**
     * Regular expression DATESTAMP: TIMESTAMP: DATESTAMP:.
     *
     * 2021-10-27T12:32:13.753-0400: 0.250: 2021-10-27T12:32:13.753-0400: Total time for which application threads were
     * stopped: 0.0012571 seconds, Stopping threads took: 0.0000262 seconds
     */
    private static final String REGEX_DATESTAMP_TIMESTAMP_DATESTAMP = "^" + JdkRegEx.DATESTAMP + ": "
            + JdkRegEx.TIMESTAMP + ": " + JdkRegEx.DATESTAMP
            + ": (Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_PATTERN = Pattern
            .compile(REGEX_DATESTAMP_TIMESTAMP_DATESTAMP);

    /**
     * Regular expression DATESTAMP: TIMESTAMP: DATESTAMP: TIMESTAMP:.
     *
     * 2022-11-01T22:19:41.968+0800: 583069.402: 2022-11-01T22:19:41.968+0800: 583069.402: Total time for which
     * application threads were stopped: 0.1477543 seconds, Stopping threads took: 0.0000903 seconds
     */
    private static final String REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_TIMESTAMP = "^" + JdkRegEx.DATESTAMP + ": "
            + JdkRegEx.TIMESTAMP + ": " + JdkRegEx.DATESTAMP + ": " + JdkRegEx.TIMESTAMP
            + ": (Total time for which application threads were stopped: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping "
            + "threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_TIMESTAMP_PATTERN = Pattern
            .compile(REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_TIMESTAMP);

    /**
     * Regular expression missing decorator.
     */
    private static final String REGEX_DECORATOR_MISSING = "^: (Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DECORATOR_MISSING_PATTERN = Pattern.compile(REGEX_DECORATOR_MISSING);

    /**
     * Regular expression for no preprocessing needed
     */
    private static final String REGEX_NO_PREPROCESSING = "^(" + ApplicationStoppedTimeEvent._REGEX + ")$";

    private static final Pattern REGEX_NO_PREPROCESSING_PATTERN = Pattern.compile(REGEX_NO_PREPROCESSING);

    /**
     * Regular expression TIMESTAMP: (with preceding cruft).
     *
     * : 492683.478: Total time for which application threads were stopped: 0.1442017 seconds, Stopping threads took:
     * 0.0001502 seconds
     */
    private static final String REGEX_TIMESTAMP = "^: " + JdkRegEx.TIMESTAMP
            + ": (Total time for which application threads were stopped: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping "
            + "threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_TIMESTAMP_PATTERN = Pattern.compile(REGEX_TIMESTAMP);

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "APPLICATION_STOPPED_TIME_PREPROCESS_ACTION_TOKEN";

    /**
     * @param logLine
     *            The log line to test.
     * @param priorLogEvent
     *            The previous log line event.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, LogEvent priorLogEvent) {
        return REGEX_NO_PREPROCESSING_PATTERN.matcher(logLine).matches()
                || REGEX_DECORATOR_MISSING_PATTERN.matcher(logLine).matches()
                || REGEX_TIMESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param priorLogEvent
     *            The previous log line event.
     * @param logEntry
     *            The current log line.
     * @param nextLogEntry
     *            The next log line.
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @param context
     *            Information to make preprocessing decisions.
     * @param preprocessEvents
     *            Preprocessing events used in later analysis.
     */
    public ApplicationStoppedTimePreprocessAction(LogEvent priorLogEvent, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context, List<PreprocessEvent> preprocessEvents) {

        Matcher matcher;
        if ((matcher = REGEX_NO_PREPROCESSING_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DECORATOR_MISSING_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                // Remove colon
                this.logEntry = matcher.group(1);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(19);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(11) + ": " + matcher.group(21) + ": " + matcher.group(23);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(10) + ": " + matcher.group(20);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(21) + ": " + matcher.group(23);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(10) + ": " + matcher.group(21);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(2);
            }
            context.add(ApplicationStoppedTimePreprocessAction.NEWLINE);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public PreprocessActionType getType() {
        return PreprocessActionType.APPLICATION_STOPPED_TIME;
    }
}
