namespace Reclaim.LogService.Models;

/// <summary>
/// Shape of a single log event received from the Java/Spring Boot backend.
/// Field names are camelCase to match the JSON produced by HttpLogAppender.java.
/// </summary>
public class LogEntryDto
{
    /// <summary>ISO-8601 timestamp of when the log event was created in the source app.</summary>
    public string Timestamp { get; set; } = string.Empty;

    /// <summary>Log level: TRACE, DEBUG, INFO, WARN, or ERROR.</summary>
    public string Level { get; set; } = string.Empty;

    /// <summary>The originating logger name (usually the fully-qualified Java class name).</summary>
    public string Logger { get; set; } = string.Empty;

    /// <summary>The log message text.</summary>
    public string Message { get; set; } = string.Empty;

    /// <summary>Name of the thread that produced the log event.</summary>
    public string? Thread { get; set; }

    /// <summary>Stack trace text, present only for ERROR events with an exception.</summary>
    public string? Exception { get; set; }

    /// <summary>Fixed identifier of the source application, e.g. "reclaim-backend".</summary>
    public string? Source { get; set; }
}
