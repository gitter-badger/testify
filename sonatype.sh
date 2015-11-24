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
    SKIP_TESTS="-DskipTests=true -Dmaven.test.skip=true"
    MAVEN_SETTINGS="--settings settings.xml"
    RELEASE="-Prelease"
    if [ "$TRAVIS_BRANCH" = "master" ]; then
        echo "Deploying release artifacts of '$TRAVIS_BRANCH' branch"
        echo "Staging '$TRAVIS_BRANCH' branch"
        mvn clean deploy $MVN_SETTINGS $RELEASE $SKIP_TESTS -B
        echo "Releasing '$TRAVIS_BRANCH' branch"
        mvn nexus-staging:release MVN_SETTINGS $SKIP_TESTS -B
        PROJECT_VERSION=$(mvn -q org.codehaus.mojo:exec-maven-plugin:1.4.0:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive)
        echo "Creating GitHub 'testify-$PROJECT_VERSION' Release"
        curl -H "Content-Type: application/json" -X POST \
        -d '{
          "tag_name": "'"testify-$PROJECT_VERSION"'",
          "target_commitish": "master",
          "name": "'"Testify Release v$PROJECT_VERSION"'",
          "body": "'"Testify Release v$PROJECT_VERSION"'",
          "draft": false,
          "prerelease": false
        }' \
        https://api.github.com/repos/FitburIO/testify/releases?access_token=$RELEASE_TOKEN

    elif [ "$TRAVIS_BRANCH" = "develop" ]; then
        echo "Deploying snapshot artifacts of '$TRAVIS_BRANCH' branch"
        mvn clean deploy $MVN_SETTINGS $RELEASE $SKIP_TESTS -B

    else
        echo "Unknown '$TRAVIS_BRANCH' branch. Artifacts not deployed."
    fi
fi

echo "All Done! Keep on Testifying!"
