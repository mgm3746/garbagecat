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

/**
 * Garbage collection triggers for OpenJDK and Oracle JDK.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public enum GcTrigger {
    //
    ALLOCATION_FAILURE("Allocation Failure"),
    // Ctrl-Break with -XX:+PrintClassHistogram trigger causes a full GC.
    CLASS_HISTOGRAM("Class Histogram"),
    //
    CMS_FINAL_REMARK("CMS Final Remark"),
    //
    CMS_INITIAL_MARK("CMS Initial Mark"),
    /**
     * <p>
     * CMS concurrent mode failure trigger.
     * </p>
     * 
     * <p>
     * The concurrent collection of the old generation did not finish before the old generation became full. There is
     * not enough space in the old generation to support the rate of promotion from the young generation. The JVM
     * initiates a full GC using a slow (single threaded) serial collector in an attempt to free space. The concurrent
     * low pause collector measures the rate at which the the old generation is filling and the amount of time between
     * collections and uses this historical data to calculate when to start the concurrent collection (plus adds some
     * padding) so that it will finish just in time before the old generation becomes full.
     * </p>
     * 
     * <p>
     * Possible causes:
     * </p>
     * 
     * <ol>
     * <li>The heap is too small.</li>
     * <li>There is a change in application behavior (e.g. a load increase) that causes the young promotion rate to
     * exceed historical data. If this is the case, the concurrent mode failures will happen near the change in
     * behavior, then after a few collections the CMS collector will adjust based on the new promotion rate. Performance
     * will suffer for a short period until the CMS collector recalibrates. Use -XX:CMSInitiatingOccupancyFraction=NN
     * (default 92) to handle changes in application behavior; however, the tradeoff is that there will be more
     * collections.</li>
     * <li>The application has large variances in object allocation rates, causing large variances in young generation
     * promotion rates, leading to the CMS collector not being able to accurately predict the time between collections.
     * Use -XX:CMSIncrementalSafetyFactor=NN (default 10) to start the concurrent collection NN% sooner than the
     * calculated time.</li>
     * <li>There is premature promotion from the young to the old generation, causing the old generation to fill up with
     * short-lived objects. The default value for -XX:MaxTenuringThreshold for the CMS collector is 0, meaning that
     * objects surviving a young collection are immediately promoted to the old generation. Use
     * -XX:MaxTenuringThreshold=32 to allow more time for objects to expire in the young generation.</li>
     * <li>If the old generation has available space, the cause is likely fragmentation. Fragmentation can be avoided by
     * increasing the heap size.</li>
     * <li>The Perm/Metaspace fills up during the CMS cycle. The CMS collector does not collect Perm/Metaspace by
     * default. Add -XX:+CMSClassUnloadingEnabled to collect Perm/Metaspace in the CMS concurrent cycle. If the
     * concurrent mode failure is not able to reclaim Perm/Metaspace, also increase the size. For example:
     * -XX:PermSize=256M -XX:MaxPermSize=256M (Perm) or -XX:MetaspaceSize=2G -XX:MaxMetaspaceSize=2G (Metaspace).</li>
     * </ol>
     */
    CONCURRENT_MODE_FAILURE("concurrent mode failure"),
    /**
     * <p>
     * CMS concurrent mode interrupted trigger.
     * </p>
     * 
     * <p>
     * Caused by the following user or serviceability requested gc.
     * </p>
     * 
     * <p>
     * User requested gc:
     * </p>
     * <ol>
     * <li>System.gc()</li>
     * <li>JVMTI ForceGarbageCollection</li>
     * </ol>
     * 
     * <p>
     * Serviceability requested gc:
     * </p>
     * <ol>
     * <li>JVMTI ForceGarbageCollection</li>
     * <li>Heap Inspection</li>
     * <li>Heap Dump</li>
     * </ol>
     */
    CONCURRENT_MODE_INTERRUPTED("concurrent mode interrupted"),
    /**
     * <p>
     * Explicit garbage collection trigger (e.g. Distributed Garbage Collection (DGC), jcmd &lt;pid&gt; GC.run).
     * </p>
     */
    DIAGNOSTIC_COMMAND("Diagnostic Command"),
    /**
     * <p>
     * Ergonomics trigger. GC happens for a heuristics reason. A heuristic is a rule of thumb or pattern the JVM uses to
     * achieve a performance goal or improve performance.
     * </p>
     * 
     * <p>
     * For example:
     * </p>
     * 
     * <ol>
     * <li>There is not enough old space to handle a young collection, based on promotion statistics. A full collection
     * is done in an attempt to free space.</li>
     * <li>The young and/or old spaces need to be resized in an effort to meet a maximum pause time or throughput goal.
     * A full collection is needed to do the resizing.
     * </ol>
     */
    ERGONOMICS("Ergonomics"),
    /**
     * G1 Compaction Pause trigger. A full gc due to too high heap occupancy in the old generation at the start of a
     * concurrent cycle during the concurrent start phase (when liveness information is determined).
     */
    G1_COMPACTION_PAUSE("G1 Compaction Pause"),
    /**
     * G1 Evacuation Pause trigger. Live objects are copied out of one region (evacuated) to another region to free
     * contiguous space. For both young and mixed collections.
     */
    G1_EVACUATION_PAUSE("G1 Evacuation Pause"),
    /**
     * Humongous object allocation trigger.
     * 
     * Objects larger than 1/2 {@link org.github.joa.JvmOptions#getG1HeapRegionSize()} are treated as humongous and
     * allocated directly into the contiguous set of humongous regions.
     * 
     * If the allocation triggers a {@link org.eclipselabs.garbagecat.domain.YoungCollection}, it means the humongous
     * allocation failed.
     * 
     * If it triggers a {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent} or
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungInitialMarkEvent}, it means the Initiating
     * Heap Occupancy Percent (IHOP) check performed with every humongous allocation failed. The old generation
     * occupancy as a percent of the total heap size reached (&gt;=) IHOP. IHOP is initially set to
     * <code>InitiatingHeapOccupancyPercent</code> (default 45) and adaptive based on ergonomics. If adaptive IHOP is
     * disabled with <code>-XX:-G1UseAdaptiveIHOP</code>, IHOP is fixed at <code>InitiatingHeapOccupancyPercent</code>.
     */
    G1_HUMONGOUS_ALLOCATION("G1 Humongous Allocation"),
    //
    G1_PREVENTIVE_COLLECTION("G1 Preventive Collection"),
    /**
     * The GC triggered after the GCLocker has been released.
     * 
     * The GCLocker is used to prevent GC while JNI (native) code is running in a "critical region."
     * 
     * Certain JNI function pairs are classified as "critical" because a Java object is passed into the JNI code. While
     * the code is running inside the "critical region" it is necessary to prevent GC from happening to prevent
     * compaction from changing the Java object memory address (and cause a crash due to the JNI code accessing a bad
     * memory address).
     * 
     * For example, any code running between <code>GetPrimitiveArrayCritical</code> and
     * <code>ReleasePrimitiveArrayCritical</code> is a "critical region".
     * 
     * If a thread cannot find a free area of heap large enough to allocate an object, it triggers a GC. If another
     * thread is running JNI code in a "critical region", the GC cannot happen, so the the JVM requests a "GCLocker
     * Initiated GC" and waits.
     * 
     * The expectation is that the GCLocker will not be held for a long time (there will not be long running code in a
     * "critical region").
     */
    GCLOCKER_INITIATED_GC("GCLocker Initiated GC"),
    //
    HEAP_DUMP_INITIATED_GC("Heap Dump Initiated GC"),
    //
    HEAP_INSPECTION_INITIATED_GC("Heap Inspection Initiated GC"),
    // JVM Tool Interface explicit GC trigger
    JVMTI_FORCED_GARBAGE_COLLECTION("JvmtiEnv ForceGarbageCollection"),
    /**
     * Run after METADATA_GC_THRESHOLD fails to resize the Metaspace. A full collection is run to clean up soft
     * references and free Metaspace. If this fails to free space, OutOfMemoryError is thrown.
     */
    LAST_DITCH_COLLECTION("Last ditch collection"),
    /**
     * Metadata GC Threshold trigger. When the Metaspace is resized. The JVM has failed to allocate memory for something
     * that should be stored in Metaspace and does a full collection before attempting to resize the Metaspace.
     */
    METADATA_GC_THRESHOLD("Metadata GC Threshold"),
    //
    METADATE_GC_CLEAR_SOFT_REFERENCES("Metadata GC Clear Soft References"),
    //
    NONE("NONE"),
    /**
     * <p>
     * Promotion failed trigger.
     * </p>
     * 
     * <p>
     * Occurs when objects cannot be moved from the young to the old generation due to lack of space or fragmentation.
     * The young generation collection backs out of the young collection and initiates a
     * {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} full collection in an attempt to free up and
     * compact space. This is an expensive operation that typically results in large pause times.
     * </p>
     * 
     * <p>
     * The CMS collector is not a compacting collector. It discovers garbage and adds the memory to free lists of
     * available space that it maintains based on popular object sizes. If many objects of varying sizes are allocated,
     * the free lists will be split. This can lead to many free lists whose total size is large enough to satisfy the
     * calculated free space needed for promotions; however, there is not enough contiguous space for one of the objects
     * being promoted.
     * </p>
     * 
     * <p>
     * Prior to Java 5.0 the space requirement was the worst-case scenario that all young generation objects get
     * promoted to the old generation (the young generation guarantee). Starting in Java 5.0 the space requirement is an
     * estimate based on recent promotion history and is usually much less than the young generation guarantee.
     * </p>
     */
    PROMOTION_FAILED("promotion failed"),
    /**
     * TODO:
     */
    SHENANDOAH_EVACUATION("Evacuation"),
    /**
     * TODO:
     */
    SHENANDOAH_EVICTION("Eviction"),
    /**
     * TODO:
     */
    SHENANDOAH_MARK("Mark"),
    /**
     * TODO:
     */
    SHENANDOAH_OUTSIDE_OF_CYCLE("Outside of Cycle"),
    /**
     * TODO:
     */
    SHENANDOAH_PROCESS_WEAKREFS("process weakrefs"),

    /**
     * TODO:
     */
    SHENANDOAH_UNLOAD_CLASSES("unload classes"),
    /**
     * TODO:
     */
    SHENANDOAH_UPDATE_REFS("[uU]pdate [rR]efs"),
    // Explicit garbage collection invoked.
    SYSTEM_GC("System(.gc\\(\\))?"),
    /**
     * <p>
     * To Space Exhausted trigger. A G1_YOUNG_PAUSE, G1_MIXED_PAUSE, or G1_YOUNG_INITIAL_MARK collection cannot happen
     * due to "to-space exhausted". There is not enough free space in the heap for survived and/or promoted objects, and
     * the heap cannot be expanded. This is a very expensive operation. Sometimes the collector's ergonomics can resolve
     * the issue by dynamically re-sizing heap regions. If it cannot, it invokes a G1_FULL_GC in an attempt to reclaim
     * enough space to continue. All of the following are possible resolutions:
     * </p>
     * 
     * <ol>
     * 
     * <li>Increase the heap size.</li>
     * <li>Increase <code>-XX:G1ReservePercent</code> and the heap size to increase the amount of to-space reserve
     * memory.</li>
     * <li>Reduce the <code>-XX:InitiatingHeapOccupancyPercent</code> (default 45) to start the marking cycle
     * earlier.</li>
     * <li>Increase the number of parallel marking threads with <code>-XX:ConcGCThreads</code>. For example:
     * <code>-XX:ConcGCThreads=16</code>.
     * </ol>
     */
    TO_SPACE_EXHAUSTED("to-space exhausted"),
    /**
     * <p>
     * To Space Overflow trigger. A G1_YOUNG_PAUSE, G1_MIXED_PAUSE, or G1_YOUNG_INITIAL_MARK collection cannot happen
     * due to "to-space overflow". There is not enough free space in the heap for survived and/or promoted objects, and
     * the heap cannot be expanded. This is a very expensive operation. Sometimes the collector's ergonomics can resolve
     * the issue by dynamically re-sizing heap regions. If it cannot, it invokes a G1_FULL_GC in an attempt to reclaim
     * enough space to continue. All of the following are possible resolutions:
     * </p>
     * 
     * <ol>
     * <li>Increase the heap size.</li>
     * <li>Increase <code>-XX:G1ReservePercent</code> and the heap size to increase the amount of to-space reserve
     * memory.</li>
     * <li>Reduce the <code>-XX:InitiatingHeapOccupancyPercent</code> (default 45) to start the marking cycle
     * earlier.</li>
     * <li>Increase the number of parallel marking threads with <code>-XX:ConcGCThreads</code>. For example:
     * <code>-XX:ConcGCThreads=16</code>.
     * </ol>
     */
    TO_SPACE_OVERFLOW("to-space overflow"),
    //
    UNKNOWN("UNKNOWN");

    /**
     * Get <code>GcTrigger</code> from log literal.
     * 
     * @param literal
     *            The trigger literal.
     * @return The <code>GcTrigger</code>.
     */
    public static GcTrigger getTrigger(final String literal) {
        if (literal != null) {
            if (literal.matches(ALLOCATION_FAILURE.regex))
                return ALLOCATION_FAILURE;
            if (literal.matches(CLASS_HISTOGRAM.regex))
                return CLASS_HISTOGRAM;
            if (literal.matches(CMS_FINAL_REMARK.regex))
                return CMS_FINAL_REMARK;
            if (literal.matches(CMS_INITIAL_MARK.regex))
                return CMS_INITIAL_MARK;
            if (literal.matches(CONCURRENT_MODE_FAILURE.regex))
                return CONCURRENT_MODE_FAILURE;
            if (literal.matches(CONCURRENT_MODE_INTERRUPTED.regex))
                return CONCURRENT_MODE_INTERRUPTED;
            if (literal.matches(DIAGNOSTIC_COMMAND.regex))
                return DIAGNOSTIC_COMMAND;
            if (literal.matches(ERGONOMICS.regex))
                return ERGONOMICS;
            if (literal.matches(G1_COMPACTION_PAUSE.regex))
                return G1_COMPACTION_PAUSE;
            if (literal.matches(G1_EVACUATION_PAUSE.regex))
                return G1_EVACUATION_PAUSE;
            if (literal.matches(G1_HUMONGOUS_ALLOCATION.regex))
                return G1_HUMONGOUS_ALLOCATION;
            if (literal.matches(G1_PREVENTIVE_COLLECTION.regex))
                return G1_PREVENTIVE_COLLECTION;
            if (literal.matches(GCLOCKER_INITIATED_GC.regex))
                return GCLOCKER_INITIATED_GC;
            if (literal.matches(HEAP_DUMP_INITIATED_GC.regex))
                return HEAP_DUMP_INITIATED_GC;
            if (literal.matches(HEAP_INSPECTION_INITIATED_GC.regex))
                return HEAP_INSPECTION_INITIATED_GC;
            if (literal.matches(JVMTI_FORCED_GARBAGE_COLLECTION.regex))
                return JVMTI_FORCED_GARBAGE_COLLECTION;
            if (literal.matches(LAST_DITCH_COLLECTION.regex))
                return LAST_DITCH_COLLECTION;
            if (literal.matches(METADATE_GC_CLEAR_SOFT_REFERENCES.regex))
                return METADATE_GC_CLEAR_SOFT_REFERENCES;
            if (literal.matches(METADATA_GC_THRESHOLD.regex))
                return METADATA_GC_THRESHOLD;
            if (literal.matches(PROMOTION_FAILED.regex))
                return PROMOTION_FAILED;
            if (literal.matches(SHENANDOAH_EVACUATION.regex))
                return SHENANDOAH_EVACUATION;
            if (literal.matches(SHENANDOAH_EVICTION.regex))
                return SHENANDOAH_EVICTION;
            if (literal.matches(SHENANDOAH_MARK.regex))
                return SHENANDOAH_MARK;
            if (literal.matches(SHENANDOAH_OUTSIDE_OF_CYCLE.regex))
                return SHENANDOAH_OUTSIDE_OF_CYCLE;
            if (literal.matches(SHENANDOAH_PROCESS_WEAKREFS.regex))
                return SHENANDOAH_PROCESS_WEAKREFS;
            if (literal.matches(SHENANDOAH_UNLOAD_CLASSES.regex))
                return SHENANDOAH_UNLOAD_CLASSES;
            if (literal.matches(SHENANDOAH_UPDATE_REFS.regex))
                return SHENANDOAH_UPDATE_REFS;
            if (literal.matches(SYSTEM_GC.regex))
                return SYSTEM_GC;
            if (literal.matches(TO_SPACE_EXHAUSTED.regex))
                return TO_SPACE_EXHAUSTED;
            if (literal.matches(TO_SPACE_OVERFLOW.regex))
                return TO_SPACE_OVERFLOW;
            return UNKNOWN;
        }
        return NONE;
    }

    private final String regex;

    private GcTrigger(final String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
