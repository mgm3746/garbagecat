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
package org.eclipselabs.garbagecat.domain;

import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.APPLICATION_LOGGING;
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.THREAD_DUMP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestApplicationLoggingEvent {

    @Test
    void testReportable() {
        String logLine = "00:02:05,067 INFO  [STDOUT] log4j: setFile ended";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                APPLICATION_LOGGING + " incorrectly indentified as reportable.");
    }

    @Test
    void testHhMmSsError() {
        String logLine = "00:02:05,067 INFO  [STDOUT] log4j: setFile ended";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testHhMmSsInfo() {
        String logLine = "10:58:38,610 ERROR [ContainerBase] Servlet.service() for servlet "
                + "HttpControllerServletXml threw exception java.lang.OutOfMemoryError: Java heap space";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testHhMmSsWarn() {
        String logLine = "11:05:57,018 WARN  [JBossManagedConnectionPool] Throwable while attempting to "
                + "get a new connection: null";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testYyyyMmDd() {
        String logLine = "2010-03-25 17:00:51,581 ERROR [example.com.servlet.DynamoServlet] "
                + "getParameter(message.order) can't access property order in class java.lang.String";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testException() {
        String logLine = "java.sql.SQLException: pingDatabase failed status=-1";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testOracleException() {
        String logLine = "ORA-12514, TNS:listener does not currently know of service requested in connect descriptor";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + APPLICATION_LOGGING + ".");
    }

    @Test
    void testStackTrace() {
        String logLine = "\tat oracle.jdbc.driver.SQLStateMapping.newSQLException(SQLStateMapping.java:70)";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testStackTraceCausedBy() {
        String logLine = "Caused by: java.sql.SQLException: Listener refused the connection with the following error:";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testStackTraceEllipses() {
        String logLine = "\t... 56 more";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossDivider() {
        String logLine = "=========================================================================";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossBootstrapEnvironement() {
        String logLine = "  JBoss Bootstrap Environment";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossHome() {
        String logLine = "  JBOSS_HOME: /opt/jboss/jboss-eap-4.3/jboss-as";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossJava() {
        String logLine = "  JAVA: /opt/java/bin/java";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossClasspath() {
        String logLine = "  CLASSPATH: /opt/jboss/jboss-eap-4.3/jboss-as/bin/run.jar:/opt/java/lib/tools.jar";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }

    @Test
    void testJBossJavaOptsWarning() {
        String logLine = "JAVA_OPTS already set in environment; overriding default settings with values: -d64";
        assertTrue(ApplicationLoggingEvent.match(logLine), "Log line not recognized as " + THREAD_DUMP + ".");
    }
}
