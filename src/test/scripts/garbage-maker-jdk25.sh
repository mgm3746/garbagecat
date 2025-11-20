#!/bin/sh
#
# Generates garbage collection logging for JDK25 collectors for all gc tags (gc*) and safepoint info level with standard decorators.
#
# Usage: sh ./garbage-maker-jdk25.sh 
#

JAVA_HOME=/usr/lib/jvm/java-25-openjdk
GARBAGECAT_HOME=../../../target
GARBAGECAT_VERSION=-5.0.2-SNAPSHOT

##### Create GC Logging #####

# default decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-serial-new-serial-old.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-g1.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-shenandoah.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-z.log::filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# uptime (seconds after JVM start) decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-serial-new-serial-old-uptime.log:uptime:filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-uptime.log:uptime:filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-g1-uptime.log:uptime:filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-shenandoah-uptime.log:uptime:filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-z-uptime.log:uptime:filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# time (datestamp) decorator #

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-serial-new-serial-old-time.log:time:filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx64m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-time.log:time:filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms1m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-g1-time.log:time:filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-shenandoah-time.log:time:filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

$JAVA_HOME/bin/java -Xms32m -Xmx96m -Xlog:gc*,safepoint=info:file=$GARBAGECAT_HOME/jdk25-z-time.log:time:filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

##### Analyze GC Logging #####

# default decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-serial-new-serial-old.txt $GARBAGECAT_HOME/jdk25-serial-new-serial-old.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting.txt $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-g1.txt $GARBAGECAT_HOME/jdk25-g1.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-shenandoah.txt $GARBAGECAT_HOME/jdk25-shenandoah.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-z.txt $GARBAGECAT_HOME/jdk25-z.log

# uptime (seconds after JVM start) decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-serial-new-serial-old-uptime.txt $GARBAGECAT_HOME/jdk25-serial-new-serial-old-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-uptime.txt $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-g1-uptime.txt $GARBAGECAT_HOME/jdk25-g1-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-shenandoah-uptime.txt $GARBAGECAT_HOME/jdk25-shenandoah-uptime.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-z-uptime.txt $GARBAGECAT_HOME/jdk25-z-uptime.log

# time (datestamp) decorator #

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-serial-new-serial-old-time.txt $GARBAGECAT_HOME/jdk25-serial-new-serial-old-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-time.txt $GARBAGECAT_HOME/jdk25-parallel-scavenge-parallel-old-compacting-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-g1-time.txt $GARBAGECAT_HOME/jdk25-g1-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-shenandoah-time.txt $GARBAGECAT_HOME/jdk25-shenandoah-time.log

$JAVA_HOME/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk25-z-time.txt $GARBAGECAT_HOME/jdk25-z-time.log
