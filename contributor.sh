#!/bin/bash

# This script will enable/disable the 'contributor' profile to ensure all 
# tests and checks pass, since the GC logs will be available.

# Turn on the Maven Config file with Contributor Profile activated
enable() {
    if [ -f .mvn/maven.config.contributor ]; then
        cp .mvn/maven.config.contributor .mvn/maven.config
    fi
    echo "Maven 'contributor' profile activated. See ./mvn/maven.config"
}

disable() {
    if [ -f .mvn/maven.config ]; then
        rm .mvn/maven.config
    fi
    echo "./mvn/maven.config file removed."
}

# Prints usage help
printhelp() {
    cat << EOF
Microsoft GCToolKit
Copyright (c) 2021, Microsoft Corporation

$ contributor.sh [-e] | [-d] | [-h]
Arguments:
-e: enables the 'contributor' profile by creating .mvn/maven.config file with -Pcontributor.
-d: disables the 'contributor' profile by deleting .mvn/maven.config file.
EOF
    exit 0
}

# MAIN

# Parse flags

if [ "$#" -eq 0 ] || [ "$#" -gt 1 ]; then 
    printhelp;
fi

eflag=0
dflag=0

while getopts "hed" optname; do
  case "$optname" in
    "h")
      printhelp
      ;;
    "d")
      dflag=1
      ;;
    "e")
      eflag=1
      ;;
    *)
      echo "Unknow argument. See ./contributor.sh -h"
      ;;
  esac
done

if [ $dflag -eq 1 ] && [ "$#" -eq 1 ]; then
    disable
elif [ $eflag -eq 1 ] && [ "$#" -eq 1 ]; then
    enable
else
    printhelp
fi    
