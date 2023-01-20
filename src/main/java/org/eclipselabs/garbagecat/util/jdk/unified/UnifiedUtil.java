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

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.GcUtil;
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
     * The number of regex patterns in <code>UnifiedRegEx.DECORATOR</code>. Convenience field to make the code resilient
     * to decorator pattern changes.
     */
    public static final int DECORATOR_SIZE = Pattern.compile(UnifiedRegEx.DECORATOR)
            .matcher("[2020-02-14T15:21:55.207-0500] GC(44) Pause Young (Normal) (G1 Evacuation Pause)").groupCount();

    /**
     * Arbitrary date for determining time intervals when gc logging includes only uptime.
     */
    public static final Date JVM_START_DATE = GcUtil.parseDateStamp("2000-01-01T00:00:00.000-0500");

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
            case USING_CMS:
            case USING_G1:
            case USING_PARALLEL:
            case USING_SERIAL:
            case USING_SHENANDOAH:
            case USING_Z:
            case Z_MARK_END:
            case Z_MARK_START:
            case Z_RELOCATE_START:
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
