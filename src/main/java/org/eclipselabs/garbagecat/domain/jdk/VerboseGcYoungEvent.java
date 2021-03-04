/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.Memory;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * VERBOSE_GC_YOUNG
 * </p>
 * 
 * <p>
 * Young collection when <code>-verbose:gc</code> used without <code>-XX:+PrintGCDetails</code>. It is not possible to
 * determine the collector from the logging pattern, but it can be determined from
 * {@link org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent} output.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 2205570.508: [GC 1726387K-&gt;773247K(3097984K), 0.2318035 secs]
 * </pre>
 * 
 * <p>
 * 2) Missing beginning occupancy:
 * </p>
 * 
 * <pre>
 * 90.168: [GC 876593K(1851392K), 0.0701780 secs]
 * </pre>
 * 
 * <p>
 * 3) With datestamp:
 * </p>
 * 
 * <pre>
 * 2016-07-22T11:49:00.678+0100: 4.970: [GC (Allocation Failure)  136320K-&gt;18558K(3128704K), 0.1028162 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class VerboseGcYoungEvent extends UnknownCollector
        implements BlockingEvent, YoungCollection, CombinedData, TriggerData {

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
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedBegin;

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedEnd;

    /**
     * Combined young + old generation allocation.
     */
    private Memory combinedAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_CMS_INITIAL_MARK + "|" + JdkRegEx.TRIGGER_CMS_FINAL_REMARK + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_SYSTEM_GC + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[GC( \\("
            + TRIGGER + "\\) )?(--)? (" + JdkRegEx.SIZE_K + "->)?" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), "
            + JdkRegEx.DURATION + "\\]?[ ]*$";

    private static Pattern pattern = Pattern.compile(VerboseGcYoungEvent.REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public VerboseGcYoungEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            trigger = matcher.group(14);
            if (matcher.group(17) != null) {
                combinedBegin = Memory.kilobytes(matcher.group(18));
            } else {
                // set it to the end
                combinedBegin = Memory.kilobytes(matcher.group(19));
            }
            combinedEnd = Memory.kilobytes(matcher.group(19));
            combinedAllocation = Memory.kilobytes(matcher.group(20));
            duration = JdkMath.convertSecsToMicros(matcher.group(21)).intValue();
        }
    }

    /**
     * Alternate constructor. Create logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public VerboseGcYoungEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getName() {
        return JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString();
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

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public String getTrigger() {
        return trigger;
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
