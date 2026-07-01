# Platform Base

Shared infrastructure boilerplate for local and cluster deployment.

- `namespace.yaml`: shared namespace
- `mysql-statefulset.yaml`: shared MySQL for demo-only schema separation
- `mysql-init/00-create-databases.sql`: demo schema and user bootstrap template
- `kafka-statefulset.yaml`: shared Kafka broker for demo-only messaging
- `kustomization.yaml`: base aggregation
