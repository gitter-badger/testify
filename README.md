# Testify

[![Join the chat at https://gitter.im/FitburIO/testify](https://badges.gitter.im/FitburIO/testify.svg)](https://gitter.im/FitburIO/testify?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/FitburIO/testify.svg?branch=develop)](https://travis-ci.org/FitburIO/testify)
[![CodecovIO](https://codecov.io/github/FitburIO/testify/coverage.svg?branch=develop)](https://codecov.io/github/FitburIO/testify?branch=develop)
[![Latest Release](https://img.shields.io/github/release/FitburIO/testify.svg)](https://github.com/FitburIO/testify/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fitbur.testify/parent/badge.svg?style=flat)](http://repo1.maven.org/maven2/com/fitbur/testify/)
[![License](https://img.shields.io/github/license/FitburIO/testify.svg)](LICENSE)

[![Throughput Graph](https://graphs.waffle.io/FitburIO/testify/throughput.svg)](https://waffle.io/FitburIO/testify/metrics)

## Overview
Testify is an annotation driven Java Testing Framework that provides uniform
and seamless way to write Unit, Integration, and System tests.

### Completed Features
- Uniform Annotations for Unit, Integration and System Testing
- Managed Test Case Configuration and Isolation
- JSR-330 Dependency Injection Annotations Support
- JUnit Testing Framework Support
- Full Integration with Mockito
- Pluggable Test Need API
 - In-Memory HSQL
- Pluggable Container Test Needs API
 - Docker Container Supported
- Pluggable Server API
 - Undertown Server Supported
- Pluggable Client API
 - Jersey Client Supported
- Spring Integration Testing
- Spring MVC/REST System Testing
- Spring Boot System Testing

### Planned Features
- JUnit
 - HK2 Integration Testing
 - Jersey 2 System Testing
 - Spring Boot System Testing
 - DropWizard System Testing
 - Guice Integration Testing
- TestNG
 - Unit Testing
 - Spring Integration Testing
 - HK2 Integration Testing
 - Guice Integration Testing
 - Jersey 2 System Testing
 - Spring System Testing
 - Spring Boot System Testing
 - DropWizard System Testing

## Versioning
Testify has an automated release system and uses [Semantic Versioning](http://semver.org/)
version numbering system.
```
major.minor.patch
```

| number | meaning                                                                    |
| ------ | -------------------------------------------------------------------------- |
| major  | major version, with most probably incompatible change in API and behavior  |
| minor  | minor version, important enough change to bump this number                 |
| patch  | a released build number incremented automatically a pull request is merged |

## Documentation
The documentation is available [here](http://fitburio.github.io/testify/). Be
sure to look at the [example code](https://github.com/FitburIO/testify/tree/master/examples)

## Issue Tracking
Report issues via the [Testify Issues](https://github.com/FitburIO/testify/issues).
Think you've found a bug? Please consider submitting a reproduction project via
the [Testify Issue](https://github.com/FitburIO/testify/issues).

## Building from Source
Testify uses a [Maven](https://maven.apache.org/)-based build system. To build
from source follow the bellow instructions:

### Prerequisites
- Git v1.9.1  or above
- JDK 8 (be sure to set `JAVA_HOME`)
- Maven 3.0.5 or above

### Check out sources
`git clone git@github.com:FitburIO/testify.git`

or

`git clone https://github.com/FitburIO/testify.git`

### Install all Testify jars into your local Maven cache
`mvn install -Dmaven.test.skip`

### Compile and test and build all jars
`mvn clean install`

## Contributing
[Pull requests](http://help.github.com/send-pull-requests) are welcome.

## Staying in Touch
TODO

## License
The Testify is released under [Apache Software License, Version 2.0](LICENSE).

Enjoy and keep on Testifying!
