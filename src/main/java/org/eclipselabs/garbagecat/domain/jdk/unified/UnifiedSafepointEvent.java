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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.SafepointEvent;
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
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Three lines:
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
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSafepointEvent implements SafepointEvent, UnifiedLogging {

    /**
     * Regular expressions defining the logging.
     */
    public static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Entering safepoint region: "
            + UnifiedSafepoint.triggerRegEx() + UnifiedRegEx.DECORATOR + " Leaving safepoint region"
            + UnifiedRegEx.DECORATOR
            + " Total time for which application threads were stopped: (\\d{1,4}[\\.\\,]\\d{7}) seconds, "
            + "Stopping threads took: (\\d{1,4}[\\.\\,]\\d{7}) seconds[ ]*$";

    /**
     * RegEx pattern.
     */
    private static Pattern pattern = Pattern.compile(REGEX);

    public static Pattern getPattern() {
        return pattern;
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

    public static void setPattern(Pattern pattern) {
        UnifiedSafepointEvent.pattern = pattern;
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the safepoint event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The elapsed clock time the application threads were stopped (at safepont) in microseconds (rounded).
     */
    private int timeThreadsStopped;

    /**
     * The elapsed clock time to stop all threads (bring the JVM to safepoint) in microseconds (rounded).
     */
    private int timeToStopThreads;

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
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            trigger = UnifiedSafepoint.getTrigger(matcher.group(24));
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
                    timestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            timeThreadsStopped = JdkMath.convertSecsToMicros(matcher.group(71)).intValue();
            timeToStopThreads = JdkMath.convertSecsToMicros(matcher.group(72)).intValue();
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
     *            The elapsed clock time to stop all threads (bring the JVM to safepoint) in microseconds (rounded).
     * @param timeToStopThreads
     *            The elapsed clock time the application threads were stopped (at safepont) in microseconds (rounded).
     */
    public UnifiedSafepointEvent(String logEntry, long timestamp, int timeToStopThreads, int timeThreadsStopped) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.timeToStopThreads = timeToStopThreads;
        this.timeThreadsStopped = timeThreadsStopped;
    }

    /**
     * The elapsed clock time for the safepoint event in microseconds (rounded). timeToStopThreads seems to be time in
     * addition to timeThreadsStopped.
     */
    public int getDuration() {
        return timeThreadsStopped + timeToStopThreads;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimeThreadsStopped() {
        return timeThreadsStopped;
    }

    public int getTimeToStopThreads() {
        return timeToStopThreads;
    }

    public Trigger getTrigger() {
        return trigger;
    }

}
