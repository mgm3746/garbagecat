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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
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
 * <p>
 * 1) Standard logging:
 * </p>
 * 
 * <pre>
 * 2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), 0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), 0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * <p>
 * 2) Logging with multiple datestamps:
 * </p>
 * 
 * <pre>
 * 2013-12-09T16:18:17.813+0000: 13.086: [GC2013-12-09T16:18:17.813+0000: 13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] 272640K->33532K(1014528K), 0.0383306 secs] [Times: user=0.11 sys=0.02, real=0.04 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 13.086: [GC 13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] 272640K->33532K(1014528K), 0.0383306 secs] [Times: user=0.11 sys=0.02, real=0.04 secs]
 * </pre>
 * 
 * <p>
 * 3) G1 Ergonomics logging:
 * </p>
 * 
 * <pre>
 * 2016-02-11T17:26:43.599-0500:  12042.669: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target pause time: 500.00 ms]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 12042.669: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target pause time: 500.00 ms]
 * </pre>
 * 
 * <p>
 * 3) Double datestamp:
 * </p>
 * 
 * <pre>
 * 2016-02-16T03:13:56.897-0500: 2016-02-16T03:13:56.897-0500: 23934.242: 23934.242: [GC concurrent-root-region-scan-start]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 23934.242: [GC concurrent-root-region-scan-start]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class DateStampPrefixPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the logging line.
     */
    private static final String REGEX_LINE = "^" + JdkRegEx.DATESTAMP + "(:)? ( )?(" + JdkRegEx.DATESTAMP + "(: )?)?(("
            + JdkRegEx.TIMESTAMP + ": )(:)?( )?)(" + JdkRegEx.TIMESTAMP + ": )?(" + JdkRegEx.DATESTAMP + ": )?(.*)$";

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
        Pattern p = Pattern.compile(JdkRegEx.DATESTAMP + "(:)? ( )?(" + JdkRegEx.DATESTAMP + "(: )?)?(("
                + JdkRegEx.TIMESTAMP + ": )(:)?( )?)(" + JdkRegEx.TIMESTAMP + ": )?(" + JdkRegEx.DATESTAMP + ": )?");
        Matcher matcher = p.matcher(logEntry);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(26));
        }
        matcher.appendTail(sb);
        this.logEntry = sb.toString();
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
