# Mall backend rebuild

This repository is the clean foundation for rebuilding the mall e-commerce backend. Milestone 1
contains build, module, test, architecture, and CI scaffolding only. It intentionally contains no
runnable Spring Boot application or commerce feature.

## Required toolchain

- Eclipse Temurin 17.0.17+10
- Maven 3.9.16 through the committed Maven Wrapper
- Python 3 for the test-report verification script

The build enforces a Java runtime in the 17 release line and Maven 3.9.16. A newer installed JDK is
not a substitute for the accepted Java 17 runtime.

## Modules

The reactor contains exactly six modules:

1. `mall-common`
2. `mall-persistence`
3. `mall-security`
4. `mall-admin`
5. `mall-portal`
6. `mall-search`

Each module currently contains only a package marker and foundation tests. Module names reserve
future ownership boundaries; they do not imply implemented capabilities.

## Verify

```bash
./mvnw -version
./mvnw -B -ntp clean verify
./scripts/verify-test-reports.sh
```

The Maven lifecycle performs no Docker, image, deployment, or external-service action.

## Scope boundary

Milestone 1 excludes Spring Boot entry points, controllers, services, repositories, mappers, DTOs,
entities, migrations, runtime configuration, infrastructure definitions, integrations, and business
features. `mall-demo` is intentionally omitted.

Foundation decisions are indexed in [docs/architecture/decisions/README.md](docs/architecture/decisions/README.md).
