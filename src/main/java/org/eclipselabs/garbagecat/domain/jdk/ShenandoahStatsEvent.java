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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.HeaderEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * SHENANDOAH_STATS
 * </p>
 * 
 * <p>
 * Output from XX:+PrintGCDetails.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * All times are wall-clock times, except per-root-class counters, that are sum over
 * all workers. Dividing the &lt;total&gt; over the root stage time estimates parallelism.
 *   Update Region States              789 us
 *     S: &lt;total&gt;                    69130 us
 *     S: JNI Handles Roots              7 us, workers (us): ---,   7, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *     S: JFR Weak Roots                 1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---,
 *     S: Flat Profiler Roots          129 us, workers (us): ---, ---, 129, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *   Weak Roots                         36 us, parallelism: 0.94x
 *     WR: &lt;total&gt;                      34 us
 *     WR: JFR Weak Roots                0 us, workers (us):   0, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *     WR: JNI Weak Roots               33 us, workers (us):  33, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *   Update Region States              234 us
 *   Choose Collection Set             440 us
 *   Rebuild Free Set                   36 us
 *     E: &lt;total&gt;                    69151 us
 *     E: JNI Handles Roots              3 us, workers (us):   3, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *     E: JFR Weak Roots                 1 us, workers (us): ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *     E: Flat Profiler Roots           22 us, workers (us):  22, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,
 *     UR: &lt;total&gt;                    3127 us
 *   Update Region States              226 us
 *   Trash Collection Set               61 us
 *   Rebuild Free Set                   45 us
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahStatsEvent extends ShenandoahCollector implements HeaderEvent, ThrowAwayEvent {

    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^All times are wall-clock times, except per-root-class counters, that "
            + "are sum over$";
    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            _REGEX_HEADER,
            //
            "^all workers\\. Dividing the <total> over the root stage time estimates parallelism\\.$",
            // Main headings
            "^(Concurrent (Cleanup|Evacuation|Marking|Precleaning|Reset|Update Refs)|Pacing|"
                    + "Pause Degenerated GC \\([GN]\\)|Pause Final Mark \\([GN]\\)|Pause Final Update Refs \\([GN]\\)|"
                    + "Pause Init Mark \\([GN]\\)|Pause Init  Update Refs \\([GN]\\)|Pause Full GC \\([GN]\\))"
                    + "[ ]{1,}\\d{1,} us$",
            // Indented 2 spaces
            "^  (Accumulate Stats|Adjust Pointers|Calculate Addresses|Choose Collection Set|Copy Objects|"
                    + "Degen Update Roots|Finish Queues|Finish Work|Initial Evacuation |Make Parsable|Mark|"
                    + "Post Heap Dump|Pre Heap Dump|Prepare|Rebuild Free Set|Resize TLABs|Retire TLABs|Scan Roots|"
                    + "System Purge|Trash Collection Set|Weak References|Weak Roots|Update Region States|Update Roots)"
                    + "[ ]{1,}\\d{1,} us.*$",
            // Indented 4 spaces: acronyms
            "^    (DU|E|FA|FS|FU|S|UR|WR): .*$",
            // Indented 4 spaces: words
            "^    (CLDG|Deallocate Metadata|Enqueue|Finish Queues|Humongous Objects|Parallel Cleanup|Process|"
                    + "Rebuild Region Sets|Regular Objects|Reset Complete Bitmap|System Purge|Unload Classes|"
                    + "Weak References)[ ]{1,}\\d{1,} us$",
            // Indented 6 spaces
            "^      (Enqueue|Process|Unload Classes)[ ]{1,}\\d{1,} us$",
            //
            "^Allocation pacing accrued:$",
            //
            "^[ ]{0,}\\d{1,} of[ ]{0,}\\d{1,} ms \\([ ]{0,}\\d{1,}\\.\\d%\\):.+$"
            //
    };
    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(REGEX.length);

    static {
        for (String regex : REGEX) {
            REGEX_PATTERN_LIST.add(Pattern.compile(regex));
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
        boolean match = false;
        for (int i = 0; i < REGEX_PATTERN_LIST.size(); i++) {
            Pattern pattern = REGEX_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                match = true;
                break;
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
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahStatsEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.SHENANDOAH_STATS;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return 0;
    }

    @Override
    public boolean isHeader() {
        boolean isHeader = false;
        if (this.logEntry != null) {
            isHeader = logEntry.matches(_REGEX_HEADER);
        }
        return isHeader;
    }
}
