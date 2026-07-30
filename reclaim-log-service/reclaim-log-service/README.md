# Reclaim.LogService — .NET Logging Sink

A minimal ASP.NET Core 8 Web API whose only job is to receive log events
from the Spring Boot backend and write them durably to disk (and console).
It contains no business logic, no database of its own beyond its log files,
and is not part of the Reclaim domain API — it exists purely as a logging
destination, reached over a single HTTP endpoint.

## What it does

```
Spring Boot (Java)  --HTTP POST-->  Reclaim.LogService (.NET)  --Serilog-->  logs/reclaim-backend-YYYYMMDD.log
      (every log line,                  /api/logs                                + console
       fire-and-forget, async)
```

Every `logger.info(...)`, `logger.warn(...)`, `logger.error(...)` etc. call
already present anywhere in the Spring Boot codebase is automatically
shipped here too — **no existing Java code needs to change**. This works
because it plugs in as an additional Logback appender, not as a new
explicit logging call scattered through the codebase.

## Prerequisites

- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)

## 1. Run the .NET logging service

```bash
cd reclaim-log-service
dotnet restore
dotnet run
```

By default it listens on `http://localhost:5225` (configured in
`appsettings.json` under `Kestrel:Endpoints:Http:Url`). On startup it
creates a `logs/` folder next to the project and starts writing
`reclaim-backend-<date>.log`.

**Change the API key before running for real:** `appsettings.json` ships
with `LogService:ApiKey = "CHANGE_ME_LOCAL_DEV_KEY"`. Change it to your own
value (or override via the environment variable `LogService__ApiKey` —
note the double underscore, which is how .NET config binds nested keys
from environment variables) and use the **same** value in the Spring Boot
side's `logging.dotnet.api-key` property, or requests will get `401`.

### Verify it's running

```bash
curl http://localhost:5225/health
# {"status":"UP","service":"Reclaim.LogService"}

curl -X POST http://localhost:5225/api/logs \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: CHANGE_ME_LOCAL_DEV_KEY" \
  -d '{"timestamp":"2026-07-30T10:00:00Z","level":"INFO","logger":"test","message":"Hello from curl","source":"manual-test"}'
```

Check `logs/reclaim-backend-<today>.log` — the line should appear there
within a moment.

Swagger UI is available at `http://localhost:5225/swagger` in the
Development environment.

## 2. Spring Boot side — already wired in this project

Three things were added to the backend, none of which require touching any
existing controller/service code:

1. **`src/main/java/com/cdac/logging/HttpLogAppender.java`** — a custom
   Logback appender that serializes each log event to JSON and POSTs it to
   the .NET service asynchronously (fire-and-forget), with a 2-second
   timeout and silent, non-recursive failure handling if the service is
   unreachable.
2. **`src/main/resources/logback-spring.xml`** — registers `HttpLogAppender`
   (wrapped in Logback's `AsyncAppender` for zero request-thread impact)
   alongside Spring Boot's normal console appender, and reads its target
   URL/API key from `application.properties` via `<springProperty>`.
3. **`application.properties`** — two new keys:
   ```properties
   logging.dotnet.url=http://localhost:5225/api/logs
   logging.dotnet.api-key=CHANGE_ME_LOCAL_DEV_KEY
   ```
   Leave `logging.dotnet.url` empty (or remove the line) to disable this
   appender entirely — `HttpLogAppender.start()` simply won't start
   without a URL, and every other appender keeps working normally.

No new Maven dependency was needed — `java.net.http.HttpClient` is built
into the JDK (11+), and `jackson-databind` is already on the classpath via
`spring-boot-starter-web`.

## 3. Run everything together

```bash
# Terminal 1
cd reclaim-log-service && dotnet run

# Terminal 2
cd reclaim_application && mvn spring-boot:run

# Terminal 3
cd reclaim-frontend && npm run dev
```

Use the app normally (log in, report an item, etc.) and watch
`reclaim-log-service/logs/reclaim-backend-<date>.log` fill up in real time
alongside the Spring Boot console output — both are receiving the exact
same log events.

## 4. What happens if the .NET service is down?

Nothing breaks. `HttpLogAppender` is wrapped in an `AsyncAppender` with
`neverBlock=true`, uses a 2-second connect timeout, and swallows delivery
failures after printing one line to `stderr` (to avoid spamming the
console if the service stays down for a long time). The Spring Boot app's
own console logging and all business functionality are completely
unaffected.

## 5. Honest scope note

Because a JVM process cannot literally execute .NET code in-process, this
is technically a second small running service reached over HTTP — it is
not "inside" the Java application. It was deliberately kept to a single,
narrow responsibility (receiving and durably writing log events) with zero
business logic, so it does not turn the overall system into a
microservice architecture; it's an auxiliary logging sink, not a decomposed
service boundary for Reclaim's domain functionality.

## 6. Not yet verified by an actual run

This code was written and carefully hand-reviewed against known-stable
ASP.NET Core 8 / Serilog / Logback APIs, but **could not be compiled or run
in the environment this was built in** (no `dotnet` SDK, no network access
to restore NuGet packages or re-verify Maven). Please run
`dotnet restore && dotnet run` and `mvn compile` and report back the first
thing that breaks, if anything — the same way we caught real issues in the
frontend/backend integration earlier.
