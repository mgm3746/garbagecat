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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * DATE_STAMP_PREFIX
 * </p>
 * 
 * <p>
 * Logging with a datestamp prefixing the normal timestamp indicating the number of seconds after JVM startup. Enabled
 * with the <code>-XX:+PrintGCDateStamps</code> option added in JDK 1.6 update 4.
 * </p>
 * 
 * <p>
 * It appears that initial implementations replace the timestamp with a datestamp and later versions of the JDK prefix
 * the normal timestamp with a datestamp.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K-&gt;858K(341376K), 0.0030008 secs] 273152K-&gt;858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 84.335: [GC 84.336: [ParNew: 273152K-&gt;858K(341376K), 0.0030008 secs] 273152K-&gt;858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class DateStampPrefixPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the logging line.
     */
    private static final String REGEX_LINE = "^" + JdkRegEx.DATESTAMP + ": (" + JdkRegEx.TIMESTAMP + ": (.*))$";
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
     */
    public DateStampPrefixPreprocessAction(String logEntry) {
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            String logEntryMinusDateStamp = matcher.group(11);
            this.logEntry = logEntryMinusDateStamp;
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.DATE_STAMP_PREFIX.toString();
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
