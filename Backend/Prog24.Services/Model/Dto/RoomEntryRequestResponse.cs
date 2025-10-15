using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class RoomEntryRequestResponse
    {
        public int Id { get; set; }
        public int StudentId { get; set; }
        public string StudentName { get; set; } = null!;
        public string StudentEmail { get; set; } = null!;
        public int InstructorId { get; set; }
        public string InstructorName { get; set; } = null!;
        public int RoomId { get; set; }
        public string RoomNumber { get; set; } = null!;
        public string BuildingName { get; set; } = null!;
        public int? CourseId { get; set; }
        public string? CourseName { get; set; }
        public DateTime RequestTime { get; set; }
        public string Status { get; set; } = null!;
        public string? Reason { get; set; }
        public DateTime? ResponseTime { get; set; }
    }
}
