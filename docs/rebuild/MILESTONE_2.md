# Milestone 2: Reproducible local infrastructure

- Planning status: Ready for review; implementation has not started
- Task-graph scope: I0 and I0.1 only
- Depends on: Milestone 0 decisions and completed Milestone 1 foundation
- Next milestone after acceptance: Milestone 3 (D1/D1.1/D2/P0/P0.1)

## 1. Goal

Milestone 2 will make the backend's external dependencies reproducible on a developer machine and
in CI. From a clean checkout and empty project-owned Docker volumes, one Compose project must start
MySQL, Redis, MongoDB, RabbitMQ, Elasticsearch with IK, and MinIO; initialize the service-level
prerequisites; and prove that the accepted Java clients and intended local identities can connect.

This is infrastructure bootstrap, not application or business-feature development. At the end of
the milestone the six Maven modules will still contain no commerce implementation and no runnable
Spring Boot applications. The achievement is a known-good environment on which schema and
persistence work can safely begin in Milestone 3.

The accepted rebuild guide names this milestone **“Reproducible infrastructure (I0/I0.1).”** The
task graph defines the same scope:

- I0: one Compose stack with an explicit network, health checks, named volumes, an environment
  template, and pinned service images;
- I0.1: automated service prerequisites such as MySQL identities/grants, RabbitMQ identity/vhost,
  the Elasticsearch analyzer, and a private MinIO bucket policy strategy.

The task graph takes precedence over the broader phrase “Stage 1 foundation.” Milestone 2 must not
pull Milestone 3 schema work or later application features forward.

## 2. Scope

Milestone 2 includes:

1. Resolving the three Milestone 0 decisions whose gates are “before I0.”
2. A single `compose.yaml` for the six required dependency services.
3. One project-scoped network, named volumes, loopback-only host ports, and useful health checks.
4. Exact service tags and immutable upstream image digests from the accepted version evidence.
5. A checksum-pinned Elasticsearch 8.18.8 image containing IK 8.18.8.
6. A committed `.env.example` with variable names and non-secret local example values; `.env`
   remains ignored.
7. Local service bootstrap:
   - the empty MySQL `mall` database and separate migration/admin/portal/search identities;
   - authenticated Redis with an explicit local persistence and memory policy;
   - an authenticated MongoDB application database, explicit collection names, and approved
     indexes if MongoDB is retained;
   - a RabbitMQ application user, `/mall` vhost, permissions, and tested TTL/dead-letter/failure
     topology;
   - the installed IK analyzer, with no production product index;
   - a private MinIO bucket and a least-privilege application identity.
8. Idempotent, non-interactive bootstrap and verification scripts.
9. Explicitly activated integration smoke tests using the dependency versions managed by the
   Spring Boot 3.5.14 baseline. These tests may add test-only dependencies and test code, but no
   production Java code.
10. A separate CI infrastructure job on Linux AMD64. The ordinary Maven job must remain Docker-free.
11. A short local-infrastructure runbook covering startup, verification, restart, reset, endpoints,
    and troubleshooting.

Required service versions are inherited from Milestone 0:

| Service | Accepted version |
|---|---|
| MySQL | 8.4.6 LTS |
| Redis | 7.4.6 |
| MongoDB | 8.0.16, only if U-11 is accepted |
| RabbitMQ | 4.1.4-management |
| Elasticsearch | 8.18.8 |
| IK analyzer | 8.18.8, exact artifact and SHA-256 from U-04 |
| MinIO | `RELEASE.2025-09-07T16-13-09Z` |

The accepted evidence already records multi-platform image-list digests. Use those digest-qualified
references rather than reselecting versions or using `latest`. A newly introduced helper image,
such as a MinIO client image, must receive the same exact-tag, AMD64/ARM64, pull, and digest evidence
before it is committed.

## 3. Out of scope

Do not implement any of the following in Milestone 2:

- the legacy `document/sql/mall.sql` dump, Flyway migrations, tables, relational constraints,
  required RBAC/settings seeds, or demo fixtures;
- production Java code, Spring Boot application entry points, runtime `application*.yml`,
  controllers, DTOs, services, repositories, mappers, entities, or domain models;
- authentication, JWT, RBAC, CORS, API contracts, error handling, or logging design;
- product, member, cart, promotion, coupon, points, order, inventory, payment, or search features;
- MongoDB repositories or member-history behavior;
- RabbitMQ publishers, consumers, outbox records, order cancellation behavior, retries in Java, or
  an exactly-once-delivery claim;
- an Elasticsearch product mapping, index template, product alias, document import, synonyms,
  custom product dictionary, ranking, highlighting, or relevance tuning;
- a MinIO/OSS application adapter, upload API, object metadata model, or public bucket/read policy;
- application container images, Nginx routes, Logstash, Kibana, production observability, deployment,
  backup/restore, high availability, production sizing, or registry publication;
- real production credentials, production TLS certificates, Alipay configuration, or Aliyun OSS;
- `mall-demo` or a read-only demo profile unless U-06 is explicitly changed before implementation.

The old SQL dump must not be mounted into MySQL “just to make it usable.” A database server with an
empty `mall` database and correct identities is the intended result. Milestone 3 owns the schema.

## 4. Prerequisites from Milestone 1

Before Milestone 2 implementation begins, confirm all of the following:

- the repository contains exactly the six accepted modules;
- `./mvnw` runs Maven 3.9.16 on Java 17;
- `./mvnw -B -ntp clean verify` passes with nonzero tests in every module;
- `.github/workflows/verify.yml` is green;
- ADR-0001 through ADR-0003 remain accepted;
- the default Maven lifecycle performs no Docker, network-service, image-build, or deployment work;
- the working tree is understood before changes begin.

Milestone 1 intentionally did not add runtime infrastructure. Although the original rebuild guide
listed `.env.example` among foundation outputs, the clean repository does not currently contain it.
That is not permission to add runtime application configuration: Milestone 2 should add only the
environment contract needed by Compose, bootstrap, and infrastructure tests.

