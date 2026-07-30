namespace Reclaim.LogService.Middleware;

/// <summary>
/// Minimal shared-secret check. This service is only ever called
/// server-to-server by the Spring Boot backend, never by a browser,
/// so a static API key header is sufficient here.
/// </summary>
public class ApiKeyMiddleware
{
    private const string HeaderName = "X-Api-Key";

    private readonly RequestDelegate _next;
    private readonly IConfiguration _configuration;

    public ApiKeyMiddleware(RequestDelegate next, IConfiguration configuration)
    {
        _next = next;
        _configuration = configuration;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        // Let health checks through without a key so container/orchestrator
        // probes and quick manual checks don't need the secret.
        if (context.Request.Path.StartsWithSegments("/health"))
        {
            await _next(context);
            return;
        }

        // Allow Swagger during development
        if (context.Request.Path.StartsWithSegments("/swagger"))
        {
            await _next(context);
            return;
        }

        var expectedKey = _configuration["LogService:ApiKey"];

        if (string.IsNullOrEmpty(expectedKey))
        {
            // Fail closed: if no key is configured, refuse all traffic
            // rather than silently accepting unauthenticated log writes.
            context.Response.StatusCode = StatusCodes.Status500InternalServerError;
            await context.Response.WriteAsync("LogService:ApiKey is not configured on the server.");
            return;
        }

        if (!context.Request.Headers.TryGetValue(HeaderName, out var providedKey) ||
            providedKey != expectedKey)
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
            await context.Response.WriteAsync("Missing or invalid X-Api-Key header.");
            return;
        }

        await _next(context);
    }
}
