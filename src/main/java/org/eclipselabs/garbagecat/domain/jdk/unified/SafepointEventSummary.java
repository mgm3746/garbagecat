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

import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;

/**
 * <code>SafepointEvent</code> summary used for reporting
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class SafepointEventSummary {

    /**
     * The <code>Trigger</code>
     */
    private Trigger trigger;

    /**
     * Total number of events.
     */
    private long count;

    /**
     * Total pause time (milliseconds).
     */
    private long pauseTotal;

    /**
     * Max pause time (milliseconds).
     */
    private int pauseMax;

    /**
     * Default constructor.
     * 
     * @param trigger
     *            The <code>Trigger</code>.
     * @param count
     *            Number of events.
     * @param pauseTotal
     *            Total pause time of events
     * @param pauseMax
     *            Max pause time of events
     */
    public SafepointEventSummary(Trigger trigger, long count, long pauseTotal, int pauseMax) {
        this.trigger = trigger;
        this.count = count;
        this.pauseTotal = pauseTotal;
        this.pauseMax = pauseMax;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public long getCount() {
        return count;
    }

    public long getPauseTotal() {
        return pauseTotal;
    }

    public long getPauseMax() {
        return pauseMax;
    }
}
