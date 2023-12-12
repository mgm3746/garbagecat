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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * GC_LOCKER_RETRY
 * </p>
 * 
 * <p>
 * The GCLocker is used to prevent GC while JNI (native) code is running in a "critical region."
 * </p>
 * 
 * <p>
 * Certain JNI function pairs are classified as "critical" because a Java object is passed into the JNI code. While the
 * code is running inside the "critical region" it is necessary to prevent GC from happening to prevent compaction from
 * changing the Java object memory address (and cause a crash due to the JNI code accessing a bad memory address).
 * </p>
 * 
 * <p>
 * For example, any code running between <code>GetPrimitiveArrayCritical</code> and
 * <code>ReleasePrimitiveArrayCritical</code> is a "critical region".
 * </p>
 * 
 * <p>
 * If a thread cannot find a free area of heap large enough to allocate an object, it triggers a GC. If another thread
 * is running JNI code in a "critical region", the GC cannot happen, so the the JVM requests a "GCLocker Initiated GC"
 * and waits.
 * </p>
 * 
 * <p>
 * The expectation is that the GCLocker will not be held for a long time (there will not be long running code in a
 * "critical region"). Also, native code must not call other JNI functions inside a critical region.
 * </p>
 * 
 * <p>
 * When the GCLocker is released, there will be a minor GC, and the thread trying to allocate the object will get
 * rescheduled and retry the memory allocation.
 * </p>
 * 
 * <p>
 * If the memory allocation fails again (either GC does not free up enough memory, or other threads allocate the freed
 * memory), this event will get logged, and the JVM will request a second "GCLocker Initiated GC" and wait.
 * </p>
 * 
 * <p>
 * If the allocation fails again, OutOfMemoryError is thrown.
 * </p>
 * 
 * <p>
 * The default 2 tries before throwing OutOfMemoryError can be changed with the following diagnostic option:
 * </p>
 * 
 * <pre>
 * -XX:+UnlockDiagnosticVMOptions -XX:GCLockerRetryAllocationCount=N
 * </pre>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [2023-02-12T07:16:14.167+0200][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried waiting for GCLocker too often allocating 1235 words
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcLockerRetryEvent extends G1Collector implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    public static final String _REGEX = "^" + UnifiedRegEx.DECORATOR
            + ".+Retried waiting for GCLocker too often allocating \\d{1,} words[ ]*$";

    private static final Pattern pattern = Pattern.compile(_REGEX);

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
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public GcLockerRetryEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
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
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_LOCKER_RETRY.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
