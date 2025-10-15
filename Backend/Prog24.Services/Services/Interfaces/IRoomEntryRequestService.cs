using Prog24.Services.Model.Dto;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services.Interfaces
{
    public interface IRoomEntryRequestService
    {
        Task<RoomEntryRequestResponse> CreateRequest(CreateRoomEntryRequestDto request);
        Task<RoomEntryRequestResponse> UpdateRequestStatus(UpdateRoomEntryRequestDto update);
        Task<RoomEntryRequestResponse> ApproveStudentEntry(ApproveStudentEntryDto dto);
        Task<List<RoomEntryRequestResponse>> GetRequestsByInstructor(int instructorId, string? status = null);
        Task<List<RoomEntryRequestResponse>> GetRequestsByStudent(int studentId);
        Task<RoomEntryRequestResponse?> GetRequestById(int requestId);
        Task<List<RoomEntryRequestResponse>> GetPendingRequestsByRoom(int roomId);
        Task<List<RoomEntryRequestResponse>> GetPendingRequestsForOngoingLecture(int instructorId);
        Task<int> ExpireOldRequests(int expirationHours = 24);
    }
}
