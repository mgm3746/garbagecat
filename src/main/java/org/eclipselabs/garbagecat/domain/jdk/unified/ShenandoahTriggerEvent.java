/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
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
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * SHENANDOAH_TRIGGER
 * </p>
 * 
 * <p>
 * Trigger information logging.
 * </p>
 * 
 * <h3>Example Logging</h3>
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
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahTriggerEvent extends ShenandoahCollector implements UnifiedLogging, LogEvent, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            // Learning
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] Trigger: Learning \\d of \\d. Free \\("
                    + JdkRegEx.SIZE + "\\) is below initial threshold \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            // Average
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] Trigger: Average GC time \\(" + JdkRegEx.DURATION_JDK9
                    + "\\) is above the time for allocation rate \\(" + JdkRegEx.ALLOCATION_RATE
                    + "\\) to deplete free headroom \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            // Free
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] Trigger: Free \\(" + JdkRegEx.SIZE
                    + "\\) is below minimum threshold \\(" + JdkRegEx.SIZE + "\\)[ ]*$"
            //
    };

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString();
    }

    public long getTimestamp() {
        throw new UnsupportedOperationException("Event does not include timestamp information");
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
