# AIDA

Artificial Intelligent Document Analyzer.

Esta etapa prepara a infraestrutura base: Spring Boot, PostgreSQL 17 com pgvector, Flyway, Actuator, OpenAPI e o esqueleto modular da aplicação. Upload, RAG e chamadas a modelos ainda não fazem parte desta etapa.

## Pré-requisitos

- JDK 21
- Maven 3.9+ (ou `mvnw.cmd`, se o wrapper estiver funcional no ambiente)
- Docker Desktop com Docker Compose

## Executar o PostgreSQL

No PowerShell, a partir da raiz do projeto:

```powershell
Copy-Item .env.example .env
docker compose up -d postgres
docker compose ps
```

O banco fica disponível em `localhost:5432`, e a migração do Spring Boot habilita a extensão `vector` quando a aplicação inicia.

## Executar a aplicação

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/aida"
$env:DB_USERNAME = "aida"
$env:DB_PASSWORD = "change-me"
$env:OPENAI_API_KEY = ""
mvn spring-boot:run
```

Verificações úteis:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
Start-Process http://localhost:8080/swagger-ui.html
```

## Build e testes

```powershell
mvn dependency:resolve
mvn clean verify
```

O teste de contexto usa Testcontainers e inicia um PostgreSQL com pgvector. Ele exige Docker em execução e não faz chamadas reais à OpenAI.

## Estrutura modular

Os pacotes reservados são `document`, `ingestion`, `question` e `shared`, sob `com.fehell.aida`. Eles permanecem sem classes artificiais até que cada funcionalidade seja aprovada e implementada.
