using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class SubjectStudent
    {
        [ForeignKey("Subject")]
        public int Subject_Id { get; set; }
        [ForeignKey("Student")]
        public int Student_Id { get; set; }
        public Subject Subject { get; set; } = null!;
        public Student Student { get; set; } = null!;
    }
}