Run this baseline before implementation:

```bash
git status --short
./mvnw -version
./mvnw -B -ntp clean verify
./scripts/verify-test-reports.sh
docker version
docker compose version
```

The evidence records Docker Engine 29.5.3 and Compose v5.1.4 as the versions actually tested on the
Milestone 0 host. It does **not** prove an older minimum. If implementation is attempted with an
older version, verify every required feature—especially `docker compose up --wait` and dependency
conditions—instead of claiming it is supported.

### Required decision gate before I0

Milestone 0 explicitly placed these decisions before I0. Reviewing this guide must either accept the
safe defaults below or replace them with equally explicit decisions:

| Decision | Proposed safe default for review | Evidence required before implementation |
|---|---|---|
| U-05: database identities/grants | Separate migration owner, admin read-write, portal read-write, and search read-only identities. Application identities never use MySQL root. | Accepted database-identity ADR with an executable grant matrix. |
| U-06: read-only demo | No demo profile and no `mall-demo` module. | Accepted product/operations ADR. |
| U-11: MongoDB justification | Retain MongoDB only for member product collections, brand follows, and read history; use explicit collections and indexes. | Accepted datastore ADR naming collections, ownership, indexes, and operational cost. |

If any of these remains undecided, stop before creating `compose.yaml`. A “safe default” bounded
Milestone 0; it did not silently convert a later decision into an accepted one.

## 5. System design concerns

### 5.1 Reproducibility has several layers

An exact image tag is better than `latest`, but a tag can still be repointed. A digest identifies the
exact image content. Compose should therefore use both, for example `mysql:8.4.6@sha256:...`, so the
version remains readable while the bytes remain fixed.

The custom Elasticsearch image has no accepted published digest yet. Its reproducibility comes from
a digest-pinned Elasticsearch base image, an exact IK URL, a mandatory SHA-256 check during image
build, and a version-controlled Dockerfile. Publishing multi-platform images is not authorized by
this milestone; build and run the image natively on developer ARM64 and CI AMD64 instead.

### 5.2 Running, healthy, ready, and initialized are different states

- **Running** means a container process exists.
- **Healthy** means its health check can reach the service protocol.
- **Ready** means the service can accept the operation the next stage needs.
- **Initialized** means required users, grants, vhosts, policies, collections, or buckets exist.

`docker compose ps` alone proves only the first two. The milestone verification script must prove the
last two. Bootstrap jobs must wait for health rather than relying on startup order or arbitrary
sleep durations.

### 5.3 Service ownership and data authority

MySQL remains the system of record. Redis is a disposable cache/rate-limit aid, Elasticsearch is a
rebuildable read model, RabbitMQ transports messages, MongoDB is limited to the three approved member
features, and MinIO stores blobs. No correctness invariant may exist only in Redis, Elasticsearch,
or a RabbitMQ message.

Milestone 2 creates service prerequisites, not business data. A private bucket is infrastructure; a
product object is business data. A Mongo index is infrastructure; a member-history repository is
application code. The boundary matters.

### 5.4 Least privilege starts locally

Local infrastructure is not production, but root-everywhere hides broken assumptions. Give each
application role only the privileges it will need. Keep root/bootstrap identities separate and never
put them in future application configuration. Negative tests—such as proving the search identity
cannot write—are as important as successful connection tests.

### 5.5 Secrets and host exposure

`.env.example` documents variable names and non-secret local examples. Developers copy it to the
ignored `.env`; production values never belong in either file. Bind published ports to `127.0.0.1`,
not every host interface. Use one internal Compose network and service DNS for container-to-container
traffic.

### 5.6 Host and container addresses are intentionally different

Inside Compose, service names are DNS names. Programs running directly on the host use localhost and
published ports. Do not copy the old aliases (`db`, `es`, `rabbit`) or `external_links` design.

| Service | From another Compose service | From the developer host |
|---|---|---|
| MySQL | `mysql:3306` | `127.0.0.1:${MYSQL_HOST_PORT}` |
| Redis | `redis:6379` | `127.0.0.1:${REDIS_HOST_PORT}` |
| MongoDB | `mongo:27017` | `127.0.0.1:${MONGO_HOST_PORT}` |
| RabbitMQ AMQP | `rabbitmq:5672` | `127.0.0.1:${RABBITMQ_HOST_PORT}` |
| RabbitMQ management | `rabbitmq:15672` | `127.0.0.1:${RABBITMQ_MANAGEMENT_HOST_PORT}` |
| Elasticsearch | `elasticsearch:9200` | `127.0.0.1:${ELASTICSEARCH_HOST_PORT}` |
| MinIO API | `minio:9000` | `127.0.0.1:${MINIO_HOST_PORT}` |
| MinIO console | `minio:9001` | `127.0.0.1:${MINIO_CONSOLE_HOST_PORT}` |

Host-port variables allow a developer to avoid common port conflicts without changing the stable
in-network contract.

### 5.7 Bootstrap must be safe to repeat

Use operations such as “create if missing,” compare-before-update, and declarative definition import.
Running bootstrap twice must not duplicate indexes, reset data, broaden a policy, or fail because an
object already exists. Repeated `docker compose up` with existing volumes and a full empty-volume
reset are separate tests; both must pass.

### 5.8 Preserve the Milestone 1 build contract

Normal `./mvnw verify` must remain fast and external-service-free. Infrastructure integration tests
must use an explicit profile such as `-Pinfrastructure-it`. The separate CI infrastructure job may
start Compose and activate that profile. Do not bind Compose startup or image building to Maven
`package` or normal `verify`.

### 5.9 Cross-platform evidence is part of acceptance

The required developer platform is macOS Apple Silicon/ARM64; CI and production target Linux AMD64.
Every upstream image in the accepted matrix supports both. The custom Elasticsearch image must be
built and run on native ARM64 locally and native AMD64 in CI. A successful ARM64 build alone does not
prove CI compatibility.

