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

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_CLEANUP
 * </p>
 * 
 * <p>
 * G1 collector cleanup phase. The final phase of the marking cycle when accounting (identify free regions and regions
 * that are candidates for mixed garbage collection) and scrubbing takes place.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 18.650: [GC cleanup 297M-&gt;236M(512M), 0.0014690 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC cleanup 297M-&gt;236M(512M), 0.0014690 secs]
 * </pre>
 * 
 * <p>
 * 3) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC cleanup 297M-&gt;236M(512M), 0.0014690 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1CleanupEvent extends G1Collector implements BlockingEvent, ParallelEvent, CombinedData, TimesData {

    private static final Pattern pattern = Pattern.compile(G1CleanupEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR
            + " \\[GC cleanup( )?(1745.419: \\[G1Ergonomics \\(Concurrent Cycles\\) finish cleanup, occupancy: "
            + JdkRegEx.SIZE_BYTES + " bytes, capacity: " + JdkRegEx.SIZE_BYTES + " bytes, known garbage: "
            + JdkRegEx.SIZE_BYTES + " bytes \\(\\d{1,2}\\.\\d{2} %\\)\\])?(" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\))?, " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

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
     * Young generation size at beginning of GC event.
     */
    private Memory combined = Memory.ZERO;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory combinedAvailable = Memory.ZERO;

    /**
     * Young generation size at end of GC event.
     */
    private Memory combinedEnd = Memory.ZERO;

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
    public G1CleanupEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
            }
            if (matcher.group(20) != null) {
                combined = memory(matcher.group(20), matcher.group(22).charAt(0)).convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(23), matcher.group(25).charAt(0)).convertTo(KILOBYTES);
                combinedAvailable = memory(matcher.group(26), matcher.group(28).charAt(0)).convertTo(KILOBYTES);
            }
            duration = JdkMath.convertSecsToMicros(matcher.group(29)).intValue();
            if (matcher.group(32) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(33)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(34)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(35)).intValue();
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
    public G1CleanupEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combined;
    }

    public Memory getCombinedSpace() {
        return combinedAvailable;
    }

    public long getDuration() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_CLEANUP.toString();
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
