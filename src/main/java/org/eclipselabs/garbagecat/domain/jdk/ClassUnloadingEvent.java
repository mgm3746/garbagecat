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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CLASS_UNLOADING
 * </p>
 * 
 * <p>
 * Perm gen/Metasapce collection "Unloading class" logging enabled with <code>-XX:+TraceClassUnloading</code>. The perm
 * gen is collected at the beginning of some old collections, resulting in the perm gen / metaspace logging being
 * intermingled with the old collection logging. This data is currently not being used for analysis.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * 65.343: [Full GC[Unloading class $Proxy111]
 * [Unloading class $Proxy225]
 * [Unloading class $Proxy481]
 * [Unloading class $Proxy245]
 *  [PSYoungGen: 32064K-&gt;0K(819840K)] [PSOldGen: 355405K-&gt;387085K(699072K)] 387470K-&gt;387085K(1518912K) [PSPermGen: 115215K-&gt;115215K(238912K)], 1.5692400 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 65.343: [Full GC [PSYoungGen: 32064K-&gt;0K(819840K)] [PSOldGen: 355405K-&gt;387085K(699072K)] 387470K-&gt;387085K(1518912K) [PSPermGen: 115215K-&gt;115215K(238912K)], 1.5692400 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ClassUnloadingEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^( )?" + JdkRegEx.UNLOADING_CLASS_BLOCK + "(.*)$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ClassUnloadingEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.CLASS_UNLOADING.toString();
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
        return PATTERN.matcher(logLine).matches();
    }
}
