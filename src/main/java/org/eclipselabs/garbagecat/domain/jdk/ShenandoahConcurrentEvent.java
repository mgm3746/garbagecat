/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.Memory;
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
 * and metaspace size and occupancy.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard logging:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:29.311-0400: 0.373: [Concurrent reset 16991K-&gt;17152K(17408K), 0.435 ms]
 * </pre>
 * 
 * <pre>
 * 2020-08-13T16:38:29.318+0000: 432034.969: [Concurrent reset, 26.427 ms]
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
 * <p>
 * 2) Preprocessed with Metaspace block:
 * </p>
 * 
 * <pre>
 * 2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K-&gt;4701K(37888K), 0.048 ms], [Metaspace: 6477K-&gt;6481K(1056768K)]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahConcurrentEvent extends ShenandoahCollector
        implements LogEvent, ParallelEvent, CombinedData, ClassData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR
            + " \\[Concurrent (cleanup|evacuation|marking|marking \\(process weakrefs\\)|marking \\(unload classes\\)|"
            + "precleaning|reset|update references)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\))?, " + JdkRegEx.DURATION_MS + "\\](, \\[Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\])?[ ]*$";

    private static Pattern PATTERN = Pattern.compile(_REGEX);

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

    /**
     * Permanent generation or metaspace occupancy at end of GC event.
     */
    private Memory classOccupancyEnd;

    /**
     * Permanent generation or metaspace occupancy at beginning of GC event.
     */
    private Memory classOccupancyInit;

    /**
     * Space allocated to permanent generation or metaspace.
     */
    private Memory classSpace;

    /**
     * Combined size at end of GC event.
     */
    private Memory combinedOccupancyEnd;

    /**
     * Combined size at beginning of GC event.
     */
    private Memory combinedOccupancyInit;

    /**
     * Combined available space.
     */
    private Memory combinedSpace;

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
    public ShenandoahConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
            }
            if (matcher.group(JdkUtil.DECORATOR_SIZE + 2) != null) {
                combinedOccupancyInit = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 3),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 5).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 6),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 8).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 9),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
            }
            if (matcher.group(JdkUtil.DECORATOR_SIZE + 13) != null) {
                classOccupancyInit = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 14),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 16).charAt(0)).convertTo(KILOBYTES);
                classOccupancyEnd = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 17),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 19).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(JdkUtil.DECORATOR_SIZE + 20),
                        matcher.group(JdkUtil.DECORATOR_SIZE + 22).charAt(0)).convertTo(KILOBYTES);
            }
        }
    }

    public Memory getClassOccupancyEnd() {
        return classOccupancyEnd;
    }

    public Memory getClassOccupancyInit() {
        return classOccupancyInit;
    }

    public Memory getClassSpace() {
        return classSpace;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedOccupancyEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedOccupancyInit;
    }

    public Memory getCombinedSpace() {
        return combinedSpace;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
