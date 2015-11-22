#!/bin/bash

mvn clean deploy --settings settings.xml -Prelease -Dmaven.test.skip -B

