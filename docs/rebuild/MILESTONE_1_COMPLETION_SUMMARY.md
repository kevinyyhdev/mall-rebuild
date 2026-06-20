# Milestone 1 Completion Summary

## 1. What we built

Milestone 1 created a new, clean repository for the mall backend rebuild. The goal was to build a
safe project foundation before writing any business features.

The repository is a Maven multi-module project with six modules:

1. `mall-common`
2. `mall-persistence`
3. `mall-security`
4. `mall-admin`
5. `mall-portal`
6. `mall-search`

The project uses the committed Maven Wrapper 3.9.16, so developers and CI use the same Maven
version. Maven Enforcer requires Java 17 at build time, and the compiler targets Java release 17.
Tests run by default in every module; there is no inherited `skipTests` setting.

Each module has a small package marker, a marker test, and an ArchUnit architecture test. The
repository also has a basic GitHub Actions workflow that prepares Temurin 17, runs Maven verification,
and checks the generated test reports.

This milestone did **not** implement business behavior. It added no database schema or migrations,
Docker setup, authentication, product management, orders, payments, search integration, object
storage, controllers, services, repositories, mappers, DTOs, or entities.

## 2. Why this matters

A rebuild is safer when the project rules exist before feature code. If features were added first,
developers could accidentally recreate the old repository's problems, such as skipped tests, unclear
module ownership, broad dependencies, or build steps coupled to Docker.

The Maven Wrapper and pinned plugin versions make the build more reproducible: a developer should
not get a different result just because a different Maven version is installed globally. Requiring
Java 17 also proves that the build runs on the accepted runtime instead of merely compiling Java 17
code from a newer JDK.

Mandatory tests give the team an early safety net. The report verification script goes one step
further than a green Maven result: it checks that every expected module actually produced test
reports and executed at least one test.

Architecture rules protect module and package boundaries while the codebase is still small. This is
much easier than trying to untangle incorrect dependencies after many features have been written.

## 3. Key files and modules

| File or directory | Purpose |
|---|---|
| `pom.xml` | Aggregates exactly six modules and centrally defines Java, dependency, plugin, test, formatting, coverage, and enforcement policy. |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Provide the checksum-protected Maven 3.9.16 wrapper for Unix and Windows. |
| `mall-common/` | Reserves the shared, domain-neutral package boundary; it currently contains only a marker and foundation tests. |
| `mall-persistence/` | Reserves the future relational persistence boundary without adding database code yet. |
| `mall-security/` | Reserves the future security boundary without implementing authentication or authorization. |
| `mall-admin/` | Reserves the future admin application boundary without adding an application entry point or features. |
| `mall-portal/` | Reserves the future customer-facing portal boundary without adding an application entry point or features. |
| `mall-search/` | Reserves the future search application boundary without adding Elasticsearch code. |
| `scripts/verify-test-reports.sh` | Parses Surefire XML and fails when a module has missing, malformed, empty, failed, or errored test reports. |
| `.github/workflows/verify.yml` | Defines the basic Java 17 GitHub Actions verification job for pushes and pull requests. |
| `docs/architecture/decisions/` | Records the accepted module, package-naming, and build/test decisions. |

The child POMs are intentionally small. They inherit the parent policy and declare only JUnit 5 and
ArchUnit as test dependencies. Allowed internal module dependencies are not added until code needs
them.

## 4. Verification evidence

| Item | Result |
|---|---|
| Repository | `/Users/kevinyang/Desktop/Development/mall-rebuild` |
| Branch | `main` |
| Milestone 1 commit | `8774a392d13f14649407995900316dbf2dabc222` |
| Maven | Apache Maven 3.9.16 through `./mvnw` |
| Java | Eclipse Temurin 17.0.17+10 |
| `./mvnw -B -ntp clean verify` | `BUILD SUCCESS`; parent and all six modules succeeded |
| Architecture checks | Passed in all six modules; reactor and POM guardrails also passed |
| Local negative control | A deliberately broken marker assertion made Maven fail; reverting it restored the green build |
| Completion working tree | `git status --short` produced no output |

Test-report results:

| Module | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `mall-common` | 3 | 0 | 0 | 0 |
| `mall-persistence` | 2 | 0 | 0 | 0 |
| `mall-security` | 2 | 0 | 0 | 0 |
| `mall-admin` | 2 | 0 | 0 | 0 |
| `mall-portal` | 2 | 0 | 0 | 0 |
| `mall-search` | 2 | 0 | 0 | 0 |
| **Total** | **13** | **0** | **0** | **0** |

The architecture tests import each real module marker, so they do not pass only because a package is
empty. They reject forbidden module dependencies and reserve rules for future `api`, `application`,
`domain`, and `infrastructure` packages. A separate reactor test checks the exact module list,
allowed internal dependency edges, mandatory tests, and the absence of Docker/image plugins in the
foundation lifecycle.

## 5. System design and trade-offs

| Choice | Trade-off |
|---|---|
| Define module boundaries before features | Some folders look almost empty now, but future code starts with clear ownership rules. |
| Use package markers now | The architecture tests inspect real classes without introducing Spring Boot applications before they can be tested properly. |
| Add tests and CI before business logic | Initial setup takes longer, but later changes immediately receive repeatable feedback. |
| Defer Docker and databases | Milestone 1 cannot run the commerce system, but its build stays fast, simple, and independent of local services. |
| Pin the toolchain and plugins | Version updates require deliberate maintenance, but builds are less likely to change unexpectedly. |

The main trade-off is slower setup at the beginning in exchange for safer development later. The
foundation is independently testable, and later milestones can add one responsibility at a time.

## 6. Interview talking points

- “I rebuilt the project foundation as a Maven multi-module repository with six clear module boundaries.”
- “I used a committed Maven Wrapper and enforced Java 17 so local and CI builds use the accepted toolchain.”
- “I imported the Spring Boot BOM centrally without pulling broad runtime dependencies into every module.”
- “I prevented the legacy skipped-test problem by running tests by default and failing modules that discover no tests.”
- “I added a report script that proves every expected module executed at least one test.”
- “I used ArchUnit and a reactor-structure test to protect package, module, and POM dependency rules.”
- “I performed a local negative-control check to prove that a broken assertion makes the Maven build fail.”
- “I intentionally kept infrastructure and business features out of Milestone 1 so the foundation could be verified independently.”
- “I did not claim production readiness; this milestone produced build and architecture scaffolding only.”

## 7. What remains later

The following work is intentionally deferred:

- Run the workflow in a remote GitHub repository and record the required red/green CI rehearsal.
- Add Spring Boot application entry points when they can be tested without hidden service dependencies.
- Add versioned database migrations and tested persistence code.
- Build reproducible infrastructure and Docker configuration.
- Implement authentication, authorization, and admin RBAC.
- Implement product, admin, portal, search, order, payment, and storage features.

Milestone 1 is therefore ready as a reviewed foundation, not as a runnable or production-ready
e-commerce backend.
