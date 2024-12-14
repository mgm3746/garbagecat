#!/bin/sh
#
# Generates garbage collection logging for JDK17 collectors for all gc tags (gc*) and safepoint info level with standard decorators.
#
# Usage: sh ./garbage-maker-jdk17.sh 
#

JAVA_HOME=/etc/alternatives/java_sdk_17_openjdk
GARBAGECAT_HOME=../../../target
GARBAGECAT_VERSION=-5.0.1-SNAPSHOT

##### Create GC Logging #####

# default decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-serial-new-serial-old.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-g1.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-shenandoah.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-z.log::filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# uptime (seconds after JVM start) decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-serial-new-serial-old-uptime.log:uptime:filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-uptime.log:uptime:filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-g1-uptime.log:uptime:filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-shenandoah-uptime.log:uptime:filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-z-uptime.log:uptime:filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# time (datestamp) decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-serial-new-serial-old-time.log:time:filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-time.log:time:filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-g1-time.log:time:filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-shenandoah-time.log:time:filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk17-z-time.log:time:filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

##### Analyze GC Logging #####

# default decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-serial-new-serial-old.txt $GARBAGECAT_HOME/jdk17-serial-new-serial-old.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting.txt $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-g1.txt $GARBAGECAT_HOME/jdk17-g1.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-shenandoah.txt $GARBAGECAT_HOME/jdk17-shenandoah.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-z.txt $GARBAGECAT_HOME/jdk17-z.log

# uptime (seconds after JVM start) decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-serial-new-serial-old-uptime.txt $GARBAGECAT_HOME/jdk17-serial-new-serial-old-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-uptime.txt $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-g1-uptime.txt $GARBAGECAT_HOME/jdk17-g1-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-shenandoah-uptime.txt $GARBAGECAT_HOME/jdk17-shenandoah-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-z-uptime.txt $GARBAGECAT_HOME/jdk17-z-uptime.log

# time (datestamp) decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-serial-new-serial-old-time.txt $GARBAGECAT_HOME/jdk17-serial-new-serial-old-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-time.txt $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-g1-time.txt $GARBAGECAT_HOME/jdk17-g1-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-shenandoah-time.txt $GARBAGECAT_HOME/jdk17-shenandoah-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-z-time.txt $GARBAGECAT_HOME/jdk17-z-time.log
