# Change Log
All notable changes to this project will be documented in this file. This project
adheres to [Semantic Versioning](http://semver.org/). The change log file consists
of sections listing each version and the date they were released along with what
was added, changed, deprecated, removed, fix and security fixes.

- Added - Lists new features
- Changed - Lists changes in existing functionality
- Deprecated -  Lists once-stable features that will be removed in upcoming releases
- Removed - Lists deprecated features removed in this release
- Fixed - Lists any bug fixes
- Security - Lists security fixes to security vulnerabilities

## [Unreleased]
### Added
- Added methods to the NeedDescriptor interface to get service locator and
test method name
- A common TestNeedDescriptor implementation to core
### Removed
- Spring NeedDescriptor implementations in favor of TestNeedDescriptor
### Changed
- NeedProvider init method is now optional
- Simplified InMemoryHSQL (removed init and destroy code)

## [0.0.1] - 2015-11-23
### Added
- Initial release
- Added JUnit unit testing support
- Added JUnit Spring integration testing support

## [0.0.2] - 2015-11-26
### Added
- Dependency, configuration and wiring verification and logging.
- Added CHANGELOG.md
- Exclusion of debug information from release artifact .class files.
- Updated README.md with Github Releases shield

## [0.0.3] - 2015-12-06
### Added
- NeedProvider (replaces TestNeed)
- Resource and Resources (replaces WebResource)
- Ability to inject a List or Set of services of a certain type
- Ability to inject a Map containing service name and instance entries
- Ability to inject a service by name using @javax.inject.Named qualifier annotation

### Changed
- Moved di-api code to the api module
- Moved need-api code to the api module and
- Updated API JavaDocs
- Added more configuration, dependency and wiring verification check
- Simplified JUnit unit and integration runners and listeners
- Refined logging and wiring, configuration, and dependency exception handling
- Updated InMemoryHSQL to use the new NeedProvider interface and added need test cases
- Simplified ServiceLocator interface to use Type rather than Class

### Removed
- Removed unused Arg and Args annotations from API
- Removed di-api module (code now in api module)
- Removed need-api module (code now in api module)
- Removed TestNeed class (breaking change, use NeedProvider instead)
- Removed WebResource (use Resource instead)
- Removed TestException class in favor of IllegalStateException
- Removed HSQLTestException and HSQLContext (not necessary with the addition of NeedProvider)
- Removed Tests module in favor of testing inside modules themselves

## [0.0.4] - 2015-12-13
### Changed
- Changed Mock annotation to Fake
- Changed UnitTestRunner to UnitTest
- Changed SpringIntegrationTestRunner to SpringIntegrationTest
- Host of fixes for issue relating to test class wiring

## [0.0.5] - 2015-12-25
### Added
- Shaded third-party dependencies
- Added support for Dependency Injection Framework specific annotations

### Fixed
 - Issue #9 - Shade Guava and include it in the Distributed

### Changed
- TestInjector contract now takes FieldDescriptor

## [0.0.6] - 2016-01-06
### Added
- Support for in-server testing
- API for Server Provider and initial implementation using Undertow
- @Scan annotation to enable package scanning to load services
- Ability to support DI framework injection and qualifier annotations
- Spring JUnit System Test support

### Fixed
- Issue #18 - Testify Doesn't Handle @Ignore

## [0.0.7] - 2016-01-09
### Added
- Support for out-server testing using a http/rest client
- API for Client Provider and initial implementation using JerseyClient
- New junit-core module to share common JUnit classes
- Support for running JUnit unit, integration, system test classes in parallel
- Verification check for missing cut class in unit tests
- Ability to test bad test setup cases

### Removed
- UnitTestRunListener and moved its code to UnitTest
- SpringIntegrationTestRunListener and moved its code to the SpringIntegrationTest
- SpringSystemTestRunListener and moved its code to SpringSystemTest

### Fixed
- Fixed verification language to improve readability and consistency
- Verification message when a CUT class has multiple constructors
