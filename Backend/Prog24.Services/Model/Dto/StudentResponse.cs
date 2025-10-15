using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class StudentResponse
    {
        public int UserId { get; set; }
        public string Name { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string? Gender { get; set; }
        public int MajorId { get; set; }
        public string MajorName { get; set; } = null!;
    }
}
