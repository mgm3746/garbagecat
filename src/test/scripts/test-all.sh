#!/bin/sh
#
# Test all JDKs/collectors with standard recommended (production) gc logging events.
# Use the datestamp decorator for non-unified logging and the "default" decorator for unified logging.
# "time" is actually the recommended (production) unified decorator, but "default" is best for garbagecat troubleshooting.
#
# Usage: sh ./test-all.sh
#

GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-4.0.2-SNAPSHOT

##### JDK8 #####

# Create GC Logging

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-test.log -XX:+UseSerialGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-test.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-test.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-test.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-test.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-test.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-test.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-test.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-test.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-test.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-test.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-test.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-test.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-cms-details-test.log -XX:-UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-test.log -XX:+UseG1GC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-test.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-test.log -XX:+UseShenandoahGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-test.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-test.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-test.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-test.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-test.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-test.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-test.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-test.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-test.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-test.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-test.txt $GARBAGECAT_HOME/jdk8-par-new-cms-foreground-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-cms-test.txt $GARBAGECAT_HOME/jdk8-par-new-cms-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-test.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-serial-new-cms-test.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o $GARBAGECAT_HOME/jdk8-serial-new-cms-details-test.txt $GARBAGECAT_HOME/jdk8-serial-new-cms-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk8-g1-test.txt $GARBAGECAT_HOME/jdk8-g1-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk8-g1-details-test.txt $GARBAGECAT_HOME/jdk8-g1-details-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk8-shenandoah-test.txt $GARBAGECAT_HOME/jdk8-shenandoah-test.log

/etc/alternatives/java_sdk_1.8.0_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk8-shenandoah-details-test.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-test.log

##### JDK11 #####

# Create GC Logging

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-serial-new-serial-old-test.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-test.log::filesize=50M -XX:+UseParallelGC -XX:-UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-test.log::filesize=50M -XX:+UseParallelGC -XX:+UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-par-new-cms-test.log::filesize=50M -XX:+UseConcMarkSweepGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-g1-test.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-shenandoah-test.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-serial-new-serial-old-test.txt $GARBAGECAT_HOME/jdk11-serial-new-serial-old-test.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-test.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-test.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-test.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-test.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-par-new-cms-test.txt $GARBAGECAT_HOME/jdk11-par-new-cms-test.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-g1-test.txt $GARBAGECAT_HOME/jdk11-g1-test.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-shenandoah-test.txt $GARBAGECAT_HOME/jdk11-shenandoah-test.log

##### JDK17 #####

# Create GC Logging

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-serial-new-serial-old-test.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-test.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-g1-test.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-shenandoah-test.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-z-test.log::filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-serial-new-serial-old-test.txt $GARBAGECAT_HOME/jdk17-serial-new-serial-old-test.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-test.txt $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-test.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-g1-test.txt $GARBAGECAT_HOME/jdk17-g1-test.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-shenandoah-test.txt $GARBAGECAT_HOME/jdk17-shenandoah-test.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-z-test.txt $GARBAGECAT_HOME/jdk17-z-test.log


##### JDK21 #####

# Create GC Logging

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-serial-new-serial-old-test.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-test.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-g1-test.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-shenandoah-test.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-z-nongen-test.log::filesize=50M -XX:+UseZGC -XX:-ZGenerational -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+heap=info,gc+heap+exit=info,gc+init=info,gc+metaspace=info,gc+start=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-z-gen-test.log::filesize=50M -XX:+UseZGC -XX:+ZGenerational -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-serial-new-serial-old-test.txt $GARBAGECAT_HOME/jdk21-serial-new-serial-old-test.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-test.txt $GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-test.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-g1-test.txt $GARBAGECAT_HOME/jdk21-g1-test.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-shenandoah-test.txt $GARBAGECAT_HOME/jdk21-shenandoah-test.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-z-nongen-test.txt $GARBAGECAT_HOME/jdk21-z-nongen-test.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-z-gen-test.txt $GARBAGECAT_HOME/jdk21-z-gen-test.log