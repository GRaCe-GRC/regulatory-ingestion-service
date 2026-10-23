# regulatory-ingestion-service

Acquires regulatory files, stores them and publishes RegulationIngested.

> **Status:** creates and reads regulatory authorities through the [Regulatory API](https://github.com/GRaCe-GRC/regulatory-api).

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)

## Getting started
The service calls the Regulatory API. To run both on one machine, start the Regulatory API on
port 8080 (see its README), then this service on another port:

```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

| Environment variable | Default | Description |
|---|---|---|
| `REGULATORY_API_URL` | `http://localhost:8080` | Base URL of the Regulatory API |

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Authorities API

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/authorities` | `name`, `jurisdiction`, `createdBy` | `201`, empty body, `Location` header with the new authority's URL |
| `GET /api/v1/authorities` | | `200` with the list of authorities |
| `GET /api/v1/authorities/{id}` | | `200` with the authority |
| `DELETE /api/v1/authorities/{id}` | | `204`, empty body |

Requests are validated here (`400` with an `errors` field per property) and then sent to the
Regulatory API. Its `4xx` problem details (`404` unknown id, `409` duplicate name) are returned
unchanged; when it fails, cannot be reached or returns no `Location` for a new authority, the
response is `502`. Calls to the Regulatory API are in `boot/client/RegulatoryApiClient`, which uses the Regulatory
API's own DTOs (`service/model/dto`); `domain/mapper/RegulatoryAuthorityMapper` converts them to and
from this service's DTOs.

## Test and build
```bash
./mvnw verify
```

Branch verify runs the same command on every push to a branch and on every pull request, through the
shared [Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml) (`.github/workflows/branch-verify.yml`).

## Versions and images
Versions follow semantic versioning and come from the commit messages. Every merge to `main`
runs `.github/workflows/release.yml`: the CI again, then the shared
[release workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/README.md#release):

| Commit | Release |
|---|---|
| `fix: GRC-<n>: ...` | Patch |
| `feat: GRC-<n>: ...` | Minor |
| `chore:`, `docs:`, ... | No release |

A release creates the `vX.Y.Z` tag, a GitHub Release with notes, and the image
`ghcr.io/grace-grc/regulatory-ingestion-service:<version>` (also tagged `latest`). The version is not stored in
`pom.xml`: it uses `${revision}`, which is `0.0.1-SNAPSHOT` for local builds and the release
version in CI.

```bash
docker pull ghcr.io/grace-grc/regulatory-ingestion-service:latest
```

## Architecture
This repository is part of the **service layer**. It talks to the system of records only through the
inner APIs, and to other services through events (Amazon SQS/SNS).
See the [GRaCe-GRC documentation hub](https://github.com/GRaCe-GRC/GRaCe-GRC-docs).

## Contributing
See the organization's [contributing guide](https://github.com/GRaCe-GRC/.github/blob/main/CONTRIBUTING.md).
Commit messages use Conventional Commits with the ticket ID, for example
`feat: GRC-9: add <change>`.

## License
Licensed under the [Apache License 2.0](LICENSE).
