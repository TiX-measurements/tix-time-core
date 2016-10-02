#!/usr/bin/env bash
if [ "$TRAVIS_REPO_SLUG" == "TiX-measurements/tix-time-core" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "deploy" ]; then
  if [[ $(./gradlew -q getVersion) == *SNAPSHOT* ]]; then
      echo 'The deploy branch is only to deploy final releases. No snapshots allowed.'
      exit 0
  fi

  echo -e "Starting publish to Sonatype...\n"

  ./gradlew uploadArchives -PnexusUsername="${NEXUS_USERNAME}" -PnexusPassword="${NEXUS_PASSWORD}" -Psigning.keyId="${SIGNING_KEY_ID}" -Psigning.password="${SIGNING_PASSWORD}" -Psigning.secretKeyRingFile=.utility/secring.gpg
  RETVAL=$?

  if [ $RETVAL -eq 0 ]; then
    echo 'Completed publish!'
  else
    echo 'Publish failed.'
    exit 1
  fi
fi
