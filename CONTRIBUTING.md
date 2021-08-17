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


## Prerequisite for Contributing on  GCTooKit

The gctoolkit build relies on test data which is archived in [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry). This requires you to [authenticate to GitHub packages with a personal access token (PAT)](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token) to build and test.

If your organization uses Single Sign-On (SSO), also follow the directions under [Authorizing a personal access token for use with SAML single sign-on](https://docs.github.com/en/github/authenticating-to-github/authenticating-with-saml-single-sign-on/authorizing-a-personal-access-token-for-use-with-saml-single-sign-on).

You must also add `github` as a server in your `~/.m2/settings.xml` file. Replace `USERNAME` with your GitHub user name and `TOKEN` with your PAT.

```xml
    <server>
      <id>github</id>
      <username>USERNAME</username>
      <password>TOKEN</password>
    </server>
```

### To Test

Once above steps are configured you can execute testcases with following command.

* `mvn test -Pcontributor` - run unit tests (this project uses TestNG)

### Additional build properties
* `skipUnpack` - boolean. Defaults to `false`. This tells the build to skip unpacking the gctoolkit-testdata logs.
  If the test data has already be extracted to the gclogs directory, setting this property to `true` can save
  a minute or so of build time.

### Contributor maven profile
* `-Pcontributor` maven profile will  enable downloading test data,checks that are required for build and running unit tests .
  
