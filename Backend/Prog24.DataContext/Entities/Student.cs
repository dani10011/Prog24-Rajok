using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Student
    {
        [Key]
        [ForeignKey("User")]
        public int User_Id { get; set; }
        public string? Gender { get; set; }
        [ForeignKey("Major")]
        public int Major_Id { get; set; }

        public User User { get; set; } = null!;
        public Major Major { get; set; } = null!;
        public ICollection<CourseStudent> CourseStudents { get; set; } = new List<CourseStudent>();
        public ICollection<SubjectStudent> SubjectStudents { get; set; } = new List<SubjectStudent>();
    }
}
