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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * TENURING_DISTRIBUTION
 * </p>
 * 
 * <p>
 * <code>-XX:+PrintTenuringDistribution</code> logging. This data is currently not being used for any analysis.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent}:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew
 * Desired survivor size 2228224 bytes, new threshold 1 (max 15)
 * - age   1:    3177664 bytes,    3177664 total
 * - age   2:    1278784 bytes,    4456448 total
 * : 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew: 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * <p>
 * 2) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * 877369.458: [GC 877369.459: [ParNew (promotion failed)
 * Desired survivor size 120795952 bytes, new threshold 3 (max 31)
 * - age   1:   92513688 bytes,   92513688 total
 * - age   2:   16401312 bytes,  108915000 total
 * - age   3:   19123776 bytes,  128038776 total
 * - age   4:    6178856 bytes,  134217632 total
 * : 917504K-&gt;917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: [CMS-concurrent-mark: 5.714/11.380 secs] [Times: user=14.72 sys=4.81, real=11.38 secs]
 *  (concurrent mode failure): 1567700K-&gt;1571451K(1572864K), 14.6444240 secs] 2370842K-&gt;1694149K(2490368K), [CMS Perm : 46359K-&gt;46354K(77352K)], 20.2345470 secs] [Times: user=22.17 sys=4.56, real=20.23 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 877369.458: [GC 877369.459: [ParNew (promotion failed): 917504K-&gt;917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: [CMS-concurrent-mark: 5.714/11.380 secs] (concurrent mode failure): 1567700K-&gt;1571451K(1572864K), 14.6444240 secs] 2370842K-&gt;1694149K(2490368K), [CMS Perm : 46359K-&gt;46354K(77352K)], 20.2345470 secs] [Times: user=22.17 sys=4.56, real=20.23 secs]
 * </pre>
 * 
 * <p>
 * 3) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent} :
 * </p>
 * 
 * <pre>
 * 10.392: [GC
 * Desired survivor size 497025024 bytes, new threshold 7 (max 15)
 *  [PSYoungGen: 970752K-&gt;104301K(1456128K)] 970752K-&gt;104301K(3708928K), 0.1992940 secs] [Times: user=0.68 sys=0.05, real=0.20 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 10.392: [GC [PSYoungGen: 970752K-&gt;104301K(1456128K)] 970752K-&gt;104301K(3708928K), 0.1992940 secs] [Times: user=0.68 sys=0.05, real=0.20 secs]
 * </pre>
 * 
 * <p>
 * 4) No space after "GC" with datestamp:
 * </p>
 * 
 * <pre>
 * 2016-07-24T07:11:53.101-0400: 7.729: [GC2016-07-24T07:11:53.101-0400: 7.729: [ParNew
 * Desired survivor size 67108864 bytes, new threshold 1 (max 15)
 * - age   1:  108710016 bytes,  108710016 total
 * : 889671K-&gt;121719K(917504K), 0.2231670 secs] 889671K-&gt;160630K(6160384K), 0.2232600 secs] [Times: user=0.36 sys=0.01, real=0.22 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 7.729: [GC7.729: [ParNew: 889671K-&gt;121719K(917504K), 0.2231670 secs] 889671K-&gt;160630K(6160384K), 0.2232600 secs] [Times: user=0.36 sys=0.01, real=0.22 secs]
 * </pre>
 * 
 * <p>
 * 5) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} with trigger after GC:
 * </p>
 * 
 * <pre>
 * 
 * 2.372: [GC (Allocation Failure) 2.372: [ParNew
 * Desired survivor size 78643200 bytes, new threshold 15 (max 15)
 * - age   1:   28758920 bytes,   28758920 total
 * : 1228800K-&gt;28198K(1382400K), 0.0440820 secs] 1228800K-&gt;28198K(6137856K), 0.0443030 secs] [Times: user=0.34 sys=0.02, real=0.05 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 2.372: [GC (Allocation Failure) 2.372: [ParNew: 1228800K-&gt;28198K(1382400K), 0.0440820 secs] 1228800K-&gt;28198K(6137856K), 0.0443030 secs] [Times: user=0.34 sys=0.02, real=0.05 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TenuringDistributionEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            //
            "^Desired survivor size \\d{1,11} bytes, new threshold \\d{1,2} \\(max \\d{1,2}\\)$",
            //
            "^- age[ ]+\\d{1,2}:[ ]+\\d{1,11} bytes,[ ]+\\d{1,11} total$" };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public TenuringDistributionEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString();
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
