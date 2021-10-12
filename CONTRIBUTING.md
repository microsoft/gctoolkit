# Contributing

This project welcomes contributions and suggestions. Most contributions require you to
agree to a Contributor License Agreement (CLA) declaring that you have the right to,
and actually do, grant us the rights to use your contribution. For details, visit
[https://cla.microsoft.com](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need
to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the
instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/)
or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Submit an Issue

if you wish to contribute to GCToolKit we would kindly ask that you submit an issue to the issue tracker. Doing so will help with the management of the project.
 
## Build

The build uses the Maven wrapper (`mvnw`) to help ensure reproducible builds and so we don't force you to change your system Maven install.

* `mvnw clean` - remove build artifacts
* `mvnw compile` - compile the source code

## Test

You can execute test cases with following command.

* `mvnw test` - run unit tests (this project uses JUnit 5)

## Package

The packaging is vanilla Maven.

* `mvnw package` - create the binaries.

## Deploy / Publish

This is a task performed by the core project maintainers, if you think they're behind or would like to get a release out please raise a GitHub issue.
