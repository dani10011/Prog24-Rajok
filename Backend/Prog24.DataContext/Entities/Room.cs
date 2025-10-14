using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Room
    {
        [Key]
        public int Id { get; set; }
        [ForeignKey("Building")]
        public int Building_Id { get; set; }
        public string Room_Number { get; set; } = null!;
        public int Capacity { get; set; }
        public bool Is_Computer_Room { get; set; }

        public Building Building { get; set; } = null!;
        public ICollection<Course> Courses { get; set; } = new List<Course>();
        public ICollection<Reservation> Reservations { get; set; } = new List<Reservation>();
    }
}
