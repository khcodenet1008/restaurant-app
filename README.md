# Restaurant App

Starter project boilerplate for the CBLM restaurant microservices assignment.

## Repositories

- `restaurant-app/`: application code, service-local config, service-local manifests, contracts, and docs
- `restaurant-gitops/`: GitOps desired state, overlays, and release-time deployment assembly

## Services

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`
- `notification-service`
- `inventory-service`
- `kitchen-service`

## Structure

- `services/`: one folder per service owner
- `docs/contracts/`: API, route, and event contracts
- `docs/standards/`: frozen team conventions and config reference
- `docs/database/`: SQL template guide and database ownership notes
- `platform/`: service-local Kubernetes manifests and shared platform base
- `.github/workflows/`: CI workflow boilerplate

## Ownership Model

- Member 1: `gateway-service`, `menu-service`
- Member 2: `order-service`
- Member 3: `payment-service`, `notification-service`
- Member 4: `inventory-service`, `kitchen-service`

## Next Steps

1. Freeze standards in `docs/standards/`.
2. Keep work inside the owned service folders.
3. Publish API or event contracts before cross-service integration.
4. Use `restaurant-gitops/` only for shared deployment assembly and overlays.
5. Use `docs/database/README.md` and service-local Flyway migrations for database work.
