/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_CONCURRENT
 * </p>
 * 
 * <p>
 * Any number of events that happen concurrently with the JVM's execution of application threads. These events are not
 * included in the GC analysis since there is no application pause time.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 251.781: [GC concurrent-root-region-scan-start]
 * </pre>
 * 
 * <pre>
 * 252.707: [GC concurrent-root-region-scan-end, 0.0769810]
 * </pre>
 * 
 * <pre>
 * 252.707: [GC concurrent-mark-start]
 * </pre>
 * 
 * <pre>
 * 252.888: [GC concurrent-mark-end, 0.6282750 sec]
 * </pre>
 * 
 * <pre>
 * 253.102: [GC concurrent-cleanup-start]
 * </pre>
 * 
 * <pre>
 * 253.189: [GC concurrent-cleanup-end, 0.0001200]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1ConcurrentEvent implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[GC concurrent.+$";
    private static final Pattern pattern = Pattern.compile(REGEX);
    
    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;


    /**
     * Create detail logging event from log entry.
     */
    public G1ConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;

        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_CONCURRENT.toString();
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
        return logLine.matches(REGEX);
    }

}
