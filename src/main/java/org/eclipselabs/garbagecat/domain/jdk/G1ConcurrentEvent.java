/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
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
 * <p>
 * 1) Standard formats:
 * </p>
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
 * <pre>
 * 27768.373: [GC concurrent-root-region-scan-start]
 * </pre>
 * 
 * <pre>
 * 27768.671: [GC concurrent-root-region-scan-end, 0.2974990 secs]
 * </pre>
 * 
 * <p>
 * 2) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction}:
 * </p>
 * 
 * <pre>
 * 27744.494: [GC concurrent-mark-start], 0.3349320 secs] 10854M-&gt;9765M(26624M) [Times: user=0.98 sys=0.00, real=0.33 secs]
 * </pre>
 * 
 * <p>
 * 3) With datestamp and "secs" after duration:
 * </p>
 * 
 * <pre>
 * 2016-02-11T18:15:35.431-0500: 14974.501: [GC concurrent-cleanup-end, 0.0033880 secs]
 * </pre>
 * 
 * <p>
 * 4) With string deduplication and G1 detail sizes (to one decimal):
 * </p>
 * 
 * <pre>
 * 8.556: [GC concurrent-string-deduplication, 906.5K-&gt;410.2K(496.3K), avg 54.8%, 0.0162924 secs]
 * </pre>
 * 
 * <p>
 * 5) Double timestamp:
 * </p>
 * 
 * <pre>
 * 23743.632: 23743.632: [GC concurrent-root-region-scan-start]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1ConcurrentEvent extends G1Collector implements LogEvent, ParallelEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(: )?(" + JdkRegEx.DATESTAMP + "(: )?)?(" + JdkRegEx.DATESTAMP
            + "(: )?)?(: )?" + JdkRegEx.TIMESTAMP + "?(: )?(" + JdkRegEx.DATESTAMP + ": )?(: )?(" + JdkRegEx.TIMESTAMP
            + ": )?(: )?\\[GC concurrent-(((root-region-scan|mark|cleanup)-(start|end|abort|reset-for-overflow))"
            + "|string-deduplication)(\\])?(,)?( " + JdkRegEx.DURATION + ")?(\\])?( " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))?(, avg " + JdkRegEx.PERCENT + ", " + JdkRegEx.DURATION
            + "\\])?" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

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
    public G1ConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;

        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(27) != null) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(27)).longValue();
                }
            }
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
        return pattern.matcher(logLine).matches();
    }
}
