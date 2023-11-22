#!/bin/bash

# Build the WAR file that will be installed in the Docker image.

set -exo pipefail

# Build the SiaV2 service
cd sia2
../gradlew --stacktrace --info clean build test
