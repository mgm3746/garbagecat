# garbagecat #

A command line tool that parses Java garbage collection logging and does analysis to support JVM tuning and troubleshooting for OpenJDK and Sun/Oracle JDK. It differs from other tools in that it goes beyond the simple math of calculating statistics such as maximum pause time and throughput. It analyzes collectors, triggers, JVM version, JVM options, and OS information and reports error/warn/info level analysis and recommendations.

## Supports ##

  * OpenJDK
  * Sun/Oracle JDK 1.5 and higher
  * Best utilized with the following GC logging options:

>-XX:+PrintGC -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime

## Installation ##

### Fedora 

```
# dnf copr enable bostrt/garbagecat
# dnf install garbagecat
```

### RHEL 7

More to come after https://bugzilla.redhat.com/show_bug.cgi?id=1429831 is completed. For now, Drop in the YUM repo file into your /etc/yum/repos.d/. Here's the repo file for [RHEL 7](https://copr.fedorainfracloud.org/coprs/bostrt/garbagecat/repo/epel-7/bostrt-garbagecat-epel-7.repo).

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
usage: garbagecat [OPTION]... [FILE]
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
========================================
Throughput less than 90%
----------------------------------------
...
2017-01-12T05:17:38.064-0500: 2163.994: [GC remark, 0.0614813 secs] [Times: user=1.48 sys=0.01, real=0.06 secs]
2017-01-12T05:17:38.126-0500: 2164.057: [GC cleanup 24G->23G(42G), 0.0188102 secs] [Times: user=0.49 sys=0.00, real=0.02 secs]
...
2017-01-12T05:21:43.121-0500: 2409.051: [GC remark, 0.0317641 secs] [Times: user=0.81 sys=0.00, real=0.03 secs]
2017-01-12T05:21:43.153-0500: 2409.084: [GC cleanup 21G->21G(42G), 0.0174042 secs] [Times: user=0.45 sys=0.00, real=0.02 secs]
...
========================================
JVM:
----------------------------------------
Version: Java HotSpot(TM) 64-Bit Server VM (25.45-b02) for linux-amd64 JRE (1.8.0_45-b14), built on Apr 10 2015 10:07:45 by "java_re" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)
Options: -XX:CompressedClassSpaceSize=1073741824 -XX:+DoEscapeAnalysis -XX:GCLogFileSize=2097152 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/fi/logs/rpdswafir1/rpdswafir1-ahs1-tcs1 -XX:InitialHeapSize=45097156608 -XX:MaxGCPauseMillis=500 -XX:MaxHeapSize=45097156608 -XX:MaxMetaspaceSize=5368709120 -XX:MetaspaceSize=5368709120 -XX:NumberOfGCLogFiles=5 -XX:+PrintGC -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:ThreadStackSize=256 -XX:-UseCompressedOops -XX:+UseG1GC -XX:-UseGCLogFileRotation -XX:+UseLargePages
Memory: Memory: 4k page, physical 528792532k(351929072k free), swap 30736380k(30736380k free)
========================================
SUMMARY:
----------------------------------------
# GC Events: 99
Event Types: G1_YOUNG_PAUSE, G1_YOUNG_INITIAL_MARK, G1_CONCURRENT, G1_REMARK, G1_CLEANUP, G1_MIXED_PAUSE
# Parallel Events: 75
# Parallel Events with Low Parallelism: 0
Max Heap Occupancy: 38063309K
Max Heap Space: 44040192K
GC Throughput: 100%
GC Max Pause: 0.682 secs
GC Total Pause: 17.702 secs
Stopped Time Throughput: 100%
Stopped Time Max Pause: 0.683 secs
Stopped Time Total: 18.756 secs
GC/Stopped Ratio: 94%
First Datestamp: 2017-01-12T04:41:34.411-0500
First Timestamp: 0.341 secs
Last Datestamp: 2017-01-12T06:50:33.313-0500
Last Timestamp: 7739.244 secs
========================================
ANALYSIS:
----------------------------------------
error
----------------------------------------
*Compressed class pointers space size is set (-XX:CompressedClassSpaceSize), and heap >= 32G. Remove -XX:CompressedClassSpaceSize, as compressed object references should not be used on heaps >= 32G.
*Humongous objects are being allocated on an old version of the JDK that is not able to fully reclaim humongous objects during young collections. Upgrade to JDK8 update 60 or later.
----------------------------------------
warn
----------------------------------------
*Many environments (e.g. JBoss versions prior to EAP6) cause the RMI subsystem to be loaded. RMI manages Distributed Garbage Collection (DGC) by calling System.gc() to clean up unreachable remote objects, resulting in unnecessary major (full) garbage collection that can seriously impact performance. The default interval changed from 1 minute to 1 hour in JDK6 update 45. DGC is required to prevent memory leaks when making remote method calls or exporting remote objects like EJBs; however, test explicitly setting the DGC client and server intervals to longer intervals to minimize the impact of explicit garbage collection. For example, 4 hours (values in milliseconds): -Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=14400000. Or if not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM), disable explicit garbage collection altogether with -XX:+DisableExplicitGC.
*Consider adding -XX:+ExplicitGCInvokesConcurrent so explicit garbage collection is handled concurrently by the CMS and G1 collectors. Or if not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM), disable explicit garbage collection altogether with -XX:+DisableExplicitGC.
*Number of GC log files is defined (-XX:NumberOfGCLogFiles), yet GC log file rotation is disabled. Either remove -XX:NumberOfGCLogFiles or enable GC log file rotation with -XX:+UseGCLogFileRotation.
----------------------------------------
info
----------------------------------------
*Last log line not identified. This is typically caused by the gc log being copied while the JVM is in the middle of logging an event, resulting in truncated logging. If it is not due to truncated logging, report the unidentified logging line: https://github.com/mgm3746/garbagecat/issues.
*GC log file rotation is disabled (-XX:-UseGCLogFileRotation). Consider enabling rotation to protect disk space.
========================================
1 UNIDENTIFIED LOG LINE(S):
----------------------------------------
2017-01-12T06:50:34.314-0500: 7740.244
========================================
```

Notes:
  1. The report contains five sections: (1) Bottlenecks, (2) JVM, (3) Summary, (4) Analysis, and (5) Unidentified log lines.
  1. A bottleneck is when throughput between two consecutive blocking gc events is less than the specified throughput threshold.
  1. An ellipsis (...) between log lines in the bottleneck section indicates time periods when throughput was above the threshold.
  1. If the bottleneck section is missing, then no bottlenecks were found for the given threshold.
  1. See the org.eclipselabs.garbagecat.domain.jdk package summary javadoc for gc event type definitions. There is a table that lists all the event types with links to detailed explanations and example logging.
  1. A garbage collection event can span multiple garbage collection log lines.
  1. You can get a good idea where hotspots are by generationg the report multiple times with varying throughput threshold levels.
  1. There is a limit of 1000 unidentified log lines that will be reported. If there are any unidentified logging lines, try running again with the -p preprocess option enabled. Note that it is fairly common for the last line to be truncated, and this is not an issue.
  1. Please report unidentified log lines by opening an issue and zipping up and attaching the garbage collection logging: https://github.com/mgm3746/garbagecat/issues.

## Example ##

https://github.com/mgm3746/garbagecat/tree/master/src/test/data/gc.log

```
java -jar garbagecat.jar /path/to/gc.log
```

The bottom of the report shows 80 unidentified lines:
```
========================================
80 UNIDENTIFIED LOG LINE(S):
----------------------------------------
2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) 2016-10-10T19:17:37.771-0700: 2030.108: [ParNew2016-10-10T19:17:37.773-0700: 2030.110: [CMS-concurrent-abortable-preclean: 0.050/0.150 secs] [Times: user=0.11 sys=0.03, real=0.15 secs]
: 144966K->14227K(153344K), 0.0798105 secs] 6724271K->6623823K(8371584K), 0.0802771 secs] [Times: user=0.15 sys=0.01, real=0.08 secs]
...
```

Run with the preprocess flag:

```
java -jar garbagecat.jar -p /path/to/gc.log
```

There are no unidentified log lines after preprocessing. However, there are many bottlenecks where throughput is < 90% (default). To get a better idea of bottlenecks, run with -t 50 to see stretches where more time is spent running gc/jvm threads than application threads.

```
java -jar garbagecat.jar -t 50 -p /path/to/gc.log
```

There are still a lot of bottlenecks reported; however, none are for a very long stretch. For example, the following lines show 3 gc events where more time was spent doing gc than running application threads, but it covers less than 1 second: 62339.274 --> 62339.684:

```
========================================
Throughput less than 50%
----------------------------------------
...
2016-10-11T12:02:46.937-0700: 62339.274: [GC (Allocation Failure) 2016-10-11T12:02:46.938-0700: 62339.275: [ParNew: 152015K->17024K(153344K), 0.0754387 secs] 6994550K->6895910K(8371584K), 0.0760508 secs] [Times: user=0.15 sys=0.00, real=0.08 secs]
2016-10-11T12:02:47.089-0700: 62339.426: [GC (CMS Final Remark) [YG occupancy: 107402 K (153344 K)]2016-10-11T12:02:47.089-0700: 62339.426: [Rescan (parallel) , 0.0358321 secs]2016-10-11T12:02:47.125-0700: 62339.462: [weak refs processing, 0.0055111 secs]2016-10-11T12:02:47.131-0700: 62339.468: [class unloading, 0.1042745 secs]2016-10-11T12:02:47.235-0700: 62339.572: [scrub symbol table, 0.0822672 secs]2016-10-11T12:02:47.317-0700: 62339.654: [scrub string table, 0.0045528 secs][1 CMS-remark: 6878886K(8218240K)] 6986289K(8371584K), 0.2329748 secs] [Times: user=0.27 sys=0.00, real=0.24 secs]
2016-10-11T12:02:47.347-0700: 62339.684: [GC (Allocation Failure) 2016-10-11T12:02:47.347-0700: 62339.684: [ParNew: 153335K->17024K(153344K), 0.0889831 secs] 7032068K->6936404K(8371584K), 0.0894930 secs] [Times: user=0.17 sys=0.00, real=0.09 secs]
...
```

There are no localized throughput bottlenecks, and the summary shows good overall throughput and a relatively low max pause:

```
========================================
SUMMARY:
----------------------------------------
# GC Events: 36586
Event Types: PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK, CMS_SERIAL_OLD
# Parallel Events: 36585
# Parallel Events with Low Parallelism: 78
Parallel Event with Lowest Parallelism: 2016-10-10T19:17:36.730-0700: 2029.067: [GC (CMS Initial Mark) [1 CMS-initial-mark: 6579305K(8218240K)] 6609627K(8371584K), 0.0088184 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
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
========================================
```

However, the summary shows the slow, single-threaded CMS_SERIAL_OLD collector was used. Based on the parallel vs. overall event numbers (36585 vs. 36586), it appears there was just one single event. The analysis provides guidance how to address this issue and other best practices:

```
========================================
ANALYSIS:
----------------------------------------
error
----------------------------------------
*The CMS_SERIAL_OLD collector is being invoked for one of the following reasons: (1) Fragmentation. The concurrent low pause collector does not compact. When fragmentation becomes an issue a serial collection compacts the heap. If the old generation has available space, the cause is likely fragmentation. Fragmentation can be avoided by increasing the heap size. (2) Resizing Perm/Metaspace. If Perm/Metaspace occupancy is near Perm/Metaspace allocation, the cause is likely Perm/Metaspace. Perm/Metaspace resizing can be avoided by setting the minimum Perm/Metaspace size equal to the the maximum Perm/Metaspace size. For example: -XX:PermSize=256M -XX:MaxPermSize=256M (Perm) or -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M (Metaspace). (3) Undetermined reasons. Possibly the JVM requires a certain amount of heap or combination of resources that is not being met, and consequently the concurrent low pause collector is not used despite being specified with the -XX:+UseConcMarkSweepGC option. The CMS_SERIAL_OLD collector is a serial (single-threaded) collector, which means it will take a very long time to collect a large heap. For optimal performance, tune to avoid serial collections.
*CMS promotion failed. A young generation collection is not able to complete because there is not enough space in the old generation for promotion. The old generation has available space, but it is not contiguous. When fragmentation is an issue, the concurrent low pause collector invokes a slow (single-threaded) serial collector to compact the heap. Tune to avoid fragmentation: (1) Increase the heap size. (2) Use -XX:CMSInitiatingOccupancyFraction=NN (default 92) to run the CMS cycle more frequently to increase sweeping of dead objects in the old generation to free lists (e.g. -XX:CMSInitiatingOccupancyFraction=85 -XX:+UseCMSInitiatingOccupancyOnly). (3) Do heap dump analysis to determine if there is unintended object retention that can be addressed to decrease heap demands. Or move to a collector that handles fragmentation more efficiently: (1) G1 compacts the young and old generations during evacuation using a multi-threaded collector. (2) Shenandoah compacts concurrently. Temporarily add -XX:PrintFLSStatistics=1 and -XX:+PrintPromotionFailure to get additional insight into fragmentation.
----------------------------------------
warn
----------------------------------------
*The Metaspace size should be explicitly set. The Metaspace size is unlimited by default and will auto increase in size up to what the OS will allow, so not setting it can swamp the OS. Explicitly set the Metaspace size. For example: -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M.
*Explicit garbage collection has been disabled with -XX:+DisableExplicitGC. That is fine if the JVM is not making remote method calls and not exporting remote objects like EJBs (everything runs in the same JVM). If there are remote objects, do not use -XX:+DisableExplicitGC, as it can result in a memory leak. It is also possible the application depends on explicit garbage collection in some other way. If explicit garbage collection is required, remove -XX:+DisableExplicitGC and set sun.rmi.dgc.client.gcInterval and sun.rmi.dgc.server.gcInterval to values longer than the default 1 hour. Also add -XX:+ExplicitGCInvokesConcurrent so explicit garbage collection is handled concurrently if using the CMS or G1 collectors. For example, 4 hours (values in milliseconds): -Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=14400000 -XX:+ExplicitGCInvokesConcurrent.
*The CMS collector does not always collect Perm/Metaspace by default (e.g. prior to JDK 1.8). Add -XX:+CMSClassUnloadingEnabled to collect Perm/Metaspace in the CMS concurrent cycle and avoid Perm/Metaspace collections being done by a slow (single threaded) serial collector.
*-XX:+PrintGCApplicationStoppedTime missing. Required to determine overall throughput and identify throughput and pause issues not related to garbage collection, as many JVM operations besides garbage collection require all threads to reach a safepoint to execute.
----------------------------------------
info
----------------------------------------
*When UseCompressedOops and UseCompressedClassesPointers (JDK 1.8 u40+) are enabled (default) the Metaspace reported in the GC logging is the sum of two native memory spaces: (1) class metadata. (2) compressed class pointers. It is recommended to explicitly set the compressed class pointers space. For example: -XX:CompressedClassSpaceSize=1G.
*GC log file rotation is not enabled. Consider enabling rotation (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N -XX:NumberOfGCLogFiles=N) to protect disk space.
========================================
```
