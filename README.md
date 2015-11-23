# Testify
[![Build Status](https://travis-ci.org/FitburIO/testify.svg?branch=develop)](https://travis-ci.org/FitburIO/testify)
[![codecov.io](https://codecov.io/github/FitburIO/testify/coverage.svg?branch=develop)](https://codecov.io/github/FitburIO/testify?branch=develop)
[![Maven Central](https://img.shields.io/maven-central/v/com.fitbur.testify/parent.svg)]()
[![License](https://img.shields.io/github/license/FitburIO/testify.svg)](LICENSE)

## Overview
Testify is an annotation driven Java testing framework integrated with Mockito to provide uniform and seamless way to write unit tests, integration tests, and system tests using your prefered Dependency Injection Framework and Test Runner Framework.

### Completed Features
* JUnit
  * Unit Testing
  * Spring Integration Testing

### Planned Features
* JUnit
  * Spring System Tests
  * HK2 Integration Testing
  * Jersey 2 System Testing
  * Guice Integration Testing
* TestNG based unit tests
  * Unit Testing
  * Spring Integration Testing
  * Spring System Testing
  * HK2 Integration Testing
  * Jersey 2 System Testing
  * Guice Integration Testing


## Versioning

Testify has an automated release system and uses [_semver_](http://semver.org/) version numbering system.

```
major.minor.patch
```

| number | meaning                                                                               |
| ------ | ------------------------------------------------------------------------------------- |
| major  | major version, with most probably incompatible change in API and behavior             |
| minor  | minor version, important enough change to bump this number                            |
| patch  | a released build number incremented automatically a pull request is merged            |

## Documentation
TODO

## Getting Support
TODO

## Issue Tracking
Report issues via the [Testify Issues](https://github.com/FitburIO/testify/issues). Think you've found a
bug? Please consider submitting a reproduction project via the
[Testify Issue](https://github.com/FitburIO/testify/issues).

## Building from Source
Testify uses a [Maven][]-based build system. To build from source follow the bellow instructions:

### Prerequisites
* Git v1.9.1  or above
* JDK 8 (be sure `JAVA_HOME` is set)
* Maven 3.0.5 or above

### Check out sources
`git clone git@github.com:FitburIO/testify.git`

or

`git clone https://github.com/FitburIO/testify.git`

### Install all testify-\* jars into your local Maven cache
`mvn install -Dmaven.test.skip`

### Compile and test; build all jars, distribution zips, and docs
`mvn clean install`

## Contributing
[Pull requests](http://help.github.com/send-pull-requests) are welcome.

## Staying in Touch
TODO

## License
The Testify is released under version 2.0 of the Apache License.



Enjoy Testify and keep on testifying!


