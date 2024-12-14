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
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * Base logging event.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface LogEvent {

    /**
     * @return The event type identifier.
     */
    EventType getEventType();

    /**
     * @return The log entry for the event.
     */
    String getLogEntry();

    /**
     * @return The time when the event begins, in milliseconds after JVM startup or from the arbitrary point in time
     *         {@link org.eclipselabs.garbagecat.util.GcUtil#JVM_START_DATE}, if the JVM startup time is unknown.
     */
    long getTimestamp();
}
