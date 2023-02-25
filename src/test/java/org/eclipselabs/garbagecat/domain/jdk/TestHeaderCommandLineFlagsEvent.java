/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestHeaderCommandLineFlagsEvent {

    @Test
    void testJBossHeader() {
        String logLine = "  JAVA_OPTS: -Dprogram.name=run.sh -d64 -server -Xms10000m -Xmx10000m -ss512k "
                + "-XX:PermSize=512m -XX:MaxPermSize=512m -XX:NewSize=3000m -XX:MaxNewSize=3000m -XX:SurvivorRatio=6 "
                + "-XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=5 -verbose:gc -XX:+PrintGC -XX:+PrintGCDetails "
                + "-XX:+PrintGCDateStamps -XX:-TraceClassUnloading -XX:+UseConcMarkSweepGC -XX:+UseParNewGC "
                + "-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 "
                + "-XX:+UseCMSInitiatingOccupancyOnly -XX:ParallelGCThreads=8 -XX:+CMSScavengeBeforeRemark "
                + "-Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false "
                + "-Djava.net.preferIPv4Stack=true -Djboss.platform.mbeanserver "
                + "-Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl "
                + "-XX:ErrorFile=/opt/jboss/jboss-eap-4.3/jboss-as/server/edreams/log/crash/hs_err_pid%p.log "
                + "-XX:+DisableExplicitGC "
                + "-Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 "
                + "-Dsun.lang.ClassLoader.allowArraySyntax=true -DrobotsEngine=register "
                + "-DconfigurationEngine.configDir=/opt/odigeo/properties/ "
                + "-Djavax.net.ssl.keyStore=/opt/edreams/keys/java/keyStore "
                + "-Djavax.net.ssl.keyStorePassword=changeit";
        assertTrue(HeaderCommandLineFlagsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + ".");
        HeaderCommandLineFlagsEvent event = new HeaderCommandLineFlagsEvent(logLine);
        String jvmOptions = "-Dprogram.name=run.sh -d64 -server -Xms10000m -Xmx10000m -ss512k "
                + "-XX:PermSize=512m -XX:MaxPermSize=512m -XX:NewSize=3000m -XX:MaxNewSize=3000m -XX:SurvivorRatio=6 "
                + "-XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=5 -verbose:gc -XX:+PrintGC -XX:+PrintGCDetails "
                + "-XX:+PrintGCDateStamps -XX:-TraceClassUnloading -XX:+UseConcMarkSweepGC -XX:+UseParNewGC "
                + "-XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 "
                + "-XX:+UseCMSInitiatingOccupancyOnly -XX:ParallelGCThreads=8 -XX:+CMSScavengeBeforeRemark "
                + "-Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false "
                + "-Djava.net.preferIPv4Stack=true -Djboss.platform.mbeanserver "
                + "-Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl "
                + "-XX:ErrorFile=/opt/jboss/jboss-eap-4.3/jboss-as/server/edreams/log/crash/hs_err_pid%p.log "
                + "-XX:+DisableExplicitGC "
                + "-Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 "
                + "-Dsun.lang.ClassLoader.allowArraySyntax=true -DrobotsEngine=register "
                + "-DconfigurationEngine.configDir=/opt/odigeo/properties/ "
                + "-Djavax.net.ssl.keyStore=/opt/edreams/keys/java/keyStore "
                + "-Djavax.net.ssl.keyStorePassword=changeit";
        assertEquals(jvmOptions, event.getJvmOptions(), "Flags not parsed correctly.");
    }

    @Test
    void testLine() {
        String logLine = "CommandLine flags: -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=75 "
                + "-XX:+CMSScavengeBeforeRemark -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses "
                + "-XX:GCLogFileSize=8388608 -XX:InitialHeapSize=13958643712 -XX:MaxHeapSize=13958643712 "
                + "-XX:MaxPermSize=402653184 -XX:MaxTenuringThreshold=6 -XX:NewRatio=2 -XX:NumberOfGCLogFiles=8 "
                + "-XX:OldPLABSize=16 -XX:PermSize=402653184 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails "
                + "-XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseGCLogFileRotation "
                + "-XX:+UseParNewGC";
        assertTrue(HeaderCommandLineFlagsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + ".");
        HeaderCommandLineFlagsEvent event = new HeaderCommandLineFlagsEvent(logLine);
        String jvmOptions = "-XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=75 "
                + "-XX:+CMSScavengeBeforeRemark -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses "
                + "-XX:GCLogFileSize=8388608 -XX:InitialHeapSize=13958643712 -XX:MaxHeapSize=13958643712 "
                + "-XX:MaxPermSize=402653184 -XX:MaxTenuringThreshold=6 -XX:NewRatio=2 -XX:NumberOfGCLogFiles=8 "
                + "-XX:OldPLABSize=16 -XX:PermSize=402653184 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails "
                + "-XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseGCLogFileRotation "
                + "-XX:+UseParNewGC";
        assertEquals(jvmOptions, event.getJvmOptions(), "Flags not parsed correctly.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "CommandLine flags: -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=75 "
                + "-XX:+CMSScavengeBeforeRemark -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses "
                + "-XX:GCLogFileSize=8388608 -XX:InitialHeapSize=13958643712 -XX:MaxHeapSize=13958643712 "
                + "-XX:MaxPermSize=402653184 -XX:MaxTenuringThreshold=6 -XX:NewRatio=2 -XX:NumberOfGCLogFiles=8 "
                + "-XX:OldPLABSize=16 -XX:PermSize=402653184 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails "
                + "-XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseGCLogFileRotation "
                + "-XX:+UseParNewGC";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " incorrectly indentified as blocking.");
    }
}
