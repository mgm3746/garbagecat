/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.eclipselabs.garbagecat.util.Constants;

import junit.framework.Assert;
import junit.framework.TestCase;

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
            String[] args = new String[12];
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
            // Instead of a file, use a location sure to exist.
            args[11] = System.getProperty("user.dir");
            // Pass null object since parseOptions is static
            Object o = parseOptions.invoke(null, (Object) args);
            CommandLine cmd = (CommandLine) o;
            Assert.assertNotNull(cmd);
            Assert.assertTrue("'-" + Constants.OPTION_HELP_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_HELP_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_JVMOPTIONS_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_JVMOPTIONS_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_PREPROCESS_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_PREPROCESS_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_STARTDATETIME_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_STARTDATETIME_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_THRESHOLD_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_THRESHOLD_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_REORDER_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_REORDER_SHORT));
            Assert.assertTrue("'-" + Constants.OPTION_OUTPUT_SHORT + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_OUTPUT_SHORT));
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
            String[] args = new String[12];
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
            // Instead of a file, use a location sure to exist.
            args[11] = System.getProperty("user.dir");
            // Pass null object since parseOptions is static
            Object o = parseOptions.invoke(null, (Object) args);
            CommandLine cmd = (CommandLine) o;
            Assert.assertNotNull(cmd);
            Assert.assertTrue("'-" + Constants.OPTION_HELP_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_HELP_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_JVMOPTIONS_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_JVMOPTIONS_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_PREPROCESS_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_PREPROCESS_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_STARTDATETIME_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_THRESHOLD_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_THRESHOLD_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_REORDER_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_REORDER_LONG));
            Assert.assertTrue("'-" + Constants.OPTION_OUTPUT_LONG + "' is a valid option",
                    cmd.hasOption(Constants.OPTION_OUTPUT_LONG));
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
            // An unrecognized option throws an <code>UnrecognizedOptionException</code>, which is
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
            // Anything the invoked method throws is wrapped by InvocationTargetException.
            Assert.assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
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
            Assert.fail("Should have raised an InvocationTargetException with an underlying PareseException");
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
            Assert.assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
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
            Assert.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
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
            Assert.assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
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
            Assert.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
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
            Assert.assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
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
            Assert.fail("Should have raised an InvocationTargetException with an underlying IllegalArgumentException");
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
            Assert.assertTrue("Epected ParseException not thrown.", e.getTargetException() instanceof ParseException);
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
