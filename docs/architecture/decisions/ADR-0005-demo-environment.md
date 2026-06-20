# ADR-0005: Demo environment exclusion

- Status: Accepted
- Date: 2026-06-21
- Resolves: U-06

## Context

The legacy reactor contains `mall-demo`, but the accepted module boundary omits it. A read-only demo
environment would also require its own authorization behavior, grants, fixtures, deployment surface,
and maintenance policy. No current product requirement justifies that additional architecture.

Milestone 2 must decide this before environment profiles and infrastructure identities are named.

## Decision

The rebuild has no demo environment profile and no `mall-demo` module.

Local infrastructure supports development and automated verification only. It must not create demo
credentials, read-only demo grants, demo-only service containers, demo fixtures, demo routes, or a
hidden environment flag that changes application authorization.

## Consequences

- The Maven reactor remains the six modules accepted by ADR-0001.
- `.env.example`, future Compose profiles, database identities, and CI jobs contain no demo variant.
- Test fixtures introduced in later milestones remain test data, not a deployable demo environment.
- A future real demonstration requirement needs a new product/operations decision with a bounded use
  case, security model, data lifecycle, and maintenance owner.
- This ADR creates no module, profile, fixture, container, or application behavior.
