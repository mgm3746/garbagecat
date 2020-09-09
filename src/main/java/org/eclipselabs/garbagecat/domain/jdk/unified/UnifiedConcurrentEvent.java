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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_CONCURRENT
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} or
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1ConcurrentEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * Any number of events that happen concurrently with the JVM's execution of application threads. These events are not
 * included in the GC analysis since there is no application pause time.
 * </p>
 * 
 * <h3>CMS</h3>
 * 
 * <pre>
 * [0.082s][info][gc] GC(1) Concurrent Mark
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Mark 1.428ms
 * </pre>
 * 
 * <pre>
 * [0.054s][info][gc           ] GC(1) Concurrent Mark 1.260ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms
 * </pre>
 * 
 * <pre>
 * [0.054s][info][gc           ] GC(1) Concurrent Preclean 0.033ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.084s][info][gc] GC(1) Concurrent Sweep
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms
 * </pre>
 * 
 * <pre>
 * [0.055s][info][gc           ] GC(1) Concurrent Sweep 0.298ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Reset
 * </pre>
 * 
 * <pre>
 * [0.086s][info][gc] GC(1) Concurrent Reset 0.841ms
 * </pre>
 * 
 * <pre>
 * [0.056s][info][gc           ] GC(1) Concurrent Reset 0.693ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * G1:
 * </p>
 * 
 * <pre>
 * [36.400s][info][gc] GC(1330) Concurrent Cycle
 * </pre>
 * 
 * <pre>
 * [36.606s][info][gc] GC(1335) Concurrent Cycle 90.487ms
 * </pre>
 * 
 * <p>
 * Detailed logging:
 * </p>
 * 
 * <pre>
 * [16.601s][info][gc           ] GC(1033) Concurrent Cycle
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks 0.019ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions 0.283ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Mark (16.601s)
 * </pre>
 * 
 * <pre>
 * [16.050s][info][gc,marking   ] GC(969) Concurrent Mark (16.017s, 16.050s) 33.614ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Mark From Roots
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,task      ] GC(1033) Using 1 workers of 1 for marking
 * </pre>
 * 
 * <pre>
 * [16.053s][info][gc,marking    ] GC(969) Concurrent Rebuild Remembered Sets
 * </pre>
 * 
 * <pre>
 * [16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark
 * </pre>
 * 
 * <pre>
 * [16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark 0.428ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedConcurrentEvent extends UnknownCollector implements UnifiedLogging, ParallelEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            //
            "^" + UnifiedRegEx.DECORATOR + " Concurrent Cycle( " + UnifiedRegEx.DURATION + ")?$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " Concurrent (Cleanup for Next Mark|Clear Claimed Marks|Create Live Data|Mark|Mark Abort|"
                    + "Mark From Roots|Preclean|Rebuild Remembered Sets|Reset|Scan Root Regions|Sweep)( \\("
                    + JdkRegEx.TIMESTAMP + "s(, " + JdkRegEx.TIMESTAMP + "s)?\\))?( " + UnifiedRegEx.DURATION + ")?"
                    + TimesData.REGEX_JDK9 + "?[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Using \\d workers of \\d for (full compaction|marking)$"
            //
    };

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString();
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
