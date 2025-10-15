using Microsoft.EntityFrameworkCore;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services
{
    public class RoomEntryRequestService : IRoomEntryRequestService
    {
        private readonly AppDbContext _dbContext;

        public RoomEntryRequestService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<RoomEntryRequestResponse> CreateRequest(CreateRoomEntryRequestDto request)
        {
            var currentTime = DateTime.UtcNow;
            string? failureReason = null;

            // Look up student by NFC ID (either Card_Id or Phone_Id)
            var student = await _dbContext.Student
                .FirstOrDefaultAsync(s => s.Card_Id == request.NfcId || s.Phone_Id == request.NfcId);

            if (student == null)
            {
                failureReason = "Student not found with provided NFC ID";
                // Log failed attempt with null student
                await LogAttempt(0, null, request.RoomId, currentTime, "Entry", false, failureReason, request.NfcId);
                throw new Exception(failureReason);
            }

            // Find the active course in the specified room
            var activeCourse = await _dbContext.Course
                .Where(c => c.Room_Id == request.RoomId
                    && c.Start_Time <= currentTime
                    && c.End_Time >= currentTime)
                .FirstOrDefaultAsync();

            if (activeCourse == null)
            {
                failureReason = "No active course found in the specified room at this time";
                await LogAttempt(student.User_Id, null, request.RoomId, currentTime, "Entry", false, failureReason, request.NfcId);
                throw new Exception(failureReason);
            }

            // Check if student is already in the class
            var existingAttendance = await _dbContext.Student_class_attendance
                .FirstOrDefaultAsync(sca => sca.Student_Id == student.User_Id
                    && sca.Course_Id == activeCourse.Id
                    && sca.Exit_Time == null);

            if (existingAttendance != null)
            {
                failureReason = "Student is already checked into this class";
                await LogAttempt(student.User_Id, activeCourse.Id, request.RoomId, currentTime, "Entry", false, failureReason, request.NfcId);
                throw new Exception(failureReason);
            }

            // Create attendance record
            var attendance = new StudentClassAttendance
            {
                Student_Id = student.User_Id,
                Course_Id = activeCourse.Id,
                Room_Id = request.RoomId,
                Entry_Time = currentTime,
                Exit_Time = null
            };

            _dbContext.Student_class_attendance.Add(attendance);

            // Create room entry request
            var roomEntryRequest = new RoomEntryRequest
            {
                Student_Id = student.User_Id,
                Instructor_Id = activeCourse.Instructor_Id,
                Room_Id = request.RoomId,
                Course_Id = activeCourse.Id,
                Request_Time = currentTime,
                Status = "Pending",
                Reason = request.Reason
            };

            _dbContext.Room_entry_request.Add(roomEntryRequest);

            // Log successful attempt
            await LogAttempt(student.User_Id, activeCourse.Id, request.RoomId, currentTime, "Entry", true, null, request.NfcId);

            await _dbContext.SaveChangesAsync();

            return await GetRequestById(roomEntryRequest.Id)
                ?? throw new Exception("Failed to retrieve created request");
        }

        private async Task LogAttempt(int studentId, int? courseId, int roomId, DateTime attemptTime, string action, bool success, string? failureReason, string nfcIdUsed)
        {
            var log = new StudentClassLog
            {
                Student_Id = studentId,
                Course_Id = courseId,
                Room_Id = roomId,
                Attempt_Time = attemptTime,
                Action = action,
                Success = success,
                Failure_Reason = failureReason,
                Nfc_Id_Used = nfcIdUsed
            };

            _dbContext.Student_class_log.Add(log);
        }

        public async Task<RoomEntryRequestResponse> UpdateRequestStatus(UpdateRoomEntryRequestDto update)
        {
            var request = await _dbContext.Room_entry_request
                .FirstOrDefaultAsync(r => r.Id == update.RequestId);

            if (request == null)
            {
                throw new Exception("Request not found");
            }

            if (request.Status != "Pending")
            {
                throw new Exception("Only pending requests can be updated");
            }

            request.Status = update.Status;
            request.Response_Time = DateTime.UtcNow;

            await _dbContext.SaveChangesAsync();

            return await GetRequestById(request.Id)
                ?? throw new Exception("Failed to retrieve updated request");
        }

        public async Task<List<RoomEntryRequestResponse>> GetRequestsByInstructor(int instructorId, string? status = null)
        {
            var query = _dbContext.Room_entry_request
                .Include(r => r.Student)
                    .ThenInclude(s => s.User)
                .Include(r => r.Instructor)
                    .ThenInclude(i => i.User)
                .Include(r => r.Room)
                    .ThenInclude(rm => rm.Building)
                .Include(r => r.Course)
                    .ThenInclude(c => c.Subject)
                .Where(r => r.Instructor_Id == instructorId);

            if (!string.IsNullOrEmpty(status))
            {
                query = query.Where(r => r.Status == status);
            }

            var requests = await query
                .OrderByDescending(r => r.Request_Time)
                .ToListAsync();

            return requests.Select(MapToResponse).ToList();
        }

        public async Task<List<RoomEntryRequestResponse>> GetRequestsByStudent(int studentId)
        {
            var requests = await _dbContext.Room_entry_request
                .Include(r => r.Student)
                    .ThenInclude(s => s.User)
                .Include(r => r.Instructor)
                    .ThenInclude(i => i.User)
                .Include(r => r.Room)
                    .ThenInclude(rm => rm.Building)
                .Include(r => r.Course)
                    .ThenInclude(c => c.Subject)
                .Where(r => r.Student_Id == studentId)
                .OrderByDescending(r => r.Request_Time)
                .ToListAsync();

            return requests.Select(MapToResponse).ToList();
        }

        public async Task<RoomEntryRequestResponse?> GetRequestById(int requestId)
        {
            var request = await _dbContext.Room_entry_request
                .Include(r => r.Student)
                    .ThenInclude(s => s.User)
                .Include(r => r.Instructor)
                    .ThenInclude(i => i.User)
                .Include(r => r.Room)
                    .ThenInclude(rm => rm.Building)
                .Include(r => r.Course)
                    .ThenInclude(c => c.Subject)
                .FirstOrDefaultAsync(r => r.Id == requestId);

            return request == null ? null : MapToResponse(request);
        }

        public async Task<List<RoomEntryRequestResponse>> GetPendingRequestsByRoom(int roomId)
        {
            var requests = await _dbContext.Room_entry_request
                .Include(r => r.Student)
                    .ThenInclude(s => s.User)
                .Include(r => r.Instructor)
                    .ThenInclude(i => i.User)
                .Include(r => r.Room)
                    .ThenInclude(rm => rm.Building)
                .Include(r => r.Course)
                    .ThenInclude(c => c.Subject)
                .Where(r => r.Room_Id == roomId && r.Status == "Pending")
                .OrderByDescending(r => r.Request_Time)
                .ToListAsync();

            return requests.Select(MapToResponse).ToList();
        }

        public async Task<int> ExpireOldRequests(int expirationHours = 24)
        {
            var expirationTime = DateTime.UtcNow.AddHours(-expirationHours);

            var expiredRequests = await _dbContext.Room_entry_request
                .Where(r => r.Status == "Pending" && r.Request_Time < expirationTime)
                .ToListAsync();

            foreach (var request in expiredRequests)
            {
                request.Status = "Expired";
            }

            await _dbContext.SaveChangesAsync();

            return expiredRequests.Count;
        }

        private RoomEntryRequestResponse MapToResponse(RoomEntryRequest request)
        {
            return new RoomEntryRequestResponse
            {
                Id = request.Id,
                StudentId = request.Student_Id,
                StudentName = request.Student.User.Name,
                StudentEmail = request.Student.User.Email,
                InstructorId = request.Instructor_Id,
                InstructorName = request.Instructor.User.Name,
                RoomId = request.Room_Id,
                RoomNumber = request.Room.Room_Number,
                BuildingName = request.Room.Building.Name,
                CourseId = request.Course_Id,
                CourseName = request.Course?.Subject?.Name,
                RequestTime = request.Request_Time,
                Status = request.Status,
                Reason = request.Reason,
                ResponseTime = request.Response_Time
            };
        }
    }
}
