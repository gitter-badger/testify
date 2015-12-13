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

## [0.0.4] - 2015-12-10
### Changed
- Changed Mock annotation to Fake

## [0.0.5] - 2015-12-10
- Changed UnitTestRunner to UnitTest
- Changed SpringIntegrationTestRunner to SpringIntegrationTest
- Host of fixes for issue relating to test class injection

