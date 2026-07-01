# Shared Standards

Freeze these standards before deep implementation:

- Package naming: `com.example.restaurant.<service>`
- Error body: `code`, `message`, `traceId`, `timestamp`
- Correlation header: `X-Correlation-Id`
- Event envelope: `eventId`, `eventVersion`, `occurredAt`, `source`, `traceId`, `payload`
- Default Java version: `17`
- Default Spring profile key: `SPRING_PROFILES_ACTIVE`
- Per-service schema ownership only
- No direct cross-service database reads or writes

## Folder Rules

- `services/<service>/`: service owner only
- `docs/contracts/<service>/`: service owner only
- `platform/services/<service>/`: service owner only
- `docs/contracts/events/`, `platform/base/`, and GitOps overlays: shared integration phase only
