using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class CourseResponse
    {
        public int CourseId { get; set; }
        public string SubjectName { get; set; } = null!;
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public string DayOfWeek { get; set; } = null!;
        public string RoomNumber { get; set; } = null!;
        public string BuildingName { get; set; } = null!;
        public int StudentCount { get; set; }
    }
}
