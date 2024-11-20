#!/bin/bash

set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_HOME=$SCRIPT_DIR/../..

cd $PROJECT_HOME

GIT_AUTHOR_NAME='Git' \
GIT_AUTHOR_EMAIL='noreply@github.com' \
GIT_COMMITTER_NAME='Git' \
GIT_COMMITTER_EMAIL='noreply@github.com' \
./mvnw -B -DskipTests clean release:clean release:prepare -Prelease -DpushChanges=false
