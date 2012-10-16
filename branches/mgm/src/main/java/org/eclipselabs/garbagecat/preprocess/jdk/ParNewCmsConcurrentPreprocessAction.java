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
 * PAR_NEW_CMS_CONCURRENT
 * </p>
 * 
 * <p>
 * Combine {@link org.eclipselabs.garbagecat.domain.jdk.ParNewCmsConcurrentEvent} logging split across 2 lines.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: 0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]
 * : 212981K-&gt;3156K(242304K), 0.0364435 secs] 4712182K-&gt;4502357K(4971420K), 0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: 0.043/0.144 secs]: 212981K-&gt;3156K(242304K), 0.0364435 secs] 4712182K-&gt;4502357K(4971420K), 0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ParNewCmsConcurrentPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the 1st logging line.
     */
    private static final String REGEX_LINE1 = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
            + ": \\[ParNew" + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-(abortable-preclean|mark|sweep): "
            + JdkRegEx.DURATION_FRACTION + "\\])" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * Regular expressions defining the 2nd logging line.
     */
    private static final String REGEX_LINE2 = "^: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     */
    public ParNewCmsConcurrentPreprocessAction(String logEntry) {
        Pattern pattern = Pattern.compile(REGEX_LINE1);
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            this.logEntry = logEntry;
            if (matcher.group(1) != null) {
                // Retain logging before
                this.logEntry = matcher.group(1);
            }
        } else {
            this.logEntry = logEntry + "\n";
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.PAR_NEW_CMS_CONCURRENT.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @param nextLogLine
     *            The next log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, String priorLogLine, String nextLogLine) {
        boolean isFirstLine = logLine.matches(REGEX_LINE1) && nextLogLine.matches(REGEX_LINE2);
        boolean isSecondLine = logLine.matches(REGEX_LINE2) && priorLogLine.matches(REGEX_LINE1);
        return isFirstLine || isSecondLine;
    }
}
