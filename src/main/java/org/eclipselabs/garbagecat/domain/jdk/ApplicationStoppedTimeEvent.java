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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * APPLICATION_STOPPED_TIME
 * </p>
 * 
 * <p>
 * Logging enabled with the <code>-XX:+PrintGCApplicationStoppedTime</code> JVM option. It shows the time spent in a
 * safepoint, when all threads are stopped and reachable by the JVM. Many JVM operations require that all threads be in
 * a safepoint to execute. The most common is a "stop the world" garbage collection.
 * </p>
 * 
 * <p>
 * This option used to only include garbage collection time, and it used to not be accurate. Therefore it was ignored.
 * However, beginning in JDK7/8, it started including stopped time for the other JVM operations performed at safepoint.
 * Therefore, it is now a required logging option to determine overall throughput and identify throughput and pause
 * issues not related to garbage collection.
 * </p>
 * 
 * <p>
 * Other JVM operations that require a safepoint:
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
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * Total time for which application threads were stopped: 0.0968457 seconds
 * </pre>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Prior to JDK8 update 40:
 * </p>
 * 
 * <pre>
 * Total time for which application threads were stopped: 0.0968457 seconds
 * </pre>
 * 
 * <p>
 * 2) JDK8 update 40 with the time (out of the total stopped time) waiting for the threads to arrive at a safepoint (
 * "Stopping threads took"):
 * </p>
 * 
 * <pre>
 * 0.147: Total time for which application threads were stopped: 0.0000921 seconds, Stopping threads took: 0.0000190 seconds
 * </pre>
 * 
 * <p>
 * 3) With negative stopped time. A bug?
 * </p>
 * 
 * <pre>
 * 51185.692: Total time for which application threads were stopped: -0.0005950 seconds, Stopping threads took: 0.0003310 seconds
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ApplicationStoppedTimeEvent implements SafepointEvent {

    /**
     * Regular expressions defining the logging.
     */
    public static final String _REGEX = "^(" + JdkRegEx.DECORATOR + " )?Total time for which application threads "
            + "were stopped: ((-)?\\d{1,4}[\\.\\,]\\d{7}) seconds(, Stopping threads took: "
            + "((-)?\\d{1,4}[\\.\\,]\\d{7}) seconds)?[ ]{0,}$";

    /**
     * RegEx pattern.
     */
    private static Pattern PATTERN = Pattern.compile(_REGEX);

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
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;
    /**
     * The elapsed clock time the application threads were stopped (at safepoint) in microseconds (rounded).
     */
    private long timeThreadsStopped;

    /**
     * The elapsed clock time to stop all threads (bring the JVM to safepoint) in microseconds (rounded).
     */
    private long timeToStopThreads;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ApplicationStoppedTimeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            long endTimestamp = 0;
            if (matcher.group(14) != null && matcher.group(14).matches(JdkRegEx.TIMESTAMP)) {
                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
            } else if (matcher.group(2) != null) {
                if (matcher.group(2).matches(JdkRegEx.TIMESTAMP)) {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(2)).longValue();
                } else {
                    // Datestamp only.
                    endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            }
            timeThreadsStopped = JdkMath.convertSecsToMicros(matcher.group(15)).intValue();
            if (matcher.group(18) != null) {
                timeToStopThreads = JdkMath.convertSecsToMicros(matcher.group(18)).intValue();
            }
            if (endTimestamp > 0) {
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(getDurationMicros()).longValue();
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
     *            The elapsed clock time to stop all threads (bring the JVM to safepoint) in microseconds (rounded).
     * @param timeThreadsStopped
     *            The elapsed clock time the application threads were stopped (at safepoint) in microseconds (rounded).
     */
    public ApplicationStoppedTimeEvent(String logEntry, long timestamp, long timeToStopThreads,
            long timeThreadsStopped) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.timeToStopThreads = timeToStopThreads;
        this.timeThreadsStopped = timeThreadsStopped;
    }

    /**
     * The elapsed clock time for the safepoint event in microseconds (rounded). timeToStopThreads seems to be a subset
     * of timeThreadsStopped.
     */
    public long getDurationMicros() {
        return timeThreadsStopped;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.APPLICATION_STOPPED_TIME;
    }

    public String getLogEntry() {
        return logEntry;
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
}
