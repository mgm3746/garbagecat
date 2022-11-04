/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
     * Blank line.
     */
    public static final String BLANK_LINE = "^" + UnifiedRegEx.DECORATOR + "\\s*$";

    /**
     * Logging event with only the time decorator (datestamp).
     */
    public static final String DATESTAMP_EVENT = "^\\[" + JdkRegEx.DATESTAMP
            + "\\](\\[info\\]\\[(gc|safepoint)(,(cds|cpu|ergo|heap|init|marking|metaspace|mmu|phases|stats|start|"
            + "stringtable|stringdedup|task))?(,(coops|exit|start))?[ ]{0,13}\\])?.*";

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
     * 
     * <p>
     * 7) -Xlog:gc*,gc+age=trace,safepoint:file=gc_%p_%t.log:utctime,pid,tags:filecount=32,filesize=64m (Elasticsearch
     * default):
     * </p>
     * 
     * <pre>
     * [2022-08-03T06:58:37.056+0000][1863][gc] Using G1
     * </pre>
     */
    public static final String DECORATOR = "\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS
            + ")\\])?(\\[\\d{1,}\\])?(\\[(debug|info)[ ]{0,}\\])?(\\[(gc|safepoint)(,(age|alloc|cds|cpu|ergo|heap|ihop|"
            + "init|load|marking|metaspace|mmu|nmethod|phases|plab|ref|reloc|stats|start|stringtable|stringdedup|task|"
            + "tlab))?(,(coops|cset|exit|ref|refine|region|start|stats))?" + "[ ]{0,}\\])?( "
            + UnifiedRegEx.GC_EVENT_NUMBER + ")?";

    /**
     * The garbage collection event number in JDK9+ unified logging.
     * 
     * For example: GC(6)
     */
    public static final String GC_EVENT_NUMBER = "GC\\(\\d{1,}\\)";

    /**
     * Seconds since JVM started.
     * 
     * For example: 25.016s
     */
    public static final String UPTIME = "(\\d{0,}[\\.\\,]\\d{3})s";

    /**
     * Milliseconds since JVM started.
     * 
     * For example: 3ms
     */
    public static final String UPTIMEMILLIS = "(\\d{1,})ms";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private UnifiedRegEx() {

    }

}
