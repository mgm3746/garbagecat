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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * UNIFIED_REMARK
 * </p>
 * 
 * <p>
 * Remark collection JDK9+ with <code>-Xlog:gc:file=&lt;file&gt;</code> (no details).
 * </p>
 * 
 * <p>
 * Collector is one of the following: (1) {@link org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent}, (2)
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1RemarkEvent}.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [4.353s][info][gc] GC(3130) Pause Remark 5M-&gt;5M(7M) 1.398ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedRemarkEvent extends UnknownCollector implements BlockingEvent, ParallelEvent {

    /**
     * Regular expressions defining the logging JDK9+.
     */
    private static final String REGEX = "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Remark " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) " + JdkRegEx.DURATION_JDK9 + "[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in milliseconds (rounded).
     */
    private int duration;

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
    public UnifiedRemarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            long endTimestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            duration = JdkMath.roundMillis(matcher.group(11)).intValue();
            timestamp = endTimestamp - duration;
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
     */
    public UnifiedRemarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public int getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_REMARK.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX);
    }
}
