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

## Deploy

```bash
kubectl create namespace restaurant-demo --dry-run=client -o yaml | kubectl apply -f -
kubectl -n restaurant-demo create secret generic mysql-secret \
  --from-literal=root-password=root1234 \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k ../restaurant-gitops/overlays/dev
```
