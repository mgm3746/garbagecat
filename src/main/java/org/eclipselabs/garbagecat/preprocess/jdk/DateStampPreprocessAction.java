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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * DATE_STAMP
 * </p>
 * 
 * <p>
 * Logging with a datestamp instead of the timestamp indicating the number of seconds after JVM startup. Enabled with
 * the <code>-XX:+PrintGCDateStamps</code> option added in JDK 1.6 update 4.
 * </p>
 * 
 * <p>
 * It appears that initial implementations replace the timestamp with a datestamp and later versions of the JDK prefix
 * the normal timestamp with a datestamp.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 2010-02-26T09:32:12.486-0600: [GC [ParNew: 150784K-&gt;3817K(169600K), 0.0328800 secs] 150784K-&gt;3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 142.973: [GC [ParNew: 150784K-&gt;3817K(169600K), 0.0328800 secs] 150784K-&gt;3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * <p>
 * 2) Without a colon after the date:
 * </p>
 * 
 * <pre>
 * 2016-02-11T18:50:24.070-0500 16705.217: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 273946, predicted base time: 242.44 ms, remaining time: 257.56 ms, target pause time: 500.00 ms]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 16705.217: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 273946, predicted base time: 242.44 ms, remaining time: 257.56 ms, target pause time: 500.00 ms]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class DateStampPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the logging line.
     */
    private static final String REGEX_LINE = "^" + JdkRegEx.DATESTAMP + "(:)? (.*)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX_LINE);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     */
    public DateStampPreprocessAction(String logEntry, Date jvmStartDate) {
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            String logEntryMinusDateStamp = matcher.group(12);
            Date datestamp = GcUtil.parseDateStamp(matcher.group(1));
            long diff = GcUtil.dateDiff(jvmStartDate, datestamp);
            if (diff < 0) {
                throw new TimeWarpException(
                        "JVM start date (" + jvmStartDate + ") is after logging datestamp (" + datestamp + ")");
            }
            this.logEntry = JdkMath.convertMillisToSecs(diff) + ": " + logEntryMinusDateStamp;
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.DATE_STAMP.toString();
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
