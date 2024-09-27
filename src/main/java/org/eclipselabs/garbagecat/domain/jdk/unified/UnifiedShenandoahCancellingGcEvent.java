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

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * SHENANDOAH_CANCELLING_GC
 * </p>
 * 
 * <p>
 * Cancelling GC information logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Unified:
 * </p>
 * 
 * <pre>
 * [72.659s][info][gc] Cancelling GC: Stopping VM
 * </pre>
 * 
 * <p>
 * 2) JDK8:
 * </p>
 * 
 * <pre>
 * Cancelling GC: Stopping VM
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedShenandoahCancellingGcEvent extends ShenandoahCollector implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^(" + UnifiedRegEx.DECORATOR + " )?Cancelling GC: Stopping VM[ ]*$";

    private static Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedShenandoahCancellingGcEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString();
    }

    public long getTimestamp() {
        return 0;
    }
}
