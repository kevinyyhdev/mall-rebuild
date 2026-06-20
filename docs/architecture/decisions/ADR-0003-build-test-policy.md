# ADR-0003: Build and test policy

- Status: Accepted
- Date: 2026-06-21

## Decision

The committed Maven Wrapper runs Maven 3.9.16. Maven itself runs on Java 17, and compilation uses
release 17. The root POM imports the Spring Boot 3.5.14 dependency BOM without inheriting broad
runtime dependencies.

Surefire runs `*Test` tests and fails a module with no discovered unit tests. Failsafe reserves
`*IT` for later integration tests. JaCoCo emits coverage data without a foundation-stage percentage
threshold. Formatting and Maven Enforcer checks run in the normal verification lifecycle.

Every module must report at least one executed test. A separate report verifier rejects missing,
empty, malformed, failed, or errored Surefire XML reports.

The default Maven lifecycle must not contact Docker, a service, an image registry, or a deployment
target. Image and deployment work belongs to later explicit milestones.
