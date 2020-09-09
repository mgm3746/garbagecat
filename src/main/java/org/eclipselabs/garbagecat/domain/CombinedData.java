/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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
 * Combined young plus old generation data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface CombinedData {

    /**
     * @return Young + old generation initial occupancy (kilobytes).
     */
    int getCombinedOccupancyInit();

    /**
     * @return Total young + old generation space (kilobytes) at the end of the event (i.e. it reflects any resizing).
     */
    int getCombinedSpace();

    /**
     * @return Young + old generation end occupancy (kilobytes).
     */
    int getCombinedOccupancyEnd();
}
