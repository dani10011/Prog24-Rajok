using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
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
    }
}
