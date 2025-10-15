using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class StudentCurrentLectureStatusResponse
    {
        public int? CourseId { get; set; }
        public string? SubjectName { get; set; }
        public string? RoomNumber { get; set; }
        public string? BuildingName { get; set; }
        public string? InstructorName { get; set; }
        public DateTime? StartTime { get; set; }
        public DateTime? EndTime { get; set; }
        public bool IsInAttendance { get; set; }
        public DateTime? EntryTime { get; set; }
        public bool HasPendingRequest { get; set; }
        public int? PendingRequestId { get; set; }
        public string? RequestReason { get; set; }
    }
}
