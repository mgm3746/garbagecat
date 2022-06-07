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
 * JDK17 Metaspace event printed at the bottom of every Shenandoah gc.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [0.303s][info][gc,metaspace] Metaspace: 3378K(3584K)-&gt;3378K(3584K) NonClass: 3120K(3200K)-&gt;3120K(3200K) Class: 258K(384K)-&gt;258K(384K)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahMetaspaceEvent extends ShenandoahCollector implements ThrowAwayEvent {

    private static Pattern pattern = Pattern.compile(ShenandoahMetaspaceEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^^\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS
            + ")\\])?\\[info\\]\\[gc,metaspace\\] Metaspace: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) NonClass: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Class: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)[ ]*$";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
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
        throw new UnsupportedOperationException("Event does not include timestamp information");
    }
}
