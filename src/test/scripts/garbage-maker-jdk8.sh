#!/bin/sh
#
# Generates garbage collection logging for various collectors/settings.
#
# Usage: sh ./garbage-maker-jdk8.sh 
#

GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-3.0.6-SNAPSHOT

##### Create GC Logging #####

# datestamp: #

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.log -XX:+UseSerialGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-date.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-date.log -XX:+UseG1GC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-date.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-date.log -XX:+UseShenandoahGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-date.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# timestamp: #

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.log -XX:+UseSerialGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-time.log -XX:+UseG1GC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-time.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-time.log -XX:+UseShenandoahGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-time.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# datestamp: timestamp: #

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.log -XX:+UseSerialGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.log -XX:+UseSerialGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.log -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.log -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.log -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.log -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-date-time.log -XX:+UseG1GC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-g1-details-date-time.log -XX:+UseG1GC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms1m -Xmx96m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-date-time.log -XX:+UseShenandoahGC -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

java -Xms8m -Xmx128m -verbose:gc -Xloggc:$GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.log -XX:+UseShenandoahGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

##### Analyze GC Logging #####

# datestamp: #

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-date.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-date.txt $GARBAGECAT_HOME/jdk8-g1-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-date.txt $GARBAGECAT_HOME/jdk8-g1-details-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-date.txt $GARBAGECAT_HOME/jdk8-shenandoah-date.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-date.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-date.log

# timestamp: #

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-time.txt $GARBAGECAT_HOME/jdk8-g1-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-time.txt $GARBAGECAT_HOME/jdk8-g1-details-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-time.logjava -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-datestamp.log

# datestamp: timestamp: #

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-serial-new-serial-old-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-serial-old-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.txt $GARBAGECAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-parallel-serial-old-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o $GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.txt $GARBAGECAT_HOME/jdk8-par-new-cms-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-date-time.txt $GARBAGECAT_HOME/jdk8-g1-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-g1-details-date-time.txt $GARBAGECAT_HOME/jdk8-g1-details-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-date-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-date-time.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -o $GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.txt $GARBAGECAT_HOME/jdk8-shenandoah-details-date-time.log
