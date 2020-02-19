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

import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
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
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}:
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
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}:
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
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}:
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
 * 4) {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}:
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
 * 5) {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}:
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
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER
                    + " Pause (Init|Final) Mark( \\(update refs\\))?( \\(process weakrefs\\))?$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER
                    + " Using \\d of \\d workers for (init|final) (marking|reference update)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER
                    + " Using \\d of \\d workers for stw degenerated gc$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Good progress for (free|used) space: "
                    + JdkRegEx.SIZE + ", need " + JdkRegEx.SIZE + "$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pacer for Mark. Expected Live: "
                    + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: (inf|\\d{1,2}\\.\\d)x$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahDegeneratedGcEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pause Degenerated GC \\(Mark\\)$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Adaptive CSet Selection. Target Free: "
                    + JdkRegEx.SIZE + ", Actual Free: " + JdkRegEx.SIZE + ", Max CSet: " + JdkRegEx.SIZE
                    + ", Min Garbage: " + JdkRegEx.SIZE,
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Collectable Garbage: " + JdkRegEx.SIZE
                    + " \\(\\d{1,2}% of total\\), " + JdkRegEx.SIZE + " CSet, \\d{1,3} CSet regions",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalMarkEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Immediate Garbage: " + JdkRegEx.SIZE
                    + " \\(\\d{1,2}% of total\\), \\d{1,4} regions",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pacer for Evacuation. Used CSet: "
                    + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalEvacEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pause Final Evac$",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahFinalUpdateEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pause (Init|Final) Update Refs",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahInitUpdateEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER + " Pacer for Update Refs. Used: "
                    + JdkRegEx.SIZE + ", Free: " + JdkRegEx.SIZE + ", Non-Taxable: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx",
            // {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahConcurrentEvent}
            "^" + UnifiedRegEx.DECORATOR + " " + UnifiedRegEx.GC_EVENT_NUMBER
                    + " Using \\d{1,2} of \\d{1,2} workers for concurrent "
                    + "(reset|marking|preclean|evacuation|reference update)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "( " + UnifiedRegEx.GC_EVENT_NUMBER + ")? Free: " + JdkRegEx.SIZE
                    + " \\(\\d{1,4} regions\\), Max regular: " + JdkRegEx.SIZE + ", Max humongous: " + JdkRegEx.SIZE
                    + ", External frag: \\d{1,3}%, Internal frag: \\d{1,2}%$",
            //
            "^" + UnifiedRegEx.DECORATOR + "( " + UnifiedRegEx.GC_EVENT_NUMBER + ")? Evacuation Reserve: "
                    + JdkRegEx.SIZE + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Pacer for Idle. Initial: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Free headroom: " + JdkRegEx.SIZE + " \\(free\\) - " + JdkRegEx.SIZE
                    + " \\(spike\\) - " + JdkRegEx.SIZE + " \\(penalties\\) = " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Uncommitted " + JdkRegEx.SIZE + ". Heap: " + JdkRegEx.SIZE + " reserved, "
                    + JdkRegEx.SIZE + " committed, " + JdkRegEx.SIZE + " used$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Failed to allocate " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Cancelling GC: Allocation Failure$",
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
        // TODO: Get rid of this and make them throwaway events?
        for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
            if (logLine.matches(REGEX_THROWAWAY[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}