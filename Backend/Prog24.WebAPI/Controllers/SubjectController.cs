using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class SubjectController : ControllerBase
    {
        private readonly ISubjectService _SubjectService;

        public SubjectController(ISubjectService SubjectService)
        {
            _SubjectService = SubjectService;
        }

        [HttpGet]
        public async Task<IActionResult> GetSubjects()
        {
            var Subjects = await _SubjectService.GetSubjects();
            return Ok(Subjects);
        }
    }
}
