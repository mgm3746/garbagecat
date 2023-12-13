/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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
 * SHENANDOAH_TRIGGER
 * </p>
 * 
 * <p>
 * Trigger information logging. Broken out from {@link org.eclipselabs.garbagecat.domain.jdk.GcInfoEvent} for possible
 * future analysis.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK8:
 * </p>
 * 
 * <pre>
 * Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)
 * </pre>
 * 
 * <pre>
 * Trigger: Average GC time (12.56 ms) is above the time for allocation rate (899 MB/s) to deplete free headroom (11466K)
 * </pre>
 * 
 * <p>
 * 2) Unified:
 * </p>
 * 
 * <pre>
 * [0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)
 * </pre>
 * 
 * <pre>
 * [0.814s][info][gc] Trigger: Average GC time (18.86 ms) is above the time for allocation rate (328.75 MB/s) to deplete free headroom (5M)
 * </pre>
 * 
 * <pre>
 * [24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)
 * </pre>
 * 
 * <p>
 * 3) JDK17:
 * </p>
 * 
 * <pre>
 * [10.508s][info][gc          ] Trigger: Average GC time (16.09 ms) is above the time for average allocation rate (409 MB/s) to deplete free headroom (5742K) (margin of error = 1.80)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahTriggerEvent extends ShenandoahCollector implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            // Learning
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Learning \\d{1,} of \\d{1,}. Free \\(" + JdkRegEx.SIZE
                    + "\\) is below initial threshold \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            // Average
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Average GC time \\(" + JdkRegEx.DURATION_MS
                    + "\\) is above the time for( (average|instantaneous))? allocation rate \\("
                    + JdkRegEx.ALLOCATION_RATE + "\\) to deplete free headroom \\(" + JdkRegEx.SIZE
                    + "\\)( \\((margin of error|spike threshold) = \\d{1,}\\.\\d{2}\\))?[ ]*$",
            // Free
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Free \\(" + JdkRegEx.SIZE
                    + "\\) is below minimum threshold \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            // Time
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Time since last GC \\(\\d{1,} ms\\) is larger "
                    + "than guaranteed interval \\(\\d{1,} ms\\)[ ]*$",
            // Allocation Failure
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Handle Allocation Failure[ ]*$",
            // Metadata GC Threshold
            "^(" + UnifiedRegEx.DECORATOR + " )?Trigger: Metadata GC Threshold[ ]*$"
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

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString();
    }

    public long getTimestamp() {
        return 0;
    }
}
