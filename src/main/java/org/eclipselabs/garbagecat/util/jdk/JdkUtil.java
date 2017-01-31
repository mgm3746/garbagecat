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
package org.eclipselabs.garbagecat.util.jdk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ApplicationLoggingEvent;
import org.eclipselabs.garbagecat.domain.BlankLineEvent;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkWithClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.FlsStatisticsEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1ConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGCEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1MixedPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1RemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcLockerEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.LogFileEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelOldCompactingEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ReferenceGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.domain.jdk.ThreadDumpEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcYoungEvent;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;

/**
 * <p>
 * Utility methods and constants for OpenJDK and Sun JDK.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JdkUtil {

    /**
     * Defined logging events.
     */
    public enum LogEventType {
        SERIAL_OLD, SERIAL_NEW, PAR_NEW, PARALLEL_SERIAL_OLD, PARALLEL_SCAVENGE, PARALLEL_OLD_COMPACTING,
        //
        CMS_SERIAL_OLD, CMS_REMARK_WITH_CLASS_UNLOADING, CMS_REMARK, CMS_INITIAL_MARK, CMS_CONCURRENT,
        //
        APPLICATION_CONCURRENT_TIME, APPLICATION_STOPPED_TIME, UNKNOWN, VERBOSE_GC_YOUNG, VERBOSE_GC_OLD,
        //
        G1_YOUNG_PAUSE, G1_MIXED_PAUSE, G1_CONCURRENT, G1_YOUNG_INITIAL_MARK, G1_REMARK, G1_CLEANUP, G1_FULL_GC,
        //
        HEADER_COMMAND_LINE_FLAGS, HEADER_MEMORY, HEADER_VERSION, REFERENCE_GC, CLASS_HISTOGRAM, HEAP_AT_GC,
        //
        CLASS_UNLOADING, APPLICATION_LOGGING, THREAD_DUMP, BLANK_LINE, GC_OVERHEAD_LIMIT, LOG_FILE, FLS_STATISTICS,
        //
        GC_LOCKER, TENURING_DISTRIBUTION
    };

    /**
     * Defined preprocessing actions.
     */
    public enum PreprocessActionType {
        APPLICATION_CONCURRENT_TIME, APPLICATION_STOPPED_TIME, DATE_STAMP, G1, CMS, PARALLEL, SERIAL
    };

    /**
     * Defined triggers.
     */
    public enum TriggerType {
        SYSTEM_GC, METADATA_GC_THRESHOLD, ALLOCATION_FAILURE, UNDEFINED, UNKNOWN, TO_SPACE_EXHAUSTED,
        //
        G1_EVACUATION_PAUSE, GCLOCATER_INITIATED_GC, CMS_INITIAL_MARK, CMS_FINAL_REMARK, CMS_CONCURRENT_MODE_FAILURE,
        //
        CMS_CONCURRENT_MODE_INTERRUPTED, CLASS_HISTOGRAM, LAST_DITCH_COLLECTION, JVMTI_FORCED_GARBAGE_COLLECTION,
        //
        ERGONOMICS, HEAP_INSPECTION_INITIATED_GC;
    };

    /**
     * Defined collector families.
     */
    public enum CollectorFamily {
        SERIAL, PARALLEL, CMS, G1
    }

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkUtil() {

    }

    /**
     * Identify the log line garbage collection event.
     * 
     * @param logLine
     *            The log entry.
     * @return The <code>LogEventType</code> of the log entry.
     */
    public static final LogEventType identifyEventType(String logLine) {

        // In order of most common events to limit checking
        // G1
        if (G1YoungPauseEvent.match(logLine))
            return LogEventType.G1_YOUNG_PAUSE;
        if (G1MixedPauseEvent.match(logLine))
            return LogEventType.G1_MIXED_PAUSE;
        if (G1ConcurrentEvent.match(logLine))
            return LogEventType.G1_CONCURRENT;
        if (G1YoungInitialMarkEvent.match(logLine))
            return LogEventType.G1_YOUNG_INITIAL_MARK;
        if (G1RemarkEvent.match(logLine))
            return LogEventType.G1_REMARK;
        if (G1FullGCEvent.match(logLine))
            return LogEventType.G1_FULL_GC;
        if (G1CleanupEvent.match(logLine))
            return LogEventType.G1_CLEANUP;
        // CMS
        if (ParNewEvent.match(logLine))
            return LogEventType.PAR_NEW;
        if (CmsSerialOldEvent.match(logLine))
            return LogEventType.CMS_SERIAL_OLD;
        if (CmsInitialMarkEvent.match(logLine))
            return LogEventType.CMS_INITIAL_MARK;
        if (CmsRemarkEvent.match(logLine))
            return LogEventType.CMS_REMARK;
        if (CmsRemarkWithClassUnloadingEvent.match(logLine))
            return LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING;
        if (CmsConcurrentEvent.match(logLine))
            return LogEventType.CMS_CONCURRENT;
        // Parallel
        if (ParallelScavengeEvent.match(logLine))
            return LogEventType.PARALLEL_SCAVENGE;
        if (ParallelSerialOldEvent.match(logLine))
            return LogEventType.PARALLEL_SERIAL_OLD;
        if (ParallelOldCompactingEvent.match(logLine))
            return LogEventType.PARALLEL_OLD_COMPACTING;
        // Serial
        if (SerialOldEvent.match(logLine))
            return LogEventType.SERIAL_OLD;
        if (SerialNewEvent.match(logLine))
            return LogEventType.SERIAL_NEW;
        // Other
        if (ApplicationConcurrentTimeEvent.match(logLine))
            return LogEventType.APPLICATION_CONCURRENT_TIME;
        if (ApplicationStoppedTimeEvent.match(logLine))
            return LogEventType.APPLICATION_STOPPED_TIME;
        if (VerboseGcYoungEvent.match(logLine))
            return LogEventType.VERBOSE_GC_YOUNG;
        if (VerboseGcOldEvent.match(logLine))
            return LogEventType.VERBOSE_GC_OLD;
        if (ReferenceGcEvent.match(logLine))
            return LogEventType.REFERENCE_GC;
        if (ClassUnloadingEvent.match(logLine))
            return LogEventType.CLASS_UNLOADING;
        if (HeapAtGcEvent.match(logLine))
            return LogEventType.HEAP_AT_GC;
        if (TenuringDistributionEvent.match(logLine))
            return LogEventType.TENURING_DISTRIBUTION;
        if (ClassHistogramEvent.match(logLine))
            return LogEventType.CLASS_HISTOGRAM;
        if (ApplicationLoggingEvent.match(logLine))
            return LogEventType.APPLICATION_LOGGING;
        if (ThreadDumpEvent.match(logLine))
            return LogEventType.THREAD_DUMP;
        if (LogFileEvent.match(logLine))
            return LogEventType.LOG_FILE;
        if (BlankLineEvent.match(logLine))
            return LogEventType.BLANK_LINE;
        if (GcOverheadLimitEvent.match(logLine))
            return LogEventType.GC_OVERHEAD_LIMIT;
        if (FlsStatisticsEvent.match(logLine))
            return LogEventType.FLS_STATISTICS;
        if (GcLockerEvent.match(logLine))
            return LogEventType.GC_LOCKER;
        if (HeaderCommandLineFlagsEvent.match(logLine))
            return LogEventType.HEADER_COMMAND_LINE_FLAGS;
        if (HeaderMemoryEvent.match(logLine))
            return LogEventType.HEADER_MEMORY;
        if (HeaderVersionEvent.match(logLine))
            return LogEventType.HEADER_VERSION;

        // no idea what event is
        return LogEventType.UNKNOWN;
    }

    /**
     * Create <code>LogEvent</code> from GC log line.
     * 
     * @param logLine
     *            The log line as it appears in the GC log.
     * @return The <code>LogEvent</code> corresponding to the log line.
     */
    public static final LogEvent parseLogLine(String logLine) {
        LogEventType eventType = identifyEventType(logLine);
        LogEvent event = null;
        switch (eventType) {
        // G1
        case G1_CLEANUP:
            event = new G1CleanupEvent(logLine);
            break;
        case G1_CONCURRENT:
            event = new G1ConcurrentEvent(logLine);
            break;
        case G1_FULL_GC:
            event = new G1FullGCEvent(logLine);
            break;
        case G1_MIXED_PAUSE:
            event = new G1MixedPauseEvent(logLine);
            break;
        case G1_REMARK:
            event = new G1RemarkEvent(logLine);
            break;
        case G1_YOUNG_INITIAL_MARK:
            event = new G1YoungInitialMarkEvent(logLine);
            break;
        case G1_YOUNG_PAUSE:
            event = new G1YoungPauseEvent(logLine);
            break;
        // CMS
        case PAR_NEW:
            event = new ParNewEvent(logLine);
            break;
        case CMS_CONCURRENT:
            event = new CmsConcurrentEvent();
            break;
        case CMS_INITIAL_MARK:
            event = new CmsInitialMarkEvent(logLine);
            break;
        case CMS_REMARK:
            event = new CmsRemarkEvent(logLine);
            break;
        case CMS_REMARK_WITH_CLASS_UNLOADING:
            event = new CmsRemarkWithClassUnloadingEvent(logLine);
            break;
        case CMS_SERIAL_OLD:
            event = new CmsSerialOldEvent(logLine);
            break;
        // Parallel
        case PARALLEL_OLD_COMPACTING:
            event = new ParallelOldCompactingEvent(logLine);
            break;
        case PARALLEL_SCAVENGE:
            event = new ParallelScavengeEvent(logLine);
            break;
        case PARALLEL_SERIAL_OLD:
            event = new ParallelSerialOldEvent(logLine);
            break;
        // Serial
        case SERIAL_NEW:
            event = new SerialNewEvent(logLine);
            break;
        case SERIAL_OLD:
            event = new SerialOldEvent(logLine);
            break;
        // Other
        case APPLICATION_CONCURRENT_TIME:
            event = new ApplicationConcurrentTimeEvent();
            break;
        case APPLICATION_LOGGING:
            event = new ApplicationLoggingEvent(logLine);
            break;
        case APPLICATION_STOPPED_TIME:
            event = new ApplicationStoppedTimeEvent(logLine);
            break;
        case BLANK_LINE:
            event = new BlankLineEvent(logLine);
            break;
        case CLASS_HISTOGRAM:
            event = new ClassHistogramEvent(logLine);
            break;
        case CLASS_UNLOADING:
            event = new ClassUnloadingEvent(logLine);
            break;
        case FLS_STATISTICS:
            event = new FlsStatisticsEvent(logLine);
            break;
        case GC_LOCKER:
            event = new GcLockerEvent(logLine);
            break;
        case HEAP_AT_GC:
            event = new HeapAtGcEvent(logLine);
            break;
        case GC_OVERHEAD_LIMIT:
            event = new GcOverheadLimitEvent(logLine);
            break;
        case LOG_FILE:
            event = new LogFileEvent(logLine);
            break;
        case REFERENCE_GC:
            event = new ReferenceGcEvent(logLine);
            break;
        case TENURING_DISTRIBUTION:
            event = new TenuringDistributionEvent(logLine);
            break;
        case THREAD_DUMP:
            event = new ThreadDumpEvent(logLine);
            break;
        case UNKNOWN:
            event = new UnknownEvent(logLine);
            break;
        case VERBOSE_GC_OLD:
            event = new VerboseGcOldEvent(logLine);
            break;
        case VERBOSE_GC_YOUNG:
            event = new VerboseGcYoungEvent(logLine);
            break;
        case HEADER_COMMAND_LINE_FLAGS:
            event = new HeaderCommandLineFlagsEvent(logLine);
            break;
        case HEADER_MEMORY:
            event = new HeaderMemoryEvent(logLine);
            break;
        case HEADER_VERSION:
            event = new HeaderVersionEvent(logLine);
            break;
        default:
            throw new AssertionError("Unexpected event type value: " + eventType);
        }
        return event;
    }

    /**
     * Create <code>BlockingEvent</code> from values.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @param logEntry
     *            Log entry.
     * @param timestamp
     *            Log entry timestamp.
     * @param duration
     *            The duration of the log event.
     * @return The <code>BlockingEvent</code> for the given event values.
     */
    public static final BlockingEvent hydrateBlockingEvent(LogEventType eventType, String logEntry, long timestamp,
            int duration) {
        BlockingEvent event = null;
        switch (eventType) {
        // G1
        case G1_YOUNG_PAUSE:
            event = new G1YoungPauseEvent(logEntry, timestamp, duration);
            break;
        case G1_MIXED_PAUSE:
            event = new G1MixedPauseEvent(logEntry, timestamp, duration);
            break;
        case G1_YOUNG_INITIAL_MARK:
            event = new G1YoungInitialMarkEvent(logEntry, timestamp, duration);
            break;
        case G1_REMARK:
            event = new G1RemarkEvent(logEntry, timestamp, duration);
            break;
        case G1_CLEANUP:
            event = new G1CleanupEvent(logEntry, timestamp, duration);
            break;
        case G1_FULL_GC:
            event = new G1FullGCEvent(logEntry, timestamp, duration);
            break;
        // CMS
        case PAR_NEW:
            event = new ParNewEvent(logEntry, timestamp, duration);
            break;
        case CMS_SERIAL_OLD:
            event = new CmsSerialOldEvent(logEntry, timestamp, duration);
            break;
        case CMS_INITIAL_MARK:
            event = new CmsInitialMarkEvent(logEntry, timestamp, duration);
            break;
        case CMS_REMARK:
            event = new CmsRemarkEvent(logEntry, timestamp, duration);
            break;
        case CMS_REMARK_WITH_CLASS_UNLOADING:
            event = new CmsRemarkWithClassUnloadingEvent(logEntry, timestamp, duration);
            break;
        // Parallel
        case PARALLEL_SCAVENGE:
            event = new ParallelScavengeEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_SERIAL_OLD:
            event = new ParallelSerialOldEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_OLD_COMPACTING:
            event = new ParallelOldCompactingEvent(logEntry, timestamp, duration);
            break;
        // Serial
        case SERIAL_OLD:
            event = new SerialOldEvent(logEntry, timestamp, duration);
            break;
        case SERIAL_NEW:
            event = new SerialNewEvent(logEntry, timestamp, duration);
            break;
        // Other
        case VERBOSE_GC_YOUNG:
            event = new VerboseGcYoungEvent(logEntry, timestamp, duration);
            break;
        case VERBOSE_GC_OLD:
            event = new VerboseGcOldEvent(logEntry, timestamp, duration);
            break;

        default:
            throw new AssertionError("Unexpected event type value: " + eventType + ": " + logEntry);
        }
        return event;
    }

    /**
     * @param eventType
     *            The event type to test.
     * @return true if the log event is blocking, false if it is concurrent or informational.
     */
    public static final boolean isBlocking(LogEventType eventType) {

        boolean isBlocking = true;

        switch (eventType) {
        case APPLICATION_CONCURRENT_TIME:
        case APPLICATION_STOPPED_TIME:
        case CLASS_HISTOGRAM:
        case CLASS_UNLOADING:
        case CMS_CONCURRENT:
        case FLS_STATISTICS:
        case GC_LOCKER:
        case GC_OVERHEAD_LIMIT:
        case G1_CONCURRENT:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VERSION:
        case HEAP_AT_GC:
        case LOG_FILE:
        case REFERENCE_GC:
        case THREAD_DUMP:
        case TENURING_DISTRIBUTION:
        case UNKNOWN:
            isBlocking = false;
        default:
            break;
        }

        return isBlocking;
    }

    public static final LogEventType determineEventType(String eventTypeString) {
        LogEventType logEventType = null;
        LogEventType[] logEventTypes = LogEventType.values();
        for (int i = 0; i < logEventTypes.length; i++) {
            if (logEventTypes[i].toString().equals(eventTypeString)) {
                logEventType = logEventTypes[i];
                break;
            }
        }
        return logEventType;
    }

    /**
     * Check to see if a log line includes any datestamps.
     * 
     * @param logLine
     *            The log line.
     * @return True if the log line includes a datestamp, false otherwise..
     */
    public static final boolean isLogLineWithDateStamp(String logLine) {
        String regex = "^(.*)" + JdkRegEx.DATESTAMP + "(.*)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(logLine).matches();
    }

    /**
     * Convert all log entry timestamps to a datestamp.
     * 
     * @param logEntry
     *            The log entry.
     * @param jvmStartDate
     *            The date/time the JVM started.
     * @return the log entry with the timestamp converted to a datestamp.
     */
    public static final String convertLogEntryTimestampsToDateStamp(String logEntry, Date jvmStartDate) {
        // Add the colon or space after the timestamp format so durations will
        // not get picked up.
        Pattern pattern = Pattern.compile(JdkRegEx.TIMESTAMP + "(: )");
        Matcher matcher = pattern.matcher(logEntry);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            Date date = GcUtil.getDatePlusTimestamp(jvmStartDate,
                    JdkMath.convertSecsToMillis(matcher.group(1)).longValue());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            // Only update the timestamp, keep the colon or space.
            matcher.appendReplacement(sb, formatter.format(date) + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Convert all log entry datestamps to a timestamp (number of seconds after JVM startup).
     * 
     * @param logEntry
     *            The log entry.
     * @param jvmStartDate
     *            The date/time the JVM started.
     * @return the log entry with the timestamp converted to a date/time.
     */
    public static final String convertLogEntryDateStampToTimeStamp(String logEntry, Date jvmStartDate) {
        // Add the colon or space after the datestamp format so durations will
        // not get picked up.
        Pattern pattern = Pattern.compile(JdkRegEx.DATESTAMP + "(: )");
        Matcher matcher = pattern.matcher(logEntry);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {

            Date date = GcUtil.getDatePlusTimestamp(jvmStartDate,
                    JdkMath.convertSecsToMillis(matcher.group(1)).longValue());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            // Only update the datestamp, keep the colon or space.
            matcher.appendReplacement(sb, formatter.format(date) + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Determine if the garbage collection event should be classified as a bottleneck.
     * 
     * @param gcEvent
     *            Current garbage collection event.
     * @param priorEvent
     *            Previous garbage collection event.
     * @param throughputThreshold
     *            Throughput threshold (percent of time spent not doing garbage collection for a given time interval) to
     *            be considered a bottleneck. Whole number 0-100.
     * @return True if the garbage collection event pause time meets the bottleneck definition.
     */
    public static final boolean isBottleneck(BlockingEvent gcEvent, BlockingEvent priorEvent, int throughputThreshold)
            throws TimeWarpException {
        // Timestamp is the start of a garbage collection event; therefore, the
        // interval is from the
        // end of the prior event to the end of the current event.
        long interval = gcEvent.getTimestamp() + gcEvent.getDuration() - priorEvent.getTimestamp()
                - priorEvent.getDuration();

        // Verify data integrity
        if (gcEvent.getTimestamp() < (priorEvent.getTimestamp() + priorEvent.getDuration())) {
            throw new TimeWarpException("Event overlap: " + System.getProperty("line.separator")
                    + priorEvent.getLogEntry() + System.getProperty("line.separator") + gcEvent.getLogEntry());
        }
        if (interval <= 0) {
            throw new TimeWarpException("Negative interval: " + System.getProperty("line.separator")
                    + priorEvent.getLogEntry() + System.getProperty("line.separator") + gcEvent.getLogEntry());
        }

        // Determine the maximum duration for the given interval that meets the
        // throughput goal.
        BigDecimal durationThreshold = new BigDecimal(100 - throughputThreshold);
        durationThreshold = durationThreshold.movePointLeft(2);
        durationThreshold = durationThreshold.multiply(new BigDecimal(interval));
        durationThreshold.setScale(0, RoundingMode.DOWN);
        return (gcEvent.getDuration() > durationThreshold.intValue());
    }

    /**
     * Parse out the JVM option scalar value. For example, the value for <code>-Xss128k</code> is 128k. The value for
     * <code>-XX:PermSize=128M</code> is 128M.
     * 
     * @param option
     *            The JVM option.
     * @return The JVM option value.
     */
    public static final String getOptionValue(String option) {
        String value = null;
        if (option != null) {
            String regex = "^-[a-zA-Z:.]+(=)?(\\d{1,12}(" + JdkRegEx.OPTION_SIZE + ")?)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(option);
            if (matcher.find()) {
                value = matcher.group(2);
            }
        }
        return value;
    }

    /**
     * Convert SIZE_G1_DETAILS to SIZE_G1.
     * 
     * @param size
     *            The size (e.g. '128.0').
     * @param units
     *            The units (e.g. 'G').
     * @return The size block in G1 format (e.g. '131072M').
     */
    public static String convertSizeG1DetailsToSizeG1(final String size, final char units) {

        BigDecimal sizeG1 = new BigDecimal(size);
        char unitsG1;

        switch (units) {

        case 'B':
            // Convert to K
            sizeG1 = sizeG1.divide(new BigDecimal("1024"));
            unitsG1 = 'K';
            break;
        case 'K':
            unitsG1 = 'K';
            break;
        case 'M':
            unitsG1 = 'M';
            break;
        case 'G':
            // Convert to M
            sizeG1 = sizeG1.multiply(new BigDecimal("1024"));
            unitsG1 = 'M';
            break;
        default:
            throw new AssertionError("Unexpected units value: " + units);

        }
        sizeG1 = sizeG1.setScale(0, RoundingMode.HALF_EVEN);
        return Integer.toString(sizeG1.intValue()) + unitsG1;
    }

    /**
     * Convert JVM size option to bytes.
     * 
     * @param size
     *            The size in various units (e.g. 'k').
     * @return The size in bytes.
     */
    public static long convertOptionSizeToBytes(final String size) {

        String regex = "(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?";

        String value = null;
        char units = 'b';

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(size);
        if (matcher.find()) {
            value = matcher.group(1);
            if (matcher.group(2) != null) {
                units = matcher.group(2).charAt(0);
            }
        }

        BigDecimal bytes = new BigDecimal(value);

        switch (units) {

        case 'b':
        case 'B':
            // do nothing
            break;
        case 'k':
        case 'K':
            bytes = bytes.multiply(Constants.KILOBYTE);
            break;
        case 'm':
        case 'M':
            bytes = bytes.multiply(Constants.MEGABYTE);
            break;
        case 'g':
        case 'G':
            bytes = bytes.multiply(Constants.GIGABYTE);
            break;
        default:
            throw new AssertionError("Unexpected units value: " + units);

        }
        return bytes.longValue();
    }

    /**
     * @param eventType
     *            The event type to test.
     * @return true if the log event is should be included in the report event list, false otherwise.
     */
    public static final boolean isReportable(LogEventType eventType) {

        boolean reportable = true;

        switch (eventType) {
        case APPLICATION_STOPPED_TIME:
        case CLASS_HISTOGRAM:
        case FLS_STATISTICS:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VERSION:
        case LOG_FILE:
        case UNKNOWN:
            reportable = false;
            break;
        default:
            break;
        }

        return reportable;
    }
}
