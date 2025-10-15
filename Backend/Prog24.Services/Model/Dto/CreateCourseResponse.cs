using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class CreateCourseResponse
    {
        public int CourseId { get; set; }
        public int SubjectId { get; set; }
        public string SubjectName { get; set; } = null!;
        public int InstructorId { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public int RoomId { get; set; }
        public string RoomNumber { get; set; } = null!;
        public string BuildingName { get; set; } = null!;
        public string Message { get; set; } = null!;
    }
}
