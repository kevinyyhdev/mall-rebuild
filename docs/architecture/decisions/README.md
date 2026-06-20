# Architecture decision index

Milestone 0 was accepted on 2026-06-20 and remains the source of the rebuild's historical product
and platform decisions. Milestone 1 carries those decisions forward without reopening them.
Milestone 2 Stage 1 accepts the decisions that must precede local infrastructure implementation.

Accepted historical constraints relevant to this foundation include Java 17, Spring Boot 3.5.14,
Maven 3.9.16, the `com.macro.mall` namespace, six modules, omission of `mall-demo`, tests enabled by
default, and no Docker or external-service side effects in the Maven lifecycle.

Foundation decisions recorded in this repository:

- [ADR-0001: Module boundaries](ADR-0001-module-boundaries.md)
- [ADR-0002: Package naming](ADR-0002-package-naming.md)
- [ADR-0003: Build and test policy](ADR-0003-build-test-policy.md)

Milestone 2 environment-contract decisions:

- [ADR-0004: Database identities and grants](ADR-0004-database-identities.md)
- [ADR-0005: Demo environment exclusion](ADR-0005-demo-environment.md)
- [ADR-0006: MongoDB boundary, collections, and indexes](ADR-0006-mongodb-boundary.md)

The decisions above define boundaries and names only. Compose, service bootstrap, feature,
persistence, security, payment, search, and storage implementation remain deferred to their assigned
Milestone 2 stages or later milestones.
