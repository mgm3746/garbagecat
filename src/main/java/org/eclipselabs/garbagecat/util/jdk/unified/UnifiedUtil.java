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
package org.eclipselabs.garbagecat.util.jdk.unified;

import java.util.List;
import java.util.regex.Matcher;

import org.eclipselabs.garbagecat.domain.BlankLineEvent;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.NullEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
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
     * Identify the unified log line.
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
        LogEventType eventType = LogEventType.UNKNOWN;
        switch (collectorFamily) {
        case CMS:
            if (UnifiedCmsInitialMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_CMS_INITIAL_MARK;
            } else if (UnifiedParNewEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PAR_NEW;
            }
            break;
        case G1:
            if (UnifiedG1FullGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_FULL_GC_PARALLEL;
            } else if (UnifiedG1CleanupEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_CLEANUP;
            } else if (UnifiedG1InfoEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_INFO;
            } else if (UnifiedG1MixedPauseEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_MIXED_PAUSE;
            } else if (UnifiedG1YoungInitialMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK;
            } else if (UnifiedG1YoungPauseEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_PAUSE;
            } else if (UnifiedG1YoungPrepareMixedEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED;
            }
            break;
        case PARALLEL:
            if (UnifiedParallelCompactingOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
            } else if (UnifiedParallelScavengeEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PARALLEL_SCAVENGE;
            } else if (UnifiedSerialOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SERIAL_OLD;
            }
            break;
        case SERIAL:
            if (UnifiedSerialNewEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SERIAL_NEW;
            } else if (UnifiedSerialOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SERIAL_OLD;
            }
            break;
        case SHENANDOAH:
            if (UnifiedShenandoahDegeneratedGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC;
            } else if (UnifiedShenandoahFinalEvacEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC;
            } else if (UnifiedShenandoahFinalMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK;
            } else if (UnifiedShenandoahFinalUpdateRefsEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS;
            } else if (UnifiedShenandoahFullGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FULL_GC;
            } else if (UnifiedShenandoahInitMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_INIT_MARK;
            } else if (UnifiedShenandoahInitUpdateRefsEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS;
            } else if (logLine.matches(UnifiedShenandoahStatsEvent._REGEX_HEADER)
                    || (UnifiedShenandoahStatsEvent.match(logLine)
                            && priorLogEvent instanceof UnifiedShenandoahStatsEvent)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_STATS;
            } else if (UnifiedShenandoahTriggerEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_TRIGGER;
            }
            break;
        case UNKNOWN:
            if (UnifiedG1FullGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_FULL_GC_PARALLEL;
            } else if (UnifiedCmsInitialMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_CMS_INITIAL_MARK;
            } else if (UnifiedG1CleanupEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_CLEANUP;
            } else if (UnifiedG1InfoEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_INFO;
            } else if (UnifiedG1MixedPauseEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_MIXED_PAUSE;
            } else if (UnifiedG1YoungInitialMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK;
            } else if (UnifiedG1YoungPauseEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_PAUSE;
            } else if (UnifiedG1YoungPrepareMixedEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED;
            } else if (UnifiedParallelCompactingOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD;
            } else if (UnifiedParallelScavengeEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PARALLEL_SCAVENGE;
            } else if (UnifiedParNewEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_PAR_NEW;
            } else if (UnifiedSerialNewEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SERIAL_NEW;
            } else if (UnifiedSerialOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SERIAL_OLD;
            } else if (UnifiedShenandoahDegeneratedGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC;
            } else if (UnifiedShenandoahFinalEvacEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC;
            } else if (UnifiedShenandoahFinalMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK;
            } else if (UnifiedShenandoahFinalUpdateRefsEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS;
            } else if (UnifiedShenandoahFullGcEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_FULL_GC;
            } else if (UnifiedShenandoahInitMarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_INIT_MARK;
            } else if (UnifiedShenandoahInitUpdateRefsEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS;
            } else if (logLine.matches(UnifiedShenandoahStatsEvent._REGEX_HEADER)
                    || (UnifiedShenandoahStatsEvent.match(logLine)
                            && priorLogEvent instanceof UnifiedShenandoahStatsEvent)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_STATS;
            } else if (UnifiedShenandoahTriggerEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SHENANDOAH_TRIGGER;
            } else if (ZAllocationStallEvent.match(logLine)) {
                eventType = LogEventType.Z_ALLOCATION_STALL;
            } else if (ZConcurrentEvent.match(logLine)) {
                eventType = LogEventType.Z_CONCURRENT;
            } else if (ZMarkEndEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END;
            } else if (ZMarkEndOldEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END_OLD;
            } else if (ZMarkEndYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END_YOUNG;
            } else if (ZMarkStartEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START;
            } else if (ZMarkStartYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START_YOUNG;
            } else if (ZMarkStartYoungAndOldEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START_YOUNG_AND_OLD;
            } else if (ZRelocateStartEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START;
            } else if (ZRelocateStartOldEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START_OLD;
            } else if (ZRelocateStartYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START_YOUNG;
            } else if (ZRelocationStallEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATION_STALL;
            } else if (logLine.matches(ZStatsEvent._REGEX_HEADER)
                    || (ZStatsEvent.match(logLine) && priorLogEvent instanceof ZStatsEvent)) {
                eventType = LogEventType.Z_STATS;
                break;
            }
        case Z:
            if (ZAllocationStallEvent.match(logLine)) {
                eventType = LogEventType.Z_ALLOCATION_STALL;
            } else if (ZConcurrentEvent.match(logLine)) {
                eventType = LogEventType.Z_CONCURRENT;
            } else if (ZMarkEndEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END;
            } else if (ZMarkEndOldEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END_OLD;
            } else if (ZMarkEndYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_END_YOUNG;
            } else if (ZMarkStartEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START;
            } else if (ZMarkStartYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START_YOUNG;
            } else if (ZMarkStartYoungAndOldEvent.match(logLine)) {
                eventType = LogEventType.Z_MARK_START_YOUNG_AND_OLD;
            } else if (ZRelocateStartEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START;
            } else if (ZRelocateStartOldEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START_OLD;
            } else if (ZRelocateStartYoungEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATE_START_YOUNG;
            } else if (ZRelocationStallEvent.match(logLine)) {
                eventType = LogEventType.Z_RELOCATION_STALL;
            } else if (logLine.matches(ZStatsEvent._REGEX_HEADER)
                    || (ZStatsEvent.match(logLine) && priorLogEvent instanceof ZStatsEvent)) {
                eventType = LogEventType.Z_STATS;
            }
            break;
        default:
            break;
        }
        if (eventType == LogEventType.UNKNOWN) {
            if (UnifiedHeapEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_HEAP;
            } else if (OomeMetaspaceEvent.match(logLine)) {
                eventType = LogEventType.OOME_METASPACE;
            } else if (UnifiedSafepointEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_SAFEPOINT;
            } else if (UnifiedConcurrentEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_CONCURRENT;
            } else if (logLine.matches(UnifiedFooterStatsEvent._REGEX_HEADER)
                    || (UnifiedFooterStatsEvent.match(logLine) && priorLogEvent instanceof UnifiedFooterStatsEvent)) {
                eventType = LogEventType.UNIFIED_FOOTER_STATS;
            } else if (UnifiedGcLockerRetryEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_GC_LOCKER_RETRY;
            } else if (UnifiedHeaderEvent.match(logLine)
                    && (priorLogEvent instanceof NullEvent || priorLogEvent instanceof UnifiedHeaderEvent)) {
                eventType = LogEventType.UNIFIED_HEADER;
            } else if (UnifiedOldEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_OLD;
            } else if (UnifiedRemarkEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_REMARK;
            } else if (UnifiedYoungEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_YOUNG;
            } else if (UnifiedBlankLineEvent.match(logLine) && !BlankLineEvent.match(logLine)) {
                eventType = LogEventType.UNIFIED_BLANK_LINE;
            }
        }
        return eventType;
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
            case OOME_METASPACE:
            case UNIFIED_G1_FULL_GC_PARALLEL:
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
