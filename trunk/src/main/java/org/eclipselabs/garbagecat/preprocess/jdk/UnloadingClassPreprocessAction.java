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
 * UNLOADING_CLASS
 * </p>
 * 
 * <p>
 * Remove perm gen collection "Unloading class" logging. The perm gen is collected at the beginning of some old
 * collections, resulting in the perm gen logging being intermingled with the old collection logging. For example:
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 830048.804: [Full GC 830048.804: [CMS[Unloading class sun.reflect.GeneratedConstructorAccessor73]
 * [Unloading class sun.reflect.GeneratedConstructorAccessor70]
 * : 1572185K-&gt;1070163K(1572864K), 6.8812400 secs] 2489689K-&gt;1070163K(2490368K), [CMS Perm : 46357K-&gt;46348K(77352K)], 6.8821630 secs] [Times: user=6.87 sys=0.00, real=6.88 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 830048.804: [Full GC 830048.804: [CMS: 1572185K-&gt;1070163K(1572864K), 6.8812400 secs] 2489689K-&gt;1070163K(2490368K), [CMS Perm : 46357K-&gt;46348K(77352K)], 6.8821630 secs] [Times: user=6.87 sys=0.00, real=6.88 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnloadingClassPreprocessAction implements PreprocessAction {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^(.*)" + JdkRegEx.UNLOADING_CLASS_BLOCK + "(.*)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create Unloading class event from log entry.
     * 
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     */
    public UnloadingClassPreprocessAction(String logEntry, String nextLogEntry) {
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            // Do not add a newline if the next line requires preprocessing or the next log line is
            // an unknown logging event (in that case assume the logging is split).
            if (nextLogEntry != null
                    && (match(nextLogEntry) || JdkUtil.identifyEventType(nextLogEntry).equals(
                            JdkUtil.LogEventType.UNKNOWN))) {
                // No newline
                this.logEntry = matcher.group(1) + matcher.group(2);
            } else {
                // Newline
                this.logEntry = matcher.group(1) + matcher.group(2) + System.getProperty("line.separator");
                ;
            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.UNLOADING_CLASS.toString();
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
