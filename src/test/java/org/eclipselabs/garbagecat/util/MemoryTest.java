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
package org.eclipselabs.garbagecat.util;

import static org.eclipselabs.garbagecat.util.Memory.bytes;
import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
public class MemoryTest {

    @Test
    public void canParse() {
        Memory oneKilobyte = memory("1K");
        assertEquals(oneKilobyte.getValue(KILOBYTES), 1L);
        assertEquals(oneKilobyte.getValue(BYTES), 1024L);
    }

    @Test
    public void parseIsCaseInsensitiv() {
        String string = "8K";
        assertEquals(memory(string.toLowerCase()), memory(string.toUpperCase()));
    }

    @Test
    public void hasToString() {
        assertEquals(memory("32M").toString(), "32M");
        assertEquals(memory("32M").convertTo(KILOBYTES).toString(), "32768K");
    }

    @Test
    public void canEqualOnInstancesUsingDifferentUnits() {
        assertEquals(kilobytes(2048), megabytes(2));
    }

    @Test
    public void canAdd() {
        assertEquals(megabytes(2).plus(kilobytes(1)), kilobytes(2049));
    }

    @Test
    public void canSubtract() {
        assertEquals(megabytes(2).minus(kilobytes(1)), kilobytes(2047));
    }

    @Test
    public void canCompare() {
        assertFalse(bytes(1).greaterThan(bytes(2)));
        assertFalse(bytes(1).greaterThan(bytes(1)));
        assertTrue(bytes(2).greaterThan(bytes(1)));

        assertTrue(bytes(1).lessThan(bytes(2)));
        assertFalse(bytes(1).lessThan(bytes(1)));
        assertFalse(bytes(2).lessThan(bytes(1)));
    }

}
