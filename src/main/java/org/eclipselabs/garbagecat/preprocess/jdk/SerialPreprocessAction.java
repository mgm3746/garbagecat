/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.ApplicationLoggingEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;

/**
 * <p>
 * SERIAL preprocessing.
 * </p>
 * 
 * <p>
 * Fix issues with Serial logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * *
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent} with
 * {@link org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent}:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew
 * Desired survivor size 2228224 bytes, new threshold 1 (max 15)
 * - age   1:    3177664 bytes,    3177664 total
 * - age   2:    1278784 bytes,    4456448 total
 * : 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew: 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class SerialPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning of collection.
     * 
     * 10.204: [GC 10.204: [DefNew
     */
    private static final String REGEX_RETAIN_BEGINNING = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
            + ": \\[DefNew)$";

    /**
     * Regular expression for retained end of collection.
     * 
     * : 36825K->4352K(39424K), 0.0224830 secs] 44983K->14441K(126848K), 0.0225800 secs]
     */
    private static final String REGEX_RETAIN_END = "^(: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\])$";

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "SERIAL_PREPROCESS_ACTION_TOKEN";

    /**
     * @param logLine
     *            The log line to test.
     * @param priorLogEvent
     *            The previous log line event.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, LogEvent priorLogEvent) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_BEGINNING) || logLine.matches(REGEX_RETAIN_END)) {
            match = true;
        } else {
            LogEvent event = JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN);
            if (event instanceof ThrowAwayEvent) {
                match = true;
            }
        }
        return match;
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
    public SerialPreprocessAction(LogEvent priorLogEvent, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context, List<PreprocessEvent> preprocessEvents) {

        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            this.logEntry = PreprocessAction.clearEntangledLines(entangledLogLines, this.logEntry);
            context.remove(PreprocessAction.NEWLINE);
        } else {
            LogEvent event = JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN);
            if (event instanceof ApplicationConcurrentTimeEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.APPLICATION_CONCURRENT_TIME)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.APPLICATION_CONCURRENT_TIME);
            } else if (event instanceof ApplicationLoggingEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.APPLICATION_LOGGING)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.APPLICATION_LOGGING);
            } else if (event instanceof ClassHistogramEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.CLASS_HISTOGRAM)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.CLASS_HISTOGRAM);
            } else if (event instanceof ClassUnloadingEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.CLASS_UNLOADING)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.CLASS_UNLOADING);
            } else if (event instanceof HeapAtGcEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.HEAP_AT_GC)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.HEAP_AT_GC);
            } else if (event instanceof TenuringDistributionEvent
                    && !preprocessEvents.contains(PreprocessAction.PreprocessEvent.TENURING_DISTRIBUTION)) {
                preprocessEvents.add(PreprocessAction.PreprocessEvent.TENURING_DISTRIBUTION);
            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public PreprocessActionType getType() {
        return PreprocessActionType.SERIAL;
    }
}
