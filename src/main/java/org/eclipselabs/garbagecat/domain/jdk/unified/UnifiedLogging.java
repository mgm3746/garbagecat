/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;

/**
 * <p>
 * Unified logging (JDK9+).
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface UnifiedLogging extends LogEvent {

    /**
     * Regular expression for recognized decorations prepending logging.
     * 
     * <p>
     * 1) Default uptime,level,tags (e.g. -Xlog:gc*:file=gc.log::filecount=4,filesize=50M or
     * -Xlog:gc*:file=gc.log:uptime,level,tags:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [0.057s][info][gc,start ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 2) uptime (e.g. -Xlog:gc*:file=gc.log:uptime:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 3) uptimemillis (e.g. -Xlog:gc*:file=gc.log:uptimemillis:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [052ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 4) time (e.g. -Xlog:gc*:file=gc.log:time:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 5) time,uptime (e.g. -Xlog:gc*:file=gc.log:time,uptime:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500][0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     * 
     * <p>
     * 6) time,uptimemillis (e.g. -Xlog:gc*:file=gc.log:time,uptimemillis:filecount=4,filesize=50M):
     * <p>
     * 
     * <pre>
     * [2020-02-14T15:21:55.207-0500][52ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * </pre>
     */
    public static final String DECORATOR = "(\\[" + JdkRegEx.DATESTAMP + "\\])?(\\[(" + JdkRegEx.TIMESTAMP + "s|"
            + JdkRegEx.TIMESTAMP_MILLIS + ")\\])?(\\[info\\]\\[gc"
            + "(,(cpu|ergo|heap|init|marking|metaspace|phases|stats|start|stringtable|task))?"
            + "(,(coops|exit|start))?[ ]{0,13}\\])?";

    /**
     * Blank line.
     */
    public static final String BLANK_LINE = "^" + DECORATOR + "\\s*$";

}
