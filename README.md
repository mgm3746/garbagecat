# garbagecat #

Parses Java garbage collection logging and provides analysis to support JVM tuning and troubleshooting for OpenJDK and Sun JDK. It differs from other tools in that it goes beyond the simple math of calculating statistics such as maximum pause time and throughput. It adds context to these numbers by identifying the associated collector or collector phase, which allows for much deeper insight and analysis. This is especially relevant to collectors such as the Concurrent Mark Sweep collector that have multiple concurrent and stop-the-world phases.

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
java -jar garbagecat-2.0.0.jar --help
usage: garbagecat [OPTION]... [FILE]
 -h,--help                  help
 -o,--options <arg>         JVM options used during JVM run 
 -p,--preprocess            preprocessing flag
 -r,--reorder               reorder logging by timestamp
 -s,--startdatetime <arg>   JVM start datetime (yyyy-MM-dd HH:mm:ss,SSS)
                            for converting GC logging timestamps to datetime
 -t,--threshold <arg>       threshold (0-100) for throughput bottleneck
                            reporting
```

Notes:
  1. Specifying the JVM options used during the JVM run allows for more detailed analysis.
  1. When preprocessing is enabled, a preprocessed file will be created in the same location as the input file with a ".pp" file extension added.
  1. See general preprocessing and JDK preprocessing for more information on preprocessing.
  1. The startdatetime option sets the JVM startup datetime so GC logging timestamps (the number of seconds since JVM startup) can be converted to a standard datetime when reporting bottlenecks. This allows GC bottlenecks and application logging to be correlated.
  1. If threshold is not defined, it defaults to 90.
  1. Throughput = (Time spent not doing GC) / (Total Time). Throughput of 100 means no time spent doing GC (good). Throughput of 0 means all time spent doing GC (bad).
  1. A report called report.txt is created in the directory where the **garbagecat** tool is run.

## Report ##

```
========================================
Throughput less than 25%
========================================
1735.401: \[GC pause (G1 Evacuation Pause) (young), 0.0621580 secs] 14029M->13926M(30720M) [Times: user=0.27 sys=0.00, real=0.06 secs] 
1735.537: \[GC remark, 0.2534012 secs] [Times: user=0.34 sys=0.02, real=0.26 secs] 
1735.792: \[GC cleanup 13G->13G(30G), 0.0668881 secs] [Times: user=0.38 sys=0.00, real=0.06 secs] 
...
2677.971: \[GC pause (G1 Evacuation Pause) (young), 0.0622566 secs] 13926M->13824M(30720M) [Times: user=0.28 sys=0.00, real=0.06 secs] 
2678.042: \[GC remark, 0.3963506 secs] [Times: user=0.45 sys=0.00, real=0.39 secs] 
2678.441: \[GC cleanup 13G->13G(30G), 0.0828542 secs] [Times: user=0.47 sys=0.00, real=0.08 secs] 
========================================
SUMMARY:
========================================
# GC Events: 10965
Event Types: APPLICATION_STOPPED_TIME, G1_YOUNG_PAUSE, G1_FULL_GC, G1_YOUNG_INITIAL_MARK, G1_CONCURRENT, G1_REMARK, G1_CLEANUP, G1_MIXED_PAUSE
Max Heap Space: 31457280K
Max Heap Occupancy: 14365696K
GC Throughput: 91%
GC Max Pause: 5539 ms
GC Total Pause: 543988 ms
# Stopped Time Events: 21094
Stopped Time Throughput: 79%
Stopped Time Max Pause: 20829 ms
Stopped Time Total: 1243683 ms
GC/Stopped Ratio: 44%
First Timestamp: 6556 ms
Last Timestamp: 6039764 ms
========================================
ANALYSIS:
========================================
*Add -XX:+DisableExplicitGC or -XX:+ExplicitGCInvokesConcurrent if not making remote method calls and not exporting remote objects (e.g. EJBs).
*A significant amount of stopped time (>20%) is not GC related. Check for inverted parallelism and other GC operations that require a safepoint: Deoptimization, PrintThreads, PrintJNI, FindDeadlock, ThreadDump, EnableBiasLocking, RevokeBias, HeapDumper, GetAllStackTrace.
========================================
```

Notes:
  1. The report contains three sections: (1) Bottlenecks, (2) Summary, and (3) Unidentified log lines.
  1. A bottleneck is when throughput between two consecutive GC events of the same type is less than the specified throughput threshold.
  1. Bottlenecks are reported within the same GC event type because there does not seem to be a correlation between timestamps between event types (due to different threads, perhaps?). For example, the duration of a tenured collection can exceed the time interval elapsed since the previous young collection. Also, it makes sense to look for bottlenecks within an event type, as typical issues are continuous young collections one after another or continuous old collections one after another.
  1. An ellipsis (...) between log lines in the bottleneck section indicates time periods when throughput was above the threshold.
  1. If the bottleneck section is missing, then no bottlenecks were found for the given threshold.
  1. See the org.eclipselabs.garbagecat.domain.jdk package summary javadoc for GC event type definitions. There is a table that lists all the event types with links to detailed explanations and example logging.
  1. A garbage collection event corresponds to a single garbage collection log line.
  1. You can get a good idea where hot spots are by running the report multiple times with varying throughput threshold levels.
  1. There is a limit of 1000 unidentified log lines that will be reported.
  1. If there are any unidentifed logging lines, run the report again with the -p preprocessing option enabled. Preprocessing removes extraneous information (e.g. application logging when there is not a dedicated log file) and makes any format adjustments needed for parsing (e.g. combining logging that the JVM sometimes splits across two lines).
  1. Once prepocessing is done once, do not use the -p preprocessing option again, but instead specify the preprocessed file as input (e.g. java -jar /path/to/garbagecat-2.0.0.jar gc.log.pp).
  1. Please report unidentified log lines by opening an issue and zipping up and attaching the garbage collection logging.

## Example ##

  1. Run without any options:
```
java -jar garbagecat-2.0.0.jar gc.log
```
  1. The bottom of the report shows **garbagecat** found 1000 (the maximum) unidentified lines:
```
...
========================================
1000 UNIDENTIFIED LOG LINE(S):
========================================
6.065: [Full GC {Heap before gc invocations=0:
 par new generation   total 261952K, used 199028K [0x80b90000, 0x90b90000, 0x90b90000)
  eden space 261760K,  76% used [0x80b90000, 0x8cded0d0, 0x90b30000)
  from space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
  to   space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 concurrent mark-sweep generation total 262144K, used 0K [0x90b90000, 0xa0b90000, 0xd8b90000)
 concurrent-mark-sweep perm gen total 262144K, used 15186K [0xd8b90000, 0xe8b90000, 0xf0b90000)
6.065: [CMS: 0K->8200K(262144K), 0.2010540 secs] 199028K->8200K(524096K), [CMS Perm : 15186K->15115K(262144K)]Heap after gc invocations=1:
 par new generation   total 261952K, used 0K [0x80b90000, 0x90b90000, 0x90b90000)
  eden space 261760K,   0% used [0x80b90000, 0x80b90000, 0x90b30000)
  from space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
  to   space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 concurrent mark-sweep generation total 262144K, used 8200K [0x90b90000, 0xa0b90000, 0xd8b90000)
 concurrent-mark-sweep perm gen total 262144K, used 15115K [0xd8b90000, 0xe8b90000, 0xf0b90000)
}
, 0.2012310 secs]
...
```
  1. Run with the preproccess flag:
```
java -jar garbagecat-2.0.0.jar -p gc.log
```
  1. There are no unidentified log lines after preprocessing:
```
...
========================================
SUMMARY:
========================================
# GC Events: 24744
GC Event Types: CMS_SERIAL_OLD, PAR_NEW, CMS_INITIAL_MARK, CMS_CONCURRENT, CMS_REMARK, PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE, PAR_NEW_CONCURRENT_MODE_FAILURE, CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE, PAR_NEW_PROMOTION_FAILED
Max Heap Space: 1441600K
Max Heap Occupancy: 1441600K
Max Perm Space: 262144K
Max Perm Occupancy: 73168K
Throughput: 98%
Max Pause: 37643 ms
Total Pause: 37765732 ms
First Timestamp: 6065 ms
Last Timestamp: 2500548254 ms
========================================
...
```
  1. Throughput 95% and above is considered good, so 98% throughput is very good. However, overall throughput will sometimes mask localized throughput issues, especially in this case when the GC logging covers almost 29 days.
  1. There is a max pause time of almost 40 seconds, not good.
  1. The PAR\_NEW\_PROMOTION\_FAILED\_CMS\_CONCURRENT\_MODE\_FAILURE, PAR\_NEW\_CONCURRENT\_MODE\_FAILURE, CMS\_SERIAL\_OLD\_CONCURRENT\_MODE\_FAILURE, PAR\_NEW\_PROMOTION\_FAILED, and CMS\_SERIAL\_OLD events are all concerns for similar reasons.

The steps above really are all that is needed to determine the issue; however, following is some additional analysis to show the rest of the tool's functionality.

  1. The max perm size appears to be much larger than needed. Since this is 32-bit, address space can be an issue. It looks like 128MB would be plenty big enough.
  1. To get an idea where the biggest GC bottlenecks are, run again, this time specifying a throughput threshold of 10% (90+% of time spent in GC). You don't need to preprocess again, so pass in the preprocessed file:
```
java -jar garbagecat-2.0.0.jar -t 10 gc.log.pp
```
  1. The new report shows many stretches where almost all time is spent doing garbage collection. For example, this 70 second interval:
```
2495490.528: [Full GC 2495490.528: [CMS (concurrent mode failure): 1179647K->1179648K(1179648K), 7.7971770 secs] 1441599K->1440481K(1441600K), [CMS Perm : 72831K->72830K(262144K)], 7.7974610 secs]
2495498.338: [Full GC 2495498.338: [CMS (concurrent mode failure): 1179648K->1179648K(1179648K), 7.9398610 secs] 1441599K->1440455K(1441600K), [CMS Perm : 72832K->72832K(262144K)], 7.9401510 secs]
2495506.293: [Full GC 2495506.293: [CMS (concurrent mode failure): 1179648K->1179648K(1179648K), 7.9886750 secs] 1441599K->1440570K(1441600K), [CMS Perm : 72834K->72833K(262144K)], 7.9889480 secs]
2495514.300: [Full GC 2495514.301: [CMS (concurrent mode failure): 1179648K->1179647K(1179648K), 8.7149400 secs] 1441600K->1440451K(1441600K), [CMS Perm : 72839K->72831K(262144K)], 8.7153850 secs]
2495523.446: [Full GC 2495523.446 (concurrent mode failure): 1179647K->1179647K(1179648K), 12.9441410 secs] 1441599K->1440523K(1441600K), [CMS Perm : 72832K->72831K(262144K)], 12.9444090 secs]
...
2495536.400: [Full GC 2495536.400: [CMS (concurrent mode failure): 1179647K->1179647K(1179648K), 7.8316830 secs] 1441599K->1440330K(1441600K), [CMS Perm : 72831K->72824K(262144K)], 7.8319710 secs]
2495544.248: [Full GC 2495544.249: [CMS (concurrent mode failure): 1179648K->1179647K(1179648K), 7.2906420 secs] 1441599K->1440451K(1441600K), [CMS Perm : 72835K->72827K(262144K)], 7.2910060 secs]
2495551.565: [Full GC 2495551.565: [CMS (concurrent mode failure): 1179647K->1179647K(1179648K), 7.5929380 secs] 1441599K->1440526K(1441600K), [CMS Perm : 72844K->72834K(262144K)], 7.5933150 secs]
2495559.638: [Full GC 2495559.638 (concurrent mode failure): 1179647K->1179647K(1179648K), 12.0032770 secs] 1441599K->1440585K(1441600K), [CMS Perm : 72841K->72832K(262144K)], 12.0035460 secs]
```
  1. Suppose we want to get an idea of the use cases being executed related to the above garbage collection. To do this, pass in the first timestamp in the JBoss boot.log using the startdatetime  option. For example, suppose we know that JBoss was started on September 15, 2009 and see the following first line in boot.log:
```
10:50:43,343 INFO  [Server] Starting JBoss (MX MicroKernel)...
```
  1. Run again with the following options:
```
java -jar garbagecat-2.0.0.jar -t 10 -s "2009-09-15 10:50:43,343" gc.log.pp
```
  1. The report now displays the bottleneck lines with a date/time that can be used to cross reference application and/or web access logging:
```
2009-10-14 08:02:13,871: [Full GC 2009-10-14 08:02:13,871: [CMS (concurrent mode failure): 1179647K->1179648K(1179648K), 7.7971770 secs] 1441599K->1440481K(1441600K), [CMS Perm : 72831K->72830K(262144K)], 7.7974610 secs]
2009-10-14 08:02:21,681: [Full GC 2009-10-14 08:02:21,681: [CMS (concurrent mode failure): 1179648K->1179648K(1179648K), 7.9398610 secs] 1441599K->1440455K(1441600K), [CMS Perm : 72832K->72832K(262144K)], 7.9401510 secs]
2009-10-14 08:02:29,636: [Full GC 2009-10-14 08:02:29,636: [CMS (concurrent mode failure): 1179648K->1179648K(1179648K), 7.9886750 secs] 1441599K->1440570K(1441600K), [CMS Perm : 72834K->72833K(262144K)], 7.9889480 secs]
2009-10-14 08:02:37,643: [Full GC 2009-10-14 08:02:37,644: [CMS (concurrent mode failure): 1179648K->1179647K(1179648K), 8.7149400 secs] 1441600K->1440451K(1441600K), [CMS Perm : 72839K->72831K(262144K)], 8.7153850 secs]
2009-10-14 08:02:46,789: [Full GC 2495523.446 (concurrent mode failure): 1179647K->1179647K(1179648K), 12.9441410 secs] 1441599K->1440523K(1441600K), [CMS Perm : 72832K->72831K(262144K)], 12.9444090 secs]
...
2009-10-14 08:02:59,743: [Full GC 2009-10-14 08:02:59,743: [CMS (concurrent mode failure): 1179647K->1179647K(1179648K), 7.8316830 secs] 1441599K->1440330K(1441600K), [CMS Perm : 72831K->72824K(262144K)], 7.8319710 secs]
2009-10-14 08:03:07,591: [Full GC 2009-10-14 08:03:07,592: [CMS (concurrent mode failure): 1179648K->1179647K(1179648K), 7.2906420 secs] 1441599K->1440451K(1441600K), [CMS Perm : 72835K->72827K(262144K)], 7.2910060 secs]
2009-10-14 08:03:14,908: [Full GC 2009-10-14 08:03:14,908: [CMS (concurrent mode failure): 1179647K->1179647K(1179648K), 7.5929380 secs] 1441599K->1440526K(1441600K), [CMS Perm : 72844K->72834K(262144K)], 7.5933150 secs]
2009-10-14 08:03:22,981: [Full GC 2495559.638 (concurrent mode failure): 1179647K->1179647K(1179648K), 12.0032770 secs] 1441599K->1440585K(1441600K), [CMS Perm : 72841K->72832K(262144K)], 12.0035460 secs]
```
