using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{

    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class BuildingController : ControllerBase
    {
        private readonly IBuildingService _BuildingService;

        public BuildingController(IBuildingService BuildingService)
        {
            _BuildingService = BuildingService;
        }

        [HttpGet]
        public async Task<IActionResult> GetBuildings()
        {
            var Buildingies = await _BuildingService.GetBuildings();
            return Ok(Buildingies);
        }
    }
    
}
