#!/bin/sh
#
# Generates garbage collection logging for various collectors/settings.
#
# Usage: sh ./garbage-maker-jdk11.sh 
#

GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-3.0.1-SNAPSHOT

##### Create GC Logging #####

java -Xms1m -Xmx64m -verbose:gc -Xlog:gc*:file=$GARBAGECAT_HOME/jdk11-serial-new-serial-old.log -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xlog:gc*:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old.log -XX:+UseParallelGC -XX:-UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xlog:gc*:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting.log -XX:+UseParallelGC -XX:+UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xlog:gc*:file=$GARBAGECAT_HOME/jdk11-par-new-cms.log -XX:+UseConcMarkSweepGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

java -Xms1m -Xmx64m -verbose:gc -Xlog:gc*:file=$GARBAGECAT_HOME/jdk11-g1.log -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

##### Analyze GC Logging #####

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-serial-new-serial-old.txt $GARBAGECAT_HOME/jdk11-serial-new-serial-old.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p  -t 50 -o $GARBAGECAT_HOME/jdk11-par-new-cms.txt $GARBAGECAT_HOME/jdk11-par-new-cms.log

java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-g1.txt $GARBAGECAT_HOME/jdk11-g1.log
