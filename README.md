# garbagecat #

A command line tool that parses Java garbage collection logging and does analysis to support JVM tuning and troubleshooting for OpenJDK and Sun/Oracle JDK. It differs from other tools in that it goes beyond the simple math of calculating statistics such as maximum pause time and throughput. It analyzes collectors, triggers, JVM version, JVM options, and OS information and reports error/warn/info level analysis and recommendations.

## Supports ##

  * OpenJDK
  * Sun/Oracle JDK 1.5 and higher
  * Best utilized with the following GC logging options:

>-XX:+PrintGC -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime

## Building ##

Maven 2.2 is required. Download the latest Maven 2.2 (2.2.1): http://maven.apache.org/download.html

Copy the download to where you want to install it and unzip it:
```
cp ~/Downloads/apache-maven-2.2.1-bin.tar.gz ~/opt/
cd ~/opt/
tar -xvzf apache-maven-2.2.1-bin.tar.gz
rm apache-maven-2.2.1-bin.tar.gz
```


Set M2\_HOME and add the maven executables to your PATH. For example, in  ~/.bash\_profile:
```
M2_HOME=/home/mmillson/opt/apache-maven-2.2.1
PATH=$M2_HOME/bin:$PATH
export PATH M2_HOME
```

Get source:
```
git clone https://github.com/mgm3746/garbagecat.git
```

Build it:
```
cd garbagecat
mvn clean (rebuilding)
mvn assembly:assembly
mvn javadoc:javadoc
```

If you get the following error:

>org.apache.maven.surefire.booter.SurefireExecutionException: TestCase; nested exception is
>java.lang.NoClassDefFoundError: TestCase

Run the following command:

```
mvn -U -fn clean install
```

## Usage ##

```
java -jar garbagecat-2.0.9-SNAPSHOT.jar --help
usage: garbagecat \[OPTION]... \[FILE]
 -h,--help                  help
 -j,--jvmoptions <arg>      JVM options used during JVM run 
 -o,--output <arg>          output file name (default report.txt)
 -p,--preprocess            do preprocessing
 -r,--reorder               reorder logging by timestamp
 -s,--startdatetime <arg>   JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS)
                            for converting GC logging timestamps to datetime
 -t,--threshold <arg>       threshold (0-100) for throughput bottleneck
                            reporting
 -v,--version               version
```

Notes:
  1. JVM options are can be passed in if they are not present in the gc logging header. Specifying the JVM options used during the JVM run allows for more detailed analysis.
  1. By default a report called report.txt is created in the directory where the **garbagecat** tool is run. Specifying a custom name for the output file is useful when analyzing multiple gc logs.
  1. Preprocessing is sometimes required (e.g. when non-standard JVM options are used). It removes extraneous logging and makes any format adjustments needed for parsing (e.g. combining logging that the JVM sometimes splits across multiple lines). 
  1. When preprocessing is enabled, a preprocessed file will be created in the same location as the input file with a ".pp" file extension added. 
  1. Reordering is for gc logging that has gotten out of time/date order. Very rare, but some logging management systems/processes are susceptible to this happening (e.g. logging stored in a central repository).
  1. The startdatetime option is required when the gc logging has datestamps (e.g. 2017-04-03T03:13:06.756-0500) but no timestamps (e.g. 121.107), something that will not happen when using the standard recommended JVM options. Timestamps are required for garbagecat analysis, so if the logging does not have timestamps, you will need to pass in the JVM startup datetime so gc logging timestamps can be computed.
  1. If threshold is not defined, it defaults to 90.
  1. Throughput = (Time spent not doing gc) / (Total Time). Throughput of 100 means no time spent doing gc (good). Throughput of 0 means all time spent doing gc (bad).

## Report ##

