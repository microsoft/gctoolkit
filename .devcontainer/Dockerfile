# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.192.0/containers/ubuntu/.devcontainer/base.Dockerfile

# [Choice] Ubuntu version: bionic, focal
ARG VARIANT="focal"
FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIANT}

# Install Microsoft Build of OpenJDK 11
ENV JAVA_HOME /usr/lib/jvm/msopenjdk-11-amd64
ENV PATH "${JAVA_HOME}/bin:${PATH}"

COPY --from=mcr.microsoft.com/openjdk/jdk:11-ubuntu $JAVA_HOME $JAVA_HOME
