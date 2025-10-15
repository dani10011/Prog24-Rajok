using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Instructor
    {
        [Key]
        [ForeignKey("User")]
        public int User_Id { get; set; }
        [ForeignKey("Faculty")]
        public int Faculty_Id { get; set; }
        [ForeignKey("Department")]
        public int Department_Id { get; set; }

        public User User { get; set; } = null!;
        public Faculty Faculty { get; set; } = null!;
        public Department Department { get; set; } = null!;
        public ICollection<Subject> Subjects { get; set; } = new List<Subject>();
        public ICollection<Course> Courses { get; set; } = new List<Course>();
        public ICollection<RoomEntryRequest> RoomEntryRequests { get; set; } = new List<RoomEntryRequest>();
    }
}
