using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class StudentClassAttendance
    {
        [Key]
        public int Id { get; set; }

        [ForeignKey("Student")]
        public int Student_Id { get; set; }

        [ForeignKey("Course")]
        public int Course_Id { get; set; }

        [ForeignKey("Room")]
        public int Room_Id { get; set; }

        public DateTime Entry_Time { get; set; }
        public DateTime? Exit_Time { get; set; }

        public Student Student { get; set; } = null!;
        public Course Course { get; set; } = null!;
        public Room Room { get; set; } = null!;
    }
}
