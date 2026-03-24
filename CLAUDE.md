# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

This is a Maven multi-module Spring Boot 3.5.9 project (Java 17).

```bash
# Build all modules (skip tests)
mvn clean package -DskipTests

# Build a single service
mvn clean package -DskipTests -pl services/order-service -am

# Run a single service (requires Nacos, MySQL, etc.)
mvn spring-boot:run -pl services/order-service

# Start infrastructure (Nacos, RabbitMQ, Seata, Zipkin)
docker compose -f infra/docker-compose.yml up -d
```

No tests are currently set up in the project beyond the default Spring Boot test stubs.

## Architecture

**Microservices e-commerce platform with flash sale (seckill) capability**, built on Spring Cloud Alibaba.

### Module Layout

- `libs/common-core` — Shared DTOs, constants (`GlobalConstants`), exception hierarchy (`BizException`/`IResultCode`/`SystemCode`), unified response wrapper (`Result<T>` with traceId from MDC)
- `libs/common-web` — `GlobalExceptionHandler` (RestControllerAdvice), validation support. Depends on common-core
- `services/gateway` — Spring Cloud Gateway (reactive). OAuth2 resource server JWT validation. Routes configured via Nacos
- `services/auth-service` — Spring Authorization Server (OAuth2 token issuer, RSA 2048-bit JWK)
- `services/user-service` — User CRUD
- `services/inventory-service` — Stock management with RabbitMQ listener for rollback
- `services/order-service` — Order lifecycle with delay queue timeout cancellation (30s TTL dead letter → auto-cancel unpaid orders → broadcast inventory rollback)
- `services/payment-service` — Payment processing, calls back to order-service via Feign
- `services/seckill-service` — High-concurrency flash sale: Redis Lua script for atomic stock deduction, Redis Set for user deduplication, Sentinel rate limiting, async order creation via RabbitMQ

### Key Infrastructure

- **Service Discovery & Config:** Nacos (port 18848, credentials nacos/nacos)
- **Messaging:** RabbitMQ (port 5672/15672, credentials admin/admin). Exchanges and queues defined in `GlobalConstants`
- **Distributed Transactions:** Seata (AT mode, ports 7091/8091)
- **Tracing:** Micrometer + Brave → Zipkin (port 9411). TraceId propagated via MDC and custom header `X-Request-Id`
- **Circuit Breaking:** Sentinel with `@SentinelResource` annotations and Feign fallback classes

### Communication Patterns

- **Sync:** OpenFeign clients (e.g., `InventoryClient`, `OrderClient`) with fallback classes
- **Async:** RabbitMQ topic/direct exchanges. Key flows:
  - Seckill → `seckill.order.queue` → async order creation
  - Order creation → delay queue (TTL) → dead letter → timeout cancellation → `inventory.stock.rollback.queue`
  - Order cancel → `order.event.exchange` → inventory rollback notification

### Conventions

- **ORM:** MyBatis Plus. Entity IDs use `@TableId(type = IdType.ASSIGN_ID)` (snowflake) for orders, auto-increment for simple entities
- **Error codes:** Implement `IResultCode` interface as enums (see `SystemCode`, `SeckillErrorCode`)
- **Response:** Always return `Result<T>` from controllers. Never throw raw exceptions — use `BizException` for business errors
- **MQ constants:** All queue/exchange/routing-key names centralized in `GlobalConstants`
- **Redis keys:** Prefixed via constants in `GlobalConstants` (e.g., `seckill:stock:`, `seckill:users:`)
- **Packages:** `com.axin.flashsale.{service}.{layer}` where layer is controller/service/entity/mapper/config/listener/client
- **Lombok:** `@Data`, `@Builder`, `@Slf4j` used throughout
- **Config:** Each service reads from Nacos with `spring.config.import: optional:nacos:${service}-${profile}.yml`
