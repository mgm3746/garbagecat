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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * Z_CONCURRENT
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
 * 1) Metaspace:
 * </p>
 * 
 * <pre>
 * [2024-11-19T04:02:29.854+0800][41508.444s][info ][gc,metaspace            ] GC(131) O: Metaspace: 55M used, 58M committed, 1088M reserved
 * </pre>
 * 
 * <p>
 * 2) Heap:
 * </p>
 * 
 * <pre>
 * [2024-11-19T04:02:29.854+0800][41508.445s][info ][gc                      ] GC(131) Major Collection (Proactive) 15120M(64%)->12048M(51%) 17.390s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ZConcurrentEvent extends UnknownCollector implements UnifiedLogging, CombinedData, ClassData {

    /**
     * Regular expression metaspace collection.
     */
    private static final String _REGEX_METASPACE = "([OYy]: )?Metaspace: " + JdkRegEx.SIZE + " used, " + JdkRegEx.SIZE
            + " committed, " + JdkRegEx.SIZE + " reserved";

    /**
     * Regular expression metaspace collection.
     */
    private static final String _REGEX_HEAP = "(Garbage|Major|Minor) Collection \\((Allocation Rate|"
            + "Allocation Stall|CodeCache GC Threshold|High Usage|Metadata GC Threshold|Proactive|Warmup)\\) "
            + JdkRegEx.SIZE + "\\(\\d{1,3}%\\)->" + JdkRegEx.SIZE + "\\((\\d{1,3})%\\)( (\\d{0,}[\\.\\,]\\d{3})s)?";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " (" + _REGEX_METASPACE + "|" + _REGEX_HEAP
            + ")[ ]*$";

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
     * The elapsed clock time for the event in microseconds (rounded).
     */
    private long eventTime;

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
    public ZConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(_REGEX_METASPACE)) {
                timestamp = UnifiedUtil.calculateTime(matcher);
                classOccupancyInit = Memory.ZERO;
                classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8).charAt(0)).convertTo(KILOBYTES);
            } else if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(_REGEX_HEAP)) {
                if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 21) != null) {
                    eventTime = JdkMath.convertSecsToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22)).intValue();
                }
                long time = UnifiedUtil.calculateTime(matcher);
                if (!isEndstamp()) {
                    timestamp = time;
                } else {
                    timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
                }
                combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 16).charAt(0)).convertTo(KILOBYTES);
                BigDecimal end = new BigDecimal(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17));
                combinedOccupancyEnd = memory(Integer.toString(end.intValue()),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19).charAt(0)).convertTo(KILOBYTES);
                BigDecimal percent = new BigDecimal(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 20)).movePointLeft(2);
                BigDecimal allocation = end.divide(percent, 0, RoundingMode.HALF_EVEN);
                combinedSpace = memory(Integer.toString(allocation.intValue()),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19).charAt(0)).convertTo(KILOBYTES);
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
        return JdkUtil.LogEventType.Z_CONCURRENT.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isEndstamp() {
        return true;
    }
}
