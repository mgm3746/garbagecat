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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * OOME_METASPACE
 * </p>
 * 
 * <h2>Example Logging</h2>
 *
 * <p>
 * 1) Class:
 * </p>
 *
 * <pre>
 *[2024-05-06T13:01:18.988+0300][3619401490ms] Metaspace (class) allocation failed for size 1459
 * </pre>
 *
 * <p>
 * 2) Data:
 * </p>
 *
 * <pre>
 *[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class OomeMetaspaceEvent implements UnifiedLogging, ThrowAwayEvent {
    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR
            + "[ ]+(Metaspace \\((class|data)\\) allocation failed for size \\d{1,}|"
            + "(- (commit_granule_bytes|commit_granule_words|enlarge_chunks_in_place|handle_deallocations|"
            + "new_chunks_are_fully_committed|uncommit_free_chunks|use_allocation_guard|"
            + "virtual_space_node_default_size)|Both|CDS|Chunk freelists|Class( space)?|CompressedClassSpaceSize|"
            + "Current GC threshold|Initial GC threshold|Internal statistics|MaxMetaspaceSize|MetaspaceReclaimPolicy|"
            + "Non-[cC]lass( space)?|num_(allocs_failed_limit|arena_births|arena_deaths|chunk_merges|chunk_splits|"
            + "chunks_enlarged|chunks_returned_to_freelist|chunks_taken_from_freelist|inconsistent_stats|purges|"
            + "space_committed|space_uncommitted|vsnodes_births|vsnodes_deaths)|" + "Usage|Virtual space):.*)$";
    private static final Pattern PATTERN = Pattern.compile(_REGEX);

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
    public OomeMetaspaceEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.OOME_METASPACE.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return 0;
    }

    @Override
    public boolean isEndstamp() {
        return false;
    }
}
