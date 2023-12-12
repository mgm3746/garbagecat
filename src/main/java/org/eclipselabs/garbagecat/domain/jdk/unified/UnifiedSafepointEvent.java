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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;

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
 * 1) JDK81/11 on three lines (timestamp is beginning of safepoint):
 * </p>
 * 
 * <pre>
 * [2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: CollectForMetadataAllocation
 * [2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Leaving safepoint region
 * [2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time for which application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds
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
 * 2) JDK17 on a single line (timestamp is end of safepoint):
 * </p>
 * 
 * <pre>
 * [0.062s][info][safepoint   ] Safepoint "G1CollectForAllocation", Time since last: 22756680 ns, Reaching safepoint: 19114 ns, At safepoint: 910407 ns, Total: 929521 ns
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSafepointEvent implements SafepointEvent, UnifiedLogging {

    /**
     * Regular expressions defining the JDK8/11 logging.
     */
    public static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Entering safepoint region: "
            + UnifiedSafepoint.triggerRegEx() + UnifiedRegEx.DECORATOR + " Leaving safepoint region"
            + UnifiedRegEx.DECORATOR
            + " Total time for which application threads were stopped: (\\d{1,}[\\.\\,]\\d{7}) seconds, "
            + "Stopping threads took: (\\d{1,}[\\.\\,]\\d{7}) seconds[ ]*$";
    /**
     * Regular expressions defining the JDK17+ logging.
     */
    public static final String REGEX_JDK17 = "^" + UnifiedRegEx.DECORATOR + " Safepoint \""
            + UnifiedSafepoint.triggerRegEx()
            + "\", Time since last: \\d{1,} ns, Reaching safepoint: (\\d{1,}) ns(, Cleanup: (\\d{1,}) ns)?, "
            + "At safepoint: (\\d{1,}) ns, Total: \\d{1,} ns[ ]*$";
    /**
     * RegEx pattern.
     */
    private static final Pattern REGEX_JDK17_PATTERN = Pattern.compile(REGEX_JDK17);
    /**
     * RegEx pattern.
     */
    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PATTERN.matcher(logLine).matches() || REGEX_JDK17_PATTERN.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time spent on internal VM cleanup activities.
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
        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
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
        } else if ((matcher = REGEX_JDK17_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                trigger = UnifiedSafepoint.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
                timeToStopThreads = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2));
                if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3) != null) {
                    timeCleanup = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4));
                }
                timeThreadsStopped = Long.parseLong(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5));
                long time = UnifiedUtil.calculateTime(matcher);
                timestamp = time - JdkMath.convertNanosToMillis(getDurationNanos()).longValue();
            }
        }
    }

    /**
     * Alternate constructor. Create safepoint event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the safepoint event started in milliseconds after JVM startup.
     * @param timeToStopThreads
     *            The elapsed clock time to stop all threads (bring the JVM to safepoint) in nanoseconds (rounded).
     * @param timeThreadsStopped
     *            The elapsed clock time the application threads were stopped (at safepoint) in nanoseconds (rounded).
     * @param timeCleanup
     *            The elapsed clock time for VM internal cleanup activities (at safepoint) in nanoseconds (rounded).
     */
    public UnifiedSafepointEvent(String logEntry, long timestamp, long timeToStopThreads, long timeThreadsStopped,
            long timeCleanup) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.timeToStopThreads = timeToStopThreads;
        this.timeThreadsStopped = timeThreadsStopped;
        this.timeCleanup = timeCleanup;
    }

    /**
     * The elapsed clock time for the safepoint event in nanoseconds (rounded). timeToStopThreads seems to be time in
     * addition to timeThreadsStopped.
     */
    public long getDuration() {
        return timeThreadsStopped + timeToStopThreads;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString();
    }

    public long getDurationMicros() {
        return (JdkMath.convertNanosToMicros(timeThreadsStopped + timeToStopThreads)).longValue();
    }

    public long getDurationNanos() {
        return timeThreadsStopped + timeToStopThreads;
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
        return REGEX_JDK17_PATTERN.matcher(logEntry).matches();
    }

}
