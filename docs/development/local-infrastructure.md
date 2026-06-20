# Local infrastructure contract

## Current status

Milestone 2 Stage 1 is the accepted environment contract only. There is currently no `compose.yaml`,
no infrastructure bootstrap, and no dependency service to start from this repository. Commands in
this document describe names that later Milestone 2 stages must implement; they are not a claim that
the stack already exists.

## Purpose

The future local stack will provide reproducible development and CI dependencies for the mall
backend. It will contain MySQL, Redis, MongoDB, RabbitMQ, Elasticsearch with IK, and MinIO. It will
not contain application containers, a demo environment, Nginx, Logstash, Kibana, or production
deployment services.

The contract is frozen before Compose so bootstrap scripts, verification tests, and later application
configuration use the same names. `.env.example` is the canonical variable-name contract. Its values
are non-secret local examples, not production configuration.

## Preparing the local environment file

Later stages will use an ignored `.env` file:

```bash
cp .env.example .env
```

Do not put production credentials in `.env.example` or commit `.env`. Stage 1 does not require
copying the file or starting Docker.

## Compose and endpoint contract

The Compose service names and in-network ports are stable. Host ports remain overrideable through
`.env` so local port conflicts do not change container-to-container addresses.

| Dependency | Compose DNS endpoint | Developer-host endpoint | Host-port variable |
|---|---|---|---|
| MySQL | `mysql:3306` | `127.0.0.1:3306` by default | `MYSQL_HOST_PORT` |
| Redis | `redis:6379` | `127.0.0.1:6379` by default | `REDIS_HOST_PORT` |
| MongoDB | `mongo:27017` | `127.0.0.1:27017` by default | `MONGO_HOST_PORT` |
| RabbitMQ AMQP | `rabbitmq:5672` | `127.0.0.1:5672` by default | `RABBITMQ_HOST_PORT` |
| RabbitMQ management | `rabbitmq:15672` | `127.0.0.1:15672` by default | `RABBITMQ_MANAGEMENT_HOST_PORT` |
| Elasticsearch | `elasticsearch:9200` | `127.0.0.1:9200` by default | `ELASTICSEARCH_HOST_PORT` |
| MinIO API | `minio:9000` | `127.0.0.1:9000` by default | `MINIO_HOST_PORT` |
| MinIO console | `minio:9001` | `127.0.0.1:9001` by default | `MINIO_CONSOLE_HOST_PORT` |

Later Compose work must bind published ports to `127.0.0.1`, use these service names, and avoid fixed
container names, legacy aliases, links, and host-specific absolute volume paths.

## MySQL contract

- Database: `mall`
- Bootstrap identity: `root`, never used by an application
- Migration identity: `mall_migration`, the only non-root schema-changing identity
- Admin identity: `mall_admin`, application read/write without schema changes
- Portal identity: `mall_portal`, application read/write without schema changes
- Search identity: `mall_search`, read-only

ADR-0004 defines the authoritative privilege boundary. Stage 1 creates no database or account.
Milestone 3—not infrastructure bootstrap—creates tables, constraints, migrations, and seed data.

## Redis contract

Redis will require the password named by `REDIS_PASSWORD`. It remains a cache/rate-limit aid rather
than a system of record. The exact bounded-memory and local persistence configuration belongs to the
later Redis implementation stage. Stage 1 creates no Redis configuration or keys.

## MongoDB contract

- Database: `mall_portal`
- Root/bootstrap identity: supplied by `MONGO_ROOT_*`, never used by an application
- Application identity: `mall_portal_app`, limited to `mall_portal`

| Collection | Index contract |
|---|---|
| `member_product_collections` | Unique `uq_member_product_collections_member_product` on `{memberId: 1, productId: 1}` |
| `member_brand_follows` | Unique `uq_member_brand_follows_member_brand` on `{memberId: 1, brandId: 1}` |
| `member_read_history` | Non-unique `ix_member_read_history_member_created` on `{memberId: 1, createTime: -1}` |

There is no read-history TTL policy and no permission to add other MongoDB features implicitly.
Stage 1 creates no MongoDB database, user, collection, index, or document.

## RabbitMQ contract

- Virtual host: `/mall`
- Bootstrap identity: `mall_local_admin`, used only for local initialization/management
- Application identity: `mall_app`, limited to the `/mall` vhost

Later stages will define and verify the version-controlled exchanges, queues, bindings, TTL/DLX
path, and failure DLQ. Stage 1 creates none of that topology and does not implement producers or
consumers.

## Elasticsearch contract

The service name is `elasticsearch`, and the host port defaults to `9200`. A later stage will build
the accepted Elasticsearch 8.18.8 image with checksum-pinned IK 8.18.8. Stage 1 creates no image,
plugin, product index, template, mapping, alias, or search behavior.

## MinIO contract

- Private product bucket: `mall-product`
- Root/bootstrap identity: `mall_minio_root`, never used by an application
- Application identity: `mall_storage_app`, later limited to the approved private bucket policy

Stage 1 creates no MinIO service, identity, bucket, policy, object, or storage adapter. Public bucket
access is not part of the contract.

## Demo decision

There is no demo Compose profile, no demo credential set, and no `mall-demo` module. A future real
demo requirement needs a new decision rather than an undocumented flag or reused production path.

## Stage boundary

Stage 1 ends after the ADRs, `.env.example`, and this contract verify successfully. Do not run Docker
or add any of the following during Stage 1:

- `compose.yaml` or files under `infra/`;
- health, bootstrap, reset, or infrastructure verification scripts;
- POM, CI, or integration-test changes;
- application YAML or Java production code;
- schema migrations, tables, seeds, or business data.

The next stage may create the Compose topology only after a separate approval.
