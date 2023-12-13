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

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * SHENANDOAH_METASPACE
 * </p>
 * 
 * <p>
 * JDK17 Metaspace event printed at the bottom of every Shenandoah gc. Looks the same as
 * <code>UnifiedPreprocessAction.REGEX_RETAIN_MIDDLE_METASPACE_DATA</code> except there is no
 * <code>UnifiedRegEx.GC_EVENT_NUMBER</code>.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) With log level, tags:
 * </p>
 * 
 * <pre>
 * [0.303s][info][gc,metaspace] Metaspace: 3378K(3584K)-&gt;3378K(3584K) NonClass: 3120K(3200K)-&gt;3120K(3200K) Class: 258K(384K)-&gt;258K(384K)
 * </pre>
 *
 * <p>
 * 2) Datestamp only:
 * </p>
 * 
 * <pre>
 * [2022-08-09T17:56:59.141-0400] Metaspace: 3448K(3648K)-&gt;3465K(3648K) NonClass: 3163K(3264K)-&gt;3179K(3264K) Class: 285K(384K)-&gt;285K(384K)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahMetaspaceEvent extends ShenandoahCollector implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^^\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS
            + ")\\])?(\\[info\\]\\[gc,metaspace\\])? Metaspace: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) NonClass: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Class: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)[ ]*$";

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
    public ShenandoahMetaspaceEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString();
    }

    public long getTimestamp() {
        return 0;
    }
}
