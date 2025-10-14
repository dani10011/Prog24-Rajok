using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    //[Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class UserController : ControllerBase
    {
        private readonly IUserService _UserService;

        public UserController(IUserService UserService)
        {
            _UserService = UserService;
        }

        [HttpGet]
        public async Task<IActionResult> GetUsers()
        {
            var Users = await _UserService.GetUsers();
            return Ok(Users);
        }
    }
}
