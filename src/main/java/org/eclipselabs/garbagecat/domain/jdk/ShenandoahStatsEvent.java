/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * SHENANDOAH_STATS
 * </p>
 * 
 * <p>
 * Output from XX:+PrintGCDetails (JDK8 extended logs) or -Xlog:gc+stats (JDK11 extended statistics).
 * </p>
 * 
 * <h3>Example Logging</h3>
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
public class ShenandoahStatsEvent extends ShenandoahCollector implements ThrowAwayEvent {

    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            "^All times are wall-clock times, except per-root-class counters, that are sum over$",
            //
            "^all workers. Dividing the <total> over the root stage time estimates parallelism.$",
            //
            "^  Update Region States.+$",
            //
            "^    (DU|E|S|UR|WR): <total>.+$",
            //
            "^    (DU|E|S): JNI Handles Roots.+$",
            //
            "^    (DU|E|S|WR): JFR Weak Roots.+$",
            //
            "^    (DU|WR): JNI Weak Roots.+$",
            //
            "^    (E|S): Flat Profiler Roots.+$",
            //
            "^  Weak Roots.+$",
            //
            "^  (Choose|Trash) Collection Set.+$",
            //
            "^  Rebuild Free Set.+$",
            //
            "^  Finish Work.+$",
            //
            "^Pause Degenerated GC \\((G|N)\\).+$",
            //
            "^  Degen Update Roots.+$",
            //
            "^    DU: (CLDG|Code Cache|Flat Profiler|JVMTI|Management|String Table|Synchronizer|System Dict|Thread|"
                    + "Universe) Roots.+$"
            //
    };

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_STATS.toString();
    }

    public long getTimestamp() {
        throw new UnsupportedOperationException("Event does not include timestamp information");
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
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}
