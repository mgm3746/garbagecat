package org.eclipselabs.garbagecat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.cli.CommandLine;

public class TestMain extends TestCase {

	public void testShortOptions() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class.forName("java.lang.IllegalArgumentException");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[9];
			args[0] = "-h";
			args[1] = "-o";
			args[2] = "-Xmx2048m";
			args[3] = "-p";
			args[4] = "-s";
			args[5] = "2009-09-18 00:00:08,172";
			args[6] = "-t";
			args[7] = "80";
			// Instead of a file, use a location sure to exist.
			args[8] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			Object o = parseOptions.invoke(null, (Object) args);
			CommandLine cmd = (CommandLine) o;
			Assert.assertNotNull(cmd);
			Assert.assertTrue("'-h' is a valid option", cmd.hasOption("h"));
			Assert.assertTrue("'-o' is a valid option", cmd.hasOption("o"));
			Assert.assertTrue("'-p' is a valid option", cmd.hasOption("p"));
			Assert.assertTrue("'-s' is a valid option", cmd.hasOption("s"));
			Assert.assertTrue("'-t' is a valid option", cmd.hasOption("t"));
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Anything the invoked method throws is wrapped by InvocationTargetException.
			Assert.fail("InvocationTargetException: " + e.getMessage());
		}
	}

	public void testLongOptions() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[9];
			args[0] = "--help";
			args[1] = "--options";
			args[2] = "-Xmx2048m";
			args[3] = "--preprocess";
			args[4] = "--startdatetime";
			args[5] = "2009-09-18 00:00:08,172";
			args[6] = "--threshold";
			args[7] = "80";
			// Instead of a file, use a location sure to exist.
			args[8] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			Object o = parseOptions.invoke(null, (Object) args);
			CommandLine cmd = (CommandLine) o;
			Assert.assertNotNull(cmd);
			Assert.assertTrue("'--help' is a valid option", cmd.hasOption("help"));
			Assert.assertTrue("'--options' is a valid option", cmd.hasOption("options"));
			Assert.assertTrue("'--preprocess' is a valid option", cmd.hasOption("preprocess"));
			Assert
					.assertTrue("'--startdatetime' is a valid option", cmd
							.hasOption("startdatetime"));
			Assert.assertTrue("'--threshold' is a valid option", cmd.hasOption("threshold"));
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Assert.fail("InvocationTargetException: " + e.getMessage());
		}
	}

	public void testInvalidOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[3];
			// Test typo (extra 'h')
			args[0] = "--threshhold";
			args[1] = "80";
			// Instead of a file, use a location sure to exist.
			args[2] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			Object o = parseOptions.invoke(null, (Object) args);
			CommandLine cmd = (CommandLine) o;
			// An nrecognized option throws an <code>UnrecognizedOptionException</code>, which is
			// caught and the usage line output.
			Assert.assertNull("An invalid option was accepted.", cmd);
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Assert.fail("InvocationTargetException: " + e.getMessage());
		}
	}

	public void testInvalidThresholdShortOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[3];
			args[0] = "-t";
			args[1] = "xxx";
			// Instead of a file, use a location sure to exist.
			args[2] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Assert
					.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Anything the invoked method throws is wrapped by InvocationTargetException.
			Assert.assertTrue("Epected IllegalArgumentException not thrown.", e
					.getTargetException() instanceof IllegalArgumentException);
		}
	}

	public void testInvalidThresholdLongOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[3];
			args[0] = "--threshold";
			args[1] = "xxx";
			// Instead of a file, use a location sure to exist.
			args[2] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Assert
					.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Anything the invoked method throws is wrapped by InvocationTargetException.
			Assert.assertTrue("Epected IllegalArgumentException not thrown.", e
					.getTargetException() instanceof IllegalArgumentException);
		}
	}

	public void testInvalidStartDateTimeShortOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[3];
			args[0] = "-s";
			args[1] = "xxx";
			// Instead of a file, use a location sure to exist.
			args[2] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Assert
					.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Anything the invoked method throws is wrapped by InvocationTargetException.
			Assert.assertTrue("Epected IllegalArgumentException not thrown.", e
					.getTargetException() instanceof IllegalArgumentException);
		}
	}

	public void testInvalidStartDateTimeLongOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[3];
			args[0] = "--startdatetime";
			args[1] = "xxx";
			// Instead of a file, use a location sure to exist.
			args[2] = System.getProperty("user.dir");
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Assert
					.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Anything the invoked method throws is wrapped by InvocationTargetException.
			Assert.assertTrue("Epected IllegalArgumentException not thrown.", e
					.getTargetException() instanceof IllegalArgumentException);
		}
	}

	public void testShortHelpOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[1];
			args[0] = "-h";
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Object o = parseOptions.invoke(null, (Object) args);
			CommandLine cmd = (CommandLine) o;
			// CommandLine will be null if only the help option is passed in.
			Assert.assertNull(cmd);
			Assert.assertTrue("'-h' is a valid option", true);
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Assert.fail("InvocationTargetException: " + e.getMessage());
		}
	}

	public void testLongHelpOption() {
		try {
			Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
			Class<?>[] argTypes = new Class[] { String[].class };
			Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
			// Make private method accessible
			parseOptions.setAccessible(true);
			// Method arguments
			String[] args = new String[1];
			args[0] = "--help";
			// Pass null object since parseOptions is static
			parseOptions.invoke(null, (Object) args);
			Object o = parseOptions.invoke(null, (Object) args);
			CommandLine cmd = (CommandLine) o;
			// CommandLine will be null if only the help option is passed in.
			Assert.assertNull(cmd);
			Assert.assertTrue("'--help' is a valid option", true);
		} catch (ClassNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (SecurityException e) {
			Assert.fail("SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Assert.fail("NoSuchMethodException: " + e.getMessage());
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Assert.fail("InvocationTargetException: " + e.getMessage());
		}
	}
}
