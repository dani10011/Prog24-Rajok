using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class DepartmentController : ControllerBase
    {
        private readonly IDepartmentService _DepartmentService;

        public DepartmentController(IDepartmentService DepartmentService)
        {
            _DepartmentService = DepartmentService;
        }

        [HttpGet]
        public async Task<IActionResult> GetDepartments()
        {
            var Departmenties = await _DepartmentService.GetDepartments();
            return Ok(Departmenties);
        }
    }
}
