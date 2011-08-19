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
 * Permanent generation data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface PermData {

    /**
     * @return Perm generation initial occupancy (kilobytes).
     */
    int getPermOccupancyInit();

    /**
     * @return Total perm generation space (kilobytes) at the end of the event (i.e. it reflects any resizing).
     */
    int getPermSpace();

    /**
     * @return Perm generation end occupancy (kilobytes).
     */
    int getPermOccupancyEnd();
}
