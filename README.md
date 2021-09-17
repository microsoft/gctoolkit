# Microsoft GCToolKit

GCToolkit is a set of libraries for analyzing HotSpot Java garbage collection (GC) log files. The toolkit parses GC log files into discrete events and provides an API for aggregating data from those events. This allows the user to create arbitrary and complex analyses of the state of managed memory in the Java Virtual Machine (JVM) represented by the garbage collection log.

For more detail you can read our [Launch Blog Post](https://devblogs.microsoft.com/java/introducing-microsoft-gctoolkit/).

[![GCToolKit build with Maven](https://github.com/microsoft/gctoolkit/actions/workflows/maven.yml/badge.svg)](https://github.com/microsoft/gctoolkit/actions/workflows/maven.yml)

---

## Test Coverage Report

**Core API Coverage** </br>![Coverage](.github/badges/jacoco-api-coverage.svg)

**Core :: Parser</br>**![Coverage::Core::Parser](.github/badges/jacoco-parser-coverage.svg)

**Core :: Vertx**</br>![Coverage::Core::Parser](.github/badges/jacoco-vertx-coverage.svg)

---

## Introduction

Managed memory in the Java Virtual Machine (JVM) is composed of 3 main pieces:

1. Memory buffers known as Java heap
1. Allocators which perform the work of getting data into Java heap
1. Garbage Collection (GC).

While GC is responsible for recovering memory in Java heap that is no longer in use, the term is often used as a euphemism for memory management. The phrasing of _Tuning GC_ or _tuning the collector_ are often used with the understanding that it refers to tuning the JVM’s memory management subsystem. The best source of telemetry data for tuning GC comes from GC Logs and GCToolKit has been helpful in making this task easier by providing parsers, models and an API to build analytics with. You can run the Maven project [HeapOccupancyAfterCollectionSummary sample](./sample/README.md) as an example of this.

## Usage

In order to use this library you'll need to add its dependencies to your project. We provide the instructions for Maven below.

### Maven Coordinates

The GCToolKit artifacts are in GitHub packages. To use the GCToolKit artifacts as dependencies in your project, `github` must be added as a repository in your POM file.

```xml
<repositories>
    ...
    
    <repository>
        <id>github</id>
        <name>GCToolKit packages</name>
        <url>https://maven.pkg.github.com/microsoft/*</url>
    </repository>
    
    ...
</repositories>
```

You'll then want to add the `api`, `parser` and `vertx` modules to your project in the `dependencyManagement` and/or `dependencies` section as you see fit.

```xml
<dependencies>
    ...
    
    <dependency>
        <groupId>com.microsoft.gctoolkit</groupId>
        <artifactId>api</artifactId>
        <version>2.0.1</version>
    </dependency>
    
    <dependency>
        <groupId>com.microsoft.gctoolkit</groupId>
        <artifactId>parser</artifactId>
        <version>2.0.1</version>
    </dependency>
    
    <dependency>
        <groupId>com.microsoft.gctoolkit</groupId>
        <artifactId>vertx</artifactId>
        <version>2.0.1</version>
    </dependency>

    ...
</dependencies>
```

### Example

See [sample/README](./sample/README.md)

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for full details including more options for building and testing the project.

## License

Microsoft GCToolKit is licensed under the [MIT](https://github.com/microsoft/gctoolkit/blob/master/LICENSE) license.
