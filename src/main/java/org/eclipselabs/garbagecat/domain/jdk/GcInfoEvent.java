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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * GC_INFO
 * 
 * TODO: Move to UnifiedHeaderEvent?
 * </p>
 * 
 * <p>
 * Information printed at the beginning of gc logging and between cycles.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Jdk8 header:
 * </p>
 * 
 * <pre>
 * Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent
 * Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent
 * </pre>
 * 
 * <p>
 * 2) Unified header default (uptime,level,tags):
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
 * 3) Unified header datestamp and milliseconds (time,millis):
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
 * 4) Unified between cycles default (uptime,level,tags):
 * </p>
 * 
 * <p>
 * Note: Trigger is broken out to a separate event, {@link org.eclipselabs.garbagecat.domain.jdk.ShenandoahTriggerEvent}
 * ,for possible future analsysis.
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
public class GcInfoEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Max TLAB size: " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?GC threads: \\d parallel, \\d concurrent$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?Reference processing: (parallel|parallel discovery, parallel processing)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Shenandoah GC mode: Snapshot-At-The-Beginning \\(SATB\\)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Shenandoah heuristics: [a|A]daptive$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Initialize Shenandoah heap( with initial size \\d{10} bytes|: "
                    + JdkRegEx.SIZE + " initial, " + JdkRegEx.SIZE + " min, " + JdkRegEx.SIZE + " max)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Pacer for Idle. Initial: " + JdkRegEx.SIZE
                    + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Safepointing mechanism: global-page poll$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Free: " + JdkRegEx.SIZE + "( \\(\\d{1,4} regions\\))?, "
                    + "Max( regular)?: " + JdkRegEx.SIZE + "( regular)?, (Max humongous: )?" + JdkRegEx.SIZE
                    + "( humongous)?, (External )?[fF]rag: \\d{1,3}%( external)?, (Internal frag: )?\\d{1,3}%"
                    + "( internal; Reserve: " + JdkRegEx.SIZE + ", Max: " + JdkRegEx.SIZE + ")?$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Evacuation Reserve: " + JdkRegEx.SIZE
                    + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Regions: \\d{1,} x " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Heuristics ergonomically sets (-XX:\\+ExplicitGCInvokesConcurrent|"
                    + "-XX:\\+ShenandoahImplicitGCInvokesConcurrent)$"
            //
    };

    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(REGEX.length);

    static {
        for (String regex : REGEX) {
            REGEX_PATTERN_LIST.add(Pattern.compile(regex));
        }
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
        for (int i = 0; i < REGEX_PATTERN_LIST.size(); i++) {
            Pattern pattern = REGEX_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                match = true;
                break;
            }
        }
        return match;
    }

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
}
