using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Model.Dto;
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

        [HttpGet("{userId}")]
        public async Task<IActionResult> GetCoursesByUserId(int userId)
        {
            try
            {
                var courses = await _CourseService.GetCoursesByUserId(userId);
                return Ok(courses);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost]
        public async Task<IActionResult> CreateCourse([FromBody] CreateCourseRequest request)
        {
            try
            {
                var response = await _CourseService.CreateCourse(request);
                return Ok(response);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
