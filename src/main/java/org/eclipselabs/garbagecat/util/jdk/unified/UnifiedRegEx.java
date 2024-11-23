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

import java.util.regex.Pattern;

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

    public static final String DEBUG_LOGGING = "(\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS + ")\\])?"
            + UnifiedRegEx.PID + "?" + UnifiedRegEx.LEVEL + "?)" + UnifiedRegEx.TAGS + "?( "
            + UnifiedRegEx.GC_EVENT_NUMBER + ")?";

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
    public static final String DECORATOR = "(\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS + ")\\])?"
            + UnifiedRegEx.HOSTNAME + "?" + UnifiedRegEx.PID + "?" + UnifiedRegEx.LEVEL + "?)" + UnifiedRegEx.TAGS
            + "?( " + UnifiedRegEx.GC_EVENT_NUMBER + ")?";

    /**
     * The number of regex patterns in <code>UnifiedLogging.DECORATOR</code>. Convenience field to make the code
     * resilient to decorator pattern changes.
     */
    public static final int DECORATOR_SIZE = Pattern.compile(DECORATOR).matcher("[2020-02-14T15:21:55.207-0500]")
            .groupCount();

    /**
     * Regular expression for the garbage collection event number.
     * 
     * For example: GC(6)
     */
    public static final String GC_EVENT_NUMBER = "GC\\(\\d{1,}\\)";

    /**
     * Regular expression for the hostname block.
     * 
     * For example: [localhost.localdomain]
     */
    public static final String HOSTNAME = "(\\[[A-Za-z\\.\\d]{1,}\\])";

    /**
     * Regular expression for the log level.
     * 
     * For example: [info]
     */
    public static final String LEVEL = "(\\[(debug|info|trace)[ ]{0,}\\])";

    /**
     * Regular expression for the process id block.
     * 
     * For example: [1863]
     */
    public static final String PID = "(\\[\\d{1,}\\])";

    /**
     * <p>
     * Regular expression for a &gt;= JDK9 release string (unified).
     * </p>
     * 
     * For example:
     * 
     * <pre>
     * 11.0.9+11-LTS
     * 12.0.1+12
     * 17.0.1+12-LTS
     * 21+35-2513
     * 21.0.1+12-LTS
     * </pre>
     */
    public static final String RELEASE_STRING = "((9|[12]\\d)(\\.\\d\\.(\\d{1,}))?\\+\\d{1,}(-.+)?)";

    /**
     * Regular expression for a `gc,start` event. Used to determine if a timestamp is when the event started or ended.
     */
    public static final String TAG_GC_START = "^.+\\[gc,start[ ]{0,}\\].+";

    /**
     * Regular expression for the tags block.
     * 
     * For example:
     * 
     * [gc ]
     * 
     * [gc,heap ]
     */
    public static final String TAGS = "(\\[((age|alloc|arguments|cds|coops|cpu|cset|ergo|exit|gc|heap|humongous|ihop|"
            + "init|load|marking|metaspace|mmu|nmethod|phases|plab|ref|reloc|ref|refine|region|remset|safepoint|stats|"
            + "start|stringtable|stringdedup|tracking|task|time|tlab)[,]{0,1}){1,}[ ]{0,}\\])";

    /**
     * Logging event with only the time decorator (datestamp).
     */
    public static final String TIME_DECORATOR = "^\\[" + JdkRegEx.DATESTAMP + "\\].*";

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
