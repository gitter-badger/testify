#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    if [ "$TRAVIS_BRANCH" = "master" ]; then
      echo "Releasing $TRAVIS_BRANCH' branch."
      mvn nexus-staging-maven-plugin:release --settings settings.xml -Prelease -Dmaven.test.skip -B -T 1C

    elif [ "$TRAVIS_BRANCH" = "develop" ]; then
      echo "Staging '$TRAVIS_BRANCH' branch"
      mvn clean deploy --settings settings.xml -Prelease -Dmaven.test.skip -B -T 1C

    else
        echo "Unknown '$TRAVIS_BRANCH' branch. Artifacts not deployed."
    fi
fi
