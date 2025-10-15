using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class TimetableController : ControllerBase
    {
        private readonly ITimetableService _timetableService;

        public TimetableController(ITimetableService timetableService)
        {
            _timetableService = timetableService;
        }

        [HttpGet]
        public async Task<IActionResult> GetUserTimetable(int userId)
        {
            var timetable = await _timetableService.GetUserTimetable(userId);

            if (timetable == null)
            {
                return NotFound(new { message = $"User with ID {userId} not found." });
            }

            return Ok(timetable);
        }
    }
}
