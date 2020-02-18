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
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * UNIFIED_G1_MIXED_PAUSE
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1MixedPauseEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * G1 mixed generation collection. Performed at the same time as a young collection, so it is a collection of the young
 * space and the low liveness regions of the old space. The <code>-XX:InitiatingHeapOccupancyPercent</code> defines the
 * heap occupancy threshold for initiating a mixed pause.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * [15.114s][info][gc            ] GC(1195) Pause Young (Mixed) (G1 Evacuation Pause) 15M-&gt;12M(31M) 4.194ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1MixedPauseEvent extends G1Collector implements UnifiedLogging, BlockingEvent, YoungCollection,
        ParallelEvent, PermData, CombinedData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")";

    /**
     * Regular expression defining preprocessed logging.
     */
    private static final String REGEX_PREPROCESSED = "^" + UnifiedLogging.DECORATOR + " " + JdkRegEx.GC_EVENT_NUMBER
            + " Pause Young \\(Mixed\\) \\(" + TRIGGER + "\\) Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) "
            + JdkRegEx.DURATION_JDK9 + TimesData.REGEX_JDK9 + "[ ]*$";

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
     * Permanent generation size (kilobytes) at beginning of GC event.
     */
    private int permGen;

    /**
     * Permanent generation size (kilobytes) at end of GC event.
     */
    private int permGenEnd;

    /**
     * Space allocated to permanent generation (kilobytes).
     */
    private int permGenAllocation;

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
    public UnifiedG1MixedPauseEvent(String logEntry) {
        this.logEntry = logEntry;

        Pattern pattern = Pattern.compile(REGEX_PREPROCESSED);
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13).matches(JdkRegEx.TIMESTAMP_MILLIS)) {
                timestamp = Long.parseLong(matcher.group(15));
            } else {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
            }
            trigger = matcher.group(21);
            permGen = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(22)), matcher.group(24).charAt(0));
            permGenEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(25)), matcher.group(27).charAt(0));
            permGenAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(28)), matcher.group(30).charAt(0));
            combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(31)), matcher.group(33).charAt(0));
            combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(34)), matcher.group(36).charAt(0));
            combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(37)),
                    matcher.group(39).charAt(0));
            duration = JdkMath.convertMillisToMicros(matcher.group(40)).intValue();
            if (matcher.group(41) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(42)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(43)).intValue();
            } else {
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
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
    public UnifiedG1MixedPauseEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString();
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

    public int getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(int permGen) {
        this.permGen = permGen;
    }

    public int getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(int permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public int getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(int permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
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
        return logLine.matches(REGEX_PREPROCESSED);
    }
}
