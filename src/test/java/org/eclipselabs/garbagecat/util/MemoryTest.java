package org.eclipselabs.garbagecat.util;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class MemoryTest {

	@Test
	public void canParse() {
		Memory oneKilobyte = memory("1K");
		assertThat(oneKilobyte.getValue(KILOBYTES), is(equalTo(1L)));
		assertThat(oneKilobyte.getValue(BYTES), equalTo(1024L));
	}

	@Test
	public void canCompareTwoInstances() {
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

}
