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
        public DbSet<RoomEntryRequest> Room_entry_request { get; set; }
        public DbSet<Student> Student { get; set; }
        public DbSet<StudentClassAttendance> Student_class_attendance { get; set; }
        public DbSet<StudentClassLog> Student_class_log { get; set; }
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

            modelBuilder.Entity<RoomEntryRequest>()
                .HasOne(r => r.Student)
                .WithMany(s => s.RoomEntryRequests)
                .HasForeignKey(r => r.Student_Id)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<RoomEntryRequest>()
                .HasOne(r => r.Instructor)
                .WithMany(i => i.RoomEntryRequests)
                .HasForeignKey(r => r.Instructor_Id)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<RoomEntryRequest>()
                .HasOne(r => r.Room)
                .WithMany(rm => rm.RoomEntryRequests)
                .HasForeignKey(r => r.Room_Id)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<RoomEntryRequest>()
                .HasOne(r => r.Course)
                .WithMany(c => c.RoomEntryRequests)
                .HasForeignKey(r => r.Course_Id)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<RoomEntryRequest>()
                .HasIndex(r => new { r.Student_Id, r.Request_Time });

            modelBuilder.Entity<RoomEntryRequest>()
                .HasIndex(r => new { r.Instructor_Id, r.Status });

            modelBuilder.Entity<Student>()
                .HasIndex(s => s.Card_Id)
                .IsUnique();

            modelBuilder.Entity<Student>()
                .HasIndex(s => s.Phone_Id)
                .IsUnique();

            modelBuilder.Entity<StudentClassAttendance>()
                .HasIndex(sca => new { sca.Student_Id, sca.Course_Id, sca.Exit_Time });

            modelBuilder.Entity<StudentClassLog>()
                .HasIndex(scl => new { scl.Student_Id, scl.Attempt_Time });

            modelBuilder.Entity<StudentClassLog>()
                .HasIndex(scl => new { scl.Room_Id, scl.Attempt_Time });
        }
    }
}
