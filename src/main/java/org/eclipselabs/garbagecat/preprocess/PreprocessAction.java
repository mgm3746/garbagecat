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

/**
 * Base preprocessing action: (1) Separate entangled logging. (2) Condense multiple lines to a single line.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface PreprocessAction {

    /**
     * Defined preprocessing events.
     */
    public enum PreprocessEvent {
        REFERENCE_GC
    }

    /**
     * Indicates that the log entry should be output on a new line (e.g. a single line event or the beginning of an
     * event that spans multiple lines). If the previous log entry did not end with a newline, one will need to be
     * appended before outputting the current entry.
     */
    public static final String NEWLINE = "NEWLINE";

    /**
     * @return The log entry for the action.
     */
    String getLogEntry();

    /**
     * @return The action identifier.
     */
    String getName();
}
