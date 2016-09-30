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
 * General CMS preprocessing.
 * </p>
 *
 * <p>
 * Fix issues with CMS logging.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} combined with
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent}:
 * </p>
 *
 * <pre>
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
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} combined with
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} without trigger:
 * </p>
 *
 * <pre>
 * 10.963: [GC10.963: [ParNew10.977: [CMS-concurrent-abortable-preclean: 0.088/0.197 secs] [Times: user=0.33 sys=0.05, real=0.20 secs]
 * : 115327K->12800K(115328K), 0.0155930 secs] 349452K->251716K(404548K), 0.0156840 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 10.963: [GC10.963: [ParNew: 115327K->12800K(115328K), 0.0155930 secs] 349452K->251716K(404548K), 0.0156840 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
 * 10.977: [CMS-concurrent-abortable-preclean: 0.088/0.197 secs] [Times: user=0.33 sys=0.05, real=0.20 secs]
 * </pre>
 * 
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent} across 2 lines:
 * </p>
 * 
 * <pre>
 * 44.684: [Full GC44.684: [CMS44.877: [CMS-concurrent-mark: 1.508/2.428 secs] [Times: user=3.44 sys=0.49, real=2.42 secs]
 *  (concurrent mode failure): 1218548K->413373K(1465840K), 1.3656970 secs] 1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] [Times: user=1.33 sys=0.01, real=1.37 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 44.684: [Full GC44.684: [CMS (concurrent mode failure): 1218548K->413373K(1465840K), 1.3656970 secs] 1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] [Times: user=1.33 sys=0.01, real=1.37 secs]
 * 44.877: [CMS-concurrent-mark: 1.508/2.428 secs] [Times: user=3.44 sys=0.49, real=2.42 secs]
 * 
 * <p>
 * 4) {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} combined with {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} with trigger and space after trigger:
 * </p>
 *
 * <pre>
 * 45.574: [GC (Allocation Failure) 45.574: [ParNew45.670: [CMS-concurrent-abortable-preclean: 3.276/4.979 secs] [Times: user=7.75 sys=0.28, real=4.98 secs]
 * : 619008K->36352K(619008K), 0.2165661 secs] 854952K->363754K(4157952K), 0.2168066 secs] [Times: user=0.30 sys=0.00, real=0.22 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 45.574: [GC (Allocation Failure) 45.574: [ParNew: 619008K->36352K(619008K), 0.2165661 secs] 854952K->363754K(4157952K), 0.2168066 secs] [Times: user=0.30 sys=0.00, real=0.22 secs]
 * 45.670: [CMS-concurrent-abortable-preclean: 3.276/4.979 secs] [Times: user=7.75 sys=0.28, real=4.98 secs]
 * </pre>
 * 
 * <p>
 * 5) JDK 8 {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent} combined with
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} across 2 lines:
 * </p>
 * 
 * <pre>
 * 706.707: [Full GC (Allocation Failure) 706.708: [CMS709.137: [CMS-concurrent-mark: 3.381/5.028 secs] [Times: user=23.92 sys=3.02, real=5.03 secs]
 *  (concurrent mode failure): 2655937K->2373842K(2658304K), 11.6746550 secs] 3973407K->2373842K(4040704K), [Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] [Times: user=14.05 sys=0.02,
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 706.707: [Full GC (Allocation Failure) 706.708: [CMS (concurrent mode failure): 2655937K->2373842K(2658304K), 11.6746550 secs] 3973407K->2373842K(4040704K), [Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] [Times: user=14.05 sys=0.02, real=11.68 secs]
 * 709.137: [CMS-concurrent-mark: 3.381/5.028 secs] [Times: user=23.92 sys=3.02, real=5.03 secs]
 * </pre>
 * 
 * <p>
 * 6) JDK 8 {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailureEvent} combined with
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} across 2 lines:
 * </p>
 * 
 * <pre>
 * 719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), 0.0000470 secs]719.521: [CMS722.601: [CMS-concurrent-mark: 3.567/3.633 secs] [Times: user=10.91 sys=0.69, real=3.63 secs]
 *  (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] 3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] [Times: user=15.38 sys=0.02, real=12.35 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), 0.0000470 secs] (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] 3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] [Times: user=15.38 sys=0.02, real=12.35 secs]
 * 719.521: [CMS722.601: [CMS-concurrent-mark: 3.567/3.633 secs] [Times: user=10.91 sys=0.69, real=3.63 secs]
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class CmsPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning PAR_NEW mixed with CMS_CONCURRENT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC( \\("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\))?( )?" + JdkRegEx.TIMESTAMP + ": \\[ParNew( \\("
            + JdkRegEx.TRIGGER_PROMOTION_FAILED + "\\))?(: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[CMS)?)("
            + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-(abortable-preclean|mark): " + JdkRegEx.DURATION_FRACTION
            + "\\]" + JdkRegEx.TIMES_BLOCK + "?)[ ]*$";

    /**
     * Regular expression for retained beginning CMS_SERIAL_OLD mixed with CMS_CONCURRENT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_SERIAL_CONCURRENT = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[Full GC( )?(\\(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\) )?" + JdkRegEx.TIMESTAMP
            + ": \\[CMS)(" + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-(mark|abortable-preclean): "
            + JdkRegEx.DURATION_FRACTION + "\\]" + JdkRegEx.TIMES_BLOCK + "?)[ ]*$";

    /**
     * Regular expression for retained beginning CMS_SERIAL_OLD bailing outT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_SERIAL_BAILING = "^" + JdkRegEx.TIMESTAMP + ": \\[Full GC "
            + JdkRegEx.TIMESTAMP + ": \\[CMSbailing out to foreground collection[ ]*$";

    /**
     * Regular expression for retained beginning PAR_NEW bailing outT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_PARNEW_BAILING = "^" + JdkRegEx.TIMESTAMP + ": \\[GC "
            + JdkRegEx.TIMESTAMP + ": \\[ParNew( \\(" + JdkRegEx.TRIGGER_PROMOTION_FAILED + "\\))?: " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[CMS(Java HotSpot\\(TM\\) Server VM warning: )?bailing out to foreground collection[ ]*$";

    /**
     * Middle line when logging is split over 3 lines (e.g. bailing).
     */
    private static final String REGEX_RETAIN_MIDDLE_CONCURRENT = "^" + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-mark: "
            + JdkRegEx.DURATION_FRACTION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * Regular expression for retained end.
     */
    private static final String REGEX_RETAIN_END = "^( \\(" + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "\\))?( \\("
            + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "\\)\\[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE
            + "\\)\\]" + JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[class unloading, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION + "\\])?: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)(, \\[(CMS Perm |Metaspace): " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])?" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION
            + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

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
    public CmsPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines) {

        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT)) {
            // Par_NEW mixed with CMS_CONCURRENT
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(13) + System.getProperty("line.separator"));
            }
            // Output beginning of PAR_NEW line
            this.logEntry = matcher.group(1);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_SERIAL_CONCURRENT)) {
            // CMS_SERIAL_OLD mixed with CMS_CONCURRENT
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_SERIAL_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(6) + System.getProperty("line.separator"));
            }
            // Output beginning of CMS_SERIAL_OLD line
            this.logEntry = matcher.group(1);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_SERIAL_BAILING)
                || logEntry.matches(REGEX_RETAIN_BEGINNING_PARNEW_BAILING)) {
            // do nothing
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_CONCURRENT)) {
            // Save line to output at the end
            entangledLogLines.add(logEntry);
            if (priorLogEntry.matches(REGEX_RETAIN_BEGINNING_SERIAL_BAILING)
                    || priorLogEntry.matches(REGEX_RETAIN_BEGINNING_PARNEW_BAILING)) {
                this.logEntry = priorLogEntry;

            }
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
     * @param nextLogLine
     *            The next log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, String priorLogLine, String nextLogLine) {
        return (logLine.matches(REGEX_RETAIN_BEGINNING_PARNEW_CONCURRENT) && nextLogLine.matches(REGEX_RETAIN_END))
                || logLine.matches(REGEX_RETAIN_BEGINNING_SERIAL_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_BEGINNING_SERIAL_BAILING)
                || logLine.matches(REGEX_RETAIN_BEGINNING_PARNEW_BAILING)
                || logLine.matches(REGEX_RETAIN_MIDDLE_CONCURRENT) || logLine.matches(REGEX_RETAIN_END);
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