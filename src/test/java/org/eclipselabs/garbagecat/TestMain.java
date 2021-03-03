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
package org.eclipselabs.garbagecat;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.util.Constants;



public class TestMain {

	@Test
    public void testShortOptions() throws Exception {
        Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
		Class.forName("java.lang.IllegalArgumentException");
		Class<?>[] argTypes = new Class[] { String[].class };
		Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
		// Make private method accessible
		parseOptions.setAccessible(true);
		// Method arguments
		String[] args = new String[14];
		args[0] = "-h";
		args[1] = "-j";
		args[2] = "-Xmx2048m";
		args[3] = "-p";
		args[4] = "-s";
		args[5] = "2009-09-18 00:00:08,172";
		args[6] = "-t";
		args[7] = "80";
		args[8] = "-r";
		args[9] = "-o";
		args[10] = "12345678.txt";
		args[11] = "-v";
		args[12] = "-l";
		// Instead of a file, use a location sure to exist.
		args[13] = System.getProperty("user.dir");
		// Pass null object since parseOptions is static
		Object o = parseOptions.invoke(null, (Object) args);
		CommandLine cmd = (CommandLine) o;
		assertNotNull(cmd);
		assertTrue("'-" + Constants.OPTION_HELP_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_HELP_SHORT));
		assertTrue("'-" + Constants.OPTION_JVMOPTIONS_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_JVMOPTIONS_SHORT));
		assertTrue("'-" + Constants.OPTION_PREPROCESS_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_PREPROCESS_SHORT));
		assertTrue("'-" + Constants.OPTION_STARTDATETIME_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_STARTDATETIME_SHORT));
		assertTrue("'-" + Constants.OPTION_THRESHOLD_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_THRESHOLD_SHORT));
		assertTrue("'-" + Constants.OPTION_REORDER_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_REORDER_SHORT));
		assertTrue("'-" + Constants.OPTION_OUTPUT_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_OUTPUT_SHORT));
		assertTrue("'-" + Constants.OPTION_VERSION_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_VERSION_SHORT));
		assertTrue("'-" + Constants.OPTION_LATEST_VERSION_SHORT + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_LATEST_VERSION_SHORT));
    }

   @Test
    public void testLongOptions() throws Exception {
        Class<?> c = Class.forName("org.eclipselabs.garbagecat.Main");
		Class<?>[] argTypes = new Class[] { String[].class };
		Method parseOptions = c.getDeclaredMethod("parseOptions", argTypes);
		// Make private method accessible
		parseOptions.setAccessible(true);
		// Method arguments
		String[] args = new String[14];
		args[0] = "--help";
		args[1] = "--jvmoptions";
		args[2] = "-Xmx2048m";
		args[3] = "--preprocess";
		args[4] = "--startdatetime";
		args[5] = "2009-09-18 00:00:08,172";
		args[6] = "--threshold";
		args[7] = "80";
		args[8] = "--reorder";
		args[9] = "--output";
		args[10] = "12345678.txt";
		args[11] = "--version";
		args[12] = "--latest";
		// Instead of a file, use a location sure to exist.
		args[13] = System.getProperty("user.dir");
		// Pass null object since parseOptions is static
		Object o = parseOptions.invoke(null, (Object) args);
		CommandLine cmd = (CommandLine) o;
		assertNotNull(cmd);
		assertTrue("'-" + Constants.OPTION_HELP_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_HELP_LONG));
		assertTrue("'-" + Constants.OPTION_JVMOPTIONS_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_JVMOPTIONS_LONG));
		assertTrue("'-" + Constants.OPTION_PREPROCESS_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_PREPROCESS_LONG));
		assertTrue("'-" + Constants.OPTION_STARTDATETIME_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG));
		assertTrue("'-" + Constants.OPTION_THRESHOLD_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_THRESHOLD_LONG));
		assertTrue("'-" + Constants.OPTION_REORDER_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_REORDER_LONG));
		assertTrue("'-" + Constants.OPTION_OUTPUT_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_OUTPUT_LONG));
		assertTrue("'-" + Constants.OPTION_VERSION_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_VERSION_LONG));
		assertTrue("'-" + Constants.OPTION_LATEST_VERSION_LONG + "' is a valid option",
		        cmd.hasOption(Constants.OPTION_LATEST_VERSION_LONG));
    }

    @Test
    public void testInvalidOption() throws Exception {
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
            // An unrecognized option throws an <code>UnrecognizedOptionException</code>, which is
            // caught and the usage line output.
            assertNull("An invalid option was accepted.", cmd);
        } catch (InvocationTargetException e) {
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
        }
    }

    @Test
    public void testInvalidThresholdShortOption() throws Exception {
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
            fail("Should have raised an InvocationTargetException with an underlying PareseException");
        } catch (InvocationTargetException e) {
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
        }
    }

    @Test
    public void testInvalidThresholdLongOption() throws Exception {
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
            fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
        } catch (InvocationTargetException e) {
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
        }
    }

   @Test
    public void testInvalidStartDateTimeShortOption() throws Exception {
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
            fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
        } catch (InvocationTargetException e) {
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
        }
    }

    @Test
    public void testInvalidStartDateTimeLongOption() throws Exception {
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
            fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
        } catch (InvocationTargetException e) {
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
        }
    }

    @Test
    public void testShortHelpOption() throws Exception {
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
		assertNull(cmd);
		assertTrue("'-h' is a valid option", true);
    }

    @Test
    public void testLongHelpOption() throws Exception {
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
		assertNull(cmd);
		assertTrue("'--help' is a valid option", true);
    }
}
