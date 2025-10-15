using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class UpdateRoomEntryRequestDto
    {
        [Required]
        public int RequestId { get; set; }

        [Required]
        [RegularExpression("^(Approved|Denied)$", ErrorMessage = "Status must be either 'Approved' or 'Denied'")]
        public string Status { get; set; } = null!;
    }
}
