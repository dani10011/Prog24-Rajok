using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Subject
    {
        [Key]
        public int Id { get; set; }
        public string Name { get; set; } = null!;
        [ForeignKey("Instructor")]
        public int Instructor_Id { get; set; }
        public Instructor Instructor { get; set; } = null!;

        public ICollection<Course> Courses { get; set; } = new List<Course>();
        public ICollection<SubjectStudent> SubjectStudents { get; set; } = new List<SubjectStudent>();
    }
}
