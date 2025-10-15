using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class StudentInstructorResponse
    {
        public int InstructorId { get; set; }
        public string InstructorName { get; set; } = null!;
        public List<InstructorCourseInfo> Courses { get; set; } = new List<InstructorCourseInfo>();
    }

    public class InstructorCourseInfo
    {
        public int CourseId { get; set; }
        public string SubjectName { get; set; } = null!;
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public string RoomNumber { get; set; } = null!;
    }
}
