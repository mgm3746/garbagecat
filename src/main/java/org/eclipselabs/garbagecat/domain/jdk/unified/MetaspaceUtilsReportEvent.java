/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * METASPACE_UTILS_REPORT
 * </p>
 * 
 * <p>
 * JDK11+
 * </p>
 * 
 * <p>
 * openjdk/src/hotspot/share/memory/metaspace.cpp:void Metaspace::report_metadata_oome(ClassLoaderData* loader_data,
 * size_t word_size, MetaspaceObj::Type type, MetadataType mdtype, TRAPS)
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class MetaspaceUtilsReportEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^\\[(" + JdkRegEx.DATESTAMP + "|" + UnifiedRegEx.UPTIME + "|"
            + UnifiedRegEx.UPTIMEMILLIS + ")\\](\\[(" + UnifiedRegEx.UPTIME + "|" + UnifiedRegEx.UPTIMEMILLIS
            + ")\\])?[ ]+(Both|CDS|Chunk freelists|Class( space)?|CompressedClassSpaceSize|"
            + "(Current|Initial) GC threshold|MaxMetaspaceSize|Non-[cC]lass( space)?|Usage|Virtual space):.*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PATTERN.matcher(logLine).matches();
    }

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.METASPACE_UTILS_REPORT.toString();
    }

    public long getTimestamp() {
        throw new UnsupportedOperationException("Event does not include timestamp information");
    }
}
