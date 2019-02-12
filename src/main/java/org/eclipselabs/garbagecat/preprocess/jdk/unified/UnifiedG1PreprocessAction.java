/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

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
 * G1 unified logging preprocessing.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent}:
 * </p>
 *
 * <pre>
 * [0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
 * [0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation
 * [0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
 * [0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms
 * [0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms
 * [0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms
 * [0.101s][info][gc,heap      ] GC(0) Eden regions: 1-&gt;0(1)
 * [0.101s][info][gc,heap      ] GC(0) Survivor regions: 0-&gt;1(1)
 * [0.101s][info][gc,heap      ] GC(0) Old regions: 0-&gt;0
 * [0.101s][info][gc,heap      ] GC(0) Humongous regions: 0-&gt;0
 * [0.101s][info][gc,metaspace ] GC(0) Metaspace: 4463K-&gt;4463K(1056768K)
 * [0.101s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 0M-&gt;0M(2M) 1.371ms
 * [0.101s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.101s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 0M-&gt;0M(2M) 1.371ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent}:
 * </p>
 *
 * <pre>
 * [16.051s][info][gc,start     ] GC(969) Pause Remark
 * [16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: 5786 processed, 4 removed, symbols: 38663 processed, 11 removed
 * [16.053s][info][gc            ] GC(969) Pause Remark 29M-&gt;29M(46M) 2.328ms
 * [16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [16.053s][info][gc            ] GC(969) Pause Remark 29M-&gt;29M(46M) 2.328ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * *
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent}:
 * </p>
 *
 * <pre>
 * [16.081s][info][gc,start      ] GC(969) Pause Cleanup
 * [16.082s][info][gc            ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms
 * [16.082s][info][gc,cpu        ] GC(969) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [16.082s][info][gc            ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class UnifiedG1PreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning UNIFIED_G1_YOUNG_PAUSE collection.
     * 
     * [0.101s][info][gc ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 0M-&gt;0M(2M) 1.371ms
     * 
     * [16.601s][info][gc ] GC(1032) Pause Young (Concurrent Start) (G1 Evacuation Pause) 38M-&gt;20M(46M) 0.772ms
     */
    private static final String REGEX_RETAIN_PAUSE_YOUNG = "^(\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc           \\] " + JdkRegEx.GC_EVENT_NUMBER
            + " Pause Young \\((Normal|Concurrent Start)\\) \\(G1 Evacuation Pause\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained beginning UNIFIED_G1_CLEANUP collection.
     * 
     * [16.082s][info][gc ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms
     */
    private static final String REGEX_RETAIN_CLEANUP = "^(\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc            \\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Cleanup " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained beginning UNIFIED_REMARK collection.
     * 
     * [16.053s][info][gc ] GC(969) Pause Remark 29M-&gt;29M(46M) 2.328ms
     */
    private static final String REGEX_RETAIN_REMARK = "^(\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc            \\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Remark " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained end times data.
     * 
     * [16.601s][info][gc,cpu ] GC(1032) User=0.00s Sys=0.00s Real=0.00s
     */
    private static final String REGEX_RETAIN_END_TIMES_DATA = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,cpu[ ]{7,8}\\] " + JdkRegEx.GC_EVENT_NUMBER + TimesData.REGEX_JDK9 + "$";

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {

            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start[ ]{5,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(G1 Evacuation Pause\\)$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,task[ ]{6,7}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Using \\d{1,2} workers of \\d{1,2} for (evacuation|marking)$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases[ ]{4,5}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + "   ((Pre Evacuate|Evacuate|Post Evacuate|Other) Collection Set|Other): " + JdkRegEx.DURATION_JDK9
                    + "$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,heap[ ]{6,7}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " (Eden|Survivor|Old|Humongous) regions: \\d{1,3}->\\d{1,3}(\\(\\d{1,3}\\))?$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,metaspace[ ]{1,2}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start[ ]{5,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Remark$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,stringtable\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Cleaned string and symbol table, strings: \\d{1,4} processed, \\d removed, "
                    + "symbols: \\d{1,5} processed, \\d{1,2} removed$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start      \\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Cleanup$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>G1PreprocessAction</code> to identify concurrent events intermingled with
     * non-concurrent events to store them in the intermingled log lines list for output after the non-concurrent event.
     */
    public static final String TOKEN = "UNIFIED_G1_PREPROCESS_ACTION_TOKEN";

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
    public UnifiedG1PreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {

        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_PAUSE_YOUNG)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_PAUSE_YOUNG);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_CLEANUP)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_CLEANUP);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_REMARK)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_REMARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_END_TIMES_DATA)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END_TIMES_DATA);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(2);
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
        return JdkUtil.PreprocessActionType.UNIFIED_G1.toString();
    }

    /**
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_PAUSE_YOUNG)
                || logLine.matches(REGEX_RETAIN_PAUSE_YOUNG) | logLine.matches(REGEX_RETAIN_CLEANUP)
                || logLine.matches(REGEX_RETAIN_END_TIMES_DATA)) {
            match = true;
        } else {
            // TODO: Get rid of this and make them throwaway events?
            for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
                if (logLine.matches(REGEX_THROWAWAY[i])) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    /**
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
}