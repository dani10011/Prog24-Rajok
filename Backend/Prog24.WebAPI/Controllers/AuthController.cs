using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;
        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        [HttpPost]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var response = await _authService.Login(request);
            return Ok(response);
        }
    }
}
