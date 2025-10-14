using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class CourseStudentController : ControllerBase
    {
        private readonly ICourseStudentService _CourseStudentService;

        public CourseStudentController(ICourseStudentService CourseStudentService)
        {
            _CourseStudentService = CourseStudentService;
        }

        [HttpGet]
        public async Task<IActionResult> GetCourseStudents()
        {
            var CourseStudenties = await _CourseStudentService.GetCourseStudents();
            return Ok(CourseStudenties);
        }
    }
}
