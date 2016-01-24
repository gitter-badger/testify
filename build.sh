#!/bin/bash
#
# Copyright 2016 Sharmarke Aden.
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

MVN_SETTINGS=" --settings settings.xml"
PROJECT_VERSION=$(mvn -q org.codehaus.mojo:exec-maven-plugin:1.4.0:exec -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive)
echo "Building Testify v$PROJECT_VERSION"

mvn verify -B -Pbuild $MVN_SETTINGS

STATUS=$?

if [ $STATUS -eq 0 ]; then
    echo "Build Successful"
    exit 0
else
    echo "Build Failed"
    exit 1
fi
