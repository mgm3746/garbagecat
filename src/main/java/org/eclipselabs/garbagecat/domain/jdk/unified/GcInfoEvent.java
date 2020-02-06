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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * GC_INFO
 * </p>
 * 
 * <p>
 * Information printed at the beginning of gc logging with <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K
 * [2019-02-05T14:47:31.091-0200][3ms] Max TLAB size: 512K
 * [2019-02-05T14:47:31.091-0200][3ms] GC threads: 4 parallel, 4 concurrent
 * [2019-02-05T14:47:31.091-0200][3ms] Reference processing: parallel
 * [2019-02-05T14:47:31.091-0200][3ms] Shenandoah heuristics: adaptive
 * [2019-02-05T14:47:31.091-0200][3ms] Initialize Shenandoah heap with initial size 1366294528 bytes
 * [2019-02-05T14:47:31.091-0200][3ms] Pacer for Idle. Initial: 26M, Alloc Tax Rate: 1.0x
 * [2019-02-05T14:47:31.092-0200][4ms] Safepointing mechanism: global-page poll
 * [2019-02-05T14:47:34.156-0200][3068ms] Trigger: Learning 1 of 5. Free (912M) is below initial threshold (912M)
 * [2019-02-05T14:47:34.156-0200][3068ms] Free: 912M (1824 regions), Max regular: 512K, Max humongous: 933376K, External frag: 1%, Internal frag: 0%
 * [2019-02-05T14:47:34.156-0200][3068ms] Evacuation Reserve: 65M (131 regions), Max regular: 512K
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcInfoEvent implements UnifiedLogging, LogEvent, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Humongous object threshold: "
                    + JdkRegEx.SIZE + "$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Max TLAB size: " + JdkRegEx.SIZE
                    + "$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS
                    + "\\] GC threads: \\d parallel, \\d concurrent$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Reference processing: parallel$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Shenandoah heuristics: adaptive$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS
                    + "\\] Initialize Shenandoah heap with initial size \\d{10} bytes$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Pacer for Idle. Initial: "
                    + JdkRegEx.SIZE + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS
                    + "\\] Safepointing mechanism: global-page poll$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS
                    + "\\] Trigger: Learning \\d of \\d\\. Free \\(" + JdkRegEx.SIZE
                    + "\\) is below initial threshold \\(" + JdkRegEx.SIZE + "\\)$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Free: " + JdkRegEx.SIZE
                    + " \\(\\d{1,4} regions\\), Max regular: " + JdkRegEx.SIZE + ", Max humongous: " + JdkRegEx.SIZE
                    + ", External frag: \\d%, Internal frag: \\d%$",
            //
            "^\\[" + JdkRegEx.DATESTAMP + "\\]\\[" + JdkRegEx.TIMESTAMP_MILLIS + "\\] Evacuation Reserve: "
                    + JdkRegEx.SIZE + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public GcInfoEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_INFO.toString();
    }

    public long getTimestamp() {
        return timestamp;
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
