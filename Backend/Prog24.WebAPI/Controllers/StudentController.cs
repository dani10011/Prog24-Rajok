using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class StudentController : ControllerBase
    {
        private readonly IStudentService _StudentService;

        public StudentController(IStudentService StudentService)
        {
            _StudentService = StudentService;
        }

        [HttpGet]
        public async Task<IActionResult> GetStudents()
        {
            var Students = await _StudentService.GetStudents();
            return Ok(Students);
        }

        [HttpGet("{studentUserId}")]
        public async Task<IActionResult> GetStudentInstructors(int studentUserId)
        {
            var instructors = await _StudentService.GetStudentInstructors(studentUserId);
            return Ok(instructors);
        }

        [HttpGet("{studentUserId}")]
        public async Task<IActionResult> GetStudentCourseLocations(int studentUserId)
        {
            var courseLocations = await _StudentService.GetStudentCourseLocations(studentUserId);
            return Ok(courseLocations);
        }

        [HttpGet("{studentUserId}")]
        public async Task<IActionResult> GetCurrentLectureStatus(int studentUserId)
        {
            var status = await _StudentService.GetCurrentLectureStatus(studentUserId);
            return Ok(status);
        }
    }
}
