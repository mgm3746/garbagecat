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

/**
 * Regular expression utility methods and constants for OpenJDK and Oracle JDK.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JdkRegEx {

    /**
     * Timestamp. Milliseconds since JVM started.
     * 
     * For example: 487.020
     */
    public static final String TIMESTAMP = "(\\d{0,12}[\\.\\,]\\d{3})";

    /**
     * Datestamp. Absolute date/time the JVM uses with <code>-XX:+PrintGCDateStamps</code>.
     * 
     * For example:
     * 
     * 1) Minus GMT: 2010-02-26T09:32:12.486-0600
     * 
     * 2) Plus GMT: 2010-04-16T12:11:18.979+0200
     */
    public static final String DATESTAMP = "((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(-|\\+)"
            + "(\\d{4}))";

    /**
     * Datetime.
     * 
     * For example:
     * 
     * 2016-10-18 01:50:54
     */
    public static final String DATETIME = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    /**
     * The size of memory in kilobytes. Sometimes there is a space between the number and the "K" units.
     * 
     * For example: 27808K, 16172 K
     * 
     * TODO: Combine with SIZE_K?
     */
    public static final String SIZE_K = "(\\d{1,9})[ ]?K";

    /**
     * Byte units identifier.
     */
    public static final String BYTES = "B";

    /**
     * Kilobyte units identifier.
     */
    public static final String KILOBYTES = "K";

    /**
     * Megabyte units identifier.
     */
    public static final String MEGABYTES = "M";

    /**
     * Gigabyte units identifier.
     */
    public static final String GIGABYTES = "G";

    /**
     * The size of memory in bytes (B), kilobytes (K), megabytes (M), or gigabytes (G) to a whole number or to one
     * decimal place.
     * 
     * See with G1 collector <code>-XX:+PrintGCDetails</code>.
     * 
     * With the G1 collector units are not consistent line to line or even within a single logging line.
     * 
     * Whole number examples: 2128K, 30M, 30G
     * 
     * Decimal examples: 0.0B, 8192.0K, 28.0M, 30.0G
     * 
     * With comma: 306,0M
     */
    public static final String SIZE = "(\\d{1,8}([\\.,]\\d)?)([" + BYTES + KILOBYTES + MEGABYTES + GIGABYTES + "])";

    /**
     * The size of memory in bytes. No units.
     */
    public static final String SIZE_BYTES = "(\\d{1,11})";

    /**
     * A memory address.
     */
    public static final String ADDRESS = "(0x[0-9a-f]{16})";

    /**
     * The duration of the event in seconds with 7-8 decimal places.
     * 
     * For example: 0.0225213 secs, 0.00376500 secs
     */
    public static final String DURATION = "(\\d{1,4}[\\.\\,]\\d{7,8})( sec)?(s)?";

    /**
     * The duration of the event as a fraction of a time period.
     * 
     * For example: 2.272/29.793 secs
     */
    public static final String DURATION_FRACTION = "(\\d{1,4}[\\.\\,]\\d{3}\\/\\d{1,5}[\\.\\,]\\d{3}) secs";

    /**
     * Data when the CMS collector is run in incremental mode with the <code>-XX:+CMSIncrementalMode</code> JVM option.
     * In this mode, the CMS collector does not hold the processor(s) for the entire long concurrent phases but
     * periodically stops them and yields the processor back to other threads in the application. It divides the work to
     * be done in concurrent phases into small chunks called duty cycles and schedules them between minor collections.
     * This is very useful for applications that need low pause times and are run on machines with a small number of
     * processors. The icms_dc value is the time in percentage that the concurrent work took between two young
     * generation collections.
     * 
     * For example: icms_dc=70
     */
    public static final String ICMS_DC_BLOCK = "( icms_dc=\\d{1,3} )";

    /**
     * Permanent generation collection class unloading.
     * 
     * For example:
     * 
     * [Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]
     */
    public static final String UNLOADING_CLASS_BLOCK = "\\[Unloading class [a-zA-Z\\.\\$\\d_]+\\]";

    /**
     * Blank line.
     */
    public static final String BLANK_LINE = "^\\s+$";

    /**
     * Percent.
     * 
     * For example: avg 54.8%
     */
    public static final String PERCENT = "\\d{1,3}\\.\\d%";

    /**
     * Allocation rate.
     * 
     * For example: 328.75 MB/s
     */
    public static final String ALLOCATION_RATE = "\\d{1,6}(\\.\\d{2})? [KM]B\\/s";

    /**
     * System.gc() trigger. Explicit garbage collection invoked.
     */
    public static final String TRIGGER_SYSTEM_GC = "System(.gc\\(\\))?";

    /**
     * Metadata GC Threshold trigger. When the Metaspace is resized. The JVM has failed to allocate memory for something
     * that should be stored in Metaspace and does a full collection before attempting to resize the Metaspace.
     */
    public static final String TRIGGER_METADATA_GC_THRESHOLD = "Metadata GC Threshold";

    /**
     * Allocation Failure trigger.
     */
    public static final String TRIGGER_ALLOCATION_FAILURE = "Allocation Failure";

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
     * <li>Reduce the <code>-XX:InitiatingHeapOccupancyPercent</code> (default 45) to start the marking cycle earlier.
     * </li>
     * <li>Increase the number of parallel marking threads with <code>-XX:ConcGCThreads</code>. For example:
     * <code>-XX:ConcGCThreads=16</code>.
     * </ol>
     */
    public static final String TRIGGER_TO_SPACE_EXHAUSTED = "to-space exhausted";

    /**
     * <p>
     * To Space Overflow trigger. A G1_YOUNG_PAUSE, G1_MIXED_PAUSE, or G1_YOUNG_INITIAL_MARK collection cannot happen
     * due to "to-space oeverflow". There is not enough free space in the heap for survived and/or promoted objects, and
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
     * <li>Reduce the <code>-XX:InitiatingHeapOccupancyPercent</code> (default 45) to start the marking cycle earlier.
     * </li>
     * <li>Increase the number of parallel marking threads with <code>-XX:ConcGCThreads</code>. For example:
     * <code>-XX:ConcGCThreads=16</code>.
     * </ol>
     */
    public static final String TRIGGER_TO_SPACE_OVERFLOW = "to-space overflow";

    /**
     * G1 Evacuation Pause trigger. Live objects are copied out of one region (evacuated) to another region to free
     * contiguous space. For both young and mixed collections.
     */
    public static final String TRIGGER_G1_EVACUATION_PAUSE = "G1 Evacuation Pause";

    /**
     * GCLocker Initiated GC trigger. A GC initiated after the JNI critical region is released. This is caused when a GC
     * is requested when a thread is in the JNI critical region. GC is blocked until all threads exit the JNI critical
     * region.
     */
    public static final String TRIGGER_GCLOCKER_INITIATED_GC = "GCLocker Initiated GC";

    /**
     * CMS Initial Mark trigger
     */
    public static final String TRIGGER_CMS_INITIAL_MARK = "CMS Initial Mark";

    /**
     * CMS Final Remark trigger
     */
    public static final String TRIGGER_CMS_FINAL_REMARK = "CMS Final Remark";

    /**
     * CMS Final Remark trigger
     */
    public static final String TRIGGER_CLASS_HISTOGRAM = "Class Histogram";

    /**
     * Run after TRIGGER_METADATA_GC_THRESHOLD fails to resize the Metaspace. A full collection is run to clean up soft
     * references and free Metaspace. If this fails to free space, OutOfMemoryError is thrown.
     */
    public static final String TRIGGER_LAST_DITCH_COLLECTION = "Last ditch collection";

    /**
     * JVM Tool Interface explicit GC trigger
     */
    public static final String TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION = "JvmtiEnv ForceGarbageCollection";

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
    public static final String TRIGGER_ERGONOMICS = "Ergonomics";

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
    public static final String TRIGGER_CONCURRENT_MODE_FAILURE = "concurrent mode failure";

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
    public static final String TRIGGER_CONCURRENT_MODE_INTERRUPTED = "concurrent mode interrupted";

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
     * 
     */
    public static final String TRIGGER_PROMOTION_FAILED = "promotion failed";

    /**
     * Heap inspection initiated gc trigger.
     */
    public static final String TRIGGER_HEAP_INSPECTION_INITIATED_GC = "Heap Inspection Initiated GC";

    /**
     * Heap dump initiated gc trigger.
     */
    public static final String TRIGGER_HEAP_DUMP_INITIATED_GC = "Heap Dump Initiated GC";

    /**
     * Humongous object allocation failed trigger.
     */
    public static final String TRIGGER_G1_HUMONGOUS_ALLOCATION = "G1 Humongous Allocation";

    /**
     * Units for JVM options that take a byte number.
     * 
     * For example: -Xss128k -Xmx2048m -Xms2G
     */
    public static final String OPTION_SIZE = "(b|B|k|K|m|M|g|G)";

    /**
     * <code>-XX:+PrintPromotionFailure</code> output.
     * 
     * For example:
     * 
     * (0: promotion failure size = 200) (1: promotion failure size = 8) (2: promotion failure size = 200) (3: promotion
     * failure size = 200) (4: promotion failure size = 200) (5: promotion failure size = 200) (6: promotion failure
     * size = 200) (7: promotion failure size = 200) (8: promotion failure size = 10) (9: promotion failure size = 10)
     * (10: promotion failure size = 10) (11: promotion failure size = 200) (12: promotion failure size = 200) (13:
     * promotion failure size = 10) (14: promotion failure size = 200) (15: promotion failure size = 200) (16: promotion
     * failure size = 200) (17: promotion failure size = 200) (18: promotion failure size = 200) (19: promotion failure
     * size = 200) (20: promotion failure size = 10) (21: promotion failure size = 200) (22: promotion failure size =
     * 10) (23: promotion failure size = 45565) (24: promotion failure size = 10) (25: promotion failure size = 4) (26:
     * promotion failure size = 200) (27: promotion failure size = 200) (28: promotion failure size = 10) (29: promotion
     * failure size = 200) (30: promotion failure size = 200) (31: promotion failure size = 200) (32: promotion failure
     * size = 200)
     */
    public static final String PRINT_PROMOTION_FAILURE = "( \\(\\d{1,2}: promotion failure size = \\d{1,10}\\) ){1,64}";

    /**
     * Regular expression for recognized decorations prepending logging.
     * 
     * <p>
     * 1) time: uptime:
     * </p>
     * 
     * <pre>
     * 2020-03-10T08:03:29.311-0400: 0.373: [Concurrent reset 16991K-&gt;17152K(17408K), 0.435 ms]
     * </pre>
     */
    public static final String DECORATOR = JdkRegEx.DATESTAMP + ": " + JdkRegEx.TIMESTAMP + ":";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkRegEx() {

    }

}
