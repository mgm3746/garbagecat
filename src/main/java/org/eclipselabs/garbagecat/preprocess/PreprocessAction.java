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
package org.eclipselabs.garbagecat.preprocess;

import java.util.List;

import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;

/**
 * Base preprocessing action: (1) Separate entangled logging. (2) Condense multiple lines to a single line.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface PreprocessAction {

    /**
     * Defined events identified during preprocessing that are used for analysis (e.g. <code>ThrowAwayEvent</code>s).
     */
    public enum PreprocessEvent {
        APPLICATION_CONCURRENT_TIME, APPLICATION_LOGGING, CLASS_HISTOGRAM, CLASS_UNLOADING, FLS_STATISTICS, HEAP_AT_GC,
        //
        OOME_METASPACE, REFERENCE_GC, TENURING_DISTRIBUTION, Z_STATS
    }

    /**
     * Indicates that the log entry should be output on a new line (e.g. a single line event or the beginning of an
     * event that spans multiple lines). If the previous log entry did not end with a newline, one will need to be
     * appended before outputting the current entry.
     */
    public static final String NEWLINE = "NEWLINE";

    /**
     * <code>PreprocessActionType</code> context identifier.
     */
    public static final String PREPROCESS_ACTION_TYPE = "PREPROCESS_ACTION_TYPE";

    /**
     * @param entangledLogLines
     *            Prior log lines that were output out of order.
     * @param logEntry
     *            The current preprocessed logging line.
     * @return The logEntry with any entangledLogLines appended.
     */
    public static String clearEntangledLines(List<String> entangledLogLines, String logEntry) {
        StringBuffer logEntries = new StringBuffer(logEntry);
        if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
            // Output any entangled log lines
            for (String logLine : entangledLogLines) {
                logEntries.append(Constants.LINE_SEPARATOR);
                logEntries.append(logLine);
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
        return logEntries.toString();
    }

    /**
     * @return The log entry for the action.
     */
    String getLogEntry();

    /**
     * @return The action type identifier.
     */
    PreprocessActionType getType();
}
