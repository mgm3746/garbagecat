/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

/**
 * Regular expression utility methods and constants for OpenJDK and Sun JDK.
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
     * The size of memory in kilobytes (K). Sometimes there is a space between the number and units.
     * 
     * For example: 27808K, 16172 K
     */
    public static final String SIZE = "(\\d{1,8})[ ]?K";

    /**
     * The size of memory in kilobytes (K) or megabytes (M). Starting with JDK 1.7, units are not consistent line to
     * line or even within a single logging line.
     * 
     * For example: 2128K, 30M
     */
    public static final String SIZE_JDK7 = "(\\d{1,8})([KM])";

    /**
     * Kilobyte units identifier.
     */
    public static final String KILOBYTES = "K";

    /**
     * Megabyte units identifier.
     */
    public static final String MEGABYTES = "M";

    /**
     * The duration of the event in seconds with 7-8 decimal places.
     * 
     * For example: 0.0225213 secs
     */
    public static final String DURATION = "(\\d{1,4}[\\.\\,]\\d{7,8}) secs";

    /**
     * The duration of the event as a fraction of a time period.
     * 
     * For example: 2.272/29.793 secs
     */
    public static final String DURATION_FRACTION = "(\\d{1,4}\\.\\d{3}\\/\\d{1,4}\\.\\d{3}) secs";

    /**
     * Times data. I'm not really sure what JVM option(s) cause this to be output, but I see it added to the end of GC
     * events.
     * 
     * For example: [Times: user=0.31 sys=0.00, real=0.04 secs]
     */
    public static final String TIMES_BLOCK = "( \\[Times: user=\\d{1,4}[\\.\\,]\\d{2} sys=\\d{1,4}[\\.\\,]\\d{2}, "
            + "real=\\d{1,4}[\\.\\,]\\d{2} secs\\])";

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
     * Make default constructor private so the class cannot be instantiated.
     */
    private JdkRegEx() {

    }

}
