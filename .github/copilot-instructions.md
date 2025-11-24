## Quick context

- This repository is a multi-module Java Spring Boot microservices system (parent POM under `base-pom/pom.xml` / `pom.xml`). Modules: `user-service`, `part-service`, `campaign-service`, `vehicle-service`, `warranty-service`. There is also an `api-gateway` (Spring Cloud Gateway, reactive) and a frontend (Vite) in `frontend/`.
- Java 21, Spring Boot 3.x and Spring Cloud (2023.x) are used across modules. Many services include `Dockerfile` and the repository contains a root `docker-compose.yml` for local multi-service runs.

## Purpose for an AI coding agent

Be immediately productive by focusing on the multi-module Maven layout, how services communicate (Feign, load balancer, gateway), and the build/run conventions used in this project.

## What to know (high-value facts)

- Parent POM: `base-pom/pom.xml` (defines shared dependencies and Java version). Individual modules inherit from that parent (see `part-service/pom.xml`).
- API Gateway: `api-gateway/` uses Spring Cloud Gateway (`spring-boot-starter-webflux` + `spring-cloud-starter-gateway`). Look in `api-gateway/src/main/java` for routing/auth filters.
- Inter-service communication: services use OpenFeign (`spring-cloud-starter-openfeign`) and Spring Cloud load-balancer; resilience is provided by Resilience4j (`spring-cloud-starter-circuitbreaker-resilience4j`).
- Security & tokens: JWT libraries (jjwt) appear in several modules (e.g. `warranty-service`, `api-gateway`) — expect token parsing and shared auth patterns in gateway and services.
- Persistence: Spring Data JPA + MySQL connector; look for repositories under `*/src/main/java/**/repository`.
- Frontend: `frontend/` is a Vite app. Scripts: `npm run dev`, `npm run build`, `npm run preview` (see `frontend/package.json`).

## Build & run (concrete commands for Windows PowerShell)

- Build entire multi-module project (fast, skip tests):
  - .\mvnw.cmd clean package -DskipTests
- Build a single module and its dependencies (e.g., `part-service`):
  - .\mvnw.cmd -pl part-service -am clean package -DskipTests
- Run a module locally (Spring Boot):
  - From repo root: .\mvnw.cmd -pl part-service spring-boot:run
  - Or run JAR: java -jar .\part-service\target\part-service-0.0.1-SNAPSHOT.jar
- Docker / compose: root `docker-compose.yml` assembles multiple services. Rebuild and up:
  - docker-compose up --build

Notes: many modules include an executable `Dockerfile`. Use the module folder as build context when building a single image.

## Project-specific patterns & conventions

- Module inheritance: use `base-pom/pom.xml` and module `pom.xml` files avoid re-declaring versions present in the parent.
- Lombok is used (provided scope) — code often relies on generated getters/setters/constructors. Watch for missing annotation processors in IDE if trying to compile locally.
- Tests are sometimes intentionally skipped in module POMs (e.g., `api-gateway` surefire config). CI or local work may rely on `-DskipTests`.
- OpenFeign clients live alongside service interfaces; search for `@FeignClient` to find integration points. Circuit breakers are applied using Resilience4j annotations/config.
- JWT handling: token logic often lives in gateway filters or a shared security package inside services. Search for `io.jsonwebtoken` imports.

## Files and locations to inspect for common tasks

- Parent and shared config: `base-pom/pom.xml`, root `pom.xml`
- Gateway routes & filters: `api-gateway/src/main/java` and `api-gateway/src/main/resources` (for application config)
- Module examples: `part-service/pom.xml`, `warranty-service/pom.xml` (shows Feign, JWT, OpenAPI usage)
- Docker + compose: `Dockerfile` in service folders and `docker-compose.yml` at repo root
- Frontend: `frontend/package.json`, `frontend/src/` (JS components and services under `frontend/src/services`)

## Suggestions for the agent when authoring code or PRs (concrete, repo-specific)

- When adding a dependency, prefer adding it to `base-pom/pom.xml` if it will be used by multiple modules. For a module-specific library, add it to that module's `pom.xml` without duplicating versions.
- Keep Java target at 21. Do not downgrade Java unless the whole repo is bumped.
- When changing inter-service contracts (DTOs, REST paths), update any Feign clients and Gateway route rules accordingly; search for usages with a workspace-wide search for the controller path.
- For new endpoints, add OpenAPI annotations — `part-service` uses `springdoc-openapi-starter-webmvc-ui` for API docs.

## Quick troubleshooting hints

- If Lombok-generated members are not found: ensure annotation processors are enabled in the IDE and IDE JDK matches the project's Java version.
- If builds fail due to Spring Cloud versions mismatch, check `base-pom/pom.xml` (spring-cloud.version) and the parent `pom.xml` for aligned versions.
- To inspect runtime config, check `src/main/resources/application.yml` in the module or the assembled `target/classes/application.yml` after a build.

If anything above is incomplete or you'd like examples added (e.g., common JWT utility file, typical Feign client example), tell me which module to inspect and I will add short, focused examples or merge existing guidance into this file.
