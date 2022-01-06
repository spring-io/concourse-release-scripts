#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
./gradlew -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false --no-daemon --max-workers=4 -PdeploymentRepository=${repository} build publishAllPublicationsToDeploymentRepository
version=$( awk -F '=' '$1 == "version" { print $2 }' gradle.properties )
popd > /dev/null

cp git-repo/build/concourse-release-scripts.jar built-artifact/
echo $version > built-artifact/version

