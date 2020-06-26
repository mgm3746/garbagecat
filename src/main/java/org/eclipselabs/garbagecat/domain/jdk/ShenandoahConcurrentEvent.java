/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * SHENANDOAH_CONCURRENT
 * </p>
 * 
 * <p>
 * Any number of events that happen concurrently with the JVM's execution of application threads. These events are not
 * included in the GC analysis since there is no application pause time; however, they are used to determine max heap
 * space and occupancy.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard unified logging:
 * </p>
 * 
 * <pre>
 * [0.437s][info][gc] GC(0) Concurrent reset 15M-&gt;16M(64M) 4.701ms
 * </pre>
 * 
 * <pre>
 * [0.528s][info][gc] GC(1) Concurrent marking 16M-&gt;17M(64M) 7.045ms
 * </pre>
 * 
 * <pre>
 * [0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M-&gt;19M(64M) 15.264ms
 * </pre>
 * 
 * <pre>
 * [0.455s][info][gc] GC(0) Concurrent precleaning 19M-&gt;19M(64M) 0.202ms
 * </pre>
 * 
 * <pre>
 * [0.465s][info][gc] GC(0) Concurrent evacuation 17M-&gt;19M(64M) 6.528ms
 * </pre>
 * 
 * <pre>
 * [0.470s][info][gc] GC(0) Concurrent update references 19M-&gt;19M(64M) 4.708ms
 * </pre>
 * 
 * <pre>
 * [0.472s][info][gc] GC(0) Concurrent cleanup 18M-&gt;15M(64M) 0.036ms
 * </pre>
 * 
 * <p>
 * 2) With <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>.
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset
 * </pre>
 * 
 * <p>
 * 3) JDK8:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:29.311-0400: 0.373: [Concurrent reset 16991K-&gt;17152K(17408K), 0.435 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.365-0400: 0.427: [Concurrent marking 16498K-&gt;17020K(21248K), 2.462 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) 17759K-&gt;19325K(19456K), 6.892 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.322-0400: 0.384: [Concurrent precleaning 19325K-&gt;19357K(19456K), 0.092 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Concurrent evacuation 9712K-&gt;9862K(23296K), 0.144 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K-&gt;12443K(23296K), 3.463 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.431-0400: 0.493: [Concurrent cleanup 12501K-&gt;8434K(23296K), 0.034 ms]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahConcurrentEvent extends ShenandoahCollector implements LogEvent, ParallelEvent, CombinedData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
            + ") [\\[]{0,1}Concurrent (reset|uncommit|marking( \\(update refs\\))?( \\(process weakrefs\\))?|"
            + "precleaning|evacuation|update references|cleanup)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)[,]{0,1} " + UnifiedRegEx.DURATION + ")?[\\]]{0,1}[ ]*$";

    private static Pattern pattern = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Combined size (kilobytes) at beginning of GC event.
     */
    private int combined;

    /**
     * Combined size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Combined available space (kilobytes).
     */
    private int combinedAvailable;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                int duration = 0;
                if (matcher.group(50) != null) {
                    duration = JdkMath.convertMillisToMicros(matcher.group(50)).intValue();
                }

                if (matcher.group(1).matches(UnifiedRegEx.DECORATOR)) {
                    long endTimestamp;
                    if (matcher.group(13).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(29));
                    } else if (matcher.group(13).matches(UnifiedRegEx.UPTIME)) {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(24)).longValue();
                    } else {
                        if (matcher.group(27) != null) {
                            if (matcher.group(27).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                                endTimestamp = Long.parseLong(matcher.group(29));
                            } else {
                                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(28)).longValue();
                            }
                        } else {
                            // Datestamp only.
                            endTimestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(13));
                        }
                    }
                    timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                } else {
                    // JDK8
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                }
                if (matcher.group(40) != null) {
                    combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(41)), matcher.group(43).charAt(0));
                    combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(44)),
                            matcher.group(46).charAt(0));
                    combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(47)),
                            matcher.group(49).charAt(0));
                }

            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getCombinedOccupancyInit() {
        return combined;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAvailable;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString();
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
