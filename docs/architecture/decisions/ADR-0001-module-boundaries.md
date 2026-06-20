# ADR-0001: Module boundaries

- Status: Accepted
- Date: 2026-06-21

## Decision

The initial reactor contains exactly `mall-common`, `mall-persistence`, `mall-security`,
`mall-admin`, `mall-portal`, and `mall-search`.

Library dependencies point inward. `mall-common` depends on no internal module.
`mall-persistence` and `mall-security` may depend on `mall-common` but not on each other. Application
modules may depend only on their approved library modules and never on another application module.
Unused allowed dependencies are not declared.

`mall-demo` is omitted. No seventh architecture or test production module is introduced.

## Enforcement

Maven reactor checks validate the module list and child dependency declarations. ArchUnit tests in
each module inspect a real marker class and reject forbidden bytecode dependencies.
