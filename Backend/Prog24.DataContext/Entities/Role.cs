using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext.Entities
{
    public class Role
    {
        [Key]
        public int Id { get; set; }
        public string Role_Name { get; set; } = null!;

        public ICollection<User> Users { get; set; } = new List<User>();
    }
}
