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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * UNIFIED_YOUNG
 * </p>
 * 
 * <p>
 * Young collection JDK9+ with <code>-Xlog:gc:file=&lt;file&gt;</code> (no details).
 * </p>
 * 
 * <p>
 * Collector is one of the following: (1) {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent}, (2)
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent}, (3)
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent}.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK8/11:
 * </p>
 * 
 * <pre>
 * [0.053s][info][gc] GC(0) Pause Young (Allocation Failure) 0M-&gt;0M(1M) 0.914ms
 * </pre>
 * 
 * <p>
 * 2) JDK17:
 * </p>
 * 
 * <pre>
 * [0.070s][info][gc,start    ] GC(2) Pause Young (Allocation Failure) 1M-&gt;1M(2M) 0.663ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedYoungEvent extends UnknownCollector
        implements UnifiedLogging, BlockingEvent, YoungCollection, CombinedData, TriggerData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Young \\(" + __TRIGGER + "\\) "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + TimesData.REGEX_JDK9 + "?[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
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
    private long eventTime;

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
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedYoungEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5).charAt(0)).convertTo(KILOBYTES);
            combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8).charAt(0)).convertTo(KILOBYTES);
            combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
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
    public UnifiedYoungEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
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
        return eventTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_YOUNG.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }

    public boolean isEndstamp() {
        // default assumes gc,start not logged (e.g. not preprocessed)
        boolean isEndStamp = true;
        isEndStamp = !logEntry.matches(UnifiedRegEx.TAG_GC_START);
        return isEndStamp;
    }
}
