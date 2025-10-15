using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class StudentClassLog
    {
        [Key]
        public int Id { get; set; }

        [ForeignKey("Student")]
        public int Student_Id { get; set; }

        [ForeignKey("Course")]
        public int? Course_Id { get; set; }

        [ForeignKey("Room")]
        public int Room_Id { get; set; }

        public DateTime Attempt_Time { get; set; }

        [MaxLength(50)]
        public string Action { get; set; } = null!; // "Entry" or "Exit"

        public bool Success { get; set; }

        [MaxLength(500)]
        public string? Failure_Reason { get; set; }

        [MaxLength(100)]
        public string Nfc_Id_Used { get; set; } = null!;

        public Student Student { get; set; } = null!;
        public Course? Course { get; set; }
        public Room Room { get; set; } = null!;
    }
}
