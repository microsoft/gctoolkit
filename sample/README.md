## GCToolKit introductory sample

This sample shows how to use [GCToolKit](../README.md) to analyze a GC log file and report on 
total heap occupancy after a GC cycle has been completed. For more details on how GCToolKit and 
this sample work, visit [Introducing Microsoft GCToolKit](https://devblogs.microsoft.com/java/introducing-microsoft-gctoolkit/).

### To run the sample

#### From Maven

Compile the sample with `mvn compile`, then use `mvn exec:exec` to run the sample. 

```shell
$ mvn clean compile
$ mvn exec:exec
```

By default, the sample analyzes `../gclogs/preunified/cms/defnew/details/defnew.log`. Set the parameter
`gcLogPath` to analyze a different GC log file.
```shell
mvn exec:exec -DgcLogFile=../gclogs/unified/parallel/parallelgc.log
```

#### From the command line
The sample can also be run from the command line with Java 11 or higher. Compile the sample with `mvn compile dependency:copy-dependencies`,
then run `java` with `--module-path` and give it the path to a GC log file as an argument.
```shell
$ mvn clean compile dependency:copy-dependencies
$ java java --module-path target/classes:target/lib --module gctoolkit.sample/com.microsoft.gctoolkit.sample.Main ../gclogs/preunified/cms/defnew/details/defnew.log
```

### Troubleshooting

<dl>
<dt>mvn exec:java fails</dt>
<dd>
If you try to run the <code><nobr>exec:java</nobr></code> goal, you may see the following error:
<pre>Unable to parse configuration of mojo org.codehaus.mojo:exec-maven-plugin:3.0.0:java for parameter arguments: Cannot store value into array</pre>
This has something to do with the <a href="https://www.mojohaus.org/exec-maven-plugin/)">Exec Maven Plugin</a>, which
automatically builds the command line from the project dependencies. But it is unclear exactly what the cause of this 
issue is. The solution is to use the <code><nobr>exec:exec</nobr></code> goal.
</dd>
<dt>java.nio.file.NoSuchFileException: ../gclogs/preunified/cms/defnew/details/defnew.log</dt>
<dd>
There are two possible issues here.
<ol>
<li>There is no <code>gclogs</code> directory in the  <nobr>top-level</nobr> directory. The sample uses <nobr>unit-test</nobr> data from building GCToolKit.
Either run <code><nobr>mvn test</nobr></code> from the <nobr>top-level</nobr> directory, or use 
<code><nobr>-DgcLogFile=&lt;path-to-gc-log-file&gt;</nobr></code> argument to specify a log file.</li> 
<li>The sample is not being run from the <code>sample</code> directory. Please ensure you are in 
the sample directory before executing <code><nobr>mvn exec:exec</nobr></code></li>
</ol>
</dd>
</dl>