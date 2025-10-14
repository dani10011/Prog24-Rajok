using Microsoft.AspNetCore.Mvc;
using Prog24.DataContext.Entities;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class FacultyController : Controller
    {
        private readonly IFacultyService _facultyService;

        public FacultyController(IFacultyService facultyService)
        {
            _facultyService = facultyService;
        }

        [HttpGet]
        public async Task<List<Faculty>> GetFaculties()
        {
             var faculties = await _facultyService.GetFaculties();
             return faculties;
        }

        //public IActionResult Index()
        //{
        //    return View();
        //}
    }
}
