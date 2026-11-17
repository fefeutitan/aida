# AIDA

Artificial Intelligent Document Analyzer.

AIDA analyzes PDF documents for future use in generative AI and RAG workflows. At this stage, the application provides document management, local storage, text extraction with PDFBox, and metadata persistence. Embeddings, RAG, OCR, and model calls are not part of this stage yet.

## Prerequisites

- JDK 21
- Maven 3.9+ (or `mvnw.cmd`, if the wrapper works in your environment)
- Docker Desktop with Docker Compose

## Run PostgreSQL

From the project root, in PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
docker compose ps
```

The database is available at `127.0.0.1:5433`. Flyway migrations enable the `vector` extension and create the `documents` table when the application starts.

## Run the application

```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/aida"
$env:DB_USERNAME = "aida"
$env:DB_PASSWORD = "change-me"
$env:OPENAI_API_KEY = ""
mvn spring-boot:run
```

Useful checks:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
Start-Process http://localhost:8080/swagger-ui.html
```

## PDF document management

The default upload limit is 10 MB. The storage directory can be changed with `AIDA_STORAGE_LOCATION`; the default is `./data/documents`.

Upload a PDF:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/v1/documents `
  -Form @{ file = Get-Item .\example.pdf }
```

List documents:

```powershell
Invoke-RestMethod "http://localhost:8080/api/v1/documents?page=0&size=20"
```

Retrieve or delete a document:

```powershell
$id = "00000000-0000-0000-0000-000000000000"
Invoke-RestMethod "http://localhost:8080/api/v1/documents/$id"
Invoke-RestMethod -Method Delete "http://localhost:8080/api/v1/documents/$id"
```

Each physical file receives a UUID-based name, and the original filename is never used as a filesystem path. API responses contain metadata only; the full extracted text is not exposed.

## Build and tests

```powershell
mvn dependency:resolve
mvn clean verify
```

Unit tests cover PDFBox extraction, the document service, and the controller. The context test uses Testcontainers, runs Flyway migrations, and verifies the `vector` extension and `documents` table. Docker must be running for integration tests. No test makes real OpenAI calls.

Current limitations: no OCR for scanned PDFs, embeddings, vector indexing, or RAG.

## Modular structure

The reserved packages are `document`, `ingestion`, `question`, and `shared`, under `com.fehell.aida`. They should remain free of artificial classes until each feature is approved and implemented.
