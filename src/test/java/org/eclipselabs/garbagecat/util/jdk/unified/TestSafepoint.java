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
package org.eclipselabs.garbagecat.util.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestSafepoint {

    @Test
    public void testTriggerIdentity() {
        Safepoint.Trigger[] triggers = Safepoint.Trigger.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(Safepoint.Trigger.UNKNOWN)) {
                assertFalse(Safepoint.identifyTrigger(triggers[i].name()).equals(Safepoint.Trigger.UNKNOWN),
                        triggers[i].name() + " not identified.");
            }
        }
    }

    @Test
    public void testTriggerLiteral() {
        Safepoint.Trigger[] triggers = Safepoint.Trigger.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(Safepoint.Trigger.UNKNOWN)) {
                try {
                    Safepoint.getTriggerLiteral(triggers[i]);
                } catch (AssertionError e) {
                    fail(triggers[i].name() + " literal not found.");
                }
            }
        }
    }
}