```
\========================================
Throughput less than 50%
\========================================
2016-10-11T12:32:59.782-0700: 64152.119: \[GC (Allocation Failure) 2016-10-11T12:32:59.782-0700: 64152.119: \[ParNew: 153319K->17024K(153344K), 0.0751215 secs] 6705727K->6595480K(8371584K), 0.0757982 secs] \[Times: user=0.15 sys=0.00, real=0.08 secs]
2016-10-11T12:32:59.861-0700: 64152.198: \[GC (CMS Initial Mark) \[1 CMS-initial-mark: 6578456K(8218240K)] 6595622K(8371584K), 0.0112740 secs] \[Times: user=0.02 sys=0.00, real=0.01 secs]
...
2016-10-11T12:33:03.408-0700: 64155.745: \[GC (Allocation Failure) 2016-10-11T12:33:03.408-0700: 64155.745: \[ParNew: 153083K->17024K(153344K), 0.0627566 secs] 6828120K->6716303K(8371584K), 0.0632619 secs] \[Times: user=0.12 sys=0.00, real=0.06 secs]
2016-10-11T12:33:03.474-0700: 64155.811: \[GC (CMS Final Remark) \[YG occupancy: 18045 K (153344 K)]2016-10-11T12:33:03.475-0700: 64155.812: \[Rescan (parallel) , 0.0238728 secs]2016-10-11T12:33:03.498-0700: 64155.835: \[weak refs processing, 0.0049139 secs]2016-10-11T12:33:03.503-0700: 64155.840: \[class unloading, 0.0956019 secs]2016-10-11T12:33:03.599-0700: 64155.936: \[scrub symbol table, 0.0649527 secs]2016-10-11T12:33:03.664-0700: 64156.001: \[scrub string table, 0.0057970 secs]\[1 CMS-remark: 6699279K(8218240K)] 6717324K(8371584K), 0.1956041 secs] \[Times: user=0.22 sys=0.00, real=0.20 secs]
\========================================
JVM:
\----------------------------------------
Version: Java HotSpot(TM) 64-Bit Server VM (25.102-b14) for linux-amd64 JRE (1.8.0_102-b14), built on Jun 22 2016 18:43:17 by "java_re" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)
Options: -XX:CMSInitiatingOccupancyFraction=80 -XX:+CMSParallelRemarkEnabled -XX:+DisableExplicitGC -XX:+DoEscapeAnalysis -XX:ErrorFile=/home/jbcures/errors/hs_err_pid%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/jboss/app-files/crash/cures/ -XX:InitialHeapSize=8589934592 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=174485504 -XX:MaxTenuringThreshold=6 -XX:NewSize=174485504 -XX:OldPLABSize=16 -XX:OldSize=348971008 -XX:ParallelGCThreads=2 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC
Memory: Memory: 4k page, physical 32878232k(18756444k free), swap 4194300k(4194300k free)
\========================================
SUMMARY:
\========================================
\# GC Events: 36586
Event Types: PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK, CMS_SERIAL_OLD
\# Parallel Events: 36257
\# Low Parellism Events: 0
NewRatio: 54
Max Heap Occupancy: 7092037K
Max Heap Space: 8371584K
Max Perm/Metaspace Occupancy: 167164K
Max Perm/Metaspace Space: 1204224K
GC Throughput: 96%
GC Max Pause: 7.527 secs
GC Total Pause: 2608.102 secs
First Datestamp: 2016-10-10T18:43:49.025-0700
First Timestamp: 1.362 secs
Last Datestamp: 2016-10-11T12:34:47.720-0700
Last Timestamp: 64260.057 secs
\========================================
ANALYSIS:
\----------------------------------------
error
\----------------------------------------
*The CMS_SERIAL_OLD collector is being invoked for one of the following reasons: (1) Fragmentation. The concurrent low pause collector does not compact. When fragmentation becomes an issue a serial collection compacts the heap. If the old generation has available space, the cause is likely fragmentation. Fragmentation can be avoided by increasing the heap size. (2) Resizing Perm/Metaspace. If Perm/Metaspace occupancy is near Perm/Metaspace allocation, the cause is likely Perm/Metaspace. Perm/Metaspace resizing can be avoided by setting the minimum Perm/Metaspace size equal to the the maximum Perm/Metaspace size. For example: -XX:PermSize=256M -XX:MaxPermSize=256M (Perm) or -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M (Metaspace). (3) Undetermined reasons. Possibly the JVM requires a certain amount of heap or combination of resources that is not being met, and consequently the concurrent low pause collector is not used despite being specified with the -XX:+UseConcMarkSweepGC option. The CMS_SERIAL_OLD collector is a serial (single-threaded) collector, which means it will take a very long time to collect a large heap. For optimal performance, tune to avoid serial collections.
*CMS promotion failed. A young generation collection is not able to complete because there is not enough space in the old generation for promotion. The old generation has available space, but it is not contiguous. When fragmentation is an issue, the concurrent low pause collector invokes a slow (single-threaded) serial collector to compact the heap. Tune to avoid fragmentation: (1) Increase the heap size. (2) Use -XX:CMSInitiatingOccupancyFraction=NN (default 92) to run the CMS cycle more frequently to increase sweeping of dead objects in the old generation to free lists (e.g. -XX:CMSInitiatingOccupancyFraction=85 -XX:+UseCMSInitiatingOccupancyOnly). (3) Do heap dump analysis to determine if there is unintended object retention that can be addressed to decrease heap demands. Or move to a collector that handles fragmentation more efficiently: (1) G1 compacts the young and old generations during evacuation using a multi-threaded collector. (2) Shenandoah compacts concurrently. Temporarily add -XX:PrintFLSStatistics=1 and -XX:+PrintPromotionFailure to get additional insight into fragmentation.
\----------------------------------------
warn
\----------------------------------------
*The Metaspace size should be explicitly set. The Metaspace size is unlimited by default and will auto increase in size up to what the OS will allow, so not setting it can swamp the OS. Explicitly set the Metaspace size. For example: -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M.
*Explicit garbage collection has been disabled with -XX:+DisableExplicitGC. That is fine if the JVM is not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM). If there are remote objects, do not use -XX:+DisableExplicitGC, as it can result in a memory leak. It is also possible the application depends on explicit garbage collection in some other way. If explicit garbage collection is required, remove -XX:+DisableExplicitGC and set sun.rmi.dgc.client.gcInterval and sun.rmi.dgc.server.gcInterval to values longer than the default 1 hour. Also add -XX:+ExplicitGCInvokesConcurrent so explicit garbage collection is handled concurrently if using the CMS or G1 collectors. For example, 4 hours (values in milliseconds): -Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=14400000 -XX:+ExplicitGCInvokesConcurrent.
*The CMS collector does not always collect Perm/Metaspace by default (e.g. prior to JDK 1.8). Add -XX:+CMSClassUnloadingEnabled to collect Perm/Metaspace in the CMS concurrent cycle and avoid Perm/Metaspace collections being done by a slow (single threaded) serial collector.
*-XX:+PrintGCApplicationStoppedTime missing. Required to determine overall throughput and identify throughput and pause issues not related to garbage collection, as many JVM operations besides garbage collection require all threads to reach a safepoint to execute.
\----------------------------------------
info
\----------------------------------------
*When UseCompressedOops and UseCompressedClassesPointers (JDK 1.8 u40+) are enabled (default) the Metaspace reported in the GC logging is the sum of two native memory spaces: (1) class metadata. (2) compressed class pointers. It is recommended to explicitly set the compressed class pointers space. For example: -XX:CompressedClassSpaceSize=1G.
*GC log file rotation is not enabled. Consider enabling rotation (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N -XX:NumberOfGCLogFiles=N) to protect disk space.
========================================
```

