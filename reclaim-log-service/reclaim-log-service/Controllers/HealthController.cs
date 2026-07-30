using Microsoft.AspNetCore.Mvc;

namespace Reclaim.LogService.Controllers;

[ApiController]
[Route("health")]
public class HealthController : ControllerBase
{
    [HttpGet]
    public IActionResult Get() => Ok(new { status = "UP", service = "Reclaim.LogService" });
}