## 6. Expected file changes during implementation

This is a planning inventory, not a command to create the files now. Exact helper filenames may be
adjusted during review, but responsibilities must not be merged into application production code.

| File or directory | Intended responsibility |
|---|---|
| `.env.example` | Non-secret local variable and host-port contract. |
| `.gitignore` | Continue ignoring `.env` and secret/local override files. |
| `compose.yaml` | Required infrastructure services, one network, named volumes, health checks, and bootstrap jobs. |
| `infra/mysql/init/` | Idempotent database/identity/grant bootstrap; no application tables or seed rows. |
| `infra/mongo/init/` | Authenticated database, explicit collections, and approved indexes if U-11 retains MongoDB. |
| `infra/rabbitmq/definitions.json` | Version-controlled `/mall` topology and policies without production secrets. |
| `infra/rabbitmq/` supporting config/bootstrap | Environment-driven application identity and definitions loading. |
| `infra/elasticsearch/Dockerfile` | Digest-pinned Elasticsearch plus checksum-verified IK 8.18.8 installation. |
| `infra/minio/init.sh` and policy input | Idempotent private bucket and least-privilege identity setup. |
| `scripts/infra/check-prerequisites.sh` | Friendly checks for Docker/Compose, required variables, ports, and supported architecture. |
| `scripts/infra/verify.sh` | Protocol, identity, privilege, topology, analyzer, index, and bucket assertions. |
| `scripts/infra/reset.sh` | Guarded removal of this Compose project's containers and volumes only. |
| `docs/development/local-infrastructure.md` | Junior-friendly startup, verification, endpoint, reset, and troubleshooting runbook. |
| `docs/architecture/decisions/ADR-0004-*.md` onward | Accepted U-05, U-06, U-11, and local-infrastructure boundary decisions. |
| `docs/architecture/decisions/README.md` | Index the new accepted ADRs. |
| `pom.xml` and selected module POMs | An explicit infrastructure-test profile and test-only client dependencies. |
| selected `src/test/java/.../*IT.java` files | Real client connection tests; no production behavior. |
| `.github/workflows/verify.yml` | Separate Linux AMD64 infrastructure verification job with unconditional diagnostics/cleanup. |
| `README.md` | Link to the local-infrastructure runbook without claiming the applications are runnable. |

Do not create a seventh Maven module for infrastructure tests. Use the existing owning modules and
test scope only.

## 7. Step-by-step implementation stages

Each stage below has the same seven teaching questions. Complete and verify one stage before moving
to the next.

### Stage 1 — Accept the I0 decisions and freeze the environment contract

#### 1. What we are building

Record accepted ADRs for U-05, U-06, and U-11. Define the names of services, databases, users,
buckets, vhosts, collections, environment variables, and host-port overrides before writing Compose.
Create the planning portion of `.env.example` and the local-infrastructure runbook.

At minimum the environment contract should distinguish bootstrap credentials from application
credentials. Suggested variable groups are:

- `MYSQL_ROOT_*`, `MYSQL_MIGRATION_*`, `MYSQL_ADMIN_*`, `MYSQL_PORTAL_*`, `MYSQL_SEARCH_*`;
- `REDIS_PASSWORD`;
- `MONGO_ROOT_*`, `MONGO_APP_*`;
- `RABBITMQ_BOOTSTRAP_*`, `RABBITMQ_APP_*`;
- `MINIO_ROOT_*`, `MINIO_APP_*`;
- one `*_HOST_PORT` variable for each published endpoint.

#### 2. Why it comes now

Bootstrap files encode names and privileges. Writing them before accepting the decisions would turn
an unresolved assumption into executable architecture and make later correction more expensive.

#### 3. How it works conceptually

An environment contract is an interface. Compose supplies values to containers; bootstrap scripts
use them to create local identities; verification tests use the same values to connect. Later
application configuration will consume the same names without learning root credentials.

#### 4. System design concerns

- Keep production secrets and account identifiers out of Git.
- Use obvious non-secret local examples rather than real-looking production values.
- Decide exact Mongo collection names and indexes only if U-11 is accepted.
- Record that the demo default is “absent,” not a hidden feature flag.
- Keep host ports configurable and container ports stable.

#### 5. What files will change

- `.env.example`
- `.gitignore` only if its existing rules need correction
- `docs/development/local-infrastructure.md`
- three or more ADRs plus `docs/architecture/decisions/README.md`

#### 6. What commands verify success

```bash
git check-ignore .env
git check-ignore -v .env.example || true
rg -n "Status: Accepted|U-05|U-06|U-11" docs/architecture/decisions
git diff --check
```

Expected: `.env` is ignored, `.env.example` is not ignored, all three decisions are explicit, and no
whitespace error exists. The `|| true` permits the expected “not ignored” result for `.env.example`;
review the output rather than treating silence as proof.

#### 7. What must not be implemented yet

Do not create service containers, application YAML, schema migrations, collections before the Mongo
decision, business data, or production identities.

### Stage 2 — Create the Compose topology, pin inputs, and define health

#### 1. What we are building

Create one `compose.yaml` with the six required service names: `mysql`, `redis`, `mongo`, `rabbitmq`,
`elasticsearch`, and `minio`. Add one explicit internal network, one named volume per persistent
service, loopback-only published ports, resource-conscious local settings, and protocol-aware health
checks. Do not use `container_name`, `links`, `external_links`, host-specific absolute volume paths,
or a deprecated top-level Compose `version` key.

Pin every upstream image by exact tag and accepted image-list digest. Pin and document any new helper
image before using it. Add a prerequisite script that reports missing variables, unavailable ports,
unsupported architecture, and Docker/Compose information with actionable messages.

#### 2. Why it comes now

