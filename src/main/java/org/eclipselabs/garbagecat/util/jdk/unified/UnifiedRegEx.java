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
package org.eclipselabs.garbagecat.util.jdk.unified;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;

/**
 * Regular expression utility methods and constants for OpenJDK and derivatives unified logging.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedRegEx {

    /**
     * Seconds since JVM started.
     * 
     * For example: 25.016s
     */
    public static final String UPTIME = "(\\d{0,12}[\\.\\,]\\d{3})s";

    /**
     * Milliseconds since JVM started.
     * 
     * For example: 3ms
     */
    public static final String UPTIMEMILLIS = "(\\d{1,9})ms";

    /**
     * The duration of the event in milliseconds with 3 decimal places, introduced JDK9.
     * 
     * For example: 2.969ms, 0.2ms, 15.91 ms
     */
    public static final String DURATION = "(\\d{1,7}[\\.\\,]\\d{1,3})[ ]{0,1}ms";

    /**
     * The garbage collection event number in JDK9+ unified logging.
     * 
     * For example: GC(6)
     */
    public static final String GC_EVENT_NUMBER = "GC\\(\\d{1,7}\\)";

    /**
     * Regular expression for recognized decorations prepending logging.
     * 
     * <p>
     * 1) Default uptime,level,tags (e.g. -Xlog:gc*:file=gc.log::filecount=4,filesize=50M or
     * -Xlog:gc*:file=gc.log:uptime,level,tags:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [0.057s][info][gc,start ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 2) uptime (e.g. -Xlog:gc*:file=gc.log:uptime:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 3) uptimemillis (e.g. -Xlog:gc*:file=gc.log:uptimemillis:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [052ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 4) time (e.g. -Xlog:gc*:file=gc.log:time:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 5) time,uptime (e.g. -Xlog:gc*:file=gc.log:time,uptime:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500][0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 6) time,uptimemillis (e.g. -Xlog:gc*:file=gc.log:time,uptimemillis:filecount=4,filesize=50M):
     * </p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500][52ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     */
    public static final String DECORATOR = "\\[(" + JdkRegEx.DATESTAMP + "|" + UPTIME + "|" + UPTIMEMILLIS + ")\\](\\[("
            + UPTIME + "|" + UPTIMEMILLIS + ")\\])?(\\[info\\]\\[(gc|safepoint)"
            + "(,(cds|cpu|ergo|heap|init|marking|metaspace|phases|stats|start|stringtable|task))?"
            + "(,(coops|exit|start))?[ ]{0,13}\\])?( " + UnifiedRegEx.GC_EVENT_NUMBER + ")?";

    /**
     * Blank line.
     */
    public static final String BLANK_LINE = "^" + DECORATOR + "\\s*$";

    /**
     * Logging event with only the time decorator (datestamp).
     */
    public static final String DATESTAMP_EVENT = "\\[" + JdkRegEx.DATESTAMP + "\\](\\[info\\]\\[gc"
            + "(,(cpu|ergo|heap|init|marking|metaspace|phases|stats|start|stringtable|task))?"
            + "(,(coops|exit|start))?[ ]{0,13}\\])?.*";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private UnifiedRegEx() {

    }

}
