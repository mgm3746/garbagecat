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

/**
 * <p>
 * Regular expression constants for safepoint triggers.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSafepoint {

    /**
     * Defined triggers.
     */
    public enum Trigger {
        BULK_REVOKE_BIAS, CGC_OPERATION, CLASSLOADER_STATS_OPERATION, CLEAN_CLASSLOADER_DATA_METASPACES, CLEANUP,
        //
        CMS_FINAL_REMARK, CMS_INITIAL_MARK, COLLECT_FOR_METADATA_ALLOCATION, DEOPTIMIZE, ENABLE_BIASED_LOCKING, EXIT,
        //
        FIND_DEADLOCKS, FORCE_SAFEPOINT, G1_COLLECT_FOR_ALLOCATION, G1_COLLECT_FULL, G1_CONCURRENT,
        //
        G1_INC_COLLECTION_PAUSE, G1_PAUSE_CLEANUP, G1_PAUSE_REMARK, G1_TRY_INITIATE_CONC_MARK, GC_HEAP_INSPECTION,
        //
        GEN_COLLECT_FOR_ALLOCATION, GEN_COLLECT_FULL_CONCURRENT, GET_ALL_STACK_TRACES, GET_THREAD_LIST_STACK_TRACES,
        //
        HALT, HANDSHAKE_FALL_BACK, HEAP_DUMPER, IC_BUFFER_FULL, JFR_CHECKPOINT, JFR_OLD_OBJECT, MARK_ACTIVE_N_METHODS,
        //
        NO_VM_OPERATION, PARALLEL_GC_FAILED_ALLOCATION, PARALLEL_GC_SYSTEM_GC, PRINT_JNI, PRINT_THREADS,
        //
        REDEFINE_CLASSES, REPORT_JAVA_OUT_OF_MEMORY, REVOKE_BIAS, SET_NOTIFY_JVMTI_EVENTS_MODE,
        //
        SHENANDOAH_DEGENERATED_GC, SHENANDOAH_FINAL_MARK_START_EVAC, SHENANDOAH_FINAL_UPDATE_REFS,
        //
        SHENANDOAH_INIT_MARK, SHENANDOAH_INIT_UPDATE_REFS, THREAD_DUMP, UNKNOWN, X_MARK_END, X_MARK_START,
        //
        X_RELOCATE_START, Z_MARK_END, Z_MARK_END_OLD, Z_MARK_END_YOUNG, Z_MARK_START, Z_MARK_START_YOUNG,
        //
        Z_MARK_START_YOUNG_AND_OLD, Z_RELOCATE_START, Z_RELOCATE_START_OLD, Z_RELOCATE_START_YOUNG
    };

    /**
     * <p>
     * Bulk operation when the compiler has to recompile previously compiled code due to the compiled code no longer
     * being valid (e.g. a dynamic object has changed) or with tiered compilation when client compiled code is replaced
     * with server compiled code.
     * </p>
     */
    public static final String BULK_REVOKE_BIAS = "BulkRevokeBias";

    /**
     * <ul>
     * <li>{@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent}</li>
     * <li>{@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent}</li>
     * </ul>
     */
    public static final String CGC_OPERATION = "CGC_Operation";

    /**
     * <p>
     * Classloader metrics event.
     * </p>
     */
    public static final String CLASSLOADER_STATS_OPERATION = "ClassLoaderStatsOperation";

    /**
     * <p>
     * Work related to concurrent class unloading that has to be done in a safepoint.
     * </p>
     */
    public static final String CLEAN_CLASSLOADER_DATA_METASPACES = "CleanClassLoaderDataMetaspaces";

    /**
     * <p>
     * Various cleanup operations that require a safepoint: deflate monitors, update inline caches, compilation policy,
     * symbol table rehash, string table rehash, ClassLoaderData (CLD) purge, dictionary resize.
     * </p>
     */
    public static final String CLEANUP = "Cleanup";

    /**
     * <p>
     * CMS Final Remark.The second stop-the-world phase of the concurrent low pause collector. All live objects are
     * marked, starting with the objects identified in the CMS Initial Mark. This event does not do any garbage
     * collection. It rescans objects directly reachable from GC roots, processes weak references, and remarks objects.
     * </p>
     */
    public static final String CMS_FINAL_REMARK = "CMS_Final_Remark";

    /**
     * <p>
     * CMS Initial Mark. A phase of the concurrent low pause collector that identifies the initial set of live objects
     * directly reachable from GC roots. This event does not do any garbage collection, only marking of objects.
     * </p>
     */
    public static final String CMS_INITIAL_MARK = "CMS_Initial_Mark";

    /**
     * <p>
     * When the Metaspace is resized. The JVM has failed to allocate memory for something that should be stored in
     * Metaspace and does a full collection before attempting to resize the Metaspace.
     * </p>
     */
    public static final String COLLECT_FOR_METADATA_ALLOCATION = "CollectForMetadataAllocation";

    /**
     * <p>
     * When the compiler has to recompile previously compiled code due to the compiled code no longer being valid (e.g.
     * a dynamic object has changed) or with tiered compilation when client compiled code is replaced with server
     * compiled code.
     * </p>
     */
    public static final String DEOPTIMIZE = "Deoptimize";

    /**
     * <p>
     * Biased locking is an optimization to reduce the overhead of uncontested locking. It assumes a thread owns a
     * monitor until another thread tries to acquire it.
     * </p>
     * 
     * <p>
     * EnableBiasedLocking is the operation the JVM does on startup when BiasedLocking is enabled (default for JDK8 and
     * 11).
     * </p>
     * 
     * <p>
     * BiasedLocking is being disabled and deprecated in JDK 17, as it's typically not relevant to modern workloads:
     * https://bugs.openjdk.java.net/browse/JDK-8231265.
     * </p>
     */
    public static final String ENABLE_BIASED_LOCKING = "EnableBiasedLocking";

    /**
     * <p>
     * JVM exit.
     * </p>
     */
    public static final String EXIT = "Exit";

    /**
     * <p>
     * Find deadlocks event.
     * </p>
     */
    public static final String FIND_DEADLOCKS = "FindDeadlocks";

    /**
     * <p>
     * Force safepoint event.
     * </p>
     */
    public static final String FORCE_SAFEPOINT = "ForceSafepoint";

    /**
     * <ul>
     * <li>{@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1MixedPauseEvent}</li>
     * <li>{@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent}</li>
     * <li>{@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPrepareMixedEvent}</li>
     * </ul>
     */
    public static final String G1_COLLECT_FOR_ALLOCATION = "G1CollectForAllocation";

    /**
     * <p>
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1FullGcEvent}
     * </p>
     */
    public static final String G1_COLLECT_FULL = "G1CollectFull";

    /**
     * <p>
     * Small pauses to set up and tear down
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedConcurrentEvent}s.
     * </p>
     * 
     * <p>
     * Small pauses that set up and tear down concurrent phases.
     * </p>
     */
    public static final String G1_CONCURRENT = "G1Concurrent";

    /**
     * <p>
     * G1 incremental collection.
     * </p>
     */
    public static final String G1_INC_COLLECTION_PAUSE = "G1IncCollectionPause";

    /**
     * <p>
     * {@link #G1_CONCURRENT} was split into {@link #G1_PAUSE_REMARK} and {@link #G1_PAUSE_CLEANUP} in JDK17u8.
     * Reference: https://mail.openjdk.org/pipermail/hotspot-dev/2022-January/056810.html.
     * </p>
     *
     * <p>
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent}
     * </p>
     */
    public static final String G1_PAUSE_CLEANUP = "G1PauseCleanup";

    /**
     * <p>
     * {@link #G1_CONCURRENT} was split into {@link #G1_PAUSE_REMARK} and {@link #G1_PAUSE_CLEANUP} in JDK17u8.
     * Reference: https://mail.openjdk.org/pipermail/hotspot-dev/2022-January/056810.html.
     * </p>
     */
    public static final String G1_PAUSE_REMARK = "G1PauseRemark";

    /**
     * <p>
     * G1 concurrent mark.
     * </p>
     */
    public static final String G1_TRY_INITIATE_CONC_MARK = "G1TryInitiateConcMark";

    /**
     * <p>
     * Prints class histogram on SIGBREAK if PrintClassHistogram is specified and also the attach "inspectheap"
     * operation (e.g. jcmd &lt;pid&gt; GC.class_histogram).
     * </p>
     */
    public static final String GC_HEAP_INSPECTION = "GC_HeapInspection";

    /**
     * <p>
     * Generational collector allocation failure.
     * </p>
     */
    public static final String GEN_COLLECT_FOR_ALLOCATION = "GenCollectForAllocation";

    /**
     * <p>
     * Generational collector full concurrent collect? How can a concurrent collection require a safepoint?
     * </p>
     */
    public static final String GEN_COLLECT_FULL_CONCURRENT = "GenCollectFullConcurrent";

    /**
     * <p>
     * JVMTI method to get stack trace information in native code for all threads.
     * </p>
     */
    public static final String GET_ALL_STACK_TRACES = "GetAllStackTraces";

    /**
     * <p>
     * JVMTI methods to get stack trace information in native code for a list of threads.
     * </p>
     */
    public static final String GET_THREAD_LIST_STACK_TRACES = "GetThreadListStackTraces";

    /**
     * <p>
     * JVM halt (exit).
     * </p>
     */
    public static final String HALT = "Halt";

    /**
     * <p>
     * The safepoint for executing an alternate code path when a handshake fails due to a platform not support
     * handshakes.
     * </p>
     */
    public static final String HANDSHAKE_FALL_BACK = "HandshakeFallback";

    /**
     * <p>
     * Full heap dump (a heap summary does not require a safepoint).
     * </p>
     */
    public static final String HEAP_DUMPER = "HeapDumper";

    /**
     * <p>
     * Safepoint for managing inline cache buffer when it is full (clear? resize?).
     * </p>
     */
    public static final String IC_BUFFER_FULL = "ICBufferFull";

    /**
     * <p>
     * JFR event to write or clear the queue. Will be removed and split into JFRSafepointClear and JFRSafepointWrite in
     * JDK24. Reference: <a href="https://bugs.openjdk.org/browse/JDK-8338314">JDK-8338314</a>.
     * </p>
     */
    public static final String JFR_CHECKPOINT = "JFRCheckpoint";

    /**
     * <p>
     * JFR jdk.OldObjectSample event for additional tracking of heap objects (e.g. path to gc roots). Reference:
     * <a href="https://github.com/oracle/graal/issues/5145">Add JFR jdk.OldObjectSample event</a>.
     * </p>
     */
    public static final String JFR_OLD_OBJECT = "JFROldObject";

    /**
     * <p>
     * Stack scanning of active methods required by the code cache sweeper thread, which tries to free space when the
     * code cache fills. This safepoint is an indication of code cache memory pressure.
     * </p>
     */
    public static final String MARK_ACTIVE_N_METHODS = "MarkActiveNMethods";

    /**
     * <p>
     * Guaranteed safepoint to process non-urgent JVM operations. The interval is enabled by
     * <code>-XX:+UnlockDiagnosticVMOptions</code> and controlled by <code>-XX:GuaranteedSafepointInterval=N</code>
     * (default 300000 seconds = 5 minutes).
     * </p>
     */
    public static final String NO_VM_OPERATION = "no vm operation";

    /**
     * <p>
     * Parallel collection.
     * </p>
     */
    public static final String PARALLEL_GC_FAILED_ALLOCATION = "ParallelGCFailedAllocation";

    /**
     * <p>
     * Parallel collection initiated by explicit gc.
     * </p>
     */
    public static final String PARALLEL_GC_SYSTEM_GC = "ParallelGCSystemGC";

    /**
     * <p>
     * Print JNI information.
     * </p>
     */
    public static final String PRINT_JNI = "PrintJNI";

    /**
     * <p>
     * Printing a stack trace.
     * </p>
     */
    public static final String PRINT_THREADS = "PrintThreads";

    /**
     * <p>
     * Redefine classes.
     * </p>
     */
    public static final String REDEFINE_CLASSES = "RedefineClasses";

    /**
     * <p>
     * OutOfMemoryError.
     * </p>
     */
    public static final String REPORT_JAVA_OUT_OF_MEMORY = "ReportJavaOutOfMemory";

    /**
     * <p>
     * Biased locking is an optimization to reduce the overhead of uncontested locking. It assumes a thread owns a
     * monitor until another thread tries to acquire it.
     * </p>
     * 
     * <p>
     * RevokeBias is the operation the JVM does to undo the optimization when a different thread tries to acquire the
     * monitor.
     * </p>
     * 
     * <p>
     * BiasedLocking is being disabled and deprecated in JDK 17, as it's typically not relevant to modern workloads:
     * https://bugs.openjdk.java.net/browse/JDK-8231265.
     * </p>
     */
    public static final String REVOKE_BIAS = "RevokeBias";

    /**
     * <p>
     * Safepoint to support the JVM tool interface (JVMTI).
     * </p>
     */
    public static final String SET_NOTIFY_JVMTI_EVENTS_MODE = "SetNotifyJvmtiEventsMode";

    /**
     * <p>
     * Shenandoah degenerated gc.
     * </p>
     */
    public static final String SHENANDOAH_DEGENERATED_GC = "ShenandoahDegeneratedGC";

    /**
     * <p>
     * Shenandoah final mark.
     * </p>
     */
    public static final String SHENANDOAH_FINAL_MARK_START_EVAC = "ShenandoahFinalMarkStartEvac";

    /**
     * <p>
     * Shenandoah final update.
     * </p>
     */
    public static final String SHENANDOAH_FINAL_UPDATE_REFS = "ShenandoahFinalUpdateRefs";

    /**
     * <p>
     * Shenandoah initial mark.
     * </p>
     */
    public static final String SHENANDOAH_INIT_MARK = "ShenandoahInitMark";

    /**
     * <p>
     * Shenandoah initial update.
     * </p>
     */
    public static final String SHENANDOAH_INIT_UPDATE_REFS = "ShenandoahInitUpdateRefs";

    /**
     * <p>
     * Generating a thread dump.
     * </p>
     */
    public static final String THREAD_DUMP = "ThreadDump";

    /**
     * <p>
     * Second phase of non-generational (JDK21+) Z garbage collector. Do reference processing, weak root cleaning, and
     * mark regions to compact.
     * </p>
     */
    public static final String X_MARK_END = "XMarkEnd";

    /**
     * <p>
     * First phase of non-generational (JDK21+) Z garbage collector. Mark objects pointed to by roots.
     * </p>
     */
    public static final String X_MARK_START = "XMarkStart";

    /**
     * <p>
     * Third phase of non-generational (JDK21+) Z garbage collector. Region compaction.
     * </p>
     */
    public static final String X_RELOCATE_START = "XRelocateStart";

    /**
     * <p>
     * The second phase of the legacy (&lt;JDK21) Z garbage collector. Do reference processing, weak root cleaning, and
     * mark regions to compact.
     * </p>
     */
    public static final String Z_MARK_END = "ZMarkEnd";

    /**
     * <p>
     * The second phase of the generational (JDK21+) Z garbage collector. Do reference processing, weak root cleaning,
     * and mark regions to compact in the old generation.
     * </p>
     */
    public static final String Z_MARK_END_OLD = "ZMarkEndOld";

    /**
     * <p>
     * The second phase of the generational (JDK21+) Z garbage collector. Do reference processing, weak root cleaning,
     * and mark regions to compact in the young generation.
     * </p>
     */
    public static final String Z_MARK_END_YOUNG = "ZMarkEndYoung";

    /**
     * <p>
     * The first phase of the legacy (&lt;JDK21) Z garbage collector. Mark objects pointed to by roots.
     * </p>
     */
    public static final String Z_MARK_START = "ZMarkStart";

    /**
     * <p>
     * The first phase of the generational (JDK21+) Z garbage collector. Mark objects pointed to by roots in the young
     * generation.
     * </p>
     */
    public static final String Z_MARK_START_YOUNG = "ZMarkStartYoung";

    /**
     * <p>
     * The first phase of the generational (JDK21+) Z garbage collector. Mark objects pointed to by roots in the young
     * and tenured generations.
     * </p>
     */
    public static final String Z_MARK_START_YOUNG_AND_OLD = "ZMarkStartYoungAndOld";

    /**
     * <p>
     * The third phase of the legacy (&lt;JDK21) Z garbage collector. Region compaction.
     * </p>
     */
    public static final String Z_RELOCATE_START = "ZRelocateStart";

    /**
     * <p>
     * The third phase of the generational (JDK21+) Z garbage collector. Region compaction in the young tenured.
     * </p>
     */
    public static final String Z_RELOCATE_START_OLD = "ZRelocateStartOld";

    /**
     * <p>
     * The third phase of the generational (JDK21+) Z garbage collector. Region compaction in the young generation.
     * </p>
     */
    public static final String Z_RELOCATE_START_YOUNG = "ZRelocateStartYoung";

    /**
     * Get <code>Trigger</code> from log literal.
     * 
     * @param triggerLiteral
     *            The trigger literal.
     * @return The <code>Trigger</code>.
     */
    public static final Trigger getTrigger(String triggerLiteral) {
        if (BULK_REVOKE_BIAS.matches(triggerLiteral))
            return Trigger.BULK_REVOKE_BIAS;
        if (CGC_OPERATION.matches(triggerLiteral))
            return Trigger.CGC_OPERATION;
        if (CLASSLOADER_STATS_OPERATION.matches(triggerLiteral))
            return Trigger.CLASSLOADER_STATS_OPERATION;
        if (CLEANUP.matches(triggerLiteral))
            return Trigger.CLEANUP;
        if (CLEAN_CLASSLOADER_DATA_METASPACES.matches(triggerLiteral))
            return Trigger.CLEAN_CLASSLOADER_DATA_METASPACES;
        if (COLLECT_FOR_METADATA_ALLOCATION.matches(triggerLiteral))
            return Trigger.COLLECT_FOR_METADATA_ALLOCATION;
        if (CMS_FINAL_REMARK.matches(triggerLiteral))
            return Trigger.CMS_FINAL_REMARK;
        if (CMS_INITIAL_MARK.matches(triggerLiteral))
            return Trigger.CMS_INITIAL_MARK;
        if (DEOPTIMIZE.matches(triggerLiteral))
            return Trigger.DEOPTIMIZE;
        if (ENABLE_BIASED_LOCKING.matches(triggerLiteral))
            return Trigger.ENABLE_BIASED_LOCKING;
        if (EXIT.matches(triggerLiteral))
            return Trigger.EXIT;
        if (FIND_DEADLOCKS.matches(triggerLiteral))
            return Trigger.FIND_DEADLOCKS;
        if (FORCE_SAFEPOINT.matches(triggerLiteral))
            return Trigger.FORCE_SAFEPOINT;
        if (GC_HEAP_INSPECTION.matches(triggerLiteral))
            return Trigger.GC_HEAP_INSPECTION;
        if (G1_COLLECT_FOR_ALLOCATION.matches(triggerLiteral))
            return Trigger.G1_COLLECT_FOR_ALLOCATION;
        if (G1_COLLECT_FULL.matches(triggerLiteral))
            return Trigger.G1_COLLECT_FULL;
        if (G1_CONCURRENT.matches(triggerLiteral))
            return Trigger.G1_CONCURRENT;
        if (G1_INC_COLLECTION_PAUSE.matches(triggerLiteral))
            return Trigger.G1_INC_COLLECTION_PAUSE;
        if (G1_PAUSE_CLEANUP.matches(triggerLiteral))
            return Trigger.G1_PAUSE_CLEANUP;
        if (G1_PAUSE_REMARK.matches(triggerLiteral))
            return Trigger.G1_PAUSE_REMARK;
        if (G1_TRY_INITIATE_CONC_MARK.matches(triggerLiteral))
            return Trigger.G1_TRY_INITIATE_CONC_MARK;
        if (GEN_COLLECT_FOR_ALLOCATION.matches(triggerLiteral))
            return Trigger.GEN_COLLECT_FOR_ALLOCATION;
        if (GEN_COLLECT_FULL_CONCURRENT.matches(triggerLiteral))
            return Trigger.GEN_COLLECT_FULL_CONCURRENT;
        if (GET_ALL_STACK_TRACES.matches(triggerLiteral))
            return Trigger.GET_ALL_STACK_TRACES;
        if (GET_THREAD_LIST_STACK_TRACES.matches(triggerLiteral))
            return Trigger.GET_THREAD_LIST_STACK_TRACES;
        if (HALT.matches(triggerLiteral))
            return Trigger.HALT;
        if (HANDSHAKE_FALL_BACK.matches(triggerLiteral))
            return Trigger.HANDSHAKE_FALL_BACK;
        if (HEAP_DUMPER.matches(triggerLiteral))
            return Trigger.HEAP_DUMPER;
        if (IC_BUFFER_FULL.matches(triggerLiteral))
            return Trigger.IC_BUFFER_FULL;
        if (JFR_CHECKPOINT.matches(triggerLiteral))
            return Trigger.JFR_CHECKPOINT;
        if (JFR_OLD_OBJECT.matches(triggerLiteral))
            return Trigger.JFR_OLD_OBJECT;
        if (MARK_ACTIVE_N_METHODS.matches(triggerLiteral))
            return Trigger.MARK_ACTIVE_N_METHODS;
        if (NO_VM_OPERATION.matches(triggerLiteral))
            return Trigger.NO_VM_OPERATION;
        if (PARALLEL_GC_FAILED_ALLOCATION.matches(triggerLiteral))
            return Trigger.PARALLEL_GC_FAILED_ALLOCATION;
        if (PARALLEL_GC_SYSTEM_GC.matches(triggerLiteral))
            return Trigger.PARALLEL_GC_SYSTEM_GC;
        if (PRINT_JNI.matches(triggerLiteral))
            return Trigger.PRINT_JNI;
        if (PRINT_THREADS.matches(triggerLiteral))
            return Trigger.PRINT_THREADS;
        if (REDEFINE_CLASSES.matches(triggerLiteral))
            return Trigger.REDEFINE_CLASSES;
        if (REPORT_JAVA_OUT_OF_MEMORY.matches(triggerLiteral))
            return Trigger.REPORT_JAVA_OUT_OF_MEMORY;
        if (REVOKE_BIAS.matches(triggerLiteral))
            return Trigger.REVOKE_BIAS;
        if (SET_NOTIFY_JVMTI_EVENTS_MODE.matches(triggerLiteral))
            return Trigger.SET_NOTIFY_JVMTI_EVENTS_MODE;
        if (SHENANDOAH_DEGENERATED_GC.matches(triggerLiteral))
            return Trigger.SHENANDOAH_DEGENERATED_GC;
        if (SHENANDOAH_FINAL_MARK_START_EVAC.matches(triggerLiteral))
            return Trigger.SHENANDOAH_FINAL_MARK_START_EVAC;
        if (SHENANDOAH_FINAL_UPDATE_REFS.matches(triggerLiteral))
            return Trigger.SHENANDOAH_FINAL_UPDATE_REFS;
        if (SHENANDOAH_INIT_MARK.matches(triggerLiteral))
            return Trigger.SHENANDOAH_INIT_MARK;
        if (SHENANDOAH_INIT_UPDATE_REFS.matches(triggerLiteral))
            return Trigger.SHENANDOAH_INIT_UPDATE_REFS;
        if (THREAD_DUMP.matches(triggerLiteral))
            return Trigger.THREAD_DUMP;
        if (X_MARK_END.matches(triggerLiteral))
            return Trigger.X_MARK_END;
        if (X_MARK_START.matches(triggerLiteral))
            return Trigger.X_MARK_START;
        if (X_RELOCATE_START.matches(triggerLiteral))
            return Trigger.X_RELOCATE_START;
        if (Z_MARK_END.matches(triggerLiteral))
            return Trigger.Z_MARK_END;
        if (Z_MARK_END_OLD.matches(triggerLiteral))
            return Trigger.Z_MARK_END_OLD;
        if (Z_MARK_END_YOUNG.matches(triggerLiteral))
            return Trigger.Z_MARK_END_YOUNG;
        if (Z_MARK_START.matches(triggerLiteral))
            return Trigger.Z_MARK_START;
        if (Z_MARK_START_YOUNG.matches(triggerLiteral))
            return Trigger.Z_MARK_START_YOUNG;
        if (Z_MARK_START_YOUNG_AND_OLD.matches(triggerLiteral))
            return Trigger.Z_MARK_START_YOUNG_AND_OLD;
        if (Z_RELOCATE_START.matches(triggerLiteral))
            return Trigger.Z_RELOCATE_START;
        if (Z_RELOCATE_START_OLD.matches(triggerLiteral))
            return Trigger.Z_RELOCATE_START_OLD;
        if (Z_RELOCATE_START_YOUNG.matches(triggerLiteral))
            return Trigger.Z_RELOCATE_START_YOUNG;

        return Trigger.UNKNOWN;
    }

    /**
     * Get <code>Trigger</code> log literal.
     * 
     * @param trigger
     *            The trigger.
     * @return The trigger literal in the log line.
     */
    public static final String getTriggerLiteral(Trigger trigger) {
        String triggerLiteral = null;

        switch (trigger) {

        case BULK_REVOKE_BIAS:
            triggerLiteral = BULK_REVOKE_BIAS;
            break;
        case CLASSLOADER_STATS_OPERATION:
            triggerLiteral = CLASSLOADER_STATS_OPERATION;
            break;
        case CLEANUP:
            triggerLiteral = CLEANUP;
            break;
        case CLEAN_CLASSLOADER_DATA_METASPACES:
            triggerLiteral = CLEAN_CLASSLOADER_DATA_METASPACES;
            break;
        case CGC_OPERATION:
            triggerLiteral = CGC_OPERATION;
            break;
        case CMS_FINAL_REMARK:
            triggerLiteral = CMS_FINAL_REMARK;
            break;
        case CMS_INITIAL_MARK:
            triggerLiteral = CMS_INITIAL_MARK;
            break;
        case COLLECT_FOR_METADATA_ALLOCATION:
            triggerLiteral = COLLECT_FOR_METADATA_ALLOCATION;
            break;
        case DEOPTIMIZE:
            triggerLiteral = DEOPTIMIZE;
            break;
        case ENABLE_BIASED_LOCKING:
            triggerLiteral = ENABLE_BIASED_LOCKING;
            break;
        case EXIT:
            triggerLiteral = EXIT;
            break;
        case FIND_DEADLOCKS:
            triggerLiteral = FIND_DEADLOCKS;
            break;
        case FORCE_SAFEPOINT:
            triggerLiteral = FORCE_SAFEPOINT;
            break;
        case GC_HEAP_INSPECTION:
            triggerLiteral = GC_HEAP_INSPECTION;
            break;
        case G1_COLLECT_FOR_ALLOCATION:
            triggerLiteral = G1_COLLECT_FOR_ALLOCATION;
            break;
        case G1_COLLECT_FULL:
            triggerLiteral = G1_COLLECT_FULL;
            break;
        case G1_CONCURRENT:
            triggerLiteral = G1_CONCURRENT;
            break;
        case G1_INC_COLLECTION_PAUSE:
            triggerLiteral = G1_INC_COLLECTION_PAUSE;
            break;
        case G1_PAUSE_CLEANUP:
            triggerLiteral = G1_PAUSE_CLEANUP;
            break;
        case G1_PAUSE_REMARK:
            triggerLiteral = G1_PAUSE_REMARK;
            break;
        case G1_TRY_INITIATE_CONC_MARK:
            triggerLiteral = G1_TRY_INITIATE_CONC_MARK;
            break;
        case GEN_COLLECT_FOR_ALLOCATION:
            triggerLiteral = GEN_COLLECT_FOR_ALLOCATION;
            break;
        case GEN_COLLECT_FULL_CONCURRENT:
            triggerLiteral = GEN_COLLECT_FULL_CONCURRENT;
            break;
        case GET_ALL_STACK_TRACES:
            triggerLiteral = GET_ALL_STACK_TRACES;
            break;
        case GET_THREAD_LIST_STACK_TRACES:
            triggerLiteral = GET_THREAD_LIST_STACK_TRACES;
            break;
        case HALT:
            triggerLiteral = HALT;
            break;
        case HANDSHAKE_FALL_BACK:
            triggerLiteral = HANDSHAKE_FALL_BACK;
            break;
        case HEAP_DUMPER:
            triggerLiteral = HEAP_DUMPER;
            break;
        case IC_BUFFER_FULL:
            triggerLiteral = IC_BUFFER_FULL;
            break;
        case JFR_CHECKPOINT:
            triggerLiteral = JFR_CHECKPOINT;
            break;
        case JFR_OLD_OBJECT:
            triggerLiteral = JFR_OLD_OBJECT;
            break;
        case MARK_ACTIVE_N_METHODS:
            triggerLiteral = MARK_ACTIVE_N_METHODS;
            break;
        case NO_VM_OPERATION:
            triggerLiteral = NO_VM_OPERATION;
            break;
        case PARALLEL_GC_FAILED_ALLOCATION:
            triggerLiteral = PARALLEL_GC_FAILED_ALLOCATION;
            break;
        case PARALLEL_GC_SYSTEM_GC:
            triggerLiteral = PARALLEL_GC_SYSTEM_GC;
            break;
        case PRINT_JNI:
            triggerLiteral = PRINT_JNI;
            break;
        case PRINT_THREADS:
            triggerLiteral = PRINT_THREADS;
            break;
        case REDEFINE_CLASSES:
            triggerLiteral = REDEFINE_CLASSES;
            break;
        case REPORT_JAVA_OUT_OF_MEMORY:
            triggerLiteral = REPORT_JAVA_OUT_OF_MEMORY;
            break;
        case REVOKE_BIAS:
            triggerLiteral = REVOKE_BIAS;
            break;
        case SET_NOTIFY_JVMTI_EVENTS_MODE:
            triggerLiteral = SET_NOTIFY_JVMTI_EVENTS_MODE;
            break;
        case SHENANDOAH_DEGENERATED_GC:
            triggerLiteral = SHENANDOAH_DEGENERATED_GC;
            break;
        case SHENANDOAH_FINAL_MARK_START_EVAC:
            triggerLiteral = SHENANDOAH_FINAL_MARK_START_EVAC;
            break;
        case SHENANDOAH_FINAL_UPDATE_REFS:
            triggerLiteral = SHENANDOAH_FINAL_UPDATE_REFS;
            break;
        case SHENANDOAH_INIT_MARK:
            triggerLiteral = SHENANDOAH_INIT_MARK;
            break;
        case SHENANDOAH_INIT_UPDATE_REFS:
            triggerLiteral = SHENANDOAH_INIT_UPDATE_REFS;
            break;
        case THREAD_DUMP:
            triggerLiteral = THREAD_DUMP;
            break;
        case X_MARK_END:
            triggerLiteral = X_MARK_END;
            break;
        case X_MARK_START:
            triggerLiteral = X_MARK_START;
            break;
        case X_RELOCATE_START:
            triggerLiteral = X_RELOCATE_START;
            break;
        case Z_MARK_END:
            triggerLiteral = Z_MARK_END;
            break;
        case Z_MARK_END_OLD:
            triggerLiteral = Z_MARK_END_OLD;
            break;
        case Z_MARK_END_YOUNG:
            triggerLiteral = Z_MARK_END_YOUNG;
            break;
        case Z_MARK_START:
            triggerLiteral = Z_MARK_START;
            break;
        case Z_MARK_START_YOUNG:
            triggerLiteral = Z_MARK_START_YOUNG;
            break;
        case Z_MARK_START_YOUNG_AND_OLD:
            triggerLiteral = Z_MARK_START_YOUNG_AND_OLD;
            break;
        case Z_RELOCATE_START:
            triggerLiteral = Z_RELOCATE_START;
            break;
        case Z_RELOCATE_START_OLD:
            triggerLiteral = Z_RELOCATE_START_OLD;
            break;
        case Z_RELOCATE_START_YOUNG:
            triggerLiteral = Z_RELOCATE_START_YOUNG;
            break;

        default:
            throw new AssertionError("Unexpected trigger value: " + trigger);
        }
        return triggerLiteral;
    }

    /**
     * Identify the safepoint trigger.
     * 
     * @param trigger
     *            The Trigger String stored in the database.
     * @return The <code>Trigger</code>.
     */
    public static final Trigger identifyTrigger(String trigger) {
        if (Trigger.BULK_REVOKE_BIAS.name().matches(trigger))
            return Trigger.BULK_REVOKE_BIAS;
        if (Trigger.CGC_OPERATION.name().matches(trigger))
            return Trigger.CGC_OPERATION;
        if (Trigger.CLASSLOADER_STATS_OPERATION.name().matches(trigger))
            return Trigger.CLASSLOADER_STATS_OPERATION;
        if (Trigger.CLEANUP.name().matches(trigger))
            return Trigger.CLEANUP;
        if (Trigger.CLEAN_CLASSLOADER_DATA_METASPACES.name().matches(trigger))
            return Trigger.CLEAN_CLASSLOADER_DATA_METASPACES;
        if (Trigger.COLLECT_FOR_METADATA_ALLOCATION.name().matches(trigger))
            return Trigger.COLLECT_FOR_METADATA_ALLOCATION;
        if (Trigger.CMS_FINAL_REMARK.name().matches(trigger))
            return Trigger.CMS_FINAL_REMARK;
        if (Trigger.CMS_INITIAL_MARK.name().matches(trigger))
            return Trigger.CMS_INITIAL_MARK;
        if (Trigger.DEOPTIMIZE.name().matches(trigger))
            return Trigger.DEOPTIMIZE;
        if (Trigger.ENABLE_BIASED_LOCKING.name().matches(trigger))
            return Trigger.ENABLE_BIASED_LOCKING;
        if (Trigger.EXIT.name().matches(trigger))
            return Trigger.EXIT;
        if (Trigger.FIND_DEADLOCKS.name().matches(trigger))
            return Trigger.FIND_DEADLOCKS;
        if (Trigger.FORCE_SAFEPOINT.name().matches(trigger))
            return Trigger.FORCE_SAFEPOINT;
        if (Trigger.GC_HEAP_INSPECTION.name().matches(trigger))
            return Trigger.GC_HEAP_INSPECTION;
        if (Trigger.G1_COLLECT_FOR_ALLOCATION.name().matches(trigger))
            return Trigger.G1_COLLECT_FOR_ALLOCATION;
        if (Trigger.G1_COLLECT_FULL.name().matches(trigger))
            return Trigger.G1_COLLECT_FULL;
        if (Trigger.G1_CONCURRENT.name().matches(trigger))
            return Trigger.G1_CONCURRENT;
        if (Trigger.G1_INC_COLLECTION_PAUSE.name().matches(trigger))
            return Trigger.G1_INC_COLLECTION_PAUSE;
        if (Trigger.G1_PAUSE_CLEANUP.name().matches(trigger))
            return Trigger.G1_PAUSE_CLEANUP;
        if (Trigger.G1_PAUSE_REMARK.name().matches(trigger))
            return Trigger.G1_PAUSE_REMARK;
        if (Trigger.G1_TRY_INITIATE_CONC_MARK.name().matches(trigger))
            return Trigger.G1_TRY_INITIATE_CONC_MARK;
        if (Trigger.GEN_COLLECT_FOR_ALLOCATION.name().matches(trigger))
            return Trigger.GEN_COLLECT_FOR_ALLOCATION;
        if (Trigger.GEN_COLLECT_FULL_CONCURRENT.name().matches(trigger))
            return Trigger.GEN_COLLECT_FULL_CONCURRENT;
        if (Trigger.GET_ALL_STACK_TRACES.name().matches(trigger))
            return Trigger.GET_ALL_STACK_TRACES;
        if (Trigger.GET_THREAD_LIST_STACK_TRACES.name().matches(trigger))
            return Trigger.GET_THREAD_LIST_STACK_TRACES;
        if (Trigger.HALT.name().matches(trigger))
            return Trigger.HALT;
        if (Trigger.HANDSHAKE_FALL_BACK.name().matches(trigger))
            return Trigger.HANDSHAKE_FALL_BACK;
        if (Trigger.HEAP_DUMPER.name().matches(trigger))
            return Trigger.HEAP_DUMPER;
        if (Trigger.IC_BUFFER_FULL.name().matches(trigger))
            return Trigger.IC_BUFFER_FULL;
        if (Trigger.JFR_CHECKPOINT.name().matches(trigger))
            return Trigger.JFR_CHECKPOINT;
        if (Trigger.JFR_OLD_OBJECT.name().matches(trigger))
            return Trigger.JFR_OLD_OBJECT;
        if (Trigger.MARK_ACTIVE_N_METHODS.name().matches(trigger))
            return Trigger.MARK_ACTIVE_N_METHODS;
        if (Trigger.NO_VM_OPERATION.name().matches(trigger))
            return Trigger.NO_VM_OPERATION;
        if (Trigger.PARALLEL_GC_FAILED_ALLOCATION.name().matches(trigger))
            return Trigger.PARALLEL_GC_FAILED_ALLOCATION;
        if (Trigger.PARALLEL_GC_SYSTEM_GC.name().matches(trigger))
            return Trigger.PARALLEL_GC_SYSTEM_GC;
        if (Trigger.PRINT_JNI.name().matches(trigger))
            return Trigger.PRINT_JNI;
        if (Trigger.PRINT_THREADS.name().matches(trigger))
            return Trigger.PRINT_THREADS;
        if (Trigger.REDEFINE_CLASSES.name().matches(trigger))
            return Trigger.REDEFINE_CLASSES;
        if (Trigger.REPORT_JAVA_OUT_OF_MEMORY.name().matches(trigger))
            return Trigger.REPORT_JAVA_OUT_OF_MEMORY;
        if (Trigger.REVOKE_BIAS.name().matches(trigger))
            return Trigger.REVOKE_BIAS;
        if (Trigger.SET_NOTIFY_JVMTI_EVENTS_MODE.name().matches(trigger))
            return Trigger.SET_NOTIFY_JVMTI_EVENTS_MODE;
        if (Trigger.SHENANDOAH_DEGENERATED_GC.name().matches(trigger))
            return Trigger.SHENANDOAH_DEGENERATED_GC;
        if (Trigger.SHENANDOAH_FINAL_MARK_START_EVAC.name().matches(trigger))
            return Trigger.SHENANDOAH_FINAL_MARK_START_EVAC;
        if (Trigger.SHENANDOAH_FINAL_UPDATE_REFS.name().matches(trigger))
            return Trigger.SHENANDOAH_FINAL_UPDATE_REFS;
        if (Trigger.SHENANDOAH_INIT_MARK.name().matches(trigger))
            return Trigger.SHENANDOAH_INIT_MARK;
        if (Trigger.SHENANDOAH_INIT_UPDATE_REFS.name().matches(trigger))
            return Trigger.SHENANDOAH_INIT_UPDATE_REFS;
        if (Trigger.THREAD_DUMP.name().matches(trigger))
            return Trigger.THREAD_DUMP;
        if (Trigger.X_MARK_END.name().matches(trigger))
            return Trigger.X_MARK_END;
        if (Trigger.X_MARK_START.name().matches(trigger))
            return Trigger.X_MARK_START;
        if (Trigger.X_RELOCATE_START.name().matches(trigger))
            return Trigger.X_RELOCATE_START;
        if (Trigger.Z_MARK_END.name().matches(trigger))
            return Trigger.Z_MARK_END;
        if (Trigger.Z_MARK_END_OLD.name().matches(trigger))
            return Trigger.Z_MARK_END_OLD;
        if (Trigger.Z_MARK_END_YOUNG.name().matches(trigger))
            return Trigger.Z_MARK_END_YOUNG;
        if (Trigger.Z_MARK_START.name().matches(trigger))
            return Trigger.Z_MARK_START;
        if (Trigger.Z_MARK_START_YOUNG.name().matches(trigger))
            return Trigger.Z_MARK_START_YOUNG;
        if (Trigger.Z_MARK_START_YOUNG_AND_OLD.name().matches(trigger))
            return Trigger.Z_MARK_START_YOUNG_AND_OLD;
        if (Trigger.Z_RELOCATE_START.name().matches(trigger))
            return Trigger.Z_RELOCATE_START;
        if (Trigger.Z_RELOCATE_START_OLD.name().matches(trigger))
            return Trigger.Z_RELOCATE_START_OLD;
        if (Trigger.Z_RELOCATE_START_YOUNG.name().matches(trigger))
            return Trigger.Z_RELOCATE_START_YOUNG;

        // no idea what trigger is
        return Trigger.UNKNOWN;
    }

    /**
     * Convenience method for concatenating triggers into a regular expression. For example:
     * 
     * "(trigger1|trigger2|trigger3)"
     *
     * @return The <code>Trigger</code> regular expression.
     */
    public static final String triggerRegEx() {
        StringBuilder regex = new StringBuilder();
        regex.append("(");
        UnifiedSafepoint.Trigger[] triggers = UnifiedSafepoint.Trigger.values();
        boolean firstTrigger = true;
        for (int i = 0; i < triggers.length; i++) {
            if (triggers[i] != Trigger.EXIT && triggers[i] != Trigger.HALT && triggers[i] != Trigger.UNKNOWN) {
                if (!firstTrigger) {
                    regex.append("|");
                } else {
                    firstTrigger = false;
                }
                regex.append(getTriggerLiteral(triggers[i]));
            }
        }
        regex.append(")");
        return regex.toString();
    }
}
