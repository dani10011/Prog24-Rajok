using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class ApproveStudentEntryDto
    {
        [Required]
        public int InstructorId { get; set; }

        [Required]
        public int StudentId { get; set; }

        [Required]
        public bool IsApproved { get; set; }
    }
}
