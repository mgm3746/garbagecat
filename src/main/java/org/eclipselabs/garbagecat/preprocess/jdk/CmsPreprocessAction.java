/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * Generall CMS preprocessing.
 * </p>
 *
 * <p>
 * Fix issues with CMS logging.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} combined with {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent}:
 * </p>
 *
 * 46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: [CMS-concurrent-abortable-preclean: 1.427/2.228 secs] [Times: user=1.56 sys=0.01, real=2.23 secs]
 * : 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] [Times: user=0.15 sys=0.01, real=0.04 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 46674.719: [GC (Allocation Failure)46674.719: [ParNew: 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] [Times: user=0.15 sys=0.01, real=0.04 secs]
 * 46674.749: [CMS-concurrent-abortable-preclean: 1.427/2.228 secs] [Times: user=1.56 sys=0.01, real=2.23 secs]
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class CmsPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning PAR_NEW mixed with CMS_CONCURRENT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[GC \\(Allocation Failure\\)" + JdkRegEx.TIMESTAMP + ": \\[ParNew)(" + JdkRegEx.TIMESTAMP
            + ": \\[CMS-concurrent-abortable-preclean: " + JdkRegEx.DURATION_FRACTION + "\\]" + JdkRegEx.TIMES_BLOCK
            + ")[ ]*$";

    /**
     * Regular expression for retained end.
     */
    private static final String REGEX_RETAIN_END = "^: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     *
     * @param priorLogEntry
     *            The prior log line.
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     * @param entangledLogLines
     *            Log lines to be output out of order.
     */
    public CmsPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntr,
            List<String> entangledLogLines) {
        
        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT)) {
            // Par_NEW mixed with CMS_CONCURRENT
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(4) + System.getProperty("line.separator"));
            }
            // Output beginning of PAR_NEW line
            this.logEntry = matcher.group(1);            
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            this.logEntry = logEntry + System.getProperty("line.separator");
            clearEntangledLines(entangledLogLines);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.CMS.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     *
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT) || logLine.matches(REGEX_RETAIN_END);
    }

    /**
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines.size() > 0) {
            // Output any entangled log lines
            Iterator<String> iterator = entangledLogLines.iterator();
            while (iterator.hasNext()) {
                String logLine = iterator.next();        
                this.logEntry = this.logEntry + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }
}