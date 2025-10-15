using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Course
    {
        [Key]
        public int Id { get; set; }
        [ForeignKey("Subject")]
        public int Subject_Id { get; set; }
        [ForeignKey("Instructor")]
        public int Instructor_Id { get; set; }
        public DateTime Start_Time { get; set; }
        public DateTime End_Time { get; set; }
        [ForeignKey("Room")]
        public int Room_Id { get; set; }

        public Subject Subject { get; set; } = null!;
        public Instructor Instructor { get; set; } = null!;
        public Room Room { get; set; } = null!;
        public ICollection<Reservation> Reservations { get; set; } = new List<Reservation>();
        public ICollection<CourseStudent> CourseStudents { get; set; } = new List<CourseStudent>();
        public ICollection<RoomEntryRequest> RoomEntryRequests { get; set; } = new List<RoomEntryRequest>();
    }
}
