# Microsoft GCToolKit

GCToolKit is a set of libraries for analyzing ***Hotspot*** Java garbage collection (GC) log files. The toolkit parses GC log files into discrete events and provides an API for aggregating data from those events. This allows the user to create arbitrary and complex analyses of the state of managed memory in the Java Virtual Machine (JVM) represented by the garbage collection log.

[![GCToolKit build with Maven](https://github.com/microsoft/gctoolkit/actions/workflows/maven.yml/badge.svg)](https://github.com/microsoft/gctoolkit/actions/workflows/maven.yml)

---
**NOTE**

The gctoolkit build relies on test data which is archived in [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry). This requires you to [authenticate to GitHub packages with a personal access token (PAT)](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token) 
to build and test. 

If your organization uses Single Sign-On (SSO), also follow the directions under [Authorizing a personal access token for use with SAML single sign-on](https://docs.github.com/en/github/authenticating-to-github/authenticating-with-saml-single-sign-on/authorizing-a-personal-access-token-for-use-with-saml-single-sign-on).

You must also add github as a server in your `~/.m2/settings.xml` file. Replace USERNAME with your GitHub user name and TOKEN with your PAT.
```xml
    <server>
      <id>github</id>
      <username>USERNAME</username>
      <password>TOKEN</password>
    </server>
```
---
## Getting Started

### Maven Coordinates
```xml
<repository>
    <id>github</id>
    <name>GCToolKit packages</name>
    <url>https://maven.pkg.github.com/microsoft/*</url>
</repository>
```

```xml
<dependency>
    <groupId>com.microsoft.gctoolkit</groupId>
    <artifactId>api</artifactId>
    <version>2.0.1-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.microsoft.gctoolkit</groupId>
    <artifactId>parser</artifactId>
    <version>2.0.1-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.microsoft.gctoolkit</groupId>
    <artifactId>vertx</artifactId>
    <version>2.0.1-SNAPSHOT</version>
</dependency>
```
### Example

## Build and Test

The build is vanilla Maven.

<br/>`mvn clean` - remove build artifacts
<br/>`mvn compile` - compile the source code
<br/>`mvn test` - run unit tests (this project uses TestNG)
<br/>`mvn package` - build the .jar file

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, view [Microsoft's CLA](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## License

Microsoft JFR Streaming Library is licensed under the [MIT](https://github.com/microsoft/jfr-streaming/blob/master/LICENSE) license.
