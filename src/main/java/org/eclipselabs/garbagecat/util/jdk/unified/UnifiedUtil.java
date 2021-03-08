/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

/**
 * <p>
 * Utility methods and constants for for OpenJDK and derivatives unified logging.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public final class UnifiedUtil {

	private static final Date jvmStartDate = GcUtil.parseStartDateTime("2000-01-01 00:00:00.000");

	private UnifiedUtil() {
        super();
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
            case UNIFIED_APPLICATION_STOPPED_TIME:
            case UNIFIED_BLANK_LINE:
            case UNIFIED_CONCURRENT:
            case UNIFIED_CMS_INITIAL_MARK:
            case UNIFIED_G1_CLEANUP:
            case UNIFIED_G1_INFO:
            case UNIFIED_G1_MIXED_PAUSE:
            case UNIFIED_G1_YOUNG_INITIAL_MARK:
            case UNIFIED_G1_YOUNG_PAUSE:
            case UNIFIED_G1_YOUNG_PREPARE_MIXED:
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
            case USING_SHENANDOAH:
            case USING_PARALLEL:
            case USING_SERIAL:
                return true;
            default:
            }
        }
        return false;
    }

    /**
     * Convert datestamp to milliseconds. For example: Convert 2019-02-05T14:47:34.229-0200 to 23.
     * 
     * @param datestamp
     *            Absolute date/time.
     * @return Milliseconds from a point in time.
     */
    public static long convertDatestampToMillis(String datestamp) {
        // Calculate uptimemillis from random date/time
        Date eventDate = GcUtil.parseDateStamp(datestamp);
        return GcUtil.dateDiff(jvmStartDate, eventDate);
    }
}
