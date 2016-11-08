/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * GC_LOCKER
 * </p>
 * 
 * <p>
 * After a JNI critical section was exited the GC wanted tried to do a young collection, but it failed due to the full
 * promotion guarantee. This will cause the serial collector to be invoked.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * GC locker: Trying a full collection because scavenge failed
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcLockerEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^GC locker: Trying a full collection because scavenge failed$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public GcLockerEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_LOCKER.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

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
}