Notes:
  1. The report contains five sections: (1) Bottlenecks, (2) JVM, (3) Summary, (4) Analysis, and (5) Unidentified log lines.
  1. A bottleneck is when throughput between two consecutive blocking gc events is less than the specified throughput threshold.
  1. An ellipsis (...) between log lines in the bottleneck section indicates time periods when throughput was above the threshold.
  1. If the bottleneck section is missing, then no bottlenecks were found for the given threshold.
  1. See the org.eclipselabs.garbagecat.domain.jdk package summary javadoc for gc event type definitions. There is a table that lists all the event types with links to detailed explanations and example logging.
  1. A garbage collection event corresponds to a single garbage collection (preprocessed) log line.
  1. You can get a good idea where hot spots are by running the report multiple times with varying throughput threshold levels.
  1. There is a limit of 1000 unidentified log lines that will be reported.
  1. If there are any unidentified logging lines, run again with the -p preprocess option enabled.
  1. Once prepocessing is done once, it is not required to use the -p preprocess option again, but instead can specify the preprocessed file as input (e.g. java -jar garbagecat.jar /path/to/gc.log.pp). However, if you do that, you may lose some analysis (e.g. related to extraneous logging that is removed in the preparsed file).
  1. Please report unidentified log lines by opening an issue and zipping up and attaching the garbage collection logging: https://github.com/mgm3746/garbagecat/issues.

## Example 1 ##

  1. Run without any options:
