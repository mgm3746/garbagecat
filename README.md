# garbagecat #

A command line tool that parses Java garbage collection logging and does analysis to support JVM tuning and troubleshooting for OpenJDK and Sun/Oracle JDK. It differs from other tools in that it goes beyond the simple math of calculating statistics such as maximum pause time and throughput. It analyzes collectors, triggers, JVM version, JVM options, and OS information and reports error/warn/info level analysis and recommendations.

## Supports ##

OpenJDK derivatives:
* Adoptium/AdoptOpenJDK
* Azul
* Microsoft Build of OpenJDK
* Oracle JDK
* Red Hat build of OpenJDK
* etc.

### Recommended GC Logging Options ###

JDK5 - JDK8:

>-XX:+PrintGC -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime

JDK8:

1) datestamp:

```
-XX:+PrintGC -Xloggc:gc_%p_%t.log -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:GCLogFileSize=50M -XX:NumberOfGCLogFiles=4


2021-10-08T20:22:22.788-0600: [GC (Allocation Failure) [PSYoungGen: 328070K->55019K(503808K)] 649122K->396284K(1078272K), 0.3093583 secs] [Times: user=0.43 sys=0.12, real=0.31 secs]
```

2) timestamp:

```
-XX:+PrintGC -Xloggc:gc_%p_%t.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:GCLogFileSize=50M -XX:NumberOfGCLogFiles=4


668.806: [GC (Allocation Failure) [PSYoungGen: 328070K->55019K(503808K)] 649122K->396284K(1078272K), 0.3093583 secs] [Times: user=0.43 sys=0.12, real=0.31 secs]
```

3) datestamp: timestamp:

```
-XX:+PrintGC -Xloggc:gc_%p_%t.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+UseGCLogFileRotation -XX:GCLogFileSize=50M -XX:NumberOfGCLogFiles=4


2021-10-08T20:22:22.788-0600: 668.806: [GC (Allocation Failure) [PSYoungGen: 328070K->55019K(503808K)] 649122K->396284K(1078272K), 0.3093583 secs] [Times: user=0.43 sys=0.12, real=0.31 secs]
```

JDK9+:

1) \[time\]

```
-Xlog:gc*,safepoint=info:file=gc_%p_%t.log:time:filecount=4,filesize=50M

[2020-02-14T15:21:55.207-0500] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
```

2) \[uptime\]

```
-Xlog:gc*,safepoint=info:file=gc_%p_%t.log:uptime:filecount=4,filesize=50M

[0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
```

3) \[uptimemillis\]

```
-Xlog:gc*,safepoint=info:file=gc_%p_%t.log:uptimemillis:filecount=4,filesize=50M

[052ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
```

4) \[time\]\[uptime\]

```
-Xlog:gc*,safepoint=info:file=gc_%p_%t.log:time,uptime:filecount=4,filesize=50M

[2020-02-14T15:21:55.207-0500][0.052s] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
```

5) \[time\]\[uptimemillis\]

```
-Xlog:gc*,safepoint=info:file=gc_%p_%t.log:time,uptimemillis:filecount=4,filesize=50M

[2020-02-14T15:21:55.207-0500][052ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
```

## Installation ##

