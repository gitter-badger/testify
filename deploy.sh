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

SKIP_TESTS="-DskipTests=true -Dmaven.test.skip=true"
MAVEN_SETTINGS="--settings settings.xml"
RELEASE="-Prelease"

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    echo "Deploying '$TRAVIS_BRANCH' branch"
    if [ "$TRAVIS_BRANCH" = "master" ]; then
        PROJECT_VERSION=$(mvn -q org.codehaus.mojo:exec-maven-plugin:1.4.0:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive)
        echo "Staging v$PROJECT_VERSION release artifacts"
        mvn clean deploy $MAVEN_SETTINGS $RELEASE $SKIP_TESTS -B

        echo "Releasing artifacts"
        mvn nexus-staging:release $MAVEN_SETTINGS $SKIP_TESTS -B

    elif [ "$TRAVIS_BRANCH" = "develop" ]; then
        echo "Snapshoting v$PROJECT_VERSION"
        echo "Deploying snapshot artifacts"
        mvn clean deploy $MAVEN_SETTINGS $RELEASE $SKIP_TESTS -B

    else
        echo "Branch '$TRAVIS_BRANCH' not a master or develop. Artifacts will not be deployed."
    fi

fi

echo "All Done! Keep on Testifying!"
