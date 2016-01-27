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
set -ev

MVN_SETTINGS="--settings settings.xml"
PROJECT_VERSION=$(mvn -q org.codehaus.mojo:exec-maven-plugin:1.4.0:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive)

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    if [ "$TRAVIS_BRANCH" = "master" ]; then
        echo "Deploying Testify $PROJECT_VERSION Release Artifacts"

        mvn -B -Prelease clean deploy $MVN_SETTINGS

        if [ $? -eq 0 ]; then
            echo "Release Deployment Successful"
            exit 0
        else
            echo "Release Deployment Failed"
            exit 1
        fi

    elif [ "$TRAVIS_BRANCH" = "develop" ]; then
        echo "Deploying Testify $PROJECT_VERSION Snapshot Artifacts"

        mvn -B -Pstage clean deploy $MVN_SETTINGS

        if [ $? -eq 0 ]; then
            echo "Deployment Successful"
            exit 0
        else
            echo "Deployment Failed"
            exit 1
        fi

    else
        echo "Deployment of branch '$TRAVIS_BRANCH' not supported."
    fi
fi

echo "All Done! Keep on Testifying!"
exit 0
