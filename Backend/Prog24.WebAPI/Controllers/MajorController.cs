using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class MajorController : ControllerBase
    {
        private readonly IMajorService _MajorService;

        public MajorController(IMajorService MajorService)
        {
            _MajorService = MajorService;
        }

        [HttpGet]
        public async Task<IActionResult> GetMajors()
        {
            var Majories = await _MajorService.GetMajors();
            return Ok(Majories);
        }
    }
}
