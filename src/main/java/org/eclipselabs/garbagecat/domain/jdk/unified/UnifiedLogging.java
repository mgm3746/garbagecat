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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.domain.LogEvent;

/**
 * <p>
 * Unified logging (JDK9+).
 * 
 * The datestamp/uptime has changed from the time when the <code>LogEvent</code> begins (prior to JDK9) to the time when
 * the <code>LogEvent</code> ends.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface UnifiedLogging extends LogEvent {

    /**
     * Defined tags.
     * 
     * GC = gc
     * 
     * GC_CPU = gc+cpu
     */
    public enum Tag {
        GC, GC_CPU, GC_HEAP, GC_INIT, GC_START, SAFEPOINT, UNKNOWN
    }

    /**
     * @return The log event tag.
     */
    Tag getTag();

    /**
     * <p>
     * Report if the timestamp in a log event is the time the event begins or ends. This is important for setting
     * {@link org.eclipselabs.garbagecat.domain.LogEvent#getTimestamp()}, the time the event begins.
     * </p>
     * 
     * <p>
     * For example, the logging below represents a single
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent}.
     * </p>
     * 
     * <p>
     * The timestamp in the `gc,start` event (`2022-08-03T06:58:41.313+0000`) is a true timestamp (the time the gc
     * started). The timestamp in the `gc` event (`2022-08-03T06:58:41.321+0000`) is an endstamp (the time the gc
     * ended). The difference between the two equals the `7,870ms` pause time.
     * </p>
     * 
     * <pre>
     * [2022-08-03T06:58:41.313+0000][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * [2022-08-03T06:58:41.313+0000][gc,task     ] GC(0) Using 8 workers of 8 for evacuation
     * [2022-08-03T06:58:41.313+0000][gc,age      ] GC(0) Desired survivor size 41943040 bytes, new threshold 15 
     * (max threshold 15)
     * [2022-08-03T06:58:41.321+0000][gc,age      ] GC(0) Age table with threshold 15 (max threshold 15)
     * [2022-08-03T06:58:41.321+0000][gc,age      ] GC(0) - age   1:   16037032 bytes,   16037032 total
     * [2022-08-03T06:58:41.321+0000][gc,metaspace] GC(0) Metaspace: 19460K(19840K)-&gt;19460K(19840K) NonClass: 
     * 17082K(17280K)-&gt;17082K(17280K) Class: 2378K(2560K)-&gt;2378K(2560K)
     * [2022-08-03T06:58:41.321+0000][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 
     * 615M-&gt;23M(12288M) 7,870ms
     * [2022-08-03T06:58:41.321+0000][gc,cpu      ] GC(0) User=0,04s Sys=0,00s Real=0,01s
     * </pre>
     * 
     * @return true if the timestamp is the time when the event ends, false if it is the time the event begins.
     */
    boolean isEndstamp();
}
