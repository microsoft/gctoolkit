# GCToolKit introductory sample

This sample shows how to use [GCToolKit](../README.md) to analyze a GC log file and report on total heap occupancy after a GC cycle has been completed. For more details on how GCToolKit and this sample work, visit [Introducing Microsoft GCToolKit](https://devblogs.microsoft.com/java/introducing-microsoft-gctoolkit/).

## To run the sample

We use the Maven wrapper (`mvnw`) so you don't have to change your system wide Maven.

### From Maven

Build and install the reactor from the repository root. Then run the sample from the `sample` directory so its default relative log path resolves correctly.

```shell
./mvnw clean install
cd sample
../mvnw exec:exec
```

By default, the sample analyzes `../gclogs/preunified/cms/defnew/details/defnew.log`. Set the property `gcLogFile` to analyze a different GC log file.

```shell
../mvnw exec:exec -DgcLogFile=/path/to/gc.log
```

### From the command line

The sample can also be run from the command line with Java 11 or higher. From the `sample` directory, after installing the reactor as shown above, compile the sample with `../mvnw compile dependency:copy-dependencies`,
then run `java` with `--module-path` and give it the path to a GC log file as an argument.

```shell
../mvnw clean compile dependency:copy-dependencies
java --module-path target/classes:target/lib --module com.microsoft.gctoolkit.sample/com.microsoft.gctoolkit.sample.Main ../gclogs/preunified/cms/defnew/details/defnew.log
```

## Troubleshooting

### `mvnw exec:java` fails

If you try to run the `exec:java` goal, you may see the following error:

`Unable to parse configuration of mojo org.codehaus.mojo:exec-maven-plugin:3.0.0:java for parameter arguments: Cannot store value into array`

This has something to do with the [Exec Maven Plugin](https://www.mojohaus.org/exec-maven-plugin/), which automatically builds the command line from the project dependencies. But it is unclear exactly what the cause of this issue is. The solution is to use the `exec:exec` goal.

### java.nio.file.NoSuchFileException: ../gclogs/preunified/cms/defnew/details/defnew.log

There are two possible issues here.

1. The default log file is missing. Run `./mvnw test` from the repository root to download the test data, or pass `-DgcLogFile=<path-to-gc-log-file>` to use your own log.
1. The sample is not being run from the `sample` directory. Please ensure you are in the sample directory before executing `../mvnw exec:exec`.
