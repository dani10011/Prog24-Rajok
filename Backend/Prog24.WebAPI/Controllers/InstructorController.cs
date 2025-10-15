using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
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

        [HttpGet]
        public async Task<IActionResult> GetInstructorCourses(int instructorId)
        {
            var courses = await _InstructorService.GetInstructorCourses(instructorId);

            if (courses == null)
            {
                return NotFound(new { message = $"Instructor with ID {instructorId} not found." });
            }

            return Ok(courses);
        }
    }
}
