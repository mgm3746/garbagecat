/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.SerialCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_SERIAL_NEW
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) DefNew: 983K-&gt;128K(1152K) Tenured: 0K-&gt;458K(768K) Metaspace: 246K-&gt;246K(1056768K) 0M-&gt;0M(1M) 1.393ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSerialNewEvent extends SerialCollector implements UnifiedLogging, BlockingEvent, YoungCollection,
        SerialCollection, YoungData, OldData, PermMetaspaceData, TriggerData {

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
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX_PREPROCESSED = "" + UnifiedRegEx.DECORATOR + " Pause Young \\(" + TRIGGER
            + "\\) \\DefNew: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Tenured: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Metaspace: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + TimesData.REGEX_JDK9 + "[ ]*$";

    private static final Pattern pattern = Pattern.compile(UnifiedSerialNewEvent.REGEX_PREPROCESSED);

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedSerialNewEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(12));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(11)).longValue();
            } else {
                if (matcher.group(14) != null) {
                    if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(16));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(15)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(24);
            young = memory(matcher.group(25), matcher.group(27).charAt(0)).convertTo(KILOBYTES);
            youngEnd = memory(matcher.group(28), matcher.group(30).charAt(0)).convertTo(KILOBYTES);
            youngAvailable = memory(matcher.group(31), matcher.group(33).charAt(0)).convertTo(KILOBYTES);
            old = memory(matcher.group(34), matcher.group(36).charAt(0)).convertTo(KILOBYTES);
            oldEnd = memory(matcher.group(37), matcher.group(39).charAt(0)).convertTo(KILOBYTES);
            oldAllocation = memory(matcher.group(40), matcher.group(42).charAt(0)).convertTo(KILOBYTES);
            permGen = memory(matcher.group(43), matcher.group(45).charAt(0)).convertTo(KILOBYTES);
            permGenEnd = memory(matcher.group(46), matcher.group(48).charAt(0)).convertTo(KILOBYTES);
            permGenAllocation = memory(matcher.group(49), matcher.group(51).charAt(0)).convertTo(KILOBYTES);
            duration = JdkMath.convertMillisToMicros(matcher.group(61)).intValue();
        }
    }

    /**
     * Alternate constructor. Create serial logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedSerialNewEvent(String logEntry, long timestamp, int duration) {
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

    public Memory getYoungOccupancyInit() {
        return young;
    }

    public Memory getYoungOccupancyEnd() {
        return youngEnd;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    protected void setTrigger(String trigger) {
        this.trigger = trigger;
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
