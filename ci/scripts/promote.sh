#!/bin/bash

source $(dirname $0)/common.sh

pushd artifactory-repo > /dev/null
version=$( get_revision_from_buildinfo )
popd > /dev/null

export BUILD_INFO_LOCATION=$(pwd)/artifactory-repo/build-info.json

released=$(find ./artifactory-repo -name "concourse-release-scripts-*.jar")
cp $released /concourse-release-scripts.jar

java -jar /concourse-release-scripts.jar promote $RELEASE_TYPE $BUILD_INFO_LOCATION > /dev/null || { exit 1; }

echo "Promotion complete"
echo $version > version/version

cp $released built-artifact/concourse-release-scripts.jar
echo $version > built-artifact/version
