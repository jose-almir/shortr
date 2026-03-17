# Shortr

A fast, no-nonsense URL shortener built with **Spring Boot 4**, **Java 25**, **PostgreSQL**, and **Redis**.

## Tech Stack

| Layer         | Technology                           |
|---------------|--------------------------------------|
| Runtime       | Java 25                              |
| Framework     | Spring Boot 4.0.3 (Web MVC)         |
| Database      | PostgreSQL 17                        |
| Cache         | Redis 7                              |
| Templates     | Thymeleaf                            |
| Validation    | Jakarta Bean Validation              |
| Build         | Maven                                |
| Testing       | JUnit 5 + Testcontainers            |
| Dev tools     | Lombok, Spring Boot DevTools         |

## Architecture

The project follows a **layered DDD** structure with clear separation of concerns:

```
com.almirdev.shortr
├── domain/              # Pure business rules — no framework imports
│   ├── model/           # Url entity
│   ├── repository/      # UrlRepository interface (port)
│   ├── service/         # UrlShorteningService, UrlRedirectService (ports)
│   ├── strategy/        # ShortCodeStrategy interface (port)
│   └── exception/       # UrlNotFoundException
├── application/         # Use-case orchestration
│   ├── service/         # UrlShorteningServiceImpl, UrlRedirectServiceImpl
│   └── dto/             # ShortenRequest, ShortenResponse
├── infrastructure/      # Framework adapters
│   └── strategy/        # Base62Strategy, RandomStrategy, ShortCodeGenerator
└── web/                 # HTTP layer
    ├── controller/      # UrlController, IndexController
    └── exception/       # GlobalExceptionHandler
```

### Layer Dependency Diagram

```mermaid
graph TD
    WEB["web/controller"] --> APP["application/service"]
    APP --> DOM_SVC["domain/service"]
    APP --> DOM_REPO["domain/repository"]
    APP --> INFRA_STRAT["infrastructure/strategy"]
    INFRA_STRAT --> DOM_STRAT["domain/strategy"]

    style DOM_SVC fill:#1a1a1a,stroke:#22d3ee,color:#e5e5e5
    style DOM_REPO fill:#1a1a1a,stroke:#22d3ee,color:#e5e5e5
    style DOM_STRAT fill:#1a1a1a,stroke:#22d3ee,color:#e5e5e5
    style APP fill:#1a1a1a,stroke:#a78bfa,color:#e5e5e5
    style INFRA_STRAT fill:#1a1a1a,stroke:#f97316,color:#e5e5e5
    style WEB fill:#1a1a1a,stroke:#34d399,color:#e5e5e5
```

## How It Works

### URL Shortening — `POST /shorten`

```mermaid
sequenceDiagram
    participant C as Client
    participant API as UrlController
    participant SVC as UrlShorteningServiceImpl
    participant CACHE as Redis
    participant DB as PostgreSQL

    C->>API: POST /shorten { longUrl }
    API->>SVC: shorten(longUrl)
    SVC->>CACHE: GET long:{longUrl}
    alt Cache Hit
        CACHE-->>SVC: shortCode
    else Cache Miss
        SVC->>DB: findByLongUrl(longUrl)
        alt Found in DB
            DB-->>SVC: Url entity
        else Not Found
            SVC->>SVC: shortCodeGenerator.generate(longUrl)
            SVC->>DB: save(Url)
        end
        SVC->>CACHE: SET long:{longUrl} → shortCode (TTL 24h)
    end
    SVC-->>API: shortCode
    API-->>C: 201 { shortCode }
```

### URL Redirect — `GET /{shortCode}`

```mermaid
sequenceDiagram
    participant C as Client
    participant API as UrlController
    participant SVC as UrlRedirectServiceImpl
    participant CACHE as Redis
    participant DB as PostgreSQL

    C->>API: GET /{shortCode}
    API->>SVC: redirect(shortCode)
    SVC->>CACHE: GET short:{shortCode}
    alt Cache Hit
        CACHE-->>SVC: longUrl
    else Cache Miss
        SVC->>DB: findByShortCode(shortCode)
        DB-->>SVC: Url entity
        SVC->>CACHE: SET short:{shortCode} → longUrl (TTL 7d)
    end
    SVC-->>API: longUrl
    API-->>C: 301 Location: longUrl
```

## Short Code Generation

The strategy is selected at startup via the `shortr.strategy` property using `@ConditionalOnProperty`:

```mermaid
graph LR
    GEN["ShortCodeGenerator"] --> IFACE["«interface» ShortCodeStrategy"]
    IFACE --> B62["Base62Strategy"]
    IFACE --> RND["RandomStrategy"]

    style IFACE fill:#1a1a1a,stroke:#22d3ee,color:#e5e5e5
    style B62 fill:#1a1a1a,stroke:#f97316,color:#e5e5e5
    style RND fill:#1a1a1a,stroke:#f97316,color:#e5e5e5
    style GEN fill:#1a1a1a,stroke:#a78bfa,color:#e5e5e5
```

| Strategy   | Config value         | How it works                                    |
|------------|----------------------|-------------------------------------------------|
| **Base62** | `shortr.strategy=base62` (default) | MD5 hash → first 8 bytes → Base62 encoding |
| **Random** | `shortr.strategy=random`           | 8 random chars from `[0-9a-zA-Z]`          |

## Caching Strategy

| Cache Key             | Direction            | TTL  |
|-----------------------|----------------------|------|
| `long:{longUrl}`      | longUrl → shortCode  | 24h  |
| `short:{shortCode}`   | shortCode → longUrl  | 7d   |

## Data Model

| Column         | Type        | Constraints                 |
|----------------|-------------|-----------------------------|
| `id`           | `BIGSERIAL` | PK, auto-increment         |
| `short_code`   | `VARCHAR(20)` | Unique, indexed           |
| `long_url`     | `TEXT`      | Not null, indexed           |
| `created_at`   | `TIMESTAMP` | Not null, auto-generated    |

## Getting Started

### Prerequisites

- Java 25+
- Docker & Docker Compose

### Run

```bash
# Start PostgreSQL and Redis
docker compose up -d

# Start the application
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

### Test

Integration tests use **Testcontainers** — Docker must be running:

```bash
./mvnw test
```

## License

MIT
