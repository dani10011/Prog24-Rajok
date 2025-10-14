using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class SubjectStudentController : ControllerBase
    {
        private readonly ISubjectStudentService _SubjectStudentService;

        public SubjectStudentController(ISubjectStudentService SubjectStudentService)
        {
            _SubjectStudentService = SubjectStudentService;
        }

        [HttpGet]
        public async Task<IActionResult> GetSubjectStudents()
        {
            var SubjectStudents = await _SubjectStudentService.GetSubjectStudents();
            return Ok(SubjectStudents);
        }
    }
}
