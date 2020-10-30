/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.domain.jdk.FooterHeapEvent;
import org.eclipselabs.garbagecat.domain.jdk.FooterStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1ConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGCEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1MixedPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1RemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcInfoEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcLockerEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeapAtGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.LogFileEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ReferenceGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCancellingGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConsiderClassUnloadingConcMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahDegeneratedGcMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalEvacEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFinalUpdateEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahInitUpdateEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahTriggerEvent;
import org.eclipselabs.garbagecat.domain.jdk.TenuringDistributionEvent;
import org.eclipselabs.garbagecat.domain.jdk.ThreadDumpEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.VerboseGcYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.HeapAddressEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.HeapRegionSizeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedBlankLineEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedCmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1InfoEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1MixedPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPrepareMixedEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UsingCmsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UsingG1Event;
import org.eclipselabs.garbagecat.domain.jdk.unified.UsingParallelEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UsingSerialEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UsingShenandoahEvent;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;

/**
 * <p>
 * Utility methods and constants for OpenJDK and Oracle JDK.
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
        // unified
        FOOTER_STATS, GC_INFO, HEAP_REGION_SIZE, HEAP_ADDRESS, UNIFIED_APPLICATION_STOPPED_TIME, UNIFIED_BLANK_LINE,
        //
        UNIFIED_CONCURRENT, UNIFIED_CMS_INITIAL_MARK, UNIFIED_G1_CLEANUP, UNIFIED_G1_INFO, UNIFIED_G1_MIXED_PAUSE,
        //
        UNIFIED_G1_YOUNG_INITIAL_MARK, UNIFIED_G1_YOUNG_PAUSE, UNIFIED_G1_YOUNG_PREPARE_MIXED, UNIFIED_OLD,
        //
        UNIFIED_PAR_NEW, UNIFIED_PARALLEL_COMPACTING_OLD, UNIFIED_PARALLEL_SCAVENGE, UNIFIED_REMARK,
        //
        UNIFIED_SERIAL_NEW, UNIFIED_SERIAL_OLD, UNIFIED_YOUNG, USING_CMS, USING_G1, USING_PARALLEL, USING_SERIAL,
        //
        USING_SHENANDOAH,
        // serial
        SERIAL_NEW, SERIAL_OLD,
        // parallel
        PAR_NEW, PARALLEL_SCAVENGE, PARALLEL_SERIAL_OLD, PARALLEL_COMPACTING_OLD,
        // cms
        CMS_SERIAL_OLD, CMS_REMARK, CMS_INITIAL_MARK, CMS_CONCURRENT,
        // g1
        G1_YOUNG_PAUSE, G1_MIXED_PAUSE, G1_CONCURRENT, G1_YOUNG_INITIAL_MARK, G1_REMARK, G1_CLEANUP, G1_FULL_GC,
        // shenandoah
        SHENANDOAH_CANCELLING_GC, SHENANDOAH_CONCURRENT, SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK,
        //
        SHENANDOAH_DEGENERATED_GC_MARK, SHENANDOAH_FINAL_EVAC, SHENANDOAH_FINAL_MARK, SHENANDOAH_FINAL_UPDATE,
        //
        SHENANDOAH_INIT_MARK, SHENANDOAH_INIT_UPDATE, SHENANDOAH_STATS, SHENANDOAH_TRIGGER,
        // other
        APPLICATION_CONCURRENT_TIME, APPLICATION_LOGGING, APPLICATION_STOPPED_TIME, BLANK_LINE, CLASS_HISTOGRAM,
        //
        CLASS_UNLOADING, FLS_STATISTICS, FOOTER_HEAP, GC_LOCKER, GC_OVERHEAD_LIMIT, HEADER_COMMAND_LINE_FLAGS,
        //
        HEADER_MEMORY, HEADER_VERSION, HEAP_AT_GC, LOG_FILE, REFERENCE_GC, TENURING_DISTRIBUTION, THREAD_DUMP,
        //
        UNKNOWN, VERBOSE_GC_YOUNG, VERBOSE_GC_OLD
    };

    /**
     * Defined preprocessing actions.
     */
    public enum PreprocessActionType {
        APPLICATION_CONCURRENT_TIME, APPLICATION_STOPPED_TIME, DATE_STAMP, G1, CMS, PARALLEL, SERIAL, SHENANDOAH,
        //
        UNIFIED, UNIFIED_G1
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
        ERGONOMICS, HEAP_INSPECTION_INITIATED_GC, HEAP_DUMP_INITIATED_GC;
    };

    /**
     * Defined collector families.
     */
    public enum CollectorFamily {
        SERIAL, PARALLEL, CMS, G1, SHENANDOAH, UNKNOWN
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

        // Unified (alphabetical)
        if (FooterHeapEvent.match(logLine))
            return LogEventType.FOOTER_HEAP;
        if (HeapAddressEvent.match(logLine))
            return LogEventType.HEAP_ADDRESS;
        if (HeapRegionSizeEvent.match(logLine))
            return LogEventType.HEAP_REGION_SIZE;
        if (UnifiedApplicationStoppedTimeEvent.match(logLine))
            return LogEventType.UNIFIED_APPLICATION_STOPPED_TIME;
        if (UnifiedBlankLineEvent.match(logLine) && !BlankLineEvent.match(logLine))
            return LogEventType.UNIFIED_BLANK_LINE;
        if (UnifiedCmsInitialMarkEvent.match(logLine))
            return LogEventType.UNIFIED_CMS_INITIAL_MARK;
        if (UnifiedConcurrentEvent.match(logLine))
            return LogEventType.UNIFIED_CONCURRENT;
        if (UnifiedG1CleanupEvent.match(logLine))
            return LogEventType.UNIFIED_G1_CLEANUP;
        if (UnifiedG1InfoEvent.match(logLine))
            return LogEventType.UNIFIED_G1_INFO;
        if (UnifiedG1MixedPauseEvent.match(logLine))
            return LogEventType.UNIFIED_G1_MIXED_PAUSE;
        if (UnifiedG1YoungInitialMarkEvent.match(logLine))
            return LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK;
        if (UnifiedG1YoungPauseEvent.match(logLine))
            return LogEventType.UNIFIED_G1_YOUNG_PAUSE;
        if (UnifiedG1YoungPrepareMixedEvent.match(logLine))
            return LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED;
        if (UnifiedOldEvent.match(logLine))
            return LogEventType.UNIFIED_OLD;
        if (UnifiedParallelCompactingOldEvent.match(logLine))
            return LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
        if (UnifiedParallelScavengeEvent.match(logLine))
            return LogEventType.UNIFIED_PARALLEL_SCAVENGE;
        if (UnifiedParNewEvent.match(logLine))
            return LogEventType.UNIFIED_PAR_NEW;
        if (UnifiedRemarkEvent.match(logLine))
            return LogEventType.UNIFIED_REMARK;
        if (UnifiedSerialNewEvent.match(logLine))
            return LogEventType.UNIFIED_SERIAL_NEW;
        if (UnifiedSerialOldEvent.match(logLine))
            return LogEventType.UNIFIED_SERIAL_OLD;
        if (UnifiedYoungEvent.match(logLine))
            return LogEventType.UNIFIED_YOUNG;
        if (UsingCmsEvent.match(logLine))
            return LogEventType.USING_CMS;
        if (UsingG1Event.match(logLine))
            return LogEventType.USING_G1;
        if (UsingParallelEvent.match(logLine))
            return LogEventType.USING_PARALLEL;
        if (UsingSerialEvent.match(logLine))
            return LogEventType.USING_SERIAL;
        if (UsingShenandoahEvent.match(logLine))
            return LogEventType.USING_SHENANDOAH;

        // Unknown
        if (VerboseGcYoungEvent.match(logLine))
            return LogEventType.VERBOSE_GC_YOUNG;
        if (VerboseGcOldEvent.match(logLine))
            return LogEventType.VERBOSE_GC_OLD;

        // In order of most common events to limit checking

        // G1
        if (UsingG1Event.match(logLine))
            return LogEventType.USING_G1;
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
        if (UsingCmsEvent.match(logLine))
            return LogEventType.USING_CMS;
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
        if (UsingParallelEvent.match(logLine))
            return LogEventType.USING_PARALLEL;
        if (ParallelScavengeEvent.match(logLine))
            return LogEventType.PARALLEL_SCAVENGE;
        if (ParallelSerialOldEvent.match(logLine))
            return LogEventType.PARALLEL_SERIAL_OLD;
        if (ParallelCompactingOldEvent.match(logLine))
            return LogEventType.PARALLEL_COMPACTING_OLD;

        // Serial
        if (UsingSerialEvent.match(logLine))
            return LogEventType.USING_SERIAL;
        if (SerialOldEvent.match(logLine))
            return LogEventType.SERIAL_OLD;
        if (SerialNewEvent.match(logLine))
            return LogEventType.SERIAL_NEW;

        // Shenandoah
        if (ShenandoahCancellingGcEvent.match(logLine))
            return LogEventType.SHENANDOAH_CANCELLING_GC;
        if (ShenandoahConcurrentEvent.match(logLine))
            return LogEventType.SHENANDOAH_CONCURRENT;
        if (ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK;
        if (ShenandoahDegeneratedGcMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_DEGENERATED_GC_MARK;
        if (ShenandoahFinalEvacEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_EVAC;
        if (ShenandoahFinalMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_MARK;
        if (ShenandoahFinalUpdateEvent.match(logLine))
            return LogEventType.SHENANDOAH_FINAL_UPDATE;
        if (ShenandoahInitMarkEvent.match(logLine))
            return LogEventType.SHENANDOAH_INIT_MARK;
        if (ShenandoahInitUpdateEvent.match(logLine))
            return LogEventType.SHENANDOAH_INIT_UPDATE;
        if (ShenandoahStatsEvent.match(logLine))
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
        if (FooterStatsEvent.match(logLine))
            return LogEventType.FOOTER_STATS;
        if (GcInfoEvent.match(logLine))
            return LogEventType.GC_INFO;
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
        if (ReferenceGcEvent.match(logLine))
            return LogEventType.REFERENCE_GC;

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
        // Unified (order of appearance)
        case HEAP_ADDRESS:
            event = new HeapAddressEvent(logLine);
            break;
        case HEAP_REGION_SIZE:
            event = new HeapRegionSizeEvent(logLine);
            break;
        case UNIFIED_APPLICATION_STOPPED_TIME:
            event = new UnifiedApplicationStoppedTimeEvent(logLine);
            break;
        case UNIFIED_BLANK_LINE:
            event = new UnifiedBlankLineEvent(logLine);
            break;
        case UNIFIED_CONCURRENT:
            event = new UnifiedConcurrentEvent();
            break;
        case UNIFIED_CMS_INITIAL_MARK:
            event = new UnifiedCmsInitialMarkEvent(logLine);
            break;
        case UNIFIED_G1_CLEANUP:
            event = new UnifiedG1CleanupEvent(logLine);
            break;
        case UNIFIED_G1_INFO:
            event = new UnifiedG1InfoEvent(logLine);
            break;
        case UNIFIED_G1_MIXED_PAUSE:
            event = new UnifiedG1MixedPauseEvent(logLine);
            break;
        case UNIFIED_G1_YOUNG_INITIAL_MARK:
            event = new UnifiedG1YoungInitialMarkEvent(logLine);
            break;
        case UNIFIED_G1_YOUNG_PAUSE:
            event = new UnifiedG1YoungPauseEvent(logLine);
            break;
        case UNIFIED_G1_YOUNG_PREPARE_MIXED:
            event = new UnifiedG1YoungPrepareMixedEvent(logLine);
            break;
        case UNIFIED_OLD:
            event = new UnifiedOldEvent(logLine);
            break;
        case UNIFIED_PARALLEL_COMPACTING_OLD:
            event = new UnifiedParallelCompactingOldEvent(logLine);
            break;
        case UNIFIED_PARALLEL_SCAVENGE:
            event = new UnifiedParallelScavengeEvent(logLine);
            break;
        case UNIFIED_PAR_NEW:
            event = new UnifiedParNewEvent(logLine);
            break;
        case UNIFIED_REMARK:
            event = new UnifiedRemarkEvent(logLine);
            break;
        case UNIFIED_SERIAL_NEW:
            event = new UnifiedSerialNewEvent(logLine);
            break;
        case UNIFIED_SERIAL_OLD:
            event = new UnifiedSerialOldEvent(logLine);
            break;
        case UNIFIED_YOUNG:
            event = new UnifiedYoungEvent(logLine);
            break;
        case USING_CMS:
            event = new UsingCmsEvent(logLine);
            break;
        case USING_G1:
            event = new UsingG1Event(logLine);
            break;
        case USING_SHENANDOAH:
            event = new UsingShenandoahEvent(logLine);
            break;
        case FOOTER_HEAP:
            event = new FooterHeapEvent(logLine);
            break;
        case FOOTER_STATS:
            event = new FooterStatsEvent(logLine);
            break;
        case USING_PARALLEL:
            event = new UsingParallelEvent(logLine);
            break;
        case USING_SERIAL:
            event = new UsingSerialEvent(logLine);
            break;

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

        // Shenandoah
        case SHENANDOAH_CANCELLING_GC:
            event = new ShenandoahCancellingGcEvent();
            break;
        case SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK:
            event = new ShenandoahConsiderClassUnloadingConcMarkEvent(logLine);
            break;
        case SHENANDOAH_CONCURRENT:
            event = new ShenandoahConcurrentEvent(logLine);
            break;
        case SHENANDOAH_DEGENERATED_GC_MARK:
            event = new ShenandoahDegeneratedGcMarkEvent(logLine);
            break;
        case SHENANDOAH_FINAL_EVAC:
            event = new ShenandoahFinalEvacEvent(logLine);
            break;
        case SHENANDOAH_FINAL_MARK:
            event = new ShenandoahFinalMarkEvent(logLine);
            break;
        case SHENANDOAH_FINAL_UPDATE:
            event = new ShenandoahFinalUpdateEvent(logLine);
            break;
        case SHENANDOAH_INIT_MARK:
            event = new ShenandoahInitMarkEvent(logLine);
            break;
        case SHENANDOAH_INIT_UPDATE:
            event = new ShenandoahInitUpdateEvent(logLine);
            break;
        case SHENANDOAH_STATS:
            event = new ShenandoahStatsEvent();
            break;
        case SHENANDOAH_TRIGGER:
            event = new ShenandoahTriggerEvent();
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
        case CMS_SERIAL_OLD:
            event = new CmsSerialOldEvent(logLine);
            break;

        // Parallel
        case PARALLEL_COMPACTING_OLD:
            event = new ParallelCompactingOldEvent(logLine);
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
        case GC_INFO:
            event = new GcInfoEvent(logLine);
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

        // Unified (alphabetical)
        case UNIFIED_CMS_INITIAL_MARK:
            event = new UnifiedCmsInitialMarkEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_G1_CLEANUP:
            event = new UnifiedG1CleanupEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_G1_YOUNG_INITIAL_MARK:
            event = new UnifiedG1YoungInitialMarkEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_G1_MIXED_PAUSE:
            event = new UnifiedG1MixedPauseEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_G1_YOUNG_PAUSE:
            event = new UnifiedG1YoungPauseEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_G1_YOUNG_PREPARE_MIXED:
            event = new UnifiedG1YoungPrepareMixedEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_OLD:
            event = new UnifiedOldEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_PARALLEL_COMPACTING_OLD:
            event = new UnifiedParallelCompactingOldEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_PARALLEL_SCAVENGE:
            event = new UnifiedParallelScavengeEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_PAR_NEW:
            event = new UnifiedParNewEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_REMARK:
            event = new UnifiedRemarkEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_SERIAL_NEW:
            event = new UnifiedSerialNewEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_SERIAL_OLD:
            event = new UnifiedSerialOldEvent(logEntry, timestamp, duration);
            break;
        case UNIFIED_YOUNG:
            event = new UnifiedYoungEvent(logEntry, timestamp, duration);
            break;

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
        // Shenandoah
        case SHENANDOAH_DEGENERATED_GC_MARK:
            event = new ShenandoahDegeneratedGcMarkEvent(logEntry, timestamp, duration);
            break;
        case SHENANDOAH_FINAL_EVAC:
            event = new ShenandoahFinalEvacEvent(logEntry, timestamp, duration);
            break;
        case SHENANDOAH_FINAL_MARK:
            event = new ShenandoahFinalMarkEvent(logEntry, timestamp, duration);
            break;
        case SHENANDOAH_FINAL_UPDATE:
            event = new ShenandoahFinalUpdateEvent(logEntry, timestamp, duration);
            break;
        case SHENANDOAH_INIT_MARK:
            event = new ShenandoahInitMarkEvent(logEntry, timestamp, duration);
            break;
        case SHENANDOAH_INIT_UPDATE:
            event = new ShenandoahInitUpdateEvent(logEntry, timestamp, duration);
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
        // Parallel
        case PARALLEL_SCAVENGE:
            event = new ParallelScavengeEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_SERIAL_OLD:
            event = new ParallelSerialOldEvent(logEntry, timestamp, duration);
            break;
        case PARALLEL_COMPACTING_OLD:
            event = new ParallelCompactingOldEvent(logEntry, timestamp, duration);
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
        case FOOTER_HEAP:
        case FOOTER_STATS:
        case GC_INFO:
        case GC_LOCKER:
        case GC_OVERHEAD_LIMIT:
        case G1_CONCURRENT:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VERSION:
        case HEAP_ADDRESS:
        case HEAP_AT_GC:
        case HEAP_REGION_SIZE:
        case LOG_FILE:
        case REFERENCE_GC:
        case SHENANDOAH_CANCELLING_GC:
        case SHENANDOAH_CONCURRENT:
        case SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK:
        case SHENANDOAH_STATS:
        case SHENANDOAH_TRIGGER:
        case THREAD_DUMP:
        case TENURING_DISTRIBUTION:
        case UNIFIED_APPLICATION_STOPPED_TIME:
        case UNIFIED_CONCURRENT:
        case UNIFIED_G1_INFO:
        case UNKNOWN:
        case USING_SERIAL:
        case USING_PARALLEL:
        case USING_CMS:
        case USING_G1:
        case USING_SHENANDOAH:
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
    public static final String getDateStamp(String logLine) {
        String datestamp = null;
        String regex = "^(.*)" + JdkRegEx.DATESTAMP + "(.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find()) {
            datestamp = matcher.group(2);
        }
        return datestamp;
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
     * Determine if the <code>BlockingEvent</code> should be classified as a bottleneck.
     * 
     * @param gcEvent
     *            Current <code>BlockingEvent</code>.
     * @param priorEvent
     *            Previous <code>BlockingEvent</code>.
     * @param throughputThreshold
     *            Throughput threshold (percent of time spent not doing garbage collection for a given time interval) to
     *            be considered a bottleneck. Whole number 0-100.
     * @return True if the <code>BlockingEvent</code> pause time meets the bottleneck definition.
     */
    public static final boolean isBottleneck(BlockingEvent gcEvent, BlockingEvent priorEvent, int throughputThreshold)
            throws TimeWarpException {
        /*
         * Check for logging time warps, which could be an indication of mixed logging from multiple JVM runs. JDK8
         * seems to have threading issues where sometimes logging gets mixed up under heavy load, and an event appears
         * to start before the previous event finished. They are mainly very small overlaps or a few milliseconds.
         */
        if (gcEvent.getTimestamp() < priorEvent.getTimestamp()) {
            throw new TimeWarpException("Bad order: " + Constants.LINE_SEPARATOR + priorEvent.getLogEntry()
                    + Constants.LINE_SEPARATOR + gcEvent.getLogEntry());
        } else if (gcEvent.getTimestamp() < (priorEvent.getTimestamp()
                + JdkMath.convertMicrosToMillis(priorEvent.getDuration()).longValue() - 1000)) {
            // Only report if overlap > 1 sec to account for small overlaps due to JDK threading issues
            throw new TimeWarpException("Event overlap: " + Constants.LINE_SEPARATOR + priorEvent.getLogEntry()
                    + Constants.LINE_SEPARATOR + gcEvent.getLogEntry());
        } else if (gcEvent.getTimestamp() <= (priorEvent.getTimestamp()
                + JdkMath.convertMicrosToMillis(priorEvent.getDuration()).longValue())) {
            // Small (<1 sec) event overlap
            return true;
        } else {
            /*
             * Timestamp is the start of a vm event; therefore, the interval is from the end of the prior event to the
             * end of the current event.
             */
            long interval = gcEvent.getTimestamp() + JdkMath.convertMicrosToMillis(gcEvent.getDuration()).longValue()
                    - priorEvent.getTimestamp() - JdkMath.convertMicrosToMillis(priorEvent.getDuration()).longValue();
            // Determine the maximum duration for the given interval that meets the throughput goal.
            BigDecimal durationThreshold = new BigDecimal(100 - throughputThreshold);
            durationThreshold = durationThreshold.movePointLeft(2);
            durationThreshold = durationThreshold.multiply(new BigDecimal(interval));
            durationThreshold.setScale(0, RoundingMode.DOWN);
            return (JdkMath.convertMicrosToMillis(gcEvent.getDuration()).longValue() > durationThreshold.intValue());
        }
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
     * Convert SIZE_G1_DETAILS to SIZE.
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
        case APPLICATION_CONCURRENT_TIME:
        case APPLICATION_LOGGING:
        case APPLICATION_STOPPED_TIME:
        case BLANK_LINE:
        case CLASS_HISTOGRAM:
        case CLASS_UNLOADING:
        case FLS_STATISTICS:
        case FOOTER_HEAP:
        case FOOTER_STATS:
        case GC_INFO:
        case GC_LOCKER:
        case GC_OVERHEAD_LIMIT:
        case HEADER_COMMAND_LINE_FLAGS:
        case HEADER_MEMORY:
        case HEADER_VERSION:
        case HEAP_ADDRESS:
        case HEAP_AT_GC:
        case HEAP_REGION_SIZE:
        case LOG_FILE:
        case REFERENCE_GC:
        case SHENANDOAH_CANCELLING_GC:
        case SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK:
        case SHENANDOAH_STATS:
        case SHENANDOAH_TRIGGER:
        case UNIFIED_BLANK_LINE:
        case UNIFIED_G1_INFO:
        case UNKNOWN:
            reportable = false;
            break;
        default:
            break;
        }

        return reportable;
    }
}
