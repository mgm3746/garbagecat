/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.Memory;

/**
 * Combined young plus old generation data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface CombinedData {

    /**
     * @return Young + old generation end occupancy (kilobytes).
     */
    Memory getCombinedOccupancyEnd();

    /**
     * @return Young + old generation initial occupancy (kilobytes).
     */
    Memory getCombinedOccupancyInit();

    /**
     * @return Total young + old generation space (kilobytes) at the end of the event (i.e. it reflects any resizing).
     */
    Memory getCombinedSpace();
}
