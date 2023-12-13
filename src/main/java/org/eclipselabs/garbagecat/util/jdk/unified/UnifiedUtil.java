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

import org.eclipselabs.garbagecat.domain.TimeWarpException;
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
            case UNIFIED_SAFEPOINT:
            case UNIFIED_BLANK_LINE:
            case UNIFIED_CONCURRENT:
            case UNIFIED_CMS_INITIAL_MARK:
            case UNIFIED_G1_CLEANUP:
            case G1_FULL_GC_PARALLEL:
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
            case UNIFIED_SERIAL_NEW:
            case UNIFIED_SERIAL_OLD:
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
