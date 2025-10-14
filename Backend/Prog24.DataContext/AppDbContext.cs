using Microsoft.EntityFrameworkCore;
using Prog24.DataContext.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.DataContext
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
        {
        }

        // Define your DbSets here
        public DbSet<Faculty> Faculty { get; set; }
        // public DbSet<YourEntity> YourEntities { get; set; }
    }
}
