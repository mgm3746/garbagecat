package org.eclipselabs.garbagecat;

import java.io.File;
import java.net.URISyntaxException;

public final class TestUtil {
	
	private TestUtil() {
		super();
	}

	public static File getFile(String name) {
		try {
			return new File(TestUtil.class.getClassLoader().getResource("data/" + name).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
