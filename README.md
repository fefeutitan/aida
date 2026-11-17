# AIDA

Artificial Intelligent Document Analyzer.

O AIDA analisa documentos PDF para uso futuro em fluxos de IA generativa e RAG. Nesta etapa, a aplicação oferece gestão de documentos, armazenamento local, extração de texto com PDFBox e persistência de metadados. Embeddings, RAG, OCR e chamadas a modelos ainda não fazem parte desta etapa.

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

O banco fica disponível em `127.0.0.1:5433`, e as migrações Flyway habilitam a extensão `vector` e criam a tabela `documents` quando a aplicação inicia.

## Executar a aplicação

```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5433/aida"
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

## Gestão de documentos PDF

O limite padrão de upload é 10 MB. O diretório de armazenamento pode ser alterado com `AIDA_STORAGE_LOCATION` e, por padrão, é `./data/documents`.

Enviar um PDF:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/v1/documents `
  -Form @{ file = Get-Item .\exemplo.pdf }
```

Listar documentos:

```powershell
Invoke-RestMethod "http://localhost:8080/api/v1/documents?page=0&size=20"
```

Consultar ou excluir um documento:

```powershell
$id = "00000000-0000-0000-0000-000000000000"
Invoke-RestMethod "http://localhost:8080/api/v1/documents/$id"
Invoke-RestMethod -Method Delete "http://localhost:8080/api/v1/documents/$id"
```

O arquivo físico recebe um nome baseado em UUID e o nome original não é usado como caminho. A resposta da API contém somente metadados; o texto extraído não é exposto integralmente.

## Build e testes

```powershell
mvn dependency:resolve
mvn clean verify
```

Os testes unitários cobrem a extração PDFBox, o serviço e o controller. O teste de contexto usa Testcontainers, executa as migrações Flyway e verifica a extensão `vector` e a tabela `documents`. Docker deve estar em execução para os testes de integração. Nenhum teste faz chamadas reais à OpenAI.

Limitações atuais: não há OCR para PDFs escaneados, embeddings, indexação vetorial ou RAG.

## Estrutura modular

Os pacotes reservados são `document`, `ingestion`, `question` e `shared`, sob `com.fehell.aida`. Eles permanecem sem classes artificiais até que cada funcionalidade seja aprovada e implementada.
