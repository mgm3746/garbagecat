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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_HEADER_VERSION
 * </p>
 * 
 * <p>
 * <code>UnifiedHeaderEvent</code> with JDK version information.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeaderVersionEvent extends UnifiedHeaderEvent {

    private static Pattern pattern = Pattern.compile(UnifiedHeaderVersionEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Version: " + UnifiedRegEx.RELEASE_STRING
            + " \\(release\\)[ ]*$";

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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedHeaderVersionEvent(String logEntry) {
        super(logEntry);
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    super.setTimestamp(Long.parseLong(matcher.group(12)));
                } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                    super.setTimestamp(JdkMath.convertSecsToMillis(matcher.group(11)).longValue());
                } else {
                    if (matcher.group(14) != null) {
                        if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            super.setTimestamp(Long.parseLong(matcher.group(16)));
                        } else {
                            super.setTimestamp(JdkMath.convertSecsToMillis(matcher.group(15)).longValue());
                        }
                    } else {
                        // Datestamp only.
                        super.setTimestamp(JdkUtil.convertDatestampToMillis(matcher.group(1)));
                    }
                }
            }
        }
    }

    /**
     * The Java release string. For example:
     * 
     * <pre>
     * 1.8.0_332-b09-1
     * 11.0.15+9-LTS-1
     * 17.0.3+6-LTS-2
     * </pre>
     * 
     * @return The Java release string.
     */
    public String getJdkReleaseString() {
        String jdkReleaseString = null;
        Matcher matcher = pattern.matcher(super.getLogEntry());
        if (matcher.find()) {
            jdkReleaseString = matcher.group(DECORATOR_SIZE + 1);
        }
        return jdkReleaseString;
    }

    /**
     * @return The JDK version (e.g. '8'), or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined. Not available in unified logging (JDK11+).
     */
    public int getJdkVersionMajor() {
        int jdkVersionMajor = org.github.joa.domain.JvmContext.UNKNOWN;
        if (super.getLogEntry() != null) {
            Matcher matcher = pattern.matcher(super.getLogEntry());
            if (matcher.find()) {
                int index = DECORATOR_SIZE + 2;
                if (matcher.group(index) != null) {
                    if (matcher.group(index).equals("1.6.0")) {
                        jdkVersionMajor = 6;
                    } else if (matcher.group(index).equals("1.7.0")) {
                        jdkVersionMajor = 7;
                    } else if (matcher.group(index).equals("1.8.0")) {
                        jdkVersionMajor = 8;
                    } else {
                        jdkVersionMajor = Integer.parseInt(matcher.group(index));
                    }
                }
            }
        }
        return jdkVersionMajor;
    }
    
    /**
     * @return The JDK update (e.g. '60'), or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined.
     */
    public int getJdkVersionMinor() {
        int jdkVersionMinor = org.github.joa.domain.JvmContext.UNKNOWN;
        if (super.getLogEntry() != null) {
            Matcher matcher = pattern.matcher(super.getLogEntry());
            if (matcher.find()) {
                int index = DECORATOR_SIZE + 3;
                if (matcher.group(index) != null) {
                    jdkVersionMinor = Integer.parseInt(matcher.group(index));
                }
            }
        }
        return jdkVersionMinor;
    }

    public String getLogEntry() {
        return super.getLogEntry();
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString();
    }

    public long getTimestamp() {
        return super.getTimestamp();
    }
}
