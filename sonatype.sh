#!/bin/bash

if [ "$TRAVIS_BRANCH" = "master" ]; then
  echo "Releasing $TRAVIS_BRANCH' branch."
  mvn nexus-staging:release --settings settings.xml -Prelease -Dmaven.test.skip -B

elif [ "$TRAVIS_BRANCH" = "develop" ]; then
  echo "Staging '$TRAVIS_BRANCH' branch"
  mvn clean deploy --settings settings.xml -Prelease -Dmaven.test.skip -B

else
    echo "Unknown '$TRAVIS_BRANCH' branch. Artifacts not deployed."
fi
