using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class EventController : ControllerBase
    {
        private readonly IEventService _EventService;

        public EventController(IEventService EventService)
        {
            _EventService = EventService;
        }

        [HttpGet]
        public async Task<IActionResult> GetEvents()
        {
            var Eventies = await _EventService.GetEvents();
            return Ok(Eventies);
        }
    }
}
