using Microsoft.EntityFrameworkCore;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
using Prog24.Services.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services
{
    public class ReservationService : IReservationService
    {
        private readonly AppDbContext _dbContext;
        public ReservationService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<List<Reservation>> GetReservations()
        {
            var result = await _dbContext.Reservation.ToListAsync();
            return result;
        }
    }
}
