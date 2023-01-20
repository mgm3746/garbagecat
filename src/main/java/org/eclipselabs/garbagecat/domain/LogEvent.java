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
package org.eclipselabs.garbagecat.domain;

/**
 * Base logging event.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface LogEvent {

    /**
     * @return The log entry for the event.
     */
    String getLogEntry();

    /**
     * TODO: Change to LogEventType.
     * 
     * @return The event identifier.
     */
    String getName();

    /**
     * @return The time when the event begins, in milliseconds after JVM startup.
     */
    long getTimestamp();
}
