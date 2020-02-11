/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
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
 * Unified logging preprocessing.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent}:
 * </p>
 *
 * <pre>
 * [0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)
 * [0.112s][info][gc,heap        ] GC(3) DefNew: 1016K-&gt;128K(1152K)
 * [0.112s][info][gc,heap        ] GC(3) Tenured: 929K-&gt;1044K(1552K)
 * [0.112s][info][gc,metaspace   ] GC(3) Metaspace: 1222K-&gt;1222K(1056768K)
 * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M-&gt;1M(2M) 0.700ms
 * [0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M-&gt;1M(2M) 0.700ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 2) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedOldEvent} mixed in @link
 * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent}:
 * </p>
 *
 * <pre>
 * [4.057s][info][gc,start       ] GC(2263) Pause Young (Allocation Failure)
 * [4.057s][info][gc,start       ] GC(2264) Pause Full (Allocation Failure)
 * [4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects
 * [4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms
 * [4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses
 * [4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms
 * [4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers
 * [4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms
 * [4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects
 * [4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms
 * [4.067s][info][gc             ] GC(2264) Pause Full (Allocation Failure) 5M-&gt;5M(8M) 9.355ms
 * [4.067s][info][gc,heap        ] GC(2263) DefNew: 2377K-&gt;0K(2624K)
 * [4.067s][info][gc,heap        ] GC(2263) Tenured: 5622K-&gt;5442K(5632K)
 * [4.067s][info][gc,metaspace   ] GC(2263) Metaspace: 3623K-&gt;3623K(1056768K)
 * [4.067s][info][gc             ] GC(2263) Pause Young (Allocation Failure) 7M-&gt;5M(12M) 9.636ms
 * [4.067s][info][gc,cpu         ] GC(2263) User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [4.067s][info][gc             ] GC(2264) Pause Full (Allocation Failure) 5M-&gt;5M(8M) 9.355ms
 * [4.067s][info][gc             ] GC(2263) Pause Young (Allocation Failure) 7M-&gt;5M(12M) 9.636ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class UnifiedPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning UNIFIED_YOUNG_PAUSE collection.
     * 
     * <pre>
     * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms
     * </pre>
     */
    private static final String REGEX_RETAIN_PAUSE_YOUNG = "^(\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc             \\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Young \\("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained end times data.
     * 
     * <pre>
     * [0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s
     * </pre>
     */
    private static final String REGEX_RETAIN_END_TIMES_DATA = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,cpu         \\] " + JdkRegEx.GC_EVENT_NUMBER + TimesData.REGEX_JDK9 + "$";

    /**
     * Regular expressions for lines thrown away.
     * 
     * <pre>
     * [0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)
     * [0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)
     * [0.112s][info][gc,heap        ] GC(3) Tenured: 929K->1044K(1552K)
     * [0.112s][info][gc,metaspace   ] GC(3) Metaspace: 1222K->1222K(1056768K)
     * </pre>
     */
    private static final String[] REGEX_THROWAWAY = {

            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start[ ]{5,7}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause (Young|Full) \\(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\)$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,(heap|metaspace)[ ]{3,8}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " (DefNew|Tenured|Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
                    + "\\)$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Phase 1: Mark live objects( " + JdkRegEx.DURATION_JDK9 + ")?$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Phase 2: Compute new object addresses( " + JdkRegEx.DURATION_JDK9 + ")?$",
            //

            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Phase 3: Adjust pointers( " + JdkRegEx.DURATION_JDK9 + ")?$",
            //

            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Phase 4: Move objects( " + JdkRegEx.DURATION_JDK9 + ")?$",
            // Discard intermingled UNIFIED_OLD, as the data/time is included in UNIFIED_YOUNG.
            "^(\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc             \\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Full \\(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\) " + JdkRegEx.SIZE + "->"
                    + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$"
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
     * For example, it is used with the <code>UnifiedPreprocessAction</code> to identify concurrent events intermingled
     * with non-concurrent events to store them in the intermingled log lines list for output after the non-concurrent
     * event.
     */
    public static final String TOKEN = "UNIFIED_PREPROCESS_ACTION_TOKEN";

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
    public UnifiedPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
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
        return JdkUtil.PreprocessActionType.UNIFIED.toString();
    }

    /**
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_PAUSE_YOUNG) || logLine.matches(REGEX_RETAIN_END_TIMES_DATA)) {
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