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
package org.eclipselabs.garbagecat.util.jdk;

import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;

/**
 * Analysis constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public enum Analysis {

    /**
     * Property key for concurrent mode failure.
     */
    ERROR_CMS_CONCURRENT_MODE_FAILURE("error.cms.concurrent.mode.failure"),

    /**
     * Property key for concurrent mode interrupted.
     */
    ERROR_CMS_CONCURRENT_MODE_INTERRUPTED("error.cms.concurrent.mode.interrupted"),

    /**
     * Property key for the CMS_SERIAL_OLD collector being invoked due to a GCLocker initiated PAR_NEW collection
     * failing due to the full promotion guarantee.
     */
    ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED("error.cms.par.new.gc.locker.failed"),

    /**
     * Property key for promotion failed.
     */
    ERROR_CMS_PROMOTION_FAILED("error.cms.promotion.failed"),

    /**
     * Property key for explicit garbage collection invoking the CMS_SERIAL_OLD collector.
     */
    ERROR_EXPLICIT_GC_SERIAL_CMS("error.explicit.gc.serial.cms"),

    /**
     * Property key for explicit garbage collection invoking the G1_FULL_GC collector.
     */
    ERROR_EXPLICIT_GC_SERIAL_G1("error.explicit.gc.serial.g1"),

    /**
     * Property key for G1 evacuation failure.
     */
    ERROR_G1_EVACUATION_FAILURE("error.g1.evacuation.failure"),

    /**
     * Property key for humongous allocations on an old JDK not able to fully reclaim humongous objects during young
     * collections (&lt; JDK8 u60).
     */
    ERROR_G1_HUMONGOUS_JDK_OLD("error.g1.humongous.jdk.old"),

    /**
     * Property key for the garbage collection overhead limit being reached.
     */
    ERROR_GC_TIME_LIMIT_EXCEEEDED("error.gc.time.limit.exceeded"),

    /**
     * Property key for when the Metaspace is not able to be resized, and the JVM is doing full, serial collections
     * attempting to free Metaspace before throwing OutOfMemoryError. The Metaspace is undersized, or there is a
     * Metaspace leak.
     */
    ERROR_METASPACE_ALLOCATION_FAILURE("error.metaspace.allocation.failure"),

    /**
     * Property key for Metaspace::report_metadata_oome at the end of gc.log.
     */
    ERROR_OOME_METASPACE("error.oome.metaspace"),

    /**
     * Property key for insufficient physical memory.
     */
    ERROR_PHYSICAL_MEMORY("error.physical.memory"),

    /**
     * Property key for the SERIAL_OLD collector being invoked for reasons other than explicit gc.
     */
    ERROR_SERIAL_GC("error.serial.gc"),

    /**
     * Property key for the CMS collector invoking a serial collection for reasons other than explicit gc.
     */
    ERROR_SERIAL_GC_CMS("error.serial.gc.cms"),

    /**
     * Property key for the G1 collector invoking a serial collection for reasons other than explicit gc.
     */
    ERROR_SERIAL_GC_G1("error.serial.gc.g1"),

    /**
     * Property key for the Parallel (Throughput) PARALLEL_SERIAL_OLD (single-threaded) collector.
     */
    ERROR_SERIAL_GC_PARALLEL("error.serial.gc.parallel"),

    /**
     * Property key for the following VM warning:
     * 
     * <pre>
     * Failed to reserve shared memory. (error = 12)
     * </pre>
     */
    ERROR_SHARED_MEMORY_12("error.shared.memory.12"),

    /**
     * Property key for the SHENANDOAH_FULL_GC collector being invoked.
     */
    ERROR_SHENANDOAH_FULL_GC("error.shenandoah.full.gc"),

    /**
     * Property key for unidentified log lines w/o preparsing.
     */
    ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE("error.unidentified.log.lines.preparse"),

    /**
     * Property key for partial log file.
     */
    INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED("info.first.timestamp.threshold.exceeded"),

    /**
     * Property key for humongous allocations.
     */
    INFO_G1_HUMONGOUS_ALLOCATION("info.g1.humongous.allocation"),

    /**
     * Property key for young space &gt;= old space.
     */
    INFO_NEW_RATIO_INVERTED("info.new.ratio.inverted"),

    /**
     * Property key for a very old JDK with a permanent generation.
     */
    INFO_PERM_GEN("info.perm.gen"),

    /**
     * Property key for swapping disabled.
     */
    INFO_SWAP_DISABLED("info.swap.disabled"),

    /**
     * Property key for swapping.
     */
    INFO_SWAPPING("info.swapping"),

    /**
     * Property key for one or more thread dumps in gc logging.
     */
    INFO_THREAD_DUMP("info.thread.dump"),

    /**
     * Property key for unidentified last log line w/ preparsing.
     */
    INFO_UNIDENTIFIED_LOG_LINE_LAST("info.unidentified.log.line.last"),

    /**
     * Property key for application logging mixed with gc logging.
     */
    WARN_APPLICATION_LOGGING("warn.application.logging"),

    /**
     * Property key for application stopped time missing.
     */
    WARN_APPLICATION_STOPPED_TIME_MISSING("warn.application.stopped.time.missing"),

    /**
     * Property key for class histogram output due to one of the following options:
     * 
     * <pre>
     * -XX:+PrintClassHistogram
     * -XX:+PrintClassHistogramBeforeFullGC
     * -XX:+PrintClassHistogramAfterFullGC
     * </pre>
     */
    WARN_CLASS_HISTOGRAM("warn.class.histogram"),

    /**
     * Property key for CMS collector class unloading not enabled.
     */
    WARN_CMS_CLASS_UNLOADING_NOT_ENABLED("warn.cms.class.unloading.not.enabled"),

    /**
     * Property key for specifying both the CMS collector running in incremental mode and an initiating occupancy
     * fraction.
     */
    WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT("warn.cms.inc.mode.with.init.occup.fract"),

    /**
     * Property key for CMS collector running in incremental mode.
     */
    WARN_CMS_INCREMENTAL_MODE("warn.cms.incremental.mode"),

    /**
     * Property key for CMS initial mark low parallelism.
     */
    WARN_CMS_INITIAL_MARK_LOW_PARALLELISM("warn.cms.initial.mark.low.parallelism"),

    /**
     * Property key for CMS remark low parallelism.
     */
    WARN_CMS_REMARK_LOW_PARALLELISM("warn.cms.remark.low.parallelism"),

    /**
     * Property key for diagnostic initiated gc.
     */
    WARN_EXPLICIT_GC_DIAGNOSTIC("warn.explicit.gc.diagnostic"),

    /**
     * Property key for explicit garbage collection invoking the G1_YOUNG_INITIAL_MARK collector.
     */
    WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK("warn.explicit.gc.g1.young.initial.mark"),

    /**
     * Property key for JVM TI initiated gc.
     */
    WARN_EXPLICIT_GC_JVMTI("warn.explicit.gc.jvmti"),

    /**
     * Property key for explicit garbage collection invoking the PARALLEL_COMPACTING_OLD collector.
     */
    WARN_EXPLICIT_GC_PARALLEL("warn.explicit.gc.parallel"),

    /**
     * Property key for explicit garbage collection invoking the SERIAL_OLD collector.
     */
    WARN_EXPLICIT_GC_SERIAL("warn.explicit.gc.serial"),

    /**
     * Property key for explicit garbage collection invoking the PARALLEL_SERIAL_OLD collector.
     */
    WARN_EXPLICIT_GC_SERIAL_PARALLEL("warn.explicit.gc.serial.parallel"),

    /**
     * Property key for explicit garbage collection invoking a full collection (collector unknown).
     */
    WARN_EXPLICIT_GC_UNKNOWN("warn.explicit.gc.unknown"),

    /**
     * Property key for the ratio of gc time vs. safepoint time showing a significant amount of safepoint time (&gt;20%)
     * is not GC related.
     */
    WARN_GC_SAFEPOINT_RATIO("warn.gc.safepoint.ratio"),

    /**
     * Property key for the ratio of gc time vs. stopped time showing a significant amount of stopped time (&gt;20%) is
     * not GC related.
     */
    WARN_GC_STOPPED_RATIO("warn.gc.stopped.ratio"),

    /**
     * Property key for heap dump initiated gc.
     */
    WARN_HEAP_DUMP_INITIATED_GC("warn.heap.dump.initiated.gc"),

    /**
     * Property key for heap inspection initiated gc.
     */
    WARN_HEAP_INSPECTION_INITIATED_GC("warn.heap.inspection.initiated.gc"),

    /**
     * Property key for inverted parallelism.
     */
    WARN_PARALLELISM_INVERTED("warn.parallelism.inverted"),

    /**
     * Property key for min perm not equal to max perm.
     */
    WARN_PERM_MIN_NOT_EQUAL_MAX("warn.perm.min.not.equal.max"),

    /**
     * Property key for perm gen not explicitly set.
     */
    WARN_PERM_SIZE_NOT_SET("warn.perm.size.not.set"),

    /**
     * Property key for adding option to output command line flags at beginning of gc logging.
     */
    WARN_PRINT_COMMANDLINE_FLAGS("warn.print.commandline.flags"),

    /**
     * Property key for -XX:-PrintGCCause.
     */
    WARN_PRINT_GC_CAUSE_DISABLED("warn.print.gc.cause.disabled"),

    /**
     * Property key for -XX:+PrintGCCause missing.
     */
    WARN_PRINT_GC_CAUSE_MISSING("warn.print.gc.cause.missing"),

    /**
     * Property key for -XX:_PrintGCCause not enabled.
     */
    WARN_PRINT_GC_CAUSE_NOT_ENABLED("warn.print.gc.cause.not.enabled"),

    /**
     * Property key for printing additional heap data (-XX:+PrintHeapAtGC).
     */
    WARN_PRINT_HEAP_AT_GC("warn.print.heap.at.gc"),

    /**
     * Property key for outputting tenuring distribution information (-XX:+PrintTenuringDistribution).
     */
    WARN_PRINT_TENURING_DISTRIBUTION("warn.print.tenuring.distribution"),

    /**
     * Property key for inverted serialism.
     */
    WARN_SERIALISM_INVERTED("warn.serialism.inverted"),

    /**
     * Property key for sys greater than user time.
     */
    WARN_SYS_GT_USER("warn.sys.gt.user"),

    /**
     * Property key for unidentified line(s) needing reporting.
     */
    WARN_UNIDENTIFIED_LOG_LINE_REPORT("warn.unidentified.log.line.report");

    private String key;

    private Analysis(final String key) {
        this.key = key;
    }

    /**
     * @return Analysis property file key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return Analysis property file value.
     */
    public String getValue() {
        return GcUtil.getPropertyValue(Constants.ANALYSIS_PROPERTY_FILE, key);
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
