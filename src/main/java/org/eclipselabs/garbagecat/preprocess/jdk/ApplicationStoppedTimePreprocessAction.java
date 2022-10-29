/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
 * 2) Decorator missing:
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
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class ApplicationStoppedTimePreprocessAction implements PreprocessAction {

    /**
     * Regular expression for no preprocessing needed
     */
    private static final String REGEX_NO_PREPROCESSING = "^(" + ApplicationStoppedTimeEvent.REGEX + ")$";

    private static final Pattern REGEX_NO_PREPROCESSING_PATTERN = Pattern.compile(REGEX_NO_PREPROCESSING);

    /**
     * Regular expression missing decorator.
     */
    private static final String REGEX_DECORATOR_MISSING = "^: (Total time for which application threads were stopped: "
            + "(-)?\\d{1,4}[\\.\\,]\\d{7} seconds, Stopping threads took: (-)?\\d{1,4}[\\.\\,]\\d{7} seconds)[ ]*$";

    private static final Pattern REGEX_DECORATOR_MISSING_PATTERN = Pattern.compile(REGEX_DECORATOR_MISSING);

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
    private static final String REGEX_DATESTAMP_DATESTAMP_TIMESTAMP = "^" + JdkRegEx.DATESTAMP + ": "
            + JdkRegEx.DATESTAMP + "(: )?" + JdkRegEx.TIMESTAMP
            + ": (: )?(Total time for which application threads were stopped: "
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
            + JdkRegEx.DATESTAMP + ": " + JdkRegEx.TIMESTAMP + ": " + JdkRegEx.TIMESTAMP
            + ": (Total time for which application threads were stopped: "
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
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "APPLICATION_STOPPED_TIME_PREPROCESS_ACTION_TOKEN";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     *
     * @param priorLogEntry
     *            The prior log line.
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @param context
     *            Information to make preprocessing decisions.
     */
    public ApplicationStoppedTimePreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {

        Matcher matcher;
        if ((matcher = REGEX_NO_PREPROCESSING_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_DECORATOR_MISSING_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                // Remove colon
                this.logEntry = matcher.group(1);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(19);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(10) + ": " + matcher.group(20) + ": " + matcher.group(22);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(10) + ": " + matcher.group(20);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + ": " + matcher.group(19) + ": " + matcher.group(21);
            }
            context.add(ApplicationStoppedTimePreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        }        
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     *
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @param nextLogLine
     *            The next log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_NO_PREPROCESSING_PATTERN.matcher(logLine).matches()
                || REGEX_DECORATOR_MISSING_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_TIMESTAMP_DATESTAMP_PATTERN.matcher(logLine).matches()
                || REGEX_DATESTAMP_DATESTAMP_TIMESTAMP_TIMESTAMP_PATTERN.matcher(logLine).matches();
    }
}
