#!/bin/sh
#
# Generates garbage collection logging for various collectors/settings..
#
# Usage: sh ./garbage-maker-jdk8.sh 
#

GARBAGECAT_HOME=~/workspace/garbagecat/target/
GARBAGECAT_VERSION=-2.0.13-SNAPSHOT

##### Create GC Logging #####

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/serial-new-serial-old-no-details.log -XX:+UseSerialGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/serial-new-serial-old-details.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-no-details.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-details.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-no-details.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-details.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/par-new-parallel-serial-old-no-details.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/par-new-parallel-serial-old-details.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/par-new-cms-no-details.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/par-new-cms-details.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/g1-no-details.log -XX:+UseG1GC -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/g1-details.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar ./gc.log

##### Analyze GC Logging #####

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/serial-new-serial-old-no-details.txt $GARBAGECAT_HOME/serial-new-serial-old-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/serial-new-serial-old-details.txt $GARBAGECAT_HOME/serial-new-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-no-details.txt $GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-details.txt $GARBAGECAT_HOME/parallel-scavenge-parallel-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-no-details.txt $GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-details.txt $GARBAGECAT_HOME/parallel-scavenge-parallel-old-compacting-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/par-new-parallel-serial-old-no-details.txt $GARBAGECAT_HOME/par-new-parallel-serial-old-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/par-new-parallel-serial-old-details.txt $GARBAGECAT_HOME/par-new-parallel-serial-old-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/par-new-cms-no-details.txt $GARBAGECAT_HOME/par-new-cms-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/par-new-cms-details.txt $GARBAGECAT_HOME/par-new-cms-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/g1-no-details.txt $GARBAGECAT_HOME/g1-no-details.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/g1-details.txt $GARBAGECAT_HOME/g1-details.log
