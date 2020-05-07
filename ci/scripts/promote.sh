#!/bin/bash

source $(dirname $0)/common.sh

version=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )
export BUILD_INFO_LOCATION=$(pwd)/artifactory-repo/build-info.json

released=$(find ./artifactory-repo -name "concourse-release-scripts-*.jar")
cp $released /concourse-release-scripts.jar

java -jar /concourse-release-scripts.jar promote $RELEASE_TYPE $BUILD_INFO_LOCATION > /dev/null || { exit 1; }

java -jar /concourse-release-scripts.jar distribute $RELEASE_TYPE $BUILD_INFO_LOCATION > /dev/null || { exit 1; }

echo "Promotion complete"
echo $version > version/version
