using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class CourseController : ControllerBase
    {
        private readonly ICourseService _CourseService;

        public CourseController(ICourseService CourseService)
        {
            _CourseService = CourseService;
        }

        [HttpGet]
        public async Task<IActionResult> GetCourses()
        {
            var Courseies = await _CourseService.GetCourses();
            return Ok(Courseies);
        }
    }
}
