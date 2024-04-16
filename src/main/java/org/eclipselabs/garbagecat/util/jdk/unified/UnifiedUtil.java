/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk.unified;

import java.util.List;
import java.util.regex.Matcher;

import org.eclipselabs.garbagecat.domain.BlankLineEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.NullEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.jdk.unified.MetaspaceUtilsReportEvent;
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
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedHeapEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahDegeneratedGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalEvacEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFinalUpdateRefsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahInitMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahInitUpdateRefsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahStatsEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahTriggerEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.ZAllocationStallEvent;
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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

/**
 * <p>
 * Utility methods and constants for OpenJDK and derivatives unified logging.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public final class UnifiedUtil {

    /**
     * @param matcher
     *            The unified log line <code>Matcher</code>.
     * @return The time when the GC event either started or ended in milliseconds after: (1) JVM startup. (2)
     *         <code>JVM_START_DATE</code>, if startup time is unknown.
     */
    public static final long calculateTime(Matcher matcher) throws TimeWarpException {
        long time = 0L;
        if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
            time = Long.parseLong(matcher.group(13));
        } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
            time = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
        } else {
            if (matcher.group(15) != null) {
                if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    time = Long.parseLong(matcher.group(17));
                } else {
                    time = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                }
            } else {
                // Datestamp only.
                time = JdkUtil.convertDatestampToMillis(matcher.group(2));
            }
        }
        if (time < 0) {
            throw new TimeWarpException("Time < 0: " + matcher.group(0));
        } else {
            return time;
        }
    }

    /**
     * Identify the unified log line event.
     * 
     * @param logLine
     *            The log line.
     * @param priorLogEvent
     *            The prior log line <code>LogEvent</code>.
     * @return The <code>LogEventType</code> of the log entry.
     */
    public static final LogEventType identifyEventType(String logLine, LogEvent priorLogEvent) {

        // (mostly-alphabetical)
        if (UnifiedG1FullGcEvent.match(logLine))
            return LogEventType.G1_FULL_GC_PARALLEL;
        if (UnifiedHeapEvent.match(logLine))
            return LogEventType.UNIFIED_HEAP;
        if (MetaspaceUtilsReportEvent.match(logLine))
            return LogEventType.METASPACE_UTILS_REPORT;
        if (OomeMetaspaceEvent.match(logLine))
            return LogEventType.OOME_METASPACE;
        if (UnifiedSafepointEvent.match(logLine))
            return LogEventType.UNIFIED_SAFEPOINT;
        if (UnifiedCmsInitialMarkEvent.match(logLine))
            return LogEventType.UNIFIED_CMS_INITIAL_MARK;
        if (UnifiedConcurrentEvent.match(logLine))
            return LogEventType.UNIFIED_CONCURRENT;
        if (logLine.matches(UnifiedFooterStatsEvent._REGEX_HEADER)
                || (UnifiedFooterStatsEvent.match(logLine) && priorLogEvent instanceof UnifiedFooterStatsEvent))
            return LogEventType.UNIFIED_FOOTER_STATS;
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
        if (UnifiedGcLockerRetryEvent.match(logLine))
            return LogEventType.UNIFIED_GC_LOCKER_RETRY;
        if (UnifiedHeaderEvent.match(logLine)
                && (priorLogEvent instanceof NullEvent || priorLogEvent instanceof UnifiedHeaderEvent))
            return LogEventType.UNIFIED_HEADER;
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
        if (UnifiedShenandoahDegeneratedGcEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC;
        if (UnifiedShenandoahFinalEvacEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC;
        if (UnifiedShenandoahFinalMarkEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK;
        if (UnifiedShenandoahFinalUpdateRefsEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS;
        if (UnifiedShenandoahFullGcEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_FULL_GC;
        if (UnifiedShenandoahInitMarkEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_INIT_MARK;
        if (UnifiedShenandoahInitUpdateRefsEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS;
        if (logLine.matches(UnifiedShenandoahStatsEvent._REGEX_HEADER)
                || (UnifiedShenandoahStatsEvent.match(logLine) && priorLogEvent instanceof UnifiedShenandoahStatsEvent))
            return LogEventType.UNIFIED_SHENANDOAH_STATS;
        if (UnifiedShenandoahTriggerEvent.match(logLine))
            return LogEventType.UNIFIED_SHENANDOAH_TRIGGER;
        if (UnifiedYoungEvent.match(logLine))
            return LogEventType.UNIFIED_YOUNG;
        if (ZAllocationStallEvent.match(logLine))
            return LogEventType.Z_ALLOCATION_STALL;
        if (ZMarkEndEvent.match(logLine))
            return LogEventType.Z_MARK_END;
        if (ZMarkEndOldEvent.match(logLine))
            return LogEventType.Z_MARK_END_OLD;
        if (ZMarkEndYoungEvent.match(logLine))
            return LogEventType.Z_MARK_END_YOUNG;
        if (ZMarkStartEvent.match(logLine))
            return LogEventType.Z_MARK_START;
        if (ZMarkStartYoungEvent.match(logLine))
            return LogEventType.Z_MARK_START_YOUNG;
        if (ZMarkStartYoungAndOldEvent.match(logLine))
            return LogEventType.Z_MARK_START_YOUNG_AND_OLD;
        if (ZRelocateStartEvent.match(logLine))
            return LogEventType.Z_RELOCATE_START;
        if (ZRelocateStartOldEvent.match(logLine))
            return LogEventType.Z_RELOCATE_START_OLD;
        if (ZRelocateStartYoungEvent.match(logLine))
            return LogEventType.Z_RELOCATE_START_YOUNG;
        if (ZRelocationStallEvent.match(logLine))
            return LogEventType.Z_RELOCATION_STALL;
        if (ZStatsEvent.match(logLine))
            return LogEventType.Z_STATS;
        if (UnifiedBlankLineEvent.match(logLine) && !BlankLineEvent.match(logLine))
            return LogEventType.UNIFIED_BLANK_LINE;

        // no idea what event is
        return LogEventType.UNKNOWN;
    }

    /**
     * @param eventTypes
     *            The JVM event types.
     * @return <code>true</code> if the JVM events indicate unified logging (JDK9+), false otherwise.
     */
    public static final boolean isUnifiedLogging(List<LogEventType> eventTypes) {
        for (LogEventType eventType : eventTypes) {
            switch (eventType) {
            case HEAP_ADDRESS:
            case HEAP_REGION_SIZE:
            case METASPACE_UTILS_REPORT:
            case OOME_METASPACE:
            case G1_FULL_GC_PARALLEL:
            case UNIFIED_SHENANDOAH_FINAL_ROOTS:
            case UNIFIED_BLANK_LINE:
            case UNIFIED_CMS_INITIAL_MARK:
            case UNIFIED_CONCURRENT:
            case UNIFIED_FOOTER_STATS:
            case UNIFIED_G1_CLEANUP:
            case UNIFIED_G1_INFO:
            case UNIFIED_G1_MIXED_PAUSE:
            case UNIFIED_G1_YOUNG_INITIAL_MARK:
            case UNIFIED_G1_YOUNG_PAUSE:
            case UNIFIED_G1_YOUNG_PREPARE_MIXED:
            case UNIFIED_HEADER:
            case UNIFIED_OLD:
            case UNIFIED_REMARK:
            case UNIFIED_PARALLEL_COMPACTING_OLD:
            case UNIFIED_PARALLEL_SCAVENGE:
            case UNIFIED_PAR_NEW:
            case UNIFIED_SAFEPOINT:
            case UNIFIED_SERIAL_NEW:
            case UNIFIED_SERIAL_OLD:
            case UNIFIED_SHENANDOAH_DEGENERATED_GC:
            case UNIFIED_SHENANDOAH_FINAL_EVAC:
            case UNIFIED_SHENANDOAH_FINAL_MARK:
            case UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS:
            case UNIFIED_SHENANDOAH_FULL_GC:
            case UNIFIED_SHENANDOAH_INIT_MARK:
            case UNIFIED_SHENANDOAH_INIT_UPDATE_REFS:
            case UNIFIED_SHENANDOAH_STATS:
            case UNIFIED_SHENANDOAH_TRIGGER:
            case UNIFIED_YOUNG:
            case Z_ALLOCATION_STALL:
            case Z_MARK_END:
            case Z_MARK_END_OLD:
            case Z_MARK_END_YOUNG:
            case Z_MARK_START:
            case Z_MARK_START_YOUNG:
            case Z_MARK_START_YOUNG_AND_OLD:
            case Z_RELOCATE_START:
            case Z_RELOCATE_START_OLD:
            case Z_RELOCATE_START_YOUNG:
            case Z_RELOCATION_STALL:
            case Z_STATS:
                return true;
            default:
            }
        }
        return false;
    }

    private UnifiedUtil() {
        super();
    }
}
