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

import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * USING_G1
 * 
 * TODO: Move to UnifiedHeaderEvent?
 * </p>
 * 
 * <p>
 * Initial line of JDK9+ logging indicating collector family.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) With <code>-Xlog:gc:file=&lt;file&gt;</code> (no details).
 * </p>
 * 
 * <pre>
 * [0.003s][info][gc] Using G1
 * </pre>
 * 
 * <p>
 * 2) With <code>-Xlog:gc*:file=&lt;file&gt;</code> (details).
 * </p>
 * 
 * <pre>
 * [0.003s][info][gc     ] Using G1
 * </pre>
 * 
 * <p>
 * 3) With datestamp, no gc, no logging level, ms (JDK11).
 * </p>
 * 
 * <pre>
 * [2019-05-09T01:38:55.426+0000][18ms] Using G1
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UsingG1Event extends G1Collector implements UnifiedLogging {
    private static Pattern pattern = Pattern.compile(UsingG1Event.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Using G1[ ]*$";

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
    public UsingG1Event(String logEntry) {
        this.logEntry = logEntry;

        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
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
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.USING_G1.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
