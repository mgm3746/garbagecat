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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
 * <pre>
 * Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent
 * Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcInfoEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX[] = {
            //
            "^Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^Max TLAB size: " + JdkRegEx.SIZE + "$",
            //
            "^GC threads: \\d parallel, \\d concurrent$",
            //
            "^Reference processing: (parallel|parallel discovery, parallel processing)$",
            //
            "^Shenandoah GC mode: Snapshot-At-The-Beginning \\(SATB\\)$",
            //
            "^Shenandoah heuristics: [a|A]daptive$",
            //
            "^Initialize Shenandoah heap( with initial size \\d{10} bytes|: " + JdkRegEx.SIZE + " initial, "
                    + JdkRegEx.SIZE + " min, " + JdkRegEx.SIZE + " max)$",
            //
            "^Pacer for Idle. Initial: " + JdkRegEx.SIZE + ", Alloc Tax Rate: \\d\\.\\dx$",
            //
            "^Safepointing mechanism: global-page poll$",
            //
            "^Free: " + JdkRegEx.SIZE + "( \\(\\d{1,4} regions\\))?, " + "Max( regular)?: " + JdkRegEx.SIZE
                    + "( regular)?, (Max humongous: )?" + JdkRegEx.SIZE
                    + "( humongous)?, (External )?[fF]rag: \\d{1,3}%( external)?, (Internal frag: )?\\d{1,3}%"
                    + "( internal; Reserve: " + JdkRegEx.SIZE + ", Max: " + JdkRegEx.SIZE + ")?$",
            //
            "^Evacuation Reserve: " + JdkRegEx.SIZE + " \\(\\d{1,3} regions\\), Max regular: " + JdkRegEx.SIZE + "$",
            //
            "^Regions: \\d{1,} x " + JdkRegEx.SIZE + "$",
            //
            "^Humongous object threshold: " + JdkRegEx.SIZE + "$",
            //
            "^Heuristics ergonomically sets (-XX:\\+ExplicitGCInvokesConcurrent|"
                    + "-XX:\\+ShenandoahImplicitGCInvokesConcurrent)$"

    };

    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(_REGEX.length);

    static {
        for (String regex : _REGEX) {
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
