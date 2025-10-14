using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class InstructorController : ControllerBase
    {
        private readonly IInstructorService _InstructorService;

        public InstructorController(IInstructorService InstructorService)
        {
            _InstructorService = InstructorService;
        }

        [HttpGet]
        public async Task<IActionResult> GetInstructors()
        {
            var Instructories = await _InstructorService.GetInstructors();
            return Ok(Instructories);
        }
    }
}
