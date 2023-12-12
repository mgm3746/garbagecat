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
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
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
 * <h2>Example Logging</h2>
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
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.CMS_INITIAL_MARK.getRegex() + "|" + GcTrigger.CMS_FINAL_REMARK.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC( \\(" + __TRIGGER + "\\) )?(--)? ("
            + JdkRegEx.SIZE_K + "->)?" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION
            + "\\]?[ ]*$";

    private static Pattern pattern = Pattern.compile(_REGEX);

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
     * Combined young + old generation allocation.
     */
    private Memory combinedAllocation;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedBegin;

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedEnd;

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
     * The trigger for the GC event.
     */
    private GcTrigger trigger;

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
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
            }
            trigger = GcTrigger.getTrigger(matcher.group(15));
            if (matcher.group(18) != null) {
                combinedBegin = kilobytes(matcher.group(19));
            } else {
                // set it to the end
                combinedBegin = kilobytes(matcher.group(20));
            }
            combinedEnd = kilobytes(matcher.group(20));
            combinedAllocation = kilobytes(matcher.group(21));
            duration = JdkMath.convertSecsToMicros(matcher.group(22)).intValue();
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

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public long getDurationMicros() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }
}
