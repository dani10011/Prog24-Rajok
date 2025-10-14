using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class TimetableResponse
    {
        public int UserId { get; set; }
        public string UserName { get; set; } = null!;
        public string UserRole { get; set; } = null!;
        public List<TimetableItemResponse> TimetableItems { get; set; } = new List<TimetableItemResponse>();
    }
}