Bootstrap cannot be reliable until service identity, networking, storage, image bytes, and readiness
signals are stable. This creates the common lifecycle used by every later service stage.

#### 3. How it works conceptually

Compose creates a project namespace. Named volumes retain local state across an ordinary stop/start.
The project network gives each service a stable DNS name. Health checks run a real protocol command,
and `depends_on` health/completion conditions keep bootstrap jobs from racing service startup.

#### 4. System design concerns

- A TCP port opening is weaker than a successful authenticated command.
- Health checks must not print secrets.
- Fixed `container_name` values break isolation between clones and CI jobs.
- Loopback binding limits exposure but is not a production security design.
- Resource limits must keep the stack usable on a laptop without inventing production sizing.
- Use the Compose project name in `.env`; cleanup scripts must refuse an empty or unexpected name.

#### 5. What files will change

- `compose.yaml`
- `.env.example`
- `scripts/infra/check-prerequisites.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
cp .env.example .env
./scripts/infra/check-prerequisites.sh
docker compose config --quiet
docker compose config --images
```

Review the rendered configuration to prove that no `latest`, unexpanded required variable, public
`0.0.0.0` binding, absolute developer path, `container_name`, link, or external link remains.

#### 7. What must not be implemented yet

Do not mount the legacy SQL dump, add application containers, add Nginx/ELK, build Java images, or
hide service initialization inside arbitrary sleeps.

### Stage 3 — Bootstrap MySQL identities and configure Redis as a cache

#### 1. What we are building

For MySQL, create the empty `mall` database and separate local identities for migrations, admin,
portal, and search. Apply an accepted grant matrix: migration owns schema change, admin and portal
receive only approved application read/write privileges, and search is read-only. Configure UTF-8
at the service boundary and UTC server/session behavior without deciding Milestone 3 table collation
or schema design.

For Redis, require authentication, set a bounded local memory policy, and record the local
persistence decision. The recommended local default is disposable cache state rather than AOF,
because correctness must not depend on Redis. Later security/session designs must not quietly reverse
that principle.

#### 2. Why it comes now

MySQL is the system of record and the first dependency of Milestone 3. Redis is simpler but shares
the need for environment-driven credentials and clear state semantics. Establishing least privilege
now prevents future code from assuming root or unauthenticated access.

#### 3. How it works conceptually

MySQL grants attach allowed operations to an identity and database. Successful login does not prove
correct authorization, so verification checks both allowed and denied operations. Redis password
authentication prevents accidental unauthenticated local use; its eviction/persistence policy states
whether data is durable or reconstructible.

#### 4. System design concerns

- Do not give schema-changing privileges to application identities.
- Do not rely only on usernames; assert `SHOW GRANTS` and negative operations.
- Do not create application tables merely to test writes. Use grant inspection and safe session-local
  probes until Milestone 3 supplies tables.
- Do not expose MySQL root or Redis credentials to future application configuration.
- Avoid logging passwords in shell tracing, process output, or CI logs.
- The final database collation and schema timezone decisions remain Milestone 3 gates.

#### 5. What files will change

- `compose.yaml`
- `infra/mysql/init/` bootstrap scripts
- `.env.example`
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
docker compose up -d --wait mysql redis
docker compose ps mysql redis
./scripts/infra/verify.sh mysql redis
docker compose restart mysql redis
./scripts/infra/verify.sh mysql redis
```

Verification must prove authenticated `SELECT 1` for every MySQL identity, the approved grant set,
denied write/schema operations for search, denied schema operations for admin/portal, unauthenticated
Redis rejection, and authenticated Redis `PONG`.

#### 7. What must not be implemented yet

Do not import tables or rows, configure Flyway, copy `mall.sql`, design Redis key formats, add Redis
serialization, implement caches, or use Redis for order IDs, locks, token truth, or other correctness.

### Stage 4 — Bootstrap MongoDB only for the accepted member-data boundary

#### 1. What we are building

If U-11 retains MongoDB, create an authenticated `mall_portal` database, a non-root application
identity, explicitly named collections for product collections, brand follows, and read history, and
the indexes accepted by the MongoDB ADR. The expected minimum is:

- unique `(memberId, productId)` for product collections;
- unique `(memberId, brandId)` for brand follows;
- `(memberId, createTime descending)` for read-history queries.

The ADR must choose exact collection names. Do not let a future framework derive them implicitly.

If U-11 removes MongoDB, omit the service and this bootstrap entirely and revise the scope/acceptance
criteria before implementation; do not leave an unused container “for compatibility.”

#### 2. Why it comes now

MongoDB adds operational cost and its old collection names and uniqueness rules were implicit. The
environment should either make the intended boundary reproducible now or not include MongoDB at all.

#### 3. How it works conceptually

A compound unique index lets the database atomically reject two records with the same ownership and
target pair. That closes the race in the old “find then save” pattern. A query index orders one
member's history without scanning every document.

#### 4. System design concerns

- Collection/index creation must be idempotent.
- The application identity must be limited to `mall_portal`.
- Do not create sample member documents.
- Do not choose TTL retention without an approved product/operations rule.
- Indexes enforce storage invariants, but future Java code must still enforce member ownership.

#### 5. What files will change

- `compose.yaml`
- `infra/mongo/init/` bootstrap files
- `.env.example`
- the MongoDB ADR and ADR index
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
docker compose up -d --wait mongo
./scripts/infra/verify.sh mongo
docker compose restart mongo
./scripts/infra/verify.sh mongo
```

Verification must authenticate as the application user, assert the database and exact collection
names, compare index keys and uniqueness options, prove a duplicate ownership pair is rejected using
temporary verification data, clean that data, and prove the app user lacks admin-database privilege.

#### 7. What must not be implemented yet

Do not add Spring Data documents/repositories, member APIs, real member records, history retention,
ownership services, or migrations that depend on product/member domain classes.

### Stage 5 — Initialize RabbitMQ identity, vhost, and failure-aware topology

