# ADR-0002: Package naming

- Status: Accepted
- Date: 2026-06-21

## Decision

The root namespace is `com.macro.mall`. Each module owns one root package and its descendants:

- `com.macro.mall.common`
- `com.macro.mall.persistence`
- `com.macro.mall.security`
- `com.macro.mall.admin`
- `com.macro.mall.portal`
- `com.macro.mall.search`

No new class is placed directly in `com.macro.mall`. Future feature code will use feature-oriented
`api`, `application`, `domain`, and `infrastructure` packages. Milestone 1 creates none of those
feature packages and contains marker types only.

Legacy misspellings and flat controller/service/DAO layouts are not imported into the clean
namespace.
