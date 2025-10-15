using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class ReservationController : ControllerBase
    {
        private readonly IReservationService _ReservationService;

        public ReservationController(IReservationService ReservationService)
        {
            _ReservationService = ReservationService;
        }

        [HttpGet]
        public async Task<IActionResult> GetReservations()
        {
            var Reservationies = await _ReservationService.GetReservations();
            return Ok(Reservationies);
        }
    }
}
