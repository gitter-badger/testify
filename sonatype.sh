#!/bin/bash
#
# Copyright 2015 Sharmarke Aden.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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