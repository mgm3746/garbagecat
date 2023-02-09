/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_REMARK
 * </p>
 * 
 * <p>
 * G1 collector remarking phase.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 252.889: [GC remark, 0.0178990 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC remark, 0.0178990 secs]
 * </pre>
 * 
 * <p>
 * 3) With -XX:+PrintReferenceGC:
 * </p>
 * 
 * <pre>
 * 2021-08-20T11:53:44.348+0100: 2377830.399: [GC remark 2021-08-20T11:53:44.348+0100: 2377830.399: [Finalize Marking, 0.0012914 secs] 2021-08-20T11:53:44.350+0100: 2377830.400: [GC ref-proc2021-08-20T11:53:44.350+0100: 2377830.400: [SoftReference, 18076174 refs, 2.6283514 secs]2021-08-20T11:53:46.978+0100: 2377833.028: [WeakReference, 18636 refs, 0.0029750 secs]2021-08-20T11:53:46.981+0100: 2377833.031: [FinalReference, 17387271 refs, 2.5263032 secs]2021-08-20T11:53:49.507+0100: 2377835.558: [PhantomReference, 0 refs, 2136 refs, 0.0012040 secs]2021-08-20T11:53:49.509+0100: 2377835.559: [JNI Weak Reference, 0.0001679 secs], 14.8775199 secs] 2021-08-20T11:53:59.227+0100: 2377845.278: [Unloading, 0.0178265 secs], 14.9383332 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1RemarkEvent extends G1Collector implements BlockingEvent, ParallelEvent, TimesData {
    private static final Pattern pattern = Pattern.compile(G1RemarkEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC remark( " + JdkRegEx.DECORATOR
            + " \\[Finalize Marking, " + JdkRegEx.DURATION + "\\] " + JdkRegEx.DECORATOR + " \\[GC ref-proc"
            + JdkRegEx.DECORATOR + " \\[SoftReference, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR
            + " \\[WeakReference, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR
            + " \\[FinalReference, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR
            + " \\[PhantomReference, \\d{1,} refs, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR
            + " \\[JNI Weak Reference, " + JdkRegEx.DURATION + "\\], " + JdkRegEx.DURATION + "\\] " + JdkRegEx.DECORATOR
            + " \\[Unloading, " + JdkRegEx.DURATION + "\\])?, " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX
            + "?[ ]*$";

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

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal = TimesData.NO_DATA;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys = TimesData.NO_DATA;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser = TimesData.NO_DATA;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public G1RemarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
            }
            duration = JdkMath.convertSecsToMicros(matcher.group(143)).intValue();
            if (matcher.group(146) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(147)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(148)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(149)).intValue();
            }
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
     *            The elapsed clock time for the GC event in microseconds.
     */
    public G1RemarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_REMARK.toString();
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public int getTimeReal() {
        return timeReal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeUser() {
        return timeUser;
    }
}
