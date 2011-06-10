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
 * GC_TIME_LIMIT_EXCEEDED
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} or
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelOldCompactingEvent} logging that is split across 2 lines due to
 * the garbage collection overhead limit being reached. This happens when 98% of the total time is spent in garbage
 * collection and less than 2% of the heap is recovered. This feature is a throttle to prevent applications from running
 * for an extended period of time while making little or no progress because the heap is too small. If necessary, this
 * feature can be disabled with the <code>-XX:-UseGCOverheadLimit</code> option.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} with "GC time <em>would exceed</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 3743.645: [Full GC [PSYoungGen: 419840K-&gt;415020K(839680K)] [PSOldGen: 5008922K-&gt;5008922K(5033984K)] 5428762K-&gt;5423942K(5873664K) [PSPermGen: 193275K-&gt;193275K(262144K)]      GC time would exceed GCTimeLimit of 98%
 * , 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 *<pre>
 * 3743.645: [Full GC [PSYoungGen: 419840K-&gt;415020K(839680K)] [PSOldGen: 5008922K-&gt;5008922K(5033984K)] 5428762K-&gt;5423942K(5873664K) [PSPermGen: 193275K-&gt;193275K(262144K)], 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]
 * </pre>
 * 
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} with "GC time <em>is exceeding</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 3924.453: [Full GC [PSYoungGen: 419840K-&gt;418436K(839680K)] [PSOldGen: 5008601K-&gt;5008601K(5033984K)] 5428441K-&gt;5427038K(5873664K) [PSPermGen: 193278K-&gt;193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%
 * </pre>
 * 
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.ParallelOldCompactingEvent} with "GC time <em>is exceeding</em>
 * GCTimeLimit":
 * </p>
 * 
 * <pre>
 * 52767.809: [Full GC [PSYoungGen: 109294K-&gt;94333K(184960K)] [ParOldGen: 1307971K-&gt;1307412K(1310720K)] 1417265K-&gt;1401746K(1495680K) [PSPermGen: 113654K-&gt;113646K(196608K)]        GC time is exceeding GCTimeLimit of 98%
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcTimeLimitExceededPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the 1st logging line.
     */
    private static final String REGEX_LINE1 = "^(" + JdkRegEx.TIMESTAMP + ": \\[Full GC \\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] \\[(PS|Par)OldGen: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) \\[PSPermGen: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])(      |\t)GC time (would exceed|is exceeding) GCTimeLimit of 98%$";

    /**
     * Regular expressions defining the 2nd logging line.
     */
    private static final String REGEX_LINE2 = "^, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     */
    public GcTimeLimitExceededPreprocessAction(String logEntry) {
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
        return JdkUtil.PreprocessActionType.GC_TIME_LIMIT_EXCEEDED.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, String priorLogLine) {
        return (logLine.matches(REGEX_LINE1) || (logLine.matches(REGEX_LINE2) && priorLogLine.matches(REGEX_LINE1)));
    }
}
