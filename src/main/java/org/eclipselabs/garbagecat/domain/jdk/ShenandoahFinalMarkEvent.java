/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * SHENANDOAH_FINAL_MARK
 * </p>
 * 
 * <p>
 * Finishes the concurrent marking by draining all pending marking/update queues and re-scanning the root set. It also
 * initializes evacuation by figuring out the regions to be evacuated (collection set), pre-evacuating some roots, and
 * generally prepares runtime for the next phase. Part of this work can be done concurrently during Concurrent
 * precleaning phase. This is the second pause in the cycle, and the most dominant time consumers here are draining the
 * queues and scanning the root set[1].
 * 
 * [1]<a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah GC</a>
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]
 * </pre>
 * 
 * <p>
 * 2) Process weakrefs:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:45.915-0400: 16.977: [Pause Final Mark (process weakrefs), 1.040 ms]
 * </pre>
 * 
 * <p>
 * 3) Update refs:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:07:16.371-0400: 227.433: [Pause Final Mark (update refs), 0.589 ms]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahFinalMarkEvent extends ShenandoahCollector implements BlockingEvent, ParallelEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR
            + " \\[Pause Final Mark( \\((process weakrefs|unload classes|update refs)\\))?, " + JdkRegEx.DURATION_MS
            + "\\][ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
    }

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

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
    public ShenandoahFinalMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            duration = JdkMath.convertMillisToMicros(matcher.group(JdkUtil.DECORATOR_SIZE + 3)).intValue();
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
            }
        }
    }

    /**
     * Alternate constructor. Create event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ShenandoahFinalMarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDurationMicros() {
        return duration;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.SHENANDOAH_FINAL_MARK;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
