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

import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.Safepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * SAFEPOINT
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent} with unified logging (JDK9+).
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
public class SafepointEvent implements UnifiedLogging {

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the safepoint event in microseconds (rounded).
     */
    private int duration;

    /**
     * The time when the safepoint event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The trigger for the safepoint event.
     */
    private String trigger;

    /**
     * Regular expressions defining the logging.
     */
    public static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Entering safepoint region: "
            + Safepoint.triggerRegEx() + UnifiedRegEx.DECORATOR + " Leaving safepoint region" + UnifiedRegEx.DECORATOR
            + " Total time for which application threads were stopped: (\\d{1,4}[\\.\\,]\\d{7}) seconds, "
            + "Stopping threads took: (\\d{1,4}[\\.\\,]\\d{7}) seconds[ ]*$";
    /**
     * RegEx pattern.
     */
    private static Pattern pattern = Pattern.compile(REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public SafepointEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            trigger = matcher.group(24);
            if (matcher.group(48).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(59));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(58)).longValue();
            } else {
                if (matcher.group(61) != null) {
                    if (matcher.group(61).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(63));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(62)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(48));
                }
            }
            duration = JdkMath.convertSecsToMicros(matcher.group(71)).intValue()
                    + JdkMath.convertSecsToMicros(matcher.group(72)).intValue();
        }
    }

    /**
     * Alternate constructor. Create safepoint event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the safepoint event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the safepoint event in microseconds (rounded).
     */
    public SafepointEvent(String logEntry, long timestamp, int duration) {
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

    public String getName() {
        return JdkUtil.LogEventType.SAFEPOINT.toString();
    }

    public long getTimestamp() {
        return timestamp;
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
