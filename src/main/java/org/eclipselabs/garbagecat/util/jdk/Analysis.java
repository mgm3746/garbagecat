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
     * Property key for adaptive size policy disabled with -XX:-UseAdaptiveSizePolicy.
     */
    ERROR_ADAPTIVE_SIZE_POLICY_DISABLED("error.adaptive.size.policy.disabled"),

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
     * Property key for multi-threaded CMS initial mark disabled with -XX:-CMSParallelInitialMarkEnabled.
     */
    ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED("error.cms.parallel.initial.mark.disabled"),

    /**
     * Property key for multi-threaded CMS remark disabled with -XX:-CMSParallelRemarkEnabled.
     */
    ERROR_CMS_PARALLEL_REMARK_DISABLED("error.cms.parallel.remark.disabled"),

    /**
     * Property key for promotion failed.
     */
    ERROR_CMS_PROMOTION_FAILED("error.cms.promotion.failed"),

    /**
     * Property key for not specifying the CMS collector be used for old collections, causing the CMS_SERIAL_OLD
     * collector to be used by default.
     */
    ERROR_CMS_SERIAL_OLD("error.cms.serial.old"),

    /**
     * Property key for compressed class pointers disabled (-XX:-UseCompressedClassPointers), and heap &lt; 32G.
     */
    ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G("error.comp.class.disabled.heap.lt.32g"),

    /**
     * Property key for compressed class pointers enabled (-XX:+UseCompressedClassPointers), and heap &gt;= 32G.
     */
    ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G("error.comp.class.enabled.heap.gt.32g"),

    /**
     * Property key for compressed class pointers space size set (-XX:CompressedClassSpaceSize), and heap &gt;= 32G.
     */
    ERROR_COMP_CLASS_SIZE_HEAP_GT_32G("error.comp.class.size.heap.gt.32g"),

    /**
     * Property key for undersized compressed class pointers space causing full GCs.
     */
    ERROR_COMP_CLASS_SPACE_UNDERSIZED("error.comp.class.space.undersized"),

    /**
     * Property key for compressed object references disabled (-XX:-UseCompressedOops), and heap &lt; 32G.
     */
    ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G("error.comp.oops.disabled.heap.lt.32g"),

    /**
     * Property key for compressed object references enabled (-XX:+UseCompressedOops), and heap &gt;= 32G.
     */
    ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G("error.comp.oops.enabled.heap.gt.32g"),

    /**
     * Property key for logging with datestamps only, no timestamps.
     */
    ERROR_DATESTAMP_NO_TIMESTAMP("error.datestamp.no.timestamp"),

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
     * Property key for when MaxMetaspaceSize is less than CompressedClassSpaceSize. MaxMetaspaceSize includes
     * CompressedClassSpaceSize, so MaxMetaspaceSize should be larger than CompressedClassSpaceSize.
     */
    ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE("error.metaspace.size.lt.comp.class.size"),

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
     * Property key for unidentified log lines w/o preparsing.
     */
    ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE("error.unidentified.log.lines.preparse"),

    /**
     * Property key for compressed class pointers size set (-XX:CompressedClassSpaceSize) with compressed class pointers
     * disabled (-XX:+UseCompressedClassPointers).
     */
    INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED("info.comp.class.size.comp.class.disabled"),

    /**
     * Property key for compressed class pointers size set (-XX:CompressedClassSpaceSize) with compressed object
     * references disabled (-XX:-UseCompressedOops).
     */
    INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED("info.comp.class.size.comp.oops.disabled"),

    /**
     * Property key for -XX:-ExplicitGCInvokesConcurrentAndUnloadsClasses in combination with -XX:+DisableExplicitGC.
     */
    INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA("info.cruft.exp.gc.inv.con.and.unl.cla"),

    /**
     * Property key for -XX:+UnlockDiagnosticVMOptions.
     */
    INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED("info.diagnostic.vm.options.enabled"),

    /**
     * Property key for experimental jvm options enabled with <code>-XX:+UnlockExperimentalVMOptions</code>.
     */
    INFO_EXPERIMENTAL_VM_OPTIONS("info.experimental.vm.options.enabled"),

    /**
     * Property key for partial log file.
     */
    INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED("info.first.timestamp.threshold.exceeded"),

    /**
     * Property key for humongous allocations.
     */
    INFO_G1_HUMONGOUS_ALLOCATION("info.g1.humongous.allocation"),

    /**
     * Property key for summarized remembered set processing output.
     */
    INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT("info.g1.summarize.rset.stats.output"),

    /**
     * Property key for GC log file rotation disabled (-XX:-UseGCLogFileRotation).
     */
    INFO_GC_LOG_FILE_ROTATION_DISABLED("info.gc.log.file.rotation.disabled"),

    /**
     * Property key for GC log file rotation not enabled (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N
     * -XX:NumberOfGCLogFiles=N).
     */
    INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED("info.gc.log.file.rotation.not.enabled"),

    /**
     * Property key for heap dumps enabled without specifying a location with the -XX:HeapDumpPath option.
     */
    INFO_HEAP_DUMP_PATH_MISSING("info.heap.dump.path.missing"),

    /**
     * Property key for instrumentation.
     */
    INFO_INSTRUMENTATION("info.instrumentation"),

    /**
     * Property key for JMX enabled with -Dcom.sun.management.jmxremote or -XX:+ManagementServer.
     */
    INFO_JMX_ENABLED("info.jmx.enabled"),

    /**
     * Property key for overriding the number of times an object is copied between survivor spaces being set with
     * -XX:MaxTenuringThreshold=N (0-15). 0 = disabled. 15 (default) = promote when the survivor space fills. Unless
     * testing has shown this improves performance, consider removing this option to allow the default value to be
     * applied.
     */
    INFO_MAX_TENURING_OVERRIDE("info.max.tenuring.override"),

    /**
     * Property key for native library.
     */
    INFO_NATIVE("info.native"),

    /**
     * Property key for young space &gt;= old space.
     */
    INFO_NEW_RATIO_INVERTED("info.new.ratio.inverted"),

    /**
     * Property key for otherwise unaccounted JVM options disabled.
     */
    INFO_UNACCOUNTED_OPTIONS_DISABLED("info.unaccounted.options.disabled"),

    /**
     * Property key for a very old JDK with a permanent generation.
     */
    INFO_PERM_GEN("info.perm.gen"),

    /**
     * Property key for disabling Adaptive Resize Policy output with -XX:-PrintAdaptiveSizePolicy.
     */
    INFO_PRINT_ADAPTIVE_RESIZE_PLCY_DISABLED("info.print.adaptive.resize.plcy.disabled"),

    /**
     * Property key for enabling Adaptive Resize Policy output with -XX:+PrintAdaptiveSizePolicy.
     */
    INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED("info.print.adaptive.resize.plcy.enabled"),

    /**
     * Property key for CMS Free List Space statistics being output.
     */
    INFO_PRINT_FLS_STATISTICS("info.print.fls.statistics"),

    /**
     * Property key for -XX:+PrintPromotionFailure.
     */
    INFO_PRINT_PROMOTION_FAILURE("info.print.promotion.failure"),

    /**
     * Property key for the survivor ratio being set with -XX:SurvivorRatio=N (e.g. -XX:SurvivorRatio=6 ).
     * 
     */
    INFO_SURVIVOR_RATIO("info.survivor.ratio"),

    /**
     * Property key for the target survivor ratio being set with XX:TargetSurvivorRatio=N (e.g.
     * -XX:TargetSurvivorRatio=90).
     * 
     */
    INFO_SURVIVOR_RATIO_TARGET("info.survivor.ratio.target"),

    /**
     * Property key for swapping.
     */
    INFO_SWAPPING("info.swapping"),

    /**
     * Property key for swapping disabled.
     */
    INFO_SWAP_DISABLED("info.swap.disabled"),

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
     * Property key for biased locking disabled (-XX:-UseBiasedLocking).
     */
    WARN_BIASED_LOCKING_DISABLED("warn.biased.locking.disabled"),

    /**
     * Property key for disabling compiling bytecode in the background.
     */
    WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED("warn.bytecode.compile.background.disabled"),

    /**
     * Property key for bytecode compilation disabled.
     */
    WARN_BYTECODE_COMPILE_DISABLED("warn.bytecode.compile.disabled"),

    /**
     * Property key for precompiling bytecode.
     */
    WARN_BYTECODE_COMPILE_FIRST_INVOCATION("warn.bytecode.compile.first.invocation"),

    /**
     * Property key for -XX:+UseCGroupMemoryLimitForHeap.
     */
    WARN_CGROUP_MEMORY_LIMIT("warn.cgroup.memory.limit"),

    /**
     * Property key for class histogram output.
     */
    WARN_CLASS_HISTOGRAM("warn.class.histogram"),

    /**
     * Property key for class unloading disabled with -XX:-ClassUnloading.
     */
    WARN_CLASS_UNLOADING_DISABLED("warn.class.unloading.disabled"),

    /**
     * Property key for CMS collector class unloading disabled.
     */
    WARN_CMS_CLASS_UNLOADING_DISABLED("warn.cms.class.unloading.disabled"),

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
     * Property key for -XX:CMSInitiatingOccupancyFraction without -XX:+UseCMSInitiatingOccupancyOnly.
     */
    WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING("warn.cms.init.occupancy.only.missing"),

    /**
     * Property key for CMS initial mark low parallelism.
     */
    WARN_CMS_INITIAL_MARK_LOW_PARALLELISM("warn.cms.initial.mark.low.parallelism"),

    /**
     * Property key for PAR_NEW collector disabled.
     */
    WARN_CMS_PAR_NEW_DISABLED("warn.cms.par.new.disabled"),

    /**
     * Property key for CMS remark low parallelism.
     */
    WARN_CMS_REMARK_LOW_PARALLELISM("warn.cms.remark.low.parallelism"),

    /**
     * Property key for compressed class pointers disabled (-XX:-UseCompressedClassPointers), and heap size unknown.
     */
    WARN_COMP_CLASS_DISABLED_HEAP_UNK("warn.comp.class.disabled.heap.unk"),

    /**
     * Property key for compressed object references disabled (-XX:-UseCompressedOops), and heap size unknown.
     */
    WARN_COMP_OOPS_DISABLED_HEAP_UNK("warn.comp.oops.disabled.heap.unk"),

    /**
     * Property key for explicit garbage collection disabled.
     */
    WARN_EXPLICIT_GC_DISABLED("warn.explicit.gc.disabled"),

    /**
     * Property key for explicit garbage collection disabled and specifying concurrent collections.
     */
    WARN_EXPLICIT_GC_DISABLED_CONCURRENT("warn.explicit.gc.disabled.concurrent"),

    /**
     * Property key for explicit garbage collection invoking the G1_YOUNG_INITIAL_MARK collector.
     */
    WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK("warn.explicit.gc.g1.young.initial.mark"),

    /**
     * Property key for JVM TI initiated gc.
     */
    WARN_EXPLICIT_GC_JVMTI("warn.explicit.gc.jvmti"),

    /**
     * Property key for explicit garbage not collected concurrently.
     */
    WARN_EXPLICIT_GC_NOT_CONCURRENT("warn.explicit.gc.not.concurrent"),

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
     * Property key for fast unordered timestamps (experimental) enabled with
     * <code>-XX:+UseFastUnorderedTimeStamps</code>.
     */
    WARN_FAST_UNORDERED_TIMESTAMPS("warn.fast.unordered.timestamps"),

    /**
     * Property key for the JDK8 prior to update 40.
     */
    WARN_G1_JDK8_PRIOR_U40("warn.g1.jdk8.prior.u40"),

    /**
     * Property key for the JDK8 prior to update 40 recommendations.
     */
    WARN_G1_JDK8_PRIOR_U40_RECS("warn.g1.jdk8.prior.u40.recs"),

    /**
     * Property key for the occupancy threshold for a region to be considered as a candidate region for a G1_CLEANUP
     * collection being specified with <code>-XX:G1MixedGCLiveThresholdPercent=NN</code>.
     */
    WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT("warn.g1.mixed.gc.live.thrshld.prcnt"),

    /**
     * Property key for specifying the number of GC log files (-XX:NumberOfGCLogFiles) to keep with log rotation is
     * disabled (-XX:-UseGCLogFileRotation).
     */
    WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED("warn.gc.log.file.num.rotation.disabled"),

    /**
     * Property key for specifying the gc log file size that triggers rotation (-XX:GCLogFileSize=N) is small (&lt; 5M).
     */
    WARN_GC_LOG_FILE_SIZE_SMALL("warn.gc.log.file.size.small"),

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
     * Property key for heap dump on memory error option disabled.
     */
    WARN_HEAP_DUMP_ON_OOME_DISABLED("warn.heap.dump.on.oome.disabled"),

    /**
     * Property key for heap dump on out of memory error option missing.
     */
    WARN_HEAP_DUMP_ON_OOME_MISSING("warn.heap.dump.on.oome.missing"),

    /**
     * Property key for heap dump filename specified.
     */
    WARN_HEAP_DUMP_PATH_FILENAME("warn.heap.dump.path.filename"),

    /**
     * Property key for heap inspection initiated gc.
     */
    WARN_HEAP_INSPECTION_INITIATED_GC("warn.heap.inspection.initiated.gc"),

    /**
     * Property key for min heap not equal to max heap.
     */
    WARN_HEAP_MIN_NOT_EQUAL_MAX("warn.heap.min.not.equal.max"),

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
     * Property key for printing a class histogram when a thread dump is initiated (-XX:+PrintClassHistogram).
     */
    WARN_PRINT_CLASS_HISTOGRAM("warn.print.class.histogram"),

    /**
     * Property key for printing a class histogram after a full gc (-XX:+PrintClassHistogramAfterFullGC).
     */
    WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC("warn.print.class.histogram.after.full.gc"),

    /**
     * Property key for printing a class histogram before a full gc (-XX:+PrintClassHistogramBeforeFullGC).
     */
    WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC("warn.print.class.histogram.before.full.gc"),

    /**
     * Property key for adding option to output command line flags at beginning of gc logging.
     */
    WARN_PRINT_COMMANDLINE_FLAGS("warn.print.commandline.flags"),

    /**
     * Property key for printing application concurrent time (-XX:+PrintGCApplicationConcurrentTime).
     */
    WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME("warn.print.gc.application.concurrent.time"),

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
     * Property key for gc details option disabled.
     */
    WARN_PRINT_GC_DETAILS_DISABLED("warn.print.gc.details.disabled"),

    /**
     * Property key for adding option to output details at gc needed for analysis.
     */
    WARN_PRINT_GC_DETAILS_MISSING("warn.print.gc.details.missing"),

    /**
     * Property key for printing additional heap data (-XX:+PrintHeapAtGC).
     */
    WARN_PRINT_HEAP_AT_GC("warn.print.heap.at.gc"),

    /**
     * Property key for -XX:+PrintReferenceGC.
     */
    WARN_PRINT_REFERENCE_GC_ENABLED("warn.print.reference.gc.enabled"),

    /**
     * Property key for -XX:+PrintStringDeduplicationStatistics.
     */
    WARN_PRINT_STRING_DEDUP_STATS_ENABLED("warn.print.string.dedup.stats.enabled"),

    /**
     * Property key for outputting tenuring distribution information (-XX:+PrintTenuringDistribution).
     */
    WARN_PRINT_TENURING_DISTRIBUTION("warn.print.tenuring.distribution"),

    /**
     * Property key for -Dsun.rmi.dgc.client.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT("warn.rmi.dgc.client.gcInterval.redundant"),

    /**
     * Property key for small sun.rmi.dgc.client.gcInterval.
     */
    WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL("warn.rmi.dgc.client.gcInterval.small"),

    /**
     * Property key for -Dsun.rmi.dgc.server.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT("warn.rmi.dgc.server.gcInterval.redundant"),

    /**
     * Property key for small sun.rmi.dgc.server.gcInterval.
     */
    WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL("warn.rmi.dgc.server.gcInterval.small"),

    /**
     * Property key for disabling tenuring with -XX:MaxTenuringThreshold=0 or by setting it to a value greater than 15
     * (e.g. -XX:MaxTenuringThreshold=32).
     */
    WARN_TENURING_DISABLED("warn.tenuring.disabled"),

    /**
     * Property key for thread stack size is large.
     */
    WARN_THREAD_STACK_SIZE_LARGE("warn.thread.stack.size.large"),

    /**
     * Property key for thread stack size not set.
     */
    WARN_THREAD_STACK_SIZE_NOT_SET("warn.thread.stack.size.not.set"),

    /**
     * Property key for -XX:+TieredCompilation.
     */
    WARN_TIERED_COMPILATION_ENABLED("warn.tiered.compilation.enabled"),

    /**
     * Property key for outputting class unloading information (-XX:+TraceClassUnloading).
     */
    WARN_TRACE_CLASS_UNLOADING("warn.trace.class.unloading"),

    /**
     * Property key for unidentified line(s) needing reporting.
     */
    WARN_UNIDENTIFIED_LOG_LINE_REPORT("warn.unidentified.log.line.report"),

    /**
     * Property key for -XX:+UseMembar.
     */
    WARN_USE_MEMBAR("warn.use.membar");

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
