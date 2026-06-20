# ADR-0006: MongoDB boundary, collections, and indexes

- Status: Accepted
- Date: 2026-06-21
- Resolves: U-11

## Context

The legacy portal stores member product collections, member brand follows, and member read history
in MongoDB. Its `@Document` declarations do not specify collection names, and its find-then-save
flows do not enforce ownership-pair uniqueness atomically. Retaining MongoDB adds operational cost,
so its scope and storage contract must be explicit before local infrastructure is created.

## Decision

Retain MongoDB only for these three member features. The application database is `mall_portal`, and
the local non-root application identity is `mall_portal_app`.

Collection and index names are part of the storage contract:

| Collection | Purpose | Required secondary index |
|---|---|---|
| `member_product_collections` | Products collected by a member | Unique `uq_member_product_collections_member_product` on `{memberId: 1, productId: 1}` |
| `member_brand_follows` | Brands followed by a member | Unique `uq_member_brand_follows_member_brand` on `{memberId: 1, brandId: 1}` |
| `member_read_history` | Product-reading events for a member | Non-unique `ix_member_read_history_member_created` on `{memberId: 1, createTime: -1}` |

The application identity is limited to `mall_portal`; it is not a MongoDB administrator. Collection
and index bootstrap must be idempotent. The two unique compound indexes are the database-level guard
against duplicate member/target ownership pairs. Future application code must still enforce that a
member can access only their own records.

No TTL index is accepted for read history because no retention requirement exists. No other mall
feature may use MongoDB without a new decision. Exact field types, document schemas, repositories,
and member-facing behavior remain later feature work.

## Consequences

- Framework-derived collection names are forbidden; future document mappings must use these exact
  names.
- Empty-environment bootstrap will create only the three approved collections and indexes, not
  member documents or demo data.
- Verification must compare index names, key order, direction, and uniqueness options and must prove
  duplicate ownership pairs are rejected.
- MySQL remains the system of record for commerce correctness; MongoDB ownership data must not become
  an authority for orders, inventory, payment, or authorization.
- This ADR creates no MongoDB service, database, user, collection, index, document, repository, or
  production Java code. Executable bootstrap begins in a later Milestone 2 stage.
