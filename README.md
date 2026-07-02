# Restaurant App

Simple classroom baseline for the CBLM restaurant microservices assignment.

## Services

- `gateway-service`
- `menu-service`
- `order-service`
- `payment-service`

## Current Scope

- `gateway-service` forwards public API routes and adds a correlation ID header
- `menu-service` provides menu item read APIs
- `order-service` creates, reads, and cancels orders
- `payment-service` provides a mock confirm API and publishes payment results
- Kafka is kept only for simple order and payment events
- Istio mesh is prepared only for these same 4 services

## Build

```bash
mvn clean package
docker build -t docker.io/khcodenet1008/gateway-service:dev services/gateway-service
docker build -t docker.io/khcodenet1008/menu-service:dev services/menu-service
docker build -t docker.io/khcodenet1008/order-service:dev services/order-service
docker build -t docker.io/khcodenet1008/payment-service:dev services/payment-service
```

## CI/CD

- Pull requests and `main` pushes run Maven tests for:
  - `gateway-service`
  - `menu-service`
  - `order-service`
  - `payment-service`
- Pushes to `main` also:
  - build and push Docker Hub images tagged with the commit SHA and `latest`
  - update `restaurant-gitops` deployment image tags to the same SHA

Required GitHub Actions secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `GITOPS_REPO_TOKEN`

## Deploy

```bash
kubectl create namespace restaurant-demo --dry-run=client -o yaml | kubectl apply -f -
kubectl -n restaurant-demo create secret generic mysql-secret \
  --from-literal=root-password=root1234 \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k ../restaurant-gitops/overlays/dev
```

Inspect MySQL with phpMyAdmin:

```bash
kubectl port-forward service/phpmyadmin -n restaurant-demo 8081:80
```

Then open `http://localhost:8081` and sign in with:

- server: `mysql`
- username: `restaurant_app`
- password: `change-me`

## Frontend

The workspace root now also includes a simple learning UI in `../frontend`.

Run it after port-forwarding the gateway:

```bash
kubectl port-forward deployment/gateway-service -n restaurant-demo 8080:8080
cd ../frontend
npm install
npm run dev
```

Frontend environment variable:

```bash
VITE_GATEWAY_API_BASE_URL=http://localhost:8080
```
