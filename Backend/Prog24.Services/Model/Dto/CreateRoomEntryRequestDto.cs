using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Model.Dto
{
    public class CreateRoomEntryRequestDto
    {
        [Required]
        [MaxLength(100)]
        public string NfcId { get; set; } = null!;

        [Required]
        public int RoomId { get; set; }

        [MaxLength(500)]
        public string? Reason { get; set; }
    }
}
