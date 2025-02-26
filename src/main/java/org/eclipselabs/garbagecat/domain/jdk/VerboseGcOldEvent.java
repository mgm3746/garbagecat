/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * VERBOSE_GC_OLD
 * </p>
 * 
 * <p>
 * Full collection when <code>-verbose:gc</code> used without <code>-XX:+PrintGCDetails</code>. It is not possible to
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
 * 2143132.151: [Full GC 1606823K-&gt;1409859K(2976064K), 12.0855599 secs]
 * </pre>
 *
 * <p>
 * 2) with -XX:+UseG1GC (G1 sizes):
 * </p>
 * 
 * <pre>
 * 2017-03-20T04:30:01.936+0800: 2950.666: [Full GC 8134M-&gt;2349M(8192M), 10.3726320 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class VerboseGcOldEvent extends UnknownCollector
        implements BlockingEvent, OldCollection, CombinedData, TriggerData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[Full GC( \\(" + __TRIGGER + "\\) )? "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]?[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
    }

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedOccupancyEnd;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedOccupancyInit;

    /**
     * Combined young + old generation allocation.
     */
    private Memory combinedSpace;

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
    public VerboseGcOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
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
            combinedOccupancyInit = memory(matcher.group(17), matcher.group(19).charAt(0)).convertTo(KILOBYTES);
            combinedOccupancyEnd = memory(matcher.group(20), matcher.group(22).charAt(0)).convertTo(KILOBYTES);
            combinedSpace = memory(matcher.group(23), matcher.group(25).charAt(0)).convertTo(KILOBYTES);
            duration = JdkMath.convertSecsToMicros(matcher.group(26)).intValue();
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
    public VerboseGcOldEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedOccupancyEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedOccupancyInit;
    }

    public Memory getCombinedSpace() {
        return combinedSpace;
    }

    public long getDurationMicros() {
        return duration;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.VERBOSE_GC_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }
}