```
java -jar garbagecat.jar gc.log
```
  1. The bottom of the report shows **garbagecat** found 1000 (the maximum) unidentified lines:
```
...
========================================
1000 UNIDENTIFIED LOG LINE(S):
========================================
2016-10-27T19:06:01.273-0400: 1.079: [GC Before GC:
1.080: [ParNew: 377487K->8458K(5505024K), 0.0540670 secs] 377487K->8458K(43253760K)After GC:
, 0.0541410 secs] [Times: user=0.95 sys=0.03, real=0.05 secs]
2016-10-27T19:06:25.090-0400: 24.897: [GC Before GC:
24.897: [ParNew: 4727050K->266680K(5505024K), 0.1855380 secs] 4727050K->266680K(43253760K)After GC:
, 0.1856260 secs] [Times: user=2.31 sys=0.03, real=0.19 secs]
...
```
  1. Run with the preproccess flag:
```
java -jar garbagecat.jar -p gc1.log
```
  1. There are no unidentified log lines after preprocessing:
```
...
\========================================
SUMMARY:
\========================================
\# GC Events: 1080
Event Types: PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK
\# Parallel Events: 1080
\# Low Parellism Events: 63
Lowest Parellism: 2016-10-27T19:06:01.327-0400: 1.134: \[GC \[1 CMS-initial-mark: 0K(37748736K)] 8458K(43253760K), 0.0035950 secs] \[Times: user=0.01 sys=0.00, real=0.01 secs]
NewRatio: 7
Max Heap Occupancy: 34097110K
Max Heap Space: 43253760K
GC Throughput: 100%
GC Max Pause: 2.402 secs
GC Total Pause: 226.967 secs
First Datestamp: 2016-10-27T19:06:01.273-0400
First Timestamp: 1.079 secs
Last Datestamp: 2016-10-30T14:48:32.387-0400
Last Timestamp: 243752.193 secs
\========================================
ANALYSIS:
\----------------------------------------
warn
\----------------------------------------
*The -XX:+PrintHeapAtGC option is enabled. The additional data output with this option is not being used for analysis, so it is extra logging overhead. If there is not a good use case for enabling this option, remove it.
*The CMS collector does not always collect Perm/Metaspace by default (e.g. prior to JDK 1.8). Add -XX:+CMSClassUnloadingEnabled to collect Perm/Metaspace in the CMS concurrent cycle and avoid Perm/Metaspace collections being done by a slow (single threaded) serial collector.
*-XX:+PrintGCApplicationStoppedTime missing. Required to determine overall throughput and identify throughput and pause issues not related to garbage collection, as many JVM operations besides garbage collection require all threads to reach a safepoint to execute.
----------------------------------------
info
----------------------------------------
*The -XX:PrintFLSStatistics option is being used. The additional data output with this option is not typically needed and adds significant logging overhead. If there is not a good use case for using this option, remove it.
\========================================
```
  1. Collectors look good for the CMS family.
  1. Generally throughput 95% and above is considered good, so 100% (rounded up) throughput is excellent. However, overall throughput will sometimes mask localized throughput issues, especially when the gc logging covers many days.
  1. A max pause time of ~2 seconds is generally very good.
  1. The are a few parallel events where the parallelism (ratio of user:real time) is low, but nothing concerning.
  1. Analysis includes some best practice advice, but nothing concerning.
  

## Example 2 ##

  1. Run with the preproccess flag:
  
```
java -jar garbagecat.jar -p gc2.log
```

1. Extremely low throughput, huge max pause, low number of parallel events:
```
...
========================================
SUMMARY:
----------------------------------------
# GC Events: 2254
Event Types: G1_YOUNG_PAUSE, G1_YOUNG_INITIAL_MARK, G1_FULL_GC, G1_CONCURRENT
# Parallel Events: 762
# Low Parellism Events: 2
Lowest Parellism: 2016-10-12T09:48:56.888+0200: 16.013: \[GC pause (G1 Evacuation Pause) (young), 0.3319233 secs]\[Eden: 2712.0M(2686.0M)->0.0B(1310.0M) Survivors: 32.0M->340.0M Heap: 2930.0M(5120.0M)->582.0M(5120.0M)] [Times: user=0.76 sys=0.10, real=1.96 secs]
Max Heap Occupancy: 3292160K
Max Heap Space: 5242880K
Max Perm/Metaspace Occupancy: 396834K
Max Perm/Metaspace Space: 1511424K
GC Throughput: 2%
GC Max Pause: 4371.466 secs
GC Total Pause: 9619.572 secs
First Datestamp: 2016-10-12T09:48:48.381+0200
First Timestamp: 7.507 secs
Last Timestamp: 9859.653 secs
\========================================

```

