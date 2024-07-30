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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * SAFEPOINT
 * </p>
 * 
 * <p>
 * Logging enabled with the <code>safepoint=info</code> unified logging option. It shows the time spent in a safepoint,
 * when all threads are stopped and reachable by the JVM. Many JVM operations require that all threads be in a safepoint
 * to execute. The most common is a "stop the world" garbage collection.
 * </p>
 * 
 * <p>
 * A required logging option to determine overall throughput and identify throughput and pause issues not related to
 * garbage collection.
 * </p>
 * 
 * <p>
 * Examples of other JVM operations besides gc that require a safepoint:
 * </p>
 * <ul>
 * <li>ThreadDump</li>
 * <li>HeapDumper</li>
 * <li>GetAllStackTrace</li>
 * <li>PrintThreads</li>
 * <li>PrintJNI</li>
 * <li>RevokeBias</li>
 * <li>Deoptimization</li>
 * <li>FindDeadlock</li>
 * <li>EnableBiasLocking</li>
 * </ul>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK11 on three lines that wrap the garbage collection ("Entering" timestamp is beginning of safepoint):
 * </p>
 * 
 * <pre>
 * [2021-09-14T11:38:33.217-0500][3.874s][info][safepoint    ] Entering safepoint region: CollectForMetadataAllocation
 * [2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young (Concurrent Start) (Metadata GC Threshold)
 * [2021-09-14T11:38:33.218-0500][3.875s][info][gc,task      ] GC(0) Using 8 workers of 8 for evacuation
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 7.6ms
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 1.4ms
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,phases    ] GC(0)   Other: 0.9ms
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,heap      ] GC(0) Eden regions: 14-&gt;0(100)
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,heap      ] GC(0) Survivor regions: 0-&gt;2(13)
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,heap      ] GC(0) Old regions: 0-&gt;0
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,heap      ] GC(0) Humongous regions: 0-&gt;0
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,metaspace ] GC(0) Metaspace: 20058K-&gt;20058K(1069056K)
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc           ] GC(0) Pause Young (Concurrent Start) (Metadata GC Threshold) 56M-&gt;7M(8192M) 10.037ms
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,cpu       ] GC(0) User=0.04s Sys=0.00s Real=0.01s
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc           ] GC(1) Concurrent Cycle
 * [2021-09-14T11:38:33.227-0500][3.884s][info][gc,marking   ] GC(1) Concurrent Clear Claimed Marks
 * [2021-09-14T11:38:33.228-0500][3.884s][info][gc,marking   ] GC(1) Concurrent Clear Claimed Marks 0.020ms
 * [2021-09-14T11:38:33.228-0500][3.884s][info][gc,marking   ] GC(1) Concurrent Scan Root Regions
 * [2021-09-14T11:38:33.228-0500][3.884s][info][safepoint    ] Leaving safepoint region
 * [2021-09-14T11:38:33.228-0500][3.884s][info][safepoint    ] Total time for which application threads were stopped: 0.0104763 seconds, Stopping threads took: 0.0000101 seconds
 * </pre>
 * 
 * <p>
 * Preprocessed into a single line:
 * </p>
 * 
 * <pre>
 * [2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Leaving safepoint region[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds
 * </pre>
 * 
 * <p>
 * 2) JDK17 on a single line (timestamp is end of safepoint) that comes after garbage collection:
 * </p>
 * 
 * <pre>
 * [0.067s][info][gc,start    ] GC(3) Pause Young (Normal) (G1 Evacuation Pause)
 * [0.067s][info][gc,task     ] GC(3) Using 2 workers of 10 for evacuation
 * [0.068s][info][gc,phases   ] GC(3)   Pre Evacuate Collection Set: 0.0ms
 * [0.068s][info][gc,phases   ] GC(3)   Merge Heap Roots: 0.0ms
 * [0.068s][info][gc,phases   ] GC(3)   Evacuate Collection Set: 0.5ms
 * [0.068s][info][gc,phases   ] GC(3)   Post Evacuate Collection Set: 0.1ms
 * [0.068s][info][gc,phases   ] GC(3)   Other: 0.0ms
 * [0.068s][info][gc,heap     ] GC(3) Eden regions: 1-&gt;0(1)
 * [0.068s][info][gc,heap     ] GC(3) Survivor regions: 1-&gt;1(1)
 * [0.068s][info][gc,heap     ] GC(3) Old regions: 0-&gt;1
 * [0.068s][info][gc,heap     ] GC(3) Archive regions: 2-&gt;2
 * [0.068s][info][gc,heap     ] GC(3) Humongous regions: 0-&gt;0
 * [0.068s][info][gc,metaspace] GC(3) Metaspace: 1071K(1280K)-&gt;1071K(1280K) NonClass: 981K(1088K)-&gt;981K(1088K) Class: 89K(192K)-&gt;89K(192K)
 * [0.068s][info][gc          ] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 2M-&gt;2M(7M) 0.681ms
 * [0.068s][info][gc,cpu      ] GC(3) User=0.00s Sys=0.00s Real=0.00s
 * [0.068s][info][safepoint   ] Safepoint "G1CollectForAllocation", Time since last: 3273659 ns, Reaching safepoint: 12838 ns, At safepoint: 704423 ns, Total: 717261 ns
 * </pre>
 * 
 * 3) JDK17 &gt;= update 8 preprocessed to include "17U8" context. Prior to JDK17u8, {@link #timeCleanup} was included
 * in {@link #timeToStopThreads} ("Reaching safepoint"), so it is necessary to know the JDK version to accurately
 * determine the total time in safepoint. Reference: https://bugs.openjdk.org/browse/JDK-8297154
 * 
 * <pre>
 * [2023-12-12T10:21:02.708+0200][info][safepoint   ] 17U8 Safepoint "Cleanup", Time since last: 1000407638 ns, Reaching safepoint: 18298588 ns, Cleanup: 9032 ns, At safepoint: 461108 ns, Total: 18768728 ns
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSafepointEvent implements SafepointEvent, UnifiedLogging {

    /**
     * Regular expressions defining the JDK8/11 logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Entering safepoint region: "
            + UnifiedSafepoint.triggerRegEx() + UnifiedRegEx.DECORATOR + " Leaving safepoint region"
            + UnifiedRegEx.DECORATOR
            + " Total time for which application threads were stopped: (\\d{1,}[\\.\\,]\\d{7}) seconds, "
            + "Stopping threads took: (\\d{1,}[\\.\\,]\\d{7}) seconds[ ]*$";
    /**
     * Regular expressions defining the JDK17 &lt; update 8 logging. Logging included in {@link #timeCleanup} in
     * {@link #timeToStopThreads}.
     * 
     * Reference: https://bugs.openjdk.org/browse/JDK-8297154
     * 
     * [1.708s] JDK17U8 Safepoint \"G1CollectForAllocation\", Time since last: 11990384 ns, " + "Reaching safepoint:
     * 2496 ns, Cleanup: 11042 ns, At safepoint: 623787 ns, Total: 637325 ns
     */
    private static final String _REGEX_JDK17 = "^" + UnifiedRegEx.DECORATOR + "( (" + UnifiedPreprocessAction.JDK17U8
            + " )?Safepoint \"" + UnifiedSafepoint.triggerRegEx()
            + "\", Time since last: \\d{1,} ns, Reaching safepoint: (\\d{1,}) ns(, Cleanup: (\\d{1,}) ns)?, "
            + "At safepoint: (\\d{1,}) ns, Total: \\d{1,} ns)[ ]*$";

    /**
     * RegEx pattern for JDK8 and JDK11.
     */
    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * RegEx pattern for JDK17+ (output on a single line).
     */
    public static final Pattern PATTERN_JDK17 = Pattern.compile(_REGEX_JDK17);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches() || PATTERN_JDK17.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time spent on internal VM cleanup activities.
     * 
     * JDK17 logging prior to update 8 included this time in {@link #timeToStopThreads} ("Reaching safepoint").
     * Reference: https://bugs.openjdk.org/browse/JDK-8297154
     */
    private long timeCleanup;

    /**
     * The time when the safepoint event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The elapsed clock time the application threads were stopped (at safepont) in nanoseconds (rounded).
     */
    private long timeThreadsStopped;

    /**
     * The elapsed clock time to stop all threads (bring the JVM to safepoint) in nanoseconds (rounded).
     */
    private long timeToStopThreads;

    /**
     * The <code>Trigger</code> for the safepoint event.
     */
    private Trigger trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedSafepointEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher;
        if ((matcher = PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                trigger = UnifiedSafepoint.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
                // Has a true timestamp (it outputs the beginning logging before the safepoint).
                if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    timestamp = Long.parseLong(matcher.group(13));
                } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                } else {
                    if (matcher.group(15) != null) {
                        if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            timestamp = Long.parseLong(matcher.group(17));
                        } else {
                            timestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                        }
                    } else {
                        // Datestamp only.
                        timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                    }
                }
                timeThreadsStopped = JdkMath.convertSecsToNanos(matcher.group(3 * UnifiedRegEx.DECORATOR_SIZE + 2))
                        .longValue();
                timeToStopThreads = JdkMath.convertSecsToNanos(matcher.group(3 * UnifiedRegEx.DECORATOR_SIZE + 3))
                        .longValue();
            }
        } else if ((matcher = PATTERN_JDK17.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                trigger = UnifiedSafepoint.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3));
                timeToStopThreads = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4));
                if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5) != null) {
                    timeCleanup = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6));
                }
                timeThreadsStopped = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7));
                long time = UnifiedUtil.calculateTime(matcher);
                timestamp = time - JdkMath.convertNanosToMillis(getDurationNanos()).longValue();
            }
        }
    }

    public long getDurationMicros() {
        return JdkMath.convertNanosToMicros(getDurationNanos()).longValue();
    }

    /**
     * The safepoint duration in nanoseconds. JDK17 logging prior to update 8 included {@link #timeCleanup} in
     * {@link #timeToStopThreads} ("Reaching safepoint"). Reference: https://bugs.openjdk.org/browse/JDK-8297154
     * 
     * @return The safepoint duration in nanoseconds.
     */
    public long getDurationNanos() {
        long durationNanos;
        Matcher matcher = PATTERN_JDK17.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2) != null) {
                durationNanos = timeThreadsStopped + timeToStopThreads + timeCleanup;
            } else {
                durationNanos = timeThreadsStopped + timeToStopThreads;
            }
        } else {
            durationNanos = timeThreadsStopped + timeToStopThreads;
        }
        return durationNanos;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimeCleanup() {
        return timeCleanup;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimeThreadsStopped() {
        return timeThreadsStopped;
    }

    public long getTimeToStopThreads() {
        return timeToStopThreads;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public boolean isEndstamp() {
        return PATTERN_JDK17.matcher(logEntry).matches();
    }

}
