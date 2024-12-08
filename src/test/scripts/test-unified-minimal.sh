#!/bin/sh
#
# Test unified logging with "minimal" gc logging events:
# Use the "default" decorator for troubleshooting purposes (vs. "time" which is minimal, most efficient).
#
# Usage: sh ./test-all.sh
#

GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-4.0.3-SNAPSHOT

##### JDK11 #####

# Create GC Logging

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-serial-new-serial-old-minimal.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-minimal.log::filesize=50M -XX:+UseParallelGC -XX:-UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-minimal.log::filesize=50M -XX:+UseParallelGC -XX:+UseParallelOldGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-par-new-cms-minimal.log::filesize=50M -XX:+UseConcMarkSweepGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-g1-minimal.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk11-shenandoah-minimal.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-serial-new-serial-old-minimal.txt $GARBAGECAT_HOME/jdk11-serial-new-serial-old-minimal.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-minimal.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-serial-old-minimal.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-minimal.txt $GARBAGECAT_HOME/jdk11-parallel-scavenge-parallel-old-compacting-minimal.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-par-new-cms-minimal.txt $GARBAGECAT_HOME/jdk11-par-new-cms-minimal.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-g1-minimal.txt $GARBAGECAT_HOME/jdk11-g1-minimal.log

/etc/alternatives/java_sdk_11_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk11-shenandoah-minimal.txt $GARBAGECAT_HOME/jdk11-shenandoah-minimal.log

##### JDK17 #####

# Create GC Logging

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-serial-new-serial-old-minimal.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-minimal.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-g1-minimal.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-shenandoah-minimal.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk17-z-minimal.log::filesize=50M -XX:+UseZGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-serial-new-serial-old-minimal.txt $GARBAGECAT_HOME/jdk17-serial-new-serial-old-minimal.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-minimal.txt $GARBAGECAT_HOME/jdk17-parallel-scavenge-parallel-old-compacting-minimal.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-g1-minimal.txt $GARBAGECAT_HOME/jdk17-g1-minimal.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-shenandoah-minimal.txt $GARBAGECAT_HOME/jdk17-shenandoah-minimal.log

/etc/alternatives/java_sdk_17_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk17-z-minimal.txt $GARBAGECAT_HOME/jdk17-z-minimal.log


##### JDK21 #####

# Create GC Logging

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-serial-new-serial-old-minimal.log::filesize=50M -XX:+UseSerialGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx64m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-minimal.log::filesize=50M -XX:+UseParallelGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms1m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-g1-minimal.log::filesize=50M -XX:+UseG1GC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-shenandoah-minimal.log::filesize=50M -XX:+UseShenandoahGC -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-z-nongen-minimal.log::filesize=50M -XX:+UseZGC -XX:-ZGenerational -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -Xms32m -Xmx96m -Xlog:gc=info,gc+cpu=info,gc+init=info,safepoint=info:file=$GARBAGECAT_HOME/jdk21-z-gen-minimal.log::filesize=50M -XX:+UseZGC -XX:+ZGenerational -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -t 50 -o /dev/null ./gc.log

# Analyze GC Logging

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-serial-new-serial-old-minimal.txt $GARBAGECAT_HOME/jdk21-serial-new-serial-old-minimal.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-minimal.txt $GARBAGECAT_HOME/jdk21-parallel-scavenge-parallel-old-compacting-minimal.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-g1-minimal.txt $GARBAGECAT_HOME/jdk21-g1-minimal.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-shenandoah-minimal.txt $GARBAGECAT_HOME/jdk21-shenandoah-minimal.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-z-nongen-minimal.txt $GARBAGECAT_HOME/jdk21-z-nongen-minimal.log

/etc/alternatives/java_sdk_21_openjdk/bin/java -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -p -t 50 -o $GARBAGECAT_HOME/jdk21-z-gen-minimal.txt $GARBAGECAT_HOME/jdk21-z-gen-minimal.log