using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class RegisterRequest
    {
        public int RoleId { get; set; }
        public string Name { get; set; }
        public string NeptunCode { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }        
    }
}
