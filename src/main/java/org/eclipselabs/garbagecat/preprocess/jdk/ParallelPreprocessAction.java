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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ApplicationLoggingEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;

/**
 * <p>
 * PARALLEL preprocessing.
 * </p>
 * 
 * <p>
 * Fix issues with Parallel logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} with "GC time <em>would exceed</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 3743.645: [Full GC [PSYoungGen: 419840K-&gt;415020K(839680K)] [PSOldGen: 5008922K-&gt;5008922K(5033984K)] 5428762K-&gt;5423942K(5873664K) [PSPermGen: 193275K-&gt;193275K(262144K)]      GC time would exceed GCTimeLimit of 98%
 * , 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 3743.645: [Full GC [PSYoungGen: 419840K-&gt;415020K(839680K)] [PSOldGen: 5008922K-&gt;5008922K(5033984K)] 5428762K-&gt;5423942K(5873664K) [PSPermGen: 193275K-&gt;193275K(262144K)], 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]
 * </pre>
 * 
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} with "GC time <em>is exceeding</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 3924.453: [Full GC [PSYoungGen: 419840K-&gt;418436K(839680K)] [PSOldGen: 5008601K-&gt;5008601K(5033984K)] 5428441K-&gt;5427038K(5873664K) [PSPermGen: 193278K-&gt;193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%
 * </pre>
 * 
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent} with "GC time <em>is exceeding</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 52767.809: [Full GC [PSYoungGen: 109294K-&gt;94333K(184960K)] [ParOldGen: 1307971K-&gt;1307412K(1310720K)] 1417265K-&gt;1401746K(1495680K) [PSPermGen: 113654K-&gt;113646K(196608K)]        GC time is exceeding GCTimeLimit of 98%
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ParallelPreprocessAction implements PreprocessAction {

    /**
     * Regular expression class unloading logging.
     */
    private static final String REGEX_BEGINNING_UNLOADING_CLASS = "^(" + JdkRegEx.TIMESTAMP + ": \\[Full GC)"
            + JdkRegEx.UNLOADING_CLASS_BLOCK + "(.*)$";

    private static final Pattern REGEX_BEGINNING_UNLOADING_CLASS_PATTERN = Pattern
            .compile(REGEX_BEGINNING_UNLOADING_CLASS);

    /**
     * Regular expression GCTimeLimit exceeded logging.
     */
    private static final String REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED = "^(" + JdkRegEx.DECORATOR
            + " \\[Full GC \\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\] \\[(PS|Par)OldGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) \\[PSPermGen: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\])(      |\t)(GC time (would exceed|is exceeding) GCTimeLimit of 98%)$";

    private static final Pattern REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED);

    /**
     * Regular expression beginning PARALLEL_COMPACTING_OLD or PARALLEL_SERIAL_OLD with -XX:+PrintAdaptiveSizePolicy
     * logging.
     * 
     * 2021-04-09T07:19:43.692-0400: 74865.313: [Full GC (Ergonomics) AdaptiveSizeStart: 74869.165 collection: 1223
     * 
     */
    private static final String REGEX_RETAIN_BEGINNING_OLD_ADAPTIVE_SIZE_POLICY = "^(" + JdkRegEx.DECORATOR
            + " \\[Full GC \\(Ergonomics\\) )AdaptiveSizeStart: " + JdkRegEx.TIMESTAMP + " collection: \\d{1,}[ ]{0,}$";

    private static final Pattern REGEX_RETAIN_BEGINNING_OLD_ADAPTIVE_SIZE_POLICY_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_OLD_ADAPTIVE_SIZE_POLICY);

    /**
     * Regular expression beginning PARALLEL_SCAVENGE.
     * 
     * 10.392: [GC
     */
    private static final String REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC)$";

    private static final Pattern REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE);

    /**
     * Regular expression beginning PARALLEL_SCAVENGE with -XX:+PrintAdaptiveSizePolicy logging.
     * 
     * 2021-04-09T00:00:27.785-0400: 48509.406: [GC (Allocation Failure) AdaptiveSizePolicy::update_averages: survived:
     * 51216232 promoted: 106256 overflow: false
     */
    private static final String REGEX_RETAIN_BEGINNING_SCAVENGE_ADAPTIVE_SIZE_POLICY = "^(" + JdkRegEx.DECORATOR
            + " \\[GC \\((" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex()
            + ")\\) )AdaptiveSizePolicy::update_averages:  survived: \\d{1,}  promoted: "
            + "\\d{1,}  overflow: (false|true)$";

    private static final Pattern REGEX_RETAIN_BEGINNING_SCAVENGE_ADAPTIVE_SIZE_POLICY_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_SCAVENGE_ADAPTIVE_SIZE_POLICY);

    /**
     * Regular expression for retained end of collection.
     * 
     * [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] 387470K->387085K(1518912K) [PSPermGen:
     * 115215K->115215K(238912K)], 1.5692400 secs]
     * 
     * [PSYoungGen: 970752K->104301K(1456128K)] 970752K->104301K(3708928K), 0.1992940 secs] [Times: user=0.68 sys=0.05,
     * real=0.20 secs]
     * 
     * , 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]
     * 
     * [PSYoungGen: 115174K->0K(5651968K)] [ParOldGen: 5758620K->1232841K(5767168K)] 5873794K->1232841K(11419136K),
     * [Metaspace: 214025K->213385K(1257472K)], 3.8546414 secs] [Times: user=10.71 sys=0.72, real=3.86 secs]
     */
    private static final String REGEX_RETAIN_END = "^(  | )?((\\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\]( \\[(ParOldGen|PSOldGen): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\])? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)[,]{0,1}( \\[(PSPermGen|Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\])?)?, " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?)[ ]*$";

    private static final Pattern REGEX_RETAIN_END_PATTERN = Pattern.compile(REGEX_RETAIN_END);

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            // -XX:+PrintAdaptiveSizePolicy
            "^AdaptiveSize(Start|Stop).+$",
            //
            "^  avg_survived_padded_avg.+$",
            //
            "^Desired (eden|survivor) size.+$",
            //
            "^(PS)?AdaptiveSizePolicy.+$",
            //
            "^\\d{1,} desired_eden_size: \\d{1,}$"
            //
    };

    private static final List<Pattern> THROWAWAY_PATTERN_LIST = new ArrayList<>(REGEX_THROWAWAY.length);

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "PARALLEL_PREPROCESS_ACTION_TOKEN";

    static {
        for (String regex : REGEX_THROWAWAY) {
            THROWAWAY_PATTERN_LIST.add(Pattern.compile(regex));
        }
    }

    /**
     * Determine if the log line is can be thrown away
     * 
     * @return true if the log line matches a throwaway pattern, false otherwise.
     */
    private static final boolean isThrowaway(String logLine) {
        boolean throwaway = false;
        for (int i = 0; i < THROWAWAY_PATTERN_LIST.size(); i++) {
            Pattern pattern = THROWAWAY_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                throwaway = true;
                break;
            }
        }
        return throwaway;
    }

    /**
     * @param logLine
     *            The log line to test.
     * @param priorLogEvent
     *            The previous log line event.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, LogEvent priorLogEvent) {
        boolean match = false;
        if (REGEX_BEGINNING_UNLOADING_CLASS_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_SCAVENGE_ADAPTIVE_SIZE_POLICY_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_OLD_ADAPTIVE_SIZE_POLICY_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_PATTERN.matcher(logLine).matches()) {
            match = true;
        } else if (isThrowaway(logLine)) {
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
    public ParallelPreprocessAction(LogEvent priorLogEvent, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context, List<PreprocessEvent> preprocessEvents) {

        Matcher matcher;
        // Beginning logging
        // (matcher = _PATTERN.matcher(logEntry)).matches()
        if ((matcher = REGEX_BEGINNING_UNLOADING_CLASS_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED_PATTERN.matcher(logEntry)).matches()) {
            // Remove GCTimeLimit output
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
                entangledLogLines.add(matcher.group(53));
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE_PATTERN.matcher(logEntry)).matches()) {
            // Remove beginning PARALLEL_SCAVENGE output
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_SCAVENGE_ADAPTIVE_SIZE_POLICY_PATTERN.matcher(logEntry))
                .matches()) {
            // Remove ending AdaptiveResizePolicy output
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_OLD_ADAPTIVE_SIZE_POLICY_PATTERN.matcher(logEntry)).matches()) {
            // Remove ending AdaptiveResizePolicy output
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_END_PATTERN.matcher(logEntry)).matches()) {
            // End of logging event
            matcher.reset();
            if (matcher.matches()) {
                if (matcher.group(1) != null) {
                    this.logEntry = " " + matcher.group(2);
                } else {
                    this.logEntry = matcher.group(2);
                }
            }
            this.logEntry = PreprocessAction.clearEntangledLines(entangledLogLines, this.logEntry);
            context.remove(PreprocessAction.NEWLINE);
            context.remove(TOKEN);
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
        return PreprocessActionType.PARALLEL;
    }
}
