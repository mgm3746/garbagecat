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
package org.eclipselabs.garbagecat.util.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedSafepoint {

    @Test
    public void testTriggerIdentity() {
        UnifiedSafepoint.Trigger[] triggers = UnifiedSafepoint.Trigger.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(UnifiedSafepoint.Trigger.UNKNOWN)) {
                assertFalse(
                        UnifiedSafepoint.identifyTrigger(triggers[i].name()).equals(UnifiedSafepoint.Trigger.UNKNOWN),
                        triggers[i].name() + " not identified.");
            }
        }
    }

    @Test
    public void testTriggerLiteral() {
        UnifiedSafepoint.Trigger[] triggers = UnifiedSafepoint.Trigger.values();
        for (int i = 0; i < triggers.length; i++) {
            if (!triggers[i].equals(UnifiedSafepoint.Trigger.UNKNOWN)) {
                try {
                    UnifiedSafepoint.getTriggerLiteral(triggers[i]);
                } catch (AssertionError e) {
                    fail(triggers[i].name() + " literal not found.");
                }
            }
        }
    }
}
