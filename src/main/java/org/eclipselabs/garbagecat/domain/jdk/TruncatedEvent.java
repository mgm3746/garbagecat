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
 * TRUNCATED
 * </p>
 * 
 * <p>
 * A garbage collection event where the logging has been truncated for some reason. It could be an indication the JVM is
 * under stress.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) The beginning of a {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} or
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent}.
 * </p>
 * 
 * <pre>
 * 100.714: [Full GC 100.714: [CMS
 * </pre>
 * 
 * <p>
 * 2) The beginning of a {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} followed by a
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent}.
 * </p>
 * 
 * <pre>
 * 9641.622: [GC 9641.622: [ParNew9641.696: [CMS-concurrent-abortable-preclean: 0.029/0.129 secs] [Times: user=0.25 sys=0.07, real=0.13 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TruncatedEvent implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            /*
             * The beginning of a {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} or {@link
             * org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent}.
             */
            "^" + JdkRegEx.TIMESTAMP + ": \\[Full GC " + JdkRegEx.TIMESTAMP + ": \\[CMS$",
            /*
             * The beginning of a {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} followed by a {@link
             * org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent}.
             */
            "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP + ": \\[ParNew" + JdkRegEx.TIMESTAMP
                    + ": \\[CMS-concurrent-abortable-preclean: " + JdkRegEx.DURATION_FRACTION + "\\]"
                    + JdkRegEx.TIMES_BLOCK + "?[ ]*$" };
    private static Pattern pattern = Pattern.compile("^" + JdkRegEx.TIMESTAMP + ".*$");

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create ParNew detail logging event from log entry.
     */
    public TruncatedEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.TRUNCATED.toString();
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
        boolean isMatch = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }

}
