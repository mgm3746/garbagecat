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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * UNLOADING_CLASS
 * </p>
 * 
 * <p>
 * Remove perm gen collection "Unloading class" logging. The perm gen is collected at the beginning of some old
 * collections, resulting in the perm gen logging being intermingled with the old collection logging. For example:
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 830048.804: [Full GC 830048.804: [CMS[Unloading class sun.reflect.GeneratedConstructorAccessor73]
 * [Unloading class sun.reflect.GeneratedConstructorAccessor70]
 * : 1572185K-&gt;1070163K(1572864K), 6.8812400 secs] 2489689K-&gt;1070163K(2490368K), [CMS Perm : 46357K-&gt;46348K(77352K)], 6.8821630 secs] [Times: user=6.87 sys=0.00, real=6.88 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 830048.804: [Full GC 830048.804: [CMS: 1572185K-&gt;1070163K(1572864K), 6.8812400 secs] 2489689K-&gt;1070163K(2490368K), [CMS Perm : 46357K-&gt;46348K(77352K)], 6.8821630 secs] [Times: user=6.87 sys=0.00, real=6.88 secs]
 * </pre>
 * 
 * TODO: Replace this with ParallelSerialOldPreprocessorAction and make UnloadingClassEvent a throwaway event.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ParallelSerialOldPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX_BEGINNING_UNLOADING_CLASS = "^(" + JdkRegEx.TIMESTAMP + ": \\[Full GC)"
            + JdkRegEx.UNLOADING_CLASS_BLOCK + "(.*)$";

    /**
     * Regular expression for retained end.
     * 
     * [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] 387470K->387085K(1518912K) [PSPermGen:
     * 115215K->115215K(238912K)], 1.5692400 secs]
     */
    private static final String REGEX_RETAIN_END = "^( \\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\] \\[PSOldGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) \\[PSPermGen: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION
            + "\\])( )?$";

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create Unloading class event from log entry.
     * 
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     * @param context
     *            Information to make preprocessing decisions.
     */
    public ParallelSerialOldPreprocessAction(String logEntry, String nextLogEntry, Set<String> context) {

        // Beginning logging
        if (logEntry.matches(REGEX_BEGINNING_UNLOADING_CLASS)) {
            Pattern pattern = Pattern.compile(REGEX_BEGINNING_UNLOADING_CLASS);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX_BEGINNING_UNLOADING_CLASS) || logLine.matches(REGEX_RETAIN_END);
    }
}
