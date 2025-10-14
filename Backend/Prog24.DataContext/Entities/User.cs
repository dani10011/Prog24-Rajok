using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class User
    {
        [Key]
        public int Id { get; set; }
        [ForeignKey("Role")]
        public int Role_Id { get; set; }
        public string Name { get; set; } = null!;
        public string Neptun_Code { get; set; } = null!;
        public string Email { get; set; } = null!;
        public string Password_Hash { get; set; } = null!;

        public Role Role { get; set; } = null!;
        public Instructor? Instructor { get; set; }
        public Student? Student { get; set; }
    }
}
