#!/bin/bash

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
    rm -rf gclogs .tmp-gctoolkit-testdata

    mkdir gclogs

    wget https://github.com/microsoft/gctoolkit-testdata/archive/refs/heads/main.zip -O gctoolkit-testdata.zip

    # Extract to ../gctoolkit-testdata
    unzip gctoolkit-testdata.zip
    rm gctoolkit-testdata.zip

    # Rename folder, as it comes with git hash in the name
    mv gctoolkit-testdata-main .tmp-gctoolkit-testdata

    cp -r .tmp-gctoolkit-testdata/gctoolkit-gclogs/preunified gclogs/
    cp -r .tmp-gctoolkit-testdata/gctoolkit-gclogs/streaming gclogs/
    cp -r .tmp-gctoolkit-testdata/gctoolkit-gclogs/unified gclogs/
    cp -r .tmp-gctoolkit-testdata/gctoolkit-gclogs-rolling/rolling gclogs/
    cp -r .tmp-gctoolkit-testdata/gctoolkit-shenandoah-logs/shenandoah gclogs/
    cp -r .tmp-gctoolkit-testdata/gctoolkit-zgc-logs/zgc gclogs/

    rm -rf .tmp-gctoolkit-testdata
}

# Prints usage help
printhelp() {
    cat << EOF
Microsoft GCToolKit Test Data Downloader
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

if [ "$#" -eq 0 ] || [ "$#" -gt 2 ]; then 
    printhelp;
fi

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

if [ $rflag -eq 1 ] && [ "$#" -eq 1 ]; then
    revert
elif [ $rflag -ne 1 ]; then
    if [ $dflag -eq 1 ]; then
        download 
    fi

    if [ $aflag -eq 1 ]; then
        activate
    fi
else
    printhelp
fi    
