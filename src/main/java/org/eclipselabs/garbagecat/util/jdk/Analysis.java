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
     * Property key for explicit garbage collection.
     */
    public static final String KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED = "first.timestamp.threshold.exceeded";

    /**
     * Property key for explicit garbage collection invoking the PARALLELL_OLD_COMPACTING collector.
     */
    public static final String KEY_EXPLICIT_GC_PARALLEL = "explicit.gc.parallel";

    /**
     * Property key for explicit garbage collection invoking the SERIAL_OLD collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL = "explicit.gc.serial";

    /**
     * Property key for explicit garbage collection invoking the CMS_SERIAL_OLD collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL_CMS = "explicit.gc.serial.cms";

    /**
     * Property key for explicit garbage collection invoking the G1_FULL_GC collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL_G1 = "explicit.gc.serial.g1";

    /**
     * Property key for explicit garbage collection invoking the PARALLEL_SERIAL_OLD collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL_PARALLEL = "explicit.gc.serial.parallel";

    /**
     * Property key for explicit garbage collection disabled.
     */
    public static final String KEY_EXPLICIT_GC_DISABLED = "explicit.gc.disabled";

    /**
     * Property key for explicit garbage collection disabled.
     */
    public static final String KEY_EXPLICIT_GC_NOT_CONCURRENT = "explicit.gc.not.concurrent";

    /**
     * Property key for explicit garbage collection disabled and specifying concurrent collections.
     */
    public static final String KEY_EXPLICIT_GC_DISABLED_CONCURRENT = "explicit.gc.disabled.concurrent";

    /**
     * Property key for -XX:+PrintGCApplicationStoppedTime missing.
     */
    public static final String KEY_APPLICATION_STOPPED_TIME_MISSING = "application.stopped.time.missing";

    /**
     * Property key for the ratio of gc time vs. stopped time showing a significant amount of stopped time (&gt;20%) is
     * not GC related.
     */
    public static final String KEY_GC_STOPPED_RATIO = "gc.stopped.ratio";

    /**
     * Property key for thread stack size not set.
     */
    public static final String KEY_THREAD_STACK_SIZE_NOT_SET = "thread.stack.size.not.set";

    /**
     * Property key for thread stack size is large.
     */
    public static final String KEY_THREAD_STACK_SIZE_LARGE = "thread.stack.size.large";

    /**
     * Property key for min heap not equal to max heap.
     */
    public static final String KEY_MIN_HEAP_NOT_EQUAL_MAX_HEAP = "min.heap.not.equal.max.heap";

    /**
     * Property key for perm gen or metaspace size not explicitly set.
     */
    public static final String KEY_PERM_METASPACE_NOT_SET = "perm.metaspace.not.set";

    /**
     * Property key for min perm not equal to max perm.
     */
    public static final String KEY_MIN_PERM_NOT_EQUAL_MAX_PERM = "min.perm.not.equal.max.perm";

    /**
     * Property key for metaspace not equal to max metaspace.
     */
    public static final String KEY_MIN_METASPACE_NOT_EQUAL_MAX_METASPACE = "min.metaspace.not.equal.max.metaspace";

    /**
     * Property key for the SERIAL_OLD collector being invoked for reasons other than explicit gc.
     */
    public static final String KEY_SERIAL_GC = "serial.gc";

    /**
     * Property key for the CMS collector invoking a serial collection for reasons other than explicit gc.
     */
    public static final String KEY_SERIAL_GC_CMS = "serial.gc.cms";

    /**
     * Property key for the G1 collector invoking a serial collection for reasons other than explicit gc.
     */
    public static final String KEY_SERIAL_GC_G1 = "serial.gc.g1";

    /**
     * Property key for the Parallel (Throughput) collector invoking a serial collection for reasons other than explicit
     * gc.
     */
    public static final String KEY_SERIAL_GC_PARALLEL = "serial.gc.parallel";

    /**
     * Property key for the RMI Distributed Garbage Collection (DGC) not being managed.
     */
    public static final String KEY_RMI_DGC_NOT_MANAGED = "rmi.dgc.not.managed";

    /**
     * Property key for -Dsun.rmi.dgc.client.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    public static final String KEY_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT = "rmi.dgc.client.gcInterval.redundant";

    /**
     * Property key for -Dsun.rmi.dgc.server.gcInterval.redundant in combination with -XX:+DisableExplicitGC.
     */
    public static final String KEY_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT = "rmi.dgc.server.gcInterval.redundant";

    /**
     * Property key for small sun.rmi.dgc.client.gcInterval.
     */
    public static final String KEY_RMI_DGC_CLIENT_GCINTERVAL_SMALL = "rmi.dgc.client.gcInterval.small";

    /**
     * Property key for small sun.rmi.dgc.server.gcInterval.
     */
    public static final String KEY_RMI_DGC_SERVER_GCINTERVAL_SMALL = "rmi.dgc.server.gcInterval.small";

    /**
     * Property key for heap dump on out of memory error option missing.
     */
    public static final String KEY_HEAP_DUMP_ON_OOME_MISSING = "heap.dump.on.oome.missing";

    /**
     * Property key for heap dump on memory error option disabled.
     */
    public static final String KEY_HEAP_DUMP_ON_OOME_DISABLED = "heap.dump.on.oome.disabled";

    /**
     * Property key for instrumentation.
     */
    public static final String KEY_INSTRUMENTATION = "instrumentation";

    /**
     * Property key for native library.
     */
    public static final String KEY_NATIVE = "native";

    /**
     * Property key for disabling compiling bytecode in the background.
     */
    public static final String KEY_BYTECODE_BACKGROUND_COMPILE_DISABLED = "bytecode.compile.background.disabled";

    /**
     * Property key for precompiling bytecode.
     */
    public static final String KEY_BYTECODE_COMPILE_FIRST_INVOCATION = "bytecode.compile.first.invocation";

    /**
     * Property key for bytecode compilation disabled.
     */
    public static final String KEY_BYTECODE_COMPILE_DISABLED = "bytecode.compile.disabled";

    /**
     * Property key for adding option to output command line flags at beginning of gc logging.
     */
    public static final String KEY_PRINT_COMMANDLINE_FLAGS = "print.commandline.flags";

    /**
     * Property key for adding option to output details at gc needed for analysis.
     */
    public static final String KEY_PRINT_GC_DETAILS_MISSING = "print.gc.details.missing";

    /**
     * Property key for gc details option disabled.
     */
    public static final String KEY_PRINT_GC_DETAILS_DISABLED = "print.gc.details.disabled";

    /**
     * Property key for not specifying the cms be used for old collections.
     */
    public static final String KEY_CMS_NEW_SERIAL_OLD = "cms.new.serial.old";

    /**
     * Property key for concurrent mode failure.
     */
    public static final String KEY_CMS_CONCURRENT_MODE_FAILURE = "cms.concurrent.mode.failure";

    /**
     * Property key for concurrent mode interrupted.
     */
    public static final String KEY_CMS_CONCURRENT_MODE_INTERRUPTED = "cms.concurrent.mode.interrupted";

    /**
     * Property key for promotion failed.
     */
    public static final String KEY_CMS_PROMOTION_FAILED = "cms.promotion.failed";

    /**
     * Property key for CMS collector not unloading classes.
     */
    public static final String KEY_CMS_CLASS_UNLOADING_DISABLED = "cms.class.unloading.disabled";

    /**
     * Property key for CMS collector running in incremental mode.
     */
    public static final String KEY_CMS_INCREMENTAL_MODE = "cms.incremental.mode";

    /**
     * Property key for specifying both the CMS collector running in incremental mode and an initiating occupancy
     * fraction.
     */
    public static final String KEY_CMS_INC_MODE_INIT_OCCUP_FRACT_CONFLICT = "cms.inc.mode.init.occup.fract.conflict";

    /**
     * Property key for -XX:+PrintReferenceGC.
     */
    public static final String KEY_PRINT_REFERENCE_GC_ENABLED = "print.reference.gc.enabled";

    /**
     * Property key for -XX:+PrintGCCause.
     */
    public static final String KEY_PRINT_GC_CAUSE_MISSING = "print.gc.cause.missing";

    /**
     * Property key for -XX:-PrintGCCause.
     */
    public static final String KEY_PRINT_GC_CAUSE_DISABLED = "print.gc.cause.disabled";

    /**
     * Property key for -XX:+TieredCompilation, which has issues with JDK7.
     */
    public static final String KEY_JDK7_TIERED_COMPILATION_ENABLED = "jdk7.tiered.compilation.enabled";

    /**
     * Property key for -XX:+PrintStringDeduplicationStatistics.
     */
    public static final String KEY_PRINT_STRING_DEDUP_STATS_ENABLED = "print.string.dedup.stats.enabled";

    /**
     * Property key for biased locking disabled (-XX:-UseBiasedLocking).
     */
    public static final String KEY_BIASED_LOCKING_DISABLED = "biased.locking.disabled";

    /**
     * Property key for heap dump initiated gc.
     */
    public static final String KEY_HEAP_DUMP_INITIATED_GC = "heap.dump.initiated.gc";

    /**
     * Property key for heap inspection initiated gc.
     */
    public static final String KEY_HEAP_INSPECTION_INITIATED_GC = "heap.inspection.initiated.gc";

    /**
     * Property key for printing a class histogram when a thread dump is initiated (-XX:+PrintClassHistogram).
     */
    public static final String KEY_PRINT_CLASS_HISTOGRAM = "print.class.histogram";

    /**
     * Property key for when the Metaspace is not able to be resized, and the JVM is doing full, serial collections
     * attempting to free Metaspace before throwing OutOfMemoryError. The Metaspace is undersized, or there is a
     * Metaspace leak.
     */
    public static final String KEY_METASPACE_ALLOCATION_FAILURE = "metaspace.allocation.failure";

    /**
     * Property key for JVM TI initiated gc.
     */
    public static final String KEY_EXPLICIT_GC_JVMTI = "explicit.gc.jvmti";

    /**
     * Property key for printing application concurrent time (-XX:+PrintGCApplicationConcurrentTime).
     */
    public static final String KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME = "print.gc.application.concurrent.time";

    /**
     * Property key for printing additional heap data (-XX:+PrintHeapAtGC).
     */
    public static final String KEY_PRINT_HEAP_AT_GC = "print.heap.at.gc";

    /**
     * Property key for outputting class unloading information (-XX:+TraceClassUnloading).
     */
    public static final String KEY_TRACE_CLASS_UNLOADING = "trace.class.unloading";

    /**
     * Property key for the garbage collection overhead limit being reached.
     */
    public static final String KEY_GC_OVERHEAD_LIMIT = "trace.class.unloading";

    /**
     * Property key for the garbage collection overhead limit being reached.
     */
    public static final String KEY_COMPRESSED_CLASS_SPACE_NOT_SET = "compressed.class.space.size.not.set";

    /**
     * Property key for CMS Free List Space statistics being output.
     */
    public static final String KEY_PRINT_FLS_STATISTICS = "print.fls.statistics";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private Analysis() {

    }
}