Note: The Fedora and RHEL installs are release dependent. To ensure you have the latest code, [build garbagecat](https://github.com/mgm3746/garbagecat#building).

### Fedora ###

```
# dnf copr enable bostrt/garbagecat
# dnf install garbagecat
```

### RHEL Method #1 ###

#### RHEL7 ####

```
# yum install yum-plugin-copr --enablerepo=rhel-7-server-optional-rpms
# yum copr enable bostrt/garbagecat
# yum install garbagecat
```

#### RHEL8 ####

```
# yum install dnf-plugins-core
# yum copr enable bostrt/garbagecat
# yum install garbagecat
```

### RHEL Method #2 ###

Put the YUM repo file into your /etc/yum/repos.d/:
* [RHEL 7 repo file](https://copr.fedorainfracloud.org/coprs/bostrt/garbagecat/repo/epel-7/bostrt-garbagecat-epel-7.repo)
* [RHEL 8 repo file](https://copr.fedorainfracloud.org/coprs/bostrt/garbagecat/repo/epel-8/bostrt-garbagecat-epel-78.repo)

## Building ##

Download the latest Maven: [http://maven.apache.org/download.html](http://maven.apache.org/download.html).

Copy the download to where you want to install it and unzip it. For example:

```
$ cp ~/Downloads/apache-maven-3.6.3-bin.tar.gz ~/opt/
$ cd ~/opt/
$ tar -xvzf apache-maven-3.6.3-bin.tar.gz
$ rm apache-maven-3.6.3-bin.tar.gz
```

Get source:

```
$ git clone https://github.com/mgm3746/garbagecat.git
```

Build it:

```
$ cd garbagecat
$ /opt/apache-maven-3.6.3/bin/mvn clean (rebuilding)
$ /opt/apache-maven-3.6.3/bin/mvn assembly:assembly
$ export JAVA_HOME=/usr/lib/jvm/java/ (or wherever a JDK is installed)
$ /opt/apache-maven-3.6.3/bin/mvn javadoc:javadoc
```

If you get the following error:

>org.apache.maven.surefire.booter.SurefireExecutionException: TestCase; nested exception is
>java.lang.NoClassDefFoundError: TestCase

Run the following command:

```
$ /opt/apache-maven-3.6.3/bin/mvn -U -fn clean install
```

## Usage ##

```
$ java -jar garbagecat.jar --help
usage: garbagecat [OPTION]... [FILE]
 -h,--help                  help
 -j,--jvmoptions <arg>      JVM options used during JVM run
 -l,--latest                latest version 
 -o,--output <arg>          output file name (default report.txt)
 -p,--preprocess            do preprocessing
 -r,--reorder               reorder logging by timestamp
 -s,--startdatetime <arg>   JVM start datetime (yyyy-MM-dd HH:mm:ss.SSS)
                            for converting GC logging timestamps to datetime
 -t,--threshold <arg>       threshold (0-100) for throughput bottleneck
                            reporting
 -v,--version               version
```

Notes:
  1. The Fedora/RHEL install allows garbagecat to be run as an executable file. For example: `garbagecat --help`.
  1. JVM options are can be passed in if they are not present in the gc logging header. Specifying the JVM options used during the JVM run allows for more detailed analysis.
  1. By default a report called report.txt is created in the directory where the **garbagecat** tool is run. Specifying a custom name for the output file is useful when analyzing multiple gc logs.
  1. Version information is included in the report by using the version and.or latest version options.
  1. Preprocessing is sometimes required (e.g. when non-standard JVM options are used). It removes extraneous logging and makes any format adjustments needed for parsing (e.g. combining logging that the JVM sometimes splits across multiple lines). 
  1. When preprocessing is enabled, a preprocessed file will be created in the same location as the input file with a ".pp" file extension added. 
  1. Reordering is for gc logging that has gotten out of time/date order. Very rare, but some logging management systems/processes are susceptible to this happening (e.g. logging stored in a central repository).
  1. The startdatetime option is required when the gc logging has datestamps (e.g. 2017-04-03T03:13:06.756-0500) but no timestamps (e.g. 121.107), something that will not happen when using the standard recommended JVM options. Timestamps are required for garbagecat analysis, so if the logging does not have timestamps, you will need to pass in the JVM startup datetime so gc logging timestamps can be computed.
  1. If threshold is not defined, it defaults to 90.
  1. Throughput = (Time spent not doing gc) / (Total Time). Throughput of 100 means no time spent doing gc (good). Throughput of 0 means all time spent doing gc (bad).

## Example ##

https://github.com/mgm3746/garbagecat/tree/master/src/test/gc-example.log

```
$ java -jar garbagecat.jar -v -l /path/to/garbagecat/src/test/gc-example.log
```

### Report ###

```
gc-example.log
=======================================================================
Running garbagecat version: 3.0.6-SNAPSHOT
Latest garbagecat version/tag: v3.0.5
=======================================================================
Throughput less than 90%
----------------------------------------
2016-10-10T18:43:49.025-0700: 1.362: [GC (Allocation Failure) 2016-10-10T18:43:49.025-0700: 1.362: [ParNew: 136320K->8757K(153344K), 0.1186872 secs] 136320K->8757K(8371584K), 0.1190032 secs] [Times: user=0.34 sys=0.01, real=0.12 secs]
2016-10-10T18:43:50.538-0700: 2.875: [GC (Allocation Failure) 2016-10-10T18:43:50.538-0700: 2.875: [ParNew: 145077K->17024K(153344K), 0.1866956 secs] 145077K->24017K(8371584K), 0.1869439 secs] [Times: user=0.46 sys=0.03, real=0.19 secs]
...
=======================================================================
JVM:
-----------------------------------------------------------------------
Version: Java HotSpot(TM) 64-Bit Server VM (25.102-b14) for linux-amd64 JRE (1.8.0_102-b14), built on Jun 22 2016 18:43:17 by "java_re" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)
Options: -XX:CMSInitiatingOccupancyFraction=80 -XX:+CMSParallelRemarkEnabled -XX:+DisableExplicitGC -XX:+DoEscapeAnalysis -XX:ErrorFile=/home/jbcures/errors/hs_err_pid%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/jboss/app-files/crash/cures/ -XX:InitialHeapSize=8589934592 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=174485504 -XX:MaxTenuringThreshold=6 -XX:NewSize=174485504 -XX:OldPLABSize=16 -XX:OldSize=348971008 -XX:ParallelGCThreads=2 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC
Memory: Memory: 4k page, physical 32878232k(18756444k free), swap 4194300k(4194300k free)
=======================================================================
SUMMARY:
-----------------------------------------------------------------------
Datestamp First: 2016-10-10T18:43:49.025-0700
Timestamp First: 1.362 secs
Datestamp Last: 2016-10-11T12:34:47.720-0700
Timestamp Last: 64260.057 secs
# GC Events: 36546
Event Types: PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK, CMS_SERIAL_OLD
# Parallel Events: 36545
# Inverted Parallelism: 2
Inverted Parallelism Max: 2016-10-11T08:06:39.037-0700: 48171.374: [GC (CMS Initial Mark) [1 CMS-initial-mark: 6578959K(8218240K)] 6599305K(8371584K), 0.0118105 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
NewRatio: 54
Heap Occupancy Max: 7092037K
Heap After GC Max: 6988066K
Heap Space Max: 8371584K
Metaspace Occupancy Max: 167164K
Metaspace After GC Max: 167164K
Metaspace Space Max: 1204224K
GC Throughput: 96%
GC Pause Max: 7.527 secs
GC Pause Total: 2623.499 secs
=======================================================================
ANALYSIS:
-----------------------------------------------------------------------
error
-----------------------------------------------------------------------
*The CMS_SERIAL_OLD collector is being invoked for one of the following reasons: (1) Fragmentation. The concurrent low pause collector does not compact. When fragmentation becomes an issue a serial collection compacts the heap. If the old generation has available space, the cause is likely fragmentation. Fragmentation can be avoided by increasing the heap size. (2) Metaspace class metadata or compressed class pointers allocation failure. The GC attempts to free/resize metaspace. (3) Resizing perm gen. If perm gen occupancy is near perm gen allocation, the cause is likely perm gen. Perm gen resizing can be avoided by setting the minimum perm gen size equal to the the maximum perm gen size. For example: -XX:PermSize=256M -XX:MaxPermSize=256M. (4) Undetermined reasons. Possibly the JVM requires a certain amount of heap or combination of resources that is not being met, and consequently the concurrent low pause collector is not used despite being specified with the -XX:+UseConcMarkSweepGC option. The CMS_SERIAL_OLD collector is a serial (single-threaded) collector, which means it will take a very long time to collect a large heap. For optimal performance, tune to avoid serial collections.
...
-----------------------------------------------------------------------
warn
-----------------------------------------------------------------------
*CMS remark low parallelism: (1) If using JDK7 or earlier, add -XX:+CMSParallelRemarkEnabled, as remark is single-threaded by default in JDK7 and earlier. (2) Check if multi-threaded remark is disabled with -XX:-CMSParallelRemarkEnabled, and replace with -XX:+CMSParallelRemarkEnabled. (4) Add -XX:+ParallelRefProcEnabled to enable multi-threaded reference processing if the "weak refs processing" time is a significant amount of the total CMS remark time. (5) Check for swapping and if the number of GC threads (-XX:ParallelGCThreads=<n>) is appropriate for the number of cpu/cores and any processes sharing cpu.
...
-----------------------------------------------------------------------
info
-----------------------------------------------------------------------
*GC log file rotation is not enabled. Consider enabling rotation (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N[K|M|G] -XX:NumberOfGCLogFiles=N) to protect disk space.
...
=======================================================================
80 UNIDENTIFIED LOG LINE(S):
-----------------------------------------------------------------------
2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) 2016-10-10T19:17:37.771-0700: 2030.108: [ParNew2016-10-10T19:17:37.773-0700: 2030.110: [CMS-concurrent-abortable-preclean: 0.050/0.150 secs] [Times: user=0.11 sys=0.03, real=0.15 secs]
...
=======================================================================
```

Notes:
  1. The report contains six sections: (1) Version, (2) Bottlenecks, (3) JVM, (4) Summary, (5) Analysis, and (6) Unidentified log lines.
  1. A bottleneck is when throughput between two consecutive blocking gc events is less than the specified throughput threshold.
  1. An ellipsis (...) between log lines in the bottleneck section indicates time periods when throughput was above the threshold.
  1. If the bottleneck section is missing, then no bottlenecks were found for the given threshold.
  1. See the org.eclipselabs.garbagecat.domain.jdk package summary javadoc for gc event type definitions. There is a table that lists all the event types with links to detailed explanations and example logging.
  1. A garbage collection event can span multiple garbage collection log lines.
  1. You can get a good idea where hotspots are by generating the report multiple times with varying throughput threshold levels.
  1. There is a limit of 1000 unidentified log lines that will be reported. If there are any unidentified logging lines, try running again with the -p preprocess option enabled. Note that it is fairly common for the last line to be truncated, and this is not an issue.
  1. Please report unidentified log lines by opening an issue and zipping up and attaching the garbage collection logging: https://github.com/mgm3746/garbagecat/issues.

### Analysis ###

The bottom of the report shows 80 unidentified lines:

```
=======================================================================
80 UNIDENTIFIED LOG LINE(S):
-----------------------------------------------------------------------
2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) 2016-10-10T19:17:37.771-0700: 2030.108: [ParNew2016-10-10T19:17:37.773-0700: 2030.110: [CMS-concurrent-abortable-preclean: 0.050/0.150 secs] [Times: user=0.11 sys=0.03, real=0.15 secs]
...
```

Run with the preprocess flag:

```
$ java -jar garbagecat.jar -p -v -l /path/to/garbagecat/src/test/gc-example.log
```

There are no unidentified log lines after preprocessing. However, there are many bottlenecks where throughput is < 90% (default). To get a better idea of bottlenecks, run with -t 50 to see stretches where more time is spent running gc/jvm threads than application threads.

```
$ java -jar garbagecat.jar -v -l -p -t 50 ~/path/to/garbagecat/src/test/gc-example.log
```

There are still a lot of bottlenecks reported; however, none are for a very long stretch. For example, the following lines show 3 gc events where more time was spent doing gc than running application threads, but it covers less than 1 second: 62339.274 --> 62339.684:

```
=======================================================================
Throughput less than 50%
-----------------------------------------------------------------------
...
2016-10-11T12:02:46.937-0700: 62339.274: [GC (Allocation Failure) 2016-10-11T12:02:46.938-0700: 62339.275: [ParNew: 152015K->17024K(153344K), 0.0754387 secs] 6994550K->6895910K(8371584K), 0.0760508 secs] [Times: user=0.15 sys=0.00, real=0.08 secs]
2016-10-11T12:02:47.089-0700: 62339.426: [GC (CMS Final Remark) [YG occupancy: 107402 K (153344 K)]2016-10-11T12:02:47.089-0700: 62339.426: [Rescan (parallel) , 0.0358321 secs]2016-10-11T12:02:47.125-0700: 62339.462: [weak refs processing, 0.0055111 secs]2016-10-11T12:02:47.131-0700: 62339.468: [class unloading, 0.1042745 secs]2016-10-11T12:02:47.235-0700: 62339.572: [scrub symbol table, 0.0822672 secs]2016-10-11T12:02:47.317-0700: 62339.654: [scrub string table, 0.0045528 secs][1 CMS-remark: 6878886K(8218240K)] 6986289K(8371584K), 0.2329748 secs] [Times: user=0.27 sys=0.00, real=0.24 secs]
2016-10-11T12:02:47.347-0700: 62339.684: [GC (Allocation Failure) 2016-10-11T12:02:47.347-0700: 62339.684: [ParNew: 153335K->17024K(153344K), 0.0889831 secs] 7032068K->6936404K(8371584K), 0.0894930 secs] [Times: user=0.17 sys=0.00, real=0.09 secs]
...
```

There are no localized throughput bottlenecks, and the summary shows good overall throughput and a relatively low max pause:

```
=======================================================================
SUMMARY:
-----------------------------------------------------------------------
Datestamp First: 2016-10-10T18:43:49.025-0700
Timestamp First: 1.362 secs
Datestamp Last: 2016-10-11T12:34:47.720-0700
Timestamp Last: 64260.057 secs
# GC Events: 36586
Event Types: PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK, CMS_SERIAL_OLD
# Parallel Events: 36585
# Inverted Parallelism: 2
Inverted Parallelism Max: 2016-10-11T08:06:39.037-0700: 48171.374: [GC (CMS Initial Mark) [1 CMS-initial-mark: 6578959K(8218240K)] 6599305K(8371584K), 0.0118105 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
NewRatio: 54
Heap Occupancy Max: 7092037K
Heap After GC Max: 6988066K
Heap Space Max: 8371584K
Metaspace Occupancy Max: 167164K
Metaspace After GC Max: 167164K
Metaspace Space Max: 1204224K
GC Throughput: 96%
GC Pause Max: 7.527 secs
GC Pause Total: 2626.402 secs
=======================================================================
```

However, the summary shows the slow, single-threaded CMS_SERIAL_OLD collector was used. Based on the parallel vs. overall event numbers (36585 vs. 36586), it appears there was just one single event. The analysis provides guidance how to address this issue and other best practices:

```
=======================================================================
ANALYSIS:
-----------------------------------------------------------------------
error
-----------------------------------------------------------------------
*The CMS_SERIAL_OLD collector is being invoked for one of the following reasons: (1) Fragmentation. The concurrent low pause collector does not compact. When fragmentation becomes an issue a serial collection compacts the heap. If the old generation has available space, the cause is likely fragmentation. Fragmentation can be avoided by increasing the heap size. (2) Metaspace class metadata or compressed class pointers allocation failure. The GC attempts to free/resize metaspace. (3) Resizing perm gen. If perm gen occupancy is near perm gen allocation, the cause is likely perm gen. Perm gen resizing can be avoided by setting the minimum perm gen size equal to the the maximum perm gen size. For example: -XX:PermSize=256M -XX:MaxPermSize=256M. (4) Undetermined reasons. Possibly the JVM requires a certain amount of heap or combination of resources that is not being met, and consequently the concurrent low pause collector is not used despite being specified with the -XX:+UseConcMarkSweepGC option. The CMS_SERIAL_OLD collector is a serial (single-threaded) collector, which means it will take a very long time to collect a large heap. For optimal performance, tune to avoid serial collections.
*CMS promotion failed. A young generation collection is not able to complete because there is not enough space in the old generation for promotion. The old generation has available space, but it is not contiguous. When fragmentation is an issue, the concurrent low pause collector invokes a slow (single-threaded) serial collector to compact the heap. Tune to avoid fragmentation: (1) Increase the heap size. (2) Use -XX:CMSInitiatingOccupancyFraction=N (default 92) to run the CMS cycle more frequently to increase sweeping of dead objects in the old generation to free lists (e.g. -XX:CMSInitiatingOccupancyFraction=85 -XX:+UseCMSInitiatingOccupancyOnly). (3) Do heap dump analysis to determine if there is unintended object retention that can be addressed to decrease heap demands. Or move to a collector that handles fragmentation more efficiently: (1) G1 compacts the young and old generations during evacuation using a multi-threaded collector. (2) Shenandoah compacts concurrently. Temporarily add -XX:PrintFLSStatistics=1 and -XX:+PrintPromotionFailure to get additional insight into fragmentation.
-----------------------------------------------------------------------
warn
-----------------------------------------------------------------------
*CMS remark low parallelism: (1) If using JDK7 or earlier, add -XX:+CMSParallelRemarkEnabled, as remark is single-threaded by default in JDK7 and earlier. (2) Check if multi-threaded remark is disabled with -XX:-CMSParallelRemarkEnabled, and replace with -XX:+CMSParallelRemarkEnabled. (4) Add -XX:+ParallelRefProcEnabled to enable multi-threaded reference processing if the "weak refs processing" time is a significant amount of the total CMS remark time. (5) Check for swapping and if the number of GC threads (-XX:ParallelGCThreads=<n>) is appropriate for the number of cpu/cores and any processes sharing cpu.
*CMS initial mark low parallelism: (1) If using JDK6 or earlier, initial mark is single-threaded. Consider upgrading to JDK7 or later for multi-threaded initial mark. (2) If using JDK7, add -XX:+CMSParallelInitialMarkEnabled, as initial mark is single-threaded by default in JDK7. (3) Check if multi-threaded remark is disabled with -XX:-CMSParallelInitialMarkEnabled, and replace with -XX:+CMSParallelInitialMarkEnabled. (3) Check for swapping and if the number of GC threads (-XX:ParallelGCThreads=<n>) is appropriate for the number of cpu/cores and any processes sharing cpu.
*Explicit garbage collection has been disabled with -XX:+DisableExplicitGC. The JVM uses explicit garbage collection to manage direct memory (to free space when MaxDirectMemorySize is reached) and the Remote Method Invocation (RMI) system (to clean up unreachable remote objects). Disabling it for those use cases can cause a memory leak. Verify the application does not use direct memory (e.g. java.nio.DirectByteBuffer), does not make remote method calls or export remote objects like EJBs (everything runs in the same JVM), and does not depend on explicit garbage collection in some other way. Known applications that use direct memory: JBoss EAP7 (IO subsystem). If explicit garbage collection is required, remove -XX:+DisableExplicitGC, and if using the CMS or G1 collector, add -XX:+ExplicitGCInvokesConcurrent so explicit garbage collection is handled concurrently.
*The CMS collector does not always collect Perm/Metaspace by default (e.g. prior to JDK 1.8). Add -XX:+CMSClassUnloadingEnabled to collect Perm/Metaspace in the CMS concurrent cycle and avoid Perm/Metaspace collections being done by a slow (single threaded) serial collector.
*A heap dump file name has been specified with the -XX:HeapDumpPath (e.g. -XX:HeapDumpPath=/mydir/heapdump.hprof). Generally you only want to specify a directory (e.g. -XX:HeapDumpPath=/mydir/) and let the JVM use the default file name, which includes the process id. If you specify a file name, heap dumps will not overwrite each other. Only the initial file will be written. Subsequent heap dumps will fail due to the existing file. Unless disk space is a concern, you typically do not want to lose data. Reference: https://access.redhat.com/solutions/21109
*Application stopped time missing. Enable with -XX:+PrintGCApplicationStoppedTime (<= JDK8) or with safepoint logging at info level (e.g. -Xlog:gc*,safepoint=info:file=gc.log:uptime:filecount=4,filesize=50M). Required to determine overall throughput and identify throughput and pause issues not related to garbage collection, as many JVM operations besides garbage collection require all threads to reach a safepoint to execute. Reference: https://access.redhat.com/solutions/18656
*There is evidence of inverted parallelism. With parallel (multi-threaded) collector events, the "user" + "sys" time should be approximately equal to the "real" (wall) time multiplied by the # of GC threads. For example, if there are 3 GC threads we would expect a parallel collection that takes 1 second of "real" time to take approximately 3 seconds of "user" + "sys" time. The parallelism is 3x. If the parallelism is 1x ("user" + "sys" = "real"), the parallel collection is not offering any efficiency over a serial (single-threaded) collection. When "user" + "sys" < "real", the parallelism is inverted. Inverted parallelism can be a sign of high i/o (e.g. disk or network access) or not enough CPU (e.g. GC threads competing with each other or other processes). Check for swapping and if the number of GC threads (-XX:ParallelGCThreads=<n>) is appropriate for the number of cpu/cores and any processes sharing cpu.
-----------------------------------------------------------------------
info
-----------------------------------------------------------------------
*GC log file rotation is not enabled. Consider enabling rotation (-XX:+UseGCLogFileRotation -XX:GCLogFileSize=N[K|M|G] -XX:NumberOfGCLogFiles=N) to protect disk space.
*The number of times an object is copied between survivor spaces is being set with -XX:MaxTenuringThreshold=N (0-15). 0 = disabled. 15 (default) = promote when the survivor space fills. Unless testing has shown this improves performance, consider removing this option to allow the default value to be applied.
=======================================================================
```

## Copyright ##

Copyright (c) 2008-2021 Mike Millson

All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at [http://www.eclipse.org/legal/epl-v10.html](http://www.eclipse.org/legal/epl-v10.html).
