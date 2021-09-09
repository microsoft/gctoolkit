#!/bin/bash -x

# This script is an alternative method for not having to deal
# with GitHub Package Registry.
# 
# By calling this script, the latest source code of gctoolkit-testdata 
# will be downloaded, and the project will be built and installed in the
# local Maven repository.
# 
# This script will also enable the 'contributor' profile to ensure all 
# tests and checks pass, since the GC logs will be available.

# Turn on the Maven Config file with Contributor Profile activated
activate() {
    if [ -f .mvn/maven.config.contributor ]; then
        cp .mvn/maven.config.contributor .mvn/maven.config
    fi
    echo "Maven 'contributor' profile activated. See ./mvn/maven.config"
}

revert() {
    if [ -f .mvn/maven.config ]; then
        rm .mvn/maven.config
    fi
    echo "./mvn/maven.config file removed."
}

# Download latest GCToolkit Test Data pack
download() {
    rm -rf gctoolkit-testdata

    wget $(curl -s https://api.github.com/repos/microsoft/gctoolkit-testdata/releases/latest | grep 'zipball_url' | cut -d\" -f4) -O gctoolkit-testdata.zip

    # Extract to ../gctoolkit-testdata
    unzip gctoolkit-testdata.zip
    rm gctoolkit-testdata.zip

    # Rename folder, as it comes with git hash in the name
    mv microsoft-gctoolkit-testdata-* gctoolkit-testdata

    # Remove -SNAPSHOT (See issue: https://github.com/microsoft/gctoolkit-testdata/issues/6)
    sh mvnw -f gctoolkit-testdata versions:set -DremoveSnapshot
    sh mvnw -f gctoolkit-testdata install

    rm -rf gctoolkit-testdata

    echo "GCToolKit Test Data downloaded and installed in your local Maven repository."
}

# Prints usage help
printhelp() {
    cat << EOF
Microsoft GCToolKit Test Data
Copyright (c) 2021, Microsoft Corporation

$ contributor.sh [ [-d] [-a] | [-r] ] | [-h]
Arguments:
-d: downloads gctoolkit-testdata, builds and installs artifacts in local Maven repository.
-a: activates the 'contributor' profile by creating Fa .mvn/maven.config file with -Pcontributor.
-r: reverts the .mvn/maven.config file to disable 'contributor' Maven profile.
EOF
    exit 0
}

# MAIN

# Parse flags

if [ "$#" -eq 0 ]; then printhelp; fi

dflag=0
aflag=0
rflag=0

while getopts "hdar" optname; do
  case "$optname" in
    "h")
      printhelp
      ;;
    "d")
      dflag=1
      ;;
    "a")
      aflag=1
      ;;
    "r")
      rflag=1
      ;;
    *)
      echo "Unknow argument. See ./contributor.sh -h"
      ;;
  esac
done

if [ $((dflag + rflag)) -gt 1 ]; then
    printhelp
elif [ $dflag -eq 1 ]; then
    download
fi

if [ $((aflag + rflag)) -gt 1 ]; then
    printhelp
elif [ $aflag -eq 1 ]; then
    activate
elif [ $rflag -eq 1 ]; then
    revert
else
    printhelp
fi
