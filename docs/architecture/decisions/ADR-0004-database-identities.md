# ADR-0004: Database identities and grants

- Status: Accepted
- Date: 2026-06-21
- Resolves: U-05

## Context

The legacy development configuration connects applications to MySQL as `root`. That hides missing
privileges, gives compromised application code schema-changing authority, and prevents operators
from identifying which application performed a database action.

Milestone 2 must freeze the identity boundary before Compose or bootstrap scripts encode it. The
database remains empty until Milestone 3 creates versioned migrations.

## Decision

The MySQL database is named `mall`. Local infrastructure defines five identities with separate
credentials:

| Identity | Purpose | Allowed privilege class |
|---|---|---|
| `root` | Initial server bootstrap and exceptional local maintenance only | Server bootstrap; never an application credential |
| `mall_migration` | Run versioned schema migrations beginning in Milestone 3 | Approved schema changes and migration-owned data changes within `mall` only |
| `mall_admin` | Future admin application | Read and write application data within `mall` only |
| `mall_portal` | Future portal application | Read and write application data within `mall` only |
| `mall_search` | Future search synchronization/read path | Read application data within `mall` only |

Application read-write means `SELECT`, `INSERT`, `UPDATE`, and `DELETE` on approved `mall` objects.
It does not include `CREATE`, `ALTER`, `DROP`, `TRUNCATE`, `INDEX`, `REFERENCES`, `TRIGGER`, `EVENT`,
user administration, `GRANT OPTION`, global privileges, or server administration. The search
identity receives `SELECT` only and must fail write and schema-changing probes.

The migration identity is the only non-root identity allowed to change the `mall` schema. Its exact
executable grant list must be the smallest set required by the accepted Milestone 3 migration design;
it must not receive global administrative privileges or `GRANT OPTION`.

Credential variable names are defined in `.env.example`. The committed values are visibly local,
non-secret examples. Production identity names, hosts, credentials, and secret delivery are not
decided here and must not be copied from local examples.

## Consequences

- Future applications cannot use `root` even for local convenience.
- Bootstrap verification must prove allowed connections and denied privilege boundaries.
- Admin and portal use different identities even while their initial privilege classes are similar,
  allowing later audit and independent revocation.
- Milestone 3 may narrow data access further after tables and ownership boundaries exist. Broadening
  schema or global privileges requires a new decision.
- This ADR creates no database, account, grant, table, migration, or seed data. Those executable
  infrastructure steps begin after Stage 1 approval.
