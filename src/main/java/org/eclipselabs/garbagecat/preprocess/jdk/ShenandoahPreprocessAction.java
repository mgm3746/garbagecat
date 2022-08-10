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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * Shenandoah JDK8 logging preprocessing.
 * </p>
 *
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent}:
 * </p>
 *
 * <pre>
 * 2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking
 *     Failed to allocate TLAB, 4096K
 *     Cancelling GC: Allocation Failure
 * , 2714.003 ms]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking, 2714.003 ms]
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class ShenandoahPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning //SHENANDOAH_CONCURRENT marking event
     * 
     * 2021-10-27T19:37:39.139-0400: [Concurrent evacuation
     * 
     * 2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking
     * 
     * 2022-08-09T16:05:31.240-0400: [Concurrent reset
     * 
     * 19.373: [Concurrent update references
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT = "^(" + JdkRegEx.DECORATOR
            + " \\[Concurrent (evacuation|marking|reset|update references))$";
    private static final Pattern REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_CONCURRENT);

    /**
     * Regular expression for retained beginning SHENANDOAH_CONCURRENT cleanup event
     * 
     * 2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP = "^(" + JdkRegEx.DECORATOR
            + " \\[Concurrent cleanup " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + UnifiedRegEx.DURATION + "\\])$";
    private static final Pattern REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP);

    /**
     * Regular expression for retained Metaspace block.
     * 
     * , [Metaspace: 6477K->6481K(1056768K)]
     */
    private static final String REGEX_RETAIN_METASPACE = "(, \\[Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\])[ ]*";
    private static final Pattern REGEX_RETAIN_METASPACE_PATTERN = Pattern.compile(REGEX_RETAIN_METASPACE);

    /**
     * Regular expression for retained duration. This can come in the middle or at the end of a logging event split over
     * multiple lines. Check the TOKEN to see if in the middle of preprocessing an event that spans multiple lines.
     * 
     * , 27.5589374 secs]
     */
    private static final String REGEX_RETAIN_DURATION = "(, " + UnifiedRegEx.DURATION + "\\])[ ]*";
    private static final Pattern REGEX_RETAIN_DURATION_PATTERN = Pattern.compile(REGEX_RETAIN_DURATION);

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^" + JdkRegEx.DECORATOR
                    + " (\\[)?Pause (Init|Final) Mark( \\((update refs|unload classes)\\))?( \\(process weakrefs\\))?"
                    + "(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcEvent}
            "^" + JdkRegEx.DECORATOR
                    + " (\\[)?Pause Degenerated GC \\((Evacuation|Mark|Outside of Cycle|Update Refs)\\)"
                    + "(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFullGcEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause Full, start\\]$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^" + JdkRegEx.DECORATOR + " (\\[)?Pause Final Evac(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^" + JdkRegEx.DECORATOR + " (\\[)?Pause (Init|Final) Update Refs(, start\\])?",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^[ ]{1,4}(Bad|Good) progress for (free|used) space: " + JdkRegEx.SIZE + ", need " + JdkRegEx.SIZE + "$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            "^[ ]{1,4}Pacer for Update Refs. Used: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: (inf|\\d{1,}\\.\\d)x",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^[ ]{1,4}Immediate Garbage: " + JdkRegEx.SIZE + " \\(\\d{1,3}% of total\\), \\d{1,4} regions",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^[ ]{1,4}Using \\d of \\d workers for (init|final) (marking|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            "^[ ]{1,4}Pacer for Mark. Expected Live: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: (inf|\\d{1,}\\.\\d)x$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^[ ]{1,4}Adaptive CSet Selection. Target Free: " + JdkRegEx.SIZE + ", Actual Free: " + JdkRegEx.SIZE
                    + ", Max CSet: " + JdkRegEx.SIZE + ", Min Garbage: " + JdkRegEx.SIZE,
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^[ ]{1,4}Pacer for Evacuation. Used CSet: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE
                    + ", Non-Taxable: " + JdkRegEx.SIZE + ", Alloc Tax Rate: \\d{1,}\\.\\dx$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahConcurrentEvent}
            "^[ ]{1,4}Using \\d{1,2} of \\d{1,2} workers for concurrent "
                    + "(reset|marking|preclean|evacuation|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^[ ]{1,4}Collectable Garbage: " + JdkRegEx.SIZE + " \\(\\d{1,3}%( of total)?\\)(, Immediate: "
                    + JdkRegEx.SIZE + " \\(\\d{1,3}%\\))?, (CSet: )?" + JdkRegEx.SIZE
                    + " (\\(\\d{1,3}%\\))?(CSet, \\d{1,3} CSet regions)?",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^[ ]{1,4}Using \\d of \\d workers for (full|stw degenerated) gc$",
            //
            "^[ ]{1,4}Pacer for (Reset|Precleaning). Non-Taxable: " + JdkRegEx.SIZE + "$",
            //
            "^[ ]{0,4}Failed to allocate ((Shared|TLAB), )?" + JdkRegEx.SIZE + "$",
            //
            "^[ ]{0,4}Cancelling GC: (Allocation Failure|Stopping VM|Upgrade To Full GC)$",
            //
            "^Free: " + JdkRegEx.SIZE + " \\(\\d{1,4} regions\\), Max regular: " + JdkRegEx.SIZE + ", Max humongous: "
                    + JdkRegEx.SIZE + ", External frag: \\d{1,3}%, Internal frag: \\d{1,3}%$",
            //
            "^Free headroom: " + JdkRegEx.SIZE + " \\(free\\) - " + JdkRegEx.SIZE + " \\(spike\\) - " + JdkRegEx.SIZE
                    + " \\(penalties\\) = " + JdkRegEx.SIZE + "$",
            //
            "^" + JdkRegEx.DECORATOR
                    + " (\\[)?Concurrent (cleanup|evacuation|marking|precleaning|reset|update references)"
                    + "( \\((process weakrefs|unload classes|update refs)\\))?( \\(process weakrefs\\))?(, start\\])?$"
            //
    };

    private static final List<Pattern> THROWAWAY_PATTERN_LIST = new ArrayList<>(REGEX_THROWAWAY.length);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. Shenandoah, G1).
     * This context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>ShenandoahPreprocessAction</code> to identify concurrent events
     * intermingled with non-concurrent events to store them in the intermingled log lines list for output after the
     * non-concurrent event.
     */
    public static final String TOKEN = "SHENANDOAH_PREPROCESS_ACTION_TOKEN";

    static {
        for (String regex : REGEX_THROWAWAY) {
            THROWAWAY_PATTERN_LIST.add(Pattern.compile(regex));
        }
    }

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
    public ShenandoahPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {
        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_CONCURRENT)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_METASPACE)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_METASPACE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_DURATION)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_DURATION);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            // Sometimes this is the end of a logging event
            if (entangledLogLines != null && !entangledLogLines.isEmpty() && newLoggingEvent(nextLogEntry)) {
                clearEntangledLines(entangledLogLines);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (JdkUtil.parseLogLine(logEntry) instanceof ShenandoahConcurrentEvent && !isThrowaway(logEntry)) {
            // Stand alone event
            // TODO: Instead of throwing away some concurrent events, could save them to output at the end
            this.logEntry = logEntry;
            context.add(TOKEN_BEGINNING_OF_EVENT);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.SHENANDOAH.toString();
    }

    /**
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_METASPACE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_DURATION_PATTERN.matcher(logLine).matches()) {
            match = true;
        } else if (isThrowaway(logLine)) {
            match = true;
        }
        return match;
    }

    /**
     * TODO: Move to superclass.
     * 
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
            // Output any entangled log lines
            for (String logLine : entangledLogLines) {
                this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }

    /**
     * Convenience method to test if a log line is the start of a new logging event or a complete logging event (vs. the
     * middle or end of a multi line logging event).
     * 
     * @param logLine
     *            The log line to test.
     * @return True if the line is the start of a new logging event or a complete logging event.
     */
    private boolean newLoggingEvent(String logLine) {
        return true;
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
}
