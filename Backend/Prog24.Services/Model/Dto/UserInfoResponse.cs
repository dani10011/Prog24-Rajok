using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class UserInfoResponse
    {
        public int Id { get; set; }
        public string Name { get; set; } = null!;
        public int RoleId { get; set; }
        public string RoleName { get; set; } = null!;
        public string Email { get; set; } = null!;
    }
}
