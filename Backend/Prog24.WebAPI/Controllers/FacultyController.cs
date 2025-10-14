using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.DataContext.Entities;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class FacultyController : ControllerBase
    {
        private readonly IFacultyService _facultyService;

        public FacultyController(IFacultyService facultyService)
        {
            _facultyService = facultyService;
        }

        [HttpGet]
        public async Task<IActionResult> GetFaculties()
        {
             var faculties = await _facultyService.GetFaculties();
             return Ok(faculties);
        }

        //public IActionResult Index()
        //{
        //    return View();
        //}
    }
}
