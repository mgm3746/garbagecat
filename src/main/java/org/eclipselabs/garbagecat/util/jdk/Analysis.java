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
     * Property key for explicit gc with concurrent collectors (CMS, G1).
     */
    public static final String KEY_EXPLICIT_GC_UNECESSARY_CMS_G1 = "explicit.gc.unnecessary.cms.g1";

    /**
     * Property key for explicit gc with non concurrent collectors (Serial, ParallelSerial).
     */
    public static final String KEY_EXPLICIT_GC_UNNECESSARY = "explicit.gc.unnecessary";

    /**
     * Property key for explicit garbage collection by a serial collector.
     */
    public static final String KEY_EXPLICIT_GC_SERIAL = "explicit.gc.serial";

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
     * Property key for the ratio of gc time vs. stopped time showing a significant amount of stopped time (>20%) is not
     * GC related.
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
     * Property key for the Throughput collector invoking a serial collection.
     */
    public static final String KEY_SERIAL_GC_THROUGHPUT = "serial.gc.throughput";

    /**
     * Property key for the CMS collector invoking a serial collection.
     */
    public static final String KEY_SERIAL_GC_CMS = "serial.gc.cms";

    /**
     * Property key for the G1 collector invoking a serial collection.
     */
    public static final String KEY_SERIAL_GC_G1 = "serial.gc.g1";

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
     * Property key for disabling compiling bytecode in the background.
     */
    public static final String KEY_BYTECODE_BACKGROUND_COMPILe_DISABLED = "bytecode.compile.background.disabled";

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
     * Property key for adding option to output details at gc needed for anaylysis.
     */
    public static final String KEY_PRINT_GC_DETAILS_MISSING = "print.gc.details.missing";

    /**
     * Property key for not specifying the cms be used for old collections.
     */
    public static final String KEY_CMS_NEW_SERIAL_OLD = "cms.new.serial.old";

    /**
     * Property key for concurrent mode failure.
     */
    public static final String KEY_CMS_CONCURRENT_MODE_FAILURE = "cms.concurrent.mode.failure";

    /**
     * Property key for promotion failed.
     */
    public static final String KEY_CMS_PROMOTION_FAILED = "cms.promotion.failed";

    /**
     * Property key for concurrent mode failure.
     */
    public static final String KEY_CMS_CLASSUNLOADING_MISSING = "cms.classunloading.missing";

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
     * Make default constructor private so the class cannot be instantiated.
     */
    private Analysis() {

    }
}
