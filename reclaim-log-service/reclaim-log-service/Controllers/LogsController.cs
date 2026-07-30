using Microsoft.AspNetCore.Mvc;
using Reclaim.LogService.Models;
using Serilog;

namespace Reclaim.LogService.Controllers;

[ApiController]
[Route("api/logs")]
public class LogsController : ControllerBase
{
    /// <summary>
    /// Receives a single log event from the Spring Boot backend and writes
    /// it to the configured Serilog sinks (rolling file + console).
    /// This endpoint intentionally does nothing else — no business logic,
    /// no database access beyond the log file itself.
    /// </summary>
    [HttpPost]
    public IActionResult Ingest([FromBody] LogEntryDto entry)
    {
        if (string.IsNullOrWhiteSpace(entry.Message))
        {
            return BadRequest(new { error = "message is required" });
        }

        var logger = Log.ForContext("SourceApp", entry.Source ?? "unknown")
                         .ForContext("SourceLogger", entry.Logger)
                         .ForContext("SourceThread", entry.Thread)
                         .ForContext("SourceTimestamp", entry.Timestamp);

        switch (entry.Level?.ToUpperInvariant())
        {
            case "ERROR":
                logger.Error("{Message}{NewLine}{Exception}", 
                    entry.Message, Environment.NewLine, entry.Exception);
                break;
            case "WARN":
            case "WARNING":
                logger.Warning(entry.Message);
                break;
            case "DEBUG":
                logger.Debug(entry.Message);
                break;
            case "TRACE":
                logger.Verbose(entry.Message);
                break;
            default:
                logger.Information(entry.Message);
                break;
        }

        return Accepted();
    }

    /// <summary>Batch variant — accepts multiple entries in one call.</summary>
    [HttpPost("batch")]
    public IActionResult IngestBatch([FromBody] List<LogEntryDto> entries)
    {
        foreach (var entry in entries)
        {
            Ingest(entry);
        }
        return Accepted(new { received = entries.Count });
    }
}
