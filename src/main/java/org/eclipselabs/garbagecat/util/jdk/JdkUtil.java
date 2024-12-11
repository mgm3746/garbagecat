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
import org.eclipselabs.garbagecat.domain.NullEvent;
import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationConcurrentTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent;
import org.eclipselabs.garbagecat.domain.jdk.ClassUnloadingEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.FlsStatisticsEvent;
import org.eclipselabs.garbagecat.domain.jdk.FooterStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1ConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1MixedPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1RemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcInfoEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcLockerScavengeFailedEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVmInfoEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapEvent;
import org.eclipselabs.garbagecat.domain.jdk.LogFileEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahTriggerEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.domain.jdk.ThreadDumpEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.OomeMetaspaceEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedBlankLineEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedCmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedFooterStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1FullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1InfoEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1MixedPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPrepareMixedEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedGcLockerRetryEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedHeaderEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedHeapDumpAfterFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedHeapDumpBeforeFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedHeapEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedLogging;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahCancellingGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahDegeneratedGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalEvacEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalRootsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalUpdateRefsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahInitMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahInitUpdateRefsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahTriggerEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.VmWarningEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZAllocationStallEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkEndEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkEndOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkEndYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkStartEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkStartYoungAndOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZMarkStartYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZRelocateStartEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZRelocateStartOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZRelocateStartYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZRelocationStallEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZStatsEvent;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * Utility methods and constants for OpenJDK and Oracle JDK.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public final class JdkUtil {

    /**
     * Defined collector families.
     * 
     * TODO: Move to joa?
     */
    public enum CollectorFamily {
        CMS, G1, PARALLEL, SERIAL, SHENANDOAH, UNKNOWN, Z
    }

    /**
     * Defined logging events.
     */
    public enum LogEventType {
        APPLICATION_CONCURRENT_TIME, APPLICATION_LOGGING, APPLICATION_STOPPED_TIME, BLANK_LINE, CLASS_HISTOGRAM,
        //
        CLASS_UNLOADING, CMS_CONCURRENT, CMS_INITIAL_MARK, CMS_REMARK, CMS_SERIAL_OLD, FLS_STATISTICS, FOOTER_STATS,
        //
        G1_CLEANUP, G1_CONCURRENT, G1_FULL_GC_SERIAL, G1_MIXED_PAUSE, G1_REMARK, G1_YOUNG_INITIAL_MARK, G1_YOUNG_PAUSE,
        //
        GC_INFO, GC_LOCKER_RETRY_LIMIT, GC_LOCKER_SCAVENGE_FAILED, GC_OVERHEAD_LIMIT, HEADER_COMMAND_LINE_FLAGS,
        //
        HEADER_MEMORY, HEADER_VM_INFO, HEAP, HEAP_ADDRESS, HEAP_AT_GC, HEAP_REGION_SIZE, LOG_FILE, NULL,
        //
        OOME_METASPACE, PAR_NEW, PARALLEL_COMPACTING_OLD, PARALLEL_SCAVENGE, PARALLEL_SERIAL_OLD, SERIAL_NEW,
        //
        SERIAL_OLD, SHENANDOAH_CONCURRENT, SHENANDOAH_DEGENERATED_GC, SHENANDOAH_FINAL_EVAC, SHENANDOAH_FINAL_MARK,
        //
        SHENANDOAH_FINAL_UPDATE, SHENANDOAH_FULL_GC, SHENANDOAH_INIT_MARK, SHENANDOAH_INIT_UPDATE, SHENANDOAH_STATS,
        //
        SHENANDOAH_TRIGGER, TENURING_DISTRIBUTION, THREAD_DUMP, UNIFIED_BLANK_LINE, UNIFIED_CMS_INITIAL_MARK,
        //
        UNIFIED_CONCURRENT, UNIFIED_FOOTER_STATS, UNIFIED_G1_CLEANUP, UNIFIED_G1_FULL_GC_PARALLEL, UNIFIED_G1_INFO,
        //
        UNIFIED_G1_MIXED_PAUSE, UNIFIED_G1_YOUNG_INITIAL_MARK, UNIFIED_G1_YOUNG_PAUSE, UNIFIED_G1_YOUNG_PREPARE_MIXED,
        //
        UNIFIED_GC_LOCKER_RETRY, UNIFIED_HEADER, UNIFIED_HEAP, UNIFIED_HEAP_DUMP_AFTER_FULL_GC,
        //
        UNIFIED_HEAP_DUMP_BEFORE_FULL_GC, UNIFIED_OLD, UNIFIED_PAR_NEW, UNIFIED_PARALLEL_COMPACTING_OLD,
        //
        UNIFIED_PARALLEL_SCAVENGE, UNIFIED_REMARK, UNIFIED_SAFEPOINT, UNIFIED_SERIAL_NEW, UNIFIED_SERIAL_OLD,
        //
        UNIFIED_SHENANDOAH_CANCELLING_GC, UNIFIED_SHENANDOAH_DEGENERATED_GC, UNIFIED_SHENANDOAH_FINAL_EVAC,
        //
        UNIFIED_SHENANDOAH_FINAL_MARK, UNIFIED_SHENANDOAH_FINAL_ROOTS, UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS,
        //
        UNIFIED_SHENANDOAH_FULL_GC, UNIFIED_SHENANDOAH_INIT_MARK, UNIFIED_SHENANDOAH_INIT_UPDATE_REFS,
        //
        UNIFIED_SHENANDOAH_STATS, UNIFIED_SHENANDOAH_TRIGGER, UNIFIED_YOUNG, UNKNOWN, VERBOSE_GC_OLD, VERBOSE_GC_YOUNG,
        //
        VM_WARNING, Z_ALLOCATION_STALL, Z_CONCURRENT, Z_MARK_END, Z_MARK_END_OLD, Z_MARK_END_YOUNG, Z_MARK_START,
        //
        Z_MARK_START_YOUNG, Z_MARK_START_YOUNG_AND_OLD, Z_RELOCATE_START, Z_RELOCATE_START_OLD, Z_RELOCATE_START_YOUNG,
        //
        Z_RELOCATION_STALL, Z_STATS
    }

    /**
     * Defined preprocessing actions.
     */
    public enum PreprocessActionType {
        APPLICATION_STOPPED_TIME, CMS, DATE_STAMP, G1, PARALLEL, SERIAL, SHENANDOAH, UNIFIED
    }

    /**
     * The number of regex patterns in <code>UnifiedRegEx.DECORATOR</code>. Convenience field to make the code resilient
     * to decorator pattern changes.
     */
    public static final int DECORATOR_SIZE = Pattern.compile(JdkRegEx.DECORATOR)
            .matcher("2020-03-10T08:03:29.311-0400: 0.373:").groupCount();

    /**
     * Convert datestamp to milliseconds from a point in time.
     * 
     * @param datestamp
     *            Absolute date/time.
     * @return Milliseconds from a point in time.
     */
    public static long convertDatestampToMillis(String datestamp) {
        // Calculate uptimemillis from random date/time
        Date eventDate = GcUtil.parseDateStamp(datestamp);
        return GcUtil.dateDiff(GcUtil.JVM_START_DATE, eventDate);
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            matcher.appendReplacement(sb, formatter.format(date) + matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static final LogEventType determineEventType(String eventTypeString) {
        LogEventType[] logEventTypes = LogEventType.values();
        for (LogEventType logEventType : logEventTypes) {
            if (logEventType.toString().equals(eventTypeString)) {
                return logEventType;
            }
        }
        return null;
    }

    /**
     * Get log line decorator.
     * 
     * @param logLine
     *            The log line.
     * @return The log line decorator, null otherwise.
     */
    public static final String getDecorator(String logLine) {
        String decorator = null;
        if (logLine != null) {
            String regex = "^(" + JdkRegEx.DECORATOR + ")(.*)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(logLine);
            decorator = matcher.find() ? matcher.group(1) : null;
        }
        return decorator;
    }

    /**
     * Create <code>BlockingEvent</code> from values.
     * 
     * @param eventType
     *            Log entry <code>LogEventType</code>.
     * @param logEntry
     *            Log entry.
     * @param timestamp
     *            Log entry timestamp (milliseconds after JVM startup).
     * @param duration
     *            The duration of the log event in microseconds (rounded)
     * @return The <code>BlockingEvent</code> for the given event values.
     */
    public static final BlockingEvent hydrateBlockingEvent(LogEventType eventType, String logEntry, long timestamp,
            int duration) {
        switch (eventType) {

        // Unified (alphabetical)
        case UNIFIED_CMS_INITIAL_MARK:
            return new UnifiedCmsInitialMarkEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_CLEANUP:
            return new UnifiedG1CleanupEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_FULL_GC_PARALLEL:
            return new UnifiedG1FullGcEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_YOUNG_INITIAL_MARK:
            return new UnifiedG1YoungInitialMarkEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_MIXED_PAUSE:
            return new UnifiedG1MixedPauseEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_YOUNG_PAUSE:
            return new UnifiedG1YoungPauseEvent(logEntry, timestamp, duration);
        case UNIFIED_G1_YOUNG_PREPARE_MIXED:
            return new UnifiedG1YoungPrepareMixedEvent(logEntry, timestamp, duration);
        case UNIFIED_OLD:
            return new UnifiedOldEvent(logEntry, timestamp, duration);
        case UNIFIED_PARALLEL_COMPACTING_OLD:
            return new UnifiedParallelCompactingOldEvent(logEntry, timestamp, duration);
        case UNIFIED_PARALLEL_SCAVENGE:
            return new UnifiedParallelScavengeEvent(logEntry, timestamp, duration);
        case UNIFIED_PAR_NEW:
            return new UnifiedParNewEvent(logEntry, timestamp, duration);
        case UNIFIED_REMARK:
            return new UnifiedRemarkEvent(logEntry, timestamp, duration);
        case UNIFIED_SERIAL_NEW:
            return new UnifiedSerialNewEvent(logEntry, timestamp, duration);
        case UNIFIED_SERIAL_OLD:
            return new UnifiedSerialOldEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_DEGENERATED_GC:
            return new UnifiedShenandoahDegeneratedGcEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_FINAL_EVAC:
            return new UnifiedShenandoahFinalEvacEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_FINAL_MARK:
            return new UnifiedShenandoahFinalMarkEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS:
            return new UnifiedShenandoahFinalUpdateRefsEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_FULL_GC:
            return new UnifiedShenandoahFullGcEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_INIT_MARK:
            return new UnifiedShenandoahInitMarkEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_INIT_UPDATE_REFS:
            return new UnifiedShenandoahInitUpdateRefsEvent(logEntry, timestamp, duration);
        case UNIFIED_YOUNG:
            return new UnifiedYoungEvent(logEntry, timestamp, duration);

        // G1
        case G1_YOUNG_PAUSE:
            return new G1YoungPauseEvent(logEntry, timestamp, duration);
        case G1_MIXED_PAUSE:
            return new G1MixedPauseEvent(logEntry, timestamp, duration);
        case G1_YOUNG_INITIAL_MARK:
            return new G1YoungInitialMarkEvent(logEntry, timestamp, duration);
        case G1_REMARK:
            return new G1RemarkEvent(logEntry, timestamp, duration);
        case G1_CLEANUP:
            return new G1CleanupEvent(logEntry, timestamp, duration);
        case G1_FULL_GC_SERIAL:
            return new G1FullGcEvent(logEntry, timestamp, duration);
        // Shenandoah
        case SHENANDOAH_DEGENERATED_GC:
            return new ShenandoahDegeneratedGcEvent(logEntry, timestamp, duration);
        case SHENANDOAH_FINAL_EVAC:
            return new ShenandoahFinalEvacEvent(logEntry, timestamp, duration);
        case SHENANDOAH_FINAL_MARK:
            return new ShenandoahFinalMarkEvent(logEntry, timestamp, duration);
        case UNIFIED_SHENANDOAH_FINAL_ROOTS:
            return new UnifiedShenandoahFinalRootsEvent(logEntry, timestamp, duration);
        case SHENANDOAH_FINAL_UPDATE:
            return new ShenandoahFinalUpdateEvent(logEntry, timestamp, duration);
        case SHENANDOAH_FULL_GC:
            return new ShenandoahFullGcEvent(logEntry, timestamp, duration);
        case SHENANDOAH_INIT_MARK:
            return new ShenandoahInitMarkEvent(logEntry, timestamp, duration);
        case SHENANDOAH_INIT_UPDATE:
            return new ShenandoahInitUpdateEvent(logEntry, timestamp, duration);
        // CMS
        case PAR_NEW:
            return new ParNewEvent(logEntry, timestamp, duration);
        case CMS_SERIAL_OLD:
            return new CmsSerialOldEvent(logEntry, timestamp, duration);
        case CMS_INITIAL_MARK:
            return new CmsInitialMarkEvent(logEntry, timestamp, duration);
        case CMS_REMARK:
            return new CmsRemarkEvent(logEntry, timestamp, duration);
        // Parallel
        case PARALLEL_SCAVENGE:
            return new ParallelScavengeEvent(logEntry, timestamp, duration);
        case PARALLEL_SERIAL_OLD:
            return new ParallelSerialOldEvent(logEntry, timestamp, duration);
        case PARALLEL_COMPACTING_OLD:
            return new ParallelCompactingOldEvent(logEntry, timestamp, duration);
        // Serial
        case SERIAL_OLD:
            return new SerialOldEvent(logEntry, timestamp, duration);
        case SERIAL_NEW:
            return new SerialNewEvent(logEntry, timestamp, duration);
        // Other
        case VERBOSE_GC_YOUNG:
            return new VerboseGcYoungEvent(logEntry, timestamp, duration);
        case VERBOSE_GC_OLD:
            return new VerboseGcOldEvent(logEntry, timestamp, duration);
        default:
            throw new AssertionError("Unexpected event type value: " + eventType + ": " + logEntry);
        }
    }

    /**
     * Identify the log line garbage collection event.
     * 
     * @param logLine
     *            The log line.
     * @param priorLogEvent
     *            The prior log line <code>LogEvent</code>.
     * @param collectorFamily
     *            The <code>CollectorFamily</code>.
     * @return The <code>LogEventType</code> of the log entry.
     */
    public static final LogEventType identifyEventType(String logLine, LogEvent priorLogEvent,
            CollectorFamily collectorFamily) {
        LogEventType logEventType = LogEventType.UNKNOWN;
        if (priorLogEvent instanceof UnifiedLogging) {
            // Unified
            logEventType = UnifiedUtil.identifyEventType(logLine, priorLogEvent, collectorFamily);
        } else if (priorLogEvent == null || priorLogEvent instanceof NullEvent
                || priorLogEvent instanceof UnknownEvent) {
            // Unknown
            logEventType = UnifiedUtil.identifyEventType(logLine, priorLogEvent, collectorFamily);
            if (logEventType == LogEventType.UNKNOWN) {
                logEventType = identifyLegacyEventType(logLine, priorLogEvent);
            }
        } else {
            // Legacy
            logEventType = identifyLegacyEventType(logLine, priorLogEvent);
        }
        return logEventType;
    }

    /**
     * Identify the log line garbage collection event.
     * 
     * @param logLine
     *            The log line.
     * @param priorLogEvent
     *            The prior log line <code>LogEvent</code>.
     * @return The <code>LogEventType</code> of the log entry.
     */
    public static final LogEventType identifyLegacyEventType(String logLine, LogEvent priorLogEvent) {

        // In order of most common events to limit checking

        // Unknown collector (has to go 1st)
        if (VerboseGcYoungEvent.match(logLine))
            return LogEventType.VERBOSE_GC_YOUNG;
        if (VerboseGcOldEvent.match(logLine))
            return LogEventType.VERBOSE_GC_OLD;

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
        if (G1FullGcEvent.match(logLine))
            return LogEventType.G1_FULL_GC_SERIAL;
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
        if (CmsConcurrentEvent.match(logLine))
            return LogEventType.CMS_CONCURRENT;

        // Parallel
        if (ParallelScavengeEvent.match(logLine))
            return LogEventType.PARALLEL_SCAVENGE;
        if (ParallelSerialOldEvent.match(logLine))
            return LogEventType.PARALLEL_SERIAL_OLD;
        if (ParallelCompactingOldEvent.match(logLine))
            return LogEventType.PARALLEL_COMPACTING_OLD;

        // Serial
        if (SerialOldEvent.match(logLine))
            return LogEventType.SERIAL_OLD;
        if (SerialNewEvent.match(logLine))
            return LogEventType.SERIAL_NEW;

        // Shenandoah
        if (UnifiedShenandoahCancellingGcEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_CANCELLING_GC;
        if (ShenandoahConcurrentEvent.match(logLine))
            return LogEventType.SHENANDOAH_CONCURRENT;
        if (ShenandoahDegeneratedGcEvent.match(logLine))
            return LogEventType.SHENANDOAH_DEGENERATED_GC;
        if (ShenandoahFinalEvacEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_EVAC;
        if (ShenandoahFinalMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_MARK;
        if (UnifiedShenandoahFinalRootsEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS;
        if (ShenandoahFinalUpdateEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_UPDATE;
        if (ShenandoahFullGcEvent.match(logLine))
            return LogEventType.SHENANDOAH_FULL_GC;
        if (ShenandoahInitMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_INIT_MARK;
        if (ShenandoahInitUpdateEvent.match(logLine))
            return LogEventType.SHENANDOAH_INIT_UPDATE;
        if (logLine.matches(ShenandoahStatsEvent._REGEX_HEADER)
                || (ShenandoahStatsEvent.match(logLine) && priorLogEvent instanceof ShenandoahStatsEvent))
            return LogEventType.SHENANDOAH_STATS;
        if (ShenandoahTriggerEvent.match(logLine))
            return LogEventType.SHENANDOAH_TRIGGER;

        // Other
        if (ApplicationConcurrentTimeEvent.match(logLine))
            return LogEventType.APPLICATION_CONCURRENT_TIME;
        if (ApplicationStoppedTimeEvent.match(logLine))
            return LogEventType.APPLICATION_STOPPED_TIME;
        if (ClassUnloadingEvent.match(logLine))
            return LogEventType.CLASS_UNLOADING;
        if (logLine.matches(FooterStatsEvent._REGEX_HEADER)
                || (FooterStatsEvent.match(logLine) && priorLogEvent instanceof FooterStatsEvent))
            return LogEventType.FOOTER_STATS;
        if (GcInfoEvent.match(logLine) && !(priorLogEvent instanceof UnifiedHeaderEvent))
            return LogEventType.GC_INFO;
        if (logLine.matches(HeapEvent._REGEX_HEADER)
                || (HeapEvent.match(logLine) && priorLogEvent instanceof HeapEvent))
            return LogEventType.HEAP;
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
        if (GcOverheadLimitEvent.match(logLine))
            return LogEventType.GC_OVERHEAD_LIMIT;
        if (FlsStatisticsEvent.match(logLine))
            return LogEventType.FLS_STATISTICS;
        if (GcLockerScavengeFailedEvent.match(logLine))
            return LogEventType.GC_LOCKER_SCAVENGE_FAILED;
        if (HeaderCommandLineFlagsEvent.match(logLine))
            return LogEventType.HEADER_COMMAND_LINE_FLAGS;
        if (HeaderMemoryEvent.match(logLine))
            return LogEventType.HEADER_MEMORY;
        if (HeaderVmInfoEvent.match(logLine))
            return LogEventType.HEADER_VM_INFO;
        if (VmWarningEvent.match(logLine))
            return LogEventType.VM_WARNING;
        if (BlankLineEvent.match(logLine))
            return LogEventType.BLANK_LINE;

        // no idea what event is
        return LogEventType.UNKNOWN;
    }

    /**
     * @param eventType
     *            The event type to test.
     * @return true if the log event is blocking, false if it is concurrent or informational.
     */
    public static final boolean isBlocking(LogEventType eventType) {
        switch (eventType) {
        case APPLICATION_CONCURRENT_TIME:
        case APPLICATION_STOPPED_TIME:
        case CLASS_HISTOGRAM:
        case CLASS_UNLOADING:
        case CMS_CONCURRENT:
        case FLS_STATISTICS:
        case UNIFIED_HEAP:
        case UNIFIED_HEAP_DUMP_AFTER_FULL_GC:
        case UNIFIED_HEAP_DUMP_BEFORE_FULL_GC:
        case FOOTER_STATS:
        case G1_CONCURRENT:
        case GC_INFO:
        case GC_LOCKER_SCAVENGE_FAILED:
        case GC_OVERHEAD_LIMIT:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VM_INFO:
        case HEAP:
        case HEAP_ADDRESS:
        case HEAP_AT_GC:
        case HEAP_REGION_SIZE:
        case LOG_FILE:
        case UNIFIED_SHENANDOAH_CANCELLING_GC:
        case SHENANDOAH_CONCURRENT:
        case SHENANDOAH_STATS:
        case SHENANDOAH_TRIGGER:
        case THREAD_DUMP:
        case TENURING_DISTRIBUTION:
        case UNIFIED_CONCURRENT:
        case UNIFIED_FOOTER_STATS:
        case UNIFIED_G1_INFO:
        case UNIFIED_GC_LOCKER_RETRY:
        case UNIFIED_HEADER:
        case UNIFIED_SAFEPOINT:
        case UNIFIED_SHENANDOAH_STATS:
        case UNIFIED_SHENANDOAH_TRIGGER:
        case UNKNOWN:
        case VM_WARNING:
        case Z_ALLOCATION_STALL:
        case Z_RELOCATION_STALL:
        case Z_STATS:
            return false;
        default:
            return true;
        }

    }

    /**
     * Determine if the <code>SafepointEvent</code> should be classified as a bottleneck.
     * 
     * @param event
     *            Current <code>SafepointEvent</code>.
     * @param priorEvent
     *            Previous <code>SafepointEvent</code>.
     * @param throughputThreshold
     *            Throughput threshold (percent of time spent not doing garbage collection for a given time interval) to
     *            be considered a bottleneck. Whole number 0-100.
     * @return True if the <code>SafepointEvent</code> meets the bottleneck definition.
     */
    public static final boolean isBottleneck(SafepointEvent event, SafepointEvent priorEvent, int throughputThreshold)
            throws TimeWarpException {
        boolean isBottleneck = false;
        /*
         * Check for logging time warps, which could be an indication of mixed logging from multiple JVM runs. JDK8
         * seems to have threading issues where sometimes logging gets mixed up under heavy load, and an event appears
         * to start before the previous event finished. They are mainly very small overlaps or a few milliseconds.
         */
        long eventTimestampNanos = JdkMath.convertMillisToNanos(String.valueOf(event.getTimestamp())).longValue();
        // Exclude <code>ApplicationStoppedTime</code> w/o datestamp/timestamp
        // Exclude microevents where timestamps are equal (for report readability)
        if (eventTimestampNanos > 0 && event.getTimestamp() != priorEvent.getTimestamp()) {
            long priorEventTimestampNanos = JdkMath.convertMillisToNanos(String.valueOf(priorEvent.getTimestamp()))
                    .longValue();
            long priorEventDurationNanos;
            if (priorEvent instanceof UnifiedSafepointEvent) {
                priorEventDurationNanos = priorEvent.getDurationMicros();
            } else {
                priorEventDurationNanos = JdkMath.convertMicrosToNanos(priorEvent.getDurationMicros()).longValue();
            }
            if (eventTimestampNanos < priorEventTimestampNanos - 1000000L) {
                // Only report if 2nd event > 1 millisecond before 1st event
                throw new TimeWarpException("Bad order: " + Constants.LINE_SEPARATOR + priorEvent.getLogEntry()
                        + Constants.LINE_SEPARATOR + event.getLogEntry());
            } else if (eventTimestampNanos < priorEventTimestampNanos + priorEventDurationNanos - 5000000000L) {
                // Only report if overlap > 5 sec to account for overlaps due to JDK threading issues and use of
                // -XX:+UseFastUnorderedTimeStamps
                // TODO: Make this configurable w/ a command line option?
                throw new TimeWarpException("Event overlap: " + Constants.LINE_SEPARATOR + priorEvent.getLogEntry()
                        + Constants.LINE_SEPARATOR + event.getLogEntry());
            } else {
                long eventDurationNanos;
                if (event instanceof UnifiedSafepointEvent) {
                    eventDurationNanos = event.getDurationMicros();
                } else {
                    eventDurationNanos = JdkMath.convertMicrosToNanos(event.getDurationMicros()).longValue();
                }
                /*
                 * Timestamp is the start of an event; therefore, the interval is from the prior event timestamp to the
                 * current event endstamp.
                 */
                long interval = eventTimestampNanos + eventDurationNanos - priorEventTimestampNanos;
                // Determine the maximum duration for the given interval that meets the throughput goal.
                BigDecimal durationThresholdNanos = new BigDecimal(100 - throughputThreshold);
                durationThresholdNanos = durationThresholdNanos.movePointLeft(2);
                durationThresholdNanos = durationThresholdNanos.multiply(new BigDecimal(interval));
                durationThresholdNanos.setScale(0, RoundingMode.DOWN);
                isBottleneck = (eventDurationNanos + priorEventDurationNanos) > durationThresholdNanos.longValue();
            }
        }
        return isBottleneck;
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
     * @param eventType
     *            The event type to test.
     * @return true if the log event is should be included in the report event list, false otherwise.
     */
    public static final boolean isReportable(LogEventType eventType) {
        switch (eventType) {
        case APPLICATION_CONCURRENT_TIME:
        case APPLICATION_LOGGING:
        case APPLICATION_STOPPED_TIME:
        case BLANK_LINE:
        case CLASS_HISTOGRAM:
        case CLASS_UNLOADING:
        case FLS_STATISTICS:
        case UNIFIED_HEAP:
        case UNIFIED_HEAP_DUMP_AFTER_FULL_GC:
        case UNIFIED_HEAP_DUMP_BEFORE_FULL_GC:
        case FOOTER_STATS:
        case GC_INFO:
        case GC_LOCKER_SCAVENGE_FAILED:
        case GC_OVERHEAD_LIMIT:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VM_INFO:
        case HEAP:
        case HEAP_ADDRESS:
        case HEAP_AT_GC:
        case HEAP_REGION_SIZE:
        case LOG_FILE:
        case OOME_METASPACE:
        case UNIFIED_SHENANDOAH_CANCELLING_GC:
        case SHENANDOAH_STATS:
        case SHENANDOAH_TRIGGER:
        case UNIFIED_BLANK_LINE:
        case UNIFIED_FOOTER_STATS:
        case UNIFIED_G1_INFO:
        case UNIFIED_HEADER:
        case UNIFIED_SAFEPOINT:
        case UNIFIED_SHENANDOAH_STATS:
        case UNIFIED_SHENANDOAH_TRIGGER:
        case UNKNOWN:
        case VM_WARNING:
        case Z_STATS:
            return false;
        default:
            return true;
        }

    }

    /**
     * @param logLine
     *            The log line.
     * @param priorLogEvent
     *            The prior log line <code>LogEvent</code>.
     * @param collectorFamily
     *            The <code>CollectorFamily</code>.
     * @return The <code>LogEvent</code> for the log line.
     */
    public static final LogEvent parseLogLine(String logLine, LogEvent priorLogEvent, CollectorFamily collectorFamily) {
        LogEventType eventType = identifyEventType(logLine, priorLogEvent, collectorFamily);
        switch (eventType) {
        // Unified (order of appearance)
        case UNIFIED_SAFEPOINT:
            return new UnifiedSafepointEvent(logLine);
        case UNIFIED_BLANK_LINE:
            return new UnifiedBlankLineEvent(logLine);
        case UNIFIED_CONCURRENT:
            return new UnifiedConcurrentEvent(logLine);
        case UNIFIED_CMS_INITIAL_MARK:
            return new UnifiedCmsInitialMarkEvent(logLine);
        case UNIFIED_FOOTER_STATS:
            return new UnifiedFooterStatsEvent(logLine);
        case UNIFIED_G1_CLEANUP:
            return new UnifiedG1CleanupEvent(logLine);
        case UNIFIED_G1_FULL_GC_PARALLEL:
            return new UnifiedG1FullGcEvent(logLine);
        case UNIFIED_G1_INFO:
            return new UnifiedG1InfoEvent(logLine);
        case UNIFIED_G1_MIXED_PAUSE:
            return new UnifiedG1MixedPauseEvent(logLine);
        case UNIFIED_G1_YOUNG_INITIAL_MARK:
            return new UnifiedG1YoungInitialMarkEvent(logLine);
        case UNIFIED_G1_YOUNG_PAUSE:
            return new UnifiedG1YoungPauseEvent(logLine);
        case UNIFIED_G1_YOUNG_PREPARE_MIXED:
            return new UnifiedG1YoungPrepareMixedEvent(logLine);
        case UNIFIED_HEADER:
            return new UnifiedHeaderEvent(logLine);
        case UNIFIED_OLD:
            return new UnifiedOldEvent(logLine);
        case UNIFIED_PARALLEL_COMPACTING_OLD:
            return new UnifiedParallelCompactingOldEvent(logLine);
        case UNIFIED_PARALLEL_SCAVENGE:
            return new UnifiedParallelScavengeEvent(logLine);
        case UNIFIED_PAR_NEW:
            return new UnifiedParNewEvent(logLine);
        case UNIFIED_REMARK:
            return new UnifiedRemarkEvent(logLine);
        case UNIFIED_SERIAL_NEW:
            return new UnifiedSerialNewEvent(logLine);
        case UNIFIED_SERIAL_OLD:
            return new UnifiedSerialOldEvent(logLine);
        case UNIFIED_SHENANDOAH_DEGENERATED_GC:
            return new UnifiedShenandoahDegeneratedGcEvent(logLine);
        case UNIFIED_SHENANDOAH_FINAL_EVAC:
            return new UnifiedShenandoahFinalEvacEvent(logLine);
        case UNIFIED_SHENANDOAH_FINAL_MARK:
            return new UnifiedShenandoahFinalMarkEvent(logLine);
        case UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS:
            return new UnifiedShenandoahFinalUpdateRefsEvent(logLine);
        case UNIFIED_SHENANDOAH_FULL_GC:
            return new UnifiedShenandoahFullGcEvent(logLine);
        case UNIFIED_SHENANDOAH_INIT_MARK:
            return new UnifiedShenandoahInitMarkEvent(logLine);
        case UNIFIED_SHENANDOAH_INIT_UPDATE_REFS:
            return new UnifiedShenandoahInitUpdateRefsEvent(logLine);
        case UNIFIED_SHENANDOAH_STATS:
            return new UnifiedShenandoahStatsEvent(logLine);
        case UNIFIED_SHENANDOAH_TRIGGER:
            return new UnifiedShenandoahTriggerEvent(logLine);
        case UNIFIED_YOUNG:
            return new UnifiedYoungEvent(logLine);
        case UNIFIED_GC_LOCKER_RETRY:
            return new UnifiedGcLockerRetryEvent(logLine);
        case UNIFIED_HEAP:
            return new UnifiedHeapEvent(logLine);
        case UNIFIED_HEAP_DUMP_AFTER_FULL_GC:
            return new UnifiedHeapDumpAfterFullGcEvent(logLine);
        case UNIFIED_HEAP_DUMP_BEFORE_FULL_GC:
            return new UnifiedHeapDumpBeforeFullGcEvent(logLine);
        case FOOTER_STATS:
            return new FooterStatsEvent(logLine);

        // G1
        case G1_CLEANUP:
            return new G1CleanupEvent(logLine);
        case G1_CONCURRENT:
            return new G1ConcurrentEvent(logLine);
        case G1_FULL_GC_SERIAL:
            return new G1FullGcEvent(logLine);
        case G1_MIXED_PAUSE:
            return new G1MixedPauseEvent(logLine);
        case G1_REMARK:
            return new G1RemarkEvent(logLine);
        case G1_YOUNG_INITIAL_MARK:
            return new G1YoungInitialMarkEvent(logLine);
        case G1_YOUNG_PAUSE:
            return new G1YoungPauseEvent(logLine);

        // Shenandoah
        case UNIFIED_SHENANDOAH_CANCELLING_GC:
            return new UnifiedShenandoahCancellingGcEvent(logLine);
        case SHENANDOAH_CONCURRENT:
            return new ShenandoahConcurrentEvent(logLine);
        case SHENANDOAH_DEGENERATED_GC:
            return new ShenandoahDegeneratedGcEvent(logLine);
        case SHENANDOAH_FINAL_EVAC:
            return new ShenandoahFinalEvacEvent(logLine);
        case SHENANDOAH_FINAL_MARK:
            return new ShenandoahFinalMarkEvent(logLine);
        case UNIFIED_SHENANDOAH_FINAL_ROOTS:
            return new UnifiedShenandoahFinalRootsEvent(logLine);
        case SHENANDOAH_FINAL_UPDATE:
            return new ShenandoahFinalUpdateEvent(logLine);
        case SHENANDOAH_FULL_GC:
            return new ShenandoahFullGcEvent(logLine);
        case SHENANDOAH_INIT_MARK:
            return new ShenandoahInitMarkEvent(logLine);
        case SHENANDOAH_INIT_UPDATE:
            return new ShenandoahInitUpdateEvent(logLine);
        case SHENANDOAH_STATS:
            return new ShenandoahStatsEvent(logLine);
        case SHENANDOAH_TRIGGER:
            return new ShenandoahTriggerEvent(logLine);

        // Z
        case Z_ALLOCATION_STALL:
            return new ZAllocationStallEvent(logLine);
        case Z_CONCURRENT:
            return new ZConcurrentEvent(logLine);
        case Z_MARK_END:
            return new ZMarkEndEvent(logLine);
        case Z_MARK_END_OLD:
            return new ZMarkEndOldEvent(logLine);
        case Z_MARK_END_YOUNG:
            return new ZMarkEndYoungEvent(logLine);
        case Z_MARK_START:
            return new ZMarkStartEvent(logLine);
        case Z_MARK_START_YOUNG:
            return new ZMarkStartYoungEvent(logLine);
        case Z_MARK_START_YOUNG_AND_OLD:
            return new ZMarkStartYoungAndOldEvent(logLine);
        case Z_RELOCATE_START:
            return new ZRelocateStartEvent(logLine);
        case Z_RELOCATE_START_OLD:
            return new ZRelocateStartOldEvent(logLine);
        case Z_RELOCATE_START_YOUNG:
            return new ZRelocateStartYoungEvent(logLine);
        case Z_RELOCATION_STALL:
            return new ZRelocationStallEvent(logLine);
        case Z_STATS:
            return new ZStatsEvent(logLine);

        // CMS
        case PAR_NEW:
            return new ParNewEvent(logLine);
        case CMS_CONCURRENT:
            return new CmsConcurrentEvent(logLine);
        case CMS_INITIAL_MARK:
            return new CmsInitialMarkEvent(logLine);
        case CMS_REMARK:
            return new CmsRemarkEvent(logLine);
        case CMS_SERIAL_OLD:
            return new CmsSerialOldEvent(logLine);

        // Parallel
        case PARALLEL_COMPACTING_OLD:
            return new ParallelCompactingOldEvent(logLine);
        case PARALLEL_SCAVENGE:
            return new ParallelScavengeEvent(logLine);
        case PARALLEL_SERIAL_OLD:
            return new ParallelSerialOldEvent(logLine);

        // Serial
        case SERIAL_NEW:
            return new SerialNewEvent(logLine);
        case SERIAL_OLD:
            return new SerialOldEvent(logLine);

        // Other
        case APPLICATION_CONCURRENT_TIME:
            return new ApplicationConcurrentTimeEvent(logLine);
        case APPLICATION_LOGGING:
            return new ApplicationLoggingEvent(logLine);
        case APPLICATION_STOPPED_TIME:
            return new ApplicationStoppedTimeEvent(logLine);
        case BLANK_LINE:
            return new BlankLineEvent(logLine);
        case CLASS_HISTOGRAM:
            return new ClassHistogramEvent(logLine);
        case CLASS_UNLOADING:
            return new ClassUnloadingEvent(logLine);
        case FLS_STATISTICS:
            return new FlsStatisticsEvent(logLine);
        case GC_INFO:
            return new GcInfoEvent(logLine);
        case GC_LOCKER_SCAVENGE_FAILED:
            return new GcLockerScavengeFailedEvent(logLine);
        case GC_OVERHEAD_LIMIT:
            return new GcOverheadLimitEvent(logLine);
        case HEADER_COMMAND_LINE_FLAGS:
            return new HeaderCommandLineFlagsEvent(logLine);
        case HEADER_MEMORY:
            return new HeaderMemoryEvent(logLine);
        case HEADER_VM_INFO:
            return new HeaderVmInfoEvent(logLine);
        case HEAP:
            return new HeapEvent(logLine);
        case HEAP_AT_GC:
            return new HeapAtGcEvent(logLine);
        case LOG_FILE:
            return new LogFileEvent(logLine);
        case OOME_METASPACE:
            return new OomeMetaspaceEvent(logLine);
        case TENURING_DISTRIBUTION:
            return new TenuringDistributionEvent(logLine);
        case THREAD_DUMP:
            return new ThreadDumpEvent(logLine);
        case UNKNOWN:
            return new UnknownEvent(logLine);
        case VERBOSE_GC_OLD:
            return new VerboseGcOldEvent(logLine);
        case VERBOSE_GC_YOUNG:
            return new VerboseGcYoungEvent(logLine);
        case VM_WARNING:
            return new VmWarningEvent(logLine);
        default:
            throw new AssertionError("Unexpected event type value: " + eventType);
        }
    }

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkUtil() {
        super();
    }
}
