using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class CreateCourseRequest
    {
        public int SubjectId { get; set; }
        public int InstructorId { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public int RoomId { get; set; }
        public string? NameSuffix { get; set; } = "ZH"; // Defaults to "ZH" for tests
    }
}