1. The analysis reflects what we see in the summary information:


```
\========================================
ANALYSIS:
\----------------------------------------
error
\----------------------------------------
*The G1_FULL_GC collector is being invoked due to the Old space or Perm/Metaspace filling up. Old space causes: (1) It is filled with humongous objects. Humongous objects can only be collected by the G1_FULL_GC collector. (2) The heap is swamped before the marking cycle is able to complete and a mixed collection reclaim space. Reduce the -XX:InitiatingHeapOccupancyPercent (default 45) to start the marking cycle earlier. For example: -XX:InitiatingHeapOccupancyPercent=40. (3) The heap is simply too small, (4) Bugs in JDK7 that cause G1 Full GC collections. For example: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8030849. The G1_FULL_GC collector is a serial (single-threaded) collector, which means it will take a very long time to collect a large heap. For optimal performance, tune to avoid serial collections.
*Metaspace allocation failure. The Metaspace is not able to be resized, and the JVM is doing full, serial collections attempting to free Metaspace. The class metadata or compressed class pointers space is undersized, or there is a Metaspace leak. Increase the class metadata and compressed class pointers spaces to sufficiently large values to observe if there is a runaway leak or Metaspace usage levels off. For example, the following options will result in a 512M space for class metadata and 1G space for compressed class pointers: -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M -XX:CompressedClassSpaceSize=1G.
\----------------------------------------
warn
\----------------------------------------
*The JVM Tools Interface (TI) API is being called (e.g. by some tool) to explicitly invoke garbage collection.
*Many environments (e.g. JBoss versions prior to EAP6) cause the RMI subsystem to be loaded. RMI manages Distributed Garbage Collection (DGC) by calling System.gc() to clean up unreachable remote objects, resulting in unnecessary major (full) garbage collection that can seriously impact performance. The default interval changed from 1 minute to 1 hour in JDK6 update 45. DGC is required to prevent memory leaks when making remote method calls or exporting remote objects like EJBs; however, test explicitly setting the DGC client and server intervals to longer intervals to minimize the impact of explicit garbage collection. For example, 4 hours (values in milliseconds): -Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=14400000. Or if not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM), disable explicit garbage collection altogether with -XX:+DisableExplicitGC.
*Consider adding -XX:+ExplicitGCInvokesConcurrent so explicit garbage collection is handled concurrently by the CMS and G1 collectors. Or if not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM), disable explicit garbage collection altogether with -XX:+DisableExplicitGC.
*Add -XX:+HeapDumpOnOutOfMemoryError. This option does not impact performance (until the heap is actually written out); it is simply a flag to indicate that a heap dump should be generated when the first thread throws OutOfMemoryError. Generally this JVM argument should always be used, as it provides critical information in case of a memory error.
*Consider removing -XX:+TieredCompilation. It is known to cause performance issues with JDK7 (http://bugs.java.com/view_bug.do?bug_id=7159766) and JDK8 (https://bugzilla.redhat.com/show_bug.cgi?id=1420222).
*-XX:+PrintGCApplicationStoppedTime missing. Required to determine overall throughput and identify throughput and pause issues not related to garbage collection, as many JVM operations besides garbage collection require all threads to reach a safepoint to execute.
\----------------------------------------
info
\----------------------------------------
*Last log line not identified. This is typically caused by the gc log being copied while the JVM is in the middle of logging an event, resulting in truncated logging. If it is not due to truncated logging, report the unidentified logging line: https://github.com/mgm3746/garbagecat/issues.
*When UseCompressedOops and UseCompressedClassesPointers (JDK 1.8 u40+) are enabled (default) the Metaspace reported in the GC logging is the sum of two native memory spaces: (1) class metadata. (2) compressed class pointers. It is recommended to explicitly set the compressed class pointers space. For example: -XX:CompressedClassSpaceSize=1G.
*GC log file rotation is not enabled. Consider enabling rotation (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N -XX:NumberOfGCLogFiles=N) to protect disk space.
\========================================
```
