using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Reservation
    {
        [Key]
        public int Id { get; set; }
        [ForeignKey("Room")]
        public int Room_Id { get; set; }
        public DateTime Start_Time { get; set; }
        public DateTime End_Time { get; set; }
        [ForeignKey("Event")]
        public int? Event_Id { get; set; }
        [ForeignKey("Course")]
        public int? Course_Id { get; set; }

        public Room Room { get; set; } = null!;
        public Event? Event { get; set; }
        public Course? Course { get; set; }
    }
}
