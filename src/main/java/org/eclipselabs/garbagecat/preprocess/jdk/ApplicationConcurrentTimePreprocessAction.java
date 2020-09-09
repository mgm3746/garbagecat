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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * APPLICATION_CONCURRENT_TIME
 * </p>
 * 
 * TODO: Move to CMS preprocessor.
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent} split across 2 lines. Split into
 * separate events. Appears to happen when the JVM is under stress with low throughput. It could be a JVM bug.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <h4>{@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} on second line.</h4>
 * 
 * <pre>
 * 1122748.949Application time: 0.0005210 seconds
 * : [CMS-concurrent-mark-start]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * Application time: 0.0005210 seconds
 * 1122748.949: [CMS-concurrent-mark-start]
 * </pre>
 * 
 * <h4>{@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} on first line.</h4>
 * 
 * <pre>
 * 408365.532: [CMS-concurrent-mark: 0.476/10.257 secs]Application time: 0.0576080 seconds
 *  [Times: user=6.00 sys=0.28, real=10.26 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * Application time: 0.0576080 seconds
 * 408365.532: [CMS-concurrent-mark: 0.476/10.257 secs] [Times: user=6.00 sys=0.28, real=10.26 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ApplicationConcurrentTimePreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the 1st logging line.
     */
    private static final String REGEX_LINE1 = "^(" + JdkRegEx.TIMESTAMP
            + ")(: \\[CMS-concurrent-(abortable-preclean|mark|preclean): " + JdkRegEx.DURATION_FRACTION
            + "\\])?(Application time: \\d{1,4}\\.\\d{7} seconds)$";

    /**
     * Regular expressions defining the 2nd logging line.
     */
    private static final String REGEX_LINE2 = "^(: \\[CMS-concurrent-mark-start\\])[ ]*$";

    /**
     * Regular expression for retained end.
     * 
     * [Times: user=0.15 sys=0.02, real=0.05 secs]
     */
    private static final String REGEX_RETAIN_END = "^" + TimesData.REGEX + "[ ]*$";

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "APPLICATION_CONCURRENT_TIME_PREPROCESS_ACTION_TOKEN";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param context
     *            Information to make preprocessing decisions.
     */
    public ApplicationConcurrentTimePreprocessAction(String logEntry, Set<String> context) {
        if (logEntry.matches(REGEX_LINE1)) {
            Pattern pattern = Pattern.compile(REGEX_LINE1);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                // Split line1 logging apart
                if (matcher.group(6) != null) {
                    this.logEntry = matcher.group(6) + Constants.LINE_SEPARATOR;
                    if (matcher.group(1) != null) {
                        this.logEntry = this.logEntry + matcher.group(1);
                    }
                    if (matcher.group(3) != null) {
                        this.logEntry = this.logEntry + matcher.group(3);
                    }
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_LINE2)) {
            this.logEntry = logEntry + Constants.LINE_SEPARATOR;
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.APPLICATION_CONCURRENT_TIME.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, String priorLogLine) {
        return logLine.matches(REGEX_LINE1) || logLine.matches(REGEX_LINE2) || logLine.matches(REGEX_RETAIN_END);
    }
}
