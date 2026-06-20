# Architecture decision index

Milestone 0 was accepted on 2026-06-20 and remains the source of the rebuild's historical product
and platform decisions. Milestone 1 carries those decisions forward without reopening them.

Accepted historical constraints relevant to this foundation include Java 17, Spring Boot 3.5.14,
Maven 3.9.16, the `com.macro.mall` namespace, six modules, omission of `mall-demo`, tests enabled by
default, and no Docker or external-service side effects in the Maven lifecycle.

Foundation decisions recorded in this repository:

- [ADR-0001: Module boundaries](ADR-0001-module-boundaries.md)
- [ADR-0002: Package naming](ADR-0002-package-naming.md)
- [ADR-0003: Build and test policy](ADR-0003-build-test-policy.md)

Feature, persistence, infrastructure, security, payment, search, and storage decisions remain
deferred to their assigned later milestones.
