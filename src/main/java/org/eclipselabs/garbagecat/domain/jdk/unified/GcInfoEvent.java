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

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * GC_INFO
 * </p>
 * 
 * <p>
 * Information printed at the beginning of gc logging and between cycles.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Header default (uptime,level,tags):
 * </p>
 * 
 * <pre>
 * [0.006s][info][gc,init] Regions: 256 x 256K
 * [0.006s][info][gc,init] Humongous object threshold: 256K
 * [0.006s][info][gc,init] Max TLAB size: 256K
 * [0.006s][info][gc,init] GC threads: 2 parallel, 1 concurrent
 * [0.006s][info][gc,init] Reference processing: parallel
 * [0.006s][info][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent
 * [0.006s][info][gc     ] Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent
 * [0.006s][info][gc,init] Shenandoah heuristics: adaptive
 * [0.007s][info][gc,ergo] Pacer for Idle. Initial: 1310K, Alloc Tax Rate: 1.0x
 * [0.007s][info][gc,init] Initialize Shenandoah heap: 32768K initial, 32768K min, 65536K max
 * [0.007s][info][gc,init] Safepointing mechanism: global-page poll
 * 
 * 
 * </pre>
 * 
 * <p>
 * 2) Header datestamp and milliseconds (time,millis):
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K
 * [2019-02-05T14:47:31.091-0200][3ms] Max TLAB size: 512K
 * [2019-02-05T14:47:31.091-0200][3ms] GC threads: 4 parallel, 4 concurrent
 * [2019-02-05T14:47:31.091-0200][3ms] Reference processing: parallel
 * [2019-02-05T14:47:31.091-0200][3ms] Shenandoah heuristics: adaptive
 * [2019-02-05T14:47:31.091-0200][3ms] Initialize Shenandoah heap with initial size 1366294528 bytes
 * [2019-02-05T14:47:31.091-0200][3ms] Pacer for Idle. Initial: 26M, Alloc Tax Rate: 1.0x
 * [2019-02-05T14:47:31.092-0200][4ms] Safepointing mechanism: global-page poll*
 * </pre>
 * 
 * <p>
 * 3) Between cycles default (uptime,level,tags):
 * 
 * Note: Trigger is broken out to a separate event,
 * {@link org.eclipselabs.garbagecat.domain.jdk.unified.ShenandoahTriggerEvent} ,for possible future analsysis.
 * </p>
 * 
 * <pre>
 * [0.635s][info][gc,ergo      ] Free: 47665K (189 regions), Max regular: 256K, Max humongous: 39680K, External frag: 17%, Internal frag: 1%
 * [0.635s][info][gc,ergo      ] Evacuation Reserve: 3328K (13 regions), Max regular: 256K
 * [0.635s][info][gc,ergo      ] Pacer for Idle. Initial: 1310K, Alloc Tax Rate: 1.0x
 * [0.724s][info][gc           ] Trigger: Average GC time (20.65 ms) is above the time for allocation rate (400 MB/s) to deplete free headroom (8008K)
 * [0.724s][info][gc,ergo      ] Free headroom: 11284K (free) - 3276K (spike) - 0B (penalties) = 8008K
 * [0.724s][info][gc,ergo      ] Free: 11284K (46 regions), Max regular: 256K, Max humongous: 10496K, External frag: 7%, Internal frag: 4%
 * [0.724s][info][gc,ergo      ] Evacuation Reserve: 3328K (13 regions), Max regular: 256K
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcInfoEvent implements UnifiedLogging, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            //
            "^" + UnifiedLogging.DECORATOR + " Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedLogging.DECORATOR + " Max TLAB size: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedLogging.DECORATOR + " GC threads: \\d parallel, \\d concurrent$",
            //
            "^" + UnifiedLogging.DECORATOR + " Reference processing: parallel$",
            //
            "^" + UnifiedLogging.DECORATOR + " Shenandoah heuristics: adaptive$",
            //
            "^" + UnifiedLogging.DECORATOR + " Initialize Shenandoah heap( with initial size \\d{10} bytes|: "
                    + JdkRegEx.SIZE + " initial, " + JdkRegEx.SIZE + " min, " + JdkRegEx.SIZE + " max)$",
            //
            "^" + UnifiedLogging.DECORATOR + " Pacer for Idle. Initial: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^" + UnifiedLogging.DECORATOR + " Safepointing mechanism: global-page poll$",
            //
            "^" + UnifiedLogging.DECORATOR + " Free: " + JdkRegEx.SIZE + " \\(\\d{1,4} regions\\), Max regular: "
                    + JdkRegEx.SIZE + ", Max humongous: " + JdkRegEx.SIZE
                    + ", External frag: \\d%, Internal frag: \\d%$",
            //
            "^" + UnifiedLogging.DECORATOR + " Evacuation Reserve: " + JdkRegEx.SIZE
                    + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedLogging.DECORATOR + " Regions: \\d{1,3} x " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedLogging.DECORATOR + " Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedLogging.DECORATOR + " Heuristics ergonomically sets (-XX:\\+ExplicitGCInvokesConcurrent|"
                    + "-XX:\\+ShenandoahImplicitGCInvokesConcurrent)$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public GcInfoEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_INFO.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}
