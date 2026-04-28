# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

dbstream-driver is a JDBC proxy driver for Change Data Capture (CDC). It intercepts SQL operations at the JDBC layer, parses them, and emits structured `DBEvent` objects containing the before/after data. Zero runtime dependencies beyond Lombok (optional).

- **Java 8**, Maven build, published to Maven Central as `com.codingapi.dbstream:dbstream-driver`

## Build & Test Commands

```bash
./mvnw clean compile              # compile
./mvnw clean test                 # run all tests
./mvnw clean test -P travis      # run tests with JaCoCo + OpenClover coverage
./mvnw clean deploy -P ossrh     # publish to Maven Central (needs GPG)
./mvnw test -Dtest=InsertSQLParserTest   # run a single test class
```

Tests use Surefire with `forkCount=1, reuseForks=false`.

## Architecture

### JDBC Proxy Pattern

Registered via SPI (`META-INF/services/java.sql.Driver`). The static initializer self-registers with `DriverManager`. The `connect()` flow:

1. `DBStreamProxyDriver` finds the real driver from `DriverManager`, caches by URL
2. `DBScanner` scans database metadata (tables, columns, PKs) via `java.sql.DatabaseMetaData`, serializes to `.dbstream/<jdbcKey>/` for caching
3. Returns `ConnectionProxy` wrapping the real connection + `DBMetaData`

All Statement/PreparedStatement/CallableStatement creation is proxied. `PreparedStatementProxy` captures every `setXxx()` parameter.

### Event Flow

```
SQL execute → SQLRunningContext.before()/after() → DBEventListener
  → SQLParser (table name extraction, regex-based)
  → DBEventParser.prepare() [before] / loadEvents() [after]
  → TransactionEventPools (ThreadLocal, auto-commit=immediate push, manual=accumulate)
  → DBEventPusher.push() on commit
```

- **INSERT**: Extract values from SQL + JDBC params, handle batch via `DBEventCacheContext`
- **UPDATE**: Query DB for pre-image (before), post-image (after) using primary keys
- **DELETE**: Query DB for pre-image before deletion

### Key Singletons

All use eager initialization (`private static final`):
- `DBStreamContext` — public API facade for registering listeners/pushers
- `SQLRunningContext` — orchestrates `SQLExecuteListener` callbacks
- `DBEventContext` — manages `DBEventPusher` instances, dispatches events
- `TransactionEventPools` — ThreadLocal event accumulation per transaction
- `DBMetaContext` — metadata cache keyed by `jdbcKey` (`sha256(jdbcUrl#schema)`)

### SQL Parsers

Regex-based (no external library). `InsertSQLParser`/`UpdateSQLParser`/`DeleteSQLParser` each extract table name, columns, WHERE clause, and detect aliases/batch patterns. SQL classification in `SQLUtils` by first keyword.

## Test Structure

Two categories:
- **SQL parser unit tests** (`com.codingapi.dbstream.sqlparser`) — JUnit 5 parameterized tests from CSV files in `src/test/resources/`. No Spring context.
- **Integration tests** (`com.example.dbstream.tests`) — Spring Boot test with H2, verifies full event lifecycle (insert/update/delete/batch/rollback). Each test registers a custom `DBEventPusher` with assertions.

Integration test JDBC URL in `src/test/resources/application.properties` uses the DBStream driver wrapping H2.
