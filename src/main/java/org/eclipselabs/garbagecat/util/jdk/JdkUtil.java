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
package org.eclipselabs.garbagecat.util.jdk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkWithClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1ConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGCEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1MixedPause;
import org.eclipselabs.garbagecat.domain.jdk.G1RemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungPause;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewCmsConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewCmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailureEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailurePermDataEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsConcurrentModeFailureEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsSerialOldPermDataEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedTruncatedEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelOldCompactingEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.PrintReferenceGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialSerialOldPermDataEvent;
import org.eclipselabs.garbagecat.domain.jdk.TruncatedEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcYoungEvent;
import org.eclipselabs.garbagecat.preprocess.ApplicationLoggingPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ThreadDumpPreprocessAction;
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
        SERIAL_OLD, SERIAL, PAR_NEW_CONCURRENT_MODE_FAILURE, PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA,
        //
        PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD, PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA,
        //
        PAR_NEW_PROMOTION_FAILED, PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE,
        //
        PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA, PAR_NEW, PAR_NEW_CMS_CONCURRENT,
        //
        PAR_NEW_CMS_SERIAL_OLD, PARALLEL_SERIAL_OLD, PARALLEL_SCAVENGE, PARALLEL_OLD_COMPACTING, CMS_SERIAL_OLD,
        //
        CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE, CMS_REMARK_WITH_CLASS_UNLOADING, CMS_REMARK, CMS_INITIAL_MARK,
        //
        CMS_CONCURRENT, APPLICATION_CONCURRENT_TIME, APPLICATION_STOPPED_TIME, UNKNOWN, SERIAL_SERIAL_OLD,
        //
        SERIAL_SERIAL_OLD_PERM_DATA, VERBOSE_GC_YOUNG, VERBOSE_GC_OLD, TRUNCATED, PAR_NEW_PROMOTION_FAILED_TRUNCATED,
        //
        G1_YOUNG_PAUSE, G1_MIXED_PAUSE, G1_CONCURRENT, G1_YOUNG_INITIAL_MARK, G1_REMARK, G1_CLEANUP, G1_FULL_GC,
        //
        HEADER_COMMAND_LINE_FLAGS, HEADER_MEMORY, HEADER_VERSION, PRINT_REFERENCE_GC
    };

    /**
     * Defined preprocessing actions.
     */
    public enum PreprocessActionType {
        APPLICATION_CONCURRENT_TIME, APPLICATION_LOGGING, APPLICATION_STOPPED_TIME, CMS_CONCURRENT_MODE_FAILURE,
        //
        DATE_STAMP, DATE_STAMP_PREFIX, GC_TIME_LIMIT_EXCEEDED, PAR_NEW_CMS_CONCURRENT, PRINT_HEAP_AT_GC,
        //
        PRINT_TENURING_DISTRIBUTION, THREAD_DUMP, UNLOADING_CLASS, G1, CMS
    };

    /**
     * Defined triggers.
     */
    public enum TriggerType {
        SYSTEM_GC, METADATA_GC_THRESHOLD, ALLOCATION_FAILURE, UNDEFINED, UNKNOWN, TO_SPACE_EXHAUSTED,
        //
        G1_EVACUATION_PAUSE, GCLOCATER_INITIATED_GC, CMS_INITIAL_MARK, CMS_FINAL_REMARK;
    };

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
        if (ParallelScavengeEvent.match(logLine))
            return LogEventType.PARALLEL_SCAVENGE;
        if (ParNewEvent.match(logLine))
            return LogEventType.PAR_NEW;
        if (ParNewCmsConcurrentEvent.match(logLine))
            return LogEventType.PAR_NEW_CMS_CONCURRENT;
        if (ParallelSerialOldEvent.match(logLine))
            return LogEventType.PARALLEL_SERIAL_OLD;
        if (ParallelOldCompactingEvent.match(logLine))
            return LogEventType.PARALLEL_OLD_COMPACTING;
        if (SerialOldEvent.match(logLine))
            return LogEventType.SERIAL_OLD;
        if (CmsSerialOldEvent.match(logLine))
            return LogEventType.CMS_SERIAL_OLD;
        if (CmsSerialOldConcurrentModeFailureEvent.match(logLine))
            return LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE;
        if (CmsInitialMarkEvent.match(logLine))
            return LogEventType.CMS_INITIAL_MARK;
        if (CmsRemarkEvent.match(logLine))
            return LogEventType.CMS_REMARK;
        if (CmsRemarkWithClassUnloadingEvent.match(logLine))
            return LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING;
        if (ParNewPromotionFailedCmsSerialOldEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD;
        if (ParNewPromotionFailedCmsSerialOldPermDataEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA;
        if (ParNewPromotionFailedEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED;
        if (ParNewPromotionFailedCmsConcurrentModeFailureEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE;
        if (ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA;
        if (ParNewConcurrentModeFailureEvent.match(logLine))
            return LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE;
        if (ParNewConcurrentModeFailurePermDataEvent.match(logLine))
            return LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA;
        if (ParNewCmsSerialOldEvent.match(logLine))
            return LogEventType.PAR_NEW_CMS_SERIAL_OLD;
        if (SerialEvent.match(logLine))
            return LogEventType.SERIAL;
        if (SerialSerialOldEvent.match(logLine))
            return LogEventType.SERIAL_SERIAL_OLD;
        if (SerialSerialOldPermDataEvent.match(logLine))
            return LogEventType.SERIAL_SERIAL_OLD_PERM_DATA;
        if (CmsConcurrentEvent.match(logLine))
            return LogEventType.CMS_CONCURRENT;
        if (ApplicationConcurrentTimeEvent.match(logLine))
            return LogEventType.APPLICATION_CONCURRENT_TIME;
        if (ApplicationStoppedTimeEvent.match(logLine))
            return LogEventType.APPLICATION_STOPPED_TIME;
        if (VerboseGcYoungEvent.match(logLine))
            return LogEventType.VERBOSE_GC_YOUNG;
        if (VerboseGcOldEvent.match(logLine))
            return LogEventType.VERBOSE_GC_OLD;
        if (TruncatedEvent.match(logLine))
            return LogEventType.TRUNCATED;
        if (ParNewPromotionFailedTruncatedEvent.match(logLine))
            return LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED;
        if (G1YoungPause.match(logLine))
            return LogEventType.G1_YOUNG_PAUSE;
        if (G1MixedPause.match(logLine))
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
        if (HeaderCommandLineFlagsEvent.match(logLine))
            return LogEventType.HEADER_COMMAND_LINE_FLAGS;
        if (HeaderMemoryEvent.match(logLine))
            return LogEventType.HEADER_MEMORY;
        if (HeaderVersionEvent.match(logLine))
            return LogEventType.HEADER_VERSION;
        if (PrintReferenceGcEvent.match(logLine))
            return LogEventType.PRINT_REFERENCE_GC;

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
        case PARALLEL_SCAVENGE:
            event = new ParallelScavengeEvent(logLine);
            break;
        case PAR_NEW:
            event = new ParNewEvent(logLine);
            break;
        case PAR_NEW_CMS_CONCURRENT:
            event = new ParNewCmsConcurrentEvent(logLine);
            break;
        case PARALLEL_SERIAL_OLD:
            event = new ParallelSerialOldEvent(logLine);
            break;
        case PARALLEL_OLD_COMPACTING:
            event = new ParallelOldCompactingEvent(logLine);
            break;
        case SERIAL_OLD:
            event = new SerialOldEvent(logLine);
            break;
        case CMS_SERIAL_OLD:
            event = new CmsSerialOldEvent(logLine);
            break;
        case CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE:
            event = new CmsSerialOldConcurrentModeFailureEvent(logLine);
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
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD:
            event = new ParNewPromotionFailedCmsSerialOldEvent(logLine);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA:
            event = new ParNewPromotionFailedCmsSerialOldPermDataEvent(logLine);
            break;
        case PAR_NEW_PROMOTION_FAILED:
            event = new ParNewPromotionFailedEvent(logLine);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE:
            event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logLine);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA:
            event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logLine);
            break;
        case PAR_NEW_CONCURRENT_MODE_FAILURE:
            event = new ParNewConcurrentModeFailureEvent(logLine);
            break;
        case PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA:
            event = new ParNewConcurrentModeFailurePermDataEvent(logLine);
            break;
        case PAR_NEW_CMS_SERIAL_OLD:
            event = new ParNewCmsSerialOldEvent(logLine);
            break;
        case SERIAL:
            event = new SerialEvent(logLine);
            break;
        case SERIAL_SERIAL_OLD:
            event = new SerialSerialOldEvent(logLine);
            break;
        case SERIAL_SERIAL_OLD_PERM_DATA:
            event = new SerialSerialOldPermDataEvent(logLine);
            break;
        case CMS_CONCURRENT:
            event = new CmsConcurrentEvent();
            break;
        case APPLICATION_CONCURRENT_TIME:
            event = new ApplicationConcurrentTimeEvent();
            break;
        case APPLICATION_STOPPED_TIME:
            event = new ApplicationStoppedTimeEvent(logLine);
            break;
        case VERBOSE_GC_YOUNG:
            event = new VerboseGcYoungEvent(logLine);
            break;
        case VERBOSE_GC_OLD:
            event = new VerboseGcOldEvent(logLine);
            break;
        case TRUNCATED:
            event = new TruncatedEvent(logLine);
            break;
        case PAR_NEW_PROMOTION_FAILED_TRUNCATED:
            event = new ParNewPromotionFailedTruncatedEvent(logLine);
            break;
        case G1_YOUNG_PAUSE:
            event = new G1YoungPause(logLine);
            break;
        case G1_MIXED_PAUSE:
            event = new G1MixedPause(logLine);
            break;
        case G1_CONCURRENT:
            event = new G1ConcurrentEvent(logLine);
            break;
        case G1_YOUNG_INITIAL_MARK:
            event = new G1YoungInitialMarkEvent(logLine);
            break;
        case G1_REMARK:
            event = new G1RemarkEvent(logLine);
            break;
        case G1_CLEANUP:
            event = new G1CleanupEvent(logLine);
            break;
        case G1_FULL_GC:
            event = new G1FullGCEvent(logLine);
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
        case PRINT_REFERENCE_GC:
            event = new PrintReferenceGcEvent(logLine);
            break;
        case UNKNOWN:
            event = new UnknownEvent(logLine);
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
        case PARALLEL_SCAVENGE:
            event = new ParallelScavengeEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW:
            event = new ParNewEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_CMS_CONCURRENT:
            event = new ParNewCmsConcurrentEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_SERIAL_OLD:
            event = new ParallelSerialOldEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_OLD_COMPACTING:
            event = new ParallelOldCompactingEvent(logEntry, timestamp, duration);
            break;
        case SERIAL_OLD:
            event = new SerialOldEvent(logEntry, timestamp, duration);
            break;
        case CMS_SERIAL_OLD:
            event = new CmsSerialOldEvent(logEntry, timestamp, duration);
            break;
        case CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE:
            event = new CmsSerialOldConcurrentModeFailureEvent(logEntry, timestamp, duration);
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
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD:
            event = new ParNewPromotionFailedCmsSerialOldEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA:
            event = new ParNewPromotionFailedCmsSerialOldPermDataEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE:
            event = new ParNewPromotionFailedCmsConcurrentModeFailureEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA:
            event = new ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_CONCURRENT_MODE_FAILURE:
            event = new ParNewConcurrentModeFailureEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA:
            event = new ParNewConcurrentModeFailurePermDataEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_PROMOTION_FAILED:
            event = new ParNewPromotionFailedEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_CMS_SERIAL_OLD:
            event = new ParNewCmsSerialOldEvent(logEntry, timestamp, duration);
            break;
        case SERIAL:
            event = new SerialEvent(logEntry, timestamp, duration);
            break;
        case SERIAL_SERIAL_OLD:
            event = new SerialSerialOldEvent(logEntry, timestamp, duration);
            break;
        case SERIAL_SERIAL_OLD_PERM_DATA:
            event = new SerialSerialOldPermDataEvent(logEntry, timestamp, duration);
            break;
        case VERBOSE_GC_YOUNG:
            event = new VerboseGcYoungEvent(logEntry, timestamp, duration);
            break;
        case VERBOSE_GC_OLD:
            event = new VerboseGcOldEvent(logEntry, timestamp, duration);
            break;
        case PAR_NEW_PROMOTION_FAILED_TRUNCATED:
            event = new ParNewPromotionFailedTruncatedEvent(logEntry, timestamp, duration);
            break;
        case G1_YOUNG_PAUSE:
            event = new G1YoungPause(logEntry, timestamp, duration);
            break;
        case G1_MIXED_PAUSE:
            event = new G1MixedPause(logEntry, timestamp, duration);
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
        default:
            throw new AssertionError("Unexpected event type value: " + eventType + ": " + logEntry);
        }
        return event;
    }

    /**
     * @param eventType
     * @return true if the log event is blocking, false if it is concurrent or informational.
     */
    public static final boolean isBlocking(LogEventType eventType) {
        return !(eventType == JdkUtil.LogEventType.CMS_CONCURRENT || eventType == JdkUtil.LogEventType.G1_CONCURRENT
                || eventType == JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME
                || eventType == JdkUtil.LogEventType.APPLICATION_STOPPED_TIME
                || eventType == JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS
                || eventType == JdkUtil.LogEventType.HEADER_MEMORY || eventType == JdkUtil.LogEventType.HEADER_VERSION
                || eventType == JdkUtil.LogEventType.UNKNOWN);
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
     * Check to see if a log line should be discarded or kept for analysis.
     * 
     * @param logLine
     *            The log line.
     * @return True if the log line is not related to garbage collection logging or can otherwise be discarded, false
     *         if the log line should be retained.
     */
    public static final boolean discardLogLine(String logLine) {
        return ThreadDumpPreprocessAction.match(logLine) || ApplicationLoggingPreprocessAction.match(logLine)
                || logLine.length() == 0 || logLine.matches(JdkRegEx.BLANK_LINE);
    }

    /**
     * Determine if the garbage collection event should be classified as a bottleneck.
     * 
     * @param gcEvent
     *            Current garbage collection event.
     * @param priorEvent
     *            Previous garbage collection event.
     * @param throughputThreshold
     *            Throughput threshold (percent of time spent not doing garbage collection for a given time interval)
     *            to be considered a bottleneck. Whole number 0-100.
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
            System.out.println("prior event: " + priorEvent.getLogEntry());
            throw new TimeWarpException("Event overlap: " + gcEvent.getLogEntry());
        }
        if (interval <= 0) {
            throw new TimeWarpException("Negative interval: " + gcEvent.getLogEntry());
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
     * Convert {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #SIZE_G1_DETAILS} to
     * {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #SIZE_G1}.
     * 
     * @param size
     *            The size in {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #SIZE_G1_DETAILS} format (e.g.
     *            '128.0').
     * @param units
     *            The units in {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #SIZE_G1_DETAILS} format (e.g.
     *            'G').
     * @return The size block in {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #SIZE_G1} format (e.g.
     *         '131072M').
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
     * Convert {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #OPTION_SIZE} to bytes.
     * 
     * @param size
     *            The size in {@value org.eclipselabs.garbagecat.util.jdk.JdkRegEx #OPTION_SIZE} format (e.g. '128k').
     * @return The size in bytes.
     */
    public static long convertOptionSizeToBytes(final String size) {

        String regex = "(\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?";

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
     * Identify the log line garbage collection event.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @return True if the <code>LogEventType</code> is G1, false otherwise.
     */
    public static final boolean isG1LogEventType(LogEventType eventType) {
        boolean isG1 = false;

        switch (eventType) {
        case G1_YOUNG_PAUSE:
        case G1_MIXED_PAUSE:
        case G1_YOUNG_INITIAL_MARK:
        case G1_REMARK:
        case G1_CLEANUP:
        case G1_FULL_GC:
            isG1 = true;
        default:
            break;
        }

        return isG1;
    }
    
    /**
     * Identify the log line garbage collection event.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @return True if the <code>LogEventType</code> is CMS, false otherwise.
     */
    public static final boolean isCmsLogEventType(LogEventType eventType) {
        boolean isCms = false;

        switch (eventType) {
        case CMS_CONCURRENT:
        case CMS_SERIAL_OLD:
        case CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE:
        case CMS_INITIAL_MARK:
        case CMS_REMARK:
        case CMS_REMARK_WITH_CLASS_UNLOADING: 
        case PAR_NEW:
        case PAR_NEW_CMS_CONCURRENT:
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD:
        case PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA:
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE:
        case PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA:
        case PAR_NEW_CONCURRENT_MODE_FAILURE:
        case PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA:
        case PAR_NEW_PROMOTION_FAILED:
        case PAR_NEW_CMS_SERIAL_OLD:
        case PAR_NEW_PROMOTION_FAILED_TRUNCATED:                        
            isCms = true;
        default:
            break;
        }

        return isCms;
    }
}
