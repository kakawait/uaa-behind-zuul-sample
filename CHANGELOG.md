# Change Log

All notable changes to this project will be documented in this file.

## [0.0.4]

### Changed

- No more use _loopback trick_ and replace by _load balanced_ `RestTemplate` for `accessTokenUri` (#22)

## [0.0.3]

### Added

- `docker` and `docker-compose` support (#12) (thanks to [bilak](https://github.com/bilak))

### Changed

- Upgrade `spring-boot` to `1.4.1` and `spring-cloud` to `Camden.RELEASE` (#11) (thanks to [bilak](https://github.com/bilak))

## [0.0.2]

### Added

- Upgrade README.md to add *disclamer* section

### Changed

- Upgrade `spring-boot` to `1.3.5` and `spring-cloud` to `Brixton.SR1` (#11) (thanks to [bilak](https://github.com/bilak))
- Use fully qualified URL instead of relative URI to avoid HSTS redirection when using SSL.

So instead of returning `/uaa/oauth/authorize` we are not returning full URL, we are calculating *base url* from
`HttpServletRequest`
