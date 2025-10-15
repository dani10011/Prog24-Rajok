using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class StudentCurrentLectureStatusResponse
    {
        public bool IsInLecture { get; set; }
        public int? LectureId { get; set; }
        public string? CourseName { get; set; }
        public string? InstructorName { get; set; }
        public string? RoomNumber { get; set; }
        public string? BuildingName { get; set; }
        public string? StartTime { get; set; }  // ISO 8601 string
        public string? EndTime { get; set; }    // ISO 8601 string
        public string? AttendanceStatus { get; set; }  // "Present", "Absent", "Late", "Pending"
        public string? Message { get; set; }
    }
}
