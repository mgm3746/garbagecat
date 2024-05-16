/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;

/**
 * <p>
 * Shenandoah logging (both non-unified and unified) preprocessing.
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
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent}:
 * </p>
 *
 * <pre>
 * [41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)
 * [41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for init marking
 * [41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, Non-Taxable: 0M, Alloc Tax Rate: 8.5x
 * [41.893s][info][gc           ] GC(1500) Pause Init Mark (update refs) (process weakrefs) 0.295ms
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [41.893s][info][gc           ] GC(1500) Pause Init Mark (update refs) (process weakrefs) 0.295ms
 * </pre>
 * 
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}:
 * </p>
 *
 * <pre>
 * [41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)
 * [41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking
 * [41.911s][info][gc,ergo      ] GC(1500) Adaptive CSet Selection. Target Free: 6M, Actual Free: 14M, Max CSet: 2M, Min Garbage: 0M
 * [41.911s][info][gc,ergo      ] GC(1500) Collectable Garbage: 5M (18% of total), 0M CSet, 21 CSet regions
 * [41.911s][info][gc,ergo      ] GC(1500) Immediate Garbage: 9M (33% of total), 37 regions
 * [41.911s][info][gc,ergo      ] GC(1500) Pacer for Evacuation. Used CSet: 5M, Free: 18M, Non-Taxable: 1M, Alloc Tax Rate: 1.1x
 * [41.911s][info][gc           ] GC(1500) Pause Final Mark (update refs) (process weakrefs) 0.429ms
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [41.911s][info][gc           ] GC(1500) Pause Final Mark (update refs) (process weakrefs) 0.429ms
 * </pre>
 *
 * <p>
 * 4) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent}:
 * </p>
 *
 * <pre>
 * [41.912s][info][gc,start     ] GC(1500) Pause Final Evac
 * [41.912s][info][gc           ] GC(1500) Pause Final Evac 0.022ms
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [41.912s][info][gc           ] GC(1500) Pause Final Evac 0.022ms
 * </pre>
 * 
 * <p>
 * 5) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent}:
 * </p>
 *
 * <pre>
 * [69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs
 * [69.612s][info][gc,ergo      ] GC(2582) Pacer for Update Refs. Used: 49M, Free: 11M, Non-Taxable: 1M, Alloc Tax Rate: 5.4x
 * [69.612s][info][gc           ] GC(2582) Pause Init Update Refs 0.036ms
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [69.612s][info][gc           ] GC(2582) Pause Init Update Refs 0.036ms
 * </pre>
 * 
 * <p>
 * 6) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent}:
 * </p>
 *
 * <pre>
 * [69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs
 * [69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update
 * [69.644s][info][gc           ] GC(2582) Pause Final Update Refs 0.302ms
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [69.644s][info][gc           ] GC(2582) Pause Final Update Refs 0.302ms
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class ShenandoahPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning of SHENANDOAH_CONCURRENT
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
     * Regular expression for retained beginning event.
     * 
     * {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent} cleanup:
     * 
     * 2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]
     * 
     * 2022-10-28T10:58:59.285-0400: [Concurrent marking, 0.506 ms]
     * 
     * {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent}:
     * 
     * 2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms]
     */
    private static final String REGEX_RETAIN_BEGINNING_EVENT = "^(" + JdkRegEx.DECORATOR
            + " \\[(Concurrent cleanup|Pause Full) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION_MS + "\\])$";

    private static final Pattern REGEX_RETAIN_BEGINNING_EVENT_PATTERN = Pattern.compile(REGEX_RETAIN_BEGINNING_EVENT);

    /**
     * Regular expression for retained duration. This can come in the middle or at the end of a logging event split over
     * multiple lines. Check the TOKEN to see if in the middle of preprocessing an event that spans multiple lines.
     * 
     * , 27.5589374 secs]
     */
    private static final String REGEX_RETAIN_DURATION = "(, " + JdkRegEx.DURATION_MS + "\\])[ ]*";

    private static final Pattern REGEX_RETAIN_DURATION_PATTERN = Pattern.compile(REGEX_RETAIN_DURATION);

    /**
     * Regular expression for retained ending Metaspace block.
     * 
     * , [Metaspace: 6477K->6481K(1056768K)]
     */
    private static final String REGEX_RETAIN_END_METASPACE = "(, \\[Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\])[ ]*";

    private static final Pattern REGEX_RETAIN_END_METASPACE_PATTERN = Pattern.compile(REGEX_RETAIN_END_METASPACE);

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause Final Mark( \\((process weakrefs|unload classes)\\))?, start\\]$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause Init Mark( \\((process weakrefs|unload classes)\\))?, start\\]$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause Degenerated GC \\((Evacuation|Mark|Update Refs)\\), start\\]$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcEvent}
            "^    (Bad|Good) progress for (free|used) space: " + JdkRegEx.SIZE + ", need " + JdkRegEx.SIZE + "$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcMarkEvent}
            "^    Using \\d of \\d workers for (full|stw degenerated) gc$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent}
            "^    Pacer for Update Refs. Used: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: (inf|\\d{1,}\\.\\d)x",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent}
            "^    Pacer for Evacuation. Used CSet: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: (\\d{1,}\\.\\d|inf)x$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent}
            "^    Pacer for Mark. Expected Live: " + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: (inf|\\d{1,}\\.\\d)x$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent}
            "^" + JdkRegEx.DECORATOR + " (\\[)?Pause Final Evac(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause Full, start\\]$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}
            "^    Collectable Garbage: " + JdkRegEx.SIZE + " \\(\\d{1,3}%( of total)?\\)(, Immediate: " + JdkRegEx.SIZE
                    + " \\(\\d{1,3}%\\))?, (CSet: )?" + JdkRegEx.SIZE
                    + " (\\(\\d{1,3}%\\))?(CSet, \\d{1,} CSet regions)?",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent}
            "^    Using \\d of \\d workers for (init|final) (marking|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}
            "^    Adaptive CSet Selection. Target Free: " + JdkRegEx.SIZE + ", Actual Free: " + JdkRegEx.SIZE
                    + ", Max CSet: " + JdkRegEx.SIZE + ", Min Garbage: " + JdkRegEx.SIZE,
            //
            "^Free: " + JdkRegEx.SIZE + " \\(\\d{1,4} regions\\), Max regular: " + JdkRegEx.SIZE + ", Max humongous: "
                    + JdkRegEx.SIZE + ", External frag: \\d{1,3}%, Internal frag: \\d{1,3}%$",
            "^    Using \\d{1,2} of \\d{1,2} workers for concurrent "
                    + "(reset|marking|preclean|evacuation|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent}
            "^" + JdkRegEx.DECORATOR + " \\[Pause (Init|Final) Update Refs, start\\]$",
            //
            "^    Cancelling GC: (Allocation Failure|Stopping VM|Upgrade To Full GC)$",
            //
            "^    Failed to allocate ((Shared|TLAB), )?" + JdkRegEx.SIZE + "$",
            //
            "^Free headroom: " + JdkRegEx.SIZE + " \\(free\\) - " + JdkRegEx.SIZE + " \\(spike\\) - " + JdkRegEx.SIZE
                    + " \\(penalties\\) = " + JdkRegEx.SIZE + "$",
            //
            "^    Pacer for (Reset|Precleaning). Non-Taxable: " + JdkRegEx.SIZE + "$",
            //
            "^" + JdkRegEx.DECORATOR + " \\[Concurrent (cleanup|evacuation|marking|marking \\(process weakrefs\\)|"
                    + "marking \\((update refs|unload classes)\\)|marking \\(update refs\\) \\(process weakrefs\\)|"
                    + "precleaning|reset|uncommit|update references), start\\]$" };

    private static final List<Pattern> THROWAWAY_PATTERN_LIST = new ArrayList<>(REGEX_THROWAWAY.length);

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. Shenandoah, G1).
     * This context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>ShenandoahPreprocessAction</code> to identify concurrent events
     * intermingled with non-concurrent events to store them in the intermingled log lines list for output after the
     * non-concurrent event.
     */
    public static final String TOKEN = "SHENANDOAH_PREPROCESS_ACTION_TOKEN";

    /**
     * Indicates the current log entry is either the beginning of a @link
     * org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent} or @link
     * org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent} that spans multiple logging lines, or it is a single
     * line logging event.
     */
    private static final String TOKEN_BEGINNING_SHENANDOAH = "TOKEN_BEGINNING_OF_SHENANDOAH";

    /**
     * Indicates the current log entry is either the beginning of a @link
     * org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent} that spans multiple logging lines, or it is a
     * single line logging event.
     */
    private static final String TOKEN_BEGINNING_SHENANDOAH_CONCURRENT = "TOKEN_BEGINNING_OF_SHENANDOAH_CONCURRENT";

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
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_EVENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_METASPACE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_DURATION_PATTERN.matcher(logLine).matches()
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahConcurrentEvent
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahDegeneratedGcEvent
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitUpdateEvent
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitMarkEvent
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalMarkEvent
                || JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalUpdateEvent) {
            match = true;
        } else if (isThrowaway(logLine)) {
            match = true;
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

        Matcher matcher;

        // Beginning logging
        if ((matcher = REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
                context.add(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_EVENT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
                context.add(TOKEN_BEGINNING_SHENANDOAH);
            }
            context.add(PreprocessAction.NEWLINE);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_END_METASPACE_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                // throw away unrelated metaspace lines
                if (context.contains(TOKEN_BEGINNING_SHENANDOAH)
                        || context.contains(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT)) {
                    this.logEntry = logEntry;
                    context.remove(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
                }
            }
            context.remove(PreprocessAction.NEWLINE);
        } else if ((matcher = REGEX_RETAIN_DURATION_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            // Sometimes this is the end of a logging event
            if (entangledLogLines != null && !entangledLogLines.isEmpty() && newLoggingEvent(nextLogEntry)) {
                clearEntangledLines(entangledLogLines);
                context.remove(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
            }
            context.remove(PreprocessAction.NEWLINE);
            context.remove(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
        } else if (JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahDegeneratedGcEvent
                || JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalMarkEvent
                || JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalUpdateEvent
                || JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitMarkEvent
                || JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitUpdateEvent) {
            this.logEntry = logEntry;
            context.add(PreprocessAction.NEWLINE);
            context.remove(TOKEN_BEGINNING_SHENANDOAH);
            context.remove(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
        } else if (JdkUtil.parseLogLine(logEntry, null, CollectorFamily.UNKNOWN) instanceof ShenandoahConcurrentEvent
                && !isThrowaway(logEntry)) {
            // Stand alone event
            if (!(context.contains(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT)
                    || context.contains(TOKEN_BEGINNING_SHENANDOAH))) {
                this.logEntry = logEntry;
                context.add(PreprocessAction.NEWLINE);
                // TODO: ?
                // context.add(TOKEN_BEGINNING_SHENANDOAH_CONCURRENT);
            } else {
                // output intermingled lines at end
                entangledLogLines.add(logEntry);
                context.remove(PreprocessAction.NEWLINE);
            }
        }
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
                // Add to prior line if current line is not an ending pattern
                if ((this.logEntry != null && this.logEntry.matches(TimesData.REGEX_JDK9))
                        || (logLine != null && !logLine.endsWith(Constants.LINE_SEPARATOR))) {
                    this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
                } else {
                    this.logEntry = this.logEntry + logLine;
                }
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.SHENANDOAH.toString();
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
}
