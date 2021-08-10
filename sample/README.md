# GCToolKit Sample

This sample shows how to use [GCToolKit](../README.md) to analyze a GC log file and report on total heap occupancy after a GC cycle has been completed. For more details on how GCToolKit and this sample work, visit [Introducing Microsoft GCToolKit](https://devblogs.microsoft.com/java/introducing-microsoft-gctoolkit/).

## To run the sample

### From Maven

Compile the sample with `mvn compile`, then use `mvn exec:exec` to run the sample.

```shell
$ mvn clean compile
$ mvn exec:exec
```

By default, the sample analyzes `../gclogs/preunified/cms/defnew/details/defnew.log`. Set the parameter `gcLogPath` to analyze a different GC log file.

```shell
mvn exec:exec -DgcLogFile=../gclogs/unified/parallel/parallelgc.log
```

### From the command line

The sample can also be run from the command line with Java 11 or higher. Compile the sample with `mvn compile dependency:copy-dependencies`,
then run `java` with `--module-path` and give it the path to a GC log file as an argument.

```shell
$ mvn clean compile dependency:copy-dependencies
$ java java --module-path target/classes:target/lib --module gctoolkit.sample/com.microsoft.gctoolkit.sample.Main ../gclogs/preunified/cms/defnew/details/defnew.log
```

## Troubleshooting

### `mvn exec:java` fails

If you try to run the `exec:java` goal, you may see the following error:

`Unable to parse configuration of mojo org.codehaus.mojo:exec-maven-plugin:3.0.0:java for parameter arguments: Cannot store value into array`

This has something to do with the [Exec Maven Plugin](https://www.mojohaus.org/exec-maven-plugin/), which automatically builds the command line from the project dependencies. But it is unclear exactly what the cause of this issue is. The solution is to use the `exec:exec` goal.

### java.nio.file.NoSuchFileException: ../gclogs/preunified/cms/defnew/details/defnew.log

There are two possible issues here.

1. There is no `gclogs` directory in the _top-level_ directory. The sample uses `unit-test` data from building GCToolKit. Either run `mvn test` from the _top-level_ directory, or use `-DgcLogFile=<path-to-gc-log-file>` argument to specify a log file.
1. The sample is not being run from the `sample` directory. Please ensure you are in the sample directory before executing `mvn exec:exec`.
