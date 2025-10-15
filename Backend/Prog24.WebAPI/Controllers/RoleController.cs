using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class RoleController : ControllerBase
    {
        private readonly IRoleService _RoleService;

        public RoleController(IRoleService RoleService)
        {
            _RoleService = RoleService;
        }

        [HttpGet]
        public async Task<IActionResult> GetRoles()
        {
            var Roles = await _RoleService.GetRoles();
            return Ok(Roles);
        }
    }
}
