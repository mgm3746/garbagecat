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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
 * 1) Standard logging:
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
 * <pre>
 * [0.528s][info][gc] GC(1) Concurrent marking 16M-&gt;17M(64M) 7.045ms
 * </pre>
 * 
 * <p>
 * 2) Shenandoah with <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>.
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahConcurrentEvent extends ShenandoahCollector
        implements UnifiedLogging, LogEvent, ParallelEvent, CombinedData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[(" + JdkRegEx.TIMESTAMP + "s|"
            + JdkRegEx.TIMESTAMP_MILLIS + ")\\](\\[info\\])?(\\[gc(,start)?[ ]{0,11}\\])?( " + JdkRegEx.GC_EVENT_NUMBER
            + ")? Concurrent (reset|uncommit|marking( \\(update refs\\))?( \\(process weakrefs\\))?|"
            + "precleaning|evacuation|update references|cleanup)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")?[ ]*$";

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
                // TODO: Is this correct?
                long endTimestamp;
                if (matcher.group(12).matches(JdkRegEx.TIMESTAMP_MILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(14));
                } else {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                }
                int duration = 0;
                if (matcher.group(32) != null) {
                    duration = JdkMath.convertMillisToMicros(matcher.group(32)).intValue();
                }
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                if (matcher.group(22) != null) {
                    combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(23)), matcher.group(25).charAt(0));
                    combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(26)),
                            matcher.group(28).charAt(0));
                    combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(29)),
                            matcher.group(31).charAt(0));

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
