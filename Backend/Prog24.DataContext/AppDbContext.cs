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

        public DbSet<Building> Building { get; set; }
        public DbSet<Course> Course { get; set; }
        public DbSet<CourseStudent> Course_student { get; set; }
        public DbSet<Department> Department { get; set; }
        public DbSet<Event> Event { get; set; }
        public DbSet<Faculty> Faculty { get; set; }
        public DbSet<Instructor> Instructor { get; set; }
        public DbSet<Major> Major { get; set; }
        public DbSet<Reservation> Reservation { get; set; }
        public DbSet<Role> Role { get; set; }
        public DbSet<Room> Room { get; set; }
        public DbSet<Student> Student { get; set; }
        public DbSet<Subject> Subject { get; set; }
        public DbSet<SubjectStudent> Subject_student { get; set; }
        public DbSet<User> User { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<User>()
        .HasIndex(u => u.Neptun_Code).IsUnique();
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Email).IsUnique();

            modelBuilder.Entity<Instructor>()
                .HasKey(i => i.User_Id);

            modelBuilder.Entity<Student>()
                .HasKey(s => s.User_Id);

            

            modelBuilder.Entity<CourseStudent>()
                .HasKey(cs => new { cs.Course_Id, cs.Student_Id });

            modelBuilder.Entity<SubjectStudent>()
                .HasKey(ss => new { ss.Subject_Id, ss.Student_Id });

            
        }
    }
}
