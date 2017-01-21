/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

/**
 * Analysis constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Analysis {

    /**
     * Property file.
     */
    public static final String PROPERTY_FILE = "analysis";

    /**
     * Property key for partial log file.
     */
    public static final String INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED = "info.first.timestamp.threshold.exceeded";

    /**
     * Property key for explicit garbage collection invoking the PARALLEL_OLD_COMPACTING collector.
     */
    public static final String WARN_EXPLICIT_GC_PARALLEL = "warn.explicit.gc.parallel";

    /**
     * Property key for explicit garbage collection invoking the SERIAL_OLD collector.
     */
    public static final String WARN_EXPLICIT_GC_SERIAL = "warn.explicit.gc.serial";

    /**
     * Property key for explicit garbage collection invoking the CMS_SERIAL_OLD collector.
     */
    public static final String ERROR_EXPLICIT_GC_SERIAL_CMS = "error.explicit.gc.serial.cms";

    /**
     * Property key for explicit garbage collection invoking the G1_FULL_GC collector.
     */
    public static final String ERROR_EXPLICIT_GC_SERIAL_G1 = "error.explicit.gc.serial.g1";

    /**
     * Property key for explicit garbage collection invoking the PARALLEL_SERIAL_OLD collector.
     */
    public static final String WARN_EXPLICIT_GC_SERIAL_PARALLEL = "warn.explicit.gc.serial.parallel";

    /**
     * Property key for explicit garbage collection disabled.
     */
    public static final String WARN_EXPLICIT_GC_DISABLED = "warn.explicit.gc.disabled";

    /**
     * Property key for explicit garbage not collected concurrently.
     */
    public static final String ERROR_EXPLICIT_GC_NOT_CONCURRENT = "error.explicit.gc.not.concurrent";

    /**
     * Property key for explicit garbage collection disabled and specifying concurrent collections.
     */
    public static final String WARN_EXPLICIT_GC_DISABLED_CONCURRENT = "warn.explicit.gc.disabled.concurrent";

    /**
     * Property key for -XX:+PrintGCApplicationStoppedTime missing.
     */
    public static final String WARN_APPLICATION_STOPPED_TIME_MISSING = "warn.application.stopped.time.missing";

    /**
     * Property key for the ratio of gc time vs. stopped time showing a significant amount of stopped time (&gt;20%) is
     * not GC related.
     */
    public static final String WARN_GC_STOPPED_RATIO = "warn.gc.stopped.ratio";

    /**
     * Property key for thread stack size not set.
     */
    public static final String WARN_THREAD_STACK_SIZE_NOT_SET = "warn.thread.stack.size.not.set";

    /**
     * Property key for thread stack size is large.
     */
    public static final String WARN_THREAD_STACK_SIZE_LARGE = "warn.thread.stack.size.large";

    /**
     * Property key for min heap not equal to max heap.
     */
    public static final String WARN_HEAP_MIN_NOT_EQUAL_MAX = "warn.heap.min.not.equal.max";

    /**
     * Property key for perm gen or metaspace size not explicitly set.
     */
    public static final String WARN_PERM_METASPACE_SIZE_NOT_SET = "warn.perm.metaspace.size.not.set";

    /**
     * Property key for perm gen not explicitly set.
     */
    public static final String WARN_PERM_SIZE_NOT_SET = "warn.perm.size.not.set";

    /**
     * Property key for metaspace size not explicitly set.
     */
    public static final String WARN_METASPACE_SIZE_NOT_SET = "warn.metaspace.size.not.set";

    /**
     * Property key for min perm not equal to max perm.
     */
    public static final String WARN_PERM_MIN_NOT_EQUAL_MAX = "warn.perm.min.not.equal.max";

    /**
     * Property key for metaspace not equal to max metaspace.
     */
    public static final String WARN_METASPACE_MIN_NOT_EQUAL_MAX = "warn.metaspace.min.not.equal.max";

    /**
     * Property key for the SERIAL_OLD collector being invoked for reasons other than explicit gc.
     */
    public static final String ERROR_SERIAL_GC = "error.serial.gc";

    /**
     * Property key for the CMS collector invoking a serial collection for reasons other than explicit gc.
     */
    public static final String ERROR_SERIAL_GC_CMS = "error.serial.gc.cms";

    /**
     * Property key for the G1 collector invoking a serial collection for reasons other than explicit gc.
     */
    public static final String ERROR_SERIAL_GC_G1 = "error.serial.gc.g1";

    /**
     * Property key for the Parallel (Throughput) PARALLEL_SERIAL_OLD (single-threaded) collector.
     */
    public static final String ERROR_SERIAL_GC_PARALLEL = "error.serial.gc.parallel";

    /**
     * Property key for the RMI Distributed Garbage Collection (DGC) not being managed.
     */
    public static final String WARN_RMI_DGC_NOT_MANAGED = "warn.rmi.dgc.not.managed";

    /**
     * Property key for -Dsun.rmi.dgc.client.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    public static final String WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT = "warn.rmi.dgc.client.gcInterval.redundant";

    /**
     * Property key for -Dsun.rmi.dgc.server.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    public static final String WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT = "warn.rmi.dgc.server.gcInterval.redundant";

    /**
     * Property key for small sun.rmi.dgc.client.gcInterval.
     */
    public static final String WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL = "warn.rmi.dgc.client.gcInterval.small";

    /**
     * Property key for small sun.rmi.dgc.server.gcInterval.
     */
    public static final String WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL = "warn.rmi.dgc.server.gcInterval.small";

    /**
     * Property key for heap dump on out of memory error option missing.
     */
    public static final String WARN_HEAP_DUMP_ON_OOME_MISSING = "warn.heap.dump.on.oome.missing";

    /**
     * Property key for heap dump on memory error option disabled.
     */
    public static final String WARN_HEAP_DUMP_ON_OOME_DISABLED = "warn.heap.dump.on.oome.disabled";

    /**
     * Property key for instrumentation.
     */
    public static final String INFO_INSTRUMENTATION = "info.instrumentation";

    /**
     * Property key for native library.
     */
    public static final String INFO_NATIVE = "info.native";

    /**
     * Property key for disabling compiling bytecode in the background.
     */
    public static final String WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED = "warn.bytecode.compile.background.disabled";

    /**
     * Property key for precompiling bytecode.
     */
    public static final String WARN_BYTECODE_COMPILE_FIRST_INVOCATION = "warn.bytecode.compile.first.invocation";

    /**
     * Property key for bytecode compilation disabled.
     */
    public static final String WARN_BYTECODE_COMPILE_DISABLED = "warn.bytecode.compile.disabled";

    /**
     * Property key for adding option to output command line flags at beginning of gc logging.
     */
    public static final String WARN_PRINT_COMMANDLINE_FLAGS = "warn.print.commandline.flags";

    /**
     * Property key for adding option to output details at gc needed for analysis.
     */
    public static final String WARN_PRINT_GC_DETAILS_MISSING = "warn.print.gc.details.missing";

    /**
     * Property key for gc details option disabled.
     */
    public static final String WARN_PRINT_GC_DETAILS_DISABLED = "warn.print.gc.details.disabled";

    /**
     * Property key for not specifying the cms be used for old collections.
     */
    public static final String ERROR_CMS_NEW_SERIAL_OLD = "error.cms.new.serial.old";

    /**
     * Property key for concurrent mode failure.
     */
    public static final String ERROR_CMS_CONCURRENT_MODE_FAILURE = "error.cms.concurrent.mode.failure";

    /**
     * Property key for concurrent mode interrupted.
     */
    public static final String ERROR_CMS_CONCURRENT_MODE_INTERRUPTED = "error.cms.concurrent.mode.interrupted";

    /**
     * Property key for promotion failed.
     */
    public static final String ERROR_CMS_PROMOTION_FAILED = "error.cms.promotion.failed";

    /**
     * Property key for PAR_NEW collector disabled.
     */
    public static final String WARN_CMS_PAR_NEW_DISABLED = "warn.cms.par.new.disabled";

    /**
     * Property key for CMS collector not unloading classes.
     */
    public static final String WARN_CMS_CLASS_UNLOADING_DISABLED = "warn.cms.class.unloading.disabled";

    /**
     * Property key for CMS collector running in incremental mode.
     */
    public static final String WARN_CMS_INCREMENTAL_MODE = "warn.cms.incremental.mode";

    /**
     * Property key for specifying both the CMS collector running in incremental mode and an initiating occupancy
     * fraction.
     */
    public static final String WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT = "warn.cms.inc.mode.with.init.occup.fract";

    /**
     * Property key for -XX:+PrintReferenceGC.
     */
    public static final String WARN_PRINT_REFERENCE_GC_ENABLED = "warn.print.reference.gc.enabled";

    /**
     * Property key for -XX:+PrintGCCause.
     */
    public static final String WARN_PRINT_GC_CAUSE_MISSING = "warn.print.gc.cause.missing";

    /**
     * Property key for -XX:-PrintGCCause.
     */
    public static final String WARN_PRINT_GC_CAUSE_DISABLED = "warn.print.gc.cause.disabled";

    /**
     * Property key for -XX:+TieredCompilation, which has issues with JDK7.
     */
    public static final String WARN_JDK7_TIERED_COMPILATION_ENABLED = "warn.jdk7.tiered.compilation.enabled";

    /**
     * Property key for -XX:+PrintStringDeduplicationStatistics.
     */
    public static final String WARN_PRINT_STRING_DEDUP_STATS_ENABLED = "warn.print.string.dedup.stats.enabled";

    /**
     * Property key for biased locking disabled (-XX:-UseBiasedLocking).
     */
    public static final String WARN_BIASED_LOCKING_DISABLED = "warn.biased.locking.disabled";

    /**
     * Property key for heap dump initiated gc.
     */
    public static final String WARN_HEAP_DUMP_INITIATED_GC = "warn.heap.dump.initiated.gc";

    /**
     * Property key for heap inspection initiated gc.
     */
    public static final String WARN_HEAP_INSPECTION_INITIATED_GC = "warn.heap.inspection.initiated.gc";

    /**
     * Property key for class histogram output.
     */
    public static final String WARN_CLASS_HISTOGRAM = "warn.class.histogram";

    /**
     * Property key for printing a class histogram when a thread dump is initiated (-XX:+PrintClassHistogram).
     */
    public static final String WARN_PRINT_CLASS_HISTOGRAM = "warn.print.class.histogram";

    /**
     * Property key for printing a class histogram when a thread dump is initiated
     * (-XX:+PrintClassHistogramAfterFullGC).
     */
    public static final String WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC = "warn.print.class.histogram.after.full.gc";

    /**
     * Property key for printing a class histogram when a thread dump is initiated
     * (-XX:+PrintClassHistogramBeforeFullGC).
     */
    public static final String WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC = "warn.print.class.histogram.before.full.gc";

    /**
     * Property key for when the Metaspace is not able to be resized, and the JVM is doing full, serial collections
     * attempting to free Metaspace before throwing OutOfMemoryError. The Metaspace is undersized, or there is a
     * Metaspace leak.
     */
    public static final String ERROR_METASPACE_ALLOCATION_FAILURE = "error.metaspace.allocation.failure";

    /**
     * Property key for JVM TI initiated gc.
     */
    public static final String WARN_EXPLICIT_GC_JVMTI = "warn.explicit.gc.jvmti";

    /**
     * Property key for printing application concurrent time (-XX:+PrintGCApplicationConcurrentTime).
     */
    public static final String WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME = "warn.print.gc.application.concurrent.time";

    /**
     * Property key for printing additional heap data (-XX:+PrintHeapAtGC).
     */
    public static final String WARN_PRINT_HEAP_AT_GC = "warn.print.heap.at.gc";

    /**
     * Property key for outputting class unloading information (-XX:+TraceClassUnloading).
     */
    public static final String WARN_TRACE_CLASS_UNLOADING = "warn.trace.class.unloading";

    /**
     * Property key for the garbage collection overhead limit being reached.
     */
    public static final String ERROR_GC_TIME_LIMIT_EXCEEEDED = "error.gc.time.limit.exceeded";

    /**
     * Property key for compressed class pointers disabled (-XX:-UseCompressedClassPointers), and heap &lt; 32G.
     */
    public static final String ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G = "error.comp.class.disabled.heap.lt.32g";

    /**
     * Property key for compressed class pointers enabled (-XX:+UseCompressedClassPointers), and heap &gt;= 32G.
     */
    public static final String ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G = "error.comp.class.enabled.heap.gt.32g";

    /**
     * Property key for compressed class pointers space size set (-XX:CompressedClassSpaceSize), and heap &gt;= 32G.
     */
    public static final String ERROR_COMP_CLASS_SIZE_HEAP_GT_32G = "error.comp.class.size.heap.gt.32g";

    /**
     * Property key for compressed object references disabled (-XX:-UseCompressedOops), and heap &lt; 32G.
     */
    public static final String ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G = "error.comp.oops.disabled.heap.lt.32g";

    /**
     * Property key for compressed object references enabled (-XX:+UseCompressedOops), and heap &gt;= 32G.
     */
    public static final String ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G = "error.comp.oops.enabled.heap.gt.32g";

    /**
     * Property key for the compressed class space size not set.
     */
    public static final String INFO_COMP_CLASS_SIZE_NOT_SET = "info.comp.class.size.not.set";

    /**
     * Property key for compressed class pointers size set (-XX:CompressedClassSpaceSize) with compressed object
     * references disabled (-XX:-UseCompressedOops).
     */
    public static final String INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED = "info.comp.class.size.comp.oops.disabled";

    /**
     * Property key for compressed class pointers size set (-XX:CompressedClassSpaceSize) with compressed class pointers
     * disabled (-XX:+UseCompressedClassPointers).
     */
    public static final String INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED = "info.comp.class.size.comp.class.disabled";

    /**
     * Property key for compressed class pointers disabled (-XX:-UseCompressedClassPointers), and heap size unknown.
     */
    public static final String WARN_COMP_CLASS_DISABLED_HEAP_UNK = "warn.comp.class.disabled.heap.unk";

    /**
     * Property key for compressed object references disabled (-XX:-UseCompressedOops), and heap size unknown.
     */
    public static final String WARN_COMP_OOPS_DISABLED_HEAP_UNK = "warn.comp.oops.disabled.heap.unk";

    /**
     * Property key for CMS Free List Space statistics being output.
     */
    public static final String INFO_PRINT_FLS_STATISTICS = "info.print.fls.statistics";

    /**
     * Property key for G1 evacuation failure.
     */
    public static final String ERROR_G1_EVACUATION_FAILURE = "error.g1.evacuation.failure";

    /**
     * Property key for outputting tenuring distribution information (-XX:+PrintTenuringDistribution).
     */
    public static final String WARN_PRINT_TENURING_DISTRIBUTION = "warn.print.tenuring.distribution";

    /**
     * Property key for GC log file rotation disabled (-XX:-UseGCLogFileRotation).
     */
    public static final String INFO_GC_LOG_FILE_ROTATION_DISABLED = "info.gc.log.file.rotation.disabled";

    /**
     * Property key for specify number of GC log files (-XX:NumberOfGCLogFiles) with log rotation disabled
     * (-XX:-UseGCLogFileRotation).
     */
    public static final String WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED = "warn.gc.log.file.num.rotation.disabled";

    /**
     * Property key for swapping.
     */
    public static final String INFO_SWAPPING = "info.swapping";

    /**
     * Property key for insufficient physical memory.
     */
    public static final String ERROR_PHYSICAL_MEMORY = "error.physical.memory";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private Analysis() {

    }
}
