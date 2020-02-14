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
 * SHENANDOAH_INIT_MARK
 * </p>
 * 
 * <p>
 * Initiates the concurrent marking. It prepares the heap and application threads for concurrent mark, and then scans
 * the root set. This is the first pause in the cycle, and the most dominant consumer is the root set scan. Therefore,
 * its duration is dependent on the root set size[1].
 * 
 * [1]<a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah GC</a>
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * [0.521s][info][gc] GC(1) Pause Init Mark 0.453ms
 * </pre>
 * 
 * <p>
 * 2) process weakrefs:
 * </p>
 * 
 * <pre>
 * [0.456s][info][gc] GC(0) Pause Init Mark (process weakrefs) 0.868ms
 * </pre>
 * 
 * <p>
 * 3) update refs:
 * </p>
 * 
 * <pre>
 *[10.453s][info][gc] GC(279) Pause Init Mark (update refs) 0.244ms
 * </pre>
 * 
 * <p>
 * 4) With <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>:
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pause Init Mark (process weakrefs) 2.904ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahInitMarkEvent extends ShenandoahCollector
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
            + " Pause Init Mark( \\(update refs\\))?( \\(process weakrefs\\))? " + JdkRegEx.DURATION_JDK9 + "[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahInitMarkEvent(String logEntry) {
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
                duration = JdkMath.convertMillisToMicros(matcher.group(20)).intValue();
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
    public ShenandoahInitMarkEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString();
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
