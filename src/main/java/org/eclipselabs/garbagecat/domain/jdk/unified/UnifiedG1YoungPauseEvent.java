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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * UNIFIED_G1_YOUNG_PAUSE
 * </p>
 *
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * G1 young generation collection. Live objects from Eden and Survivor regions are copied to new regions, either to a
 * survivor region or promoted to the old space.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Standard format:
 * </p>
 * 
 * <pre>
 * [18.406s][info][gc] GC(1012) Pause Young (Normal) (G1 Evacuation Pause) 38M-&gt;19M(46M) 1.815ms
 * </pre>
 * 
 * <p>
 * Preprocessed from logging with details:
 * </p>
 * 
 * <pre>
 * [0.101s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 0M-&gt;0M(2M) 1.371ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungPauseEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, YoungCollection, ParallelEvent, CombinedData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")";

    /**
     * Regular expression defining standard logging (no details).
     */
    private static final String REGEX = "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Young \\((Normal|Concurrent Start)\\) \\(" + TRIGGER + "\\) "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + "[ ]*$";

    /**
     * Regular expression defining preprocessed logging.
     */
    private static final String REGEX_PREPROCESSED = "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc[ ]{11,12}\\] "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(" + TRIGGER + "\\) "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9
            + TimesData.REGEX_JDK9 + "[ ]*$";

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
     * Combined young + old generation size (kilobytes) at beginning of GC event.
     */
    private int combinedBegin;

    /**
     * Combined young + old generation size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Combined young + old generation allocation (kilobytes).
     */
    private int combinedAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * The time of all threads added together in centiseconds.
     */
    private int timeUser;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedG1YoungPauseEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                long endTimestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                trigger = matcher.group(3);
                combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(4)), matcher.group(6).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(7)), matcher.group(9).charAt(0));
                combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(10)),
                        matcher.group(12).charAt(0));
                duration = JdkMath.convertMillisToMicros(matcher.group(13)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        } else if (logEntry.matches(REGEX_PREPROCESSED)) {
            Pattern pattern = Pattern.compile(REGEX_PREPROCESSED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                long endTimestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                trigger = matcher.group(4);
                combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(5)), matcher.group(7).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(8)), matcher.group(10).charAt(0));
                combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(11)),
                        matcher.group(13).charAt(0));
                duration = JdkMath.convertMillisToMicros(matcher.group(14)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                if (matcher.group(15) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(16)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(17)).intValue();
                } else {
                    timeUser = TimesData.NO_DATA;
                    timeReal = TimesData.NO_DATA;
                }
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
    public UnifiedG1YoungPauseEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString();
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

    public int getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAllocation;
    }

    public String getTrigger() {
        return trigger;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeReal);
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX) || logLine.matches(REGEX_PREPROCESSED);
    }
}
