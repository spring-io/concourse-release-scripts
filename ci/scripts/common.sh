source /opt/concourse-java.sh

# Get the revision from a build-info.json file
get_revision_from_buildinfo() {
  cat build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/'
}

