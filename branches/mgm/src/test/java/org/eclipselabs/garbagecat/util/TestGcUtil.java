/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcUtil extends TestCase {

    public void testStartDateTime() {
        String startDateTime = "2009-09-18 00:00:08,172";
        Assert.assertTrue("Start date/time not recognized as a valid format.", GcUtil.isValidStartDateTime(startDateTime));
    }

    public void testInvalidStartDateTime() {
        // Replace comma with space
        String startDateTime = "2009-09-18 00:00:08 172";
        Assert.assertFalse("Start date/time recognized as a valid format.", GcUtil.isValidStartDateTime(startDateTime));
    }

    public void testConvertStartDateTimeStringToDate() {
        String startDateTime = "2009-09-18 16:24:08,172";
        Date date = GcUtil.parseStartDateTime(startDateTime);
        Assert.assertNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Assert.assertEquals("Start year not parsed correctly.", 2009, calendar.get(Calendar.YEAR));
        Assert.assertEquals("Start month not parsed correctly.", 8, calendar.get(Calendar.MONTH));
        Assert.assertEquals("Start day not parsed correctly.", 18, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals("Start hour not parsed correctly.", 16, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals("Start minute not parsed correctly.", 24, calendar.get(Calendar.MINUTE));
        Assert.assertEquals("Start second not parsed correctly.", 8, calendar.get(Calendar.SECOND));
        Assert.assertEquals("Start millisecond not parsed correctly.", 172, calendar.get(Calendar.MILLISECOND));
    }

    public void testNumberOfDaysInZeroMilliSeconds() {
        long milliSeconds = 0;
        Assert.assertEquals("Number of days calculated wrong.", 0, GcUtil.daysInMilliSeconds(milliSeconds));
    }

    public void testNumberOfDaysInMilliSecondsLessThanOneDay() {
        long milliSeconds = 82800000L;
        Assert.assertEquals("Number of days calculated wrong.", 0, GcUtil.daysInMilliSeconds(milliSeconds));
    }

    public void testNumberOfDaysInMilliSecondsEqualOneDay() {
        long milliSeconds = 86400000L;
        Assert.assertEquals("Number of days calculated wrong.", 1, GcUtil.daysInMilliSeconds(milliSeconds));
    }

    public void testNumberOfDaysInMilliSeconds9Days() {
        long milliSeconds = 863999999L;
        Assert.assertEquals("Number of days calculated wrong.", 9, GcUtil.daysInMilliSeconds(milliSeconds));
    }

    public void testAddingDateAndTimestampZero() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 0L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-18 19:21:44,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp10Ms() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 10L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-18 19:21:44,022", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp1Sec() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 1000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-18 19:21:45,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp1Min() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 60000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-18 19:22:44,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp1Hr() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 3600000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-18 20:21:44,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp1Day() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 86400000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-08-19 19:21:44,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateAndTimestamp30Days() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 2592000000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "1966-09-17 19:21:44,012", formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)));
    }

    public void testAddingDateWith2DigitMonth() {
        String jvmStarted = "2009-11-01 02:30:52,917";
        long gcLogTimestamp = 353647157L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        Assert.assertEquals("Date calculated wrong.", "2009-11-05 04:45:00,074", formatter.format(GcUtil.getDatePlusTimestamp(GcUtil.parseStartDateTime(jvmStarted), gcLogTimestamp)));
    }

    public void testGetPropertyValues() {
        Assert.assertNotNull("Could not retrieve stack_thread_size.not_set.", GcUtil.getPropertyValue("analysis", "thread.stack.size.not.set"));
        Assert.assertNotNull("Could not retrieve min.heap.not.equal.max.heap.", GcUtil.getPropertyValue("analysis", "min.heap.not.equal.max.heap"));
    }

    public void testConvertDateStampStringToDate() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        Date date = GcUtil.parseDateStamp(datestamp);
        Assert.assertNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Assert.assertEquals("Datestamp year not parsed correctly.", 2010, calendar.get(Calendar.YEAR));
        Assert.assertEquals("Datestamp month not parsed correctly.", 1, calendar.get(Calendar.MONTH));
        Assert.assertEquals("Datestamp day not parsed correctly.", 26, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals("Datestamp hour not parsed correctly.", 9, calendar.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals("Datestamp minute not parsed correctly.", 32, calendar.get(Calendar.MINUTE));
        Assert.assertEquals("Datestamp second not parsed correctly.", 12, calendar.get(Calendar.SECOND));
        Assert.assertEquals("Datestamp millisecond not parsed correctly.", 486, calendar.get(Calendar.MILLISECOND));
    }

    public void testDateDiff() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 27);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);
        Date end = calendar.getTime();

        Assert.assertEquals("Date difference incorrect.", 90061001L, GcUtil.dateDiff(start, end));
    }

    public void testPartialLog() {
        Assert.assertFalse("Not a partial log.", GcUtil.isPartialLog(59999));
        Assert.assertTrue("Is a partial log.", GcUtil.isPartialLog(60001));
    }
}
