#!/bin/sh
#
# Generates garbage collection logging for various collectors/settings.
#
# Usage: sh ./garbage-maker-jdk8.sh 
#

GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-3.0.4-SNAPSHOT

##### Create GC Logging #####

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old.log -XX:+UseSerialGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1.log -XX:+UseG1GC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah.log -XX:+UseShenandoahGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

##### Analyze GC Logging #####

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms.txt $GARBAGECAT_HOME/jdk8-par-new-cms.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1.txt $GARBAGECAT_HOME/jdk8-g1.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details.txt $GARBAGECAT_HOME/jdk8-g1-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah.txt $GARBAGECAT_HOME/jdk8-shenandoah.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details.txt $GARBAGECAT_HOME/jdk8-shenandoah-details.log
