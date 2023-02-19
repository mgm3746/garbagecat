#!/bin/sh
#
# Generates garbage collection logging for various collectors/settings.
#
# Usage: sh ./garbage-maker-jdk8.sh 
#

JAVA_HOME=/etc/alternatives/java_sdk_1.8.0_openjdk
GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-4.0.1-SNAPSHOT

##### Create GC Logging #####

# datestamp: #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.log -XX:+UseSerialGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-date.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-details-date.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-date.log -XX:+UseG1GC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-date.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-date.log -XX:+UseShenandoahGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-date.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# timestamp: #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.log -XX:+UseSerialGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-time.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-details-time.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-time.log -XX:+UseG1GC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-time.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-time.log -XX:+UseShenandoahGC -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-time.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:-PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# datestamp: timestamp: #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.log -XX:+UseSerialGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-date-time.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-details-date-time.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-date-time.log -XX:+UseG1GC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-date-time.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-date-time.log -XX:+UseShenandoahGC -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

##### Analyze GC Logging #####

# datestamp: #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-date.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-details-date.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-date.txt $GARBAGECAT_HOME/jdk8-g1-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-date.txt $GARBAGECAT_HOME/jdk8-g1-details-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-date.txt $GARBAGECAT_HOME/jdk8-shenandoah-date.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-date.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-date.log

# timestamp: #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-time.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-details-time.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-time.txt $GARBAGECAT_HOME/jdk8-g1-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-time.txt $GARBAGECAT_HOME/jdk8-g1-details-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-time.log$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-time.log

# datestamp: timestamp: #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-cms-details-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-date-time.txt $GARBAGECAT_HOME/jdk8-g1-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-date-time.txt $GARBAGECAT_HOME/jdk8-g1-details-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-date-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-date-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.log
