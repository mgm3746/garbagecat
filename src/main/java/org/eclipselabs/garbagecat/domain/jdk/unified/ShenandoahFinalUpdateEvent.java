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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * SHENANDOAH_FINAL_UPDATE
 * </p>
 * 
 * <p>
 * Finishes the update references phase by re-updating the existing root set. It also recycles the regions from the
 * collection set, because now heap does not have references to (stale) objects to them. This is the last pause in the
 * cycle, and its duration is dependent on the size of root set[1].
 * 
 * [1]<a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah GC</a>
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahFinalUpdateEvent extends ShenandoahCollector
        implements UnifiedLogging, BlockingEvent, ParallelEvent {

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[((" + JdkRegEx.TIMESTAMP + "s)|("
            + JdkRegEx.TIMESTAMP_MILLIS + "))\\](\\[info\\]\\[gc[ ]{0,11}\\])? " + JdkRegEx.GC_EVENT_NUMBER
            + " Pause Final Update Refs " + JdkRegEx.DURATION_JDK9 + "[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahFinalUpdateEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                // TODO: Is this correct?
                long endTimestamp;
                if (matcher.group(12).matches(JdkRegEx.TIMESTAMP_MILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(16));
                } else {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
                }
                duration = JdkMath.convertMillisToMicros(matcher.group(18)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
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
    public ShenandoahFinalUpdateEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }
}
