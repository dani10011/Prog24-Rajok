using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class CourseStudent
    {
        [ForeignKey("Course")]
        public int Course_Id { get; set; }
        [ForeignKey("Student")]
        public int Student_Id { get; set; }
        public DateTime Enrolled_On { get; set; }

        public Course Course { get; set; } = null!;
        public Student Student { get; set; } = null!;
    }
}
