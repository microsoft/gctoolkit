#!/bin/bash

set -euxo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_HOME=$SCRIPT_DIR/../..

cd $PROJECT_HOME

previous_release_version=$( \
  curl --location --silent \
  -H "Accept: application/vnd.github+json" \
  https://api.github.com/repos/microsoft/gctoolkit/releases/latest | grep 'tag_name' \
)
previous_release_version=$( perl -pe 's/"tag_name":\s+"(.*?)",?/$1/' <<< "${previous_release_version}" )

release_version=$( git tag --sort=-taggerdate --list | head -n 1 )

./mvnw -B -pl :gctoolkit -Pjreleaser jreleaser:release \
  -Djreleaser.previous.tag.name="${previous_release_version}" \
  -Djreleaser.tag.name="${release_version}"

