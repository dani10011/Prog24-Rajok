using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Department
    {
        [Key]
        public int Id { get; set; }
        public string Name { get; set; } = null!;
        [ForeignKey("Faculty")]
        public int Faculty_Id { get; set; }

        public Faculty Faculty { get; set; } = null!;
        public ICollection<Instructor> Instructors { get; set; } = new List<Instructor>();
    }
}
