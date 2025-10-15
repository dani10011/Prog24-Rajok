using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;

namespace Prog24.WebAPI.Controllers
{
    [Authorize]
    [ApiController]
    [Route("api/[controller]/[action]")]
    public class RoomEntryRequestController : ControllerBase
    {
        private readonly IRoomEntryRequestService _roomEntryRequestService;

        public RoomEntryRequestController(IRoomEntryRequestService roomEntryRequestService)
        {
            _roomEntryRequestService = roomEntryRequestService;
        }

        /// <summary>
        /// Create a new room entry request (Student endpoint)
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> CreateRequest([FromBody] CreateRoomEntryRequestDto request)
        {
            try
            {
                var result = await _roomEntryRequestService.CreateRequest(request);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Update room entry request status (Instructor endpoint)
        /// </summary>
        [HttpPut]
        public async Task<IActionResult> UpdateRequestStatus([FromBody] UpdateRoomEntryRequestDto update)
        {
            try
            {
                var result = await _roomEntryRequestService.UpdateRequestStatus(update);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Approve or deny student entry by instructor for their ongoing lecture (Teacher endpoint)
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> ApproveStudentEntry([FromBody] ApproveStudentEntryDto dto)
        {
            try
            {
                var result = await _roomEntryRequestService.ApproveStudentEntry(dto);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Get all requests for a specific instructor, optionally filtered by status
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetRequestsByInstructor([FromQuery] int instructorId, [FromQuery] string? status = null)
        {
            try
            {
                var requests = await _roomEntryRequestService.GetRequestsByInstructor(instructorId, status);
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Get all requests for a specific student
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetRequestsByStudent([FromQuery] int studentId)
        {
            try
            {
                var requests = await _roomEntryRequestService.GetRequestsByStudent(studentId);
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Get a specific request by ID
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetRequestById([FromQuery] int requestId)
        {
            try
            {
                var request = await _roomEntryRequestService.GetRequestById(requestId);
                if (request == null)
                {
                    return NotFound(new { message = "Request not found" });
                }
                return Ok(request);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Get all pending requests for a specific room
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetPendingRequestsByRoom([FromQuery] int roomId)
        {
            try
            {
                var requests = await _roomEntryRequestService.GetPendingRequestsByRoom(roomId);
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Get all pending requests for the instructor's ongoing lecture
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetPendingRequestsForOngoingLecture([FromQuery] int instructorId)
        {
            try
            {
                var requests = await _roomEntryRequestService.GetPendingRequestsForOngoingLecture(instructorId);
                return Ok(requests);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Expire old pending requests (Admin/System endpoint)
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> ExpireOldRequests([FromQuery] int expirationHours = 24)
        {
            try
            {
                var count = await _roomEntryRequestService.ExpireOldRequests(expirationHours);
                return Ok(new { message = $"{count} requests expired", expiredCount = count });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
