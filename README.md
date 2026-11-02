# NexERP Backend — Etapa 0

Fundação do backend modular do NexERP. Esta etapa prepara a aplicação Spring Boot,
PostgreSQL, Flyway, Actuator, Docker Compose, teste de integração com Testcontainers
e CI básico. Autenticação, autorização e RLS ficam para a Etapa 1.

## Pré-requisitos

- Windows 11 Pro
- JDK 21
- Maven 3.9+
- Docker Desktop com Linux containers e Docker Compose

## Execução local com Docker Compose

No PowerShell, dentro desta pasta:

```powershell
Copy-Item .env.example .env
notepad .env
docker compose --env-file .env up -d postgres
```

Altere `DB_PASSWORD` no `.env` antes de iniciar o banco. Para subir também a API e o
proxy local:

```powershell
docker compose --env-file .env up --build -d
```

Endpoints de verificação:

- API: `http://localhost:8080/actuator/health`
- Caddy local: `http://localhost:8081/actuator/health`

O resultado esperado é HTTP 200 com status `UP`.

## Execução pelo Maven

Com o PostgreSQL do Compose em execução, defina as variáveis no PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_URL = "jdbc:postgresql://localhost:5432/nexerp"
$env:DB_USER = "nexerp"
$env:DB_PASSWORD = "a-mesma-senha-do-arquivo-env"
```

Depois:

```powershell
mvn clean verify
mvn spring-boot:run
```

## Testes

Os testes de integração iniciam um PostgreSQL 16 via Testcontainers e verificam que o
Flyway cria as tabelas de baseline:

```powershell
mvn clean verify
```

O Docker Desktop precisa estar em execução para esses testes.

O projeto fixa Testcontainers 1.21.4 para manter compatibilidade com versões recentes
do Docker Engine. O teste usa a configuração padrão do Docker Desktop no Windows.

## Estrutura

Os módulos seguem a separação `api`, `application`, `domain` e `infra`. Nesta etapa os
pacotes estão preparados, mas as regras de negócio serão implementadas nas etapas
seguintes.

## Limites desta etapa

- Não há login, JWT, refresh token ou RBAC.
- O isolamento completo por tenant e empresa ainda não está implementado.
- RLS será implementado na Etapa 1, junto com o contexto transacional.
- Não há vendas, estoque operacional, fiscal, financeiro ou integração Angular.
- O Caddy está configurado apenas como proxy local; TLS de produção será tratado no
  hardening.
