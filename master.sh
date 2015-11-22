#!/bin/bash

mvn nexus-staging:release --settings settings.xml -Prelease -Dmaven.test.skip -B

