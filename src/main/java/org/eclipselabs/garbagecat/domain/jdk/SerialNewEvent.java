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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * SERIAL_NEW
 * </p>
 * 
 * <p>
 * Young generation collector used when <code>-XX:+UseSerialGC</code> JVM option specified.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 7.798: [GC 7.798: [DefNew: 37172K-&gt;3631K(39296K), 0.0209300 secs] 41677K-&gt;10314K(126720K), 0.0210210 secs]
 * </pre>
 * 
 * <p>
 * 2) With erroneous "Full":
 * </p>
 * 
 * <pre>
 * 142352.790: [Full GC 142352.790: [DefNew: 444956K-&gt;28315K(471872K), 0.0971099 secs] 1020658K-&gt;604017K(1520448K), 0.0972451 secs]
 * </pre>
 * 
 * <p>
 * 3) No space after "GC":
 * </p>
 * 
 * <pre>
 * 4.296: [GC4.296: [DefNew: 68160K-&gt;8512K(76672K), 0.0528470 secs] 68160K-&gt;11664K(1325760K), 0.0530640 secs] [Times: user=0.04 sys=0.00, real=0.05 secs]
 * </pre>
 * 
 * <p>
 * 4) With trigger:
 * </p>
 * 
 * <pre>
 * 2.218: [GC (Allocation Failure) 2.218: [DefNew: 209792K-&gt;15933K(235968K), 0.0848369 secs] 209792K-&gt;15933K(760256K), 0.0849244 secs] [Times: user=0.03 sys=0.06, real=0.08 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class SerialNewEvent extends SerialCollector
        implements BlockingEvent, YoungCollection, YoungData, OldData, TriggerData, SerialCollection, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[(Full )?GC( \\(" + __TRIGGER + "\\))?( )?"
            + JdkRegEx.DECORATOR + " \\[DefNew: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(_REGEX);

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
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

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
     * The trigger for the GC event.
     */
    private GcTrigger trigger;

    /**
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public SerialNewEvent(String logEntry) {
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
            trigger = GcTrigger.getTrigger(matcher.group(18));
            young = kilobytes(matcher.group(31));
            youngEnd = kilobytes(matcher.group(32));
            youngAvailable = kilobytes(matcher.group(33));
            old = kilobytes(matcher.group(37)).minus(young);
            oldEnd = kilobytes(matcher.group(38)).minus(youngEnd);
            oldAllocation = kilobytes(matcher.group(39)).minus(youngAvailable);
            duration = JdkMath.convertSecsToMicros(matcher.group(40)).intValue();
            if (matcher.group(43) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(44)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(45)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(46)).intValue();
            }
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
    public SerialNewEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDurationMicros() {
        return duration;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.SERIAL_NEW;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SERIAL_NEW.toString();
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public int getPermGen() {
        throw new UnsupportedOperationException("Event does not include perm gen information");
    }

    public int getPermGenAllocation() {
        throw new UnsupportedOperationException("Event does not include perm gen information");
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

    public GcTrigger getTrigger() {
        return trigger;
    }

    public Memory getYoungOccupancyEnd() {
        return youngEnd;
    }

    public Memory getYoungOccupancyInit() {
        return young;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }
}