#### 1. What we are building

Create an environment-driven local application user, `/mall` vhost, narrow permissions, and a
version-controlled `definitions.json`. Provision and test the minimum exchanges, queues, bindings,
TTL/dead-letter route, and terminal failure DLQ required by the accepted guide. Keep management
credentials separate from application credentials.

The topology is infrastructure prerequisite only. Its names and policies must be documented, and
the old `mall.order.direct` / `mall.order.direct.ttl` behavior should be treated as evidence rather
than copied blindly. If final order-specific names would prematurely freeze O2 behavior, use a
clearly versioned infrastructure contract and record which semantics O2 must later confirm.

#### 2. Why it comes now

The old Compose file only started RabbitMQ; it did not create the `/mall` vhost or credentials. The
old application created part of its topology at runtime and had no bounded retry/failure DLQ. A clean
environment needs deterministic prerequisites before any producer or consumer is written.

#### 3. How it works conceptually

A producer publishes to an exchange, a binding routes to a queue, and a consumer reads the queue.
TTL/dead-letter configuration can move an expired message to another exchange. A failure DLQ holds
messages that exhausted bounded handling so failures remain visible and replay can be deliberate.
None of this makes delivery exactly once; future consumers must be idempotent.

#### 4. System design concerns

- `definitions.json` must not contain a real production password.
- If definitions cannot interpolate environment variables, create the user/permissions in an
  idempotent bootstrap step and keep topology in definitions; do not hard-code a secret to work
  around the limitation.
- Durable topology must be safe to import twice.
- Do not create unbounded retry loops.
- Verify permissions through the application identity, not only the management identity.
- O2 still owns publisher confirms, outbox delivery, reconciliation, consumer idempotency, and replay.

#### 5. What files will change

- `compose.yaml`
- `infra/rabbitmq/definitions.json`
- supporting RabbitMQ config/bootstrap files
- `.env.example`
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
docker compose up -d --wait rabbitmq
./scripts/infra/verify.sh rabbitmq
docker compose restart rabbitmq
./scripts/infra/verify.sh rabbitmq
```

Verification must assert the `/mall` vhost, user permissions, exchanges, queues, bindings, policy
arguments, and failure DLQ. A short-lived test message must traverse the TTL/dead-letter path and be
removed afterward. Import/bootstrap must be run twice without duplication or failure.

#### 7. What must not be implemented yet

Do not add Java producers/consumers, order cancellation, outbox tables, retry code, a scheduler,
business payload schemas, or replay tooling.

### Stage 6 — Build and verify Elasticsearch with checksum-pinned IK

#### 1. What we are building

Create `infra/elasticsearch/Dockerfile` from the accepted Elasticsearch image-list digest. Fetch or
copy the exact `elasticsearch-analysis-ik-8.18.8.zip`, verify SHA-256
`e7d239c3261e2f5862034286bb30e9109f31917e8cc7a93431281ce54da8f966`, install it during image build,
remove the ZIP, and run Elasticsearch as its non-root image user. Configure no remote IK dictionary
and restrict runtime egress.

The Compose service builds this image before startup. Verification checks plugin presence, cluster
health, and `_analyze` behavior for the U-04 acceptance terms without leaving a production product
index behind.

#### 2. Why it comes now

Search mappings created later depend on analyzer availability. If analyzer installation is delayed
until Q0 or container startup, developers can create incompatible indexes or experience mutable
startup failures. Building it now freezes the prerequisite while search behavior remains deferred.

#### 3. How it works conceptually

An Elasticsearch analyzer converts text into tokens used for indexing and queries. The IK plugin
adds `ik_max_word`. Installing it in the image makes every container from that image have the same
plugin before Elasticsearch starts. The checksum detects changed or corrupted artifact bytes.

#### 4. System design concerns

- Pin the base by digest and the plugin by exact URL plus SHA-256.
- Never install the plugin at container startup.
- Preserve non-root file ownership; U-04 proved that root-owned temporary files break cleanup.
- The plugin requests outbound-network entitlement, so configure no remote dictionary and restrict
  egress.
- Single-node yellow health can be valid with an unassigned replica; assert active primaries and
  explain the condition rather than accepting arbitrary red/yellow state.
- `红米` relevance remains a Q0 concern; do not add dictionaries or silently change analyzers here.

#### 5. What files will change

- `infra/elasticsearch/Dockerfile`
- `compose.yaml`
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
docker compose build --no-cache elasticsearch
docker compose up -d --wait elasticsearch
./scripts/infra/verify.sh elasticsearch
docker compose exec -T elasticsearch bin/elasticsearch-plugin list
```

Run the build and analyzer checks on native ARM64 locally and native AMD64 in CI. Expected tokens are
the U-04 recorded cases for `手机`, `小米手机`, `华为Mate手机`, `男士运动鞋`,
`iPhone 15 Pro Max`, and `红米K70保护壳`.

#### 7. What must not be implemented yet

Do not create the production product index/template/alias, import products, add search repositories,
configure synonyms/custom dictionaries, implement highlighting/ranking, or publish application or
infrastructure images to a registry.

### Stage 7 — Bootstrap private MinIO storage

#### 1. What we are building

Start the accepted MinIO release, create a non-root application identity, create the private
`mall-product` bucket (and only an additionally approved private bucket), and attach a least-privilege
policy scoped to those resources. Use an idempotent bootstrap job or script and an exact, verified
client mechanism.

#### 2. Why it comes now

Later object-storage work needs a stable, private local target. The old implementation created a
bucket from an unauthenticated upload path and granted public read. Bootstrap must establish the
opposite default before an adapter or API exists.

#### 3. How it works conceptually

MinIO exposes an S3-compatible object API. A bucket groups objects, and an identity policy controls
which actions apply to which bucket/object paths. “Private” means anonymous requests receive no
object access; authorized access uses application credentials or later short-lived signed URLs.

#### 4. System design concerns

