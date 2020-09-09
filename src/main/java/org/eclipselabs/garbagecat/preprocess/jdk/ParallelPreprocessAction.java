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

import java.util.Iterator;
import java.util.List;
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
 * PARALLEL preprocessing.
 * </p>
 * 
 * <p>
 * Fix issues with Parallel logging.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * *
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

    /**
     * Regular expression GCTimeLimit exceeded logging.
     */
    private static final String REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[Full GC \\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\] \\[(PS|Par)OldGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\] " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\) \\[PSPermGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)\\])(      |\t)(GC time (would exceed|is exceeding) GCTimeLimit of 98%)$";

    /**
     * Regular expression beginning PARALLEL_SCAVENGE.
     * 
     * 10.392: [GC
     */
    private static final String REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC)$";

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
     */
    private static final String REGEX_RETAIN_END = "^(  | )?((\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\]( \\[PSOldGen: " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\])? " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)( \\[PSPermGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\])?)?, " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?)[ ]*$";

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     */
    public static final String TOKEN = "PARALLEL_PREPROCESS_ACTION_TOKEN";

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
    public ParallelPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {

        // Beginning logging
        if (logEntry.matches(REGEX_BEGINNING_UNLOADING_CLASS)) {
            Pattern pattern = Pattern.compile(REGEX_BEGINNING_UNLOADING_CLASS);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED)) {
            // Remove GCTimeLimit output
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
                entangledLogLines.add(matcher.group(28));
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE)) {
            // Remove beginning PARALLEL_SCAVENGE output
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                if (matcher.group(1) != null) {
                    this.logEntry = " " + matcher.group(2);
                } else {
                    this.logEntry = matcher.group(2);
                }
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.PARALLEL.toString();
    }

    /**
     * TODO: Move to superclass.
     * 
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && entangledLogLines.size() > 0) {
            // Output any entangled log lines
            Iterator<String> iterator = entangledLogLines.iterator();
            while (iterator.hasNext()) {
                String logLine = iterator.next();
                this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX_BEGINNING_UNLOADING_CLASS)
                || logLine.matches(REGEX_RETAIN_BEGINNING_GC_TIME_LIMIT_EXCEEDED)
                || logLine.matches(REGEX_RETAIN_BEGINNING_PARALLEL_SCAVENGE) || logLine.matches(REGEX_RETAIN_END);
    }
}
