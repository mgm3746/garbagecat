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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
public class MemoryTest {

    @Test
    public void canParse() {
        Memory oneKilobyte = memory("1K");
        assertThat(oneKilobyte.getValue(KILOBYTES), is(equalTo(1L)));
        assertThat(oneKilobyte.getValue(BYTES), is(equalTo(1024L)));
    }

    @Test
    public void parseIsCaseInsensitiv() {
        String string = "8K";
        assertThat(memory(string.toLowerCase()), is(equalTo(memory(string.toUpperCase()))));
    }

    @Test
    public void hasToString() {
        assertThat(memory("32M").toString(), is(equalTo("32M")));
        assertThat(memory("32M").convertTo(KILOBYTES).toString(), is(equalTo("32768K")));
    }

    @Test
    public void canEqualOnInstancesUsingDifferentUnits() {
        assertThat(kilobytes(2048), is(equalTo(megabytes(2))));
    }

    @Test
    public void canAdd() {
        assertThat(megabytes(2).plus(kilobytes(1)), is(equalTo(kilobytes(2049))));
    }

    @Test
    public void canSubtract() {
        assertThat(megabytes(2).minus(kilobytes(1)), is(equalTo(kilobytes(2047))));
    }

    @Test
    public void canCompare() {
        assertThat(bytes(1).greaterThan(bytes(2)), is(false));
        assertThat(bytes(1).greaterThan(bytes(1)), is(false));
        assertThat(bytes(2).greaterThan(bytes(1)), is(true));

        assertThat(bytes(1).lessThan(bytes(2)), is(true));
        assertThat(bytes(1).lessThan(bytes(1)), is(false));
        assertThat(bytes(2).lessThan(bytes(1)), is(false));
    }

}
