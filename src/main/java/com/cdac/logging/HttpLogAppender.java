package com.cdac.logging;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;

/**
 * Ships every log event to the standalone Reclaim.LogService (.NET) over
 * HTTP, in addition to whatever other appenders (console, file, etc.) are
 * configured.
 *
 * <p>Design goals, in priority order:
 * <ol>
 *   <li><b>Never break the application.</b> If the .NET service is down,
 *       slow, or misconfigured, this appender must not throw, must not
 *       block the caller noticeably, and must not spam its own error
 *       output. Sending is fire-and-forget via {@link HttpClient#sendAsync}.</li>
 *   <li><b>Never cause a logging feedback loop.</b> Failures here are
 *       reported to {@code System.err} directly, never via SLF4J/Logback,
 *       which could otherwise re-enter this same appender.</li>
 *   <li>Keep the payload shape stable and simple — a flat JSON object
 *       matching {@code Reclaim.LogService.Models.LogEntryDto} on the
 *       .NET side.</li>
 * </ol>
 *
 * <p>This appender is normally wrapped in a Logback {@code AsyncAppender}
 * (see logback-spring.xml) so that even the (very small) cost of building
 * the JSON payload and handing it to the HTTP client happens off the
 * request-handling thread.
 */
public class HttpLogAppender extends AppenderBase<ILoggingEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** How long to wait for the .NET service to accept the request before giving up. */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    // --- Configurable via logback-spring.xml <appender> child elements ---
    private String url;
    private String apiKey;
    private String sourceName = "reclaim-backend";

    // Simple noisy-failure guard: after the first delivery failure we stop
    // printing to System.err for the rest of this appender's lifetime, so a
    // fully offline log service doesn't flood stderr on every log line.
    private volatile boolean failureAlreadyReported = false;

    @Override
    public void start() {
        if (url == null || url.isBlank()) {
            addError("HttpLogAppender: 'url' is not configured — appender will not start.");
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }

        try {
            String json = MAPPER.writeValueAsString(buildPayload(event));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));

            if (apiKey != null && !apiKey.isBlank()) {
                requestBuilder.header("X-Api-Key", apiKey);
            }

            httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.discarding())
            .exceptionally(ex -> {
                reportDeliveryFailure(ex);
                return null;
            });

        } catch (Exception ex) {
            reportDeliveryFailure(ex);
        }
    }

    private Map<String, Object> buildPayload(ILoggingEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.ofEpochMilli(event.getTimeStamp()).toString());
        payload.put("level", event.getLevel().toString());
        payload.put("logger", event.getLoggerName());
        payload.put("message", event.getFormattedMessage());
        payload.put("thread", event.getThreadName());
        payload.put("source", sourceName);

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            payload.put("exception", ThrowableProxyUtil.asString(throwableProxy));
        }

        return payload;
    }

    private Void reportDeliveryFailure(Throwable ex) {
        if (!failureAlreadyReported) {
            failureAlreadyReported = true;
            System.err.println("[HttpLogAppender] Could not reach log service at " + url
                    + " (" + ex.getClass().getSimpleName() + ": " + ex.getMessage()
                    + "). Further delivery failures will be suppressed for this run.");
        }
        return null;
    }

    // --- Setters used by Logback when parsing logback-spring.xml ---

    public void setUrl(String url) {
        this.url = url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}