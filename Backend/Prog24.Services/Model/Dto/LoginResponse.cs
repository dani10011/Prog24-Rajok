using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class LoginResponse
    {
        public int UserId { get; set; }
        public string Token { get; set; }
        public string? PhoneId { get; set; }
    }
}
