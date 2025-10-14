using Microsoft.EntityFrameworkCore;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services
{
    public class TimetableService : ITimetableService
    {
        private readonly AppDbContext _dbContext;

        public TimetableService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<TimetableResponse?> GetUserTimetable(int userId)
        {
            // Get user with role, student, and instructor data
            var user = await _dbContext.User
                .Include(u => u.Role)
                .Include(u => u.Student)
                .Include(u => u.Instructor)
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return null;
            }

            var timetableItems = new List<TimetableItemResponse>();

            // Check if user is a student
            if (user.Student != null)
            {
                // Get courses for student through CourseStudent relationship
                var studentCourses = await _dbContext.Course_student
                    .Where(cs => cs.Student_Id == userId)
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.Subject)
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.Room)
                            .ThenInclude(r => r.Building)
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.Instructor)
                            .ThenInclude(i => i.User)
                    .Select(cs => cs.Course)
                    .ToListAsync();

                timetableItems = studentCourses.Select(course => new TimetableItemResponse
                {
                    CourseId = course.Id,
                    SubjectName = course.Subject.Name,
                    StartTime = course.Start_Time,
                    EndTime = course.End_Time,
                    DayOfWeek = course.Start_Time.DayOfWeek.ToString(),
                    RoomNumber = course.Room.Room_Number,
                    BuildingName = course.Room.Building.Name,
                    InstructorName = course.Instructor.User.Name,
                    StudentCount = null
                }).OrderBy(t => t.StartTime).ToList();
            }
            // Check if user is an instructor
            else if (user.Instructor != null)
            {
                // Get courses taught by instructor
                var instructorCourses = await _dbContext.Course
                    .Where(c => c.Instructor_Id == userId)
                    .Include(c => c.Subject)
                    .Include(c => c.Room)
                        .ThenInclude(r => r.Building)
                    .Include(c => c.CourseStudents)
                    .ToListAsync();

                timetableItems = instructorCourses.Select(course => new TimetableItemResponse
                {
                    CourseId = course.Id,
                    SubjectName = course.Subject.Name,
                    StartTime = course.Start_Time,
                    EndTime = course.End_Time,
                    DayOfWeek = course.Start_Time.DayOfWeek.ToString(),
                    RoomNumber = course.Room.Room_Number,
                    BuildingName = course.Room.Building.Name,
                    InstructorName = null,
                    StudentCount = course.CourseStudents.Count
                }).OrderBy(t => t.StartTime).ToList();
            }

            var response = new TimetableResponse
            {
                UserId = user.Id,
                UserName = user.Name,
                UserRole = user.Role.Role_Name,
                TimetableItems = timetableItems
            };

            return response;
        }
    }
}
