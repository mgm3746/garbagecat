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
import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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

    private static Pattern pattern = Pattern.compile(VerboseGcOldEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[Full GC( \\(" + VerboseGcOldEvent.TRIGGER
            + "\\) )? (" + JdkRegEx.SIZE_K + "|" + JdkRegEx.SIZE + ")->(" + JdkRegEx.SIZE_K + "|" + JdkRegEx.SIZE
            + ")\\((" + JdkRegEx.SIZE_K + "|" + JdkRegEx.SIZE + ")\\), " + JdkRegEx.DURATION + "\\]?[ ]*$";

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_ERGONOMICS + "|" + JdkRegEx.TRIGGER_SYSTEM_GC + ")";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
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
    private String trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public VerboseGcOldEvent(String logEntry) {
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
            trigger = matcher.group(15);
            if (matcher.group(17).matches(JdkRegEx.SIZE_K)) {
                combinedBegin = kilobytes(matcher.group(18));
            } else {
                combinedBegin = memory(matcher.group(19), matcher.group(21).charAt(0)).convertTo(KILOBYTES);
            }
            if (matcher.group(22).matches(JdkRegEx.SIZE_K)) {
                combinedEnd = kilobytes(matcher.group(23));
            } else {
                combinedEnd = memory(matcher.group(24), matcher.group(26).charAt(0)).convertTo(KILOBYTES);
            }
            if (matcher.group(27).matches(JdkRegEx.SIZE_K)) {
                combinedAllocation = kilobytes(matcher.group(28));
            } else {
                combinedAllocation = memory(matcher.group(29), matcher.group(31).charAt(0)).convertTo(KILOBYTES);
            }
            duration = JdkMath.convertSecsToMicros(matcher.group(32)).intValue();
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
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public long getDuration() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.VERBOSE_GC_OLD.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTrigger() {
        return trigger;
    }
}
