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
- NeedProvider to replace TestNeed
- Resource and Resources to replace WebResource

### Changed
- Moved di-api code to the api module 
- Moved need-api code to the api module and
- Updated api javadocs
- Added more configuration, dependency and wiring verification check
- Simplified JUnit unit and integration runners and listeners.
- Updated InMemoryHSQL to use the new NeedProvider api and added need test cases.

### Removed
- Removed unused Arg and Args annotations.
- Removed di-api module (code now in api module)
- Removed need-api module (code now in api module)
- Removed TestNeed class (breaking change, use NeedProvider instead)
- Removed WebResource (use Resource instead)
- Removed TestException class in favor of IllegalStateException and IllegalArgumentException
- Removed HSQLTestException and HSQLContext (not necessary with the addition of NeedProvider)
- Removed Tests module in favor of testing inside modules themselves.

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

