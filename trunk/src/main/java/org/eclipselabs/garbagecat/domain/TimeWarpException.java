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
package org.eclipselabs.garbagecat.domain;

/**
 * Exception when the {@link org.eclipselabs.garbagecat.domain.LogEvent} chronology is not possible. For example, a
 * {@link org.eclipselabs.garbagecat.domain.BlockingEvent} that starts before a previous
 * {@link org.eclipselabs.garbagecat.domain.BlockingEvent} finishes.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
@SuppressWarnings("serial")
public class TimeWarpException extends RuntimeException {

    public TimeWarpException(String string) {
        super(string);
    }

}
