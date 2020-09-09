/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * HEADER_MEMORY
 * </p>
 * 
 * <p>
 * Memory header.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * Memory: 4k page, physical 65806300k(58281908k free), swap 16777212k(16777212k free)
 * </pre>
 * 
 * <p>
 * 2) Without swap data:
 * </p>
 * 
 * <pre>
 * Memory: 8k page, physical 535035904k(398522432k free)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderMemoryEvent implements LogEvent {

    /**
     * Regular expression for memory size.
     */
    private static final String SIZE = "(\\d{1,9})k";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^Memory: (4|8)k page, physical " + SIZE + "\\(" + SIZE + " free\\)(, swap "
            + SIZE + "\\(" + SIZE + " free\\))?$";

    private static Pattern pattern = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Physical memory (kilobytes).
     */
    private int physicalMemory;

    /**
     * Physical memory free (kilobytes).
     */
    private int physicalMemoryFree;

    /**
     * Swap size (kilobytes).
     */
    private int swap;

    /**
     * Swap free (kilobytes).
     */
    private int swapFree;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public HeaderMemoryEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            physicalMemory = Integer.parseInt(matcher.group(2));
            physicalMemoryFree = Integer.parseInt(matcher.group(3));
            if (matcher.group(4) != null) {
                swap = Integer.parseInt(matcher.group(5));
                swapFree = Integer.parseInt(matcher.group(6));
            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEADER_MEMORY.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public int getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    public int getSwap() {
        return swap;
    }

    public int getSwapFree() {
        return swapFree;
    }

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
}
