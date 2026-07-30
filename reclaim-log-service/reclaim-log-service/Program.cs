using Microsoft.OpenApi.Models;
using Reclaim.LogService.Middleware;
using Serilog;
using Serilog.Events;

var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------------------
// Serilog: writes every ingested log event to console + a daily rolling
// file under ./logs/reclaim-backend-.log. This IS the ".NET logger" —
// the Spring Boot app forwards its log events here over HTTP, and this
// service is responsible only for durably recording them.
// ---------------------------------------------------------------------
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
.MinimumLevel.Override("Microsoft", LogEventLevel.Warning)
.MinimumLevel.Override("System", LogEventLevel.Warning)
    .Enrich.FromLogContext()
    .WriteTo.Console(
        outputTemplate:
        "[{Timestamp:HH:mm:ss} {Level:u3}] ({Properties:j})" +
        " {Message:lj}{NewLine}{Exception}")
    .WriteTo.File(
        path: "logs/reclaim-backend-.log",
        rollingInterval: RollingInterval.Day,
        retainedFileCountLimit: 30,
       outputTemplate:
"{Timestamp:yyyy-MM-dd HH:mm:ss.fff zzz} [{Level:u3}] " + "[{SourceContext}] " +
"{Message:lj} " + "[App:{SourceApp}] " + "[Logger:{SourceLogger}] " +
"[Thread:{SourceThread}]" + "{NewLine}{Exception}"
)
    .CreateLogger();

builder.Host.UseSerilog();

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Reclaim Log Service API",
        Version = "v1",
        Description = "Logging service for Reclaim Spring Boot backend."
    });

    options.AddSecurityDefinition("ApiKey", new OpenApiSecurityScheme
    {
        Description = "Enter API Key",
        Type = SecuritySchemeType.ApiKey,
        Name = "X-Api-Key",
        In = ParameterLocation.Header,
        Scheme = "ApiKeyScheme"
    });

    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "ApiKey"
                }
            },
            Array.Empty<string>()
        }
    });
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Shared-secret check on every request except /health (see ApiKeyMiddleware).
app.UseMiddleware<ApiKeyMiddleware>();

app.MapControllers();

Log.Information(
    "Reclaim.LogService started at {Url}",
    builder.Configuration["Kestrel:Endpoints:Http:Url"]);

app.Run();