- Root credentials bootstrap the server but never belong to application code.
- Scope the application policy to approved bucket actions and paths.
- Prove anonymous access is denied, not merely that bucket creation succeeds.
- Do not enable public read for convenience.
- A helper/client image is another supply-chain dependency and must be pinned/tested.
- Object naming, MIME validation, scanning, metadata, retention, and deletion rules belong to OBJ0.

#### 5. What files will change

- `compose.yaml`
- `infra/minio/init.sh`
- a version-controlled MinIO policy input if needed
- `.env.example`
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
docker compose up -d --wait minio
./scripts/infra/verify.sh minio
docker compose restart minio
./scripts/infra/verify.sh minio
```

Verification must prove live/ready health, application-identity access to the intended private
bucket, denial outside its scope, denial of anonymous listing/reading, exact policy content, and
idempotent second bootstrap.

#### 7. What must not be implemented yet

Do not add upload/delete endpoints, public bucket policies, signed-URL APIs, object-key generation,
file validation, an `ObjectStorage` port, a MinIO Java adapter, or Aliyun OSS.

### Stage 8 — Add automated verification and real Java-client smoke tests

#### 1. What we are building

Finish `scripts/infra/verify.sh` as the authoritative initialized-state check. Add an explicit Maven
profile such as `infrastructure-it` and test-scoped dependencies/tests in existing owning modules so
the accepted Boot-managed client generation performs real connections:

- MySQL Connector/J against each MySQL identity;
- Lettuce against authenticated Redis;
- MongoDB sync driver against `mall_portal` if retained;
- RabbitMQ AMQP/Spring Rabbit client against `/mall`;
- Elasticsearch Java client against 8.18.8 with IK;
- MinIO Java SDK against the private bucket.

Suggested ownership keeps broad clients out of `mall-common`: MySQL privilege tests in
`mall-persistence`, Redis/MinIO in `mall-admin`, Mongo/RabbitMQ in `mall-portal`, and Elasticsearch in
`mall-search`. Dependencies remain test-only.

#### 2. Why it comes now

CLI checks prove server behavior but not Java client compatibility. U-03 explicitly deferred
connection-level testing to I0. Adding these tests only after all bootstrap stages gives failures one
clear meaning: the accepted client cannot use the initialized contract.

#### 3. How it works conceptually

The Spring Boot BOM selects compatible dependency versions. Test code instantiates those real clients
and connects to host-published endpoints using `.env` values. Maven Failsafe runs `*IT` only when the
explicit profile is selected. Normal unit verification remains isolated.

#### 4. System design concerns

- Do not add a seventh module or broad production dependencies.
- Do not start Docker from Maven.
- Make the explicit profile fail if required variables/services are absent; make the default build
  ignore these external tests.
- Assert both protocol success and privilege/policy boundaries.
- Keep test data uniquely named, bounded, and cleaned in `finally`/teardown.
- Never print connection strings containing passwords.

#### 5. What files will change

- `pom.xml`
- selected module POMs
- selected `src/test/java/.../infrastructure/*IT.java` files
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`

#### 6. What commands verify success

```bash
./mvnw -B -ntp clean verify
./scripts/verify-test-reports.sh
./mvnw -B -ntp -Pinfrastructure-it verify
./scripts/infra/verify.sh
```

Run the first two commands once with the Compose project stopped to prove the default build remains
service-independent. Run the profile only after `docker compose up -d --wait`.

Use dependency output to confirm the intended clients, not accidental versions:

```bash
./mvnw -Pinfrastructure-it dependency:tree \
  -Dincludes=com.mysql:mysql-connector-j,io.lettuce:lettuce-core,org.mongodb:mongodb-driver-sync,com.rabbitmq:amqp-client,co.elastic.clients:elasticsearch-java,io.minio:minio
```

#### 7. What must not be implemented yet

Do not add Spring Boot entry points, production configuration classes, repositories, entities,
service logic, Testcontainers-based schema tests, or make ordinary `verify` depend on a live service.

### Stage 9 — Prove clean-state, restart, CI AMD64, and safe cleanup

#### 1. What we are building

Add a separate GitHub Actions infrastructure job that starts the exact stack on Linux AMD64, builds
the IK image, waits for health/bootstrap, runs shell and Java-client verification, captures logs on
failure, and always removes project resources. Complete the runbook and guarded reset script.

Perform two local rehearsals:

1. empty-volume creation from scratch;
2. stop/start with existing volumes plus a repeated bootstrap run.

#### 2. Why it comes now

A stack that works only because a developer already initialized volumes is not reproducible. CI gives
an independent empty AMD64 host, while restart/idempotency checks expose scripts that only work once.

#### 3. How it works conceptually

The CI job creates `.env` from safe CI/local examples, renders Compose, builds the custom image,
starts dependencies, runs assertions, and executes cleanup in an `always()` step. The ordinary Maven
job stays separate, preserving the foundation's fast feedback and Docker-free lifecycle.

#### 4. System design concerns

- `docker compose down --volumes` is destructive; scope it to the known project and explain it.
- Never run global prune or delete unrelated images, volumes, or build cache.
- Capture `docker compose ps` and bounded service logs before cleanup on failure.
- CI must use no production secrets or registry credentials.
- A green local ARM64 run and a green CI AMD64 run are both required.
- Do not claim production readiness from a local single-node stack.

#### 5. What files will change

- `.github/workflows/verify.yml`
- `scripts/infra/reset.sh`
- `scripts/infra/verify.sh`
- `docs/development/local-infrastructure.md`
- `README.md`

#### 6. What commands verify success

Run from the repository root. The first command deletes **only this Compose project's named volumes**
when the project name guard is correct:

```bash
./scripts/infra/reset.sh
docker compose config --quiet
docker compose build --pull
docker compose up -d --wait
docker compose ps
./scripts/infra/verify.sh
./mvnw -B -ntp -Pinfrastructure-it verify

docker compose down
docker compose up -d --wait
./scripts/infra/verify.sh
./mvnw -B -ntp -Pinfrastructure-it verify

./mvnw -B -ntp clean verify
./scripts/verify-test-reports.sh
```

Then push only after review and require the ordinary Maven job and separate infrastructure job to be
green. Record the exact workflow run/commit in the Milestone 2 completion summary.

#### 7. What must not be implemented yet

Stop after the I0/I0.1 acceptance evidence. Do not “keep going” into Flyway, the mall schema,
application startup, app images, features, deployment, Nginx, or observability.

## 8. Verification command sequence

The final Milestone 2 verification sequence should be documented and executable from a clean
checkout as follows:

```bash
# Baseline remains independent of Docker services.
./mvnw -version
./mvnw -B -ntp clean verify
./scripts/verify-test-reports.sh

# Render and validate infrastructure configuration.
cp .env.example .env
./scripts/infra/check-prerequisites.sh
docker compose config --quiet

# Destructive only to the guarded mall-rebuild Compose project.
./scripts/infra/reset.sh

# Empty-volume bootstrap and complete verification.
docker compose build --pull
docker compose up -d --wait
docker compose ps
./scripts/infra/verify.sh
./mvnw -B -ntp -Pinfrastructure-it verify

# Existing-volume restart and idempotency verification.
docker compose down
docker compose up -d --wait
./scripts/infra/verify.sh
./mvnw -B -ntp -Pinfrastructure-it verify

# Leave no running project when the rehearsal is complete.
docker compose down
```

When deliberately testing the full reset, use `./scripts/infra/reset.sh` rather than an unguarded
manual command. Never use `docker system prune`, a global volume prune, or legacy destructive shell
scripts from the evidence repository.

## 9. Acceptance criteria

Milestone 2 is accepted only when every item below is true:

- [ ] U-05, U-06, and U-11 are explicitly accepted or replaced in ADRs.
- [ ] The default Maven build remains green and performs no Docker/service/image work.
- [ ] `docker compose config --quiet` passes with no unexpanded required variables.
- [ ] There is one project network, project-scoped named volumes, no fixed container names, no links,
      no host-specific absolute mounts, and only loopback-published ports.
- [ ] Every upstream service/helper image has an exact tag and immutable digest with AMD64/ARM64
      evidence; no `latest` tag exists.
- [ ] From empty project volumes, `docker compose up -d --wait` completes without manual container
      edits.
- [ ] All six retained services become healthy within documented timeouts.
- [ ] MySQL contains an empty `mall` database and the accepted least-privilege identities; no legacy
      tables or seed rows exist.
- [ ] Search cannot write to MySQL; application identities cannot change schema; root is not an
      application credential.
- [ ] Redis rejects unauthenticated access and returns authenticated `PONG`; its local memory and
      persistence policy is documented.
- [ ] If MongoDB is retained, its app identity, exact collections, unique ownership indexes, and
      history query index are present and verified.
- [ ] RabbitMQ has the `/mall` vhost, correct app permissions, exact topology, working TTL/DLX path,
      and terminal failure DLQ.
- [ ] The Elasticsearch image builds from pinned inputs on ARM64 and AMD64, lists `analysis-ik`, and
      passes the accepted `_analyze` cases without a production product index.
- [ ] MinIO has only approved private buckets and a non-root app identity; anonymous and out-of-scope
      access is denied.
- [ ] Bootstrap and verification can run twice without errors, duplicates, permission broadening, or
      destructive reset.
- [ ] Real Java clients selected by the accepted dependency baseline connect through the explicit
      infrastructure-test profile.
- [ ] A fresh local ARM64 rehearsal and fresh CI Linux AMD64 job are green.
- [ ] CI always captures useful failure diagnostics and cleans up only its Compose project.
- [ ] No migration, schema, business data, production Java, runtime application configuration,
      application image, or feature has been added.
- [ ] The runbook lets a junior developer start, verify, restart, and safely reset the environment
      without shelling into a container to repair it.

## 10. Stop condition

Stop Milestone 2 when an empty Docker state becomes a healthy, initialized, verified local dependency
environment on developer ARM64 and CI AMD64, and the same bootstrap passes again with existing
volumes. The ordinary Maven verification must still pass with the infrastructure stopped.

Do **not** begin Milestone 3 until all acceptance criteria are recorded in a Milestone 2 completion
summary. In particular, a green `docker compose ps` is not enough: identities, negative privileges,
RabbitMQ routing, Mongo indexes, IK analysis, MinIO privacy, Java client connections, idempotency, and
CI architecture must all be proved.

If U-05, U-06, or U-11 is not accepted, if any image/helper remains mutable, if bootstrap needs a
manual container edit, or if verification only works with pre-existing volumes, the milestone is not
complete. Stop and resolve the cause rather than compensating with undocumented steps.

## 11. Common mistakes

- Copying the old Compose file and preserving MySQL 5.7, Elasticsearch 7.17, MongoDB 4, RabbitMQ
  3.9, an untagged MinIO image, `container_name`, absolute `/mydata` mounts, links, and inconsistent
  service aliases.
- Treating an exact tag as immutable without using the accepted digest.
- Adding a helper image with `latest` while carefully pinning the main services.
- Calling a container “ready” because its process is running or its port is open.
- Using `sleep 30` instead of health and completion conditions.
- Putting root/bootstrap credentials into future application properties.
- Hard-coding credentials in `definitions.json` because it does not expand environment variables.
- Logging secrets through `set -x`, rendered URLs, failed command lines, or CI diagnostics.
- Binding ports to all host interfaces when only local access is needed.
- Using fixed container names, which makes parallel clones and CI runs collide.
- Importing `mall.sql`, creating app tables, or seeding RBAC/settings to make the environment feel
  complete. That is Milestone 3.
- Letting MongoDB derive collection names or using find-then-insert without a unique index.
- Copying the old Rabbit topology without a failure DLQ or claiming TTL/DLX provides exactly-once
  processing.
- Installing IK at container startup or downloading it without checking the accepted SHA-256.
- Creating a production Elasticsearch product index before Q0.
- Making a MinIO bucket public so a browser can access it during testing.
- Adding broad client dependencies to `mall-common` or production scope just for smoke tests.
- Making normal `./mvnw verify` depend on running Docker services.
- Running `docker system prune`, deleting unrelated volumes, or copying legacy destructive scripts.
- Adding Logstash, Kibana, Nginx, application containers, or production hardening before the minimum
  I0/I0.1 stack passes.
- Claiming a tested Docker/Compose minimum lower than the versions actually verified.
- Continuing into Milestone 3 because infrastructure finished early.

## 12. What to review before approving implementation

Reviewers should focus on these decisions rather than line-by-line shell syntax:

1. Explicitly accept or replace the U-05 grant model.
2. Explicitly accept no demo profile under U-06.
3. Explicitly accept retaining MongoDB under U-11, including its exact collection/index contract.
4. Confirm the required service list remains MySQL, Redis, MongoDB, RabbitMQ, Elasticsearch+IK, and
   MinIO—without Nginx/ELK/application containers.
5. Confirm all host ports bind to loopback and are overrideable.
6. Confirm Redis is treated as disposable local cache state.
7. Confirm RabbitMQ topology provisioning belongs in I0.1 while Java messaging semantics remain O2.
8. Confirm Elasticsearch creates no production product index in this milestone.
9. Confirm MinIO remains private and Aliyun OSS stays deferred.
10. Confirm test-only Java client smoke tests are acceptable and normal Maven verification remains
    service-independent.
11. Confirm no old SQL dump, migrations, seed data, or application runtime configuration enters this
    milestone.
12. Confirm project-scoped volume deletion is acceptable for the documented clean-state rehearsal.

## 13. Exact implementation prompt to use after review

Use the following prompt only after the three decision lines at the top have been explicitly approved
or edited to match the accepted ADR decisions:

```text
Implement Milestone 2 for the mall backend rebuild in:

/Users/kevinyang/Desktop/Development/mall-rebuild

Use this reviewed guide as the implementation contract:

docs/rebuild/MILESTONE_2.md

Accepted review decisions:

1. U-05: use separate MySQL migration-owner, admin read-write, portal read-write, and search
   read-only identities; application identities must not use root or receive schema-changing grants.
2. U-06: do not create a demo profile or mall-demo module.
3. U-11: retain MongoDB only for member product collections, brand follows, and read history, using
   the exact collection names and indexes approved in the Milestone 2 ADR.

Implement only task-graph items I0 and I0.1. Do not begin Milestone 3 or any feature work.

Before editing, read the Milestone 2 guide completely, inspect the current working tree, rerun the
Milestone 1 verification, and preserve unrelated user changes. If any accepted decision above
conflicts with the reviewed guide or is still unresolved, stop and report the blocker before creating
infrastructure files.

Create the single reproducible local dependency stack, environment template, decision ADRs,
idempotent service bootstrap, verification scripts, local runbook, explicit test-only Java-client
integration profile, and separate Linux AMD64 CI infrastructure job described by the guide. Use the
exact accepted service versions and immutable upstream image digests from the Milestone 0 evidence.
For IK 8.18.8, pin the accepted Elasticsearch base digest and verify the exact artifact SHA-256
e7d239c3261e2f5862034286bb30e9109f31917e8cc7a93431281ce54da8f966
during image build. Do not introduce an unpinned helper image.

Keep normal ./mvnw verify independent of Docker and external services. Infrastructure integration
tests must run only through an explicit profile and must use test-scope dependencies in the existing
six modules; do not create a seventh module or production Java code.

Verify all positive and negative privilege boundaries, RabbitMQ TTL/DLX/failure topology, retained
MongoDB collections/indexes, IK analyzer terms, and MinIO private policies. Prove an empty-volume
bootstrap, an existing-volume restart, a repeated idempotent bootstrap, native developer ARM64, and
CI Linux AMD64. Cleanup must be guarded and limited to this Compose project; never use global prune.

Do not create or import SQL schema/tables/seeds/migrations, Spring Boot applications or runtime YAML,
controllers/services/repositories/entities, business features, product search mappings/indexes,
messaging producers/consumers/outbox behavior, storage adapters/upload APIs, app images, Nginx, ELK,
deployment, or production secrets.

When implementation and verification are complete, create a Milestone 2 completion summary that
records exact files, versions/digests, commands, test counts, local ARM64 evidence, CI AMD64 run/commit,
known limitations, and an explicit statement that Milestone 3 has not started. Stop at the Milestone
2 acceptance boundary.
```

## 14. Source basis

This guide was derived from the accepted evidence and the current clean repository, especially:

- old evidence `docs/rebuild/REBUILD_GUIDE.md`, sections 3–5 and Milestone 2;
- old evidence `docs/rebuild/REBUILD_TASK_GRAPH.md`, I0/I0.1 and Stage 1 ordering;
- old evidence `docs/rebuild/MILESTONE_0.md` and its U-05/U-06/U-11 gates;
- old evidence `docs/rebuild/REBUILD_RISK_REGISTER.md`, especially H-02/H-04 and M-01–M-08,
  M-18, M-20, M-22, and M-28;
- old evidence `docs/architecture/version-matrix.md`;
- old evidence `docs/evidence/u03-service-compatibility.md`;
- old evidence `docs/evidence/u04-ik-analyzer.md`;
- this repository's `docs/rebuild/MILESTONE_1_COMPLETION_SUMMARY.md`, root/module POMs, CI workflow,
  and ADR-0001 through ADR-0003.

The old repository is evidence only. Its Compose files, credentials, links, versions, host paths,
SQL dump, and deployment scripts are not implementation templates.
