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
package org.eclipselabs.garbagecat.domain;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationLoggingEvent {

    @Test
    public void testReportable() {
        String logLine = "00:02:05,067 INFO  [STDOUT] log4j: setFile ended";
        assertFalse(
                JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHhMmSsError() {
        String logLine = "00:02:05,067 INFO  [STDOUT] log4j: setFile ended";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testHhMmSsInfo() {
        String logLine = "10:58:38,610 ERROR [ContainerBase] Servlet.service() for servlet "
                + "HttpControllerServletXml threw exception java.lang.OutOfMemoryError: Java heap space";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testHhMmSsWarn() {
        String logLine = "11:05:57,018 WARN  [JBossManagedConnectionPool] Throwable while attempting to "
                + "get a new connection: null";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testYyyyMmDd() {
        String logLine = "2010-03-25 17:00:51,581 ERROR [example.com.servlet.DynamoServlet] "
                + "getParameter(message.order) can't access property order in class java.lang.String";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testException() {
        String logLine = "java.sql.SQLException: pingDatabase failed status=-1";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testOracleException() {
        String logLine = "ORA-12514, TNS:listener does not currently know of service requested in connect descriptor";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_LOGGING.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testStackTrace() {
        String logLine = "\tat oracle.jdbc.driver.SQLStateMapping.newSQLException(SQLStateMapping.java:70)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testStackTraceCausedBy() {
        String logLine = "Caused by: java.sql.SQLException: Listener refused the connection with the following error:";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testStackTraceEllipses() {
        String logLine = "\t... 56 more";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossDivider() {
        String logLine = "=========================================================================";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossBootstrapEnvironement() {
        String logLine = "  JBoss Bootstrap Environment";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossHome() {
        String logLine = "  JBOSS_HOME: /opt/jboss/jboss-eap-4.3/jboss-as";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossJava() {
        String logLine = "  JAVA: /opt/java/bin/java";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossClasspath() {
        String logLine = "  CLASSPATH: /opt/jboss/jboss-eap-4.3/jboss-as/bin/run.jar:/opt/java/lib/tools.jar";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }

    @Test
    public void testJBossJavaOptsWarning() {
        String logLine = "JAVA_OPTS already set in environment; overriding default settings with values: -d64";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".",
                ApplicationLoggingEvent.match(logLine));
    }
}
