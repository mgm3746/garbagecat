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
package org.eclipselabs.garbagecat.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;

/**
 * Common garbage collection utility methods and constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcUtil {

    /**
     * <p>
     * Regular expression for valid JVM start date/time in yyyy-MM-dd HH:mm:ss,SSS format (see
     * <code>SimpleDateFormat</code> for date and time pattern definitions).
     * </p>
     * 
     * For example:
     * 
     * <pre>
     * 2009-09-18 00:00:08,172
     * </pre>
     */
    public static final String START_DATE_TIME_REGEX = "^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}),"
            + "(\\d{3})$";

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private GcUtil() {

    }

    /**
     * Check to see if the entered startdatetime is a valid format.
     * 
     * @param startDateTime
     *            The startdatetime <code>String</code>.
     * @return true if a valid format, false otherwise.
     */
    public static final boolean isValidStartDateTime(String startDateTime) {
        return startDateTime.matches(START_DATE_TIME_REGEX);
    }

    /**
     * Convert startdatetime <code>String</code> to a <code>Date</code>.
     * 
     * @param startDateTime
     *            The startdatetime <code>String</code> in <code>START_DATE_TIME_REGEX</code> format.
     * @return the startdatetime <code>Date</code>.
     */
    public static final Date parseStartDateTime(String startDateTime) {
        Date date = null;
        Pattern pattern = Pattern.compile(START_DATE_TIME_REGEX);
        Matcher matcher = pattern.matcher(startDateTime);
        if (matcher.find()) {
            date = getDate(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5),
                    matcher.group(6), matcher.group(7));
        }
        return date;
    }

    /**
     * Convert datestamp <code>String</code> to a <code>Date</code>.
     * 
     * @param datestamp
     *            The datestamp <code>String</code> in <code>JdkRegEx.DATESTAMP</code> format.
     * @return the datestamp in <code>Date</code> format.
     */
    public static final Date parseDateStamp(String datestamp) {
        Date date = null;
        Pattern pattern = Pattern.compile(JdkRegEx.DATESTAMP);
        Matcher matcher = pattern.matcher(datestamp);
        if (matcher.find()) {
            date = getDate(matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6),
                    matcher.group(7), matcher.group(8));
        }
        return date;
    }

    /**
     * Convert date parts to a <code>Date</code>.
     * 
     * @param yyyy
     *            The year.
     * @param MM
     *            The month.
     * @param dd
     *            The day.
     * @param HH
     *            The hour.
     * @param mm
     *            The minute.
     * @param ss
     *            The seconds.
     * @param SSS
     *            The milliseconds.
     * @return The date part strings converted to a <code>Date</code>
     */
    private static final Date getDate(String yyyy, String MM, String dd, String HH, String mm, String ss, String SSS) {
        Calendar calendar = Calendar.getInstance();
        if (yyyy == null || MM == null || dd == null || HH == null || mm == null || ss == null || SSS == null) {
            throw new IllegalArgumentException("One or more date parts are missing.");
        }
        calendar.set(Calendar.YEAR, Integer.valueOf(yyyy));
        calendar.set(Calendar.MONTH, Integer.valueOf(MM).intValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dd).intValue());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(HH).intValue());
        calendar.set(Calendar.MINUTE, Integer.valueOf(mm).intValue());
        calendar.set(Calendar.SECOND, Integer.valueOf(ss).intValue());
        calendar.set(Calendar.MILLISECOND, Integer.valueOf(SSS).intValue());
        return calendar.getTime();
    }

    /**
     * Add milliseconds to a given <code>Date</code>.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param timestamp
     *            Time interval in milliseconds.
     * @return start <code>Date</code> + timestamp.
     */
    public static final Date getDatePlusTimestamp(Date start, long timestamp) {
        long millis = start.getTime() + timestamp;
        return new Date(millis);
    }

    /**
     * Calculate the number of whole days (24 hour periods) for a given number of milliseconds
     * 
     * @param timestamp
     *            Time in milliseconds.
     * @return the number of whole days.
     */
    public static final int daysInMilliSeconds(long timestamp) {
        BigDecimal days = new BigDecimal(timestamp);
        return days.divideToIntegralValue(new BigDecimal(1000 * 60 * 60 * 24)).intValue();
    }

    /**
     * Determine whether the first JVM event timestamp indicates a partial log file or events that were not in a
     * recognizable format.
     * 
     * @param firstTimestamp
     *            The first JVM event timestamp (milliseconds).
     * @return True if the first timestamp is within the first timestamp threshold, false otherwise.
     */
    public static final boolean isPartialLog(long firstTimestamp) {
        return (firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000);
    }

    /**
     * Retrieve the value for a given property file and key.
     * 
     * @param propertyFile
     *            The property file.
     * @param key
     *            The property key.
     * @return The value for the given property file and key.
     */
    public static final String getPropertyValue(String propertyFile, String key) {
        ResourceBundle rb = ResourceBundle.getBundle("META-INF." + propertyFile);
        return rb.getString(key);
    }

    /**
     * Calculate the number of milliseconds between two dates.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param end
     *            End <code>Date</code>.
     * @return The interval between two dates in milliseconds.
     */
    public static final long dateDiff(Date start, Date end) {
        return end.getTime() - start.getTime();
    }
}
