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

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * Shenandoah logging preprocessing.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent}:
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
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent}:
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
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent}:
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
 * 4) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent}:
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
 * 5) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent}:
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
 * <p>
 * 6) {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent}:
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
     * Regular expression for retained beginning SHENANDOAH_CONCURRENT marking event
     * 
     * 2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT_MARKING = "^(" + JdkRegEx.DECORATOR
            + " \\[Concurrent marking)$";

    /**
     * Regular expression for retained beginning SHENANDOAH_CONCURRENT cleanup event
     * 
     * 2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP = "^(" + JdkRegEx.DECORATOR
            + " \\[Concurrent cleanup " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + UnifiedRegEx.DURATION + "\\])$";

    /**
     * Regular expression for retained Metaspace block.
     * 
     * , [Metaspace: 6477K->6481K(1056768K)]
     */
    private static final String REGEX_RETAIN_METASPACE = "(, \\[Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\])[ ]*";

    /**
     * Regular expression for retained duration. This can come in the middle or at the end of a logging event split over
     * multiple lines. Check the TOKEN to see if in the middle of preprocessing an event that spans multiple lines.
     * 
     * , 27.5589374 secs]
     */
    private static final String REGEX_RETAIN_DURATION = "(, " + UnifiedRegEx.DURATION + "\\])[ ]*";

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
                    + ") (\\[)?Pause (Init|Final) Mark( \\((update refs|unload classes)\\))?( \\(process weakrefs\\))?"
                    + "(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{1,4}Using \\d of \\d workers for (init|final) (marking|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " Using \\d of \\d workers for stw degenerated gc$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " Good progress for (free|used) space: " + JdkRegEx.SIZE + ", need "
                    + JdkRegEx.SIZE + "$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Pacer for Mark. Expected Live: " + JdkRegEx.SIZE + ", Free: "
                    + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE + ", Alloc Tax Rate: (inf|\\d{1,2}\\.\\d)x$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcEvent}
            "^" + UnifiedRegEx.DECORATOR + " Pause Degenerated GC \\((Mark|Outside of Cycle)\\)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Adaptive CSet Selection. Target Free: " + JdkRegEx.SIZE
                    + ", Actual Free: " + JdkRegEx.SIZE + ", Max CSet: " + JdkRegEx.SIZE + ", Min Garbage: "
                    + JdkRegEx.SIZE,
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Collectable Garbage: " + JdkRegEx.SIZE
                    + " \\(\\d{1,2}%( of total)?\\)(, Immediate: " + JdkRegEx.SIZE + " \\(\\d{1,2}%\\))?, (CSet: )?"
                    + JdkRegEx.SIZE + " (\\(\\d{1,2}%\\))?(CSet, \\d{1,3} CSet regions)?",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Immediate Garbage: " + JdkRegEx.SIZE
                    + " \\(\\d{1,2}% of total\\), \\d{1,4} regions",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Pacer for Evacuation. Used CSet: " + JdkRegEx.SIZE + ", Free: "
                    + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE + ", Alloc Tax Rate: \\d\\.\\dx$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR + ") (\\[)?Pause Final Evac(, start\\])?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
                    + ") (\\[)?Pause (Init|Final) Update Refs(, start\\])?",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Pacer for Update Refs. Used: " + JdkRegEx.SIZE + ", Free: "
                    + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE + ", Alloc Tax Rate: \\d\\.\\dx",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahConcurrentEvent}
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Using \\d{1,2} of \\d{1,2} workers for concurrent "
                    + "(reset|marking|preclean|evacuation|reference update)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Free: " + JdkRegEx.SIZE + " \\(\\d{1,4} regions\\), Max regular: "
                    + JdkRegEx.SIZE + ", Max humongous: " + JdkRegEx.SIZE
                    + ", External frag: \\d{1,3}%, Internal frag: \\d{1,2}%$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Evacuation Reserve: " + JdkRegEx.SIZE
                    + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Pacer for Idle. Initial: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Free headroom: " + JdkRegEx.SIZE + " \\(free\\) - " + JdkRegEx.SIZE
                    + " \\(spike\\) - " + JdkRegEx.SIZE + " \\(penalties\\) = " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Uncommitted " + JdkRegEx.SIZE + ". Heap: " + JdkRegEx.SIZE + " reserved, "
                    + JdkRegEx.SIZE + " committed, " + JdkRegEx.SIZE + " used$",
            //
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Failed to allocate (TLAB, )?" + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Cancelling GC: (Allocation Failure|Stopping VM)$",
            //
            "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
                    + ") (\\[)?Concurrent (cleanup|evacuation|marking|precleaning|reset|update references)"
                    + "( \\((process weakrefs|unload classes|update refs)\\))?( \\(process weakrefs\\))?(, start\\])?$",
            //
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{1,4}Pacer for (Reset|Precleaning). Non-Taxable: " + JdkRegEx.SIZE
                    + "$"
            //
    };

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
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_CONCURRENT_MARKING)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CONCURRENT_MARKING);
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
            if (entangledLogLines != null && entangledLogLines.size() > 0 && newLoggingEvent(nextLogEntry)) {
                clearEntangledLines(entangledLogLines);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
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
        if (logLine.matches(REGEX_RETAIN_BEGINNING_CONCURRENT_MARKING)
                || logLine.matches(REGEX_RETAIN_BEGINNING_CONCURRENT_CLEANUP) || logLine.matches(REGEX_RETAIN_METASPACE)
                || logLine.matches(REGEX_RETAIN_DURATION)) {
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
     * TODO: Move to superclass.
     * 
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
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