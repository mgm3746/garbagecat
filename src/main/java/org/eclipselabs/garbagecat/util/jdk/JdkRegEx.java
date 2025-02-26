/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
public final class JdkRegEx {

    /**
     * A memory address.
     */
    public static final String ADDRESS = "(0x[0-9a-f]{8,16})";

    /**
     * Allocation rate.
     * 
     * For example: 328.75 MB/s
     */
    public static final String ALLOCATION_RATE = "\\d{1,6}(\\.\\d{2})? [BKM]B\\/s";

    /**
     * Blank line.
     */
    public static final String BLANK_LINE = "^\\s*$";

    /**
     * <p>
     * Regular expression for valid JDK build date/time in MMM d yyyy HH:mm:ss format (see <code>SimpleDateFormat</code>
     * for date and time pattern definitions).
     * </p>
     * 
     * For example:
     * 
     * <pre>
     * Oct  6 2018 06:46:09
     * </pre>
     */
    public static final String BUILD_DATE_TIME = "([a-zA-Z]{3})[ ]{1,2}(\\d{1,2}) (\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})";

    /**
     * Byte units identifier.
     */
    public static final String BYTES = "B";

    /**
     * Datestamp. Absolute date/time the JVM uses with <code>-XX:+PrintGCDateStamps</code>.
     * 
     * For example:
     * 
     * 1) Minus GMT: 2010-02-26T09:32:12.486-0600
     * 
     * 2) Plus GMT: 2010-04-16T12:11:18.979+0200
     */
    public static final String DATESTAMP = "((\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})"
            + "([-\\+]\\d{4}))";

    /**
     * Logging event with only the time decorator (datestamp).
     */
    public static final String DATESTAMP_EVENT = "^" + JdkRegEx.DATESTAMP + ": (?!" + JdkRegEx.TIMESTAMP + ").*";

    /**
     * Datetime.
     * 
     * For example:
     * 
     * 2016-10-18 01:50:54
     */
    public static final String DATETIME = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    /**
     * Regular expression for recognized decorations prepending logging.
     * 
     * <p>
     * 1) -XX:+PrintGCTimeStamps:
     * </p>
     * 
     * <pre>
     * 2020-03-10T08:03:29.311-0400: 0.373:
     * </pre>
     * 
     * <p>
     * 2) -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps:
     * </p>
     * 
     * <pre>
     * 2020-03-10T08:03:29.311-0400:
     * </pre>
     * 
     * <p>
     * 3) -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps:
     * </p>
     * 
     * <pre>
     * 0.373:
     * </pre>
     */
    public static final String DECORATOR = "(" + DATESTAMP + "|" + JdkRegEx.TIMESTAMP + "):( " + JdkRegEx.TIMESTAMP
            + ":)?";

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
     * The duration of the event in milliseconds with 1-3 decimal places.
     * 
     * For example: 2.969ms, 0.2ms, 15.91 ms
     */
    public static final String DURATION_MS = "(\\d{1,}[\\.\\,]\\d{1,3})[ ]{0,1}ms";

    /**
     * Gigabyte units identifier.
     */
    public static final String GIGABYTES = "G";

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
     * Kilobyte units identifier.
     */
    public static final String KILOBYTES = "K";

    /**
     * Megabyte units identifier.
     */
    public static final String MEGABYTES = "M";

    /**
     * Units for JVM options that take a byte number.
     * 
     * For example: -Xss128k -Xmx2048m -Xms2G
     */
    public static final String OPTION_SIZE = "(b|B|k|K|m|M|g|G)";

    /**
     * Percent.
     * 
     * For example: avg 54.8%
     */
    public static final String PERCENT = "\\d{1,3}\\.\\d%";

    /**
     * -XX:+PrintHeapAtGC output.
     */
    public static final String PRINT_HEAP_AT_GC = "{Heap before gc invocations=146:";

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
     * -XX:+PrintReferenceGC output.
     */
    public static final String PRINT_REFERENCE_GC_BLOCK = JdkRegEx.DECORATOR + " \\[SoftReference, \\d{1,} refs, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR + " \\[WeakReference, \\d{1,} refs, " + JdkRegEx.DURATION
            + "\\]" + JdkRegEx.DECORATOR + " \\[FinalReference, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.DECORATOR + " \\[PhantomReference, \\d{1,} refs, \\d{1,} refs, " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.DECORATOR + " \\[JNI Weak Reference, " + JdkRegEx.DURATION + "\\]";

    /**
     * <p>
     * Regular expression for a &lt;= JDK8 release string (non-unified).
     * </p>
     * 
     * For example:
     * 
     * <pre>
     * 1.8.0_131-b11
     * 1.8.0_342-b07
     * </pre>
     */
    public static final String RELEASE_STRING = "((1.6.0|1.7.0|1.8.0).+)";

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
     * 
     * Whole number with 'K': 27808K, 16172 K
     */
    public static final String SIZE = "(\\d{1,10}([\\.,]\\d)?)[ ]{0,1}([" + BYTES + KILOBYTES + MEGABYTES + GIGABYTES
            + "])";

    /**
     * The size of memory in bytes. No units.
     */
    public static final String SIZE_BYTES = "(\\d{1,11})";

    /**
     * Timestamp. Milliseconds since JVM started.
     * 
     * For example: 487.020
     */
    public static final String TIMESTAMP = "(\\d{0,12}[\\.\\,]\\d{3})";

    /**
     * Permanent generation collection class unloading.
     * 
     * For example:
     * 
     * [Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]
     */
    public static final String UNLOADING_CLASS_BLOCK = "\\[Unloading class [a-zA-Z\\.\\$\\d_]+\\]";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkRegEx() {
        super();
    }

}
